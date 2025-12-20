# 📅 TaskFlow - Gestor de Tareas y Calendario

> Aplicación de escritorio para gestión integral de tareas con vista de calendario, desarrollada con JavaFX

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-24.0.1-blue?style=for-the-badge&logo=oracle)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📸 Capturas de Pantalla

### Vista Principal - Calendario Mensual
<img width="1919" alt="Vista Principal del Calendario" src="https://github.com/user-attachments/assets/21f3d0b3-362f-4759-9687-bd5b4e527918" />

### Crear Nueva Tarea
<img width="622" alt="Modal de Creación de Tareas" src="https://github.com/user-attachments/assets/7131ac36-7580-4d22-b063-ca36780673c7" />

---

## ✨ Características Principales

### 🗓️ Gestión de Tareas
- ✅ Crear, editar y eliminar tareas
- 📝 Tareas con nombre y descripción opcional
- 📅 Fechas de inicio y fin personalizables
- ⚡ Tres niveles de prioridad (Importante, Normal, Baja)
- 🔄 Estados de tarea: Pendiente, En Progreso, Completada, Vencida

### 📆 Vista de Calendario
- 📊 Visualización mensual interactiva
- 🔍 Búsqueda de tareas en tiempo real
- 📋 Panel lateral con resumen de tareas:
  - Tareas pendientes
  - Tareas en progreso
  - Tareas completadas
  - Tareas vencidas
- ⏰ Lista de próximas tareas ordenadas por fecha

### 💾 Persistencia de Datos
- 🗄️ Almacenamiento permanente de tareas
- 🔄 Carga automática al iniciar la aplicación
- 💿 Guardado automático de cambios

### 🎨 Interfaz de Usuario
- 🖥️ Interfaz intuitiva y limpia
- 🔘 Botones de navegación de calendario (día, semana, mes, año)
- 👤 Sistema de usuarios (Usuario Demo)
- 📱 Ventanas modales para creación/edición

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| **Java** | 17+ | Lenguaje principal |
| **JavaFX** | 24.0.1 | Framework de UI (Controls, FXML, Graphics) |
| **iCal4j** | Latest | Manejo de eventos y calendarios |
| **JavaFX Scene Builder** | - | Diseño visual de interfaces FXML |
| **IntelliJ IDEA** | - | IDE de desarrollo |

---

## 📋 Requisitos Previos

Antes de ejecutar este proyecto, asegúrate de tener instalado:

- [Java JDK 17+](https://www.oracle.com/java/technologies/downloads/)
- [JavaFX SDK 24.0.1](https://gluonhq.com/products/javafx/)
- IDE recomendado: [IntelliJ IDEA](https://www.jetbrains.com/idea/) o [Eclipse](https://www.eclipse.org/)

---

## 🚀 Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/XxGisussxX/JavaFX-TaskManager.git
cd JavaFX-TaskManager
```

### 2. Configurar JavaFX

Descarga JavaFX SDK y configura la ruta en tu IDE:

**IntelliJ IDEA:**
```
File → Project Structure → Libraries → + → Java
Selecciona la carpeta /lib del JavaFX SDK
```

**Eclipse:**
```
Project → Properties → Java Build Path → Libraries → Add External JARs
Selecciona todos los .jar de /lib del JavaFX SDK
```

### 3. Configurar VM Options

Agrega estas VM options en tu configuración de ejecución:

```bash
--module-path "C:/ruta/a/javafx-sdk-24.0.1/lib" 
--add-modules javafx.controls,javafx.fxml 
--enable-native-access=javafx.graphics
```

> ⚠️ Reemplaza la ruta con tu ubicación real del JavaFX SDK

### 4. Ejecutar la Aplicación

```bash
# Desde tu IDE, ejecuta la clase MainApp
# O desde terminal (con Java correctamente configurado):
java --module-path /ruta/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar TaskFlow.jar
```

---

## 🏗️ Arquitectura del Proyecto

```
JavaFX-TaskManager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org.example/
│   │   │       ├── controller/
│   │   │       │   ├── CalendarioController.java
│   │   │       │   ├── CrearTareaController.java
│   │   │       │   └── SesionUsuario.java
│   │   │       ├── model/
│   │   │       │   ├── Calendario.java
│   │   │       │   ├── Estado.java
│   │   │       │   ├── Prioridad.java
│   │   │       │   ├── Tarea.java
│   │   │       │   └── Usuario.java
│   │   │       ├── service/
│   │   │       │   ├── BaseDatos.java
│   │   │       │   └── CalendarioService.java
│   │   │       └── MainApp.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── VistaPrincipal.fxml
│   │       │   └── CrearTarea.fxml
│   │       ├── css/
│   │       │   ├── calendario-styles.css
│   │       │   └── styles.css
│   │       └── images/
│   └── test/
├── target/
└── .gitignore
```

### Patrones de Diseño Implementados

- **MVC (Model-View-Controller)**: Separación clara entre lógica de negocio, controladores y vistas FXML
- **Service Layer**: Capa de servicios (`BaseDatos`, `CalendarioService`) para lógica de negocio
- **Singleton**: Para gestión de sesión de usuario (`SesionUsuario`)
- **Enums**: Para estados (`Estado`) y prioridades (`Prioridad`) type-safe
- **Observer Pattern**: JavaFX Properties para actualización reactiva de la UI

---

## 🎯 Funcionalidades Detalladas

### Estados de Tarea

| Estado | Descripción | Color |
|--------|-------------|-------|
| 🟡 **Pendiente** | Tarea recién creada | Amarillo |
| 🔵 **En Progreso** | Tarea en desarrollo | Azul |
| 🟢 **Completada** | Tarea finalizada | Verde |
| 🔴 **Vencida** | Tarea pasada de fecha | Rojo |

### Prioridades

- **⚡ Importante**: Tareas críticas (color destacado)
- **📌 Normal**: Tareas estándar
- **📎 Baja**: Tareas no urgentes

---

## 🐛 Solución de Warnings Conocidos

Al ejecutar, verás algunos warnings relacionados con JavaFX 24. Son **advertencias de deprecación** del JDK, no afectan la funcionalidad:

```
WARNING: Restricted methods will be blocked in a future release
```

**Solución:** Ya incluida en VM options con `--enable-native-access=javafx.graphics`

```
WARNING: ical4j.properties not found
```

**Solución:** No requiere acción, la librería funciona con configuración por defecto.

---

## 📝 Ejemplos de Uso

### Crear una Tarea Nueva

1. Click en **"+ Nueva Tarea"**
2. Completa los campos:
   - Nombre: "Reunión con equipo"
   - Descripción: "Planificación sprint Q1"
   - Fecha Inicio: 20/12/2025
   - Fecha Fin: 20/12/2025
   - Prioridad: Importante
3. Click en **"Guardar"**

### Buscar Tareas

Usa la barra de búsqueda en la parte superior para filtrar tareas por nombre en tiempo real.

### Cambiar Vista del Calendario

Usa los botones **Día | Semana | Mes | Año** para cambiar la visualización.

---

## 🔮 Próximas Mejoras (Roadmap)

- [ ] Notificaciones de tareas vencidas
- [ ] Exportar/Importar tareas (JSON, CSV)
- [ ] Modo oscuro
- [ ] Estadísticas y gráficos de productividad
- [ ] Sincronización en la nube
- [ ] Subtareas anidadas
- [ ] Etiquetas personalizadas
- [ ] Filtros avanzados

---

## 💡 Aprendizajes del Proyecto

Este proyecto me permitió dominar:

- ✅ **JavaFX**: Layouts, FXML, Controllers, Scene Builder
- ✅ **Persistencia**: Manejo de almacenamiento de datos en aplicaciones de escritorio
- ✅ **UI/UX**: Diseño de interfaces intuitivas y responsivas
- ✅ **Arquitectura**: Patrones MVC y separación de responsabilidades
- ✅ **Manejo de fechas**: Librerías de calendario y zona horaria (iCal4j)
- ✅ **Event Handling**: Eventos de usuario y actualización reactiva de vistas
- ✅ **Type Safety**: Uso de Enums para estados y prioridades

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea tu rama (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.

---

## 👤 Autor

**Jesús David Santamaría Díaz**

- 🌐 GitHub: [@XxGisussxX](https://github.com/XxGisussxX)
- 💼 LinkedIn: [jesus-santamaria](https://www.linkedin.com/in/jesus-santamaria-4816381b0/)
- 📧 Email: jesussantamariadiaz299@gmail.com
- 📍 Ubicación: Cali, Colombia

---

## 🙏 Agradecimientos

- [JavaFX Documentation](https://openjfx.io/) - Documentación oficial de JavaFX
- [iCal4j Library](https://www.ical4j.org/) - Librería para manejo de calendarios
- [Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/) - Tutoriales oficiales de Java

---

<div align="center">

⭐ **Si este proyecto te fue útil, considera darle una estrella** ⭐

Hecho con ❤️ y ☕ en Cali, Colombia

</div>
