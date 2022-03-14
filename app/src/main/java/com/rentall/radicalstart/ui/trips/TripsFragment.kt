package com.rentall.radicalstart.ui.trips

import android.os.Bundle
import android.util.Log
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


class TripsFragment : BaseFragment<FragmentTripsBinding, TripsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips
    override val viewModel: TripsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(TripsViewModel::class.java)

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
        if(viewModel.upcomingNetworkState.value?.status == Status.FAILED) {
            viewModel.upcomingTripRetry()
        }
        if(viewModel.networkState.value?.status == Status.FAILED) {
            viewModel.tripRetry()
        }
    }

    fun onRefresh() {
        Log.d("Trip123", "123456")
        viewModel.upcomingTripRefresh()
        viewModel.tripRefresh()
    }


    inner class MyAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> TripsListFragment.newInstance("upcoming")
                1 -> TripsListFragment.newInstance("previous")
                else -> Fragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 ->  if(isAdded && activity!=null){ return resources.getString(R.string.upcoming_trips) }
                1 ->  if(isAdded && activity!=null){ return resources.getString(R.string.previous_trips) }
            }
            return null
        }
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}
