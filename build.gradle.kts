plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
    id("com.github.node-gradle.node") version "latest.release"
    id("org.openrewrite.build.moderne-proprietary-license") version "latest.release"
}

// Set as appropriate for your organization
group = "org.openrewrite.recipe"
description = "Migrate JavaScript projects using codemods"

val rewriteVersion = rewriteRecipe.rewriteVersion.get()
dependencies {
    implementation(platform("org.openrewrite:rewrite-bom:$rewriteVersion"))

    implementation("org.openrewrite:rewrite-core")

    testImplementation("org.openrewrite:rewrite-test")
}

license {
    exclude("**/package.json")
    exclude("**/package-lock.json")
}

node {
    nodeProjectDir.set(file("build/resources/main/codemods"))
}

tasks.named("npmInstall") {
    dependsOn(tasks.named("processResources"))
}

tasks.named("classes") {
    dependsOn(tasks.named("npmInstall"))
}
