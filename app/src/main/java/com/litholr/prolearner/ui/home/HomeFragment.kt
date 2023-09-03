package com.litholr.prolearner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
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
        val countObservers = object: CountObservers {
            override fun getCountOfAllContentsByISBN(isbn: String): LiveData<Int> {
                return mainViewModel.getCountOfAllContentsByISBN(isbn)
            }

            override fun getCountOfContentsCheckedByISBN(isbn: String): LiveData<Int> {
                return mainViewModel.getCountOfContentsCheckedByISBN(isbn)
            }
        }
        binding.savedBookList.apply {
            adapter = SavedBookAdapter(
                savedBookInfoClickListener,
                this@HomeFragment,
                countObservers
                )
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
}


