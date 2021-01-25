package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.internal.os.IResultReceiver;
import java.lang.annotation.RCUnownedRef;
import java.util.ArrayList;

public final class WindowManagerImpl implements WindowManager {
    private ArrayList<View> mBlockInPCViews;
    private final Context mContext;
    private IBinder mDefaultToken;
    @UnsupportedAppUsage
    private final WindowManagerGlobal mGlobal;
    @RCUnownedRef
    private final Window mParentWindow;

    public WindowManagerImpl(Context context) {
        this(context, null);
    }

    private WindowManagerImpl(Context context, Window parentWindow) {
        this.mGlobal = WindowManagerGlobal.getInstance();
        this.mBlockInPCViews = new ArrayList<>();
        this.mContext = context;
        this.mParentWindow = parentWindow;
    }

    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(this.mContext, parentWindow);
    }

    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        return new WindowManagerImpl(displayContext, this.mParentWindow);
    }

    public void setDefaultToken(IBinder token) {
        this.mDefaultToken = token;
    }

    @Override // android.view.ViewManager
    public void addView(View view, ViewGroup.LayoutParams params) {
        if (view == null || view.mContext == null || !"com.huawei.android.launcher".equals(view.mContext.getPackageName()) || !HwPCUtils.isValidExtDisplayId(view.mContext)) {
            applyDefaultToken(params);
            this.mGlobal.addView(view, params, this.mContext.getDisplay(), this.mParentWindow);
            return;
        }
        synchronized (this.mBlockInPCViews) {
            this.mBlockInPCViews.add(view);
        }
    }

    @Override // android.view.ViewManager
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        synchronized (this.mBlockInPCViews) {
            if (!this.mBlockInPCViews.contains(view)) {
                applyDefaultToken(params);
                this.mGlobal.updateViewLayout(view, params);
            }
        }
    }

    private void applyDefaultToken(ViewGroup.LayoutParams params) {
        if (this.mDefaultToken != null && this.mParentWindow == null) {
            if (params instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
                if (wparams.token == null) {
                    wparams.token = this.mDefaultToken;
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }
    }

    @Override // android.view.ViewManager
    public void removeView(View view) {
        synchronized (this.mBlockInPCViews) {
            if (this.mBlockInPCViews.contains(view)) {
                this.mBlockInPCViews.remove(view);
            } else {
                this.mGlobal.removeView(view, false);
            }
        }
    }

    @Override // android.view.WindowManager
    public void removeViewImmediate(View view) {
        synchronized (this.mBlockInPCViews) {
            if (this.mBlockInPCViews.contains(view)) {
                this.mBlockInPCViews.remove(view);
            } else {
                this.mGlobal.removeView(view, true);
            }
        }
    }

    @Override // android.view.WindowManager
    public void requestAppKeyboardShortcuts(final WindowManager.KeyboardShortcutsReceiver receiver, int deviceId) {
        try {
            WindowManagerGlobal.getWindowManagerService().requestAppKeyboardShortcuts(new IResultReceiver.Stub() {
                /* class android.view.WindowManagerImpl.AnonymousClass1 */

                @Override // com.android.internal.os.IResultReceiver
                public void send(int resultCode, Bundle resultData) throws RemoteException {
                    receiver.onKeyboardShortcutsReceived(resultData.getParcelableArrayList(WindowManager.PARCEL_KEY_SHORTCUTS_ARRAY));
                }
            }, deviceId);
        } catch (RemoteException e) {
        }
    }

    @Override // android.view.WindowManager
    public Display getDefaultDisplay() {
        return this.mContext.getDisplay();
    }

    @Override // android.view.WindowManager
    public Region getCurrentImeTouchRegion() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getCurrentImeTouchRegion();
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // android.view.WindowManager
    public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService().setShouldShowWithInsecureKeyguard(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }

    @Override // android.view.WindowManager
    public void setShouldShowSystemDecors(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService().setShouldShowSystemDecors(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }

    @Override // android.view.WindowManager
    public boolean shouldShowSystemDecors(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().shouldShowSystemDecors(displayId);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.WindowManager
    public void setShouldShowIme(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService().setShouldShowIme(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }

    @Override // android.view.WindowManager
    public boolean shouldShowIme(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().shouldShowIme(displayId);
        } catch (RemoteException e) {
            return false;
        }
    }
}
