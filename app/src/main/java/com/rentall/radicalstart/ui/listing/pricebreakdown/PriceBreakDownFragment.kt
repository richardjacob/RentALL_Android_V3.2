package com.rentall.radicalstart.ui.listing.pricebreakdown

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentListingPricebreakdownBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.ui.listing.guest.GuestFragment
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.vo.ListingInitData
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject



class PriceBreakDownFragment: BaseFragment<FragmentListingPricebreakdownBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_pricebreakdown
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingPricebreakdownBinding

    var startDate: String = ""
    var endDate: String = ""
    var guestCount: String = ""
    lateinit var dateGuest : ListingDetails.PriceBreakDown
    val array = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.viewModel = viewModel
        mBinding.btnBook.text = viewModel.bookingText.get()
        mBinding.breakDownToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.btnBook.onClick {
            if (viewModel.billingCalculation.value != null) {
                if (viewModel.listingDetails.value!!.userId() != viewModel.getUserId()) {
                    viewModel.checkVerification()
                } else {
                    Toast.makeText(context, resources.getString(R.string.you_cannot_book_your_own_list), Toast.LENGTH_LONG).show()
                }
            } else {
                showSnackbar(resources.getString(R.string.info), resources.getString(R.string.please_select_another_date_to_book))
            }
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.loadInitialValues(activity?.intent!!).observe(viewLifecycleOwner, Observer {
            it?.let { initValues ->
                setup(initValues)
            }
        })

        viewModel.dateGuestCount.observe(viewLifecycleOwner, Observer {
            it?.let { dateGuestCount ->
                dateGuest = dateGuestCount
                mBinding.rlListingPricebreakdown.requestModelBuild()
            }
        })

        viewModel.billingCalculation.observe(viewLifecycleOwner, Observer {
            mBinding.rlListingPricebreakdown.requestModelBuild()
        })

        viewModel.listingDetails.observe(viewLifecycleOwner, Observer {
            listingDetails -> listingDetails?.let {
            it.houseRules()?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                    t?.itemName()?.let { rules ->
                        array.add(rules)
                    }
                }
            }
        })
    }

    private fun setup(it: ListingInitData) = try {
        mBinding.rlListingPricebreakdown.withModels {
            viewholderPricebreakListinfo {
                id("info")
                type(it.roomType)
                img(it.photo[0])
                price(viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(viewModel.listingDetails.value?.listingData()?.currency()!!,
                        viewModel.listingDetails.value?.listingData()?.basePrice()!!.toDouble())))
                reviewsStarRating(it.ratingStarCount)
                if (viewModel.reviewCount.value != null) {
                    reviewsCount(viewModel.reviewCount.value)
                } else {
                    reviewsCount(0)
                }
            }

            viewholderDividerPadding {
                id("divider1")
            }

            viewholderListingPricebreakCheckinout {
                id("checkinout")
                checkIn(getCalenderMonth(viewModel.startDate.value))
                checkOut(getCalenderMonth(viewModel.endDate.value))
                guest(it.guestCount)
                checkInOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                checkOutOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                guestOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openFragment(GuestFragment())}})
            }

            viewholderDividerPadding {
                id("divider2")
            }

            if (viewModel.isLoading.get()) {
                viewholderLoader {
                    id("viewListingLoading")
                    isLoading(true)
                }
            } else {
                if (viewModel.billingCalculation.value != null) {
                    viewholderPricebreakSummary {
                        id("summary")
                        val isRTL= TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())==View.LAYOUT_DIRECTION_RTL
                       val baseRTL = viewModel.billingCalculation.value!!.nights().toString() + " x " + BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.averagePrice()!!)

                        val baseLTR = BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.averagePrice()!!) + " x " + viewModel.billingCalculation.value!!.nights()
                        val ngt = resources.getQuantityString(R.plurals.night_count, viewModel.billingCalculation.value!!.nights() ?: 0);
                        basePrice(if(isRTL) baseRTL else baseLTR)
                        basePriceNights( ngt)
                            basePriceRight(BindingAdapters.getCurrencySymbol(it.selectedCurrency) +
                                    Utils.formatDecimal(viewModel.billingCalculation.value!!.priceForDays()!!))

                        if(viewModel.billingCalculation.value!!.isSpecialPriceAssigned()!!) {
                            spIconVisible(true)
                            onBind { _, view, _ ->
                                val imgView = view.dataBinding.root.findViewById<ImageView>(R.id.specialPriceIcon)
                                val pricingLay = view.dataBinding.root.findViewById<LinearLayout>(R.id.spl_pricing_layout)
                               /* imgView.requestFocus()
                                imgView.setOnFocusChangeListener { v, hasFocus ->
                                    if (hasFocus){
                                        v.onFocusChangeListener.onFocusChange(pricingLay, true)
                                    }else{
                                        v.onFocusChangeListener.onFocusChange(pricingLay, false)
                                    }
                                }*/
                                imgView.setOnClickListener {
                                    pricingLay.visible()
                                    val handler = Handler()
                                    val r = Runnable {  pricingLay.gone() }
                                    handler.postDelayed(r, 1000)
                                }

                            }
                        }else{
                            spIconVisible(false)
                        }
                        if (viewModel.billingCalculation.value!!.cleaningPrice() != null && viewModel.billingCalculation.value!!.cleaningPrice()!! > 0) {
                            cleaningPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.cleaningPrice()!!))
                            cleaningPriceVisibility(true)
                        } else {
                            cleaningPriceVisibility(false)
                        }

                        if (viewModel.billingCalculation.value!!.guestServiceFee() != null && viewModel.billingCalculation.value!!.guestServiceFee() != 0.0) {
                            servicePrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.guestServiceFee()!!))
                            servicePriceVisibility(true)
                        } else {
                            servicePriceVisibility(false)
                        }

                        if (viewModel.billingCalculation.value!!.discountLabel() != null && viewModel.billingCalculation.value!!.discount()!! > 0) {
                            discountVisibility(true)
                            val str = viewModel.billingCalculation.value!!.discountLabel()!!
                            val strArray = str.split(" ")
                            val builder = StringBuilder()
                            for (s in strArray) {
                                val cap = s.substring(0, 1).toUpperCase() + s.substring(1)
                                builder.append(cap + " ")
                            }
                            discountText(builder.toString())
                            discountPrice("-" + BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.discount()!!))
                            totalPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal((viewModel.billingCalculation.value!!.total()!!)))
                        } else {
                            discountVisibility(false)
                            totalPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.total()!!))
                        }
                    }
                } else {
                    viewholderCenterTextPlaceholder {
                        id("ExploreBilling - No Dates")
                        header( resources.getString(R.string.no_dates_available_to_book_please_select_another_date))
                        large(false)
                    }
                }
            }
        }
    } catch (e: KotlinNullPointerException) {
        e.printStackTrace()
    }

    private fun getCalenderMonth(dateString: String?): String {
        try {
            return if (dateString != null && dateString != "0" && dateString.isNotEmpty()) {
                val startDateArray: List<String> = dateString.split("-")
                val year: Int = startDateArray[0].toInt()
                val month = startDateArray[1].toInt()
                val date = startDateArray[2].toInt()

                val monthPattern = SimpleDateFormat("MMM", Locale.ENGLISH)
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, date)
                monthPattern.format(cal.time) + " " + date
            } else {
                resources.getString(R.string.add_date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    override fun onRetry() {
        viewModel.getBillingCalculation()
    }

}