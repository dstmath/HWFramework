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
import com.android.server.audio.AudioService;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

final class DockObserver extends SystemService {
    private static final String DOCK_STATE_PATH = "/sys/class/switch/dock/state";
    private static final String DOCK_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/dock";
    private static final int MSG_DOCK_STATE_CHANGED = 0;
    private static final String TAG = "DockObserver";
    /* access modifiers changed from: private */
    public int mActualDockState = 0;
    private final boolean mAllowTheaterModeWakeFromDock;
    private final Handler mHandler = new Handler(true) {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                DockObserver.this.handleDockStateChange();
                DockObserver.this.mWakeLock.release();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final UEventObserver mObserver = new UEventObserver() {
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
    /* access modifiers changed from: private */
    public int mPreviousDockState = 0;
    /* access modifiers changed from: private */
    public int mReportedDockState = 0;
    private boolean mSystemReady;
    /* access modifiers changed from: private */
    public boolean mUpdatesStopped;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    private final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            String value;
            if (DumpUtils.checkDumpPermission(DockObserver.this.getContext(), DockObserver.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (DockObserver.this.mLock) {
                        if (args != null) {
                            try {
                                if (args.length != 0) {
                                    if (!"-a".equals(args[0])) {
                                        if (args.length == 3 && "set".equals(args[0])) {
                                            String key = args[1];
                                            value = args[2];
                                            if (AudioService.CONNECT_INTENT_KEY_STATE.equals(key)) {
                                                boolean unused = DockObserver.this.mUpdatesStopped = true;
                                                DockObserver.this.setDockStateLocked(Integer.parseInt(value));
                                            } else {
                                                pw.println("Unknown set option: " + key);
                                            }
                                        } else if (args.length != 1 || !"reset".equals(args[0])) {
                                            pw.println("Dump current dock state, or:");
                                            pw.println("  set state <value>");
                                            pw.println("  reset");
                                        } else {
                                            boolean unused2 = DockObserver.this.mUpdatesStopped = false;
                                            DockObserver.this.setDockStateLocked(DockObserver.this.mActualDockState);
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                pw.println("Bad value: " + value);
                            } catch (Throwable th) {
                                while (true) {
                                }
                                throw th;
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

    public DockObserver(Context context) {
        super(context);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
        this.mAllowTheaterModeWakeFromDock = context.getResources().getBoolean(17956878);
        init();
        this.mObserver.startObserving(DOCK_UEVENT_MATCH);
    }

    public void onStart() {
        publishBinderService(TAG, new BinderService());
    }

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
        FileReader file;
        synchronized (this.mLock) {
            try {
                char[] buffer = new char[1024];
                file = new FileReader(DOCK_STATE_PATH);
                setActualDockStateLocked(Integer.parseInt(new String(buffer, 0, file.read(buffer, 0, 1024)).trim()));
                this.mPreviousDockState = this.mActualDockState;
                file.close();
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "This kernel does not have dock station support");
            } catch (Exception e2) {
                Slog.e(TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, e2);
            } catch (Throwable th) {
                file.close();
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setActualDockStateLocked(int newState) {
        this.mActualDockState = newState;
        if (!this.mUpdatesStopped) {
            setDockStateLocked(newState);
        }
    }

    /* access modifiers changed from: private */
    public void setDockStateLocked(int newState) {
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
    public void handleDockStateChange() {
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
                if (whichSound != null) {
                    if (Settings.Global.getString(cr, whichSound) != null) {
                        Uri soundUri = Uri.parse("file://" + soundPath);
                        if (soundUri != null) {
                            Ringtone sfx = RingtoneManager.getRingtone(getContext(), soundUri);
                            if (sfx != null) {
                                sfx.setStreamType(1);
                                sfx.play();
                            }
                        }
                    }
                }
            }
            getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }
}
