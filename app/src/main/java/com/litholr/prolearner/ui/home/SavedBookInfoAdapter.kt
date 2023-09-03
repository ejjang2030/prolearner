package com.litholr.prolearner.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import api.naver.BookResult
import com.litholr.prolearner.R
import com.litholr.prolearner.data.local.entity.SavedBookInfo
import com.litholr.prolearner.databinding.CardviewSavedbookinfoBinding

class SavedBookAdapter(
    private val savedBookInfoClickListener: SavedBookInfoClickListener,
    private val lifecycleOwner: LifecycleOwner,
    private val countObservers: CountObservers)
    : RecyclerView.Adapter<SavedBookViewHolder>() {
    private var savedBookInfoList: List<SavedBookInfo> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : SavedBookViewHolder {
        return SavedBookViewHolder(
            CardviewSavedbookinfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false),
            savedBookInfoClickListener,
            lifecycleOwner,
            countObservers
            )
    }
    override fun onBindViewHolder(holder: SavedBookViewHolder, position: Int) {
        holder.bindItem(savedBookInfoList[position])
    }
    override fun getItemCount(): Int = savedBookInfoList.size

    fun updateSavedBookInfoList(savedBookInfoList: List<SavedBookInfo>) {
        this.savedBookInfoList = savedBookInfoList
    }


}

class SavedBookViewHolder(
    private val savedBookViewBinding: CardviewSavedbookinfoBinding,
    private val savedBookInfoClickListener: SavedBookInfoClickListener,
    private val lifecycleOwner: LifecycleOwner,
    private val countObservers: CountObservers
    )
    : RecyclerView.ViewHolder(savedBookViewBinding.root) {
    fun updatePercentage(percent: Int) {
        savedBookViewBinding.progress.apply {
            if(percent >= 100) {
                setProgressDrawableColor(resources.getColor(R.color.readCompletedColor))
            } else {
                setProgressDrawableColor(resources.getColor(R.color.readingColor))
            }
            setCornerRadius(10f)
            setProgressPercentage(percent.toDouble(), false)
        }
        savedBookViewBinding.percentage.text = "${percent}%"
        savedBookViewBinding.percentage.apply {
            if(percent >= 100) {
                setTextColor(resources.getColor(R.color.readCompletedColor))
            } else {
                setTextColor(resources.getColor(R.color.readingColor))
            }
        }
    }

    fun bindItem(item: SavedBookInfo) {
        savedBookViewBinding.savedBookInfo = item
        savedBookViewBinding.root.setOnClickListener {
            savedBookInfoClickListener.onItemClick(item.bookResult, false)
        }

        item.catalog?.let { // 프로그레스바를 위한 것
            val contentInfoObserver = ContentInfoObserver(item.isbn, countObservers)
            contentInfoObserver.setCountObserver(lifecycleOwner) { updatePercentage(it) }
        }
    }
}

class ContentInfoObserver(isbn: String, countObservers: CountObservers) {
    val countAllObserver = countObservers.getCountOfAllContentsByISBN(isbn)
    val countCheckedObserver = countObservers.getCountOfContentsCheckedByISBN(isbn)
    val percent: MediatorLiveData<Int> = MediatorLiveData()

    private fun getPercent(cnt: Int, total: Int): Int = ((cnt.toDouble() / total.toDouble()) * 100).toInt()
    init {
        percent.addSource(countAllObserver, Observer { percent.postValue(getPercent(countCheckedObserver.value ?: 0, it))})
        percent.addSource(countCheckedObserver, Observer { percent.postValue(getPercent(it, countAllObserver.value ?: 0))})
    }

    fun setCountObserver(lifecycleOwner: LifecycleOwner, fn: (Int) -> (Unit)) { percent.observe(lifecycleOwner) { fn(it) } }
}

interface SavedBookInfoClickListener {
    fun onItemClick(bookResult: BookResult, isFirst: Boolean = true)
}

interface CountObservers {
    fun getCountOfAllContentsByISBN(isbn: String): LiveData<Int>
    fun getCountOfContentsCheckedByISBN(isbn: String): LiveData<Int>
}