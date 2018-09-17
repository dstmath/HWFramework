package com.android.server.print;

import android.app.ActivityManager;
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
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
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
        private final Object mLock = new Object();
        private final UserManager mUserManager;
        private final SparseArray<UserState> mUserStates = new SparseArray();

        PrintManagerImpl(Context context) {
            this.mContext = context;
            this.mUserManager = (UserManager) context.getSystemService("user");
            registerContentObservers();
            registerBroadcastReceivers();
        }

        /* JADX WARNING: Missing block: B:11:0x0034, code:
            r6 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r1 = r0.print(r10, r11, r12, r4, r5);
     */
        /* JADX WARNING: Missing block: B:19:0x0047, code:
            android.os.Binder.restoreCallingIdentity(r6);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
            }
            return r1;
        }

        /* JADX WARNING: Missing block: B:11:0x001e, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r5 = r4.getPrintJobInfos(r2);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public List<PrintJobInfo> getPrintJobInfos(int appId, int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
            return r5;
        }

        /* JADX WARNING: Missing block: B:13:0x0021, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:15:?, code:
            r5 = r4.getPrintJobInfo(r10, r2);
     */
        /* JADX WARNING: Missing block: B:21:0x0031, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
            }
            return r5;
        }

        /* JADX WARNING: Missing block: B:12:0x0020, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r4 = r3.getCustomPrinterIcon(r8);
     */
        /* JADX WARNING: Missing block: B:20:0x0030, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Icon getCustomPrinterIcon(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
            return r4;
        }

        /* JADX WARNING: Missing block: B:12:0x0020, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r4.cancelPrintJob(r9, r2);
     */
        /* JADX WARNING: Missing block: B:20:0x002f, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void cancelPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    } else {
                        int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                        UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:12:0x0020, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r4.restartPrintJob(r9, r2);
     */
        /* JADX WARNING: Missing block: B:20:0x002f, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void restartPrintJob(PrintJobId printJobId, int appId, int userId) {
            if (printJobId != null) {
                int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
                synchronized (this.mLock) {
                    if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    } else {
                        int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                        UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:12:0x001e, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r4 = r3.getPrintServices(r8);
     */
        /* JADX WARNING: Missing block: B:20:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public List<PrintServiceInfo> getPrintServices(int selectionFlags, int userId) {
            Preconditions.checkFlagsArgument(selectionFlags, 3);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
            return r4;
        }

        /* JADX WARNING: Missing block: B:21:0x0052, code:
            r2 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:23:?, code:
            r5.setPrintServiceEnabled(r10, r11);
     */
        /* JADX WARNING: Missing block: B:29:0x0061, code:
            android.os.Binder.restoreCallingIdentity(r2);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setPrintServiceEnabled(ComponentName service, boolean isEnabled, int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            int appId = UserHandle.getAppId(Binder.getCallingUid());
            if (appId != 1000) {
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
            }
        }

        /* JADX WARNING: Missing block: B:12:0x001a, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r4 = r3.getPrintServiceRecommendations();
     */
        /* JADX WARNING: Missing block: B:20:0x002a, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public List<RecommendationInfo> getPrintServiceRecommendations(int userId) {
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return null;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
            return r4;
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.createPrinterDiscoverySession(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void createPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.destroyPrinterDiscoverySession(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:14:0x002a, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:16:?, code:
            r3.startPrinterDiscovery(r8, r9);
     */
        /* JADX WARNING: Missing block: B:22:0x0039, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.stopPrinterDiscovery(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void stopPrinterDiscovery(IPrinterDiscoveryObserver observer, int userId) {
            observer = (IPrinterDiscoveryObserver) Preconditions.checkNotNull(observer);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x0022, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.validatePrinters(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x0031, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void validatePrinters(List<PrinterId> printerIds, int userId) {
            printerIds = (List) Preconditions.checkCollectionElementsNotNull(printerIds, "PrinterId");
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.startPrinterStateTracking(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startPrinterStateTracking(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.stopPrinterStateTracking(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void stopPrinterStateTracking(PrinterId printerId, int userId) {
            printerId = (PrinterId) Preconditions.checkNotNull(printerId);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:10:0x0023, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:12:?, code:
            r4.addPrintJobStateChangeListener(r9, r2);
     */
        /* JADX WARNING: Missing block: B:18:0x0032, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addPrintJobStateChangeListener(IPrintJobStateChangeListener listener, int appId, int userId) throws RemoteException {
            listener = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                } else {
                    int resolvedAppId = resolveCallingAppEnforcingPermissions(appId);
                    UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
                }
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.removePrintJobStateChangeListener(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void removePrintJobStateChangeListener(IPrintJobStateChangeListener listener, int userId) {
            listener = (IPrintJobStateChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.addPrintServicesChangeListener(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addPrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) throws RemoteException {
            listener = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.removePrintServicesChangeListener(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void removePrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) {
            listener = (IPrintServicesChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.addPrintServiceRecommendationsChangeListener(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) throws RemoteException {
            listener = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        /* JADX WARNING: Missing block: B:11:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r3.removePrintServiceRecommendationsChangeListener(r8);
     */
        /* JADX WARNING: Missing block: B:19:0x002e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) {
            listener = (IRecommendationsChangeListener) Preconditions.checkNotNull(listener);
            int resolvedUserId = resolveCallingUserEnforcingPermissions(userId);
            synchronized (this.mLock) {
                if (resolveCallingProfileParentLocked(resolvedUserId) != getCurrentUserId()) {
                    return;
                }
                UserState userState = getOrCreateUserStateLocked(resolvedUserId, false);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            fd = (FileDescriptor) Preconditions.checkNotNull(fd);
            pw = (PrintWriter) Preconditions.checkNotNull(pw);
            if (DumpUtils.checkDumpPermission(this.mContext, PrintManagerService.LOG_TAG, pw)) {
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
        }

        private void registerContentObservers() {
            final Uri enabledPrintServicesUri = Secure.getUriFor("disabled_print_services");
            this.mContext.getContentResolver().registerContentObserver(enabledPrintServicesUri, false, new ContentObserver(BackgroundThread.getHandler()) {
                public void onChange(boolean selfChange, Uri uri, int userId) {
                    if (enabledPrintServicesUri.equals(uri)) {
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
            }, -1);
        }

        private void registerBroadcastReceivers() {
            new PackageMonitor() {
                private boolean hasPrintService(String packageName) {
                    Intent intent = new Intent("android.printservice.PrintService");
                    intent.setPackage(packageName);
                    List<ResolveInfo> installedServices = PrintManagerImpl.this.mContext.getPackageManager().queryIntentServicesAsUser(intent, 268435460, getChangingUserId());
                    return installedServices != null ? installedServices.isEmpty() ^ 1 : false;
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
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
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
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
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
                        UserState userState = PrintManagerImpl.this.getOrCreateUserStateLocked(getChangingUserId(), false, false);
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

        private UserState getOrCreateUserStateLocked(int userId, boolean lowPriority, boolean enforceUserUnlockingOrUnlocked) {
            if (!enforceUserUnlockingOrUnlocked || (this.mUserManager.isUserUnlockingOrUnlocked(userId) ^ 1) == 0) {
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

        private void handleUserUnlocked(final int userId) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    if (PrintManagerImpl.this.mUserManager.isUserUnlockingOrUnlocked(userId)) {
                        UserState userState;
                        synchronized (PrintManagerImpl.this.mLock) {
                            userState = PrintManagerImpl.this.getOrCreateUserStateLocked(userId, true, false);
                            userState.updateIfNeededLocked();
                        }
                        userState.removeObsoletePrintJobs();
                    }
                }
            });
        }

        private void handleUserStopped(final int userId) {
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
                    int identifier = parent.getUserHandle().getIdentifier();
                    return identifier;
                }
                Binder.restoreCallingIdentity(identity);
                return -10;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private int resolveCallingAppEnforcingPermissions(int appId) {
            int callingUid = Binder.getCallingUid();
            if (callingUid == 0 || callingUid == 1000 || callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
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
                return ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "", null);
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
