package com.huawei.hwwifiproservice;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DnsResolver;
import android.net.LinkProperties;
import android.net.Network;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.metrics.ValidationProbeEvent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class NetworkMonitor {
    private static final String CAPTIVE_PORTAL_URL_HTTPS_BACKUP = "captive_portal_probe_url_https_backup";
    private static final String CAPTIVE_PORTAL_URL_HTTPS_MAIN = "captive_portal_probe_url_https_main";
    private static final String CAPTIVE_PORTAL_URL_HTTP_BACKUP = "captive_portal_probe_url_http_backup";
    private static final String CAPTIVE_PORTAL_URL_HTTP_MAIN = "captive_portal_probe_url_http_main";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.32 Safari/537.36";
    private static final int DNS_PROBE_TIMEOUT_MS = 1000;
    private static final int FAILED_CONTENT_LENGTH = 4;
    private static final String HTML_TITLE_HTTPS_EN = "https://";
    private static final String HTML_TITLE_HTTP_EN = "http://";
    private static final int HTTP_RESPONSE_OK = 200;
    public static final int HTTP_RES_CODE_BAD_REQUEST = 400;
    public static final int HTTP_RES_CODE_CLIENT_ERRORS_MAX = 499;
    private static final String KEYWORD_HTTPS_FALLBACK_URL = "captive_portal_https_fallback_url";
    public static final String KEY_NETWORK_NAME = "Network";
    public static final String KEY_WORDS_REDIRECTION = "location.replace";
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final int PROBE_TOTAL_TIMEOUT_MS = 10000;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = "HwNetworkMonitor";
    private static final int TAG_SYSTEM_PROBE = -127;
    private static final int X_HWCLOUD_REQID_LEN = 32;
    private boolean isUseHttps;
    private final CaptivePortalProbeSpec[] mCaptivePortalFallbackSpecs;
    private final URL[] mCaptivePortalFallbackUrls;
    private final URL mCaptivePortalHttpUrl;
    private final URL mCaptivePortalHttpsFallbackUrl;
    private final URL mCaptivePortalHttpsUrl;
    private final String mCaptivePortalUserAgent;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final Dependencies mDependencies;
    private long mDetectTime;
    protected boolean mIsCaptivePortalCheckEnabled;
    private LinkProperties mLinkProperties;
    private Network mNetwork;
    private int mNextFallbackUrlIndex = 0;
    private final Random mRandom;

    public NetworkMonitor(Context context) {
        this.mContext = context;
        this.mDependencies = Dependencies.DEFAULT;
        this.mCaptivePortalUserAgent = getCaptivePortalUserAgent();
        this.mCaptivePortalHttpsUrl = makeUrl(getCaptivePortalServerHttpsUrl());
        this.mCaptivePortalHttpsFallbackUrl = makeUrl(getCaptivePortalServerHttpsFallbackUrl());
        this.mCaptivePortalHttpUrl = makeUrl(getCaptivePortalServerHttpUrl());
        this.mCaptivePortalFallbackUrls = makeCaptivePortalFallbackUrls();
        this.mIsCaptivePortalCheckEnabled = isCaptivePortalCheckEnabled();
        this.isUseHttps = isUseHttpsValidation();
        this.mRandom = this.mDependencies.getRandom();
        this.mLinkProperties = new LinkProperties();
        this.mCaptivePortalFallbackSpecs = makeCaptivePortalFallbackProbeSpecs();
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public static boolean isClientErrorRespCode(int respCode) {
        return respCode >= 400 && respCode <= 499;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    private void validationLog(int probeType, Object url, String msg) {
        HwHiLog.d(TAG, false, "%{public}s %{private}s %{public}s", new Object[]{ValidationProbeEvent.getProbeName(probeType), url, msg});
    }

    private static class OneAddressPerFamilyNetwork extends Network {
        OneAddressPerFamilyNetwork(Network network) {
            super(network.getPrivateDnsBypassingCopy());
        }

        @Override // android.net.Network
        public InetAddress[] getAllByName(String host) throws UnknownHostException {
            List<InetAddress> addrs = Arrays.asList(super.getAllByName(host));
            LinkedHashMap<Class, InetAddress> addressByFamily = new LinkedHashMap<>();
            addressByFamily.put(addrs.get(0).getClass(), addrs.get(0));
            Collections.shuffle(addrs);
            for (InetAddress addr : addrs) {
                addressByFamily.put(addr.getClass(), addr);
            }
            return (InetAddress[]) addressByFamily.values().toArray(new InetAddress[addressByFamily.size()]);
        }
    }

    private boolean isCaptivePortalCheckEnabled() {
        return this.mDependencies.getSetting(this.mContext, "captive_portal_mode", 1) != 0;
    }

    private boolean isUseHttpsValidation() {
        return this.mDependencies.getSetting(this.mContext, "captive_portal_use_https", 1) == 1;
    }

    private String getCaptivePortalServerHttpsUrl() {
        return getSettingFromResource(this.mContext, 33685605, 33685621, "captive_portal_https_url");
    }

    private String getCaptivePortalServerHttpsFallbackUrl() {
        return getSettingFromResource(this.mContext, 33685604, 33685620, KEYWORD_HTTPS_FALLBACK_URL);
    }

    public final String getCaptivePortalServerHttpUrl() {
        return getSettingFromResource(this.mContext, 33685603, 33685619, "captive_portal_http_url");
    }

    private URL[] makeCaptivePortalFallbackUrls() {
        URL[] settingProviderUrls;
        try {
            String firstUrl = this.mDependencies.getSetting(this.mContext, "captive_portal_fallback_url", (String) null);
            if (!TextUtils.isEmpty(firstUrl)) {
                String otherUrls = this.mDependencies.getSetting(this.mContext, "captive_portal_other_fallback_urls", "");
                settingProviderUrls = (URL[]) convertStrings((firstUrl + "," + otherUrls).split(","), new Function() {
                    /* class com.huawei.hwwifiproservice.$$Lambda$NetworkMonitor$YEIaWAMgPWwAKzHG21jjYuYoJhc */

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return NetworkMonitor.this.makeUrl((String) obj);
                    }
                }, new URL[0]);
            } else {
                settingProviderUrls = new URL[0];
            }
            return (URL[]) getArrayConfig(settingProviderUrls, 33816590, 33816596, new Function() {
                /* class com.huawei.hwwifiproservice.$$Lambda$NetworkMonitor$YEIaWAMgPWwAKzHG21jjYuYoJhc */

                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return NetworkMonitor.this.makeUrl((String) obj);
                }
            });
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Error parsing configured fallback URLs", new Object[0]);
            return new URL[0];
        }
    }

    private CaptivePortalProbeSpec[] makeCaptivePortalFallbackProbeSpecs() {
        try {
            String settingsValue = this.mDependencies.getSetting(this.mContext, "captive_portal_fallback_probe_specs", (String) null);
            CaptivePortalProbeSpec[] emptySpecs = new CaptivePortalProbeSpec[0];
            return (CaptivePortalProbeSpec[]) getArrayConfig(TextUtils.isEmpty(settingsValue) ? emptySpecs : (CaptivePortalProbeSpec[]) CaptivePortalProbeSpec.parseCaptivePortalProbeSpecs(settingsValue).toArray(emptySpecs), 33816585, 33816595, $$Lambda$3BQoMf0hhFMxc9ZobyUHVdWpOE0.INSTANCE);
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Error parsing configured fallback probe specs", new Object[0]);
            return null;
        }
    }

    private String getSettingFromResource(Context context, int configResource, int defaultResource, String symbol) {
        String setting = "";
        if (symbol == null) {
            return setting;
        }
        char c = 65535;
        int hashCode = symbol.hashCode();
        if (hashCode != 159424546) {
            if (hashCode != 665011825) {
                if (hashCode == 1573706704 && symbol.equals(KEYWORD_HTTPS_FALLBACK_URL)) {
                    c = 2;
                }
            } else if (symbol.equals("captive_portal_https_url")) {
                c = 1;
            }
        } else if (symbol.equals("captive_portal_http_url")) {
            c = 0;
        }
        if (c == 0) {
            setting = Settings.Global.getString(this.mContext.getContentResolver(), CAPTIVE_PORTAL_URL_HTTP_MAIN);
        } else if (c == 1) {
            setting = Settings.Global.getString(this.mContext.getContentResolver(), CAPTIVE_PORTAL_URL_HTTPS_MAIN);
        } else if (c != 2) {
            HwHiLog.d(TAG, false, "unexpected symbol!", new Object[0]);
        } else {
            setting = Settings.Global.getString(this.mContext.getContentResolver(), CAPTIVE_PORTAL_URL_HTTPS_BACKUP);
        }
        if (!TextUtils.isEmpty(setting)) {
            HwHiLog.d(TAG, false, "get %{private}s from global settings ", new Object[]{symbol});
            return setting;
        }
        Resources res = context.getResources();
        String setting2 = res.getString(configResource);
        if (!TextUtils.isEmpty(setting2)) {
            return setting2;
        }
        String setting3 = this.mDependencies.getSetting(context, symbol, (String) null);
        if (!TextUtils.isEmpty(setting3)) {
            return setting3;
        }
        return res.getString(defaultResource);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.huawei.hwwifiproservice.NetworkMonitor */
    /* JADX WARN: Multi-variable type inference failed */
    private <T> T[] getArrayConfig(T[] providerValue, int configResId, int defaultResId, Function<String, T> resourceConverter) {
        Resources res = this.mContext.getResources();
        String[] configValue = res.getStringArray(configResId);
        if (configValue.length == 0) {
            if (providerValue.length > 0) {
                return providerValue;
            }
            configValue = res.getStringArray(defaultResId);
        }
        return (T[]) convertStrings(configValue, resourceConverter, Arrays.copyOf(providerValue, 0));
    }

    private <T> T[] convertStrings(String[] strings, Function<String, T> converter, T[] emptyArray) {
        ArrayList<T> convertedValues = new ArrayList<>(strings.length);
        for (String configString : strings) {
            T convertedValue = null;
            try {
                convertedValue = converter.apply(configString);
            } catch (Exception e) {
                HwHiLog.e(TAG, false, "Error parsing configuration", new Object[0]);
            }
            if (convertedValue != null) {
                convertedValues.add(convertedValue);
            }
        }
        return (T[]) convertedValues.toArray(emptyArray);
    }

    private String getCaptivePortalUserAgent() {
        return this.mDependencies.getSetting(this.mContext, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private URL nextFallbackUrl() {
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), CAPTIVE_PORTAL_URL_HTTP_BACKUP);
        if (!TextUtils.isEmpty(setting)) {
            HwHiLog.d(TAG, false, "get http fallback url from global settings", new Object[0]);
            return makeUrl(setting);
        } else if (this.mCaptivePortalFallbackUrls.length == 0) {
            return null;
        } else {
            int idx = Math.abs(this.mNextFallbackUrlIndex) % this.mCaptivePortalFallbackUrls.length;
            this.mNextFallbackUrlIndex += this.mRandom.nextInt();
            return this.mCaptivePortalFallbackUrls[idx];
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CaptivePortalProbeSpec nextFallbackSpec() {
        if (isEmpty(this.mCaptivePortalFallbackSpecs)) {
            return null;
        }
        int abs = Math.abs(this.mRandom.nextInt());
        CaptivePortalProbeSpec[] captivePortalProbeSpecArr = this.mCaptivePortalFallbackSpecs;
        return captivePortalProbeSpecArr[abs % captivePortalProbeSpecArr.length];
    }

    /* access modifiers changed from: protected */
    public CaptivePortalProbeResult isCaptivePortal() {
        CaptivePortalProbeResult result;
        if (!this.mIsCaptivePortalCheckEnabled) {
            HwHiLog.d(TAG, false, "Validation disabled.", new Object[0]);
            return CaptivePortalProbeResult.SUCCESS;
        }
        Network network = getNetworkForTypeWifi();
        if (network != null) {
            this.mNetwork = network;
            URL pacUrl = null;
            URL httpsUrl = this.mCaptivePortalHttpsUrl;
            URL httpsFallbackUrl = this.mCaptivePortalHttpsFallbackUrl;
            URL httpUrl = makeUrl(this.mCaptivePortalHttpUrl.toString() + "_" + UUID.randomUUID().toString());
            LinkProperties linkProperties = this.mConnectivityManager.getLinkProperties(this.mNetwork);
            if (linkProperties != null) {
                this.mLinkProperties = linkProperties;
            }
            ProxyInfo proxyInfo = this.mLinkProperties.getHttpProxy();
            if (proxyInfo != null && !Uri.EMPTY.equals(proxyInfo.getPacFileUrl()) && (pacUrl = makeUrl(proxyInfo.getPacFileUrl().toString())) == null) {
                return CaptivePortalProbeResult.FAILED;
            }
            if (pacUrl == null && (httpUrl == null || httpsUrl == null)) {
                return CaptivePortalProbeResult.FAILED;
            }
            long startTime = SystemClock.elapsedRealtime();
            if (pacUrl != null) {
                result = sendDnsAndHttpProbes(null, pacUrl, 3);
            } else if (this.isUseHttps) {
                result = sendParallelHttpProbes(proxyInfo, httpsUrl, httpUrl, httpsFallbackUrl);
            } else {
                result = sendDnsAndHttpProbes(proxyInfo, httpUrl, 1);
            }
            long endTime = SystemClock.elapsedRealtime();
            HwHiLog.d("HwNetworkMonitor/" + this.mNetwork.toString(), false, "isCaptivePortal: isSuccessful()=%{public}s isPortal()=%{public}s RedirectUrl=%{public}s Time=%{public}s ms", new Object[]{String.valueOf(result.isSuccessful()), String.valueOf(result.isPortal()), result.redirectUrl, String.valueOf(endTime - startTime)});
            this.mDetectTime = endTime - startTime;
            return result;
        }
        HwHiLog.e(TAG, false, "failed to get network, return", new Object[0]);
        return CaptivePortalProbeResult.FAILED;
    }

    public Bundle getProbeResponse() {
        CaptivePortalProbeResult captivePortalProbeResult = isCaptivePortal();
        int responseCode = 204;
        if (captivePortalProbeResult.isSuccessful()) {
            responseCode = 204;
        } else if (captivePortalProbeResult.isFailed()) {
            responseCode = CaptivePortalProbeResult.FAILED_CODE;
        } else if (captivePortalProbeResult.isPortal()) {
            responseCode = CaptivePortalProbeResult.PORTAL_CODE;
        } else {
            HwHiLog.d("HwNetworkMonitor/" + this.mNetwork.toString(), false, "probe response code is abnormal", new Object[0]);
        }
        Bundle result = new Bundle();
        result.putInt(HwNetworkPropertyChecker.BUNDLE_PROBE_RESPONSE_CODE, responseCode);
        result.putString(HwNetworkPropertyChecker.BUNDLE_FLAG_REDIRECT_URL, captivePortalProbeResult.redirectUrl);
        result.putString(HwNetworkPropertyChecker.BUNDLE_FLAG_USED_SERVER, this.mCaptivePortalHttpUrl.toString());
        result.putLong(HwNetworkPropertyChecker.BUNDLE_PROBE_RESPONSE_TIME, this.mDetectTime);
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CaptivePortalProbeResult sendDnsAndHttpProbes(ProxyInfo proxy, URL url, int probeType) {
        sendDnsProbe(proxy != null ? proxy.getHost() : url.getHost());
        return sendHttpProbe(url, probeType, null);
    }

    private Network getNetworkForTypeWifi() {
        Bundle bundle = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 14, null);
        if (bundle != null) {
            return (Network) bundle.getParcelable(KEY_NETWORK_NAME);
        }
        return null;
    }

    private InetAddress[] sendDnsProbeWithTimeout(String host, int timeoutMs) throws UnknownHostException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<InetAddress>> resultRef = new AtomicReference<>();
        DnsResolver.Callback<List<InetAddress>> callback = new DnsResolver.Callback<List<InetAddress>>() {
            /* class com.huawei.hwwifiproservice.NetworkMonitor.AnonymousClass1 */

            public void onAnswer(List<InetAddress> answer, int code) {
                if (code == 0) {
                    resultRef.set(answer);
                }
                latch.countDown();
            }

            @Override // android.net.DnsResolver.Callback
            public void onError(DnsResolver.DnsException e) {
                HwHiLog.d(NetworkMonitor.TAG, false, "DNS error resolving", new Object[0]);
                latch.countDown();
            }
        };
        int oldTag = TrafficStats.getAndSetThreadStatsTag(TAG_SYSTEM_PROBE);
        this.mDependencies.getDnsResolver().query(this.mNetwork, host, 0, $$Lambda$NetworkMonitor$jDiMI336ywkAeYDRFEv8gCiYEI.INSTANCE, null, callback);
        TrafficStats.setThreadStatsTag(oldTag);
        try {
            latch.await((long) timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            HwHiLog.d(TAG, false, "DNS failed with InterruptedException", new Object[0]);
        }
        List<InetAddress> results = resultRef.get();
        if (results != null && results.size() != 0) {
            return (InetAddress[]) results.toArray(new InetAddress[0]);
        }
        throw new UnknownHostException(host);
    }

    private void sendDnsProbe(String host) {
        if (!TextUtils.isEmpty(host)) {
            HwHiLog.d(TAG, false, "PROBE_DNS start", new Object[0]);
            ValidationProbeEvent.getProbeName(0);
            try {
                InetAddress[] addresses = sendDnsProbeWithTimeout(host, DNS_PROBE_TIMEOUT_MS);
                StringBuffer buffer = new StringBuffer();
                for (InetAddress address : addresses) {
                    buffer.append(',');
                    buffer.append(address.getHostAddress());
                }
                String str = "OK " + buffer.substring(1);
                HwHiLog.d(TAG, false, "PROBE_DNS completed: ok.", new Object[0]);
            } catch (UnknownHostException e) {
                HwHiLog.e(TAG, false, "PROBE_DNS completed: fail.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x0257, code lost:
        r13 = "sendHttpProbe, errorStream close happen error";
        r10 = "sendHttpProbe, inputStream close happen error";
        r15 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x02b9, code lost:
        r13 = "sendHttpProbe, errorStream close happen error";
        r10 = "sendHttpProbe, inputStream close happen error";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01a0, code lost:
        r15 = r15;
        r13 = "sendHttpProbe, errorStream close happen error";
        r10 = "sendHttpProbe, inputStream close happen error";
     */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01df A[SYNTHETIC, Splitter:B:108:0x01df] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0204 A[SYNTHETIC, Splitter:B:113:0x0204] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0256 A[ExcHandler: IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException (e java.lang.Throwable), Splitter:B:94:0x01ba] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02b8 A[ExcHandler: IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException (e java.lang.Throwable), Splitter:B:40:0x00a3] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02c6  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x02cb A[SYNTHETIC, Splitter:B:162:0x02cb] */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x02ee A[SYNTHETIC, Splitter:B:167:0x02ee] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0332  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x033c  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0341 A[SYNTHETIC, Splitter:B:181:0x0341] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x0364 A[SYNTHETIC, Splitter:B:186:0x0364] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0372  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x037c  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0386  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x038b A[SYNTHETIC, Splitter:B:199:0x038b] */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x03ae A[SYNTHETIC, Splitter:B:204:0x03ae] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x012b A[SYNTHETIC, Splitter:B:63:0x012b] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x019f A[ExcHandler: IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException (e java.lang.Throwable), Splitter:B:104:0x01d4] */
    private CaptivePortalProbeResult sendHttpProbe(URL url, int probeType, CaptivePortalProbeSpec probeSpec) {
        String str;
        String str2;
        Throwable th;
        Throwable th2;
        String str3;
        Exception e;
        StringBuilder sb;
        String requestHeader;
        long requestTimestamp;
        long responseTimestamp;
        StringBuilder sb2;
        Throwable th3;
        InputStream errorStream;
        HttpURLConnection urlConnection;
        HwHiLog.d(TAG, false, "%{public}s start", new Object[]{ValidationProbeEvent.getProbeName(probeType)});
        HttpURLConnection urlConnection2 = null;
        int httpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
        String redirectUrl = null;
        int oldTag = TrafficStats.getAndSetThreadStatsTag(TAG_SYSTEM_PROBE);
        InputStream inputStream = null;
        InputStream errorStream2 = null;
        try {
            urlConnection2 = (HttpURLConnection) this.mNetwork.openConnection(url);
            if (probeType == 2 || probeType == 6) {
                try {
                    if (urlConnection2 instanceof HttpsURLConnection) {
                        try {
                            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection2;
                            SecureSSLSocketFactory socketFactory = SecureSSLSocketFactory.getInstance(this.mContext);
                            if (socketFactory != null) {
                                urlConnection = urlConnection2;
                                try {
                                    if (socketFactory instanceof SSLSocketFactory) {
                                        httpsUrlConnection.setSSLSocketFactory(socketFactory);
                                        httpsUrlConnection.setHostnameVerifier(SecureSSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
                                        urlConnection2 = httpsUrlConnection;
                                        urlConnection2.setInstanceFollowRedirects(probeType != 3);
                                        urlConnection2.setConnectTimeout(10000);
                                        urlConnection2.setReadTimeout(10000);
                                        urlConnection2.setRequestProperty("Connection", "close");
                                        urlConnection2.setUseCaches(false);
                                        if (this.mCaptivePortalUserAgent != null) {
                                            try {
                                                urlConnection2.setRequestProperty("User-Agent", this.mCaptivePortalUserAgent);
                                            } catch (IOException | UncheckedIOException e2) {
                                                e = e2;
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str3 = "sendHttpProbe, inputStream close happen error";
                                            } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e3) {
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str2 = "sendHttpProbe, inputStream close happen error";
                                                try {
                                                    HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                                                    if (urlConnection2 != null) {
                                                    }
                                                    if (inputStream != null) {
                                                    }
                                                    if (errorStream2 != null) {
                                                    }
                                                    TrafficStats.setThreadStatsTag(oldTag);
                                                    if (probeSpec != null) {
                                                    }
                                                } catch (Throwable th4) {
                                                    th2 = th4;
                                                    th = th2;
                                                    if (urlConnection2 != null) {
                                                    }
                                                    if (inputStream != null) {
                                                    }
                                                    if (errorStream2 != null) {
                                                    }
                                                    TrafficStats.setThreadStatsTag(oldTag);
                                                    throw th;
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str2 = "sendHttpProbe, inputStream close happen error";
                                                if (urlConnection2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                if (errorStream2 != null) {
                                                }
                                                TrafficStats.setThreadStatsTag(oldTag);
                                                throw th;
                                            }
                                        }
                                        requestHeader = urlConnection2.getRequestProperties().toString();
                                        requestTimestamp = SystemClock.elapsedRealtime();
                                        httpResponseCode = urlConnection2.getResponseCode();
                                        redirectUrl = urlConnection2.getHeaderField("location");
                                        try {
                                            responseTimestamp = SystemClock.elapsedRealtime();
                                            sb2 = new StringBuilder();
                                            try {
                                                sb2.append("time=");
                                                sb2.append(responseTimestamp - requestTimestamp);
                                                sb2.append("ms ret=");
                                                sb2.append(httpResponseCode);
                                                sb2.append(" request=");
                                                sb2.append(requestHeader);
                                                sb2.append(" headers=");
                                                sb2.append(urlConnection2.getHeaderFields());
                                                validationLog(probeType, url, sb2.toString());
                                                if (httpResponseCode == 200) {
                                                    try {
                                                        long contentLength = urlConnection2.getContentLengthLong();
                                                        HwHiLog.d(TAG, false, "200 response contentLength = %{public}s", new Object[]{String.valueOf(contentLength)});
                                                        if (probeType == 3) {
                                                            validationLog(probeType, url, "PAC fetch 200 response interpreted as 204 response.");
                                                            httpResponseCode = 204;
                                                        } else if (contentLength == -1) {
                                                            inputStream = urlConnection2.getInputStream();
                                                            if (inputStream.read() == -1) {
                                                                validationLog(probeType, url, "Empty 200 response interpreted as failed response.");
                                                                httpResponseCode = 599;
                                                            }
                                                        } else if (contentLength <= 4) {
                                                            validationLog(probeType, url, "200 response with Content-length <= 4 interpreted as failed response.");
                                                            httpResponseCode = 599;
                                                        } else {
                                                            validationLog(probeType, url, "can not get probe response code.");
                                                        }
                                                        if ("close".equalsIgnoreCase(urlConnection2.getHeaderField("Connection")) && httpResponseCode == 200 && probeType == 1) {
                                                            HwHiLog.d(TAG, false, "Connection close, 200 response interpreted as 204 response.", new Object[0]);
                                                            httpResponseCode = 599;
                                                        }
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        str = "sendHttpProbe, errorStream close happen error";
                                                        str2 = "sendHttpProbe, inputStream close happen error";
                                                        if (urlConnection2 != null) {
                                                        }
                                                        if (inputStream != null) {
                                                        }
                                                        if (errorStream2 != null) {
                                                        }
                                                        TrafficStats.setThreadStatsTag(oldTag);
                                                        throw th;
                                                    }
                                                }
                                                if (probeType == 1 || probeType == 4) {
                                                    try {
                                                        errorStream = urlConnection2.getErrorStream();
                                                        try {
                                                            httpResponseCode = checkSuccessRespCode(httpResponseCode, urlConnection2);
                                                            errorStream2 = errorStream;
                                                            httpResponseCode = checkClientErrorRespCode(httpResponseCode, errorStream);
                                                        } catch (IOException | UncheckedIOException e4) {
                                                            str = "sendHttpProbe, errorStream close happen error";
                                                            str3 = "sendHttpProbe, inputStream close happen error";
                                                            e = e4;
                                                            errorStream2 = errorStream;
                                                            redirectUrl = redirectUrl;
                                                            validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                                            if (probeType == 1) {
                                                            }
                                                            if (urlConnection2 != null) {
                                                            }
                                                            if (inputStream != null) {
                                                            }
                                                            if (errorStream2 != null) {
                                                            }
                                                            TrafficStats.setThreadStatsTag(oldTag);
                                                            if (probeSpec != null) {
                                                            }
                                                        } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e5) {
                                                            str = "sendHttpProbe, errorStream close happen error";
                                                            str2 = "sendHttpProbe, inputStream close happen error";
                                                            errorStream2 = errorStream;
                                                            redirectUrl = redirectUrl;
                                                            HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                                                            if (urlConnection2 != null) {
                                                            }
                                                            if (inputStream != null) {
                                                            }
                                                            if (errorStream2 != null) {
                                                            }
                                                            TrafficStats.setThreadStatsTag(oldTag);
                                                            if (probeSpec != null) {
                                                            }
                                                        } catch (Throwable th7) {
                                                            str = "sendHttpProbe, errorStream close happen error";
                                                            str2 = "sendHttpProbe, inputStream close happen error";
                                                            th = th7;
                                                            errorStream2 = errorStream;
                                                            if (urlConnection2 != null) {
                                                            }
                                                            if (inputStream != null) {
                                                            }
                                                            if (errorStream2 != null) {
                                                            }
                                                            TrafficStats.setThreadStatsTag(oldTag);
                                                            throw th;
                                                        }
                                                    } catch (IOException | UncheckedIOException e6) {
                                                        str = "sendHttpProbe, errorStream close happen error";
                                                        str3 = "sendHttpProbe, inputStream close happen error";
                                                        e = e6;
                                                        redirectUrl = redirectUrl;
                                                        validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                                        if (probeType == 1) {
                                                        }
                                                        if (urlConnection2 != null) {
                                                        }
                                                        if (inputStream != null) {
                                                        }
                                                        if (errorStream2 != null) {
                                                        }
                                                        TrafficStats.setThreadStatsTag(oldTag);
                                                        if (probeSpec != null) {
                                                        }
                                                    } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e7) {
                                                    }
                                                }
                                                if (httpResponseCode == 599 && probeType == 1) {
                                                    try {
                                                        HwHiLog.d(TAG, false, "http FAILED_CODE, need confirmed by fallback.", new Object[0]);
                                                    } catch (IOException | UncheckedIOException e8) {
                                                        e = e8;
                                                        redirectUrl = redirectUrl;
                                                        str = "sendHttpProbe, errorStream close happen error";
                                                        str3 = "sendHttpProbe, inputStream close happen error";
                                                        validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                                        if (probeType == 1) {
                                                        }
                                                        if (urlConnection2 != null) {
                                                        }
                                                        if (inputStream != null) {
                                                        }
                                                        if (errorStream2 != null) {
                                                        }
                                                        TrafficStats.setThreadStatsTag(oldTag);
                                                        if (probeSpec != null) {
                                                        }
                                                    } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e9) {
                                                    }
                                                }
                                                urlConnection2.disconnect();
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e10) {
                                                        HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, "sendHttpProbe, inputStream close happen error", new Object[0]);
                                                    }
                                                }
                                                if (errorStream2 != null) {
                                                    try {
                                                        errorStream2.close();
                                                    } catch (IOException e11) {
                                                        HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, "sendHttpProbe, errorStream close happen error", new Object[0]);
                                                    }
                                                }
                                                TrafficStats.setThreadStatsTag(oldTag);
                                                redirectUrl = redirectUrl;
                                            } catch (IOException | UncheckedIOException e12) {
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str3 = "sendHttpProbe, inputStream close happen error";
                                                e = e12;
                                                redirectUrl = redirectUrl;
                                                validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                                if (probeType == 1) {
                                                }
                                                if (urlConnection2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                if (errorStream2 != null) {
                                                }
                                                TrafficStats.setThreadStatsTag(oldTag);
                                                if (probeSpec != null) {
                                                }
                                            } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e13) {
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str2 = "sendHttpProbe, inputStream close happen error";
                                                redirectUrl = redirectUrl;
                                                HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                                                if (urlConnection2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                if (errorStream2 != null) {
                                                }
                                                TrafficStats.setThreadStatsTag(oldTag);
                                                if (probeSpec != null) {
                                                }
                                            } catch (Throwable th8) {
                                                th3 = th8;
                                                str = "sendHttpProbe, errorStream close happen error";
                                                str2 = "sendHttpProbe, inputStream close happen error";
                                                th = th3;
                                                if (urlConnection2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                if (errorStream2 != null) {
                                                }
                                                TrafficStats.setThreadStatsTag(oldTag);
                                                throw th;
                                            }
                                        } catch (IOException | UncheckedIOException e14) {
                                            str = "sendHttpProbe, errorStream close happen error";
                                            str3 = "sendHttpProbe, inputStream close happen error";
                                            e = e14;
                                            validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                            if (probeType == 1) {
                                            }
                                            if (urlConnection2 != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            if (errorStream2 != null) {
                                            }
                                            TrafficStats.setThreadStatsTag(oldTag);
                                            if (probeSpec != null) {
                                            }
                                        } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e15) {
                                            str = "sendHttpProbe, errorStream close happen error";
                                            str2 = "sendHttpProbe, inputStream close happen error";
                                            HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                                            if (urlConnection2 != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            if (errorStream2 != null) {
                                            }
                                            TrafficStats.setThreadStatsTag(oldTag);
                                            if (probeSpec != null) {
                                            }
                                        } catch (Throwable th9) {
                                            str = "sendHttpProbe, errorStream close happen error";
                                            str2 = "sendHttpProbe, inputStream close happen error";
                                            th = th9;
                                            if (urlConnection2 != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            if (errorStream2 != null) {
                                            }
                                            TrafficStats.setThreadStatsTag(oldTag);
                                            throw th;
                                        }
                                        if (probeSpec != null) {
                                            return new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
                                        }
                                        return probeSpec.getResult(httpResponseCode, redirectUrl);
                                    }
                                } catch (IOException | UncheckedIOException e16) {
                                    e = e16;
                                    str = "sendHttpProbe, errorStream close happen error";
                                    str3 = "sendHttpProbe, inputStream close happen error";
                                    urlConnection2 = urlConnection;
                                    validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                                    if (probeType == 1) {
                                    }
                                    if (urlConnection2 != null) {
                                    }
                                    if (inputStream != null) {
                                    }
                                    if (errorStream2 != null) {
                                    }
                                    TrafficStats.setThreadStatsTag(oldTag);
                                    if (probeSpec != null) {
                                    }
                                } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e17) {
                                    str = "sendHttpProbe, errorStream close happen error";
                                    str2 = "sendHttpProbe, inputStream close happen error";
                                    urlConnection2 = urlConnection;
                                    HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                                    if (urlConnection2 != null) {
                                    }
                                    if (inputStream != null) {
                                    }
                                    if (errorStream2 != null) {
                                    }
                                    TrafficStats.setThreadStatsTag(oldTag);
                                    if (probeSpec != null) {
                                    }
                                } catch (Throwable th10) {
                                    th = th10;
                                    str = "sendHttpProbe, errorStream close happen error";
                                    str2 = "sendHttpProbe, inputStream close happen error";
                                    urlConnection2 = urlConnection;
                                    if (urlConnection2 != null) {
                                    }
                                    if (inputStream != null) {
                                    }
                                    if (errorStream2 != null) {
                                    }
                                    TrafficStats.setThreadStatsTag(oldTag);
                                    throw th;
                                }
                            } else {
                                urlConnection = urlConnection2;
                            }
                        } catch (IOException | UncheckedIOException e18) {
                            e = e18;
                            str = "sendHttpProbe, errorStream close happen error";
                            str3 = "sendHttpProbe, inputStream close happen error";
                            validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                            if (probeType == 1) {
                                HwHiLog.e(TAG, false, "http Exception, need confirmed by fallback.", new Object[0]);
                            }
                            if (urlConnection2 != null) {
                                urlConnection2.disconnect();
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e19) {
                                    HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, str3, new Object[0]);
                                }
                            }
                            if (errorStream2 != null) {
                                try {
                                    errorStream2.close();
                                } catch (IOException e20) {
                                    sb = new StringBuilder();
                                }
                            }
                            TrafficStats.setThreadStatsTag(oldTag);
                            if (probeSpec != null) {
                            }
                        } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e21) {
                            str = "sendHttpProbe, errorStream close happen error";
                            str2 = "sendHttpProbe, inputStream close happen error";
                            HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                            if (urlConnection2 != null) {
                                urlConnection2.disconnect();
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e22) {
                                    HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, str2, new Object[0]);
                                }
                            }
                            if (errorStream2 != null) {
                                try {
                                    errorStream2.close();
                                } catch (IOException e23) {
                                    sb = new StringBuilder();
                                }
                            }
                            TrafficStats.setThreadStatsTag(oldTag);
                            if (probeSpec != null) {
                            }
                        } catch (Throwable th11) {
                            th = th11;
                            str = "sendHttpProbe, errorStream close happen error";
                            str2 = "sendHttpProbe, inputStream close happen error";
                            if (urlConnection2 != null) {
                                urlConnection2.disconnect();
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e24) {
                                    HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, str2, new Object[0]);
                                }
                            }
                            if (errorStream2 != null) {
                                try {
                                    errorStream2.close();
                                } catch (IOException e25) {
                                    HwHiLog.e("HwNetworkMonitor/" + this.mNetwork.toString(), false, str, new Object[0]);
                                }
                            }
                            TrafficStats.setThreadStatsTag(oldTag);
                            throw th;
                        }
                    } else {
                        urlConnection = urlConnection2;
                    }
                } catch (IOException | UncheckedIOException e26) {
                    str = "sendHttpProbe, errorStream close happen error";
                    str3 = "sendHttpProbe, inputStream close happen error";
                    e = e26;
                    validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                    if (probeType == 1) {
                    }
                    if (urlConnection2 != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (errorStream2 != null) {
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    if (probeSpec != null) {
                    }
                } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e27) {
                    str = "sendHttpProbe, errorStream close happen error";
                    str2 = "sendHttpProbe, inputStream close happen error";
                    HwHiLog.e(TAG, false, "https failed due to exception.", new Object[0]);
                    if (urlConnection2 != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (errorStream2 != null) {
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    if (probeSpec != null) {
                    }
                } catch (Throwable th12) {
                    str = "sendHttpProbe, errorStream close happen error";
                    str2 = "sendHttpProbe, inputStream close happen error";
                    th = th12;
                    if (urlConnection2 != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (errorStream2 != null) {
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    throw th;
                }
            } else {
                urlConnection = urlConnection2;
            }
            urlConnection2 = urlConnection;
            try {
                urlConnection2.setInstanceFollowRedirects(probeType != 3);
                urlConnection2.setConnectTimeout(10000);
                urlConnection2.setReadTimeout(10000);
                urlConnection2.setRequestProperty("Connection", "close");
                urlConnection2.setUseCaches(false);
                if (this.mCaptivePortalUserAgent != null) {
                }
                requestHeader = urlConnection2.getRequestProperties().toString();
                requestTimestamp = SystemClock.elapsedRealtime();
                httpResponseCode = urlConnection2.getResponseCode();
                redirectUrl = urlConnection2.getHeaderField("location");
                responseTimestamp = SystemClock.elapsedRealtime();
                sb2 = new StringBuilder();
                sb2.append("time=");
            } catch (IOException | UncheckedIOException e28) {
                str = "sendHttpProbe, errorStream close happen error";
                str3 = "sendHttpProbe, inputStream close happen error";
                e = e28;
                validationLog(probeType, url, "Probe failed with exception " + e.getMessage());
                if (probeType == 1) {
                }
                if (urlConnection2 != null) {
                }
                if (inputStream != null) {
                }
                if (errorStream2 != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                if (probeSpec != null) {
                }
            } catch (IllegalAccessException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e29) {
            }
            try {
                sb2.append(responseTimestamp - requestTimestamp);
                sb2.append("ms ret=");
                sb2.append(httpResponseCode);
                sb2.append(" request=");
                sb2.append(requestHeader);
                sb2.append(" headers=");
                sb2.append(urlConnection2.getHeaderFields());
                validationLog(probeType, url, sb2.toString());
                if (httpResponseCode == 200) {
                }
                errorStream = urlConnection2.getErrorStream();
                httpResponseCode = checkSuccessRespCode(httpResponseCode, urlConnection2);
                errorStream2 = errorStream;
                httpResponseCode = checkClientErrorRespCode(httpResponseCode, errorStream);
                HwHiLog.d(TAG, false, "http FAILED_CODE, need confirmed by fallback.", new Object[0]);
                urlConnection2.disconnect();
                if (inputStream != null) {
                }
                if (errorStream2 != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                redirectUrl = redirectUrl;
                if (probeSpec != null) {
                }
            } catch (Throwable th13) {
                th3 = th13;
                str = "sendHttpProbe, errorStream close happen error";
                str2 = "sendHttpProbe, inputStream close happen error";
                th = th3;
                if (urlConnection2 != null) {
                }
                if (inputStream != null) {
                }
                if (errorStream2 != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                throw th;
            }
            sb.append("HwNetworkMonitor/");
            sb.append(this.mNetwork.toString());
            HwHiLog.e(sb.toString(), false, str, new Object[0]);
            TrafficStats.setThreadStatsTag(oldTag);
            if (probeSpec != null) {
            }
        } catch (Throwable th14) {
            th2 = th14;
            str = "sendHttpProbe, errorStream close happen error";
            str2 = "sendHttpProbe, inputStream close happen error";
            th = th2;
            if (urlConnection2 != null) {
            }
            if (inputStream != null) {
            }
            if (errorStream2 != null) {
            }
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(ProxyInfo proxy, URL httpsUrl, URL httpUrl, URL httpsFallbackUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch latchFinal = new CountDownLatch(4);
        AnonymousClass1ProbeThread httpsProbe = new Thread(2, proxy, httpsUrl, httpsFallbackUrl, httpUrl, latch, latchFinal) {
            /* class com.huawei.hwwifiproservice.NetworkMonitor.AnonymousClass1ProbeThread */
            private final int mProbeType;
            private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
            final /* synthetic */ URL val$httpUrl;
            final /* synthetic */ URL val$httpsFallbackUrl;
            final /* synthetic */ URL val$httpsUrl;
            final /* synthetic */ CountDownLatch val$latch;
            final /* synthetic */ CountDownLatch val$latchFinal;
            final /* synthetic */ ProxyInfo val$proxy;

            /* JADX WARN: Incorrect args count in method signature: (I)V */
            {
                this.val$proxy = r3;
                this.val$httpsUrl = r4;
                this.val$httpsFallbackUrl = r5;
                this.val$httpUrl = r6;
                this.val$latch = r7;
                this.val$latchFinal = r8;
                this.mProbeType = probeType;
            }

            public CaptivePortalProbeResult result() {
                return this.mResult;
            }

            private boolean isHttpProbe() {
                int i = this.mProbeType;
                return i == 1 || i == 4;
            }

            private boolean isProbeValid() {
                return (!isHttpProbe() && this.mResult.isSuccessful()) || (isHttpProbe() && this.mResult.isPortal());
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                int i = this.mProbeType;
                if (i == 2) {
                    this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, i);
                } else if (i == 6) {
                    this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsFallbackUrl, i, null);
                } else if (i == 1) {
                    this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, i);
                } else {
                    CaptivePortalProbeSpec probeSpec = NetworkMonitor.this.nextFallbackSpec();
                    URL fallbackUrl = probeSpec != null ? probeSpec.getUrl() : NetworkMonitor.this.nextFallbackUrl();
                    if (fallbackUrl != null) {
                        this.mResult = NetworkMonitor.this.sendHttpProbe(fallbackUrl, this.mProbeType, probeSpec);
                    }
                }
                if (isProbeValid()) {
                    while (this.val$latch.getCount() > 0) {
                        this.val$latch.countDown();
                    }
                    while (this.val$latchFinal.getCount() > 0) {
                        this.val$latchFinal.countDown();
                    }
                }
                if (this.val$latch.getCount() > 0) {
                    this.val$latch.countDown();
                }
                if (this.val$latchFinal.getCount() > 0) {
                    this.val$latchFinal.countDown();
                }
            }
        };
        AnonymousClass1ProbeThread httpProbe = new Thread(1, proxy, httpsUrl, httpsFallbackUrl, httpUrl, latch, latchFinal) {
            /* class com.huawei.hwwifiproservice.NetworkMonitor.AnonymousClass1ProbeThread */
            private final int mProbeType;
            private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
            final /* synthetic */ URL val$httpUrl;
            final /* synthetic */ URL val$httpsFallbackUrl;
            final /* synthetic */ URL val$httpsUrl;
            final /* synthetic */ CountDownLatch val$latch;
            final /* synthetic */ CountDownLatch val$latchFinal;
            final /* synthetic */ ProxyInfo val$proxy;

            /* JADX WARN: Incorrect args count in method signature: (I)V */
            {
                this.val$proxy = r3;
                this.val$httpsUrl = r4;
                this.val$httpsFallbackUrl = r5;
                this.val$httpUrl = r6;
                this.val$latch = r7;
                this.val$latchFinal = r8;
                this.mProbeType = probeType;
            }

            public CaptivePortalProbeResult result() {
                return this.mResult;
            }

            private boolean isHttpProbe() {
                int i = this.mProbeType;
                return i == 1 || i == 4;
            }

            private boolean isProbeValid() {
                return (!isHttpProbe() && this.mResult.isSuccessful()) || (isHttpProbe() && this.mResult.isPortal());
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                int i = this.mProbeType;
                if (i == 2) {
                    this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, i);
                } else if (i == 6) {
                    this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsFallbackUrl, i, null);
                } else if (i == 1) {
                    this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, i);
                } else {
                    CaptivePortalProbeSpec probeSpec = NetworkMonitor.this.nextFallbackSpec();
                    URL fallbackUrl = probeSpec != null ? probeSpec.getUrl() : NetworkMonitor.this.nextFallbackUrl();
                    if (fallbackUrl != null) {
                        this.mResult = NetworkMonitor.this.sendHttpProbe(fallbackUrl, this.mProbeType, probeSpec);
                    }
                }
                if (isProbeValid()) {
                    while (this.val$latch.getCount() > 0) {
                        this.val$latch.countDown();
                    }
                    while (this.val$latchFinal.getCount() > 0) {
                        this.val$latchFinal.countDown();
                    }
                }
                if (this.val$latch.getCount() > 0) {
                    this.val$latch.countDown();
                }
                if (this.val$latchFinal.getCount() > 0) {
                    this.val$latchFinal.countDown();
                }
            }
        };
        try {
            httpProbe.start();
            httpsProbe.start();
            latch.await(3000, TimeUnit.MILLISECONDS);
            CaptivePortalProbeResult httpsResult = httpsProbe.result();
            CaptivePortalProbeResult httpResult = httpProbe.result();
            if (httpResult.isPortal()) {
                return httpResult;
            }
            if (httpsResult.isPortal() || httpsResult.isSuccessful()) {
                return httpsResult;
            }
            AnonymousClass1ProbeThread httpFallbackProbe = new Thread(4, proxy, httpsUrl, httpsFallbackUrl, httpUrl, latch, latchFinal) {
                /* class com.huawei.hwwifiproservice.NetworkMonitor.AnonymousClass1ProbeThread */
                private final int mProbeType;
                private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
                final /* synthetic */ URL val$httpUrl;
                final /* synthetic */ URL val$httpsFallbackUrl;
                final /* synthetic */ URL val$httpsUrl;
                final /* synthetic */ CountDownLatch val$latch;
                final /* synthetic */ CountDownLatch val$latchFinal;
                final /* synthetic */ ProxyInfo val$proxy;

                /* JADX WARN: Incorrect args count in method signature: (I)V */
                {
                    this.val$proxy = r3;
                    this.val$httpsUrl = r4;
                    this.val$httpsFallbackUrl = r5;
                    this.val$httpUrl = r6;
                    this.val$latch = r7;
                    this.val$latchFinal = r8;
                    this.mProbeType = probeType;
                }

                public CaptivePortalProbeResult result() {
                    return this.mResult;
                }

                private boolean isHttpProbe() {
                    int i = this.mProbeType;
                    return i == 1 || i == 4;
                }

                private boolean isProbeValid() {
                    return (!isHttpProbe() && this.mResult.isSuccessful()) || (isHttpProbe() && this.mResult.isPortal());
                }

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    int i = this.mProbeType;
                    if (i == 2) {
                        this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, i);
                    } else if (i == 6) {
                        this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsFallbackUrl, i, null);
                    } else if (i == 1) {
                        this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, i);
                    } else {
                        CaptivePortalProbeSpec probeSpec = NetworkMonitor.this.nextFallbackSpec();
                        URL fallbackUrl = probeSpec != null ? probeSpec.getUrl() : NetworkMonitor.this.nextFallbackUrl();
                        if (fallbackUrl != null) {
                            this.mResult = NetworkMonitor.this.sendHttpProbe(fallbackUrl, this.mProbeType, probeSpec);
                        }
                    }
                    if (isProbeValid()) {
                        while (this.val$latch.getCount() > 0) {
                            this.val$latch.countDown();
                        }
                        while (this.val$latchFinal.getCount() > 0) {
                            this.val$latchFinal.countDown();
                        }
                    }
                    if (this.val$latch.getCount() > 0) {
                        this.val$latch.countDown();
                    }
                    if (this.val$latchFinal.getCount() > 0) {
                        this.val$latchFinal.countDown();
                    }
                }
            };
            AnonymousClass1ProbeThread httpsFallbackProbe = new Thread(6, proxy, httpsUrl, httpsFallbackUrl, httpUrl, latch, latchFinal) {
                /* class com.huawei.hwwifiproservice.NetworkMonitor.AnonymousClass1ProbeThread */
                private final int mProbeType;
                private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
                final /* synthetic */ URL val$httpUrl;
                final /* synthetic */ URL val$httpsFallbackUrl;
                final /* synthetic */ URL val$httpsUrl;
                final /* synthetic */ CountDownLatch val$latch;
                final /* synthetic */ CountDownLatch val$latchFinal;
                final /* synthetic */ ProxyInfo val$proxy;

                /* JADX WARN: Incorrect args count in method signature: (I)V */
                {
                    this.val$proxy = r3;
                    this.val$httpsUrl = r4;
                    this.val$httpsFallbackUrl = r5;
                    this.val$httpUrl = r6;
                    this.val$latch = r7;
                    this.val$latchFinal = r8;
                    this.mProbeType = probeType;
                }

                public CaptivePortalProbeResult result() {
                    return this.mResult;
                }

                private boolean isHttpProbe() {
                    int i = this.mProbeType;
                    return i == 1 || i == 4;
                }

                private boolean isProbeValid() {
                    return (!isHttpProbe() && this.mResult.isSuccessful()) || (isHttpProbe() && this.mResult.isPortal());
                }

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    int i = this.mProbeType;
                    if (i == 2) {
                        this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, i);
                    } else if (i == 6) {
                        this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsFallbackUrl, i, null);
                    } else if (i == 1) {
                        this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, i);
                    } else {
                        CaptivePortalProbeSpec probeSpec = NetworkMonitor.this.nextFallbackSpec();
                        URL fallbackUrl = probeSpec != null ? probeSpec.getUrl() : NetworkMonitor.this.nextFallbackUrl();
                        if (fallbackUrl != null) {
                            this.mResult = NetworkMonitor.this.sendHttpProbe(fallbackUrl, this.mProbeType, probeSpec);
                        }
                    }
                    if (isProbeValid()) {
                        while (this.val$latch.getCount() > 0) {
                            this.val$latch.countDown();
                        }
                        while (this.val$latchFinal.getCount() > 0) {
                            this.val$latchFinal.countDown();
                        }
                    }
                    if (this.val$latch.getCount() > 0) {
                        this.val$latch.countDown();
                    }
                    if (this.val$latchFinal.getCount() > 0) {
                        this.val$latchFinal.countDown();
                    }
                }
            };
            httpsFallbackProbe.start();
            httpFallbackProbe.start();
            try {
                latchFinal.await(10000, TimeUnit.MILLISECONDS);
                if (httpProbe.result().isPortal()) {
                    return httpProbe.result();
                }
                if (httpFallbackProbe.result().isPortal()) {
                    return httpFallbackProbe.result();
                }
                if (httpsProbe.result().isSuccessful()) {
                    return httpsProbe.result();
                }
                if (httpsFallbackProbe.result().isSuccessful()) {
                    return httpsFallbackProbe.result();
                }
                boolean isHttpSuccessful = true;
                if (httpProbe.result().isSuccessful() && httpFallbackProbe.result() != null && httpFallbackProbe.result().isSuccessful()) {
                    return httpProbe.result();
                }
                if (!httpProbe.result().isSuccessful()) {
                    if (httpsFallbackProbe.result() == null || !httpsFallbackProbe.result().isSuccessful()) {
                        isHttpSuccessful = false;
                    }
                }
                if (!httpsProbe.result().isFailed() || !isHttpSuccessful) {
                    return httpsProbe.result();
                }
                return CaptivePortalProbeResult.PARTIAL;
            } catch (InterruptedException e) {
                HwHiLog.e(TAG, false, "Error: http or https probe wait interrupted!", new Object[0]);
                return CaptivePortalProbeResult.FAILED;
            }
        } catch (InterruptedException e2) {
            HwHiLog.e(TAG, false, "Error: probes wait interrupted!", new Object[0]);
            return CaptivePortalProbeResult.FAILED;
        }
    }

    /* access modifiers changed from: private */
    public URL makeUrl(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            HwHiLog.e(TAG, false, "Bad URL: %{private}s", new Object[]{url});
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Dependencies {
        public static final Dependencies DEFAULT = new Dependencies();

        Dependencies() {
        }

        public DnsResolver getDnsResolver() {
            return DnsResolver.getInstance();
        }

        public Network getPrivateDnsBypassNetwork(Network network) {
            return new OneAddressPerFamilyNetwork(network);
        }

        public Random getRandom() {
            return new Random();
        }

        public int getSetting(Context context, String symbol, int defaultValue) {
            return Settings.Global.getInt(context.getContentResolver(), symbol, defaultValue);
        }

        public String getSetting(Context context, String symbol, String defaultValue) {
            String value = Settings.Global.getString(context.getContentResolver(), symbol);
            return value != null ? value : defaultValue;
        }
    }

    private int checkSuccessRespCode(int responseCode, HttpURLConnection urlConnection) {
        if (responseCode != 204) {
            return responseCode;
        }
        String requestId = urlConnection.getHeaderField("X-Hwcloud-ReqId");
        if (requestId != null && requestId.length() == 32) {
            return responseCode;
        }
        HwHiLog.w(TAG, false, "http return 204, but request id error and unreachable!", new Object[0]);
        return CaptivePortalProbeResult.FAILED_CODE;
    }

    private int checkClientErrorRespCode(int responseCode, InputStream inputStream) {
        if (!isClientErrorRespCode(responseCode) || inputStream == null) {
            return responseCode;
        }
        String urlContent = (String) new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(System.lineSeparator()));
        if ((!urlContent.contains("http://") && !urlContent.contains(HTML_TITLE_HTTPS_EN)) || !urlContent.contains(KEY_WORDS_REDIRECTION)) {
            return responseCode;
        }
        HwHiLog.d(TAG, false, "http return %{public}d, reset for the url in the content of response PORTAL_CODE", new Object[]{Integer.valueOf(responseCode)});
        return CaptivePortalProbeResult.PORTAL_CODE;
    }
}
