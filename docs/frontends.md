# Usar Gahvoria desde otros launchers

Gahvoria puede abrir tus juegos desde **Díptico**, **Daijishō**, **ES-DE**, **Cocoon**, **iiSU** o cualquier launcher que sepa abrir un archivo con una app.

> Estado: la entrada está hecha y probada con archivos de ejemplo. Falta probarla con cada launcher real (Díptico, Cocoon, iiSU y los demás), y puede pedir un ajuste según cómo cada uno llame a la app.

## 1. Crear los accesos

1. Agrega tus juegos a la biblioteca de Gahvoria.
2. Ve a **Ajustes › Otros launchers › Crear accesos de mis juegos**.

Gahvoria escribe un archivo pequeño por juego en `ROMs/Gahvoria` (en el almacenamiento del equipo), por ejemplo `Pokémon Añil.gahvoria`. Adentro solo está la ruta de la carpeta del juego. Los juegos que agregues después se suman solos. Quitar un juego de la biblioteca no borra su acceso ni ningún archivo.

La carpeta se llama como la app a propósito: en el launcher, elígela como la carpeta del «emulador» Gahvoria.

> Si prefieres hacerlos a mano: un archivo de texto con extensión `.gahvoria` cuya primera línea sea la carpeta del juego (por ejemplo `/storage/emulated/0/Juegos/Pokemon Anil`).

## 2. Cómo se abre

Todos los launchers deben llamar a esta actividad:

```
io.github.nikalbazu24.gahvoria/io.github.nikalbazu24.gahvoria.LaunchActivity
```

con la acción `android.intent.action.VIEW` y como dato el archivo `.gahvoria` (`file://` o `content://`). También acepta:

- la carpeta del juego,
- cualquier archivo dentro del juego (por ejemplo `Game.ini`),
- un extra de texto `path` con la ruta de la carpeta.

Ejemplo con `am`:

```
am start -n io.github.nikalbazu24.gahvoria/io.github.nikalbazu24.gahvoria.LaunchActivity \
  -a android.intent.action.VIEW -d "file:///storage/emulated/0/ROMs/Gahvoria/Pokemon Anil.gahvoria"
```

Si el juego no se puede abrir (falta el acceso a archivos, la carpeta se movió, el motor aún no se juega), Gahvoria se abre y lo explica con un aviso. Al salir del juego, Android vuelve al launcher.

## Lo que Gahvoria no hace

- No agrega a la biblioteca los juegos que se abren desde fuera; solo juega.
- No borra ni mueve juegos, accesos ni partidas.
