package com.rentall.radicalstart.services

import android.content.Context
import net.gotev.uploadservice.ServerResponse
import net.gotev.uploadservice.UploadInfo
import net.gotev.uploadservice.UploadServiceBroadcastReceiver
import timber.log.Timber


class PhotoUploadReceiver : UploadServiceBroadcastReceiver()  {

    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
        Timber.tag("ProfileFragment").d("upload - onCancelled - %d", uploadInfo?.progressPercent)
    }

    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
        Timber.tag("ProfileFragment123").d("upload - onProgress - %d", uploadInfo?.progressPercent)
    }

    override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
        Timber.tag("ProfileFragment").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
    }

    override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
        Timber.tag("ProfileFragment12332").d("upload - onComplete - %d, response - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString)
    }

//    private var mDelegate: WeakReference<UploadStatusDelegate>
//    private var mUploadID: String? = null

    /*fun PhotoUploadReceiver(delegate: UploadStatusDelegate) {
        mDelegate = WeakReference(delegate)
    }*/

    /*fun setUploadID(uploadID: String) {
        mUploadID = uploadID
    }*/

   /* override fun shouldAcceptEventFrom(uploadInfo: UploadInfo?): Boolean {
        return mUploadID != null && uploadInfo!!.uploadId == mUploadID
    }

    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
        if (mDelegate.get() != null) {
            mDelegate.get()!!.onProgress(context, uploadInfo)
        }
    }

    override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: Exception?) {
        if (mDelegate.get() != null) {
            mDelegate.get()!!.onError(context, uploadInfo, serverResponse, exception)
        }
    }

    override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
        if (mDelegate.get() != null) {
            mDelegate.get()!!.onCompleted(context, uploadInfo, serverResponse)
        }
    }

    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
        if (mDelegate.get() != null) {
            mDelegate.get()!!.onCancelled(context, uploadInfo)
        }
    }*/
}