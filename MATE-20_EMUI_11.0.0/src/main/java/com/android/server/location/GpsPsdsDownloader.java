package com.android.server.location;

import android.net.TrafficStats;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GpsPsdsDownloader {
    private static final int CONNECTION_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(30));
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_USER_AGENT = "Android";
    private static final long MAXIMUM_CONTENT_LENGTH_BYTES = 1000000;
    private static final int READ_TIMEOUT_MS = ((int) TimeUnit.SECONDS.toMillis(60));
    private static final String TAG = "GpsPsdsDownloader";
    private int mByteCount;
    private IHwGpsLogServices mHwGpsLogServices;
    private int mNextServerIndex;
    private final String[] mPsdsServers;
    private final String mUserAgent;

    GpsPsdsDownloader(Properties properties) {
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
            Log.e(TAG, "No PSDS servers were specified in the GPS configuration");
            this.mPsdsServers = null;
        } else {
            this.mPsdsServers = new String[count];
            int count2 = 0;
            if (server1 != null) {
                this.mPsdsServers[0] = server1;
                count2 = 0 + 1;
            }
            if (server2 != null) {
                this.mPsdsServers[count2] = server2;
                count2++;
            }
            if (server3 != null) {
                this.mPsdsServers[count2] = server3;
                count2++;
            }
            this.mNextServerIndex = new Random().nextInt(count2);
        }
        this.mHwGpsLogServices = HwServiceFactory.getNewHwGpsLogService();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public byte[] downloadPsdsData() {
        byte[] result = null;
        int startIndex = this.mNextServerIndex;
        if (this.mPsdsServers == null) {
            return null;
        }
        while (result == null) {
            int oldTag = TrafficStats.getAndSetThreadStatsTag(-188);
            try {
                result = doDownload(this.mPsdsServers[this.mNextServerIndex]);
                TrafficStats.setThreadStatsTag(oldTag);
                this.mNextServerIndex++;
                if (this.mNextServerIndex == this.mPsdsServers.length) {
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
        if (this.mHwGpsLogServices != null) {
            boolean isXtraDownloadSuccess = true;
            if (result == null && this.mByteCount != -1) {
                isXtraDownloadSuccess = false;
            }
            this.mHwGpsLogServices.updateXtraDloadStatus(isXtraDownloadSuccess);
            if (isXtraDownloadSuccess) {
                this.mHwGpsLogServices.injectExtraParam("extra_data");
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b0, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b1, code lost:
        if (r4 != null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b7, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b8, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bb, code lost:
        throw r6;
     */
    public byte[] doDownload(String url) {
        if (DEBUG) {
            Log.d(TAG, "Downloading PSDS data from " + url);
        }
        HttpURLConnection connection = null;
        try {
            HttpURLConnection connection2 = (HttpURLConnection) new URL(url).openConnection();
            connection2.setRequestProperty("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
            connection2.setRequestProperty("x-wap-profile", "http://www.openmobilealliance.org/tech/profiles/UAPROF/ccppschema-20021212#");
            connection2.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection2.setReadTimeout(READ_TIMEOUT_MS);
            connection2.connect();
            int statusCode = connection2.getResponseCode();
            if (statusCode != 200) {
                if (DEBUG) {
                    Log.d(TAG, "HTTP error downloading gps PSDS: " + statusCode);
                }
                connection2.disconnect();
                return null;
            }
            InputStream in = connection2.getInputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            do {
                int count = in.read(buffer);
                if (count != -1) {
                    bytes.write(buffer, 0, count);
                } else {
                    byte[] byteArray = bytes.toByteArray();
                    in.close();
                    connection2.disconnect();
                    return byteArray;
                }
            } while (((long) bytes.size()) <= MAXIMUM_CONTENT_LENGTH_BYTES);
            if (DEBUG) {
                Log.d(TAG, "PSDS file too large");
            }
            in.close();
            connection2.disconnect();
            return null;
        } catch (IOException ioe) {
            if (DEBUG) {
                Log.d(TAG, "Error downloading gps PSDS: ", ioe);
            }
            if (0 != 0) {
                connection.disconnect();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                connection.disconnect();
            }
            throw th;
        }
    }
}
