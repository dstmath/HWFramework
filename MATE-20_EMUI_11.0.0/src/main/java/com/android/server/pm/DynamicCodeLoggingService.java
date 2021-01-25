package com.android.server.pm;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.os.ServiceManager;
import android.util.ByteStringUtils;
import android.util.EventLog;
import android.util.Log;
import com.android.server.pm.dex.DynamicCodeLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicCodeLoggingService extends JobService {
    private static final int AUDIT_AVC = 1400;
    private static final int AUDIT_WATCHING_JOB_ID = 203142925;
    private static final long AUDIT_WATCHING_PERIOD_MILLIS = TimeUnit.HOURS.toMillis(2);
    private static final String AVC_PREFIX = "type=1400 ";
    private static final boolean DEBUG = false;
    private static final Pattern EXECUTE_NATIVE_AUDIT_PATTERN = Pattern.compile(".*\\bavc: granted \\{ execute(?:_no_trans|) \\} .*\\bpath=(?:\"([^\" ]*)\"|([0-9A-F]+)) .*\\bscontext=u:r:untrusted_app(?:_25|_27)?:.*\\btcontext=u:object_r:app_data_file:.*\\btclass=file\\b.*");
    private static final int IDLE_LOGGING_JOB_ID = 2030028;
    private static final long IDLE_LOGGING_PERIOD_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final String TAG = DynamicCodeLoggingService.class.getName();
    private volatile boolean mAuditWatchingStopRequested = false;
    private volatile boolean mIdleLoggingStopRequested = false;

    public static void schedule(Context context) {
        ComponentName serviceName = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, DynamicCodeLoggingService.class.getName());
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        js.schedule(new JobInfo.Builder(IDLE_LOGGING_JOB_ID, serviceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(IDLE_LOGGING_PERIOD_MILLIS).build());
        js.schedule(new JobInfo.Builder(AUDIT_WATCHING_JOB_ID, serviceName).setRequiresDeviceIdle(true).setRequiresBatteryNotLow(true).setPeriodic(AUDIT_WATCHING_PERIOD_MILLIS).build());
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getJobId();
        if (jobId == IDLE_LOGGING_JOB_ID) {
            this.mIdleLoggingStopRequested = false;
            new IdleLoggingThread(params).start();
            return true;
        } else if (jobId != AUDIT_WATCHING_JOB_ID) {
            return false;
        } else {
            this.mAuditWatchingStopRequested = false;
            new AuditWatchingThread(params).start();
            return true;
        }
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters params) {
        int jobId = params.getJobId();
        if (jobId == IDLE_LOGGING_JOB_ID) {
            this.mIdleLoggingStopRequested = true;
            return true;
        } else if (jobId != AUDIT_WATCHING_JOB_ID) {
            return false;
        } else {
            this.mAuditWatchingStopRequested = true;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static DynamicCodeLogger getDynamicCodeLogger() {
        return ((PackageManagerService) ServiceManager.getService("package")).getDexManager().getDynamicCodeLogger();
    }

    private class IdleLoggingThread extends Thread {
        private final JobParameters mParams;

        IdleLoggingThread(JobParameters params) {
            super("DynamicCodeLoggingService_IdleLoggingJob");
            this.mParams = params;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            DynamicCodeLogger dynamicCodeLogger = DynamicCodeLoggingService.getDynamicCodeLogger();
            for (String packageName : dynamicCodeLogger.getAllPackagesWithDynamicCodeLoading()) {
                if (DynamicCodeLoggingService.this.mIdleLoggingStopRequested) {
                    Log.w(DynamicCodeLoggingService.TAG, "Stopping IdleLoggingJob run at scheduler request");
                    return;
                }
                dynamicCodeLogger.logDynamicCodeLoading(packageName);
            }
            DynamicCodeLoggingService.this.jobFinished(this.mParams, false);
        }
    }

    private class AuditWatchingThread extends Thread {
        private final JobParameters mParams;

        AuditWatchingThread(JobParameters params) {
            super("DynamicCodeLoggingService_AuditWatchingJob");
            this.mParams = params;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            if (processAuditEvents()) {
                DynamicCodeLoggingService.this.jobFinished(this.mParams, false);
            }
        }

        private boolean processAuditEvents() {
            try {
                int[] tags = {EventLog.getTagCode("auditd")};
                if (tags[0] == -1) {
                    return true;
                }
                DynamicCodeLogger dynamicCodeLogger = DynamicCodeLoggingService.getDynamicCodeLogger();
                List<EventLog.Event> events = new ArrayList<>();
                EventLog.readEvents(tags, events);
                for (int i = 0; i < events.size(); i++) {
                    if (DynamicCodeLoggingService.this.mAuditWatchingStopRequested) {
                        Log.w(DynamicCodeLoggingService.TAG, "Stopping AuditWatchingJob run at scheduler request");
                        return false;
                    }
                    EventLog.Event event = events.get(i);
                    int uid = event.getUid();
                    if (Process.isApplicationUid(uid)) {
                        Object data = event.getData();
                        if (data instanceof String) {
                            String message = (String) data;
                            if (message.startsWith(DynamicCodeLoggingService.AVC_PREFIX)) {
                                Matcher matcher = DynamicCodeLoggingService.EXECUTE_NATIVE_AUDIT_PATTERN.matcher(message);
                                if (matcher.matches()) {
                                    String path = matcher.group(1);
                                    if (path == null) {
                                        path = DynamicCodeLoggingService.unhex(matcher.group(2));
                                    }
                                    dynamicCodeLogger.recordNative(uid, path);
                                }
                            }
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e(DynamicCodeLoggingService.TAG, "AuditWatchingJob failed", e);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static String unhex(String hexEncodedPath) {
        byte[] bytes = ByteStringUtils.fromHexToByteArray(hexEncodedPath);
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(bytes);
    }
}
