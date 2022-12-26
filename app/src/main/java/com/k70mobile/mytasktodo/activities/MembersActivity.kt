package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.adapters.MemberListItemAdapter
import com.k70mobile.mytasktodo.databinding.ActivityMembersBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.Board
import com.k70mobile.mytasktodo.models.User
import com.k70mobile.mytasktodo.utils.Constants

class MembersActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private lateinit var assignedMemberList: ArrayList<User>
    private var anyChangesMade: Boolean = false
    private var binding: ActivityMembersBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    fun setupMembersList(list: ArrayList<User>) {
        assignedMemberList = list
        hideProgressDialog()

        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemAdapter(this, list)
        binding?.rvMembersList?.adapter = adapter
    }

    fun memberDetails(user: User) {
        boardDetails.assignedTo.add(user.id)
        FirestoreDB().assignMemberToBoard(this, boardDetails, user)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMembersActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = resources.getString(R.string.members)
        binding?.toolbarMembersActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.action_add_member -> {
                    dialogSearchMember()
                    return true
                }
            }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreDB().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Hãy nhập Email của thành viên cần thêm",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccessfully(user: User) {
        hideProgressDialog()
        assignedMemberList.add(user)
        // If 'memberAssignSuccessfully' was called, changes were made.
        anyChangesMade = true
        // Add member details into screen
        // When call 'setupMembersList()', list 'assignedMemberList' will be assigned into 'list'
        // of 'setupMembersList()' and display on Recycler view
        setupMembersList(assignedMemberList)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}