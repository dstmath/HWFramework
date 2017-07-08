package com.android.server.wifi;

import java.io.IOException;

public class WnmData {
    private static final int ESS = 1;
    private final long mBssid;
    private final boolean mDeauthEvent;
    private final int mDelay;
    private final boolean mEss;
    private final int mMethod;
    private final String mUrl;

    public static WnmData buildWnmData(String event) throws IOException {
        boolean z = true;
        String[] segments = event.split(" ");
        if (segments.length < 2) {
            throw new IOException("Short event");
        }
        String str = segments[ESS];
        if (str.equals(WifiMonitor.HS20_SUB_REM_STR)) {
            if (segments.length == 4) {
                return new WnmData(Long.parseLong(segments[0], 16), segments[3], Integer.parseInt(segments[2]));
            }
            throw new IOException("Expected 4 segments");
        } else if (!str.equals(WifiMonitor.HS20_DEAUTH_STR)) {
            throw new IOException("Unknown event type");
        } else if (segments.length != 5) {
            throw new IOException("Expected 5 segments");
        } else {
            int codeID = Integer.parseInt(segments[2]);
            if (codeID < 0 || codeID > ESS) {
                throw new IOException("Unknown code");
            }
            long parseLong = Long.parseLong(segments[0], 16);
            String str2 = segments[4];
            if (codeID != ESS) {
                z = false;
            }
            return new WnmData(parseLong, str2, z, Integer.parseInt(segments[3]));
        }
    }

    private WnmData(long bssid, String url, int method) {
        this.mBssid = bssid;
        this.mUrl = url;
        this.mMethod = method;
        this.mEss = false;
        this.mDelay = -1;
        this.mDeauthEvent = false;
    }

    private WnmData(long bssid, String url, boolean ess, int delay) {
        this.mBssid = bssid;
        this.mUrl = url;
        this.mEss = ess;
        this.mDelay = delay;
        this.mMethod = -1;
        this.mDeauthEvent = true;
    }

    public long getBssid() {
        return this.mBssid;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public boolean isDeauthEvent() {
        return this.mDeauthEvent;
    }

    public int getMethod() {
        return this.mMethod;
    }

    public boolean isEss() {
        return this.mEss;
    }

    public int getDelay() {
        return this.mDelay;
    }
}
