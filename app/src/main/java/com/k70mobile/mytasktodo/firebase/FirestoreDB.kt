package com.k70mobile.mytasktodo.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.k70mobile.mytasktodo.activities.*
import com.k70mobile.mytasktodo.models.Board
import com.k70mobile.mytasktodo.models.User
import com.k70mobile.mytasktodo.utils.Constants

class FirestoreDB {

    private val firestore = FirebaseFirestore.getInstance()

    fun getBoardsList(activity: MainActivity) {
        firestore.collection(Constants.BOARDS)
            // Checking the Board's collection where the value for assigned to is the current user ID.
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())

                val boardList: ArrayList<Board> = ArrayList()
                for (item in document.documents) {
                    // whatever object you have there
                    val board = item.toObject(Board::class.java)!! // toObject function is non null
                    board.documentedID = item.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board, $it")
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        firestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Cập nhật thông tin thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        firestore
            .collection(Constants.BOARDS)
            .document(board.documentedID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Task list update successfully")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccessfully()
                }
            }
            .addOnFailureListener {
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board, $it")
            }
    }
    // A function to make an entry of the registered user in the firestore database.
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        // Start a collection in firestore
        firestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess(userInfo)
                activity.finish()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        firestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                // Make user object out of whatever is given in the document
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        firestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(activity, "Bảng đã được tạo thành công!", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board, ${exception.printStackTrace()}"
                )
                Toast.makeText(activity, "Không thể tạo bảng, xin hãy thử lại", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // this Function gives the current user UID
    fun getCurrentUserID(): String {
        // Check if the current user is not null,
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        firestore.collection(Constants.BOARDS)
            // Checking the Board's collection where the value for assigned to is the current user ID.
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentedID = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board, $it")
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        // Checking the database and get the users, then get the list of all the users that fit
        // this criteria (whereIn(Constants.ID, assignedTo), and put all of them into 'usersList'
        // with the for loop
        firestore
            .collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                Log.i("assigned member list", it.documents.toString())

                val userList: ArrayList<User> = ArrayList()

                for (item in it.documents) {
                    val user = item.toObject(User::class.java)
                    userList.add(user!!)
                }
                if (activity is MembersActivity) {
                    activity.setupMembersList(userList)
                } else if (activity is TaskListActivity) {
                    activity.boardMemberDetailsList(userList)
                }
            }
            .addOnFailureListener {
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board, $it")
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        firestore
            .collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    // The email address must be unique, no one will have to or one email address
                    // cannot has two accounts
                    val user = it.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("Email $email không tồn tại")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting a board, $it")
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignToHashMap = HashMap<String, Any>()
        assignToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        firestore
            .collection(Constants.BOARDS)
            .document(board.documentedID)
            .update(assignToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccessfully(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while add a member, $it")
            }
    }
}