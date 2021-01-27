package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
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
import com.android.internal.R;
import com.android.internal.policy.PhoneWindow;
import java.util.Formatter;
import java.util.Locale;

public class MediaController extends FrameLayout {
    private static final int sDefaultTimeout = 3000;
    private final AccessibilityManager mAccessibilityManager;
    @UnsupportedAppUsage
    private View mAnchor;
    @UnsupportedAppUsage
    private final Context mContext;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private TextView mCurrentTime;
    @UnsupportedAppUsage
    private View mDecor;
    @UnsupportedAppUsage
    private WindowManager.LayoutParams mDecorLayoutParams;
    private boolean mDragging;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private TextView mEndTime;
    private final Runnable mFadeOut;
    @UnsupportedAppUsage
    private ImageButton mFfwdButton;
    @UnsupportedAppUsage
    private final View.OnClickListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private final View.OnLayoutChangeListener mLayoutChangeListener;
    private boolean mListenersSet;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private ImageButton mNextButton;
    private View.OnClickListener mNextListener;
    @UnsupportedAppUsage
    private ImageButton mPauseButton;
    private CharSequence mPauseDescription;
    private final View.OnClickListener mPauseListener;
    private CharSequence mPlayDescription;
    @UnsupportedAppUsage
    private MediaPlayerControl mPlayer;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private ImageButton mPrevButton;
    private View.OnClickListener mPrevListener;
    @UnsupportedAppUsage
    private ProgressBar mProgress;
    @UnsupportedAppUsage
    private ImageButton mRewButton;
    @UnsupportedAppUsage
    private final View.OnClickListener mRewListener;
    @UnsupportedAppUsage
    private View mRoot;
    @UnsupportedAppUsage
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    private final Runnable mShowProgress;
    @UnsupportedAppUsage
    private boolean mShowing;
    private final View.OnTouchListener mTouchListener;
    private final boolean mUseFastForward;
    @UnsupportedAppUsage
    private Window mWindow;
    @UnsupportedAppUsage
    private WindowManager mWindowManager;

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
            /* class android.widget.MediaController.AnonymousClass1 */

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                MediaController.this.updateFloatingWindowLayout();
                if (MediaController.this.mShowing) {
                    MediaController.this.mWindowManager.updateViewLayout(MediaController.this.mDecor, MediaController.this.mDecorLayoutParams);
                }
            }
        };
        this.mTouchListener = new View.OnTouchListener() {
            /* class android.widget.MediaController.AnonymousClass2 */

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !MediaController.this.mShowing) {
                    return false;
                }
                MediaController.this.hide();
                return false;
            }
        };
        this.mFadeOut = new Runnable() {
            /* class android.widget.MediaController.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                MediaController.this.hide();
            }
        };
        this.mShowProgress = new Runnable() {
            /* class android.widget.MediaController.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                int pos = MediaController.this.setProgress();
                if (!MediaController.this.mDragging && MediaController.this.mShowing && MediaController.this.mPlayer.isPlaying()) {
                    MediaController mediaController = MediaController.this;
                    mediaController.postDelayed(mediaController.mShowProgress, (long) (1000 - (pos % 1000)));
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass5 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.doPauseResume();
                MediaController.this.show(3000);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            /* class android.widget.MediaController.AnonymousClass6 */

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                MediaController.this.show(3600000);
                MediaController.this.mDragging = true;
                MediaController mediaController = MediaController.this;
                mediaController.removeCallbacks(mediaController.mShowProgress);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (((long) progress) * ((long) MediaController.this.mPlayer.getDuration())) / 1000;
                    MediaController.this.mPlayer.seekTo((int) newposition);
                    if (MediaController.this.mCurrentTime != null) {
                        MediaController.this.mCurrentTime.setText(MediaController.this.stringForTime((int) newposition));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                MediaController.this.mDragging = false;
                MediaController.this.setProgress();
                MediaController.this.updatePausePlay();
                MediaController.this.show(3000);
                MediaController mediaController = MediaController.this;
                mediaController.post(mediaController.mShowProgress);
            }
        };
        this.mRewListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass7 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() - 5000);
                MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mFfwdListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass8 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() + 15000);
                MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mRoot = this;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        View view = this.mRoot;
        if (view != null) {
            initControllerView(view);
        }
    }

    public MediaController(Context context, boolean useFastForward) {
        super(context);
        this.mLayoutChangeListener = new View.OnLayoutChangeListener() {
            /* class android.widget.MediaController.AnonymousClass1 */

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                MediaController.this.updateFloatingWindowLayout();
                if (MediaController.this.mShowing) {
                    MediaController.this.mWindowManager.updateViewLayout(MediaController.this.mDecor, MediaController.this.mDecorLayoutParams);
                }
            }
        };
        this.mTouchListener = new View.OnTouchListener() {
            /* class android.widget.MediaController.AnonymousClass2 */

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !MediaController.this.mShowing) {
                    return false;
                }
                MediaController.this.hide();
                return false;
            }
        };
        this.mFadeOut = new Runnable() {
            /* class android.widget.MediaController.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                MediaController.this.hide();
            }
        };
        this.mShowProgress = new Runnable() {
            /* class android.widget.MediaController.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                int pos = MediaController.this.setProgress();
                if (!MediaController.this.mDragging && MediaController.this.mShowing && MediaController.this.mPlayer.isPlaying()) {
                    MediaController mediaController = MediaController.this;
                    mediaController.postDelayed(mediaController.mShowProgress, (long) (1000 - (pos % 1000)));
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass5 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.doPauseResume();
                MediaController.this.show(3000);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            /* class android.widget.MediaController.AnonymousClass6 */

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                MediaController.this.show(3600000);
                MediaController.this.mDragging = true;
                MediaController mediaController = MediaController.this;
                mediaController.removeCallbacks(mediaController.mShowProgress);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (((long) progress) * ((long) MediaController.this.mPlayer.getDuration())) / 1000;
                    MediaController.this.mPlayer.seekTo((int) newposition);
                    if (MediaController.this.mCurrentTime != null) {
                        MediaController.this.mCurrentTime.setText(MediaController.this.stringForTime((int) newposition));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                MediaController.this.mDragging = false;
                MediaController.this.setProgress();
                MediaController.this.updatePausePlay();
                MediaController.this.show(3000);
                MediaController mediaController = MediaController.this;
                mediaController.post(mediaController.mShowProgress);
            }
        };
        this.mRewListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass7 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() - 5000);
                MediaController.this.setProgress();
                MediaController.this.show(3000);
            }
        };
        this.mFfwdListener = new View.OnClickListener() {
            /* class android.widget.MediaController.AnonymousClass8 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaController.this.mPlayer.seekTo(MediaController.this.mPlayer.getCurrentPosition() + 15000);
                MediaController.this.setProgress();
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
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindow = new PhoneWindow(this.mContext);
        this.mWindow.setWindowManager(this.mWindowManager, null, null);
        this.mWindow.requestFeature(1);
        this.mDecor = this.mWindow.getDecorView();
        this.mDecor.setOnTouchListener(this.mTouchListener);
        this.mWindow.setContentView(this);
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
    /* access modifiers changed from: public */
    private void updateFloatingWindowLayout() {
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
        View view2 = this.mAnchor;
        if (view2 != null) {
            view2.removeOnLayoutChangeListener(this.mLayoutChangeListener);
        }
        this.mAnchor = view;
        View view3 = this.mAnchor;
        if (view3 != null) {
            view3.addOnLayoutChangeListener(this.mLayoutChangeListener);
        }
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(-1, -1);
        removeAllViews();
        addView(makeControllerView(), frameParams);
    }

    /* access modifiers changed from: protected */
    public View makeControllerView() {
        this.mRoot = ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.media_controller, (ViewGroup) null);
        initControllerView(this.mRoot);
        return this.mRoot;
    }

    private void initControllerView(View v) {
        Resources res = this.mContext.getResources();
        this.mPlayDescription = res.getText(R.string.lockscreen_transport_play_description);
        this.mPauseDescription = res.getText(R.string.lockscreen_transport_pause_description);
        this.mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        ImageButton imageButton = this.mPauseButton;
        if (imageButton != null) {
            imageButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        ImageButton imageButton2 = this.mFfwdButton;
        int i = 0;
        if (imageButton2 != null) {
            imageButton2.setOnClickListener(this.mFfwdListener);
            if (!this.mFromXml) {
                this.mFfwdButton.setVisibility(this.mUseFastForward ? 0 : 8);
            }
        }
        this.mRewButton = (ImageButton) v.findViewById(R.id.rew);
        ImageButton imageButton3 = this.mRewButton;
        if (imageButton3 != null) {
            imageButton3.setOnClickListener(this.mRewListener);
            if (!this.mFromXml) {
                ImageButton imageButton4 = this.mRewButton;
                if (!this.mUseFastForward) {
                    i = 8;
                }
                imageButton4.setVisibility(i);
            }
        }
        this.mNextButton = (ImageButton) v.findViewById(R.id.next);
        ImageButton imageButton5 = this.mNextButton;
        if (imageButton5 != null && !this.mFromXml && !this.mListenersSet) {
            imageButton5.setVisibility(8);
        }
        this.mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        ImageButton imageButton6 = this.mPrevButton;
        if (imageButton6 != null && !this.mFromXml && !this.mListenersSet) {
            imageButton6.setVisibility(8);
        }
        this.mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            if (progressBar instanceof SeekBar) {
                ((SeekBar) progressBar).setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(1000);
        }
        this.mEndTime = (TextView) v.findViewById(R.id.time);
        this.mCurrentTime = (TextView) v.findViewById(R.id.time_current);
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
            ImageButton imageButton = this.mPauseButton;
            if (imageButton != null) {
                imageButton.requestFocus();
            }
            disableUnsupportedButtons();
            updateFloatingWindowLayout();
            this.mWindowManager.addView(this.mDecor, this.mDecorLayoutParams);
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
    /* access modifiers changed from: public */
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        this.mFormatBuilder.setLength(0);
        if (hours > 0) {
            return this.mFormatter.format("%d:%02d:%02d", Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)).toString();
        }
        return this.mFormatter.format("%02d:%02d", Integer.valueOf(minutes), Integer.valueOf(seconds)).toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int setProgress() {
        MediaPlayerControl mediaPlayerControl = this.mPlayer;
        if (mediaPlayerControl == null || this.mDragging) {
            return 0;
        }
        int position = mediaPlayerControl.getCurrentPosition();
        int duration = this.mPlayer.getDuration();
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            if (duration > 0) {
                progressBar.setProgress((int) ((((long) position) * 1000) / ((long) duration)));
            }
            this.mProgress.setSecondaryProgress(this.mPlayer.getBufferPercentage() * 10);
        }
        TextView textView = this.mEndTime;
        if (textView != null) {
            textView.setText(stringForTime(duration));
        }
        TextView textView2 = this.mCurrentTime;
        if (textView2 != null) {
            textView2.setText(stringForTime(position));
        }
        return position;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == 0) {
            show(0);
        } else if (action == 1) {
            show(3000);
        } else if (action == 3) {
            hide();
        }
        return true;
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent ev) {
        show(3000);
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == 0;
        if (keyCode == 79 || keyCode == 85 || keyCode == 62) {
            if (uniqueDown) {
                doPauseResume();
                show(3000);
                ImageButton imageButton = this.mPauseButton;
                if (imageButton != null) {
                    imageButton.requestFocus();
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
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPauseButton.setImageResource(17301539);
                this.mPauseButton.setContentDescription(this.mPauseDescription);
                return;
            }
            this.mPauseButton.setImageResource(17301540);
            this.mPauseButton.setContentDescription(this.mPlayDescription);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doPauseResume() {
        if (this.mPlayer.isPlaying()) {
            this.mPlayer.pause();
        } else {
            this.mPlayer.start();
        }
        updatePausePlay();
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        ImageButton imageButton = this.mPauseButton;
        if (imageButton != null) {
            imageButton.setEnabled(enabled);
        }
        ImageButton imageButton2 = this.mFfwdButton;
        if (imageButton2 != null) {
            imageButton2.setEnabled(enabled);
        }
        ImageButton imageButton3 = this.mRewButton;
        if (imageButton3 != null) {
            imageButton3.setEnabled(enabled);
        }
        ImageButton imageButton4 = this.mNextButton;
        boolean z = true;
        if (imageButton4 != null) {
            imageButton4.setEnabled(enabled && this.mNextListener != null);
        }
        ImageButton imageButton5 = this.mPrevButton;
        if (imageButton5 != null) {
            if (!enabled || this.mPrevListener == null) {
                z = false;
            }
            imageButton5.setEnabled(z);
        }
        ProgressBar progressBar = this.mProgress;
        if (progressBar != null) {
            progressBar.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return MediaController.class.getName();
    }

    private void installPrevNextListeners() {
        ImageButton imageButton = this.mNextButton;
        boolean z = true;
        if (imageButton != null) {
            imageButton.setOnClickListener(this.mNextListener);
            this.mNextButton.setEnabled(this.mNextListener != null);
        }
        ImageButton imageButton2 = this.mPrevButton;
        if (imageButton2 != null) {
            imageButton2.setOnClickListener(this.mPrevListener);
            ImageButton imageButton3 = this.mPrevButton;
            if (this.mPrevListener == null) {
                z = false;
            }
            imageButton3.setEnabled(z);
        }
    }

    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        this.mNextListener = next;
        this.mPrevListener = prev;
        this.mListenersSet = true;
        if (this.mRoot != null) {
            installPrevNextListeners();
            ImageButton imageButton = this.mNextButton;
            if (imageButton != null && !this.mFromXml) {
                imageButton.setVisibility(0);
            }
            ImageButton imageButton2 = this.mPrevButton;
            if (imageButton2 != null && !this.mFromXml) {
                imageButton2.setVisibility(0);
            }
        }
    }
}
