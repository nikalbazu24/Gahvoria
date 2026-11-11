# Gahvoria — reglas del proyecto

Dueño: Nicolás (no programa; prueba en teléfono y AYN Thor). Lee `docs/PLAN.md` antes de proponer cambios.

- App nueva desde cero. Solo se reutiliza el motor mkxp-z (`engine/`). No copies pantallas ni flujos de Essentia.
- Identificador `io.github.nikalbazu24.gahvoria` y firma propia (secretos `GAHVORIA_KEYSTORE_*`). Convive con Essentia.
- Las partidas quedan en la carpeta del juego, o en `Essentia/Partidas/<juego>` si no se puede escribir ahí (misma regla que Essentia y JoiPlay). No muevas ni borres partidas sin doble confirmación.
- Quitar un juego de la biblioteca nunca borra archivos.
- Ambos motores (moderno y clásico) son obligatorios: `tools/verify_apk.py` impide publicar sin ellos.
- La versión del actualizador sale de los metadatos del APK (`tools/release_manifest.py`).
- El actualizador nunca instala sin confirmación y comprueba que el APK sea Gahvoria con la versión anunciada.
- Diseño: cualquier cambio visual se muestra antes como boceto interactivo. Dirección aprobada: boceto v7 (grafito y coral, Montserrat, cartucho, barra de vidrio).
- No prometas rendimiento ni compatibilidad total sin medir en la Thor. Godot (fase 2b) y Unity (fase 4) no se anuncian como jugables antes de tiempo.
- Sin claves ni APIs de pago en el repositorio. Scripts de juegos privados no se publican.
- La lógica de `core/` no usa Android y se prueba con JUnit (`./gradlew :app:testReleaseUnitTest`).
