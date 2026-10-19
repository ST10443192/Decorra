package com.example.decorra.data

import org.json.JSONArray
import org.json.JSONObject

internal fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }

internal fun JSONObject.stringOrNull(key: String): String? =
    if (isNull(key)) null else getString(key)

internal fun <T> List<T>.toJsonArray(transform: (T) -> Any): JSONArray =
    JSONArray().also { array -> forEach { array.put(transform(it)) } }

// Themes
internal fun DecorTheme.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("description", description)
    .put("tags", tags.toJsonArray { it })
    .put("imageUri", imageUri)

internal fun JSONObject.toTheme() = DecorTheme(
    id = getString("id"),
    name = getString("name"),
    description = getString("description"),
    tags = getJSONArray("tags").let { a -> List(a.length()) { a.getString(it) } },
    imageUri = stringOrNull("imageUri")
)

// Items
internal fun DecorItem.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("priceCents", priceCents)
    .put("themeId", themeId)
    .put("imageUri", imageUri)
    .put("rentable", rentable)
    .put("stock", stock)
    .put("minStock", minStock)
    .put("unitCostCents", unitCostCents)

internal fun JSONObject.toItem() = DecorItem(
    id = getString("id"),
    name = getString("name"),
    priceCents = getLong("priceCents"),
    themeId = stringOrNull("themeId"),
    imageUri = stringOrNull("imageUri"),
    rentable = optBoolean("rentable", false),
    stock = if (isNull("stock")) null else getInt("stock"),
    minStock = optInt("minStock", 0),
    unitCostCents = optLong("unitCostCents", 0)
)

// Quotes
internal fun Quote.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("themeId", themeId)
    .put("themeName", themeName)
    .put("eventEpochDay", eventEpochDay)
    .put("guests", guests)
    .put("status", status.name)
    .put("clientName", clientName)
    .put("clientEmail", clientEmail)
    .put("clientPhone", clientPhone)
    .put("note", note)
    .put("eventType", eventType)
    .put("venue", venue)
    .put("sentToClient", sentToClient)
    .put("lines", lines.toJsonArray { line ->
        JSONObject()
            .put("itemId", line.itemId)
            .put("name", line.name)
            .put("unitPriceCents", line.unitPriceCents)
            .put("quantity", line.quantity)
    })
    .put("tasks", tasks.toJsonArray { task ->
        JSONObject()
            .put("id", task.id)
            .put("title", task.title)
            .put("done", task.done)
    })

internal fun JSONObject.toQuote() = Quote(
    id = getString("id"),
    themeId = getString("themeId"),
    themeName = getString("themeName"),
    eventEpochDay = getLong("eventEpochDay"),
    guests = getInt("guests"),
    status = QuoteStatus.valueOf(getString("status")),
    clientName = optString("clientName"),
    clientEmail = optString("clientEmail"),
    clientPhone = optString("clientPhone"),
    note = optString("note"),
    eventType = optString("eventType"),
    venue = optString("venue"),
    sentToClient = optBoolean("sentToClient", false),
    lines = getJSONArray("lines").objects().map {
        QuoteLine(
            itemId = it.getString("itemId"),
            name = it.getString("name"),
            unitPriceCents = it.getLong("unitPriceCents"),
            quantity = it.getInt("quantity")
        )
    },
    tasks = getJSONArray("tasks").objects().map {
        QuoteTask(
            id = it.getString("id"),
            title = it.getString("title"),
            done = it.getBoolean("done")
        )
    }
)

// Messages and notifications
internal fun ChatMessage.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("text", text)
    .put("fromClient", fromClient)
    .put("timeMillis", timeMillis)
    .put("clientEmail", clientEmail)

internal fun JSONObject.toChatMessage() = ChatMessage(
    id = getString("id"),
    text = getString("text"),
    fromClient = getBoolean("fromClient"),
    timeMillis = getLong("timeMillis"),
    clientEmail = optString("clientEmail")
)

internal fun AppNotification.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("body", body)
    .put("timeMillis", timeMillis)
    .put("clientEmail", clientEmail)
    .put("type", type)
    .put("arg", arg)
    .put("forAdmin", forAdmin)
    .put("read", read)

internal fun JSONObject.toNotification() = AppNotification(
    id = getString("id"),
    title = optString("title"),
    body = optString("body"),
    timeMillis = getLong("timeMillis"),
    clientEmail = optString("clientEmail"),
    type = optString("type"),
    arg = optString("arg"),
    forAdmin = optBoolean("forAdmin", false),
    read = optBoolean("read", false)
)