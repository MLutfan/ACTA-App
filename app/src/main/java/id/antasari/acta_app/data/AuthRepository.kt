package id.antasari.acta_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String, username: String, photoUrl: String = ""): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                val userData = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "photoUrl" to photoUrl,
                    "points" to 0,
                    "level" to 1,
                    // Pastikan field ini konsisten dengan HomeScreen (seeds/exp)
                    // Saya tambahkan exp & seeds default agar tidak error di Home
                    "exp" to 0,
                    "seeds" to 0,
                    "currentStreak" to 0,
                    "currentLevel" to 1,
                    "totalCo2Saved" to 0.0,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(user.uid).set(userData).await()
            }

            Result.success(user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PERBAIKAN DI SINI ---
    // Saya ubah namanya jadi 'signOut' agar terbaca oleh ProfileScreen
    fun signOut() {
        auth.signOut()
    }

    suspend fun addPoints(amount: Int): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("No User Login"))

        return try {
            db.collection("users").document(user.uid)
                .update("points", com.google.firebase.firestore.FieldValue.increment(amount.toLong()))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}