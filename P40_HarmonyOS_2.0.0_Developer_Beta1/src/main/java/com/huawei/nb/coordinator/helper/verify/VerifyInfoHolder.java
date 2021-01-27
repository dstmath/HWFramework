package com.huawei.nb.coordinator.helper.verify;

import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.utils.logger.DSLog;
import java.util.Map;
import java.util.TreeMap;

public final class VerifyInfoHolder {
    private static final Object INSTANCE_LOCK = new Object();
    private static final long MILLISECONDS_IN_A_MINUTE = 60000;
    private static Map<Integer, VerifyInfoHolder> sInstances = new TreeMap();
    private boolean deviceCASentFlag = false;
    private boolean hasToken = false;
    private final Object lock = new Object();
    private long startTime = 0;
    private long timeOut = 0;
    private int verifyMode;
    private String verifyToken = "";

    private VerifyInfoHolder(int i) {
        this.verifyMode = i;
        if (i == 1 || i == 0) {
            this.hasToken = true;
        }
    }

    public static VerifyInfoHolder getInstance(int i) {
        VerifyInfoHolder verifyInfoHolder;
        synchronized (INSTANCE_LOCK) {
            verifyInfoHolder = sInstances.get(Integer.valueOf(i));
            if (verifyInfoHolder == null) {
                verifyInfoHolder = new VerifyInfoHolder(i);
                sInstances.put(Integer.valueOf(i), verifyInfoHolder);
            }
        }
        return verifyInfoHolder;
    }

    public int getVerifyMode() {
        int i;
        synchronized (this.lock) {
            i = this.verifyMode;
        }
        return i;
    }

    public void setVerifyMode(int i) {
        this.verifyMode = i;
    }

    public String getVerifyToken() {
        String str;
        synchronized (this.lock) {
            str = this.verifyToken;
        }
        return str;
    }

    public void setVerifyToken(String str) {
        synchronized (this.lock) {
            this.verifyToken = str;
        }
    }

    public long getStartTime() {
        long j;
        synchronized (this.lock) {
            j = this.startTime;
        }
        return j;
    }

    public void setStartTime(long j) {
        synchronized (this.lock) {
            this.startTime = j;
        }
    }

    public long getTimeOut() {
        long j;
        synchronized (this.lock) {
            j = this.timeOut;
        }
        return j;
    }

    public void setTimeOut(long j) {
        synchronized (this.lock) {
            this.timeOut = j;
        }
    }

    public void setDeviceCASentFlag(boolean z) {
        synchronized (this.lock) {
            this.deviceCASentFlag = z;
        }
    }

    public boolean getDeviceCASentFlag() {
        boolean z;
        synchronized (this.lock) {
            z = this.deviceCASentFlag;
        }
        return z;
    }

    public boolean isHasToken() {
        boolean z;
        synchronized (this.lock) {
            z = this.hasToken;
        }
        return z;
    }

    public boolean isTokenExpired() {
        boolean isExpiried;
        synchronized (this.lock) {
            isExpiried = isExpiried();
        }
        return isExpiried;
    }

    private boolean isExpiried() {
        if (!(this.timeOut == 0 || this.startTime == 0)) {
            if (System.currentTimeMillis() - this.startTime < this.timeOut * 60000) {
                return false;
            }
            this.timeOut = 0;
            this.startTime = 0;
        }
        return true;
    }

    public void updateToken(HttpResponse httpResponse, IVerify iVerify) {
        if (httpResponse != null && iVerify != null) {
            synchronized (this.lock) {
                this.verifyToken = httpResponse.getHeaderValue(iVerify.verifyTokenHeader());
                try {
                    this.timeOut = Long.parseLong(httpResponse.getHeaderValue(IVerifyVar.TIME_OUT_HEADER));
                } catch (NumberFormatException unused) {
                    DSLog.e(" Fail to get verify timeOut: ", new Object[0]);
                }
                this.startTime = System.currentTimeMillis();
                this.deviceCASentFlag = true;
            }
        }
    }
}
