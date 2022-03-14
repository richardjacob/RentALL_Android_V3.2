package com.rentall.radicalstart.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rentall.radicalstart.data.model.db.DefaultListing
import com.rentall.radicalstart.data.model.db.Filter
import com.rentall.radicalstart.data.model.db.WishList
import com.rentall.radicalstart.data.model.db.WishListGroup

@Dao
interface WishListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wishList: WishList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(wishlists: List<WishList>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(wishListGroup: WishListGroup)

    @Query("SELECT * FROM wishList")
    fun loadAll(): List<WishList>

    @Query("SELECT * FROM wishListGroup")
    fun loadAllGroups(): List<WishListGroup>

}