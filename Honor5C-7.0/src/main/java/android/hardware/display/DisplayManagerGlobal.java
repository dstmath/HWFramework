package android.hardware.display;

import android.content.Context;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.IDisplayManagerCallback.Stub;
import android.hardware.display.VirtualDisplay.Callback;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.DisplayInfo;
import android.view.Surface;
import java.util.ArrayList;

public final class DisplayManagerGlobal {
    private static final boolean DEBUG = false;
    public static final int EVENT_DISPLAY_ADDED = 1;
    public static final int EVENT_DISPLAY_CHANGED = 2;
    public static final int EVENT_DISPLAY_REMOVED = 3;
    private static final boolean HWFLOW = false;
    private static final String TAG = "DisplayManager";
    private static final boolean USE_CACHE = false;
    private static DisplayManagerGlobal sInstance;
    private DisplayManagerCallback mCallback;
    private int[] mDisplayIdCache;
    private final SparseArray<DisplayInfo> mDisplayInfoCache;
    private final ArrayList<DisplayListenerDelegate> mDisplayListeners;
    private final IDisplayManager mDm;
    private final Object mLock;
    private int mWifiDisplayScanNestCount;

    private static final class DisplayListenerDelegate extends Handler {
        public final DisplayListener mListener;

        public DisplayListenerDelegate(DisplayListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper(), null, true);
            this.mListener = listener;
        }

        public void sendDisplayEvent(int displayId, int event) {
            sendMessage(obtainMessage(event, displayId, 0));
        }

        public void clearEvents() {
            removeCallbacksAndMessages(null);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DisplayManagerGlobal.EVENT_DISPLAY_ADDED /*1*/:
                    this.mListener.onDisplayAdded(msg.arg1);
                case DisplayManagerGlobal.EVENT_DISPLAY_CHANGED /*2*/:
                    this.mListener.onDisplayChanged(msg.arg1);
                case DisplayManagerGlobal.EVENT_DISPLAY_REMOVED /*3*/:
                    this.mListener.onDisplayRemoved(msg.arg1);
                default:
            }
        }
    }

    private final class DisplayManagerCallback extends Stub {
        private DisplayManagerCallback() {
        }

        public void onDisplayEvent(int displayId, int event) {
            DisplayManagerGlobal.this.handleDisplayEvent(displayId, event);
        }
    }

    private static final class VirtualDisplayCallback extends IVirtualDisplayCallback.Stub {
        private VirtualDisplayCallbackDelegate mDelegate;

        public VirtualDisplayCallback(Callback callback, Handler handler) {
            if (callback != null) {
                this.mDelegate = new VirtualDisplayCallbackDelegate(callback, handler);
            }
        }

        public void onPaused() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(0);
            }
        }

        public void onResumed() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(DisplayManagerGlobal.EVENT_DISPLAY_ADDED);
            }
        }

        public void onStopped() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(DisplayManagerGlobal.EVENT_DISPLAY_CHANGED);
            }
        }
    }

    private static final class VirtualDisplayCallbackDelegate extends Handler {
        public static final int MSG_DISPLAY_PAUSED = 0;
        public static final int MSG_DISPLAY_RESUMED = 1;
        public static final int MSG_DISPLAY_STOPPED = 2;
        private final Callback mCallback;

        public VirtualDisplayCallbackDelegate(Callback callback, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper(), null, true);
            this.mCallback = callback;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPLAY_PAUSED /*0*/:
                    this.mCallback.onPaused();
                case MSG_DISPLAY_RESUMED /*1*/:
                    this.mCallback.onResumed();
                case MSG_DISPLAY_STOPPED /*2*/:
                    this.mCallback.onStopped();
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.display.DisplayManagerGlobal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.display.DisplayManagerGlobal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.display.DisplayManagerGlobal.<clinit>():void");
    }

    private DisplayManagerGlobal(IDisplayManager dm) {
        this.mLock = new Object();
        this.mDisplayListeners = new ArrayList();
        this.mDisplayInfoCache = new SparseArray();
        this.mDm = dm;
    }

    public static DisplayManagerGlobal getInstance() {
        DisplayManagerGlobal displayManagerGlobal;
        synchronized (DisplayManagerGlobal.class) {
            if (sInstance == null) {
                IBinder b = ServiceManager.getService(Context.DISPLAY_SERVICE);
                if (b != null) {
                    sInstance = new DisplayManagerGlobal(IDisplayManager.Stub.asInterface(b));
                }
            }
            displayManagerGlobal = sInstance;
        }
        return displayManagerGlobal;
    }

    public DisplayInfo getDisplayInfo(int displayId) {
        try {
            synchronized (this.mLock) {
                DisplayInfo info = this.mDm.getDisplayInfo(displayId);
                if (info == null) {
                    return null;
                }
                registerCallbackIfNeededLocked();
                return info;
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public int[] getDisplayIds() {
        try {
            int[] displayIds;
            synchronized (this.mLock) {
                displayIds = this.mDm.getDisplayIds();
                registerCallbackIfNeededLocked();
            }
            return displayIds;
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public Display getCompatibleDisplay(int displayId, DisplayAdjustments daj) {
        DisplayInfo displayInfo = getDisplayInfo(displayId);
        if (displayInfo == null) {
            return null;
        }
        return new Display(this, displayId, displayInfo, daj);
    }

    public Display getRealDisplay(int displayId) {
        return getCompatibleDisplay(displayId, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    public void registerDisplayListener(DisplayListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mLock) {
            if (findDisplayListenerLocked(listener) < 0) {
                this.mDisplayListeners.add(new DisplayListenerDelegate(listener, handler));
                registerCallbackIfNeededLocked();
            }
        }
    }

    public void unregisterDisplayListener(DisplayListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mLock) {
            int index = findDisplayListenerLocked(listener);
            if (index >= 0) {
                ((DisplayListenerDelegate) this.mDisplayListeners.get(index)).clearEvents();
                this.mDisplayListeners.remove(index);
            }
        }
    }

    private int findDisplayListenerLocked(DisplayListener listener) {
        int numListeners = this.mDisplayListeners.size();
        for (int i = 0; i < numListeners; i += EVENT_DISPLAY_ADDED) {
            if (((DisplayListenerDelegate) this.mDisplayListeners.get(i)).mListener == listener) {
                return i;
            }
        }
        return -1;
    }

    private void registerCallbackIfNeededLocked() {
        if (this.mCallback == null) {
            this.mCallback = new DisplayManagerCallback();
            try {
                this.mDm.registerCallback(this.mCallback);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private void handleDisplayEvent(int displayId, int event) {
        synchronized (this.mLock) {
            int numListeners = this.mDisplayListeners.size();
            for (int i = 0; i < numListeners; i += EVENT_DISPLAY_ADDED) {
                ((DisplayListenerDelegate) this.mDisplayListeners.get(i)).sendDisplayEvent(displayId, event);
            }
        }
    }

    public void startWifiDisplayScan() {
        synchronized (this.mLock) {
            if (HWFLOW) {
                Log.d(TAG, "startWifiDisplayScan mWifiDisplayScanNestCount=" + this.mWifiDisplayScanNestCount);
            }
            int i = this.mWifiDisplayScanNestCount;
            this.mWifiDisplayScanNestCount = i + EVENT_DISPLAY_ADDED;
            if (i == 0) {
                registerCallbackIfNeededLocked();
                try {
                    this.mDm.startWifiDisplayScan();
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            }
        }
    }

    public void stopWifiDisplayScan() {
        synchronized (this.mLock) {
            int i = this.mWifiDisplayScanNestCount - 1;
            this.mWifiDisplayScanNestCount = i;
            if (i == 0) {
                try {
                    this.mDm.stopWifiDisplayScan();
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            } else if (this.mWifiDisplayScanNestCount < 0) {
                Log.wtf(TAG, "Wifi display scan nest count became negative: " + this.mWifiDisplayScanNestCount);
                this.mWifiDisplayScanNestCount = 0;
            }
            if (HWFLOW) {
                Log.d(TAG, "stopWifiDisplayScan mWifiDisplayScanNestCount=" + this.mWifiDisplayScanNestCount);
            }
        }
    }

    public void connectWifiDisplay(String deviceAddress) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
        try {
            this.mDm.connectWifiDisplay(deviceAddress);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void pauseWifiDisplay() {
        try {
            this.mDm.pauseWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void resumeWifiDisplay() {
        try {
            this.mDm.resumeWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void disconnectWifiDisplay() {
        try {
            this.mDm.disconnectWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void renameWifiDisplay(String deviceAddress, String alias) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
        try {
            this.mDm.renameWifiDisplay(deviceAddress, alias);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void forgetWifiDisplay(String deviceAddress) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
        try {
            this.mDm.forgetWifiDisplay(deviceAddress);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public WifiDisplayStatus getWifiDisplayStatus() {
        try {
            return this.mDm.getWifiDisplayStatus();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void requestColorTransform(int displayId, int colorTransformId) {
        try {
            this.mDm.requestColorTransform(displayId, colorTransformId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public VirtualDisplay createVirtualDisplay(Context context, MediaProjection projection, String name, int width, int height, int densityDpi, Surface surface, int flags, Callback callback, Handler handler) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must be non-null and non-empty");
        } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
            throw new IllegalArgumentException("width, height, and densityDpi must be greater than 0");
        } else {
            VirtualDisplayCallback callbackWrapper = new VirtualDisplayCallback(callback, handler);
            try {
                int displayId = this.mDm.createVirtualDisplay(callbackWrapper, projection != null ? projection.getProjection() : null, context.getPackageName(), name, width, height, densityDpi, surface, flags);
                if (displayId < 0) {
                    Log.e(TAG, "Could not create virtual display: " + name);
                    return null;
                }
                Display display = getRealDisplay(displayId);
                if (display != null) {
                    return new VirtualDisplay(this, display, callbackWrapper, surface);
                }
                Log.wtf(TAG, "Could not obtain display info for newly created virtual display: " + name);
                try {
                    this.mDm.releaseVirtualDisplay(callbackWrapper);
                    return null;
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            } catch (RemoteException ex2) {
                throw ex2.rethrowFromSystemServer();
            }
        }
    }

    public void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface) {
        try {
            this.mDm.setVirtualDisplaySurface(token, surface);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void resizeVirtualDisplay(IVirtualDisplayCallback token, int width, int height, int densityDpi) {
        try {
            this.mDm.resizeVirtualDisplay(token, width, height, densityDpi);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void releaseVirtualDisplay(IVirtualDisplayCallback token) {
        try {
            this.mDm.releaseVirtualDisplay(token);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public int getLowPowerDisplayLevel() {
        try {
            return this.mDm.getLowPowerDisplayLevel();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setLowPowerDisplayLevel(int level) {
        try {
            this.mDm.setLowPowerDisplayLevel(level);
        } catch (RemoteException e) {
        }
    }
}
