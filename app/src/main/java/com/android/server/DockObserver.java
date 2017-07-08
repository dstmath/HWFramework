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
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
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
    private int mActualDockState;
    private final boolean mAllowTheaterModeWakeFromDock;
    private final Handler mHandler;
    private final Object mLock;
    private final UEventObserver mObserver;
    private final PowerManager mPowerManager;
    private int mPreviousDockState;
    private int mReportedDockState;
    private boolean mSystemReady;
    private boolean mUpdatesStopped;
    private final WakeLock mWakeLock;

    /* renamed from: com.android.server.DockObserver.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(boolean $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DockObserver.MSG_DOCK_STATE_CHANGED /*0*/:
                    DockObserver.this.handleDockStateChange();
                    DockObserver.this.mWakeLock.release();
                default:
            }
        }
    }

    private final class BinderService extends Binder {
        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DockObserver.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump dock observer service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (DockObserver.this.mLock) {
                    if (args != null) {
                        if (args.length != 0) {
                            if (!"-a".equals(args[DockObserver.MSG_DOCK_STATE_CHANGED])) {
                                if (args.length == 3 && "set".equals(args[DockObserver.MSG_DOCK_STATE_CHANGED])) {
                                    String key = args[1];
                                    String value = args[2];
                                    try {
                                        if (AudioService.CONNECT_INTENT_KEY_STATE.equals(key)) {
                                            DockObserver.this.mUpdatesStopped = true;
                                            DockObserver.this.setDockStateLocked(Integer.parseInt(value));
                                        } else {
                                            pw.println("Unknown set option: " + key);
                                        }
                                    } catch (NumberFormatException e) {
                                        pw.println("Bad value: " + value);
                                    }
                                } else if (args.length == 1 && "reset".equals(args[DockObserver.MSG_DOCK_STATE_CHANGED])) {
                                    DockObserver.this.mUpdatesStopped = false;
                                    DockObserver.this.setDockStateLocked(DockObserver.this.mActualDockState);
                                } else {
                                    pw.println("Dump current dock state, or:");
                                    pw.println("  set state <value>");
                                    pw.println("  reset");
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

    public DockObserver(Context context) {
        super(context);
        this.mLock = new Object();
        this.mActualDockState = MSG_DOCK_STATE_CHANGED;
        this.mReportedDockState = MSG_DOCK_STATE_CHANGED;
        this.mPreviousDockState = MSG_DOCK_STATE_CHANGED;
        this.mHandler = new AnonymousClass1(true);
        this.mObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
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
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
        this.mAllowTheaterModeWakeFromDock = context.getResources().getBoolean(17956915);
        init();
        this.mObserver.startObserving(DOCK_UEVENT_MATCH);
    }

    public void onStart() {
        publishBinderService(TAG, new BinderService());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
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
                char[] buffer = new char[DumpState.DUMP_PROVIDERS];
                file = new FileReader(DOCK_STATE_PATH);
                setActualDockStateLocked(Integer.parseInt(new String(buffer, MSG_DOCK_STATE_CHANGED, file.read(buffer, MSG_DOCK_STATE_CHANGED, DumpState.DUMP_PROVIDERS)).trim()));
                this.mPreviousDockState = this.mActualDockState;
                file.close();
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "This kernel does not have dock station support");
            } catch (Exception e2) {
                Slog.e(TAG, "", e2);
            } catch (Throwable th) {
                file.close();
            }
        }
    }

    private void setActualDockStateLocked(int newState) {
        this.mActualDockState = newState;
        if (!this.mUpdatesStopped) {
            setDockStateLocked(newState);
        }
    }

    private void setDockStateLocked(int newState) {
        if (newState != this.mReportedDockState) {
            this.mReportedDockState = newState;
            if (this.mSystemReady) {
                if (this.mAllowTheaterModeWakeFromDock || Global.getInt(getContext().getContentResolver(), "theater_mode_on", MSG_DOCK_STATE_CHANGED) == 0) {
                    this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server:DOCK");
                }
                updateLocked();
            }
        }
    }

    private void updateLocked() {
        this.mWakeLock.acquire();
        this.mHandler.sendEmptyMessage(MSG_DOCK_STATE_CHANGED);
    }

    private void handleDockStateChange() {
        synchronized (this.mLock) {
            Slog.i(TAG, "Dock state changed from " + this.mPreviousDockState + " to " + this.mReportedDockState);
            int previousDockState = this.mPreviousDockState;
            this.mPreviousDockState = this.mReportedDockState;
            ContentResolver cr = getContext().getContentResolver();
            if (Global.getInt(cr, "device_provisioned", MSG_DOCK_STATE_CHANGED) == 0) {
                Slog.i(TAG, "Device not provisioned, skipping dock broadcast");
                return;
            }
            Intent intent = new Intent("android.intent.action.DOCK_EVENT");
            intent.addFlags(536870912);
            intent.putExtra("android.intent.extra.DOCK_STATE", this.mReportedDockState);
            if (Global.getInt(cr, "dock_sounds_enabled", 1) == 1) {
                String whichSound = null;
                if (this.mReportedDockState == 0) {
                    if (previousDockState == 1 || previousDockState == 3 || previousDockState == 4) {
                        whichSound = "desk_undock_sound";
                    } else if (previousDockState == 2) {
                        whichSound = "car_undock_sound";
                    }
                } else if (this.mReportedDockState == 1 || this.mReportedDockState == 3 || this.mReportedDockState == 4) {
                    whichSound = "desk_dock_sound";
                } else if (this.mReportedDockState == 2) {
                    whichSound = "car_dock_sound";
                }
                if (whichSound != null) {
                    String soundPath = Global.getString(cr, whichSound);
                    if (soundPath != null) {
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
