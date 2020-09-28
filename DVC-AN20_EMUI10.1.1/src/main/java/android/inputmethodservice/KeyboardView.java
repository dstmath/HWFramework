package android.inputmethodservice;

import android.annotation.UnsupportedAppUsage;
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

@Deprecated
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
    private boolean mDisambiguateSwipe;
    private int[] mDistances;
    private int mDownKey;
    private long mDownTime;
    private boolean mDrawPending;
    private GestureDetector mGestureDetector;
    Handler mHandler;
    private boolean mHeadsetRequiredToHearPasswordsAnnounced;
    private boolean mInMultiTap;
    private Keyboard.Key mInvalidatedKey;
    @UnsupportedAppUsage
    private Drawable mKeyBackground;
    private int[] mKeyIndices;
    private int mKeyTextColor;
    private int mKeyTextSize;
    private Keyboard mKeyboard;
    private OnKeyboardActionListener mKeyboardActionListener;
    private boolean mKeyboardChanged;
    private Keyboard.Key[] mKeys;
    @UnsupportedAppUsage
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
    private boolean mPossiblePoly;
    private boolean mPreviewCentered;
    private int mPreviewHeight;
    private StringBuilder mPreviewLabel;
    private int mPreviewOffset;
    private PopupWindow mPreviewPopup;
    @UnsupportedAppUsage
    private TextView mPreviewText;
    private int mPreviewTextSizeLarge;
    private boolean mProximityCorrectOn;
    private int mProximityThreshold;
    private int mRepeatKeyIndex;
    private int mShadowColor;
    private float mShadowRadius;
    private boolean mShowPreview;
    private boolean mShowTouchPoints;
    private int mStartX;
    private int mStartY;
    private int mSwipeThreshold;
    private SwipeTracker mSwipeTracker;
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

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.KeyboardView, defStyleAttr, defStyleRes);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int previewLayout = 0;
        int n = a.getIndexCount();
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
        this.mPreviewPopup = new PopupWindow(context);
        if (previewLayout != 0) {
            this.mPreviewText = (TextView) inflate.inflate(previewLayout, (ViewGroup) null);
            this.mPreviewTextSizeLarge = (int) this.mPreviewText.getTextSize();
            this.mPreviewPopup.setContentView(this.mPreviewText);
            this.mPreviewPopup.setBackgroundDrawable(null);
        } else {
            this.mShowPreview = false;
        }
        this.mPreviewPopup.setTouchable(false);
        this.mPopupKeyboard = new PopupWindow(context);
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
        this.mSwipeThreshold = (int) (getResources().getDisplayMetrics().density * 500.0f);
        this.mDisambiguateSwipe = getResources().getBoolean(R.bool.config_swipeDisambiguation);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        resetMultiTap();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initGestureDetector();
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                /* class android.inputmethodservice.KeyboardView.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    int i = msg.what;
                    if (i == 1) {
                        KeyboardView.this.showKey(msg.arg1);
                    } else if (i == 2) {
                        KeyboardView.this.mPreviewText.setVisibility(4);
                    } else if (i != 3) {
                        if (i == 4) {
                            KeyboardView.this.openPopupIfRequired((MotionEvent) msg.obj);
                        }
                    } else if (KeyboardView.this.repeatKey()) {
                        sendMessageDelayed(Message.obtain(this, 3), 50);
                    }
                }
            };
        }
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                /* class android.inputmethodservice.KeyboardView.AnonymousClass2 */

                @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
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
                        KeyboardView keyboardView = KeyboardView.this;
                        keyboardView.detectAndSendKey(keyboardView.mDownKey, KeyboardView.this.mStartX, KeyboardView.this.mStartY, me1.getEventTime());
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
        Keyboard keyboard = this.mKeyboard;
        if (keyboard == null || !keyboard.setShifted(shifted)) {
            return false;
        }
        invalidateAllKeys();
        return true;
    }

    public boolean isShifted() {
        Keyboard keyboard = this.mKeyboard;
        if (keyboard != null) {
            return keyboard.isShifted();
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

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        dismissPopupKeyboard();
    }

    private CharSequence adjustCase(CharSequence label) {
        if (!this.mKeyboard.isShifted() || label == null || label.length() >= 3 || !Character.isLowerCase(label.charAt(0))) {
            return label;
        }
        return label.toString().toUpperCase();
    }

    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Keyboard keyboard = this.mKeyboard;
        if (keyboard == null) {
            setMeasuredDimension(this.mPaddingLeft + this.mPaddingRight, this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        int width = keyboard.getMinWidth() + this.mPaddingLeft + this.mPaddingRight;
        if (View.MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(width, this.mKeyboard.getHeight() + this.mPaddingTop + this.mPaddingBottom);
    }

    private void computeProximityThreshold(Keyboard keyboard) {
        Keyboard.Key[] keys;
        if (!(keyboard == null || (keys = this.mKeys) == null)) {
            int length = keys.length;
            int dimensionSum = 0;
            for (Keyboard.Key key : keys) {
                dimensionSum += Math.min(key.width, key.height) + key.gap;
            }
            if (dimensionSum >= 0 && length != 0) {
                this.mProximityThreshold = (int) ((((float) dimensionSum) * 1.4f) / ((float) length));
                int i = this.mProximityThreshold;
                this.mProximityThreshold = i * i;
            }
        }
    }

    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Keyboard keyboard = this.mKeyboard;
        if (keyboard != null) {
            keyboard.resize(w, h);
        }
        this.mBuffer = null;
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawPending || this.mBuffer == null || this.mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(this.mBuffer, 0.0f, 0.0f, (Paint) null);
    }

    private void onBufferDraw() {
        boolean drawSingleKey;
        Keyboard.Key[] keys;
        Keyboard.Key invalidKey;
        int keyCount;
        if (this.mBuffer == null || this.mKeyboardChanged) {
            Bitmap bitmap = this.mBuffer;
            if (bitmap == null || (this.mKeyboardChanged && !(bitmap.getWidth() == getWidth() && this.mBuffer.getHeight() == getHeight()))) {
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
            if (invalidKey2 == null || !canvas.getClipBounds(clipRegion) || (invalidKey2.x + kbdPaddingLeft) - 1 > clipRegion.left || (invalidKey2.y + kbdPaddingTop) - 1 > clipRegion.top || invalidKey2.x + invalidKey2.width + kbdPaddingLeft + 1 < clipRegion.right || invalidKey2.y + invalidKey2.height + kbdPaddingTop + 1 < clipRegion.bottom) {
                drawSingleKey = false;
            } else {
                drawSingleKey = true;
            }
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int keyCount2 = keys2.length;
            int i = 0;
            while (i < keyCount2) {
                Keyboard.Key key = keys2[i];
                if (!drawSingleKey || invalidKey2 == key) {
                    keyBackground.setState(key.getCurrentDrawableState());
                    String label = key.label == null ? null : adjustCase(key.label).toString();
                    Rect bounds = keyBackground.getBounds();
                    keyCount = keyCount2;
                    if (key.width != bounds.right || key.height != bounds.bottom) {
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
                        invalidKey = invalidKey2;
                        keys = keys2;
                    } else if (key.icon != null) {
                        int drawableX = ((((key.width - padding.left) - padding.right) - key.icon.getIntrinsicWidth()) / 2) + padding.left;
                        int drawableY = ((((key.height - padding.top) - padding.bottom) - key.icon.getIntrinsicHeight()) / 2) + padding.top;
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
            this.mInvalidatedKey = null;
            if (this.mMiniKeyboardOnScreen) {
                paint.setColor(((int) (this.mBackgroundDimAmount * 255.0f)) << 24);
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), paint);
            }
            this.mCanvas.restore();
            this.mDrawPending = false;
            this.mDirtyRect.setEmpty();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0045, code lost:
        if (r11.codes[0] <= 32) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0047, code lost:
        r14 = r11.codes.length;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004a, code lost:
        if (r12 >= r7) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004c, code lost:
        r7 = r12;
        r6 = r8[r10];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004f, code lost:
        if (r20 != null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0051, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        r15 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0055, code lost:
        r1 = r17.mDistances;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        if (r15 >= r1.length) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        if (r1[r15] <= r12) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005e, code lost:
        r16 = r4;
        java.lang.System.arraycopy(r1, r15, r1, r15 + r14, (r1.length - r15) - r14);
        java.lang.System.arraycopy(r20, r15, r20, r15 + r14, (r20.length - r15) - r14);
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0071, code lost:
        if (r1 >= r14) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0073, code lost:
        r20[r15 + r1] = r11.codes[r1];
        r17.mDistances[r15 + r1] = r12;
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0085, code lost:
        r15 = r15 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008e, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0091, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0093, code lost:
        r10 = r10 + 1;
        r1 = r18;
        r2 = r19;
        r4 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003a, code lost:
        if (r14 >= r17.mProximityThreshold) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x003c, code lost:
        if (r13 != false) goto L_0x003e;
     */
    private int getKeyIndices(int x, int y, int[] allKeys) {
        int i = x;
        int i2 = y;
        Keyboard.Key[] keys = this.mKeys;
        int primaryIndex = -1;
        int closestKey = -1;
        int closestKeyDist = this.mProximityThreshold + 1;
        Arrays.fill(this.mDistances, Integer.MAX_VALUE);
        int[] nearestKeyIndices = this.mKeyboard.getNearestKeys(i, i2);
        int keyCount = nearestKeyIndices.length;
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
        return primaryIndex == -1 ? closestKey : primaryIndex;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != -1) {
            Keyboard.Key[] keyArr = this.mKeys;
            if (index < keyArr.length) {
                Keyboard.Key key = keyArr[index];
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
    }

    private CharSequence getPreviewText(Keyboard.Key key) {
        if (!this.mInMultiTap) {
            return adjustCase(key.label);
        }
        int i = 0;
        this.mPreviewLabel.setLength(0);
        StringBuilder sb = this.mPreviewLabel;
        int[] iArr = key.codes;
        int i2 = this.mTapCount;
        if (i2 >= 0) {
            i = i2;
        }
        sb.append((char) iArr[i]);
        return adjustCase(this.mPreviewLabel);
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = this.mCurrentKeyIndex;
        PopupWindow previewPopup = this.mPreviewPopup;
        this.mCurrentKeyIndex = keyIndex;
        Keyboard.Key[] keys = this.mKeys;
        int i = this.mCurrentKeyIndex;
        if (oldKeyIndex != i) {
            if (oldKeyIndex != -1 && keys.length > oldKeyIndex) {
                Keyboard.Key oldKey = keys[oldKeyIndex];
                oldKey.onReleased(i == -1);
                invalidateKey(oldKeyIndex);
                int keyCode = oldKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(256, keyCode);
                sendAccessibilityEventForUnicodeCharacter(65536, keyCode);
            }
            int keyCode2 = this.mCurrentKeyIndex;
            if (keyCode2 != -1 && keys.length > keyCode2) {
                Keyboard.Key newKey = keys[keyCode2];
                newKey.onPressed();
                invalidateKey(this.mCurrentKeyIndex);
                int keyCode3 = newKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(128, keyCode3);
                sendAccessibilityEventForUnicodeCharacter(32768, keyCode3);
            }
        }
        if (oldKeyIndex != this.mCurrentKeyIndex && this.mShowPreview) {
            this.mHandler.removeMessages(1);
            if (previewPopup.isShowing() && keyIndex == -1) {
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(2), 70);
            }
            if (keyIndex == -1) {
                return;
            }
            if (!previewPopup.isShowing() || this.mPreviewText.getVisibility() != 0) {
                Handler handler2 = this.mHandler;
                handler2.sendMessageDelayed(handler2.obtainMessage(1, keyIndex, 0), 0);
                return;
            }
            showKey(keyIndex);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void showKey(int keyIndex) {
        PopupWindow previewPopup = this.mPreviewPopup;
        Keyboard.Key[] keys = this.mKeys;
        if (keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Keyboard.Key key = keys[keyIndex];
            if (key.icon != null) {
                this.mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
                this.mPreviewText.setText((CharSequence) null);
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
            iArr[1] = iArr[1] + this.mMiniKeyboardOffsetY;
            this.mPreviewText.getBackground().setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
            int i = this.mPopupPreviewX;
            int[] iArr2 = this.mCoordinates;
            this.mPopupPreviewX = i + iArr2[0];
            this.mPopupPreviewY += iArr2[1];
            getLocationOnScreen(iArr2);
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
        Keyboard.Key[] keyArr = this.mKeys;
        if (keyArr != null && keyIndex >= 0 && keyIndex < keyArr.length) {
            Keyboard.Key key = keyArr[keyIndex];
            this.mInvalidatedKey = key;
            this.mDirtyRect.union(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, key.x + key.width + this.mPaddingLeft, key.y + key.height + this.mPaddingTop);
            onBufferDraw();
            invalidate(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, key.x + key.width + this.mPaddingLeft, key.y + key.height + this.mPaddingTop);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean openPopupIfRequired(MotionEvent me) {
        int i;
        if (this.mPopupLayout != 0 && (i = this.mCurrentKey) >= 0) {
            Keyboard.Key[] keyArr = this.mKeys;
            if (i < keyArr.length) {
                boolean result = onLongPress(keyArr[i]);
                if (result) {
                    this.mAbortKey = true;
                    showPreview(-1);
                }
                return result;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onLongPress(Keyboard.Key popupKey) {
        Keyboard keyboard;
        int popupKeyboardId = popupKey.popupResId;
        if (popupKeyboardId == 0) {
            return false;
        }
        this.mMiniKeyboardContainer = this.mMiniKeyboardCache.get(popupKey);
        View view = this.mMiniKeyboardContainer;
        if (view == null) {
            this.mMiniKeyboardContainer = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPopupLayout, (ViewGroup) null);
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(16908326);
            View closeButton = this.mMiniKeyboardContainer.findViewById(16908327);
            if (closeButton != null) {
                closeButton.setOnClickListener(this);
            }
            this.mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                /* class android.inputmethodservice.KeyboardView.AnonymousClass3 */

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void onKey(int primaryCode, int[] keyCodes) {
                    KeyboardView.this.mKeyboardActionListener.onKey(primaryCode, keyCodes);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void onText(CharSequence text) {
                    KeyboardView.this.mKeyboardActionListener.onText(text);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void swipeLeft() {
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void swipeRight() {
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void swipeUp() {
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void swipeDown() {
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
                public void onPress(int primaryCode) {
                    KeyboardView.this.mKeyboardActionListener.onPress(primaryCode);
                }

                @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
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
            this.mMiniKeyboard = (KeyboardView) view.findViewById(16908326);
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

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent event) {
        if (!this.mAccessibilityManager.isTouchExplorationEnabled() || event.getPointerCount() != 1) {
            return true;
        }
        int action = event.getAction();
        if (action == 7) {
            event.setAction(2);
        } else if (action == 9) {
            event.setAction(0);
        } else if (action == 10) {
            event.setAction(1);
        }
        return onTouchEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent me) {
        boolean result;
        int pointerCount = me.getPointerCount();
        int action = me.getAction();
        long now = me.getEventTime();
        if (pointerCount != this.mOldPointerCount) {
            if (pointerCount == 1) {
                MotionEvent down = MotionEvent.obtain(now, now, 0, me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, false);
                down.recycle();
                if (action == 1) {
                    result = onModifiedTouchEvent(me, true);
                }
            } else {
                MotionEvent up = MotionEvent.obtain(now, now, 1, this.mOldPointerX, this.mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else if (pointerCount == 1) {
            result = onModifiedTouchEvent(me, false);
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
        int i;
        int i2;
        boolean continueLongPress;
        int touchX2 = ((int) me.getX()) - this.mPaddingLeft;
        int touchY2 = ((int) me.getY()) - this.mPaddingTop;
        int i3 = this.mVerticalCorrection;
        if (touchY2 >= (-i3)) {
            touchY2 += i3;
        }
        int action = me.getAction();
        long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX2, touchY2, null);
        this.mPossiblePoly = possiblePoly;
        if (action == 0) {
            this.mSwipeTracker.clear();
        }
        this.mSwipeTracker.addMovement(me);
        if (this.mAbortKey && action != 0 && action != 3) {
            return true;
        }
        if (this.mGestureDetector.onTouchEvent(me)) {
            showPreview(-1);
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            return true;
        } else if (this.mMiniKeyboardOnScreen && action != 3) {
            return true;
        } else {
            if (action == 0) {
                int touchX3 = 0;
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
                    touchX3 = this.mKeys[keyIndex].codes[0];
                }
                onKeyboardActionListener.onPress(touchX3);
                int i4 = this.mCurrentKey;
                if (i4 < 0 || !this.mKeys[i4].repeatable) {
                    i = -1;
                } else {
                    this.mRepeatKeyIndex = this.mCurrentKey;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 400);
                    repeatKey();
                    if (this.mAbortKey) {
                        this.mRepeatKeyIndex = -1;
                    } else {
                        i = -1;
                    }
                }
                if (this.mCurrentKey != i) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                }
                showPreview(keyIndex);
            } else if (action == 1) {
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
                long j = this.mCurrentKeyTime;
                if (j >= this.mLastKeyTime || j >= 70 || (i2 = this.mLastKey) == -1) {
                    touchX = touchX2;
                    touchY = touchY2;
                } else {
                    this.mCurrentKey = i2;
                    touchX = this.mLastCodeX;
                    touchY = this.mLastCodeY;
                }
                showPreview(-1);
                Arrays.fill(this.mKeyIndices, -1);
                if (this.mRepeatKeyIndex == -1 && !this.mMiniKeyboardOnScreen && !this.mAbortKey) {
                    detectAndSendKey(this.mCurrentKey, touchX, touchY, eventTime);
                }
                invalidateKey(keyIndex);
                this.mRepeatKeyIndex = -1;
                this.mLastX = touchX;
                this.mLastY = touchY;
                return true;
            } else if (action == 2) {
                if (keyIndex != -1) {
                    int i5 = this.mCurrentKey;
                    if (i5 == -1) {
                        this.mCurrentKey = keyIndex;
                        this.mCurrentKeyTime = eventTime - this.mDownTime;
                        continueLongPress = false;
                    } else if (keyIndex == i5) {
                        this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                        continueLongPress = true;
                    } else {
                        continueLongPress = false;
                        if (this.mRepeatKeyIndex == -1) {
                            resetMultiTap();
                            this.mLastKey = this.mCurrentKey;
                            this.mLastCodeX = this.mLastX;
                            this.mLastCodeY = this.mLastY;
                            this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = 0;
                        }
                    }
                } else {
                    continueLongPress = false;
                }
                if (!continueLongPress) {
                    this.mHandler.removeMessages(4);
                    if (keyIndex != -1) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                    }
                }
                showPreview(this.mCurrentKey);
                this.mLastMoveTime = eventTime;
            } else if (action == 3) {
                removeMessages();
                dismissPopupKeyboard();
                this.mAbortKey = true;
                showPreview(-1);
                invalidateKey(this.mCurrentKey);
            }
            touchX = touchX2;
            touchY = touchY2;
            this.mLastX = touchX;
            this.mLastY = touchY;
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean repeatKey() {
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
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(3);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(1);
        }
    }

    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissPopupKeyboard() {
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
            } else if (eventTime > this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                resetMultiTap();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SwipeTracker {
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
            int drop = -1;
            long[] pastTime = this.mPastTime;
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
            float[] pastX2 = this.mPastX;
            float[] pastY = this.mPastY;
            long[] pastTime2 = this.mPastTime;
            float oldestX = pastX2[0];
            float oldestY = pastY[0];
            long oldestTime = pastTime2[0];
            float accumX = 0.0f;
            float accumY = 0.0f;
            int N = 0;
            while (N < 4 && pastTime2[N] != 0) {
                N++;
            }
            int i = 1;
            while (i < N) {
                int dur = (int) (pastTime2[i] - oldestTime);
                if (dur == 0) {
                    pastX = pastX2;
                    pastTime = pastTime2;
                } else {
                    pastX = pastX2;
                    pastTime = pastTime2;
                    float vel = ((pastX2[i] - oldestX) / ((float) dur)) * ((float) units);
                    if (accumX == 0.0f) {
                        accumX = vel;
                    } else {
                        accumX = (accumX + vel) * 0.5f;
                    }
                    float vel2 = ((pastY[i] - oldestY) / ((float) dur)) * ((float) units);
                    if (accumY == 0.0f) {
                        accumY = vel2;
                    } else {
                        accumY = (accumY + vel2) * 0.5f;
                    }
                }
                i++;
                pastX2 = pastX;
                pastTime2 = pastTime;
            }
            if (accumX < 0.0f) {
                f = Math.max(accumX, -maxVelocity);
            } else {
                f = Math.min(accumX, maxVelocity);
            }
            this.mXVelocity = f;
            if (accumY < 0.0f) {
                f2 = Math.max(accumY, -maxVelocity);
            } else {
                f2 = Math.min(accumY, maxVelocity);
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
}
