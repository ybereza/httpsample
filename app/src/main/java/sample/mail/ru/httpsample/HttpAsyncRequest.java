package sample.mail.ru.httpsample;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.Arrays;


public class HttpAsyncRequest extends AsyncTask<String, Integer, String> {
    private WeakReference<MainActivity.RequestListener> mListener;
    protected int mErrorStringID;

    public HttpAsyncRequest(MainActivity.RequestListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected String doInBackground(String... params) {
        if (params != null && params.length > 0) {
            HttpRequest request = new HttpRequest(params[0]);
            int status = request.makeRequest();

            if (status == HttpRequest.REQUEST_OK) {
                return StringUtils.join(Arrays.asList(request.getHeaders(), request.getContent()), "\n");
            }
            else if (status == HttpRequest.REQUEST_REDIRECT) {
                return doInBackground(request.getRedirectURL());
            }
            else {
                mErrorStringID = request.getErrorStringId();
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
