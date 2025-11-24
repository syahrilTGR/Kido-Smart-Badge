### Deskripsi Proyek & Pertanyaan Kunci: Aplikasi Android KidoSmartBadge

Dokumen ini bertujuan untuk memberikan gambaran menyeluruh tentang status aplikasi Android saat ini dan mengidentifikasi pertanyaan-pertanyaan kunci yang memerlukan keputusan dari stakeholder untuk pengembangan selanjutnya.

#### **Bagian 1: Status & Fungsionalitas Aplikasi Saat Ini**

Aplikasi Android KidoSmartBadge telah berhasil dikembangkan menjadi sebuah prototipe fungsional yang stabil. Aplikasi ini berfungsi sebagai antarmuka utama bagi dua peran pengguna yang berbeda: **Orang Tua (Ortu)** dan **Mentor**.

**Fitur Utama yang Telah Diimplementasikan:**

1.  **Sistem Login Berbasis Peran (Role-based):**
    -   Pengguna login menggunakan email dan password.
    -   Aplikasi secara otomatis mendeteksi peran pengguna ("Ortu" atau "Mentor") dari Firebase dan menampilkan halaman yang sesuai.
    -   Sesi login bersifat persisten (pengguna tidak perlu login ulang setiap kali membuka aplikasi) dan fungsionalitas logout telah tersedia.

2.  **Alur untuk Orang Tua (Ortu):**
    -   **Registrasi Mandiri yang Aman:** Orang tua dapat membuat akun baru secara mandiri melalui aplikasi dengan memvalidasi **UID Kartu** yang sudah didaftarkan sebelumnya oleh admin. Ini mengikat akun ke kartu fisik.
    -   **Manajemen Anak:** Setelah registrasi atau login, orang tua dapat melihat daftar anak yang tertaut dengan akunnya.
    -   **Halaman Detail Anak:** Dengan mengklik nama anak, orang tua dapat melihat halaman detail yang berisi:
        -   **Riwayat Kehadiran:** Daftar tanggal dan waktu kapan anak melakukan absensi.
        -   **Daftar Pencapaian:** Daftar semua "badge" atau proyek yang telah berhasil diklaim oleh anak.
    -   **Penautan Kartu (Pairing):** Terdapat alur alternatif untuk menautkan kartu baru ke akun orang tua yang sudah ada menggunakan kode 6 digit dari perangkat ESP32.

3.  **Alur untuk Mentor:**
    -   **Persetujuan Proyek yang Praktis:** Halaman Mentor kini menampilkan **dropdown berisi nama semua anak** yang terdaftar di sistem.
    -   Mentor tidak perlu lagi mengetik UID kartu. Mereka cukup memilih nama anak dari daftar, mengetik nama proyek yang disetujui, dan menekan "Approve".

4.  **Arsitektur & Keamanan:**
    -   Aplikasi dibangun dengan arsitektur modern (MVVM per layar, Jetpack Compose, Navigation).
    -   Alur registrasi dan login dirancang dengan mempertimbangkan keamanan untuk mencegah pengguna biasa mendapatkan hak akses Mentor.

#### **Bagian 2: Pertanyaan Kunci untuk Pemilik KidoRobot**

Untuk memastikan pengembangan selanjutnya sejalan dengan visi produk dan kebutuhan bisnis, berikut adalah daftar pertanyaan yang perlu didiskusikan:

**A. Pertanyaan tentang Logika Bisnis & Data**

1.  **Daftar Proyek/Kursus:**
    -   "Saat ini, Mentor mengetik nama proyek secara bebas. Apakah seharusnya ada **daftar proyek atau kursus yang sudah ditentukan** (misalnya: 'Dasar Robotik', 'Coding Python', 'Desain 3D') yang bisa dipilih Mentor dari sebuah daftar?"
    -   *(Kenapa ini penting? Jika ya, kita perlu membuat struktur data baru di Firebase untuk menyimpan daftar kursus ini dan mengubah UI Mentor dari input teks menjadi dropdown).*

2.  **Detail "Badge":**
    -   "Saat ini, 'badge' atau pencapaian hanya berupa nama proyek. Apakah sebuah badge seharusnya memiliki **detail lain?** Misalnya: ikon/gambar, deskripsi singkat, atau poin/nilai yang didapat?"
    -   *(Kenapa ini penting? Ini akan memperkaya halaman detail anak dan memerlukan perubahan pada struktur data `/badges_earned`)*.

3.  **Manajemen Kartu Baru:**
    -   "Alur registrasi kita saat ini mengasumsikan ada daftar UID kartu di `/unclaimed_cards`. Bagaimana **proses kerja di dunia nyata** untuk memasukkan UID kartu baru ke dalam sistem tersebut sebelum kartu diberikan ke pelanggan?"
    -   *(Kenapa ini penting? Untuk memastikan alur teknis kita cocok dengan alur operasional di lapangan. Apakah perlu dibuatkan aplikasi admin khusus untuk ini?)*

4.  **Tipe Absensi:**
    -   "Sekarang tipe absensi hanya 'DATANG'. Apakah ada kebutuhan untuk tipe lain di masa depan, seperti **'PULANG'**, atau bahkan 'IZIN' dan 'SAKIT' yang diinput oleh orang tua melalui aplikasi?"
    -   *(Kenapa ini penting? Ini akan mempengaruhi logika di ESP32 dan juga bisa menjadi fitur baru untuk aplikasi orang tua).*

**B. Pertanyaan tentang Fitur & Pengalaman Pengguna (UX)**

5.  **Notifikasi untuk Orang Tua:**
    -   "Seberapa penting bagi orang tua untuk mendapatkan **notifikasi push** secara real-time? Misalnya, saat anak berhasil absen atau saat anak mendapatkan badge baru."
    -   *(Kenapa ini penting? Implementasi push notification (via Firebase Cloud Messaging) adalah langkah pengembangan yang cukup besar dan perlu diprioritaskan).*

6.  **Visi Halaman Detail Anak:**
    -   "Selain riwayat kehadiran dan daftar badge, informasi apalagi yang paling berharga untuk ditampilkan di halaman detail anak? Apakah ada bayangan untuk menampilkan **grafik kemajuan, ringkasan mingguan, atau catatan dari Mentor?**"
    -   *(Kenapa ini penting? Untuk memandu desain dan pengembangan fitur di halaman yang paling sering dilihat orang tua).*

7.  **Kebutuhan Mentor:**
    -   "Selain menyetujui proyek, apakah ada fitur lain yang dibutuhkan oleh Mentor? Misalnya, **melihat riwayat persetujuan yang telah mereka berikan** atau **menambahkan catatan** saat menyetujui proyek?"
    -   *(Kenapa ini penting? Untuk mengembangkan sisi aplikasi Mentor agar lebih dari sekadar satu fungsi).*
