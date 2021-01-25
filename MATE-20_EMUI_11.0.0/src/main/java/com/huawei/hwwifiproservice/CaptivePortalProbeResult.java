package com.huawei.hwwifiproservice;

public final class CaptivePortalProbeResult {
    public static final CaptivePortalProbeResult FAILED = new CaptivePortalProbeResult(FAILED_CODE);
    public static final int FAILED_CODE = 599;
    public static final CaptivePortalProbeResult PARTIAL = new CaptivePortalProbeResult(-1);
    public static final int PARTIAL_CODE = -1;
    public static final int PORTAL_CODE = 302;
    public static final CaptivePortalProbeResult SUCCESS = new CaptivePortalProbeResult(204);
    public static final int SUCCESS_CODE = 204;
    public final String detectUrl;
    public int mHttpResponseCode;
    public final CaptivePortalProbeSpec probeSpec;
    public final String redirectUrl;

    public CaptivePortalProbeResult(int httpResponseCode) {
        this(httpResponseCode, null, null);
    }

    public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl2, String detectUrl2) {
        this(httpResponseCode, redirectUrl2, detectUrl2, null);
    }

    public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl2, String detectUrl2, CaptivePortalProbeSpec probeSpec2) {
        this.mHttpResponseCode = httpResponseCode;
        this.redirectUrl = redirectUrl2;
        this.detectUrl = detectUrl2;
        this.probeSpec = probeSpec2;
    }

    public boolean isSuccessful() {
        return this.mHttpResponseCode == 204;
    }

    public boolean isPortal() {
        int i;
        return !isSuccessful() && (i = this.mHttpResponseCode) >= 200 && i <= 399;
    }

    public boolean isFailed() {
        return !isSuccessful() && !isPortal();
    }
}
