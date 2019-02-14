package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.ApiResponseAdapter
import com.everlytic.android.pushnotificationsdk.use
import org.json.JSONObject
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.util.*

internal class EverlyticHttp(installUrl: String, apiUsername: String, apiKey: String) {

    val baseUrl = "https://$installUrl/api/3.0/"
    var authenticator = EverlyticApiAuthenticator(apiUsername, apiKey)

    fun get(url: String, responseHandler: ResponseHandler) {
        performHttpConnection(
            "get",
            baseUrl + url,
            null,
            responseHandler = responseHandler
        )
    }

    fun post(url: String, jsonBodyData: String, responseHandler: ResponseHandler) {
        performHttpConnection(
            "post",
            baseUrl + url,
            jsonBodyData,
            responseHandler = responseHandler
        )
    }

    private fun performHttpConnection(
        method: String,
        url: String,
        jsonBodyData: String?,
        timeout: Int = TIMEOUT,
        responseHandler: ResponseHandler
    ) {
        logd("performHttpConnection")
        val conn = URL(url).openConnection() as HttpURLConnection
        var jsonResult: String? = null
        conn.use {
            logd("Inside use statement")
            try {
                logd("Inside try catch")
                useCaches = false
                connectTimeout = timeout
                readTimeout = timeout
                doOutput = true
                requestMethod = method.toUpperCase()
                applyConnectionHeaders()

                if (jsonBodyData.isNullOrBlank() == false) {
                    logd("==> jsonBodyData not null")
                    doInput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                    val bytes = jsonBodyData.toByteArray()
                    setFixedLengthStreamingMode(bytes.size)
                    logd("==> writing jsonBodyData")
                    outputStream.write(bytes)
                }

                val responseCode = responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    logd("==> success response code")
                    val istream = inputStream
                    val scanner = Scanner(istream, "UTF-8")
                    logd("==> reading response")
                    jsonResult = readStringFromScanner(scanner)
                    scanner.close()

                    logd("==> handling success response")
                    handleSuccessfulResponse(responseHandler, jsonResult)
                } else {
                    logd("==> response had error")
                    val stream = if (errorStream != null) errorStream else inputStream

                    stream?.let {
                        val scanner = Scanner(it, "UTF-8")
                        logd("==> reading error stream")
                        jsonResult = readStringFromScanner(scanner)
                        scanner.close()
                    }

                    responseHandler.onFailure(responseCode, jsonResult, null)
                }

            } catch (throwable: Throwable) {
                if (throwable is ConnectException || throwable is UnknownHostException) {
                    responseHandler.onFailure(responseCode, null, throwable)
                }
            }
        }
    }

    private fun handleSuccessfulResponse(
        responseHandler: ResponseHandler,
        jsonResult: String?
    ) {

        val response = ApiResponseAdapter.fromJson(JSONObject(jsonResult))

        if (response.result == "error") {
            responseHandler.onFailure(400, jsonResult, null)
        } else {
            responseHandler.onSuccess(response)
        }

    }

    private fun HttpURLConnection.applyConnectionHeaders() {
        logd("==> applyConnectionHeaders:0")
        authenticator.authenticate(this)
        logd("==> applyConnectionHeaders:1")
        addRequestProperty("X-EV-SDK-Version-Name", BuildFacade.getBuildConfigVersionName())
        logd("==> applyConnectionHeaders:2")
        addRequestProperty("X-EV-SDK-Version-Code", BuildFacade.getBuildConfigVersionCode().toString())
        logd("==> applyConnectionHeaders:3")
    }

    private fun readStringFromScanner(scanner: Scanner): String? {
        return if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
    }

    interface ResponseHandler {
        fun onSuccess(response: ApiResponse?)
        fun onFailure(code: Int, response: String?, throwable: Throwable?)
    }

    companion object {
        const val TIMEOUT = 60_000
    }
}