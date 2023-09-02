package com.litholr.prolearner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.naver.BookResult
import com.litholr.prolearner.R
import com.litholr.prolearner.data.local.entity.SavedBookInfo
import com.litholr.prolearner.databinding.CardviewSavedbookinfoBinding
import com.litholr.prolearner.databinding.FragmentHomeBinding
import com.litholr.prolearner.ui.base.BaseFragment
import com.litholr.prolearner.ui.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_home

    val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateBegin(savedInstanceState: Bundle?) {
        init()
        setObservers()
    }
    // 더보기 검색 할 수 있게 (새로 추가 한 것은 위로 올릴 수 있도록- 저장한 시점도 저장 할 수 있게) 정렬기능
    // 읽은 percentage 정도 계산해서 sorting

    private fun init() {
        val savedBookInfoClickListener = object: SavedBookInfoClickListener {
            override fun onItemClick(bookResult: BookResult, isFirst: Boolean) {
                mainViewModel.updateBottomNavToBook(bookResult, isFirst)
            }
        }
        binding.savedBookList.apply {
            adapter = SavedBookAdapter(savedBookInfoClickListener)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun setObservers() {
        mainViewModel.getSavedBookInfoAll().observe(this@HomeFragment) { savedBookInfoList ->
            val adapter = binding.savedBookList.adapter as SavedBookAdapter
            adapter.updateSavedBookInfoList(savedBookInfoList)
            adapter.notifyDataSetChanged()
        }
    }

    inner class SavedBookAdapter(private val savedBookInfoClickListener: SavedBookInfoClickListener)
        : RecyclerView.Adapter<SavedBookAdapter.SavedBookViewHolder>() {
        private var savedBookInfoList: List<SavedBookInfo> = emptyList()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : SavedBookViewHolder {
            return SavedBookViewHolder(
                CardviewSavedbookinfoBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false))
        }
        override fun onBindViewHolder(holder: SavedBookViewHolder, position: Int) {
            holder.bindItem(savedBookInfoList[position])
        }
        override fun getItemCount(): Int = savedBookInfoList.size

        fun updateSavedBookInfoList(savedBookInfoList: List<SavedBookInfo>) {
            this.savedBookInfoList = savedBookInfoList
        }

        inner class SavedBookViewHolder(private val savedBookViewBinding: CardviewSavedbookinfoBinding)
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
                    val contentInfoObserver = ContentInfoObserver(item.isbn)
                    contentInfoObserver.percent.observe(this@HomeFragment) {
                        updatePercentage(it)
                    }
//                    CoroutineScope(Dispatchers.Main).launch {
//                        Log.d("SavedBookAdapter", "${item.countOfContentsChecked!!}, ${item.countOfAllContents!!}")
//                        val percent =
//                            ((item.countOfContentsChecked!!.toDouble() / item.countOfAllContents!!.toDouble()) * 100).toInt()
//                    }
                }
            }
        }

        inner class ContentInfoObserver(isbn: String) {
            val countAllObserver = mainViewModel.getCountOfAllContentsByISBN(isbn)
            val countCheckedObserver = mainViewModel.getCountOfContentsCheckedByISBN(isbn)
            val percent: MediatorLiveData<Int> = MediatorLiveData()

            private fun getPercent(cnt: Int, total: Int): Int = ((cnt.toDouble() / total.toDouble()) * 100).toInt()
            init {
                percent.addSource(countAllObserver, Observer { percent.postValue(getPercent(countCheckedObserver.value ?: 0, it))})
                percent.addSource(countCheckedObserver, Observer { percent.postValue(getPercent(it, countAllObserver.value ?: 0))})
            }
        }
    }

    interface SavedBookInfoClickListener {
        fun onItemClick(bookResult: BookResult, isFirst: Boolean = true)
    }
}


