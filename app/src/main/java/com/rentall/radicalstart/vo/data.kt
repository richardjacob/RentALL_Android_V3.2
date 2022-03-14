package com.rentall.radicalstart.vo

import android.os.Parcelable
import androidx.databinding.ObservableField
import com.rentall.radicalstart.ui.auth.AuthViewModel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate


data class Listing(
        val image: String,
        val type: String,
        val title: String,
        val price: String,
        val ratingStar: Int,
        val rating: String,
        val amount : String = "" ,
        val lat: Double = 0.0,
        val long: Double = 0.0,
        var selected: Boolean = false ,
        val id: Int = 0,
        val selectedCurrency: String = "",
        val currency: String = "",
        val base: String = "",
        val bookingType: String? = "",
        var isWishList: Boolean,
        var isOwnerList: Boolean = false,
        var per_night : String = " per night"
)

data class Languages(
        var languagesText: String,
        val languagesValue: String,
        var isChecked: Boolean = false
)

@Parcelize
data class ListingInitData(
        var title: String = "",
        var photo: ArrayList<String> = ArrayList(),
        var id: Int = 0,
        var roomType: String = "",
        var ratingStarCount: Int? = 0,
        var reviewCount: Int? = 0,
        var price: String = "",
        var guestCount: Int = 0,
        var startDate: String = "0",
        var endDate: String = "0",
        val selectedCurrency: String = "",
        val currencyBase: String = "",
        val currencyRate: String = "",
        var hostName: String = "",
        var ownerPhoto: String? = "",
        var location: String = "",
        var mapImage: String = "",
        var bookingType: String = "",
        var minGuestCount: Int? = 0,
        var maxGuestCount: Int? = 0,
        var isWishList: Boolean = false,
        var isPreview : Boolean = false


) : Parcelable

@Parcelize
data class InboxMsgInitData(
        val threadId: Int,
        val guestName: String,
        val guestPicture: String?,
        val guestId: String,
        val hostName: String,
        val hostPicture: String?,
        val hostId: String,
        val senderID: Int?,
        val receiverID: Int?,
        val listID: Int?
) : Parcelable


data class ProfileDetails(
        var userName: String?,
        var createdAt: String?,
        var picture: String?,
        var email: String?,
        var emailVerification: Boolean? = false,
        var idVerification: Boolean? = false,
        var googleVerification: Boolean? = false,
        var fbVerification: Boolean? = false,
        var phoneVerification: Boolean? = false,
        var userType: String?,
        var addedList: Boolean? = false
)

data class ListDetailsStep3(
        var noticePeriod: String?,
        var noticeFrom: String?,
        var noticeTo: String?,
        var availableDate: String?,
        var cancellationPolicy: String?,
        var minStay: String?,
        var maxStay: String?,
        var basePrice: Double?,
        var cleaningPrice: Double?,
        var currency: String?,
        var weekDiscount: Int?,
        var monthDiscount: Int?
)

data class ListDetailsStep2(
        var listPhotos: HashSet<String>?,
        var coverPhoto: Int?,
        var title: String?,
        var desc: String?
)

data class ListPhotos(
        var name: String?,
        var id: Int?,
        var uploadStatus: Boolean?,
        var retryStatus: Boolean?
)

@Parcelize
data class BillingDetails(
        var checkIn :String,
        var checkOut :String,
        var basePrice :Double,
        var nights :Int,
        var cleaningPrice :Double,
        var guestServiceFee :Double,
        var discount :Double?,
        var discountLabel :String?,
        var total :Double,
        var houseRule : ArrayList<String>,
        var image :String,
        var title: String,
        val cancellation: String,
        val cancellationContent: String,
        val guest: Int,
        val hostServiceFee: Double,
        val listId: Int,
        val currency: String,
        val bookingType: String,
        var isProfilePresent: Boolean,
        var averagePrice: Double,
        var priceForDays: Double,
        var specialPricing: String,
        var isSpecialPriceAssigned: Boolean
) : Parcelable

data class UserDetails(
        val firstName: String,
        val lastName: String,
        val gender: String,
        val dateOfBirth: String,
        val phoneNumber: String,
        val preferredLanguage: String,
        val preferredCurrency: String,
        val createdAt: String,
        val picture: String
)

data class SavedList(
        val id: Int,
        val title: String,
        var img: String?,
        var wishListCount: Int? = 0,
        var isWishList: Boolean,
        var isRetry: String,
        var progress: AuthViewModel.LottieProgress = AuthViewModel.LottieProgress.NORMAL
)

data class PersonCount(
        var itemId: Int?,
        var itemName: String?,
        val startValue: Int?,
        val endValue : Int?,
        var updatedCount: Int? = 0
)

data class BecomeHostStep1(
        var placeType: String?,
        var guestCapacity: String?,
        var houseType: String?,
        var guestSpace: String?,
        var roomCapacity: String?,
        var yesNoOptions: String?,
        var totalGuestCount: Int?,
        var bedroomCount: String?,
        var bedType: String?,
        var bedTypes: String?,
        var beds:Int?,
        var bedCount: Int?,
        var bathroomCount: Double?,
        var bathroomSpace: String?,
        var country: String?,
        var street: String?,
        var buildingName: String?,
        var state: String?,
        var zipcode: String?,
        var lat: Double?,
        var lng: Double?,
        var isMapTouched: Boolean?,
        var amentiesSelected: ArrayList<Int?>,
        var safetyAmentiesSelected: ArrayList<Int?>,
        var guestSpacesSelected: ArrayList<Int?>
)

data class Payout(
        val id: Int,
        val name: String,
        val method: Int,
        val currency : String,
        var isDefault: Boolean,
        val payEmail: String?,
        val pinDigit: Int?,
        var isVerified: Boolean
)

data class ManageList(
        var id: Int,
        var title: String?,
        var imageName: String?,
        var isPublish: Boolean?,
        var isReady: Boolean?,
        var step1Status: String?,
        var step2Status: String?,
        var step3Status: String?,
        var location: String?,
        var roomType: String?,
        var created: String?,
        var listApprovelStatus : String?
)

data class PhotoList(
        var refId: String,
        var id: Int?,
        var name: String?,
        var type: String?,
        var isCover: Int?,
        var isRetry: Boolean = false,
        var isLoading: Boolean = false
)

data class DefaultListing(
        val id: Int,
        val title: String,
        val personCapacity: Int,
        val beds: Int,
        val bookingType: String,
        val coverPhoto: Int?,
        val reviewsCount: Int?,
        val reviewsStarRating: Int?,
        var wishListStatus: Boolean?,
        val isListOwner: Boolean?,
        val listPhotoName: String?,
        val roomType: String?,
        val basePrice: Double,
        val currency: String,
        var wishListGroupCount: Int? = 0
)

data class SearchListing(
        val id: Int,
        val title: String,
        val personCapacity: Int,
        val beds: Int,
        val bookingType: String,
        val coverPhoto: Int?,
        val reviewsCount: Int?,
        val reviewsStarRating: Int?,
        var wishListStatus: Boolean?,
        val isListOwner: Boolean?,
        val listPhotoName: String?,
        val roomType: String?,
        val basePrice: Double,
        val currency: String,
        var wishListGroupCount: Int = 0,
        val listPhotos: ArrayList<Photo> = ArrayList(),
        val lat: Double = 0.0,
        val lng: Double = 0.0
)

data class PreApproved(
        val id: Int = 0,
        val type: String = "",
        var isApproved: Boolean = false,
        val createdAt: Long,
        var endDate: String = "",
        var startDate: String = "",
        var personCapacity: Int,
        var content: String = "",
        var reservationID: Int = 0
)

data class Photo(
        val id: Int,
        val name: String
)

data class Dates(
        val blockedDates: String,
        val reservationID: Int?,
        val calendarStatus: String?,
        val isSpecialPrice: Int?
)

data class SpecialDate(
        val date: LocalDate,
        val isSpecialPrice: Double?
)

data class PaymentType(
        val typeText : String,
        val typeInt : Int
)