package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.adapters.TaskListItemsAdapter
import com.k70mobile.mytasktodo.databinding.ActivityTaskListBinding
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.Board
import com.k70mobile.mytasktodo.models.Card
import com.k70mobile.mytasktodo.models.Task
import com.k70mobile.mytasktodo.models.User
import com.k70mobile.mytasktodo.utils.Constants
import java.text.FieldPosition

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null
    private lateinit var boardDetails: Board
    private lateinit var boardDocumentID: String
    lateinit var assignedMemberDetailsList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().getBoardDetails(this, boardDocumentID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.MEMBERS_REQUEST_CODE
            || requestCode == Constants.CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreDB().getBoardDetails(this, boardDocumentID)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
                startActivityForResult(intent, Constants.MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMemberDetailsList)
        startActivityForResult(intent, Constants.CARD_DETAILS_REQUEST_CODE)
    }
    // each time user resume this activity, then app want to reload everything.
//    override fun onResume() {
//        showProgressDialog(resources.getString(R.string.please_wait))
//        FirestoreDB().getBoardDetails(this, boardDocumentID)
//        super.onResume()
//    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = boardDetails.name
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {
        boardDetails = board

        hideProgressDialog()
        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().getBoardDetails(this, boardDetails.documentedID)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreDB().getCurrentUserID())
        // Update the board
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreDB().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().addUpdateTaskList(this, boardDetails)
    }

    // When update card, we also update board's details
    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreDB().getCurrentUserID())

        val card = Card(cardName, FirestoreDB().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )

        boardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreDB().addUpdateTaskList(this, boardDetails)
    }

    fun boardMemberDetailsList(list: ArrayList<User>) {
        assignedMemberDetailsList = list

        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        boardDetails.taskList.add(addTaskList)

        binding?.rvTaskList?.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        binding?.rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, boardDetails.taskList)
        binding?.rvTaskList?.adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        boardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().addUpdateTaskList(this, boardDetails)
    }
}