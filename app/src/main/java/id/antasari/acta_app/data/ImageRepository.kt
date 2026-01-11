package id.antasari.acta_app.data

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.ByteArrayOutputStream
import java.util.UUID

class ImageRepository(private val context: Context) {

    private var isInitialized = false

    // 1. Inisialisasi Cloudinary (Hanya sekali)
    private fun initCloudinary() {
        if (!isInitialized) {
            try {
                // Cek apakah sudah init sebelumnya agar tidak crash
                MediaManager.get()
                isInitialized = true
            } catch (e: Exception) {
                // Masukkan Credential Cloudinary Kamu Di Sini!
                val config = HashMap<String, String>()
                config["cloud_name"] = "dqw8qsnd5"
                config["api_key"] = "522281866577485"
                config["api_secret"] = "kXguv7-Y7bZbBA7y9KMpGTMQGt4"

                MediaManager.init(context, config)
                isInitialized = true
            }
        }
    }

    // 2. Fungsi Upload Foto
    fun uploadImage(
        bitmap: Bitmap,
        folderName: String = "acta_missions", // Folder di Cloudinary
        onSuccess: (String) -> Unit, // Callback mengembalikan URL gambar
        onError: (String) -> Unit
    ) {
        initCloudinary()

        // Ubah Bitmap jadi ByteArray agar bisa diupload
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val uniqueName = "mission_${UUID.randomUUID()}"

        // Mulai Upload
        MediaManager.get().upload(byteArray)
            .option("public_id", uniqueName)
            .option("folder", folderName)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Bisa tambah loading indicator jika mau
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Update progress bar
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Ambil URL aman (https) dari hasil upload
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onError("Gagal mendapatkan URL gambar.")
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Upload Gagal: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    onError("Upload di-reschedule.")
                }
            })
            .dispatch()
    }
}