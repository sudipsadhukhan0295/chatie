package com.demo.chatie.login.repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.demo.chatie.login.view.LoginActivity.Companion.TAG
import com.demo.chatie.model.User
import com.demo.chatie.network.ApiResponse
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LoginRepository {

    private val auth = FirebaseAuth.getInstance()
    val userLiveData = MutableLiveData<ApiResponse<FirebaseUser>>()
    private val db = FirebaseFirestore.getInstance()


    fun sendOtp(email: String, activity: Activity): MutableLiveData<ApiResponse<String>> {
        val liveData = MutableLiveData<ApiResponse<String>>()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(email, 60, TimeUnit.SECONDS, activity,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted:$credential")

                    signInWithPhoneAuthCredential(credential, activity)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        liveData.value = ApiResponse(e)
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "onCodeSent:$verificationId")
                    liveData.value = ApiResponse(verificationId)
                }
            }
        )
        return liveData
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, activity: Activity) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    userLiveData.value = ApiResponse(task.result?.user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                        userLiveData.value =
                            ApiResponse(task.exception)
                    }
                }
            }
    }

    suspend fun checkUserAvailability(uid: String): ApiResponse<DocumentSnapshot> {

        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("user").document(uid).get().await()
                return@withContext ApiResponse(querySnapshot)
            } catch (t: Throwable) {
                return@withContext ApiResponse<DocumentSnapshot>(t)
            }
        }
    }

    suspend fun addUser(uid: String, userDetail: User): ApiResponse<out Any> {
        addUserDetailOnAuth(userDetail.name)
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("user").document(uid).set(userDetail).await()
                return@withContext ApiResponse(querySnapshot)
            } catch (t: Throwable) {
                return@withContext ApiResponse<DocumentSnapshot>(t)
            }
        }
    }

    private fun addUserDetailOnAuth(name: String?) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }
    }
}