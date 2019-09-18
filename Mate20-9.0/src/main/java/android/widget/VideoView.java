package android.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Cea708CaptionRenderer;
import android.media.ClosedCaptionRenderer;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.SubtitleController;
import android.media.SubtitleTrack;
import android.media.TtmlRenderer;
import android.media.WebVttRenderer;
import android.net.Uri;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class VideoView extends SurfaceView implements MediaController.MediaPlayerControl, SubtitleController.Anchor {
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    private static final String TAG = "VideoView";
    private AudioAttributes mAudioAttributes;
    /* access modifiers changed from: private */
    public int mAudioFocusType;
    /* access modifiers changed from: private */
    public AudioManager mAudioManager;
    private int mAudioSession;
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener;
    /* access modifiers changed from: private */
    public boolean mCanPause;
    /* access modifiers changed from: private */
    public boolean mCanSeekBack;
    /* access modifiers changed from: private */
    public boolean mCanSeekForward;
    private MediaPlayer.OnCompletionListener mCompletionListener;
    /* access modifiers changed from: private */
    public int mCurrentBufferPercentage;
    /* access modifiers changed from: private */
    public int mCurrentState;
    private MediaPlayer.OnErrorListener mErrorListener;
    private Map<String, String> mHeaders;
    private MediaPlayer.OnInfoListener mInfoListener;
    /* access modifiers changed from: private */
    public MediaController mMediaController;
    /* access modifiers changed from: private */
    public MediaPlayer mMediaPlayer;
    /* access modifiers changed from: private */
    public MediaPlayer.OnCompletionListener mOnCompletionListener;
    /* access modifiers changed from: private */
    public MediaPlayer.OnErrorListener mOnErrorListener;
    /* access modifiers changed from: private */
    public MediaPlayer.OnInfoListener mOnInfoListener;
    /* access modifiers changed from: private */
    public MediaPlayer.OnPreparedListener mOnPreparedListener;
    private final Vector<Pair<InputStream, MediaFormat>> mPendingSubtitleTracks;
    MediaPlayer.OnPreparedListener mPreparedListener;
    SurfaceHolder.Callback mSHCallback;
    /* access modifiers changed from: private */
    public int mSeekWhenPrepared;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener;
    private SubtitleTrack.RenderingWidget mSubtitleWidget;
    private SubtitleTrack.RenderingWidget.OnChangedListener mSubtitlesChangedListener;
    /* access modifiers changed from: private */
    public int mSurfaceHeight;
    /* access modifiers changed from: private */
    public SurfaceHolder mSurfaceHolder;
    /* access modifiers changed from: private */
    public int mSurfaceWidth;
    /* access modifiers changed from: private */
    public int mTargetState;
    private Uri mUri;
    /* access modifiers changed from: private */
    public int mVideoHeight;
    /* access modifiers changed from: private */
    public int mVideoWidth;

    public interface STCallback {
        void pause();

        void seekTo(int i);

        void setPlayer(MediaPlayer mediaPlayer);

        void start();
    }

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPendingSubtitleTracks = new Vector<>();
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mAudioFocusType = 1;
        this.mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                int unused = VideoView.this.mVideoWidth = mp.getVideoWidth();
                int unused2 = VideoView.this.mVideoHeight = mp.getVideoHeight();
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    VideoView.this.requestLayout();
                }
            }
        };
        this.mPreparedListener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                int unused = VideoView.this.mCurrentState = 2;
                Metadata data = mp.getMetadata(false, false);
                if (data != null) {
                    boolean unused2 = VideoView.this.mCanPause = !data.has(1) || data.getBoolean(1);
                    boolean unused3 = VideoView.this.mCanSeekBack = !data.has(2) || data.getBoolean(2);
                    boolean unused4 = VideoView.this.mCanSeekForward = !data.has(3) || data.getBoolean(3);
                    data.recycleParcel();
                } else {
                    boolean unused5 = VideoView.this.mCanPause = VideoView.this.mCanSeekBack = VideoView.this.mCanSeekForward = true;
                }
                if (VideoView.this.mOnPreparedListener != null) {
                    VideoView.this.mOnPreparedListener.onPrepared(VideoView.this.mMediaPlayer);
                }
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.setEnabled(true);
                }
                int unused6 = VideoView.this.mVideoWidth = mp.getVideoWidth();
                int unused7 = VideoView.this.mVideoHeight = mp.getVideoHeight();
                int seekToPosition = VideoView.this.mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    VideoView.this.seekTo(seekToPosition);
                }
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    if (VideoView.this.mSurfaceWidth != VideoView.this.mVideoWidth || VideoView.this.mSurfaceHeight != VideoView.this.mVideoHeight) {
                        return;
                    }
                    if (VideoView.this.mTargetState == 3) {
                        VideoView.this.start();
                        if (VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show();
                        }
                    } else if (VideoView.this.isPlaying()) {
                    } else {
                        if ((seekToPosition != 0 || VideoView.this.getCurrentPosition() > 0) && VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show(0);
                        }
                    }
                } else if (VideoView.this.mTargetState == 3) {
                    VideoView.this.start();
                }
            }
        };
        this.mCompletionListener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                int unused = VideoView.this.mCurrentState = 5;
                int unused2 = VideoView.this.mTargetState = 5;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if (VideoView.this.mOnCompletionListener != null) {
                    VideoView.this.mOnCompletionListener.onCompletion(VideoView.this.mMediaPlayer);
                }
                if (VideoView.this.mAudioFocusType != 0) {
                    VideoView.this.mAudioManager.abandonAudioFocus(null);
                }
            }
        };
        this.mInfoListener = new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                VideoView.this.adjustIsBuffering(arg1);
                if (VideoView.this.mOnInfoListener != null) {
                    VideoView.this.mOnInfoListener.onInfo(mp, arg1, arg2);
                }
                return true;
            }
        };
        this.mErrorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                int messageId;
                Log.d(VideoView.TAG, "Error: " + framework_err + "," + impl_err);
                int unused = VideoView.this.mCurrentState = -1;
                int unused2 = VideoView.this.mTargetState = -1;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if ((VideoView.this.mOnErrorListener == null || !VideoView.this.mOnErrorListener.onError(VideoView.this.mMediaPlayer, framework_err, impl_err)) && VideoView.this.getWindowToken() != null) {
                    Resources resources = VideoView.this.mContext.getResources();
                    if (framework_err == 200) {
                        messageId = 17039381;
                    } else {
                        messageId = 17039377;
                    }
                    new AlertDialog.Builder(VideoView.this.mContext).setMessage(messageId).setPositiveButton(17039376, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (VideoView.this.mOnCompletionListener != null) {
                                VideoView.this.mOnCompletionListener.onCompletion(VideoView.this.mMediaPlayer);
                            }
                        }
                    }).setCancelable(false).show();
                }
                return true;
            }
        };
        this.mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                int unused = VideoView.this.mCurrentBufferPercentage = percent;
                VideoView.this.onBufferingUpdateOuter(mp, percent);
            }
        };
        this.mSHCallback = new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                int unused = VideoView.this.mSurfaceWidth = w;
                int unused2 = VideoView.this.mSurfaceHeight = h;
                boolean hasValidSize = false;
                boolean isValidState = VideoView.this.mTargetState == 3;
                if (VideoView.this.mVideoWidth == w && VideoView.this.mVideoHeight == h) {
                    hasValidSize = true;
                }
                if (VideoView.this.mMediaPlayer != null && isValidState && hasValidSize) {
                    if (VideoView.this.mSeekWhenPrepared != 0) {
                        VideoView.this.seekTo(VideoView.this.mSeekWhenPrepared);
                    }
                    VideoView.this.start();
                }
            }

            public void surfaceCreated(SurfaceHolder holder) {
                SurfaceHolder unused = VideoView.this.mSurfaceHolder = holder;
                VideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                SurfaceHolder unused = VideoView.this.mSurfaceHolder = null;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                VideoView.this.release(true);
            }
        };
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAudioAttributes = new AudioAttributes.Builder().setUsage(1).setContentType(3).build();
        getHolder().addCallback(this.mSHCallback);
        getHolder().setType(3);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        this.mCurrentState = 0;
        this.mTargetState = 0;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(this.mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(this.mVideoHeight, heightMeasureSpec);
        if (this.mVideoWidth > 0 && this.mVideoHeight > 0) {
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == 1073741824 && heightSpecMode == 1073741824) {
                width = widthSpecSize;
                height = heightSpecSize;
                if (this.mVideoWidth * height < this.mVideoHeight * width) {
                    width = (this.mVideoWidth * height) / this.mVideoHeight;
                } else if (this.mVideoWidth * height > this.mVideoHeight * width) {
                    height = (this.mVideoHeight * width) / this.mVideoWidth;
                }
            } else if (widthSpecMode == 1073741824) {
                width = widthSpecSize;
                height = (this.mVideoHeight * width) / this.mVideoWidth;
                if (heightSpecMode == Integer.MIN_VALUE && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == 1073741824) {
                height = heightSpecSize;
                width = (this.mVideoWidth * height) / this.mVideoHeight;
                if (widthSpecMode == Integer.MIN_VALUE && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = this.mVideoWidth;
                height = this.mVideoHeight;
                if (heightSpecMode == Integer.MIN_VALUE && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = (this.mVideoWidth * height) / this.mVideoHeight;
                }
                if (widthSpecMode == Integer.MIN_VALUE && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = (this.mVideoHeight * width) / this.mVideoWidth;
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    public CharSequence getAccessibilityClassName() {
        return VideoView.class.getName();
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        this.mUri = uri;
        this.mHeaders = headers;
        this.mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setAudioFocusRequest(int focusGain) {
        if (focusGain == 0 || focusGain == 1 || focusGain == 2 || focusGain == 3 || focusGain == 4) {
            this.mAudioFocusType = focusGain;
            return;
        }
        throw new IllegalArgumentException("Illegal audio focus type " + focusGain);
    }

    public void setAudioAttributes(AudioAttributes attributes) {
        if (attributes != null) {
            this.mAudioAttributes = attributes;
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioAttributes");
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) {
        if (this.mMediaPlayer == null) {
            this.mPendingSubtitleTracks.add(Pair.create(is, format));
            return;
        }
        try {
            this.mMediaPlayer.addSubtitleSource(is, format);
        } catch (IllegalStateException e) {
            this.mInfoListener.onInfo(this.mMediaPlayer, 901, 0);
        }
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
            this.mTargetState = 0;
            this.mAudioManager.abandonAudioFocus(null);
        }
    }

    /* access modifiers changed from: private */
    public void openVideo() {
        if (this.mUri != null && this.mSurfaceHolder != null) {
            release(false);
            if (this.mAudioFocusType != 0) {
                this.mAudioManager.requestAudioFocus(null, this.mAudioAttributes, this.mAudioFocusType, 0);
            }
            try {
                this.mMediaPlayer = new MediaPlayer();
                Context context = getContext();
                SubtitleController controller = new SubtitleController(context, this.mMediaPlayer.getMediaTimeProvider(), this.mMediaPlayer);
                controller.registerRenderer(new WebVttRenderer(context));
                controller.registerRenderer(new TtmlRenderer(context));
                controller.registerRenderer(new Cea708CaptionRenderer(context));
                controller.registerRenderer(new ClosedCaptionRenderer(context));
                this.mMediaPlayer.setSubtitleAnchor(controller, this);
                if (this.mAudioSession != 0) {
                    this.mMediaPlayer.setAudioSessionId(this.mAudioSession);
                } else {
                    this.mAudioSession = this.mMediaPlayer.getAudioSessionId();
                }
                onSTCallbackSetPlayer(this.mMediaPlayer);
                this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
                this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
                this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
                this.mMediaPlayer.setOnInfoListener(this.mInfoListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
                this.mCurrentBufferPercentage = 0;
                this.mMediaPlayer.setDataSource(this.mContext, this.mUri, this.mHeaders);
                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setAudioAttributes(this.mAudioAttributes);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.mMediaPlayer.prepareAsync();
                Iterator<Pair<InputStream, MediaFormat>> it = this.mPendingSubtitleTracks.iterator();
                while (it.hasNext()) {
                    Pair<InputStream, MediaFormat> pending = it.next();
                    try {
                        this.mMediaPlayer.addSubtitleSource((InputStream) pending.first, (MediaFormat) pending.second);
                    } catch (IllegalStateException e) {
                        this.mInfoListener.onInfo(this.mMediaPlayer, 901, 0);
                    }
                }
                this.mCurrentState = 1;
                attachMediaController();
            } catch (IOException ex) {
                Log.w(TAG, "Unable to open content: " + this.mUri, ex);
                this.mCurrentState = -1;
                this.mTargetState = -1;
                this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
            } catch (IllegalArgumentException ex2) {
                Log.w(TAG, "Unable to open content: " + this.mUri, ex2);
                this.mCurrentState = -1;
                this.mTargetState = -1;
                this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
            } finally {
                this.mPendingSubtitleTracks.clear();
            }
        }
    }

    public void setMediaController(MediaController controller) {
        if (this.mMediaController != null) {
            this.mMediaController.hide();
        }
        this.mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (this.mMediaPlayer != null && this.mMediaController != null) {
            this.mMediaController.setMediaPlayer(this);
            this.mMediaController.setAnchorView(getParent() instanceof View ? (View) getParent() : this);
            this.mMediaController.setEnabled(isInPlaybackState());
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        this.mOnPreparedListener = l;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        this.mOnCompletionListener = l;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        this.mOnInfoListener = l;
    }

    /* access modifiers changed from: private */
    public void release(boolean cleartargetstate) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mPendingSubtitleTracks.clear();
            this.mCurrentState = 0;
            if (cleartargetstate) {
                this.mTargetState = 0;
            }
            if (this.mAudioFocusType != 0) {
                this.mAudioManager.abandonAudioFocus(null);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0 && isInPlaybackState() && this.mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return super.onTouchEvent(ev);
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        if (ev.getAction() == 0 && isInPlaybackState() && this.mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return super.onTrackballEvent(ev);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = (keyCode == 4 || keyCode == 24 || keyCode == 25 || keyCode == 164 || keyCode == 82 || keyCode == 5 || keyCode == 6) ? false : true;
        if (isInPlaybackState() && isKeyCodeSupported && this.mMediaController != null) {
            if (keyCode == 79 || keyCode == 85) {
                if (this.mMediaPlayer.isPlaying()) {
                    pause();
                    this.mMediaController.show();
                } else {
                    start();
                    this.mMediaController.hide();
                }
                return true;
            } else if (keyCode == 126) {
                if (!this.mMediaPlayer.isPlaying()) {
                    start();
                    this.mMediaController.hide();
                }
                return true;
            } else if (keyCode == 86 || keyCode == 127) {
                if (this.mMediaPlayer.isPlaying()) {
                    pause();
                    this.mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (this.mMediaController.isShowing()) {
            this.mMediaController.hide();
        } else {
            this.mMediaController.show();
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            this.mMediaPlayer.start();
            this.mCurrentState = 3;
        }
        this.mTargetState = 3;
    }

    public void pause() {
        if (isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            this.mCurrentState = 4;
        }
        this.mTargetState = 4;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return this.mMediaPlayer.getDuration();
        }
        return -1;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return this.mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            this.mMediaPlayer.seekTo(msec);
            this.mSeekWhenPrepared = 0;
            return;
        }
        this.mSeekWhenPrepared = msec;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && this.mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (this.mMediaPlayer != null) {
            return this.mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (this.mMediaPlayer == null || this.mCurrentState == -1 || this.mCurrentState == 0 || this.mCurrentState == 1) ? false : true;
    }

    public boolean canPause() {
        return this.mCanPause;
    }

    public boolean canSeekBackward() {
        return this.mCanSeekBack;
    }

    public boolean canSeekForward() {
        return this.mCanSeekForward;
    }

    public int getAudioSessionId() {
        if (this.mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            this.mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return this.mAudioSession;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mSubtitleWidget != null) {
            this.mSubtitleWidget.onAttachedToWindow();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mSubtitleWidget != null) {
            this.mSubtitleWidget.onDetachedFromWindow();
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mSubtitleWidget != null) {
            measureAndLayoutSubtitleWidget();
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mSubtitleWidget != null) {
            int saveCount = canvas.save();
            canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            this.mSubtitleWidget.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    private void measureAndLayoutSubtitleWidget() {
        this.mSubtitleWidget.setSize((getWidth() - getPaddingLeft()) - getPaddingRight(), (getHeight() - getPaddingTop()) - getPaddingBottom());
    }

    public void setSubtitleWidget(SubtitleTrack.RenderingWidget subtitleWidget) {
        if (this.mSubtitleWidget != subtitleWidget) {
            boolean attachedToWindow = isAttachedToWindow();
            if (this.mSubtitleWidget != null) {
                if (attachedToWindow) {
                    this.mSubtitleWidget.onDetachedFromWindow();
                }
                this.mSubtitleWidget.setOnChangedListener(null);
            }
            this.mSubtitleWidget = subtitleWidget;
            if (subtitleWidget != null) {
                if (this.mSubtitlesChangedListener == null) {
                    this.mSubtitlesChangedListener = new SubtitleTrack.RenderingWidget.OnChangedListener() {
                        public void onChanged(SubtitleTrack.RenderingWidget renderingWidget) {
                            VideoView.this.invalidate();
                        }
                    };
                }
                setWillNotDraw(false);
                subtitleWidget.setOnChangedListener(this.mSubtitlesChangedListener);
                if (attachedToWindow) {
                    subtitleWidget.onAttachedToWindow();
                    requestLayout();
                }
            } else {
                setWillNotDraw(true);
            }
            invalidate();
        }
    }

    public Looper getSubtitleLooper() {
        return Looper.getMainLooper();
    }

    public void setSTCallback(STCallback call) {
    }

    /* access modifiers changed from: protected */
    public void onSTCallbackSetPlayer(MediaPlayer mediaPlayer) {
    }

    /* access modifiers changed from: protected */
    public void adjustIsBuffering(int arg1) {
    }

    /* access modifiers changed from: protected */
    public void onBufferingUpdateOuter(MediaPlayer mp, int percent) {
    }

    public boolean getCacheState() {
        return false;
    }

    public void setVideoScale(int width, int height) {
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    /* access modifiers changed from: protected */
    public MediaPlayer getMediaPlayer() {
        return this.mMediaPlayer;
    }

    /* access modifiers changed from: protected */
    public boolean isInPlaybackStateOuter() {
        return isInPlaybackState();
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener l) {
    }
}
