package ohos.backgroundtaskmgr;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class BackgroundTaskManagerProxy implements IBackgroundTaskManager {
    private static final int INVALID_ID = -1;
    private static final int LOG_DOMAIN = 218109696;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final String TAG = "BackgroundTaskManagerProxy";
    private static final String TRANSACT_EXCEPTION = "Send Request Error";
    private static BackgroundTaskManagerProxy proxy = new BackgroundTaskManagerProxy();
    private final Object remoteLock = new Object();
    private IRemoteObject remoteObj;
    private boolean serviceValid = false;

    private BackgroundTaskManagerProxy() {
    }

    public static BackgroundTaskManagerProxy getProxy() {
        return proxy;
    }

    public IRemoteObject asObject() {
        synchronized (this.remoteLock) {
            if (this.remoteObj == null || !this.serviceValid) {
                this.remoteObj = SysAbilityManager.getSysAbility(1903);
                if (this.remoteObj == null) {
                    return this.remoteObj;
                }
                this.serviceValid = true;
                this.remoteObj.addDeathRecipient(new BackgroundTaskManagerDeathRecipient(), 0);
                return this.remoteObj;
            }
            return this.remoteObj;
        }
    }

    @Override // ohos.backgroundtaskmgr.IBackgroundTaskManager
    public DelaySuspendInfo requestSuspendDelay(Context context, String str, ExpiredCallback expiredCallback) throws RemoteException {
        int readInt;
        int readInt2;
        if (context == null || expiredCallback == null) {
            throw new NullPointerException("params should not be null.");
        }
        String bundleName = context.getBundleName();
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        synchronized (this.remoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject != null) {
                try {
                    if (!obtain.writeInterfaceToken(IBackgroundTaskManager.DESCRIPTOR)) {
                        throw new RemoteException();
                    } else if (!obtain.writeString(bundleName)) {
                        throw new RemoteException();
                    } else if (!obtain.writeString(str)) {
                        throw new RemoteException();
                    } else if (obtain.writeRemoteObject(expiredCallback.getCallback().asObject())) {
                        this.serviceValid = asObject.sendRequest(3, obtain, obtain2, new MessageOption());
                        if (this.serviceValid) {
                            readInt = obtain2.readInt();
                            readInt2 = obtain2.readInt();
                            obtain.reclaim();
                            obtain2.reclaim();
                        } else {
                            throw new RemoteException(TRANSACT_EXCEPTION);
                        }
                    } else {
                        throw new RemoteException();
                    }
                } catch (RemoteException e) {
                    throw e;
                } catch (Throwable th) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    throw th;
                }
            } else {
                throw new RemoteException(TRANSACT_EXCEPTION);
            }
        }
        return new DelaySuspendInfo(readInt, readInt2);
    }

    @Override // ohos.backgroundtaskmgr.IBackgroundTaskManager
    public void cancelSuspendDelay(Context context, int i) throws RemoteException {
        if (context == null) {
            throw new NullPointerException("params should not be null.");
        } else if (i != -1) {
            String bundleName = context.getBundleName();
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            synchronized (this.remoteLock) {
                IRemoteObject asObject = asObject();
                if (asObject != null) {
                    try {
                        if (!obtain.writeInterfaceToken(IBackgroundTaskManager.DESCRIPTOR)) {
                            throw new RemoteException();
                        } else if (!obtain.writeString(bundleName)) {
                            throw new RemoteException();
                        } else if (obtain.writeInt(i)) {
                            this.serviceValid = asObject.sendRequest(4, obtain, obtain2, new MessageOption());
                            if (this.serviceValid) {
                                obtain.reclaim();
                                obtain2.reclaim();
                            } else {
                                throw new RemoteException(TRANSACT_EXCEPTION);
                            }
                        } else {
                            throw new RemoteException();
                        }
                    } catch (RemoteException e) {
                        throw e;
                    } catch (Throwable th) {
                        obtain.reclaim();
                        obtain2.reclaim();
                        throw th;
                    }
                } else {
                    throw new RemoteException(TRANSACT_EXCEPTION);
                }
            }
        }
    }

    @Override // ohos.backgroundtaskmgr.IBackgroundTaskManager
    public int getRemainingDelayTime(Context context, int i) throws RemoteException {
        int readInt;
        if (context == null) {
            throw new NullPointerException("params should not be null.");
        } else if (i == -1) {
            return 0;
        } else {
            String bundleName = context.getBundleName();
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            synchronized (this.remoteLock) {
                IRemoteObject asObject = asObject();
                if (asObject != null) {
                    try {
                        if (!obtain.writeInterfaceToken(IBackgroundTaskManager.DESCRIPTOR)) {
                            throw new RemoteException();
                        } else if (!obtain.writeString(bundleName)) {
                            throw new RemoteException();
                        } else if (obtain.writeInt(i)) {
                            this.serviceValid = asObject.sendRequest(5, obtain, obtain2, new MessageOption());
                            if (this.serviceValid) {
                                readInt = obtain2.readInt();
                                obtain.reclaim();
                                obtain2.reclaim();
                            } else {
                                throw new RemoteException(TRANSACT_EXCEPTION);
                            }
                        } else {
                            throw new RemoteException();
                        }
                    } catch (RemoteException e) {
                        throw e;
                    } catch (Throwable th) {
                        obtain.reclaim();
                        obtain2.reclaim();
                        throw th;
                    }
                } else {
                    throw new RemoteException();
                }
            }
            return readInt;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.remoteLock) {
            getProxy().remoteObj = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public static class BackgroundTaskManagerDeathRecipient implements IRemoteObject.DeathRecipient {
        private BackgroundTaskManagerDeathRecipient() {
        }

        public void onRemoteDied() {
            BackgroundTaskManagerProxy.getProxy().setRemoteObject(null);
            HiLog.info(BackgroundTaskManagerProxy.LOG_LABEL, "onRemoteDied", new Object[0]);
        }
    }
}
