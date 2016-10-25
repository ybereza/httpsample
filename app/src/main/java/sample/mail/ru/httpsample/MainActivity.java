package sample.mail.ru.httpsample;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.CharacterPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MainFragment())
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettings() {
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .addToBackStack("SETTINGS")
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
        else {
            super.onBackPressed();
        }
    }

    protected static String readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[16384];

        while ((read = is.read(data, 0, data.length)) != -1) {
            outputStream.write(data, 0, read);
        }

        outputStream.flush();
        return outputStream.toString("utf-8");
    }

    public static class MainFragment extends Fragment implements RequestListener {
        private Button mOverHttp;
        private Button mOverSocket;
        private TextView mOutput;

        private AsyncTask<?, ?, ?> mRequestTask;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.content_main, null);
            mOverHttp = (Button)v.findViewById(R.id.http_button);
            mOverHttp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRequestTask != null) {
                        mRequestTask.cancel(true);
                    }
                    mOutput.setText("");
                    mRequestTask = new HttpRequest(MainFragment.this).execute(
                            isOverHTTPS() ? "https://mail.ru" : "http://mail.ru"
                    );
                }
            });
            mOverSocket = (Button)v.findViewById(R.id.socket_button);
            mOverSocket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRequestTask != null) {
                        mRequestTask.cancel(true);
                    }
                    mOutput.setText("");
                    mRequestTask = new SocketRequest(MainFragment.this).execute(
                            "mail.ru", isOverHTTPS() ? "443" : "80"
                    );
                }
            });

            mOutput = (TextView)v.findViewById(R.id.output);

            return v;
        }

        @Override
        public void onStart() {
            super.onStart();
            mOverHttp.setText(getString(isOverHTTPS() ? R.string.https : R.string.http));
        }

        @Override
        public void onStop() {
            super.onStop();
            mRequestTask.cancel(true);
        }

        @Override
        public void onRequestResult(String result) {
            mOutput.setText(result);
        }

        @Override
        public void onRequestError(int errorStringID) {
            mOutput.setText(errorStringID);
        }

        private boolean isOverHTTPS() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            return prefs.getBoolean("pref_https", true);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    private interface RequestListener {
        void onRequestResult(String result);
        void onRequestError(int errorStringID);
    }

    private static class HttpRequest extends AsyncTask<String, Integer, String> {
        private WeakReference<RequestListener> mListener;
        private int mErrorStringID;

        public HttpRequest(RequestListener listener) {
            mListener = new WeakReference<RequestListener>(listener);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params != null && params.length > 0) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.setInstanceFollowRedirects(true);
                        Map<String, List<String>> headers = connection.getHeaderFields();
                        InputStream is = new BufferedInputStream(connection.getInputStream());
                        String html = readInputStream(is);
                        StringBuilder builder = new StringBuilder();
                        for (Map.Entry<String, List<String>> values : headers.entrySet()) {
                            builder.append(values.getKey()).append(": ")
                                    .append(StringUtils.join(values.getValue(), ";"))
                                    .append('\n');
                        }
                        builder.append('\n');
                        builder.append(html);
                        is.close();
                        return builder.toString();
                    }
                    catch (SocketTimeoutException ex) {
                        ex.printStackTrace();
                        mErrorStringID = R.string.error_timeout;
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                        mErrorStringID = R.string.error_connecting;
                    }
                }
                catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    mErrorStringID = R.string.incorrect_url;
                }
            }
            else {
                mErrorStringID = R.string.too_few_params;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!isCancelled()) {
                RequestListener l = mListener.get();
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

    private static class SocketRequest extends AsyncTask<String, Integer, String> {
        private WeakReference<RequestListener> mListener;
        private int mErrorStringID;

        public SocketRequest(RequestListener listener) {
            mListener = new WeakReference<RequestListener>(listener);
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

        protected byte[] getRequestString(String address) {
            String request = "GET / HTTP/1.1\r\nHost: " + address + "\r\nConnection: Close\r\n\r\n";
            return request.getBytes(Charset.forName("UTF-8"));
        }

        protected String performDefaultConnection(String address, int port) throws UnknownHostException, IOException {
            Socket socket = new Socket(address, port);
            InputStream is = new BufferedInputStream(socket.getInputStream());
            OutputStream os = socket.getOutputStream();
            os.write(getRequestString(address));
            os.flush();
            String output = readInputStream(is);
            is.close();
            os.close();
            socket.close();
            return output;
        }

        protected String performSecureConnection(String address, int port) throws UnknownHostException, IOException {
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
            String output = readInputStream(is);
            is.close();
            os.close();
            socket.close();
            return output;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!isCancelled()) {
                RequestListener l = mListener.get();
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
}
