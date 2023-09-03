package com.litholr.prolearner.ui.main

import android.content.Context
import androidx.lifecycle.*
import com.litholr.prolearner.data.local.entity.SavedBookInfo
import api.naver.BookResult
import api.naver.NaverSearching
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.litholr.prolearner.R
import com.litholr.prolearner.data.local.AppDBRepository
import com.litholr.prolearner.data.local.entity.ContentInfo
import com.litholr.prolearner.ui.base.BaseViewModel
import com.litholr.prolearner.utils.PLToast
import com.litholr.prolearner.utils.SecretId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDBRepository: AppDBRepository
) : BaseViewModel() {
    var naver = NaverSearching(SecretId.NAVER_CLIENT_ID, SecretId.NAVER_CLIENT_ID_SECRET)
    private val searchResultItemCount = 10

    private val _query = MutableLiveData("")
    val query: LiveData<String?>
        get() = _query

    private val _page = MutableLiveData(1)
    val page: LiveData<Int>
        get() = _page

    private val _books = MutableLiveData<ArrayList<BookResult>>(ArrayList())
    val books: MutableLiveData<ArrayList<BookResult>>
        get() = _books

    var results = MutableLiveData("")

    private val _selectedBook = MutableLiveData<BookResult>()
    val selectedBook: LiveData<BookResult>
        get() = _selectedBook

    val _bookResultState = MutableLiveData(BookResultState.BEFORE_SEARCH)
    val bookResultState: LiveData<BookResultState>
        get() = _bookResultState

    enum class BottomNav { HOME, SEARCH, PROFILE, BOOK, NONE }
    val _bottomNav = MutableLiveData(BottomNav.HOME)
    val bottomNav: LiveData<BottomNav>
        get() = _bottomNav

    var savedBookInfo: SavedBookInfo? = null

    var _isSavedBookPage = MutableLiveData<Boolean>()
    val isSavedBookPage: LiveData<Boolean>
        get() = _isSavedBookPage

    fun searchBook() {
        naver.searchBook(query.value!!, 10, page.value!!, "sim") { call, response, t ->
            if(response != null) {
                if(response.isSuccessful) {
                    val result = response.body()
                    if(result == null) {
                        this.showToastNullOfSearchResult()
                        return@searchBook
                    }
                    results.postValue(result.toString())
                    result.let { bookSearchResult ->
                        books.postValue(ArrayList(bookSearchResult.items))
                    }
                }
            }
        }
    }

    fun saveBook(context: Context) {
        if(savedBookInfo != null) {
            CoroutineScope(Dispatchers.IO).launch {
                savedBookInfo!!.catalog!!.getBookContentTableList()!!.let {
                    it.asSequence().forEachIndexed { index, s ->
                        val contentInfo = ContentInfo(
                            isbn = savedBookInfo!!.isbn,
                            contentSortNumber = index,
                            contentTitle = s,
                            isChecked = false,
                            parentContent = 0
                        )
                        insertContentInfo(contentInfo)
                    }
                    savedBookInfo!!.countOfAllContents = it.size
                    savedBookInfo!!.countOfContentsChecked = 0
                    insertSavedBookInfo(savedBookInfo!!)
                }
            }
            PLToast.makeToast(context, "책이 저장되었습니다.")
            this._bottomNav.postValue(BottomNav.HOME)
        }
    }

    fun deleteBook(context: Context) {
        if(savedBookInfo != null) {
            viewModelScope.launch { appDBRepository.deleteBook(savedBookInfo!!.isbn) }
            PLToast.makeToast(context, "책이 삭제되었습니다.")
            this._bottomNav.postValue(BottomNav.HOME)
        } else {
            PLToast.makeToast(context, "삭제할 책이 이미 존재하지 않습니다.")
        }
    }

    fun setOnNavigationItemSelectedListener(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    _bottomNav.value = BottomNav.HOME
                    true
                }
                R.id.search -> {
                    _bottomNav.value = BottomNav.SEARCH
                    true
                }
//                R.id.profile -> {
//                    _bottomNav.value = BottomNav.PROFILE
//                    true
//                }
                else -> false
            }
        }
    }

    fun onSearchButtonClick(query: String) {
        updateQuery(query)
        searchBook()
    }

    fun toBack() {
        if(_isSavedBookPage.value!!) {
            _bottomNav.value = BottomNav.HOME
        } else {
            _bottomNav.value = BottomNav.SEARCH
        }
    }

    enum class BookResultState { BEFORE_SEARCH, SEARCH_RESULT_NULL, BOOK_INFO_NULL, BOOK_CONTENTS_NULL }

    fun showToastNullOfSearchResult() {
        this._bookResultState.postValue(BookResultState.SEARCH_RESULT_NULL)
    }
    fun showToastNullOfBookInfo() {
        this._bookResultState.postValue(BookResultState.BOOK_INFO_NULL)
        this._bottomNav.postValue(BottomNav.SEARCH)
    }
    fun showToastNullOfBookContents() {
        this._bookResultState.postValue(BookResultState.BOOK_CONTENTS_NULL)
    }
    fun updateQuery(query: String?) {
        this._books.value!!.clear()
        this._query.postValue(query.toString())
        this._page.postValue(1)
    }
    fun updateBottomNavToBook(bookResult: BookResult, isFirst: Boolean = true) {
        this._isSavedBookPage.value = !isFirst
        this._selectedBook.postValue(bookResult)
        this._bottomNav.postValue(BottomNav.BOOK)
    }
    fun updateScrollPage() {
        this._page.postValue(this._page.value!!.plus(1))
        searchBook()
    }
    fun getSearchResultItemCount(): Int {
        return this.searchResultItemCount
    }
    
    // for room db
    // SavedBookInfo
    fun getSavedBookInfoAll(): LiveData<List<SavedBookInfo>> = appDBRepository.getSavedBookInfoAll().asLiveData()
    fun insertSavedBookInfo(savedBookInfo: SavedBookInfo) = viewModelScope.launch {
        appDBRepository.insertSavedBookInfo(savedBookInfo) }
    fun getSavedBookInfoByISBN(isbn: String): LiveData<SavedBookInfo> =
        appDBRepository.getSavedBookInfoByIsbn(isbn).asLiveData()
    fun updatestartDate(isbn: String, startDate: String) =
        viewModelScope.launch { appDBRepository.updateStartDate(isbn, startDate) }
    fun updateendDate(isbn: String, endDate: String) =
        viewModelScope.launch { appDBRepository.updateEndDate(isbn, endDate) }


    // ContentInfo
    fun insertContentInfo(contentInfo: ContentInfo) =
        viewModelScope.launch { appDBRepository.insertContent(contentInfo) }
    suspend fun getContentInfoListByISBN(isbn: String): List<ContentInfo> =
        appDBRepository.getContentList(isbn)
    fun updateContentChecked(isbn: String, sortNumber: Int, isChecked: Boolean) = viewModelScope.launch {
        appDBRepository.updateChecked(isbn, sortNumber, isChecked)
    }

    fun getCountOfAllContentsByISBN(isbn: String): LiveData<Int> =
        appDBRepository.getCountOfAllContentsByISBN(isbn).asLiveData()
    fun getCountOfContentsCheckedByISBN(isbn: String): LiveData<Int> =
        appDBRepository.getCountOfContentsCheckedByISBN(isbn).asLiveData()

}