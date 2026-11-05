package cl.duoc.clorocontrol.data.sync

import cl.duoc.clorocontrol.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CloroControlHttpApi(
    private val baseUrl: String = BuildConfig.API_BASE_URL
) {
    fun postMeasurement(payload: JSONObject) = request("measurements", "POST", payload)
    fun postAction(payload: JSONObject) = request("actions", "POST", payload)
    fun updateAction(clientId: String, payload: JSONObject) =
        request("actions/${URLEncoder.encode(clientId, Charsets.UTF_8.name())}", "PUT", payload)

    fun fetchMeasurements(): JSONArray = getArray("measurements")
    fun fetchActions(): JSONArray = getArray("actions")

    private fun getArray(path: String): JSONArray {
        val response = request(path, "GET", null)
        if (response.first !in 200..299) throw SyncHttpException(response.first, response.second)
        return JSONArray(response.second.ifBlank { "[]" })
    }

    private fun request(path: String, method: String, body: JSONObject?): Pair<Int, String> {
        if (baseUrl.isBlank()) throw IOException("No se configuró API_BASE_URL.")
        val normalizedBase = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        val connection = (URL(normalizedBase + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return code to response
        } finally {
            connection.disconnect()
        }
    }
}

class SyncHttpException(
    val statusCode: Int,
    message: String
) : IOException("API respondió HTTP $statusCode: $message")
