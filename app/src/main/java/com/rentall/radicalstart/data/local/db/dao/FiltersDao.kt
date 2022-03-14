package com.rentall.radicalstart.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rentall.radicalstart.data.model.db.Filter


@Dao
interface FiltersDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(question: Filter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(questions: List<Filter>)

    @Query("SELECT * FROM filter")
    fun loadAll(): List<Filter>
}