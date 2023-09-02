package com.litholr.prolearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import api.naver.BookResult
import com.ejjang2030.bookcontentparser.api.naver.BookCatalog

@Entity(tableName="SavedBookInfo")
data class SavedBookInfo(
    @PrimaryKey @ColumnInfo(name = "isbn") val isbn: String, // isbn
    @ColumnInfo(name = "start_date") val startDate: String?,
    @ColumnInfo(name = "end_date") val endDate: String?,
    @ColumnInfo(name = "catalog") val catalog: BookCatalog?,
    @ColumnInfo(name = "bookResult") val bookResult: BookResult,
    @ColumnInfo(name = "count_of_all_contents") var countOfAllContents: Int = 0,
    @ColumnInfo(name = "count_of_contents_checked") var countOfContentsChecked: Int = 0
    // 목차리스트도 여기 포함하는 것도 고려해보기(나중에 성능상의 문제를 생각하면)
) {
    fun getAuthorListToJoinedString(): String? {
        return catalog?.authorList?.joinToString(", ")
    }
}