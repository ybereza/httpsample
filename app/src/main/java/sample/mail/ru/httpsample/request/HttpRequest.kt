package sample.mail.ru.httpsample.request

import sample.mail.ru.httpsample.R
import sample.mail.ru.httpsample.asString
import sample.mail.ru.httpsample.join
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL


class HttpRequest(private val mURL: String) {
    var content = ""
        private set
    var headers = ""
        private set
    var redirectURL = ""
        private set

    var errorStringId: Int = 0
        private set

    fun makeRequest(): Int {
        try {
            val url = URL(mURL)
            val connection: HttpURLConnection
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.instanceFollowRedirects = true
                val responseCode = connection.responseCode

                fun readContent() : Int {
                    val headers = connection.headerFields
                    content = connection.inputStream.asString()

                    val builder = StringBuilder()
                    for ((key, value) in headers) {
                        if (key != null) {
                            builder.append(key).append(": ")
                                    .append("".join(value, ";"))
                                    .append('\n')
                        } else {
                            builder.append(value).append('\n')
                        }
                    }
                    this.headers = builder.toString()
                    return REQUEST_OK
                }

                fun getRedirectURI() : Int {
                    redirectURL = connection.getHeaderField("Location")
                    return REQUEST_REDIRECT
                }

                return when(responseCode) {
                    HttpURLConnection.HTTP_OK -> readContent()
                    HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> getRedirectURI()
                    else -> {
                        errorStringId = R.string.server_error
                        REQUEST_ERROR
                    }
                }
            } catch (ex: SocketTimeoutException) {
                ex.printStackTrace()
                errorStringId = R.string.error_timeout
            } catch (ex: IOException) {
                ex.printStackTrace()
                errorStringId = R.string.error_connecting
            }

        } catch (ex: MalformedURLException) {
            ex.printStackTrace()
            errorStringId = R.string.incorrect_url
        }

        return REQUEST_ERROR
    }

    companion object {
        const val REQUEST_OK = 0
        const val REQUEST_REDIRECT = 1
        const val REQUEST_ERROR = 2
    }
}
