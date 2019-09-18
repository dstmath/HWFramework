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
    private static final String UNIQUE_ID_PREFIX = "virtual:";
    private final Handler mHandler;
    private final SurfaceControlDisplayFactory mSurfaceControlDisplayFactory;
    private final ArrayMap<IBinder, VirtualDisplayDevice> mVirtualDisplayDevices;

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

    private final class MediaProjectionCallback extends IMediaProjectionCallback.Stub {
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

    @VisibleForTesting
    public interface SurfaceControlDisplayFactory {
        IBinder createDisplay(String str, boolean z);
    }

    private final class VirtualDisplayDevice extends DisplayDevice implements IBinder.DeathRecipient {
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
        private Display.Mode mMode;
        final String mName;
        final String mOwnerPackageName;
        private final int mOwnerUid;
        private int mPendingChanges;
        private boolean mStopped;
        private Surface mSurface;
        /* access modifiers changed from: private */
        public int mUniqueIndex;
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

        public void performTraversalLocked(SurfaceControl.Transaction t) {
            if ((this.mPendingChanges & 2) != 0) {
                t.setDisplaySize(getDisplayTokenLocked(), this.mWidth, this.mHeight);
            }
            if ((this.mPendingChanges & 1) != 0) {
                setSurfaceLocked(t, this.mSurface);
            }
            this.mPendingChanges = 0;
        }

        public void setSurfaceLocked(Surface surface) {
            if (!this.mStopped && this.mSurface != surface) {
                boolean z = false;
                boolean z2 = this.mSurface != null;
                if (surface != null) {
                    z = true;
                }
                if (z2 != z) {
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
            if (this.mInfo == null) {
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.name = this.mName;
                this.mInfo.uniqueId = getUniqueId();
                this.mInfo.width = this.mWidth;
                this.mInfo.height = this.mHeight;
                this.mInfo.modeId = this.mMode.getModeId();
                this.mInfo.defaultModeId = this.mMode.getModeId();
                int i = 1;
                this.mInfo.supportedModes = new Display.Mode[]{this.mMode};
                this.mInfo.densityDpi = this.mDensityDpi;
                this.mInfo.xDpi = (float) this.mDensityDpi;
                this.mInfo.yDpi = (float) this.mDensityDpi;
                this.mInfo.presentationDeadlineNanos = 16666666;
                this.mInfo.flags = 0;
                if ((this.mFlags & 1) == 0) {
                    this.mInfo.flags |= 48;
                }
                if ((this.mFlags & 16) != 0) {
                    this.mInfo.flags &= -33;
                } else {
                    this.mInfo.flags |= 128;
                }
                if ((this.mFlags & 4) != 0) {
                    this.mInfo.flags |= 4;
                }
                int i2 = 3;
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
                this.mInfo.type = 5;
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                if ((this.mFlags & 64) == 0) {
                    i2 = 0;
                }
                displayDeviceInfo.touch = i2;
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                if (this.mSurface != null) {
                    i = 2;
                }
                displayDeviceInfo2.state = i;
                this.mInfo.ownerUid = this.mOwnerUid;
                this.mInfo.ownerPackageName = this.mOwnerPackageName;
            }
            return this.mInfo;
        }
    }

    public /* bridge */ /* synthetic */ void dumpLocked(PrintWriter printWriter) {
        super.dumpLocked(printWriter);
    }

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
        IMediaProjection iMediaProjection = projection;
        String str = ownerPackageName;
        String baseUniqueId = name;
        String str2 = uniqueId;
        boolean secure = (flags & 4) != 0;
        IBinder appToken = callback.asBinder();
        IBinder displayToken = this.mSurfaceControlDisplayFactory.createDisplay(baseUniqueId, secure);
        StringBuilder sb = new StringBuilder();
        sb.append(UNIQUE_ID_PREFIX);
        sb.append(str);
        sb.append(",");
        int i = ownerUid;
        sb.append(i);
        sb.append(",");
        sb.append(baseUniqueId);
        sb.append(",");
        String baseUniqueId2 = sb.toString();
        int uniqueIndex = getNextUniqueIndex(baseUniqueId2);
        if (str2 == null) {
            uniqueId2 = baseUniqueId2 + uniqueIndex;
        } else {
            uniqueId2 = UNIQUE_ID_PREFIX + str + ":" + str2;
        }
        String str3 = baseUniqueId2;
        boolean z2 = secure;
        VirtualDisplayDevice device = new VirtualDisplayDevice(displayToken, appToken, i, str, baseUniqueId, width, height, densityDpi, surface, flags, new Callback(callback, this.mHandler), uniqueId2, uniqueIndex);
        IBinder appToken2 = appToken;
        this.mVirtualDisplayDevices.put(appToken2, device);
        IMediaProjection iMediaProjection2 = projection;
        if (iMediaProjection2 != null) {
            try {
                iMediaProjection2.registerCallback(new MediaProjectionCallback(appToken2));
            } catch (RemoteException e) {
                z = false;
            }
        }
        z = false;
        try {
            appToken2.linkToDeath(device, 0);
            return device;
        } catch (RemoteException e2) {
        }
        this.mVirtualDisplayDevices.remove(appToken2);
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
    public void handleBinderDiedLocked(IBinder appToken) {
        this.mVirtualDisplayDevices.remove(appToken);
    }

    /* access modifiers changed from: private */
    public void handleMediaProjectionStoppedLocked(IBinder appToken) {
        VirtualDisplayDevice device = this.mVirtualDisplayDevices.remove(appToken);
        if (device != null) {
            Slog.i(TAG, "Virtual display device released because media projection stopped: " + device.mName);
            device.stopLocked();
        }
    }
}
