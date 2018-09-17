package com.android.server.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import java.util.ArrayList;

public class EasyWakeUpView extends FrameLayout {
    private static int[] BITMAP_SET = new int[10];
    private static final boolean DEBUG = false;
    private static String TAG = "EasyWakeUpView";
    public static final int WAKEUP_INDEX_DOUBLE_CLICK = -1;
    public static final int WAKEUP_INDEX_INVALID = -2;
    public static final int WAKEUP_INDEX_LETTER_C = 5;
    public static final int WAKEUP_INDEX_LETTER_E = 6;
    public static final int WAKEUP_INDEX_LETTER_M = 7;
    public static final int WAKEUP_INDEX_LETTER_O = 4;
    public static final int WAKEUP_INDEX_LETTER_W = 8;
    public static final int WAKEUP_INDEX_SLIP_DOWN = 1;
    public static final int WAKEUP_INDEX_SLIP_LEFT = 2;
    public static final int WAKEUP_INDEX_SLIP_RIGHT = 3;
    public static final int WAKEUP_INDEX_SLIP_UP = 0;
    boolean isfirst;
    private GIFFrameView mGifFrameView;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;
    private EasyWakeUpCallback mWakeUpCallback;

    public interface EasyWakeUpCallback {
        void disappearTrackAnimation();
    }

    public class GIFFrameView extends View {
        private int Index = 0;
        private Bitmap bitmap = null;
        private Point center;
        private int mIndex = 0;
        private ArrayList<Point> paramList;

        public void setParamList(ArrayList<Point> paramList) {
            this.paramList = paramList;
        }

        public GIFFrameView(Context context, int index) {
            super(context);
            this.mIndex = index;
        }

        public void setCenter(Point center) {
            this.center = center;
        }

        protected void onDraw(Canvas canvas) {
            if (EasyWakeUpView.this.mWakeLock.isHeld()) {
                EasyWakeUpView.this.mWakeLock.release();
            }
            canvas.save();
            loadBitmap();
            if (this.bitmap == null) {
                this.Index = 0;
                EasyWakeUpView.this.isfirst = true;
            } else if (EasyWakeUpView.this.isfirst) {
                canvas.drawColor(-16777216);
                postDelayed(new Runnable() {
                    public void run() {
                        Log.e(EasyWakeUpView.TAG, "wake up draw start!");
                        EasyWakeUpView.this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
                        GIFFrameView.this.invalidate();
                    }
                }, 32);
                EasyWakeUpView.this.isfirst = false;
            } else {
                Paint Paint = new Paint();
                int bitmapwidth = this.bitmap.getWidth();
                int bitmapheight = this.bitmap.getHeight();
                if (this.Index < 20) {
                    int x = (this.Index % 5) * (bitmapwidth / 5);
                    int y = (this.Index / 5) * (bitmapheight / 4);
                    Rect src = new Rect(x, y, (bitmapwidth / 5) + x, (bitmapheight / 4) + y);
                    Log.e(EasyWakeUpView.TAG, "wake up draw   Index = " + this.Index + "; Screen = " + EasyWakeUpView.this.mPowerManager.isScreenOn());
                    this.Index++;
                    canvas.drawBitmap(this.bitmap, src, drawImage(canvas, bitmapwidth, bitmapheight, null), Paint);
                    canvas.restore();
                    invalidate();
                    return;
                }
                this.Index = 0;
                Log.e(EasyWakeUpView.TAG, "wake up draw end!");
                stopFrameAnimation();
                this.bitmap.recycle();
                this.bitmap = null;
                EasyWakeUpView.this.isfirst = true;
            }
        }

        private Rect drawImage(Canvas canvas, int bitmapwidth, int bitmapheight, Rect dst) {
            switch (this.mIndex) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return drawLineToRotate(canvas, this.mIndex, bitmapwidth, bitmapheight);
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return drawLetterToRotate(canvas, this.mIndex);
                default:
                    return dst;
            }
        }

        private void loadBitmap() {
            if (this.bitmap == null) {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(), EasyWakeUpView.BITMAP_SET[this.mIndex], options);
                int imageWidth = options.outWidth;
                int imageHeight = options.outHeight;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 3;
                this.bitmap = BitmapFactory.decodeResource(getResources(), EasyWakeUpView.BITMAP_SET[this.mIndex], options);
            }
        }

        private Rect drawLetterToRotate(Canvas canvas, int mindex) {
            float mRotate = 0.0f;
            float angle;
            switch (mindex) {
                case 4:
                case 6:
                    mRotate = 0.0f;
                    break;
                case 5:
                    float dy = (float) (((Point) this.paramList.get(1)).y - ((Point) this.paramList.get(0)).y);
                    angle = (float) Math.toDegrees(Math.atan((double) ((-((float) (((Point) this.paramList.get(1)).x - ((Point) this.paramList.get(0)).x))) / dy)));
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    mRotate = angle % 360.0f;
                    break;
                case 7:
                case 8:
                    angle = (float) Math.toDegrees(Math.atan((double) (((float) (((Point) this.paramList.get(1)).y - ((Point) this.paramList.get(0)).y)) / ((float) (((Point) this.paramList.get(1)).x - ((Point) this.paramList.get(0)).x)))));
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    mRotate = angle % 360.0f;
                    break;
            }
            Rect dst = new Rect(((Point) this.paramList.get(3)).x - this.center.x, ((Point) this.paramList.get(2)).y - this.center.y, ((Point) this.paramList.get(5)).x - this.center.x, ((Point) this.paramList.get(4)).y - this.center.y);
            canvas.translate((float) this.center.x, (float) this.center.y);
            dst.left = (dst.left * 3) / 2;
            dst.top = (dst.top * 3) / 2;
            dst.right = (dst.right * 3) / 2;
            dst.bottom = (dst.bottom * 3) / 2;
            canvas.rotate(mRotate);
            return dst;
        }

        private Rect drawLineToRotate(Canvas canvas, int mindex, int bitmapwidth, int bitmapheight) {
            Rect dst = null;
            float dx = (float) (((Point) this.paramList.get(1)).x - ((Point) this.paramList.get(0)).x);
            float dy = (float) (((Point) this.paramList.get(1)).y - ((Point) this.paramList.get(0)).y);
            float angle = (float) Math.toDegrees(Math.atan((double) (dy / dx)));
            switch (mindex) {
                case 0:
                    if (dx > 0.0f) {
                        dst = new Rect((((Point) this.paramList.get(0)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(1)).y - this.center.y, (((Point) this.paramList.get(1)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y);
                    } else {
                        dst = new Rect((((Point) this.paramList.get(1)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(1)).y - this.center.y, (((Point) this.paramList.get(0)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y);
                    }
                    if (angle <= 0.0f) {
                        angle += 90.0f;
                        break;
                    }
                    angle -= 90.0f;
                    break;
                case 1:
                    if (angle > 0.0f) {
                        angle -= 90.0f;
                    } else {
                        angle += 90.0f;
                    }
                    if (dx <= 0.0f) {
                        dst = new Rect((((Point) this.paramList.get(1)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y, (((Point) this.paramList.get(0)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(1)).y - this.center.y);
                        break;
                    }
                    dst = new Rect((((Point) this.paramList.get(0)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y, (((Point) this.paramList.get(1)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(1)).y - this.center.y);
                    break;
                case 2:
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    if (dy <= 0.0f) {
                        dst = new Rect(((Point) this.paramList.get(1)).x - this.center.x, (((Point) this.paramList.get(1)).y - (bitmapheight / 8)) - this.center.y, ((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(0)).y + (bitmapheight / 8)) - this.center.y);
                        break;
                    }
                    dst = new Rect(((Point) this.paramList.get(1)).x - this.center.x, (((Point) this.paramList.get(0)).y - (bitmapheight / 8)) - this.center.y, ((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(1)).y + (bitmapheight / 8)) - this.center.y);
                    break;
                case 3:
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    if (dy <= 0.0f) {
                        dst = new Rect(((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(1)).y - (bitmapheight / 8)) - this.center.y, ((Point) this.paramList.get(1)).x - this.center.x, (((Point) this.paramList.get(0)).y + (bitmapheight / 8)) - this.center.y);
                        break;
                    }
                    dst = new Rect(((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(0)).y - (bitmapheight / 8)) - this.center.y, ((Point) this.paramList.get(1)).x - this.center.x, (((Point) this.paramList.get(1)).y + (bitmapheight / 8)) - this.center.y);
                    break;
            }
            float mRotate = angle % 360.0f;
            canvas.translate((float) this.center.x, (float) this.center.y);
            canvas.rotate(mRotate);
            return dst;
        }

        public void stopFrameAnimation() {
            if (EasyWakeUpView.this.mWakeUpCallback != null) {
                EasyWakeUpView.this.mWakeUpCallback.disappearTrackAnimation();
            }
        }
    }

    static {
        BITMAP_SET[0] = 33751255;
        BITMAP_SET[1] = 33751252;
        BITMAP_SET[2] = 33751253;
        BITMAP_SET[3] = 33751254;
        BITMAP_SET[4] = 33751250;
        BITMAP_SET[5] = 33751247;
        BITMAP_SET[6] = 33751248;
        BITMAP_SET[7] = 33751249;
        BITMAP_SET[8] = 33751251;
    }

    public EasyWakeUpView(Context paramContext, int index) {
        this(paramContext, null, index);
    }

    public EasyWakeUpView(Context paramContext, AttributeSet paramAttributeSet, int index) {
        this(paramContext, paramAttributeSet, 0, index);
    }

    public EasyWakeUpView(Context paramContext, AttributeSet paramAttributeSet, int paramInt, int index) {
        super(paramContext, paramAttributeSet, paramInt);
        this.mPowerManager = null;
        this.mGifFrameView = null;
        this.mWakeLock = null;
        this.isfirst = true;
        setBackgroundColor(-16777216);
        this.mGifFrameView = new GIFFrameView(paramContext, index);
    }

    public void setPowerManager(PowerManager powerManager) {
        this.mPowerManager = powerManager;
    }

    public void setEasyWakeUpCallback(EasyWakeUpCallback callback) {
        this.mWakeUpCallback = callback;
    }

    public void startTrackAnimation(ArrayList<Point> localList, int index) {
        if (localList == null) {
            Log.v(TAG, TAG + " startTrackAnimation and return false for the localList is Null");
        } else if (localList.size() < 2) {
            Log.v(TAG, TAG + " startTrackAnimation and return false for  the size of pointList is less than 2");
        } else {
            removeAllViews();
            this.mWakeLock = this.mPowerManager.newWakeLock(536870913, "");
            this.mWakeLock.acquire();
            addCharView_Up(localList, index);
        }
    }

    private void addCharView_Up(ArrayList<Point> paramList, int index) {
        this.mGifFrameView.setCenter(getCenter(paramList, index));
        this.mGifFrameView.setParamList(paramList);
        addView(this.mGifFrameView, new LayoutParams(-1, -1));
    }

    private Point getCenter(ArrayList<Point> pointList, int index) {
        Point center = new Point();
        if (index >= 0 && index <= 3) {
            center.x = (((Point) pointList.get(0)).x + ((Point) pointList.get(1)).x) / 2;
            center.y = (((Point) pointList.get(0)).y + ((Point) pointList.get(1)).y) / 2;
        } else if (index >= 4) {
            center.x = (((Point) pointList.get(5)).x + ((Point) pointList.get(3)).x) / 2;
            center.y = (((Point) pointList.get(4)).y + ((Point) pointList.get(2)).y) / 2;
        }
        return center;
    }

    private int maxInt(int x, int y) {
        return x > y ? x : y;
    }

    private int minInt(int x, int y) {
        return x > y ? y : x;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
