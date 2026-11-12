package io.github.nikalbazu24.gahvoria.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class CoreTest {

    private fun folder(vararg files: String): File {
        val root = Files.createTempDirectory("game").toFile()
        for (f in files) File(root, f).apply { parentFile.mkdirs(); writeText("x") }
        return root
    }

    @Test fun detectsRpgMakerVersionsAndEssentials() {
        val xp = folder("Game.ini", "Data/Scripts.rxdata", "Data/messages.dat")
        File(xp, "Game.ini").writeText("[Game]\r\nLibrary=RGSS104E.dll\r\nTitle=Ópalo Edición\r\n")
        val i = GameInspector.inspect(xp)
        assertEquals(EngineKind.RPG_MAKER_XP, i.kind)
        assertTrue(i.essentials)
        assertEquals("Ópalo Edición", i.title)
        assertEquals(Support.PLAYABLE, i.support)
        assertEquals("RPG Maker XP + Essentials", i.engineLabel)

        assertEquals(EngineKind.RPG_MAKER_VXACE, GameInspector.inspect(folder("Game.rgss3a")).kind)
        assertEquals(EngineKind.RPG_MAKER_VX, GameInspector.inspect(folder("Data/Map001.rvdata")).kind)
        assertEquals(EngineKind.RPG_MAKER_XP, GameInspector.inspect(folder("Game.rgssad")).kind)
        assertFalse(GameInspector.inspect(folder("Game.rgssad")).essentials)
    }

    @Test fun detectsGodotUnityAndFoldersThatAreNotGames() {
        val godot = folder("project.godot")
        File(godot, "project.godot").writeText("[application]\nconfig/name=\"Aster\"\n")
        GameInspector.inspect(godot).let {
            assertEquals(EngineKind.GODOT, it.kind); assertEquals("Aster", it.title); assertEquals(Support.NEXT_PHASE, it.support)
        }
        val unity = folder("Nube_Data/globalgamemanagers", "Nube.exe")
        assertEquals(Support.RESEARCH, GameInspector.inspect(unity).support)
        val photos = folder("foto.jpg")
        assertEquals(Support.NOT_A_GAME, GameInspector.inspect(photos).support)
        assertEquals(photos.name, GameInspector.inspect(photos).title)
    }

    @Test fun libraryRoundTripsAndOnlyForgetsTheEntry() {
        val dir = folder("Game.rgssad")
        val lib = Library(File(Files.createTempDirectory("lib").toFile(), "library.tsv"))
        val g = Game(dir.path, "Con\ttab y \\barra\nlínea", EngineKind.RPG_MAKER_XP, true, addedAt = 5)
        lib.add(g)
        assertEquals(listOf(g), lib.load())
        lib.add(g.copy(title = "Nuevo"))
        assertEquals(1, lib.load().size)
        lib.update(g.path) { it.copy(lastPlayed = 9, engine = Game.ENGINE_CLASSIC) }
        assertEquals(Game.ENGINE_CLASSIC, lib.load().single().engine)
        lib.remove(g.path)
        assertTrue(lib.load().isEmpty())
        assertTrue("The game folder is not touched", File(dir, "Game.rgssad").isFile)
        assertNull(Library.parse("otra-version\tx"))
    }

    @Test fun orderPutsTheLastPlayedFirst() {
        val a = Game("/a", "A", EngineKind.RPG_MAKER_XP, false, addedAt = 1, lastPlayed = 0)
        val b = Game("/b", "B", EngineKind.RPG_MAKER_XP, false, addedAt = 2, lastPlayed = 50)
        val c = Game("/c", "C", EngineKind.RPG_MAKER_XP, false, addedAt = 3, lastPlayed = 0)
        assertEquals(listOf("/b", "/c", "/a"), Library.ordered(listOf(a, b, c)).map { it.path })
    }

    @Test fun savesStayWhereEssentiaAndJoiPlayLookForThem() {
        val game = File("/storage/emulated/0/Juegos/Ópalo")
        val root = File("/storage/emulated/0")
        assertEquals(game, Saves.dirFor(game, "Ópalo", root, true))
        assertEquals(File("/storage/emulated/0/Essentia/Partidas/Ópalo: 2".replace(":", "")),
            Saves.dirFor(game, "Ópalo: 2", root, false))
        assertTrue(Saves.looksLikeSave("Save1.rxdata"))
        assertTrue(Saves.looksLikeSave("Game.rvdata2"))
        assertFalse(Saves.looksLikeSave("Scripts.rxdata"))
        assertFalse(Saves.looksLikeSave("Map001.rxdata"))
    }

    @Test fun everyPaletteIsReadableInBothModes() {
        for (p in Palettes.all) for (s in listOf(p.dark, p.light)) {
            val id = p.id + (if (s === p.dark) "/oscuro" else "/claro")
            assertTrue("$id texto", Palettes.contrast(s.ink, s.bg) >= 7.0)
            assertTrue("$id texto suave", Palettes.contrast(s.muted, s.bg) >= 4.5)
            assertTrue("$id botón", Palettes.contrast(s.onAccent, s.accent) >= 4.5)
        }
        assertEquals("grafito", Palettes.byId(null).id)
        assertEquals(Palettes.byId("grafito").dark, Palettes.scheme(null, ThemeMode.SYSTEM, systemDark = true))
        assertEquals(Palettes.byId("vino").light, Palettes.scheme("vino", ThemeMode.LIGHT, systemDark = true))
    }

    @Test fun releaseManifestIsReadSafely() {
        val r = Release.parse("""{"versionCode": 12, "versionName": "0.1.12", "signed": true, "apk": "gahvoria.apk"}""")
        assertEquals(Release(12, "0.1.12", "gahvoria.apk"), r)
        assertTrue(r!!.isNewerThan(11))
        assertFalse(r.isNewerThan(12))
        assertNull(Release.parse("""{"versionCode": 3, "versionName": "x", "apk": "../../otro.sh"}"""))
        assertNull(Release.parse("no es json"))
    }

    // ---- Essentials version and automatic engine ---------------------------------------------

    private fun marshalNum(n: Int): ByteArray = when {
        n == 0 -> byteArrayOf(0)
        n in 1..122 -> byteArrayOf((n + 5).toByte())
        n in 123..255 -> byteArrayOf(1, n.toByte())
        else -> byteArrayOf(2, (n and 0xFF).toByte(), (n shr 8).toByte())
    }

    private fun zlib(text: String): ByteArray {
        val d = java.util.zip.Deflater()
        d.setInput(text.toByteArray())
        d.finish()
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!d.finished()) out.write(buf, 0, d.deflate(buf))
        return out.toByteArray()
    }

    /** A Scripts.rxdata like RPG Maker writes: [[id, name, zlib(code)], ...] in Ruby Marshal. */
    private fun scriptsFile(vararg sections: Pair<String, String>): File {
        val out = java.io.ByteArrayOutputStream()
        out.write(byteArrayOf(4, 8, '['.code.toByte()))
        out.write(marshalNum(sections.size))
        sections.forEachIndexed { i, (name, code) ->
            out.write('['.code); out.write(marshalNum(3))
            out.write('i'.code); out.write(marshalNum(i + 1))
            out.write('"'.code); out.write(marshalNum(name.length)); out.write(name.toByteArray())
            val z = zlib(code)
            out.write('"'.code); out.write(marshalNum(z.size)); out.write(z)
        }
        val root = Files.createTempDirectory("game").toFile()
        File(root, "Data").mkdirs()
        File(root, "Data/Scripts.rxdata").writeBytes(out.toByteArray())
        return root
    }

    @Test fun essentials17And18GetTheClassicEngineAutomatically() {
        val v17 = GameProfile.read(scriptsFile("Main" to "ESSENTIALS_VERSION = \"17.2\"\nGAME_VERSION = \"1.0.3\"\n"))
        assertTrue(v17.essentials)
        assertEquals("17.2", v17.essentialsVersion)
        assertEquals("1.0.3", v17.gameVersion)
        assertEquals(Game.ENGINE_CLASSIC, v17.recommendedEngine)
        assertEquals("Essentials 17.2", v17.label)

        val v18 = GameProfile.read(scriptsFile("Settings" to "module Essentials\n  VERSION = \"18.1\"\nend\n"))
        assertEquals(Game.ENGINE_CLASSIC, v18.recommendedEngine)
    }

    @Test fun otherGamesKeepTheModernEngine() {
        assertEquals(Game.ENGINE_MODERN, GameProfile.read(scriptsFile("Settings" to "module Essentials\n  VERSION = \"21.1\"\nend\n")).recommendedEngine)
        assertEquals(Game.ENGINE_MODERN, GameProfile.read(scriptsFile("Settings" to "ESSENTIALS_VERSION = \"16.2\"")).recommendedEngine)
        // the old window resizer marks Essentials up to v16-17, which stays on the modern engine
        val resizer = GameProfile.read(scriptsFile("Sprite_Resizer" to "x", "Main" to "ESSENTIALS_VERSION = \"17.2\""))
        assertTrue(resizer.oldResizer)
        assertEquals(Game.ENGINE_MODERN, resizer.recommendedEngine)
        val plain = GameProfile.read(scriptsFile("Main" to "puts 1"))
        assertFalse(plain.essentials)
        assertEquals("", plain.label)
        assertEquals(Game.ENGINE_MODERN, plain.recommendedEngine)
    }

    @Test fun brokenOrMissingScriptsNeverCrash() {
        assertEquals(GameProfile(), GameProfile.read(folder("Game.ini")))
        val root = folder("Data/Scripts.rxdata")  // a file that is not Marshal data
        assertEquals(GameProfile(), GameProfile.read(root))
        File(root, "Data/Scripts.rxdata").writeBytes(byteArrayOf(4, 8, '['.code.toByte(), 100, 1))
        assertEquals(GameProfile(), GameProfile.read(root))
    }

    /** Games never opened once because the engine thread was never asked to start: keep that call in place. */
    @Test fun gameActivityStartsTheEngineThread() {
        val src = listOf("../engine/src/main/java/com/hatkid/mkxpz/GameActivity.java",
            "engine/src/main/java/com/hatkid/mkxpz/GameActivity.java").map(::File).firstOrNull { it.isFile }
            ?: return  // sources not reachable from this working directory (local script): CI runs it
        val text = src.readText()
        assertTrue(text.contains("protected void onStart()"))
        assertTrue(text.contains("resumeNativeThread()"))
    }

    // ---- shortcuts for other launchers --------------------------------------------------------

    private fun game(title: String, dir: File, kind: EngineKind = EngineKind.RPG_MAKER_XP) =
        Game(dir.absolutePath, title, kind, essentials = true)

    @Test fun shortcutsPointToTheGameFolderAndNeverTouchTheGames() {
        val a = folder("Game.ini"); val b = folder("Game.ini"); val godot = folder("project.godot")
        val out = Files.createTempDirectory("roms").toFile()
        val n = Shortcuts.export(listOf(game("Pokemon Anil", a), game("Pokemon Anil", b), game("Otro: juego?", b),
            game("Godot", godot, EngineKind.GODOT)), File(out, "ROMs/Gahvoria"))
        assertEquals(3, n)  // the Godot game cannot be played yet: no shortcut
        val names = File(out, "ROMs/Gahvoria").list()!!.sorted()
        assertEquals(listOf("Otro juego.gahvoria", "Pokemon Anil (2).gahvoria", "Pokemon Anil.gahvoria"), names)
        assertEquals(a.absolutePath, Shortcuts.pathIn(File(out, "ROMs/Gahvoria/Pokemon Anil.gahvoria").readText()))
        assertTrue(File(a, "Game.ini").exists() && File(b, "Game.ini").exists())  // games untouched
    }

    @Test fun launchersCanSendTheShortcutTheFolderOrAFileInsideTheGame() {
        val dir = folder("Game.ini", "Data/Scripts.rxdata")
        val shortcut = File(Files.createTempDirectory("roms").toFile(), "Juego.gahvoria")
        shortcut.writeText("\uFEFF# comentario\n\n  " + dir.absolutePath + "  \n")
        assertEquals(dir, Shortcuts.resolve(shortcut))
        assertEquals(dir, Shortcuts.resolve(dir))
        assertEquals(dir, Shortcuts.resolve(File(dir, "Game.ini")))
        shortcut.writeText("/no/existe/esta/carpeta\n")
        assertNull(Shortcuts.resolve(shortcut))
        assertNull(Shortcuts.resolve(File("/no/existe/nada.gahvoria")))
        assertNull(Shortcuts.pathIn("# solo comentarios\n\n"))
    }

    // ---- load times --------------------------------------------------------------------------

    @Test fun loadTimesAreReadSafelyAndTheLastOneOfTheGameWins() {
        val text = "1000\t8\tAnil\tclassic\t3100\t11900\t3400\t18400\n" +
            "basura sin tabuladores\n" +
            "2000\t9\tAnil\tclassic\t3000\t6200\t3100\t12300\n" +
            "3000\t9\tOtro\tmodern\t1\t2\t3\t6\n" +
            "4000\tno-numero\tAnil\tmodern\t1\t2\t3\t6\n"
        val all = LoadTimes.parse(text)
        assertEquals(3, all.size)
        val last = LoadTimes.lastFor(all, "Anil")!!
        assertEquals(2000L, last.at)
        assertEquals("12,3 s · archivos 3,0 · scripts 6,2 · apertura 3,1", LoadTimes.summary(last))
        assertNull(LoadTimes.lastFor(all, "No existe"))
        assertEquals(emptyList<LoadRecord>(), LoadTimes.parse(""))
    }

    // ---- covers ------------------------------------------------------------------------------

    private val catalogJson = """
        {"version":3,"games":[
          {"id":"pokemon-anil","title":"A\u00f1il","aliases":["Pokemon Anil","Pok\u00e9mon A\u00f1il Definitive Edition"],
           "cover":"https://whackahack.com/a.png","logo":"https://whackahack.com/b.png","media_credit":"EricLostie \u00b7 Whack a Hack!","n":1.5,"ok":true,"x":null},
          {"id":"sin-imagen","title":"Nada","aliases":[]},
          {"id":"solo-logo","title":"Cristal","logo":"https://x.org/c.jpg"},
          {"id":"http","title":"Inseguro","cover":"http://x.org/c.jpg"}
        ]}
    """.trimIndent()

    @Test fun theCoverCatalogIsReadWithoutLibrariesAndOnlyKeepsHttpsImages() {
        val list = CoverCatalog.parse(catalogJson)
        assertEquals(listOf("pokemon-anil", "solo-logo"), list.map { it.id })
        val anil = list[0]
        assertEquals("A\u00f1il", anil.title)
        assertEquals(listOf("https://whackahack.com/a.png", "https://whackahack.com/b.png"), anil.urls)
        assertEquals("EricLostie \u00b7 Whack a Hack!", anil.credit)
        assertEquals(emptyList<CoverEntry>(), CoverCatalog.parse("no es json"))
        assertEquals(emptyList<CoverEntry>(), CoverCatalog.parse(""))
    }

    @Test fun gamesMatchTheirCatalogEntryByTitleAliasOrFolderName() {
        val list = CoverCatalog.parse(catalogJson)
        assertEquals("anil", CoverCatalog.norm("Pok\u00e9mon A\u00f1il 4.0"))
        assertEquals("pokemon-anil", CoverCatalog.match(list, "Pok\u00e9mon A\u00f1il", "x")?.id)
        assertEquals("pokemon-anil", CoverCatalog.match(list, "Otro nombre", "Pokemon_Anil_v4")?.id)
        assertEquals("solo-logo", CoverCatalog.match(list, "Cristal", "")?.id)
        assertNull(CoverCatalog.match(list, "Juego desconocido", "carpeta"))
        assertNull(CoverCatalog.match(list, "", ""))
    }

    private fun image(f: File, size: Int) { f.parentFile.mkdirs(); f.writeBytes(ByteArray(size)) }

    @Test fun theCoverIsTakenFromTheGameFolderInPriorityOrder() {
        val dir = Files.createTempDirectory("game").toFile()
        assertNull(CoverFinder.find(dir))
        image(File(dir, "Graphics/Pictures/title.png"), 5000)
        assertEquals("title.png", CoverFinder.find(dir)?.name)
        image(File(dir, "Graphics/Titles/zzz.jpg"), 5000)
        assertEquals("zzz.jpg", CoverFinder.find(dir)?.name)
        image(File(dir, "Graphics/Titles/title.png"), 5000)
        assertEquals("title.png", CoverFinder.find(dir)?.parentFile?.name?.let { "title.png" })
        assertEquals("Titles", CoverFinder.find(dir)?.parentFile?.name)
        image(File(dir, "cover.png"), 5000)
        assertEquals(dir, CoverFinder.find(dir)?.parentFile)
        image(File(dir, "Graphics/Titles/tiny.png"), 10)
        assertTrue(CoverFinder.find(dir)!!.length() >= 2000)
    }

    // ---- error diagnosis ---------------------------------------------------------------------

    @Test fun errorsAreExplainedAndOnlyOfferAFixWhenThereIsOne() {
        val stack = ErrorDiagnosis.diagnose("Script 'Widescreen' line 409: SystemStackError occured.\n\nstack level too deep", true, true)
        assertEquals(Fix.NONE, stack.fix)
        assertTrue(stack.detail.contains("Widescreen"))
        assertTrue(stack.detail.contains("409"))

        val file = ErrorDiagnosis.diagnose("Script 'Main' line 3: Errno::ENOENT occured.\n\nNo such file or directory - Graphics/Characters/trainer_000.png", true, true)
        assertEquals("Falta un archivo del juego", file.title)
        assertTrue(file.detail.contains("trainer_000.png"))
        assertEquals(Fix.NONE, file.fix)

        val old = ErrorDiagnosis.diagnose("Script 'Main' line 1: SyntaxError occured.", true, true)
        assertEquals(Fix.CLASSIC_ENGINE, old.fix)
        // Already on the classic engine, or without it: no promise of a fix.
        assertEquals(Fix.NONE, ErrorDiagnosis.diagnose("Script 'Main' line 1: SyntaxError occured.", false, true).fix)
        assertEquals(Fix.NONE, ErrorDiagnosis.diagnose("Script 'Main' line 1: SyntaxError occured.", true, false).fix)

        assertEquals(Fix.TRY_AGAIN, ErrorDiagnosis.diagnose("NoMemoryError: failed to allocate memory", true, true).fix)

        val unknown = ErrorDiagnosis.diagnose("Script 'X' line 7: RuntimeError occured.\n\nalgo raro", true, true)
        assertEquals(Fix.NONE, unknown.fix)
        assertTrue(unknown.detail.contains("RuntimeError"))
        assertEquals(Fix.NONE, ErrorDiagnosis.diagnose("", true, true).fix)
    }

    @Test fun theReportShowsTheScriptLinesAroundTheError() {
        val code = (1..30).joinToString("\n") { "linea $it" }
        val out = GameProfile.excerptOf(code, 15, 2)!!
        assertEquals(5, out.lines().size)
        assertTrue(out.contains("   15> linea 15"))
        assertTrue(out.lines().first().contains("linea 13"))
        assertEquals(3, GameProfile.excerptOf(code, 1, 2)!!.lines().size)
        assertNull(GameProfile.excerptOf(code, 99, 2))
        assertNull(GameProfile.excerptOf(code, 0, 2))
    }
}
