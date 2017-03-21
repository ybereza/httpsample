package sample.mail.ru.httpsample;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MainActivity extends AppCompatActivity {

    interface RequestListener {
        void onRequestResult(String result);
        void onRequestError(int errorStringID);
    }

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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

    public static class MainFragment extends Fragment implements RequestListener {
        private Button mOverHttp;
        private Button mOverSocket;
        private Button mDownloadJSON;

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
                    mRequestTask = new HttpAsyncRequest(MainFragment.this).execute(
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
                    mRequestTask = new SocketAsyncRequest(MainFragment.this).execute(
                            "mail.ru", isOverHTTPS() ? "443" : "80"
                    );
                }
            });
            mDownloadJSON = (Button)v.findViewById(R.id.download_json);
            mDownloadJSON.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRequestTask != null) {
                        mRequestTask.cancel(true);
                    }
                    mOutput.setText("");
                    mRequestTask = new DownloadJSONAsync(MainFragment.this).execute(DownloadJSONAsync.JSON_URL);
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
            if (mRequestTask != null) {
                mRequestTask.cancel(true);
            }
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
}
