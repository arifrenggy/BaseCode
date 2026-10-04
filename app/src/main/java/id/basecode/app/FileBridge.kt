package id.basecode.app

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Jembatan ke file lewat Storage Access Framework (SAF):
 * baca file, simpan file, dan kelola daftar file terakhir dibuka.
 */
object FileBridge {

    private const val PREFS = "basecode_prefs"
    private const val KEY_RECENTS = "recents"

    /** Buka file SAF dan ambil isi teksnya. Uri harus punya izin persistable. */
    fun readText(context: Context, uri: Uri): String? = try {
        // Simpan izin akses untuk dibuka lagi nanti
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Beberapa penyedia file tidak mendukung izin persistable; abaikan.
        }
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    } catch (_: IOException) {
        null
    }

    /** Tulis teks ke file SAF (menimpa isi). */
    fun writeText(context: Context, uri: Uri, text: String): Boolean = try {
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
        } != null
    } catch (_: IOException) {
        false
    }

    /** Nama tampilan dari Uri SAF. */
    fun displayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                cursor.getString(idx)?.let { return it }
            }
        }
        return uri.lastPathSegment ?: "file"
    }

    // ---------- Riwayat file (recents) ----------

    data class RecentFile(val uri: String, val name: String)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun addRecent(context: Context, uri: Uri, name: String) {
        val list = getRecents(context).toMutableList()
        list.removeAll { it.uri == uri.toString() }
        list.add(0, RecentFile(uri.toString(), name))
        // Simpan maksimal 10 file terakhir
        while (list.size > 10) list.removeAt(list.size - 1)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("uri", it.uri).put("name", it.name))
        }
        prefs(context).edit().putString(KEY_RECENTS, arr.toString()).apply()
    }

    fun getRecents(context: Context): List<RecentFile> {
        val raw = prefs(context).getString(KEY_RECENTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                RecentFile(o.getString("uri"), o.optString("name", "file"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
