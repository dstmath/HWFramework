package android.inputmethodservice;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard.Key;
import android.media.AudioManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.preference.Preference;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.Settings.Secure;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telecom.AudioState;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardView extends View implements OnClickListener {
    private static final int DEBOUNCE_TIME = 70;
    private static final boolean DEBUG = false;
    private static final int DELAY_AFTER_PREVIEW = 70;
    private static final int DELAY_BEFORE_PREVIEW = 0;
    private static final int[] KEY_DELETE = null;
    private static final int LONGPRESS_TIMEOUT = 0;
    private static final int[] LONG_PRESSABLE_STATE_SET = null;
    private static int MAX_NEARBY_KEYS = 0;
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
    private Key mInvalidatedKey;
    private Drawable mKeyBackground;
    private int[] mKeyIndices;
    private int mKeyTextColor;
    private int mKeyTextSize;
    private Keyboard mKeyboard;
    private OnKeyboardActionListener mKeyboardActionListener;
    private boolean mKeyboardChanged;
    private Key[] mKeys;
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
    private Map<Key, View> mMiniKeyboardCache;
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

    private static class SwipeTracker {
        static final int LONGEST_PAST_TIME = 200;
        static final int NUM_PAST = 4;
        final long[] mPastTime;
        final float[] mPastX;
        final float[] mPastY;
        float mXVelocity;
        float mYVelocity;

        private SwipeTracker() {
            this.mPastX = new float[NUM_PAST];
            this.mPastY = new float[NUM_PAST];
            this.mPastTime = new long[NUM_PAST];
        }

        public void clear() {
            this.mPastTime[KeyboardView.LONGPRESS_TIMEOUT] = 0;
        }

        public void addMovement(MotionEvent ev) {
            long time = ev.getEventTime();
            int N = ev.getHistorySize();
            for (int i = KeyboardView.LONGPRESS_TIMEOUT; i < N; i += KeyboardView.MSG_SHOW_PREVIEW) {
                addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i), ev.getHistoricalEventTime(i));
            }
            addPoint(ev.getX(), ev.getY(), time);
        }

        private void addPoint(float x, float y, long time) {
            int drop = KeyboardView.NOT_A_KEY;
            long[] pastTime = this.mPastTime;
            int i = KeyboardView.LONGPRESS_TIMEOUT;
            while (i < NUM_PAST && pastTime[i] != 0) {
                if (pastTime[i] < time - 200) {
                    drop = i;
                }
                i += KeyboardView.MSG_SHOW_PREVIEW;
            }
            if (i == NUM_PAST && drop < 0) {
                drop = KeyboardView.LONGPRESS_TIMEOUT;
            }
            if (drop == i) {
                drop += KeyboardView.NOT_A_KEY;
            }
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            if (drop >= 0) {
                int start = drop + KeyboardView.MSG_SHOW_PREVIEW;
                int count = (4 - drop) + KeyboardView.NOT_A_KEY;
                System.arraycopy(pastX, start, pastX, KeyboardView.LONGPRESS_TIMEOUT, count);
                System.arraycopy(pastY, start, pastY, KeyboardView.LONGPRESS_TIMEOUT, count);
                System.arraycopy(pastTime, start, pastTime, KeyboardView.LONGPRESS_TIMEOUT, count);
                i -= drop + KeyboardView.MSG_SHOW_PREVIEW;
            }
            pastX[i] = x;
            pastY[i] = y;
            pastTime[i] = time;
            i += KeyboardView.MSG_SHOW_PREVIEW;
            if (i < NUM_PAST) {
                pastTime[i] = 0;
            }
        }

        public void computeCurrentVelocity(int units) {
            computeCurrentVelocity(units, Float.MAX_VALUE);
        }

        public void computeCurrentVelocity(int units, float maxVelocity) {
            float max;
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            long[] pastTime = this.mPastTime;
            float oldestX = pastX[KeyboardView.LONGPRESS_TIMEOUT];
            float oldestY = pastY[KeyboardView.LONGPRESS_TIMEOUT];
            long oldestTime = pastTime[KeyboardView.LONGPRESS_TIMEOUT];
            float accumX = 0.0f;
            float accumY = 0.0f;
            int N = KeyboardView.LONGPRESS_TIMEOUT;
            while (N < NUM_PAST && pastTime[N] != 0) {
                N += KeyboardView.MSG_SHOW_PREVIEW;
            }
            for (int i = KeyboardView.MSG_SHOW_PREVIEW; i < N; i += KeyboardView.MSG_SHOW_PREVIEW) {
                int dur = (int) (pastTime[i] - oldestTime);
                if (dur != 0) {
                    float f = (float) units;
                    float vel = ((pastX[i] - oldestX) / ((float) dur)) * r0;
                    if (accumX == 0.0f) {
                        accumX = vel;
                    } else {
                        accumX = (accumX + vel) * NetworkHistoryUtils.RECOVERY_PERCENTAGE;
                    }
                    f = (float) units;
                    vel = ((pastY[i] - oldestY) / ((float) dur)) * r0;
                    if (accumY == 0.0f) {
                        accumY = vel;
                    } else {
                        accumY = (accumY + vel) * NetworkHistoryUtils.RECOVERY_PERCENTAGE;
                    }
                }
            }
            if (accumX < 0.0f) {
                max = Math.max(accumX, -maxVelocity);
            } else {
                max = Math.min(accumX, maxVelocity);
            }
            this.mXVelocity = max;
            if (accumY < 0.0f) {
                max = Math.max(accumY, -maxVelocity);
            } else {
                max = Math.min(accumY, maxVelocity);
            }
            this.mYVelocity = max;
        }

        public float getXVelocity() {
            return this.mXVelocity;
        }

        public float getYVelocity() {
            return this.mYVelocity;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.inputmethodservice.KeyboardView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.inputmethodservice.KeyboardView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.KeyboardView.<clinit>():void");
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 18219141);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, LONGPRESS_TIMEOUT);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCurrentKeyIndex = NOT_A_KEY;
        this.mCoordinates = new int[MSG_REMOVE_PREVIEW];
        this.mPreviewCentered = DEBUG;
        this.mShowPreview = true;
        this.mShowTouchPoints = true;
        this.mCurrentKey = NOT_A_KEY;
        this.mDownKey = NOT_A_KEY;
        this.mKeyIndices = new int[12];
        this.mRepeatKeyIndex = NOT_A_KEY;
        this.mClipRegion = new Rect(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT);
        this.mSwipeTracker = new SwipeTracker();
        this.mOldPointerCount = MSG_SHOW_PREVIEW;
        this.mDistances = new int[MAX_NEARBY_KEYS];
        this.mPreviewLabel = new StringBuilder(MSG_SHOW_PREVIEW);
        this.mDirtyRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyleAttr, defStyleRes);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int previewLayout = LONGPRESS_TIMEOUT;
        int n = a.getIndexCount();
        for (int i = LONGPRESS_TIMEOUT; i < n; i += MSG_SHOW_PREVIEW) {
            int attr = a.getIndex(i);
            switch (attr) {
                case LONGPRESS_TIMEOUT /*0*/:
                    this.mShadowColor = a.getColor(attr, LONGPRESS_TIMEOUT);
                    break;
                case MSG_SHOW_PREVIEW /*1*/:
                    this.mShadowRadius = a.getFloat(attr, 0.0f);
                    break;
                case MSG_REMOVE_PREVIEW /*2*/:
                    this.mKeyBackground = a.getDrawable(attr);
                    break;
                case MSG_REPEAT /*3*/:
                    this.mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                    break;
                case MSG_LONGPRESS /*4*/:
                    this.mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                    this.mKeyTextColor = a.getColor(attr, Color.BLACK);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                    previewLayout = a.getResourceId(attr, LONGPRESS_TIMEOUT);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                    this.mPreviewOffset = a.getDimensionPixelOffset(attr, LONGPRESS_TIMEOUT);
                    break;
                case AudioState.ROUTE_SPEAKER /*8*/:
                    this.mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                    this.mVerticalCorrection = a.getDimensionPixelOffset(attr, LONGPRESS_TIMEOUT);
                    break;
                case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                    this.mPopupLayout = a.getResourceId(attr, LONGPRESS_TIMEOUT);
                    break;
                default:
                    break;
            }
        }
        this.mBackgroundDimAmount = this.mContext.obtainStyledAttributes(com.android.internal.R.styleable.Theme).getFloat(MSG_REMOVE_PREVIEW, NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        this.mPreviewPopup = new PopupWindow(context);
        this.mPreviewPopup.setEnterTransition(null);
        this.mPreviewPopup.setExitTransition(null);
        if (previewLayout != 0) {
            this.mPreviewText = (TextView) inflate.inflate(previewLayout, null);
            this.mPreviewTextSizeLarge = (int) this.mPreviewText.getTextSize();
            this.mPreviewPopup.setContentView(this.mPreviewText);
            this.mPreviewPopup.setBackgroundDrawable(null);
        } else {
            this.mShowPreview = DEBUG;
        }
        this.mPreviewPopup.setTouchable(DEBUG);
        this.mPopupKeyboard = new PopupWindow(context);
        this.mPopupKeyboard.setBackgroundDrawable(null);
        this.mPopupParent = this;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize(0.0f);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mPaint.setAlpha(Process.PROC_TERM_MASK);
        this.mPadding = new Rect(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT);
        this.mMiniKeyboardCache = new HashMap();
        this.mKeyBackground.getPadding(this.mPadding);
        this.mSwipeThreshold = (int) (getResources().getDisplayMetrics().density * 500.0f);
        this.mDisambiguateSwipe = getResources().getBoolean(17956943);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        resetMultiTap();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initGestureDetector();
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case KeyboardView.MSG_SHOW_PREVIEW /*1*/:
                            KeyboardView.this.showKey(msg.arg1);
                        case KeyboardView.MSG_REMOVE_PREVIEW /*2*/:
                            KeyboardView.this.mPreviewText.setVisibility(KeyboardView.MSG_LONGPRESS);
                        case KeyboardView.MSG_REPEAT /*3*/:
                            if (KeyboardView.this.repeatKey()) {
                                sendMessageDelayed(Message.obtain((Handler) this, (int) KeyboardView.MSG_REPEAT), 50);
                            }
                        case KeyboardView.MSG_LONGPRESS /*4*/:
                            KeyboardView.this.openPopupIfRequired((MotionEvent) msg.obj);
                        default:
                    }
                }
            };
        }
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
                public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                    if (KeyboardView.this.mPossiblePoly) {
                        return KeyboardView.DEBUG;
                    }
                    float absX = Math.abs(velocityX);
                    float absY = Math.abs(velocityY);
                    float deltaX = me2.getX() - me1.getX();
                    float deltaY = me2.getY() - me1.getY();
                    int travelX = KeyboardView.this.getWidth() / KeyboardView.MSG_REMOVE_PREVIEW;
                    int travelY = KeyboardView.this.getHeight() / KeyboardView.MSG_REMOVE_PREVIEW;
                    KeyboardView.this.mSwipeTracker.computeCurrentVelocity(Process.SYSTEM_UID);
                    float endingVelocityX = KeyboardView.this.mSwipeTracker.getXVelocity();
                    float endingVelocityY = KeyboardView.this.mSwipeTracker.getYVelocity();
                    boolean sendDownKey = KeyboardView.DEBUG;
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
                    return KeyboardView.DEBUG;
                }
            });
            this.mGestureDetector.setIsLongpressEnabled(DEBUG);
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        this.mKeyboardActionListener = listener;
    }

    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return this.mKeyboardActionListener;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (this.mKeyboard != null) {
            showPreview(NOT_A_KEY);
        }
        removeMessages();
        this.mKeyboard = keyboard;
        List<Key> keys = this.mKeyboard.getKeys();
        this.mKeys = (Key[]) keys.toArray(new Key[keys.size()]);
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
            return DEBUG;
        }
        invalidateAllKeys();
        return true;
    }

    public boolean isShifted() {
        if (this.mKeyboard != null) {
            return this.mKeyboard.isShifted();
        }
        return DEBUG;
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
        if (!this.mKeyboard.isShifted() || label == null || label.length() >= MSG_REPEAT || !Character.isLowerCase(label.charAt(LONGPRESS_TIMEOUT))) {
            return label;
        }
        return label.toString().toUpperCase();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mKeyboard == null) {
            setMeasuredDimension(this.mPaddingLeft + this.mPaddingRight, this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        int width = (this.mKeyboard.getMinWidth() + this.mPaddingLeft) + this.mPaddingRight;
        if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(width, (this.mKeyboard.getHeight() + this.mPaddingTop) + this.mPaddingBottom);
    }

    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard != null) {
            Key[] keys = this.mKeys;
            if (keys != null) {
                int length = keys.length;
                int dimensionSum = LONGPRESS_TIMEOUT;
                for (int i = LONGPRESS_TIMEOUT; i < length; i += MSG_SHOW_PREVIEW) {
                    Key key = keys[i];
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
        if (!(this.mDrawPending || this.mBuffer == null)) {
            if (this.mKeyboardChanged) {
            }
            canvas.drawBitmap(this.mBuffer, 0.0f, 0.0f, null);
        }
        onBufferDraw();
        canvas.drawBitmap(this.mBuffer, 0.0f, 0.0f, null);
    }

    private void onBufferDraw() {
        if (this.mBuffer == null || this.mKeyboardChanged) {
            if (this.mBuffer == null || (this.mKeyboardChanged && !(this.mBuffer.getWidth() == getWidth() && this.mBuffer.getHeight() == getHeight()))) {
                int max = Math.max(MSG_SHOW_PREVIEW, getWidth());
                this.mBuffer = Bitmap.createBitmap(width, Math.max(MSG_SHOW_PREVIEW, getHeight()), Config.ARGB_8888);
                this.mCanvas = new Canvas(this.mBuffer);
            }
            invalidateAllKeys();
            this.mKeyboardChanged = DEBUG;
        }
        Canvas canvas = this.mCanvas;
        canvas.clipRect(this.mDirtyRect, Op.REPLACE);
        if (this.mKeyboard != null) {
            Paint paint = this.mPaint;
            Drawable keyBackground = this.mKeyBackground;
            Rect clipRegion = this.mClipRegion;
            Rect padding = this.mPadding;
            int kbdPaddingLeft = this.mPaddingLeft;
            int kbdPaddingTop = this.mPaddingTop;
            Key[] keys = this.mKeys;
            Key invalidKey = this.mInvalidatedKey;
            paint.setColor(this.mKeyTextColor);
            boolean drawSingleKey = DEBUG;
            if (invalidKey != null && canvas.getClipBounds(clipRegion) && (invalidKey.x + kbdPaddingLeft) + NOT_A_KEY <= clipRegion.left && (invalidKey.y + kbdPaddingTop) + NOT_A_KEY <= clipRegion.top && ((invalidKey.x + invalidKey.width) + kbdPaddingLeft) + MSG_SHOW_PREVIEW >= clipRegion.right && ((invalidKey.y + invalidKey.height) + kbdPaddingTop) + MSG_SHOW_PREVIEW >= clipRegion.bottom) {
                drawSingleKey = true;
            }
            canvas.drawColor(LONGPRESS_TIMEOUT, Mode.CLEAR);
            int keyCount = keys.length;
            for (int i = LONGPRESS_TIMEOUT; i < keyCount; i += MSG_SHOW_PREVIEW) {
                Key key = keys[i];
                if (!drawSingleKey || invalidKey == key) {
                    String str;
                    keyBackground.setState(key.getCurrentDrawableState());
                    if (key.label == null) {
                        str = null;
                    } else {
                        str = adjustCase(key.label).toString();
                    }
                    Rect bounds = keyBackground.getBounds();
                    if (!(key.width == bounds.right && key.height == bounds.bottom)) {
                        keyBackground.setBounds(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, key.width, key.height);
                    }
                    canvas.translate((float) (key.x + kbdPaddingLeft), (float) (key.y + kbdPaddingTop));
                    keyBackground.draw(canvas);
                    if (str != null) {
                        if (str.length() <= MSG_SHOW_PREVIEW || key.codes.length >= MSG_REMOVE_PREVIEW) {
                            paint.setTextSize((float) this.mKeyTextSize);
                            paint.setTypeface(Typeface.DEFAULT);
                        } else {
                            paint.setTextSize((float) this.mLabelTextSize);
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                        }
                        paint.setShadowLayer(this.mShadowRadius, 0.0f, 0.0f, this.mShadowColor);
                        canvas.drawText(str, (float) ((((key.width - padding.left) - padding.right) / MSG_REMOVE_PREVIEW) + padding.left), (((float) (((key.height - padding.top) - padding.bottom) / MSG_REMOVE_PREVIEW)) + ((paint.getTextSize() - paint.descent()) / 2.0f)) + ((float) padding.top), paint);
                        paint.setShadowLayer(0.0f, 0.0f, 0.0f, LONGPRESS_TIMEOUT);
                    } else if (key.icon != null) {
                        int drawableX = ((((key.width - padding.left) - padding.right) - key.icon.getIntrinsicWidth()) / MSG_REMOVE_PREVIEW) + padding.left;
                        int drawableY = ((((key.height - padding.top) - padding.bottom) - key.icon.getIntrinsicHeight()) / MSG_REMOVE_PREVIEW) + padding.top;
                        canvas.translate((float) drawableX, (float) drawableY);
                        key.icon.setBounds(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                        key.icon.draw(canvas);
                        canvas.translate((float) (-drawableX), (float) (-drawableY));
                    }
                    canvas.translate((float) ((-key.x) - kbdPaddingLeft), (float) ((-key.y) - kbdPaddingTop));
                }
            }
            this.mInvalidatedKey = null;
            if (this.mMiniKeyboardOnScreen) {
                paint.setColor(((int) (this.mBackgroundDimAmount * 255.0f)) << 24);
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), paint);
            }
            this.mDrawPending = DEBUG;
            this.mDirtyRect.setEmpty();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getKeyIndices(int x, int y, int[] allKeys) {
        Key[] keys = this.mKeys;
        int primaryIndex = NOT_A_KEY;
        int closestKey = NOT_A_KEY;
        int closestKeyDist = this.mProximityThreshold + MSG_SHOW_PREVIEW;
        Arrays.fill(this.mDistances, Preference.DEFAULT_ORDER);
        int[] nearestKeyIndices = this.mKeyboard.getNearestKeys(x, y);
        int keyCount = nearestKeyIndices.length;
        int i = LONGPRESS_TIMEOUT;
        while (i < keyCount) {
            Key key = keys[nearestKeyIndices[i]];
            int dist = LONGPRESS_TIMEOUT;
            boolean isInside = key.isInside(x, y);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }
            if (this.mProximityCorrectOn) {
                dist = key.squaredDistanceFrom(x, y);
                int i2 = this.mProximityThreshold;
            }
            if (isInside) {
                if (key.codes[LONGPRESS_TIMEOUT] > 32) {
                    int nCodes = key.codes.length;
                    if (dist < closestKeyDist) {
                        closestKeyDist = dist;
                        closestKey = nearestKeyIndices[i];
                    }
                    if (allKeys != null) {
                        int j = LONGPRESS_TIMEOUT;
                        while (true) {
                            i2 = this.mDistances.length;
                            if (j >= r0) {
                                break;
                            }
                            if (this.mDistances[j] > dist) {
                                break;
                            }
                            j += MSG_SHOW_PREVIEW;
                        }
                    }
                }
                i += MSG_SHOW_PREVIEW;
            } else {
                i += MSG_SHOW_PREVIEW;
            }
        }
        if (primaryIndex == NOT_A_KEY) {
            return closestKey;
        }
        return primaryIndex;
    }

    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != NOT_A_KEY && index < this.mKeys.length) {
            Key key = this.mKeys[index];
            if (key.text != null) {
                this.mKeyboardActionListener.onText(key.text);
                this.mKeyboardActionListener.onRelease(NOT_A_KEY);
            } else {
                int code = key.codes[LONGPRESS_TIMEOUT];
                int[] codes = new int[MAX_NEARBY_KEYS];
                Arrays.fill(codes, NOT_A_KEY);
                getKeyIndices(x, y, codes);
                if (this.mInMultiTap) {
                    if (this.mTapCount != NOT_A_KEY) {
                        this.mKeyboardActionListener.onKey(-5, KEY_DELETE);
                    } else {
                        this.mTapCount = LONGPRESS_TIMEOUT;
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

    private CharSequence getPreviewText(Key key) {
        int i = LONGPRESS_TIMEOUT;
        if (!this.mInMultiTap) {
            return adjustCase(key.label);
        }
        this.mPreviewLabel.setLength(LONGPRESS_TIMEOUT);
        StringBuilder stringBuilder = this.mPreviewLabel;
        int[] iArr = key.codes;
        if (this.mTapCount >= 0) {
            i = this.mTapCount;
        }
        stringBuilder.append((char) iArr[i]);
        return adjustCase(this.mPreviewLabel);
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = this.mCurrentKeyIndex;
        PopupWindow previewPopup = this.mPreviewPopup;
        this.mCurrentKeyIndex = keyIndex;
        Key[] keys = this.mKeys;
        if (oldKeyIndex != this.mCurrentKeyIndex) {
            int keyCode;
            if (oldKeyIndex != NOT_A_KEY && keys.length > oldKeyIndex) {
                boolean z;
                Key oldKey = keys[oldKeyIndex];
                if (this.mCurrentKeyIndex == NOT_A_KEY) {
                    z = true;
                } else {
                    z = DEBUG;
                }
                oldKey.onReleased(z);
                invalidateKey(oldKeyIndex);
                keyCode = oldKey.codes[LONGPRESS_TIMEOUT];
                sendAccessibilityEventForUnicodeCharacter(TriangleMeshBuilder.TEXTURE_0, keyCode);
                sendAccessibilityEventForUnicodeCharacter(Root.FLAG_EMPTY, keyCode);
            }
            if (this.mCurrentKeyIndex != NOT_A_KEY && keys.length > this.mCurrentKeyIndex) {
                Key newKey = keys[this.mCurrentKeyIndex];
                newKey.onPressed();
                invalidateKey(this.mCurrentKeyIndex);
                keyCode = newKey.codes[LONGPRESS_TIMEOUT];
                sendAccessibilityEventForUnicodeCharacter(KeymasterDefs.KM_ALGORITHM_HMAC, keyCode);
                sendAccessibilityEventForUnicodeCharacter(Document.FLAG_ARCHIVE, keyCode);
            }
        }
        if (oldKeyIndex != this.mCurrentKeyIndex && this.mShowPreview) {
            this.mHandler.removeMessages(MSG_SHOW_PREVIEW);
            if (previewPopup.isShowing() && keyIndex == NOT_A_KEY) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REMOVE_PREVIEW), 70);
            }
            if (keyIndex == NOT_A_KEY) {
                return;
            }
            if (previewPopup.isShowing() && this.mPreviewText.getVisibility() == 0) {
                showKey(keyIndex);
            } else {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_SHOW_PREVIEW, keyIndex, LONGPRESS_TIMEOUT), 0);
            }
        }
    }

    private void showKey(int keyIndex) {
        PopupWindow previewPopup = this.mPreviewPopup;
        Key[] keys = this.mKeys;
        if (keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = keys[keyIndex];
            if (key.icon != null) {
                this.mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
                this.mPreviewText.setText(null);
            } else {
                this.mPreviewText.setCompoundDrawables(null, null, null, null);
                this.mPreviewText.setText(getPreviewText(key));
                if (key.label.length() <= MSG_SHOW_PREVIEW || key.codes.length >= MSG_REMOVE_PREVIEW) {
                    this.mPreviewText.setTextSize(LONGPRESS_TIMEOUT, (float) this.mPreviewTextSizeLarge);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT);
                } else {
                    this.mPreviewText.setTextSize(LONGPRESS_TIMEOUT, (float) this.mKeyTextSize);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
            this.mPreviewText.measure(MeasureSpec.makeMeasureSpec(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT), MeasureSpec.makeMeasureSpec(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT));
            int popupWidth = Math.max(this.mPreviewText.getMeasuredWidth(), (key.width + this.mPreviewText.getPaddingLeft()) + this.mPreviewText.getPaddingRight());
            int popupHeight = this.mPreviewHeight;
            LayoutParams lp = this.mPreviewText.getLayoutParams();
            if (lp != null) {
                lp.width = popupWidth;
                lp.height = popupHeight;
            }
            if (this.mPreviewCentered) {
                this.mPopupPreviewX = 160 - (this.mPreviewText.getMeasuredWidth() / MSG_REMOVE_PREVIEW);
                this.mPopupPreviewY = -this.mPreviewText.getMeasuredHeight();
            } else {
                this.mPopupPreviewX = (key.x - this.mPreviewText.getPaddingLeft()) + this.mPaddingLeft;
                this.mPopupPreviewY = (key.y - popupHeight) + this.mPreviewOffset;
            }
            this.mHandler.removeMessages(MSG_REMOVE_PREVIEW);
            getLocationInWindow(this.mCoordinates);
            int[] iArr = this.mCoordinates;
            iArr[LONGPRESS_TIMEOUT] = iArr[LONGPRESS_TIMEOUT] + this.mMiniKeyboardOffsetX;
            iArr = this.mCoordinates;
            iArr[MSG_SHOW_PREVIEW] = iArr[MSG_SHOW_PREVIEW] + this.mMiniKeyboardOffsetY;
            this.mPreviewText.getBackground().setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
            this.mPopupPreviewX += this.mCoordinates[LONGPRESS_TIMEOUT];
            this.mPopupPreviewY += this.mCoordinates[MSG_SHOW_PREVIEW];
            getLocationOnScreen(this.mCoordinates);
            if (this.mPopupPreviewY + this.mCoordinates[MSG_SHOW_PREVIEW] < 0) {
                if (key.x + key.width <= getWidth() / MSG_REMOVE_PREVIEW) {
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
                previewPopup.showAtLocation(this.mPopupParent, LONGPRESS_TIMEOUT, this.mPopupPreviewX, this.mPopupPreviewY);
            }
            this.mPreviewText.setVisibility(LONGPRESS_TIMEOUT);
        }
    }

    private void sendAccessibilityEventForUnicodeCharacter(int eventType, int code) {
        boolean speakPassword = DEBUG;
        if (this.mAccessibilityManager.isEnabled()) {
            String text;
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            onInitializeAccessibilityEvent(event);
            if (Secure.getIntForUser(this.mContext.getContentResolver(), Secure.ACCESSIBILITY_SPEAK_PASSWORD, LONGPRESS_TIMEOUT, -3) != 0) {
                speakPassword = true;
            }
            if (speakPassword || this.mAudioManager.isBluetoothA2dpOn() || this.mAudioManager.isWiredHeadsetOn()) {
                switch (code) {
                    case TextToSpeech.ERROR_NETWORK /*-6*/:
                        text = this.mContext.getString(17040540);
                        break;
                    case TextToSpeech.ERROR_OUTPUT /*-5*/:
                        text = this.mContext.getString(17040542);
                        break;
                    case TextToSpeech.ERROR_SERVICE /*-4*/:
                        text = this.mContext.getString(17040543);
                        break;
                    case TextToSpeech.ERROR_SYNTHESIS /*-3*/:
                        text = this.mContext.getString(17040541);
                        break;
                    case TextToSpeech.STOPPED /*-2*/:
                        text = this.mContext.getString(17040544);
                        break;
                    case NOT_A_KEY /*-1*/:
                        text = this.mContext.getString(17040545);
                        break;
                    case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                        text = this.mContext.getString(17040546);
                        break;
                    default:
                        text = String.valueOf((char) code);
                        break;
                }
            } else if (this.mHeadsetRequiredToHearPasswordsAnnounced) {
                text = this.mContext.getString(17040554);
            } else {
                if (eventType == TriangleMeshBuilder.TEXTURE_0) {
                    this.mHeadsetRequiredToHearPasswordsAnnounced = true;
                }
                text = this.mContext.getString(17040553);
            }
            event.getText().add(text);
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void invalidateAllKeys() {
        this.mDirtyRect.union(LONGPRESS_TIMEOUT, LONGPRESS_TIMEOUT, getWidth(), getHeight());
        this.mDrawPending = true;
        invalidate();
    }

    public void invalidateKey(int keyIndex) {
        if (this.mKeys != null && keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = this.mKeys[keyIndex];
            this.mInvalidatedKey = key;
            this.mDirtyRect.union(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
            onBufferDraw();
            invalidate(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
        }
    }

    private boolean openPopupIfRequired(MotionEvent me) {
        if (this.mPopupLayout == 0 || this.mCurrentKey < 0 || this.mCurrentKey >= this.mKeys.length) {
            return DEBUG;
        }
        boolean result = onLongPress(this.mKeys[this.mCurrentKey]);
        if (result) {
            this.mAbortKey = true;
            showPreview(NOT_A_KEY);
        }
        return result;
    }

    protected boolean onLongPress(Key popupKey) {
        int popupKeyboardId = popupKey.popupResId;
        if (popupKeyboardId == 0) {
            return DEBUG;
        }
        int i;
        this.mMiniKeyboardContainer = (View) this.mMiniKeyboardCache.get(popupKey);
        if (this.mMiniKeyboardContainer == null) {
            Keyboard keyboard;
            this.mMiniKeyboardContainer = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPopupLayout, null);
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
            View closeButton = this.mMiniKeyboardContainer.findViewById(R.id.closeButton);
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
                keyboard = new Keyboard(getContext(), popupKeyboardId, popupKey.popupCharacters, (int) NOT_A_KEY, getPaddingRight() + getPaddingLeft());
            } else {
                keyboard = new Keyboard(getContext(), popupKeyboardId);
            }
            this.mMiniKeyboard.setKeyboard(keyboard);
            this.mMiniKeyboard.setPopupParent(this);
            this.mMiniKeyboardContainer.measure(MeasureSpec.makeMeasureSpec(getWidth(), KeymasterDefs.KM_BIGNUM), MeasureSpec.makeMeasureSpec(getHeight(), KeymasterDefs.KM_BIGNUM));
            this.mMiniKeyboardCache.put(popupKey, this.mMiniKeyboardContainer);
        } else {
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
        }
        getLocationInWindow(this.mCoordinates);
        this.mPopupX = popupKey.x + this.mPaddingLeft;
        this.mPopupY = popupKey.y + this.mPaddingTop;
        this.mPopupX = (this.mPopupX + popupKey.width) - this.mMiniKeyboardContainer.getMeasuredWidth();
        this.mPopupY -= this.mMiniKeyboardContainer.getMeasuredHeight();
        int x = (this.mPopupX + this.mMiniKeyboardContainer.getPaddingRight()) + this.mCoordinates[LONGPRESS_TIMEOUT];
        int y = (this.mPopupY + this.mMiniKeyboardContainer.getPaddingBottom()) + this.mCoordinates[MSG_SHOW_PREVIEW];
        KeyboardView keyboardView = this.mMiniKeyboard;
        if (x < 0) {
            i = LONGPRESS_TIMEOUT;
        } else {
            i = x;
        }
        keyboardView.setPopupOffset(i, y);
        this.mMiniKeyboard.setShifted(isShifted());
        this.mPopupKeyboard.setContentView(this.mMiniKeyboardContainer);
        this.mPopupKeyboard.setWidth(this.mMiniKeyboardContainer.getMeasuredWidth());
        this.mPopupKeyboard.setHeight(this.mMiniKeyboardContainer.getMeasuredHeight());
        this.mPopupKeyboard.showAtLocation(this, LONGPRESS_TIMEOUT, x, y);
        this.mMiniKeyboardOnScreen = true;
        invalidateAllKeys();
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (!this.mAccessibilityManager.isTouchExplorationEnabled() || event.getPointerCount() != MSG_SHOW_PREVIEW) {
            return true;
        }
        switch (event.getAction()) {
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                event.setAction(MSG_REMOVE_PREVIEW);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                event.setAction(LONGPRESS_TIMEOUT);
                break;
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                event.setAction(MSG_SHOW_PREVIEW);
                break;
        }
        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent me) {
        boolean result;
        int pointerCount = me.getPointerCount();
        int action = me.getAction();
        long now = me.getEventTime();
        if (pointerCount != this.mOldPointerCount) {
            if (pointerCount == MSG_SHOW_PREVIEW) {
                MotionEvent down = MotionEvent.obtain(now, now, LONGPRESS_TIMEOUT, me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, DEBUG);
                down.recycle();
                if (action == MSG_SHOW_PREVIEW) {
                    result = onModifiedTouchEvent(me, true);
                }
            } else {
                MotionEvent up = MotionEvent.obtain(now, now, MSG_SHOW_PREVIEW, this.mOldPointerX, this.mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else if (pointerCount == MSG_SHOW_PREVIEW) {
            result = onModifiedTouchEvent(me, DEBUG);
            this.mOldPointerX = me.getX();
            this.mOldPointerY = me.getY();
        } else {
            result = true;
        }
        this.mOldPointerCount = pointerCount;
        return result;
    }

    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = ((int) me.getX()) - this.mPaddingLeft;
        int touchY = ((int) me.getY()) - this.mPaddingTop;
        if (touchY >= (-this.mVerticalCorrection)) {
            touchY += this.mVerticalCorrection;
        }
        int action = me.getAction();
        long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX, touchY, null);
        this.mPossiblePoly = possiblePoly;
        if (action == 0) {
            this.mSwipeTracker.clear();
        }
        this.mSwipeTracker.addMovement(me);
        if (this.mAbortKey && action != 0 && action != MSG_REPEAT) {
            return true;
        }
        if (this.mGestureDetector.onTouchEvent(me)) {
            showPreview(NOT_A_KEY);
            this.mHandler.removeMessages(MSG_REPEAT);
            this.mHandler.removeMessages(MSG_LONGPRESS);
            return true;
        } else if (this.mMiniKeyboardOnScreen && action != MSG_REPEAT) {
            return true;
        } else {
            switch (action) {
                case LONGPRESS_TIMEOUT /*0*/:
                    this.mAbortKey = DEBUG;
                    this.mStartX = touchX;
                    this.mStartY = touchY;
                    this.mLastCodeX = touchX;
                    this.mLastCodeY = touchY;
                    this.mLastKeyTime = 0;
                    this.mCurrentKeyTime = 0;
                    this.mLastKey = NOT_A_KEY;
                    this.mCurrentKey = keyIndex;
                    this.mDownKey = keyIndex;
                    this.mDownTime = me.getEventTime();
                    this.mLastMoveTime = this.mDownTime;
                    checkMultiTap(eventTime, keyIndex);
                    this.mKeyboardActionListener.onPress(keyIndex != NOT_A_KEY ? this.mKeys[keyIndex].codes[LONGPRESS_TIMEOUT] : LONGPRESS_TIMEOUT);
                    if (this.mCurrentKey >= 0 && this.mKeys[this.mCurrentKey].repeatable) {
                        this.mRepeatKeyIndex = this.mCurrentKey;
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REPEAT), 400);
                        repeatKey();
                        if (this.mAbortKey) {
                            this.mRepeatKeyIndex = NOT_A_KEY;
                            break;
                        }
                    }
                    if (this.mCurrentKey != NOT_A_KEY) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_LONGPRESS, me), (long) LONGPRESS_TIMEOUT);
                    }
                    showPreview(keyIndex);
                    break;
                case MSG_SHOW_PREVIEW /*1*/:
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
                    if (this.mCurrentKeyTime < this.mLastKeyTime && this.mCurrentKeyTime < 70 && this.mLastKey != NOT_A_KEY) {
                        this.mCurrentKey = this.mLastKey;
                        touchX = this.mLastCodeX;
                        touchY = this.mLastCodeY;
                    }
                    showPreview(NOT_A_KEY);
                    Arrays.fill(this.mKeyIndices, NOT_A_KEY);
                    if (!(this.mRepeatKeyIndex != NOT_A_KEY || this.mMiniKeyboardOnScreen || this.mAbortKey)) {
                        detectAndSendKey(this.mCurrentKey, touchX, touchY, eventTime);
                    }
                    invalidateKey(keyIndex);
                    this.mRepeatKeyIndex = NOT_A_KEY;
                    break;
                case MSG_REMOVE_PREVIEW /*2*/:
                    boolean continueLongPress = DEBUG;
                    if (keyIndex != NOT_A_KEY) {
                        if (this.mCurrentKey == NOT_A_KEY) {
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = eventTime - this.mDownTime;
                        } else if (keyIndex == this.mCurrentKey) {
                            this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                            continueLongPress = true;
                        } else if (this.mRepeatKeyIndex == NOT_A_KEY) {
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
                        this.mHandler.removeMessages(MSG_LONGPRESS);
                        if (keyIndex != NOT_A_KEY) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_LONGPRESS, me), (long) LONGPRESS_TIMEOUT);
                        }
                    }
                    showPreview(this.mCurrentKey);
                    this.mLastMoveTime = eventTime;
                    break;
                case MSG_REPEAT /*3*/:
                    removeMessages();
                    dismissPopupKeyboard();
                    this.mAbortKey = true;
                    showPreview(NOT_A_KEY);
                    invalidateKey(this.mCurrentKey);
                    break;
            }
            this.mLastX = touchX;
            this.mLastY = touchY;
            return true;
        }
    }

    private boolean repeatKey() {
        Key key = this.mKeys[this.mRepeatKeyIndex];
        detectAndSendKey(this.mCurrentKey, key.x, key.y, this.mLastTapTime);
        return true;
    }

    protected void swipeRight() {
        this.mKeyboardActionListener.swipeRight();
    }

    protected void swipeLeft() {
        this.mKeyboardActionListener.swipeLeft();
    }

    protected void swipeUp() {
        this.mKeyboardActionListener.swipeUp();
    }

    protected void swipeDown() {
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
            this.mHandler.removeMessages(MSG_REPEAT);
            this.mHandler.removeMessages(MSG_LONGPRESS);
            this.mHandler.removeMessages(MSG_SHOW_PREVIEW);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    private void dismissPopupKeyboard() {
        if (this.mPopupKeyboard.isShowing()) {
            this.mPopupKeyboard.dismiss();
            this.mMiniKeyboardOnScreen = DEBUG;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (!this.mPopupKeyboard.isShowing()) {
            return DEBUG;
        }
        dismissPopupKeyboard();
        return true;
    }

    private void resetMultiTap() {
        this.mLastSentIndex = NOT_A_KEY;
        this.mTapCount = LONGPRESS_TIMEOUT;
        this.mLastTapTime = -1;
        this.mInMultiTap = DEBUG;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        if (keyIndex != NOT_A_KEY) {
            Key key = this.mKeys[keyIndex];
            if (key.codes.length > MSG_SHOW_PREVIEW) {
                this.mInMultiTap = true;
                if (eventTime >= this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                    this.mTapCount = NOT_A_KEY;
                    return;
                } else {
                    this.mTapCount = (this.mTapCount + MSG_SHOW_PREVIEW) % key.codes.length;
                    return;
                }
            }
            if (eventTime > this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                resetMultiTap();
            }
        }
    }
}
