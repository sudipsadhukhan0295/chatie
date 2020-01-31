package com.demo.chatie.login.repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.demo.chatie.ApiResponse
import com.demo.chatie.login.view.LoginActivity.Companion.TAG
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class LoginRepository {

    val auth = FirebaseAuth.getInstance()
    val userLiveData = MutableLiveData<ApiResponse<FirebaseUser>>()


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
                    //resendToken = token
                }
            }
        )
        return liveData
    }

    fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        activity: Activity
    ) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    userLiveData.value = ApiResponse(task.result?.user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                        userLiveData.value = ApiResponse(task.exception)
                    }
                }
            }

    }
}