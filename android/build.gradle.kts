plugins {
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("com.google.gms.google-services") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}