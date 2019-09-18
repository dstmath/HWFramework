package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.rms.AppAssociate;
import android.telephony.NetworkScanRequest;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import com.android.internal.policy.HwPolicyFactory;
import java.util.Formatter;
import java.util.Locale;

public class MediaController extends FrameLayout {
    private static final int sDefaultTimeout = 3000;
    private final AccessibilityManager mAccessibilityManager;
    private View mAnchor;
    private final Context mContext;
    /* access modifiers changed from: private */
    public TextView mCurrentTime;
    /* access modifiers changed from: private */
    public View mDecor;
    /* access modifiers changed from: private */
    public WindowManager.LayoutParams mDecorLayoutParams;
    /* access modifiers changed from: private */
    public boolean mDragging;
    private TextView mEndTime;
    private final Runnable mFadeOut;
    private ImageButton mFfwdButton;
    private final View.OnClickListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private final View.OnLayoutChangeListener mLayoutChangeListener;
    private boolean mListenersSet;
    private ImageButton mNextButton;
    private View.OnClickListener mNextListener;
    private ImageButton mPauseButton;
    private CharSequence mPauseDescription;
    private final View.OnClickListener mPauseListener;
    private CharSequence mPlayDescription;
    /* access modifiers changed from: private */
    public MediaPlayerControl mPlayer;
    private ImageButton mPrevButton;
    private View.OnClickListener mPrevListener;
    private ProgressBar mProgress;
    private ImageButton mRewButton;
    private final View.OnClickListener mRewListener;
    private View mRoot;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    /* access modifiers changed from: private */
    public final Runnable mShowProgress;
    /* access modifiers changed from: private */
    public boolean mShowing;
    private final View.OnTouchListener mTouchListener;
    private final boolean mUseFastForward;
    private Window mWindow;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;

    public interface MediaPlayerControl {
        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        int getAudioSessionId();

        int getBufferPercentage();

        int getCurrentPosition();

        int getDuration();

        boolean isPlaying();

        void pause();

        void seekTo(int i);

        void start();
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutChangeListener = new View.OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                MediaController.this.updateFloatingWindowLayout();
                if (MediaController.this.mShowing) {
                    MediaController.this.mWindowManager.updateViewLayout(MediaController.this.mDecor, MediaController.this.mDecorLayoutParams);
                }
            }
        };
        this.mTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 && MediaController.this.mShowing) {
                    MediaController.this.hide();
                }
                return false;
            }
        };
        this.mFadeOut = new Runnable() {
            public void run() {
                MediaController.this.hide();
            }
        };
        this.mShowProgress = new Runnable() {
            public void run() {
                int pos = MediaController.this.setProgress();
                if (!MediaController.this.mDragging && MediaController.this.mShowing && MediaController.this.mPlayer.isPlaying()) {
                    MediaController.this.postDelayed(MediaController.this.mShowProgress, (long) (1000 - (pos % 1000)));
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.doPauseResume();
                MediaController.this.show(3000);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                MediaController.this.show(3600000);
                boolean unused = MediaController.this.mDragging = true;
                MediaController.this.removeCallbacks(MediaController.this.mShowProgress);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (((long) progress) * ((long) MediaController.this.mPlayer.getDuration())) / 1000;
                    MediaController.this.mPlayer.seekTo((int) newposition);
                    if (MediaController.this.mCurrentTime != null) {
                        MediaController.this.mCurrentTime.setText((CharSequence) MediaController.this.stringForTime((int) newposition));
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                boolean unused = MediaController.this.mDragging = false;
                int unused2 = MediaController.this.setProgress();
                MediaController.this.updatePausePlay();
                MediaController.this.show(3000);
                MediaController.this.post(MediaController.this.mShowProgress);
            }
        };
        this.mRewListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() - 5000);
                int unused = MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mFfwdListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() + 15000);
                int unused = MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mRoot = this;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
    }

    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
    }

    public MediaController(Context context, boolean useFastForward) {
        super(context);
        this.mLayoutChangeListener = new View.OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                MediaController.this.updateFloatingWindowLayout();
                if (MediaController.this.mShowing) {
                    MediaController.this.mWindowManager.updateViewLayout(MediaController.this.mDecor, MediaController.this.mDecorLayoutParams);
                }
            }
        };
        this.mTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 && MediaController.this.mShowing) {
                    MediaController.this.hide();
                }
                return false;
            }
        };
        this.mFadeOut = new Runnable() {
            public void run() {
                MediaController.this.hide();
            }
        };
        this.mShowProgress = new Runnable() {
            public void run() {
                int pos = MediaController.this.setProgress();
                if (!MediaController.this.mDragging && MediaController.this.mShowing && MediaController.this.mPlayer.isPlaying()) {
                    MediaController.this.postDelayed(MediaController.this.mShowProgress, (long) (1000 - (pos % 1000)));
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.doPauseResume();
                MediaController.this.show(3000);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                MediaController.this.show(3600000);
                boolean unused = MediaController.this.mDragging = true;
                MediaController.this.removeCallbacks(MediaController.this.mShowProgress);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (((long) progress) * ((long) MediaController.this.mPlayer.getDuration())) / 1000;
                    MediaController.this.mPlayer.seekTo((int) newposition);
                    if (MediaController.this.mCurrentTime != null) {
                        MediaController.this.mCurrentTime.setText((CharSequence) MediaController.this.stringForTime((int) newposition));
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                boolean unused = MediaController.this.mDragging = false;
                int unused2 = MediaController.this.setProgress();
                MediaController.this.updatePausePlay();
                MediaController.this.show(3000);
                MediaController.this.post(MediaController.this.mShowProgress);
            }
        };
        this.mRewListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() - 5000);
                int unused = MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mFfwdListener = new View.OnClickListener() {
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() + 15000);
                int unused = MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mContext = context;
        this.mUseFastForward = useFastForward;
        initFloatingWindowLayout();
        initFloatingWindow();
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
    }

    public MediaController(Context context) {
        this(context, true);
    }

    private void initFloatingWindow() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
        this.mWindow = HwPolicyFactory.getHwPhoneWindow(this.mContext);
        this.mWindow.setWindowManager(this.mWindowManager, null, null);
        this.mWindow.requestFeature(1);
        this.mDecor = this.mWindow.getDecorView();
        this.mDecor.setOnTouchListener(this.mTouchListener);
        this.mWindow.setContentView((View) this);
        this.mWindow.setBackgroundDrawableResource(17170445);
        this.mWindow.setVolumeControlStream(3);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        requestFocus();
    }

    private void initFloatingWindowLayout() {
        this.mDecorLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams p = this.mDecorLayoutParams;
        p.gravity = 51;
        p.height = -2;
        p.x = 0;
        p.format = -3;
        p.type = 1000;
        p.flags |= 8519712;
        p.token = null;
        p.windowAnimations = 0;
    }

    /* access modifiers changed from: private */
    public void updateFloatingWindowLayout() {
        int[] anchorPos = new int[2];
        this.mAnchor.getLocationOnScreen(anchorPos);
        this.mDecor.measure(View.MeasureSpec.makeMeasureSpec(this.mAnchor.getWidth(), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(this.mAnchor.getHeight(), Integer.MIN_VALUE));
        WindowManager.LayoutParams p = this.mDecorLayoutParams;
        p.width = this.mAnchor.getWidth();
        p.x = anchorPos[0] + ((this.mAnchor.getWidth() - p.width) / 2);
        p.y = (anchorPos[1] + this.mAnchor.getHeight()) - this.mDecor.getMeasuredHeight();
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        this.mPlayer = player;
        updatePausePlay();
    }

    public void setAnchorView(View view) {
        if (this.mAnchor != null) {
            this.mAnchor.removeOnLayoutChangeListener(this.mLayoutChangeListener);
        }
        this.mAnchor = view;
        if (this.mAnchor != null) {
            this.mAnchor.addOnLayoutChangeListener(this.mLayoutChangeListener);
        }
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(-1, -1);
        removeAllViews();
        addView(makeControllerView(), (ViewGroup.LayoutParams) frameParams);
    }

    /* access modifiers changed from: protected */
    public View makeControllerView() {
        this.mRoot = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(17367175, (ViewGroup) null);
        initControllerView(this.mRoot);
        return this.mRoot;
    }

    private void initControllerView(View v) {
        Resources res = this.mContext.getResources();
        this.mPlayDescription = res.getText(17040394);
        this.mPauseDescription = res.getText(17040393);
        this.mPauseButton = (ImageButton) v.findViewById(16909187);
        if (this.mPauseButton != null) {
            this.mPauseButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mFfwdButton = (ImageButton) v.findViewById(16908895);
        int i = 0;
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setOnClickListener(this.mFfwdListener);
            if (!this.mFromXml) {
                this.mFfwdButton.setVisibility(this.mUseFastForward ? 0 : 8);
            }
        }
        this.mRewButton = (ImageButton) v.findViewById(16909259);
        if (this.mRewButton != null) {
            this.mRewButton.setOnClickListener(this.mRewListener);
            if (!this.mFromXml) {
                ImageButton imageButton = this.mRewButton;
                if (!this.mUseFastForward) {
                    i = 8;
                }
                imageButton.setVisibility(i);
            }
        }
        this.mNextButton = (ImageButton) v.findViewById(16909121);
        if (this.mNextButton != null && !this.mFromXml && !this.mListenersSet) {
            this.mNextButton.setVisibility(8);
        }
        this.mPrevButton = (ImageButton) v.findViewById(16909222);
        if (this.mPrevButton != null && !this.mFromXml && !this.mListenersSet) {
            this.mPrevButton.setVisibility(8);
        }
        this.mProgress = (ProgressBar) v.findViewById(16909080);
        if (this.mProgress != null) {
            if (this.mProgress instanceof SeekBar) {
                ((SeekBar) this.mProgress).setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(1000);
        }
        this.mEndTime = (TextView) v.findViewById(16909433);
        this.mCurrentTime = (TextView) v.findViewById(16909436);
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
        installPrevNextListeners();
    }

    public void show() {
        show(3000);
    }

    private void disableUnsupportedButtons() {
        try {
            if (this.mPauseButton != null && !this.mPlayer.canPause()) {
                this.mPauseButton.setEnabled(false);
            }
            if (this.mRewButton != null && !this.mPlayer.canSeekBackward()) {
                this.mRewButton.setEnabled(false);
            }
            if (this.mFfwdButton != null && !this.mPlayer.canSeekForward()) {
                this.mFfwdButton.setEnabled(false);
            }
            if (this.mProgress != null && !this.mPlayer.canSeekBackward() && !this.mPlayer.canSeekForward()) {
                this.mProgress.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError e) {
        }
    }

    public void show(int timeout) {
        if (!this.mShowing && this.mAnchor != null) {
            setProgress();
            if (this.mPauseButton != null) {
                this.mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            updateFloatingWindowLayout();
            try {
                this.mWindowManager.addView(this.mDecor, this.mDecorLayoutParams);
            } catch (IllegalStateException e) {
                Log.w("MediaController", "already added");
            }
            this.mShowing = true;
        }
        updatePausePlay();
        post(this.mShowProgress);
        if (timeout != 0 && !this.mAccessibilityManager.isTouchExplorationEnabled()) {
            removeCallbacks(this.mFadeOut);
            postDelayed(this.mFadeOut, (long) timeout);
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void hide() {
        if (this.mAnchor != null && this.mShowing) {
            try {
                removeCallbacks(this.mShowProgress);
                this.mWindowManager.removeView(this.mDecor);
            } catch (IllegalArgumentException e) {
                Log.w("MediaController", "already removed");
            }
            this.mShowing = false;
        }
    }

    /* access modifiers changed from: private */
    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / NetworkScanRequest.MAX_SEARCH_MAX_SEC;
        this.mFormatBuilder.setLength(0);
        if (hours > 0) {
            return this.mFormatter.format("%d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
        }
        return this.mFormatter.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
    }

    /* access modifiers changed from: private */
    public int setProgress() {
        if (this.mPlayer == null || this.mDragging) {
            return 0;
        }
        int position = this.mPlayer.getCurrentPosition();
        int duration = this.mPlayer.getDuration();
        if (this.mProgress != null) {
            if (duration > 0) {
                this.mProgress.setProgress((int) ((1000 * ((long) position)) / ((long) duration)));
            }
            this.mProgress.setSecondaryProgress(this.mPlayer.getBufferPercentage() * 10);
        }
        if (this.mEndTime != null) {
            this.mEndTime.setText((CharSequence) stringForTime(duration));
        }
        if (this.mCurrentTime != null) {
            this.mCurrentTime.setText((CharSequence) stringForTime(position));
        }
        return position;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action != 3) {
            switch (action) {
                case 0:
                    show(0);
                    break;
                case 1:
                    show(3000);
                    break;
            }
        } else {
            hide();
        }
        return true;
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        show(3000);
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == 0;
        if (keyCode == 79 || keyCode == 85 || keyCode == 62) {
            if (uniqueDown) {
                doPauseResume();
                show(3000);
                if (this.mPauseButton != null) {
                    this.mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == 126) {
            if (uniqueDown && !this.mPlayer.isPlaying()) {
                this.mPlayer.start();
                updatePausePlay();
                show(3000);
            }
            return true;
        } else if (keyCode == 86 || keyCode == 127) {
            if (uniqueDown && this.mPlayer.isPlaying()) {
                this.mPlayer.pause();
                updatePausePlay();
                show(3000);
            }
            return true;
        } else if (keyCode == 25 || keyCode == 24 || keyCode == 164 || keyCode == 27) {
            return super.dispatchKeyEvent(event);
        } else {
            if (keyCode == 4 || keyCode == 82) {
                if (uniqueDown) {
                    hide();
                }
                return true;
            }
            show(3000);
            return super.dispatchKeyEvent(event);
        }
    }

    /* access modifiers changed from: private */
    public void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPauseButton.setImageResource(17301539);
                this.mPauseButton.setContentDescription(this.mPauseDescription);
            } else {
                this.mPauseButton.setImageResource(17301540);
                this.mPauseButton.setContentDescription(this.mPlayDescription);
            }
        }
    }

    /* access modifiers changed from: private */
    public void doPauseResume() {
        if (this.mPlayer.isPlaying()) {
            this.mPlayer.pause();
        } else {
            this.mPlayer.start();
        }
        updatePausePlay();
    }

    public void setEnabled(boolean enabled) {
        if (this.mPauseButton != null) {
            this.mPauseButton.setEnabled(enabled);
        }
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setEnabled(enabled);
        }
        if (this.mRewButton != null) {
            this.mRewButton.setEnabled(enabled);
        }
        boolean z = false;
        if (this.mNextButton != null) {
            this.mNextButton.setEnabled(enabled && this.mNextListener != null);
        }
        if (this.mPrevButton != null) {
            ImageButton imageButton = this.mPrevButton;
            if (enabled && this.mPrevListener != null) {
                z = true;
            }
            imageButton.setEnabled(z);
        }
        if (this.mProgress != null) {
            this.mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public CharSequence getAccessibilityClassName() {
        return MediaController.class.getName();
    }

    private void installPrevNextListeners() {
        boolean z = false;
        if (this.mNextButton != null) {
            this.mNextButton.setOnClickListener(this.mNextListener);
            this.mNextButton.setEnabled(this.mNextListener != null);
        }
        if (this.mPrevButton != null) {
            this.mPrevButton.setOnClickListener(this.mPrevListener);
            ImageButton imageButton = this.mPrevButton;
            if (this.mPrevListener != null) {
                z = true;
            }
            imageButton.setEnabled(z);
        }
    }

    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        this.mNextListener = next;
        this.mPrevListener = prev;
        this.mListenersSet = true;
        if (this.mRoot != null) {
            installPrevNextListeners();
            if (this.mNextButton != null && !this.mFromXml) {
                this.mNextButton.setVisibility(0);
            }
            if (this.mPrevButton != null && !this.mFromXml) {
                this.mPrevButton.setVisibility(0);
            }
        }
    }
}
