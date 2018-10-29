package sample.mail.ru.httpsample


import org.json.JSONArray
import org.json.JSONException
import org.json.JSONTokener

class DownloadJSONAsync(listener: (Pair<String, Int>) -> Unit) : HttpAsyncRequest(listener) {

    override fun doInBackground(vararg params: String): Pair<String, Int> {
        if (params.isNotEmpty()) {
            val request = HttpRequest(params[0])
            val status = request.makeRequest()

            return if (status == HttpRequest.REQUEST_OK) {
                val jtk = JSONTokener(request.content)
                try {
                    val jsonArray = jtk.nextValue() as JSONArray
                    val builder = StringBuilder()
                    for (i in 0 until jsonArray.length()) {
                        builder.append(jsonArray.getString(i)).append("\n")
                    }
                    Pair(builder.toString(), 0)
                } catch (ex: JSONException) {
                    Pair("", R.string.incorrect_json)
                }
            } else {
                Pair("", request.errorStringId)
            }
        }
        return Pair("", R.string.too_few_params)
    }

    companion object {
        val JSON_URL = "http://188.166.49.215/tech/imglist.json"
    }
}
