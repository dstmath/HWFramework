package android.widget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.Cea708CaptionRenderer;
import android.media.ClosedCaptionRenderer;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.Metadata;
import android.media.SubtitleController;
import android.media.SubtitleController.Anchor;
import android.media.SubtitleTrack.RenderingWidget;
import android.media.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.media.TtmlRenderer;
import android.media.WebVttRenderer;
import android.net.Uri;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.PtmLog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.EditorInfo;
import android.widget.MediaController.MediaPlayerControl;
import com.android.ims.ImsReasonInfo;
import com.android.internal.R;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;

public class VideoView extends SurfaceView implements MediaPlayerControl, Anchor {
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    private String TAG;
    private int mAudioSession;
    private OnBufferingUpdateListener mBufferingUpdateListener;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private OnCompletionListener mCompletionListener;
    private int mCurrentBufferPercentage;
    private int mCurrentState;
    private OnErrorListener mErrorListener;
    private Map<String, String> mHeaders;
    private OnInfoListener mInfoListener;
    private MediaController mMediaController;
    private MediaPlayer mMediaPlayer;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private Vector<Pair<InputStream, MediaFormat>> mPendingSubtitleTracks;
    OnPreparedListener mPreparedListener;
    Callback mSHCallback;
    private int mSeekWhenPrepared;
    OnVideoSizeChangedListener mSizeChangedListener;
    private RenderingWidget mSubtitleWidget;
    private OnChangedListener mSubtitlesChangedListener;
    private int mSurfaceHeight;
    private SurfaceHolder mSurfaceHolder;
    private int mSurfaceWidth;
    private int mTargetState;
    private Uri mUri;
    private int mVideoHeight;
    private int mVideoWidth;

    public interface STCallback {
        void pause();

        void seekTo(int i);

        void setPlayer(MediaPlayer mediaPlayer);

        void start();
    }

    public VideoView(Context context) {
        super(context);
        this.TAG = "VideoView";
        this.mCurrentState = STATE_IDLE;
        this.mTargetState = STATE_IDLE;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                VideoView.this.mVideoWidth = mp.getVideoWidth();
                VideoView.this.mVideoHeight = mp.getVideoHeight();
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    VideoView.this.requestLayout();
                }
            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                VideoView.this.mCurrentState = VideoView.STATE_PREPARED;
                Metadata data = mp.getMetadata(false, false);
                if (data != null) {
                    boolean z;
                    VideoView videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PREPARING)) {
                        z = data.getBoolean(VideoView.STATE_PREPARING);
                    } else {
                        z = true;
                    }
                    videoView.mCanPause = z;
                    videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PREPARED)) {
                        z = data.getBoolean(VideoView.STATE_PREPARED);
                    } else {
                        z = true;
                    }
                    videoView.mCanSeekBack = z;
                    videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PLAYING)) {
                        z = data.getBoolean(VideoView.STATE_PLAYING);
                    } else {
                        z = true;
                    }
                    videoView.mCanSeekForward = z;
                    data.recycleParcel();
                } else {
                    VideoView.this.mCanPause = VideoView.this.mCanSeekBack = VideoView.this.mCanSeekForward = true;
                }
                if (VideoView.this.mOnPreparedListener != null) {
                    VideoView.this.mOnPreparedListener.onPrepared(VideoView.this.mMediaPlayer);
                }
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.setEnabled(true);
                }
                VideoView.this.mVideoWidth = mp.getVideoWidth();
                VideoView.this.mVideoHeight = mp.getVideoHeight();
                int seekToPosition = VideoView.this.mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    VideoView.this.seekTo(seekToPosition);
                }
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    if (VideoView.this.mSurfaceWidth != VideoView.this.mVideoWidth || VideoView.this.mSurfaceHeight != VideoView.this.mVideoHeight) {
                        return;
                    }
                    if (VideoView.this.mTargetState == VideoView.STATE_PLAYING) {
                        VideoView.this.start();
                        if (VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show();
                        }
                    } else if (!VideoView.this.isPlaying()) {
                        if ((seekToPosition != 0 || VideoView.this.getCurrentPosition() > 0) && VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show(VideoView.STATE_IDLE);
                        }
                    }
                } else if (VideoView.this.mTargetState == VideoView.STATE_PLAYING) {
                    VideoView.this.start();
                }
            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                VideoView.this.mCurrentState = VideoView.STATE_PLAYBACK_COMPLETED;
                VideoView.this.mTargetState = VideoView.STATE_PLAYBACK_COMPLETED;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if (VideoView.this.mOnCompletionListener != null) {
                    VideoView.this.mOnCompletionListener.onCompletion(VideoView.this.mMediaPlayer);
                }
            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                VideoView.this.adjustIsBuffering(arg1);
                if (VideoView.this.mOnInfoListener != null) {
                    VideoView.this.mOnInfoListener.onInfo(mp, arg1, arg2);
                }
                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                Log.d(VideoView.this.TAG, "Error: " + framework_err + PtmLog.PAIRE_DELIMETER + impl_err);
                VideoView.this.mCurrentState = VideoView.STATE_ERROR;
                VideoView.this.mTargetState = VideoView.STATE_ERROR;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if ((VideoView.this.mOnErrorListener == null || !VideoView.this.mOnErrorListener.onError(VideoView.this.mMediaPlayer, framework_err, impl_err)) && VideoView.this.getWindowToken() != null) {
                    int messageId;
                    Resources r = VideoView.this.mContext.getResources();
                    if (framework_err == StatisticalConstant.TYPE_WIFI_CONNECT_ACTION) {
                        messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                    } else {
                        messageId = R.string.VideoView_error_text_unknown;
                    }
                    new Builder(VideoView.this.mContext).setMessage(messageId).setPositiveButton(R.string.VideoView_error_button, new OnClickListener() {
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
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                VideoView.this.mCurrentBufferPercentage = percent;
                VideoView.this.onBufferingUpdateOuter(mp, percent);
            }
        };
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                VideoView.this.mSurfaceWidth = w;
                VideoView.this.mSurfaceHeight = h;
                boolean isValidState = VideoView.this.mTargetState == VideoView.STATE_PLAYING;
                boolean hasValidSize = VideoView.this.mVideoWidth == w && VideoView.this.mVideoHeight == h;
                if (VideoView.this.mMediaPlayer != null && isValidState && hasValidSize) {
                    if (VideoView.this.mSeekWhenPrepared != 0) {
                        VideoView.this.seekTo(VideoView.this.mSeekWhenPrepared);
                    }
                    VideoView.this.start();
                }
            }

            public void surfaceCreated(SurfaceHolder holder) {
                VideoView.this.mSurfaceHolder = holder;
                VideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                VideoView.this.mSurfaceHolder = null;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                VideoView.this.release(true);
            }
        };
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, STATE_IDLE);
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, STATE_IDLE);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "VideoView";
        this.mCurrentState = STATE_IDLE;
        this.mTargetState = STATE_IDLE;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                VideoView.this.mVideoWidth = mp.getVideoWidth();
                VideoView.this.mVideoHeight = mp.getVideoHeight();
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    VideoView.this.requestLayout();
                }
            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                VideoView.this.mCurrentState = VideoView.STATE_PREPARED;
                Metadata data = mp.getMetadata(false, false);
                if (data != null) {
                    boolean z;
                    VideoView videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PREPARING)) {
                        z = data.getBoolean(VideoView.STATE_PREPARING);
                    } else {
                        z = true;
                    }
                    videoView.mCanPause = z;
                    videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PREPARED)) {
                        z = data.getBoolean(VideoView.STATE_PREPARED);
                    } else {
                        z = true;
                    }
                    videoView.mCanSeekBack = z;
                    videoView = VideoView.this;
                    if (data.has(VideoView.STATE_PLAYING)) {
                        z = data.getBoolean(VideoView.STATE_PLAYING);
                    } else {
                        z = true;
                    }
                    videoView.mCanSeekForward = z;
                    data.recycleParcel();
                } else {
                    VideoView.this.mCanPause = VideoView.this.mCanSeekBack = VideoView.this.mCanSeekForward = true;
                }
                if (VideoView.this.mOnPreparedListener != null) {
                    VideoView.this.mOnPreparedListener.onPrepared(VideoView.this.mMediaPlayer);
                }
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.setEnabled(true);
                }
                VideoView.this.mVideoWidth = mp.getVideoWidth();
                VideoView.this.mVideoHeight = mp.getVideoHeight();
                int seekToPosition = VideoView.this.mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    VideoView.this.seekTo(seekToPosition);
                }
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    if (VideoView.this.mSurfaceWidth != VideoView.this.mVideoWidth || VideoView.this.mSurfaceHeight != VideoView.this.mVideoHeight) {
                        return;
                    }
                    if (VideoView.this.mTargetState == VideoView.STATE_PLAYING) {
                        VideoView.this.start();
                        if (VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show();
                        }
                    } else if (!VideoView.this.isPlaying()) {
                        if ((seekToPosition != 0 || VideoView.this.getCurrentPosition() > 0) && VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show(VideoView.STATE_IDLE);
                        }
                    }
                } else if (VideoView.this.mTargetState == VideoView.STATE_PLAYING) {
                    VideoView.this.start();
                }
            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                VideoView.this.mCurrentState = VideoView.STATE_PLAYBACK_COMPLETED;
                VideoView.this.mTargetState = VideoView.STATE_PLAYBACK_COMPLETED;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if (VideoView.this.mOnCompletionListener != null) {
                    VideoView.this.mOnCompletionListener.onCompletion(VideoView.this.mMediaPlayer);
                }
            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                VideoView.this.adjustIsBuffering(arg1);
                if (VideoView.this.mOnInfoListener != null) {
                    VideoView.this.mOnInfoListener.onInfo(mp, arg1, arg2);
                }
                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                Log.d(VideoView.this.TAG, "Error: " + framework_err + PtmLog.PAIRE_DELIMETER + impl_err);
                VideoView.this.mCurrentState = VideoView.STATE_ERROR;
                VideoView.this.mTargetState = VideoView.STATE_ERROR;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                if ((VideoView.this.mOnErrorListener == null || !VideoView.this.mOnErrorListener.onError(VideoView.this.mMediaPlayer, framework_err, impl_err)) && VideoView.this.getWindowToken() != null) {
                    int messageId;
                    Resources r = VideoView.this.mContext.getResources();
                    if (framework_err == StatisticalConstant.TYPE_WIFI_CONNECT_ACTION) {
                        messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                    } else {
                        messageId = R.string.VideoView_error_text_unknown;
                    }
                    new Builder(VideoView.this.mContext).setMessage(messageId).setPositiveButton(R.string.VideoView_error_button, new OnClickListener() {
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
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                VideoView.this.mCurrentBufferPercentage = percent;
                VideoView.this.onBufferingUpdateOuter(mp, percent);
            }
        };
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                VideoView.this.mSurfaceWidth = w;
                VideoView.this.mSurfaceHeight = h;
                boolean isValidState = VideoView.this.mTargetState == VideoView.STATE_PLAYING;
                boolean hasValidSize = VideoView.this.mVideoWidth == w && VideoView.this.mVideoHeight == h;
                if (VideoView.this.mMediaPlayer != null && isValidState && hasValidSize) {
                    if (VideoView.this.mSeekWhenPrepared != 0) {
                        VideoView.this.seekTo(VideoView.this.mSeekWhenPrepared);
                    }
                    VideoView.this.start();
                }
            }

            public void surfaceCreated(SurfaceHolder holder) {
                VideoView.this.mSurfaceHolder = holder;
                VideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                VideoView.this.mSurfaceHolder = null;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                VideoView.this.release(true);
            }
        };
        initVideoView();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(this.mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(this.mVideoHeight, heightMeasureSpec);
        if (this.mVideoWidth > 0 && this.mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION && heightSpecMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                width = widthSpecSize;
                height = heightSpecSize;
                if (this.mVideoWidth * heightSpecSize < this.mVideoHeight * widthSpecSize) {
                    width = (this.mVideoWidth * heightSpecSize) / this.mVideoHeight;
                } else if (this.mVideoWidth * heightSpecSize > this.mVideoHeight * widthSpecSize) {
                    height = (this.mVideoHeight * widthSpecSize) / this.mVideoWidth;
                }
            } else if (widthSpecMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                width = widthSpecSize;
                height = (this.mVideoHeight * widthSpecSize) / this.mVideoWidth;
                if (heightSpecMode == RtlSpacingHelper.UNDEFINED && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                height = heightSpecSize;
                width = (this.mVideoWidth * heightSpecSize) / this.mVideoHeight;
                if (widthSpecMode == RtlSpacingHelper.UNDEFINED && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = this.mVideoWidth;
                height = this.mVideoHeight;
                if (heightSpecMode == RtlSpacingHelper.UNDEFINED && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = (this.mVideoWidth * heightSpecSize) / this.mVideoHeight;
                }
                if (widthSpecMode == RtlSpacingHelper.UNDEFINED && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = (this.mVideoHeight * widthSpecSize) / this.mVideoWidth;
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    public CharSequence getAccessibilityClassName() {
        return VideoView.class.getName();
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return View.getDefaultSize(desiredSize, measureSpec);
    }

    private void initVideoView() {
        this.mVideoWidth = STATE_IDLE;
        this.mVideoHeight = STATE_IDLE;
        getHolder().addCallback(this.mSHCallback);
        getHolder().setType(STATE_PLAYING);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        this.mPendingSubtitleTracks = new Vector();
        this.mCurrentState = STATE_IDLE;
        this.mTargetState = STATE_IDLE;
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
        this.mSeekWhenPrepared = STATE_IDLE;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) {
        if (this.mMediaPlayer == null) {
            this.mPendingSubtitleTracks.add(Pair.create(is, format));
            return;
        }
        try {
            this.mMediaPlayer.addSubtitleSource(is, format);
        } catch (IllegalStateException e) {
            this.mInfoListener.onInfo(this.mMediaPlayer, ImsReasonInfo.CODE_ECBM_NOT_SUPPORTED, STATE_IDLE);
        }
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = STATE_IDLE;
            this.mTargetState = STATE_IDLE;
            ((AudioManager) this.mContext.getSystemService("audio")).abandonAudioFocus(null);
        }
    }

    private void openVideo() {
        if (this.mUri != null && this.mSurfaceHolder != null) {
            release(false);
            ((AudioManager) this.mContext.getSystemService("audio")).requestAudioFocus(null, STATE_PLAYING, STATE_PREPARING);
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
                this.mCurrentBufferPercentage = STATE_IDLE;
                this.mMediaPlayer.setDataSource(this.mContext, this.mUri, this.mHeaders);
                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setAudioStreamType(STATE_PLAYING);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.mMediaPlayer.prepareAsync();
                for (Pair<InputStream, MediaFormat> pending : this.mPendingSubtitleTracks) {
                    try {
                        this.mMediaPlayer.addSubtitleSource((InputStream) pending.first, (MediaFormat) pending.second);
                    } catch (IllegalStateException e) {
                        this.mInfoListener.onInfo(this.mMediaPlayer, ImsReasonInfo.CODE_ECBM_NOT_SUPPORTED, STATE_IDLE);
                    }
                }
                this.mCurrentState = STATE_PREPARING;
                attachMediaController();
            } catch (IOException ex) {
                Log.w(this.TAG, "Unable to open content: " + this.mUri, ex);
                this.mCurrentState = STATE_ERROR;
                this.mTargetState = STATE_ERROR;
                this.mErrorListener.onError(this.mMediaPlayer, STATE_PREPARING, STATE_IDLE);
            } catch (IllegalArgumentException ex2) {
                Log.w(this.TAG, "Unable to open content: " + this.mUri, ex2);
                this.mCurrentState = STATE_ERROR;
                this.mTargetState = STATE_ERROR;
                this.mErrorListener.onError(this.mMediaPlayer, STATE_PREPARING, STATE_IDLE);
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

    public void setOnPreparedListener(OnPreparedListener l) {
        this.mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        this.mOnCompletionListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        this.mOnInfoListener = l;
    }

    private void release(boolean cleartargetstate) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mPendingSubtitleTracks.clear();
            this.mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                this.mTargetState = STATE_IDLE;
            }
            ((AudioManager) this.mContext.getSystemService("audio")).abandonAudioFocus(null);
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && this.mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && this.mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = (keyCode == STATE_PAUSED || keyCode == 24 || keyCode == 25 || keyCode == LogPower.AUDIO_SESSION_START || keyCode == 82 || keyCode == STATE_PLAYBACK_COMPLETED) ? false : keyCode != 6;
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
            } else if (keyCode == LogPower.TOUCH_UP) {
                if (!this.mMediaPlayer.isPlaying()) {
                    start();
                    this.mMediaController.hide();
                }
                return true;
            } else if (keyCode == 86 || keyCode == LogPower.MIME_TYPE) {
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
            this.mCurrentState = STATE_PLAYING;
        }
        this.mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            this.mCurrentState = STATE_PAUSED;
        }
        this.mTargetState = STATE_PAUSED;
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
        return STATE_ERROR;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return this.mMediaPlayer.getCurrentPosition();
        }
        return STATE_IDLE;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            this.mMediaPlayer.seekTo(msec);
            this.mSeekWhenPrepared = STATE_IDLE;
            return;
        }
        this.mSeekWhenPrepared = msec;
    }

    public boolean isPlaying() {
        return isInPlaybackState() ? this.mMediaPlayer.isPlaying() : false;
    }

    public int getBufferPercentage() {
        if (this.mMediaPlayer != null) {
            return this.mCurrentBufferPercentage;
        }
        return STATE_IDLE;
    }

    private boolean isInPlaybackState() {
        if (this.mMediaPlayer == null || this.mCurrentState == STATE_ERROR || this.mCurrentState == 0) {
            return false;
        }
        return this.mCurrentState != STATE_PREPARING;
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

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mSubtitleWidget != null) {
            this.mSubtitleWidget.onAttachedToWindow();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mSubtitleWidget != null) {
            this.mSubtitleWidget.onDetachedFromWindow();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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

    public void setSubtitleWidget(RenderingWidget subtitleWidget) {
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
                    this.mSubtitlesChangedListener = new OnChangedListener() {
                        public void onChanged(RenderingWidget renderingWidget) {
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

    protected void onSTCallbackSetPlayer(MediaPlayer mediaPlayer) {
    }

    protected void adjustIsBuffering(int arg1) {
    }

    protected void onBufferingUpdateOuter(MediaPlayer mp, int percent) {
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

    protected MediaPlayer getMediaPlayer() {
        return this.mMediaPlayer;
    }

    protected boolean isInPlaybackStateOuter() {
        return isInPlaybackState();
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
    }
}
