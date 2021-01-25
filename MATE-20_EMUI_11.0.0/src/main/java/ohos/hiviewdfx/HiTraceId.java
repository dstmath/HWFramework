package ohos.hiviewdfx;

public final class HiTraceId {
    public static final int ID_ARRAY_LEN = 16;
    private HiTraceIdImpl hitraceIdImpl;

    public HiTraceId() {
        this(new byte[16]);
    }

    public HiTraceId(byte[] bArr) {
        this.hitraceIdImpl = new HiTraceIdImpl(bArr);
    }

    public boolean isValid() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return false;
        }
        return hiTraceIdImpl.isValid();
    }

    public boolean isFlagEnabled(int i) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return false;
        }
        return hiTraceIdImpl.isFlagEnabled(i);
    }

    public void enableFlag(int i) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl != null) {
            hiTraceIdImpl.enableFlag(i);
        }
    }

    public int getFlags() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return 0;
        }
        return hiTraceIdImpl.getFlags();
    }

    public void setFlags(int i) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl != null) {
            hiTraceIdImpl.setFlags(i);
        }
    }

    public long getChainId() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return 0;
        }
        return hiTraceIdImpl.getChainId();
    }

    public void setChainId(long j) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl != null) {
            hiTraceIdImpl.setChainId(j);
        }
    }

    public long getSpanId() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return 0;
        }
        return hiTraceIdImpl.getSpanId();
    }

    public void setSpanId(long j) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl != null) {
            hiTraceIdImpl.setSpanId(j);
        }
    }

    public long getParentSpanId() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return 0;
        }
        return hiTraceIdImpl.getParentSpanId();
    }

    public void setParentSpanId(long j) {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl != null) {
            hiTraceIdImpl.setParentSpanId(j);
        }
    }

    public byte[] toBytes() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return new byte[0];
        }
        return hiTraceIdImpl.toBytes();
    }

    public String toString() {
        HiTraceIdImpl hiTraceIdImpl = this.hitraceIdImpl;
        if (hiTraceIdImpl == null) {
            return "";
        }
        return hiTraceIdImpl.toString();
    }
}
