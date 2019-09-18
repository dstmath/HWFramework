package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.DisplayListCanvas;
import android.view.PixelCopy;
import android.view.RenderNode;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceSession;
import android.view.SurfaceView;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewRootImpl;
import android.widget.Magnifier;
import com.android.internal.util.Preconditions;

public final class Magnifier {
    private static final int NONEXISTENT_PREVIOUS_CONFIG_VALUE = -1;
    private static final HandlerThread sPixelCopyHandlerThread = new HandlerThread("magnifier pixel copy result handler");
    private final int mBitmapHeight;
    private final int mBitmapWidth;
    private Callback mCallback;
    private final Point mCenterZoomCoords = new Point();
    private final Point mClampedCenterZoomCoords = new Point();
    private SurfaceInfo mContentCopySurface;
    private final Object mLock = new Object();
    private SurfaceInfo mParentSurface;
    private final Rect mPixelCopyRequestRect = new Rect();
    private final PointF mPrevPosInView = new PointF(-1.0f, -1.0f);
    private final Point mPrevStartCoordsInSurface = new Point(-1, -1);
    private final View mView;
    private final int[] mViewCoordinatesInSurface;
    private InternalPopupWindow mWindow;
    private final Point mWindowCoords = new Point();
    private final float mWindowCornerRadius;
    private final float mWindowElevation;
    private final int mWindowHeight;
    private final int mWindowWidth;
    private final float mZoom;

    public interface Callback {
        void onOperationComplete();
    }

    private static class InternalPopupWindow {
        private static final int CONTENT_BITMAP_ALPHA = 242;
        private static final int SURFACE_Z = 5;
        /* access modifiers changed from: private */
        public Bitmap mBitmap;
        private final RenderNode mBitmapRenderNode;
        /* access modifiers changed from: private */
        public Callback mCallback;
        private final int mContentHeight;
        private final int mContentWidth;
        private final Object mDestroyLock = new Object();
        private final Display mDisplay;
        private boolean mFirstDraw = true;
        private boolean mFrameDrawScheduled;
        private final Handler mHandler;
        /* access modifiers changed from: private */
        public int mLastDrawContentPositionX;
        /* access modifiers changed from: private */
        public int mLastDrawContentPositionY;
        /* access modifiers changed from: private */
        public final Object mLock;
        private final Runnable mMagnifierUpdater;
        private final int mOffsetX;
        private final int mOffsetY;
        private boolean mPendingWindowPositionUpdate;
        private final ThreadedRenderer.SimpleRenderer mRenderer;
        private final Surface mSurface;
        private final SurfaceControl mSurfaceControl;
        private final int mSurfaceHeight;
        private final SurfaceSession mSurfaceSession;
        private final int mSurfaceWidth;
        private int mWindowPositionX;
        private int mWindowPositionY;

        /* JADX INFO: finally extract failed */
        InternalPopupWindow(Context context, Display display, Surface parentSurface, int width, int height, float elevation, float cornerRadius, Handler handler, Object lock, Callback callback) {
            this.mDisplay = display;
            this.mLock = lock;
            this.mCallback = callback;
            this.mContentWidth = width;
            this.mContentHeight = height;
            this.mOffsetX = (int) (((float) width) * 0.1f);
            this.mOffsetY = (int) (0.1f * ((float) height));
            this.mSurfaceWidth = this.mContentWidth + (this.mOffsetX * 2);
            this.mSurfaceHeight = this.mContentHeight + (2 * this.mOffsetY);
            this.mSurfaceSession = new SurfaceSession(parentSurface);
            this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setFormat(-3).setSize(this.mSurfaceWidth, this.mSurfaceHeight).setName("magnifier surface").setFlags(4).build();
            this.mSurface = new Surface();
            this.mSurface.copyFrom(this.mSurfaceControl);
            this.mRenderer = new ThreadedRenderer.SimpleRenderer(context, "magnifier renderer", this.mSurface);
            this.mBitmapRenderNode = createRenderNodeForBitmap("magnifier content", elevation, cornerRadius);
            DisplayListCanvas canvas = this.mRenderer.getRootNode().start(width, height);
            try {
                canvas.insertReorderBarrier();
                canvas.drawRenderNode(this.mBitmapRenderNode);
                canvas.insertInorderBarrier();
                this.mRenderer.getRootNode().end(canvas);
                this.mHandler = handler;
                this.mMagnifierUpdater = new Runnable() {
                    public final void run() {
                        Magnifier.InternalPopupWindow.this.doDraw();
                    }
                };
                this.mFrameDrawScheduled = false;
            } catch (Throwable th) {
                this.mRenderer.getRootNode().end(canvas);
                throw th;
            }
        }

        private RenderNode createRenderNodeForBitmap(String name, float elevation, float cornerRadius) {
            RenderNode bitmapRenderNode = RenderNode.create(name, null);
            bitmapRenderNode.setLeftTopRightBottom(this.mOffsetX, this.mOffsetY, this.mOffsetX + this.mContentWidth, this.mOffsetY + this.mContentHeight);
            bitmapRenderNode.setElevation(elevation);
            Outline outline = new Outline();
            outline.setRoundRect(0, 0, this.mContentWidth, this.mContentHeight, cornerRadius);
            outline.setAlpha(1.0f);
            bitmapRenderNode.setOutline(outline);
            bitmapRenderNode.setClipToOutline(true);
            DisplayListCanvas canvas = bitmapRenderNode.start(this.mContentWidth, this.mContentHeight);
            try {
                canvas.drawColor(-16711936);
                return bitmapRenderNode;
            } finally {
                bitmapRenderNode.end(canvas);
            }
        }

        public void setContentPositionForNextDraw(int contentX, int contentY) {
            this.mWindowPositionX = contentX - this.mOffsetX;
            this.mWindowPositionY = contentY - this.mOffsetY;
            this.mPendingWindowPositionUpdate = true;
            requestUpdate();
        }

        public void updateContent(Bitmap bitmap) {
            if (this.mBitmap != null) {
                this.mBitmap.recycle();
            }
            this.mBitmap = bitmap;
            requestUpdate();
        }

        private void requestUpdate() {
            if (!this.mFrameDrawScheduled) {
                Message request = Message.obtain(this.mHandler, this.mMagnifierUpdater);
                request.setAsynchronous(true);
                request.sendToTarget();
                this.mFrameDrawScheduled = true;
            }
        }

        public void destroy() {
            synchronized (this.mDestroyLock) {
                this.mSurface.destroy();
            }
            synchronized (this.mLock) {
                this.mRenderer.destroy();
                this.mSurfaceControl.destroy();
                this.mSurfaceSession.kill();
                this.mBitmapRenderNode.destroy();
                this.mHandler.removeCallbacks(this.mMagnifierUpdater);
                if (this.mBitmap != null) {
                    this.mBitmap.recycle();
                }
            }
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0080, code lost:
            r12.mRenderer.draw(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0087, code lost:
            if (r12.mCallback == null) goto L_0x008e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0089, code lost:
            r12.mCallback.onOperationComplete();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x008e, code lost:
            return;
         */
        public void doDraw() {
            $$Lambda$Magnifier$InternalPopupWindow$vZThyvjDQhg2J1GAeOWCNqy2iiw r2;
            synchronized (this.mLock) {
                if (this.mSurface.isValid()) {
                    DisplayListCanvas canvas = this.mBitmapRenderNode.start(this.mContentWidth, this.mContentHeight);
                    try {
                        canvas.drawColor(-1);
                        Rect srcRect = new Rect(0, 0, this.mBitmap.getWidth(), this.mBitmap.getHeight());
                        Rect dstRect = new Rect(0, 0, this.mContentWidth, this.mContentHeight);
                        Paint paint = new Paint();
                        paint.setFilterBitmap(true);
                        paint.setAlpha(242);
                        canvas.drawBitmap(this.mBitmap, srcRect, dstRect, paint);
                        this.mBitmapRenderNode.end(canvas);
                        if (!this.mPendingWindowPositionUpdate) {
                            if (!this.mFirstDraw) {
                                r2 = null;
                                this.mLastDrawContentPositionX = this.mWindowPositionX + this.mOffsetX;
                                this.mLastDrawContentPositionY = this.mWindowPositionY + this.mOffsetY;
                                this.mFrameDrawScheduled = false;
                            }
                        }
                        boolean firstDraw = this.mFirstDraw;
                        this.mFirstDraw = false;
                        boolean updateWindowPosition = this.mPendingWindowPositionUpdate;
                        this.mPendingWindowPositionUpdate = false;
                        r2 = new ThreadedRenderer.FrameDrawingCallback(this.mWindowPositionX, this.mWindowPositionY, updateWindowPosition, firstDraw) {
                            private final /* synthetic */ int f$1;
                            private final /* synthetic */ int f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ boolean f$4;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                            }

                            public final void onFrameDraw(long j) {
                                Magnifier.InternalPopupWindow.lambda$doDraw$0(Magnifier.InternalPopupWindow.this, this.f$1, this.f$2, this.f$3, this.f$4, j);
                            }
                        };
                        this.mLastDrawContentPositionX = this.mWindowPositionX + this.mOffsetX;
                        this.mLastDrawContentPositionY = this.mWindowPositionY + this.mOffsetY;
                        this.mFrameDrawScheduled = false;
                    } catch (Throwable th) {
                        this.mBitmapRenderNode.end(canvas);
                        throw th;
                    }
                }
            }
        }

        public static /* synthetic */ void lambda$doDraw$0(InternalPopupWindow internalPopupWindow, int pendingX, int pendingY, boolean updateWindowPosition, boolean firstDraw, long frame) {
            synchronized (internalPopupWindow.mDestroyLock) {
                if (internalPopupWindow.mSurface.isValid()) {
                    synchronized (internalPopupWindow.mLock) {
                        internalPopupWindow.mRenderer.setLightCenter(internalPopupWindow.mDisplay, pendingX, pendingY);
                        SurfaceControl.openTransaction();
                        internalPopupWindow.mSurfaceControl.deferTransactionUntil(internalPopupWindow.mSurface, frame);
                        if (updateWindowPosition) {
                            internalPopupWindow.mSurfaceControl.setPosition((float) pendingX, (float) pendingY);
                        }
                        if (firstDraw) {
                            internalPopupWindow.mSurfaceControl.setLayer(5);
                            internalPopupWindow.mSurfaceControl.show();
                        }
                        SurfaceControl.closeTransaction();
                    }
                }
            }
        }
    }

    private static class SurfaceInfo {
        public static final SurfaceInfo NULL = new SurfaceInfo(null, 0, 0, false);
        /* access modifiers changed from: private */
        public int mHeight;
        /* access modifiers changed from: private */
        public boolean mIsMainWindowSurface;
        /* access modifiers changed from: private */
        public Surface mSurface;
        /* access modifiers changed from: private */
        public int mWidth;

        SurfaceInfo(Surface surface, int width, int height, boolean isMainWindowSurface) {
            this.mSurface = surface;
            this.mWidth = width;
            this.mHeight = height;
            this.mIsMainWindowSurface = isMainWindowSurface;
        }
    }

    static {
        sPixelCopyHandlerThread.start();
    }

    public Magnifier(View view) {
        this.mView = (View) Preconditions.checkNotNull(view);
        Context context = this.mView.getContext();
        this.mWindowWidth = context.getResources().getDimensionPixelSize(17105169);
        this.mWindowHeight = context.getResources().getDimensionPixelSize(17105167);
        this.mWindowElevation = context.getResources().getDimension(17105166);
        this.mWindowCornerRadius = getDeviceDefaultDialogCornerRadius();
        this.mZoom = context.getResources().getFloat(17105170);
        this.mBitmapWidth = Math.round(((float) this.mWindowWidth) / this.mZoom);
        this.mBitmapHeight = Math.round(((float) this.mWindowHeight) / this.mZoom);
        this.mViewCoordinatesInSurface = new int[2];
    }

    private float getDeviceDefaultDialogCornerRadius() {
        TypedArray ta = new ContextThemeWrapper(this.mView.getContext(), 16974120).obtainStyledAttributes(new int[]{16844145});
        float dialogCornerRadius = ta.getDimension(0, 0.0f);
        ta.recycle();
        return dialogCornerRadius;
    }

    public void show(float xPosInView, float yPosInView) {
        float yPosInView2;
        float xPosInView2 = Math.max(0.0f, Math.min(xPosInView, (float) this.mView.getWidth()));
        float yPosInView3 = Math.max(0.0f, Math.min(yPosInView, (float) this.mView.getHeight()));
        obtainSurfaces();
        obtainContentCoordinates(xPosInView2, yPosInView3);
        obtainWindowCoordinates();
        int startX = this.mClampedCenterZoomCoords.x - (this.mBitmapWidth / 2);
        int startY = this.mClampedCenterZoomCoords.y - (this.mBitmapHeight / 2);
        if (xPosInView2 != this.mPrevPosInView.x || yPosInView3 != this.mPrevPosInView.y) {
            if (this.mWindow == null) {
                synchronized (this.mLock) {
                    try {
                        yPosInView2 = yPosInView3;
                        InternalPopupWindow internalPopupWindow = new InternalPopupWindow(this.mView.getContext(), this.mView.getDisplay(), this.mParentSurface.mSurface, this.mWindowWidth, this.mWindowHeight, this.mWindowElevation, this.mWindowCornerRadius, Handler.getMain(), this.mLock, this.mCallback);
                        this.mWindow = internalPopupWindow;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } else {
                yPosInView2 = yPosInView3;
            }
            performPixelCopy(startX, startY, true);
            this.mPrevPosInView.x = xPosInView2;
            this.mPrevPosInView.y = yPosInView2;
        }
    }

    public void dismiss() {
        if (this.mWindow != null) {
            synchronized (this.mLock) {
                this.mWindow.destroy();
                this.mWindow = null;
            }
            this.mPrevPosInView.x = -1.0f;
            this.mPrevPosInView.y = -1.0f;
            this.mPrevStartCoordsInSurface.x = -1;
            this.mPrevStartCoordsInSurface.y = -1;
        }
    }

    public void update() {
        if (this.mWindow != null) {
            obtainSurfaces();
            performPixelCopy(this.mPrevStartCoordsInSurface.x, this.mPrevStartCoordsInSurface.y, false);
        }
    }

    public int getWidth() {
        return this.mWindowWidth;
    }

    public int getHeight() {
        return this.mWindowHeight;
    }

    public float getZoom() {
        return this.mZoom;
    }

    public Point getWindowCoords() {
        if (this.mWindow == null) {
            return null;
        }
        Rect surfaceInsets = this.mView.getViewRootImpl().mWindowAttributes.surfaceInsets;
        return new Point(this.mWindow.mLastDrawContentPositionX - surfaceInsets.left, this.mWindow.mLastDrawContentPositionY - surfaceInsets.top);
    }

    private void obtainSurfaces() {
        SurfaceInfo validMainWindowSurface = SurfaceInfo.NULL;
        if (this.mView.getViewRootImpl() != null) {
            ViewRootImpl viewRootImpl = this.mView.getViewRootImpl();
            Surface mainWindowSurface = viewRootImpl.mSurface;
            if (mainWindowSurface != null && mainWindowSurface.isValid()) {
                Rect surfaceInsets = viewRootImpl.mWindowAttributes.surfaceInsets;
                validMainWindowSurface = new SurfaceInfo(mainWindowSurface, viewRootImpl.getWidth() + surfaceInsets.left + surfaceInsets.right, viewRootImpl.getHeight() + surfaceInsets.top + surfaceInsets.bottom, true);
            }
        }
        SurfaceInfo validSurfaceViewSurface = SurfaceInfo.NULL;
        if (this.mView instanceof SurfaceView) {
            SurfaceHolder surfaceHolder = ((SurfaceView) this.mView).getHolder();
            Surface surfaceViewSurface = surfaceHolder.getSurface();
            if (surfaceViewSurface != null && surfaceViewSurface.isValid()) {
                Rect surfaceFrame = surfaceHolder.getSurfaceFrame();
                validSurfaceViewSurface = new SurfaceInfo(surfaceViewSurface, surfaceFrame.right, surfaceFrame.bottom, false);
            }
        }
        this.mParentSurface = validMainWindowSurface != SurfaceInfo.NULL ? validMainWindowSurface : validSurfaceViewSurface;
        this.mContentCopySurface = this.mView instanceof SurfaceView ? validSurfaceViewSurface : validMainWindowSurface;
    }

    private void obtainContentCoordinates(float xPosInView, float yPosInView) {
        float posY;
        float posX;
        this.mView.getLocationInSurface(this.mViewCoordinatesInSurface);
        if (this.mView instanceof SurfaceView) {
            posX = xPosInView;
            posY = yPosInView;
        } else {
            posX = ((float) this.mViewCoordinatesInSurface[0]) + xPosInView;
            posY = ((float) this.mViewCoordinatesInSurface[1]) + yPosInView;
        }
        this.mCenterZoomCoords.x = Math.round(posX);
        this.mCenterZoomCoords.y = Math.round(posY);
        Rect viewVisibleRegion = new Rect();
        this.mView.getGlobalVisibleRect(viewVisibleRegion);
        if (this.mView.getViewRootImpl() != null) {
            Rect surfaceInsets = this.mView.getViewRootImpl().mWindowAttributes.surfaceInsets;
            viewVisibleRegion.offset(surfaceInsets.left, surfaceInsets.top);
        }
        if (this.mView instanceof SurfaceView) {
            viewVisibleRegion.offset(-this.mViewCoordinatesInSurface[0], -this.mViewCoordinatesInSurface[1]);
        }
        this.mClampedCenterZoomCoords.x = Math.max(viewVisibleRegion.left + (this.mBitmapWidth / 2), Math.min(this.mCenterZoomCoords.x, viewVisibleRegion.right - (this.mBitmapWidth / 2)));
        this.mClampedCenterZoomCoords.y = this.mCenterZoomCoords.y;
    }

    private void obtainWindowCoordinates() {
        int verticalOffset = this.mView.getContext().getResources().getDimensionPixelSize(17105168);
        this.mWindowCoords.x = this.mCenterZoomCoords.x - (this.mWindowWidth / 2);
        this.mWindowCoords.y = (this.mCenterZoomCoords.y - (this.mWindowHeight / 2)) - verticalOffset;
        if (this.mParentSurface != this.mContentCopySurface) {
            this.mWindowCoords.x += this.mViewCoordinatesInSurface[0];
            this.mWindowCoords.y += this.mViewCoordinatesInSurface[1];
        }
    }

    private void performPixelCopy(int startXInSurface, int startYInSurface, boolean updateWindowPosition) {
        Rect systemInsets;
        int i = startXInSurface;
        int i2 = startYInSurface;
        if (this.mContentCopySurface.mSurface != null && this.mContentCopySurface.mSurface.isValid()) {
            int clampedStartXInSurface = Math.max(0, Math.min(i, this.mContentCopySurface.mWidth - this.mBitmapWidth));
            int clampedStartYInSurface = Math.max(0, Math.min(i2, this.mContentCopySurface.mHeight - this.mBitmapHeight));
            if (this.mParentSurface.mIsMainWindowSurface) {
                Rect systemInsets2 = this.mView.getRootWindowInsets().getSystemWindowInsets();
                systemInsets = new Rect(systemInsets2.left, systemInsets2.top, this.mParentSurface.mWidth - systemInsets2.right, this.mParentSurface.mHeight - systemInsets2.bottom);
            } else {
                systemInsets = new Rect(0, 0, this.mParentSurface.mWidth, this.mParentSurface.mHeight);
            }
            Rect windowBounds = systemInsets;
            int windowCoordsX = Math.max(windowBounds.left, Math.min(windowBounds.right - this.mWindowWidth, this.mWindowCoords.x));
            int windowCoordsY = Math.max(windowBounds.top, Math.min(windowBounds.bottom - this.mWindowHeight, this.mWindowCoords.y));
            this.mPixelCopyRequestRect.set(clampedStartXInSurface, clampedStartYInSurface, this.mBitmapWidth + clampedStartXInSurface, this.mBitmapHeight + clampedStartYInSurface);
            InternalPopupWindow currentWindowInstance = this.mWindow;
            Bitmap bitmap = Bitmap.createBitmap(this.mBitmapWidth, this.mBitmapHeight, Bitmap.Config.ARGB_8888);
            Surface access$000 = this.mContentCopySurface.mSurface;
            int i3 = clampedStartXInSurface;
            $$Lambda$Magnifier$1ctRJdojBZQzahoS7og5wm1FKM4 r10 = r0;
            int i4 = clampedStartYInSurface;
            Rect rect = this.mPixelCopyRequestRect;
            Rect rect2 = windowBounds;
            $$Lambda$Magnifier$1ctRJdojBZQzahoS7og5wm1FKM4 r0 = new PixelCopy.OnPixelCopyFinishedListener(currentWindowInstance, updateWindowPosition, windowCoordsX, windowCoordsY, bitmap) {
                private final /* synthetic */ Magnifier.InternalPopupWindow f$1;
                private final /* synthetic */ boolean f$2;
                private final /* synthetic */ int f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ Bitmap f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void onPixelCopyFinished(int i) {
                    Magnifier.lambda$performPixelCopy$0(Magnifier.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, i);
                }
            };
            PixelCopy.request(access$000, rect, bitmap, (PixelCopy.OnPixelCopyFinishedListener) r10, sPixelCopyHandlerThread.getThreadHandler());
            this.mPrevStartCoordsInSurface.x = i;
            this.mPrevStartCoordsInSurface.y = i2;
        }
    }

    public static /* synthetic */ void lambda$performPixelCopy$0(Magnifier magnifier, InternalPopupWindow currentWindowInstance, boolean updateWindowPosition, int windowCoordsX, int windowCoordsY, Bitmap bitmap, int result) {
        synchronized (magnifier.mLock) {
            if (magnifier.mWindow == currentWindowInstance) {
                if (updateWindowPosition) {
                    magnifier.mWindow.setContentPositionForNextDraw(windowCoordsX, windowCoordsY);
                }
                magnifier.mWindow.updateContent(bitmap);
            }
        }
    }

    public void setOnOperationCompleteCallback(Callback callback) {
        this.mCallback = callback;
        if (this.mWindow != null) {
            Callback unused = this.mWindow.mCallback = callback;
        }
    }

    public Bitmap getContent() {
        Bitmap createScaledBitmap;
        if (this.mWindow == null) {
            return null;
        }
        synchronized (this.mWindow.mLock) {
            createScaledBitmap = Bitmap.createScaledBitmap(this.mWindow.mBitmap, this.mWindowWidth, this.mWindowHeight, true);
        }
        return createScaledBitmap;
    }

    public Rect getWindowPositionOnScreen() {
        int[] viewLocationOnScreen = new int[2];
        this.mView.getLocationOnScreen(viewLocationOnScreen);
        int[] viewLocationInSurface = new int[2];
        this.mView.getLocationInSurface(viewLocationInSurface);
        int left = (this.mWindowCoords.x + viewLocationOnScreen[0]) - viewLocationInSurface[0];
        int top = (this.mWindowCoords.y + viewLocationOnScreen[1]) - viewLocationInSurface[1];
        return new Rect(left, top, this.mWindowWidth + left, this.mWindowHeight + top);
    }

    public static PointF getMagnifierDefaultSize() {
        Resources resources = Resources.getSystem();
        float density = resources.getDisplayMetrics().density;
        PointF size = new PointF();
        size.x = resources.getDimension(17105169) / density;
        size.y = resources.getDimension(17105167) / density;
        return size;
    }
}
