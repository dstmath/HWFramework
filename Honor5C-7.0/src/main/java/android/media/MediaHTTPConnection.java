package android.media;

import android.content.IntentFilter;
import android.media.IMediaHTTPConnection.Stub;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.rms.iaware.AwareConstant.Database;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MediaHTTPConnection extends Stub {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect;
    private boolean mAllowCrossProtocolRedirect;
    private HttpURLConnection mConnection;
    private List<String> mCookies;
    private long mCurrentOffset;
    private Map<String, String> mHeaders;
    private InputStream mInputStream;
    private boolean mIsCookieUpdated;
    private long mNativeContext;
    private long mTotalSize;
    private URL mURL;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaHTTPConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaHTTPConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaHTTPConnection.<clinit>():void");
    }

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    public MediaHTTPConnection() {
        this.mCurrentOffset = -1;
        this.mURL = null;
        this.mHeaders = null;
        this.mConnection = null;
        this.mTotalSize = -1;
        this.mInputStream = null;
        this.mCookies = null;
        this.mIsCookieUpdated = false;
        this.mAllowCrossDomainRedirect = true;
        this.mAllowCrossProtocolRedirect = true;
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
        }
        native_setup();
    }

    public IBinder connect(String uri, String headers) {
        try {
            disconnect();
            this.mAllowCrossDomainRedirect = true;
            this.mURL = new URL(uri);
            this.mHeaders = convertHeaderStringToMap(headers);
            return native_getIMemory();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean parseBoolean(String val) {
        boolean z = true;
        try {
            if (Long.parseLong(val) == 0) {
                z = false;
            }
            return z;
        } catch (NumberFormatException e) {
            if (!"true".equalsIgnoreCase(val)) {
                z = "yes".equalsIgnoreCase(val);
            }
            return z;
        }
    }

    private boolean filterOutInternalHeaders(String key, String val) {
        if ("android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            this.mAllowCrossDomainRedirect = parseBoolean(val);
            this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        } else if (!"Cookie".equalsIgnoreCase(key) || !this.mIsCookieUpdated) {
            return false;
        } else {
            Log.d(TAG, "filterOutInternalHeaders: Cookie");
        }
        return true;
    }

    private Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map = new HashMap();
        for (String pair : headers.split("\r\n")) {
            int colonPos = pair.indexOf(":");
            if (colonPos >= 0) {
                String key = pair.substring(0, colonPos);
                String val = pair.substring(colonPos + 1);
                if (!filterOutInternalHeaders(key, val)) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    public void disconnect() {
        teardownConnection();
        this.mHeaders = null;
        this.mURL = null;
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            this.mInputStream = null;
            this.mConnection.disconnect();
            this.mConnection = null;
            this.mCurrentOffset = -1;
        }
    }

    private static final boolean isLocalHost(URL url) {
        if (url == null) {
            return false;
        }
        String host = url.getHost();
        if (host == null) {
            return false;
        }
        try {
            return host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) || NetworkUtils.numericToInetAddress(host).isLoopbackAddress();
        } catch (IllegalArgumentException e) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void seekTo(long offset) throws IOException {
        teardownConnection();
        int redirectCount = 0;
        try {
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            while (true) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                this.mConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                if (this.mIsCookieUpdated) {
                    for (String cookie : this.mCookies) {
                        this.mConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                    }
                }
                if (offset > 0) {
                    this.mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                int response = this.mConnection.getResponseCode();
                if (response != 300 && response != 301 && response != 302 && response != 303 && response != HTTP_TEMP_REDIRECT) {
                    break;
                }
                redirectCount++;
                if (redirectCount <= MAX_REDIRECTS) {
                    String method = this.mConnection.getRequestMethod();
                    if (response == HTTP_TEMP_REDIRECT) {
                        if (!method.equals("GET")) {
                            if (!method.equals("HEAD")) {
                                break;
                            }
                        }
                    }
                    String location = this.mConnection.getHeaderField("Location");
                    if (location != null) {
                        URL url2 = new URL(this.mURL, location);
                        if (!url2.getProtocol().equals(IntentFilter.SCHEME_HTTPS) && !url2.getProtocol().equals(IntentFilter.SCHEME_HTTP)) {
                            break;
                        }
                        boolean sameProtocol = this.mURL.getProtocol().equals(url2.getProtocol());
                        if (!this.mAllowCrossProtocolRedirect && !sameProtocol) {
                            break;
                        }
                        boolean sameHost = this.mURL.getHost().equals(url2.getHost());
                        if (!this.mAllowCrossDomainRedirect && !sameHost) {
                            break;
                        } else if (response != HTTP_TEMP_REDIRECT) {
                            this.mURL = url2;
                        }
                    } else {
                        break;
                    }
                }
                break;
            }
            throw new NoRouteToHostException("Invalid redirect");
        } catch (IOException e) {
            this.mTotalSize = -1;
            this.mInputStream = null;
            this.mConnection = null;
            this.mCurrentOffset = -1;
            throw e;
        }
    }

    public int readAt(long offset, int size) {
        return native_readAt(offset, size);
    }

    private int readAt(long offset, byte[] data, int size) {
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset) {
                seekTo(offset);
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (NoRouteToHostException e2) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e2);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (UnknownServiceException e3) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e3);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (IOException e4) {
            return -1;
        } catch (Exception e5) {
            return -1;
        }
    }

    public long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    public String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return Database.UNKNOWN_MIME_TYPE;
            }
        }
        return this.mConnection.getContentType();
    }

    public String getUri() {
        return this.mURL.toString();
    }

    protected void finalize() {
        native_finalize();
    }
}
