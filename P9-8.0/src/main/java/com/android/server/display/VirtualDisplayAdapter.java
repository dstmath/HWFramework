package com.android.server.display;

import android.content.Context;
import android.hardware.display.IVirtualDisplayCallback;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.Display;
import android.view.Display.Mode;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.server.display.DisplayAdapter.Listener;
import com.android.server.display.DisplayManagerService.SyncRoot;
import java.io.PrintWriter;

public class VirtualDisplayAdapter extends DisplayAdapter {
    static final boolean DEBUG = false;
    static final String TAG = "VirtualDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "virtual:";
    private final Handler mHandler;
    private final SurfaceControlDisplayFactory mSurfaceControlDisplayFactory;
    private final ArrayMap<IBinder, VirtualDisplayDevice> mVirtualDisplayDevices;

    public interface SurfaceControlDisplayFactory {
        IBinder createDisplay(String str, boolean z);
    }

    private static class Callback extends Handler {
        private static final int MSG_ON_DISPLAY_PAUSED = 0;
        private static final int MSG_ON_DISPLAY_RESUMED = 1;
        private static final int MSG_ON_DISPLAY_STOPPED = 2;
        private final IVirtualDisplayCallback mCallback;

        public Callback(IVirtualDisplayCallback callback, Handler handler) {
            super(handler.getLooper());
            this.mCallback = callback;
        }

        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                        this.mCallback.onPaused();
                        return;
                    case 1:
                        this.mCallback.onResumed();
                        return;
                    case 2:
                        this.mCallback.onStopped();
                        return;
                    default:
                        return;
                }
            } catch (RemoteException e) {
                Slog.w(VirtualDisplayAdapter.TAG, "Failed to notify listener of virtual display event.", e);
            }
            Slog.w(VirtualDisplayAdapter.TAG, "Failed to notify listener of virtual display event.", e);
        }

        public void dispatchDisplayPaused() {
            sendEmptyMessage(0);
        }

        public void dispatchDisplayResumed() {
            sendEmptyMessage(1);
        }

        public void dispatchDisplayStopped() {
            sendEmptyMessage(2);
        }
    }

    private final class MediaProjectionCallback extends Stub {
        private IBinder mAppToken;

        public MediaProjectionCallback(IBinder appToken) {
            this.mAppToken = appToken;
        }

        public void onStop() {
            synchronized (VirtualDisplayAdapter.this.getSyncRoot()) {
                VirtualDisplayAdapter.this.handleMediaProjectionStoppedLocked(this.mAppToken);
            }
        }
    }

    private final class VirtualDisplayDevice extends DisplayDevice implements DeathRecipient {
        private static final int PENDING_RESIZE = 2;
        private static final int PENDING_SURFACE_CHANGE = 1;
        private static final float REFRESH_RATE = 60.0f;
        private final IBinder mAppToken;
        private final Callback mCallback;
        private int mDensityDpi;
        private int mDisplayState = 0;
        private final int mFlags;
        private int mHeight;
        private DisplayDeviceInfo mInfo;
        private Mode mMode;
        final String mName;
        final String mOwnerPackageName;
        private final int mOwnerUid;
        private int mPendingChanges;
        private boolean mStopped;
        private Surface mSurface;
        private int mUniqueIndex;
        private int mWidth;

        public VirtualDisplayDevice(IBinder displayToken, IBinder appToken, int ownerUid, String ownerPackageName, String name, int width, int height, int densityDpi, Surface surface, int flags, Callback callback, String uniqueId, int uniqueIndex) {
            super(VirtualDisplayAdapter.this, displayToken, uniqueId);
            this.mAppToken = appToken;
            this.mOwnerUid = ownerUid;
            this.mOwnerPackageName = ownerPackageName;
            this.mName = name;
            this.mWidth = width;
            this.mHeight = height;
            this.mMode = DisplayAdapter.createMode(width, height, REFRESH_RATE);
            this.mDensityDpi = densityDpi;
            this.mSurface = surface;
            this.mFlags = flags;
            this.mCallback = callback;
            this.mPendingChanges |= 1;
            this.mUniqueIndex = uniqueIndex;
        }

        public void binderDied() {
            synchronized (VirtualDisplayAdapter.this.getSyncRoot()) {
                VirtualDisplayAdapter.this.handleBinderDiedLocked(this.mAppToken);
                Slog.i(VirtualDisplayAdapter.TAG, "Virtual display device released because application token died: " + this.mOwnerPackageName);
                destroyLocked(false);
                VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 3);
            }
        }

        public void destroyLocked(boolean binderAlive) {
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
            if (binderAlive) {
                this.mCallback.dispatchDisplayStopped();
            }
        }

        public boolean hasStableUniqueId() {
            return false;
        }

        public Runnable requestDisplayStateLocked(int state, int brightness) {
            if (state != this.mDisplayState) {
                this.mDisplayState = state;
                if (state == 1) {
                    this.mCallback.dispatchDisplayPaused();
                } else {
                    this.mCallback.dispatchDisplayResumed();
                }
            }
            return null;
        }

        public void performTraversalInTransactionLocked() {
            if ((this.mPendingChanges & 2) != 0) {
                SurfaceControl.setDisplaySize(getDisplayTokenLocked(), this.mWidth, this.mHeight);
            }
            if ((this.mPendingChanges & 1) != 0) {
                setSurfaceInTransactionLocked(this.mSurface);
            }
            this.mPendingChanges = 0;
        }

        public void setSurfaceLocked(Surface surface) {
            Object obj = 1;
            if (!this.mStopped && this.mSurface != surface) {
                Object obj2 = this.mSurface != null ? 1 : null;
                if (surface == null) {
                    obj = null;
                }
                if (obj2 != obj) {
                    VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
                }
                VirtualDisplayAdapter.this.sendTraversalRequestLocked();
                this.mSurface = surface;
                this.mInfo = null;
                this.mPendingChanges |= 1;
            }
        }

        public void resizeLocked(int width, int height, int densityDpi) {
            if (this.mWidth != width || this.mHeight != height || this.mDensityDpi != densityDpi) {
                VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
                VirtualDisplayAdapter.this.sendTraversalRequestLocked();
                this.mWidth = width;
                this.mHeight = height;
                this.mMode = DisplayAdapter.createMode(width, height, REFRESH_RATE);
                this.mDensityDpi = densityDpi;
                this.mInfo = null;
                this.mPendingChanges |= 2;
            }
        }

        public void stopLocked() {
            setSurfaceLocked(null);
            this.mStopped = true;
        }

        public void dumpLocked(PrintWriter pw) {
            super.dumpLocked(pw);
            pw.println("mFlags=" + this.mFlags);
            pw.println("mDisplayState=" + Display.stateToString(this.mDisplayState));
            pw.println("mStopped=" + this.mStopped);
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            int i = 0;
            if (this.mInfo == null) {
                DisplayDeviceInfo displayDeviceInfo;
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.name = this.mName;
                this.mInfo.uniqueId = getUniqueId();
                this.mInfo.width = this.mWidth;
                this.mInfo.height = this.mHeight;
                this.mInfo.modeId = this.mMode.getModeId();
                this.mInfo.defaultModeId = this.mMode.getModeId();
                Mode[] modeArr = new Mode[]{this.mMode};
                this.mInfo.supportedModes = modeArr;
                this.mInfo.densityDpi = this.mDensityDpi;
                this.mInfo.xDpi = (float) this.mDensityDpi;
                this.mInfo.yDpi = (float) this.mDensityDpi;
                this.mInfo.presentationDeadlineNanos = 16666666;
                this.mInfo.flags = 0;
                if ((this.mFlags & 1) == 0) {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 48;
                }
                if ((this.mFlags & 16) != 0) {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags &= -33;
                } else {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 128;
                }
                if ((this.mFlags & 4) != 0) {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 4;
                }
                if ((this.mFlags & 2) != 0) {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 64;
                    if ((this.mFlags & 1) != 0 && "portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
                        this.mInfo.rotation = 3;
                    }
                }
                if ((this.mFlags & 32) != 0) {
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 512;
                }
                this.mInfo.type = 5;
                displayDeviceInfo = this.mInfo;
                if ((this.mFlags & 64) != 0) {
                    i = 3;
                }
                displayDeviceInfo.touch = i;
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                if (this.mSurface != null) {
                    i = 2;
                } else {
                    i = 1;
                }
                displayDeviceInfo2.state = i;
                this.mInfo.ownerUid = this.mOwnerUid;
                this.mInfo.ownerPackageName = this.mOwnerPackageName;
            }
            return this.mInfo;
        }
    }

    public VirtualDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener) {
        this(syncRoot, context, handler, listener, new -$Lambda$pe87L53A2dvYIZSUUR6Usyk2Zwo());
    }

    VirtualDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener, SurfaceControlDisplayFactory surfaceControlDisplayFactory) {
        super(syncRoot, context, handler, listener, TAG);
        this.mVirtualDisplayDevices = new ArrayMap();
        this.mHandler = handler;
        this.mSurfaceControlDisplayFactory = surfaceControlDisplayFactory;
    }

    public DisplayDevice createVirtualDisplayLocked(IVirtualDisplayCallback callback, IMediaProjection projection, int ownerUid, String ownerPackageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
        boolean secure = (flags & 4) != 0;
        IBinder appToken = callback.asBinder();
        IBinder displayToken = this.mSurfaceControlDisplayFactory.createDisplay(name, secure);
        String baseUniqueId = UNIQUE_ID_PREFIX + ownerPackageName + "," + ownerUid + "," + name + ",";
        int uniqueIndex = getNextUniqueIndex(baseUniqueId);
        if (uniqueId == null) {
            uniqueId = baseUniqueId + uniqueIndex;
        } else {
            uniqueId = UNIQUE_ID_PREFIX + ownerPackageName + ":" + uniqueId;
        }
        VirtualDisplayDevice device = new VirtualDisplayDevice(displayToken, appToken, ownerUid, ownerPackageName, name, width, height, densityDpi, surface, flags, new Callback(callback, this.mHandler), uniqueId, uniqueIndex);
        this.mVirtualDisplayDevices.put(appToken, device);
        if (projection != null) {
            try {
                projection.registerCallback(new MediaProjectionCallback(appToken));
            } catch (RemoteException e) {
                this.mVirtualDisplayDevices.remove(appToken);
                device.destroyLocked(false);
                return null;
            }
        }
        appToken.linkToDeath(device, 0);
        return device;
    }

    public void resizeVirtualDisplayLocked(IBinder appToken, int width, int height, int densityDpi) {
        VirtualDisplayDevice device = (VirtualDisplayDevice) this.mVirtualDisplayDevices.get(appToken);
        if (device != null) {
            device.resizeLocked(width, height, densityDpi);
        }
    }

    public void setVirtualDisplaySurfaceLocked(IBinder appToken, Surface surface) {
        VirtualDisplayDevice device = (VirtualDisplayDevice) this.mVirtualDisplayDevices.get(appToken);
        if (device != null) {
            device.setSurfaceLocked(surface);
        }
    }

    public DisplayDevice releaseVirtualDisplayLocked(IBinder appToken) {
        VirtualDisplayDevice device = (VirtualDisplayDevice) this.mVirtualDisplayDevices.remove(appToken);
        if (device != null) {
            device.destroyLocked(true);
            appToken.unlinkToDeath(device, 0);
        }
        return device;
    }

    private int getNextUniqueIndex(String uniqueIdPrefix) {
        if (this.mVirtualDisplayDevices.isEmpty()) {
            return 0;
        }
        int nextUniqueIndex = 0;
        for (VirtualDisplayDevice device : this.mVirtualDisplayDevices.values()) {
            if (device.getUniqueId().startsWith(uniqueIdPrefix) && device.mUniqueIndex >= nextUniqueIndex) {
                nextUniqueIndex = device.mUniqueIndex + 1;
            }
        }
        return nextUniqueIndex;
    }

    private void handleBinderDiedLocked(IBinder appToken) {
        this.mVirtualDisplayDevices.remove(appToken);
    }

    private void handleMediaProjectionStoppedLocked(IBinder appToken) {
        VirtualDisplayDevice device = (VirtualDisplayDevice) this.mVirtualDisplayDevices.remove(appToken);
        if (device != null) {
            Slog.i(TAG, "Virtual display device released because media projection stopped: " + device.mName);
            device.stopLocked();
        }
    }
}
