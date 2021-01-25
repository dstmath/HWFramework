package com.android.server.display;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.TextView;
import com.android.internal.util.DumpUtils;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class OverlayDisplayWindow implements DumpUtils.Dump {
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
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.display.OverlayDisplayWindow.AnonymousClass1 */

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
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

        @Override // android.hardware.display.DisplayManager.DisplayListener
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
    private final GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        /* class com.android.server.display.OverlayDisplayWindow.AnonymousClass4 */

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            OverlayDisplayWindow.access$724(OverlayDisplayWindow.this, distanceX);
            OverlayDisplayWindow.access$824(OverlayDisplayWindow.this, distanceY);
            OverlayDisplayWindow.this.relayout();
            return true;
        }
    };
    private final ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /* class com.android.server.display.OverlayDisplayWindow.AnonymousClass5 */

        @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
        public boolean onScale(ScaleGestureDetector detector) {
            OverlayDisplayWindow.access$932(OverlayDisplayWindow.this, detector.getScaleFactor());
            OverlayDisplayWindow.this.relayout();
            return true;
        }
    };
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        /* class com.android.server.display.OverlayDisplayWindow.AnonymousClass3 */

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent event) {
            float oldX = event.getX();
            float oldY = event.getY();
            event.setLocation(event.getRawX(), event.getRawY());
            OverlayDisplayWindow.this.mGestureDetector.onTouchEvent(event);
            OverlayDisplayWindow.this.mScaleGestureDetector.onTouchEvent(event);
            int actionMasked = event.getActionMasked();
            if (actionMasked == 1 || actionMasked == 3) {
                OverlayDisplayWindow.this.saveWindowParams();
            }
            event.setLocation(oldX, oldY);
            return true;
        }
    };
    private ScaleGestureDetector mScaleGestureDetector;
    private final boolean mSecure;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        /* class com.android.server.display.OverlayDisplayWindow.AnonymousClass2 */

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            OverlayDisplayWindow.this.mListener.onWindowCreated(surfaceTexture, OverlayDisplayWindow.this.mDefaultDisplayInfo.getMode().getRefreshRate(), OverlayDisplayWindow.this.mDefaultDisplayInfo.presentationDeadlineNanos, OverlayDisplayWindow.this.mDefaultDisplayInfo.state);
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            OverlayDisplayWindow.this.mListener.onWindowDestroyed();
            return true;
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
    private TextureView mTextureView;
    private String mTitle;
    private TextView mTitleTextView;
    private IVRSystemServiceManager mVrMananger;
    private int mWidth;
    private View mWindowContent;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private float mWindowScale;
    private boolean mWindowVisible;
    private int mWindowX;
    private int mWindowY;

    public interface Listener {
        void onStateChanged(int i);

        void onWindowCreated(SurfaceTexture surfaceTexture, float f, long j, int i);

        void onWindowDestroyed();
    }

    static /* synthetic */ float access$724(OverlayDisplayWindow x0, float x1) {
        float f = x0.mLiveTranslationX - x1;
        x0.mLiveTranslationX = f;
        return f;
    }

    static /* synthetic */ float access$824(OverlayDisplayWindow x0, float x1) {
        float f = x0.mLiveTranslationY - x1;
        x0.mLiveTranslationY = f;
        return f;
    }

    static /* synthetic */ float access$932(OverlayDisplayWindow x0, float x1) {
        float f = x0.mLiveScale * x1;
        x0.mLiveScale = f;
        return f;
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
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        updateDefaultDisplayInfo();
        resize(width, height, densityDpi, false);
        createWindow();
    }

    public void show() {
        if (!this.mWindowVisible) {
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
            if (!updateDefaultDisplayInfo()) {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
                return;
            }
            clearLiveState();
            updateWindowParams();
            this.mWindowManager.addView(this.mWindowContent, this.mWindowParams);
            this.mWindowVisible = true;
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
        this.mTitle = this.mContext.getResources().getString(17039997, this.mName, Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mDensityDpi));
        if (this.mSecure) {
            this.mTitle += this.mContext.getResources().getString(17039996);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateDefaultDisplayInfo() {
        if (this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo)) {
            return true;
        }
        Slog.w(TAG, "Cannot show overlay display because there is no default display upon which to show it.");
        return false;
    }

    private void createWindow() {
        this.mWindowContent = LayoutInflater.from(this.mContext).inflate(17367222, (ViewGroup) null);
        this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
        this.mTextureView = (TextureView) this.mWindowContent.findViewById(16909246);
        this.mTextureView.setPivotX(0.0f);
        this.mTextureView.setPivotY(0.0f);
        this.mTextureView.getLayoutParams().width = this.mWidth;
        this.mTextureView.getLayoutParams().height = this.mHeight;
        int i = 0;
        this.mTextureView.setOpaque(false);
        this.mTextureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
        this.mTitleTextView = (TextView) this.mWindowContent.findViewById(16909247);
        this.mTitleTextView.setText(this.mTitle);
        this.mWindowParams = new WindowManager.LayoutParams(2026);
        this.mWindowParams.flags |= 16778024;
        if (this.mSecure) {
            this.mWindowParams.flags |= 8192;
        }
        this.mWindowParams.privateFlags |= 2;
        WindowManager.LayoutParams layoutParams = this.mWindowParams;
        layoutParams.alpha = 0.8f;
        layoutParams.gravity = 51;
        layoutParams.setTitle(this.mTitle);
        this.mGestureDetector = new GestureDetector(this.mContext, this.mOnGestureListener);
        this.mScaleGestureDetector = new ScaleGestureDetector(this.mContext, this.mOnScaleGestureListener);
        this.mWindowX = (this.mGravity & 3) == 3 ? 0 : this.mDefaultDisplayInfo.logicalWidth;
        if ((this.mGravity & 48) != 48) {
            i = this.mDefaultDisplayInfo.logicalHeight;
        }
        this.mWindowY = i;
        if (this.mVrMananger.isVrVirtualDisplay(this.mName)) {
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            this.mWindowX = -1500;
            this.mWindowY = -1500;
        }
        this.mWindowScale = 0.5f;
    }

    private void updateWindowParams() {
        float scale = Math.max(0.3f, Math.min(1.0f, Math.min(Math.min(this.mWindowScale * this.mLiveScale, ((float) this.mDefaultDisplayInfo.logicalWidth) / ((float) this.mWidth)), ((float) this.mDefaultDisplayInfo.logicalHeight) / ((float) this.mHeight))));
        float offsetScale = ((scale / this.mWindowScale) - 1.0f) * 0.5f;
        int width = (int) (((float) this.mWidth) * scale);
        int height = (int) (((float) this.mHeight) * scale);
        int x = Math.max(0, Math.min((int) ((((float) this.mWindowX) + this.mLiveTranslationX) - (((float) width) * offsetScale)), this.mDefaultDisplayInfo.logicalWidth - width));
        int y = Math.max(0, Math.min((int) ((((float) this.mWindowY) + this.mLiveTranslationY) - (((float) height) * offsetScale)), this.mDefaultDisplayInfo.logicalHeight - height));
        this.mTextureView.setScaleX(scale);
        this.mTextureView.setScaleY(scale);
        WindowManager.LayoutParams layoutParams = this.mWindowParams;
        layoutParams.x = x;
        layoutParams.y = y;
        if (this.mVrMananger.isVrVirtualDisplay(this.mName)) {
            WindowManager.LayoutParams layoutParams2 = this.mWindowParams;
            IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
            layoutParams2.x = -1500;
            layoutParams2.y = -1500;
        }
        WindowManager.LayoutParams layoutParams3 = this.mWindowParams;
        layoutParams3.width = width;
        layoutParams3.height = height;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
