package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public final class CoordinatorInteractionAudit extends Audit {
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

    public CoordinatorInteractionAudit() {
        super(EVENT_ID_INTERACTION_EVENT);
    }

    @Override // com.huawei.nb.utils.reporter.audit.Audit
    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) EVENT_ID_INTERACTION_EVENT);
        if (openEventStream != null) {
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_APP_PACKAGE, this.packageName);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_URL, this.url);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NETWORKSTATE, this.netWorkState);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_DATE, this.date);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_MORNINGSUCCESSTIME, this.morningSuccessTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NOONSUCCESSTIME, this.noonSuccessTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_AFTERNOONSUCCESSTIME, this.afternoonSuccessTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NIGHTSUCCESSTIME, this.nightSuccessTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_MIDNIGHTSUCCESSTIME, this.midnightSuccessTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_MORNING_FAIL_TIME, this.morningFailTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NOON_FAIL_TIME, this.noonFailTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_AFTERNOON_FAIL_TIME, this.afternoonFailTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NIGHT_FAIL_TIME, this.nightFailTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_MIDNIGHT_FAIL_TIME, this.midnightFailTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_NEED_RETRY_TIME, this.needRetryTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_VERIFY_AVERAGE_TIME, this.verifyAverageTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_VERIFY_MAX_TIME, this.verifyMaxTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_TRANSFER_AVERAGE_TIME, this.transferAverageTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_TRANSFER_MAX_TIME, this.tranferMaxTime);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_TRANSFER_AVERAGE_DATASIZE, this.transferAverageDatasize);
            openEventStream.setParam(openEventStream, (short) INTERACTION_EVENT_TRANSFER_MAX_DATASIZE, this.transferMaxDatasize);
        }
        return openEventStream;
    }

    public static void report(CoordinatorInteractionAudit coordinatorInteractionAudit) {
        Reporter.a(coordinatorInteractionAudit);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String getNetWorkState() {
        return this.netWorkState;
    }

    public void setNetWorkState(String str) {
        this.netWorkState = str;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String str) {
        this.date = str;
    }

    public long getMorningSuccessTime() {
        return this.morningSuccessTime;
    }

    public void setMorningSuccessTime(long j) {
        this.morningSuccessTime = j;
    }

    public long getNoonSuccessTime() {
        return this.noonSuccessTime;
    }

    public void setNoonSuccessTime(long j) {
        this.noonSuccessTime = j;
    }

    public long getAfternoonSuccessTime() {
        return this.afternoonSuccessTime;
    }

    public void setAfternoonSuccessTime(long j) {
        this.afternoonSuccessTime = j;
    }

    public long getNightSuccessTime() {
        return this.nightSuccessTime;
    }

    public void setNightSuccessTime(long j) {
        this.nightSuccessTime = j;
    }

    public long getMidnightSuccessTime() {
        return this.midnightSuccessTime;
    }

    public void setMidnightSuccessTime(long j) {
        this.midnightSuccessTime = j;
    }

    public long getMorningFailTime() {
        return this.morningFailTime;
    }

    public void setMorningFailTime(long j) {
        this.morningFailTime = j;
    }

    public long getNoonFailTime() {
        return this.noonFailTime;
    }

    public void setNoonFailTime(long j) {
        this.noonFailTime = j;
    }

    public long getAfternoonFailTime() {
        return this.afternoonFailTime;
    }

    public void setAfternoonFailTime(long j) {
        this.afternoonFailTime = j;
    }

    public long getNightFailTime() {
        return this.nightFailTime;
    }

    public void setNightFailTime(long j) {
        this.nightFailTime = j;
    }

    public long getMidnightFailTime() {
        return this.midnightFailTime;
    }

    public void setMidnightFailTime(long j) {
        this.midnightFailTime = j;
    }

    public long getNeedRetryTime() {
        return this.needRetryTime;
    }

    public void setNeedRetryTime(long j) {
        this.needRetryTime = j;
    }

    public long getVerifyAverageTime() {
        return this.verifyAverageTime;
    }

    public void setVerifyAverageTime(long j) {
        this.verifyAverageTime = j;
    }

    public long getVerifyMaxTime() {
        return this.verifyMaxTime;
    }

    public void setVerifyMaxTime(long j) {
        this.verifyMaxTime = j;
    }

    public long getTransferAverageTime() {
        return this.transferAverageTime;
    }

    public void setTransferAverageTime(long j) {
        this.transferAverageTime = j;
    }

    public long getTranferMaxTime() {
        return this.tranferMaxTime;
    }

    public void setTranferMaxTime(long j) {
        this.tranferMaxTime = j;
    }

    public long getTransferAverageDatasize() {
        return this.transferAverageDatasize;
    }

    public void setTransferAverageDatasize(long j) {
        this.transferAverageDatasize = j;
    }

    public long getTransferMaxDatasize() {
        return this.transferMaxDatasize;
    }

    public void setTransferMaxDatasize(long j) {
        this.transferMaxDatasize = j;
    }
}
