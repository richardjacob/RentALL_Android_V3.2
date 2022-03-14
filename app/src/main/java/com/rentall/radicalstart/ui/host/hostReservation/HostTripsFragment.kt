package com.rentall.radicalstart.ui.host.hostReservation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.FragmentTripsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class HostTripsFragment : BaseFragment<FragmentTripsBinding, HostTripsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips
    override val viewModel: HostTripsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(HostTripsViewModel::class.java)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mBinding = viewDataBinding!!
        val myAdapter = MyAdapter(childFragmentManager)
        with(mBinding.viewPager) {
            adapter = myAdapter
            offscreenPageLimit = 2
            mBinding.tabs.post { mBinding.tabs.setupWithViewPager(this) }
            addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {
                    Timber.tag("tripsPage1").d(p0.toString())
                }
                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    // Timber.tag("tripsPage12").d(p0.toString())
                }
                override fun onPageSelected(p0: Int) {
                    Timber.tag("tripsPage123").d(p0.toString())
                }
            })
        }
    }

    override fun clearDisposal() {
        viewModel.compositeDisposable.clear() }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")) {
            if (viewModel.upcomingNetworkState.value?.status == Status.FAILED) {
                viewModel.upcomingTripRetry()
            }
            if (viewModel.networkState.value?.status == Status.FAILED) {
                viewModel.tripRetry()
            }
        }else if(viewModel.retryCalled.contains("update")){
            val value = viewModel.retryCalled.split("-")
            viewModel.approveReservation(value[1].toInt(), "", value[6], value[2], value[3], value[4].toInt(), value[5].toInt(), value[6])
        }
    }

    fun onRefresh() {
        viewModel.upcomingTripRefresh()
        viewModel.tripRefresh()
    }

    inner class MyAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> HostTripsListFragment.newInstance("upcoming")
                1 -> HostTripsListFragment.newInstance("previous")
                else -> Fragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> if(isAdded && activity!=null){ return getString(R.string.upcoming) }
                1 -> if(isAdded && activity!=null){ return getString(R.string.previous) }
            }
            return null
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}
