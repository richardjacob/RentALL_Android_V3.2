package com.rentall.radicalstart.util.binding

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.*
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.auth.birthday.BirthdayViewModel
import com.rentall.radicalstart.ui.booking.BookingViewModel
import com.rentall.radicalstart.ui.host.step_one.StepOneViewModel
import com.rentall.radicalstart.ui.inbox.msg_detail.InboxMsgViewModel
import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoViewModel
import com.rentall.radicalstart.ui.profile.edit_profile.EditProfileViewModel
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.*
import jp.wasabeef.glide.transformations.BlurTransformation
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

object BindingAdapters {

    @BindingAdapter(value = ["app:first", "app:last", "app:screen"], requireAll = false)
    @JvmStatic fun enableOrDisable(view: View, first: String?, last: String?, screen: AuthViewModel.Screen) {
        when(screen) {
            AuthViewModel.Screen.LOGIN -> {
                if (first!!.isNotEmpty() && last!!.isNotEmpty()) view.EnableAlpha(true) else view.EnableAlpha(false)
            }
            AuthViewModel.Screen.NAME -> {
                if (view is ImageView) (view).setImageResource(R.drawable.ic_right_arrow_blue)
                if (first!!.isNotEmpty() && last!!.isNotEmpty()) view.EnableAlpha(true) else view.EnableAlpha(false)
            }
            AuthViewModel.Screen.FORGOTPASSWORD -> {
                if (Utils.isValidEmail(first!!)) view.EnableAlpha(true) else view.EnableAlpha(false)
            }
            AuthViewModel.Screen.CHANGEPASSWORD -> {
                if (view is ImageView) (view).setImageResource(R.drawable.ic_right_arrow_blue)
                if (first!!.length >= 7 && last!!.length >= 7 && first == last) view.EnableAlpha(true) else view.EnableAlpha(false)
            }
            AuthViewModel.Screen.CODE -> {
                if (first!!.length >= 4) view.EnableAlpha(true) else view.EnableAlpha(false)
            }
            AuthViewModel.Screen.CREATELIST -> {
                if (first != null && last != null) {
                    if (first.isNotEmpty() && last != "-1") view.EnableAlpha(true) else view.EnableAlpha(false)
                }
            }
            else -> { }
        }
    }

    @BindingAdapter(value = ["app:dateOfBirth", "app:viewModel"])
    @JvmStatic fun checkDob(view: View, dob: Array<Int>?, viewModel: BirthdayViewModel) {
            dob?.let {
                if (Utils.getAge(dob[0], dob[1] - 1, dob[2]) >= 18) {
                    view.EnableAlpha(true)
                } else {
                    view.EnableAlpha(false)
                    viewModel.showError()
                }
            } ?: view.EnableAlpha(false)
    }

    @BindingAdapter(value = ["app:hideIt"])
    @JvmStatic fun hideIt(view: View, lottieProgress: AuthViewModel.LottieProgress) {
        when(lottieProgress) {
            AuthViewModel.LottieProgress.LOADING, AuthViewModel.LottieProgress.CORRECT -> view.disable()
            else -> view.enable()
        }
    }

    @BindingAdapter(value = ["android:checkPublishStates"])
    @JvmStatic
    fun checkPublishStates (view: TextView, @Nullable status: String?) {
        view.setBackgroundResource(R.drawable.preview_bg)
        view.setTypeface(view.getTypeface(), Typeface.NORMAL)
        view.enable()
          view.text =   when(status) {
              null -> view.resources.getString(R.string.sub_verify)
              "pending" -> {
                  view.setBackgroundResource(R.drawable.preview_bg_disable)
                  view.setTypeface(view.getTypeface(), Typeface.BOLD)
                  view.disable()
                  view.resources.getString(R.string.waiting)
              }
              "approved" -> view.resources.getString(R.string.publish)
              "declined" -> view.resources.getString(R.string.appeal)
              "published" -> view.resources.getString(R.string.unpublish)
              else -> view.resources.getString(R.string.publish_now)
          }
    }

    @BindingAdapter(value = ["app:hideNext"])
    @JvmStatic fun hideNext(view: LottieAnimationView, lottieProgress: ConfirmPhnoViewModel.LottieProgress) {
        when(lottieProgress) {
            ConfirmPhnoViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            ConfirmPhnoViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }

        }
    }

    @BindingAdapter(value = ["app:hideNextButton"])
    @JvmStatic fun hideNextButton(view: LottieAnimationView, lottieProgress: StepOneViewModel.LottieProgress) {
        when(lottieProgress) {
            StepOneViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            StepOneViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }

        }
    }

    @BindingAdapter(value = ["app:hideBookButton"])
    @JvmStatic fun hideBookButton(view: LottieAnimationView, lottieProgress: InboxMsgViewModel.LottieProgress) {
        when(lottieProgress) {
            InboxMsgViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation_white)
                view.playAnimation()
            }
            InboxMsgViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
            }

        }
    }

    @BindingAdapter(value = ["app:lottieIcon"])
    @JvmStatic fun lottieIcon(view: LottieAnimationView, lottieProgress: AuthViewModel.LottieProgress) {
        when(lottieProgress) {
            AuthViewModel.LottieProgress.NORMAL -> {
                if (view.isAnimating) {
                    view.scale = 3F
                    view.cancelAnimation()
                }
                view.setImageResource(R.drawable.ic_right_arrow_blue)
            }
            AuthViewModel.LottieProgress.LOADING -> {
                view.setAnimation(R.raw.animation)
                view.playAnimation()
            }
            AuthViewModel.LottieProgress.CORRECT -> {
                if (view.isAnimating) {
                    view.cancelAnimation()
                }
                view.setImageResource(R.drawable.ic_check_green)
            }
        }
    }

    @BindingAdapter(value = ["isWishList", "progress", "retryOption"])
    @JvmStatic fun lottieSaved(view: LottieAnimationView, isWishList: Boolean, lottieProgress: AuthViewModel.LottieProgress?, retryOption: String) {
        lottieProgress?.let {
            when(lottieProgress) {
                AuthViewModel.LottieProgress.NORMAL -> {
                    if (view.isAnimating) {
                        view.cancelAnimation()
                    }

                    if (isWishList) {
                        view.setImageResource(R.drawable.ic_heart_filled_large)
                    } else {
                        view.setImageResource(R.drawable.ic_heart_white_filled)
                    }
                    if (retryOption.contains("create")) {
                        view.disable()
                        view.setImageResource(0)
                    } else {
                        view.enable()
                    }
                }
                AuthViewModel.LottieProgress.LOADING -> {
                    view.disable()
                    view.scale = 0.7F
                    view.setAnimation(R.raw.animation)
                    view.playAnimation()
                    view.repeatCount = -1
                }
                AuthViewModel.LottieProgress.CORRECT -> {
                    if (view.isAnimating) {
                        view.cancelAnimation()
                    }
                    if (isWishList) {
                        view.scale = 1.0F
                        view.setAnimation(R.raw.heartss)
                        view.playAnimation()
                        view.repeatCount = 0
                    } else {
                        view.setImageResource(R.drawable.ic_heart_white_filled)
                    }
                    view.enable()
                }
            }
        }
    }

    @BindingAdapter(value = ["app:errorIcon"])
    @JvmStatic fun errorIcon(view: EditText, snackbarType: Boolean) {
        when(snackbarType) {
            true -> view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_priority_high_black_24dp, 0)
            false -> view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    @BindingAdapter(value = ["app:toggle"])
    @JvmStatic fun showPassword(view: EditText, show: Boolean) {
        if (show) {
            view.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            view.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        view.setSelection(view.text.length)
    }

    @Suppress("unused")
    @BindingAdapter("goneUnless")
    @JvmStatic fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
    @BindingAdapter(value = ["spanString", "type", "profileId", "isAdmin", "isListing"], requireAll = true)
    @JvmStatic fun nameSpan(view: TextView, name: String?, type: String?, profileId: Int?, isAdmin: Boolean?, isListing: Boolean?) {
        try{
            if(name!=null&&name.isNotEmpty()){
             if(isAdmin!!){
                 val spString= SpannableString(name)
                 spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                 view.text=spString
             }else{
                 lateinit var spString: SpannableString

                 val clickableSpan = object: ClickableSpan(){
                     override fun onClick(p0: View) {
                         if(profileId!=0){
                             Utils.clickWithDebounce(view) {
                                 UserProfileActivity.openProfileActivity(view.context, profileId!!)
                             }
                         }
                     }
                     override fun updateDrawState(ds: TextPaint) { // override updateDrawState
                         ds.isUnderlineText = false // set to false to remove underline
                         ds.color = ContextCompat.getColor(view.context, R.color.colorPrimary)
                     }
                 }
                 when (type) {
                     "aboutYou" -> {
                         Timber.d("Kalis Find-------->    $name")
                         val nameSub = if (name.length > 20) {
                             if (isListing!!) {
                                 name.substring(0, 15) + "...'s "
                             } else {
                                 "$name's "
                             }
                         } else {
                             "$name's "
                         }
                         spString = SpannableString(nameSub + view.context.getString(R.string.review))
                         val stIndex = nameSub.length
                         val enIndex = nameSub.length + view.context.getString(R.string.review).length
                         spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), stIndex, enIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                         spString.setSpan(clickableSpan, 0, nameSub.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                     }
                     "writeReview" -> {
                         spString = SpannableString(view.context.getString(R.string.submit_a_public_review_for) + " " + name)
                         spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, view.context.getString(R.string.submit_a_public_review_for).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                         spString.setSpan(clickableSpan, view.context.getString(R.string.submit_a_public_review_for).length + 1, (view.context.getString(R.string.submit_a_public_review_for) + " " + name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                     }
                     else -> {
                         spString= SpannableString(view.context.getString(R.string.you_reviewed) + " " + name)
                         spString.setSpan(ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.black)), 0, view.context.getString(R.string.you_reviewed).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                         spString.setSpan(clickableSpan, view.context.getString(R.string.you_reviewed).length + 1, (view.context.getString(R.string.you_reviewed) + " " + name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                     }
                 }

                 view.text=spString
                 view.movementMethod=LinkMovementMethod.getInstance()
                 view.visible()
             }

        }else{
            view.gone()
        }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    @BindingAdapter("goneUnlessInverse")
    @JvmStatic fun goneUnlessInverse(view: View, visible: Boolean) {
        view.visibility = if (visible) View.GONE else View.VISIBLE
    }

    @BindingAdapter(value = ["img", "isAdmin"], requireAll = false)
    @JvmStatic fun loadImage(view: ImageView, url: String?, isAdmin: Boolean = false) {
        if (isAdmin) {
            GlideApp.with(view.context).load(R.drawable.admin_avatar).into(view)
        } else {
            Timber.d("urlImageNotUpdated--->>  $url")
            url?.let {
                if(url.isEmpty()){
                    GlideApp.with(view.context).load(R.drawable.placeholder_avatar).into(view)
                }else{
                    GlideApp.with(view.context)
                            .load(Constants.imgAvatarMedium + url)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(view)
                }
            } ?: GlideApp.with(view.context).load(R.drawable.placeholder_avatar).into(view)
        }
    }

    @BindingAdapter("imgListing")
    @JvmStatic fun loadListingImage(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                Timber.d("Normal image-->>" + Constants.imgListingMedium + url)
                Timber.d("Small image---->>" + Constants.imgListingSmall + url)
                GlideApp.with(view.context)
                        .load(Constants.imgListingMedium + url)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .thumbnail(GlideApp.with(view.context)
                                .load(Constants.imgListingSmall + url)
                                .thumbnail(GlideApp.with(view.context)
                                        .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                        .transform(RoundedCorners(12)))
                                .transform(BlurTransformation(25, 3), CenterCrop(), RoundedCorners(12))
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("imgListingPopular")
    @JvmStatic fun loadListingImagePopular(view: ShapeableImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                Timber.d("Normal image-->>" + Constants.imgListingPopularMedium + url)
                Timber.d("Small image---->>" + Constants.imgListingPopularSmall + url)
                GlideApp.with(view.context)
                        .load(Constants.imgListingPopularMedium + url)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .thumbnail(GlideApp.with(view.context)
                                .load(Constants.imgListingPopularSmall + url)
                                .thumbnail(GlideApp.with(view.context)
                                        .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                        .transform(RoundedCorners(12)))
                                .transform(BlurTransformation(25, 3), CenterCrop(), RoundedCorners(12))
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }
    private fun loadBg(view: ImageView) {
        GlideApp.with(view.context)
                .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                .transform(RoundedCorners(12))
                .into(view)
    }

    @BindingAdapter("imgUrl")
    @JvmStatic fun loadImgUrl(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                        .load(url)
                       /* .thumbnail(GlideApp
                                .with(view.context)
                                .load("http://demo.rentallscript.com/images/upload/x_small_$url")
                                .apply(bitmapTransform(BlurTransformation(25, 3)))
                                .diskCacheStrategy(DiskCacheStrategy.ALL))*/
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
            }
        }
    }

    @BindingAdapter("hideSome")
    @JvmStatic fun hideSome(view: View, text: String) {
        view.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    @BindingAdapter("hideArrow")
    @JvmStatic fun hideArrow(view: View, text: String) {
        view.visibility = if (text.isNotEmpty()) View.GONE else View.VISIBLE
    }

    @BindingAdapter("hideVerifiedArrow")
    @JvmStatic fun hideArrow(view: View, text: Boolean) {
        view.visibility = if (text) View.GONE else View.VISIBLE
    }

    @BindingAdapter(value = ["addView", "app:viewModel"])
    @JvmStatic fun addView(viewGroup: ViewGroup, layoutID: Int, viewModel: EditProfileViewModel) {
        if (layoutID != 0) {
            viewGroup.removeAllViews()
            val inflater = LayoutInflater.from(viewGroup.context)
            when (layoutID) {
                R.layout.include_edit_email -> {
                    val viewDataBinding = IncludeEditEmailBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.email.get())
                }
                R.layout.include_edit_phone -> {
                    val viewDataBinding = IncludeEditPhoneBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.phone.get())
                }
                R.layout.include_edit_location -> {
                    val viewDataBinding = IncludeEditLocationBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.location.get())
                }
                R.layout.include_edit_aboutme -> {
                    val viewDataBinding = IncludeEditAboutmeBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.aboutMe.get())
                }
                R.layout.include_edit_name -> {
                    val viewDataBinding = IncludeEditNameBinding.inflate(inflater, viewGroup, true)
                    viewDataBinding.viewModel = viewModel
                    viewModel.temp.set(viewModel.firstName.get())
                    viewModel.temp1.set(viewModel.lastName.get())
                }
            }
        }
    }

    @BindingAdapter(value = ["memberSince"])
    @JvmStatic fun memberSince(view: TextView, text: String) {
        try {
            if (!text.isEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = text.toLong()
                view.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            }
        } catch (e: Exception) { }
    }

    @BindingAdapter(value = ["memberSinceComma"])
    @JvmStatic fun memberSinceComma(view: TextView, text: String) {
        try {
            if (!text.isEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = text.toLong()
                view.text = SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(calendar.time)
            }
        } catch (e: Exception) { }
    }


    @BindingAdapter(value = ["currencyCode", "price"], requireAll = true)
    @JvmStatic fun currencySymbol(view: TextView, currencyCode: String?, price: String?) {
        if (!currencyCode.isNullOrEmpty() && !price.isNullOrEmpty()) {
            view.text = getCurrencySymbol(currencyCode) + price +" per Night"
        }
    }

    @JvmStatic fun getCurrencySymbol(currencyCode: String?): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.symbol
        } catch (e: Exception) {
            currencyCode ?: ""
        }
    }

    @BindingAdapter("instantBook")
    @JvmStatic fun instantBook(view: ImageView, bookingType: String?) {
        if (!bookingType.isNullOrEmpty() && bookingType == "instant") {
            view.setImageResource(R.drawable.ic_light)
        } else {
            view.setImageResource(0)
        }
        /*bookingType?.let {
            view.setImageResource(R.drawable.ic_light)
            if (!bookingType.isNullOrEmpty() && bookingType == "instant") {
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_light, 0)
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }*/
    }

    @BindingAdapter(value = ["ratingCount", "reviewsCount"], requireAll = true)
    @JvmStatic fun ratingStarCount(view: RatingBar, ratingCount: Int?, reviewsCount: Int?) {
        if (ratingCount != null && ratingCount != 0 && reviewsCount != null && reviewsCount != 0) {
            val roundOff = round(ratingCount.toDouble() / reviewsCount.toDouble())
            Timber.d("ratingCount ${round(ratingCount.toDouble() / reviewsCount.toDouble())}")
            view.rating = roundOff.toFloat()
        } else {
            view.rating = 0f
        }
    }

    @BindingAdapter(value = ["ratingTotal", "reviewsTotal"], requireAll = true)
    @JvmStatic fun ratingCount(view: RatingBar, ratingCount: Double?, reviewsCount: Int?){
        if (ratingCount != null && ratingCount != 0.0 && reviewsCount != null && reviewsCount != 0) {
            val roundOff = round(ratingCount.toDouble() / reviewsCount.toDouble())
            view.rating = roundOff.toFloat()
        } else {
            view.rating = 0f
        }
    }


    @BindingAdapter("formatDOB")
    @JvmStatic fun formatDOB(view: TextView, dob: String?) {
        dob?.let {
            if (!dob.isEmpty()) {
                val string = dob.split("-")
                view.text = string[0]+"-"+string[2]+"-"+string[1]
            }
        }
    }

    @BindingAdapter("color")
    @JvmStatic fun color(view: View, color: Int?) {
        color?.let {
           view.setBackgroundColor(color)
        }
    }

    @BindingAdapter("textSize")
    @JvmStatic fun textSize(view: TextView, size: Float?) {
        size?.let {
            view.textSize = size
        }
    }

    @BindingAdapter("textStyle")
    @JvmStatic fun textStyle(view: TextView, typeface: Typeface?) {
        typeface?.let {
            view.typeface = typeface
        }
    }

    @BindingAdapter("textAlignment")
    @JvmStatic fun textAlignment(view: TextView, gravity: Gravity?) {
        gravity?.let {
            view.gravity = Gravity.CENTER
        }
    }


    @BindingAdapter(value = ["countryCheck", "textChangeLis", "offsetPos"], requireAll = true)
    @JvmStatic fun textChangeLis(view: EditText, countryCheck: Boolean, text: String?, offsetPos: Int) {
       text?.let {
           if(countryCheck){
               if(text.isNotEmpty()){
                   if(text[text.length - 1]!='-'){
                       if(text.length in offsetPos+1..offsetPos+1){
                           val sb= StringBuilder(text).insert(offsetPos, "-")
                           view.setText(sb)
                           view.setSelection(offsetPos + 2)
                       }
                   }
               }
           }
       }
    }

    @BindingAdapter("convert24to12hrs")
    @JvmStatic fun convert24to12hrs(view: TextView, time: String?) {
        time?.let {
            if (time.isNotEmpty()) {
                if (time == "Flexible") {
                    view.text = time
                } else {
                    view.text = timeConverter(time)
                }
            }
        }
    }

    fun timeConverter(time: String) : String {
        var formatedTime = time
        try {
            val sdf = SimpleDateFormat("H", Locale.ENGLISH)
            val dateObj = sdf.parse(time)
            formatedTime = SimpleDateFormat("Ka", Locale.ENGLISH).format(dateObj)
        } catch (e: ParseException) {
            e.printStackTrace()
        } finally {
            return formatedTime
        }
    }

    @BindingAdapter(value = ["guestCount", "minusLimit"], requireAll = true)
    @JvmStatic fun guestCountLimitMinus(view: ImageButton, guestCount: String?, minusLimit: Int?) {
        minusLimit?.let {
            guestCount?.let {
                if (minusLimit > guestCount.toInt() - 1) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter(value = ["guestCount", "plusLimit"], requireAll = true)
    @JvmStatic fun guestCountLimitPlus(view: ImageButton, guestCount: String?, plusLimit: Int?) {
        plusLimit?.let {
            guestCount?.let {
                if (plusLimit - 1 < guestCount.toInt()) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter(value = ["bathroomCount", "minusLimit"], requireAll = true)
    @JvmStatic fun bathroomLimitMinus(view: ImageButton, guestCount: String?, minusLimit: Int?) {
        minusLimit?.let {
            guestCount?.let {
                if (minusLimit > guestCount.toDouble() - 0.5) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter(value = ["bathroomCount", "plusLimit"], requireAll = true)
    @JvmStatic fun bathroomLimitPlus(view: ImageButton, guestCount: String?, plusLimit: Int?) {
        plusLimit?.let {
            guestCount?.let {
                if (plusLimit - 0.5 < guestCount.toDouble()) {
                    view.EnableAlpha(false)
                } else {
                    view.EnableAlpha(true)
                }
            }
        }
    }

    @BindingAdapter("guestPlural")
    @JvmStatic fun textStyle(view: TextView, guestCount: String?) {
        guestCount?.let {
            if (guestCount.toInt() > 1) {
                view.text = view.resources.getString(R.string.guests)
            } else {
                view.text = view.resources.getString(R.string.guest)
            }
        }
    }

    @BindingAdapter("statusBg")
    @JvmStatic fun statusBg(view: TextView, status: String?) {
        var label = ""
        when(status) {
            "Pending" -> label = view.resources.getString(R.string.pending)
            "Cancelled" -> label = view.resources.getString(R.string.cancelled)
            "Declined" -> label = view.resources.getString(R.string.declined)
            "Approved" -> label = view.resources.getString(R.string.approved)
            "Completed" -> label = view.resources.getString(R.string.completed)
            "Expired" -> label = view.resources.getString(R.string.expired)
        }
        label?.let {
            when(it) {
                view.resources.getString(R.string.pending) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.cancelled), view.resources.getString(R.string.declined) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.approved), view.resources.getString(R.string.completed) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_green)
                }
                view.resources.getString(R.string.expired) -> {
                    view.text = it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_orange)
                }
            }
        }
    }

    @BindingAdapter("statusInboxBg")
    @JvmStatic fun statusInboxBg(view: TextView, status: String?) {
        var label = ""
        when(status){
            "inquiry" -> label = view.resources.getString(R.string.Inquiry)
            "preApproved" -> label = view.resources.getString(R.string.pre_approved)
            "declined" -> label = view.resources.getString(R.string.declined)
            "approved" -> label = view.resources.getString(R.string.approved)
            "pending" -> label = view.resources.getString(R.string.pending)
            "cancelledByHost" -> label = view.resources.getString(R.string.cancelled_by_host)
            "cancelledByGuest" -> label = view.resources.getString(R.string.cancelled_by_guest)
            "intantBooking" -> label = view.resources.getString(R.string.approved)
            "confirmed" -> label = view.resources.getString(R.string.booking_confirmed)
            "expired" -> label = view.resources.getString(R.string.expired)
            "requestToBook" -> label = view.resources.getString(R.string.request_to_book)
            "completed" -> label = view.resources.getString(R.string.completed)
            "reflection" -> label = view.resources.getString(R.string.reflection)
            "message" -> label = view.resources.getString(R.string.message_small)
        }
        label?.let { it ->
            when(it) {
                view.resources.getString(R.string.Inquiry) -> {
                    view.text = view.resources.getString(R.string.Inquiry)//it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_lightblue)
                }
                view.resources.getString(R.string.request_to_book) -> {
                    view.text = view.resources.getString(R.string.request_to_book)//it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.pre_approved) -> {
                    view.text = view.resources.getString(R.string.pre_approved)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_blue)
                }
                view.resources.getString(R.string.declined) -> {
                    view.text = view.resources.getString(R.string.declined)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.cancelled_by_host) -> {
                    view.text = view.resources.getString(R.string.cancelled_by_host)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.cancelled_by_guest) -> {
                    view.text = view.resources.getString(R.string.cancelled_by_guest)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.expired) -> {
                    view.text = view.resources.getString(R.string.expired)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_red)
                }
                view.resources.getString(R.string.approved) -> {
                    view.text = view.resources.getString(R.string.approved)
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_green)
                }
                view.resources.getString(R.string.booking_confirmed) -> {
                    view.text = view.resources.getString(R.string.booking_confirmed)//it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_green)
                }
                view.resources.getString(R.string.completed) -> {
                    view.text = view.resources.getString(R.string.completed)//it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_green)
                }
                view.resources.getString(R.string.pending) -> {
                    view.text = view.resources.getString(R.string.pending)//it
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_orange)
                }
                view.resources.getString(R.string.message_small) -> {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.label_transparent)
                    view.text = ""
                }
            }
        }
    }

    @BindingAdapter("enableSendBtn")
    @JvmStatic fun enableSendBtn(view: TextView, text: String?) {
        text?.let {
            if (text.isNotEmpty() && text.trim().isNotEmpty()) {
                view.EnableAlpha(true)
            } else {
                view.EnableAlpha(false)
            }
        }
    }

    @BindingAdapter(value = ["phoneNo", "flag"], requireAll = true)
    @JvmStatic fun enableiuyt(view: View, phoneNo: String, flag: Boolean) {
        if (flag && phoneNo.length >= 5) {
            view.EnableAlpha(true)
        } else {
            view.EnableAlpha(false)
        }
    }

    @BindingAdapter(value = ["isWishList", "isOwnerList"], requireAll = true)
    @JvmStatic fun isWishList(view: ImageView, isWishList: Boolean?, isOwnerList: Boolean?) {
        isOwnerList?.let {
            isWishList?.let {
                if(isOwnerList.not()) {
                    Log.d("werty", isWishList.toString())
                    if (isWishList) {
                        view.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        view.setImageResource(R.drawable.ic_heart_white)
                    }
                } else {
                    view.setImageResource(0)
                    view.isClickable = false
                }
            }
        }
    }

    @BindingAdapter("dynamicHeight")
    @JvmStatic fun dynamicHeight(view: View, centerView: Boolean?) {
        centerView?.let {
            if (centerView) {
                (view.parent as RelativeLayout).layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            } else {
                (view.parent as RelativeLayout).layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    @BindingAdapter(value = ["connectView", "txt"], requireAll = true)
    @JvmStatic fun setSubText(view: TextView, connectView: TextView, text: String?){
        text?.let {
            val splitText = text.split("_".toRegex())
            if(splitText[0] == ("mail") && splitText[1] == ("false")) {
                view.text = view.resources.getString(R.string.email_verifiy_header)
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.verify_email_text)
            } else if(splitText[0] == ("mail") && splitText[1] == ("true")) {
                view.text = view.resources.getString(R.string.already_verified_text)
                connectView.visibility = View.INVISIBLE
            } else if(splitText[0] == ("fb") && splitText[1] == ("true")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.disconnect)
                view.text = view.resources.getString(R.string.fb_sub_text)
            } else if(splitText[0] == ("fb") && splitText[1] == ("false")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.connect)
                view.text = view.resources.getString(R.string.fb_sub_text)
            } else if(splitText[0] == ("google") && splitText[1] == ("false")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.connect)
                view.text = view.resources.getString(R.string.google_sub_text)
            } else if(splitText[0] == ("google") && splitText[1] == ("true")) {
                connectView.visibility = View.VISIBLE
                connectView.text = view.resources.getString(R.string.disconnect)
                view.text = view.resources.getString(R.string.google_sub_text)
            }
        }
    }

    @BindingAdapter("inputType")
    @JvmStatic fun inputType(view: EditText, text: String?){
        text?.let {
            if(text.equals("single")){
                view.inputType = InputType.TYPE_CLASS_TEXT
            }else if(text.equals("multi")){
                view.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }else if(text.equals("number")){
                view.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    @BindingAdapter("txtColor")
    @JvmStatic fun txtColor(view: TextView, color: Boolean){
        color?.let {
            if(color){
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            }else{
                view.setTextColor(ContextCompat.getColor(view.context, R.color.text_color))
            }
        }
    }

    @BindingAdapter(value = ["listingImage", "isLoadingStatus", "isRetry"], requireAll = true)
    @JvmStatic fun listingImage(view: ImageView, url: String, status: Boolean, retryStatus: Boolean) {
        url?.let {
            if (url.isNotEmpty()) {
                var imageURL : String = ""
                var imagePacleholderURL : String = ""
                if(url.contains("/")){
                    imageURL = url
                    Log.d("checkLog", "url in binding: $imageURL")
                } else{
                    imageURL = Constants.imgListingMedium + url
                    imagePacleholderURL = Constants.imgListingSmall + url
                    Log.d("checkLog123", "url in binding: $imageURL")
                }
              //  Log.d("checkLog","url in binding123: $imageURL")
                GlideApp.with(view.context)
                            .load(imageURL)
                            .thumbnail(GlideApp.with(view.context)
                                    .load(imagePacleholderURL)
                                    .thumbnail(GlideApp.with(view.context)
                                            .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                            .transform(RoundedCorners(12)))
                                    .transform(BlurTransformation(25, 3), CenterCrop(), RoundedCorners(12))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .transform(CenterCrop(), RoundedCorners(12))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(view)
                if(status)
                    view.alpha = 0.5F
                else
                    view.alpha = 1.0F
            } else {
                loadBg(view)
            }
        } ?: loadBg(view)
    }

    @BindingAdapter("nextStyle")
    @JvmStatic fun nextStyle(view: TextView, uploadBG: Boolean){
        uploadBG?.let {
            if(it){
                view.text = view.resources.getString(R.string.next)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_green)
                val nextArrow = ContextCompat.getDrawable(view.context, R.drawable.ic_next_arrow)
                //nextArrow!!.setTint(ContextCompat.getColor(view.context,R.color.white))
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, nextArrow, null)
            }else{
                view.text = view.resources.getString(R.string.skip_for_now)
                view.setTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_transparent)
                val nextArrow = ContextCompat.getDrawable(view.context, R.drawable.ic_next_arrow_green)
//                nextArrow!!.setTint(ContextCompat.getColor(view.context,R.color.colorPrimary))
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, nextArrow, null)
            }
        }
    }

    @BindingAdapter("textVisible")
    @JvmStatic fun textVisible(view: TextView, status: String){
        status.let {
            if(status.equals("active")){
                view.text = view.resources.getString(R.string.continuee)
                view.visibility = View.VISIBLE
            }else if(status.equals("completed")){
                view.text = view.resources.getString(R.string.change)
                view.visibility = View.VISIBLE
            }else{
                view.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("listImages")
    @JvmStatic fun listImages(view: ImageView, url: String?) {
        url?.let {
            if (url.isNotEmpty()) {
                GlideApp.with(view.context)
                        .load(Constants.imgListingMedium + url)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .thumbnail(GlideApp.with(view.context)
                                .load(Constants.imgListingSmall + url)
                                .thumbnail(GlideApp.with(view.context)
                                        .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                                        .transform(RoundedCorners(12)))
                                .transform(BlurTransformation(25, 3), CenterCrop(), RoundedCorners(12))
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
            } else {
                GlideApp.with(view.context)
                        .load(ContextCompat.getDrawable(view.context, R.drawable.camera))
                        .transform(RoundedCorners(12))
                        .into(view)
            }
        } ?: GlideApp.with(view.context)
                .load(ColorDrawable(ContextCompat.getColor(view.context, R.color.search_text_color)))
                .transform(RoundedCorners(12))
                .into(view)
    }

    @BindingAdapter(value = ["isRetryStatus", "isLoadingStatus"], requireAll = true)
    @JvmStatic fun coverPhotoVisibility(view: View, retryStatus: Boolean, loadingStatus: Boolean){
        if(loadingStatus || retryStatus) {
            view.visibility = View.GONE
        } else if(!loadingStatus || !retryStatus) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("drawableImage")
    @JvmStatic fun drawableImage(view: ImageView, image: Int){
        image?.let{
            view.setImageResource(image)
        }
    }

    @BindingAdapter("wishListVisible")
    @JvmStatic fun wishListVisible(view: ImageView, visible: String){
        visible.let {
            if(visible.contains("create")){
                view.visible()
            }else{
                view.gone()
            }
        }
    }

    @BindingAdapter("checkInternet")
    @JvmStatic fun checkInternet(view: TextView, checkNet: StepOneViewModel){
        view.onClick {
            checkNet?.let {
                if (NetworkUtils.isNetworkConnected(view.context)){
                    checkNet.checkValidation()
                }else{
                    checkNet.showError()
                }
            }
        }

    }

    @BindingAdapter(value = ["disReviewsCount", "disDisplayCount"])
    @JvmStatic fun disReviewsCount(view: TextView, disReviewsCount: Int?, disDisplayCount: Int?){
        disReviewsCount?.let {
            view.text = disDisplayCount.toString() + " / ${view.context.getString(R.string.reviews)} ("+ "$disReviewsCount)"
        }
    }


    @BindingAdapter(value = ["isDefault", "isVerified"], requireAll = false)
    @JvmStatic fun payoutText(view: Button, isVerified: Boolean, isDefault: Boolean){
//        if(!isDefault){
//            if(isVerified){
//                view.text=view.resources.getString(R.string.set_default)
//            }else{
//                view.text=view.resources.getString(R.string.verified)
//            }
//        }else{
//            view.text=view.resources.getString(R.string.default_txt)
//        }

        isVerified.let {
            if(!isVerified){
                view.text=view.resources.getString(R.string.verified)
            }else{
                isDefault.let {
                    if(isDefault){
                        view.text=view.resources.getString(R.string.default_txt)
                    }
                    else{
                        view.text=view.resources.getString(R.string.set_default)
                    }
                }
            }
        }

    }

}
