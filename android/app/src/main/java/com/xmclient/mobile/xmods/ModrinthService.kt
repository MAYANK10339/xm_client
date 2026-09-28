package com.xmclient.mobile.xmods

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.xmclient.mobile.core.MinecraftVersion
import com.xmclient.mobile.core.ModLoader
import com.xmclient.mobile.xmods.model.ModDependency
import com.xmclient.mobile.xmods.model.ModFile
import com.xmclient.mobile.xmods.model.UnifiedMod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ModrinthService(private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()
) {
    private val gson = Gson()

    suspend fun searchMods(
        query: String,
        version: MinecraftVersion,
        loader: ModLoader,
        limit: Int = 20,
        offset: Int = 0
    ): List<UnifiedMod> = withContext(Dispatchers.IO) {
        val facets = JsonArray().apply {
            add(JsonArray().apply { add("project_type:mod") })
            add(JsonArray().apply { add("categories:${loader.id.lowercase()}") })
            add(JsonArray().apply { add("versions:${version.versionString}") })
        }

        val urlBuilder = "https://api.modrinth.com/v2/search".toHttpUrlOrNull()?.newBuilder()
            ?: return@withContext emptyList()

        urlBuilder.addQueryParameter("limit", limit.toString())
        urlBuilder.addQueryParameter("offset", offset.toString())
        urlBuilder.addQueryParameter("index", if (query.isEmpty()) "follows" else "relevance")
        if (query.isNotEmpty()) {
            urlBuilder.addQueryParameter("query", query)
        }
        urlBuilder.addQueryParameter("facets", facets.toString())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "XMClient-Mobile/1.0.0 (Android; by Mayank Mandrai)")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = response.body?.string() ?: return@withContext emptyList()
                val root = gson.fromJson(json, JsonObject::class.java)
                val hits = root.getAsJsonArray("hits") ?: return@withContext emptyList()

                val results = mutableListOf<UnifiedMod>()
                for (hit in hits) {
                    val obj = hit.asJsonObject
                    results.add(
                        UnifiedMod(
                            id = obj.get("project_id").asString,
                            slug = obj.get("slug").asString,
                            title = obj.get("title")?.asString ?: "",
                            description = obj.get("description")?.asString ?: "",
                            author = obj.get("author")?.asString ?: "Unknown",
                            iconUrl = obj.get("icon_url")?.takeIf { !it.isJsonNull }?.asString,
                            downloads = obj.get("downloads")?.asLong ?: 0L,
                            source = "Modrinth",
                            loaders = listOf(loader.id),
                            gameVersions = listOf(version.versionString)
                        )
                    )
                }
                results
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getModFiles(
        projectIdOrSlug: String,
        version: MinecraftVersion,
        loader: ModLoader
    ): List<ModFile> = withContext(Dispatchers.IO) {
        val urlBuilder = "https://api.modrinth.com/v2/project/$projectIdOrSlug/version".toHttpUrlOrNull()?.newBuilder()
            ?: return@withContext emptyList()

        urlBuilder.addQueryParameter("loaders", "[\"${loader.id.lowercase()}\"]")
        urlBuilder.addQueryParameter("game_versions", "[\"${version.versionString}\"]")

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "XMClient-Mobile/1.0.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = response.body?.string() ?: return@withContext emptyList()
                val array = gson.fromJson(json, JsonArray::class.java) ?: return@withContext emptyList()

                val filesList = mutableListOf<ModFile>()
                for (verElem in array) {
                    val verObj = verElem.asJsonObject
                    val files = verObj.getAsJsonArray("files") ?: continue
                    val primaryFile = files.firstOrNull { it.asJsonObject.get("primary")?.asBoolean == true }
                        ?: files.firstOrNull() ?: continue

                    val fileObj = primaryFile.asJsonObject
                    val fileName = fileObj.get("filename").asString
                    val fileUrl = fileObj.get("url").asString

                    // Parse dependencies
                    val depsList = mutableListOf<ModDependency>()
                    val depArray = verObj.getAsJsonArray("dependencies")
                    if (depArray != null) {
                        for (d in depArray) {
                            val dObj = d.asJsonObject
                            val depType = dObj.get("dependency_type")?.asString ?: "optional"
                            val depProjId = dObj.get("project_id")?.takeIf { !it.isJsonNull }?.asString
                            val depVerId = dObj.get("version_id")?.takeIf { !it.isJsonNull }?.asString

                            if (depProjId != null || depVerId != null) {
                                depsList.add(
                                    ModDependency(
                                        id = depProjId ?: depVerId ?: "",
                                        name = depProjId ?: "Required Dependency",
                                        source = "Modrinth",
                                        isRequired = (depType == "required")
                                    )
                                )
                            }
                        }
                    }

                    filesList.add(
                        ModFile(
                            fileId = verObj.get("id").asString,
                            fileName = fileName,
                            downloadUrl = fileUrl,
                            gameVersion = version.versionString,
                            loader = loader.id,
                            dependencies = depsList
                        )
                    )
                }
                filesList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
