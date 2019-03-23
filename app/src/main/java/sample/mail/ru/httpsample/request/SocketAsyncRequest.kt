package sample.mail.ru.httpsample.request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sample.mail.ru.httpsample.R
import sample.mail.ru.httpsample.asString

import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


class SocketAsyncRequest (val address: String, val port : Int): RequestExecutor {
    override suspend fun execute(): Pair<String, Int> = withContext(Dispatchers.IO) {
        try {
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
}