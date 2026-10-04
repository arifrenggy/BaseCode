package id.basecode.app

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Mesin plugin BaseCode (v0.1: deteksi & daftar).
 *
 * Struktur plugin (folder di dalam filesDir/plugins/):
 *   <plugin-id>/
 *     manifest.json   -> identitas + izin
 *     main.js         -> kode plugin (JavaScript, dieksekusi fase berikutnya)
 *     ...             -> aset tambahan
 *
 * Eksekusi main.js (QuickJS) + API plugin lengkap dijadwalkan di fase 1.
 */
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val permissions: List<String>
)

object PluginEngine {

    fun pluginsDir(context: Context): File = File(context.filesDir, "plugins")

    /** Daftar semua plugin terpasang (berdasarkan manifest.json yang valid). */
    fun listInstalled(context: Context): List<PluginManifest> {
        val dir = pluginsDir(context)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()?.mapNotNull { pluginDir ->
            val manifestFile = File(pluginDir, "manifest.json")
            if (!manifestFile.isFile) return@mapNotNull null
            runCatching {
                val json = JSONObject(manifestFile.readText())
                PluginManifest(
                    id = json.optString("id", pluginDir.name),
                    name = json.optString("name", pluginDir.name),
                    version = json.optString("version", "0.0.0"),
                    description = json.optString("description", ""),
                    permissions = json.optJSONArray("permissions")?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }
                    } ?: emptyList()
                )
            }.getOrNull()
        } ?: emptyList()
    }
}
