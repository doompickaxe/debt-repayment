plugins {
    java
    alias(libs.plugins.quarkus)
    alias(libs.plugins.openapi.generator)
}

group = "com.example"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val openApiSpec = rootProject.layout.projectDirectory.file("openapi/debt-repayment-api.yaml")
val generatedServerDir = layout.buildDirectory.dir("generated/openapi")

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.rest)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.hibernate.validator)
    implementation(libs.quarkus.smallrye.openapi)
    implementation(libs.quinoa)

    testImplementation(libs.quarkus.junit)
    testImplementation(libs.rest.assured)
}

// Generate JAX-RS interfaces + models from the shared OpenAPI spec. Resources implement the interfaces.
openApiGenerate {
    generatorName = "jaxrs-spec"
    inputSpec = openApiSpec
    outputDir = generatedServerDir
    apiPackage = "com.example.debtrepayment.api"
    modelPackage = "com.example.debtrepayment.api.model"
    cleanupOutput = true
    configOptions = mapOf(
        "interfaceOnly" to "true",
        "useJakartaEe" to "true",
        "useSwaggerAnnotations" to "false",
        "useBeanValidation" to "true",
        "returnResponse" to "false",
        "useTags" to "true",
        "openApiNullable" to "false",
        "dateLibrary" to "java8",
        "sourceFolder" to "src/main/java",
        "hideGenerationTimestamp" to "true",
    )
}

sourceSets {
    main {
        java.srcDir(generatedServerDir.map { it.dir("src/main/java") })
    }
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

// Quarkus scans sources before compileJava; make sure generated code exists by then.
tasks.named("quarkusGenerateCode") { dependsOn(tasks.openApiGenerate) }
tasks.named("quarkusGenerateCodeDev") { dependsOn(tasks.openApiGenerate) }

// Serve the spec itself (not an annotation-scanned one) via SmallRye OpenAPI / Swagger UI.
tasks.processResources {
    from(openApiSpec) {
        into("META-INF")
        rename { "openapi.yaml" }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
