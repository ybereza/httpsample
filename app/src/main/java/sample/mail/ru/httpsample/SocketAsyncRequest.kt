package sample.mail.ru.httpsample

import android.os.AsyncTask

import java.io.BufferedInputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


class SocketAsyncRequest(private val listener: (Pair<String, Int>) -> Unit) : AsyncTask<String, Int, Pair<String, Int>>() {

    override fun doInBackground(vararg params: String): Pair<String, Int> {
        if (params.size > 1) {
            val address = params[0]
            val port = Integer.valueOf(params[1])
            return try {
                when(port) {
                    80 -> performDefaultConnection(address, port)
                    443 -> performSecureConnection(address, port)
                    else -> throw IOException()
                }
            } catch (ex: UnknownHostException) {
                ex.printStackTrace()
                Pair("", R.string.unknown_host)
            } catch (ex: IOException) {
                ex.printStackTrace()
                Pair("", R.string.error_connecting)
            }

        }
        return Pair("", R.string.too_few_params)
    }

    private fun getRequestString(address: String): ByteArray {
        val request = "GET / HTTP/1.1\r\nHost: $address\r\nConnection: Close\r\n\r\n"
        return request.toByteArray(Charsets.UTF_8)
    }

    private fun performDefaultConnection(address: String, port: Int): Pair<String, Int> {
        val socket = Socket(address, port)
        val os = socket.getOutputStream()
        os.write(getRequestString(address))
        os.flush()
        val output = socket.inputStream.asString()
        os.close()
        socket.close()
        return Pair(output, 0)
    }

    private fun performSecureConnection(address: String, port: Int): Pair<String, Int> {
        val sf = SSLSocketFactory.getDefault()
        val socket = sf.createSocket(address, port) as SSLSocket
        val hv = HttpsURLConnection.getDefaultHostnameVerifier()
        val s = socket.session
        if (!hv.verify(address, s)) {
            throw SSLHandshakeException("Expected " + address + ", " + "found " + s.peerPrincipal)
        }
        val os = socket.outputStream
        os.write(getRequestString(address))
        os.flush()
        val output = socket.inputStream.asString()
        os.close()
        socket.close()
        return Pair(output, 0)
    }

    override fun onPostExecute(data: Pair<String, Int>) {
        if (!isCancelled) {
            listener(data)
        }
    }
}