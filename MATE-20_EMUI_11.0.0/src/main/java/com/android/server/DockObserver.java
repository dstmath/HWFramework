package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class DockObserver extends SystemService {
    private static final String DOCK_STATE_PATH = "/sys/class/switch/dock/state";
    private static final String DOCK_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/dock";
    private static final int MSG_DOCK_STATE_CHANGED = 0;
    private static final String TAG = "DockObserver";
    private int mActualDockState = 0;
    private final boolean mAllowTheaterModeWakeFromDock;
    private final Handler mHandler = new Handler(true) {
        /* class com.android.server.DockObserver.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                DockObserver.this.handleDockStateChange();
                DockObserver.this.mWakeLock.release();
            }
        }
    };
    private final Object mLock = new Object();
    private final UEventObserver mObserver = new UEventObserver() {
        /* class com.android.server.DockObserver.AnonymousClass2 */

        public void onUEvent(UEventObserver.UEvent event) {
            if (Log.isLoggable(DockObserver.TAG, 2)) {
                Slog.v(DockObserver.TAG, "Dock UEVENT: " + event.toString());
            }
            try {
                synchronized (DockObserver.this.mLock) {
                    DockObserver.this.setActualDockStateLocked(Integer.parseInt(event.get("SWITCH_STATE")));
                }
            } catch (NumberFormatException e) {
                Slog.e(DockObserver.TAG, "Could not parse switch state from event " + event);
            }
        }
    };
    private final PowerManager mPowerManager;
    private int mPreviousDockState = 0;
    private int mReportedDockState = 0;
    private boolean mSystemReady;
    private boolean mUpdatesStopped;
    private final PowerManager.WakeLock mWakeLock;

    public DockObserver(Context context) {
        super(context);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
        this.mAllowTheaterModeWakeFromDock = context.getResources().getBoolean(17891349);
        init();
        this.mObserver.startObserving(DOCK_UEVENT_MATCH);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService(TAG, new BinderService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            synchronized (this.mLock) {
                this.mSystemReady = true;
                if (this.mReportedDockState != 0) {
                    updateLocked();
                }
            }
        }
    }

    private void init() {
        synchronized (this.mLock) {
            try {
                char[] buffer = new char[1024];
                FileReader file = new FileReader(DOCK_STATE_PATH);
                try {
                    setActualDockStateLocked(Integer.parseInt(new String(buffer, 0, file.read(buffer, 0, 1024)).trim()));
                    this.mPreviousDockState = this.mActualDockState;
                } finally {
                    file.close();
                }
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "This kernel does not have dock station support");
            } catch (Exception e2) {
                Slog.e(TAG, "", e2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActualDockStateLocked(int newState) {
        this.mActualDockState = newState;
        if (!this.mUpdatesStopped) {
            setDockStateLocked(newState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDockStateLocked(int newState) {
        if (newState != this.mReportedDockState) {
            this.mReportedDockState = newState;
            if (this.mSystemReady) {
                if (this.mAllowTheaterModeWakeFromDock || Settings.Global.getInt(getContext().getContentResolver(), "theater_mode_on", 0) == 0) {
                    this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server:DOCK");
                }
                updateLocked();
            }
        }
    }

    private void updateLocked() {
        this.mWakeLock.acquire();
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDockStateChange() {
        String soundPath;
        Ringtone sfx;
        synchronized (this.mLock) {
            Slog.i(TAG, "Dock state changed from " + this.mPreviousDockState + " to " + this.mReportedDockState);
            int previousDockState = this.mPreviousDockState;
            this.mPreviousDockState = this.mReportedDockState;
            ContentResolver cr = getContext().getContentResolver();
            boolean accessibilityEnabled = false;
            if (Settings.Global.getInt(cr, "device_provisioned", 0) == 0) {
                Slog.i(TAG, "Device not provisioned, skipping dock broadcast");
                return;
            }
            Intent intent = new Intent("android.intent.action.DOCK_EVENT");
            intent.addFlags(536870912);
            intent.putExtra("android.intent.extra.DOCK_STATE", this.mReportedDockState);
            boolean dockSoundsEnabled = Settings.Global.getInt(cr, "dock_sounds_enabled", 1) == 1;
            boolean dockSoundsEnabledWhenAccessibility = Settings.Global.getInt(cr, "dock_sounds_enabled_when_accessbility", 1) == 1;
            if (Settings.Secure.getInt(cr, "accessibility_enabled", 0) == 1) {
                accessibilityEnabled = true;
            }
            if (dockSoundsEnabled || (accessibilityEnabled && dockSoundsEnabledWhenAccessibility)) {
                String whichSound = null;
                if (this.mReportedDockState == 0) {
                    if (!(previousDockState == 1 || previousDockState == 3)) {
                        if (previousDockState != 4) {
                            if (previousDockState == 2) {
                                whichSound = "car_undock_sound";
                            }
                        }
                    }
                    whichSound = "desk_undock_sound";
                } else {
                    if (!(this.mReportedDockState == 1 || this.mReportedDockState == 3)) {
                        if (this.mReportedDockState != 4) {
                            if (this.mReportedDockState == 2) {
                                whichSound = "car_dock_sound";
                            }
                        }
                    }
                    whichSound = "desk_dock_sound";
                }
                if (!(whichSound == null || (soundPath = Settings.Global.getString(cr, whichSound)) == null)) {
                    Uri soundUri = Uri.parse("file://" + soundPath);
                    if (!(soundUri == null || (sfx = RingtoneManager.getRingtone(getContext(), soundUri)) == null)) {
                        sfx.setStreamType(1);
                        sfx.play();
                    }
                }
            }
            getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(DockObserver.this.getContext(), DockObserver.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (DockObserver.this.mLock) {
                        if (args != null) {
                            if (args.length != 0) {
                                if (!"-a".equals(args[0])) {
                                    if (args.length == 3 && "set".equals(args[0])) {
                                        String key = args[1];
                                        String value = args[2];
                                        try {
                                            if ("state".equals(key)) {
                                                DockObserver.this.mUpdatesStopped = true;
                                                DockObserver.this.setDockStateLocked(Integer.parseInt(value));
                                            } else {
                                                pw.println("Unknown set option: " + key);
                                            }
                                        } catch (NumberFormatException e) {
                                            pw.println("Bad value: " + value);
                                        }
                                    } else if (args.length != 1 || !"reset".equals(args[0])) {
                                        pw.println("Dump current dock state, or:");
                                        pw.println("  set state <value>");
                                        pw.println("  reset");
                                    } else {
                                        DockObserver.this.mUpdatesStopped = false;
                                        DockObserver.this.setDockStateLocked(DockObserver.this.mActualDockState);
                                    }
                                }
                            }
                        }
                        pw.println("Current Dock Observer Service state:");
                        if (DockObserver.this.mUpdatesStopped) {
                            pw.println("  (UPDATES STOPPED -- use 'reset' to restart)");
                        }
                        pw.println("  reported state: " + DockObserver.this.mReportedDockState);
                        pw.println("  previous state: " + DockObserver.this.mPreviousDockState);
                        pw.println("  actual state: " + DockObserver.this.mActualDockState);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }
}
