package com.rentall.radicalstart.ui.cancellation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.CancellationDataQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentCancellationBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.vo.ListingInitData
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class CancellationActivity : BaseActivity<FragmentCancellationBinding, CancellationViewModel>(), CancellationNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCancellationBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_cancellation
    override val viewModel: CancellationViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(CancellationViewModel::class.java)
    private var reservationId = 0
    private var userType = ""
    var hostProfileID = 0
   // var listCurrency = ""

    companion object {
        @JvmStatic fun openCancellationActivity(activity: Activity, reservationId: Int,hostID: Int, userType : String) {
            val intent = Intent(activity, CancellationActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            intent.putExtra("userType", userType)
            intent.putExtra("hostID",hostID)
            //intent.putExtra("listCurrency",listCurrency)
            activity.startActivityForResult(intent, 5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        intent?.extras?.let {
            reservationId = it.getInt("reservationId")
            userType = it.getString("userType")!!
            hostProfileID = it.getInt("hostID")
            //listCurrency = it.getString("listCurrency")
        }
        mBinding.actionBar.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp))
        mBinding.actionBar.ivNavigateup.onClick { finish() }
        mBinding.scroll.gone()
    }

    private fun subscribeToLiveData() {
        viewModel.loadCancellationDetails(reservationId, userType).observe(this, Observer {
            it?.let { details ->
                setDetails(details)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDetails(details: CancellationDataQuery.Results) {
        try {
            with(mBinding) {
                val checkIn = Utils.getMonth1(details.checkIn()!!)
                val checkOut = Utils.getMonth1(details.checkOut()!!)
                this.listDate = "$checkIn - $checkOut"
                this.listTitle = details.listTitle()
                val missedEarnings = details.refundToGuest()?.minus(details.hostServiceFee()!!)
                details.nonRefundableNightPrice()?.let {
                    if (it == 0.0) {
                        mBinding.rlNonRefund.gone()
                        this.nonrefundablePrice = "0"
                    } else {
                        mBinding.rlNonRefund.visible()
                        if(it !=null) {
                            this.nonrefundablePrice = viewModel?.getCurrencySymbol() + " " + Utils.formatDecimal(it)
                        }else{
                            this.nonrefundablePrice = "0"
                        }
                    }
                }
                if(this.nonrefundablePrice == null) {
                    this.nonrefundablePrice = "0"
                }
                this.refundablePrice = viewModel?.getCurrencySymbol() + " " + Utils.formatDecimal(details.refundToGuest()!!)
                this.guestCount = details.guests()!!.toString() + " " +  resources.getQuantityString(R.plurals.guest_count, details.guests()!!)
                if(details.startedIn()!! > 1) {
                    this.startedDay = details.startedIn()!!.toString() + " " + resources.getQuantityString(R.plurals.day_count, details.startedIn()!!)
                } else {
                    this.startedDay = details.startedIn()!!.toString() + " " + resources.getString(R.string.day)
                }
                this.stayingFor = details.stayingFor()!!.toInt().toString()+ " " + resources.getQuantityString(R.plurals.night_count, Math.round(details.stayingFor()!!).toInt())
                if (userType.equals("host")) {
                    mBinding.rlNonRefund.visible()
                    mBinding.tvCancelText.text = resources.getString(R.string.cancel_your_reservation)
                    this.listName = details.guestName()
                    this.tvTellYou.text = getString(R.string.tell_you, details.guestName())
                    this.listImage = details.guestProfilePicture()
                    this.etMsg.hint = getString(R.string.tell_you_hint, details.guestName())
                    mBinding.rlRefund.visibility = View.GONE
                    mBinding.tvCostNights.visibility = View.VISIBLE
                    mBinding.tvNonRefundable.text = resources.getString(R.string.missed_earnings)
                    val currency = viewModel?.getCurrencySymbol()+" " + Utils.formatDecimal(viewModel?.getConvertedRate(details.listData()!!.listingData()?.currency()!!, details.isSpecialPriceAverage!!.toDouble())!!)

                    mBinding.tvCostNights.text = currency + " x " +details.stayingFor()!!.toInt().toString()+ " " + resources.getQuantityString(R.plurals.night_count, Math.round(details.stayingFor()!!).toInt())
                    mBinding.tvNonRefundPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    mBinding.tvRefundedCost.visibility=View.GONE
                }else{
                    if(this.nonrefundablePrice.equals("0") || this.nonrefundablePrice!!.contains("-")){
                        mBinding.rlNonRefund.gone()
                    }else{
                        mBinding.rlNonRefund.visible()
                        mBinding.tvNonRefundPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    }

                    mBinding.tvCancelText.text = resources.getString(R.string.cancel_your_trip)
                    this.listName = details.hostName()
                    this.tvTellYou.text = getString(R.string.tell_you, details.hostName())
                    this.listImage = details.hostProfilePicture()
                    this.etMsg.hint = getString(R.string.tell_you_hint, details.hostName())

                    if(details.refundToGuest()!=null && details.refundToGuest()!! > 0.0){
                        mBinding.rlRefund.visibility = View.VISIBLE
                        mBinding.tvRefundedCost.visibility=View.VISIBLE
                    }else{
                        mBinding.rlRefund.visibility = View.GONE
                        mBinding.tvRefundedCost.visibility=View.GONE
                    }

                    mBinding.tvCostNights.visibility = View.INVISIBLE
                    mBinding.tvRefundedCost.text = resources.getString(R.string.you_will_be_refunded_with_the_above_cost)
                }
                this.setCancelClickListener {
                    if (this.etMsg.text.trim().isNotEmpty()) {
                        (viewModel as CancellationViewModel).retryCalled = "cancel-${this.etMsg.text.trim()}"
                        (viewModel as CancellationViewModel).cancelReservation(this.etMsg.text.trim().toString(), reservationId)
                    } else {
                        showToast(resources.getString(R.string.enter_msg))
                    }
                }
                this.setListClickListener {
                    try {
                        val currency = BindingAdapters.getCurrencySymbol(viewModel!!.getUserCurrency()) + CurrencyUtil.getRate(
                                base = viewModel!!.getCurrencyBase(),
                                to = viewModel!!.getUserCurrency(),
                                from = details.listData()!!.listingData()!!.currency()!!,
                                rateStr = viewModel!!.getCurrencyRates(),
                                amount = details.listData()!!.listingData()?.basePrice()!!.toDouble()
                        ).toString()
                        val photo = ArrayList<String>()
                        photo.add(details.listData()!!.listPhotos()!![0].name()!!)
                        ListingDetails.openListDetailsActivity(view.context, ListingInitData(
                                details.listData()!!.title()!!,
                                photo,
                                details.listData()!!.id()!!,
                                details.listData()!!.roomType()!!,
                                details.listData()!!.reviewsStarRating(),
                                details.listData()!!.reviewsCount(),
                                currency,
                                0,
                                selectedCurrency = viewModel!!.getUserCurrency(),
                                currencyBase = viewModel!!.getCurrencyBase(),
                                currencyRate = viewModel!!.getCurrencyRates(),
                                startDate = "0",
                                endDate = "0",
                                bookingType = details.listData()!!.bookingType()!!
                        ))
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
                this.setKeepClickListener { onBackPressed() }
                this.setImageClick {
                    UserProfileActivity.openProfileActivity(view.context, hostProfileID)
                }
                mBinding.scroll.visible()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun moveBackScreen() {
        val intent = Intent()
        setResult(56, intent)
        finish()
    }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")){
            viewModel.getCancellationDetails(reservationId, userType)
        }else{
            var text = viewModel.retryCalled.split("-")
            viewModel.cancelReservation(text[1], reservationId)
        }
    }

}