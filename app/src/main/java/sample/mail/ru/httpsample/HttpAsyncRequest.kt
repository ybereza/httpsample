package sample.mail.ru.httpsample

import android.os.AsyncTask

open class HttpAsyncRequest(private val listener: (Pair<String, Int>) -> Unit) : AsyncTask<String, Int, Pair<String, Int>>() {

    override fun doInBackground(vararg params: String): Pair<String, Int> {
        if (params.isNotEmpty()) {
            val request = HttpRequest(params[0])
            val status = request.makeRequest()

            return when (status) {
                HttpRequest.REQUEST_OK -> Pair("".join(listOf(request.headers, request.content), "\n"), 0)
                HttpRequest.REQUEST_REDIRECT -> doInBackground(request.redirectURL)
                else -> Pair("", request.errorStringId)
            }
        }
        return Pair("", R.string.too_few_params)
    }

    override fun onPostExecute(data : Pair<String, Int>) {
        if (!isCancelled) {
            listener(data)
        }
    }
}
