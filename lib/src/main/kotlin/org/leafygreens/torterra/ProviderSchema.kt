package org.leafygreens.torterra

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Models -> https://www.terraform.io/docs/cli/commands/providers/schema.html#providers-schema-representation
@JsonClass(generateAdapter = true)
data class ProviderSchemaWrapper(
    @Json(name = "format_version")
    val formatVersion: String,
    @Json(name = "provider_schemas")
    val providerSchemas: Map<String, ProviderSchema>
)

data class ProviderSchema(
    val provider: SchemaRepresentation,
    @Json(name = "resource_schemas")
    val resourceSchemas: Map<String, SchemaRepresentation>,
    @Json(name = "data_source_schemas")
    val dataSourceSchemas: Map<String, SchemaRepresentation>
)

data class SchemaRepresentation(
    val version: Int,
    val block: BlockRepresentation,
)

data class BlockRepresentation(
    val attributes: Map<String, BlockAttributes>?,
    @Json(name = "block_types")
    val blockTypes: Map<String, BlockType>?,
    @Json(name = "description_kind")
    val descriptionKind: String?
)

data class BlockAttributes(
//    val type: String, // TODO oh boy dis gonna suck
    val description: String?,
    @Json(name = "description_kind")
    val descriptionKind: String?,
    val required: Boolean?,
    val optional: Boolean?,
    val computed: Boolean?,
    val sensitive: Boolean?
)

enum class NestingMode {
    single,
    list,
    set,
    map
}

data class BlockType(
    @Json(name = "nesting_mode")
    val nestingMode: NestingMode?,
    val block: BlockRepresentation,
    // todo only allowed on the list and set modes... how to represent programmatically?
    @Json(name = "min_items")
    val minItems: Int?,
    @Json(name = "max_items")
    val maxItems: Int?
)