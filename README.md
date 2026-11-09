# Item Lock 1.1.1 (Fabric, Minecraft 26.3, client-only)

Kunci slot hotbar supaya item di dalamnya tidak sengaja terbuang.
Mod ini **hanya dipasang di komputermu sendiri**. Server tidak perlu memasangnya, jadi bisa dipakai di server mana pun (oneblock, SMP, dll).

## Cara pakai

1. Pilih slot hotbar yang berisi item yang mau diamankan (angka 1-9 atau scroll).
2. Tekan **K** (bisa diganti di Options > Controls > Key Binds > Item Lock).
3. Muncul gembok emas kecil di slot itu. Tekan **K** lagi di slot yang sama untuk membuka kunci.

Slot terkunci tidak bisa membuang item lewat:

- **Q** dan **Ctrl+Q** saat slot itu sedang dipilih
- **Q** / **Ctrl+Q** saat kursor di atas slot itu di layar inventory atau chest
- klik atau shift-click pada slot itu di layar inventory (item tidak bisa diangkat, jadi tidak bisa dijatuhkan lewat klik di luar jendela)
- tombol angka 1-9 di layar inventory yang akan menukar item dari slot terkunci
- klik dua kali untuk mengumpulkan item (dimatikan selama ada slot terkunci)

Daftar slot terkunci disimpan di `config/itemlock.txt`, jadi tetap ada setelah game ditutup.

## Perbedaan penting dari versi 1.0.0

- Yang dikunci adalah **slot hotbar**, bukan itemnya. Kalau item di slot terkunci habis terpakai (misalnya blok yang kamu pasang), item baru yang masuk ke slot itu juga ikut terkunci. Buka kuncinya dengan **K** kalau tidak mau.
- Hanya slot hotbar (1-9) yang bisa dikunci. Slot inventory biasa tidak.

## Yang belum dicakup

- Item masih jatuh kalau kamu mati.
- Item terkunci masih bisa dipindahkan oleh server atau plugin (misalnya GUI shop).
- Layar inventory Creative belum diblokir sepenuhnya.
- Gembok digambar dengan posisi hotbar standar. Mod HUD lain yang mengubah posisi hotbar bisa membuat gembok bergeser (fungsi kuncinya tetap jalan).

## Kebutuhan

- Minecraft Java Edition **26.3**
- **Java 25** (sudah dibawa oleh Minecraft Launcher)
- Fabric Loader **0.20.1** atau lebih baru
- Fabric API **0.165.2+26.3** atau lebih baru

Hapus `itemlock-1.0.0.jar` dan `itemlock-1.1.0.jar` (kalau ada) dari folder `mods` sebelum memasang versi ini.

Catatan 1.1.1: memperbaiki crash saat membuka Minecraft 26.3 ("Invalid descriptor ... slotClicked"). Di 26.3 method `slotClicked` punya dua versi (int dan MouseButtonEvent), sekarang keduanya ditangani terpisah.

## Cara membuat file .jar

Lewat GitHub Actions: upload isi folder ini, tunggu workflow "build" selesai, lalu unduh **Artifacts** dan ambil `itemlock-1.1.1.jar` (bukan yang `-sources`).

Di komputer sendiri: install JDK 25 lalu jalankan `./gradlew build` (Windows: `gradlew.bat build`). Hasilnya ada di `build/libs/itemlock-1.1.1.jar`.

## Struktur kode

| File | Fungsi |
|---|---|
| `client/ItemLockClient.java` | Tombol K, memblokir tombol drop, pesan di layar, dan gambar gembok di hotbar |
| `client/LockState.java` | Menyimpan slot terkunci dan menentukan klik inventory mana yang diblokir |
| `client/mixin/AbstractContainerScreenMixin.java` | Membatalkan klik di layar inventory sebelum dikirim ke server |
