package com.android.server.am;

public class BroadcastQueueExUtils {
    public static BroadcastQueueEx createBroadcastQueueEx(BroadcastQueue bq) {
        return new BroadcastQueueEx(bq);
    }
}
