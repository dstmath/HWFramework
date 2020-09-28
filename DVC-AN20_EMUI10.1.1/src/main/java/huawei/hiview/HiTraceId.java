package huawei.hiview;

public interface HiTraceId {
    void enableFlag(int i);

    long getChainId();

    int getFlags();

    long getParentSpanId();

    long getSpanId();

    boolean isFlagEnabled(int i);

    boolean isValid();

    void setChainId(long j);

    void setFlags(int i);

    void setParentSpanId(long j);

    void setSpanId(long j);

    byte[] toBytes();
}
