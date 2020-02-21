package com.demo.chatie.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.demo.chatie.App
import com.demo.chatie.R
import com.demo.chatie.ViewModelFactory
import com.demo.chatie.databinding.ActivityLoginBinding
import com.demo.chatie.home.HomeActivity
import com.demo.chatie.login.viewmodel.LoginViewModel
import com.demo.chatie.model.User
import com.demo.chatie.network.ApiResponse
import com.demo.chatie.util.Pinview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: LoginViewModel
    private var verificationId = ""
    private var uid = ""

    private val mBinding: ActivityLoginBinding by lazy {
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
    }

    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val TAG = "Login"
    }

    private val getOtpObserver =
        Observer<ApiResponse<String>> { response: ApiResponse<String>? ->
            mBinding.apply {
                if (response !== null) {
                    if (response.throwable == null) {
                        verificationId = response.responseBody.toString()
                        etOtp.visibility = View.VISIBLE
                        llInputBox.visibility = View.GONE
                        abtnSignIn.visibility = View.GONE
                    }
                }
            }
        }

    private val setUserObserver =
        Observer<ApiResponse<out Any>> { response: ApiResponse<out Any>? ->
            if (response !== null) {
                if (response.throwable == null) {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                }
            }
        }

    private val checkUserAvailability =
        Observer<ApiResponse<DocumentSnapshot>> { response: ApiResponse<DocumentSnapshot> ->
            mBinding.apply {
                if (response.throwable == null) {
                    if (response.responseBody != null) {
                        if (response.responseBody!!.data != null) {
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        } else {
                            view_phone_no.visibility = View.GONE
                            view_user_name.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.e("=================", response.throwable?.message!!)
                }
            }
        }

    private val getUserLoginObserver =
        Observer<ApiResponse<FirebaseUser>> { response: ApiResponse<FirebaseUser>? ->
            mBinding.apply {
                if (response != null) {
                    if (response.throwable == null) {
                        uid = response.responseBody?.uid!!
                        Toast.makeText(this@LoginActivity, "Successfully login", Toast.LENGTH_LONG)
                            .show()
                        viewModel.checkUserAvailability(response.responseBody?.uid!!)
                            .observe(this@LoginActivity, checkUserAvailability)
                    } else {
                        Toast.makeText(this@LoginActivity, "Can't login", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (auth.currentUser != null) {
            if (auth.currentUser!!.displayName != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else auth.signOut()
        }
        viewModel = ViewModelProvider(this, ViewModelFactory(this, (applicationContext as App)))
            .get(LoginViewModel::class.java)

        viewModel.userLiveData.observe(this, getUserLoginObserver)

        initUI()
    }

    private fun initUI() {

        mBinding.apply {
            abtnSignIn.setOnClickListener(this@LoginActivity)
            btnSave.setOnClickListener(this@LoginActivity)
            etOtp.setPinViewEventListener(object : Pinview.PinViewEventListener {

                override fun onDataEntered(pinview: Pinview, fromUser: Boolean) {
                    if (et_otp.value.length == 6) {
                        val credential =
                            PhoneAuthProvider.getCredential(verificationId, etOtp.value)

                        viewModel.login(credential)
                    }
                }
            })
        }
    }

    override fun onClick(view: View?) {
        mBinding.apply {
            when (view) {
                btnSave -> {
                    if (etName.text.toString().isNotEmpty()) {
                        val userDetail = User()
                        userDetail.name = etName.text.toString()
                        userDetail.phoneNo = auth.currentUser?.phoneNumber
                        userDetail.uid = auth.uid!!
                        viewModel.addUser(uid, userDetail)
                            .observe(this@LoginActivity, setUserObserver)
                    }
                }
                abtnSignIn -> {
                    viewModel.sendOtp("+91" + etPhoneNo.text.toString())
                        ?.observe(this@LoginActivity, getOtpObserver)
                }
            }
        }
    }
}
