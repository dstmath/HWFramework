package ohos.data.async;

import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.Context;
import ohos.data.AbsPredicates;
import ohos.data.async.AbsAsyncHandler;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.net.Uri;

public class DataAbilityAsyncHandler extends AbsAsyncHandler {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "DataAbilityAsyncHandler");
    private final DataAbilityHelper dataAbilityHelper;

    public DataAbilityAsyncHandler(Context context) {
        this.dataAbilityHelper = DataAbilityHelper.creator(context);
    }

    public DataAbilityAsyncHandler(Context context, Uri uri) {
        this.dataAbilityHelper = DataAbilityHelper.creator(context, uri);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.data.async.AbsAsyncHandler
    public AbsAsyncHandler.AbsJobHandler createJobHandler() {
        return new JobHandler();
    }

    class JobHandler extends AbsAsyncHandler.AbsJobHandler {
        JobHandler() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.data.async.AbsAsyncHandler.AbsJobHandler, ohos.eventhandler.EventHandler
        public void processEvent(InnerEvent innerEvent) {
            HiLog.info(DataAbilityAsyncHandler.LABEL, "Start processEvent in DataAbilityAsyncHandler.", new Object[0]);
            int i = innerEvent.eventId;
            int i2 = (int) innerEvent.param;
            DataAbilityAsyncHandler.this.sendEvent(InnerEvent.get(i, (long) i2, processDataAbility(i2, (AbsAsyncHandler.JobInfo) innerEvent.object, DataAbilityAsyncHandler.this.dataAbilityHelper)));
            HiLog.info(DataAbilityAsyncHandler.LABEL, "Finish processEvent in DataAbilityAsyncHandler.", new Object[0]);
        }

        private AbsAsyncHandler.JobInfo processDataAbility(int i, AbsAsyncHandler.JobInfo jobInfo, DataAbilityHelper dataAbilityHelper) {
            if (i == 1) {
                try {
                    jobInfo.result = dataAbilityHelper.query(jobInfo.uri, jobInfo.columns, (DataAbilityPredicates) jobInfo.predicates);
                } catch (RuntimeException | DataAbilityRemoteException e) {
                    HiLog.error(DataAbilityAsyncHandler.LABEL, "Async query data occur an exception %{private}s", new Object[]{e});
                    jobInfo.result = null;
                }
            } else if (i == 2) {
                try {
                    jobInfo.result = Integer.valueOf(dataAbilityHelper.insert(jobInfo.uri, jobInfo.valuesBucket));
                } catch (RuntimeException | DataAbilityRemoteException e2) {
                    HiLog.error(DataAbilityAsyncHandler.LABEL, "Async insert data occur an exception %{private}s", new Object[]{e2});
                    jobInfo.result = null;
                }
            } else if (i != 3) {
                try {
                    jobInfo.result = Integer.valueOf(dataAbilityHelper.delete(jobInfo.uri, (DataAbilityPredicates) jobInfo.predicates));
                } catch (RuntimeException | DataAbilityRemoteException e3) {
                    HiLog.error(DataAbilityAsyncHandler.LABEL, "Async delete data occur an exception %{private}s", new Object[]{e3});
                    jobInfo.result = null;
                }
            } else {
                try {
                    jobInfo.result = Integer.valueOf(dataAbilityHelper.update(jobInfo.uri, jobInfo.valuesBucket, (DataAbilityPredicates) jobInfo.predicates));
                } catch (RuntimeException | DataAbilityRemoteException e4) {
                    HiLog.error(DataAbilityAsyncHandler.LABEL, "Async update data occur an exception %{private}s", new Object[]{e4});
                    jobInfo.result = null;
                }
            }
            return jobInfo;
        }
    }

    public final void addQueryJob(JobParams jobParams, Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates, QueryCallback queryCallback) {
        checkUri(uri);
        super.addQueryJob(jobParams, uri, strArr, (AbsPredicates) dataAbilityPredicates, queryCallback);
    }

    public final void addInsertJob(JobParams jobParams, Uri uri, ValuesBucket valuesBucket, InsertCallback insertCallback) {
        checkUri(uri);
        super.addInsertJob(jobParams, uri, null, valuesBucket, insertCallback);
    }

    public final void addUpdateJob(JobParams jobParams, Uri uri, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates, UpdateCallback updateCallback) {
        checkUri(uri);
        super.addUpdateJob(jobParams, uri, valuesBucket, (AbsPredicates) dataAbilityPredicates, updateCallback);
    }

    public final void addDeleteJob(JobParams jobParams, Uri uri, DataAbilityPredicates dataAbilityPredicates, DeleteCallback deleteCallback) {
        checkUri(uri);
        super.addDeleteJob(jobParams, uri, (AbsPredicates) dataAbilityPredicates, deleteCallback);
    }

    private void checkUri(Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Value of uri should not be null.");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        this.dataAbilityHelper.release();
    }
}
