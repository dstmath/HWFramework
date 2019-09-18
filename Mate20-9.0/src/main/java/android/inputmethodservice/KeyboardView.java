package android.inputmethodservice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardView extends View implements View.OnClickListener {
    private static final int DEBOUNCE_TIME = 70;
    private static final boolean DEBUG = false;
    private static final int DELAY_AFTER_PREVIEW = 70;
    private static final int DELAY_BEFORE_PREVIEW = 0;
    private static final int[] KEY_DELETE = {-5};
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int[] LONG_PRESSABLE_STATE_SET = {16843324};
    private static int MAX_NEARBY_KEYS = 12;
    private static final int MSG_LONGPRESS = 4;
    private static final int MSG_REMOVE_PREVIEW = 2;
    private static final int MSG_REPEAT = 3;
    private static final int MSG_SHOW_PREVIEW = 1;
    private static final int MULTITAP_INTERVAL = 800;
    private static final int NOT_A_KEY = -1;
    private static final int REPEAT_INTERVAL = 50;
    private static final int REPEAT_START_DELAY = 400;
    private boolean mAbortKey;
    private AccessibilityManager mAccessibilityManager;
    private AudioManager mAudioManager;
    private float mBackgroundDimAmount;
    private Bitmap mBuffer;
    private Canvas mCanvas;
    private Rect mClipRegion;
    private final int[] mCoordinates;
    private int mCurrentKey;
    private int mCurrentKeyIndex;
    private long mCurrentKeyTime;
    private Rect mDirtyRect;
    /* access modifiers changed from: private */
    public boolean mDisambiguateSwipe;
    private int[] mDistances;
    /* access modifiers changed from: private */
    public int mDownKey;
    private long mDownTime;
    private boolean mDrawPending;
    private GestureDetector mGestureDetector;
    Handler mHandler;
    private boolean mHeadsetRequiredToHearPasswordsAnnounced;
    private boolean mInMultiTap;
    private Keyboard.Key mInvalidatedKey;
    private Drawable mKeyBackground;
    private int[] mKeyIndices;
    private int mKeyTextColor;
    private int mKeyTextSize;
    private Keyboard mKeyboard;
    /* access modifiers changed from: private */
    public OnKeyboardActionListener mKeyboardActionListener;
    private boolean mKeyboardChanged;
    private Keyboard.Key[] mKeys;
    private int mLabelTextSize;
    private int mLastCodeX;
    private int mLastCodeY;
    private int mLastKey;
    private long mLastKeyTime;
    private long mLastMoveTime;
    private int mLastSentIndex;
    private long mLastTapTime;
    private int mLastX;
    private int mLastY;
    private KeyboardView mMiniKeyboard;
    private Map<Keyboard.Key, View> mMiniKeyboardCache;
    private View mMiniKeyboardContainer;
    private int mMiniKeyboardOffsetX;
    private int mMiniKeyboardOffsetY;
    private boolean mMiniKeyboardOnScreen;
    private int mOldPointerCount;
    private float mOldPointerX;
    private float mOldPointerY;
    private Rect mPadding;
    private Paint mPaint;
    private PopupWindow mPopupKeyboard;
    private int mPopupLayout;
    private View mPopupParent;
    private int mPopupPreviewX;
    private int mPopupPreviewY;
    private int mPopupX;
    private int mPopupY;
    /* access modifiers changed from: private */
    public boolean mPossiblePoly;
    private boolean mPreviewCentered;
    private int mPreviewHeight;
    private StringBuilder mPreviewLabel;
    private int mPreviewOffset;
    private PopupWindow mPreviewPopup;
    /* access modifiers changed from: private */
    public TextView mPreviewText;
    private int mPreviewTextSizeLarge;
    private boolean mProximityCorrectOn;
    private int mProximityThreshold;
    private int mRepeatKeyIndex;
    private int mShadowColor;
    private float mShadowRadius;
    private boolean mShowPreview;
    private boolean mShowTouchPoints;
    /* access modifiers changed from: private */
    public int mStartX;
    /* access modifiers changed from: private */
    public int mStartY;
    /* access modifiers changed from: private */
    public int mSwipeThreshold;
    /* access modifiers changed from: private */
    public SwipeTracker mSwipeTracker;
    private int mTapCount;
    private int mVerticalCorrection;

    public interface OnKeyboardActionListener {
        void onKey(int i, int[] iArr);

        void onPress(int i);

        void onRelease(int i);

        void onText(CharSequence charSequence);

        void swipeDown();

        void swipeLeft();

        void swipeRight();

        void swipeUp();
    }

    private static class SwipeTracker {
        static final int LONGEST_PAST_TIME = 200;
        static final int NUM_PAST = 4;
        final long[] mPastTime;
        final float[] mPastX;
        final float[] mPastY;
        float mXVelocity;
        float mYVelocity;

        private SwipeTracker() {
            this.mPastX = new float[4];
            this.mPastY = new float[4];
            this.mPastTime = new long[4];
        }

        public void clear() {
            this.mPastTime[0] = 0;
        }

        public void addMovement(MotionEvent ev) {
            long time = ev.getEventTime();
            int N = ev.getHistorySize();
            for (int i = 0; i < N; i++) {
                addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i), ev.getHistoricalEventTime(i));
            }
            addPoint(ev.getX(), ev.getY(), time);
        }

        private void addPoint(float x, float y, long time) {
            long[] pastTime = this.mPastTime;
            int drop = -1;
            int i = 0;
            while (i < 4 && pastTime[i] != 0) {
                if (pastTime[i] < time - 200) {
                    drop = i;
                }
                i++;
            }
            if (i == 4 && drop < 0) {
                drop = 0;
            }
            if (drop == i) {
                drop--;
            }
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            if (drop >= 0) {
                int start = drop + 1;
                int count = (4 - drop) - 1;
                System.arraycopy(pastX, start, pastX, 0, count);
                System.arraycopy(pastY, start, pastY, 0, count);
                System.arraycopy(pastTime, start, pastTime, 0, count);
                i -= drop + 1;
            }
            pastX[i] = x;
            pastY[i] = y;
            pastTime[i] = time;
            int i2 = i + 1;
            if (i2 < 4) {
                pastTime[i2] = 0;
            }
        }

        public void computeCurrentVelocity(int units) {
            computeCurrentVelocity(units, Float.MAX_VALUE);
        }

        public void computeCurrentVelocity(int units, float maxVelocity) {
            float f;
            float f2;
            long[] pastTime;
            float[] pastX;
            float f3;
            int i = units;
            float f4 = maxVelocity;
            float[] pastX2 = this.mPastX;
            float[] pastY = this.mPastY;
            long[] pastTime2 = this.mPastTime;
            int N = 0;
            float oldestX = pastX2[0];
            float oldestY = pastY[0];
            long oldestTime = pastTime2[0];
            float accumX = 0.0f;
            float accumY = 0.0f;
            while (N < 4 && pastTime2[N] != 0) {
                N++;
            }
            int i2 = 1;
            while (i2 < N) {
                int dur = (int) (pastTime2[i2] - oldestTime);
                if (dur == 0) {
                    pastX = pastX2;
                    pastTime = pastTime2;
                } else {
                    pastX = pastX2;
                    pastTime = pastTime2;
                    float vel = ((pastX2[i2] - oldestX) / ((float) dur)) * ((float) i);
                    if (accumX == 0.0f) {
                        accumX = vel;
                    } else {
                        accumX = (accumX + vel) * 0.5f;
                    }
                    float vel2 = ((pastY[i2] - oldestY) / ((float) dur)) * ((float) i);
                    if (accumY == 0.0f) {
                        f3 = vel2;
                    } else {
                        f3 = (accumY + vel2) * 0.5f;
                    }
                    accumY = f3;
                }
                i2++;
                pastX2 = pastX;
                pastTime2 = pastTime;
            }
            long[] jArr = pastTime2;
            if (accumX < 0.0f) {
                f = Math.max(accumX, -f4);
            } else {
                f = Math.min(accumX, f4);
            }
            this.mXVelocity = f;
            if (accumY < 0.0f) {
                f2 = Math.max(accumY, -f4);
            } else {
                f2 = Math.min(accumY, f4);
            }
            this.mYVelocity = f2;
        }

        public float getXVelocity() {
            return this.mXVelocity;
        }

        public float getYVelocity() {
            return this.mYVelocity;
        }
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Context context2 = context;
        this.mCurrentKeyIndex = -1;
        this.mCoordinates = new int[2];
        this.mPreviewCentered = false;
        this.mShowPreview = true;
        this.mShowTouchPoints = true;
        this.mCurrentKey = -1;
        this.mDownKey = -1;
        this.mKeyIndices = new int[12];
        this.mRepeatKeyIndex = -1;
        this.mClipRegion = new Rect(0, 0, 0, 0);
        this.mSwipeTracker = new SwipeTracker();
        this.mOldPointerCount = 1;
        this.mDistances = new int[MAX_NEARBY_KEYS];
        this.mPreviewLabel = new StringBuilder(1);
        this.mDirtyRect = new Rect();
        TypedArray a = context2.obtainStyledAttributes(attrs, android.R.styleable.KeyboardView, defStyleAttr, defStyleRes);
        LayoutInflater inflate = (LayoutInflater) context2.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int n = a.getIndexCount();
        int previewLayout = 0;
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mShadowColor = a.getColor(attr, 0);
                    break;
                case 1:
                    this.mShadowRadius = a.getFloat(attr, 0.0f);
                    break;
                case 2:
                    this.mKeyBackground = a.getDrawable(attr);
                    break;
                case 3:
                    this.mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                    break;
                case 4:
                    this.mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                case 5:
                    this.mKeyTextColor = a.getColor(attr, -16777216);
                    break;
                case 6:
                    previewLayout = a.getResourceId(attr, 0);
                    break;
                case 7:
                    this.mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 8:
                    this.mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                    break;
                case 9:
                    this.mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 10:
                    this.mPopupLayout = a.getResourceId(attr, 0);
                    break;
            }
        }
        this.mBackgroundDimAmount = this.mContext.obtainStyledAttributes(R.styleable.Theme).getFloat(2, 0.5f);
        this.mPreviewPopup = new PopupWindow(context2);
        this.mPreviewPopup.setEnterTransition(null);
        this.mPreviewPopup.setExitTransition(null);
        if (previewLayout != 0) {
            this.mPreviewText = (TextView) inflate.inflate(previewLayout, null);
            this.mPreviewTextSizeLarge = (int) this.mPreviewText.getTextSize();
            this.mPreviewPopup.setContentView(this.mPreviewText);
            this.mPreviewPopup.setBackgroundDrawable(null);
        } else {
            this.mShowPreview = false;
        }
        this.mPreviewPopup.setTouchable(false);
        this.mPopupKeyboard = new PopupWindow(context2);
        this.mPopupKeyboard.setBackgroundDrawable(null);
        this.mPopupParent = this;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize((float) 0);
        this.mPaint.setTextAlign(Paint.Align.CENTER);
        this.mPaint.setAlpha(255);
        this.mPadding = new Rect(0, 0, 0, 0);
        this.mMiniKeyboardCache = new HashMap();
        this.mKeyBackground.getPadding(this.mPadding);
        this.mSwipeThreshold = (int) (500.0f * getResources().getDisplayMetrics().density);
        this.mDisambiguateSwipe = getResources().getBoolean(R.bool.config_swipeDisambiguation);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAudioManager = (AudioManager) context2.getSystemService("audio");
        resetMultiTap();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initGestureDetector();
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            KeyboardView.this.showKey(msg.arg1);
                            return;
                        case 2:
                            KeyboardView.this.mPreviewText.setVisibility(4);
                            return;
                        case 3:
                            if (KeyboardView.this.repeatKey()) {
                                sendMessageDelayed(Message.obtain(this, 3), 50);
                                return;
                            }
                            return;
                        case 4:
                            boolean unused = KeyboardView.this.openPopupIfRequired((MotionEvent) msg.obj);
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                    if (KeyboardView.this.mPossiblePoly) {
                        return false;
                    }
                    float absX = Math.abs(velocityX);
                    float absY = Math.abs(velocityY);
                    float deltaX = me2.getX() - me1.getX();
                    float deltaY = me2.getY() - me1.getY();
                    int travelX = KeyboardView.this.getWidth() / 2;
                    int travelY = KeyboardView.this.getHeight() / 2;
                    KeyboardView.this.mSwipeTracker.computeCurrentVelocity(1000);
                    float endingVelocityX = KeyboardView.this.mSwipeTracker.getXVelocity();
                    float endingVelocityY = KeyboardView.this.mSwipeTracker.getYVelocity();
                    boolean sendDownKey = false;
                    if (velocityX <= ((float) KeyboardView.this.mSwipeThreshold) || absY >= absX || deltaX <= ((float) travelX)) {
                        if (velocityX >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absY >= absX || deltaX >= ((float) (-travelX))) {
                            if (velocityY >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absX >= absY || deltaY >= ((float) (-travelY))) {
                                if (velocityY > ((float) KeyboardView.this.mSwipeThreshold) && absX < absY / 2.0f && deltaY > ((float) travelY)) {
                                    if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY >= velocityY / 4.0f) {
                                        KeyboardView.this.swipeDown();
                                        return true;
                                    }
                                    sendDownKey = true;
                                }
                            } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY <= velocityY / 4.0f) {
                                KeyboardView.this.swipeUp();
                                return true;
                            } else {
                                sendDownKey = true;
                            }
                        } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX <= velocityX / 4.0f) {
                            KeyboardView.this.swipeLeft();
                            return true;
                        } else {
                            sendDownKey = true;
                        }
                    } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX >= velocityX / 4.0f) {
                        KeyboardView.this.swipeRight();
                        return true;
                    } else {
                        sendDownKey = true;
                    }
                    if (sendDownKey) {
                        KeyboardView.this.detectAndSendKey(KeyboardView.this.mDownKey, KeyboardView.this.mStartX, KeyboardView.this.mStartY, me1.getEventTime());
                    }
                    return false;
                }
            });
            this.mGestureDetector.setIsLongpressEnabled(false);
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        this.mKeyboardActionListener = listener;
    }

    /* access modifiers changed from: protected */
    public OnKeyboardActionListener getOnKeyboardActionListener() {
        return this.mKeyboardActionListener;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (this.mKeyboard != null) {
            showPreview(-1);
        }
        removeMessages();
        this.mKeyboard = keyboard;
        List<Keyboard.Key> keys = this.mKeyboard.getKeys();
        this.mKeys = (Keyboard.Key[]) keys.toArray(new Keyboard.Key[keys.size()]);
        requestLayout();
        this.mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        this.mMiniKeyboardCache.clear();
        this.mAbortKey = true;
    }

    public Keyboard getKeyboard() {
        return this.mKeyboard;
    }

    public boolean setShifted(boolean shifted) {
        if (this.mKeyboard == null || !this.mKeyboard.setShifted(shifted)) {
            return false;
        }
        invalidateAllKeys();
        return true;
    }

    public boolean isShifted() {
        if (this.mKeyboard != null) {
            return this.mKeyboard.isShifted();
        }
        return false;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.mShowPreview = previewEnabled;
    }

    public boolean isPreviewEnabled() {
        return this.mShowPreview;
    }

    public void setVerticalCorrection(int verticalOffset) {
    }

    public void setPopupParent(View v) {
        this.mPopupParent = v;
    }

    public void setPopupOffset(int x, int y) {
        this.mMiniKeyboardOffsetX = x;
        this.mMiniKeyboardOffsetY = y;
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        this.mProximityCorrectOn = enabled;
    }

    public boolean isProximityCorrectionEnabled() {
        return this.mProximityCorrectOn;
    }

    public void onClick(View v) {
        dismissPopupKeyboard();
    }

    private CharSequence adjustCase(CharSequence label) {
        if (!this.mKeyboard.isShifted() || label == null || label.length() >= 3 || !Character.isLowerCase(label.charAt(0))) {
            return label;
        }
        return label.toString().toUpperCase();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mKeyboard == null) {
            setMeasuredDimension(this.mPaddingLeft + this.mPaddingRight, this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        int width = this.mKeyboard.getMinWidth() + this.mPaddingLeft + this.mPaddingRight;
        if (View.MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(width, this.mKeyboard.getHeight() + this.mPaddingTop + this.mPaddingBottom);
    }

    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard != null) {
            Keyboard.Key[] keys = this.mKeys;
            if (keys != null) {
                int dimensionSum = 0;
                for (Keyboard.Key key : keys) {
                    dimensionSum += Math.min(key.width, key.height) + key.gap;
                }
                if (dimensionSum >= 0 && length != 0) {
                    this.mProximityThreshold = (int) ((((float) dimensionSum) * 1.4f) / ((float) length));
                    this.mProximityThreshold *= this.mProximityThreshold;
                }
            }
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mKeyboard != null) {
            this.mKeyboard.resize(w, h);
        }
        this.mBuffer = null;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawPending || this.mBuffer == null || this.mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(this.mBuffer, 0.0f, 0.0f, (Paint) null);
    }

    private void onBufferDraw() {
        Keyboard.Key[] keys;
        Keyboard.Key invalidKey;
        int keyCount;
        if (this.mBuffer == null || this.mKeyboardChanged) {
            if (this.mBuffer == null || (this.mKeyboardChanged && !(this.mBuffer.getWidth() == getWidth() && this.mBuffer.getHeight() == getHeight()))) {
                this.mBuffer = Bitmap.createBitmap(Math.max(1, getWidth()), Math.max(1, getHeight()), Bitmap.Config.ARGB_8888);
                this.mCanvas = new Canvas(this.mBuffer);
            }
            invalidateAllKeys();
            this.mKeyboardChanged = false;
        }
        if (this.mKeyboard != null) {
            this.mCanvas.save();
            Canvas canvas = this.mCanvas;
            canvas.clipRect(this.mDirtyRect);
            Paint paint = this.mPaint;
            Drawable keyBackground = this.mKeyBackground;
            Rect clipRegion = this.mClipRegion;
            Rect padding = this.mPadding;
            int kbdPaddingLeft = this.mPaddingLeft;
            int kbdPaddingTop = this.mPaddingTop;
            Keyboard.Key[] keys2 = this.mKeys;
            Keyboard.Key invalidKey2 = this.mInvalidatedKey;
            paint.setColor(this.mKeyTextColor);
            boolean drawSingleKey = false;
            if (invalidKey2 != null && canvas.getClipBounds(clipRegion) && (invalidKey2.x + kbdPaddingLeft) - 1 <= clipRegion.left && (invalidKey2.y + kbdPaddingTop) - 1 <= clipRegion.top && invalidKey2.x + invalidKey2.width + kbdPaddingLeft + 1 >= clipRegion.right && invalidKey2.y + invalidKey2.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
            boolean drawSingleKey2 = drawSingleKey;
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int keyCount2 = keys2.length;
            int i = 0;
            while (i < keyCount2) {
                Keyboard.Key key = keys2[i];
                if (!drawSingleKey2 || invalidKey2 == key) {
                    int[] drawableState = key.getCurrentDrawableState();
                    keyBackground.setState(drawableState);
                    String label = key.label == null ? null : adjustCase(key.label).toString();
                    Rect bounds = keyBackground.getBounds();
                    int[] iArr = drawableState;
                    keyCount = keyCount2;
                    if (key.width == bounds.right && key.height == bounds.bottom) {
                        Rect rect = bounds;
                    } else {
                        Rect rect2 = bounds;
                        keyBackground.setBounds(0, 0, key.width, key.height);
                    }
                    canvas.translate((float) (key.x + kbdPaddingLeft), (float) (key.y + kbdPaddingTop));
                    keyBackground.draw(canvas);
                    if (label != null) {
                        if (label.length() <= 1 || key.codes.length >= 2) {
                            paint.setTextSize((float) this.mKeyTextSize);
                            paint.setTypeface(Typeface.DEFAULT);
                        } else {
                            paint.setTextSize((float) this.mLabelTextSize);
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                        }
                        paint.setShadowLayer(this.mShadowRadius, 0.0f, 0.0f, this.mShadowColor);
                        canvas.drawText(label, (float) ((((key.width - padding.left) - padding.right) / 2) + padding.left), ((float) (((key.height - padding.top) - padding.bottom) / 2)) + ((paint.getTextSize() - paint.descent()) / 2.0f) + ((float) padding.top), paint);
                        paint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        String str = label;
                        invalidKey = invalidKey2;
                        keys = keys2;
                    } else if (key.icon != null) {
                        int drawableX = ((((key.width - padding.left) - padding.right) - key.icon.getIntrinsicWidth()) / 2) + padding.left;
                        int drawableY = ((((key.height - padding.top) - padding.bottom) - key.icon.getIntrinsicHeight()) / 2) + padding.top;
                        String str2 = label;
                        canvas.translate((float) drawableX, (float) drawableY);
                        invalidKey = invalidKey2;
                        keys = keys2;
                        key.icon.setBounds(0, 0, key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                        key.icon.draw(canvas);
                        canvas.translate((float) (-drawableX), (float) (-drawableY));
                    } else {
                        invalidKey = invalidKey2;
                        keys = keys2;
                    }
                    canvas.translate((float) ((-key.x) - kbdPaddingLeft), (float) ((-key.y) - kbdPaddingTop));
                } else {
                    keyCount = keyCount2;
                    invalidKey = invalidKey2;
                    keys = keys2;
                }
                i++;
                keyCount2 = keyCount;
                invalidKey2 = invalidKey;
                keys2 = keys;
            }
            int keyCount3 = keyCount2;
            Keyboard.Key invalidKey3 = invalidKey2;
            Keyboard.Key[] keys3 = keys2;
            this.mInvalidatedKey = null;
            if (this.mMiniKeyboardOnScreen) {
                paint.setColor(((int) (this.mBackgroundDimAmount * 255.0f)) << 24);
                int i2 = keyCount3;
                Keyboard.Key key2 = invalidKey3;
                Keyboard.Key[] keyArr = keys3;
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), paint);
            } else {
                Keyboard.Key key3 = invalidKey3;
                Keyboard.Key[] keyArr2 = keys3;
            }
            this.mCanvas.restore();
            this.mDrawPending = false;
            this.mDirtyRect.setEmpty();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0048, code lost:
        if (r12.codes[0] <= 32) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004a, code lost:
        r10 = r12.codes.length;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004d, code lost:
        if (r13 >= r11) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004f, code lost:
        r11 = r13;
        r7 = r8[r5];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0052, code lost:
        if (r3 != null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0054, code lost:
        r16 = r4;
        r17 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0059, code lost:
        r15 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005d, code lost:
        if (r15 >= r0.mDistances.length) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0063, code lost:
        if (r0.mDistances[r15] <= r13) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0065, code lost:
        r16 = r4;
        r17 = r6;
        java.lang.System.arraycopy(r0.mDistances, r15, r0.mDistances, r15 + r10, (r0.mDistances.length - r15) - r10);
        java.lang.System.arraycopy(r3, r15, r3, r15 + r10, (r3.length - r15) - r10);
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        if (r1 >= r10) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0082, code lost:
        r3[r15 + r1] = r12.codes[r1];
        r0.mDistances[r15 + r1] = r13;
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        r16 = r4;
        r17 = r6;
        r15 = r15 + 1;
        r1 = r19;
        r2 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x009f, code lost:
        r16 = r4;
        r17 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a3, code lost:
        r5 = r5 + 1;
        r4 = r16;
        r6 = r17;
        r1 = r19;
        r2 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003d, code lost:
        if (r15 >= r0.mProximityThreshold) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x003f, code lost:
        if (r14 != false) goto L_0x0041;
     */
    private int getKeyIndices(int x, int y, int[] allKeys) {
        int i = x;
        int i2 = y;
        int[] iArr = allKeys;
        Keyboard.Key[] keys = this.mKeys;
        Arrays.fill(this.mDistances, Integer.MAX_VALUE);
        int[] nearestKeyIndices = this.mKeyboard.getNearestKeys(i, i2);
        int keyCount = nearestKeyIndices.length;
        int closestKeyDist = this.mProximityThreshold + 1;
        int closestKey = -1;
        int primaryIndex = -1;
        int i3 = 0;
        while (i3 < keyCount) {
            Keyboard.Key key = keys[nearestKeyIndices[i3]];
            int dist = 0;
            boolean isInside = key.isInside(i, i2);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i3];
            }
            if (this.mProximityCorrectOn) {
                int squaredDistanceFrom = key.squaredDistanceFrom(i, i2);
                dist = squaredDistanceFrom;
            }
        }
        if (primaryIndex == -1) {
            return closestKey;
        }
        return primaryIndex;
    }

    /* access modifiers changed from: private */
    public void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != -1 && index < this.mKeys.length) {
            Keyboard.Key key = this.mKeys[index];
            if (key.text != null) {
                this.mKeyboardActionListener.onText(key.text);
                this.mKeyboardActionListener.onRelease(-1);
            } else {
                int code = key.codes[0];
                int[] codes = new int[MAX_NEARBY_KEYS];
                Arrays.fill(codes, -1);
                getKeyIndices(x, y, codes);
                if (this.mInMultiTap) {
                    if (this.mTapCount != -1) {
                        this.mKeyboardActionListener.onKey(-5, KEY_DELETE);
                    } else {
                        this.mTapCount = 0;
                    }
                    code = key.codes[this.mTapCount];
                }
                this.mKeyboardActionListener.onKey(code, codes);
                this.mKeyboardActionListener.onRelease(code);
            }
            this.mLastSentIndex = index;
            this.mLastTapTime = eventTime;
        }
    }

    private CharSequence getPreviewText(Keyboard.Key key) {
        if (!this.mInMultiTap) {
            return adjustCase(key.label);
        }
        int i = 0;
        this.mPreviewLabel.setLength(0);
        StringBuilder sb = this.mPreviewLabel;
        int[] iArr = key.codes;
        if (this.mTapCount >= 0) {
            i = this.mTapCount;
        }
        sb.append((char) iArr[i]);
        return adjustCase(this.mPreviewLabel);
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = this.mCurrentKeyIndex;
        PopupWindow previewPopup = this.mPreviewPopup;
        this.mCurrentKeyIndex = keyIndex;
        Keyboard.Key[] keys = this.mKeys;
        if (oldKeyIndex != this.mCurrentKeyIndex) {
            if (oldKeyIndex != -1 && keys.length > oldKeyIndex) {
                Keyboard.Key oldKey = keys[oldKeyIndex];
                oldKey.onReleased(this.mCurrentKeyIndex == -1);
                invalidateKey(oldKeyIndex);
                int keyCode = oldKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(256, keyCode);
                sendAccessibilityEventForUnicodeCharacter(65536, keyCode);
            }
            if (this.mCurrentKeyIndex != -1 && keys.length > this.mCurrentKeyIndex) {
                Keyboard.Key newKey = keys[this.mCurrentKeyIndex];
                newKey.onPressed();
                invalidateKey(this.mCurrentKeyIndex);
                int keyCode2 = newKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(128, keyCode2);
                sendAccessibilityEventForUnicodeCharacter(32768, keyCode2);
            }
        }
        if (oldKeyIndex != this.mCurrentKeyIndex && this.mShowPreview) {
            this.mHandler.removeMessages(1);
            if (previewPopup.isShowing() && keyIndex == -1) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 70);
            }
            if (keyIndex == -1) {
                return;
            }
            if (!previewPopup.isShowing() || this.mPreviewText.getVisibility() != 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, keyIndex, 0), 0);
            } else {
                showKey(keyIndex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showKey(int keyIndex) {
        PopupWindow previewPopup = this.mPreviewPopup;
        Keyboard.Key[] keys = this.mKeys;
        if (keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Keyboard.Key key = keys[keyIndex];
            if (key.icon != null) {
                this.mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
                this.mPreviewText.setText(null);
            } else {
                this.mPreviewText.setCompoundDrawables(null, null, null, null);
                this.mPreviewText.setText(getPreviewText(key));
                if (key.label.length() <= 1 || key.codes.length >= 2) {
                    this.mPreviewText.setTextSize(0, (float) this.mPreviewTextSizeLarge);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT);
                } else {
                    this.mPreviewText.setTextSize(0, (float) this.mKeyTextSize);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
            this.mPreviewText.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
            int popupWidth = Math.max(this.mPreviewText.getMeasuredWidth(), key.width + this.mPreviewText.getPaddingLeft() + this.mPreviewText.getPaddingRight());
            int popupHeight = this.mPreviewHeight;
            ViewGroup.LayoutParams lp = this.mPreviewText.getLayoutParams();
            if (lp != null) {
                lp.width = popupWidth;
                lp.height = popupHeight;
            }
            if (!this.mPreviewCentered) {
                this.mPopupPreviewX = (key.x - this.mPreviewText.getPaddingLeft()) + this.mPaddingLeft;
                this.mPopupPreviewY = (key.y - popupHeight) + this.mPreviewOffset;
            } else {
                this.mPopupPreviewX = 160 - (this.mPreviewText.getMeasuredWidth() / 2);
                this.mPopupPreviewY = -this.mPreviewText.getMeasuredHeight();
            }
            this.mHandler.removeMessages(2);
            getLocationInWindow(this.mCoordinates);
            int[] iArr = this.mCoordinates;
            iArr[0] = iArr[0] + this.mMiniKeyboardOffsetX;
            int[] iArr2 = this.mCoordinates;
            iArr2[1] = iArr2[1] + this.mMiniKeyboardOffsetY;
            this.mPreviewText.getBackground().setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
            this.mPopupPreviewX += this.mCoordinates[0];
            this.mPopupPreviewY += this.mCoordinates[1];
            getLocationOnScreen(this.mCoordinates);
            if (this.mPopupPreviewY + this.mCoordinates[1] < 0) {
                if (key.x + key.width <= getWidth() / 2) {
                    this.mPopupPreviewX += (int) (((double) key.width) * 2.5d);
                } else {
                    this.mPopupPreviewX -= (int) (((double) key.width) * 2.5d);
                }
                this.mPopupPreviewY += popupHeight;
            }
            if (previewPopup.isShowing()) {
                previewPopup.update(this.mPopupPreviewX, this.mPopupPreviewY, popupWidth, popupHeight);
            } else {
                previewPopup.setWidth(popupWidth);
                previewPopup.setHeight(popupHeight);
                previewPopup.showAtLocation(this.mPopupParent, 0, this.mPopupPreviewX, this.mPopupPreviewY);
            }
            this.mPreviewText.setVisibility(0);
        }
    }

    private void sendAccessibilityEventForUnicodeCharacter(int eventType, int code) {
        String text;
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            onInitializeAccessibilityEvent(event);
            if (code != 10) {
                switch (code) {
                    case -6:
                        text = this.mContext.getString(R.string.keyboardview_keycode_alt);
                        break;
                    case -5:
                        text = this.mContext.getString(R.string.keyboardview_keycode_delete);
                        break;
                    case -4:
                        text = this.mContext.getString(R.string.keyboardview_keycode_done);
                        break;
                    case -3:
                        text = this.mContext.getString(R.string.keyboardview_keycode_cancel);
                        break;
                    case -2:
                        text = this.mContext.getString(R.string.keyboardview_keycode_mode_change);
                        break;
                    case -1:
                        text = this.mContext.getString(R.string.keyboardview_keycode_shift);
                        break;
                    default:
                        text = String.valueOf((char) code);
                        break;
                }
            } else {
                text = this.mContext.getString(R.string.keyboardview_keycode_enter);
            }
            event.getText().add(text);
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void invalidateAllKeys() {
        this.mDirtyRect.union(0, 0, getWidth(), getHeight());
        this.mDrawPending = true;
        invalidate();
    }

    public void invalidateKey(int keyIndex) {
        if (this.mKeys != null && keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Keyboard.Key key = this.mKeys[keyIndex];
            this.mInvalidatedKey = key;
            this.mDirtyRect.union(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, key.x + key.width + this.mPaddingLeft, key.y + key.height + this.mPaddingTop);
            onBufferDraw();
            invalidate(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, key.x + key.width + this.mPaddingLeft, key.y + key.height + this.mPaddingTop);
        }
    }

    /* access modifiers changed from: private */
    public boolean openPopupIfRequired(MotionEvent me) {
        if (this.mPopupLayout == 0 || this.mCurrentKey < 0 || this.mCurrentKey >= this.mKeys.length) {
            return false;
        }
        boolean result = onLongPress(this.mKeys[this.mCurrentKey]);
        if (result) {
            this.mAbortKey = true;
            showPreview(-1);
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean onLongPress(Keyboard.Key popupKey) {
        Keyboard keyboard;
        int popupKeyboardId = popupKey.popupResId;
        if (popupKeyboardId == 0) {
            return false;
        }
        this.mMiniKeyboardContainer = this.mMiniKeyboardCache.get(popupKey);
        if (this.mMiniKeyboardContainer == null) {
            this.mMiniKeyboardContainer = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPopupLayout, null);
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(16908326);
            View closeButton = this.mMiniKeyboardContainer.findViewById(16908327);
            if (closeButton != null) {
                closeButton.setOnClickListener(this);
            }
            this.mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                public void onKey(int primaryCode, int[] keyCodes) {
                    KeyboardView.this.mKeyboardActionListener.onKey(primaryCode, keyCodes);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                public void onText(CharSequence text) {
                    KeyboardView.this.mKeyboardActionListener.onText(text);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                public void swipeLeft() {
                }

                public void swipeRight() {
                }

                public void swipeUp() {
                }

                public void swipeDown() {
                }

                public void onPress(int primaryCode) {
                    KeyboardView.this.mKeyboardActionListener.onPress(primaryCode);
                }

                public void onRelease(int primaryCode) {
                    KeyboardView.this.mKeyboardActionListener.onRelease(primaryCode);
                }
            });
            if (popupKey.popupCharacters != null) {
                keyboard = new Keyboard(getContext(), popupKeyboardId, popupKey.popupCharacters, -1, getPaddingLeft() + getPaddingRight());
            } else {
                keyboard = new Keyboard(getContext(), popupKeyboardId);
            }
            this.mMiniKeyboard.setKeyboard(keyboard);
            this.mMiniKeyboard.setPopupParent(this);
            this.mMiniKeyboardContainer.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getHeight(), Integer.MIN_VALUE));
            this.mMiniKeyboardCache.put(popupKey, this.mMiniKeyboardContainer);
        } else {
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(16908326);
        }
        getLocationInWindow(this.mCoordinates);
        this.mPopupX = popupKey.x + this.mPaddingLeft;
        this.mPopupY = popupKey.y + this.mPaddingTop;
        this.mPopupX = (this.mPopupX + popupKey.width) - this.mMiniKeyboardContainer.getMeasuredWidth();
        this.mPopupY -= this.mMiniKeyboardContainer.getMeasuredHeight();
        int x = this.mPopupX + this.mMiniKeyboardContainer.getPaddingRight() + this.mCoordinates[0];
        int y = this.mPopupY + this.mMiniKeyboardContainer.getPaddingBottom() + this.mCoordinates[1];
        this.mMiniKeyboard.setPopupOffset(x < 0 ? 0 : x, y);
        this.mMiniKeyboard.setShifted(isShifted());
        this.mPopupKeyboard.setContentView(this.mMiniKeyboardContainer);
        this.mPopupKeyboard.setWidth(this.mMiniKeyboardContainer.getMeasuredWidth());
        this.mPopupKeyboard.setHeight(this.mMiniKeyboardContainer.getMeasuredHeight());
        this.mPopupKeyboard.showAtLocation(this, 0, x, y);
        this.mMiniKeyboardOnScreen = true;
        invalidateAllKeys();
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (!this.mAccessibilityManager.isTouchExplorationEnabled() || event.getPointerCount() != 1) {
            return true;
        }
        int action = event.getAction();
        if (action != 7) {
            switch (action) {
                case 9:
                    event.setAction(0);
                    break;
                case 10:
                    event.setAction(1);
                    break;
            }
        } else {
            event.setAction(2);
        }
        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent me) {
        boolean result;
        MotionEvent motionEvent = me;
        int pointerCount = me.getPointerCount();
        int action = me.getAction();
        long now = me.getEventTime();
        if (pointerCount != this.mOldPointerCount) {
            if (pointerCount == 1) {
                MotionEvent down = MotionEvent.obtain(now, now, 0, me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, false);
                down.recycle();
                if (action == 1) {
                    result = onModifiedTouchEvent(motionEvent, true);
                }
            } else {
                MotionEvent up = MotionEvent.obtain(now, now, 1, this.mOldPointerX, this.mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else if (pointerCount == 1) {
            result = onModifiedTouchEvent(motionEvent, false);
            this.mOldPointerX = me.getX();
            this.mOldPointerY = me.getY();
        } else {
            result = true;
        }
        this.mOldPointerCount = pointerCount;
        return result;
    }

    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchY;
        int touchX;
        MotionEvent motionEvent = me;
        int touchX2 = ((int) me.getX()) - this.mPaddingLeft;
        int touchY2 = ((int) me.getY()) - this.mPaddingTop;
        if (touchY2 >= (-this.mVerticalCorrection)) {
            touchY2 += this.mVerticalCorrection;
        }
        int action = me.getAction();
        long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX2, touchY2, null);
        this.mPossiblePoly = possiblePoly;
        if (action == 0) {
            this.mSwipeTracker.clear();
        }
        this.mSwipeTracker.addMovement(motionEvent);
        if (this.mAbortKey && action != 0 && action != 3) {
            return true;
        }
        if (this.mGestureDetector.onTouchEvent(motionEvent)) {
            showPreview(-1);
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            return true;
        } else if (this.mMiniKeyboardOnScreen && action != 3) {
            return true;
        } else {
            switch (action) {
                case 0:
                    int i = 0;
                    this.mAbortKey = false;
                    this.mStartX = touchX2;
                    this.mStartY = touchY2;
                    this.mLastCodeX = touchX2;
                    this.mLastCodeY = touchY2;
                    this.mLastKeyTime = 0;
                    this.mCurrentKeyTime = 0;
                    this.mLastKey = -1;
                    this.mCurrentKey = keyIndex;
                    this.mDownKey = keyIndex;
                    this.mDownTime = me.getEventTime();
                    this.mLastMoveTime = this.mDownTime;
                    checkMultiTap(eventTime, keyIndex);
                    OnKeyboardActionListener onKeyboardActionListener = this.mKeyboardActionListener;
                    if (keyIndex != -1) {
                        i = this.mKeys[keyIndex].codes[0];
                    }
                    onKeyboardActionListener.onPress(i);
                    if (this.mCurrentKey >= 0 && this.mKeys[this.mCurrentKey].repeatable) {
                        this.mRepeatKeyIndex = this.mCurrentKey;
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 400);
                        repeatKey();
                        if (this.mAbortKey) {
                            this.mRepeatKeyIndex = -1;
                            break;
                        }
                    }
                    if (this.mCurrentKey != -1) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, motionEvent), (long) LONGPRESS_TIMEOUT);
                    }
                    showPreview(keyIndex);
                    break;
                case 1:
                    removeMessages();
                    if (keyIndex == this.mCurrentKey) {
                        this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                    } else {
                        resetMultiTap();
                        this.mLastKey = this.mCurrentKey;
                        this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                        this.mCurrentKey = keyIndex;
                        this.mCurrentKeyTime = 0;
                    }
                    if (this.mCurrentKeyTime < this.mLastKeyTime && this.mCurrentKeyTime < 70 && this.mLastKey != -1) {
                        this.mCurrentKey = this.mLastKey;
                        touchX2 = this.mLastCodeX;
                        touchY2 = this.mLastCodeY;
                    }
                    touchX = touchX2;
                    touchY = touchY2;
                    showPreview(-1);
                    Arrays.fill(this.mKeyIndices, -1);
                    if (this.mRepeatKeyIndex == -1 && !this.mMiniKeyboardOnScreen && !this.mAbortKey) {
                        detectAndSendKey(this.mCurrentKey, touchX, touchY, eventTime);
                    }
                    invalidateKey(keyIndex);
                    this.mRepeatKeyIndex = -1;
                    break;
                case 2:
                    boolean continueLongPress = false;
                    if (keyIndex != -1) {
                        if (this.mCurrentKey == -1) {
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = eventTime - this.mDownTime;
                        } else if (keyIndex == this.mCurrentKey) {
                            this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                            continueLongPress = true;
                        } else if (this.mRepeatKeyIndex == -1) {
                            resetMultiTap();
                            this.mLastKey = this.mCurrentKey;
                            this.mLastCodeX = this.mLastX;
                            this.mLastCodeY = this.mLastY;
                            this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = 0;
                        }
                    }
                    if (!continueLongPress) {
                        this.mHandler.removeMessages(4);
                        if (keyIndex != -1) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, motionEvent), (long) LONGPRESS_TIMEOUT);
                        }
                    }
                    showPreview(this.mCurrentKey);
                    this.mLastMoveTime = eventTime;
                    break;
                case 3:
                    removeMessages();
                    dismissPopupKeyboard();
                    this.mAbortKey = true;
                    showPreview(-1);
                    invalidateKey(this.mCurrentKey);
                    break;
            }
            touchX = touchX2;
            touchY = touchY2;
            this.mLastX = touchX;
            this.mLastY = touchY;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean repeatKey() {
        Keyboard.Key key = this.mKeys[this.mRepeatKeyIndex];
        detectAndSendKey(this.mCurrentKey, key.x, key.y, this.mLastTapTime);
        return true;
    }

    /* access modifiers changed from: protected */
    public void swipeRight() {
        this.mKeyboardActionListener.swipeRight();
    }

    /* access modifiers changed from: protected */
    public void swipeLeft() {
        this.mKeyboardActionListener.swipeLeft();
    }

    /* access modifiers changed from: protected */
    public void swipeUp() {
        this.mKeyboardActionListener.swipeUp();
    }

    /* access modifiers changed from: protected */
    public void swipeDown() {
        this.mKeyboardActionListener.swipeDown();
    }

    public void closing() {
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
        removeMessages();
        dismissPopupKeyboard();
        this.mBuffer = null;
        this.mCanvas = null;
        this.mMiniKeyboardCache.clear();
    }

    private void removeMessages() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(1);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    /* access modifiers changed from: private */
    public void dismissPopupKeyboard() {
        if (this.mPopupKeyboard.isShowing()) {
            this.mPopupKeyboard.dismiss();
            this.mMiniKeyboardOnScreen = false;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (!this.mPopupKeyboard.isShowing()) {
            return false;
        }
        dismissPopupKeyboard();
        return true;
    }

    private void resetMultiTap() {
        this.mLastSentIndex = -1;
        this.mTapCount = 0;
        this.mLastTapTime = -1;
        this.mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        if (keyIndex != -1) {
            Keyboard.Key key = this.mKeys[keyIndex];
            if (key.codes.length > 1) {
                this.mInMultiTap = true;
                if (eventTime >= this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                    this.mTapCount = -1;
                } else {
                    this.mTapCount = (this.mTapCount + 1) % key.codes.length;
                }
            } else {
                if (eventTime > this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                    resetMultiTap();
                }
            }
        }
    }
}
