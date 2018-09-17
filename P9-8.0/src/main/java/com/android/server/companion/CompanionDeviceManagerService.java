package com.android.server.companion;

import android.app.PendingIntent;
import android.companion.AssociationRequest;
import android.companion.ICompanionDeviceDiscoveryService;
import android.companion.ICompanionDeviceDiscoveryServiceCallback;
import android.companion.ICompanionDeviceManager;
import android.companion.IFindDeviceCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.NetworkPolicyManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IDeviceIdleController;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.SettingsStringUtil.ComponentNameSet;
import android.text.BidiFormatter;
import android.util.AtomicFile;
import android.util.ExceptionUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import com.android.internal.content.PackageMonitor;
import com.android.internal.notification.NotificationAccessConfirmationActivityContract;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.Preconditions;
import com.android.server.FgThread;
import com.android.server.SystemService;
import com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass4;
import com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass6;
import com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass7;
import com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass8;
import com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass9;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class CompanionDeviceManagerService extends SystemService implements DeathRecipient {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "CompanionDeviceManagerService";
    private static final ComponentName SERVICE_TO_BIND_TO = ComponentName.createRelative("com.android.companiondevicemanager", ".DeviceDiscoveryService");
    private static final String XML_ATTR_DEVICE = "device";
    private static final String XML_ATTR_PACKAGE = "package";
    private static final String XML_FILE_NAME = "companion_device_manager_associations.xml";
    private static final String XML_TAG_ASSOCIATION = "association";
    private static final String XML_TAG_ASSOCIATIONS = "associations";
    private IAppOpsService mAppOpsManager = Stub.asInterface(ServiceManager.getService("appops"));
    private String mCallingPackage;
    private IFindDeviceCallback mFindDeviceCallback;
    private IDeviceIdleController mIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    private final CompanionDeviceManagerImpl mImpl = new CompanionDeviceManagerImpl();
    private final Object mLock = new Object();
    private AssociationRequest mRequest;
    private ServiceConnection mServiceConnection;
    private final ConcurrentMap<Integer, AtomicFile> mUidToStorage = new ConcurrentHashMap();

    private class Association {
        public final String companionAppPackage;
        public final String deviceAddress;
        public final int uid;

        /* synthetic */ Association(CompanionDeviceManagerService this$0, int uid, String deviceAddress, String companionAppPackage, Association -this4) {
            this(uid, deviceAddress, companionAppPackage);
        }

        private Association(int uid, String deviceAddress, String companionAppPackage) {
            this.uid = uid;
            this.deviceAddress = (String) Preconditions.checkNotNull(deviceAddress);
            this.companionAppPackage = (String) Preconditions.checkNotNull(companionAppPackage);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Association that = (Association) o;
            if (this.uid == that.uid && this.deviceAddress.equals(that.deviceAddress)) {
                return this.companionAppPackage.equals(that.companionAppPackage);
            }
            return false;
        }

        public int hashCode() {
            return (((this.uid * 31) + this.deviceAddress.hashCode()) * 31) + this.companionAppPackage.hashCode();
        }
    }

    class CompanionDeviceManagerImpl extends ICompanionDeviceManager.Stub {
        CompanionDeviceManagerImpl() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (Throwable e) {
                Slog.e(CompanionDeviceManagerService.LOG_TAG, "Error during IPC", e);
                RuntimeException propagate = ExceptionUtils.propagate(e, RemoteException.class);
            }
        }

        public void associate(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) throws RemoteException {
            Preconditions.checkNotNull(request, "Request cannot be null");
            Preconditions.checkNotNull(callback, "Callback cannot be null");
            checkCallerIsSystemOr(callingPackage);
            int userId = CompanionDeviceManagerService.getCallingUserId();
            checkUsesFeature(callingPackage, userId);
            long callingIdentity = Binder.clearCallingIdentity();
            try {
                CompanionDeviceManagerService.this.getContext().bindServiceAsUser(new Intent().setComponent(CompanionDeviceManagerService.SERVICE_TO_BIND_TO), CompanionDeviceManagerService.this.createServiceConnection(request, callback, callingPackage), 1, UserHandle.of(userId));
            } finally {
                Binder.restoreCallingIdentity(callingIdentity);
            }
        }

        public void stopScan(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) {
            if (Objects.equals(request, CompanionDeviceManagerService.this.mRequest) && Objects.equals(callback, CompanionDeviceManagerService.this.mFindDeviceCallback) && Objects.equals(callingPackage, CompanionDeviceManagerService.this.mCallingPackage)) {
                CompanionDeviceManagerService.this.cleanup();
            }
        }

        public List<String> getAssociations(String callingPackage, int userId) throws RemoteException {
            checkCallerIsSystemOr(callingPackage, userId);
            checkUsesFeature(callingPackage, CompanionDeviceManagerService.getCallingUserId());
            return CollectionUtils.map(CompanionDeviceManagerService.this.readAllAssociations(userId, callingPackage), new -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM());
        }

        public void disassociate(String deviceMacAddress, String callingPackage) throws RemoteException {
            Preconditions.checkNotNull(deviceMacAddress);
            checkCallerIsSystemOr(callingPackage);
            checkUsesFeature(callingPackage, CompanionDeviceManagerService.getCallingUserId());
            CompanionDeviceManagerService.this.removeAssociation(CompanionDeviceManagerService.getCallingUserId(), callingPackage, deviceMacAddress);
        }

        private void checkCallerIsSystemOr(String pkg) throws RemoteException {
            checkCallerIsSystemOr(pkg, CompanionDeviceManagerService.getCallingUserId());
        }

        private void checkCallerIsSystemOr(String pkg, int userId) throws RemoteException {
            if (!CompanionDeviceManagerService.isCallerSystem()) {
                Preconditions.checkArgument(CompanionDeviceManagerService.getCallingUserId() == userId, "Must be called by either same user or system");
                CompanionDeviceManagerService.this.mAppOpsManager.checkPackage(Binder.getCallingUid(), pkg);
            }
        }

        public PendingIntent requestNotificationAccess(ComponentName component) throws RemoteException {
            String callingPackage = component.getPackageName();
            checkCanCallNotificationApi(callingPackage);
            int userId = CompanionDeviceManagerService.getCallingUserId();
            String packageTitle = BidiFormatter.getInstance().unicodeWrap(CompanionDeviceManagerService.this.getPackageInfo(callingPackage, userId).applicationInfo.loadSafeLabel(CompanionDeviceManagerService.this.getContext().getPackageManager()).toString());
            long identity = Binder.clearCallingIdentity();
            try {
                PendingIntent activity = PendingIntent.getActivity(CompanionDeviceManagerService.this.getContext(), 0, NotificationAccessConfirmationActivityContract.launcherIntent(userId, component, packageTitle), 1409286144);
                return activity;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean hasNotificationAccess(ComponentName component) throws RemoteException {
            checkCanCallNotificationApi(component.getPackageName());
            return new ComponentNameSet(Secure.getString(CompanionDeviceManagerService.this.getContext().getContentResolver(), "enabled_notification_listeners")).contains(component);
        }

        private void checkCanCallNotificationApi(String callingPackage) throws RemoteException {
            checkCallerIsSystemOr(callingPackage);
            int userId = CompanionDeviceManagerService.getCallingUserId();
            Preconditions.checkState(ArrayUtils.isEmpty(CompanionDeviceManagerService.this.readAllAssociations(userId, callingPackage)) ^ 1, "App must have an association before calling this API");
            checkUsesFeature(callingPackage, userId);
        }

        private void checkUsesFeature(String pkg, int userId) {
            if (!CompanionDeviceManagerService.isCallerSystem()) {
                FeatureInfo[] reqFeatures = CompanionDeviceManagerService.this.getPackageInfo(pkg, userId).reqFeatures;
                String requiredFeature = "android.software.companion_device_setup";
                int numFeatures = ArrayUtils.size(reqFeatures);
                int i = 0;
                while (i < numFeatures) {
                    if (!requiredFeature.equals(reqFeatures[i].name)) {
                        i++;
                    } else {
                        return;
                    }
                }
                throw new IllegalStateException("Must declare uses-feature " + requiredFeature + " in manifest to use this API");
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
            new ShellCmd(CompanionDeviceManagerService.this, null).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    private class ShellCmd extends ShellCommand {
        public static final String USAGE = "help\nlist USER_ID\nassociate USER_ID PACKAGE MAC_ADDRESS\ndisassociate USER_ID PACKAGE MAC_ADDRESS";

        /* synthetic */ ShellCmd(CompanionDeviceManagerService this$0, ShellCmd -this1) {
            this();
        }

        private ShellCmd() {
        }

        public int onCommand(String cmd) {
            if (cmd.equals("list")) {
                ArrayList<Association> associations = CompanionDeviceManagerService.this.readAllAssociations(getNextArgInt());
                for (int i = 0; i < CollectionUtils.size(associations); i++) {
                    Association a = (Association) associations.get(i);
                    getOutPrintWriter().println(a.companionAppPackage + " " + a.deviceAddress);
                }
            } else if (cmd.equals("associate")) {
                CompanionDeviceManagerService.this.addAssociation(getNextArgInt(), getNextArgRequired(), getNextArgRequired());
            } else if (!cmd.equals("disassociate")) {
                return handleDefaultCommands(cmd);
            } else {
                CompanionDeviceManagerService.this.removeAssociation(getNextArgInt(), getNextArgRequired(), getNextArgRequired());
            }
            return 0;
        }

        private int getNextArgInt() {
            return Integer.parseInt(getNextArgRequired());
        }

        public void onHelp() {
            getOutPrintWriter().println(USAGE);
        }
    }

    public CompanionDeviceManagerService(Context context) {
        super(context);
        registerPackageMonitor();
    }

    private void registerPackageMonitor() {
        new PackageMonitor() {
            public void onPackageRemoved(String packageName, int uid) {
                CompanionDeviceManagerService.this.updateAssociations(new AnonymousClass4(packageName), getChangingUserId());
            }

            public void onPackageModified(String packageName) {
                int userId = getChangingUserId();
                if (!ArrayUtils.isEmpty(CompanionDeviceManagerService.this.readAllAssociations(userId, packageName))) {
                    CompanionDeviceManagerService.this.updateSpecialAccessPermissionForAssociatedPackage(packageName, userId);
                }
            }
        }.register(getContext(), FgThread.get().getLooper(), UserHandle.ALL, true);
    }

    public void onStart() {
        publishBinderService("companiondevice", this.mImpl);
    }

    public void binderDied() {
        Handler.getMain().post(new com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass2(this));
    }

    private void cleanup() {
        synchronized (this.mLock) {
            this.mServiceConnection = unbind(this.mServiceConnection);
            this.mFindDeviceCallback = (IFindDeviceCallback) unlinkToDeath(this.mFindDeviceCallback, this, 0);
            this.mRequest = null;
            this.mCallingPackage = null;
        }
    }

    private static <T extends IInterface> T unlinkToDeath(T iinterface, DeathRecipient deathRecipient, int flags) {
        if (iinterface != null) {
            iinterface.asBinder().unlinkToDeath(deathRecipient, flags);
        }
        return null;
    }

    private ServiceConnection unbind(ServiceConnection conn) {
        if (conn != null) {
            getContext().unbindService(conn);
        }
        return null;
    }

    private static int getCallingUserId() {
        return UserHandle.getUserId(Binder.getCallingUid());
    }

    private static boolean isCallerSystem() {
        return Binder.getCallingUid() == 1000;
    }

    private ServiceConnection createServiceConnection(final AssociationRequest request, final IFindDeviceCallback findDeviceCallback, final String callingPackage) {
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                CompanionDeviceManagerService.this.mFindDeviceCallback = findDeviceCallback;
                CompanionDeviceManagerService.this.mRequest = request;
                CompanionDeviceManagerService.this.mCallingPackage = callingPackage;
                try {
                    CompanionDeviceManagerService.this.mFindDeviceCallback.asBinder().linkToDeath(CompanionDeviceManagerService.this, 0);
                    try {
                        ICompanionDeviceDiscoveryService.Stub.asInterface(service).startDiscovery(request, callingPackage, findDeviceCallback, CompanionDeviceManagerService.this.getServiceCallback());
                    } catch (RemoteException e) {
                        Log.e(CompanionDeviceManagerService.LOG_TAG, "Error while initiating device discovery", e);
                    }
                } catch (RemoteException e2) {
                    CompanionDeviceManagerService.this.cleanup();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        return this.mServiceConnection;
    }

    private ICompanionDeviceDiscoveryServiceCallback.Stub getServiceCallback() {
        return new ICompanionDeviceDiscoveryServiceCallback.Stub() {
            public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                try {
                    return super.onTransact(code, data, reply, flags);
                } catch (Throwable e) {
                    Slog.e(CompanionDeviceManagerService.LOG_TAG, "Error during IPC", e);
                    RuntimeException propagate = ExceptionUtils.propagate(e, RemoteException.class);
                }
            }

            public void onDeviceSelected(String packageName, int userId, String deviceAddress) {
                CompanionDeviceManagerService.this.addAssociation(userId, packageName, deviceAddress);
                CompanionDeviceManagerService.this.cleanup();
            }

            public void onDeviceSelectionCancel() {
                CompanionDeviceManagerService.this.cleanup();
            }
        };
    }

    void addAssociation(int userId, String packageName, String deviceAddress) {
        updateSpecialAccessPermissionForAssociatedPackage(packageName, userId);
        recordAssociation(packageName, deviceAddress);
    }

    void removeAssociation(int userId, String pkg, String deviceMacAddress) {
        updateAssociations(new AnonymousClass9(userId, this, deviceMacAddress, pkg));
    }

    /* synthetic */ List lambda$-com_android_server_companion_CompanionDeviceManagerService_17225(int userId, String deviceMacAddress, String pkg, List associations) {
        return CollectionUtils.remove(associations, new Association(this, userId, deviceMacAddress, pkg, null));
    }

    private void updateSpecialAccessPermissionForAssociatedPackage(String packageName, int userId) {
        PackageInfo packageInfo = getPackageInfo(packageName, userId);
        if (packageInfo != null) {
            Binder.withCleanCallingIdentity(new AnonymousClass6(this, packageInfo));
        }
    }

    /* synthetic */ void lambda$-com_android_server_companion_CompanionDeviceManagerService_17629(PackageInfo packageInfo) throws Exception {
        try {
            if (containsEither(packageInfo.requestedPermissions, "android.permission.RUN_IN_BACKGROUND", "android.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND")) {
                this.mIdleController.addPowerSaveWhitelistApp(packageInfo.packageName);
            } else {
                this.mIdleController.removePowerSaveWhitelistApp(packageInfo.packageName);
            }
        } catch (RemoteException e) {
        }
        NetworkPolicyManager networkPolicyManager = NetworkPolicyManager.from(getContext());
        if (containsEither(packageInfo.requestedPermissions, "android.permission.USE_DATA_IN_BACKGROUND", "android.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND")) {
            networkPolicyManager.addUidPolicy(packageInfo.applicationInfo.uid, 4);
        } else {
            networkPolicyManager.removeUidPolicy(packageInfo.applicationInfo.uid, 4);
        }
    }

    private static <T> boolean containsEither(T[] array, T a, T b) {
        return !ArrayUtils.contains(array, a) ? ArrayUtils.contains(array, b) : true;
    }

    private PackageInfo getPackageInfo(String packageName, int userId) {
        return (PackageInfo) Binder.withCleanCallingIdentity(new AnonymousClass7(userId, this, packageName));
    }

    /* synthetic */ PackageInfo lambda$-com_android_server_companion_CompanionDeviceManagerService_19221(String packageName, int userId) throws Exception {
        try {
            return getContext().getPackageManager().getPackageInfoAsUser(packageName, 20480, userId);
        } catch (NameNotFoundException e) {
            Slog.e(LOG_TAG, "Failed to get PackageInfo for package " + packageName, e);
            return null;
        }
    }

    private void recordAssociation(String priviledgedPackage, String deviceAddress) {
        updateAssociations(new AnonymousClass8(getCallingUserId(), this, deviceAddress, priviledgedPackage));
    }

    /* synthetic */ List lambda$-com_android_server_companion_CompanionDeviceManagerService_20046(int userId, String deviceAddress, String priviledgedPackage, List associations) {
        return CollectionUtils.add(associations, new Association(this, userId, deviceAddress, priviledgedPackage, null));
    }

    private void updateAssociations(Function<List<Association>, List<Association>> update) {
        updateAssociations(update, getCallingUserId());
    }

    private void updateAssociations(Function<List<Association>, List<Association>> update, int userId) {
        AtomicFile file = getStorageFileForUser(userId);
        synchronized (file) {
            List<Association> associations = readAllAssociations(userId);
            List<Association> old = CollectionUtils.copyOf(associations);
            associations = (List) update.apply(associations);
            if (CollectionUtils.size(old) == CollectionUtils.size(associations)) {
                return;
            }
            List<Association> finalAssociations = associations;
            file.write(new com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM.AnonymousClass3(associations));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_companion_CompanionDeviceManagerService_20901(List finalAssociations, FileOutputStream out) {
        XmlSerializer xml = Xml.newSerializer();
        try {
            xml.setOutput(out, StandardCharsets.UTF_8.name());
            xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xml.startDocument(null, Boolean.valueOf(true));
            xml.startTag(null, XML_TAG_ASSOCIATIONS);
            for (int i = 0; i < CollectionUtils.size(finalAssociations); i++) {
                Association association = (Association) finalAssociations.get(i);
                xml.startTag(null, XML_TAG_ASSOCIATION).attribute(null, "package", association.companionAppPackage).attribute(null, XML_ATTR_DEVICE, association.deviceAddress).endTag(null, XML_TAG_ASSOCIATION);
            }
            xml.endTag(null, XML_TAG_ASSOCIATIONS);
            xml.endDocument();
        } catch (Exception e) {
            Slog.e(LOG_TAG, "Error while writing associations file", e);
            throw ExceptionUtils.propagate(e);
        }
    }

    private AtomicFile getStorageFileForUser(int uid) {
        return (AtomicFile) this.mUidToStorage.computeIfAbsent(Integer.valueOf(uid), new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
    }

    private ArrayList<Association> readAllAssociations(int userId) {
        return readAllAssociations(userId, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0074 A:{Splitter: B:27:0x0073, ExcHandler: org.xmlpull.v1.XmlPullParserException (r7_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:29:0x0074, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            android.util.Slog.e(LOG_TAG, "Error while reading associations file", r7);
     */
    /* JADX WARNING: Missing block: B:34:0x0080, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<Association> readAllAssociations(int userId, String packageFilter) {
        FileInputStream fileInputStream;
        Throwable th;
        Throwable th2;
        AtomicFile file = getStorageFileForUser(userId);
        if (!file.getBaseFile().exists()) {
            return null;
        }
        ArrayList<Association> result = null;
        XmlPullParser parser = Xml.newPullParser();
        synchronized (file) {
            Throwable th3 = null;
            fileInputStream = null;
            try {
                fileInputStream = file.openRead();
                parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        break;
                    } else if (type == 2 || (XML_TAG_ASSOCIATIONS.equals(parser.getName()) ^ 1) == 0) {
                        String appPackage = parser.getAttributeValue(null, "package");
                        String deviceAddress = parser.getAttributeValue(null, XML_ATTR_DEVICE);
                        if (!(appPackage == null || deviceAddress == null)) {
                            if (packageFilter == null || (packageFilter.equals(appPackage) ^ 1) == 0) {
                                result = ArrayUtils.add(result, new Association(this, userId, deviceAddress, appPackage, null));
                            }
                        }
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (Exception e) {
                    }
                }
            } catch (Throwable th22) {
                Throwable th5 = th22;
                th22 = th;
                th = th5;
            }
        }
        return result;
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (Throwable th6) {
                if (th22 == null) {
                    th22 = th6;
                } else if (th22 != th6) {
                    th22.addSuppressed(th6);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        } else {
            throw th;
        }
    }
}
