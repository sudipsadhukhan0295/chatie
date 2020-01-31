package com.demo.chatie.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo.chatie.*
import com.demo.chatie.login.viewmodel.LoginViewModel
import com.demo.chatie.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    var verificationId = ""

    var check: Int = 0
    var uid = ""

    val db = FirebaseFirestore.getInstance()

/*    private val mBinding: ActivityLoginBinding by lazy {
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
    }*/

    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val TAG = "Login"
    }

    private val getOtpObserver =
        Observer<ApiResponse<String>> { response: ApiResponse<String>? ->
            if (response !== null) {
                if (response.throwable == null) {
                    verificationId = response.responseBody.toString()
                    et_otp.visibility = View.VISIBLE
                    ll_input_box.visibility = View.GONE
                    abtn_sign_in.visibility = View.GONE
                }
            }
        }

    private val getUserLoginObserver =
        Observer<ApiResponse<FirebaseUser>> { response: ApiResponse<FirebaseUser>? ->
            if (response != null) {
                if (response.throwable == null) {
                    uid = response.responseBody?.uid!!
                    Toast.makeText(this, "Successfully login", Toast.LENGTH_LONG).show()

                    db.collection("user").document(response.responseBody?.uid!!).get()
                        .addOnSuccessListener {
                            if (it.data != null) {
                                startActivity(Intent(this, HomeActivity::class.java))
                            } else {
                                view_phone_no.visibility = View.GONE
                                view_user_name.visibility = View.VISIBLE
                            }
                        }
                } else {
                    Toast.makeText(this, "Can't login", Toast.LENGTH_LONG).show()

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

        if (check != null) {
            Log.d("asd", "asd")
        }

        if (auth.currentUser != null) startActivity(Intent(this, HomeActivity::class.java))

        setContentView(R.layout.activity_login)

        //DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this, (applicationContext as App)))
            .get(LoginViewModel::class.java)

        viewModel.userLiveData.observe(this, getUserLoginObserver)

        abtn_sign_in.setOnClickListener {
            viewModel.sendOtp("+91" + et_phone_no.text.toString())?.observe(this, getOtpObserver)
        }

        et_otp.setPinViewEventListener(object : Pinview.PinViewEventListener {

            override fun onDataEntered(pinview: Pinview, fromUser: Boolean) {
                if (et_otp.value.length == 6) {
                    val credential = PhoneAuthProvider.getCredential(
                        verificationId,
                        et_otp.value
                    )
                    viewModel.login(credential)
                }
            }
        })

        btn_save.setOnClickListener {
            if (et_name.text.toString().isNotEmpty()) {
                val userDetail = User()

                userDetail.name = et_name.text.toString()
                db.collection("user").document(uid)
                    .set(userDetail)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }
    }
}
