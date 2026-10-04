# Plugin API BaseCode (Rencana / Draf)

## Status

| Fase | Kemampuan | Rilis target |
|---|---|---|
| v0.1 | Deteksi manifest + daftar plugin | sekarang |
| v0.2 | Eksekusi main.js via QuickJS | berikutnya |
| v0.3 | API perintah, menu, snippet | fase 1 |
| v0.4 | Kontribusi bahasa (grammar TextMate) | fase 1 |

## Arsitektur

Plugin ditulis JavaScript, dieksekusi dengan QuickJS ter-embed
(di dalam aplikasi native, bukan WebView). Setiap kemampuan diizinkan
per-plugin lewat `manifest.json`:

    "permissions": ["ui", "files", "network"]

## API yang direncanakan (v0.2-v0.3)

    basecode.commands.register(id, handler)   // perintah + command palette
    basecode.ui.toast(message)               // notifikasi singkat
    basecode.ui.addMenuItem(label, commandId)
    basecode.snippets.register(snippet)      // format .json VS Code
    basecode.themes.register(theme)          // tema TextMate .json
    basecode.workspace.openFile(path)
    basecode.editor.insertText(text)

## Snippet: kompatibel VS Code

BaseCode menerima format snippet .json yang sama dengan VS Code,
jadi snippet apa pun yang beredar di GitHub bisa dipakai langsung.
