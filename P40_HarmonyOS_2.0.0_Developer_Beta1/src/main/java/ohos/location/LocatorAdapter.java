package ohos.location;

import java.io.IOException;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.global.systemres.ResourceTable;
import ohos.hiviewdfx.HiLog;
import ohos.location.callback.ILocatorAdapter;
import ohos.location.common.LBSLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

@SystemApi
public class LocatorAdapter implements ILocatorAdapter {
    private static final int EX_HAS_REPLY_HEADER = -128;
    private static final String LOCATOR_ABILITY_NAME = "LocatorAbility";
    private static final String LOCATOR_DESCRIPTOR = "location.ILocator";
    private static final Object LOCK = new Object();
    private static final int NOTIFICATION_DISMISS_TIME = 60000;
    private static final int REPLY_NO_EXCEPTION = 0;
    private static final int SECURITY_EXCEPTION = 1000;
    private static final int STRICT_POLICY = 1;
    private static final int TRANSACT_ENABLE_ABILITY = 9;
    private static final int TRANSACT_GET_CACHE_LOCATION = 5;
    private static final int TRANSACT_GET_SWITCH_STATE = 1;
    private static final int TRANSACT_REG_SWITCH_CALLBACK = 2;
    private static final int TRANSACT_REQUEST_ENABLE_ABILITY = 14;
    private static final int TRANSACT_START_LOCATING_CALLBACK = 3;
    private static final int TRANSACT_STOP_LOCATING_CALLBACK = 4;
    private static final int TRANSACT_UNREG_SWITCH_CALLBACK = 15;
    private static final int WORKSOURCE = 1;
    private static volatile LocatorAdapter instance;
    private String mBundleName;
    private Context mContext;
    private AbilityInfo mInfo;
    private IRemoteObject mRemoteObject;

    /* access modifiers changed from: private */
    public class LocatorAdapterDeathRecipient implements IRemoteObject.DeathRecipient {
        private LocatorAdapterDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(LBSLog.LOCATOR_ADAPTER, "LocatorAdapterDeathRecipient::onRemoteDied.", new Object[0]);
            LocatorAdapter.this.setRemoteObject(null);
        }
    }

    private LocatorAdapter() {
        getRemoteObject();
    }

    public static LocatorAdapter getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new LocatorAdapter();
                }
            }
        }
        return instance;
    }

    public void setAbilityInfo(AbilityInfo abilityInfo) {
        if (abilityInfo == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "application ability info is null", new Object[0]);
            this.mInfo = new AbilityInfo();
        } else {
            this.mInfo = abilityInfo;
        }
        if (this.mInfo.getBundleName() != null) {
            this.mBundleName = this.mInfo.getBundleName();
        } else {
            ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
            if (applicationInfo != null) {
                this.mBundleName = applicationInfo.getName();
            }
        }
        HiLog.info(LBSLog.LOCATOR_ADAPTER, "application bundle name: %{public}s", this.mBundleName);
    }

    public void setContext(Context context) {
        if (context == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "application context info is null", new Object[0]);
            return;
        }
        this.mContext = context.getApplicationContext();
        setAbilityInfo(context.getAbilityInfo());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        this.mRemoteObject = iRemoteObject;
    }

    private IRemoteObject getRemoteObject() {
        IRemoteObject iRemoteObject = this.mRemoteObject;
        if (iRemoteObject != null) {
            return iRemoteObject;
        }
        this.mRemoteObject = SysAbilityManager.getSysAbility(SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID);
        IRemoteObject iRemoteObject2 = this.mRemoteObject;
        if (iRemoteObject2 == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "getSysAbility(%{public}d) failed.", Integer.valueOf((int) SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID));
            return this.mRemoteObject;
        }
        iRemoteObject2.addDeathRecipient(new LocatorAdapterDeathRecipient(), 0);
        HiLog.info(LBSLog.LOCATOR_ADAPTER, "Get %{public}d completed.", Integer.valueOf((int) SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID));
        return this.mRemoteObject;
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public boolean isLocationSwitchEnbale() {
        boolean z = false;
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling isLocationSwitchEnbale", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        try {
            this.mRemoteObject.sendRequest(1, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 1) {
                z = true;
            }
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "isLocationSwitchEnbale: got RemoteException", new Object[0]);
        }
        obtain.reclaim();
        obtain2.reclaim();
        return z;
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public void requestEnable() throws IllegalArgumentException {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling requestEnable", new Object[0]);
        Context context = this.mContext;
        if (context != null) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            if (applicationInfo == null) {
                HiLog.error(LBSLog.LOCATOR_ADAPTER, "empty application info", new Object[0]);
                return;
            }
            String name = applicationInfo.getName();
            if (name == null || name.isEmpty()) {
                HiLog.error(LBSLog.LOCATOR_ADAPTER, "invalid application name", new Object[0]);
            } else if (!isLocationSwitchEnbale()) {
                showNotification(name);
            }
        } else {
            throw new IllegalArgumentException("invalid context, please new Locator object with ability context");
        }
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public void enableAbility(boolean z) {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling EnableAbility", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeBoolean(z);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.mRemoteObject.sendRequest(9, obtain, obtain2, new MessageOption());
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "EnableAbility: got RemoteException", new Object[0]);
        }
        obtain.reclaim();
        obtain2.reclaim();
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public boolean registerSwitchCallback(IRemoteObject iRemoteObject) {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling registerSwitchCallback", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeRemoteObject(iRemoteObject);
        boolean z = true;
        try {
            this.mRemoteObject.sendRequest(2, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "registerSwitchCallback: got RemoteException", new Object[0]);
            z = false;
        }
        obtain.reclaim();
        obtain2.reclaim();
        return z;
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public boolean unregisterSwitchCallback(IRemoteObject iRemoteObject) {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling unregisterSwitchCallback", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeRemoteObject(iRemoteObject);
        boolean z = true;
        try {
            this.mRemoteObject.sendRequest(15, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "unregisterSwitchCallback: got RemoteException", new Object[0]);
            z = false;
        }
        obtain.reclaim();
        obtain2.reclaim();
        return z;
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public void startLocating(RequestParam requestParam, IRemoteObject iRemoteObject, int i) {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling startLocating", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return;
        }
        String str = this.mBundleName;
        if (str == null || str.isEmpty()) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "invalid bundle name", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeSequenceable(requestParam);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeString(this.mBundleName);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.mRemoteObject.sendRequest(3, obtain, obtain2, new MessageOption(0));
            if (obtain2.readInt() != 0) {
                HiLog.error(LBSLog.LOCATOR_ADAPTER, "cause some excepiton happened in lower service.", new Object[0]);
            }
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "startLocating: got RemoteException", new Object[0]);
        }
        obtain.reclaim();
        obtain2.reclaim();
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public void stopLocating(IRemoteObject iRemoteObject) {
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling stopLocating", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeString(this.mBundleName);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.mRemoteObject.sendRequest(4, obtain, obtain2, new MessageOption(0));
            if (obtain2.readInt() != 0) {
                HiLog.error(LBSLog.LOCATOR_ADAPTER, "cause some excepiton happened in lower service.", new Object[0]);
            }
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "stopLocating: got RemoteException", new Object[0]);
        }
        obtain.reclaim();
        obtain2.reclaim();
    }

    @Override // ohos.location.callback.ILocatorAdapter
    public Location getCachedLocation() throws SecurityException {
        boolean z = false;
        HiLog.debug(LBSLog.LOCATOR_ADAPTER, "calling getCachedLocation", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "can not remote to locator sa", new Object[0]);
            return null;
        }
        Location location = new Location(0.0d, 0.0d);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeString(this.mBundleName);
        try {
            this.mRemoteObject.sendRequest(5, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt != 1000) {
                if (readInt != 0) {
                    HiLog.error(LBSLog.LOCATOR_ADAPTER, "cause some exception happened in locator service, exception = %{public}d", Integer.valueOf(readInt));
                } else {
                    z = location.unmarshalling(obtain2);
                }
                obtain.reclaim();
                obtain2.reclaim();
                if (z) {
                    return location;
                }
                return null;
            }
            throw new SecurityException("can not get cached location without location permission");
        } catch (RemoteException unused) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "stopLocating: got RemoteException", new Object[0]);
        }
    }

    private void showNotification(String str) {
        this.mContext.getResourceManager();
        try {
            String string = this.mContext.getResourceManager().getElement(ResourceTable.String_request_location_reminder_title).getString();
            String string2 = this.mContext.getResourceManager().getElement(ResourceTable.String_request_location_reminder_content).getString();
            NotificationRequest.NotificationNormalContent notificationNormalContent = new NotificationRequest.NotificationNormalContent();
            notificationNormalContent.setTitle(string).setText(string2);
            NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(notificationNormalContent);
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setContent(notificationContent);
            notificationRequest.setAutoDeletedTime(60000);
            notificationRequest.setClassification("sys");
            try {
                NotificationHelper.publishNotification(notificationRequest);
            } catch (RemoteException unused) {
                HiLog.error(LBSLog.LOCATOR_ADAPTER, "requestEnable: got RemoteException", new Object[0]);
            }
        } catch (NotExistException unused2) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "requestEnable: got NotExistException", new Object[0]);
        } catch (IOException unused3) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "requestEnable: got IOException", new Object[0]);
        } catch (WrongTypeException unused4) {
            HiLog.error(LBSLog.LOCATOR_ADAPTER, "requestEnable: got WrongTypeException", new Object[0]);
        }
    }
}
