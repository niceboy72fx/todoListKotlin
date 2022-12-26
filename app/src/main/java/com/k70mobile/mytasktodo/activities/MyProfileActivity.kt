package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.databinding.ActivityMyProfileBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.User
import com.k70mobile.mytasktodo.utils.Constants
import java.util.jar.Manifest

class MyProfileActivity : BaseActivity() {

    private var selectedImageFileUri: Uri? = null
    private var profileImageURL: String = ""

    private lateinit var userDetail: User

    private var binding: ActivityMyProfileBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreDB().loadUserData(this)

        binding?.ivProfileUserImage?.setOnClickListener {
            // Check App has permission already
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this@MyProfileActivity,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    // It could have been any number that we wanted.
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnUpdate?.setOnClickListener {
            if (selectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Ask permission for app, if user didn't give a permission, explain it's important for this app
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            }
        } else {
            Toast.makeText(
                this@MyProfileActivity,
                "Bạn vừa từ chối quyền truy cập của ứng dụng, hãy chỉnh sửa lại trong phần cài đặt của ứng dụng",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val resultOk = resultCode == Activity.RESULT_OK  //true
        val pickImageRequest = resultCode == Constants.PICK_IMAGE_REQUEST_CODE //false
        val hasData = data?.data != null //true

        Log.i("Bools", "resok: $resultOk, pickreq: $pickImageRequest, hasdata: $hasData")

        if (resultOk && hasData) {
            selectedImageFileUri = data!!.data
            try {
                // Load the user image in the ImageView.
                binding?.ivProfileUserImage?.let {
                    Glide
                        .with(this)
                        .load(Uri.parse(selectedImageFileUri.toString())) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(it) // the view in which the image will be loaded.
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = resources.getString(R.string.my_profile_title)
        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {

        userDetail = user

        binding?.ivProfileUserImage?.let {
            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(it)
        }

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)

        if (user.mobile != "") {
            binding?.etMobile?.setText(user.mobile)
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance("gs://task-todo-list-4e1bc.appspot.com").reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(selectedImageFileUri)
            )

            sRef.putFile(selectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    profileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (profileImageURL.isNotEmpty() && profileImageURL != userDetail.image) {
            userHashMap[Constants.IMAGE] = profileImageURL
        }

        if (binding?.etName?.text.toString() != userDetail.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
        }

        if (binding?.etMobile?.text.toString() != userDetail.mobile) {
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString()
        }

        FirestoreDB().updateUserProfileData(this, userHashMap)

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}