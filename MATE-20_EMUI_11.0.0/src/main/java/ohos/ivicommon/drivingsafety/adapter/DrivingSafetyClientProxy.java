package ohos.ivicommon.drivingsafety.adapter;

import java.lang.reflect.InvocationTargetException;
import ohos.app.Context;
import ohos.bundle.ApplicationInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.ivicommon.drivingsafety.model.AtomicAbilityInfo;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.ivicommon.drivingsafety.model.Position;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class DrivingSafetyClientProxy implements IDrivingSafetyClient {
    private static final int CHECK_DRIVING_SAFETY = 1;
    private static final String DRIVING_SAFETY_SERVICE = "DrivingSafetyService";
    private static final int DRIVING_SAFETY_SERVICE_ID = 4501;
    private static final int GET_DRIVING_MODE = 2;
    private static final int GET_DRIVING_RESTRAINT = 3;
    private static final String METHOD_GET_POSITION = "getPosition";
    private static final String METHOD_GET_X_POS = "getXpos";
    private static final String METHOD_GET_Y_POS = "getYpos";
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "DrivingSafetyClientProxy");
    private static final String WINDOW_POSITION_PACKAGE = "ohos.ivicommon.drivingsafety.model.WindowPosition";
    private static volatile DrivingSafetyClientProxy sInstance;
    private final DrivingSafetyDeathRecipient deathRecipient = new DrivingSafetyDeathRecipient();
    private IRemoteObject mDrivingSafetyService = null;
    private final Object mLock = new Object();
    private int xPos = -1;
    private int yPos = -1;

    private DrivingSafetyClientProxy() {
        synchronized (this.mLock) {
            this.mDrivingSafetyService = SysAbilityManager.getSysAbility(DRIVING_SAFETY_SERVICE_ID);
            if (this.mDrivingSafetyService == null) {
                HiLog.error(TAG, "getSysAbility: %s failed", DRIVING_SAFETY_SERVICE);
            } else {
                this.mDrivingSafetyService.addDeathRecipient(this.deathRecipient, 0);
                HiLog.debug(TAG, "addDeathRecipient for DrivingSafetyClientProxy", new Object[0]);
            }
        }
    }

    public static DrivingSafetyClientProxy getInstance() {
        if (sInstance == null || sInstance.asObject() == null) {
            synchronized (DrivingSafetyClientProxy.class) {
                if (sInstance == null || sInstance.asObject() == null) {
                    sInstance = new DrivingSafetyClientProxy();
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        IRemoteObject iRemoteObject;
        synchronized (this.mLock) {
            iRemoteObject = this.mDrivingSafetyService;
        }
        return iRemoteObject;
    }

    private class DrivingSafetyDeathRecipient implements IRemoteObject.DeathRecipient {
        private DrivingSafetyDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(DrivingSafetyClientProxy.TAG, "DrivingSafetyDeathRecipient::onRemoteDied.", new Object[0]);
            DrivingSafetyClientProxy.this.setRemoteObject(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mLock) {
            this.mDrivingSafetyService = iRemoteObject;
        }
    }

    private void getWindow() {
        try {
            Class<?> cls = Class.forName(WINDOW_POSITION_PACKAGE);
            Object newInstance = cls.newInstance();
            cls.getDeclaredMethod(METHOD_GET_POSITION, new Class[0]).invoke(newInstance, new Object[0]);
            this.xPos = ((Integer) cls.getDeclaredMethod(METHOD_GET_X_POS, new Class[0]).invoke(newInstance, new Object[0])).intValue();
            this.yPos = ((Integer) cls.getDeclaredMethod(METHOD_GET_Y_POS, new Class[0]).invoke(newInstance, new Object[0])).intValue();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.error(TAG, "get ability position Exception: %{public}s", WINDOW_POSITION_PACKAGE);
        }
    }

    private AtomicAbilityInfo getAbilityInfo(Context context, ControlItemEnum controlItemEnum, Position position) {
        ApplicationInfo applicationInfo;
        AtomicAbilityInfo atomicAbilityInfo = new AtomicAbilityInfo();
        if (position != null) {
            this.xPos = position.getX();
            this.yPos = position.getY();
        } else {
            getWindow();
        }
        atomicAbilityInfo.setX(this.xPos);
        atomicAbilityInfo.setY(this.yPos);
        if (!(context == null || (applicationInfo = context.getApplicationInfo()) == null)) {
            atomicAbilityInfo.setAbilityName(applicationInfo.getName());
            boolean z = true;
            if ((applicationInfo.getSupportedModes() & 1) != 1) {
                z = false;
            }
            atomicAbilityInfo.enableDriveMode(z);
        }
        if (controlItemEnum != null) {
            atomicAbilityInfo.setControlItem(controlItemEnum.getName());
        }
        return atomicAbilityInfo;
    }

    public boolean isDrivingSafety(Context context, ControlItemEnum controlItemEnum) throws RemoteException {
        return isDrivingSafety(context, controlItemEnum, null);
    }

    @Override // ohos.ivicommon.drivingsafety.adapter.IDrivingSafetyClient
    public boolean isDrivingSafety(Context context, ControlItemEnum controlItemEnum, Position position) throws RemoteException {
        boolean readBoolean;
        HiLog.debug(TAG, "isDrivingSafety start", new Object[0]);
        AtomicAbilityInfo abilityInfo = getAbilityInfo(context, controlItemEnum, position);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        synchronized (this.mLock) {
            try {
                if (this.mDrivingSafetyService == null) {
                    HiLog.error(TAG, "mDrivingSafetyService is null", new Object[0]);
                    throw new RemoteException();
                } else if (!abilityInfo.marshalling(obtain)) {
                    HiLog.error(TAG, "write atomicAbilityInfo to parcel failed", new Object[0]);
                    throw new RemoteException();
                } else if (this.mDrivingSafetyService.sendRequest(1, obtain, obtain2, messageOption)) {
                    readBoolean = obtain2.readBoolean();
                } else {
                    HiLog.error(TAG, "check drivingsafety failed", new Object[0]);
                    throw new RemoteException();
                }
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        }
        return readBoolean;
    }

    @Override // ohos.ivicommon.drivingsafety.adapter.IDrivingSafetyClient
    public boolean isDrivingMode() throws RemoteException {
        boolean readBoolean;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        synchronized (this.mLock) {
            try {
                if (this.mDrivingSafetyService == null) {
                    HiLog.error(TAG, "mDrivingSafetyService is null", new Object[0]);
                    throw new RemoteException();
                } else if (this.mDrivingSafetyService.sendRequest(2, obtain, obtain2, messageOption)) {
                    readBoolean = obtain2.readBoolean();
                } else {
                    HiLog.error(TAG, "get drivingmode failed", new Object[0]);
                    throw new RemoteException();
                }
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        }
        return readBoolean;
    }

    @Override // ohos.ivicommon.drivingsafety.adapter.IDrivingSafetyClient
    public int getRestraint() throws RemoteException {
        int readInt;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        synchronized (this.mLock) {
            try {
                if (this.mDrivingSafetyService == null) {
                    HiLog.error(TAG, "mDrivingSafetyService is null", new Object[0]);
                    throw new RemoteException();
                } else if (this.mDrivingSafetyService.sendRequest(3, obtain, obtain2, messageOption)) {
                    readInt = obtain2.readInt();
                } else {
                    HiLog.error(TAG, "get drivingrestraint failed", new Object[0]);
                    throw new RemoteException();
                }
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        }
        return readInt;
    }
}
