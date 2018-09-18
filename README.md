
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](/LICENSE)

# KotlinX Metadata Proof-of-Concept

This is a POC for [kotlinx-metadata](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlinx-metadata/jvm) library from JetBrains.
This library gives you an ability to access `@Metadata` annotation's data without messing with its Protobuf format.
 

The `@Metadata` annotation contains information about Kotlin specific features that would otherwise be lost when compiling Kotlin to Java, and are especially useful when developing annotation processors.

This POC uses a migrated `data-class-with` sample from [Eugenio Marletti](https://twitter.com/workingkills)'s [kotlin-metadata](https://github.com/Takhion/kotlin-metadata) as an example annotation processor that
can be written using [kotlinx-metadata](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlinx-metadata/jvm).
