# Template Plugin BaseCode

Template untuk menulis plugin BaseCode (editor kode untuk Android).

## Struktur

    my-plugin/
      manifest.json   -> identitas plugin + izin
      main.js         -> kode plugin (JavaScript)

## manifest.json

| Bidang | Wajib | Keterangan |
|---|---|---|
| id | Ya | ID unik, format seperti `com.nama.plugin` |
| name | Ya | Nama tampilan |
| version | Ya | SemVer, mis. `0.1.0` |
| description | Tidak | Penjelasan singkat |
| permissions | Tidak | `ui`, `files`, `network` |
| entry | Tidak | File entry, default `main.js` |
| engines.basecode | Ya | Versi minimum BaseCode |

## Status

v0.1: deteksi plugin (manifest dikenali aplikasi, muncul di beranda).
Eksekusi JavaScript via QuickJS dijadwalkan di rilis berikutnya.
Lihat `docs/PLUGIN-API.md` untuk API yang direncanakan.
