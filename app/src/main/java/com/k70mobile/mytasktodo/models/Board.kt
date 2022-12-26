package com.k70mobile.mytasktodo.models

import android.os.Parcel
import android.os.Parcelable

data class Board(
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    // Need to assign multiple user
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentedID: String = "",
    var taskList: ArrayList<Task> = ArrayList()
) : Parcelable {
    // A variable that could potentially be null and I cannot pass a null
    // to the constructor because it always expects something.
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(Task.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(name)
        writeString(image)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(documentedID)
        writeTypedList(taskList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}