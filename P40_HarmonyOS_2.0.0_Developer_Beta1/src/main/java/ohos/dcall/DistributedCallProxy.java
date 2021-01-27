package ohos.dcall;

import android.content.res.Resources;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.dmsdp.sdk.DeviceParameterConst;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.telephony.TelephonyUtils;
import ohos.utils.PacMap;
import ohos.utils.ParcelException;
import ohos.utils.net.Uri;

class DistributedCallProxy implements IDistributedCall {
    private static final String DCALL_SA_DESCRIPTOR = "OHOS.DistributedCall.IDistributedCall";
    private static final int RESULT_PERMISSION_DENY = -2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) DistributedCallUtils.LOG_ID_DCALL, "DistributedCallProxy");
    private static final String TELEPHONY_SA_DESCRIPTOR = "OHOS.Telephony.ITelephony";
    private static final int VAL_ARRAY_LIST = 18;
    private static final int VAL_BOOLEAN = 6;
    private static final int VAL_BOOLEAN_ARRAY = 15;
    private static final int VAL_BYTE = 0;
    private static final int VAL_BYTE_ARRAY = 9;
    private static final int VAL_CHAR = 7;
    private static final int VAL_CHAR_ARRAY = 16;
    private static final int VAL_DOUBLE = 5;
    private static final int VAL_DOUBLE_ARRAY = 14;
    private static final int VAL_FLOAT = 4;
    private static final int VAL_FLOAT_ARRAY = 13;
    private static final int VAL_INTEGER = 2;
    private static final int VAL_INTEGER_ARRAY = 11;
    private static final int VAL_LONG = 3;
    private static final int VAL_LONG_ARRAY = 12;
    private static final int VAL_NULL = -1;
    private static final int VAL_SHORT = 1;
    private static final int VAL_SHORT_ARRAY = 10;
    private static final int VAL_STRING = 8;
    private static final int VAL_STRING_ARRAY = 17;
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

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int distributeCallEvent(int i, String str, PacMap pacMap) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeString(str);
            writePacMapParcel(pacMap, obtain);
            getDistributedCallSrvAbility().sendRequest(13, obtain, obtain2, new MessageOption());
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to distributeCallEvent", new Object[0]);
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

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int hold(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(10, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to hold call", new Object[0]);
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
    public int unhold(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(11, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to unhold call", new Object[0]);
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
    public List<String> getPredefinedRejectMessages(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        ArrayList arrayList = new ArrayList();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getDistributedCallSrvAbility().sendRequest(12, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt > 0) {
                if (readInt <= 4) {
                    for (int i2 = 0; i2 < readInt; i2++) {
                        arrayList.add(obtain2.readString());
                    }
                    obtain2.reclaim();
                    obtain.reclaim();
                    return arrayList;
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            return arrayList;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getPredefinedRejectMessages", new Object[0]);
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
            this.mTelephonyRemoteService = SysAbilityManager.checkSysAbility((int) DeviceParameterConst.DISPLAY_DENSITY_DOUBLE);
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
        if (TelephonyUtils.isValidSlotId(i)) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
                obtain.writeInt(i);
                obtain.writeRemoteObject(iCallStateObserver.asObject());
                obtain.writeInt(i2);
                getTelephonySrvAbility().sendRequest(2006, obtain, obtain2, new MessageOption());
                if (obtain2.readInt() != -2) {
                    obtain2.reclaim();
                    obtain.reclaim();
                    return;
                }
                throw new SecurityException("Failed to add observer, permission denied!");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "Failed to addObserver", new Object[0]);
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.dcall.IDistributedCall
    public void removeCallObserver(int i, ICallStateObserver iCallStateObserver) {
        if (TelephonyUtils.isValidSlotId(i)) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
                obtain.writeInt(i);
                obtain.writeRemoteObject(iCallStateObserver.asObject());
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
            HiLog.error(TAG, "Failed to check video calling enabled", new Object[0]);
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
    public boolean hasVoiceCapability() {
        Resources system = Resources.getSystem();
        if (system != null) {
            return system.getBoolean(17891575);
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.dcall.IDistributedCall
    public int dial(Uri uri, PacMap pacMap) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            String str = null;
            if (uri != null) {
                str = uri.toString();
            }
            obtain.writeString(str);
            writePacMapParcel(pacMap, obtain);
            getDistributedCallSrvAbility().sendRequest(14, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to dial with extras", new Object[0]);
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
    public int initDialEnv(PacMap pacMap) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(DCALL_SA_DESCRIPTOR);
            writePacMapParcel(pacMap, obtain);
            getDistributedCallSrvAbility().sendRequest(15, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to initDialEnv", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    private void writePacMapParcel(PacMap pacMap, MessageParcel messageParcel) {
        if (pacMap == null || pacMap.isEmpty()) {
            HiLog.error(TAG, "writePacMapParcel: pacMap is null or empty", new Object[0]);
            messageParcel.writeInt(0);
            return;
        }
        Map all = pacMap.getAll();
        if (all == null) {
            HiLog.error(TAG, "writePacMapParcel: map is null.", new Object[0]);
            messageParcel.writeInt(0);
            return;
        }
        messageParcel.writeInt(all.size());
        for (String str : all.keySet()) {
            if (!TextUtils.isEmpty(str)) {
                messageParcel.writeString(str);
                writeValueIntoParcel(all.get(str), messageParcel);
            }
        }
    }

    private void writeValueIntoParcel(Object obj, MessageParcel messageParcel) {
        if (obj == null) {
            messageParcel.writeInt(-1);
        } else if (obj instanceof Byte) {
            messageParcel.writeInt(0);
            messageParcel.writeByte(((Byte) obj).byteValue());
        } else if (obj instanceof Short) {
            messageParcel.writeInt(1);
            messageParcel.writeShort(((Short) obj).shortValue());
        } else if (obj instanceof Integer) {
            messageParcel.writeInt(2);
            messageParcel.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            messageParcel.writeInt(3);
            messageParcel.writeLong(((Long) obj).longValue());
        } else if (obj instanceof Float) {
            messageParcel.writeInt(4);
            messageParcel.writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            messageParcel.writeInt(5);
            messageParcel.writeDouble(((Double) obj).doubleValue());
        } else if (obj instanceof Boolean) {
            messageParcel.writeInt(6);
            messageParcel.writeBoolean(((Boolean) obj).booleanValue());
        } else if (obj instanceof Character) {
            messageParcel.writeInt(7);
            messageParcel.writeChar(((Character) obj).charValue());
        } else if (obj instanceof String) {
            messageParcel.writeInt(8);
            messageParcel.writeString((String) obj);
        } else {
            writeArrayValueIntoParcel(obj, messageParcel);
        }
    }

    private void writeArrayValueIntoParcel(Object obj, MessageParcel messageParcel) {
        if (obj instanceof byte[]) {
            messageParcel.writeInt(9);
            writeByteArray((byte[]) obj, messageParcel);
        } else if (obj instanceof short[]) {
            messageParcel.writeInt(10);
            writeShortArray((short[]) obj, messageParcel);
        } else if (obj instanceof int[]) {
            messageParcel.writeInt(11);
            writeIntArray((int[]) obj, messageParcel);
        } else if (obj instanceof long[]) {
            messageParcel.writeInt(12);
            writeLongArray((long[]) obj, messageParcel);
        } else if (obj instanceof float[]) {
            messageParcel.writeInt(13);
            writeFloatArray((float[]) obj, messageParcel);
        } else if (obj instanceof double[]) {
            messageParcel.writeInt(14);
            writeDoubleArray((double[]) obj, messageParcel);
        } else if (obj instanceof boolean[]) {
            messageParcel.writeInt(15);
            writeBooleanArray((boolean[]) obj, messageParcel);
        } else if (obj instanceof char[]) {
            messageParcel.writeInt(16);
            writeCharArray((char[]) obj, messageParcel);
        } else if (obj instanceof String[]) {
            messageParcel.writeInt(17);
            writeStringArray((String[]) obj, messageParcel);
        } else if (obj instanceof ArrayList) {
            messageParcel.writeInt(18);
            writeArrayList((ArrayList) obj, messageParcel);
        } else {
            throw new ParcelException("Unsupported type in PacMap.");
        }
    }

    private void writeByteArray(byte[] bArr, MessageParcel messageParcel) {
        if (bArr == null || bArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = bArr.length;
        messageParcel.writeInt(length);
        for (byte b : bArr) {
            messageParcel.writeInt(0);
            messageParcel.writeByte(b);
        }
    }

    private void writeShortArray(short[] sArr, MessageParcel messageParcel) {
        if (sArr == null || sArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = sArr.length;
        messageParcel.writeInt(length);
        for (short s : sArr) {
            messageParcel.writeInt(1);
            messageParcel.writeShort(s);
        }
    }

    private void writeIntArray(int[] iArr, MessageParcel messageParcel) {
        if (iArr == null || iArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = iArr.length;
        messageParcel.writeInt(length);
        for (int i : iArr) {
            messageParcel.writeInt(2);
            messageParcel.writeInt(i);
        }
    }

    private void writeLongArray(long[] jArr, MessageParcel messageParcel) {
        if (jArr == null || jArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = jArr.length;
        messageParcel.writeInt(length);
        for (long j : jArr) {
            messageParcel.writeInt(3);
            messageParcel.writeLong(j);
        }
    }

    private void writeFloatArray(float[] fArr, MessageParcel messageParcel) {
        if (fArr == null || fArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = fArr.length;
        messageParcel.writeInt(length);
        for (float f : fArr) {
            messageParcel.writeInt(4);
            messageParcel.writeFloat(f);
        }
    }

    private void writeDoubleArray(double[] dArr, MessageParcel messageParcel) {
        if (dArr == null || dArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = dArr.length;
        messageParcel.writeInt(length);
        for (double d : dArr) {
            messageParcel.writeInt(5);
            messageParcel.writeDouble(d);
        }
    }

    private void writeBooleanArray(boolean[] zArr, MessageParcel messageParcel) {
        if (zArr == null || zArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = zArr.length;
        messageParcel.writeInt(length);
        for (boolean z : zArr) {
            messageParcel.writeInt(6);
            messageParcel.writeBoolean(z);
        }
    }

    private void writeCharArray(char[] cArr, MessageParcel messageParcel) {
        if (cArr == null || cArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = cArr.length;
        messageParcel.writeInt(length);
        for (char c : cArr) {
            messageParcel.writeInt(7);
            messageParcel.writeChar(c);
        }
    }

    private void writeStringArray(String[] strArr, MessageParcel messageParcel) {
        if (strArr == null || strArr.length <= 0) {
            messageParcel.writeInt(0);
            return;
        }
        int length = strArr.length;
        messageParcel.writeInt(length);
        for (String str : strArr) {
            messageParcel.writeInt(8);
            messageParcel.writeString(str);
        }
    }

    private void writeArrayList(ArrayList<?> arrayList, MessageParcel messageParcel) {
        if (arrayList == null) {
            messageParcel.writeInt(0);
            return;
        }
        messageParcel.writeInt(arrayList.size());
        Iterator<?> it = arrayList.iterator();
        while (it.hasNext()) {
            writeValueIntoParcel(it.next(), messageParcel);
        }
    }
}
