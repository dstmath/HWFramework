package com.android.server.print;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.print.IPrintDocumentAdapter;
import android.print.IPrintJobStateChangeListener;
import android.print.IPrintManager.Stub;
import android.print.IPrintServicesChangeListener;
import android.print.IPrinterDiscoveryObserver;
import android.print.PrintAttributes;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.printservice.PrintServiceInfo;
import android.printservice.recommendation.IRecommendationsChangeListener;
import android.printservice.recommendation.RecommendationInfo;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import com.android.server.power.IHwShutdownThread;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public final class PrintManagerService extends SystemService {
    private static final String LOG_TAG = "PrintManagerService";
    private final PrintManagerImpl mPrintManagerImpl;

    class PrintManagerImpl extends Stub {
        private static final int BACKGROUND_USER_ID = -10;
        private final Context mContext;
        private final Object mLock;
        private final UserManager mUserManager;
        private final SparseArray<UserState> mUserStates;

        /* renamed from: com.android.server.print.PrintManagerService.PrintManagerImpl.1 */
        class AnonymousClass1 extends ContentObserver {
            final /* synthetic */ Uri val$enabledPrintServicesUri;

            AnonymousClass1(Handler $anonymous0, Uri val$enabledPrintServicesUri) {
                this.val$enabledPrintServicesUri = val$enabledPrintServicesUri;
                super($anonymous0);
            }

            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (this.val$enabledPrintServicesUri.equals(uri)) {
                    synchronized (PrintManagerImpl.this.mLock) {
                        int userCount = PrintManagerImpl.this.mUserStates.size();
                        int i = 0;
                        while (i < userCount) {
                            if (userId == -1 || userId == PrintManagerImpl.this.mUserStates.keyAt(i)) {
                                ((UserState) PrintManagerImpl.this.mUserStates.valueAt(i)).updateIfNeededLocked();
                            }
                            i++;
                        }
                    }
                }
            }
        }

        /* renamed from: com.android.server.print.PrintManagerService.PrintManagerImpl.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ int val$userId;

            AnonymousClass3(int val$userId) {
                this.val$userId = val$userId;
            }

            public void run() {
                if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(this.val$userId)) {
                    UserState userState;
                    synchronized (PrintManagerImpl.this.mLock) {
                        userState = PrintManagerImpl.this.getOrCreateUserStateLocked(this.val$userId, true);
                        userState.updateIfNeededLocked();
                    }
                    userState.removeObsoletePrintJobs();
                }
            }
        }

        /* renamed from: com.android.server.print.PrintManagerService.PrintManagerImpl.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ int val$userId;

            AnonymousClass4(int val$userId) {
                this.val$userId = val$userId;
            }

            public void run() {
                synchronized (PrintManagerImpl.this.mLock) {
                    UserState userState = (UserState) PrintManagerImpl.this.mUserStates.get(this.val$userId);
                    if (userState != null) {
                        userState.destroyLocked();
                        PrintManagerImpl.this.mUserStates.remove(this.val$userId);
                    }
                }
            }
        }

        PrintManagerImpl(Context context) {
            this.mLock = new Object();
            this.mUserStates = new SparseArray();
            this.mContext = context;
            this.mUserManager = (UserManager) context.getSystemService("user");
            registerContentObservers();
            registerBroadcastReceivers();
        }

        public Bundle print(String printJobName, IPrintDocumentAdapter adapter, PrintAttributes attributes, String packageName, int appId, int userId) {
            printJobName = (String) Preconditions.checkStringNotEmpty(printJobName);
            adapter = (IPrintDocumentAdapter) Preconditions.checkNotNull(adapter);
            packageName = (String) Preconditions.checkStringNotEmpty(packageName);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                String resolvedPackageName = resolveCallingPackageNameEnforcingSecurity(packageName);
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    Bundle print = userState.print(printJobName, adapter, attributes, resolvedPackageName, resolvedAppId);
                    return print;
                } finally {
                    Binder.restoreCallingIdentity(identity);
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
                    List<PrintJobInfo> printJobInfos = userState.getPrintJobInfos(resolvedAppId);
                    return printJobInfos;
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
                    PrintJobInfo printJobInfo = userState.getPrintJobInfo(printJobId, resolvedAppId);
                    return printJobInfo;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public Icon getCustomPrinterIcon(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    Icon customPrinterIcon = userState.getCustomPrinterIcon(printerId);
                    return customPrinterIcon;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void cancelPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                        return;
                    }
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

        public void restartPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                        return;
                    }
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

        public List<PrintServiceInfo> getPrintServices(int selectionFlags, int userId) {
            Preconditions.checkFlagsArgument(selectionFlags, 3);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    List<PrintServiceInfo> printServices = userState.getPrintServices(selectionFlags);
                    return printServices;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void setPrintServiceEnabled(ComponentName service, boolean isEnabled, int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            int appId = UserHandle.getAppId(Binder.getCallingUid());
            if (appId != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                try {
                    if (appId != UserHandle.getAppId(this.mContext.getPackageManager().getPackageUidAsUser("com.android.printspooler", resolvedUserId))) {
                        throw new SecurityException("Only system and print spooler can call this");
                    }
                } catch (NameNotFoundException e) {
                    Log.e(PrintManagerService.LOG_TAG, "Could not verify caller", e);
                    return;
                }
            }
            service = (ComponentName) Preconditions.checkNotNull(service);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.setPrintServiceEnabled(service, isEnabled);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public List<RecommendationInfo> getPrintServiceRecommendations(int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    List<RecommendationInfo> printServiceRecommendations = userState.getPrintServiceRecommendations();
                    return printServiceRecommendations;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void createPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.createPrinterDiscoverySession(observer);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.destroyPrinterDiscoverySession(observer);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void startPrinterDiscovery(IPrinterDiscoveryObserver observer, List<PrinterId> priorityList, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            if (priorityList != null) {
                priorityList = (List) Preconditions.checkCollectionElementsNotNull(priorityList, "PrinterId");
            }
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.startPrinterDiscovery(observer, priorityList);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void stopPrinterDiscovery(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.stopPrinterDiscovery(observer);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void validatePrinters(List<PrinterId> printerIds, int userId) {
            printerIds = (List) Preconditions.checkCollectionElementsNotNull(printerIds, "PrinterId");
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.validatePrinters(printerIds);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void startPrinterStateTracking(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.startPrinterStateTracking(printerId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void stopPrinterStateTracking(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.stopPrinterStateTracking(printerId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void addPrintJobStateChangeListener(IPrintJobStateChangeListener listener, int appId, int userId) throws RemoteException {
            listener = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.addPrintJobStateChangeListener(listener, resolvedAppId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void removePrintJobStateChangeListener(IPrintJobStateChangeListener listener, int userId) {
            listener = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.removePrintJobStateChangeListener(listener);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void addPrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) throws RemoteException {
            listener = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.addPrintServicesChangeListener(listener);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void removePrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) {
            listener = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.removePrintServicesChangeListener(listener);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) throws RemoteException {
            listener = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.addPrintServiceRecommendationsChangeListener(listener);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) {
            listener = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                long identity = Binder.clearCallingIdentity();
                try {
                    userState.removePrintServiceRecommendationsChangeListener(listener);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            fd = (FileDescriptor) Preconditions.checkNotNull(fd);
            pw = (PrintWriter) Preconditions.checkNotNull(pw);
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump PrintManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            synchronized (this.mLock) {
                long identity = Binder.clearCallingIdentity();
                try {
                    pw.println("PRINT MANAGER STATE (dumpsys print)");
                    int userStateCount = this.mUserStates.size();
                    for (int i = 0; i < userStateCount; i++) {
                        ((UserState) this.mUserStates.valueAt(i)).dump(fd, pw, "");
                        pw.println();
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        private void registerContentObservers() {
            Uri enabledPrintServicesUri = Secure.getUriFor("disabled_print_services");
            this.mContext.getContentResolver().registerContentObserver(enabledPrintServicesUri, false, new AnonymousClass1(BackgroundThread.getHandler(), enabledPrintServicesUri), -1);
        }

        private void registerBroadcastReceivers() {
            new PackageMonitor() {
                private boolean hasPrintService(String packageName) {
                    Intent intent = new Intent("android.printservice.PrintService");
                    intent.setPackage(packageName);
                    List<ResolveInfo> installedServices = PrintManagerImpl.this.mContext.getPackageManager().queryIntentServicesAsUser(intent, 268435460, getChangingUserId());
                    if (installedServices == null || installedServices.isEmpty()) {
                        return false;
                    }
                    return true;
                }

                private boolean hadPrintService(UserState userState, String packageName) {
                    List<PrintServiceInfo> installedServices = userState.getPrintServices(3);
                    if (installedServices == null) {
                        return false;
                    }
                    int numInstalledServices = installedServices.size();
                    for (int i = 0; i < numInstalledServices; i++) {
                        if (((PrintServiceInfo) installedServices.get(i)).getResolveInfo().serviceInfo.packageName.equals(packageName)) {
                            return true;
                        }
                    }
                    return false;
                }

                public void onPackageModified(String packageName) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false);
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hadPrintService(userState, packageName) || hasPrintService(packageName)) {
                                userState.updateIfNeededLocked();
                            }
                        }
                        userState.prunePrintServices();
                    }
                }

                public void onPackageRemoved(String packageName, int uid) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false);
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hadPrintService(userState, packageName)) {
                                userState.updateIfNeededLocked();
                            }
                        }
                        userState.prunePrintServices();
                    }
                }

                public boolean onHandleForceStop(Intent intent, String[] stoppedPackages, int uid, boolean doit) {
                    if (!PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        return false;
                    }
                    synchronized (PrintManagerImpl.this.mLock) {
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false);
                        boolean stoppedSomePackages = false;
                        List<PrintServiceInfo> enabledServices = userState.getPrintServices(1);
                        if (enabledServices == null) {
                            return false;
                        }
                        for (PrintServiceInfo componentName : enabledServices) {
                            String componentPackage = componentName.getComponentName().getPackageName();
                            int i = 0;
                            int length = stoppedPackages.length;
                            while (i < length) {
                                if (!componentPackage.equals(stoppedPackages[i])) {
                                    i++;
                                } else if (doit) {
                                    stoppedSomePackages = true;
                                } else {
                                    return true;
                                }
                            }
                        }
                        if (stoppedSomePackages) {
                            userState.updateIfNeededLocked();
                        }
                        return false;
                    }
                }

                public void onPackageAdded(String packageName, int uid) {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(getChangingUserId())) {
                        synchronized (PrintManagerImpl.this.mLock) {
                            if (hasPrintService(packageName)) {
                                PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false).updateIfNeededLocked();
                            }
                        }
                    }
                }
            }.register(this.mContext, BackgroundThread.getHandler().getLooper(), UserHandle.ALL, true);
        }

        private UserState getOrCreateUserStateLocked(int userId, boolean lowPriority) {
            if (this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                UserState userState = (UserState) this.mUserStates.get(userId);
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

        private void handleUserUnlocked(int userId) {
            BackgroundThread.getHandler().post(new AnonymousClass3(userId));
        }

        private void handleUserStopped(int userId) {
            BackgroundThread.getHandler().post(new AnonymousClass4(userId));
        }

        private int resolveCallingProfileParentLocked(int userId) {
            if (userId == getCurrentUserId()) {
                return userId;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                UserInfo parent = this.mUserManager.getProfileParent(userId);
                if (parent != null) {
                    int identifier = parent.getUserHandle().getIdentifier();
                    return identifier;
                }
                Binder.restoreCallingIdentity(identity);
                return BACKGROUND_USER_ID;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private int resolveCallingAppEnforcingPermissions(int appId) {
            int callingUid = Binder.getCallingUid();
            if (callingUid == 0 || callingUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
                return appId;
            }
            int callingAppId = UserHandle.getAppId(callingUid);
            if (appId == callingAppId || this.mContext.checkCallingPermission("com.android.printspooler.permission.ACCESS_ALL_PRINT_JOBS") == 0) {
                return appId;
            }
            throw new SecurityException("Call from app " + callingAppId + " as app " + appId + " without com.android.printspooler.permission" + ".ACCESS_ALL_PRINT_JOBS");
        }

        private int resolveCallingUserEnforcingPermissions(int userId) {
            try {
                return ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "", null);
            } catch (RemoteException e) {
                return userId;
            }
        }

        private String resolveCallingPackageNameEnforcingSecurity(String packageName) {
            for (Object equals : this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())) {
                if (packageName.equals(equals)) {
                    return packageName;
                }
            }
            throw new IllegalArgumentException("packageName has to belong to the caller");
        }

        private int getCurrentUserId() {
            long identity = Binder.clearCallingIdentity();
            try {
                int currentUser = ActivityManager.getCurrentUser();
                return currentUser;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public PrintManagerService(Context context) {
        super(context);
        this.mPrintManagerImpl = new PrintManagerImpl(context);
    }

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
