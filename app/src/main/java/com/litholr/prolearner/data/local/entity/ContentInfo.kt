package com.litholr.prolearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="ContentInfo")
data class ContentInfo(
    @ColumnInfo(name = "isbn") val isbn: String,
    @ColumnInfo(name = "content_sort_number") val contentSortNumber: Int,
    @ColumnInfo(name = "content_title") val contentTitle: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean,
    @ColumnInfo(name = "parent_content") val parentContent: Int
) {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "content_id") var contentId: Int = 0
}
