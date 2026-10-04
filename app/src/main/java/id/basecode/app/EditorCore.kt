package id.basecode.app

import android.content.Context
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Inisialisasi dan konfigurasi inti editor sora-editor + TextMate.
 * Grammar yang dipakai sama dengan grammar VS Code (.tmLanguage.json).
 */
object EditorCore {

    private var initialized = false

    /** Peta ekstensi file -> scope TextMate (grammar VS Code). */
    private val extensionScopes = mapOf(
        "java" to "source.java",
        "kt" to "source.kotlin",
        "kts" to "source.kotlin",
        "py" to "source.python",
        "xml" to "text.xml",
        "html" to "text.html.basic",
        "htm" to "text.html.basic",
        "js" to "source.js",
        "jsx" to "source.js",
        "mjs" to "source.js",
        "md" to "text.html.markdown",
        "markdown" to "text.html.markdown",
        "css" to "source.css",
        "php" to "text.html.php",
        "json" to "source.js" // sementara: highlighting mirip JS untuk JSON
    )

    @Synchronized
    fun setup(context: Context) {
        if (initialized) return
        // Daftarkan resolver file dari assets
        FileProviderRegistry.getInstance()
            .addFileProvider(AssetsFileResolver(context.applicationContext.assets))

        // Muat tema gelap ala VS Code (darcula)
        val themeRegistry = ThemeRegistry.getInstance()
        val path = "textmate/darcula.json"
        themeRegistry.loadTheme(
            ThemeModel(
                IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(path),
                    path,
                    null
                ),
                "darcula"
            ).apply { isDark = true }
        )
        themeRegistry.setTheme("darcula")

        // Muat semua grammar dari languages.json
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")

        initialized = true
    }

    /** Buat instance CodeEditor siap pakai dengan tema dan pengaturan dasar. */
    fun createEditor(context: Context): CodeEditor {
        setup(context)
        val editor = CodeEditor(context)
        editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        editor.setWordwrap(false)
        editor.setTextSize(14f)
        editor.setLineSpacing(2f, 1.2f)
        editor.isLineNumberEnabled = true
        editor.isBlockLineEnabled = true
        return editor
    }

    /** Pilih bahasa editor berdasarkan nama file. */
    fun applyLanguage(editor: CodeEditor, fileName: String?) {
        val ext = fileName?.substringAfterLast('.', "")?.lowercase()
        val scope = ext?.let { extensionScopes[it] }
        if (scope != null) {
            editor.setEditorLanguage(TextMateLanguage.create(scope, true))
        } else {
            editor.setEditorLanguage(EmptyLanguage())
        }
    }
}

/** Operasi tambahan untuk toolbar quick-key ala Acode. */
object EditorActions {

    /** Pindahkan baris saat ini ke atas, menukar posisi dengan baris sebelumnya. */
    fun moveLineUp(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        val cursor = editor.cursor
        if (cursor.isSelected) return
        val line = cursor.leftLine
        if (line <= 0) return
        val content = editor.text
        val current = content.getLineString(line)
        val previous = content.getLineString(line - 1)
        val column = cursor.leftColumn
        content.replace(line - 1, 0, line, current.length, current + "\n" + previous)
        editor.setSelection(line - 1, column)
    }

    /** Pindahkan baris saat ini ke bawah, menukar posisi dengan baris berikutnya. */
    fun moveLineDown(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        val cursor = editor.cursor
        if (cursor.isSelected) return
        val line = cursor.leftLine
        val content = editor.text
        if (line >= content.lineCount - 1) return
        val current = content.getLineString(line)
        val next = content.getLineString(line + 1)
        val column = cursor.leftColumn
        content.replace(line, 0, line + 1, next.length, next + "\n" + current)
        editor.setSelection(line + 1, column)
    }

    /** Sisipkan tab/indentasi pada posisi kursor. */
    fun insertTab(editor: io.github.rosemoe.sora.widget.CodeEditor) {
        editor.insertText("\t", 0)
    }
}
