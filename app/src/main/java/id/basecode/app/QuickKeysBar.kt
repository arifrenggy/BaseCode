package id.basecode.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SelectionMovement

/**
 * Toolbar quick-key dua baris ala Acode, ditampilkan tepat di atas keyboard.
 * Baris 1: Shift, Tab, Cari, Undo, Redo, pindah baris ke atas, Simpan, Esc
 * Baris 2: Ctrl, Alt, pindah baris ke bawah, kiri, atas, bawah, kanan
 */
@Composable
fun QuickKeysBar(
    editor: CodeEditor,
    shiftActive: Boolean,
    onShiftToggle: () -> Unit,
    onFind: () -> Unit,
    onSave: () -> Unit,
    onEsc: () -> Unit
) {
    val bg = Color(0xFF1B1D23)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(vertical = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            QuickKey("SHFT", highlighted = shiftActive, onClick = onShiftToggle)
            QuickKey("\u2192|", onClick = { EditorActions.insertTab(editor) })
            QuickKey("\uD83D\uDD0D", onClick = onFind)
            QuickKey("\u21B6", enabled = editor.canUndo(), onClick = { editor.undo() })
            QuickKey("\u21B7", enabled = editor.canRedo(), onClick = { editor.redo() })
            QuickKey("\u2191\u2098", onClick = { EditorActions.moveLineUp(editor) })
            QuickKey("\uD83D\uDCBE", onClick = onSave)
            QuickKey("ESC", onClick = onEsc)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            QuickKey("CTRL", onClick = {})
            QuickKey("ALT", onClick = {})
            QuickKey("\u2193\u2098", onClick = { EditorActions.moveLineDown(editor) })
            QuickKey("\u2039", onClick = { editor.moveOrExtendSelection(SelectionMovement.LEFT, shiftActive) })
            QuickKey("\u2191", onClick = { editor.moveOrExtendSelection(SelectionMovement.UP, shiftActive) })
            QuickKey("\u2193", onClick = { editor.moveOrExtendSelection(SelectionMovement.DOWN, shiftActive) })
            QuickKey("\u203A", onClick = { editor.moveOrExtendSelection(SelectionMovement.RIGHT, shiftActive) })
        }
    }
}

@Composable
private fun QuickKey(
    label: String,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = when {
                !enabled -> Color(0xFF55585F)
                highlighted -> MaterialTheme.colorScheme.primary
                else -> Color(0xFFD4D4D4)
            }
        )
    }
}
