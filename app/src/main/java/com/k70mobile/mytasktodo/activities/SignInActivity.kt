package com.k70mobile.mytasktodo.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.databinding.ActivitySignInBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.User

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private var binding: ActivitySignInBinding? = null
    private var isShowPassword = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        setContentView(binding?.root)
        setupActionBar()

        binding?.btnSignIn?.setOnClickListener { signInRegisterUser() }

        binding?.ivShowPassword?.setOnClickListener { passwordLeak() }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding?.toolbarSignInActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun signInRegisterUser() {
        val email: String = binding?.etEmailSignIn?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPasswordSignIn?.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            // Sign In Account
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        FirestoreDB().loadUserData(this, true)
                    } else {
                        hideProgressDialog()
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Email không được bỏ trống!")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Mật khẩu không được bỏ trống!")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun passwordLeak() {
        if (isShowPassword) {
            binding?.etPasswordSignIn?.transformationMethod = PasswordTransformationMethod.getInstance()
            binding?.ivShowPassword?.setImageResource(R.drawable.ic_show_password)
            isShowPassword = false
        } else {
            binding?.etPasswordSignIn?.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding?.ivShowPassword?.setImageResource(R.drawable.ic_hide_password)
            isShowPassword = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun signInSuccess(user: User?) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}