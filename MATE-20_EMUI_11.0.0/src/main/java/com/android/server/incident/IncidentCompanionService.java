package com.android.server.incident;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IIncidentAuthListener;
import android.os.IIncidentCompanion;
import android.os.IIncidentManager;
import android.os.IncidentManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.DumpUtils;
import com.android.server.SystemService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class IncidentCompanionService extends SystemService {
    private static final String[] DUMP_AND_USAGE_STATS_PERMISSIONS = {"android.permission.DUMP", "android.permission.PACKAGE_USAGE_STATS"};
    private static String[] RESTRICTED_IMAGE_DUMP_ARGS = {"--hal", "--restricted_image"};
    static final String TAG = "IncidentCompanionService";
    private PendingReports mPendingReports;

    private final class BinderService extends IIncidentCompanion.Stub {
        private BinderService() {
        }

        public void authorizeReport(int callingUid, String callingPackage, String receiverClass, String reportId, int flags, IIncidentAuthListener listener) {
            enforceRequestAuthorizationPermission();
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.mPendingReports.authorizeReport(callingUid, callingPackage, receiverClass, reportId, flags, listener);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancelAuthorization(IIncidentAuthListener listener) {
            enforceRequestAuthorizationPermission();
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.mPendingReports.cancelAuthorization(listener);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void sendReportReadyBroadcast(String pkg, String cls) {
            enforceRequestAuthorizationPermission();
            long ident = Binder.clearCallingIdentity();
            try {
                Context context = IncidentCompanionService.this.getContext();
                int primaryUser = IncidentCompanionService.getAndValidateUser(context);
                if (primaryUser != -10000) {
                    Intent intent = new Intent("android.intent.action.INCIDENT_REPORT_READY");
                    intent.setComponent(new ComponentName(pkg, cls));
                    Log.d(IncidentCompanionService.TAG, "sendReportReadyBroadcast sending primaryUser=" + primaryUser + " userHandle=" + UserHandle.getUserHandleForUid(primaryUser) + " intent=" + intent);
                    context.sendBroadcastAsUserMultiplePermissions(intent, UserHandle.getUserHandleForUid(primaryUser), IncidentCompanionService.DUMP_AND_USAGE_STATS_PERMISSIONS);
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public List<String> getPendingReports() {
            enforceAuthorizePermission();
            return IncidentCompanionService.this.mPendingReports.getPendingReports();
        }

        public void approveReport(String uri) {
            enforceAuthorizePermission();
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.mPendingReports.approveReport(uri);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void denyReport(String uri) {
            enforceAuthorizePermission();
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.mPendingReports.denyReport(uri);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public List<String> getIncidentReportList(String pkg, String cls) throws RemoteException {
            enforceAccessReportsPermissions(null);
            long ident = Binder.clearCallingIdentity();
            try {
                return IncidentCompanionService.this.getIIncidentManager().getIncidentReportList(pkg, cls);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void deleteIncidentReports(String pkg, String cls, String id) throws RemoteException {
            if (pkg == null || cls == null || id == null || pkg.length() == 0 || cls.length() == 0 || id.length() == 0) {
                throw new RuntimeException("Invalid pkg, cls or id");
            }
            enforceAccessReportsPermissions(pkg);
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.getIIncidentManager().deleteIncidentReports(pkg, cls, id);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void deleteAllIncidentReports(String pkg) throws RemoteException {
            if (pkg == null || pkg.length() == 0) {
                throw new RuntimeException("Invalid pkg");
            }
            enforceAccessReportsPermissions(pkg);
            long ident = Binder.clearCallingIdentity();
            try {
                IncidentCompanionService.this.getIIncidentManager().deleteAllIncidentReports(pkg);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public IncidentManager.IncidentReport getIncidentReport(String pkg, String cls, String id) throws RemoteException {
            if (pkg == null || cls == null || id == null || pkg.length() == 0 || cls.length() == 0 || id.length() == 0) {
                throw new RuntimeException("Invalid pkg, cls or id");
            }
            enforceAccessReportsPermissions(pkg);
            long ident = Binder.clearCallingIdentity();
            try {
                return IncidentCompanionService.this.getIIncidentManager().getIncidentReport(pkg, cls, id);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            if (DumpUtils.checkDumpPermission(IncidentCompanionService.this.getContext(), IncidentCompanionService.TAG, writer)) {
                if (args.length != 1 || !"--restricted_image".equals(args[0])) {
                    IncidentCompanionService.this.mPendingReports.dump(fd, writer, args);
                } else {
                    dumpRestrictedImages(fd);
                }
            }
        }

        private void dumpRestrictedImages(FileDescriptor fd) {
            if (Build.IS_ENG || Build.IS_USERDEBUG) {
                String[] services = IncidentCompanionService.this.getContext().getResources().getStringArray(17236050);
                for (String name : services) {
                    Log.d(IncidentCompanionService.TAG, "Looking up service " + name);
                    IBinder service = ServiceManager.getService(name);
                    if (service != null) {
                        Log.d(IncidentCompanionService.TAG, "Calling dump on service: " + name);
                        try {
                            service.dump(fd, IncidentCompanionService.RESTRICTED_IMAGE_DUMP_ARGS);
                        } catch (RemoteException ex) {
                            Log.w(IncidentCompanionService.TAG, "dump --restricted_image of " + name + " threw", ex);
                        }
                    }
                }
            }
        }

        private void enforceRequestAuthorizationPermission() {
            IncidentCompanionService.this.getContext().enforceCallingOrSelfPermission("android.permission.REQUEST_INCIDENT_REPORT_APPROVAL", null);
        }

        private void enforceAuthorizePermission() {
            IncidentCompanionService.this.getContext().enforceCallingOrSelfPermission("android.permission.APPROVE_INCIDENT_REPORTS", null);
        }

        private void enforceAccessReportsPermissions(String pkg) {
            if (IncidentCompanionService.this.getContext().checkCallingPermission("android.permission.APPROVE_INCIDENT_REPORTS") != 0) {
                IncidentCompanionService.this.getContext().enforceCallingOrSelfPermission("android.permission.DUMP", null);
                IncidentCompanionService.this.getContext().enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
                if (pkg != null) {
                    enforceCallerIsSameApp(pkg);
                }
            }
        }

        private void enforceCallerIsSameApp(String pkg) throws SecurityException {
            try {
                int uid = Binder.getCallingUid();
                ApplicationInfo ai = IncidentCompanionService.this.getContext().getPackageManager().getApplicationInfoAsUser(pkg, 0, UserHandle.getCallingUserId());
                if (ai == null) {
                    throw new SecurityException("Unknown package " + pkg);
                } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                    throw new SecurityException("Calling uid " + uid + " gave package " + pkg + " which is owned by uid " + ai.uid);
                }
            } catch (PackageManager.NameNotFoundException re) {
                throw new SecurityException("Unknown package " + pkg + "\n" + re);
            }
        }
    }

    public IncidentCompanionService(Context context) {
        super(context);
        this.mPendingReports = new PendingReports(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.incident.IncidentCompanionService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.incident.IncidentCompanionService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("incidentcompanion", new BinderService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        if (phase == 1000) {
            this.mPendingReports.onBootCompleted();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IIncidentManager getIIncidentManager() throws RemoteException {
        return IIncidentManager.Stub.asInterface(ServiceManager.getService("incident"));
    }

    public static int getAndValidateUser(Context context) {
        try {
            UserInfo currentUser = ActivityManager.getService().getCurrentUser();
            UserInfo primaryUser = UserManager.get(context).getPrimaryUser();
            if (currentUser == null) {
                Log.w(TAG, "No current user.  Nobody to approve the report. The report will be denied.");
                return -10000;
            } else if (primaryUser == null) {
                Log.w(TAG, "No primary user.  Nobody to approve the report. The report will be denied.");
                return -10000;
            } else if (primaryUser.id == currentUser.id) {
                return primaryUser.id;
            } else {
                Log.w(TAG, "Only the primary user can approve bugreports, but they are not the current user. The report will be denied.");
                return -10000;
            }
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
}
