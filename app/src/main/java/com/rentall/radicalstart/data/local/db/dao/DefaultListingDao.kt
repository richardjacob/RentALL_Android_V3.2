package com.rentall.radicalstart.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.rentall.radicalstart.data.model.db.DefaultListing

@Dao
interface DefaultListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultListing: DefaultListing)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertAll(filterSubList: List<DefaultListing>)
//
//    @Query("SELECT * FROM defaultListing")
//    fun loadAll(): List<DefaultListing>
//
//    @Query("SELECT * FROM defaultListing WHERE type = :listingType")
//    fun loadAllByListingType(listingType: String): List<DefaultListing>
}