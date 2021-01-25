package ohos.dcall;

import android.content.res.Resources;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

class DistributedCallProxy implements IDistributedCall {
    private static final String DCALL_SA_DESCRIPTOR = "OHOS.DistributedCall.IDistributedCall";
    private static final int RESULT_PERMISSION_DENY = -2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) DistributedCallUtils.LOG_ID_DCALL, "DistributedCallProxy");
    private static final String TELEPHONY_SA_DESCRIPTOR = "OHOS.Telephony.ITelephony";
    private static volatile DistributedCallProxy sInstance = null;
    private IRemoteObject mDistributedCallRemoteService = null;
    private final Object mLock = new Object();
    private IRemoteObject mTelephonyRemoteService = null;

    static {
        try {
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(TAG, "Load library libipc_core.z.so failed.", new Object[0]);
        }
    }

    private DistributedCallProxy() {
    }

    public static DistributedCallProxy getInstance() {
        if (sInstance == null) {
            synchronized (DistributedCallProxy.class) {
                if (sInstance == null) {
                    sInstance = new DistributedCallProxy();
                }
            }
        }
        return sInstance;
    }

    public IRemoteObject asObject() {
        synchronized (this.mLock) {
            if (this.mDistributedCallRemoteService != null) {
                return this.mDistributedCallRemoteService;
            }
            this.mDistributedCallRemoteService = SysAbilityManager.getSysAbility(4002);
            if (this.mDistributedCallRemoteService != null) {
                this.mDistributedCallRemoteService.addDeathRecipient(new DistributedCallDeathRecipient(), 0);
            } else {
                HiLog.error(TAG, "getSysAbility(DistributedCallService) failed.", new Object[0]);
            }
            return this.mDistributedCallRemoteService;
        }
    }

    private IRemoteObject getDistributedCallSrvAbility() throws RemoteException {
        return (IRemoteObject) Optional.ofNullable(asObject()).orElseThrow($$Lambda$t9E2a5kBSvCJG3OvOwSmRDhzvos.INSTANCE);
    }

    /* access modifiers changed from: private */
    public class DistributedCallDeathRecipient implements IRemoteObject.DeathRecipient {
        private DistributedCallDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(DistributedCallProxy.TAG, "DistributedCallDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (DistributedCallProxy.this.mLock) {
                DistributedCallProxy.this.mDistributedCallRemoteService = null;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int answerCall(int i, int i2) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            HiLog.info(TAG, "answerCall: callId: %{public}s", new Object[]{Integer.valueOf(i)});
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeInt(i2);
            getDistributedCallSrvAbility().sendRequest(1, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to answerCall", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int disconnect(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            HiLog.info(TAG, "disconnect: callId: %{public}s", new Object[]{Integer.valueOf(i)});
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(2, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to disconnect", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int startDtmfTone(int i, char c) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeChar(c);
            getDistributedCallSrvAbility().sendRequest(3, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to playDtmfTone", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int stopDtmfTone(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(4, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to disconnect", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int postDialDtmfContinue(int i, boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeBoolean(z);
            getDistributedCallSrvAbility().sendRequest(5, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to disconnect", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int reject(int i, boolean z, String str) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeBoolean(z);
            obtain.writeString(str);
            getDistributedCallSrvAbility().sendRequest(6, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to reject", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.dcall.IDistributedCall
    public boolean isNewCallAllowed() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            getDistributedCallSrvAbility().sendRequest(7, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isNewCallAllowed", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int setMuted(boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeBoolean(z);
            getDistributedCallSrvAbility().sendRequest(8, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to setMuted", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int setAudioDevice(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(9, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to setAudioDevice", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    private IRemoteObject asTelephonyObject() {
        synchronized (this.mLock) {
            if (this.mTelephonyRemoteService != null) {
                return this.mTelephonyRemoteService;
            }
            this.mTelephonyRemoteService = SysAbilityManager.checkSysAbility(4001);
            if (this.mTelephonyRemoteService != null) {
                this.mTelephonyRemoteService.addDeathRecipient(new TelephonyDeathRecipient(), 0);
            } else {
                HiLog.error(TAG, "getSysAbility(TelephonyService) failed.", new Object[0]);
            }
            return this.mTelephonyRemoteService;
        }
    }

    private IRemoteObject getTelephonySrvAbility() throws RemoteException {
        return (IRemoteObject) Optional.ofNullable(asTelephonyObject()).orElseThrow($$Lambda$t9E2a5kBSvCJG3OvOwSmRDhzvos.INSTANCE);
    }

    /* access modifiers changed from: private */
    public class TelephonyDeathRecipient implements IRemoteObject.DeathRecipient {
        private TelephonyDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(DistributedCallProxy.TAG, "TelephonyDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (DistributedCallProxy.this.mLock) {
                DistributedCallProxy.this.mTelephonyRemoteService = null;
            }
        }
    }

    @Override // ohos.dcall.IDistributedCall
    public boolean hasCall() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(2001, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to hasCall", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.dcall.IDistributedCall
    public boolean dial(String str, boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z2 = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeBoolean(z);
            getTelephonySrvAbility().sendRequest(2002, obtain, obtain2, messageOption);
            z2 = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to dial", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z2;
    }

    @Override // ohos.dcall.IDistributedCall
    public void displayCallScreen(boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeBoolean(z);
            getTelephonySrvAbility().sendRequest(2003, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to displayCallScreen", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.dcall.IDistributedCall
    public void muteRinger() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(2004, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to muteRinger", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int getCallState() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(2005, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getCallState", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.dcall.IDistributedCall
    public void addCallObserver(int i, ICallStateObserver iCallStateObserver, int i2) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iCallStateObserver.asObject());
        obtain.writeInt(i2);
        try {
            getTelephonySrvAbility().sendRequest(2006, obtain, obtain2, messageOption);
            if (obtain2.readInt() == -2) {
                throw new SecurityException("Failed to add observer, permission denied!");
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to addObserver", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.dcall.IDistributedCall
    public void removeCallObserver(int i, ICallStateObserver iCallStateObserver) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iCallStateObserver.asObject());
        try {
            getTelephonySrvAbility().sendRequest(2007, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to removeObserver", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.dcall.IDistributedCall
    public boolean isVideoCallingEnabled() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(2009, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to check vedio calling enabled", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.dcall.IDistributedCall
    public void inputDialerSpecialCode(String str) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            getTelephonySrvAbility().sendRequest(2010, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to inputDialerSpecialCode", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.dcall.IDistributedCall
    public boolean isVoiceCap() {
        Resources system = Resources.getSystem();
        if (system != null) {
            return system.getBoolean(17891573);
        }
        return true;
    }
}
