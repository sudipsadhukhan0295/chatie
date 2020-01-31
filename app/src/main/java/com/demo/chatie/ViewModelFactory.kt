package com.demo.chatie

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.demo.chatie.login.viewmodel.LoginViewModel


@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(private val activity: Activity, private val app: App) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(LoginViewModel::class.java) ->
                    LoginViewModel(app.loginRepository, activity)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}