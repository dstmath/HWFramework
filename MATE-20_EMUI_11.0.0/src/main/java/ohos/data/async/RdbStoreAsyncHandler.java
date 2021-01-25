package ohos.data.async;

import java.lang.ref.WeakReference;
import ohos.data.async.AbsAsyncHandler;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.ValuesBucket;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class RdbStoreAsyncHandler extends AbsAsyncHandler {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "RdbStoreAsyncHandler");
    private final WeakReference<RdbStore> rdbStoreReference;

    public RdbStoreAsyncHandler(RdbStore rdbStore) {
        this.rdbStoreReference = new WeakReference<>(rdbStore);
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
            HiLog.info(RdbStoreAsyncHandler.LABEL, "Start processEvent in RdbStoreAsyncHandler.", new Object[0]);
            RdbStore rdbStore = (RdbStore) RdbStoreAsyncHandler.this.rdbStoreReference.get();
            if (rdbStore == null) {
                HiLog.error(RdbStoreAsyncHandler.LABEL, "Instance of RdbStore is null.", new Object[0]);
                return;
            }
            int i = innerEvent.eventId;
            int i2 = (int) innerEvent.param;
            RdbStoreAsyncHandler.this.sendEvent(InnerEvent.get(i, (long) i2, processRdb(i2, (AbsAsyncHandler.JobInfo) innerEvent.object, rdbStore)));
            HiLog.info(RdbStoreAsyncHandler.LABEL, "Finish processEvent in RdbStoreAsyncHandler.", new Object[0]);
        }

        private AbsAsyncHandler.JobInfo processRdb(int i, AbsAsyncHandler.JobInfo jobInfo, RdbStore rdbStore) {
            RdbPredicates rdbPredicates = (RdbPredicates) jobInfo.predicates;
            if (i == 1) {
                try {
                    jobInfo.result = rdbStore.query(rdbPredicates, jobInfo.columns);
                } catch (RuntimeException e) {
                    HiLog.error(RdbStoreAsyncHandler.LABEL, "Async query data occur an exception %{private}s", new Object[]{e});
                    jobInfo.result = null;
                }
            } else if (i == 2) {
                try {
                    jobInfo.result = Long.valueOf(rdbStore.insert(rdbPredicates.getTableName(), jobInfo.valuesBucket));
                } catch (RuntimeException e2) {
                    HiLog.error(RdbStoreAsyncHandler.LABEL, "Async insert data occur an exception %{private}s", new Object[]{e2});
                    jobInfo.result = null;
                }
            } else if (i != 3) {
                try {
                    jobInfo.result = Integer.valueOf(rdbStore.delete(rdbPredicates));
                } catch (RuntimeException e3) {
                    HiLog.error(RdbStoreAsyncHandler.LABEL, "Async delete data occur an exception %{private}s", new Object[]{e3});
                    jobInfo.result = null;
                }
            } else {
                try {
                    jobInfo.result = Integer.valueOf(rdbStore.update(jobInfo.valuesBucket, rdbPredicates));
                } catch (RuntimeException e4) {
                    HiLog.error(RdbStoreAsyncHandler.LABEL, "Async update data occur an exception %{private}s", new Object[]{e4});
                    jobInfo.result = null;
                }
            }
            return jobInfo;
        }
    }

    public final void addQueryJob(JobParams jobParams, String[] strArr, RdbPredicates rdbPredicates, QueryCallback queryCallback) {
        super.addQueryJob(jobParams, null, strArr, rdbPredicates, queryCallback);
    }

    public final void addInsertJob(JobParams jobParams, String str, ValuesBucket valuesBucket, InsertCallback insertCallback) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Value of tableName should not be null or empty.");
        }
        super.addInsertJob(jobParams, null, new RdbPredicates(str), valuesBucket, insertCallback);
    }

    public final void addUpdateJob(JobParams jobParams, ValuesBucket valuesBucket, RdbPredicates rdbPredicates, UpdateCallback updateCallback) {
        super.addUpdateJob(jobParams, null, valuesBucket, rdbPredicates, updateCallback);
    }

    public final void addDeleteJob(JobParams jobParams, RdbPredicates rdbPredicates, DeleteCallback deleteCallback) {
        super.addDeleteJob(jobParams, null, rdbPredicates, deleteCallback);
    }
}
