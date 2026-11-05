# Protocolo físico de prueba segura

1. Sin power bank, conecta únicamente el hub OTG y un pendrive con **archivos de prueba** al teléfono; comprueba en Android que es legible.
2. Desconecta el pendrive antes de cualquier cambio de cableado. Conecta la salida **compatible** del power bank a la entrada PD IN del hub, nunca a la salida USB DATA.
3. Conecta el conjunto conforme a las instrucciones del fabricante del hub y comprueba que Android realmente carga, no solo muestra un cable conectado.
4. Verifica que el pendrive sigue visible y permite lectura; usa primero **solo lectura**.
5. Tras respaldar los datos fuera del USB, usa un archivo de prueba para una copia y verifica el hash; comprueba el resultado directamente desde Android.
6. Si aparece calor excesivo, mensajes de error o desconexiones, detén la prueba. Nunca empalmes cables, uses accesorios de reparación de batería ni intentes forzar USB PD por software.

Éxito físico: Android muestra carga, la memoria permanece accesible y se realiza una copia verificada sin interrupciones. La prueba se aplica **por modelo/tipo de hub/tipo de power bank**, no se generaliza a todos los Android.
