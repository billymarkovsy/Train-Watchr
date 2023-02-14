package com.trainwatch.models.mbta

import android.util.Log
import com.trainwatch.Constants
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel
import java.nio.charset.StandardCharsets

private const val TAG: String = Constants.MBTA_REQUEST_CALLBACK_TAG
class MBTATransitRequestCallback : UrlRequest.Callback() {

    private val receivedBytes: ByteArrayOutputStream = ByteArrayOutputStream()
    private val channel: WritableByteChannel = Channels.newChannel(receivedBytes)

    fun getData(): String{
        return String(receivedBytes.toByteArray(), StandardCharsets.UTF_8)
    }

    override fun onRedirectReceived(request: UrlRequest?, info: UrlResponseInfo?, newLocationUrl: String?) {
        val httpStatus = info?.httpStatusCode
        if(httpStatus == null || httpStatus >= 500)
            Log.e(TAG, "HTTP Response or server unavailable")
        else if(httpStatus >= 400)
            Log.e(TAG,"Bad request w/ response $httpStatus")

        Log.i(TAG, "Redirecting request to $newLocationUrl")
        request?.followRedirect()
    }

    override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
        val httpStatus = info?.httpStatusCode
        if(httpStatus == null || httpStatus >= 500)
            Log.e(TAG, "HTTP Response or server unavailable")
        else if(httpStatus >= 400)
            Log.e(TAG,"Bad request w/ response $httpStatus")

        Log.i(TAG, "Reading response")
        val buffer = ByteBuffer.allocateDirect(8000)
        request?.read(buffer)
    }

    override fun onReadCompleted(request: UrlRequest?, info: UrlResponseInfo?, byteBuffer: ByteBuffer?) {
        try{
            Log.i(TAG, "Writing chunk to output byte array")
            byteBuffer?.flip()
            channel.write(byteBuffer)
            Log.i(TAG, "Clearing buffer, reading next chunk")
        }catch (e: IOException){
            Log.e(TAG, "Error writing bytes")
        }finally {
            byteBuffer?.clear()
        }
        request?.read(byteBuffer)
    }

    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
        receivedBytes.close()
        channel.close()
        Log.i(TAG, "Successfully read request")
    }

    override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
        receivedBytes.close()
        channel.close()
        Log.e(TAG, "Response read unsuccessfully")
        throw error!!
    }
}