# Publicar OTG POWER BRIDGE desde Termux

El propietario autoriza publicar en GitHub el proyecto de GENAI INDUSTRIES bajo `luis171987/OTG-POWER-BRIDGE`.

## Requisitos
- Tener `git`, `gh`, `unzip` y una sesión de GitHub autorizada con `gh auth login` en la cuenta `luis171987`.
- Descargar el ZIP oficial del proyecto a `Download` de Android.
- No se requiere conectar la memoria USB ni el power bank para publicar el código.

## Ejecución
Desde la carpeta extraída, ejecutar:

```bash
bash scripts/PUBLICAR_GITHUB_TERMUX.sh --publicar
```

**Comportamiento conservador:**
1. Comprueba la cuenta autenticada y que la imagen social exista.
2. Si el repositorio no existe, intenta crearlo público y subir la rama `main`.
3. Si ya existe y es público, lo clona y actualiza los archivos del proyecto sin borrar otros archivos ni reescribir el historial.
4. Si el `push` normal falla por una protección de rama, intenta subir una rama nueva y crear una pull request. **Nunca** realiza `force push`.
5. No instala APK ni publica una Release. La compilación, la firma y las pruebas reales son fases independientes.

## Lo que falta hacer manualmente en GitHub

En **Settings → Social preview → Edit → Upload an image** subir `assets/SOCIAL_PREVIEW_OTG_POWER_BRIDGE_1280x640.jpg`.

Los metadatos de la página del repositorio dependen de esta configuración de GitHub. Copiar la imagen al repositorio por sí solo **no** modifica la miniatura social que se muestra al compartir por WhatsApp/Facebook.

## Validación
- Repositorio: `https://github.com/luis171987/OTG-POWER-BRIDGE` **solo después de la publicación**.
- Estado: comprobar que es `Public` y que el README muestra el logo y las cuatro ilustraciones.
- Código: GitHub Actions puede compilar un APK debug candidato; **no** equivale a un APK público de producción.
- Seguridad: se excluyen archivos personales, credenciales, keystores y APK sin certificar.
