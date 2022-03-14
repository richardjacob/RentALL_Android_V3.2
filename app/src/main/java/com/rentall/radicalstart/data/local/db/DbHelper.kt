package com.rentall.radicalstart.data.local.db

import androidx.paging.DataSource
import com.rentall.radicalstart.data.model.db.DefaultListing
import com.rentall.radicalstart.data.model.db.Message
import io.reactivex.Observable

interface DbHelper {

/*
//    FilterList
    fun loadFilterListsForFilterId(filterId: Int): Observable<List<FilterSubList>>

    fun insertFilterList(filterSubList: FilterSubList): Observable<Boolean>

    fun insertFilterSubList(filterSubList: List<FilterSubList>): Observable<Boolean>

    fun isFilterListEmpty(): Observable<Boolean>

    fun loadAllFiltersList(): Observable<List<FilterSubList>>

//    Filter
    fun insertFilter(filter: Filter): Observable<Boolean>

    fun insertFilterList(filterList: List<Filter>): Observable<Boolean>

    fun isFilterEmpty(): Observable<Boolean>

    fun loadAllFilters(): Observable<List<Filter>> */


//    DefaultListing

    fun insertDefaultListing(defaultListing: DefaultListing) : Observable<Boolean>
//
//    fun insertDefaultListingList(defaultListing: List<DefaultListing>) : Observable<Boolean>
//
//    fun loadAllDefaultListing(listingType: String): Observable<List<DefaultListing>>



//    Currency
//    fun insertCurrencyRate(currencyRates: CurrencyRates): Observable<Boolean>
//
//    fun insertCurrencyRatesList(currencyRatesList: List<CurrencyRates>): Observable<Boolean>

/*
    fun isCurrencyRatesListEmpty(): Observable<Boolean>

    fun loadAllCurrencyRates(): Observable<List<CurrencyRates>>

    fun loadCurrencyRatesByCode(currencyCode: String): Observable<CurrencyRates>
*/


    //Message
    fun loadAllMessage(): DataSource.Factory<Int, Message>

    fun deleteMessage(): Observable<Boolean>


}