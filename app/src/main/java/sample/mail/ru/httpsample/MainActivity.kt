package sample.mail.ru.httpsample

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.*
import sample.mail.ru.httpsample.request.DownloadJSONAsync
import sample.mail.ru.httpsample.request.HttpAsyncRequest
import sample.mail.ru.httpsample.request.RequestExecutor
import sample.mail.ru.httpsample.request.SocketAsyncRequest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, MainFragment())
                    .commit()
        }
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

    class MainFragment : android.support.v4.app.Fragment(), View.OnClickListener {
        private lateinit var overHttp: Button
        private lateinit var overSocket: Button
        private lateinit var downloadJSON: Button

        private lateinit var output: TextView

        private var job : Job? = null

        private val overSSL: Boolean
            get() {
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                return prefs.getBoolean("pref_https", true)
            }

        override fun onStart() {
            super.onStart()
            overHttp.text = getString(if (overSSL) R.string.https else R.string.http)
        }

        override fun onStop() {
            job?.cancel()
            super.onStop()
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.content_main, container, false)

            overHttp = v.findViewById<View>(R.id.http_button) as Button
            overSocket = v.findViewById<View>(R.id.socket_button) as Button
            downloadJSON = v.findViewById<View>(R.id.download_json) as Button
            output = v.findViewById<View>(R.id.output) as TextView

            overHttp.setOnClickListener(this)
            overSocket.setOnClickListener(this)
            downloadJSON.setOnClickListener(this)

            return v
        }

        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.http_button -> requestToView(HttpAsyncRequest(if (overSSL) Consts.HTTPS + Consts.DEFAULT_URL else Consts.HTTP + Consts.DEFAULT_URL))
                R.id.socket_button -> requestToView(SocketAsyncRequest(Consts.DEFAULT_URL, if (overSSL) 443 else 80))
                R.id.download_json -> requestToView(DownloadJSONAsync(Consts.JSON_URL))
            }
        }

        private fun requestToView(task : RequestExecutor) {
            output.text = ""
            job = GlobalScope.launch(Dispatchers.Main) {
                val result = async {
                    task.execute()
                }
                showRequestResult(result.await())
            }
        }

        private fun showRequestResult(request : Pair<String, Int>) {
            output.text = if (request.second > 0) getString(request.second) else request.first
        }
    }

    class SettingsFragment : android.support.v7.preference.PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, param: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

    }
}
