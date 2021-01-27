package com.huawei.android.app;

import android.common.HwFrameworkFactory;

public final class HiTraceId {
    public static final int ID_ARRAY_LEN = 16;
    protected huawei.hiview.HiTraceId hitraceIdImpl;

    public HiTraceId() {
        this(new byte[16]);
    }

    public HiTraceId(byte[] idArray) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl = HwFrameworkFactory.getHiTraceId(idArray);
        }
    }

    protected HiTraceId(huawei.hiview.HiTraceId id) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl = id;
        }
    }

    public boolean isValid() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return false;
        }
        return this.hitraceIdImpl.isValid();
    }

    public boolean isFlagEnabled(int flag) {
        if (!HiTrace.isHiTraceSdkExist()) {
            return false;
        }
        return this.hitraceIdImpl.isFlagEnabled(flag);
    }

    public void enableFlag(int flag) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl.enableFlag(flag);
        }
    }

    public int getFlags() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return 0;
        }
        return this.hitraceIdImpl.getFlags();
    }

    public void setFlags(int flags) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl.setFlags(flags);
        }
    }

    public long getChainId() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return 0;
        }
        return this.hitraceIdImpl.getChainId();
    }

    public void setChainId(long chainId) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl.setChainId(chainId);
        }
    }

    public long getSpanId() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return 0;
        }
        return this.hitraceIdImpl.getSpanId();
    }

    public void setSpanId(long spanId) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl.setSpanId(spanId);
        }
    }

    public long getParentSpanId() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return 0;
        }
        return this.hitraceIdImpl.getParentSpanId();
    }

    public void setParentSpanId(long parentSpanId) {
        if (HiTrace.isHiTraceSdkExist()) {
            this.hitraceIdImpl.setParentSpanId(parentSpanId);
        }
    }

    public byte[] toBytes() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return new byte[0];
        }
        return this.hitraceIdImpl.toBytes();
    }

    public String toString() {
        if (!HiTrace.isHiTraceSdkExist()) {
            return "";
        }
        return this.hitraceIdImpl.toString();
    }
}
