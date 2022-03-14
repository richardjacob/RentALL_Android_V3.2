package com.rentall.radicalstart.dagger.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.google.firebase.iid.FirebaseInstanceId
import com.rentall.radicalstart.BuildConfig
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.dagger.ViewModelModule
import com.rentall.radicalstart.data.AppDataManager
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.local.db.AppDatabase
import com.rentall.radicalstart.data.local.db.AppDbHelper
import com.rentall.radicalstart.data.local.db.DbHelper
import com.rentall.radicalstart.data.local.prefs.AppPreferencesHelper
import com.rentall.radicalstart.data.local.prefs.PreferencesHelper
import com.rentall.radicalstart.data.remote.ApiHelper
import com.rentall.radicalstart.data.remote.AppApiHelper
import com.rentall.radicalstart.util.CustomInterceptor
import com.rentall.radicalstart.util.resource.BaseResourceProvider
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.AppScheduler
import com.rentall.radicalstart.util.rx.Scheduler
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideApiHelper(appApiHelper: AppApiHelper): ApiHelper {
        return appApiHelper
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideDataManager(appDataManager: AppDataManager): DataManager {
        return appDataManager
    }

    @Provides
    fun providePreferenceName(): String {
        return Constants.PREF_NAME
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(appPreferencesHelper: AppPreferencesHelper): PreferencesHelper {
        return appPreferencesHelper
    }

    @Provides
    @Singleton
    fun provideCacheStore(application: Application): DiskLruHttpCacheStore {
        val file = File(application.cacheDir.toURI())
        val size = 1024 * 1024
        return DiskLruHttpCacheStore(file, size.toLong())
    }

    @Provides
    @Singleton
    @Named("Interceptor")
    fun provideApolloClient(okHttpClient: OkHttpClient, cacheStore: DiskLruHttpCacheStore): ApolloClient {
        return ApolloClient.builder()
                .serverUrl(Constants.URL)
                .okHttpClient(okHttpClient)
                .httpCache(ApolloHttpCache(cacheStore))
                .build()
    }

    @Provides
    @Singleton
    @Named("NoInterceptor")
    fun provideApolloClientNoInterceptor(httpLoggingInterceptor: HttpLoggingInterceptor,
                                         cacheStore: DiskLruHttpCacheStore
    ): ApolloClient {
        return ApolloClient.builder()
                .serverUrl(Constants.URL)
                .okHttpClient(OkHttpClient()
                        .newBuilder()
                        .addInterceptor(httpLoggingInterceptor)
                        .build())
                .httpCache(ApolloHttpCache(cacheStore))
                .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
            customInterceptor: CustomInterceptor,
            httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient()
                .newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(customInterceptor)
                .build()
    }


    @Provides
    @Singleton
    fun provideHeaderInterceptor(preferencesHelper: PreferencesHelper): CustomInterceptor {
        return CustomInterceptor(preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }
        //interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @Singleton
    fun provideFirebaseInstance(): FirebaseInstanceId {
        return FirebaseInstanceId.getInstance()
    }

    @Provides
    fun provideScheduler(): Scheduler {
        return AppScheduler()
    }

    @Provides
    @Singleton
    fun provideResource(resourceProvider: ResourceProvider): BaseResourceProvider {
        return resourceProvider
    }

     @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    @Provides
    @Singleton
    fun provideDbHelper(appDbHelper: AppDbHelper): DbHelper {
        return appDbHelper
    }
}