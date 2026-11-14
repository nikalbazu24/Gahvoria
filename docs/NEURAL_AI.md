# Gahvoria Neural AI · Fase 1 experimental

Base auditada: `5a204b8b0ead29c66892beb2c2508f5c152da408`. Revisados `CLAUDE.md`, `docs/PLAN.md`, el arranque en `Launcher` y `GameActivity`, el servicio, el script de Ruby y las operaciones de memoria en `Translations`.

## Auditoría y cambios

| Hallazgo | Cambio experimental |
| --- | --- |
| Java borraba todos los archivos de solicitud antes de obtener respuesta. Un fallo afectaba al lote completo y podía perder todas las peticiones de ese turno. | Procesamiento de hasta ocho frases por solicitud. Las solicitudes con error se vuelven a intentar en la sesión con pausa y vencimiento. Después, una nueva consulta de Ruby puede volver a pedir la frase. |
| El servicio marcaba frases como conocidas antes de escribirlas. Una salida vacía, con códigos dañados o igual al original quedaba grabada como solución definitiva. El lector también podía aceptar una última línea incompleta. | La memoria registra traducciones que cambiaron y pasaron la validación. El índice se actualiza después de sincronizar el lote. Se recuperan filas UTF-8 completas y se recorta una cola incompleta. Las filas antiguas inválidas se ignoran al leer. |
| Ruby leía directamente el histórico completo y guardaba por valor todos los textos traducidos. Una frase original podía coincidir con una traducción anterior y quedar bloqueada. | Ruby recibe respuestas validadas en un buzón de sesión y conserva hasta 4096 entradas activas. Se evita reprocesar el mismo texto dentro de las llamadas de visualización. Los diálogos tienen una espera de hasta 350 ms y los menús devuelven el original de inmediato cuando falta respuesta. |
| DeepL sin clave bloqueaba incluso los aciertos del histórico. La detección de idioma en ML Kit recibía marcadores y una descarga fallida podía dejar el cliente sin volver a preparar su modelo. | Primero se sirve la memoria. DeepL queda como opción de transición. ML Kit detecta sobre el texto original y reintenta la preparación del modelo sin bloquear el trabajador durante toda la descarga. Se quitó el bloqueo previo basado en la presencia del paquete de Google Play, que impedía siquiera intentar la ruta dinámica. Verificar el funcionamiento en equipos sin Google en la prueba física. |
| Se restauraban marcadores duplicados o reordenados y se podía incorporar un código nuevo sin detectarlo. | Validación estricta antes de publicar o persistir respuestas. Se protegen códigos reconocidos de RPG Maker, etiquetas, saltos, tabulaciones y variables habituales. |

La memoria conserva la ubicación `filesDir/translations/<juego>/<idioma>.tsv`. El escritor vive en el proceso `:game` y el buzón temporal está en `cacheDir/translation-session`. Abrir de nuevo limpia el buzón temporal y vuelve a consultar el histórico al necesitar cada frase. El directorio sigue derivándose del nombre y la ruta de la instalación. Mover o renombrar el juego puede separar su histórico y dos rutas con el mismo hash de 32 bits y el mismo nombre podrían coincidir. El menú existente de limpieza y el borrado explícito de un juego conservan su comportamiento. No se escriben partidas, carpetas del juego ni configuraciones de pantalla.

El contrato actual `TranslationBatch.Provider` recibe las frases originales y sus equivalentes protegidos, en orden. Devuelve una salida por frase o un valor nulo para una frase que no pudo traducirse. El primer adaptador neuronal usará este punto. Las claves de memoria de esta fase siguen siendo juego, idioma destino y texto original exacto. Para ampliar a escenas se requiere una nueva versión de protocolo y de memoria.

## Candidatos investigados

| Candidato | Licencia y requisitos | Decisión |
| --- | --- | --- |
| [Mozilla Firefox Translations](https://github.com/mozilla/translations) + [Bergamot](https://github.com/browsermt/bergamot-translator), inglés→español | MPL 2.0 para el modelo y Bergamot. El [registro oficial](https://storage.googleapis.com/moz-fx-translations-data--303e-prod-translations-data/db/models.json) lista el modelo de 31,25 M parámetros y 31,56 MB descomprimidos, más vocabulario SentencePiece y lista léxica. Hace falta comprobar compilación ARM64 y medir el coste del runtime y de las dependencias en Android. | Primer prototipo local. Preparar JNI, construir con el NDK del repo y probar CPU con un trabajador. Revisar licencias de dependencias y obligaciones de distribución antes de empaquetar. |
| [Helsinki-NLP OPUS-MT en→es](https://huggingface.co/Helsinki-NLP/opus-mt-en-es) | La ficha indica Apache 2.0. El [archivo PyTorch](https://huggingface.co/Helsinki-NLP/opus-mt-en-es/tree/main) pesa 312 MB. Evaluar exportación a [ONNX Runtime Mobile](https://onnxruntime.ai/docs/tutorials/mobile/), INT8 y un decodificador con caché. El tamaño y la RAM de la exportación se deben medir. | Referencia ligera de desarrollo y opción de ajuste mediante entrenamiento supervisado o adaptadores donde sean compatibles. Verificar licencias del checkpoint, runtime y datos por separado. |
| [Helsinki-NLP OPUS-MT tc-big en→es](https://huggingface.co/Helsinki-NLP/opus-mt-tc-big-en-es) | CC BY 4.0 según su ficha. [Safetensors](https://huggingface.co/Helsinki-NLP/opus-mt-tc-big-en-es/tree/main) de 466 MB antes de comprimir o cuantizar. | Comparación de calidad y posible base de entrenamiento. Medir RAM, espacio e inferencia antes de considerar distribución móvil. |
| [NLLB-200 distilled 600M](https://huggingface.co/facebook/nllb-200-distilled-600M) | CC BY-NC 4.0. Solo los pesos INT8 de 600 M parámetros ocuparían del orden de 600 MB sin metadatos, vocabulario o activaciones. | Excluido del paquete inicial por licencia no comercial y presupuesto móvil. |
| [MADLAD-400 3B MT](https://huggingface.co/google/madlad400-3b-mt) | Apache 2.0 según su ficha. 3.000 M de parámetros equivalen a aproximadamente 1,5 GB de pesos a 4 bits, antes del runtime. | Referencia de laboratorio para investigación de multilingüismo y destilación. |

Para cualquier runtime integrado, revisar [MPL y licencias secundarias](https://www.mozilla.org/en-US/MPL/2.0/FAQ/) y [la compatibilidad de Apache 2.0 con GPLv2](https://www.gnu.org/licenses/license-list.html#apache2). El repositorio está bajo GPLv2. Publicar paquetes de pesos con versión, hash SHA-256, avisos, tokenizer y origen verificable. Descargar una vez con consentimiento o permitir una instalación manual. La inferencia debe funcionar en modo avión y no usar cuentas, claves ni servicios de Google.

## Siguiente trabajo para Claude o Codex

1. Probar Bergamot con los artefactos en→es fijados por versión. Medir el runtime CPU arm64 y su consumo mientras corre mkxp-z. Encapsular carga, cancelación e inferencia en un adaptador de `TranslationBatch.Provider`. Si el modelo no está instalado, servir la memoria y mostrar el original en los fallos de caché.
2. Añadir al protocolo versionado el idioma fuente y, cuando exista un dato fiable, el tipo de texto, mapa, evento, personaje y las líneas previas de la misma conversación. Separar diálogos, menús y nombres. Usar primero un glosario por juego y una búsqueda de memoria con contexto exacto. Reutilizar la memoria anterior solo donde resulte seguro. Investigar los hooks nativos de RPG Maker sin Pokémon Essentials por separado. La imagen de la escena y el OCR quedan para una fase posterior, con captura puntual y comparación de coste y calidad.
3. Curar un corpus bilingüe con diálogos originales o autorizados, glosarios propios y marcadores del motor. Separar entrenamiento, validación y prueba por juego o escena. Para Bergamot, ensayar el flujo [Marian de Mozilla](https://github.com/mozilla/translations), con ajuste desde un checkpoint entrenable disponible y cuantización al exportar, o entrenamiento/destilación de un modelo compacto. Para OPUS-MT, comparar ajuste supervisado completo con adaptadores antes de exportar a INT8. Conservar configuración, semilla, licencias y fichas de evaluación. No publicar scripts privados ni asumir que se pueden reutilizar salidas de servicios externos para entrenar.
4. Evaluar inglés→español con escenas inéditas y traducciones humanas autorizadas. Comparar a ciegas con ML Kit y, si hay acceso autorizado, DeepL. Registrar fidelidad, voz del personaje, terminología, concordancia, conservación de códigos, chrF++ y COMET. No inferir paridad por una puntuación de corpus general.
5. Tomar como piso inicial un teléfono ARM64 de 4 GB con núcleos de bajo consumo y Android de una sola pantalla. Repetir en AYN Thor con una y dos pantallas. Presupuestos provisionales: paquete completo ≤65 MB, RAM adicional ≤150 MB, p95 en caché ≤200 ms y primera traducción en caliente ≤700 ms para diálogo corto. Medir p50/p95, arranque en frío, memoria máxima, caída de FPS y temperatura en 20 minutos. Repetir sin red y con el juego guardado en una copia. Si el modelo no llega dentro del presupuesto de espera del diálogo, se muestra el original y se guarda la respuesta válida para la siguiente consulta.

## Pruebas

- El flujo **Translation checks** verifica la capa Java con JDK 17 y destino Java 8. Incluye tokens, respuestas inválidas, filas antiguas, escritura fallida, recuperación de una cola incompleta, aislamiento y una partida centinela.
- El mismo flujo prueba el buzón de Ruby 3.1 con diálogo, menús, una respuesta incompleta y el traductor apagado. El flujo habitual del APK conserva los dos motores.
- Prueba manual pendiente en teléfono y Thor: Essentials clásico y moderno, memorizar un diálogo, relanzar sin red, cambiar idioma y alternar pantalla. Verificar que la misma partida abre antes y después. No se ha desplegado un modelo neuronal ni medido su rendimiento.

## Corrección tras la prueba con Solar Eclipse

Solar Eclipse (RPG Maker XP y Essentials 19.1) siguió mostrando el original en la Thor con el proveedor local. El proceso `:game` no ejecutaba el inicializador automático de ML Kit del proceso principal. El servicio lo inicializa una vez en ese proceso, antes de crear clientes de idioma o traducción. Se refresca el aviso al volver del juego y se distingue falta de texto, preparación de idioma, fallo y aciertos de memoria. La prueba en la Thor está pendiente de repetir con el APK nuevo.

En **Ajustes → Traducción → Con qué traducir**, la opción **Gahvoria Neural AI** activa la ruta experimental. Mientras se integra el modelo, esta ruta reutiliza únicamente la memoria por juego e idioma y muestra el texto original cuando no encuentra una coincidencia. Las frases del modo experimental no se envían a DeepL ni a ML Kit. Para verificar la reparación del proveedor local en Solar Eclipse, elegir **En el teléfono (gratis)**.

## Actualizaciones de la beta

La primera compilación con el nuevo canal se instala manualmente encima de Gahvoria. En ella, **Actualizaciones → Todas las versiones** consulta la prepublicación `neural-beta`. **Solo versiones estables** sigue consultando el canal estable. Las compilaciones oficiales de desarrollo siguen consultando `dev`. Al cambiar de canal, Android solo permite instalar una versión con código superior.

El flujo normal compila y verifica ambos motores y publica en `neural-beta` al hacer push a `experimental/neural-ai-phase-1` si la firma de Gahvoria está disponible. Los pull requests producen artefactos de prueba, sin publicar en Releases. La beta no se marca como la última versión general del repositorio.

## Diálogos de introducción

El puente ahora cubre asignaciones directas a la ventana de diálogo de Pokémon Essentials cuando el juego muestra texto letra por letra. La captura habitual por `pbMessage` y `pbMessageDisplay` conserva prioridad para evitar traducir dos veces el mismo diálogo. Se amplía la espera acotada del diálogo a 650 ms y se mantienen los menús sin espera. La prueba de Ruby simula la asignación directa, una respuesta tardía y el redibujado. Falta comprobar la introducción real de Solar Eclipse en la Thor. Para frases nuevas usar **En el teléfono (gratis)**. **Gahvoria Neural AI** sigue consultando solo la memoria guardada.

## Modelo local inglés → español

La beta incluye Bergamot/Marian para **Android 9 o posterior, ARM64** y los tres archivos cuantizados del modelo Mozilla en→es 3.0. La compilación descarga unos 24 MB, fija sus hashes en `engine/neural/model-en-es.json` y verifica que el APK contenga esos mismos bytes. En el primer uso, el trabajador de traducción extrae y verifica aproximadamente 37 MB en el almacenamiento privado. El proceso del juego ejecuta la inferencia en CPU y consulta la memoria por juego antes del modelo. No necesita una red ni una clave. Se traducen los fragmentos de texto por separado cuando hay códigos RPG Maker, para conservarlos sin cambios. El modo Neural trata las frases nuevas como inglés y requiere español como destino.

La integración Android se apoya en el [adaptador MIT de WM Keyboard](https://github.com/wasi-master/wmkeyboard/tree/main/native/bergamot-jni) y compila [Mozilla Translations](https://github.com/mozilla/translations) desde una revisión fijada. Los archivos del modelo se obtienen del [catálogo de Firefox](https://firefox.settings.services.mozilla.com/v1/buckets/main/collections/translations-models-v2/records). Los avisos se incluyen en el APK. La build de prueba utiliza un trabajador y un espacio de trabajo configurado en 64 MB. Medir tamaño del proceso, latencia y FPS durante el juego antes de publicar como estable.

Prueba en dispositivo: abrir Solar Eclipse por primera vez en modo avión con **Gahvoria Neural AI** y **Español**, avanzar varios diálogos y repetir una escena. En la primera apertura, la extracción y carga del modelo pueden exceder el límite de espera de la primera línea. Revisar **Último aviso del traductor** si no aparece ninguna frase nueva en español. La validación de compilación y empaquetado en CI no sustituye esta prueba física.

## Menús, cuadros fijos y pantalla de inicio

El puente intercepta el texto antes del dibujo en los auxiliares de Essentials y cubre también `Bitmap#draw_text` de RGSS. Los cuadros fijos y selectores conocidos se redibujan cuando llega la traducción, sin cambiar la opción seleccionada. Un glosario breve aporta traducciones inmediatas al español para acciones comunes. El aviso de inicio se traduce como texto o se sustituye en memoria cuando el juego carga un gráfico de aviso reconocible en una carpeta de títulos. Otros textos incrustados en imágenes o dibujados con fuentes de sprites requieren cobertura específica u OCR. El arte y las partidas del juego quedan en su carpeta original.

Verificar en Solar Eclipse la pantalla de título, nueva partida, opciones, etiquetas fijas y selectores, en español y con Neural en modo avión. Las pruebas Ruby cubren las dos firmas de dibujo, redibujado, conservación de la selección y sustitución del gráfico de aviso sin tocar otros gráficos.

### Solar Eclipse 1.11.0

Las capturas de esta versión identifican dos láminas negras con texto blanco (créditos y aviso) y «PRESS ENTER». La beta reconoce las dos láminas por su composición en recursos de inicio y traduce los avisos de título dibujados como texto o sprites compatibles, incluyendo un lienzo transparente con el texto abajo. No edita las imágenes en disco. Los cuadros de opciones conservan las posiciones y anchos que el juego calcula para sus valores. Las traducciones se dibujan dentro de ese espacio. Las pruebas reproducen las firmas de estas láminas y los anchos de opciones. Falta comprobar los archivos de la edición instalada en el dispositivo.

## Preparación antes de jugar

Cuando un juego tiene traducción habilitada con **Gahvoria Neural AI → Español**, el motor recopila textos antes de ejecutar los scripts. Busca diálogos y opciones de mapas y eventos comunes RPG Maker, literales de las llamadas de traducción o diálogo y catálogos de mensajes de Essentials. Prioriza los textos de scripts y el mapa inicial. No reescribe archivos del juego ni toca las partidas. Lo ya traducido se reutiliza desde la memoria local por juego. Los resultados se guardan por lotes y se cargan antes del título, incluso al abrir con una preparación parcial.

La pantalla de carga incluye la etapa **Preparando traducción** entre los pasos de arranque cuando corresponde, muestra el avance y permite **Abrir con lo traducido**. Se escanean hasta 50.000 textos únicos o 24 MiB de solicitudes por arranque. El texto generado al jugar, los scripts que no exponen un literal y las imágenes sin localización específica siguen utilizando las rutas de traducción existentes cuando están disponibles. La primera pasada puede tomar varios minutos según el juego y el dispositivo.

### Arranque con preparación acotada

La preparación automática concede hasta 4 segundos al escaneo y deja el resto de un presupuesto de 12 segundos para que Neural traduzca los primeros lotes. Se comprueba el plazo entre archivos, comandos y tandas. Al agotarse el presupuesto, se abre el juego con los resultados disponibles y se evita esperar el resto del catálogo. Las traducciones completadas se reutilizan. Una operación individual de lectura o inferencia puede tardar más que su siguiente punto de comprobación. El trabajo completo desde la biblioteca queda para una fase separada.

Cada lanzamiento registra cuándo el motor entra en la preparación. Si Android registra un cierre anormal durante esa etapa, el lanzador reabre con el mismo motor y omite la preparación previa en ese intento. El reintento de motor conserva su regla habitual para errores del juego fuera de esta etapa.

## Preparación desde la biblioteca

- En las opciones del juego, abre **Pantalla → Preparar traducción**. También se puede pedir al añadir una carpeta. La preparación usa el modelo Neural inglés → español y activa la traducción para ese juego.
- El motor lee mapas, eventos, catálogos y literales antes de ejecutar scripts del fangame. El catálogo vive en el directorio privado de traducciones. La lectura vuelve a la biblioteca cuando acaba y el servicio de Android traduce por lotes en segundo plano con una notificación.
- **Pausar** y abrir un juego liberan el modelo en el límite del lote. **Reanudar** vuelve a recorrer el catálogo y omite frases con traducción guardada. La memoria se escribe por lote y sobrevive al cierre de la app. La revisión por nombres, tamaños y fechas de los archivos invalida el catálogo cuando cambian.
- El contador cubre las frases únicas detectadas por el extractor. Excluye las cadenas del glosario y no mide textos compuestos dinámicamente ni texto incrustado en imágenes. Una respuesta idéntica al original sigue pendiente en la memoria.
- Para probar en Android, prepara un juego, cambia de app, pausa desde la notificación, vuelve a la biblioteca y reanuda. Abre el juego durante la preparación y revisa la memoria y las partidas al volver. El rendimiento se mide en el dispositivo y depende del tamaño del catálogo.

## Contexto del texto durante el juego

- El puente distingue el diálogo, los rótulos de interfaz, los grupos de opciones y los roles de una placa de nombre. El glosario de un selector se elige por el conjunto de opciones. Los rótulos conocidos se resuelven antes de la memoria. Los nombres elegidos por el jugador conservan su forma y en mensajes sencillos se sustituyen por un marcador antes de pedir la traducción.
- Bergamot recibe cada intervención completa cuando tiene marcadores. Se envían como elementos de alineación internos y se restauran como códigos RGSS. Se comprueba que aparezcan en orden y que los controles iniciales sigan antes del texto. Si la alineación no conserva la estructura, se usa la traducción por fragmentos. Esta ruta funciona también desde la preparación en segundo plano.
- La memoria recalcula las entradas antiguas con códigos, variables o vocabulario normalizado la próxima vez que Neural las necesita. Las revisiones se guardan en el mismo archivo por juego, con una clave versionada. El cambio de revisión del catálogo hace que el próximo escaneo incluya las opciones literales de scripts detectadas por el extractor.
- Solar Eclipse sirve como caso de regresión. Pendiente de comprobar en el teléfono y en la Thor el encuadre de los selectores y la calidad de la alineación. El modelo recibe una intervención a la vez. El contexto entre escenas y el texto dibujado en imágenes requieren trabajo adicional.

### Apariencia, teclado y dificultad de Solar Eclipse

Los rótulos conocidos se resuelven también antes de medir y dividir el texto en los formateadores de Essentials. Así se pueden traducir encabezados y ayudas que el juego dibuja una sola vez. El texto que el usuario escribe se conserva durante el redibujado de las ventanas de entrada. El glosario incluye la confirmación de los combates más difíciles. El aviso de inicio tiene traducción para sus cadenas si el juego las dibuja como texto. Esta corrección no incorpora reconocimiento de imágenes.

## Contexto de escena y cobertura inicial

- El puente usa el enunciado de la pregunta para distinguir opciones de dirección y combate. También puede usar la última intervención del mismo mapa y evento durante 12 segundos para resolver expresiones breves. Cuando ambas líneas indican hablante, deben coincidir. Estas decisiones se toman en Ruby y se muestran directamente, de modo que una acepción no contamina la memoria de otras escenas.
- El escaneo da prioridad al mapa inicial. Al abrir el juego, el servicio pone en el buzón hasta 384 frases de ese catálogo que ya estén en memoria, sin ejecutar el modelo para esa carga. La preparación en segundo plano extrae también literales de confirmaciones, entrada de nombres y funciones de localización de Pokémon Essentials. El escáner distingue el encabezado de una ventana VX o VX Ace del texto que sigue. Los resultados anteriores permanecen disponibles.
- Se comprueba el flujo con eventos simulados. En dispositivo, verificar Solar Eclipse y Pokémon Insurgence desde una importación limpia, con especial atención a escenas con opciones, al texto previo al menú principal y al tiempo de arranque. Esta etapa procesa el texto que el juego entrega al motor. El arte rasterizado queda fuera de esta etapa.

## Prueba de primera apertura

La preparación escribe una copia acotada de las primeras traducciones disponibles del catálogo en el directorio privado del juego. Ruby carga esta copia antes de instalar los hooks, incluso si el servicio de traducción todavía está leyendo la memoria. El catálogo incluye también literales de ciertos formateadores y ventanas de texto. La notificación muestra el porcentaje de frases detectadas que tienen una traducción guardada.

1. Actualizar Gahvoria y añadir Pokémon Insurgence desde una carpeta que la app no haya preparado antes. Elegir Español y Gahvoria Neural AI.
2. Usar **Preparar traducción** desde las opciones del juego. Al terminar, anotar el porcentaje del catálogo y el tiempo aproximado de preparación.
3. Activar modo avión. Abrir el juego y revisar el título, el menú principal, el primer diálogo y sus opciones, y el primer menú dentro de la partida. Anotar cada pantalla como española, mixta o inglesa. Si tarda en traducirse, anotarlo también.
4. Cerrar y volver a abrir. Verificar que los primeros textos se reutilicen y que la partida existente siga disponible. Repetir con Solar Eclipse si se quiere comparar. No hace falta borrar los datos de Gahvoria.

El porcentaje del catálogo se calcula sobre las frases que detectó el escáner. La revisión visual mide las pantallas recorridas. La cobertura de una partida completa requiere un recorrido más amplio y la traducción de textos incrustados en imágenes.

### Cobertura desde la biblioteca

En las opciones de un juego con catálogo, abre **Pantalla → Cobertura del catálogo**. Recuenta las frases únicas con una traducción válida en la memoria española, incluso las guardadas al jugar después de la preparación. Muestra hasta 12 ejemplos pendientes para copiar. La revisión lee una instantánea de la memoria y no inicia el modelo. Las traducciones aplicadas solo al mostrar el texto y los textos en imágenes quedan fuera de la medición.

El escáner ahora incluye archivos Ruby de la carpeta Scripts directamente bajo el juego, además de los de Data/Scripts. Al pulsar **Preparar traducción** por primera vez tras actualizar, se regenera el catálogo anterior y se reutiliza la memoria. Si el escáner llega a su límite, la pantalla señala que el catálogo es parcial.
