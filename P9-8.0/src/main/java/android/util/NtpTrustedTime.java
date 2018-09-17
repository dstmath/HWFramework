package android.util;

import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import com.android.internal.R;

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
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", LogException.NO_VALUE))) {
            str = "1.cn.pool.ntp.org,1.asia.pool.ntp.org,2.asia.pool.ntp.org,ntp.neu6.edu.cn,s2f.time.edu.cn,clock.neu.edu.cn";
        } else {
            str = SystemProperties.get("ro.config.spare_ntp_server", LogException.NO_VALUE);
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
                String defaultServer = res.getString(R.string.config_ntpServer);
                long defaultTimeout = (long) res.getInteger(R.integer.config_ntpTimeout);
                String secureServer = Global.getString(resolver, Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Global.getLong(resolver, Global.NTP_TIMEOUT, defaultTimeout));
                sContext = context;
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    public boolean forceRefresh() {
        if (TextUtils.isEmpty(this.mServer) && LogException.NO_VALUE.equals(this.mbackupNtpServer)) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService("connectivity");
            }
        }
        NetworkInfo ni = this.mCM == null ? null : this.mCM.getActiveNetworkInfo();
        if (ni == null || (ni.isConnected() ^ 1) != 0) {
            return false;
        }
        SntpClient client = new SntpClient();
        if (!client.requestTime(this.mServer, (int) this.mTimeout) && !tryOtherServer(client)) {
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

    private boolean tryOtherServer(SntpClient sClient) {
        if (LogException.NO_VALUE.equals(this.mbackupNtpServer)) {
            return false;
        }
        String[] backupNtpServer = this.mbackupNtpServer.split(",");
        int i = 0;
        while (i < backupNtpServer.length) {
            if (!backupNtpServer[i].isEmpty() && !this.mServer.equals(backupNtpServer[i]) && sClient.requestTime(backupNtpServer[i], (int) this.mTimeout)) {
                return true;
            }
            i++;
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
        return Long.MAX_VALUE;
    }

    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return Long.MAX_VALUE;
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
