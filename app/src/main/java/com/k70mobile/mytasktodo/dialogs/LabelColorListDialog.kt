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
import com.k70mobile.mytasktodo.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private val list: ArrayList<String>,
    private val title: String = "",
    private val selectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        val rvList = view.findViewById<RecyclerView>(R.id.rvList)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = title
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, list, selectedColor)
        rvList.adapter = adapter

        // creating a new object of 'LabelColorListItemsAdapter' 'onItemClickListener' which is this
        // 'onItemClickListener' interface.
        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }

        }
    }

    protected abstract fun onItemSelected(color: String)
}