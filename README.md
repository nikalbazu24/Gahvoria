# Gahvoria

App para jugar fan games de **RPG Maker** y **Pokémon Essentials** en Android (teléfonos y consolas como la AYN Thor).
Hecha desde cero; el motor de juego es [mkxp-z](https://github.com/mkxp-z/mkxp-z), reutilizado como componente.

## Estructura

| Carpeta | Qué contiene |
|---|---|
| `app/` | La app: Kotlin + Jetpack Compose. Biblioteca, Añadir, Actualizar, Ajustes y opciones de cada juego. |
| `app/src/main/java/.../core/` | Lógica sin Android (detección de motor, biblioteca, partidas, paletas, versiones). Con pruebas. |
| `engine/` | El motor mkxp-z (moderno con Ruby 3.1 y clásico con Ruby 1.8), sus controles táctiles y los scripts de compatibilidad. La app solo lo usa a través de `GameActivity`. |
| `tools/` | Comprobación del APK, manifiesto del actualizador e icono. |
| `docs/` | Plan por fases y reglas. |

## Cómo se publica

Cada cambio en `main` compila el APK en GitHub Actions, corre las pruebas, comprueba que estén los dos motores
y lo publica en **Releases → dev**. La app revisa esa versión para actualizarse (siempre con confirmación).

Una versión se vuelve **estable** cuando se decide: en Actions → «Promover a estable» se copia lo que hay en
`dev` a **Releases → stable** (la que enlaza «Latest»). Quien elige «Solo versiones estables» en la app recibe
solo esas. Por ahora el canal por defecto sigue siendo `dev`.

Descarga para jugadores: https://github.com/Nikalbazu24/Gahvoria/releases/latest (existe cuando hay una versión estable).

## Licencias

Gahvoria se distribuye bajo GPL 2.0, como mkxp-z. Montserrat: SIL Open Font License 1.1.
