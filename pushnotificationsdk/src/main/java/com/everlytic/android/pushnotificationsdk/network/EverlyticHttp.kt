package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.logw
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.ApiResponseAdapter
import com.everlytic.android.pushnotificationsdk.use
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.util.*

internal class EverlyticHttp(installUrl: String, apiUsername: String, apiKey: String) {

    private val baseUrl = "https://$installUrl/api/3.0/"
    private var authenticator = EverlyticApiAuthenticator(apiUsername, apiKey)

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
        logd("::performHttpConnection()")
        val connection = URL(url).openConnection() as HttpURLConnection

        val callbackThreads = mutableListOf<Thread>()
        val connectionThread = Thread({
            connection.use {
                callbackThreads += performRequest(method, jsonBodyData, responseHandler, timeout)
            }
        }, CONN_THREAD_NAME)

        try {
            connectionThread.start()
//            connectionThread.join(THREAD_TIMEOUT)
//            if (connectionThread.state != Thread.State.TERMINATED) {
//                connectionThread.interrupt()
//            }

//            callbackThreads.firstOrNull()?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun HttpURLConnection.performRequest(
        method: String,
        jsonBodyData: String?,
        responseHandler: ResponseHandler,
        timeout: Int
    ): Thread {
        var jsonResult: String? = null
        lateinit var callbackThread: Thread
        var httpResponseCode = -1

        try {
            useCaches = false
            connectTimeout = timeout
            readTimeout = timeout
            doOutput = true
            requestMethod = method.toUpperCase()
            applyConnectionHeaders()

            if (jsonBodyData.isNullOrBlank() == false) {
                logd("jsonBodyData not null")
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                val bytes = jsonBodyData.toByteArray()
                setFixedLengthStreamingMode(bytes.size)
                logd("Writing data...")
                outputStream.write(bytes)
            }

            httpResponseCode = responseCode

            if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                logd("Request Successful")
                val istream = inputStream
                val scanner = Scanner(istream, "UTF-8")
                logd("Reading response")
                jsonResult = readStringFromScanner(scanner)
                scanner.close()

                callbackThread = handleSuccessfulResponse(responseHandler, jsonResult)
            } else {
                logd("Response had error")
                val stream = if (errorStream != null) errorStream else inputStream

                stream?.let {
                    val scanner = Scanner(it, "UTF-8")
                    logd("reading error stream")
                    jsonResult = readStringFromScanner(scanner)
                    scanner.close()
                }

                callbackThread = handleFailureResponse(responseHandler, httpResponseCode, jsonResult, null)
            }

        } catch (throwable: UnknownHostException) {
            logw("::performHttpConnection() conn::catch ${throwable.message}", throwable)
            callbackThread = handleFailureResponse(responseHandler, httpResponseCode, jsonResult, throwable)
        }

        return callbackThread
    }

    private fun handleSuccessfulResponse(
        responseHandler: ResponseHandler,
        jsonResult: String?
    ): Thread {
        logd("Handling success response. jsonResult=$jsonResult")

        return Thread {
            if (jsonResult.isNullOrBlank()) {
                responseHandler.onFailure(500, null, null)
            } else {
                val response = ApiResponseAdapter.fromJson(JSONObject(jsonResult))

                if (response.result == "error") {
                    responseHandler.onFailure(400, jsonResult, null)
                } else {
                    responseHandler.onSuccess(response)
                }
            }

        }.also {
            it.start()
        }
    }

    private fun handleFailureResponse(
        responseHandler: ResponseHandler,
        responseCode: Int,
        jsonResult: String?,
        throwable: Throwable?
    ): Thread {
        logd("::handleFailureResponse()")
        return Thread {
            responseHandler.onFailure(responseCode, jsonResult, throwable)
        }.also {
            it.start()
        }
    }

    private fun HttpURLConnection.applyConnectionHeaders() {
        authenticator.authenticate(this)
        addRequestProperty("X-EV-SDK-Version-Name", BuildFacade.getBuildConfigVersionName())
        addRequestProperty("X-EV-SDK-Version-Code", BuildFacade.getBuildConfigVersionCode().toString())
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
        const val CONN_THREAD_NAME = "EV_HTTP_CONNECTION"
        const val THREAD_TIMEOUT = TIMEOUT + 5000L
    }
}