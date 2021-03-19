package org.leafygreens.torterra

import com.squareup.moshi.*

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
  val description: String?,
  @Json(name = "description_kind")
  val descriptionKind: String?,
  val deprecated: Boolean?
)

@JsonClass(generateAdapter = false)
data class BlockAttributes(
  val type: BlockAttributeType,
  val description: String?,
  @Json(name = "description_kind")
  val descriptionKind: String?,
  val required: Boolean?,
  val optional: Boolean?,
  val computed: Boolean?,
  val sensitive: Boolean?,
  val deprecated: Boolean?
)

enum class BlockAttributeCollection(val value: String) {
  Set("set"),
  List("list"),
  Map("map"),
  Obj("object");

  companion object {
    fun fromValue(str: String): BlockAttributeCollection = when (str) {
      "set" -> Set
      "list" -> List
      "map" -> Map
      "object" -> Obj
      else -> error("Unexpected BlockAttributeCollection: $str")
    }
  }
}

sealed class BlockAttributeType() {
  class SimpleBlockAttributeType(val type: String) : BlockAttributeType()
  class NestedBlockAttributeType(val collection: BlockAttributeCollection, val nested: BlockAttributeType) : BlockAttributeType()
  class ObjectBlockAttributeType(val types: Map<String, BlockAttributeType>) : BlockAttributeType()
}

class BlockAttributeTypeJsonAdapter : JsonAdapter<BlockAttributeType>() {
  @FromJson
  override fun fromJson(reader: JsonReader): BlockAttributeType? {
    return when(reader.peek()) {
      JsonReader.Token.BEGIN_ARRAY -> readTuple(reader)
      JsonReader.Token.STRING -> BlockAttributeType.SimpleBlockAttributeType(reader.nextString())
      else -> error("Unexpected peek token: ${reader.peek()}")
    }
  }

  private fun readTuple(reader: JsonReader): BlockAttributeType {
    reader.beginArray()
    val first = BlockAttributeCollection.fromValue(reader.nextString())
    val second = when(reader.peek()) {
      JsonReader.Token.STRING -> BlockAttributeType.SimpleBlockAttributeType(reader.nextString())
      JsonReader.Token.BEGIN_ARRAY -> readTuple(reader)
      JsonReader.Token.BEGIN_OBJECT -> readObject(reader)
      else -> error("Unexpected nested peek token: ${reader.peek()}")
    }
    reader.endArray()
    return BlockAttributeType.NestedBlockAttributeType(first, second)
  }

  private fun readObject(reader: JsonReader): BlockAttributeType {
    val map = mutableMapOf<String, BlockAttributeType>()
    reader.beginObject()
    while (reader.peek() == JsonReader.Token.NAME) {
      val key = reader.nextName()
      val value = when(reader.peek()) {
        JsonReader.Token.BEGIN_ARRAY -> readTuple(reader)
        JsonReader.Token.STRING -> BlockAttributeType.SimpleBlockAttributeType(reader.nextString())
        else -> error("Unexpected peek token: ${reader.peek()}")
      }
      map[key] = value
    }
    reader.endObject()
    return BlockAttributeType.ObjectBlockAttributeType(map)
  }

  @ToJson
  override fun toJson(writer: JsonWriter, value: BlockAttributeType?) {
    when(value) {
      is BlockAttributeType.SimpleBlockAttributeType -> writer.value(value.type)
      is BlockAttributeType.NestedBlockAttributeType -> writer.writeNested(value)
      is BlockAttributeType.ObjectBlockAttributeType -> writer.writeObject(value)
      null -> TODO()
    }
  }

  private fun JsonWriter.writeNested(value: BlockAttributeType.NestedBlockAttributeType) {
    beginArray()
    value(value.collection.value)
    when(value.nested) {
      is BlockAttributeType.SimpleBlockAttributeType -> value(value.nested.type)
      is BlockAttributeType.NestedBlockAttributeType -> writeNested(value.nested)
      is BlockAttributeType.ObjectBlockAttributeType -> writeObject(value.nested)
    }
    endArray()
  }

  private fun JsonWriter.writeObject(value: BlockAttributeType.ObjectBlockAttributeType) {
    beginObject()
    value.types.forEach { (k, v) ->
      name(k)
      when(v) {
        is BlockAttributeType.SimpleBlockAttributeType -> value(v.type)
        is BlockAttributeType.NestedBlockAttributeType -> writeNested(v)
        is BlockAttributeType.ObjectBlockAttributeType -> writeObject(v)
      }
    }
    endObject()
  }

}

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
