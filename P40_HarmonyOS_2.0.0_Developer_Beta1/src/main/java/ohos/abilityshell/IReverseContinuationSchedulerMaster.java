package ohos.abilityshell;

import android.os.Handler;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.IReverseContinuationSchedulerMaster;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public interface IReverseContinuationSchedulerMaster {
    public static final int CONTINUATION_BACK = 2;
    public static final String DESCRIPTOR = "ohos.abilityshell.ReverseContinuationSchedulerMaster";
    public static final int NOTIFY_SLAVE_TERMINATED = 1;

    boolean continuationBack(Intent intent);

    void notifySlaveTerminated();

    public static class ReverseContinuationSchedulerMasterStub extends RemoteObject implements IReverseContinuationSchedulerMaster {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ReverseContinuationSchedulerMasterStub");
        private final IReverseContinuationSchedulerMaster delegator;
        private final Handler mainHandler;

        ReverseContinuationSchedulerMasterStub(IReverseContinuationSchedulerMaster iReverseContinuationSchedulerMaster, Handler handler) {
            super("ReverseContinuationSchedulerMasterStub");
            if (iReverseContinuationSchedulerMaster == null) {
                throw new IllegalArgumentException("masterDelegator can not be null.");
            } else if (handler != null) {
                this.delegator = iReverseContinuationSchedulerMaster;
                this.mainHandler = handler;
            } else {
                throw new IllegalArgumentException("mainHandler can not be null.");
            }
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
        public void notifySlaveTerminated() {
            this.delegator.notifySlaveTerminated();
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
        /* renamed from: continuationBack */
        public boolean lambda$onRemoteRequest$0$IReverseContinuationSchedulerMaster$ReverseContinuationSchedulerMasterStub(Intent intent) {
            return this.delegator.continuationBack(intent);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            AppLog.d(LABEL, "onRemoteRequest: code=%{public}d", Integer.valueOf(i));
            if (messageParcel == null || messageParcel2 == null) {
                AppLog.e(LABEL, "onRemoteRequest: Illegal argument null", new Object[0]);
                return false;
            } else if (!IReverseContinuationSchedulerMaster.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
                AppLog.e(LABEL, "onRemoteRequest:: token is invalid.", new Object[0]);
                return false;
            } else if (i == 1) {
                this.mainHandler.post(new Runnable() {
                    /* class ohos.abilityshell.$$Lambda$JXJyDWovGiB8KCvDptO1NxCfcSA */

                    @Override // java.lang.Runnable
                    public final void run() {
                        IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterStub.this.notifySlaveTerminated();
                    }
                });
                return true;
            } else if (i != 2) {
                AppLog.w(LABEL, "onRemoteRequest: Unknown code=%{public}d", Integer.valueOf(i));
                return IReverseContinuationSchedulerMaster.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                Intent intent = new Intent();
                messageParcel.readSequenceable(intent);
                this.mainHandler.post(new Runnable(intent) {
                    /* class ohos.abilityshell.$$Lambda$IReverseContinuationSchedulerMaster$ReverseContinuationSchedulerMasterStub$sS_PCDXq9m_Ti5ZtgQnMsi6Hc */
                    private final /* synthetic */ Intent f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterStub.this.lambda$onRemoteRequest$0$IReverseContinuationSchedulerMaster$ReverseContinuationSchedulerMasterStub(this.f$1);
                    }
                });
                return true;
            }
        }
    }

    public static class ReverseContinuationSchedulerMasterProxy implements IReverseContinuationSchedulerMaster {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ReverseContinuationSchedulerMasterProxy");
        private IRemoteObject remoteObject;

        ReverseContinuationSchedulerMasterProxy(IRemoteObject iRemoteObject) {
            this.remoteObject = iRemoteObject;
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
        public void notifySlaveTerminated() {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (obtain.writeInterfaceToken(IReverseContinuationSchedulerMaster.DESCRIPTOR)) {
                try {
                    if (!this.remoteObject.sendRequest(1, obtain, obtain2, messageOption)) {
                        AppLog.e(LABEL, "notifySlaveTerminated: sendRequest return false", new Object[0]);
                    }
                } catch (RemoteException e) {
                    AppLog.e(LABEL, "notifySlaveTerminated: RemoteException %{public}s", e.getMessage());
                } catch (Throwable th) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    throw th;
                }
                obtain.reclaim();
                obtain2.reclaim();
            }
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
        public boolean continuationBack(Intent intent) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (!obtain.writeInterfaceToken(IReverseContinuationSchedulerMaster.DESCRIPTOR)) {
                return false;
            }
            obtain.writeSequenceable(intent);
            try {
                if (!this.remoteObject.sendRequest(2, obtain, obtain2, messageOption)) {
                    AppLog.e(LABEL, "continuationBack: sendRequest return false", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException e) {
                AppLog.e(LABEL, "continuationBack: RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }
}
