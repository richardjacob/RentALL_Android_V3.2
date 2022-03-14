package com.rentall.radicalstart.data.remote

import javax.inject.Inject
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import javax.inject.Singleton


@Singleton
class ApiHeader @Inject
constructor(val protectedApiHeader: ProtectedApiHeader) {

    class ProtectedApiHeader(@field:SerializedName("access_token")
                             var accessToken: String?)

}