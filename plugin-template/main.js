/**
 * BaseCode plugin - contoh minimal.
 * API plugin v0.1 (draf; eksekusi JavaScript hadir di rilis berikutnya):
 *
 *   basecode.commands.register("hello", () => {
 *     basecode.ui.toast("Halo dari plugin!")
 *   })
 *
 * Untuk menguji deteksi plugin di v0.1:
 * 1. Buat folder <plugin-id>/ di dalam plugins BaseCode
 * 2. Taruh manifest.json + main.js ke dalamnya
 * 3. Plugin akan muncul di daftar "Plugin" di beranda
 */

console.log("plugin loaded")
