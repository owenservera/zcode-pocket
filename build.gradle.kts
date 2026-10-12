plugins {
    id("com.android.application") version "8.11.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
}

// Build outputs and per-project caches land under ".out/" (and Gradle's own
// ".gradle/") — dot-prefixed directories. The playbook's hash.mjs excludes
// dotfiles because they are installed artefacts, not source, so a build can
// never make the unit look drifted.
allprojects {
    layout.buildDirectory.set(file("${rootProject.projectDir}/.out/${project.name}"))
}
