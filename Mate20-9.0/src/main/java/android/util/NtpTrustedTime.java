package android.util;

import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;

public class NtpTrustedTime implements TrustedTime {
    private static final boolean LOGD = false;
    private static final String TAG = "NtpTrustedTime";
    private static Context sContext;
    private static NtpTrustedTime sSingleton;
    private ConnectivityManager mCM;
    private long mCachedNtpCertainty;
    private long mCachedNtpElapsedRealtime;
    private long mCachedNtpTime;
    private boolean mHasCache;
    private String mNtpTrustIpAddress;
    private final String mServer;
    private final long mTimeout;
    private final String mbackupNtpServer;

    private NtpTrustedTime(String server, long timeout) {
        String str;
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            str = "1.cn.pool.ntp.org,1.asia.pool.ntp.org,2.asia.pool.ntp.org,ntp.neu6.edu.cn,s2f.time.edu.cn,clock.neu.edu.cn";
        } else {
            str = SystemProperties.get("ro.config.spare_ntp_server", "");
        }
        this.mbackupNtpServer = str;
        this.mServer = server;
        this.mTimeout = timeout;
    }

    public static synchronized NtpTrustedTime getInstance(Context context) {
        NtpTrustedTime ntpTrustedTime;
        synchronized (NtpTrustedTime.class) {
            if (sSingleton == null) {
                Resources res = context.getResources();
                ContentResolver resolver = context.getContentResolver();
                String defaultServer = res.getString(17039836);
                String secureServer = Settings.Global.getString(resolver, Settings.Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Settings.Global.getLong(resolver, Settings.Global.NTP_TIMEOUT, (long) res.getInteger(17694846)));
                sContext = context;
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    public boolean forceRefresh() {
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        return forceRefresh(this.mCM == null ? null : this.mCM.getActiveNetwork());
    }

    public boolean forceRefresh(Network network) {
        if (TextUtils.isEmpty(this.mServer) && "".equals(this.mbackupNtpServer)) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        NetworkInfo ni = this.mCM == null ? null : this.mCM.getNetworkInfo(network);
        if (ni == null || !ni.isConnected()) {
            return false;
        }
        SntpClient client = new SntpClient();
        if (!client.requestTime(this.mServer, (int) this.mTimeout, network) && !tryOtherServer(client, network)) {
            return false;
        }
        this.mHasCache = true;
        this.mCachedNtpTime = client.getNtpTime();
        this.mCachedNtpElapsedRealtime = client.getNtpTimeReference();
        this.mCachedNtpCertainty = client.getRoundTripTime() / 2;
        this.mNtpTrustIpAddress = client.getNtpIpAddress();
        HwFrameworkFactory.getHwDrmManager().setNtpTime(this.mCachedNtpTime, this.mCachedNtpElapsedRealtime);
        return true;
    }

    private boolean tryOtherServer(SntpClient sClient, Network network) {
        if ("".equals(this.mbackupNtpServer)) {
            return false;
        }
        String[] backupNtpServer = this.mbackupNtpServer.split(",");
        for (int i = 0; i < backupNtpServer.length; i++) {
            if (!backupNtpServer[i].isEmpty() && !this.mServer.equals(backupNtpServer[i]) && sClient.requestTime(backupNtpServer[i], (int) this.mTimeout, network)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCache() {
        return this.mHasCache;
    }

    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return SubscriptionPlan.BYTES_UNLIMITED;
    }

    public long currentTimeMillis() {
        if (this.mHasCache) {
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    public long getCachedNtpTime() {
        return this.mCachedNtpTime;
    }

    public long getCachedNtpTimeReference() {
        return this.mCachedNtpElapsedRealtime;
    }

    public String getCachedNtpIpAddress() {
        return this.mNtpTrustIpAddress;
    }
}
