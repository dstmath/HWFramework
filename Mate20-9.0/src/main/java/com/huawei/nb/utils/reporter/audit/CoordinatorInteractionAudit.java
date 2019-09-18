package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public class CoordinatorInteractionAudit extends Audit {
    private static final int EVENT_ID_INTERACTION_EVENT = 942010105;
    private static final short INTERACTION_EVENT_AFTERNOONSUCCESSTIME = 6;
    private static final short INTERACTION_EVENT_AFTERNOON_FAIL_TIME = 11;
    private static final short INTERACTION_EVENT_APP_PACKAGE = 0;
    private static final short INTERACTION_EVENT_DATE = 3;
    private static final short INTERACTION_EVENT_MIDNIGHTSUCCESSTIME = 8;
    private static final short INTERACTION_EVENT_MIDNIGHT_FAIL_TIME = 13;
    private static final short INTERACTION_EVENT_MORNINGSUCCESSTIME = 4;
    private static final short INTERACTION_EVENT_MORNING_FAIL_TIME = 9;
    private static final short INTERACTION_EVENT_NEED_RETRY_TIME = 14;
    private static final short INTERACTION_EVENT_NETWORKSTATE = 2;
    private static final short INTERACTION_EVENT_NIGHTSUCCESSTIME = 7;
    private static final short INTERACTION_EVENT_NIGHT_FAIL_TIME = 12;
    private static final short INTERACTION_EVENT_NOONSUCCESSTIME = 5;
    private static final short INTERACTION_EVENT_NOON_FAIL_TIME = 10;
    private static final short INTERACTION_EVENT_TRANSFER_AVERAGE_DATASIZE = 19;
    private static final short INTERACTION_EVENT_TRANSFER_AVERAGE_TIME = 17;
    private static final short INTERACTION_EVENT_TRANSFER_MAX_DATASIZE = 20;
    private static final short INTERACTION_EVENT_TRANSFER_MAX_TIME = 18;
    private static final short INTERACTION_EVENT_URL = 1;
    private static final short INTERACTION_EVENT_VERIFY_AVERAGE_TIME = 15;
    private static final short INTERACTION_EVENT_VERIFY_MAX_TIME = 16;
    private long afternoonFailTime;
    private long afternoonSuccessTime;
    private String date = null;
    private long midnightFailTime;
    private long midnightSuccessTime;
    private long morningFailTime;
    private long morningSuccessTime;
    private long needRetryTime;
    private String netWorkState = null;
    private long nightFailTime;
    private long nightSuccessTime;
    private long noonFailTime;
    private long noonSuccessTime;
    private String packageName = null;
    private long tranferMaxTime;
    private long transferAverageDatasize;
    private long transferAverageTime;
    private long transferMaxDatasize;
    private String url = null;
    private long verifyAverageTime;
    private long verifyMaxTime;

    private CoordinatorInteractionAudit(String packageName2, String url2, String netWorkState2, String date2, long morningSuccessTime2, long noonSuccessTime2, long afternoonSuccessTime2, long nightSuccessTime2, long midnightSuccessTime2, long morningFailTime2, long noonFailTime2, long afternoonFailTime2, long nightFailTime2, long midnightFailTime2, long needRetryTime2, long verifyAverageTime2, long verifyMaxTime2, long transferAverageTime2, long tranferMaxTime2, long transferAverageDatasize2, long transferMaxDatasize2) {
        super(EVENT_ID_INTERACTION_EVENT);
        this.packageName = packageName2;
        this.url = url2;
        this.netWorkState = netWorkState2;
        this.date = date2;
        this.morningSuccessTime = morningSuccessTime2;
        this.noonSuccessTime = noonSuccessTime2;
        this.afternoonSuccessTime = afternoonSuccessTime2;
        this.nightSuccessTime = nightSuccessTime2;
        this.midnightSuccessTime = midnightSuccessTime2;
        this.morningFailTime = morningFailTime2;
        this.noonFailTime = noonFailTime2;
        this.afternoonFailTime = afternoonFailTime2;
        this.nightFailTime = nightFailTime2;
        this.midnightFailTime = midnightFailTime2;
        this.needRetryTime = needRetryTime2;
        this.verifyAverageTime = verifyAverageTime2;
        this.verifyMaxTime = verifyMaxTime2;
        this.transferAverageTime = transferAverageTime2;
        this.tranferMaxTime = tranferMaxTime2;
        this.transferAverageDatasize = transferAverageDatasize2;
        this.transferMaxDatasize = transferMaxDatasize2;
    }

    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx eventStreamEx = IMonitorEx.openEventStream(EVENT_ID_INTERACTION_EVENT);
        if (eventStreamEx != null) {
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_APP_PACKAGE, this.packageName);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_URL, this.url);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NETWORKSTATE, this.netWorkState);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_DATE, this.date);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_MORNINGSUCCESSTIME, this.morningSuccessTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NOONSUCCESSTIME, this.noonSuccessTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_AFTERNOONSUCCESSTIME, this.afternoonSuccessTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NIGHTSUCCESSTIME, this.nightSuccessTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_MIDNIGHTSUCCESSTIME, this.midnightSuccessTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_MORNING_FAIL_TIME, this.morningFailTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NOON_FAIL_TIME, this.noonFailTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_AFTERNOON_FAIL_TIME, this.afternoonFailTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NIGHT_FAIL_TIME, this.nightFailTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_MIDNIGHT_FAIL_TIME, this.midnightFailTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_NEED_RETRY_TIME, this.needRetryTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_VERIFY_AVERAGE_TIME, this.verifyAverageTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_VERIFY_MAX_TIME, this.verifyMaxTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_TRANSFER_AVERAGE_TIME, this.transferAverageTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_TRANSFER_MAX_TIME, this.tranferMaxTime);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_TRANSFER_AVERAGE_DATASIZE, this.transferAverageDatasize);
            eventStreamEx.setParam(eventStreamEx, INTERACTION_EVENT_TRANSFER_MAX_DATASIZE, this.transferMaxDatasize);
        }
        return eventStreamEx;
    }

    public static void report(String packageName2, String url2, String netWorkState2, String date2, long morningSuccessTime2, long noonSuccessTime2, long afternoonSuccessTime2, long nightSuccessTime2, long midnightSuccessTime2, long morningFailTime2, long noonFailTime2, long afternoonFailTime2, long nightFailTime2, long midnightFailTime2, long needRetryTime2, long verifyAverageTime2, long verifyMaxTime2, long transferAverageTime2, long tranferMaxTime2, long transferAverageDatasize2, long transferMaxDatasize2) {
        Reporter.a(new CoordinatorInteractionAudit(packageName2, url2, netWorkState2, date2, morningSuccessTime2, noonSuccessTime2, afternoonSuccessTime2, nightSuccessTime2, midnightSuccessTime2, morningFailTime2, noonFailTime2, afternoonFailTime2, nightFailTime2, midnightFailTime2, needRetryTime2, verifyAverageTime2, verifyMaxTime2, transferAverageTime2, tranferMaxTime2, transferAverageDatasize2, transferMaxDatasize2));
    }
}
