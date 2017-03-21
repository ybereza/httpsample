package sample.mail.ru.httpsample;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Copyrigh Mail.ru Games (c) 2015
 * Created by y.bereza.
 */

public class HttpRequest {
    public static final int REQUEST_OK = 0;
    public static final int REQUEST_REDIRECT = 1;
    public static final int REQUEST_ERROR = 2;

    private final String mURL;
    private String mContent;
    private String mHeaders;
    private String mRedirectURL;

    private int mErrorStringId;

    public HttpRequest(String url) {
        mURL = url;
    }

    public int makeRequest() {
        try {
            URL url = new URL(mURL);
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setInstanceFollowRedirects(true);
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK ) {
                    Map<String, List<String>> headers = connection.getHeaderFields();
                    InputStream is = new BufferedInputStream(connection.getInputStream());
                    mContent = StringUtils.readInputStream(is);

                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, List<String>> values : headers.entrySet()) {
                        if (values.getKey() != null) {
                            builder.append(values.getKey()).append(": ")
                                    .append(StringUtils.join(values.getValue(), ";"))
                                    .append('\n');
                        }
                        else {
                            builder.append(values.getValue()).append('\n');
                        }
                    }
                    mHeaders = builder.toString();
                    is.close();
                    return REQUEST_OK;
                }
                else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    mRedirectURL = connection.getHeaderField("Location");
                    return REQUEST_REDIRECT;
                }
                else {
                    mErrorStringId = R.string.server_error;
                }
            }
            catch (SocketTimeoutException ex) {
                ex.printStackTrace();
                mErrorStringId = R.string.error_timeout;
            }
            catch (IOException ex) {
                ex.printStackTrace();
                mErrorStringId = R.string.error_connecting;
            }
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
            mErrorStringId = R.string.incorrect_url;
        }
        return REQUEST_ERROR;
    }

    public String getHeaders() {
        return mHeaders;
    }

    public String getContent() {
        return mContent;
    }

    public String getRedirectURL() {
        return mRedirectURL;
    }

    public int getErrorStringId() {
        return mErrorStringId;
    }
}
