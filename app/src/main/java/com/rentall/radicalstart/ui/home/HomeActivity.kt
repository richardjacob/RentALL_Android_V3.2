package com.rentall.radicalstart.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.local.prefs.AppPreferencesHelper.Companion.PREF_KEY_NOTIFICATION
import com.rentall.radicalstart.databinding.ActivityHomeBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.ExploreFragment
import com.rentall.radicalstart.ui.inbox.InboxFragment
import com.rentall.radicalstart.ui.profile.ProfileFragment
import com.rentall.radicalstart.ui.saved.SavedDetailFragment
import com.rentall.radicalstart.ui.saved.SavedFragment
import com.rentall.radicalstart.ui.trips.TripsFragment
import com.rentall.radicalstart.util.Utils.Companion.getTypeface
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.visible
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(), HomeNavigator, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityHomeBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_home
    override val viewModel: HomeViewModel
        get() =  ViewModelProviders.of(this, mViewModelFactory).get(HomeViewModel::class.java)

    private val fragmentList = ArrayList<Fragment>()
    lateinit var pageAdapter: HomePageAdapter
    private var currentFragment: Fragment? = null
    private var isWishLoad = false
    private var eventCompositeDisposal = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        if(viewModel.loginStatus == 0){
            openSessionExpire()
        }
        viewModel.validateData()
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        topView = mBinding.root
        setUpBottomNavigation()
        validateIntent()
        viewModel.dataManager.isHostOrGuest = false
    }

    private fun validateIntent() {
        val from = intent?.getStringExtra("from")
        from?.let {
            if (it == "verification") {
                setProfile()
            } else if (it == "trip") {
                setTrips()
            } else if(it == "fcm"){
                setInbox()
            }
        }
    }

    override fun initialAdapter() {
        pageAdapter = HomePageAdapter(supportFragmentManager, createFragment())
        setUpBottomNavigationListener()
        with (mBinding.vpHome) {
            adapter = pageAdapter
            offscreenPageLimit = 4
        }
        setExplore()
    }

    private fun createFragment(): ArrayList<Fragment> {
        with (fragmentList) {
            clear()
            add(ExploreFragment())
            add(SavedFragment())
            add(TripsFragment())
            add(InboxFragment())
            add(ProfileFragment())
        }
        return fragmentList
    }

    private fun setUpBottomNavigation() {
        with(mBinding.bnExplore) {
            accentColor = ContextCompat.getColor(context!!, R.color.colorAccent)
            inactiveColor = ContextCompat.getColor(context!!, R.color.text_color)
            isTranslucentNavigationEnabled = true
            isColored = false
            isBehaviorTranslationEnabled = false
            titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
            setTitleTypeface(getTypeface(context))
        }
        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu)
        navigationAdapter.setupWithBottomNavigation(mBinding.bnExplore)
    }

    private fun setUpBottomNavigationListener() {
        mBinding.bnExplore.setOnTabSelectedListener(
                AHBottomNavigation.OnTabSelectedListener { position, wasSelected ->
                    if (currentFragment == null) {
                        currentFragment = pageAdapter.getCurrentFragment()
                    }
                    if (wasSelected) {
                        return@OnTabSelectedListener true
                    } else {
                        hideSnackbar()
                    }

                    if (currentFragment != null) {
                        (currentFragment as BaseFragment<*, *>).clearDisposal()
                    }
                    mBinding.vpHome.setCurrentItem(position, false)
                    if (currentFragment == null) {
                        return@OnTabSelectedListener true
                    }
                    currentFragment = pageAdapter.getCurrentFragment()
                    when (currentFragment) {
                        is InboxFragment -> (currentFragment as InboxFragment).onRefresh()
                        is ProfileFragment -> { (currentFragment as ProfileFragment).onRefresh() }
                        is ExploreFragment -> {
                            (currentFragment as ExploreFragment).onRefresh()
                            if (isWishLoad) {
                                 (currentFragment as ExploreFragment).onRefreshWish()
                                 isWishLoad = false
                            } else {
                                (currentFragment as ExploreFragment).onRefreshOnError()
                            }
                        }
                        is TripsFragment -> { (currentFragment as TripsFragment).onRefresh() }
                        is SavedFragment -> {
                            if (currentFragment is SavedFragment) {
                                if ((currentFragment as SavedFragment).childFragmentManager.backStackEntryCount == 1) {
                                    if ((currentFragment as SavedFragment).childFragmentManager.findFragmentByTag("SavedDetails") is SavedDetailFragment) {
                                        val savedDetailsFragment = (currentFragment as SavedFragment).
                                                childFragmentManager.findFragmentByTag("SavedDetails") as SavedDetailFragment
                                        savedDetailsFragment.onRefresh()
                                    }
                                } else {
                                    (currentFragment as SavedFragment).onRefresh()
                                }
                            }
                        }
                    }
                    true
                })
    }

    private fun subscribeToLiveData() {
        viewModel.notification.observe(this, Observer {
            it?.let { notify ->
                if (notify) {
                    mBinding.bnExplore.setNotificationBackgroundColor(ContextCompat.getColor(this, R.color.notification_red))
                    mBinding.bnExplore.setNotification("show", 3)
                } else {
                    mBinding.bnExplore.setNotificationBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    mBinding.bnExplore.setNotification( "hide", 3)
                }
            }
        })
    }

    fun hideBottomNavigation() {
        mBinding.bnExplore.hideBottomNavigation(true)
        mBinding.bnExplore.gone()
    }

    fun showBottomNavigation() {
        try {
            mBinding.bnExplore.restoreBottomNavigation(true)
            mBinding.bnExplore.visible()
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        if (::pageAdapter.isInitialized) {
            hideSnackbar()
            if (mBinding.vpHome.currentItem == 0) {
                if (pageAdapter.getCurrentFragment() is ExploreFragment ) {
                    try {
                        val count = (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.backStackEntryCount
                        when {
                            count >= 2 -> (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.popBackStack()
                            count == 1 -> {
                                showBottomNavigation()
                                (pageAdapter.getCurrentFragment() as ExploreFragment).childFragmentManager.popBackStack()
                            }
                            else -> {
                                (pageAdapter.getCurrentFragment() as ExploreFragment).onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            } else if (mBinding.vpHome.currentItem == 1) {
                if (pageAdapter.getCurrentFragment() is SavedFragment ) {
                    val count = (pageAdapter.getCurrentFragment() as SavedFragment).childFragmentManager.backStackEntryCount
                    when (count) {
                        1 -> { (pageAdapter.getCurrentFragment() as SavedFragment).refresh() }//childFragmentManager.popBackStack() }
                        else -> {
                            setExplore()
                            (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
                        }
                    }
                }
        } else {
                setExplore()
                (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
            }
        }
    }

    fun setExplore() {
        try {
            mBinding.vpHome.setCurrentItem(0, false)
            (pageAdapter.getCurrentFragment() as ExploreFragment).onRefresh()
            mBinding.bnExplore.setCurrentItem(0, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setProfile() {
        try {
            mBinding.vpHome.setCurrentItem(4, false)
            mBinding.bnExplore.setCurrentItem(4, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTrips() {
        try {
            mBinding.vpHome.setCurrentItem(2, false)
            mBinding.bnExplore.setCurrentItem(2, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setInbox() {
        try {
            mBinding.vpHome.setCurrentItem(3, false)
            mBinding.bnExplore.setCurrentItem(3, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::pageAdapter.isInitialized) {
            val currentFragment = pageAdapter.getCurrentFragment()
            if ((currentFragment) is TripsFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is InboxFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is ExploreFragment) {
                currentFragment.onRefresh()
            }
        }
        viewModel.pref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.disposeObservable()
        viewModel.pref.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        if (::pageAdapter.isInitialized.not()) {
            viewModel.defaultSetting()
        } else {
            val currentFragment = pageAdapter.getCurrentFragment()
            (currentFragment as BaseFragment<*, *>).onRetry()
            hideSnackbar()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            key?.let { key ->
                if (key == PREF_KEY_NOTIFICATION) {
                    viewModel.setNotification(sharedPreferences.getBoolean(key, false))
                }
            }
        }
    }

    fun refreshExploreItems(value: Int?, flag: Boolean, count: Int) {
        if (::pageAdapter.isInitialized) {
            val currentFragment = pageAdapter.getCurrentFragment()
            if ((currentFragment) is ExploreFragment) {
                currentFragment.onRefreshOnWishList(value, flag, count)
            }
        }
    }

    fun setWishList(flag: Boolean) {
        isWishLoad = flag
    }

    override fun onDestroy() {
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }
}

