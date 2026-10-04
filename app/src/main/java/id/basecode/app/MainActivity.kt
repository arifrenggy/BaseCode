package id.basecode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

// Palet gelap ala VS Code
val BaseCodeColors = darkColorScheme(
    primary = Color(0xFF4FC1FF),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF252526),
    surfaceVariant = Color(0xFF2D2D30),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

// Contoh kode untuk mencoba editor (preview fitur)
private const val SAMPLE_CODE = """# BaseCode preview - Python
def sapa(nama: str) -> str:
    return f"Halo, {nama}!"

for i in range(3):
    print(sapa(f"dunia {i}"))
"""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = BaseCodeColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BaseCodeApp()
                }
            }
        }
    }
}

/** Navigasi sederhana: halaman utama <-> editor. */
sealed class Screen {
    object Home : Screen()
    data class Editor(val fileName: String, val content: String) : Screen()
}

@Composable
fun BaseCodeApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    when (val s = screen) {
        is Screen.Home -> BaseCodeHome(
            onTryEditor = { screen = Screen.Editor("sample.py", SAMPLE_CODE) }
        )
        is Screen.Editor -> EditorScreen(
            fileName = s.fileName,
            initialContent = s.content,
            onBack = { screen = Screen.Home }
        )
    }
}

@Composable
fun BaseCodeHome(onTryEditor: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BaseCode",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onTryEditor) {
            Text(stringResource(R.string.btn_try_editor))
        }
    }
}

@Composable
fun EditorScreen(
    fileName: String,
    initialContent: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val editor = remember { EditorCore.createEditor(context).apply { setText(initialContent) } }

    // Terapkan bahasa sesuai ekstensi file
    androidx.compose.runtime.LaunchedEffect(fileName) {
        EditorCore.applyLanguage(editor, fileName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Bar atas: kembali + nama file
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.btn_back))
            }
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Editor utama
        AndroidView(
            factory = { editor },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Bar bawah: undo/redo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { editor.undo() }) {
                Text(stringResource(R.string.btn_undo))
            }
            OutlinedButton(onClick = { editor.redo() }) {
                Text(stringResource(R.string.btn_redo))
            }
        }
    }
}
