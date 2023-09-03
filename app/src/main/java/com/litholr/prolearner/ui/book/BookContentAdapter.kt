package com.litholr.prolearner.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.litholr.prolearner.data.local.entity.ContentInfo
import com.litholr.prolearner.databinding.BookContentItemBinding

class BookContentAdapter(var array: List<String> = ArrayList(), val isFirst: Boolean = true, val isbnInAdapter: String, val contentInfoList: List<ContentInfo>? = null, val bookContentListener: BookContentListener? = null) : RecyclerView.Adapter<BookContentViewHolder>() {
    val map = mutableMapOf<Int, Pair<String, Boolean>>()
    var checkedList: List<Boolean>? = null

    init {
        if(contentInfoList != null) {
            checkedList = contentInfoList.map { it.isChecked }.toList()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookContentViewHolder {
        return BookContentViewHolder(BookContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BookContentViewHolder, position: Int) {
        holder.bindItem(array[position])
        map[position] = Pair(array[position], false)
        if(isFirst) {
            holder.binding().check.visibility = View.GONE
        } else {
            holder.binding().check.visibility = View.VISIBLE
            if(contentInfoList != null) {
                holder.binding().check.isSelected = checkedList!!.get(position)
            }
            holder.binding().check.setOnClickListener {
                it.isSelected = !it.isSelected
                bookContentListener?.updateContentChecked(isbnInAdapter, position, it.isSelected)
            }
        }
    }

    override fun getItemCount(): Int = array.size
}

class BookContentViewHolder(private val bookContentItemBinding: BookContentItemBinding): RecyclerView.ViewHolder(bookContentItemBinding.root) {
    fun bindItem(title: String) {
        bookContentItemBinding.contentTitle.text = title
    }
    fun binding(): BookContentItemBinding {
        return bookContentItemBinding
    }
}

interface BookContentListener {
    fun updateContentChecked(isbn: String, position: Int, isSelected: Boolean)
}