package ohos.abilityshell;

import android.os.Handler;
import ohos.abilityshell.IReverseContinuationSchedulerSlave;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public interface IReverseContinuationSchedulerSlave {
    public static final String DESCRIPTOR = "ohos.abilityshell.ReverseContinuationSchedulerSlave";
    public static final int NOTIFY_REVERSE_RESULT = 3;
    public static final int PASS_MASTER = 1;
    public static final int REVERSE_CONTINUATION = 2;

    void notifyReverseResult(int i);

    void passMaster(IRemoteObject iRemoteObject);

    boolean reverseContinuation();

    public static class ReverseContinuationSchedulerSlaveStub extends RemoteObject implements IReverseContinuationSchedulerSlave {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ReverseContinuationSchedulerSlaveStub");
        private final IReverseContinuationSchedulerSlave delegator;
        private final Handler mainHandler;

        ReverseContinuationSchedulerSlaveStub(IReverseContinuationSchedulerSlave iReverseContinuationSchedulerSlave, Handler handler) {
            super("ReverseContinuationSchedulerSlaveStub");
            if (iReverseContinuationSchedulerSlave == null) {
                throw new IllegalArgumentException("slaveDelegator can not be null.");
            } else if (handler != null) {
                this.delegator = iReverseContinuationSchedulerSlave;
                this.mainHandler = handler;
            } else {
                throw new IllegalArgumentException("mainHandler can not be null.");
            }
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        /* renamed from: passMaster */
        public void lambda$onRemoteRequest$0$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub(IRemoteObject iRemoteObject) {
            this.delegator.passMaster(iRemoteObject);
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        public boolean reverseContinuation() {
            return this.delegator.reverseContinuation();
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        /* renamed from: notifyReverseResult */
        public void lambda$onRemoteRequest$1$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub(int i) {
            this.delegator.notifyReverseResult(i);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            AppLog.d(LABEL, "onRemoteRequest: code=%{public}d", Integer.valueOf(i));
            if (messageParcel == null || messageParcel2 == null) {
                AppLog.e(LABEL, "onRemoteRequest: Illegal argument null", new Object[0]);
                return false;
            } else if (!IReverseContinuationSchedulerSlave.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
                AppLog.e(LABEL, "onRemoteRequest:: token is invalid.", new Object[0]);
                return false;
            } else if (i == 1) {
                this.mainHandler.post(new Runnable(messageParcel.readRemoteObject()) {
                    /* class ohos.abilityshell.$$Lambda$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub$7VoTfyPEEo8oQl_WvEMwLT95hXo */
                    private final /* synthetic */ IRemoteObject f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveStub.this.lambda$onRemoteRequest$0$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub(this.f$1);
                    }
                });
                return true;
            } else if (i == 2) {
                this.mainHandler.post(new Runnable() {
                    /* class ohos.abilityshell.$$Lambda$Dtudsv045e0SFlqzvwaoV2s5vA */

                    @Override // java.lang.Runnable
                    public final void run() {
                        IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveStub.this.reverseContinuation();
                    }
                });
                return true;
            } else if (i != 3) {
                AppLog.w(LABEL, "onRemoteRequest: Unknown code=%{public}d", Integer.valueOf(i));
                return IReverseContinuationSchedulerSlave.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                this.mainHandler.post(new Runnable(messageParcel.readInt()) {
                    /* class ohos.abilityshell.$$Lambda$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub$N2n4iNr4hcl2greXEHlmRWZA1Gc */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveStub.this.lambda$onRemoteRequest$1$IReverseContinuationSchedulerSlave$ReverseContinuationSchedulerSlaveStub(this.f$1);
                    }
                });
                return true;
            }
        }
    }

    public static class ReverseContinuationSchedulerSlaveProxy implements IReverseContinuationSchedulerSlave, IRemoteBroker {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ReverseContinuationSchedulerSlaveProxy");
        private IRemoteObject remoteObject;

        ReverseContinuationSchedulerSlaveProxy(IRemoteObject iRemoteObject) {
            this.remoteObject = iRemoteObject;
        }

        public IRemoteObject asObject() {
            return this.remoteObject;
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        public void passMaster(IRemoteObject iRemoteObject) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (obtain.writeInterfaceToken(IReverseContinuationSchedulerSlave.DESCRIPTOR) && obtain.writeRemoteObject(iRemoteObject)) {
                try {
                    if (!this.remoteObject.sendRequest(1, obtain, obtain2, messageOption)) {
                        AppLog.e(LABEL, "passMaster: sendRequest return false", new Object[0]);
                    }
                } catch (RemoteException e) {
                    AppLog.e(LABEL, "passMaster: RemoteException %{public}s", e.getMessage());
                } catch (Throwable th) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    throw th;
                }
                obtain.reclaim();
                obtain2.reclaim();
            }
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        public boolean reverseContinuation() {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (!obtain.writeInterfaceToken(IReverseContinuationSchedulerSlave.DESCRIPTOR)) {
                return false;
            }
            try {
                if (!this.remoteObject.sendRequest(2, obtain, obtain2, messageOption)) {
                    AppLog.e(LABEL, "reverseContinuation: sendRequest return false", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException e) {
                AppLog.e(LABEL, "reverseContinuation: RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }

        @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
        public void notifyReverseResult(int i) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (obtain.writeInterfaceToken(IReverseContinuationSchedulerSlave.DESCRIPTOR) && obtain.writeInt(i)) {
                try {
                    if (!this.remoteObject.sendRequest(3, obtain, obtain2, messageOption)) {
                        AppLog.e(LABEL, "notifyReverseResult: sendRequest return false", new Object[0]);
                    }
                } catch (RemoteException e) {
                    AppLog.e(LABEL, "notifyReverseResult: RemoteException %{public}s", e.getMessage());
                } catch (Throwable th) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    throw th;
                }
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }
}
