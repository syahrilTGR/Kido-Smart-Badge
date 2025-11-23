# Laporan Progres & Panduan Developer Aplikasi KidoSmartBadge

Dokumen ini menjelaskan status pengembangan terkini dari aplikasi KidoSmartBadge, arsitektur yang digunakan, dan cara untuk melanjutkan pengembangan.

## 1. Ringkasan Proyek

Aplikasi ini berfungsi sebagai antarmuka bagi **Orang Tua (Ortu)** dan **Mentor** untuk berinteraksi dengan ekosistem KidoSmartBadge. Fitur utama yang sudah diimplementasikan adalah alur login berbasis peran (role) dan fitur spesifik untuk setiap peran:
-   **Ortu:** Menautkan kartu RFID anak ke akun mereka.
-   **Mentor:** Menyetujui proyek yang telah diselesaikan oleh anak.

## 2. Arsitektur & Teknologi

-   **UI:** 100% Jetpack Compose.
-   **Arsitektur:** Menggunakan pola MVVM (Model-View-ViewModel) per layar.
    -   **View:** Composable screen (misal: `LoginScreen.kt`, `HomeScreen.kt`).
    -   **ViewModel:** Kelas yang menangani logika UI dan state (misal: `LoginViewModel.kt`, `HomeViewModel.kt`).
-   **Navigasi:** Jetpack Navigation for Compose untuk perpindahan antar layar.
-   **Backend:** Firebase (Authentication & Realtime Database).

## 3. Alur & Fitur Utama

### 3.1. Alur Login & Role (Paling Penting!)

Aplikasi ini **tidak memiliki fitur registrasi**. Semua akun dibuat secara manual oleh admin melalui Firebase Console untuk menjaga keamanan.

1.  Pengguna memasukkan email dan password di `LoginScreen`.
2.  `LoginViewModel` melakukan otentikasi ke Firebase.
3.  Setelah berhasil, ViewModel akan membaca `role` pengguna dari Realtime Database di path `/users/{userId}/role`.
4.  Berdasarkan `role` yang didapat ("Ortu" atau "Mentor"), aplikasi akan mengarahkan pengguna ke `HomeScreen` dengan tampilan yang sesuai.

### 3.2. Alur Orang Tua (Ortu)

Setelah login, orang tua akan melihat tombol "Link a New Child's Card".
1.  **Input Nama Anak:** Menekan tombol akan membawa ke `AddChildScreen` untuk memasukkan nama panggilan anak.
2.  **Input Kode Pairing:** Setelah itu, pengguna diarahkan ke `LinkCardScreen`. Di sini, pengguna harus memasukkan kode 6 digit yang didapatkan dari perangkat ESP32.
3.  **Proses "Jabat Tangan" (Handshake):**
    -   Aplikasi memverifikasi kode ke path `/pairing_codes/{kode_6_digit}` di Firebase.
    -   Aplikasi mengecek `status` ("waiting_for_app") dan `timestamp` (tidak lebih dari 60 detik).
    -   Jika valid, aplikasi mengubah status menjadi `app_verified_waiting_for_card`.
    -   ESP32 mendeteksi perubahan status ini, lalu menunggu kartu di-tap.
    -   Saat kartu di-tap, ESP32 mengirimkan UID kartu ke path `/pairing_codes/{kode_6_digit}/rfid_uid`.
    -   Aplikasi yang sedang "mendengarkan" path tersebut menerima UID, lalu memfinalisasi penautan.
4.  **Finalisasi:** Aplikasi akan membuat dua entri baru di database:
    -   Menyimpan data anak di `/users/{uid_ortu}/children/{child_id}`.
    -   Membuat pemetaan di `/rfid_to_parent_mapping/{uid_kartu_rfid}`.

### 3.3. Alur Mentor

Setelah login, mentor akan melihat tampilan "Approve Project" di `HomeScreen`.
1.  Mentor memasukkan **Card UID** anak dan **Nama Proyek** yang disetujui.
2.  Menekan tombol "Approve" akan menulis data ke path `/approval_pending/{uid_kartu}` dengan value nama proyek.
3.  Data ini nantinya akan dibaca oleh ESP32 saat anak melakukan "Tap-to-Claim".

## 4. Struktur Database Firebase (Realtime Database)

```
/
|-- users
|   |-- {userId}
|       |-- role: "Ortu" | "Mentor"
|       |-- children
|           |-- {childId}
|               |-- name: "Nama Panggilan Anak"
|               |-- rfidUid: "UID_KARTU_HEX"
|
|-- pairing_codes
|   |-- {6_digit_code}  // Node sementara, akan dihapus setelah pairing
|       |-- status: "waiting_for_app" | "app_verified_waiting_for_card" | "completed"
|       |-- timestamp: 167... (Waktu server Firebase)
|       |-- rfid_uid: "UID_KARTU_HEX" (Diisi oleh ESP32)
|
|-- rfid_to_parent_mapping
|   |-- {rfidUid}: "{userId_ortu}"
|
|-- approval_pending
|   |-- {rfidUid}: "Nama Proyek"
|
|-- absensi (Sudah ada di kode ESP32)
|
|-- badges_earned (Sudah ada di kode ESP32)
```

## 5. Panduan Menjalankan & Setup

### 5.1. Prasyarat
-   Pastikan file `google-services.json` yang benar sudah ada di dalam folder `app/`.
-   Gunakan Android Studio versi stabil terbaru.

### 5.2. Menambahkan Pengguna Baru (Penting!)
Untuk bisa login, ikuti langkah berikut:
1.  Buka **Firebase Console > Authentication > Users > Add user**. Buat pengguna baru dengan email dan password.
2.  Salin **User UID** dari pengguna yang baru dibuat.
3.  Buka **Realtime Database**.
4.  Di bawah node `/users`, tambahkan child baru dengan **key** adalah **User UID** yang tadi disalin.
5.  Di dalam node UID tersebut, tambahkan data baru dengan **key** `role` dan **value** `Ortu` atau `Mentor`.

## 6. Struktur Kode

-   `app/src/main/java/com/example/kidosmartbadge/`
    -   `data/`: Berisi data class (`Pairing.kt`) dan repository (`PairingRepository.kt`) yang menangani logika komunikasi ke Firebase untuk proses pairing.
    -   `navigation/`: Mengatur semua alur navigasi aplikasi (`NavGraph.kt`).
    -   `ui/`: Berisi semua file UI (Composable screens) dan ViewModel-nya.
        -   `login/`: Layar login dan `LoginViewModel`.
        -   `home/`: Layar utama setelah login (`HomeScreen`) dan `HomeViewModel`.
        -   `addchild/`: Layar untuk input nama anak.
        -   `linkcard/`: Layar untuk input kode pairing dan `LinkCardViewModel`.
    -   `MainActivity.kt`: Titik masuk utama aplikasi yang hanya menyiapkan `NavGraph`.

## 7. Langkah Selanjutnya (TODO)

-   Pada `HomeScreen.kt`, di dalam `ParentView`, ada `// TODO: Display list of linked children here`. Ini adalah langkah selanjutnya yang bagus, yaitu mengambil dan menampilkan daftar anak yang sudah berhasil ditautkan dari path `/users/{uid_ortu}/children`.
