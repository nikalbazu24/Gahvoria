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
