package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.databinding.ActivityCreateBoardBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.Board
import com.k70mobile.mytasktodo.utils.Constants

class CreateBoardActivity : BaseActivity() {

    private var selectedImageFileUri: Uri? = null
    private var binding: ActivityCreateBoardBinding? = null
    private lateinit var userName: String
    private var boardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionbar()

        // By this way, We don't have to get the username here again and
        // have another database request because what we try to do is to
        // have as few requests as possible.
        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME).toString() // From MainActivity
        }

        binding?.ivBoardImage?.setOnClickListener {
            // Check App has permission already
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this@CreateBoardActivity,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    // It could have been any number that we wanted.
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnCreate?.setOnClickListener {
            if (selectedImageFileUri != null) {
                // Create board with image chooser
                uploadBoardImage()
            } else {
                // Create board without image chooser (with default image)
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun setupActionbar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener { onBackPressed() }
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
                this@CreateBoardActivity,
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
                binding?.ivBoardImage?.let {
                    Glide
                        .with(this)
                        .load(Uri.parse(selectedImageFileUri.toString())) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_board_place_holder) // A default place holder
                        .into(it) // the view in which the image will be loaded.
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createBoard() {
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(FirestoreDB().getCurrentUserID())

        val board = Board(
            binding?.etBoardName?.text.toString(),
            boardImageURL,
            userName,
            assignedUserArrayList
        )

        FirestoreDB().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference = FirebaseStorage.getInstance("gs://task-todo-list-4e1bc.appspot.com").reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(selectedImageFileUri)
        )

        sRef.putFile(selectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            Log.i("Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("Downloadable Image URL", uri.toString())
                boardImageURL = uri.toString()

                createBoard()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}