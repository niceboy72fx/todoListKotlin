package com.k70mobile.mytasktodo.activities

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.databinding.ActivitySignUpBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.User

class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null
    private var isShowPassword = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }

        binding?.ivShowPassword?.setOnClickListener { passwordLeak() }
    }

    private fun registerUser() {
        val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Tạo tài khoản")
            alertDialog.setMessage("Bạn có muốn tạo tài khoản với Email '$email' và mật khẩu là '$password'?")
            alertDialog.setPositiveButton("Đồng ý") { _, _ ->
                showProgressDialog(resources.getString(R.string.please_wait))
                // Register an account
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)
                            FirestoreDB().registerUser(this, user)
                        } else {
                            Toast.makeText(
                                this,
                                "Đăng ký không thành công, ${task.exception!!.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            alertDialog.setNegativeButton("Không") { _, _ -> }
            alertDialog.show()
        }
    }


    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Tên không được bỏ trống!")
                false
            }
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
            binding?.etPassword?.transformationMethod = PasswordTransformationMethod.getInstance()
            binding?.ivShowPassword?.setImageResource(R.drawable.ic_show_password)
            isShowPassword = false
        } else {
            binding?.etPassword?.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding?.ivShowPassword?.setImageResource(R.drawable.ic_hide_password)
            isShowPassword = true
        }
    }

    fun userRegisteredSuccess(user: User) {
        Toast.makeText(
            this,
            "Đăng ký thành công",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}