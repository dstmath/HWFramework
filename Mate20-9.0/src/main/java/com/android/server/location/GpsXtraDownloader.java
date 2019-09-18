package com.android.server.location;

import android.net.TrafficStats;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GpsXtraDownloader {
    private static final int CONNECTION_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(30));
    private static final String DEFAULT_USER_AGENT = "Android";
    private static final long MAXIMUM_CONTENT_LENGTH_BYTES = 1000000;
    private static final int READ_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(60));
    private static final String TAG = "GpsXtraDownloader";
    private int mByteCount;
    private int mDownLoadInterval;
    private IHwGpsLogServices mHwGpsLogServices;
    private long mLastDownloadTime;
    private int mNextServerIndex;
    private Properties mProperties;
    private final String mUserAgent;
    private final String[] mXtraServers;

    GpsXtraDownloader(Properties properties) {
        this.mProperties = properties;
        int count = 0;
        String server1 = properties.getProperty("XTRA_SERVER_1");
        String server2 = properties.getProperty("XTRA_SERVER_2");
        String server3 = properties.getProperty("XTRA_SERVER_3");
        count = server1 != null ? 0 + 1 : count;
        count = server2 != null ? count + 1 : count;
        count = server3 != null ? count + 1 : count;
        String agent = properties.getProperty("XTRA_USER_AGENT");
        if (TextUtils.isEmpty(agent)) {
            this.mUserAgent = DEFAULT_USER_AGENT;
        } else {
            this.mUserAgent = agent;
        }
        if (count == 0) {
            Log.e(TAG, "No XTRA servers were specified in the GPS configuration");
            this.mXtraServers = null;
        } else {
            this.mXtraServers = new String[count];
            int count2 = 0;
            if (server1 != null) {
                this.mXtraServers[0] = server1;
                count2 = 0 + 1;
            }
            if (server2 != null) {
                this.mXtraServers[count2] = server2;
                count2++;
            }
            if (server3 != null) {
                this.mXtraServers[count2] = server3;
                count2++;
            }
            this.mNextServerIndex = new Random().nextInt(count2);
        }
        this.mDownLoadInterval = Integer.parseInt(this.mProperties.getProperty("HW_XTRA_DOWNLOAD_INTERVAL"));
        this.mLastDownloadTime = Long.parseLong(this.mProperties.getProperty("LAST_XTRA_DOWNLOAD_TIME"));
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public byte[] downloadXtraData() {
        byte[] result = null;
        int startIndex = this.mNextServerIndex;
        if (this.mXtraServers == null) {
            return null;
        }
        while (result == null) {
            int oldTag = TrafficStats.getAndSetThreadStatsTag(-188);
            try {
                result = doDownload(this.mXtraServers[this.mNextServerIndex]);
                TrafficStats.setThreadStatsTag(oldTag);
                this.mNextServerIndex++;
                if (this.mNextServerIndex == this.mXtraServers.length) {
                    this.mNextServerIndex = 0;
                }
                if (this.mNextServerIndex == startIndex) {
                    break;
                }
            } catch (Throwable th) {
                TrafficStats.setThreadStatsTag(oldTag);
                throw th;
            }
        }
        this.mHwGpsLogServices = HwServiceFactory.getNewHwGpsLogService();
        if (this.mHwGpsLogServices != null) {
            boolean xtraStatus = true;
            if (result == null && this.mByteCount != -1) {
                xtraStatus = false;
            }
            this.mHwGpsLogServices.updateXtraDloadStatus(xtraStatus);
            if (xtraStatus) {
                this.mHwGpsLogServices.injectExtraParam("extra_data");
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public byte[] doDownload(String url) {
        InputStream in;
        Throwable th;
        Throwable th2;
        Throwable th3;
        String str = url;
        Log.i(TAG, "Downloading XTRA data from " + str);
        this.mByteCount = 0;
        try {
            URLConnection conn = new URL(str).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            this.mByteCount = conn.getContentLength();
            Log.i(TAG, "mByteCount size is " + this.mByteCount);
            if (this.mByteCount <= 0) {
                return null;
            }
            if (!shouldDownload(this.mByteCount)) {
                Log.i(TAG, "should not download again, download interval:" + this.mDownLoadInterval);
                return null;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(str).openConnection();
                connection.setRequestProperty("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
                connection.setRequestProperty("x-wap-profile", "http://www.openmobilealliance.org/tech/profiles/UAPROF/ccppschema-20021212#");
                connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
                connection.setReadTimeout(120000);
                connection.connect();
                Log.i(TAG, "the connection timeout:" + connection.getConnectTimeout());
                if (connection.getResponseCode() != 200) {
                    Log.i(TAG, "HTTP error downloading gps XTRA: " + statusCode);
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return null;
                }
                in = connection.getInputStream();
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    do {
                        int read = in.read(buffer);
                        int count = read;
                        if (read != -1) {
                            bytes.write(buffer, 0, count);
                        } else {
                            byte[] body = bytes.toByteArray();
                            if (in != null) {
                                in.close();
                            }
                            Log.i(TAG, "the getReadTimeout:" + connection.getReadTimeout());
                            if (this.mByteCount == body.length) {
                                this.mProperties.setProperty("LAST_XTRA_DOWNLOAD_TIME", String.valueOf(SystemClock.elapsedRealtime()));
                                this.mProperties.setProperty("LAST_SUCCESS_XTRA_DATA_SIZE", String.valueOf(this.mByteCount));
                                this.mDownLoadInterval += 600000;
                                if (this.mDownLoadInterval > 7200000) {
                                    this.mDownLoadInterval = 7200000;
                                }
                                this.mProperties.setProperty("HW_XTRA_DOWNLOAD_INTERVAL", String.valueOf(this.mDownLoadInterval));
                                Log.i(TAG, "lto downloader process ok, set download time:" + this.mProperties.getProperty("LAST_XTRA_DOWNLOAD_TIME") + ", set next down interval:" + this.mProperties.getProperty("HW_XTRA_DOWNLOAD_INTERVAL"));
                                if (connection != null) {
                                    connection.disconnect();
                                }
                                return body;
                            }
                            Log.e(TAG, "lto downloader process error");
                            if (connection != null) {
                                connection.disconnect();
                            }
                            return null;
                        }
                    } while (((long) bytes.size()) <= MAXIMUM_CONTENT_LENGTH_BYTES);
                    Log.d(TAG, "XTRA file too large");
                    if (in != null) {
                        in.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    return null;
                } catch (Throwable th4) {
                    th = th3;
                    th2 = th4;
                }
            } catch (IOException ioe) {
                Log.i(TAG, "Error downloading gps XTRA: ", ioe);
                if (connection != null) {
                    connection.disconnect();
                }
                return null;
            } catch (Throwable th5) {
                if (connection != null) {
                    connection.disconnect();
                }
                throw th5;
            }
            if (in != null) {
                if (th != null) {
                    try {
                        in.close();
                    } catch (Throwable th6) {
                        th.addSuppressed(th6);
                    }
                } else {
                    in.close();
                }
            }
            throw th2;
            throw th2;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean shouldDownload(int byteCount) {
        long currenttime = SystemClock.elapsedRealtime();
        boolean z = false;
        if (byteCount == Integer.parseInt(this.mProperties.getProperty("LAST_SUCCESS_XTRA_DATA_SIZE"))) {
            if (currenttime - this.mLastDownloadTime > ((long) this.mDownLoadInterval)) {
                z = true;
            }
            return z;
        }
        this.mDownLoadInterval = 0;
        return true;
    }
}
