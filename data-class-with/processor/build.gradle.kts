plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx/")
}

dependencies {
    compile(kotlin("stdlib"))
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    compile(project(":data-class-with:api"))
    compile("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.0.4")
}
