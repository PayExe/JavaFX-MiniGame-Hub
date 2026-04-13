plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // JavaFX modules are managed by the plugin, but you can override versions here if needed
}

javafx {
    version = "25"
    modules(
        "javafx.base",
        "javafx.graphics",
        "javafx.controls",
        "javafx.fxml",
        "javafx.media",
        "javafx.web",
        "javafx.swing"
    )
}

application {
    mainClass = "dev.skypaolo.App"
}

tasks.test {
    useJUnitPlatform()
}
