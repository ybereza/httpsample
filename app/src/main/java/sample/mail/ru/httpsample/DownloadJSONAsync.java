package sample.mail.ru.httpsample;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

public class DownloadJSONAsync extends HttpAsyncRequest{
    public static final String JSON_URL = "http://188.166.49.215/tech/imglist.json";

    public DownloadJSONAsync(MainActivity.RequestListener listener) {
        super(listener);
    }

    @Override
    protected String doInBackground(String... params) {
        if (params != null && params.length > 0) {
            HttpRequest request = new HttpRequest(params[0]);
            int status = request.makeRequest();

            if (status == HttpRequest.REQUEST_OK) {
                JSONTokener jtk = new JSONTokener(request.getContent());
                try {
                    JSONArray jsonArray = (JSONArray)jtk.nextValue();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        builder.append(jsonArray.getString(i)).append("\n");
                    }
                    return builder.toString();
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                    mErrorStringID = R.string.incorrect_json;
                }
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
}
