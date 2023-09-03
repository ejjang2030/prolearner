package com.litholr.prolearner.ui.book

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ejjang2030.bookcontentparser.api.naver.BookCatalog
import com.litholr.prolearner.R
import com.litholr.prolearner.data.local.entity.ContentInfo
import com.litholr.prolearner.data.local.entity.SavedBookInfo
import com.litholr.prolearner.databinding.FragmentBookBinding
import com.litholr.prolearner.ui.base.BaseFragment
import com.litholr.prolearner.ui.main.MainViewModel
import com.litholr.prolearner.utils.ChipInfo
import com.litholr.prolearner.utils.CustomDatePicker
import kotlinx.coroutines.*
import java.util.*

class BookFragment: BaseFragment<FragmentBookBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_book

    val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateBegin(savedInstanceState: Bundle?) {
        mainViewModel.selectedBook.observe(this) { bookResult ->
            mainViewModel.getSavedBookInfoByISBN(bookResult.isbn).observe(this) { savedBookInfo ->
                if(savedBookInfo != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val contentInfoList = mainViewModel.getContentInfoListByISBN(bookResult.isbn)
                        savedBookInfo.catalog?.let {
                            initUI(it, false, contentInfoList)
                        }
                    }
                } else {
                    mainViewModel.naver.getBookCatalog(bookResult) { bresult, catalog, call, res, t ->
                        if (res?.isSuccessful == true) {
                            if (catalog == null) {
                                mainViewModel.showToastNullOfBookInfo()
                                return@getBookCatalog
                            } else {
                                mainViewModel.savedBookInfo = SavedBookInfo(
                                    isbn = bookResult.isbn,
                                    startDate = null,
                                    endDate = null,
                                    catalog,
                                    bookResult,
                                    0,
                                    0
                                )
                                initUI(catalog)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initUI(catalog: BookCatalog, isFirst: Boolean = true, contentInfoList: List<ContentInfo>? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val contentLists = catalog.getBookContentTableList()
            if(contentLists == null) {
                mainViewModel.showToastNullOfBookContents()
                return@launch
            }
            binding.catalog = catalog
            binding.chipInfo = ChipInfo(
                Pair("저자", catalog.authorList.joinToString(", ")),
                Pair("출판사", catalog.publisher),
                Pair("정보제공", catalog.contentsSourceMallName ?: "미확인")
            )
            binding.expandTextView.setOnExpandStateChangeListener { textView, isExpanded -> }
            val bookAdapter: BookContentAdapter
            if(isFirst && contentInfoList == null) {
                binding.datePicker.visibility = View.GONE
                bookAdapter = BookContentAdapter(contentLists, isbnInAdapter = catalog.isbn)
            } else {
                binding.datePicker.visibility = View.GONE
                val bookContentListener = object: BookContentListener {
                    override fun updateContentChecked(
                        isbn: String,
                        position: Int,
                        isSelected: Boolean
                    ) {
                        mainViewModel.updateContentChecked(isbn, position, isSelected)
                    }
                }
                bookAdapter = BookContentAdapter(contentLists, false, catalog.isbn, contentInfoList, bookContentListener)
                initDatePickers()
            }
            binding.bookContents.apply {
                adapter = bookAdapter
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
        }
    }

    private fun initDatePickers() {
        binding.startDate.setOnClickListener {
            val datePicker = CustomDatePicker(requireActivity(), object: CustomDatePicker.ICustomDateListener {
                override fun onCancel() {
                }

                override fun onSet(
                    dialog: Dialog,
                    calendarSelected: Calendar,
                    dateSelected: Date,
                    year: Int,
                    monthFullName: String,
                    monthShortName: String,
                    monthNumber: Int,
                    day: Int,
                    weekDayFullName: String,
                    weekDayShortName: String
                ) {
                    binding.startDatePick.text = "$year.${monthNumber + 1}.$day"
                }
            }).apply {
                setDate(Calendar.getInstance())
                showDialog()
            }
        }
        binding.endDate.setOnClickListener {
            val datePicker = CustomDatePicker(requireActivity(), object: CustomDatePicker.ICustomDateListener {
                override fun onCancel() {
                }

                override fun onSet(
                    dialog: Dialog,
                    calendarSelected: Calendar,
                    dateSelected: Date,
                    year: Int,
                    monthFullName: String,
                    monthShortName: String,
                    monthNumber: Int,
                    day: Int,
                    weekDayFullName: String,
                    weekDayShortName: String
                ) {
                    binding.endDatePick.text = "$year.${monthNumber + 1}.$day"
                }
            }).apply {
                setDate(Calendar.getInstance())
                showDialog()
            }
        }
    }
}