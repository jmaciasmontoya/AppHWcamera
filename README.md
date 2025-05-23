# AppHWcamera

Una aplicación de cámara para Android desarrollada con Kotlin y Jetpack Compose.

## Características

- Captura de fotos usando la cámara del dispositivo
- Interfaz moderna con Material Design 3
- Manejo de permisos de cámara y almacenamiento
- Guardado automático de fotos en la galería
- Vista previa de la cámara en tiempo real

## Requisitos

- Android Studio Hedgehog | 2023.1.1 o superior
- Kotlin 1.9.0 o superior
- Android SDK 34 o superior
- Gradle 8.0 o superior

## Tecnologías Utilizadas

- Kotlin
- Jetpack Compose
- Material Design 3
- CameraX
- Accompanist Permissions
- Coil

## Configuración

1. Clona el repositorio:
```bash
git clone https://github.com/jmaciasmontoya/AppHWcamera.git
```

2. Abre el proyecto en Android Studio

3. Sincroniza el proyecto con los archivos Gradle

4. Ejecuta la aplicación en un dispositivo o emulador

## Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/apphwcamera/
│   │   │   ├── MainActivity.kt
│   │   │   └── CameraViewModel.kt
│   │   └── res/
│   └── test/
└── build.gradle.kts
```

## Uso

1. Al iniciar la aplicación, se solicitarán los permisos necesarios
2. Una vez concedidos los permisos, se mostrará la vista de la cámara
3. Presiona el botón "Tomar Foto" para capturar una imagen
4. La foto se guardará automáticamente en la galería del dispositivo

## Contribuir

Las contribuciones son bienvenidas. Por favor, sigue estos pasos:

1. Haz un Fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

Tu Nombre - [@tu_twitter](https://twitter.com/tu_twitter)

Link del Proyecto: [https://github.com/TU_USUARIO/AppHWcamera](https://github.com/TU_USUARIO/AppHWcamera) 
