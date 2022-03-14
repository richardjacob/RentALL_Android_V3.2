package com.rentall.radicalstart.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rentall.radicalstart.data.local.db.dao.DefaultListingDao
import com.rentall.radicalstart.data.local.db.dao.InboxMsgDao
import com.rentall.radicalstart.data.model.db.DefaultListing
import com.rentall.radicalstart.data.model.db.Message


@Database(entities = [(Message::class), (DefaultListing::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {

   /* abstract fun filtersDao(): FiltersDao

    abstract fun filterListDao(): FilterListDao*/

    abstract fun defaultListingDao(): DefaultListingDao

    abstract fun InboxMessage(): InboxMsgDao

    //abstract fun currencyRatesDao(): CurrencyRatesDao

}
