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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HwCustMediaPlayerImpl extends HwCustMediaPlayer {
    private static final boolean DEBUG = false;
    private static final boolean HWFLOW = ((Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) ? HWLOGW_E : false);
    private static final boolean HWLOGW_E = true;
    private static final int MAX_DNS_WAIT_TIME_MILLISECOND = 2000;
    private static final String SETTING_KEY_PROXY_HOST = "rtsp_proxy_host";
    private static final String SETTING_KEY_PROXY_PORT = "rtsp_proxy_port";
    private static final String TAG = "HwCustMediaPlayerImpl";
    private static final String TAG_FLOW = "HwCustMediaPlayerImpl_FLOW";

    /* access modifiers changed from: private */
    public class URLConvertThread implements Runnable {
        private String ipAddr;
        private boolean isStop;
        private String url;

        private URLConvertThread() {
            this.ipAddr = "ERROR";
            this.isStop = false;
        }

        public void setURL(String url2) {
            this.url = url2;
        }

        public String getIPAddr() {
            return this.ipAddr;
        }

        public boolean isStop() {
            return this.isStop;
        }

        public void run() {
            this.isStop = false;
            try {
                this.ipAddr = InetAddress.getByName(this.url).getHostAddress();
            } catch (UnknownHostException e) {
                Log.e(HwCustMediaPlayerImpl.TAG, "DNS convert UnknownHostException, bypass the proxy!");
            } catch (Exception e2) {
                Log.e(HwCustMediaPlayerImpl.TAG, "DNS convert error, bypass the proxy!");
            } catch (Throwable th) {
                this.isStop = HwCustMediaPlayerImpl.HWLOGW_E;
                throw th;
            }
            this.isStop = HwCustMediaPlayerImpl.HWLOGW_E;
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
            Log.w(TAG, "fillHeader: fill error");
        } else {
            headers.put(key, value);
        }
    }

    private boolean isConnectToNetWorkType(Context context, int type) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getType() != type) {
            return false;
        }
        return HWLOGW_E;
    }

    private Map<String, String> setProxyHeaders(Context context, Uri uri, Map<String, String> headers) {
        if (isConnectToNetWorkType(context, 1)) {
            if (HWFLOW) {
                Log.i(TAG_FLOW, "Bypass proxyHeaders for wifi-connected");
            }
        } else if ("rtsp".equalsIgnoreCase(uri.getScheme())) {
            String httpProxyHost = Settings.System.getString(context.getContentResolver(), SETTING_KEY_PROXY_HOST).trim();
            String httpProxyPort = Settings.System.getString(context.getContentResolver(), SETTING_KEY_PROXY_PORT).trim();
            if (!isIpAddrValidate(httpProxyHost)) {
                URLConvertThread urlConvertThread = new URLConvertThread();
                Thread thread = new Thread(urlConvertThread);
                urlConvertThread.setURL(httpProxyHost);
                thread.start();
                int allTime = 0;
                int thisTurnWaitTime = 50;
                while (allTime < MAX_DNS_WAIT_TIME_MILLISECOND) {
                    try {
                        Thread.sleep((long) thisTurnWaitTime);
                        if (urlConvertThread.isStop()) {
                            break;
                        }
                        allTime += thisTurnWaitTime;
                        thisTurnWaitTime = 100;
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
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == 1) {
            networkType = "WIFI";
        } else if (networkInfo == null || networkInfo.getType() != 6) {
            int dataNetworkType = ((TelephonyManager) context.getSystemService("phone")).getDataNetworkType();
            if (dataNetworkType == 5) {
                networkType = "EVDO";
            } else if (dataNetworkType == 6) {
                networkType = "DORA";
            } else if (dataNetworkType == 7) {
                networkType = "IS2000";
            } else if (dataNetworkType != 13) {
                networkType = "";
            } else {
                networkType = "LTE";
            }
        } else {
            networkType = "WIMAX";
        }
        fillHeader(headers, "x-network-type", networkType);
        return headers;
    }

    public Map<String, String> setStreamingMediaHeaders(Context context, Uri uri, Map<String, String> headers) {
        if (!((!"237".equals(SystemProperties.get("ro.config.hw_opta", "0")) || !"840".equals(SystemProperties.get("ro.config.hw_optb", "0"))) ? false : HWLOGW_E)) {
            return headers;
        }
        if (headers == null) {
            headers = new HashMap();
        }
        try {
            headers = setProxyHeaders(context, uri, headers);
            return setNetWorkTypeHeaders(context, headers);
        } catch (SecurityException e) {
            Log.e(TAG, "Set proxy or networkType function SecurityException!");
            return headers;
        } catch (Exception e2) {
            Log.e(TAG, "Proxy or networkType  function  error, bypass the header settings!");
            return headers;
        }
    }
}
