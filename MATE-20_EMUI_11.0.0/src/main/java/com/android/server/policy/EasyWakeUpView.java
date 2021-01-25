package com.android.server.policy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.List;

public class EasyWakeUpView extends FrameLayout {
    private static final float ANNULAR_360_DEGREE = 360.0f;
    private static final int BITMAP_HEIGHT_PER_PIXEL = 8;
    private static final int[] BITMAP_SETS = new int[10];
    private static final int BITMAP_SETS_SIZE = 10;
    private static final int BITMAP_WIDTH_PER_PIXEL = 10;
    private static final int DEFAULT_DELAY_TIMES_MS = 32;
    private static final int FIVE_TIMES_FACTOR = 5;
    private static final int FOUR_TIMES_FACTOR = 4;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private static final int INDEX_4 = 4;
    private static final int INDEX_5 = 5;
    private static final int MAX_INDEX_VALUE = 20;
    private static final int MIN_POINT_LIST_SIZE = 2;
    private static final float QUATER_90_DEGREE = 90.0f;
    private static final String TAG = "EasyWakeUpView";
    private static final int THREE_TIMES_FACTOR = 3;
    private static final int TWO_TIMES_FACTOR = 2;
    public static final int WAKEUP_INDEX_DOUBLE_CLICK = -1;
    public static final int WAKEUP_INDEX_INVALID = -2;
    public static final int WAKEUP_INDEX_LETTER_C = 5;
    public static final int WAKEUP_INDEX_LETTER_E = 6;
    public static final int WAKEUP_INDEX_LETTER_M = 7;
    public static final int WAKEUP_INDEX_LETTER_O = 4;
    public static final int WAKEUP_INDEX_LETTER_W = 8;
    public static final int WAKEUP_INDEX_SINGLE_CLICK = 9;
    public static final int WAKEUP_INDEX_SLIP_DOWN = 1;
    public static final int WAKEUP_INDEX_SLIP_LEFT = 2;
    public static final int WAKEUP_INDEX_SLIP_RIGHT = 3;
    public static final int WAKEUP_INDEX_SLIP_UP = 0;
    private GifFrameView mGifFrameView;
    boolean mIsFirst;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private EasyWakeUpCallback mWakeUpCallback;

    public interface EasyWakeUpCallback {
        void disappearTrackAnimation();
    }

    static {
        int[] iArr = BITMAP_SETS;
        iArr[0] = 33751255;
        iArr[1] = 33751252;
        iArr[2] = 33751253;
        iArr[3] = 33751254;
        iArr[4] = 33751250;
        iArr[5] = 33751247;
        iArr[6] = 33751248;
        iArr[7] = 33751249;
        iArr[8] = 33751251;
    }

    public EasyWakeUpView(Context paramContext, int index) {
        this(paramContext, null, index);
    }

    public EasyWakeUpView(Context paramContext, AttributeSet paramAttributeSet, int index) {
        this(paramContext, paramAttributeSet, 0, index);
    }

    public EasyWakeUpView(Context paramContext, AttributeSet paramAttributeSet, int paramInt, int index) {
        super(paramContext, paramAttributeSet, paramInt);
        this.mIsFirst = true;
        this.mPowerManager = null;
        this.mGifFrameView = null;
        this.mWakeLock = null;
        setBackgroundColor(-16777216);
        this.mGifFrameView = new GifFrameView(paramContext, index);
    }

    public void setPowerManager(PowerManager powerManager) {
        this.mPowerManager = powerManager;
    }

    public void setEasyWakeUpCallback(EasyWakeUpCallback callback) {
        this.mWakeUpCallback = callback;
    }

    public void startTrackAnimation(List<Point> localList, int index) {
        if (localList == null || localList.size() < 2) {
            Log.e(TAG, " startTrackAnimation and return false for the size of pointList is less than 2");
            return;
        }
        removeAllViews();
        this.mWakeLock = this.mPowerManager.newWakeLock(536870913, "");
        this.mWakeLock.acquire();
        addCharViewUp(localList, index);
        Log.d(TAG, "easywakeup wake up the CPU in EasyWakupView after addView");
    }

    private void addCharViewUp(List<Point> paramList, int index) {
        this.mGifFrameView.setCenter(getCenter(paramList, index));
        this.mGifFrameView.setParamList(paramList);
        addView(this.mGifFrameView, new FrameLayout.LayoutParams(-1, -1));
    }

    private Point getCenter(List<Point> pointList, int index) {
        Point center = new Point();
        if (index >= 0 && index <= 3) {
            Log.d(TAG, "point 1 = (" + pointList.get(0).x + ", " + pointList.get(0).y + ") + point 2 =  (" + pointList.get(1).x + AwarenessInnerConstants.COMMA_KEY + pointList.get(1).y + ")");
            center.x = (pointList.get(1).x + pointList.get(0).x) / 2;
            center.y = (pointList.get(1).y + pointList.get(0).y) / 2;
            return center;
        } else if (index < 4) {
            return center;
        } else {
            if (pointList.size() <= 5) {
                Log.e(TAG, " getCenter return with the size of pointList is less than 6");
                return center;
            }
            Log.d(TAG, " Top = " + pointList.get(2).y + "; Buttom = " + pointList.get(4).y + "; Left = " + pointList.get(3).x + "; Right = " + pointList.get(5).x);
            center.x = (pointList.get(3).x + pointList.get(5).x) / 2;
            center.y = (pointList.get(2).y + pointList.get(4).y) / 2;
            return center;
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /* access modifiers changed from: private */
    public class GifFrameView extends View {
        private Bitmap bitmap = null;
        private Point center;
        private int mIndex = 0;
        private int mIndexValue = 0;
        private List<Point> paramList;

        GifFrameView(Context context, int index) {
            super(context);
            this.mIndex = index;
        }

        public void setParamList(List<Point> paramList2) {
            this.paramList = paramList2;
        }

        public void setCenter(Point center2) {
            this.center = center2;
        }

        public void stopFrameAnimation() {
            if (EasyWakeUpView.this.mWakeUpCallback != null) {
                EasyWakeUpView.this.mWakeUpCallback.disappearTrackAnimation();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            if (EasyWakeUpView.this.mWakeLock.isHeld()) {
                EasyWakeUpView.this.mWakeLock.release();
            }
            canvas.save();
            loadBitmap();
            if (this.bitmap == null) {
                this.mIndexValue = 0;
                EasyWakeUpView.this.mIsFirst = true;
            } else if (EasyWakeUpView.this.mIsFirst) {
                canvas.drawColor(-16777216);
                postDelayedDrawAction();
            } else {
                int bitmapwidth = this.bitmap.getWidth();
                int bitmapheight = this.bitmap.getHeight();
                int i = this.mIndexValue;
                if (i < 20) {
                    int valueX = (i % 5) * (bitmapwidth / 5);
                    int valueY = (i / 5) * (bitmapheight / 4);
                    Rect src = new Rect(valueX, valueY, (bitmapwidth / 5) + valueX, (bitmapheight / 4) + valueY);
                    Log.e(EasyWakeUpView.TAG, "wake up draw mIndexValue = " + this.mIndexValue + "; Screen = " + EasyWakeUpView.this.mPowerManager.isScreenOn());
                    this.mIndexValue = this.mIndexValue + 1;
                    canvas.drawBitmap(this.bitmap, src, drawImage(canvas, bitmapwidth, bitmapheight, null), new Paint());
                    canvas.restore();
                    invalidate();
                    return;
                }
                this.mIndexValue = 0;
                Log.e(EasyWakeUpView.TAG, "wake up draw end!");
                stopFrameAnimation();
                this.bitmap.recycle();
                this.bitmap = null;
                EasyWakeUpView.this.mIsFirst = true;
            }
        }

        private void postDelayedDrawAction() {
            postDelayed(new Runnable() {
                /* class com.android.server.policy.EasyWakeUpView.GifFrameView.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.e(EasyWakeUpView.TAG, "wake up draw start!");
                    PowerManagerEx.wakeUp(EasyWakeUpView.this.mPowerManager, SystemClock.uptimeMillis(), EasyWakeUpView.TAG);
                    GifFrameView.this.invalidate();
                }
            }, 32);
            EasyWakeUpView.this.mIsFirst = false;
        }

        private Rect drawImage(Canvas canvas, int bitmapwidth, int bitmapheight, Rect dst) {
            int i = this.mIndex;
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return drawLineToRotate(canvas, i, bitmapwidth, bitmapheight);
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return drawLetterToRotate(canvas, i);
                default:
                    return dst;
            }
        }

        private void loadBitmap() {
            if (this.bitmap == null) {
                Log.d(EasyWakeUpView.TAG, "load bitmap begin");
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(), EasyWakeUpView.BITMAP_SETS[this.mIndex], options);
                int i = options.outWidth;
                int i2 = options.outHeight;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 3;
                this.bitmap = BitmapFactory.decodeResource(getResources(), EasyWakeUpView.BITMAP_SETS[this.mIndex], options);
                Log.d(EasyWakeUpView.TAG, "load bitmap end");
            }
        }

        private Rect drawLetterToRotate(Canvas canvas, int index) {
            float mRotate = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            switch (index) {
                case 4:
                case 6:
                    mRotate = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    break;
                case 5:
                    float dx = (float) (this.paramList.get(1).x - this.paramList.get(0).x);
                    float dy = (float) (this.paramList.get(1).y - this.paramList.get(0).y);
                    float angle = (float) Math.toDegrees(Math.atan((double) ((-dx) / dy)));
                    Log.d(EasyWakeUpView.TAG, "first point = (" + this.paramList.get(0).x + ", " + this.paramList.get(0).y + ") second point = (" + this.paramList.get(1).x + AwarenessInnerConstants.COMMA_KEY + this.paramList.get(1).y + ")");
                    StringBuilder sb = new StringBuilder();
                    sb.append("dx = ");
                    sb.append(dx);
                    sb.append("dy = ");
                    sb.append(dy);
                    sb.append(" angle = ");
                    sb.append(angle);
                    Log.d(EasyWakeUpView.TAG, sb.toString());
                    if (angle < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                        angle += EasyWakeUpView.ANNULAR_360_DEGREE;
                    }
                    mRotate = angle % EasyWakeUpView.ANNULAR_360_DEGREE;
                    Log.d(EasyWakeUpView.TAG, "mRotate = " + mRotate);
                    break;
                case 7:
                case 8:
                    float angle2 = (float) Math.toDegrees(Math.atan((double) (((float) (this.paramList.get(1).y - this.paramList.get(0).y)) / ((float) (this.paramList.get(1).x - this.paramList.get(0).x)))));
                    mRotate = (angle2 < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO ? angle2 + EasyWakeUpView.ANNULAR_360_DEGREE : angle2) % EasyWakeUpView.ANNULAR_360_DEGREE;
                    break;
            }
            if (this.paramList.size() <= 5) {
                Log.e(EasyWakeUpView.TAG, " drawLetterToRotate return with the size of paramList is less than 6");
                return new Rect();
            }
            Rect dst = new Rect(this.paramList.get(3).x - this.center.x, this.paramList.get(2).y - this.center.y, this.paramList.get(5).x - this.center.x, this.paramList.get(4).y - this.center.y);
            canvas.translate((float) this.center.x, (float) this.center.y);
            dst.left = (dst.left * 3) / 2;
            dst.top = (dst.top * 3) / 2;
            dst.right = (dst.right * 3) / 2;
            dst.bottom = (dst.bottom * 3) / 2;
            canvas.rotate(mRotate);
            return dst;
        }

        private Rect drawLineToRotate(Canvas canvas, int index, int bitmapwidth, int bitmapheight) {
            Rect dst = null;
            float dx = (float) (this.paramList.get(1).x - this.paramList.get(0).x);
            float dy = (float) (this.paramList.get(1).y - this.paramList.get(0).y);
            float angle = (float) Math.toDegrees(Math.atan((double) (dy / dx)));
            if (index == 0) {
                if (dx > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    dst = new Rect((this.paramList.get(0).x - (bitmapwidth / 10)) - this.center.x, this.paramList.get(1).y - this.center.y, (this.paramList.get(1).x + (bitmapwidth / 10)) - this.center.x, this.paramList.get(0).y - this.center.y);
                } else {
                    dst = new Rect((this.paramList.get(1).x - (bitmapwidth / 10)) - this.center.x, this.paramList.get(1).y - this.center.y, (this.paramList.get(0).x + (bitmapwidth / 10)) - this.center.x, this.paramList.get(0).y - this.center.y);
                }
                angle = handleAngle(index, angle);
            } else if (index == 1) {
                angle = handleAngle(index, angle);
                if (dx > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    dst = new Rect((this.paramList.get(0).x - (bitmapwidth / 10)) - this.center.x, this.paramList.get(0).y - this.center.y, (this.paramList.get(1).x + (bitmapwidth / 10)) - this.center.x, this.paramList.get(1).y - this.center.y);
                } else {
                    dst = new Rect((this.paramList.get(1).x - (bitmapwidth / 10)) - this.center.x, this.paramList.get(0).y - this.center.y, (this.paramList.get(0).x + (bitmapwidth / 10)) - this.center.x, this.paramList.get(1).y - this.center.y);
                }
            } else if (index == 2) {
                angle = handleAngle(index, angle);
                if (dy > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    dst = new Rect(this.paramList.get(1).x - this.center.x, (this.paramList.get(0).y - (bitmapheight / 8)) - this.center.y, this.paramList.get(0).x - this.center.x, (this.paramList.get(1).y + (bitmapheight / 8)) - this.center.y);
                } else {
                    dst = new Rect(this.paramList.get(1).x - this.center.x, (this.paramList.get(1).y - (bitmapheight / 8)) - this.center.y, this.paramList.get(0).x - this.center.x, (this.paramList.get(0).y + (bitmapheight / 8)) - this.center.y);
                }
            } else if (index == 3) {
                angle = handleAngle(index, angle);
                if (dy > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    dst = new Rect(this.paramList.get(0).x - this.center.x, (this.paramList.get(0).y - (bitmapheight / 8)) - this.center.y, this.paramList.get(1).x - this.center.x, (this.paramList.get(1).y + (bitmapheight / 8)) - this.center.y);
                } else {
                    dst = new Rect(this.paramList.get(0).x - this.center.x, (this.paramList.get(1).y - (bitmapheight / 8)) - this.center.y, this.paramList.get(1).x - this.center.x, (this.paramList.get(0).y + (bitmapheight / 8)) - this.center.y);
                }
            }
            float mRotate = angle % EasyWakeUpView.ANNULAR_360_DEGREE;
            canvas.translate((float) this.center.x, (float) this.center.y);
            canvas.rotate(mRotate);
            return dst;
        }

        private float handleAngle(int index, float angle) {
            float tempAngle = angle;
            if (index == 0 || index == 1) {
                if (angle > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    tempAngle = angle - EasyWakeUpView.QUATER_90_DEGREE;
                } else {
                    tempAngle = angle + EasyWakeUpView.QUATER_90_DEGREE;
                }
            }
            if ((index == 2 || index == 3) && angle < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                return angle + EasyWakeUpView.ANNULAR_360_DEGREE;
            }
            return tempAngle;
        }
    }
}
