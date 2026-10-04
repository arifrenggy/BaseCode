package id.basecode.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

/**
 * Jembatan ke Termux lewat intent RUN_COMMAND.
 * Catatan: pengguna harus mengaktifkan "Allow external apps" di Termux
 * (file ~/.termux/termux.properties: allow-external-apps=true).
 */
object TermuxBridge {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val EXTRA_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val TERMUX_SHELL = "/data/data/com.termux/files/usr/bin/bash"
    private const val TERMUX_HOME = "/data/data/com.termux/files/home"

    sealed class SendResult {
        object Sent : SendResult()
        object NotInstalled : SendResult()
        object PermissionDenied : SendResult()
    }

    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
        true
    } catch (_: Exception) {
        false
    }

    /** Perintah default untuk menjalankan sebuah file, berdasarkan ekstensinya. */
    fun defaultCommand(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "py" -> "python -u \"$fileName\""
            "js", "mjs" -> "node \"$fileName\""
            "php" -> "php \"$fileName\""
            "sh" -> "bash \"$fileName\""
            "html", "htm" -> "echo \"HTML file: open in browser\""
            else -> "echo \"BaseCode: no default runner for .$ext\""
        }
    }

    /** Kirim perintah ke Termux via broadcast RUN_COMMAND. */
    fun sendCommand(context: Context, command: String): SendResult {
        if (!isInstalled(context)) return SendResult.NotInstalled
        return try {
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                setPackage(TERMUX_PACKAGE)
                putExtra(EXTRA_PATH, TERMUX_SHELL)
                putExtra(EXTRA_ARGUMENTS, arrayOf("-c", command))
                putExtra(EXTRA_WORKDIR, TERMUX_HOME)
            }
            context.sendBroadcast(intent)
            SendResult.Sent
        } catch (_: Exception) {
            // Umumnya SecurityException: izin external apps belum aktif
            SendResult.PermissionDenied
        }
    }

    /** Fallback: salin perintah ke clipboard agar ditempel manual di Termux. */
    fun copyCommand(context: Context, command: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("BaseCode", command))
    }
}
