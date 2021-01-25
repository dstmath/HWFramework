package ohos.abilityshell;

import android.os.Handler;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public class ContinuationSchedulerForDmsStub extends RemoteObject implements IContinuationSchedulerForDms, IRemoteBroker {
    private static final String DESCRIPTOR = "ohos.abilityshell.ContinuationScheduler";
    private static final int RECEIVE_SLAVE_SCHEDULER = 3;
    private static final int SCHEDULE_COMPLETE_CONTINUATION = 2;
    private static final int SCHEDULE_START_CONTINUATION = 1;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private final IDistributeScheduleHandler callback;
    private final Handler mainHandler;

    public interface IDistributeScheduleHandler {
        void handleCompleteContinuation(int i);

        void handleReceiveRemoteScheduler(IRemoteObject iRemoteObject);

        int handleStartContinuation(IRemoteObject iRemoteObject, String str);
    }

    public IRemoteObject asObject() {
        return this;
    }

    ContinuationSchedulerForDmsStub(IDistributeScheduleHandler iDistributeScheduleHandler, Handler handler) {
        super("");
        if (handler != null) {
            this.callback = iDistributeScheduleHandler;
            this.mainHandler = handler;
            return;
        }
        throw new IllegalArgumentException("Handler can not be null.");
    }

    @Override // ohos.abilityshell.IContinuationSchedulerForDms
    /* renamed from: scheduleStartContinuation */
    public int lambda$onRemoteRequest$0$ContinuationSchedulerForDmsStub(IRemoteObject iRemoteObject, String str) {
        IDistributeScheduleHandler iDistributeScheduleHandler = this.callback;
        if (iDistributeScheduleHandler != null) {
            return iDistributeScheduleHandler.handleStartContinuation(iRemoteObject, str);
        }
        return -1;
    }

    @Override // ohos.abilityshell.IContinuationSchedulerForDms
    /* renamed from: scheduleCompleteContinuation */
    public void lambda$onRemoteRequest$1$ContinuationSchedulerForDmsStub(int i) {
        IDistributeScheduleHandler iDistributeScheduleHandler = this.callback;
        if (iDistributeScheduleHandler != null) {
            iDistributeScheduleHandler.handleCompleteContinuation(i);
        }
    }

    @Override // ohos.abilityshell.IContinuationSchedulerForDms
    /* renamed from: receiveSlaveScheduler */
    public void lambda$onRemoteRequest$2$ContinuationSchedulerForDmsStub(IRemoteObject iRemoteObject) {
        IDistributeScheduleHandler iDistributeScheduleHandler = this.callback;
        if (iDistributeScheduleHandler != null) {
            iDistributeScheduleHandler.handleReceiveRemoteScheduler(iRemoteObject);
        }
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        AppLog.d(SHELL_LABEL, "ContinuationSchedulerForDmsStub::onRemoteRequest code=%{public}d", Integer.valueOf(i));
        if (messageParcel == null || messageParcel2 == null) {
            AppLog.e(SHELL_LABEL, "ContinuationSchedulerForDmsStub::onRemoteRequest null", new Object[0]);
            return false;
        } else if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e(SHELL_LABEL, "ContinuationSchedulerForDmsStub::onRemoteRequest token is invalid.", new Object[0]);
            return false;
        } else {
            if (i == 1) {
                AppLog.i(SHELL_LABEL, "ContinuationSchedulerForDmsStub::SCHEDULE_START_CONTINUATION receive", new Object[0]);
                IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
                if (readRemoteObject == null) {
                    if (!messageParcel2.writeInt(-3)) {
                        return false;
                    }
                } else if (!messageParcel2.writeInt(0)) {
                    return false;
                } else {
                    this.mainHandler.post(new Runnable(readRemoteObject, messageParcel.readString()) {
                        /* class ohos.abilityshell.$$Lambda$ContinuationSchedulerForDmsStub$jWyVHUGDCldBPah6GhioDxvPgvI */
                        private final /* synthetic */ IRemoteObject f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            ContinuationSchedulerForDmsStub.this.lambda$onRemoteRequest$0$ContinuationSchedulerForDmsStub(this.f$1, this.f$2);
                        }
                    });
                }
            } else if (i == 2) {
                int readInt = messageParcel.readInt();
                AppLog.i(SHELL_LABEL, "ContinuationSchedulerForDmsStub::SCHEDULE_COMPLETE_CONTINUATION receive. Result: %{public}d", Integer.valueOf(readInt));
                this.mainHandler.post(new Runnable(readInt) {
                    /* class ohos.abilityshell.$$Lambda$ContinuationSchedulerForDmsStub$mtbYPdUcJhkrwMUsb7vJ_WhLRgw */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ContinuationSchedulerForDmsStub.this.lambda$onRemoteRequest$1$ContinuationSchedulerForDmsStub(this.f$1);
                    }
                });
            } else if (i != 3) {
                AppLog.w(SHELL_LABEL, "ContinuationSchedulerForDmsStub::onRemoteRequest unknown code", new Object[0]);
                return ContinuationSchedulerForDmsStub.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                AppLog.i(SHELL_LABEL, "ContinuationSchedulerForDmsStub::RECEIVE_REMOTE_SCHEDULER receive", new Object[0]);
                IRemoteObject readRemoteObject2 = messageParcel.readRemoteObject();
                if (readRemoteObject2 == null) {
                    AppLog.e(SHELL_LABEL, "ContinuationSchedulerForDmsStub::RECEIVE_REMOTE_SCHEDULER null", new Object[0]);
                    messageParcel2.writeInt(-3);
                    return false;
                }
                this.mainHandler.post(new Runnable(readRemoteObject2) {
                    /* class ohos.abilityshell.$$Lambda$ContinuationSchedulerForDmsStub$wIZx6fCqmX2j_Ro5ek6GRb9Gcl0 */
                    private final /* synthetic */ IRemoteObject f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ContinuationSchedulerForDmsStub.this.lambda$onRemoteRequest$2$ContinuationSchedulerForDmsStub(this.f$1);
                    }
                });
            }
            return true;
        }
    }
}
