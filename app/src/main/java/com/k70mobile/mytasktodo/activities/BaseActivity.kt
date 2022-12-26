package com.k70mobile.mytasktodo.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.databinding.ActivityBaseBinding
import com.k70mobile.mytasktodo.utils.Constants

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var progressDialog: Dialog

    private var binding: ActivityBaseBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    fun showProgressDialog(text: String) {
        progressDialog = Dialog(this)

        // Set the screen from a layout resource.
        // The resource will be inflated, adding all top-level view to the screen
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text
        // Start dialog and display it on screen
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

//    fun getCurrentUserID(): String {
//        return FirebaseAuth.getInstance().currentUser!!.uid
//    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()
        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()
    }

    fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, Constants.PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}