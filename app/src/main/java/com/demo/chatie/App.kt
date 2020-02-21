package com.demo.chatie

import android.app.Application
import android.util.Log
import com.demo.chatie.login.repository.LoginRepository
import com.demo.chatie.login.view.LoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId

class App : Application() {
    val loginRepository: LoginRepository
        get() = provideLoginRepository()

    val deviceToken: String?
        get() = getDeviceFCMToken()


    private fun provideLoginRepository(): LoginRepository {
        synchronized(this) {
            return LoginRepository()
        }
    }

    private fun getDeviceFCMToken(): String? {
        var token: String? = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(LoginActivity.TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                token = task.result?.token

                // Log and toast
                //val msg = getString(R.string.msg_token_fmt, token)
                //Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
        return token
    }
}