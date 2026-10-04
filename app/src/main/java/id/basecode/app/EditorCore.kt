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
        editor.setWordWrap(false)
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
