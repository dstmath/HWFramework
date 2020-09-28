package huawei.hiview;

public class HiTraceDummy implements HiTrace {
    @Override // huawei.hiview.HiTrace
    public HiTraceId begin(String name, int flags) {
        return new HiTraceIdDummy();
    }

    @Override // huawei.hiview.HiTrace
    public void end(HiTraceId hiTraceId) {
    }

    @Override // huawei.hiview.HiTrace
    public HiTraceId getId() {
        return new HiTraceIdDummy();
    }

    @Override // huawei.hiview.HiTrace
    public void setId(HiTraceId id) {
    }

    @Override // huawei.hiview.HiTrace
    public void clearId() {
    }

    @Override // huawei.hiview.HiTrace
    public HiTraceId createSpan() {
        return new HiTraceIdDummy();
    }

    @Override // huawei.hiview.HiTrace
    public void tracePoint(int type, HiTraceId traceId, String fmt, Object... args) {
    }
}
