# ACTA App 🌱

> **"Every action matters."**

ACTA adalah aplikasi mobile berbasis gamifikasi yang mengajak penggunanya untuk membangun kebiasaan ramah lingkungan (eco-friendly habits). Pengguna dapat menyelesaikan misi harian, memotret bukti aksi nyata, mendapatkan poin (Seeds), dan bersaing di Leaderboard.

---

## 📱 Download Demo (APK)

Ingin mencoba aplikasi ini langsung di HP Android Anda? Silakan unduh file APK melalui link di bawah ini:

👉 **[DOWNLOAD ACTA.APK DI SINI] (https://drive.google.com/drive/folders/1JWGaBopwyNnfi2RDBRb-ZmxD0yp1SPcN?usp=sharing)** 👈

> **Catatan:** Karena aplikasi ini belum rilis di Play Store, Anda mungkin perlu mengizinkan instalasi dari *"Unknown Sources"* di pengaturan HP Anda saat menginstal.

---

## ✨ Fitur Utama

### 1. 🎯 Misi & Leveling System
* **Timeline Misi:** Alur misi bertahap (Locked/Active/Completed).
* **Gamifikasi:** Dapatkan **XP** dan **Seeds** setiap menyelesaikan misi.
* **Level Up:** Naikkan status dari *Seedling* hingga menjadi *Guardian*.

### 2. 📸 Bukti Aksi (CameraX)
* Ambil foto aksi nyata langsung dari dalam aplikasi.
* Integrasi **Cloudinary** untuk penyimpanan gambar yang efisien.
* Validasi otomatis untuk klaim reward.

### 3. 🛍️ Eco-Market (Shop)
* Tukarkan **Seeds** yang didapat dengan item virtual.
* Beli **Avatar Frames** eksklusif atau item utilitas seperti *Streak Freeze*.

### 4. 🌍 Komunitas & Sosial
* **Social Feed:** Bagikan aksi kamu dan lihat aksi pengguna lain.
* **Like System:** Berikan apresiasi pada postingan komunitas.
* **Leaderboard:** Bersaing menjadi yang teratas dalam Liga Lingkungan.

### 5. 👤 Personalisasi Profil
* Pilih **Avatar Karakter** lucu (Kucing, Kelinci, Pohon, dll).
* Lihat statistik lengkap (Streak, Total XP, Seeds).
* Sistem **Badges/Achievements** yang bisa dibuka.

---

## 🛠️ Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan **Modern Android Development** stack:

* **Bahasa:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material3)
* **Backend & Database:** Firebase (Auth, Firestore)
* **Cloud Storage:** Cloudinary (untuk hosting gambar)
* **Image Loading:** Coil
* **Camera:** CameraX
* **Architecture:** MVVM (Model-View-ViewModel) concept

---

## 📸 Screenshots

| Home Screen | Mission Detail | Camera |
|:---:|:---:|:---:|
| <img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/7465f4d7-702d-448d-b901-647f5e0e72e9" /> | <img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/8e94b8d5-bf33-4ff2-83a7-a89aea9f30e1" /> | <img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/51505d2b-6df2-4e04-9d39-804275395a9c" /> |

| Feed | Shop | Profile |
|:---:|:---:|:---:|
| <img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/c3c9626e-25b9-4bb5-aaa1-c9d4ed57347c" /> |<img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/28ef8f69-9526-4e03-b4b2-cece5fbe1ff1" /> | <img width="303" height="618" alt="image" src="https://github.com/user-attachments/assets/d4811be8-4e5c-445e-8695-3f0d512f2736" /> |

---

## 🚀 Cara Menjalankan Project (Untuk Developer)

Jika Anda ingin menjalankan kode sumber ini di Android Studio:

1.  Clone repository ini.
2.  Buka di **Android Studio**.
3.  Pastikan Anda memiliki file `google-services.json` (Firebase) Anda sendiri di folder `app/`.
4.  Konfigurasi API Key Cloudinary di file `local.properties` atau `Constants`.
5.  Build dan Run!

---

Created with 💚 by **[Nama/Username GitHub Kamu]**
