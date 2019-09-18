package com.huawei.nb.coordinator.helper.verify;

import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.utils.logger.DSLog;
import java.util.Map;
import java.util.TreeMap;

public class VerifyInfoHolder {
    private static final Object INSTANCE_LOCK = new Object();
    private static final long MILLSECONDS_IN_A_MINUTE = 60000;
    private static Map<Integer, VerifyInfoHolder> sInstances = new TreeMap();
    private boolean deviceCASentFlag = false;
    private boolean hasToken = false;
    private final Object lock = new Object();
    private long startTime = 0;
    private long timeOut = 0;
    private int verifyMode = 0;
    private String verifyToken = "";

    private VerifyInfoHolder(int mode) {
        this.verifyMode = mode;
        if (mode == 1 || mode == 0) {
            this.hasToken = true;
        }
    }

    public static VerifyInfoHolder getInstance(int mode) {
        VerifyInfoHolder instance;
        synchronized (INSTANCE_LOCK) {
            instance = sInstances.get(Integer.valueOf(mode));
            if (instance == null) {
                instance = new VerifyInfoHolder(mode);
                sInstances.put(Integer.valueOf(mode), instance);
            }
        }
        return instance;
    }

    public int getVerifyMode() {
        int i;
        synchronized (this.lock) {
            i = this.verifyMode;
        }
        return i;
    }

    public void setVerifyMode(int verifyMode2) {
        this.verifyMode = verifyMode2;
    }

    public String getVerifyToken() {
        String str;
        synchronized (this.lock) {
            str = this.verifyToken;
        }
        return str;
    }

    public void setVerifyToken(String verifyToken2) {
        synchronized (this.lock) {
            this.verifyToken = verifyToken2;
        }
    }

    public long getStartTime() {
        long j;
        synchronized (this.lock) {
            j = this.startTime;
        }
        return j;
    }

    public void setStartTime(long startTime2) {
        synchronized (this.lock) {
            this.startTime = startTime2;
        }
    }

    public long getTimeOut() {
        long j;
        synchronized (this.lock) {
            j = this.timeOut;
        }
        return j;
    }

    public void setTimeOut(long timeOut2) {
        synchronized (this.lock) {
            this.timeOut = timeOut2;
        }
    }

    public void setDeviceCASentFlag(boolean flag) {
        synchronized (this.lock) {
            this.deviceCASentFlag = flag;
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
        if (this.timeOut == 0 || this.startTime == 0) {
            return true;
        }
        if (System.currentTimeMillis() - this.startTime < this.timeOut * 60000) {
            return false;
        }
        this.timeOut = 0;
        this.startTime = 0;
        return true;
    }

    public void updateToken(HttpResponse response, IVerify verify) {
        if (response != null && verify != null) {
            synchronized (this.lock) {
                this.verifyToken = response.getHeaderValue(verify.verifyTokenHeader());
                try {
                    this.timeOut = Long.parseLong(response.getHeaderValue(IVerifyVar.TIME_OUT_HEADER));
                } catch (NumberFormatException e) {
                    DSLog.e(" Fail to get verify timeOut: ", new Object[0]);
                }
                this.startTime = System.currentTimeMillis();
                this.deviceCASentFlag = true;
            }
            return;
        }
        return;
    }
}
