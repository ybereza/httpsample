package sample.mail.ru.httpsample.request


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import sample.mail.ru.httpsample.R

class DownloadJSONAsync(url : String) : HttpAsyncRequest(url) {
    override suspend fun execute(): Pair<String, Int> = withContext(Dispatchers.IO) {
        if (url.isNotEmpty()) {
            val request = HttpRequest(url)
            val status = request.makeRequest()

            if (status == HttpRequest.REQUEST_OK) {
                val jtk = JSONTokener(request.content)
                try {
                    val jsonObject = jtk.nextValue() as JSONObject
                    val builder = StringBuilder()
                    builder.append("url : ").append(jsonObject.getString("url"))
                    Pair(builder.toString(), 0)
                } catch (ex: JSONException) {
                    Pair("", R.string.incorrect_json)
                }
            } else {
                Pair("", request.errorStringId)
            }
        }
        else Pair("", R.string.too_few_params)
    }
}
