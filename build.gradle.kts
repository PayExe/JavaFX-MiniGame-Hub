plugins {
    application
    java
}

repositories {
    mavenCentral()
}

dependencies {}

application {
    mainClass = "app.App"
}

tasks.test {
    useJUnitPlatform()
}
