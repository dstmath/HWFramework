package com.android.server.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.android.internal.util.DumpUtils.Dump;
import java.io.PrintWriter;

final class OverlayDisplayWindow implements Dump {
    private static final boolean DEBUG = false;
    private static final String TAG = "OverlayDisplayWindow";
    private final boolean DISABLE_MOVE_AND_RESIZE = false;
    private final float INITIAL_SCALE = 0.5f;
    private final float MAX_SCALE = 1.0f;
    private final float MIN_SCALE = 0.3f;
    private final float WINDOW_ALPHA = 0.8f;
    private final Context mContext;
    private final Display mDefaultDisplay;
    private final DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    private int mDensityDpi;
    private final DisplayListener mDisplayListener = new DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId != OverlayDisplayWindow.this.mDefaultDisplay.getDisplayId()) {
                return;
            }
            if (OverlayDisplayWindow.this.updateDefaultDisplayInfo()) {
                OverlayDisplayWindow.this.relayout();
                OverlayDisplayWindow.this.mListener.onStateChanged(OverlayDisplayWindow.this.mDefaultDisplayInfo.state);
                return;
            }
            OverlayDisplayWindow.this.dismiss();
        }

        public void onDisplayRemoved(int displayId) {
            if (displayId == OverlayDisplayWindow.this.mDefaultDisplay.getDisplayId()) {
                OverlayDisplayWindow.this.dismiss();
            }
        }
    };
    private final DisplayManager mDisplayManager;
    private GestureDetector mGestureDetector;
    private final int mGravity;
    private int mHeight;
    private final Listener mListener;
    private float mLiveScale = 1.0f;
    private float mLiveTranslationX;
    private float mLiveTranslationY;
    private final String mName;
    private final OnGestureListener mOnGestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            OverlayDisplayWindow overlayDisplayWindow = OverlayDisplayWindow.this;
            overlayDisplayWindow.mLiveTranslationX = overlayDisplayWindow.mLiveTranslationX - distanceX;
            overlayDisplayWindow = OverlayDisplayWindow.this;
            overlayDisplayWindow.mLiveTranslationY = overlayDisplayWindow.mLiveTranslationY - distanceY;
            OverlayDisplayWindow.this.relayout();
            return true;
        }
    };
    private final OnScaleGestureListener mOnScaleGestureListener = new SimpleOnScaleGestureListener() {
        public boolean onScale(ScaleGestureDetector detector) {
            OverlayDisplayWindow overlayDisplayWindow = OverlayDisplayWindow.this;
            overlayDisplayWindow.mLiveScale = overlayDisplayWindow.mLiveScale * detector.getScaleFactor();
            OverlayDisplayWindow.this.relayout();
            return true;
        }
    };
    private final OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            float oldX = event.getX();
            float oldY = event.getY();
            event.setLocation(event.getRawX(), event.getRawY());
            OverlayDisplayWindow.this.mGestureDetector.onTouchEvent(event);
            OverlayDisplayWindow.this.mScaleGestureDetector.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case 1:
                case 3:
                    OverlayDisplayWindow.this.saveWindowParams();
                    break;
            }
            event.setLocation(oldX, oldY);
            return true;
        }
    };
    private ScaleGestureDetector mScaleGestureDetector;
    private final boolean mSecure;
    private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            OverlayDisplayWindow.this.mListener.onWindowCreated(surfaceTexture, OverlayDisplayWindow.this.mDefaultDisplayInfo.getMode().getRefreshRate(), OverlayDisplayWindow.this.mDefaultDisplayInfo.presentationDeadlineNanos, OverlayDisplayWindow.this.mDefaultDisplayInfo.state);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            OverlayDisplayWindow.this.mListener.onWindowDestroyed();
            return true;
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
    private TextureView mTextureView;
    private String mTitle;
    private TextView mTitleTextView;
    private int mWidth;
    private View mWindowContent;
    private final WindowManager mWindowManager;
    private LayoutParams mWindowParams;
    private float mWindowScale;
    private boolean mWindowVisible;
    private int mWindowX;
    private int mWindowY;

    public interface Listener {
        void onStateChanged(int i);

        void onWindowCreated(SurfaceTexture surfaceTexture, float f, long j, int i);

        void onWindowDestroyed();
    }

    public OverlayDisplayWindow(Context context, String name, int width, int height, int densityDpi, int gravity, boolean secure, Listener listener) {
        ThreadedRenderer.disableVsync();
        this.mContext = context;
        this.mName = name;
        this.mGravity = gravity;
        this.mSecure = secure;
        this.mListener = listener;
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        updateDefaultDisplayInfo();
        resize(width, height, densityDpi, false);
        createWindow();
    }

    public void show() {
        if (!this.mWindowVisible) {
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
            if (updateDefaultDisplayInfo()) {
                clearLiveState();
                updateWindowParams();
                this.mWindowManager.addView(this.mWindowContent, this.mWindowParams);
                this.mWindowVisible = true;
            } else {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
        }
    }

    public void dismiss() {
        if (this.mWindowVisible) {
            this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            this.mWindowManager.removeView(this.mWindowContent);
            this.mWindowVisible = false;
        }
    }

    public void resize(int width, int height, int densityDpi) {
        resize(width, height, densityDpi, true);
    }

    private void resize(int width, int height, int densityDpi, boolean doLayout) {
        this.mWidth = width;
        this.mHeight = height;
        this.mDensityDpi = densityDpi;
        this.mTitle = this.mContext.getResources().getString(17039901, new Object[]{this.mName, Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mDensityDpi)});
        if (this.mSecure) {
            this.mTitle += this.mContext.getResources().getString(17039900);
        }
        if (doLayout) {
            relayout();
        }
    }

    public void relayout() {
        if (this.mWindowVisible) {
            updateWindowParams();
            this.mWindowManager.updateViewLayout(this.mWindowContent, this.mWindowParams);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println("mWindowVisible=" + this.mWindowVisible);
        pw.println("mWindowX=" + this.mWindowX);
        pw.println("mWindowY=" + this.mWindowY);
        pw.println("mWindowScale=" + this.mWindowScale);
        pw.println("mWindowParams=" + this.mWindowParams);
        if (this.mTextureView != null) {
            pw.println("mTextureView.getScaleX()=" + this.mTextureView.getScaleX());
            pw.println("mTextureView.getScaleY()=" + this.mTextureView.getScaleY());
        }
        pw.println("mLiveTranslationX=" + this.mLiveTranslationX);
        pw.println("mLiveTranslationY=" + this.mLiveTranslationY);
        pw.println("mLiveScale=" + this.mLiveScale);
    }

    private boolean updateDefaultDisplayInfo() {
        if (this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo)) {
            return true;
        }
        Slog.w(TAG, "Cannot show overlay display because there is no default display upon which to show it.");
        return false;
    }

    private void createWindow() {
        int i;
        int i2 = 0;
        this.mWindowContent = LayoutInflater.from(this.mContext).inflate(17367201, null);
        this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
        this.mTextureView = (TextureView) this.mWindowContent.findViewById(16909139);
        this.mTextureView.setPivotX(0.0f);
        this.mTextureView.setPivotY(0.0f);
        this.mTextureView.getLayoutParams().width = this.mWidth;
        this.mTextureView.getLayoutParams().height = this.mHeight;
        this.mTextureView.setOpaque(false);
        this.mTextureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
        this.mTitleTextView = (TextView) this.mWindowContent.findViewById(16909140);
        this.mTitleTextView.setText(this.mTitle);
        this.mWindowParams = new LayoutParams(2026);
        LayoutParams layoutParams = this.mWindowParams;
        layoutParams.flags |= 16778024;
        if (this.mSecure) {
            layoutParams = this.mWindowParams;
            layoutParams.flags |= 8192;
        }
        layoutParams = this.mWindowParams;
        layoutParams.privateFlags |= 2;
        this.mWindowParams.alpha = 0.8f;
        this.mWindowParams.gravity = 51;
        this.mWindowParams.setTitle(this.mTitle);
        this.mGestureDetector = new GestureDetector(this.mContext, this.mOnGestureListener);
        this.mScaleGestureDetector = new ScaleGestureDetector(this.mContext, this.mOnScaleGestureListener);
        if ((this.mGravity & 3) == 3) {
            i = 0;
        } else {
            i = this.mDefaultDisplayInfo.logicalWidth;
        }
        this.mWindowX = i;
        if ((this.mGravity & 48) != 48) {
            i2 = this.mDefaultDisplayInfo.logicalHeight;
        }
        this.mWindowY = i2;
        this.mWindowScale = 0.5f;
    }

    private void updateWindowParams() {
        float scale = Math.max(0.3f, Math.min(1.0f, Math.min(Math.min(this.mWindowScale * this.mLiveScale, ((float) this.mDefaultDisplayInfo.logicalWidth) / ((float) this.mWidth)), ((float) this.mDefaultDisplayInfo.logicalHeight) / ((float) this.mHeight))));
        float offsetScale = ((scale / this.mWindowScale) - 1.0f) * 0.5f;
        int width = (int) (((float) this.mWidth) * scale);
        int height = (int) (((float) this.mHeight) * scale);
        int y = (int) ((((float) this.mWindowY) + this.mLiveTranslationY) - (((float) height) * offsetScale));
        int x = Math.max(0, Math.min((int) ((((float) this.mWindowX) + this.mLiveTranslationX) - (((float) width) * offsetScale)), this.mDefaultDisplayInfo.logicalWidth - width));
        y = Math.max(0, Math.min(y, this.mDefaultDisplayInfo.logicalHeight - height));
        this.mTextureView.setScaleX(scale);
        this.mTextureView.setScaleY(scale);
        this.mWindowParams.x = x;
        this.mWindowParams.y = y;
        this.mWindowParams.width = width;
        this.mWindowParams.height = height;
    }

    private void saveWindowParams() {
        this.mWindowX = this.mWindowParams.x;
        this.mWindowY = this.mWindowParams.y;
        this.mWindowScale = this.mTextureView.getScaleX();
        clearLiveState();
    }

    private void clearLiveState() {
        this.mLiveTranslationX = 0.0f;
        this.mLiveTranslationY = 0.0f;
        this.mLiveScale = 1.0f;
    }
}
