package org.leafygreens.torterra

import com.lordcodes.turtle.shellRun
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Torterra {

    private const val NAME_MARKER = "{NAME}"
    private const val SOURCE_MARKER = "{SOURCE}"
    private const val VERSION_MARKER = "{VERSION}"

    private const val TERRAFORM_INIT = "terraform init"
    private const val TERRAFORM_PROVIDER_SCHEMA = "terraform providers schema -json"

    private val providerTemplate = """
            terraform {
              required_providers {
                $NAME_MARKER = {
                  source = "$SOURCE_MARKER"
                  version = "$VERSION_MARKER"
                }
              }
            }
        """.trimIndent()

    @OptIn(ExperimentalPathApi::class)
    fun generate(providerLookup: ProviderLookup) {
        val providerDir = createTempDirectory("torterra-${providerLookup.name}-").toFile()
        val template = providerTemplate
            .replace(NAME_MARKER, providerLookup.name)
            .replace(SOURCE_MARKER, providerLookup.source)
            .replace(VERSION_MARKER, providerLookup.version)
        val mainTf = File("${providerDir.absolutePath}/main.tf")
        mainTf.createNewFile()
        mainTf.writeText(template)
        TERRAFORM_INIT.runCommand(providerDir)
        val schemaFile = File("${providerDir.absolutePath}/schema.json")
        schemaFile.createNewFile()
        TERRAFORM_PROVIDER_SCHEMA.runCommandToFile(providerDir, schemaFile)
    }

    private fun String.runCommand(workingDir: File) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
    }

    private fun String.runCommandToFile(workingDir: File, writeTo: File) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(writeTo)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
    }

}

fun main() {
    val testProvider = ProviderLookup("docker", "kreuzwerker/docker", "2.11.0")
//    val testProvider = ProviderLookup("aws", "hashicorp/aws", "~> 3.0")
    Torterra.generate(testProvider)
}
