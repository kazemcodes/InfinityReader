package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants


@Entity(tableName = Constants.HISTORY_TABLE)
data class History(
    @PrimaryKey(autoGenerate = false)
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
)
