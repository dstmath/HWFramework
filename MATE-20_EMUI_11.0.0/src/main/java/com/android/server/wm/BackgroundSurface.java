package com.android.server.wm;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;

public class BackgroundSurface {
    private static final boolean IS_DEBUG = false;
    private static final String TAG = "BackgroundSurface";
    private Surface mBackgroundSurface = new Surface();
    private SurfaceControl mBackgroundSurfaceControl;
    private boolean mIsVisible;
    private String mName = "";
    private WindowManagerService mService;
    private Rect mSurfaceRect = new Rect();

    public BackgroundSurface(WindowManagerService service) {
        this.mService = service;
    }

    public void createBackgroundSurfaceLocked(SurfaceControl parentSc, Rect surfaceRect, Point originPoint) {
        if (parentSc != null && surfaceRect != null && originPoint != null) {
            SurfaceControl ctrl = null;
            try {
                ctrl = this.mService.getDefaultDisplayContentLocked().makeOverlay().setName("BackgroundSurfaceControl").setBufferSize(surfaceRect.width(), surfaceRect.height()).setParent(parentSc).build();
                ctrl.setPosition((float) (-originPoint.x), (float) (-originPoint.y));
                ctrl.setLayer(-1);
                this.mBackgroundSurface.copyFrom(ctrl);
                this.mSurfaceRect = new Rect(surfaceRect);
                this.mName = parentSc.toString();
            } catch (Surface.OutOfResourcesException e) {
                Log.d(TAG, "createBackgroundSurface fail");
            }
            this.mBackgroundSurfaceControl = ctrl;
        }
    }

    public void drawBackgroundColorLocked(int color) {
        Canvas canvas = null;
        try {
            canvas = this.mBackgroundSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "BackgroundSurfaceControl illegal argument");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "BackgroundSurfaceControl out of resource");
        }
        if (canvas != null) {
            canvas.drawColor(color);
            this.mBackgroundSurface.unlockCanvasAndPost(canvas);
        }
    }

    public void updateBackgroundSurfaceLocked(SurfaceControl.Transaction transaction, boolean isVisible) {
        SurfaceControl surfaceControl = this.mBackgroundSurfaceControl;
        if (surfaceControl != null && this.mIsVisible != isVisible) {
            if (isVisible) {
                transaction.show(surfaceControl);
                this.mIsVisible = true;
                return;
            }
            transaction.hide(surfaceControl);
            this.mIsVisible = false;
        }
    }

    public void destroyBackgroundSurfaceLocked(SurfaceControl.Transaction transaction) {
        SurfaceControl surfaceControl = this.mBackgroundSurfaceControl;
        if (surfaceControl != null) {
            transaction.remove(surfaceControl);
            this.mBackgroundSurfaceControl = null;
            this.mIsVisible = false;
        }
    }

    private Bitmap getWallpaperBitmap() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mService.mContext);
        if (wallpaperManager == null) {
            return null;
        }
        DisplayInfo defaultDisplayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        Bitmap blurBitmap = wallpaperManager.getBlurBitmap(new Rect(0, 0, defaultDisplayInfo.logicalWidth, defaultDisplayInfo.logicalHeight));
        if (blurBitmap != null) {
            return Bitmap.createScaledBitmap(blurBitmap, this.mSurfaceRect.width(), this.mSurfaceRect.height(), true);
        }
        return null;
    }

    public void drawBackgroundBitmapLocked(Bitmap bitmap) {
        Canvas canvas = null;
        if (bitmap == null) {
            Log.e(TAG, "drawBackgroundBitmapLocked, bitmap is null");
            return;
        }
        try {
            canvas = this.mBackgroundSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "drawBackgroundBitmapLocked illegal argument");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "drawBackgroundBitmapLocked out of resource");
        }
        if (canvas != null) {
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
            this.mBackgroundSurface.unlockCanvasAndPost(canvas);
        }
    }

    public void drawBlurWallpaperBackgroundLocked() {
        Bitmap blurWallpaper = getWallpaperBitmap();
        if (blurWallpaper != null) {
            drawBackgroundBitmapLocked(blurWallpaper);
        }
    }
}
