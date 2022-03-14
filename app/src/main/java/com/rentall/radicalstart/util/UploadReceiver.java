package com.rentall.radicalstart.util;


import android.content.Context;
import android.util.Log;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.lang.ref.WeakReference;

public class UploadReceiver extends UploadServiceBroadcastReceiver {

    private WeakReference<UploadStatusDelegate> mDelegate;
    private String mUploadID = null;

    public UploadReceiver() {
    }

    public UploadReceiver(UploadStatusDelegate delegate) {
        mDelegate = new WeakReference<>(delegate);
    }

    public void setUploadID(String uploadID) {
        mUploadID = uploadID;
    }

   /* @Override
    protected boolean shouldAcceptEventFrom(UploadInfo uploadInfo) {
        return mUploadID != null && uploadInfo.getUploadId().equals(mUploadID);
    }*/

    @Override
    public final void onProgress(Context context, UploadInfo uploadInfo) {
        if (mDelegate != null && mDelegate.get() != null) {
            mDelegate.get().onProgress(context, uploadInfo);
            Log.e("uploadInfo_OP_global", "____" + uploadInfo.getProgressPercent());
        }
    }

    @Override
    public final void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        if (mDelegate != null && mDelegate.get() != null) {
            mDelegate.get().onError(context, uploadInfo, serverResponse, exception);
        }
    }

    @Override
    public final void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        if (mDelegate != null && mDelegate.get() != null) {
            mDelegate.get().onCompleted(context, uploadInfo, serverResponse);
        }
    }

    @Override
    public final void onCancelled(Context context, UploadInfo uploadInfo) {
        if (mDelegate != null && mDelegate.get() != null) {
            mDelegate.get().onCancelled(context, uploadInfo);
            Log.e("uploadInfo_OCom_global", "____");
        }
    }
}