# Compartir OTG POWER BRIDGE en WhatsApp y Facebook

El logo oficial de OTG POWER BRIDGE está incluido en la raíz visual del `README.md` y en el archivo `assets/LOGOTIPO_OTG_POWER_BRIDGE_OFICIAL.jpg`.

## Vista previa automática de enlaces

Para que al compartir **el enlace del repositorio** se vea el logo, hay que subir una imagen a la opción **Social preview** del propio repositorio GitHub. Dejar el JPG dentro del repositorio **no configura automáticamente** los metadatos de la página.

1. Tras publicar el repositorio público, abrir `https://github.com/luis171987/OTG-POWER-BRIDGE` e iniciar sesión como propietario.
2. Abrir **Settings** → **General** → **Social preview** → **Edit** → **Upload an image...**.
3. Seleccionar `assets/SOCIAL_PREVIEW_OTG_POWER_BRIDGE_1280x640.jpg` (1280 × 640 píxeles, JPG, menos de 1 MB).
4. Guardar; verificar primero el aspecto de la vista previa del repositorio y después compartir `https://github.com/luis171987/OTG-POWER-BRIDGE` por WhatsApp y Facebook.
5. Si una plataforma sigue mostrando una miniatura antigua, esperar a que actualice su caché de enlaces. Las plataformas deciden si muestran o no esa vista previa; no se garantiza su apariencia idéntica.

> **No compartir** un enlace que todavía no esté publicado. El enlace de GitHub Releases a un APK sólo será válido después de subir un instalador firmado y probado. No hay APK público validado hasta la fecha.

## Archivos publicados

- `README.md`: portada, logo, caso real de uso, tutorial con imágenes y funciones del prototipo.
- `assets/SOCIAL_PREVIEW_OTG_POWER_BRIDGE_1280x640.jpg`: imagen optimizada para compartir enlaces.
- `assets/01_conexion_general_3d.png` a `04_panel_android_3d.png`: ilustraciones de conexión, tutorial y frontend.
- `app/`: código fuente Android Kotlin/Compose.

## Seguridad y expectativas

La app no transforma un power bank en hub de datos. Hace falta un hub USB-C compatible con **carga PD passthrough + puerto USB de datos** y un teléfono cuyo hardware/firmware permita simultaneidad. Las ilustraciones muestran un concepto, no una certificación de funcionamiento en un modelo particular. No se publica una base de datos con archivos privados de usuarios.

Documentación oficial: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/customizing-your-repositorys-social-media-preview
