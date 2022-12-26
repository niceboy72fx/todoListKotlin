package com.k70mobile.mytasktodo.utils

object Constants {
    // Firebase Constants
    // This  is used for the collection name for USERS.
    const val USERS: String = "users"
    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
    const val READ_STORAGE_PERMISSION_CODE = 1
    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2
    // Firebase database field names
    const val MY_PROFILE_REQUEST_CODE = 11
    const val CREATE_BOARD_REQUEST_CODE = 12
    const val MEMBERS_REQUEST_CODE = 13
    const val CARD_DETAILS_REQUEST_CODE = 14

    const val IMAGE = "image"
    const val NAME = "name"
    const val MOBILE = "mobile"
    const val ASSIGNED_TO = "assignedTo"
    const val TASK_LIST = "taskList"
    const val BOARDS = "boards"
    const val DOCUMENT_ID = "documentID"

    const val BOARD_DETAIL = "board_detail"
    const val ID = "id"
    const val EMAIL = "email"
    const val BOARD_MEMBERS_LIST = "board_members_list"
    const val SELECT = "Select"
    const val UNSELECT = "UnSelect"

    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"


}