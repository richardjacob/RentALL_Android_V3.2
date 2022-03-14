package com.rentall.radicalstart.ui.profile.about.why_Host

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityAddPayoutBinding
import com.rentall.radicalstart.databinding.FragmentWhyHostBinding
import com.rentall.radicalstart.host.payout.addPayout.AddPayoutViewModel
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.host.step_one.StepOneActivity
import com.rentall.radicalstart.ui.host.step_one.StepOneViewModel
import com.rentall.radicalstart.ui.profile.about.AboutViewModel
import com.rentall.radicalstart.ui.profile.setting.SettingViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class WhyHostActivity : BaseActivity<FragmentWhyHostBinding,AboutViewModel>() {

    private val myImageList = intArrayOf(R.drawable.bg_image1, R.drawable.bg_image3, R.drawable.bg_image2, R.drawable.bg_image4)
    private val myTextList = intArrayOf(R.string.text_one, R.string.text_two, R.string.text_three, R.string.text_four)


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentWhyHostBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_why_host
    override val viewModel: AboutViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AboutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding=viewDataBinding!!
        init()
    }

    private fun init() {
        mBinding.ivNavigateup.onClick {
            super.onBackPressed()
        }
        mBinding.listYourSpace.onClick {
            val intent = Intent(this@WhyHostActivity, StepOneActivity::class.java)
            startActivity(intent)
        }
        mBinding.pager?.adapter = SlidingImageAdapter(this@WhyHostActivity, populateList())
        val  currentLocale : Locale = Utils.getCurrentLocale(applicationContext)!!
        if(resources.getBoolean(R.bool.is_left_to_right_layout).not()){
            mBinding.pager?.currentItem=mBinding.pager.adapter?.count!!-1
        }else{
            mBinding.pager?.currentItem=0
        }

        mBinding.indicator.radius = 5 * resources.displayMetrics.density
        mBinding.indicator.setViewPager(mBinding.pager)
    }

    private fun populateList(): ArrayList<ImageModel> {
        val list = ArrayList<ImageModel>()
        for (i in 0..3) {
            val imageModel = ImageModel()
            imageModel.setImage_drawables(myImageList[i])
            imageModel.setText_srings(myTextList[i])
            list.add(imageModel)
        }
        return list
    }

    override fun onRetry() {

    }
}