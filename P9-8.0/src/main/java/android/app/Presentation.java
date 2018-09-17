package android.app;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerImpl;

public class Presentation extends Dialog {
    private static final int MSG_CANCEL = 1;
    private static final String TAG = "Presentation";
    private final Display mDisplay;
    private final DisplayListener mDisplayListener;
    private final DisplayManager mDisplayManager;
    private final Handler mHandler;
    private final IBinder mToken;

    public Presentation(Context outerContext, Display display) {
        this(outerContext, display, 0);
    }

    public Presentation(Context outerContext, Display display, int theme) {
        super(createPresentationContext(outerContext, display, theme), theme, false);
        this.mToken = new Binder();
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
                if (displayId == Presentation.this.mDisplay.getDisplayId()) {
                    Presentation.this.handleDisplayRemoved();
                }
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == Presentation.this.mDisplay.getDisplayId()) {
                    Presentation.this.handleDisplayChanged();
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Presentation.this.cancel();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mDisplay = display;
        this.mDisplayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
        Window w = getWindow();
        LayoutParams attr = w.getAttributes();
        attr.token = this.mToken;
        w.setAttributes(attr);
        w.setGravity(119);
        w.setType(2037);
        setCanceledOnTouchOutside(false);
    }

    public Display getDisplay() {
        return this.mDisplay;
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    protected void onStart() {
        super.onStart();
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
        if (!isConfigurationStillValid()) {
            Log.i(TAG, "Presentation is being dismissed because the display metrics have changed since it was created.");
            this.mHandler.sendEmptyMessage(1);
        }
    }

    protected void onStop() {
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        super.onStop();
    }

    public void show() {
        super.show();
    }

    public void onDisplayRemoved() {
    }

    public void onDisplayChanged() {
    }

    private void handleDisplayRemoved() {
        onDisplayRemoved();
        cancel();
    }

    private void handleDisplayChanged() {
        onDisplayChanged();
        if (!isConfigurationStillValid()) {
            Log.i(TAG, "Presentation is being dismissed because the display metrics have changed since it was created.");
            cancel();
        }
    }

    private boolean isConfigurationStillValid() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mDisplay.getMetrics(dm);
        return dm.equalsPhysical(getResources().getDisplayMetrics());
    }

    private static Context createPresentationContext(Context outerContext, Display display, int theme) {
        if (outerContext == null) {
            throw new IllegalArgumentException("outerContext must not be null");
        } else if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        } else {
            Context displayContext = outerContext.createDisplayContext(display);
            if (theme == 0) {
                TypedValue outValue = new TypedValue();
                displayContext.getTheme().resolveAttribute(R.attr.presentationTheme, outValue, true);
                theme = outValue.resourceId;
            }
            final WindowManagerImpl displayWindowManager = ((WindowManagerImpl) outerContext.getSystemService(Context.WINDOW_SERVICE)).createPresentationWindowManager(displayContext);
            return new ContextThemeWrapper(displayContext, theme) {
                public Object getSystemService(String name) {
                    if (Context.WINDOW_SERVICE.equals(name)) {
                        return displayWindowManager;
                    }
                    return super.getSystemService(name);
                }
            };
        }
    }
}
