package com.android.server.print;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.os.UserManager;
import android.print.IPrintDocumentAdapter;
import android.print.IPrintJobStateChangeListener;
import android.print.IPrintManager;
import android.print.IPrintServicesChangeListener;
import android.print.IPrinterDiscoveryObserver;
import android.print.PrintAttributes;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.printservice.PrintServiceInfo;
import android.printservice.recommendation.IRecommendationsChangeListener;
import android.printservice.recommendation.RecommendationInfo;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.utils.PriorityDump;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public final class PrintManagerService extends SystemService {
    private static final String LOG_TAG = "PrintManagerService";
    private final PrintManagerImpl mPrintManagerImpl;

    class PrintManagerImpl extends IPrintManager.Stub {
        private static final int BACKGROUND_USER_ID = -10;
        /* access modifiers changed from: private */
        public final Context mContext;
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        /* access modifiers changed from: private */
        public final UserManager mUserManager;
        /* access modifiers changed from: private */
        public final SparseArray<UserState> mUserStates = new SparseArray<>();

        PrintManagerImpl(Context context) {
            this.mContext = context;
            this.mUserManager = (UserManager) context.getSystemService("user");
            registerContentObservers();
            registerBroadcastReceivers();
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new PrintShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        public Bundle print(String printJobName, IPrintDocumentAdapter adapter, PrintAttributes attributes, String packageName, int appId, int userId) {
            IPrintDocumentAdapter adapter2 = (IPrintDocumentAdapter) Preconditions.checkNotNull(adapter);
            if (!isPrintingEnabled()) {
                DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
                int callingUserId = UserHandle.getCallingUserId();
                long identity = Binder.clearCallingIdentity();
                try {
                    CharSequence disabledMessage = dpmi.getPrintingDisabledReasonForUser(callingUserId);
                    if (disabledMessage != null) {
                        Toast.makeText(this.mContext, Looper.getMainLooper(), disabledMessage, 1).show();
                    }
                    try {
                        adapter2.start();
                    } catch (RemoteException e) {
                        Log.e(PrintManagerService.LOG_TAG, "Error calling IPrintDocumentAdapter.start()");
                    }
                    try {
                        adapter2.finish();
                    } catch (RemoteException e2) {
                        Log.e(PrintManagerService.LOG_TAG, "Error calling IPrintDocumentAdapter.finish()");
                    }
                    return null;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                String printJobName2 = (String) Preconditions.checkStringNotEmpty(printJobName);
                String packageName2 = (String) Preconditions.checkStringNotEmpty(packageName);
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                        return null;
                    }
                    int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                    String resolvedPackageName = resolveCallingPackageNameEnforcingSecurity(packageName2);
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity2 = Binder.clearCallingIdentity();
                    try {
                        return userState.print(printJobName2, adapter2, attributes, resolvedPackageName, resolvedAppId);
                    } finally {
                        Binder.restoreCallingIdentity(identity2);
                    }
                }
            }
        }

        public List<PrintJobInfo> getPrintJobInfos(int appId, int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getPrintJobInfos(resolvedAppId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId, int userId) {
            if (printJobId == null) {
                return null;
            }
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getPrintJobInfo(printJobId, resolvedAppId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public Icon getCustomPrinterIcon(PrinterId printerId, int userId) {
            PrinterId printerId2 = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getCustomPrinterIcon(printerId2);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void cancelPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                        int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                        UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                        long identity = Binder.clearCallingIdentity();
                        try {
                            userState.cancelPrintJob(printJobId, resolvedAppId);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }
            }
        }

        public void restartPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null && isPrintingEnabled()) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                        int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                        UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                        long identity = Binder.clearCallingIdentity();
                        try {
                            userState.restartPrintJob(printJobId, resolvedAppId);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }
            }
        }

        public List<PrintServiceInfo> getPrintServices(int selectionFlags, int userId) {
            Preconditions.checkFlagsArgument(selectionFlags, 3);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICES", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getPrintServices(selectionFlags);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void setPrintServiceEnabled(ComponentName service, boolean isEnabled, int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            int appId = UserHandle.getAppId(Binder.getCallingUid());
            if (appId != 1000) {
                try {
                    if (appId != UserHandle.getAppId(this.mContext.getPackageManager().getPackageUidAsUser("com.android.printspooler", resolvedUserId))) {
                        throw new SecurityException("Only system and print spooler can call this");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(PrintManagerService.LOG_TAG, "Could not verify caller", e);
                    return;
                }
            }
            ComponentName service2 = (ComponentName) Preconditions.checkNotNull(service);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.setPrintServiceEnabled(service2, isEnabled);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public List<RecommendationInfo> getPrintServiceRecommendations(int userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICE_RECOMMENDATIONS", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getPrintServiceRecommendations();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void createPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            IPrinterDiscoveryObserver observer2 = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.createPrinterDiscoverySession(observer2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            IPrinterDiscoveryObserver observer2 = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.destroyPrinterDiscoverySession(observer2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        /* JADX WARNING: type inference failed for: r0v3, types: [java.util.Collection] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void startPrinterDiscovery(IPrinterDiscoveryObserver observer, List<PrinterId> priorityList, int userId) {
            IPrinterDiscoveryObserver observer2 = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            if (priorityList != null) {
                priorityList = Preconditions.checkCollectionElementsNotNull(priorityList, "PrinterId");
            }
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.startPrinterDiscovery(observer2, priorityList);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void stopPrinterDiscovery(IPrinterDiscoveryObserver observer, int userId) {
            IPrinterDiscoveryObserver observer2 = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.stopPrinterDiscovery(observer2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void validatePrinters(List<PrinterId> printerIds, int userId) {
            List<PrinterId> printerIds2 = (List) Preconditions.checkCollectionElementsNotNull(printerIds, "PrinterId");
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.validatePrinters(printerIds2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void startPrinterStateTracking(PrinterId printerId, int userId) {
            PrinterId printerId2 = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.startPrinterStateTracking(printerId2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void stopPrinterStateTracking(PrinterId printerId, int userId) {
            PrinterId printerId2 = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.stopPrinterStateTracking(printerId2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void addPrintJobStateChangeListener(IPrintJobStateChangeListener listener, int appId, int userId) throws RemoteException {
            IPrintJobStateChangeListener listener2 = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.addPrintJobStateChangeListener(listener2, resolvedAppId);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void removePrintJobStateChangeListener(IPrintJobStateChangeListener listener, int userId) {
            IPrintJobStateChangeListener listener2 = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.removePrintJobStateChangeListener(listener2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void addPrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) throws RemoteException {
            IPrintServicesChangeListener listener2 = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICES", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.addPrintServicesChangeListener(listener2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void removePrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) {
            IPrintServicesChangeListener listener2 = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICES", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.removePrintServicesChangeListener(listener2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) throws RemoteException {
            IRecommendationsChangeListener listener2 = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICE_RECOMMENDATIONS", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.addPrintServiceRecommendationsChangeListener(listener2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) {
            IRecommendationsChangeListener listener2 = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRINT_SERVICE_RECOMMENDATIONS", null);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) == getCurrentUserId()) {
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        userState.removePrintServiceRecommendationsChangeListener(listener2);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            FileDescriptor fd2 = (FileDescriptor) Preconditions.checkNotNull(fd);
            if (DumpUtils.checkDumpPermission(this.mContext, PrintManagerService.LOG_TAG, pw)) {
                int opti = 0;
                boolean dumpAsProto = false;
                while (opti < args.length) {
                    String opt = args[opti];
                    if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                        break;
                    }
                    opti++;
                    if (PriorityDump.PROTO_ARG.equals(opt)) {
                        dumpAsProto = true;
                    } else {
                        pw.println("Unknown argument: " + opt + "; use -h for help");
                    }
                }
                ArrayList<UserState> userStatesToDump = new ArrayList<>();
                synchronized (this.mLock) {
                    int numUserStates = this.mUserStates.size();
                    for (int i = 0; i < numUserStates; i++) {
                        userStatesToDump.add(this.mUserStates.valueAt(i));
                    }
                }
                long identity = Binder.clearCallingIdentity();
                if (dumpAsProto) {
                    try {
                        dump(new DualDumpOutputStream(new ProtoOutputStream(fd2)), userStatesToDump);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else {
                    pw.println("PRINT MANAGER STATE (dumpsys print)");
                    dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), userStatesToDump);
                }
            }
        }

        public boolean getBindInstantServiceAllowed(int userId) {
            UserState userState;
            int callingUid = Binder.getCallingUid();
            if (callingUid == 2000 || callingUid == 0) {
                synchronized (this.mLock) {
                    userState = getOrCreateUserStateLocked(userId, false);
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    return userState.getBindInstantServiceAllowed();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("Can only be called by uid 2000 or 0");
            }
        }

        public void setBindInstantServiceAllowed(int userId, boolean allowed) {
            UserState userState;
            int callingUid = Binder.getCallingUid();
            if (callingUid == 2000 || callingUid == 0) {
                synchronized (this.mLock) {
                    userState = getOrCreateUserStateLocked(userId, false);
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.setBindInstantServiceAllowed(allowed);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("Can only be called by uid 2000 or 0");
            }
        }

        private boolean isPrintingEnabled() {
            return !this.mUserManager.hasUserRestriction("no_printing", Binder.getCallingUserHandle());
        }

        private void dump(DualDumpOutputStream dumpStream, ArrayList<UserState> userStatesToDump) {
            int userStateCount = userStatesToDump.size();
            for (int i = 0; i < userStateCount; i++) {
                long token = dumpStream.start("user_states", 2246267895809L);
                userStatesToDump.get(i).dump(dumpStream);
                dumpStream.end(token);
            }
            dumpStream.flush();
        }

        private void registerContentObservers() {
            final Uri enabledPrintServicesUri = Settings.Secure.getUriFor("disabled_print_services");
            this.mContext.getContentResolver().registerContentObserver(enabledPrintServicesUri, false, new ContentObserver(BackgroundThread.getHandler()) {
                public void onChange(boolean selfChange, Uri uri, int userId) {
                    if (enabledPrintServicesUri.equals(uri)) {
                        synchronized (PrintManagerImpl.this.mLock) {
                            int userCount = PrintManagerImpl.this.mUserStates.size();
                            for (int i = 0; i < userCount; i++) {
                                if (userId == -1 || userId == PrintManagerImpl.this.mUserStates.keyAt(i)) {
                                    ((UserState) PrintManagerImpl.this.mUserStates.valueAt(i)).updateIfNeededLocked();
                                }
                            }
                        }
                    }
                }
            }, -1);
        }

        private void registerBroadcastReceivers() {
            new PackageMonitor() {
                private boolean hasPrintService(String packageName) {
                    Intent intent = new Intent("android.printservice.PrintService");
                    intent.setPackage(packageName);
                    List<ResolveInfo> installedServices = PrintManagerImpl.this.mContext.getPackageManager().queryIntentServicesAsUser(intent, 276824068, getChangingUserId());
                    return installedServices != null && !installedServices.isEmpty();
                }

                private boolean hadPrintService(UserState userState, String packageName) {
                    List<PrintServiceInfo> installedServices = userState.getPrintServices(3);
                    if (installedServices == null) {
                        return false;
                    }
                    int numInstalledServices = installedServices.size();
                    for (int i = 0; i < numInstalledServices; i++) {
                        if (installedServices.get(i).getResolveInfo().serviceInfo.packageName.equals(packageName)) {
                            return true;
                        }
                    }
                    return false;
                }

                public void onPackageModified(String packageName) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
                        boolean prunePrintServices = false;
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hadPrintService(userState, packageName) || hasPrintService(packageName)) {
                                userState.updateIfNeededLocked();
                                prunePrintServices = true;
                            }
                        }
                        if (prunePrintServices) {
                            userState.prunePrintServices();
                        }
                    }
                }

                public void onPackageRemoved(String packageName, int uid) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
                        boolean prunePrintServices = false;
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hadPrintService(userState, packageName)) {
                                userState.updateIfNeededLocked();
                                prunePrintServices = true;
                            }
                        }
                        if (prunePrintServices) {
                            userState.prunePrintServices();
                        }
                    }
                }

                /* JADX WARNING: Code restructure failed: missing block: B:26:0x0065, code lost:
                    return false;
                 */
                public boolean onHandleForceStop(Intent intent, String[] stoppedPackages, int uid, boolean doit) {
                    String[] strArr = stoppedPackages;
                    if (!PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        return false;
                    }
                    synchronized (PrintManagerImpl.this.mLock) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
                        boolean stoppedSomePackages = false;
                        List<PrintServiceInfo> enabledServices = userState.getPrintServices(1);
                        if (enabledServices == null) {
                            return false;
                        }
                        for (PrintServiceInfo componentName : enabledServices) {
                            String componentPackage = componentName.getComponentName().getPackageName();
                            int length = strArr.length;
                            int i = 0;
                            while (true) {
                                if (i >= length) {
                                    break;
                                } else if (!componentPackage.equals(strArr[i])) {
                                    i++;
                                } else if (!doit) {
                                    return true;
                                } else {
                                    stoppedSomePackages = true;
                                }
                            }
                        }
                        if (stoppedSomePackages) {
                            userState.updateIfNeededLocked();
                        }
                    }
                }

                public void onPackageAdded(String packageName, int uid) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hasPrintService(packageName)) {
                                PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false).updateIfNeededLocked();
                            }
                        }
                    }
                }
            }.register(this.mContext, BackgroundThread.getHandler().getLooper(), UserHandle.ALL, true);
        }

        private UserState getOrCreateUserStateLocked(int userId, boolean lowPriority) {
            return getOrCreateUserStateLocked(userId, lowPriority, true);
        }

        /* access modifiers changed from: private */
        public UserState getOrCreateUserStateLocked(int userId, boolean lowPriority, boolean enforceUserUnlockingOrUnlocked) {
            if (!enforceUserUnlockingOrUnlocked || this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                UserState userState = this.mUserStates.get(userId);
                if (userState == null) {
                    userState = new UserState(this.mContext, userId, this.mLock, lowPriority);
                    this.mUserStates.put(userId, userState);
                }
                if (!lowPriority) {
                    userState.increasePriority();
                }
                return userState;
            }
            throw new IllegalStateException("User " + userId + " must be unlocked for printing to be available");
        }

        /* access modifiers changed from: private */
        public void handleUserUnlocked(final int userId) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    UserState userState;
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                        synchronized (PrintManagerImpl.this.mLock) {
                            userState = PrintManagerImpl.this.getOrCreateUserStateLocked(userId, true, false);
                            userState.updateIfNeededLocked();
                        }
                        userState.removeObsoletePrintJobs();
                    }
                }
            });
        }

        /* access modifiers changed from: private */
        public void handleUserStopped(final int userId) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    synchronized (PrintManagerImpl.this.mLock) {
                        UserState userState = (UserState) PrintManagerImpl.this.mUserStates.get(userId);
                        if (userState != null) {
                            userState.destroyLocked();
                            PrintManagerImpl.this.mUserStates.remove(userId);
                        }
                    }
                }
            });
        }

        private int resolveCallingProfileParentLocked(int userId) {
            if (userId == getCurrentUserId()) {
                return userId;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo parent = this.mUserManager.getProfileParent(userId);
                if (parent != null) {
                    return parent.getUserHandle().getIdentifier();
                }
                Binder.restoreCallingIdentity(identity);
                return -10;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private int resolveCallingAppEnforcingPermissions(int appId) {
            int callingUid = Binder.getCallingUid();
            if (callingUid == 0) {
                return appId;
            }
            int callingAppId = UserHandle.getAppId(callingUid);
            if (appId == callingAppId || callingAppId == 2000 || callingAppId == 1000 || this.mContext.checkCallingPermission("com.android.printspooler.permission.ACCESS_ALL_PRINT_JOBS") == 0) {
                return appId;
            }
            throw new SecurityException("Call from app " + callingAppId + " as app " + appId + " without com.android.printspooler.permission.ACCESS_ALL_PRINT_JOBS");
        }

        private int resolveCallingUserEnforcingPermissions(int userId) {
            try {
                return ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, null);
            } catch (RemoteException e) {
                return userId;
            }
        }

        private String resolveCallingPackageNameEnforcingSecurity(String packageName) {
            for (String equals : this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())) {
                if (packageName.equals(equals)) {
                    return packageName;
                }
            }
            throw new IllegalArgumentException("packageName has to belong to the caller");
        }

        private int getCurrentUserId() {
            long identity = Binder.clearCallingIdentity();
            try {
                return ActivityManager.getCurrentUser();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public PrintManagerService(Context context) {
        super(context);
        this.mPrintManagerImpl = new PrintManagerImpl(context);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.print.PrintManagerService$PrintManagerImpl, android.os.IBinder] */
    public void onStart() {
        publishBinderService("print", this.mPrintManagerImpl);
    }

    public void onUnlockUser(int userHandle) {
        this.mPrintManagerImpl.handleUserUnlocked(userHandle);
    }

    public void onStopUser(int userHandle) {
        this.mPrintManagerImpl.handleUserStopped(userHandle);
    }
}
