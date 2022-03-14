package com.rentall.radicalstart.ui.saved.createlist

import androidx.databinding.ObservableField
import com.rentall.radicalstart.CreateWishListGroupMutation
import com.rentall.radicalstart.CreateWishListMutation
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject


class CreateListViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<CreatelistNavigator>(dataManager,resourceProvider) {

    val title = ObservableField("")
    val groupPrivacy = ObservableField("0")

    fun validateData() {
        navigator.hideKeyboard()
        if(title.get().toString().isNullOrEmpty() || title.get().toString().isBlank() || title.get().toString().equals("")) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error), resourceProvider.getString(R.string.please_enter_the_title_of_wishlist_group))
        } else {
            if(title.get().toString().length>250){
                navigator.showSnackbar(resourceProvider.getString(R.string.error), resourceProvider.getString(R.string.limit_exceeded))
            }else{
                val mytext = title.get().toString().replace("\\s+".toRegex(), " ")
                title.set(mytext)
                createWishlistGroup()
            }
        }
    }

    fun createWishlistGroup() {
        val buildMutation = CreateWishListGroupMutation
                .builder()
                .name(title.get()!!)
                .isPublic(groupPrivacy.get()!!)
                .build()

        compositeDisposable.add(dataManager.createWishListGroup(buildMutation)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.CreateWishListGroup()?.status() == 200) {
                            navigator.navigate(true)
                        }  else if(response.data()?.CreateWishListGroup()?.status() == 500) {
                            navigator.openSessionExpire()
                        }

                        else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) } )
        )
    }

    fun CreateWishList() {
        val buildMutation = CreateWishListMutation
                .builder()
                .eventKey(true)
                .build()

        compositeDisposable.add(dataManager.CreateWishList(buildMutation)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.CreateWishList()?.status() == 200) {
                            //navigator.navigateScreen(Screen.MOVETOHOME)
                        }else if(response.data()?.CreateWishList()?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) } )
        )
    }

    fun setPrivacy(value: String) {
        groupPrivacy.set(value)
    }

}