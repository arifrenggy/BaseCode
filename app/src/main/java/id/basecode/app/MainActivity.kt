package id.basecode.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

// Palet gelap ala VS Code
val BaseCodeColors = darkColorScheme(
    primary = Color(0xFF4FC1FF),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF252526),
    surfaceVariant = Color(0xFF2D2D30),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

// Contoh kode untuk mencoba editor
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

sealed class Screen {
    object Home : Screen()
    data class Editor(
        val fileName: String,
        val content: String,
        val uri: String? = null
    ) : Screen()
}

@Composable
fun BaseCodeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var recents by remember { mutableStateOf(FileBridge.getRecents(context)) }

    fun openUri(uri: Uri) {
        val content = FileBridge.readText(context, uri)
        if (content == null) {
            scope.launch {
                snackbar.showSnackbar(context.getString(R.string.cannot_open_file))
            }
            return
        }
        val name = FileBridge.displayName(context, uri)
        FileBridge.addRecent(context, uri, name)
        recents = FileBridge.getRecents(context)
        screen = Screen.Editor(name, content, uri.toString())
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { openUri(it) } }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val s = screen) {
            is Screen.Home -> BaseCodeHome(
                onTryEditor = { screen = Screen.Editor("sample.py", SAMPLE_CODE, null) },
                onOpenFile = { openLauncher.launch(arrayOf("*/*")) },
                recents = recents,
                onOpenRecent = { openUri(Uri.parse(it)) }
            )
            is Screen.Editor -> key(s.uri ?: s.fileName) {
                EditorScreen(
                    fileName = s.fileName,
                    initialContent = s.content,
                    uri = s.uri,
                    onBack = { screen = Screen.Home }
                )
            }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BaseCodeHome(
    onTryEditor: () -> Unit,
    onOpenFile: () -> Unit,
    recents: List<FileBridge.RecentFile>,
    onOpenRecent: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onOpenFile) {
                Text(stringResource(R.string.btn_open_file))
            }
            OutlinedButton(onClick = onTryEditor) {
                Text(stringResource(R.string.btn_try_editor))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.recents_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (recents.isEmpty()) {
            Text(
                text = stringResource(R.string.no_recents),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(recents) { recent ->
                    Text(
                        text = recent.name,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenRecent(recent.uri) }
                            .padding(vertical = 12.dp)
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EditorScreen(
    fileName: String,
    initialContent: String,
    uri: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val editor = remember {
        EditorCore.createEditor(context).apply { setText(initialContent) }
    }

    LaunchedEffect(fileName) {
        EditorCore.applyLanguage(editor, fileName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Bar atas
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                Button(
                    onClick = {
                        val ok = uri != null && FileBridge.writeText(
                            context, Uri.parse(uri), editor.text.toString()
                        )
                        scope.launch {
                            snackbar.showSnackbar(
                                context.getString(
                                    if (ok) R.string.saved_toast
                                    else R.string.cannot_save_file
                                )
                            )
                        }
                    },
                    enabled = uri != null
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            }

            // Editor
            AndroidView(
                factory = { editor },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Bar bawah
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
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
