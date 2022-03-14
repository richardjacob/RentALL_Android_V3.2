package com.rentall.radicalstart.ui.listing

import com.rentall.radicalstart.ui.listing.pricebreakdown.PriceBreakDownFragment
import com.rentall.radicalstart.ui.listing.photo_story.PhotoStoryFragment
import com.rentall.radicalstart.ui.listing.amenities.AmenitiesFragment
import com.rentall.radicalstart.ui.listing.cancellation.CancellationFragment
import com.rentall.radicalstart.ui.listing.contact_host.ContactHostFragment
import com.rentall.radicalstart.ui.listing.desc.DescriptionFragment
import com.rentall.radicalstart.ui.listing.guest.GuestFragment
import com.rentall.radicalstart.ui.listing.map.MapFragment
import com.rentall.radicalstart.ui.listing.review.ReviewFragment
import com.rentall.radicalstart.ui.saved.SavedBotomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ListingDetailsFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideAmenitiesFragmentFactory(): AmenitiesFragment

    @ContributesAndroidInjector
    abstract fun provideDescriptionFragmentFactory(): DescriptionFragment

    @ContributesAndroidInjector
    abstract fun provideMapFragmentFactory(): MapFragment

    @ContributesAndroidInjector
    abstract fun provideReviewFragmentFactory(): ReviewFragment

    @ContributesAndroidInjector
    abstract fun provideCancellationFragmentFactory(): CancellationFragment

    @ContributesAndroidInjector
    abstract fun providePhotoStoryFragmentFactory(): PhotoStoryFragment

    @ContributesAndroidInjector
    abstract fun providePriceBreakDownFragmentFactory(): PriceBreakDownFragment

    @ContributesAndroidInjector
    abstract fun provideContactHostFragmentFactory(): ContactHostFragment

    @ContributesAndroidInjector
    abstract fun provideGuestFragmentFactory(): GuestFragment

    @ContributesAndroidInjector
    abstract fun provideSavedBottomFactory(): SavedBotomSheet
}