package com.rentall.radicalstart.ui.host.step_two

import android.os.AsyncTask
import android.util.Log
import com.rentall.radicalstart.Constants
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import org.json.JSONObject
import java.net.URL


class UploadingImagesAsync : AsyncTask<String,Int,String>() {

    var jsonobject: JSONObject? = null
    var Json = ""
    var status:Int? = null
    var existingFileName = ""
    var TAG = "UploadingImagesAsync"

    override fun doInBackground(vararg params: String?): String {
        var conn: HttpURLConnection? = null
        var dos: DataOutputStream? = null
        var inStream: DataInputStream? = null
        val builder = StringBuilder()
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        val buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024
        val urlString = Constants.WEBSITE + Constants.uploadListPhoto
        try {
            existingFileName = params[0]!!
            Log.e("existingFileName", " existingFileName=$existingFileName")
            val fileInputStream = FileInputStream(File(existingFileName))
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn!!.setDoInput(true)
            conn!!.setDoOutput(true)
            conn!!.setUseCaches(false)
            conn!!.setRequestMethod("POST")
            conn!!.setRequestProperty("Connection", "Keep-Alive")
            conn!!.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=$boundary")
            dos = DataOutputStream(conn!!.getOutputStream())

            dos!!.writeBytes(twoHyphens + boundary + lineEnd)
            dos!!.writeBytes("Content-Disposition: form-data; name=\"auth\"$lineEnd")
            dos!!.writeBytes(lineEnd)
            dos!!.writeBytes(params[1]!!)
            dos!!.writeBytes(lineEnd)

            dos!!.writeBytes(twoHyphens + boundary + lineEnd)
            dos!!.writeBytes("Content-Disposition: form-data; name=\"listId\"$lineEnd")
            dos!!.writeBytes(lineEnd)
            dos!!.writeBytes(params[2]!!)
            dos!!.writeBytes(lineEnd)

            dos!!.writeBytes(twoHyphens + boundary + lineEnd)
            dos!!.writeBytes("Content-Disposition: form-data; name=\"file\"$lineEnd")
            dos!!.writeBytes(lineEnd)

            Log.e(TAG, "MediaPlayer-Headers are written")
            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            buffer = ByteArray(bufferSize)

            Log.v(TAG, "buffer=$buffer")

            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            while (bytesRead > 0) {
                dos!!.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                Log.v(TAG, "bytesRead=$bytesRead")
            }
            dos!!.writeBytes(lineEnd)
            dos!!.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            val inn = BufferedReader(InputStreamReader(
                    conn!!.getInputStream()))
            var inputLine: String
            Log.v("in", "" + inn)
            inputLine = inn.readLine()
            while (inputLine != null) {
                builder.append(inputLine)
                inputLine = inn.readLine()
            }

            Log.e(TAG, "MediaPlayer-File is written")
            fileInputStream.close()
            Json = builder.toString()
            dos!!.flush()
            dos!!.close()

        } catch (ex: MalformedURLException) {
            Log.e(TAG, "MediaPlayer-error: " + ex.message, ex)
        } catch (ioe: IOException) {
            Log.e(TAG, "MediaPlayer-error: " + ioe.message, ioe)
        }

        try {
            inStream = DataInputStream(conn!!.getInputStream())
            var str: String
            str = inStream!!.readLine()
            while (str != null) {
                Log.e(TAG, "MediaPlayer-Server Response$str")
                str = inStream!!.readLine()
            }
            inStream!!.close()
        } catch (ioex: IOException) {
            Log.e(TAG, "MediaPlayer-error: " + ioex.message, ioex)
        }
        val jsonobject =  JSONObject(Json);
        Log.e(TAG, "json=" + Json);
        status = jsonobject.getString("status").toInt()
        if (status == 200) {
            return "success"
        }
        return "failed"
    }
}