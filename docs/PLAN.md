# Plan por fases

Documento completo: https://claude.ai/artifact/HwzQwtcr6CKk8T3ew25cRj

| Fase | Qué se hace | Qué hace Nicolás | Estado |
|---|---|---|---|
| 0. Base | Repositorio limpio, motor mkxp-z como módulo, app nueva en Kotlin + Compose, actualizador, pruebas y compilación | Aprobar la estructura | En curso |
| 1. Diseño | Pantallas según el boceto v7 aprobado: Biblioteca, Añadir, Actualizar, Ajustes, opciones por juego | Revisar en teléfono y Thor | Primera versión dentro de la fase 0 |
| 2. RPG Maker y Essentials | Lectura de versión de Essentials, elección automática de motor, RTP, formato de pantalla | Probar 3 juegos en la Thor | En curso: lectura de versión y motor automático hechos; falta probar en la Thor |
| 2b. Godot | Detección por `project.godot`, arranque y partidas | Probar 1 juego Godot | Pendiente |
| 3. Jugar | Mando configurable, respaldos y restauración con doble confirmación, menú dentro del juego | Probar controles y restaurar | Pendiente |
| 4. Reportes, creadores y Unity | Reporte con un toque, panel de creadores, investigación de Unity | Decidir si Unity sigue | Pendiente |
| 5. Pulido | Textos en español e inglés, revisión de marca, APK final | Instalar y probar | Pendiente |

## Qué trae la fase 0

- `engine/`: mkxp-z con Ruby 3.1 y Ruby 1.8, controles táctiles, scripts de compatibilidad probados (GS Vitro, Iberia, pantalla panorámica…). Sin cambios en el código nativo.
- `app/`: interfaz nueva. Detecta RPG Maker XP/VX/VX Ace (+ Essentials), Godot y Unity por sus archivos; solo RPG Maker se juega por ahora.
- Actualizador desde el inicio (Releases → dev).
- Pruebas JVM de la lógica central; el APK no se publica si falta un motor.

## Pendiente conocido

- GS Vitro sigue con causa abierta (ver historial de Essentia). No se declara resuelto.
- Las capturas y la prueba física en la Thor son obligatorias antes de cerrar cada fase.

## 0.2 «Pulido» (en pruebas)
- Ajustes estilo Díptico: secciones a la izquierda, buscador, «Lo esencial» primero; sin opciones repetidas.
- Pantalla de carga: cartucho flotante sobre la carátula difuminada (RenderEffect en Android 12+, imagen pequeña estirada en versiones anteriores). Pendiente de ver en la Thor.
- Botones táctiles: editor por juego (mover, tamaño, ocultar, restaurar) con guía por pieza; el diseño viaja al motor en `EXTRA_PAD`.
- Borrar juego por completo: explicación + respaldo opcional, y escribir el nombre. Se niega a borrar carpetas sueltas en la raíz o que contengan otro juego. «Quitar de la biblioteca» no cambia.
- Respaldo de partidas en .zip e importación desde .zip o carpeta de otro emulador; lo reemplazado queda como `.antes`.
- Pendiente: probar en teléfono y Thor (editor táctil, carga con desenfoque, borrado en una copia de un juego).

## 0.3 «Más juegos» (primera parte, en pruebas)
- Traductor: el motor ya pedía y leía traducciones por archivos (`essentia_translate.rb`); la app ahora los responde dentro del proceso del juego (`TranslatorService`): ML Kit en el teléfono (gratis, baja el idioma la primera vez) o DeepL con clave del usuario. Memoria por juego en `filesDir/translations/<juego>/<idioma>.tsv`, se borra con el juego. Códigos del juego protegidos; si no se pueden conservar, la frase queda sin traducir.
- Pantalla por juego: panorámica (`wide_safe`, en pruebas) y pantalla retro.
- Motor automático con reintento y memoria del motor que funcionó (0.2.1).
- No hecho aún: FXAA y nitidez (necesitan código nativo y medir en la Thor), importar de Kirin/Navia XP por rutas conocidas, panorámica «pulida».
- Pendiente: probar en la Thor con un juego en inglés (ML Kit puede no funcionar si el equipo no tiene servicios de Google; DeepL no los necesita).

## Tienda (0.3c, en pruebas)
- Pestaña «Tienda»: lista (`assets/store.json`, se actualiza desde el repositorio sin nuevo APK), ficha con enlace a la página oficial del creador, filtros por idioma y «Probados». Gahvoria no aloja ni descarga juegos de terceros.
- «Instalar desde .zip»: busca la carpeta del juego dentro del .zip, descomprime a `Juegos/<nombre>` por una carpeta temporal `.part`, nunca sobre una carpeta existente, ignora rutas que se salgan del destino.
- Formulario para creadores: manda el enlace al mismo buzón privado de informes; se revisa a mano.
- Entradas iniciales con enlaces de Nicolás; todas «Sin probar». Insurgence entrega un instalador de Windows (.exe) y no se puede instalar directo. Solar Eclipse: la página no se pudo abrir desde aquí para revisarla.
- Pendiente: soporte .rar/.7z, actualizador automático de fan games, descarga directa si un creador la autoriza, 

- Instalación desde .7z y .rar (Commons Compress y junrar), detectados por sus primeros bytes. RAR versión 5 y archivos con contraseña: aviso claro, sin instalar. Sin probar en el teléfono.
- Nombre «Gahvoria»: confirmado por Nicolás (11 nov: sin choques de marca). Los títulos de juegos de la tienda siguen sin la palabra «Pokémon».

## Panel de creador y canal estable (0.4, en pruebas)
- Ajustes → Creadores: «Modo creador» (apagado por defecto). Cada juego se marca «es mío»; solo esos guardan historial de errores en `filesDir/creator/<juego>/<hora>.txt` (últimos 20, sin repetir el mismo error seguido). Nada sale del teléfono sin que el creador lo envíe.
- Panel: Resumen (versión de Essentials y del juego, motor, pantalla, carga, tiempo, carpeta, partidas; copiar datos; proponer en la tienda con esos datos), Chequeo, Informes (detalle con diagnóstico, script y registro; enviar) y Pedir ajuste (tipo, descripción de al menos 10 caracteres, adjuntos opcionales).
- `GameCheck` (core, con pruebas) solo lee archivos: Game.ini, Data, scripts, RTP, .dll, rutas `C:\`, programas del sistema, Win32API (el motor lleva una parte), GAME_VERSION. No ejecuta el juego: no sustituye probar en la Thor.
- Tienda: campo opcional `version` en `store.json`; si es mayor que el GAME_VERSION del juego, sus opciones muestran «Versión nueva en la tienda».
- No existe todavía un canal jugador → creador: los informes y pedidos llegan al buzón privado de Nicolás, no al creador del juego.
- Canal de actualizaciones: `dev` (por defecto, cada compilación) o `stable` (flujo manual `promote.yml`, copia los archivos de `dev` a Releases → stable). Si aún no hay estable, la app lo dice y no falla. Cambiar el valor por defecto a `stable` queda para cuando haya una versión estable probada.
- Pendiente: probar el panel en el teléfono y la Thor; ejecutar «Promover a estable» la primera vez.

## Boceto v16 (0.5, en pruebas)
- Paletas: once (`core/Palette.kt`), cada una con esquema oscuro y claro; una prueba comprueba el contraste.
- Icono: el manifiesto declara un `activity-alias` por paleta (`.IconGrafito`, `.IconOceano`…, solo el elegido está activo) y `data/AppIcon.kt` los alterna con `DONT_KILL_APP`. Interruptor «Cambiar también el icono» en Ajustes → Apariencia. Algunos launchers tardan en notarlo y un acceso fijado a mano puede haber que volver a ponerlo. Una prueba exige que cada paleta tenga su alias e icono. Sin verificar en el teléfono.
- Tienda: la imagen sale del `og:image` de la página oficial del juego (`core/StoreImages`, `data/StoreArt`), se guarda pequeña en caché y se busca de nuevo cada semana. No se guardan enlaces de imágenes propios; campo opcional `image` en `store.json` para fijar una. Páginas de MEGA, MediaFire, Pastebin y similares no se consultan (su imagen es el logo del sitio). Sin Internet o sin imagen queda el cartucho dibujado. Se respeta «Buscar imágenes en Internet».
- Juegos nuevos en la lista: Reborn, Rejuvenation y Clockwork, con enlace al foro de Reborn Evolved. Ese sitio no deja que un programa lo lea: no pude comprobar creador, versión ni formato de descarga. Están «Sin probar» y con esa nota.
- Teclado: botón ⌨ (arriba a la derecha, solo con controles táctiles) que abre el teclado de SDL. Envía teclas y texto. Sin verificar en el juego.
- 2D / 2.5D / 3D: `ESSENTIA_TILT` (grados, 0–60). `GraphicsPrivate::essentiaTilt` dibuja el cuadro final en 28 franjas con perspectiva (solo con Android y sin modo ambiente). Inclina todo el cuadro, también menús y batallas, y los toques sobre la imagen no siguen la inclinación. Compilado solo en CI; sin medir en la Thor.

## Motor elegido antes de abrir e instalación en segundo plano (0.5b, en pruebas)
- `RubyClues` (core) lee todos los scripts del juego: sintaxis de Ruby 1.9+ (`%i[]`, `->`, `&.`, `Encoding::`…) obliga al motor moderno; hábitos de Ruby 1.8 (`getc` como número, `each_byte` propio, `fgetdw`, `$KCODE`, `Fixnum`) llevan al clásico, salvo Essentials 19+. Si hay de ambos, manda la sintaxis. Las opciones del juego muestran la razón. Son pistas del texto, no prueban que el juego abra: la Thor decide.
- Antes de abrir se comprueba que no falten archivos de `Data` (`GameCheck.missingData`, solo lista la carpeta). Si faltan, avisa y ofrece «Abrir igual». Un archivo que falta no provoca el reintento con el otro motor.
- Error corregido: `essentia_games.rb` usaba `%i[]` y el motor clásico (Ruby 1.8.7) paraba con SyntaxError (Solar Eclipse, 0.1.30). Una prueba revisa que los scripts que carga el clásico no tengan sintaxis moderna.
- Insurgence (motor moderno): `String can't be coerced into Integer` en `fgetdw` → `each_byte`. Hipótesis, sin confirmar: el juego lee bytes como texto UTF-8 y `essentia_single_byte` falla con un carácter de varios bytes. El error ahora dice cuántos bytes, qué codificación y los primeros bytes del texto, para decidirlo con el próximo informe.
- Instalar un .zip/.7z/.rar: hilo propio (ya no el hilo compartido), servicio en primer plano con notificación de progreso (`InstallService`) y aviso de progreso en todas las pantallas. Se puede salir de la app. No hay botón de cancelar todavía.

## Gahvoria Neural AI (experimental)

La auditoría, la primera reparación del traductor y el plan del modelo local están en [Gahvoria Neural AI](NEURAL_AI.md).


### Arranque de Pokémon Reborn

- El perfil automático comprueba los scripts externos indicados por el arranque, incluyendo los ajustes de Reborn que su cargador descubre dinámicamente. Si encuentra sintaxis de hash de Ruby moderno, corrige la elección incluso si el modo automático había memorizado el motor clásico.
- Si el motor clásico devuelve el error de sintaxis correspondiente, el informe ofrece reabrir con el motor moderno. La corrección se comprueba con scripts de prueba y queda pendiente de verificar con Reborn en el teléfono.

- Reborn 19.5 avanzó al motor moderno y RubyGems pidió `rbconfig`. El APK incorpora la configuración generada por el mismo Ruby ARM64 que ejecuta el juego. Gahvoria la carga antes de que el juego agregue la biblioteca de escritorio. El verificador del APK comprueba el archivo. Queda por probar el arranque de Reborn en el teléfono.

- Tras cargar RubyGems, el actualizador de Reborn pide OpenSSL desde `Net::HTTP`. Se habilita la extensión nativa en Ruby para Android y se empaquetan sus archivos Ruby correspondientes. La compilación comprueba que la extensión esté enlazada y que los archivos lleguen al APK. Se conserva la verificación TLS.

- Reborn 19.5.0 carga Discord Rich Presence mediante una extensión nativa de escritorio. Para la estructura de scripts de Reborn, el motor moderno ofrece las cinco operaciones que usa el juego y mantiene Discord desconectado. La prueba automatizada cubre la carga repetida y comprueba que los errores de otras bibliotecas siguen visibles. El arranque hasta el menú queda por verificar en Android.

- Reborn 19.5.0 necesita las entradas lógicas D, E y F de su variante de mkxp-z. El perfil del motor moderno conecta sus atajos con el teclado y entrega tiempos de pulsación en segundos al juego. El motor añade la búsqueda `System.file_exist?` y monta las carpetas de parches declaradas antes de los archivos base. El perfil también aporta el reloj de sesión usado para el tiempo de partida y reproduce los efectos de sonido mediante la salida estéreo normal. Pruebas automatizadas cubren las entradas y el reloj. Queda por verificar el arranque hasta el primer diálogo en Android.

- La prueba en Android con 0.1.79 llegó al primer mapa y se detuvo al llamar a `TilemapUtils.updateAutoTiles`. El perfil de Reborn ofrece esa entrada y conserva la animación que Gahvoria ejecuta mediante `Tilemap#update` para cada mapa. La prueba cubre la llamada, el aislamiento del perfil y la conservación de una implementación nativa. Queda por comprobar en Android el primer mapa, el movimiento y el primer diálogo.
