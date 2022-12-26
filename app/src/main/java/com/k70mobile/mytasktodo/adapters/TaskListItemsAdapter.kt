package com.k70mobile.mytasktodo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.icu.text.Transliterator.Position
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.k70mobile.mytasktodo.R
import com.k70mobile.mytasktodo.activities.TaskListActivity
import com.k70mobile.mytasktodo.models.Card
import com.k70mobile.mytasktodo.models.Task
import java.util.Collections

open class TaskListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionDraggedFrom = -1
    private var positionDraggedTo = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(
                R.layout.item_task,
                parent,
                false
            )
        // View holder has 70% width of Screen, and height is wrap content
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(15, 0, 40, 0)
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            if (position == list.size - 1) {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            } else {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {

                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
            }

            // Create list
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                val listName =
                    holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()
                // check if the context that we currently are in is our task list activity context.
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Hãy nhập tên danh sách", Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {

                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title)
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)
                .setOnClickListener {
                    holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.GONE
                }

            // Change list name
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
                .setOnClickListener {
                    val listName =
                        holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()

                    if (listName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.updateTaskList(position, listName, model)
                        }
                    } else {
                        Toast.makeText(context, "Hãy nhập tên danh sách", Toast.LENGTH_SHORT).show()
                    }
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
                alertDeleteList(position, model.title)
            }

            holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE

                holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name)
                    .setOnClickListener {
                        holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility =
                            View.VISIBLE
                        holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility =
                            View.GONE
                    }
                holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
                    val cardName =
                        holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()
                    // check if the context that we currently are in is our task list activity context.
                    if (cardName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.addCardToTaskList(position, cardName)
                        }
                    } else {
                        Toast.makeText(context, "Hãy nhập tên thẻ", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager =
                LinearLayoutManager(context)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

            val adapter = CardListItemAdapter(context, model.cards)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })

            val dividerItemDecoration = DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)
                .addItemDecoration(dividerItemDecoration)

            //  Creates an ItemTouchHelper that will work with the given Callback.
            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPos = dragged.adapterPosition
                        val targetPos = target.adapterPosition

                        if (positionDraggedFrom == -1) {
                            positionDraggedFrom = draggedPos
                        }
                        positionDraggedTo = targetPos
                        Collections.swap(list[position].cards, draggedPos, targetPos)

                        adapter.notifyItemMoved(draggedPos, targetPos)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)

                        if (positionDraggedFrom != -1 && positionDraggedTo != -1
                            && positionDraggedFrom != positionDraggedTo
                        ) {
                            (context as TaskListActivity).updateCardsInTaskList(
                                position, list[position].cards
                            )
                        }
                        positionDraggedFrom = -1
                        positionDraggedTo = -1
                    }
                })
            helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))
        }
    }

    private fun alertDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Cảnh báo")
        builder.setMessage("Bạn có muốn xóa $title không?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Có") { dialogInterface, _ ->
            dialogInterface.dismiss() // Close dialog

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("Không") { dialogInterface, _ ->
            dialogInterface.dismiss() // Close dialog
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

//    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
//    // Resources.getSystem().displayMetrics.density: ge   t the density of the screen and convert that into an integer
//
//    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}