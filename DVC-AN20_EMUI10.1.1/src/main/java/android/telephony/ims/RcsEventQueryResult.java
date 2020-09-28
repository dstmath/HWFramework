package android.telephony.ims;

import java.util.List;

public class RcsEventQueryResult {
    private RcsQueryContinuationToken mContinuationToken;
    private List<RcsEvent> mEvents;

    public RcsEventQueryResult(RcsQueryContinuationToken continuationToken, List<RcsEvent> events) {
        this.mContinuationToken = continuationToken;
        this.mEvents = events;
    }

    public RcsQueryContinuationToken getContinuationToken() {
        return this.mContinuationToken;
    }

    public List<RcsEvent> getEvents() {
        return this.mEvents;
    }
}
