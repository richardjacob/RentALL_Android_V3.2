package com.rentall.radicalstart.ui.saved.createlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentCreatelistBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class CreateListActivity: BaseActivity<FragmentCreatelistBinding, CreateListViewModel>(), CreatelistNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCreatelistBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_createlist
    override val viewModel: CreateListViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(CreateListViewModel::class.java)
    private var mCurrentState = State.IDLE
    private var selectArray = arrayOf(true, false)

    enum class State {
        EXPANDED,
        IDLE
    }

    companion object {
        @JvmStatic fun openCreateListActivity(context: Context) {
            val intent = Intent(context, CreateListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.ivClose.onClick { finish() }
        EpoxyVisibilityTracker().attach(mBinding.rvCreateList)
        setUp()
    }

    private fun setUp() {
        try {
            mBinding.rvCreateList.withModels {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    header(resources.getString(R.string.create_a_list))
                    onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                        mCurrentState = if (percentVisibleHeight < 99) {
                            if (mCurrentState != State.EXPANDED) {
                                ViewCompat.setElevation(mBinding.ablSaved, 5F)
                            }
                            State.EXPANDED
                        } else {
                            if (mCurrentState != State.IDLE) {
                                ViewCompat.setElevation(mBinding.ablSaved, 0F)
                            }
                            State.IDLE
                        }
                    }
                }
                viewholderSavedPlaceholder {
                    id("title")
                    header(resources.getString(R.string.title))
                    isBlack(true)
                }
                viewholderSavedEt {
                    id("titleEditText")
                    msg(viewModel.title)
                }
               /* viewholderSavedPlaceholder {
                    id("privacy")
                    header("Privacy")
                    isBlack(true)
                    large(true)

                }
                viewholderCreatelistRadio {
                    id("radio")
                    text(resources.getString(R.string.public_text))
                    textDesc(resources.getString(R.string.public_savedlist_desc))
                    radioVisibility(selectArray[0])
                    onClick(View.OnClickListener { selector(0); viewModel.setPrivacy("0") })
                }
                viewholderDivider {
                    id("Divider - 2")
                }
                viewholderCreatelistRadio {
                    id("radio1")
                    text(resources.getString(R.string.private_text))
                    textDesc(resources.getString(R.string.private_savedlist_desc))
                    radioVisibility(selectArray[1])
                    onClick(View.OnClickListener { selector(1); viewModel.setPrivacy("1") })
                }*/
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onRetry() {
        viewModel.validateData()
    }

    private fun selector(index: Int) {
        selectArray.forEachIndexed { i: Int, _: Boolean ->
            selectArray[i] = index == i
        }
        mBinding.rvCreateList.requestModelBuild()
    }

    override fun navigate(isLoadSaved: Boolean) {
        if(isLoadSaved) {
            val intent = Intent()
            setResult(2, intent)
            finish()
        } else {
            finish()
        }
    }
}