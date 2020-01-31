package com.demo.chatie.login.viewmodel

import android.app.Activity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.chatie.ApiResponse
import com.demo.chatie.login.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential


class LoginViewModel(private val loginRepository: LoginRepository, private val activity: Activity) :
    ViewModel() {

    var userLiveData = MediatorLiveData<ApiResponse<FirebaseUser>>()

    init {
        userLiveData.addSource(loginRepository.userLiveData) { response ->
            userLiveData.value = response
        }
    }

    fun sendOtp(phoneNumber: String): MutableLiveData<ApiResponse<String>>? {

        return loginRepository.sendOtp(phoneNumber, activity)
    }

    fun login(credential: PhoneAuthCredential) {

        return loginRepository.signInWithPhoneAuthCredential(credential, activity)
    }
}