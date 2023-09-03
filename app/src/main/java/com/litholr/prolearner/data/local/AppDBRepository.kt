package com.litholr.prolearner.data.local

import android.util.Log
import androidx.annotation.WorkerThread
import com.litholr.prolearner.data.local.dao.ContentInfoDao
import com.litholr.prolearner.data.local.dao.SavedBookInfoDao
import com.litholr.prolearner.data.local.entity.ContentInfo
import com.litholr.prolearner.data.local.entity.SavedBookInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppDBRepository @Inject constructor(
    private val savedBookInfoDao: SavedBookInfoDao,
    private val contentInfoDao: ContentInfoDao)
{
    // SavedBookInfoDao
    fun getSavedBookInfoAll(): Flow<List<SavedBookInfo>> = savedBookInfoDao.getSavedBookInfoAll()

    suspend fun insertSavedBookInfo(savedBookInfo: SavedBookInfo) {
        withContext(Dispatchers.IO) {
            savedBookInfoDao.insertSavedBookInfo(savedBookInfo)
        }
    }

    fun getSavedBookInfoByIsbn(isbn: String): Flow<SavedBookInfo> = savedBookInfoDao.getSavedBookInfoByIsbn(isbn)

    suspend fun updateStartDate(isbn: String, startDate: String) {
        withContext(Dispatchers.IO) {
            savedBookInfoDao.updateStartDate(isbn, startDate)
        }
    }

    suspend fun updateEndDate(isbn: String, endDate: String) {
        withContext(Dispatchers.IO) {
            savedBookInfoDao.updateEndDate(isbn, endDate)
        }
    }

    // ContentInfoDao
    suspend fun insertContent(contentInfo: ContentInfo) {
        withContext(Dispatchers.IO) {
            contentInfoDao.insertContent(contentInfo)
        }
    }

    suspend fun getContentList(isbn: String): List<ContentInfo> = withContext(Dispatchers.IO) {
        contentInfoDao.getContentList(isbn)
    }

    suspend fun updateChecked(isbn: String, contentSortNumber: Int, isChecked: Boolean) {
        withContext(Dispatchers.IO) {
            contentInfoDao.updateChecked(isbn, contentSortNumber, isChecked)
        }
    }

    fun getCountOfAllContentsByISBN(isbn: String): Flow<Int> = contentInfoDao.getCountOfAllContentsByISBN(isbn)
    fun getCountOfContentsCheckedByISBN(isbn: String): Flow<Int> = contentInfoDao.getCountOfContentsCheckedByISBN(isbn)

    suspend fun deleteBook(isbn: String) {
        withContext(Dispatchers.IO) {
            contentInfoDao.deleteContentsByISBN(isbn)
            savedBookInfoDao.deleteSavedBookByISBN(isbn)
        }
    }
}