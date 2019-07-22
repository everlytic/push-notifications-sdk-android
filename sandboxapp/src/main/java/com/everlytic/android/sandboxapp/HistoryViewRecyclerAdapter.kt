package com.everlytic.android.sandboxapp

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification
import java.text.SimpleDateFormat

class HistoryViewRecyclerAdapter(private val onClick: (EverlyticNotification) -> Unit) :
    ListAdapter<EverlyticNotification, HistoryViewRecyclerAdapter.ViewHolder>(HistoryViewRecyclerAdapter.DIFFER) {

    class ViewHolder(view: View, val onClick: (EverlyticNotification) -> Unit) : RecyclerView.ViewHolder(view) {

        private lateinit var itemBacking: EverlyticNotification

        init {
            itemView.setOnClickListener {
                onClick(itemBacking)
            }
        }

        fun setItem(item: EverlyticNotification) {
            Log.d("HistoryViewRecyclerAdap", "$item")
            itemBacking = item
            itemView.findViewById<TextView>(R.id.history_item_title).text = item.title
            itemView.findViewById<TextView>(R.id.history_item_subtitle).text = item.body
            itemView.findViewById<TextView>(R.id.history_item_received_at).text = dateFormat.format(item.received_at)
            itemView.findViewById<TextView>(R.id.history_item_read_at).text =
                item.read_at?.let { dateFormat.format(it) } ?: "Unread"
        }

        companion object {
            val dateFormat = SimpleDateFormat.getDateTimeInstance()!!
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_item_history_item,
                parent,
                false
            ),
            onClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(getItem(position))
    }

    companion object {
        val DIFFER = object : DiffUtil.ItemCallback<EverlyticNotification>() {
            override fun areItemsTheSame(oldItem: EverlyticNotification, newItem: EverlyticNotification): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(oldItem: EverlyticNotification, newItem: EverlyticNotification): Boolean {
                return oldItem == newItem
            }
        }
    }
}