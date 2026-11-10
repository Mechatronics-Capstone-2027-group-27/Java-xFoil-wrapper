plugins {
    java
}

group = "matz.xfoil"
version = "1.1.0"

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }

    create("examples") {
        java.setSrcDirs(listOf("examples"))

        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

tasks.register<JavaExec>("runExample") {
    classpath = sourceSets["examples"].runtimeClasspath
    mainClass.set("main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.jar {
    archiveBaseName.set("xfoil")
}
