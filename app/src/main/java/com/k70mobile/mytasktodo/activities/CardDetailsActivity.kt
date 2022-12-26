package com.k70mobile.mytasktodo.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.adapters.CardMemberListItemsAdapter
import com.k70mobile.mytasktodo.databinding.ActivityCardDetailsBinding
import com.k70mobile.mytasktodo.dialogs.LabelColorListDialog
import com.k70mobile.mytasktodo.dialogs.MembersListDialog
import com.k70mobile.mytasktodo.firebase.FirestoreDB
import com.k70mobile.mytasktodo.models.*
import com.k70mobile.mytasktodo.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private var binding: ActivityCardDetailsBinding? = null
    private lateinit var boardDetails: Board
    private var taskListPos: Int = 0
    private var cardListPos: Int = 0

    private var selectedColor: String = ""
    private lateinit var membersDetailsList: ArrayList<User>

    private var selectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        setupActionBar()

        binding?.etNameCardDetails?.setText(
            boardDetails.taskList[taskListPos]
                .cards[cardListPos].name
        )
        // Focus on the end of the text of 'etNameCardDetails'
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        selectedColor = boardDetails.taskList[taskListPos].cards[cardListPos].labelColor
        if (selectedColor.isNotEmpty()) {
            setColor()
        }

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this, "Tên thẻ không được bỏ trống", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorsListDialog()
        }

        binding?.tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        selectedDueDateMilliSeconds =
            boardDetails.taskList[taskListPos].cards[cardListPos].dueDate

        if (selectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            val selectedDate = simpleDateFormat.format(Date(selectedDueDateMilliSeconds))
            binding?.tvSelectDueDate?.text = selectedDate
        }

        binding?.tvSelectDueDate?.setOnClickListener {
            showDatePicker()
        }
    }

    fun addUpdateTaskListSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionBar?.title = boardDetails.taskList[taskListPos].cards[cardListPos].name
        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL))
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION))
            cardListPos = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, 0)

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION))
            taskListPos = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, 0)

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST))
            membersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
    }

    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()

        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(selectedColor))
    }

    private fun labelColorsListDialog() {

        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(
            this@CardDetailsActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            selectedColor
        ) {
            override fun onItemSelected(color: String) {
                selectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(boardDetails.taskList[taskListPos].cards[cardListPos].name)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCardDetails() {
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            boardDetails.taskList[taskListPos].cards[cardListPos].createdBy,
            boardDetails.taskList[taskListPos].cards[cardListPos].assignedTo,
            selectedColor,
            selectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        boardDetails.taskList[taskListPos].cards[cardListPos] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = boardDetails.taskList[taskListPos].cards

        cardsList.removeAt(cardListPos)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListPos].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreDB().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle(resources.getString(R.string.alert))
            setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                deleteCard()
            }
            setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            create().show()
        }
    }

    private fun membersListDialog() {
        val cardAssignedMembersList = boardDetails
            .taskList[taskListPos]
            .cards[cardListPos].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in membersDetailsList.indices) {
                for (j in cardAssignedMembersList) {
                    if (membersDetailsList[i].id == j) {
                        membersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for (i in membersDetailsList.indices) {
                membersDetailsList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this,
            membersDetailsList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    // If 'user id' is not contains, add user into list, else removing it
                    if (
                        !boardDetails
                            .taskList[taskListPos]
                            .cards[cardListPos]
                            .assignedTo
                            .contains(user.id)
                    ) {
                        boardDetails
                            .taskList[taskListPos]
                            .cards[cardListPos]
                            .assignedTo.add(user.id)
                    }
                } else {
                    boardDetails
                        .taskList[taskListPos]
                        .cards[cardListPos]
                        .assignedTo.remove(user.id)

                    for (i in membersDetailsList.indices) {
                        if (membersDetailsList[i].id == user.id) {
                            membersDetailsList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            boardDetails.taskList[taskListPos].cards[cardListPos].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in membersDetailsList.indices) {
            for (j in cardAssignedMembersList) {
                if (membersDetailsList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        membersDetailsList[i].id,
                        membersDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE

            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(
                this@CardDetailsActivity,
                6
            )

            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)

            binding?.rvSelectedMembersList?.adapter = adapter

            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                binding?.tvSelectDueDate?.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */

                /** Here we have get the time in milliSeconds from Date object
                 */
                selectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }
}