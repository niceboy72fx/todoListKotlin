package com.k70mobile.mytasktodo.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.adapters.MemberListItemAdapter
import com.k70mobile.mytasktodo.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private var title: String = ""
) : Dialog(context) {

    private var adapter: MemberListItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val rvList = view.findViewById<RecyclerView>(R.id.rvList)

        tvTitle.text = title

        if (list.size > 0) {
            rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemAdapter(context, list)
            rvList.adapter = adapter

            adapter!!.setOnClickListener(
                object : MemberListItemAdapter.OnClickListener {
                    override fun onClick(position: Int, user: User, action: String) {
                        dismiss()
                        onItemSelected(user, action)
                    }
                }
            )
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}