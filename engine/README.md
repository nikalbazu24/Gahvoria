# engine — mkxp-z reutilizado

Esta carpeta es el motor de juego. Viene del port Android de mkxp-z usado por Essentia, sin cambios en el código nativo:

- `jni/`: mkxp-z (C++), Ruby 3.1 (motor moderno) y Ruby 1.8.7 (motor clásico), SDL2 y dependencias. Las dependencias se descargan y compilan en CI (`get_deps.sh`, `Makefile`, `build_ruby18.sh`).
- `src/main/java/org/libsdl/app/`: la capa Java de SDL2.
- `src/main/java/com/hatkid/mkxpz/gamepad/`: controles táctiles.
- `src/main/assets/essentia_preload/`: scripts de compatibilidad que el motor carga antes del juego. Leen variables `ESSENTIA_*` (nombres que espera el motor; no cambiarlos).
- `GameActivity`: la única puerta entre la app y el motor. El código nativo busca por nombre `GAME_PATH` y los métodos estáticos `onLoadPhase`, `onGameReady`, `setFPSVisibility`, `updateFPSText`, `getSystemLanguage`, `hasVibrator`, `vibrate`, `vibrateStop` e `inMultiWindow`.

Licencia: GPL 2.0 (ver `LICENSE` y `jni/mkxp-z/COPYING`).
