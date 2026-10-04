# BaseCode

Editor kode *native* untuk Android, bergaya VS Code, dengan Terminal Termux
dan dukungan plugin. / A native code editor for Android with VS Code vibes,
Termux terminal, and plugin support.

## Fitur (v0.1.0-alpha)

* Editor native berbasis [sora-editor](https://github.com/Rosemoe/sora-editor) dengan grammar TextMate yang sama dengan VS Code
* Syntax highlighting: Java, Kotlin, Python, HTML, JavaScript, CSS, PHP, XML, Markdown
* Buka & simpan file (Storage Access Framework) + daftar file terakhir dibuka
* Cari dalam file (sebelumnya/berikutnya)
* Undo/redo, nomor baris, tema gelap ala VS Code
* Jalankan perintah di Termux (intent `RUN_COMMAND`, fallback salin ke clipboard)
* Deteksi plugin (`manifest.json`) + template plugin resmi: `plugin-template/`
* Dua bahasa: Indonesia & English (siap ditambah bahasa lain)

## Roadmap

* v0.2: eksekusi plugin JavaScript (QuickJS), impor snippet .json VS Code
* v0.3: git dasar via Termux, tema impor .json VS Code
* v1.0: rilis publik (Play Store / F-Droid), marketplace plugin

Lihat PRD: `docs/` (Product Requirements Document v1.1).

## Build

```bash
./gradlew assembleDebug
```

Hasil: `app/build/outputs/apk/debug/app-debug.apk`. Butuh JDK 17 dan
Android SDK 34. CI GitHub Actions bisa diaktifkan: salin `docs/ci-android.yml.txt` ke `.github/workflows/android.yml` (sekali, lewat web GitHub).

## Struktur

    app/                 -> aplikasi Android (Kotlin + Jetpack Compose)
      assets/textmate/   -> grammar & tema (format VS Code)
    plugin-template/     -> template plugin resmi
    docs/                -> PRD + rencana Plugin API

## Lisensi

MIT
