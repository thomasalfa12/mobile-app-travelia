# Aplikasi Driver - Agent Travel Unsri

Repositori ini berisi kode sumber untuk aplikasi Android native yang ditujukan bagi supir dalam ekosistem Agent Travel Unsri. Aplikasi ini berfungsi sebagai "dasbor kerja" utama bagi supir untuk mengelola status, menerima tawaran pesanan, dan menjalankan perjalanan terjadwal.

Aplikasi ini dibangun menggunakan 100% Kotlin dan Jetpack Compose, dengan arsitektur MVVM yang bersih dan modern.

## Filosofi Utama: Manajemen Travel, Bukan Gojek
Penting untuk dipahami bahwa aplikasi ini tidak dirancang untuk layanan ojek/taksi on-demand konvensional. Filosofi utamanya adalah sebagai alat manajemen travel berbasis jadwal dan ride-pooling.

- **Berbasis Jadwal:** Fokus utama adalah pada pesanan terjadwal (Pra-Pesan) dan mengisi kursi pada jadwal yang sudah ada (Waitlist Dinamis).
- **Gabung Penumpang (Ride-Pooling):** Aplikasi dirancang untuk mendukung supir menerima penumpang tambahan di tengah perjalanan jika rutenya searah.
- **Papan Pekerjaan:** Supir memiliki otonomi untuk melihat dan "mengklaim" jadwal perjalanan yang tersedia untuk keesokan harinya.

## ✨ Fitur Utama
Berikut adalah daftar fungsionalitas inti yang telah diimplementasikan:

- [x] **Otentikasi Aman:**
  - Alur login dua langkah menggunakan OTP (One-Time Password) yang dikirim via WhatsApp.
  - Manajemen sesi persisten menggunakan Token JWT yang disimpan secara aman di EncryptedSharedPreferences.

- [x] **Manajemen Status & Lokasi:**
  - Tombol toggle untuk mengubah status antara ONLINE dan OFFLINE.
  - Foreground Service yang aktif saat supir online untuk melacak dan mengirim pembaruan lokasi GPS secara periodik, bahkan saat aplikasi di latar belakang.

- [x] **Penerimaan Tawaran Real-Time:**
  - Integrasi penuh dengan Firebase Cloud Messaging (FCM) untuk menerima tawaran pesanan on-the-spot.
  - Layar tawaran (Offer Screen) full-screen dengan detail rute, tarif, dan timer hitung mundur.
  - Kemampuan untuk Menerima atau Menolak tawaran, yang terhubung langsung ke API backend.

- [x] **Manajemen Perjalanan Aktif:**
  - UI dinamis yang otomatis berubah menjadi mode "In-Trip" setelah menerima pesanan.
  - Tampilan Daftar Tugas (Task List) yang menampilkan daftar penumpang yang harus dijemput secara berurutan.
  - Tombol aksi kontekstual: Navigasi (membuka Google Maps), Hubungi (membuka WhatsApp), dan Sudah Dijemput.
  - Tombol "Selesaikan Perjalanan" yang aktif setelah semua tugas selesai.

- [x] **Papan Pekerjaan (Jadwal Besok):**
  - Halaman khusus untuk melihat daftar jadwal perjalanan yang tersedia untuk hari berikutnya.
  - Kemampuan untuk mengklaim jadwal langsung dari aplikasi.

## 🛠️ Tech Stack & Arsitektur
Aplikasi ini dibangun dengan tumpukan teknologi modern yang direkomendasikan untuk pengembangan Android saat ini.

- **Bahasa:** Kotlin  
- **UI:** Jetpack Compose  
- **Arsitektur:** MVVM (Model-View-ViewModel)  
- **Asynchronous:** Kotlin Coroutines & StateFlow  
- **Networking:** Retrofit 2 & OkHttp 3  
- **Navigasi:** Jetpack Navigation Component  
- **Penyimpanan Aman:** EncryptedSharedPreferences  
- **Notifikasi Push:** Firebase Cloud Messaging (FCM)  
- **Lokasi:** Google Play Services Location API  

## 📂 Struktur Proyek
Struktur direktori proyek diatur berdasarkan fitur dan lapisan arsitektur untuk menjaga keterbacaan dan skalabilitas.

```
com.travala.driver
├── data                # Lapisan Data (Repository, Model, API Service)
│   ├── api
│   └── model
├── services            # Services yang berjalan di latar belakang (FCM, Lokasi)
├── ui                  # Semua komponen UI (Jetpack Compose)
│   ├── available_orders
│   ├── login
│   ├── navigation
│   ├── offer
│   ├── schedule
│   └── theme
└── utils               # Kelas-kelas pembantu (SessionManager, TripManager)
```

## 🚀 Panduan Setup
Untuk menjalankan proyek ini di lingkungan development, ikuti langkah-langkah berikut:

1. **Clone Repositori:**
   ```bash
   git clone https://github.com/[your-username]/[your-repo-name].git
   ```

2. **Buka di Android Studio:**  
   Buka proyek yang sudah di-clone menggunakan Android Studio versi terbaru (Hedgehog atau lebih baru direkomendasikan).

3. **Konfigurasi Firebase:**  
   - Unduh file `google-services.json` dari project Firebase Anda.  
   - Letakkan file tersebut di dalam direktori `app/`.  

4. **Konfigurasi Alamat Backend:**  
   - Buka file `data/api/RetrofitClient.kt`.  
   - Ubah nilai konstanta `BASE_URL` menjadi alamat server backend Anda (misalnya URL dari ngrok).  

   ```kotlin
   private const val BASE_URL = "https://your-backend-api.com/"
   ```

5. **Build & Run:**  
   Lakukan Gradle Sync, lalu build dan jalankan aplikasi pada emulator atau perangkat fisik.
