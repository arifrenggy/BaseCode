package id.basecode.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.widget.EditorSearcher
import kotlinx.coroutines.launch

// Palet gelap ala Acode/VS Code
val BaseCodeColors = darkColorScheme(
    primary = Color(0xFF4FC1FF),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF1B1D23),
    surfaceVariant = Color(0xFF2D2D30),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

private val BarBackground = Color(0xFF1B1D23)
private val SubtitleColor = Color(0xFF8A8F98)

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
                onOpenRecent = { openUri(Uri.parse(it)) },
                plugins = remember { PluginEngine.listInstalled(context) }
            )
            is Screen.Editor -> key(s.uri ?: s.fileName) {
                EditorScreen(
                    fileName = s.fileName,
                    initialContent = s.content,
                    uri = s.uri,
                    onBack = { screen = Screen.Home },
                    onOpenFile = { openLauncher.launch(arrayOf("*/*")) }
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
    onOpenRecent: (String) -> Unit,
    plugins: List<PluginManifest>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Bar atas ala Acode: menu + judul app
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BarBackground)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* file explorer: direncanakan */ }) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFFD4D4D4))
            }
            Text(
                text = "BaseCode",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRecent(recent.uri) }
                                .padding(vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = recent.name,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.plugins_title, plugins.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (plugins.isEmpty()) {
                Text(
                    text = stringResource(R.string.plugins_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                plugins.forEach { plugin ->
                    Text(
                        text = "${plugin.name} v${plugin.version}",
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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
    onBack: () -> Unit,
    onOpenFile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val editor = remember {
        EditorCore.createEditor(context).apply { setText(initialContent) }
    }
    var displayName by remember { mutableStateOf(fileName) }
    var searchVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var shiftActive by remember { mutableStateOf(false) }
    var termuxVisible by remember { mutableStateOf(false) }
    var renameVisible by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf(fileName) }
    var menuExpanded by remember { mutableStateOf(false) }
    var termuxCommand by remember { mutableStateOf(TermuxBridge.defaultCommand(fileName)) }

    fun doSearch() {
        if (query.isEmpty()) {
            editor.searcher.stopSearch()
        } else {
            editor.searcher.search(query, EditorSearcher.SearchOptions(false, false))
        }
    }

    fun saveFile() {
        val ok = uri != null && FileBridge.writeText(context, Uri.parse(uri), editor.text.toString())
        scope.launch {
            snackbar.showSnackbar(
                context.getString(if (ok) R.string.saved_toast else R.string.cannot_save_file)
            )
        }
    }

    LaunchedEffect(fileName) {
        EditorCore.applyLanguage(editor, fileName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Bar atas: menu, ikon file + nama + status, edit, overflow ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BarBackground)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFFD4D4D4))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(
                            if (uri == null) R.string.status_new_file else R.string.status_ready
                        ),
                        fontSize = 12.sp,
                        color = SubtitleColor
                    )
                }
                IconButton(onClick = {
                    renameValue = displayName
                    renameVisible = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD4D4D4))
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFFD4D4D4))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.btn_open_file)) },
                            onClick = { menuExpanded = false; onOpenFile() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.btn_save)) },
                            onClick = { menuExpanded = false; saveFile() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.btn_termux)) },
                            onClick = { menuExpanded = false; termuxVisible = true }
                        )
                    }
                }
            }

            // --- Tab file aktif, dengan garis biru di bawahnya ---
            Column {
                Row(
                    modifier = Modifier
                        .background(BarBackground)
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = displayName,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                        )
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = SubtitleColor,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onBack() }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // --- Editor ---
            AndroidView(
                factory = { editor },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // --- Bar cari (jika aktif) ---
            if (searchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BarBackground)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; doSearch() },
                        placeholder = { Text(stringResource(R.string.find_hint)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { editor.searcher.gotoPrevious() }) {
                        Text(stringResource(R.string.btn_find_prev))
                    }
                    TextButton(onClick = { editor.searcher.gotoNext() }) {
                        Text(stringResource(R.string.btn_find_next))
                    }
                    TextButton(onClick = {
                        editor.searcher.stopSearch()
                        query = ""
                        searchVisible = false
                    }) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            }

            // --- Toolbar quick-key ala Acode ---
            QuickKeysBar(
                editor = editor,
                shiftActive = shiftActive,
                onShiftToggle = { shiftActive = !shiftActive },
                onFind = { searchVisible = !searchVisible },
                onSave = { saveFile() },
                onEsc = {
                    when {
                        searchVisible -> {
                            editor.searcher.stopSearch()
                            query = ""
                            searchVisible = false
                        }
                        else -> editor.hideAutoCompleteWindow()
                    }
                }
            )
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (termuxVisible) {
        AlertDialog(
            onDismissRequest = { termuxVisible = false },
            title = { Text(stringResource(R.string.termux_dialog_title)) },
            text = {
                Column {
                    if (!TermuxBridge.isInstalled(context)) {
                        Text(
                            stringResource(R.string.termux_not_installed),
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(stringResource(R.string.termux_hint))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = termuxCommand,
                            onValueChange = { termuxCommand = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = TermuxBridge.isInstalled(context),
                    onClick = {
                        when (TermuxBridge.sendCommand(context, termuxCommand)) {
                            TermuxBridge.SendResult.Sent -> termuxVisible = false
                            TermuxBridge.SendResult.PermissionDenied -> {
                                TermuxBridge.copyCommand(context, termuxCommand)
                                termuxVisible = false
                            }
                            TermuxBridge.SendResult.NotInstalled -> termuxVisible = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_run))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    TermuxBridge.copyCommand(context, termuxCommand)
                    termuxVisible = false
                    scope.launch {
                        snackbar.showSnackbar(context.getString(R.string.command_copied))
                    }
                }) {
                    Text(stringResource(R.string.btn_copy))
                }
            }
        )
    }

    if (renameVisible) {
        AlertDialog(
            onDismissRequest = { renameVisible = false },
            title = { Text(stringResource(R.string.rename_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameValue.isNotBlank()) {
                        displayName = renameValue
                        EditorCore.applyLanguage(editor, renameValue)
                    }
                    renameVisible = false
                }) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameVisible = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }
}
