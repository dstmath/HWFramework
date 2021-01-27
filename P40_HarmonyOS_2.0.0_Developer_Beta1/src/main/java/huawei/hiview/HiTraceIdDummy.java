package huawei.hiview;

public class HiTraceIdDummy implements HiTraceId {
    @Override // huawei.hiview.HiTraceId
    public boolean isValid() {
        return false;
    }

    @Override // huawei.hiview.HiTraceId
    public boolean isFlagEnabled(int flag) {
        return false;
    }

    @Override // huawei.hiview.HiTraceId
    public void enableFlag(int flag) {
    }

    @Override // huawei.hiview.HiTraceId
    public int getFlags() {
        return 0;
    }

    @Override // huawei.hiview.HiTraceId
    public void setFlags(int flags) {
    }

    @Override // huawei.hiview.HiTraceId
    public long getChainId() {
        return 0;
    }

    @Override // huawei.hiview.HiTraceId
    public void setChainId(long chainId) {
    }

    @Override // huawei.hiview.HiTraceId
    public long getSpanId() {
        return 0;
    }

    @Override // huawei.hiview.HiTraceId
    public void setSpanId(long spanId) {
    }

    @Override // huawei.hiview.HiTraceId
    public long getParentSpanId() {
        return 0;
    }

    @Override // huawei.hiview.HiTraceId
    public void setParentSpanId(long parentSpanId) {
    }

    @Override // huawei.hiview.HiTraceId
    public byte[] toBytes() {
        return new byte[0];
    }
}
