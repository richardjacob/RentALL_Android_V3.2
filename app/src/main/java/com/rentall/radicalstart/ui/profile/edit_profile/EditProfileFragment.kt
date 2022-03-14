package com.rentall.radicalstart.ui.profile.edit_profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityEditProfileBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class EditProfileFragment: BaseFragment<ActivityEditProfileBinding, EditProfileViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var activityEditProfileBinding: ActivityEditProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_edit_profile
    override val viewModel: EditProfileViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(EditProfileViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityEditProfileBinding = viewDataBinding!!
        activityEditProfileBinding.actionBar.rlToolbarDone.onClick {
            viewModel.done()
        }
    }

    override fun onRetry() { }
}