package sample.mail.ru.httpsample.request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sample.mail.ru.httpsample.R
import sample.mail.ru.httpsample.join


open class HttpAsyncRequest(val url: String) : RequestExecutor {

    override suspend fun execute(): Pair<String, Int> {
        return execute(url)
    }

    suspend fun execute(address: String): Pair<String, Int> = withContext(Dispatchers.IO) {
        if (url.isNotEmpty()) {
            val request = HttpRequest(address)
            val status = request.makeRequest()

            when (status) {
                HttpRequest.REQUEST_OK -> Pair("".join(listOf(request.headers, request.content), "\n"), 0)
                HttpRequest.REQUEST_REDIRECT -> execute(request.redirectURL)
                else -> Pair("", request.errorStringId)
            }
        }
        else Pair("", R.string.too_few_params)
    }
}
