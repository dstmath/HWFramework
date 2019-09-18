package android.media;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HwCustMediaPlayerImpl extends HwCustMediaPlayer {
    private static final boolean DEBUG = false;
    private static boolean HWFLOW = ((Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) ? HWLOGW_E : DEBUG);
    private static final boolean HWLOGW_E = true;
    private static final int MAX_DNS_WAIT_TIME_MILLISECOND = 2000;
    private static final String SETTING_KEY_PROXY_HOST = "rtsp_proxy_host";
    private static final String SETTING_KEY_PROXY_PORT = "rtsp_proxy_port";
    private static final String TAG = "HwCustMediaPlayerImpl";
    private static final String TAG_FLOW = "HwCustMediaPlayerImpl_FLOW";

    private class URLConvertThread implements Runnable {
        private String mIPAddr;
        private boolean mIsStop;
        private String mURL;

        private URLConvertThread() {
            this.mIPAddr = "ERROR";
            this.mIsStop = HwCustMediaPlayerImpl.DEBUG;
        }

        public void setURL(String url) {
            this.mURL = url;
        }

        public String getIPAddr() {
            return this.mIPAddr;
        }

        public boolean isStop() {
            return this.mIsStop;
        }

        public void run() {
            this.mIsStop = HwCustMediaPlayerImpl.DEBUG;
            try {
                this.mIPAddr = InetAddress.getByName(this.mURL).getHostAddress();
            } catch (Exception ee) {
                Log.e(HwCustMediaPlayerImpl.TAG, "DNS convert error, bypass the proxy!  " + ee.getMessage());
            } catch (Throwable th) {
                this.mIsStop = HwCustMediaPlayerImpl.HWLOGW_E;
                throw th;
            }
            this.mIsStop = HwCustMediaPlayerImpl.HWLOGW_E;
        }
    }

    private boolean isIpAddrValidate(String ipAddress) {
        return Pattern.compile("((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})").matcher(ipAddress).matches();
    }

    private boolean isPortValidate(String port) {
        return Pattern.compile("(\\d){1,5}").matcher(port).matches();
    }

    private void fillHeader(Map<String, String> headers, String key, String value) {
        if (value == null || "".equals(value.trim()) || headers == null) {
            Log.w(TAG, "fillHeader: cannot fill key=" + key + ", value=" + value);
            return;
        }
        headers.put(key, value);
    }

    private boolean isConnectToNetWorkType(Context context, int type) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getType() != type) {
            return DEBUG;
        }
        return HWLOGW_E;
    }

    private Map<String, String> setProxyHeaders(Context context, Uri uri, Map<String, String> headers) {
        if (isConnectToNetWorkType(context, 1)) {
            if (HWFLOW) {
                Log.i(TAG_FLOW, "Bypass proxyHeaders for wifi-connected");
            }
        } else if ("rtsp".equals(uri.getScheme().toLowerCase())) {
            String httpProxyHost = Settings.System.getString(context.getContentResolver(), SETTING_KEY_PROXY_HOST);
            String httpProxyPort = Settings.System.getString(context.getContentResolver(), SETTING_KEY_PROXY_PORT);
            httpProxyHost.trim();
            httpProxyPort.trim();
            if (!isIpAddrValidate(httpProxyHost)) {
                URLConvertThread urlConvertThread = new URLConvertThread();
                Thread thread = new Thread(urlConvertThread);
                urlConvertThread.setURL(httpProxyHost);
                thread.start();
                int lAlltime = 0;
                int lThisTurnWaitTime = 50;
                while (lAlltime < MAX_DNS_WAIT_TIME_MILLISECOND) {
                    try {
                        Thread.sleep((long) lThisTurnWaitTime);
                        if (urlConvertThread.isStop()) {
                            break;
                        }
                        lAlltime += lThisTurnWaitTime;
                        lThisTurnWaitTime = 100;
                    } catch (InterruptedException e) {
                    }
                }
                httpProxyHost = urlConvertThread.getIPAddr();
            }
            if (!isIpAddrValidate(httpProxyHost) || !isPortValidate(httpProxyPort)) {
                Log.e(TAG, "Bypass proxyHeaders because address or port invalidate!");
            } else if (!"0.0.0.0".equals(httpProxyHost)) {
                fillHeader(headers, "hw-use-proxy", httpProxyHost + ":" + httpProxyPort);
            } else if (HWFLOW) {
                Log.i(TAG_FLOW, "Bypass proxyHeaders for 0.0.0.0");
            }
        } else if (HWFLOW) {
            Log.i(TAG_FLOW, "Bypass proxyHeaders only for rtsp protocal");
        }
        return headers;
    }

    private Map<String, String> setNetWorkTypeHeaders(Context context, Map<String, String> headers) {
        String networkType;
        String str;
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == 1) {
            str = "WIFI";
        } else if (networkInfo == null || networkInfo.getType() != 6) {
            int dataNetworkType = ((TelephonyManager) context.getSystemService("phone")).getDataNetworkType();
            if (dataNetworkType != 13) {
                switch (dataNetworkType) {
                    case 5:
                        networkType = "EVDO";
                        break;
                    case 6:
                        networkType = "DORA";
                        break;
                    case 7:
                        networkType = "IS2000";
                        break;
                    default:
                        str = "";
                        break;
                }
            } else {
                networkType = "LTE";
            }
            fillHeader(headers, "x-network-type", networkType);
            return headers;
        } else {
            str = "WIMAX";
        }
        networkType = str;
        fillHeader(headers, "x-network-type", networkType);
        return headers;
    }

    public Map<String, String> setStreamingMediaHeaders(Context context, Uri uri, Map<String, String> headers) {
        if (!((!"237".equals(SystemProperties.get("ro.config.hw_opta", "0")) || !"840".equals(SystemProperties.get("ro.config.hw_optb", "0"))) ? DEBUG : HWLOGW_E)) {
            return headers;
        }
        if (headers == null) {
            headers = new HashMap<>();
        }
        try {
            return setNetWorkTypeHeaders(context, setProxyHeaders(context, uri, headers));
        } catch (Exception ee) {
            Log.e(TAG, "Proxy or networkType  function  error, bypass the header settings!  errMsg:" + ee.getMessage());
            return headers;
        }
    }
}
