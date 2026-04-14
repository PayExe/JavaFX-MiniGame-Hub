plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // SQLite JDBC driver for database persistence
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
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
