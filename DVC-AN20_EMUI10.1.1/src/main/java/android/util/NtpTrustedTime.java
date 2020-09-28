package android.util;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.media.HwMediaFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SmsManager;
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
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            str = "1.cn.pool.ntp.org,1.asia.pool.ntp.org,2.asia.pool.ntp.org,ntp.neu6.edu.cn,s2f.time.edu.cn,clock.neu.edu.cn";
        } else {
            str = SystemProperties.get("ro.config.spare_ntp_server", "") + ",0.europe.pool.ntp.org,1.europe.pool.ntp.org,2.europe.pool.ntp.org,3.europe.pool.ntp.org";
        }
        this.mbackupNtpServer = str;
        this.mServer = server;
        this.mTimeout = timeout;
    }

    @UnsupportedAppUsage
    public static synchronized NtpTrustedTime getInstance(Context context) {
        NtpTrustedTime ntpTrustedTime;
        synchronized (NtpTrustedTime.class) {
            if (sSingleton == null) {
                Resources res = context.getResources();
                ContentResolver resolver = context.getContentResolver();
                String defaultServer = res.getString(R.string.config_ntpServer);
                String secureServer = Settings.Global.getString(resolver, Settings.Global.NTP_SERVER);
                sSingleton = new NtpTrustedTime(secureServer != null ? secureServer : defaultServer, Settings.Global.getLong(resolver, Settings.Global.NTP_TIMEOUT, (long) res.getInteger(R.integer.config_ntpTimeout)));
                sContext = context;
            }
            ntpTrustedTime = sSingleton;
        }
        return ntpTrustedTime;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public boolean forceRefresh() {
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        ConnectivityManager connectivityManager = this.mCM;
        return forceRefresh(connectivityManager == null ? null : connectivityManager.getActiveNetwork());
    }

    public boolean forceRefresh(Network network) {
        if (TextUtils.isEmpty(this.mServer) || (this.mServer == null && "".equals(this.mbackupNtpServer))) {
            return false;
        }
        synchronized (this) {
            if (this.mCM == null) {
                this.mCM = (ConnectivityManager) sContext.getSystemService(ConnectivityManager.class);
            }
        }
        ConnectivityManager connectivityManager = this.mCM;
        NetworkInfo ni = connectivityManager == null ? null : connectivityManager.getNetworkInfo(network);
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
        HwMediaFactory.getHwDrmManager().setNtpTime(this.mCachedNtpTime, this.mCachedNtpElapsedRealtime);
        return true;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public boolean hasCache() {
        return this.mHasCache;
    }

    @Override // android.util.TrustedTime
    public long getCacheAge() {
        if (this.mHasCache) {
            return SystemClock.elapsedRealtime() - this.mCachedNtpElapsedRealtime;
        }
        return Long.MAX_VALUE;
    }

    @Override // android.util.TrustedTime
    public long getCacheCertainty() {
        if (this.mHasCache) {
            return this.mCachedNtpCertainty;
        }
        return Long.MAX_VALUE;
    }

    @Override // android.util.TrustedTime
    @UnsupportedAppUsage
    public long currentTimeMillis() {
        if (this.mHasCache) {
            return this.mCachedNtpTime + getCacheAge();
        }
        throw new IllegalStateException("Missing authoritative time source");
    }

    @UnsupportedAppUsage
    public long getCachedNtpTime() {
        return this.mCachedNtpTime;
    }

    @UnsupportedAppUsage
    public long getCachedNtpTimeReference() {
        return this.mCachedNtpElapsedRealtime;
    }

    public String getCachedNtpIpAddress() {
        return this.mNtpTrustIpAddress;
    }

    private boolean tryOtherServer(SntpClient sClient, Network network) {
        if ("".equals(this.mbackupNtpServer)) {
            return false;
        }
        String[] backupNtpServer = this.mbackupNtpServer.split(SmsManager.REGEX_PREFIX_DELIMITER);
        for (int i = 0; i < backupNtpServer.length; i++) {
            if (!(backupNtpServer[i].isEmpty() || this.mServer.equals(backupNtpServer[i]) || !sClient.requestTime(backupNtpServer[i], (int) this.mTimeout, network))) {
                return true;
            }
        }
        return false;
    }
}
