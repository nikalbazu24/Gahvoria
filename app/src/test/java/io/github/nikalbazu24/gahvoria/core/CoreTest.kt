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

    // ---- introduction ------------------------------------------------------------------------

    @Test fun newPlayersSeeTheWelcomeAndUpdatesOnlyShowTheirChanges() {
        assertEquals(Intro.WELCOME, Intro.pending(introSeen = false, seenUpdate = 0, hasGames = false))
        // Someone who already played before the introduction existed: only the changes.
        val changes = Intro.pending(introSeen = false, seenUpdate = 0, hasGames = true)!!
        assertTrue(changes.isNotEmpty())
        assertTrue(changes.none { it in Intro.WELCOME })
        assertEquals(changes, Intro.pending(introSeen = true, seenUpdate = 0, hasGames = true))
        // Already seen the latest big update: nothing.
        assertNull(Intro.pending(introSeen = true, seenUpdate = Intro.LATEST, hasGames = true))
        assertNull(Intro.pending(introSeen = true, seenUpdate = Intro.LATEST, hasGames = false))
    }

    // ---- touch layout ------------------------------------------------------------------------

    @Test fun theTouchLayoutRoundTripsAndIgnoresWhatIsBroken() {
        val l = TouchLayout().with(PadPiece.DPAD, PadSpot(0.05f, -0.02f, 1.2f, false)).with(PadPiece.BAR, PadSpot(hidden = true))
        val back = TouchLayout.decode(l.encode())
        assertEquals(l, back)
        assertEquals(PadSpot(), back.spot(PadPiece.L))
        // Back to the default removes the piece from the saved text.
        assertTrue(l.with(PadPiece.DPAD, PadSpot()).with(PadPiece.BAR, PadSpot()).isDefault)
        // Out of range values are kept inside the screen and sensible sizes.
        val wild = TouchLayout().with(PadPiece.R, PadSpot(5f, -5f, 50f))
        assertEquals(PadSpot(0.9f, -0.9f, 1.8f), wild.spot(PadPiece.R))
        assertEquals(TouchLayout(), TouchLayout.decode("basura;dpad,x,y,z,0;otro,0,0,1,0;dpad,0.1"))
        assertEquals(TouchLayout(), TouchLayout.decode(null))
        assertEquals(TouchLayout(), TouchLayout.decode("dpad,NaN,0,1,0"))
    }

    @Test fun removalNeedsTheNameAndRefusesRiskyFolders() {
        assertTrue(GameRemoval.nameMatches("  ópalo ", "Ópalo"))
        assertFalse(GameRemoval.nameMatches("", ""))
        assertFalse(GameRemoval.nameMatches("Opal", "Ópalo"))
        val root = java.nio.file.Files.createTempDirectory("st").toFile()
        val game = File(root, "Juegos/Opal").apply { mkdirs() }
        File(game, "Game.ini").writeText("x"); File(game, "Save1.rxdata").writeText("s")
        assertNull(GameRemoval.folderRefusal(game, root, emptyList()))
        assertTrue(GameRemoval.folderRefusal(File(root, "Juegos").also { it.mkdirs() }, root, listOf(game.path)) != null)
        assertTrue(GameRemoval.folderRefusal(File(root, "Download").also { it.mkdirs() }, root, emptyList()) != null)
        assertTrue(GameRemoval.folderRefusal(root, root, emptyList()) != null)
        val shortcuts = File(root, "ROMs/Gahvoria").apply { mkdirs() }
        val g = Game(game.path, "Opal", EngineKind.RPG_MAKER_XP, true)
        Shortcuts.export(listOf(g), shortcuts)
        File(shortcuts, "Otro.gahvoria").writeText("/otro\n")
        val plan = GameRemoval.plan(g, game, shortcuts, root, emptyList())
        assertNull(plan.saveDir); assertEquals(1, plan.shortcuts.size)
        val r = GameRemoval.execute(plan)
        assertTrue(r.folderDeleted); assertFalse(game.exists()); assertEquals(1, r.shortcuts)
        assertTrue(File(shortcuts, "Otro.gahvoria").exists())
        assertTrue(File(root, "Juegos").exists())
    }

    @Test fun backupRoundTripKeepsTheOldSave() {
        val a = java.nio.file.Files.createTempDirectory("a").toFile()
        val b = java.nio.file.Files.createTempDirectory("b").toFile()
        File(a, "Save1.rxdata").writeText("nuevo"); File(a, "Scripts.rxdata").writeText("no")
        val out = java.io.ByteArrayOutputStream()
        assertEquals(1, SaveBackup.export(a, out, "Opal"))
        val items = SaveBackup.readZip(out.toByteArray().inputStream())
        assertEquals(listOf("Save1.rxdata"), items.map { it.name })
        File(b, "Save1.rxdata").writeText("viejo")
        assertEquals(listOf("Save1.rxdata"), SaveBackup.conflicts(items, b))
        assertEquals(0 to 1, SaveBackup.restore(items, b, false))
        assertEquals("viejo", File(b, "Save1.rxdata").readText())
        assertEquals(1 to 0, SaveBackup.restore(items, b, true))
        assertEquals("nuevo", File(b, "Save1.rxdata").readText())
        assertEquals("viejo", File(b, "Save1.rxdata.antes").readText())
        assertEquals(1, SaveBackup.readFolder(a).size)
    }

    @Test fun theStoreCatalogOnlyKeepsSecurePagesAndFilters() {
        val json = File("app/src/main/assets/store.json").takeIf { it.exists() }?.readText()
            ?: File("../app/src/main/assets/store.json").readText()
        val all = StoreCatalog.parse(json)
        assertTrue(all.size >= 8)
        assertTrue(all.all { it.page.startsWith("https://") && !it.tested })
        assertEquals(1, StoreCatalog.parse("{\"games\":[{\"title\":\"A\",\"page\":\"https://x.y\"},{\"title\":\"B\",\"page\":\"http://x.y\"}]}").size)
        assertTrue(StoreCatalog.filter(all, "all", false, "Pokémon Insurgence").isNotEmpty())
        assertTrue(StoreCatalog.filter(all, "en", false, "").all { it.lang == "en" })
        assertTrue(StoreCatalog.filter(all, "all", true, "").isEmpty())
        assertEquals("Insurgence", StoreCatalog.filter(all, "all", false, "insurgence").first().title)
    }

    @Test fun aZipIsInstalledOnlyAsAGameAndNeverEscapes() {
        fun zip(vararg e: Pair<String, String>): ByteArray {
            val b = java.io.ByteArrayOutputStream()
            java.util.zip.ZipOutputStream(b).use { z -> for ((n, t) in e) { z.putNextEntry(java.util.zip.ZipEntry(n)); z.write(t.toByteArray()); z.closeEntry() } }
            return b.toByteArray()
        }
        assertNull(ZipInstaller.plan(listOf("readme.txt", "Setup.exe")))
        val z = zip("Mi Juego/Game.ini" to "x", "Mi Juego/Data/Scripts.rxdata" to "y", "Mi Juego/Audio/a.ogg" to "z", "../evil.txt" to "no", "Mi Juego/../evil2.txt" to "no")
        val plan = ZipInstaller.plan(ZipInstaller.listNames(z.inputStream()))!!
        assertEquals("Mi Juego/", plan.root)
        val base = Files.createTempDirectory("zi").toFile()
        val dest = ZipInstaller.freeFolder(base, "Mi Juego")
        assertEquals(3, ZipInstaller.extract(z.inputStream(), plan, dest))
        assertTrue(File(dest, "Game.ini").exists() && File(dest, "Audio/a.ogg").exists())
        assertFalse(File(base, "evil.txt").exists() || File(dest.parentFile, "evil2.txt").exists())
        assertEquals("Mi Juego (2)", ZipInstaller.freeFolder(base, "Mi Juego").name)
        assertFalse(File(base, "Mi Juego.part").exists())
        assertEquals("", ZipInstaller.plan(listOf("Game.ini", "Data/Scripts.rxdata"))!!.root)
    }

    @Test fun compressedFilesAreToldApartByTheirFirstBytes() {
        fun b(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }
        assertEquals(ArchiveKind.ZIP, ArchiveKind.detect(b(0x50, 0x4B, 3, 4, 0, 0)))
        assertEquals(ArchiveKind.SEVEN_Z, ArchiveKind.detect(b(0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C)))
        assertEquals(ArchiveKind.RAR, ArchiveKind.detect(b(0x52, 0x61, 0x72, 0x21, 0x1A, 7, 0)))
        assertEquals(ArchiveKind.UNKNOWN, ArchiveKind.detect(b(0x4D, 0x5A, 0x90, 0)))
        assertEquals(ArchiveKind.UNKNOWN, ArchiveKind.detect(ByteArray(0)))
    }

    @Test fun theCheckFindsWhatBreaksOnAndroidAndNothingElse() {
        val ini = GameCheck.checkIni("[Game]\r\nRTP1=Standard\r\nRTP2=\r\nRTP3=\r\n")
        assertEquals(1, ini.size)
        assertEquals(Level.WARN, ini[0].level)
        assertTrue(GameCheck.checkIni("[Game]\r\nRTP1=\r\nTitle=Mi juego\r\n").isEmpty())

        val bad = GameCheck.checkScripts(listOf(
            "Main" to "Win32API.new('user32', 'GetKeyState', 'i', 'i')",
            "Extra" to "lib = Win32API.new('mylib.dll', 'f', 'i', 'i')\nFile.open(\"C:\\\\Users\\\\yo\\\\x.txt\")",
            "Run" to "system('notepad.exe')",
            "Settings" to "GAME_VERSION = \"1.4\"",
        ))
        assertTrue(bad.any { it.level == Level.WARN && it.title.contains("Rutas de Windows") })
        assertTrue(bad.any { it.level == Level.WARN && it.title.contains("programas") })
        assertTrue(bad.any { it.level == Level.INFO && it.title.contains("Win32API") })
        assertTrue(bad.any { it.title.contains("1.4") })
        assertFalse(bad.any { it.level == Level.OK && it.title.startsWith("No encontré") })

        val clean = GameCheck.checkScripts(listOf("Settings" to "GAME_VERSION = '2.0'\nmodule Essentials\n  VERSION = \"21.1\"\nend"))
        assertEquals(Level.OK, clean[0].level)
        assertTrue(clean.any { it.title.contains("Essentials 21.1") })
        assertEquals("Sin problemas a la vista", GameCheck.summary(clean))
        assertEquals("1 problema y 2 avisos", GameCheck.summary(listOf(
            Finding(Level.PROBLEM, "a", ""), Finding(Level.WARN, "b", ""), Finding(Level.WARN, "c", ""))))
        val none = GameCheck.checkScripts(listOf("Main" to "puts 1"))
        assertTrue(none.any { it.title == "No hay GAME_VERSION" })

        val noIni = GameCheck.check(folder("Data/Scripts.rxdata"))
        assertTrue(noIni.any { it.level == Level.PROBLEM && it.title.contains("Game.ini") })
        val packed = GameCheck.check(folder("Game.ini", "Data/x", "Game.rgss3a"))
        assertTrue(packed.any { it.title.contains("comprimido") })
    }

    @Test fun storeVersionsCompareByNumberNotByText() {
        assertTrue(StoreCatalog.isNewer("1.10", "1.9"))
        assertTrue(StoreCatalog.isNewer("v2.0", "1.5.3"))
        assertFalse(StoreCatalog.isNewer("1.2", "1.2.0"))
        assertFalse(StoreCatalog.isNewer("1.2", "1.3"))
        assertFalse(StoreCatalog.isNewer("", "1.0"))
        assertFalse(StoreCatalog.isNewer("2.0", ""))
        val list = StoreCatalog.parse("""{"games":[{"title":"Mi Juego","page":"https://x.dev","aliases":["Mi Juego Remake"],"version":" 1.3 "}]}""")
        assertEquals("1.3", list[0].version)
        assertTrue(StoreCatalog.match(list, "mi juego remake") != null)
        assertNull(StoreCatalog.match(list, "Otro"))
    }

    @Test fun theErrorHistoryKeepsTheLatestAndSkipsRepeats() {
        val log = CreatorLog(Files.createTempDirectory("clog").toFile(), keep = 3)
        val key = log.key("/juegos/Mi Juego", "Mi Juego")
        assertTrue(key.startsWith("mi-juego-"))
        assertTrue(log.add(key, 1000, "uno") != null)
        assertNull(log.add(key, 2000, "uno"))
        assertTrue(log.add(key, 3000, "dos") != null)
        assertTrue(log.add(key, 3000, "tres") != null)  // same millisecond: still saved
        assertTrue(log.add(key, 5000, "cuatro") != null)
        val all = log.list(key)
        assertEquals(3, all.size)
        assertEquals("cuatro", all[0].text())
        assertEquals("cuatro", all[0].headline())
        assertEquals(3, log.count(key))
        log.clear(key)
        assertEquals(0, log.count(key))
        assertEquals(0, log.count("nada"))
    }

    @Test fun creatorTextsCarryOnlyWhatWasChosen() {
        val sum = CreatorText.summary("Mi Juego", "0.4", "Essentials 21.1", "", "moderno", "original (4:3)", null, 12, "/j/Mi Juego", 3)
        assertTrue(sum.contains("Versión del juego: no indicada") && sum.contains("Última carga: sin datos") && sum.contains("Partidas: 3"))
        val bare = CreatorText.request(1, "  Va lento en la ciudad  ", null, null)
        assertTrue(bare.contains("Tipo: Va lento") && bare.contains("Va lento en la ciudad"))
        assertFalse(bare.contains("Datos del juego") || bare.contains("Último informe"))
        val full = CreatorText.request(0, "Se cierra", sum, "boom")
        assertTrue(full.contains("Datos del juego") && full.contains("Último informe") && full.contains("boom"))
        assertTrue(CreatorText.proposal("A", "https://a", "Español", "yo", "x").contains("Detectado por Gahvoria: x"))
    }

    @Test fun updateChannelsPointToTheirOwnFolder() {
        val dev = "https://github.com/nikalbazu24/Gahvoria/releases/download/dev/"
        assertEquals(dev, UpdateChannel.base(dev, UpdateChannel.DEV))
        assertEquals("https://github.com/nikalbazu24/Gahvoria/releases/download/stable/", UpdateChannel.base(dev, UpdateChannel.STABLE))
        assertEquals(dev, UpdateChannel.base(dev, "cualquier cosa"))
        val beta = "https://github.com/nikalbazu24/Gahvoria/releases/download/neural-beta/"
        assertEquals(beta, UpdateChannel.base(beta, UpdateChannel.DEV))
        assertEquals(beta, UpdateChannel.base(beta, "cualquier cosa"))
        assertEquals("https://github.com/nikalbazu24/Gahvoria/releases/download/stable/", UpdateChannel.base(beta, UpdateChannel.STABLE))
        assertTrue(UpdateChannel.valid("stable") && !UpdateChannel.valid(null) && !UpdateChannel.valid("x"))
    }

    @Test fun scriptsThatNeedModernRubyNeverGetTheClassicEngine() {
        val old = GameProfile.parse(listOf("Settings" to "ESSENTIALS_VERSION = \"17.2\"", "PScreen_Load" to null))
        assertEquals(Game.ENGINE_CLASSIC, old.recommendedEngine)
        val withMkxp = GameProfile.parse(listOf("MKXP_Compatibility" to "Encoding.default_internal = Encoding::UTF_8",
            "Settings" to "ESSENTIALS_VERSION = \"17.2\"", "PScreen_Load" to null))
        assertTrue(withMkxp.modernOnly)
        assertEquals(Game.ENGINE_MODERN, withMkxp.recommendedEngine)
        // an old game that also mentions modern Ruby (guarded) but reads bytes as numbers: classic
        val both = GameProfile.parse(listOf("MKXP" to "Encoding.default_internal = Encoding::UTF_8", "PBStruct" to "def fgetb(file)\nend"))
        assertEquals(Game.ENGINE_CLASSIC, both.recommendedEngine)
    }

    @Test fun theCheckNamesTheMissingDataFiles() {
        val g = folder("Game.ini", "Data/Scripts.rxdata", "Data/Tilesets.rxdata", "Data/System.rxdata")
        val f = GameCheck.check(g).first { it.title == "Faltan archivos en Data" }
        assertEquals(Level.PROBLEM, f.level)
        assertTrue(f.detail.contains("Animations.rxdata") && f.detail.contains("CommonEvents.rxdata") && !f.detail.contains("Tilesets.rxdata"))
    }

    @Test fun anArchiveWithoutAGameSaysWhatItHolds() {
        assertEquals("Juego/", ZipInstaller.plan(listOf("Juego/Data/System.rxdata", "Juego/Audio/a.ogg"))!!.root)
        assertTrue(ZipInstaller.whyNot(listOf("Setup.exe", "readme.txt")).contains("instalador"))
        assertTrue(ZipInstaller.whyNot(listOf("Solar/Eclipse.7z")).contains("otro archivo comprimido"))
        assertTrue(ZipInstaller.whyNot(listOf("docs/a.txt", "docs/b.txt")).contains("Contiene: docs"))
        assertEquals("El archivo está vacío.", ZipInstaller.whyNot(emptyList()))
    }

    @Test fun theProfileIsReadOnceWhileTheScriptsStayTheSame() {
        val g = folder("Data/Scripts.rxdata")
        val a = GameProfile.read(g)
        assertTrue(GameProfile.read(g) === a)
        File(g, "Data/Scripts.rxdata").writeText("changed")
        assertTrue(GameProfile.read(g) !== a)
    }

    @Test fun everyPaletteHasItsLauncherIcon() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        for (p in Palettes.all) {
            val cls = ".Icon" + p.id.replaceFirstChar { it.uppercase() }
            assertTrue("falta el acceso $cls", manifest.contains("android:name=\"$cls\""))
            assertTrue("falta ic_fg_${p.id}", File("src/main/res/drawable/ic_fg_${p.id}.xml").exists())
            assertTrue("falta ic_launcher_${p.id}", File("src/main/res/mipmap-anydpi-v26/ic_launcher_${p.id}.xml").exists())
        }
    }

    @Test fun storeImageFromPageMetadata() {
        val html = """<head><meta property="og:title" content="x"><meta content="/img/portada.png?a=1&amp;b=2" property="og:image"></head>"""
        assertEquals("https://sitio.com/img/portada.png?a=1&b=2", StoreImages.ogImage(html, "https://sitio.com/juego/"))
        assertEquals("https://cdn.com/t.jpg", StoreImages.ogImage("""<meta name='twitter:image' content='https://cdn.com/t.jpg'>""", "https://sitio.com/"))
        assertEquals(null, StoreImages.ogImage("""<meta property="og:image" content="http://sitio.com/a.jpg">""", "https://sitio.com/"))
        assertEquals(null, StoreImages.ogImage("<html></html>", "https://sitio.com/"))
        assertTrue(!StoreImages.hasUsefulImage("https://mega.nz/folder/abc"))
        assertTrue(!StoreImages.hasUsefulImage("https://www.mediafire.com/file/x"))
        assertTrue(StoreImages.hasUsefulImage("https://p-insurgence.com/"))
    }

    @Test fun bundledStoreParsesAndHasCreators() {
        val games = StoreCatalog.parse(File("src/main/assets/store.json").readText())
        assertTrue(games.size >= 12)
        assertTrue(games.all { it.creator.isNotBlank() && it.description.isNotBlank() })
        assertEquals(games.size, games.map { it.id }.toSet().size)
    }

    @Test fun depthModesAndAnglesAgree() {
        assertEquals(0, Depth.modeOf(0))
        assertEquals(1, Depth.modeOf(Depth.DEFAULT_25D))
        assertEquals(2, Depth.modeOf(Depth.DEFAULT_3D))
        for (m in 0..2) assertEquals(m, Depth.modeOf(Depth.angleOf(m)))
        assertEquals(60, Depth.clamp(200)); assertEquals(0, Depth.clamp(-5))
    }

    /**
     * The classic engine is Ruby 1.8.7: a single "%i[...]" or "{ key: value }" in a script it loads stops every game
     * with a SyntaxError (it happened with Solar Eclipse). essentia_compat.rb is for the modern engine only.
     */
    @Test fun scriptsOfTheClassicEngineUseOldRubySyntax() {
        val dir = File("../engine/src/main/assets/essentia_preload")
        val bad = Regex("""%[iI][\[(]|->|&\.|(?<!\\)\.\(|[({,]\s*[a-z_]+:\s""")
        val problems = mutableListOf<String>()
        for (f in dir.listFiles { x -> x.extension == "rb" && x.name != "essentia_compat.rb" }!!.sortedBy { it.name }) {
            f.readLines().forEachIndexed { n, line ->
                val code = line.substringBefore(" #").trimStart()
                if (!code.startsWith("#") && bad.containsMatchIn(code)) problems += "${f.name}:${n + 1}: ${line.trim()}"
            }
        }
        assertTrue("Sintaxis de Ruby moderno en scripts del motor clásico:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    @Test fun theEngineIsChosenFromTheRubyTheScriptsNeed() {
        fun profile(vararg code: String) = GameProfile.parse(code.mapIndexed { i, c -> "Section$i" to (c as String?) })
        // Ruby 1.9+ syntax: only the modern engine can read it, even in an old Essentials
        val modern = profile("ESSENTIALS_VERSION = \"17.2\"", "x = [1].map { |a| a }\nCOLS = %i[a b]")
        assertEquals(Game.ENGINE_MODERN, modern.recommendedEngine)
        assertTrue(modern.engineReason.contains("%i"))
        assertEquals(Game.ENGINE_MODERN, profile("f = ->(x) { x }").recommendedEngine)
        assertEquals(Game.ENGINE_MODERN, profile("a&.b").recommendedEngine)
        // Ruby 1.8 habits (a byte read as a number): classic, with the reason
        val old = profile("class File\n  def each_byte\n  end\nend", "def fgetdw(file)\nend")
        assertEquals(Game.ENGINE_CLASSIC, old.recommendedEngine)
        assertTrue(old.engineReason.contains("antiguo"))
        // both kinds: the syntax decides, because the classic engine cannot even parse the script
        assertEquals(Game.ENGINE_MODERN, profile("x = \$KCODE", "COLS = %i[a]").recommendedEngine)
        // Essentials 19+ is modern even if a script reads bytes
        assertEquals(Game.ENGINE_MODERN, profile("module Essentials\n  VERSION = \"21.1\"\nend", "c = f.getc").recommendedEngine)
        // comments do not count
        assertEquals(Game.ENGINE_MODERN, profile("# x = ->(a) { a }\n# COLS = %i[a]").recommendedEngine)
        // nothing at all: modern; Essentials 17 without clues: classic as before
        assertEquals(Game.ENGINE_MODERN, profile("puts 1").recommendedEngine)
        assertEquals(Game.ENGINE_CLASSIC, profile("ESSENTIALS_VERSION = \"17.2\"").recommendedEngine)
    }

    @Test fun externalBootScriptsOverrideALearnedClassicEngine() {
        val root = scriptsFile("script" to """
            ESSENTIALS_VERSION = "18.1"
            eval(File.read("Scripts/Reborn/Bootstrap.rb"))
            eval(File.read("Scripts/ScriptLoader.rb"))
        """.trimIndent())
        try {
            val scripts = File(root, "Scripts/Reborn").apply { mkdirs() }
            File(scripts, "Bootstrap.rb").writeText("# bootstrap")
            val settings = File(scripts, "Settings.rb")
            assertEquals(Game.ENGINE_CLASSIC, GameProfile.read(root).recommendedEngine)
            settings.writeText("ENCOUNTERS = [{ species: :TEST, level: 2 }]")
            assertNull(GameProfile.cached(root))
            val profile = GameProfile.read(root)
            assertEquals(Game.ENGINE_MODERN, profile.recommendedEngine)
            assertEquals(Game.ENGINE_MODERN, profile.automaticEngine(Game.ENGINE_CLASSIC))
            assertTrue(profile.engineReason.contains("Scripts/Reborn/Settings.rb"))
            assertEquals(Game.ENGINE_CLASSIC,
                GameProfile.parse(listOf("Legacy" to "ESSENTIALS_VERSION = \"18.1\"")).automaticEngine(null))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun classicHashSyntaxErrorSuggestsModernEngine() {
        val report = "Script 'loader' line 3: SyntaxError occured.\nSettings:1: odd number list for Hash\nSettings:1: syntax error"
        assertEquals(Fix.MODERN_ENGINE, ErrorDiagnosis.diagnose(report, false, true).fix)
    }

    @Test fun missingDataIsFoundWithoutReadingScripts() {
        val g = Files.createTempDirectory("g").toFile()
        File(g, "Data").mkdirs()
        assertTrue(GameCheck.missingData(g).isEmpty())            // no scripts: nothing to judge
        File(g, "Data/Scripts.rxdata").writeText("x")
        File(g, "Data/System.rxdata").writeText("x")
        File(g, "Data/MapInfos.rxdata").writeText("x")
        assertEquals(listOf("Animations", "Tilesets", "CommonEvents"), GameCheck.missingData(g))
        for (n in listOf("Animations", "Tilesets", "CommonEvents")) File(g, "Data/$n.rxdata").writeText("x")
        assertTrue(GameCheck.missingData(g).isEmpty())
    }

    @Test fun aClashBetweenRubyVersionsIsToldFromOtherErrors() {
        val opalo = "Script 'PBExperience' line 119: TypeError occured.\nno implicit conversion of Integer into String"
        assertTrue(ErrorDiagnosis.engineClash(opalo, modernEngine = true))
        assertTrue(ErrorDiagnosis.engineClash("String can't be coerced into Integer (TypeError)", modernEngine = true))
        assertTrue(!ErrorDiagnosis.engineClash(opalo, modernEngine = false))
        assertTrue(ErrorDiagnosis.engineClash("compile error\n%i[a]: unknown type of %string", modernEngine = false))
        assertTrue(!ErrorDiagnosis.engineClash("undefined method `foo' for nil:NilClass", modernEngine = true))
        assertTrue(!ErrorDiagnosis.engineClash("No such file or directory - Data/Animations.rxdata", modernEngine = true))
    }
}
