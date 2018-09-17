plugins {
    kotlin("jvm")
    kotlin("kapt")
}

apply { from("generated-kotlin-sources.gradle.kts") }

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx/")
}

dependencies {
    compile(kotlin("stdlib"))
    compile(project(":data-class-with:api"))
    kapt(project(":data-class-with:processor"))
}
