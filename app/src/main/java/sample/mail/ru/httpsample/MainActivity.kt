package sample.mail.ru.httpsample

import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, MainFragment())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            openSettings()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openSettings() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, SettingsFragment())
                .addToBackStack("SETTINGS")
                .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    class MainFragment : android.support.v4.app.Fragment() {
        private lateinit var overHttp: Button
        private lateinit var overSocket: Button
        private lateinit var downloadJSON: Button

        private lateinit var mOutput: TextView

        private var mRequestTask: AsyncTask<*, *, *>? = null

        private val overSSL: Boolean
            get() {
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                return prefs.getBoolean("pref_https", true)
            }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.content_main, container, false)
            overHttp = v.findViewById<View>(R.id.http_button) as Button
            overHttp.setOnClickListener {
                mRequestTask?.cancel(true)
                mOutput.text = ""
                mRequestTask = HttpAsyncRequest(::onRequestResult).execute(
                        if (overSSL) "https://mail.ru" else "http://mail.ru"
                )
            }
            overSocket = v.findViewById<View>(R.id.socket_button) as Button
            overSocket.setOnClickListener {
                mRequestTask?.cancel(true)
                mOutput.text = ""
                mRequestTask = SocketAsyncRequest(::onRequestResult).execute(
                        "mail.ru", if (overSSL) "443" else "80"
                )
            }
            downloadJSON = v.findViewById<View>(R.id.download_json) as Button
            downloadJSON.setOnClickListener {
                mRequestTask?.cancel(true)
                mOutput.text = ""
                mRequestTask = DownloadJSONAsync(::onRequestResult).execute(DownloadJSONAsync.JSON_URL)
            }

            mOutput = v.findViewById<View>(R.id.output) as TextView

            return v
        }

        override fun onStart() {
            super.onStart()
            overHttp.text = getString(if (overSSL) R.string.https else R.string.http)
        }

        override fun onStop() {
            super.onStop()
            mRequestTask?.cancel(true)
        }

        fun onRequestResult(request : Pair<String, Int>) {
            mOutput.text = if (request.second > 0) getString(request.second) else request.first
        }
    }

    class SettingsFragment : android.support.v7.preference.PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, param: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

    }
}
