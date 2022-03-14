package com.rentall.radicalstart.ui.host.step_two

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rentall.radicalstart.Constants
import net.gotev.uploadservice.*
import timber.log.Timber


class SingleUploadBroadcastReceiver : IntentService("SingleUploadBroadcastReceiver"){

    var uploadImages = ArrayList<String>()
    var token = String()
    var listID = String()
    var uploadAsync = UploadingImagesAsync()

    override fun onHandleIntent(intent: Intent?) {
            val bundle = intent!!.getBundleExtra("params")
            uploadImages = bundle?.getSerializable("uploadImages") as ArrayList<String>
            Log.d("uploadImages",uploadImages.toString())
            token = bundle.getString("accessToken") ?: ""
            listID = bundle.getString("listID") ?: ""
             uploadMultipart(uploadImages.get(0),0)
            //uploadAsync.execute(uploadImages[0],token,listID)
    }

    fun uploadMultipart(uri: String,pos:Int){
        val config = UploadNotificationConfig()
        config.completed.autoClear = true
        config.error.autoClear = true
        config.cancelled.autoClear = true
        MultipartUploadRequest(this, Constants.WEBSITE + Constants.uploadListPhoto)
                .addFileToUpload(uri, "file")
                .addHeader("auth", token)
                .addParameter("listId", listID)
                .setNotificationConfig(config)
                .setMaxRetries(1)
                .setDelegate(object : UploadStatusDelegate {

                    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                        Log.d("called", "called")
                        sendBroadCastMessage(pos,"cancelled")
                    }

                    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
                        Timber.tag("ProfileFragment").d("upload - onProgress - %d", uploadInfo?.progressPercent)
                    }

                    override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
                        Timber.tag("Service").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
                        sendBroadCastMessage(pos,"error")
                    }

                    override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
                        Log.d("checkLog123", "Completed")
                        val convertedObject = Gson().fromJson(serverResponse?.bodyAsString, JsonObject::class.java)
                        if (convertedObject.get("status").asInt == 200) {
                            val array = convertedObject.get("files").asJsonArray
                            val obj = array.get(0).asJsonObject
                            Timber.tag("fileName").d(obj.get("filename").asString)
                            Log.d("checkLog", "Completed")
                        }
                        sendBroadCastMessage(pos,"success")
                        for(i in 1 until  uploadImages.size){
                            uploadMultipart(uploadImages.get(i),i)
                        }
                    }
                }).startUpload()
    }

    fun sendBroadCastMessage(pos:Int,message: String){
        val localIntent = Intent("my.own.broadcast")
        localIntent.putExtra("result",message )
        localIntent.putExtra("pos",pos)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    override fun onCreate() {
        super.onCreate()
    }

}

