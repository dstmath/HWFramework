package ohos.data.async;

import java.util.Arrays;
import ohos.data.AbsPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.net.Uri;

abstract class AbsAsyncHandler extends EventHandler {
    private static final String CALLBACK_THREAD = "AsyncHelperCallback";
    static final int EVENT_TYPE_DELETE = 4;
    static final int EVENT_TYPE_INSERT = 2;
    static final int EVENT_TYPE_QUERY = 1;
    static final int EVENT_TYPE_UPDATE = 3;
    private static final String JOB_THREAD = "AsyncHelperJob";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "AbsAsyncHandler");
    private static EventRunner callbackRunner;
    private static EventRunner jobRunner = null;
    private EventHandler jobHandler;

    /* access modifiers changed from: package-private */
    public abstract AbsJobHandler createJobHandler();

    static {
        callbackRunner = null;
        synchronized (AbsAsyncHandler.class) {
            if (callbackRunner == null) {
                callbackRunner = EventRunner.create(CALLBACK_THREAD);
            }
        }
    }

    AbsAsyncHandler() {
        super(callbackRunner);
        synchronized (AbsAsyncHandler.class) {
            if (jobRunner == null) {
                jobRunner = EventRunner.create(JOB_THREAD);
            }
        }
        this.jobHandler = createJobHandler();
    }

    abstract class AbsJobHandler extends EventHandler {
        /* access modifiers changed from: protected */
        @Override // ohos.eventhandler.EventHandler
        public void processEvent(InnerEvent innerEvent) {
        }

        AbsJobHandler() {
            super(AbsAsyncHandler.jobRunner);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.eventhandler.EventHandler
    public void processEvent(InnerEvent innerEvent) {
        JobInfo jobInfo = (JobInfo) innerEvent.object;
        int i = (int) innerEvent.param;
        if (i == 1) {
            HiLog.info(LABEL, "start  processEvent for QUERY in AbsAsyncHandler.", new Object[0]);
            ((QueryCallback) jobInfo.callBack).onQueryDone(Integer.valueOf(jobInfo.token), jobInfo.cookie, (ResultSet) jobInfo.result);
            HiLog.info(LABEL, "finish  processEvent for QUERY in AbsAsyncHandler.", new Object[0]);
        } else if (i == 2) {
            HiLog.info(LABEL, "start  processEvent for INSERT in AbsAsyncHandler.", new Object[0]);
            ((InsertCallback) jobInfo.callBack).onInsertDone(Integer.valueOf(jobInfo.token), jobInfo.cookie, jobInfo.result == null ? null : Long.valueOf(((Number) jobInfo.result).longValue()));
            HiLog.info(LABEL, "finish  processEvent for INSERT in AbsAsyncHandler.", new Object[0]);
        } else if (i != 3) {
            HiLog.info(LABEL, "start  processEvent for DELETE in AbsAsyncHandler.", new Object[0]);
            ((DeleteCallback) jobInfo.callBack).onDeleteDone(Integer.valueOf(jobInfo.token), jobInfo.cookie, (Integer) jobInfo.result);
            HiLog.info(LABEL, "finish  processEvent for DELETE in AbsAsyncHandler.", new Object[0]);
        } else {
            HiLog.info(LABEL, "start  processEvent for UPDATE in AbsAsyncHandler.", new Object[0]);
            ((UpdateCallback) jobInfo.callBack).onUpdateDone(Integer.valueOf(jobInfo.token), jobInfo.cookie, (Integer) jobInfo.result);
            HiLog.info(LABEL, "finish  processEvent for UPDATE in AbsAsyncHandler.", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void addQueryJob(JobParams jobParams, Uri uri, String[] strArr, AbsPredicates absPredicates, QueryCallback queryCallback) {
        checkPredicates(absPredicates);
        if (queryCallback != null) {
            this.jobHandler.sendEvent(InnerEvent.get(jobParams.getToken(), 1, new JobInfo.Builder().setToken(jobParams.getToken()).setCookie(jobParams.getCookie()).setUri(uri).setColumns(strArr).setPredicates(absPredicates).setCallBack(queryCallback).build()));
            return;
        }
        throw new IllegalArgumentException("QueryCallback should not be null.");
    }

    /* access modifiers changed from: package-private */
    public void addInsertJob(JobParams jobParams, Uri uri, AbsPredicates absPredicates, ValuesBucket valuesBucket, InsertCallback insertCallback) {
        checkValueBucket(valuesBucket);
        if (insertCallback != null) {
            this.jobHandler.sendEvent(InnerEvent.get(jobParams.getToken(), 2, new JobInfo.Builder().setToken(jobParams.getToken()).setCookie(jobParams.getCookie()).setUri(uri).setValuesBucket(valuesBucket).setPredicates(absPredicates).setCallBack(insertCallback).build()));
            return;
        }
        throw new IllegalArgumentException("InsertCallback should not be null.");
    }

    /* access modifiers changed from: package-private */
    public void addUpdateJob(JobParams jobParams, Uri uri, ValuesBucket valuesBucket, AbsPredicates absPredicates, UpdateCallback updateCallback) {
        checkPredicates(absPredicates);
        checkValueBucket(valuesBucket);
        if (updateCallback != null) {
            this.jobHandler.sendEvent(InnerEvent.get(jobParams.getToken(), 3, new JobInfo.Builder().setToken(jobParams.getToken()).setCookie(jobParams.getCookie()).setUri(uri).setValuesBucket(valuesBucket).setPredicates(absPredicates).setCallBack(updateCallback).build()));
            return;
        }
        throw new IllegalArgumentException("UpdateCallback should not be null.");
    }

    /* access modifiers changed from: package-private */
    public void addDeleteJob(JobParams jobParams, Uri uri, AbsPredicates absPredicates, DeleteCallback deleteCallback) {
        checkPredicates(absPredicates);
        if (deleteCallback != null) {
            this.jobHandler.sendEvent(InnerEvent.get(jobParams.getToken(), 4, new JobInfo.Builder().setToken(jobParams.getToken()).setCookie(jobParams.getCookie()).setUri(uri).setPredicates(absPredicates).setCallBack(deleteCallback).build()));
            return;
        }
        throw new IllegalArgumentException("DeleteCallback should not be null.");
    }

    private void checkPredicates(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Value of predicates should not be null.");
        }
    }

    private void checkValueBucket(ValuesBucket valuesBucket) {
        if (valuesBucket == null || valuesBucket.isEmpty()) {
            throw new IllegalArgumentException("Value of valuesBucket should not be null or empty.");
        }
    }

    public final void cancelJob(int i) {
        this.jobHandler.removeEvent(i);
    }

    /* access modifiers changed from: package-private */
    public static final class JobInfo {
        Object callBack;
        String[] columns;
        Object cookie;
        AbsPredicates predicates;
        Object result;
        int token;
        Uri uri;
        ValuesBucket valuesBucket;

        private JobInfo(Builder builder) {
            this.token = builder.token;
            this.cookie = builder.cookie;
            this.uri = builder.uri;
            this.valuesBucket = builder.valuesBucket;
            this.columns = builder.columns;
            this.predicates = builder.predicates;
            this.callBack = builder.callBack;
            this.result = builder.result;
        }

        static final class Builder {
            private Object callBack;
            private String[] columns;
            private Object cookie;
            private AbsPredicates predicates;
            private Object result;
            private int token;
            private Uri uri;
            private ValuesBucket valuesBucket;

            Builder() {
            }

            /* access modifiers changed from: package-private */
            public Builder setResult(Object obj) {
                this.result = obj;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setValuesBucket(ValuesBucket valuesBucket2) {
                this.valuesBucket = valuesBucket2;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setColumns(String[] strArr) {
                this.columns = strArr;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setCallBack(Object obj) {
                this.callBack = obj;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setPredicates(AbsPredicates absPredicates) {
                this.predicates = absPredicates;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setCookie(Object obj) {
                this.cookie = obj;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setToken(int i) {
                this.token = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setUri(Uri uri2) {
                this.uri = uri2;
                return this;
            }

            /* access modifiers changed from: package-private */
            public JobInfo build() {
                return new JobInfo(this);
            }
        }

        public String toString() {
            return "JobInfo{result=" + this.result + ", valuesBucket=" + this.valuesBucket + ", columns=" + Arrays.toString(this.columns) + ", callBack=" + this.callBack + ", predicates=" + this.predicates + ", cookie=" + this.cookie + ", token=" + this.token + ", uri=" + this.uri + '}';
        }
    }
}
