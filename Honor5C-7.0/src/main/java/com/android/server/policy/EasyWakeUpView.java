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
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;

public class EasyWakeUpView extends FrameLayout {
    private static int[] BITMAP_SET = null;
    private static final boolean DEBUG = false;
    private static String TAG = null;
    public static final int WAKEUP_INDEX_DOUBLE_CLICK = -1;
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
        private int Index;
        private Bitmap bitmap;
        private Point center;
        private int mIndex;
        private ArrayList<Point> paramList;

        public void setParamList(ArrayList<Point> paramList) {
            this.paramList = paramList;
        }

        public GIFFrameView(Context context, int index) {
            super(context);
            this.mIndex = 0;
            this.Index = 0;
            this.bitmap = null;
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
                EasyWakeUpView.this.isfirst = EasyWakeUpView.DEBUG;
            } else {
                Paint Paint = new Paint();
                int bitmapwidth = this.bitmap.getWidth();
                int bitmapheight = this.bitmap.getHeight();
                if (this.Index < 20) {
                    int x = (this.Index % EasyWakeUpView.WAKEUP_INDEX_LETTER_C) * (bitmapwidth / EasyWakeUpView.WAKEUP_INDEX_LETTER_C);
                    int y = (this.Index / EasyWakeUpView.WAKEUP_INDEX_LETTER_C) * (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_O);
                    Rect src = new Rect(x, y, (bitmapwidth / EasyWakeUpView.WAKEUP_INDEX_LETTER_C) + x, (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_O) + y);
                    Log.e(EasyWakeUpView.TAG, "wake up draw   Index = " + this.Index + "; Screen = " + EasyWakeUpView.this.mPowerManager.isScreenOn());
                    this.Index += EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN;
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
                case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN /*1*/:
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT /*2*/:
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT /*3*/:
                    return drawLineToRotate(canvas, this.mIndex, bitmapwidth, bitmapheight);
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_O /*4*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_C /*5*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_E /*6*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_M /*7*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_W /*8*/:
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
                options.inJustDecodeBounds = EasyWakeUpView.DEBUG;
                options.inSampleSize = EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT;
                this.bitmap = BitmapFactory.decodeResource(getResources(), EasyWakeUpView.BITMAP_SET[this.mIndex], options);
            }
        }

        private Rect drawLetterToRotate(Canvas canvas, int mindex) {
            float mRotate = 0.0f;
            float angle;
            switch (mindex) {
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_O /*4*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_E /*6*/:
                    mRotate = 0.0f;
                    break;
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_C /*5*/:
                    float dy = (float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - ((Point) this.paramList.get(0)).y);
                    angle = (float) Math.toDegrees(Math.atan((double) ((-((float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - ((Point) this.paramList.get(0)).x))) / dy)));
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    mRotate = angle % 360.0f;
                    break;
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_M /*7*/:
                case EasyWakeUpView.WAKEUP_INDEX_LETTER_W /*8*/:
                    angle = (float) Math.toDegrees(Math.atan((double) (((float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - ((Point) this.paramList.get(0)).y)) / ((float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - ((Point) this.paramList.get(0)).x)))));
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    mRotate = angle % 360.0f;
                    break;
            }
            Rect dst = new Rect(((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT)).x - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT)).y - this.center.y, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_LETTER_C)).x - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_LETTER_O)).y - this.center.y);
            canvas.translate((float) this.center.x, (float) this.center.y);
            dst.left = (dst.left * EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT) / EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT;
            dst.top = (dst.top * EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT) / EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT;
            dst.right = (dst.right * EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT) / EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT;
            dst.bottom = (dst.bottom * EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT) / EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT;
            canvas.rotate(mRotate);
            return dst;
        }

        private Rect drawLineToRotate(Canvas canvas, int mindex, int bitmapwidth, int bitmapheight) {
            Rect dst = null;
            float dx = (float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - ((Point) this.paramList.get(0)).x);
            float dy = (float) (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - ((Point) this.paramList.get(0)).y);
            float angle = (float) Math.toDegrees(Math.atan((double) (dy / dx)));
            switch (mindex) {
                case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                    if (dx > 0.0f) {
                        dst = new Rect((((Point) this.paramList.get(0)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - this.center.y, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y);
                    } else {
                        dst = new Rect((((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - this.center.y, (((Point) this.paramList.get(0)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y);
                    }
                    if (angle <= 0.0f) {
                        angle += 90.0f;
                        break;
                    }
                    angle -= 90.0f;
                    break;
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN /*1*/:
                    if (angle > 0.0f) {
                        angle -= 90.0f;
                    } else {
                        angle += 90.0f;
                    }
                    if (dx <= 0.0f) {
                        dst = new Rect((((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y, (((Point) this.paramList.get(0)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - this.center.y);
                        break;
                    }
                    dst = new Rect((((Point) this.paramList.get(0)).x - (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(0)).y - this.center.y, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x + (bitmapwidth / 10)) - this.center.x, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - this.center.y);
                    break;
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_LEFT /*2*/:
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    if (dy <= 0.0f) {
                        dst = new Rect(((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - this.center.x, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y, ((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(0)).y + (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y);
                        break;
                    }
                    dst = new Rect(((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - this.center.x, (((Point) this.paramList.get(0)).y - (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y, ((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y + (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y);
                    break;
                case EasyWakeUpView.WAKEUP_INDEX_SLIP_RIGHT /*3*/:
                    if (angle < 0.0f) {
                        angle += 360.0f;
                    }
                    if (dy <= 0.0f) {
                        dst = new Rect(((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y - (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - this.center.x, (((Point) this.paramList.get(0)).y + (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y);
                        break;
                    }
                    dst = new Rect(((Point) this.paramList.get(0)).x - this.center.x, (((Point) this.paramList.get(0)).y - (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y, ((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).x - this.center.x, (((Point) this.paramList.get(EasyWakeUpView.WAKEUP_INDEX_SLIP_DOWN)).y + (bitmapheight / EasyWakeUpView.WAKEUP_INDEX_LETTER_W)) - this.center.y);
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.EasyWakeUpView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.EasyWakeUpView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.EasyWakeUpView.<clinit>():void");
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
        } else if (localList.size() < WAKEUP_INDEX_SLIP_LEFT) {
            Log.v(TAG, TAG + " startTrackAnimation and return false for  the size of pointList is less than 2");
        } else {
            removeAllViews();
            this.mWakeLock = this.mPowerManager.newWakeLock(536870913, AppHibernateCst.INVALID_PKG);
            this.mWakeLock.acquire();
            addCharView_Up(localList, index);
        }
    }

    private void addCharView_Up(ArrayList<Point> paramList, int index) {
        this.mGifFrameView.setCenter(getCenter(paramList, index));
        this.mGifFrameView.setParamList(paramList);
        addView(this.mGifFrameView, new LayoutParams(WAKEUP_INDEX_DOUBLE_CLICK, WAKEUP_INDEX_DOUBLE_CLICK));
    }

    private Point getCenter(ArrayList<Point> pointList, int index) {
        Point center = new Point();
        if (index >= 0 && index <= WAKEUP_INDEX_SLIP_RIGHT) {
            center.x = (((Point) pointList.get(0)).x + ((Point) pointList.get(WAKEUP_INDEX_SLIP_DOWN)).x) / WAKEUP_INDEX_SLIP_LEFT;
            center.y = (((Point) pointList.get(0)).y + ((Point) pointList.get(WAKEUP_INDEX_SLIP_DOWN)).y) / WAKEUP_INDEX_SLIP_LEFT;
        } else if (index >= WAKEUP_INDEX_LETTER_O) {
            center.x = (((Point) pointList.get(WAKEUP_INDEX_LETTER_C)).x + ((Point) pointList.get(WAKEUP_INDEX_SLIP_RIGHT)).x) / WAKEUP_INDEX_SLIP_LEFT;
            center.y = (((Point) pointList.get(WAKEUP_INDEX_LETTER_O)).y + ((Point) pointList.get(WAKEUP_INDEX_SLIP_LEFT)).y) / WAKEUP_INDEX_SLIP_LEFT;
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
