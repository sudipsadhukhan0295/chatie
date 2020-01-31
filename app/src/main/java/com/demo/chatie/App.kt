package com.demo.chatie

import android.app.Application
import com.demo.chatie.login.repository.LoginRepository

class App : Application() {
    val loginRepository: LoginRepository
        get() = provideLoginRepository()


    private fun provideLoginRepository(): LoginRepository {
        synchronized(this) {
            return LoginRepository()
        }
    }
}