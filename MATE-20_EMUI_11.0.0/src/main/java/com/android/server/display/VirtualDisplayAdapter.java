package com.android.server.display;

import android.content.Context;
import android.hardware.display.IVirtualDisplayCallback;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import java.io.PrintWriter;

@VisibleForTesting
public class VirtualDisplayAdapter extends DisplayAdapter {
    static final boolean DEBUG = false;
    static final String TAG = "VirtualDisplayAdapter";
    @VisibleForTesting
    static final String UNIQUE_ID_PREFIX = "virtual:";
    private final Handler mHandler;
    private final SurfaceControlDisplayFactory mSurfaceControlDisplayFactory;
    private final ArrayMap<IBinder, VirtualDisplayDevice> mVirtualDisplayDevices;

    @VisibleForTesting
    public interface SurfaceControlDisplayFactory {
        IBinder createDisplay(String str, boolean z);
    }

    @Override // com.android.server.display.DisplayAdapter
    public /* bridge */ /* synthetic */ void dumpLocked(PrintWriter printWriter) {
        super.dumpLocked(printWriter);
    }

    @Override // com.android.server.display.DisplayAdapter
    public /* bridge */ /* synthetic */ void registerLocked() {
        super.registerLocked();
    }

    public VirtualDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener) {
        this(syncRoot, context, handler, listener, $$Lambda$VirtualDisplayAdapter$PFyqeaYIEBicSVtuy5lL_bT8B0.INSTANCE);
    }

    @VisibleForTesting
    VirtualDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, SurfaceControlDisplayFactory surfaceControlDisplayFactory) {
        super(syncRoot, context, handler, listener, TAG);
        this.mVirtualDisplayDevices = new ArrayMap<>();
        this.mHandler = handler;
        this.mSurfaceControlDisplayFactory = surfaceControlDisplayFactory;
    }

    public DisplayDevice createVirtualDisplayLocked(IVirtualDisplayCallback callback, IMediaProjection projection, int ownerUid, String ownerPackageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
        String uniqueId2;
        boolean z;
        boolean secure = (flags & 4) != 0;
        IBinder appToken = callback.asBinder();
        IBinder displayToken = this.mSurfaceControlDisplayFactory.createDisplay(name, secure);
        String baseUniqueId = UNIQUE_ID_PREFIX + ownerPackageName + "," + ownerUid + "," + name + ",";
        int uniqueIndex = getNextUniqueIndex(baseUniqueId);
        if (uniqueId == null) {
            uniqueId2 = baseUniqueId + uniqueIndex;
        } else {
            uniqueId2 = UNIQUE_ID_PREFIX + ownerPackageName + ":" + uniqueId;
        }
        VirtualDisplayDevice device = new VirtualDisplayDevice(displayToken, appToken, ownerUid, ownerPackageName, name, width, height, densityDpi, surface, flags, new Callback(callback, this.mHandler), uniqueId2, uniqueIndex);
        this.mVirtualDisplayDevices.put(appToken, device);
        if (projection != null) {
            try {
                projection.registerCallback(new MediaProjectionCallback(appToken));
            } catch (RemoteException e) {
                z = false;
            }
        }
        z = false;
        try {
            appToken.linkToDeath(device, 0);
            return device;
        } catch (RemoteException e2) {
        }
        this.mVirtualDisplayDevices.remove(appToken);
        device.destroyLocked(z);
        return null;
    }

    public void resizeVirtualDisplayLocked(IBinder appToken, int width, int height, int densityDpi) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.get(appToken);
        if (device != null) {
            device.resizeLocked(width, height, densityDpi);
        }
    }

    public void setVirtualDisplaySurfaceLocked(IBinder appToken, Surface surface) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.get(appToken);
        if (device != null) {
            device.setSurfaceLocked(surface);
        }
    }

    public DisplayDevice releaseVirtualDisplayLocked(IBinder appToken) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.remove(appToken);
        if (device != null) {
            device.destroyLocked(true);
            appToken.unlinkToDeath(device, 0);
        }
        return device;
    }

    /* access modifiers changed from: package-private */
    public void setVirtualDisplayStateLocked(IBinder appToken, boolean isOn) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.get(appToken);
        if (device != null) {
            device.setDisplayState(isOn);
        }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBinderDiedLocked(IBinder appToken) {
        this.mVirtualDisplayDevices.remove(appToken);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMediaProjectionStoppedLocked(IBinder appToken) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.remove(appToken);
        if (device != null) {
            Slog.i(TAG, "Virtual display device released because media projection stopped: " + device.mName);
            device.stopLocked();
        }
    }

    /* access modifiers changed from: private */
    public final class VirtualDisplayDevice extends DisplayDevice implements IBinder.DeathRecipient {
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
        private boolean mIsDisplayOn;
        private Display.Mode mMode;
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
            boolean z = false;
            this.mPendingChanges |= 1;
            this.mUniqueIndex = uniqueIndex;
            this.mIsDisplayOn = surface != null ? true : z;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (VirtualDisplayAdapter.this.getSyncRoot()) {
                VirtualDisplayAdapter.this.handleBinderDiedLocked(this.mAppToken);
                Slog.i(VirtualDisplayAdapter.TAG, "Virtual display device released because application token died: " + this.mOwnerPackageName);
                destroyLocked(false);
                VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 3);
            }
        }

        public void destroyLocked(boolean binderAlive) {
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.release();
                this.mSurface = null;
            }
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
            if (binderAlive) {
                this.mCallback.dispatchDisplayStopped();
            }
        }

        @Override // com.android.server.display.DisplayDevice
        public boolean hasStableUniqueId() {
            return false;
        }

        @Override // com.android.server.display.DisplayDevice
        public Runnable requestDisplayStateLocked(int state, int brightness) {
            if (state == this.mDisplayState) {
                return null;
            }
            this.mDisplayState = state;
            if (state == 1) {
                this.mCallback.dispatchDisplayPaused();
                return null;
            }
            this.mCallback.dispatchDisplayResumed();
            return null;
        }

        @Override // com.android.server.display.DisplayDevice
        public void performTraversalLocked(SurfaceControl.Transaction t) {
            if ((this.mPendingChanges & 2) != 0) {
                t.setDisplaySize(getDisplayTokenLocked(), this.mWidth, this.mHeight);
            }
            if ((this.mPendingChanges & 1) != 0) {
                Slog.i(VirtualDisplayAdapter.TAG, "set surface to surfaceflinger surface: " + this.mSurface);
                try {
                    setSurfaceLocked(t, this.mSurface);
                } catch (IllegalArgumentException e) {
                    Slog.wtf(VirtualDisplayAdapter.TAG, "set surface to surfaceflinger fail", e);
                }
                Slog.i(VirtualDisplayAdapter.TAG, "set surface to surfaceflinger over");
            }
            this.mPendingChanges = 0;
        }

        public void setSurfaceLocked(Surface surface) {
            Surface surface2;
            if (!this.mStopped && (surface2 = this.mSurface) != surface) {
                boolean z = false;
                boolean z2 = surface2 != null;
                if (surface != null) {
                    z = true;
                }
                if (z2 != z) {
                    VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
                }
                VirtualDisplayAdapter.this.sendTraversalRequestLocked();
                Slog.i(VirtualDisplayAdapter.TAG, "set surface to display old surface: " + this.mSurface + ", new surface: " + surface);
                this.mSurface = surface;
                this.mInfo = null;
                this.mPendingChanges = this.mPendingChanges | 1;
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

        /* access modifiers changed from: package-private */
        public void setDisplayState(boolean isOn) {
            if (this.mIsDisplayOn != isOn) {
                this.mIsDisplayOn = isOn;
                this.mInfo = null;
                VirtualDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
            }
        }

        public void stopLocked() {
            setSurfaceLocked(null);
            this.mStopped = true;
        }

        @Override // com.android.server.display.DisplayDevice
        public void dumpLocked(PrintWriter pw) {
            super.dumpLocked(pw);
            pw.println("mFlags=" + this.mFlags);
            pw.println("mDisplayState=" + Display.stateToString(this.mDisplayState));
            pw.println("mStopped=" + this.mStopped);
        }

        @Override // com.android.server.display.DisplayDevice
        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                this.mInfo = new DisplayDeviceInfo();
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                displayDeviceInfo.name = this.mName;
                displayDeviceInfo.uniqueId = getUniqueId();
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                displayDeviceInfo2.width = this.mWidth;
                displayDeviceInfo2.height = this.mHeight;
                displayDeviceInfo2.modeId = this.mMode.getModeId();
                this.mInfo.defaultModeId = this.mMode.getModeId();
                DisplayDeviceInfo displayDeviceInfo3 = this.mInfo;
                int i = 1;
                displayDeviceInfo3.supportedModes = new Display.Mode[]{this.mMode};
                int i2 = this.mDensityDpi;
                displayDeviceInfo3.densityDpi = i2;
                displayDeviceInfo3.xDpi = (float) i2;
                displayDeviceInfo3.yDpi = (float) i2;
                displayDeviceInfo3.presentationDeadlineNanos = 16666666;
                displayDeviceInfo3.flags = 0;
                if ((this.mFlags & 1) == 0) {
                    displayDeviceInfo3.flags |= 48;
                }
                if ((this.mFlags & 16) != 0) {
                    this.mInfo.flags &= -33;
                } else {
                    this.mInfo.flags |= 128;
                }
                if ((this.mFlags & 4) != 0) {
                    this.mInfo.flags |= 4;
                }
                int i3 = 3;
                if ((this.mFlags & 2) != 0) {
                    this.mInfo.flags |= 64;
                    if ((this.mFlags & 1) != 0 && "portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
                        this.mInfo.rotation = 3;
                    }
                }
                if ((this.mFlags & 32) != 0) {
                    this.mInfo.flags |= 512;
                }
                if ((this.mFlags & 128) != 0) {
                    this.mInfo.flags |= 2;
                }
                if ((this.mFlags & 256) != 0) {
                    this.mInfo.flags |= 1024;
                }
                if ((this.mFlags & 512) != 0) {
                    this.mInfo.flags |= 4096;
                }
                DisplayDeviceInfo displayDeviceInfo4 = this.mInfo;
                displayDeviceInfo4.type = 5;
                if ((this.mFlags & 64) == 0) {
                    i3 = 0;
                }
                displayDeviceInfo4.touch = i3;
                DisplayDeviceInfo displayDeviceInfo5 = this.mInfo;
                if (this.mIsDisplayOn) {
                    i = 2;
                }
                displayDeviceInfo5.state = i;
                DisplayDeviceInfo displayDeviceInfo6 = this.mInfo;
                displayDeviceInfo6.ownerUid = this.mOwnerUid;
                displayDeviceInfo6.ownerPackageName = this.mOwnerPackageName;
            }
            return this.mInfo;
        }
    }

    /* access modifiers changed from: private */
    public static class Callback extends Handler {
        private static final int MSG_ON_DISPLAY_PAUSED = 0;
        private static final int MSG_ON_DISPLAY_RESUMED = 1;
        private static final int MSG_ON_DISPLAY_STOPPED = 2;
        private final IVirtualDisplayCallback mCallback;

        public Callback(IVirtualDisplayCallback callback, Handler handler) {
            super(handler.getLooper());
            this.mCallback = callback;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == 0) {
                    this.mCallback.onPaused();
                } else if (i == 1) {
                    this.mCallback.onResumed();
                } else if (i == 2) {
                    this.mCallback.onStopped();
                }
            } catch (RemoteException e) {
                Slog.w(VirtualDisplayAdapter.TAG, "Failed to notify listener of virtual display event.", e);
            }
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

    /* access modifiers changed from: private */
    public final class MediaProjectionCallback extends IMediaProjectionCallback.Stub {
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
}
