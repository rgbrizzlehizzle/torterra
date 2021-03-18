package org.leafygreens.torterra

import kotlinx.serialization.*
import kotlinx.serialization.json.*

// Models -> https://www.terraform.io/docs/cli/commands/providers/schema.html#providers-schema-representation

@Serializable
data class ProviderSchemaWrapper(val formatVersion: String, val providerSchemas: Map<String, ProviderSchema>)

@Serializable
data class ProviderSchema(
    val provider: SchemaRepresentation,
    val resourceSchemas: Map<String, SchemaRepresentation>,
    val dataSourceSchemas: Map<String, SchemaRepresentation>
)

@Serializable
data class SchemaRepresentation(
    val version: Int,
    val block: BlockRepresentation
)

@Serializable
data class BlockRepresentation(
    val attributes: Map<String, BlockAttributes>,
    val blockTypes: Map<String, BlockType>
)

@Serializable
data class BlockAttributes(
    val type: String,
    val description: String,
    val required: Boolean = false,
    val optional: Boolean = false,
    val computed: Boolean = false,
    val sensitive: Boolean = false
)

@Serializable
enum class NestingMode {
    single,
    list,
    set,
    map
}

@Serializable
data class BlockType(
    val nestingMode: NestingMode,
    val block: BlockRepresentation,
    // todo only allowed on the list and set modes... how to represent programmatically?
    val minItems: Int?,
    val maxItems: Int?
)