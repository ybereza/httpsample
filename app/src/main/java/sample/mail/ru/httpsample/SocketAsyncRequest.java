package sample.mail.ru.httpsample;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class SocketAsyncRequest extends AsyncTask<String, Integer, String> {
    private WeakReference<MainActivity.RequestListener> mListener;
    private int mErrorStringID;

    public SocketAsyncRequest(MainActivity.RequestListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected String doInBackground(String... params) {
        if (params != null && params.length > 1) {
            String  address = params[0];
            Integer port = Integer.valueOf(params[1]);
            try {
                return port == 80 ? performDefaultConnection(address, port) :
                        performSecureConnection(address, port);
            }
            catch (UnknownHostException ex) {
                ex.printStackTrace();
                mErrorStringID = R.string.unknown_host;
            }
            catch (IOException ex) {
                ex.printStackTrace();
                mErrorStringID = R.string.error_connecting;
            }
        }
        else {
            mErrorStringID = R.string.too_few_params;
        }
        return null;
    }

    private byte[] getRequestString(String address) {
        String request = "GET / HTTP/1.1\r\nHost: " + address + "\r\nConnection: Close\r\n\r\n";
        return request.getBytes(Charset.forName("UTF-8"));
    }

    private String performDefaultConnection(String address, int port) throws UnknownHostException, IOException {
        Socket socket = new Socket(address, port);
        InputStream is = new BufferedInputStream(socket.getInputStream());
        OutputStream os = socket.getOutputStream();
        os.write(getRequestString(address));
        os.flush();
        String output = StringUtils.readInputStream(is);
        is.close();
        os.close();
        socket.close();
        return output;
    }

    private String performSecureConnection(String address, int port) throws UnknownHostException, IOException {
        SocketFactory sf = SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) sf.createSocket(address, port);
        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        SSLSession s = socket.getSession();
        if (!hv.verify(address, s)) {
            throw new SSLHandshakeException("Expected " + address + ", " +  "found " + s.getPeerPrincipal());
        }
        InputStream is = new BufferedInputStream(socket.getInputStream());
        OutputStream os = socket.getOutputStream();
        os.write(getRequestString(address));
        os.flush();
        String output = StringUtils.readInputStream(is);
        is.close();
        os.close();
        socket.close();
        return output;
    }

    @Override
    protected void onPostExecute(String s) {
        if (!isCancelled()) {
            MainActivity.RequestListener l = mListener.get();
            if (l != null) {
                if (s != null) {
                    l.onRequestResult(s);
                }
                else {
                    l.onRequestError(mErrorStringID);
                }
            }
        }
    }
}