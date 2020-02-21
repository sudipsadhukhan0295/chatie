package com.demo.chatie.login.viewmodel

import android.app.Activity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.chatie.network.ApiResponse
import com.demo.chatie.login.repository.LoginRepository
import com.demo.chatie.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

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

        loginRepository.signInWithPhoneAuthCredential(credential, activity)
    }

    fun checkUserAvailability(uid: String): MutableLiveData<ApiResponse<DocumentSnapshot>> {
        val data = MutableLiveData<ApiResponse<DocumentSnapshot>>()

        viewModelScope.launch {
            loginRepository.checkUserAvailability(uid).let {
                data.value = it
            }
        }
        return data
    }

    fun addUser(uid: String, userDetail: User): MutableLiveData<ApiResponse<out Any>> {
        val data = MutableLiveData<ApiResponse<out Any>>()

        viewModelScope.launch {
            loginRepository.addUser(uid, userDetail).let {
                data.value = it
            }
        }
        return data
    }
}