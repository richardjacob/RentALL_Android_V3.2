package com.rentall.radicalstart.ui.host.hostHome

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
import com.rentall.radicalstart.data.local.prefs.AppPreferencesHelper
import com.rentall.radicalstart.databinding.ActivityHomeBinding
import com.rentall.radicalstart.host.calendar.CalendarListingFragment1
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.home.HomeNavigator
import com.rentall.radicalstart.ui.home.HomePageAdapter
import com.rentall.radicalstart.ui.home.HomeViewModel
import com.rentall.radicalstart.ui.host.hostInbox.HostInboxFragment
import com.rentall.radicalstart.ui.host.hostListing.HostListingFragment
import com.rentall.radicalstart.ui.host.hostReservation.HostTripsFragment
import com.rentall.radicalstart.ui.host.step_two.PhotoUploadFragment
import com.rentall.radicalstart.ui.profile.ProfileFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.visible
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import java.util.*
import javax.inject.Inject

class HostHomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(), HomeNavigator, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityHomeBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_home
    override val viewModel: HomeViewModel
        get() =  ViewModelProviders.of(this, mViewModelFactory).get(HomeViewModel::class.java)

    private val fragmentList = ArrayList<Fragment>()
    lateinit var pageAdapter: HomePageAdapter
    private var currentFragment: Fragment? = null

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
       // initialAdapter()
    }

    private fun initView() {
        topView = mBinding.root
        setUpBottomNavigation()
        viewModel.dataManager.isHostOrGuest = true
        try {
            intent?.let {
                if(intent.hasExtra("from")) {
                    val from = intent.getStringExtra("from")
                    if (from == "verification") {
                        setProfile()
                    } else if (from == "trip") {
                        setTrips()
                    } else if (from == "calendar"){
                        setCalendar()
                    } else if(from == "fcm"){
                        setInbox()
                    }
                }
            }
        }catch (e : Exception){
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

    private fun setCalendar() {
        try {
            mBinding.vpHome.setCurrentItem(1, false)
            mBinding.bnExplore.setCurrentItem(1, false)
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

    override fun initialAdapter() {
        pageAdapter = HomePageAdapter(supportFragmentManager, createFragment())
        setUpBottomNavigationListener()
        with (mBinding.vpHome) {
            adapter = pageAdapter
            offscreenPageLimit = 4
        }
   //     setExplore()
    }

    private fun createFragment(): ArrayList<Fragment> {
        with (fragmentList) {
            clear()
            add(HostListingFragment())
            add(CalendarListingFragment1())
            add(HostTripsFragment())
            add(HostInboxFragment())
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
            setTitleTypeface(Utils.getTypeface(context))
        }
        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.host_bottom_navigation)
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
                        is HostInboxFragment -> (currentFragment as HostInboxFragment).onRefresh()
                        is ProfileFragment -> { (currentFragment as ProfileFragment).onRefresh() }
                        is HostListingFragment -> { (currentFragment as HostListingFragment).onRefresh() }
                        is HostTripsFragment -> { (currentFragment as HostTripsFragment).onRefresh() }
                        is CalendarListingFragment1 -> { (currentFragment as CalendarListingFragment1).onRefresh() }
                        is PhotoUploadFragment -> { (currentFragment as PhotoUploadFragment).onRetry() }
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
        mBinding.bnExplore.restoreBottomNavigation(true)
        mBinding.bnExplore.visible()
    }

    override fun onBackPressed() {
        if (::pageAdapter.isInitialized) {
            hideSnackbar()
            if (mBinding.vpHome.currentItem == 1) {
                if (pageAdapter.getCurrentFragment() is CalendarListingFragment1) {
                    val count = (pageAdapter.getCurrentFragment() as CalendarListingFragment1).childFragmentManager.backStackEntryCount
                    when {
                        count == 1 -> {
                            showBottomNavigation()
                            (pageAdapter.getCurrentFragment() as CalendarListingFragment1).childFragmentManager.popBackStack()
                        }
                        else -> {
                            finish()
                        }
                    }
                }
            } else if (mBinding.vpHome.currentItem == 0) {
                if (pageAdapter.getCurrentFragment() is HostListingFragment) {
                    val count = (pageAdapter.getCurrentFragment() as HostListingFragment).childFragmentManager.backStackEntryCount
                    when {
                        count == 0 -> {
                            showBottomNavigation()
                            (pageAdapter.getCurrentFragment() as HostListingFragment).onBackPressed()
                        }
                        else -> {
                            finish()
                        }
                    }
                }
            } else {
                setManageList()
            }
        }else {
                finish()
            }
        }


    private fun setManageList() {
        try {
            mBinding.vpHome.setCurrentItem(0, false)
            mBinding.bnExplore.setCurrentItem(0, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setProfile(){
        viewModel.clearHttpCache()
        mBinding.vpHome.setCurrentItem(1, false)
        mBinding.bnExplore.setCurrentItem(1, false)
    }

    override fun onResume() {
        super.onResume()
        if (::pageAdapter.isInitialized) {
            val currentFragment = pageAdapter.getCurrentFragment()
            if ((currentFragment) is HostListingFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is HostInboxFragment) {
                currentFragment.onRefresh()
            }
            if ((currentFragment) is HostTripsFragment) {
                currentFragment.onRefresh()
            }
           /* if ((currentFragment) is CalendarListingFragment) {
                currentFragment.onRefresh()
            }*/
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
        val currentFragment = pageAdapter.getCurrentFragment()
        (currentFragment as BaseFragment<*, *>).onRetry()
        hideSnackbar()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            key?.let { key ->
                if (key == AppPreferencesHelper.PREF_KEY_NOTIFICATION) {
                    viewModel.setNotification(sharedPreferences.getBoolean(key, false))
                }
            }
        }
    }
}

