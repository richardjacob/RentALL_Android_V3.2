package com.rentall.radicalstart.data.local.db

import androidx.paging.DataSource
import com.rentall.radicalstart.data.model.db.DefaultListing
import com.rentall.radicalstart.data.model.db.Message
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppDbHelper @Inject constructor(
        private val mAppDatabase: AppDatabase
): DbHelper {


    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
        mAppDatabase.defaultListingDao().insert(defaultListing)
        return Observable.fromCallable { true }
    }

    override fun deleteMessage(): Observable<Boolean> {
        mAppDatabase.InboxMessage().deleteAllMessage()

        return Observable.fromCallable {
            true
        }
    }

    override fun loadAllMessage(): DataSource.Factory<Int, Message> {
        return mAppDatabase.InboxMessage().loadAll()
    }

//    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
//        return Observable.fromCallable {
//            mAppDatabase.defaultListingDao().insert(defaultListing)
//            true
//        }
//    }
//
//    override fun insertDefaultListingList(defaultListing: List<DefaultListing>): Observable<Boolean> {
//        return Observable.fromCallable {
//            mAppDatabase.defaultListingDao().insertAll(defaultListing)
//            true
//        }
//    }
//
//    override fun loadAllDefaultListing(listingType: String): Observable<List<DefaultListing>> {
//        return Observable.fromCallable { mAppDatabase.defaultListingDao().loadAllByListingType(listingType) }
//    }

    /*  override fun loadFilterListsForFilterId(filterId: Int): Observable<List<FilterSubList>> {
          return Observable.fromCallable {
              mAppDatabase.filterListDao().loadAllByFilterId(filterId)
          }
      }

      override fun isFilterListEmpty(): Observable<Boolean> {
          return Observable.fromCallable { mAppDatabase.filterListDao().loadAll().isEmpty() }
      }

      override fun isFilterEmpty(): Observable<Boolean> {
          return Observable.fromCallable { mAppDatabase.filtersDao().loadAll().isEmpty() }
      }

      override fun insertFilterList(filterSubList: FilterSubList): Observable<Boolean> {
          return Observable.fromCallable {
              try {
                  mAppDatabase.filterListDao().insert(filterSubList)
              } catch (e: Exception) {
                  Log.d("qwe", filterSubList.toString())
                  e.printStackTrace()
              }
              true
          }
      }

      override fun insertFilterSubList(filterSubList: List<FilterSubList>): Observable<Boolean> {
          return Observable.fromCallable {
              mAppDatabase.filterListDao().insertAll(filterSubList)
              true
          }
      }

      override fun insertFilter(filter: Filter): Observable<Boolean> {
          //Log.d("dd", filter.toString())
          return Observable.fromCallable(object : Callable<Boolean> {
               override fun call(): Boolean {
  //                 Log.d("pop11", filter.toString())

                   mAppDatabase.filtersDao().insert(filter)
  //                 Log.d("pop", filter.toString())
                   return true
               }
           })
      }

      override fun insertFilterList(filterList: List<Filter>): Observable<Boolean> {
          return Observable.fromCallable {
              mAppDatabase.filtersDao().insertAll(filterList)
              true
          }
      }

      override fun loadAllFilters(): Observable<List<Filter>> {
          return Observable.fromCallable { mAppDatabase.filtersDao().loadAll() }
      }

      override fun loadAllFiltersList(): Observable<List<FilterSubList>> {
          return Observable.fromCallable { mAppDatabase.filterListDao().loadAll() }
      }*/

/*    override fun insertCurrencyRate(currencyRates: CurrencyRates): Observable<Boolean> {
        return Observable.fromCallable {
            mAppDatabase.currencyRatesDao().insert(currencyRates)
            true
        }
    }

    override fun insertCurrencyRatesList(currencyRatesList: List<CurrencyRates>): Observable<Boolean> {
        return Observable.fromCallable {
            mAppDatabase.currencyRatesDao().insertAll(currencyRatesList)
            true
        }
    }

    override fun isCurrencyRatesListEmpty(): Observable<Boolean> {
        return Observable.fromCallable { mAppDatabase.currencyRatesDao().loadAll().isEmpty() }
    }

    override fun loadAllCurrencyRates(): Observable<List<CurrencyRates>> {
        return Observable.fromCallable { mAppDatabase.currencyRatesDao().loadAll() }
    }

    override fun loadCurrencyRatesByCode(currencyCode: String): Observable<CurrencyRates> {
        return Observable.fromCallable { mAppDatabase.currencyRatesDao().loadCurrencyRatesByCode(currencyCode) }
    }*/

}