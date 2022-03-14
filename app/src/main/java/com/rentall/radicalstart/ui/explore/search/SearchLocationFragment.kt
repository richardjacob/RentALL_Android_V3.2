package com.rentall.radicalstart.ui.explore.search

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.ActivitySearchBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.ExploreViewModel
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.vo.Outcome
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class SearchLocationFragment : BaseFragment<ActivitySearchBinding, ExploreViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_search
    override val viewModel: ExploreViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: ActivitySearchBinding
    private var resetBoolean=false
    private lateinit var handler: Handler
    private lateinit var textWatcher: TextWatcher
    var editTextRunnable: Runnable? = null
    private var timer: Timer? = null
    private var checkDisplayNoResult=false
    private val searchedLocationList: ArrayList<String> = ArrayList()

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        val anim = AnimationUtils.loadAnimation(baseActivity, nextAnim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) { }
            override fun onAnimationEnd(p0: Animation?) {
                if (enter) {
                    baseActivity?.let {
                        Utils.showSoftKeyboard(it)
                    }
                }
            }
            override fun onAnimationStart(p0: Animation?) {
                if (!enter) {
                    baseActivity?.let {
                        Utils.hideSoftKeyboard(it)
                    }
                }
            }
        })
        return anim
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        setUp()
        Utils.showSoftKeyboard(baseActivity!!)
    }

    private fun initView() {
        initTextWatcher()
        handler = Handler(Looper.getMainLooper())
        mBinding.inlToolbar.tvRightside.text = resources.getString(R.string.reset)
        mBinding.inlToolbar.tvRightside.visible()
        mBinding.inlToolbar.ivNavigateup.onClick {
            if(resetBoolean){
                viewModel.setLocation("")
                mBinding.etSearchLocation.setText("")
                viewModel.startSearching()
            }
            Utils.hideSoftKeyboard(baseActivity)
            baseActivity?.onBackPressed()
        }
        mBinding.inlToolbar.tvRightside.onClick {
            mBinding.etSearchLocation.setText("")
            resetBoolean=true
        }
        mBinding.etSearchLocation.addTextChangedListener(textWatcher)
        mBinding.etSearchLocation.requestFocus()
        mBinding.etSearchLocation.setOnEditorActionListener { _, actionId, _ ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH){
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun subscribeToLiveData() {
        viewModel.searchLocation.observe(viewLifecycleOwner, Observer { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    if (mBinding.etSearchLocation.text.isNotEmpty()) {
                        searchedLocationList.clear()
                        try{
                            outcome.data.forEach {
                                searchedLocationList.add(it)
                            }
                        }catch (e: Exception){
                           e.printStackTrace()
                        }
                        checkDisplayNoResult=true
                        mBinding.rvSearchLocation.requestModelBuild()
                    }else{
                        searchedLocationList.clear()
                        mBinding.rvSearchLocation.requestModelBuild()
                    }
                }
            }
        })
        viewModel.location.observe(viewLifecycleOwner, Observer {location ->
            location?.let {
                mBinding.etSearchLocation.setText(it)
                mBinding.etSearchLocation.setSelection(0, it.length)
                mBinding.etSearchLocation.requestFocus()
            }
        })
    }

    private fun initTextWatcher() {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        mBinding.inlToolbar.tvRightside.visible()
                        if (editTextRunnable != null) {
                            handler.removeCallbacks(editTextRunnable ?: Runnable {  })
                        }
                        editTextRunnable = Runnable {
                            try {
                                searchLocation(it.toString())
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        }
                        handler.postDelayed(editTextRunnable ?: Runnable {  }, 1000)
                    } else {
                        viewModel.searchLocation.value = Outcome.success(emptyList())
                        mBinding.inlToolbar.tvRightside.invisible()
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (timer != null) {
                    timer = null
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUp() {
        val itemDecor = DividerItemDecoration1(ContextCompat.getDrawable(requireContext(), R.drawable.divider), 0 , 0)
        mBinding.rvSearchLocation.addItemDecoration(itemDecor)
        mBinding.rvSearchLocation.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP){
                Utils.hideSoftKeyboard(baseActivity)
            }
            return@setOnTouchListener false
        }
        mBinding.rvSearchLocation.withModels {
            if(searchedLocationList.size>0){
                mBinding.tvNoResult.visibility=View.GONE
                searchedLocationList.forEachIndexed { _, location ->
                    viewholderExploreSearchLocation {
                        id(location)
                        location(location)
                        clickListener(View.OnClickListener {
                            if (isNetworkConnected)
                                getGeocode(location)
                            else {
                                hideKeyboard()
                                baseActivity!!.showToast(resources.getString(R.string.currently_offline))
                            }
                        } )
                    }
                }
                viewholderPoweredByGoogle {
                    id(0)
                }
            }else{
                if(mBinding.etSearchLocation.text.isNotEmpty()){
                    if(checkDisplayNoResult){
                        mBinding.tvNoResult.visibility=View.VISIBLE
                    }
                }else{
                    mBinding.tvNoResult.visibility=View.GONE
                }

            }

        }
    }

    private fun getGeocode(location: String) {
        try {
            viewModel.setLocation(location)
            mBinding.etSearchLocation.setText(location)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(editTextRunnable ?: Runnable {  })
        super.onDestroy()
    }

    override fun onRetry() {

    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    fun searchLocation(location: String) {
        Places.initialize(baseActivity!!, Constants.googleMapKey)
        val placesClient = Places.createClient(baseActivity!!)
        val token = AutocompleteSessionToken.newInstance()
        val autocompleteFilter = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.REGIONS)
                .setQuery(location)
                .build()

        var autocompletePredictions: List<AutocompletePrediction>? = null
        try {
            placesClient.findAutocompletePredictions(autocompleteFilter).addOnSuccessListener(OnSuccessListener {
                try {
                    autocompletePredictions = it.autocompletePredictions
                    Timber.tag("AutoComplete Size").w("AutoComplete Size=%s", autocompletePredictions!!.size)
                    val list = ArrayList<String>()
                    autocompletePredictions?.forEach {
                        list.add(it.getFullText(null).toString())
                    }
                    viewModel?.searchLocation?.value = Outcome.success(list)
                }catch (e: Exception){
                   e.printStackTrace()
                }
            }).addOnFailureListener(OnFailureListener {
                it.printStackTrace()
            })
        }catch (e: RuntimeExecutionException) {
            Timber.tag("search").e(e, "Error getting autocomplete prediction API call")
        }
    }
}


