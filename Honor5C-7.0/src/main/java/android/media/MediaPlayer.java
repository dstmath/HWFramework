package android.media;

import android.app.ActivityThread;
import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hsm.MediaTransactWrapper;
import android.media.AudioAttributes.Builder;
import android.media.MediaTimeProvider.OnMediaTimeListener;
import android.media.SubtitleController.Anchor;
import android.media.SubtitleController.Listener;
import android.media.SubtitleTrack.RenderingWidget;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.security.keymaster.KeymasterDefs;
import android.service.voice.VoiceInteractionSession;
import android.speech.tts.TextToSpeech.Engine;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.BitSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import libcore.io.IoBridge;
import libcore.io.Libcore;

public class MediaPlayer extends PlayerBase implements Listener {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_SELECTED_TRACK = 7;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int KEY_PARAMETER_AUDIO_ATTRIBUTES = 1400;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_SYSTEM = Integer.MIN_VALUE;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int MEDIA_META_DATA = 202;
    public static final String MEDIA_MIMETYPE_TEXT_CEA_608 = "text/cea-608";
    public static final String MEDIA_MIMETYPE_TEXT_CEA_708 = "text/cea-708";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_VTT = "text/vtt";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PAUSED = 7;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_SKIPPED = 9;
    private static final int MEDIA_STARTED = 6;
    private static final int MEDIA_STOPPED = 8;
    private static final int MEDIA_SUBTITLE_DATA = 201;
    private static final int MEDIA_TIMED_TEXT = 99;
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int PLAYBACK_RATE_AUDIO_MODE_DEFAULT = 0;
    public static final int PLAYBACK_RATE_AUDIO_MODE_RESAMPLE = 2;
    public static final int PLAYBACK_RATE_AUDIO_MODE_STRETCH = 1;
    private static final String TAG = "MediaPlayer";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private boolean mBypassInterruptionPolicy;
    private EventHandler mEventHandler;
    private BitSet mInbandTrackIndices;
    private Vector<Pair<Integer, SubtitleTrack>> mIndexTrackPairs;
    private int mListenerContext;
    private long mNativeContext;
    private long mNativeSurfaceTexture;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnSubtitleDataListener mOnSubtitleDataListener;
    private OnTimedMetaDataAvailableListener mOnTimedMetaDataAvailableListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private Vector<InputStream> mOpenSubtitleSources;
    private boolean mScreenOnWhilePlaying;
    private int mSelectedSubtitleTrackIndex;
    private boolean mStayAwake;
    private int mStreamType;
    private SubtitleController mSubtitleController;
    private OnSubtitleDataListener mSubtitleDataListener;
    private SurfaceHolder mSurfaceHolder;
    private TimeProvider mTimeProvider;
    private int mUsage;
    private WakeLock mWakeLock;

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    public interface OnSubtitleDataListener {
        void onSubtitleData(MediaPlayer mediaPlayer, SubtitleData subtitleData);
    }

    /* renamed from: android.media.MediaPlayer.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ HandlerThread val$thread;

        AnonymousClass2(HandlerThread val$thread) {
            this.val$thread = val$thread;
        }

        public void run() {
            MediaPlayer.this.mSubtitleController = new SubtitleController(ActivityThread.currentApplication(), MediaPlayer.this.mTimeProvider, MediaPlayer.this);
            MediaPlayer.this.mSubtitleController.setAnchor(new Anchor() {
                public void setSubtitleWidget(RenderingWidget subtitleWidget) {
                }

                public Looper getSubtitleLooper() {
                    return Looper.getMainLooper();
                }
            });
            this.val$thread.getLooper().quitSafely();
        }
    }

    /* renamed from: android.media.MediaPlayer.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ MediaFormat val$fFormat;
        final /* synthetic */ InputStream val$fIs;
        final /* synthetic */ HandlerThread val$thread;

        AnonymousClass3(InputStream val$fIs, MediaFormat val$fFormat, HandlerThread val$thread) {
            this.val$fIs = val$fIs;
            this.val$fFormat = val$fFormat;
            this.val$thread = val$thread;
        }

        private int addTrack() {
            if (this.val$fIs == null || MediaPlayer.this.mSubtitleController == null) {
                return MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;
            }
            SubtitleTrack track = MediaPlayer.this.mSubtitleController.addTrack(this.val$fFormat);
            if (track == null) {
                return MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;
            }
            Scanner scanner = new Scanner(this.val$fIs, "UTF-8");
            String contents = scanner.useDelimiter("\\A").next();
            synchronized (MediaPlayer.this.mOpenSubtitleSources) {
                MediaPlayer.this.mOpenSubtitleSources.remove(this.val$fIs);
            }
            scanner.close();
            synchronized (MediaPlayer.this.mIndexTrackPairs) {
                MediaPlayer.this.mIndexTrackPairs.add(Pair.create(null, track));
            }
            Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
            h.sendMessage(h.obtainMessage(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT, MediaPlayer.MEDIA_SEEK_COMPLETE, MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT, Pair.create(track, contents.getBytes())));
            return MediaPlayer.MEDIA_INFO_EXTERNAL_METADATA_UPDATE;
        }

        public void run() {
            int res = addTrack();
            if (MediaPlayer.this.mEventHandler != null) {
                MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(MediaPlayer.MEDIA_INFO, res, MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT, null));
            }
            this.val$thread.getLooper().quitSafely();
        }
    }

    /* renamed from: android.media.MediaPlayer.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ FileDescriptor val$fd3;
        final /* synthetic */ long val$length2;
        final /* synthetic */ long val$offset2;
        final /* synthetic */ HandlerThread val$thread;
        final /* synthetic */ SubtitleTrack val$track;

        AnonymousClass4(FileDescriptor val$fd3, long val$offset2, long val$length2, SubtitleTrack val$track, HandlerThread val$thread) {
            this.val$fd3 = val$fd3;
            this.val$offset2 = val$offset2;
            this.val$length2 = val$length2;
            this.val$track = val$track;
            this.val$thread = val$thread;
        }

        private int addTrack() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                Libcore.os.lseek(this.val$fd3, this.val$offset2, OsConstants.SEEK_SET);
                byte[] buffer = new byte[StrictMode.DETECT_VM_REGISTRATION_LEAKS];
                long total = 0;
                while (true) {
                    if (total >= this.val$length2) {
                        break;
                    }
                    int bytesToRead = (int) Math.min((long) buffer.length, this.val$length2 - total);
                    int bytes = IoBridge.read(this.val$fd3, buffer, MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT, bytesToRead);
                    if (bytes < 0) {
                        break;
                    }
                    bos.write(buffer, MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT, bytes);
                    total += (long) bytes;
                }
                Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
                int i = MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
                int i2 = MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
                h.sendMessage(h.obtainMessage(i, MediaPlayer.MEDIA_SEEK_COMPLETE, i2, Pair.create(this.val$track, bos.toByteArray())));
                return MediaPlayer.MEDIA_INFO_EXTERNAL_METADATA_UPDATE;
            } catch (Exception e) {
                Log.e(MediaPlayer.TAG, e.getMessage(), e);
                return MediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR;
            }
        }

        public void run() {
            int res = addTrack();
            if (MediaPlayer.this.mEventHandler != null) {
                MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(MediaPlayer.MEDIA_INFO, res, MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT, null));
            }
            this.val$thread.getLooper().quitSafely();
        }
    }

    private class EventHandler extends Handler {
        private MediaPlayer mMediaPlayer;

        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(MediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            OnCompletionListener onCompletionListener;
            TimeProvider timeProvider;
            int i;
            Parcel parcel;
            switch (msg.what) {
                case MediaPlayer.PLAYBACK_RATE_AUDIO_MODE_DEFAULT /*0*/:
                    break;
                case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT /*1*/:
                    try {
                        MediaPlayer.this.scanInternalSubtitleTracks();
                    } catch (RuntimeException e) {
                        sendMessage(obtainMessage(MediaPlayer.MEDIA_ERROR_SERVER_DIED, MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                    }
                    OnPreparedListener onPreparedListener = MediaPlayer.this.mOnPreparedListener;
                    if (onPreparedListener != null) {
                        onPreparedListener.onPrepared(this.mMediaPlayer);
                    }
                case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING /*2*/:
                    onCompletionListener = MediaPlayer.this.mOnCompletionListener;
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    MediaPlayer.this.stayAwake(MediaPlayer.METADATA_ALL);
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START /*3*/:
                    OnBufferingUpdateListener onBufferingUpdateListener = MediaPlayer.this.mOnBufferingUpdateListener;
                    if (onBufferingUpdateListener != null) {
                        onBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
                    }
                case MediaPlayer.MEDIA_SEEK_COMPLETE /*4*/:
                    OnSeekCompleteListener onSeekCompleteListener = MediaPlayer.this.mOnSeekCompleteListener;
                    if (onSeekCompleteListener != null) {
                        onSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                        break;
                    }
                    break;
                case MediaPlayer.MEDIA_SET_VIDEO_SIZE /*5*/:
                    OnVideoSizeChangedListener onVideoSizeChangedListener = MediaPlayer.this.mOnVideoSizeChangedListener;
                    if (onVideoSizeChangedListener != null) {
                        onVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                case MediaPlayer.MEDIA_STARTED /*6*/:
                case MediaPlayer.MEDIA_PAUSED /*7*/:
                    timeProvider = MediaPlayer.this.mTimeProvider;
                    if (timeProvider != null) {
                        i = msg.what;
                        timeProvider.onPaused(r0 == MediaPlayer.MEDIA_PAUSED ? MediaPlayer.METADATA_UPDATE_ONLY : MediaPlayer.METADATA_ALL);
                        break;
                    }
                    break;
                case MediaPlayer.MEDIA_STOPPED /*8*/:
                    timeProvider = MediaPlayer.this.mTimeProvider;
                    if (timeProvider != null) {
                        timeProvider.onStopped();
                        break;
                    }
                    break;
                case MediaPlayer.MEDIA_SKIPPED /*9*/:
                    break;
                case MediaPlayer.MEDIA_TIMED_TEXT /*99*/:
                    OnTimedTextListener onTimedTextListener = MediaPlayer.this.mOnTimedTextListener;
                    if (onTimedTextListener != null) {
                        if (msg.obj == null) {
                            onTimedTextListener.onTimedText(this.mMediaPlayer, null);
                        } else {
                            if (msg.obj instanceof Parcel) {
                                parcel = msg.obj;
                                TimedText timedText = new TimedText(parcel);
                                parcel.recycle();
                                onTimedTextListener.onTimedText(this.mMediaPlayer, timedText);
                            }
                        }
                    }
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED /*100*/:
                    Log.e(MediaPlayer.TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = MediaPlayer.METADATA_ALL;
                    OnErrorListener onErrorListener = MediaPlayer.this.mOnErrorListener;
                    if (onErrorListener != null) {
                        error_was_handled = onErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    onCompletionListener = MediaPlayer.this.mOnCompletionListener;
                    if (!(onCompletionListener == null || r9)) {
                        onCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    MediaPlayer.this.stayAwake(MediaPlayer.METADATA_ALL);
                case MediaPlayer.MEDIA_INFO /*200*/:
                    switch (msg.arg1) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING /*700*/:
                            Log.i(MediaPlayer.TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START /*701*/:
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END /*702*/:
                            timeProvider = MediaPlayer.this.mTimeProvider;
                            if (timeProvider != null) {
                                i = msg.arg1;
                                timeProvider.onBuffering(r0 == MediaPlayer.MEDIA_INFO_BUFFERING_START ? MediaPlayer.METADATA_UPDATE_ONLY : MediaPlayer.METADATA_ALL);
                                break;
                            }
                            break;
                        case MediaPlayer.MEDIA_INFO_METADATA_UPDATE /*802*/:
                            try {
                                MediaPlayer.this.scanInternalSubtitleTracks();
                                break;
                            } catch (RuntimeException e2) {
                                sendMessage(obtainMessage(MediaPlayer.MEDIA_ERROR_SERVER_DIED, MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                                break;
                            }
                        case MediaPlayer.MEDIA_INFO_EXTERNAL_METADATA_UPDATE /*803*/:
                            break;
                    }
                    msg.arg1 = MediaPlayer.MEDIA_INFO_METADATA_UPDATE;
                    if (MediaPlayer.this.mSubtitleController != null) {
                        MediaPlayer.this.mSubtitleController.selectDefaultTrack();
                    }
                    OnInfoListener onInfoListener = MediaPlayer.this.mOnInfoListener;
                    if (onInfoListener != null) {
                        onInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                case MediaPlayer.MEDIA_SUBTITLE_DATA /*201*/:
                    OnSubtitleDataListener onSubtitleDataListener = MediaPlayer.this.mOnSubtitleDataListener;
                    if (onSubtitleDataListener != null) {
                        if (msg.obj instanceof Parcel) {
                            parcel = (Parcel) msg.obj;
                            SubtitleData data = new SubtitleData(parcel);
                            parcel.recycle();
                            onSubtitleDataListener.onSubtitleData(this.mMediaPlayer, data);
                        }
                    }
                case MediaPlayer.MEDIA_META_DATA /*202*/:
                    OnTimedMetaDataAvailableListener onTimedMetaDataAvailableListener = MediaPlayer.this.mOnTimedMetaDataAvailableListener;
                    if (onTimedMetaDataAvailableListener != null) {
                        if (msg.obj instanceof Parcel) {
                            parcel = (Parcel) msg.obj;
                            TimedMetaData data2 = TimedMetaData.createTimedMetaDataFromParcel(parcel);
                            parcel.recycle();
                            onTimedMetaDataAvailableListener.onTimedMetaDataAvailable(this.mMediaPlayer, data2);
                        }
                    }
                default:
                    Log.e(MediaPlayer.TAG, "Unknown message type " + msg.what);
            }
        }
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnTimedMetaDataAvailableListener {
        void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData);
    }

    public interface OnTimedTextListener {
        void onTimedText(MediaPlayer mediaPlayer, TimedText timedText);
    }

    static class TimeProvider implements OnSeekCompleteListener, MediaTimeProvider {
        private static final long MAX_EARLY_CALLBACK_US = 1000;
        private static final long MAX_NS_WITHOUT_POSITION_CHECK = 5000000000L;
        private static final int NOTIFY = 1;
        private static final int NOTIFY_SEEK = 3;
        private static final int NOTIFY_STOP = 2;
        private static final int NOTIFY_TIME = 0;
        private static final int NOTIFY_TRACK_DATA = 4;
        private static final int REFRESH_AND_NOTIFY_TIME = 1;
        private static final String TAG = "MTP";
        private static final long TIME_ADJUSTMENT_RATE = 2;
        public boolean DEBUG;
        private boolean mBuffering;
        private Handler mEventHandler;
        private HandlerThread mHandlerThread;
        private long mLastNanoTime;
        private long mLastReportedTime;
        private long mLastTimeUs;
        private OnMediaTimeListener[] mListeners;
        private boolean mPaused;
        private boolean mPausing;
        private MediaPlayer mPlayer;
        private boolean mRefresh;
        private boolean mSeeking;
        private boolean mStopped;
        private long mTimeAdjustment;
        private long[] mTimes;

        private class EventHandler extends Handler {
            public EventHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == TimeProvider.REFRESH_AND_NOTIFY_TIME) {
                    switch (msg.arg1) {
                        case TimeProvider.NOTIFY_TIME /*0*/:
                            TimeProvider.this.notifyTimedEvent(MediaPlayer.METADATA_ALL);
                        case TimeProvider.REFRESH_AND_NOTIFY_TIME /*1*/:
                            TimeProvider.this.notifyTimedEvent(MediaPlayer.METADATA_UPDATE_ONLY);
                        case TimeProvider.NOTIFY_STOP /*2*/:
                            TimeProvider.this.notifyStop();
                        case TimeProvider.NOTIFY_SEEK /*3*/:
                            TimeProvider.this.notifySeek();
                        case TimeProvider.NOTIFY_TRACK_DATA /*4*/:
                            TimeProvider.this.notifyTrackData((Pair) msg.obj);
                        default:
                    }
                }
            }
        }

        public TimeProvider(MediaPlayer mp) {
            this.mLastTimeUs = 0;
            this.mPaused = MediaPlayer.METADATA_UPDATE_ONLY;
            this.mStopped = MediaPlayer.METADATA_UPDATE_ONLY;
            this.mRefresh = MediaPlayer.METADATA_ALL;
            this.mPausing = MediaPlayer.METADATA_ALL;
            this.mSeeking = MediaPlayer.METADATA_ALL;
            this.DEBUG = MediaPlayer.METADATA_ALL;
            this.mPlayer = mp;
            try {
                getCurrentTimeUs(MediaPlayer.METADATA_UPDATE_ONLY, MediaPlayer.METADATA_ALL);
            } catch (IllegalStateException e) {
                this.mRefresh = MediaPlayer.METADATA_UPDATE_ONLY;
            }
            Looper looper = Looper.myLooper();
            if (looper == null) {
                looper = Looper.getMainLooper();
                if (looper == null) {
                    this.mHandlerThread = new HandlerThread("MediaPlayerMTPEventThread", -2);
                    this.mHandlerThread.start();
                    looper = this.mHandlerThread.getLooper();
                }
            }
            this.mEventHandler = new EventHandler(looper);
            this.mListeners = new OnMediaTimeListener[NOTIFY_TIME];
            this.mTimes = new long[NOTIFY_TIME];
            this.mLastTimeUs = 0;
            this.mTimeAdjustment = 0;
        }

        private void scheduleNotification(int type, long delayUs) {
            if (!this.mSeeking || (type != 0 && type != REFRESH_AND_NOTIFY_TIME)) {
                if (this.DEBUG) {
                    Log.v(TAG, "scheduleNotification " + type + " in " + delayUs);
                }
                this.mEventHandler.removeMessages(REFRESH_AND_NOTIFY_TIME);
                this.mEventHandler.sendMessageDelayed(this.mEventHandler.obtainMessage(REFRESH_AND_NOTIFY_TIME, type, NOTIFY_TIME), (long) ((int) (delayUs / MAX_EARLY_CALLBACK_US)));
            }
        }

        public void close() {
            this.mEventHandler.removeMessages(REFRESH_AND_NOTIFY_TIME);
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
            }
        }

        protected void finalize() {
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
        }

        public void onPaused(boolean paused) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onPaused: " + paused);
                }
                if (this.mStopped) {
                    this.mStopped = MediaPlayer.METADATA_ALL;
                    this.mSeeking = MediaPlayer.METADATA_UPDATE_ONLY;
                    scheduleNotification(NOTIFY_SEEK, 0);
                } else {
                    this.mPausing = paused;
                    this.mSeeking = MediaPlayer.METADATA_ALL;
                    scheduleNotification(REFRESH_AND_NOTIFY_TIME, 0);
                }
            }
        }

        public void onBuffering(boolean buffering) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onBuffering: " + buffering);
                }
                this.mBuffering = buffering;
                scheduleNotification(REFRESH_AND_NOTIFY_TIME, 0);
            }
        }

        public void onStopped() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onStopped");
                }
                this.mPaused = MediaPlayer.METADATA_UPDATE_ONLY;
                this.mStopped = MediaPlayer.METADATA_UPDATE_ONLY;
                this.mSeeking = MediaPlayer.METADATA_ALL;
                this.mBuffering = MediaPlayer.METADATA_ALL;
                scheduleNotification(NOTIFY_STOP, 0);
            }
        }

        public void onSeekComplete(MediaPlayer mp) {
            synchronized (this) {
                this.mStopped = MediaPlayer.METADATA_ALL;
                this.mSeeking = MediaPlayer.METADATA_UPDATE_ONLY;
                scheduleNotification(NOTIFY_SEEK, 0);
            }
        }

        public void onNewPlayer() {
            if (this.mRefresh) {
                synchronized (this) {
                    this.mStopped = MediaPlayer.METADATA_ALL;
                    this.mSeeking = MediaPlayer.METADATA_UPDATE_ONLY;
                    this.mBuffering = MediaPlayer.METADATA_ALL;
                    scheduleNotification(NOTIFY_SEEK, 0);
                }
            }
        }

        private synchronized void notifySeek() {
            synchronized (this) {
                this.mSeeking = MediaPlayer.METADATA_ALL;
                try {
                    long timeUs = getCurrentTimeUs(MediaPlayer.METADATA_UPDATE_ONLY, MediaPlayer.METADATA_ALL);
                    if (this.DEBUG) {
                        Log.d(TAG, "onSeekComplete at " + timeUs);
                    }
                    OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
                    int length = onMediaTimeListenerArr.length;
                    for (int i = NOTIFY_TIME; i < length; i += REFRESH_AND_NOTIFY_TIME) {
                        OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                        if (listener == null) {
                            break;
                        }
                        listener.onSeek(timeUs);
                    }
                } catch (IllegalStateException e) {
                    if (this.DEBUG) {
                        Log.d(TAG, "onSeekComplete but no player");
                    }
                    this.mPausing = MediaPlayer.METADATA_UPDATE_ONLY;
                    notifyTimedEvent(MediaPlayer.METADATA_ALL);
                }
            }
        }

        private synchronized void notifyTrackData(Pair<SubtitleTrack, byte[]> trackData) {
            trackData.first.onData(trackData.second, MediaPlayer.METADATA_UPDATE_ONLY, -1);
        }

        private synchronized void notifyStop() {
            OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
            int length = onMediaTimeListenerArr.length;
            for (int i = NOTIFY_TIME; i < length; i += REFRESH_AND_NOTIFY_TIME) {
                OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                if (listener == null) {
                    break;
                }
                listener.onStop();
            }
        }

        private int registerListener(OnMediaTimeListener listener) {
            int i = NOTIFY_TIME;
            while (i < this.mListeners.length && this.mListeners[i] != listener && this.mListeners[i] != null) {
                i += REFRESH_AND_NOTIFY_TIME;
            }
            if (i >= this.mListeners.length) {
                OnMediaTimeListener[] newListeners = new OnMediaTimeListener[(i + REFRESH_AND_NOTIFY_TIME)];
                long[] newTimes = new long[(i + REFRESH_AND_NOTIFY_TIME)];
                System.arraycopy(this.mListeners, NOTIFY_TIME, newListeners, NOTIFY_TIME, this.mListeners.length);
                System.arraycopy(this.mTimes, NOTIFY_TIME, newTimes, NOTIFY_TIME, this.mTimes.length);
                this.mListeners = newListeners;
                this.mTimes = newTimes;
            }
            if (this.mListeners[i] == null) {
                this.mListeners[i] = listener;
                this.mTimes[i] = -1;
            }
            return i;
        }

        public void notifyAt(long timeUs, OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "notifyAt " + timeUs);
                }
                this.mTimes[registerListener(listener)] = timeUs;
                scheduleNotification(NOTIFY_TIME, 0);
            }
        }

        public void scheduleUpdate(OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "scheduleUpdate");
                }
                int i = registerListener(listener);
                if (!this.mStopped) {
                    this.mTimes[i] = 0;
                    scheduleNotification(NOTIFY_TIME, 0);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void cancelNotifications(OnMediaTimeListener listener) {
            synchronized (this) {
                int i = NOTIFY_TIME;
                while (true) {
                    if (i < this.mListeners.length) {
                        if (this.mListeners[i] != listener) {
                            if (this.mListeners[i] == null) {
                                break;
                            }
                            i += REFRESH_AND_NOTIFY_TIME;
                        } else {
                            break;
                        }
                    }
                    break;
                }
                scheduleNotification(NOTIFY_TIME, 0);
            }
        }

        private synchronized void notifyTimedEvent(boolean refreshTime) {
            long nowUs;
            try {
                nowUs = getCurrentTimeUs(refreshTime, MediaPlayer.METADATA_UPDATE_ONLY);
            } catch (IllegalStateException e) {
                this.mRefresh = MediaPlayer.METADATA_UPDATE_ONLY;
                this.mPausing = MediaPlayer.METADATA_UPDATE_ONLY;
                nowUs = getCurrentTimeUs(refreshTime, MediaPlayer.METADATA_UPDATE_ONLY);
            }
            long nextTimeUs = nowUs;
            if (!this.mSeeking) {
                if (this.DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    String str = " -> ";
                    str = ") from {";
                    sb.append("notifyTimedEvent(").append(this.mLastTimeUs).append(r18).append(nowUs).append(r18);
                    boolean first = MediaPlayer.METADATA_UPDATE_ONLY;
                    long[] jArr = this.mTimes;
                    int length = jArr.length;
                    for (int i = NOTIFY_TIME; i < length; i += REFRESH_AND_NOTIFY_TIME) {
                        long time = jArr[i];
                        if (time != -1) {
                            if (!first) {
                                sb.append(", ");
                            }
                            sb.append(time);
                            first = MediaPlayer.METADATA_ALL;
                        }
                    }
                    sb.append("}");
                    Log.d(TAG, sb.toString());
                }
                Vector<OnMediaTimeListener> activatedListeners = new Vector();
                int ix = NOTIFY_TIME;
                while (ix < this.mTimes.length && this.mListeners[ix] != null) {
                    if (this.mTimes[ix] > -1) {
                        if (this.mTimes[ix] <= MAX_EARLY_CALLBACK_US + nowUs) {
                            activatedListeners.add(this.mListeners[ix]);
                            if (this.DEBUG) {
                                Log.d(TAG, Environment.MEDIA_REMOVED);
                            }
                            this.mTimes[ix] = -1;
                        } else if (nextTimeUs == nowUs || this.mTimes[ix] < nextTimeUs) {
                            nextTimeUs = this.mTimes[ix];
                        }
                    }
                    ix += REFRESH_AND_NOTIFY_TIME;
                }
                if (nextTimeUs <= nowUs || this.mPaused) {
                    this.mEventHandler.removeMessages(REFRESH_AND_NOTIFY_TIME);
                } else {
                    if (this.DEBUG) {
                        Log.d(TAG, "scheduling for " + nextTimeUs + " and " + nowUs);
                    }
                    scheduleNotification(NOTIFY_TIME, nextTimeUs - nowUs);
                }
                for (OnMediaTimeListener listener : activatedListeners) {
                    listener.onTimedEvent(nowUs);
                }
            }
        }

        private long getEstimatedTime(long nanoTime, boolean monotonic) {
            if (this.mPaused) {
                this.mLastReportedTime = this.mLastTimeUs + this.mTimeAdjustment;
            } else {
                long timeSinceRead = (nanoTime - this.mLastNanoTime) / MAX_EARLY_CALLBACK_US;
                this.mLastReportedTime = this.mLastTimeUs + timeSinceRead;
                if (this.mTimeAdjustment > 0) {
                    long adjustment = this.mTimeAdjustment - (timeSinceRead / TIME_ADJUSTMENT_RATE);
                    if (adjustment <= 0) {
                        this.mTimeAdjustment = 0;
                    } else {
                        this.mLastReportedTime += adjustment;
                    }
                }
            }
            return this.mLastReportedTime;
        }

        public long getCurrentTimeUs(boolean refreshTime, boolean monotonic) throws IllegalStateException {
            boolean z = MediaPlayer.METADATA_UPDATE_ONLY;
            synchronized (this) {
                if (!this.mPaused || refreshTime) {
                    long nanoTime = System.nanoTime();
                    if (refreshTime || nanoTime >= this.mLastNanoTime + MAX_NS_WITHOUT_POSITION_CHECK) {
                        try {
                            this.mLastTimeUs = ((long) this.mPlayer.getCurrentPosition()) * MAX_EARLY_CALLBACK_US;
                            if (this.mPlayer.isPlaying()) {
                                z = this.mBuffering;
                            }
                            this.mPaused = z;
                            if (this.DEBUG) {
                                String str;
                                String str2 = TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                if (this.mPaused) {
                                    str = "paused";
                                } else {
                                    str = "playing";
                                }
                                Log.v(str2, stringBuilder.append(str).append(" at ").append(this.mLastTimeUs).toString());
                            }
                            this.mLastNanoTime = nanoTime;
                            if (!monotonic || this.mLastTimeUs >= this.mLastReportedTime) {
                                this.mTimeAdjustment = 0;
                            } else {
                                this.mTimeAdjustment = this.mLastReportedTime - this.mLastTimeUs;
                                if (this.mTimeAdjustment > 1000000) {
                                    this.mStopped = MediaPlayer.METADATA_ALL;
                                    this.mSeeking = MediaPlayer.METADATA_UPDATE_ONLY;
                                    scheduleNotification(NOTIFY_SEEK, 0);
                                }
                            }
                        } catch (IllegalStateException e) {
                            if (this.mPausing) {
                                this.mPausing = MediaPlayer.METADATA_ALL;
                                getEstimatedTime(nanoTime, monotonic);
                                this.mPaused = MediaPlayer.METADATA_UPDATE_ONLY;
                                if (this.DEBUG) {
                                    Log.d(TAG, "illegal state, but pausing: estimating at " + this.mLastReportedTime);
                                }
                                return this.mLastReportedTime;
                            }
                            throw e;
                        }
                    }
                    long estimatedTime = getEstimatedTime(nanoTime, monotonic);
                    return estimatedTime;
                }
                estimatedTime = this.mLastReportedTime;
                return estimatedTime;
            }
        }
    }

    public static class TrackInfo implements Parcelable {
        static final Creator<TrackInfo> CREATOR = null;
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final MediaFormat mFormat;
        final int mTrackType;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaPlayer.TrackInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaPlayer.TrackInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.TrackInfo.<clinit>():void");
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String language = this.mFormat.getString(Engine.KEY_PARAM_LANGUAGE);
            return language == null ? "und" : language;
        }

        public MediaFormat getFormat() {
            if (this.mTrackType == MEDIA_TRACK_TYPE_TIMEDTEXT || this.mTrackType == MEDIA_TRACK_TYPE_SUBTITLE) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfo(Parcel in) {
            this.mTrackType = in.readInt();
            this.mFormat = MediaFormat.createSubtitleFormat(in.readString(), in.readString());
            if (this.mTrackType == MEDIA_TRACK_TYPE_SUBTITLE) {
                this.mFormat.setInteger(MediaFormat.KEY_IS_AUTOSELECT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_DEFAULT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, in.readInt());
            }
        }

        TrackInfo(int type, MediaFormat format) {
            this.mTrackType = type;
            this.mFormat = format;
        }

        public int describeContents() {
            return MEDIA_TRACK_TYPE_UNKNOWN;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(getLanguage());
            if (this.mTrackType == MEDIA_TRACK_TYPE_SUBTITLE) {
                dest.writeString(this.mFormat.getString(MediaFormat.KEY_MIME));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_AUTOSELECT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_DEFAULT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE));
            }
        }

        public String toString() {
            StringBuilder out = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
            out.append(getClass().getName());
            out.append('{');
            switch (this.mTrackType) {
                case MEDIA_TRACK_TYPE_VIDEO /*1*/:
                    out.append("VIDEO");
                    break;
                case MEDIA_TRACK_TYPE_AUDIO /*2*/:
                    out.append("AUDIO");
                    break;
                case MEDIA_TRACK_TYPE_TIMEDTEXT /*3*/:
                    out.append("TIMEDTEXT");
                    break;
                case MEDIA_TRACK_TYPE_SUBTITLE /*4*/:
                    out.append("SUBTITLE");
                    break;
                default:
                    out.append("UNKNOWN");
                    break;
            }
            out.append(", ").append(this.mFormat.toString());
            out.append("}");
            return out.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaPlayer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaPlayer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.<clinit>():void");
    }

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _pause() throws IllegalStateException;

    private native void _prepare() throws IOException, IllegalStateException;

    private native void _release();

    private native void _reset();

    private native void _setAudioStreamType(int i);

    private native void _setAuxEffectSendLevel(float f);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException, IllegalStateException;

    private native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _setVolume(float f, float f2);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    private final native void native_finalize();

    private final native boolean native_getMetadata(boolean z, boolean z2, Parcel parcel);

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    public static native int native_pullBatteryData(Parcel parcel);

    private final native int native_setMetadataFilter(Parcel parcel);

    private final native int native_setRetransmitEndpoint(String str, int i);

    private final native void native_setup(Object obj);

    private native boolean setParameter(int i, Parcel parcel);

    public native void attachAuxEffect(int i);

    public native int getAudioSessionId();

    public native int getCurrentPosition();

    public native int getDuration();

    public native PlaybackParams getPlaybackParams();

    public native SyncParams getSyncParams();

    public native int getVideoHeight();

    public native int getVideoWidth();

    public native boolean isLooping();

    public native boolean isPlaying();

    public native void prepareAsync() throws IllegalStateException;

    public native void seekTo(int i) throws IllegalStateException;

    public native void setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    public native void setLooping(boolean z);

    public native void setNextMediaPlayer(MediaPlayer mediaPlayer);

    public native void setPlaybackParams(PlaybackParams playbackParams);

    public native void setSyncParams(SyncParams syncParams);

    public MediaPlayer() {
        super(new Builder().build());
        this.mWakeLock = null;
        this.mStreamType = MEDIA_ERROR_SYSTEM;
        this.mUsage = -1;
        this.mIndexTrackPairs = new Vector();
        this.mInbandTrackIndices = new BitSet();
        this.mSelectedSubtitleTrackIndex = -1;
        this.mSubtitleDataListener = new OnSubtitleDataListener() {
            public void onSubtitleData(MediaPlayer mp, SubtitleData data) {
                int index = data.getTrackIndex();
                synchronized (MediaPlayer.this.mIndexTrackPairs) {
                    for (Pair<Integer, SubtitleTrack> p : MediaPlayer.this.mIndexTrackPairs) {
                        if (!(p.first == null || ((Integer) p.first).intValue() != index || p.second == null)) {
                            p.second.onData(data);
                        }
                    }
                }
            }
        };
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mTimeProvider = new TimeProvider(this);
        this.mOpenSubtitleSources = new Vector();
        native_setup(new WeakReference(this));
    }

    public Parcel newRequest() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(IMEDIA_PLAYER);
        return parcel;
    }

    public void invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(PLAYBACK_RATE_AUDIO_MODE_DEFAULT);
        if (retcode != 0) {
            throw new RuntimeException("failure code: " + retcode);
        }
    }

    public void setDisplay(SurfaceHolder sh) {
        Surface surface;
        this.mSurfaceHolder = sh;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    public void setSurface(Surface surface) {
        if (this.mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        this.mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    public void setVideoScalingMode(int mode) {
        if (isVideoScalingModeSupported(mode)) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInterfaceToken(IMEDIA_PLAYER);
                request.writeInt(MEDIA_STARTED);
                request.writeInt(mode);
                invoke(request, reply);
            } finally {
                request.recycle();
                reply.recycle();
            }
        } else {
            throw new IllegalArgumentException("Scaling mode " + mode + " is not supported");
        }
    }

    public static MediaPlayer create(Context context, Uri uri) {
        return create(context, uri, null);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        int s = AudioSystem.newAudioSessionId();
        if (s <= 0) {
            s = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
        }
        return create(context, uri, holder, null, s);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            AudioAttributes aa;
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
            mp.setDataSource(context, uri);
            if (holder != null) {
                mp.setDisplay(holder);
            }
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public static MediaPlayer create(Context context, int resid) {
        int s = AudioSystem.newAudioSessionId();
        if (s <= 0) {
            s = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
        }
        return create(context, resid, null, s);
    }

    public static MediaPlayer create(Context context, int resid, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) {
                return null;
            }
            AudioAttributes aa;
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        ContentResolver resolver = context.getContentResolver();
        String scheme = uri.getScheme();
        if (WifiManager.EXTRA_PASSPOINT_ICON_FILE.equals(scheme)) {
            setDataSource(uri.getPath());
            return;
        }
        if (VoiceInteractionSession.KEY_CONTENT.equals(scheme) && SettingsEx.AUTHORITY.equals(uri.getAuthority())) {
            int type = RingtoneManager.getDefaultType(uri);
            Uri cacheUri = RingtoneManager.getCacheForType(type);
            Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
            if (!attemptDataSource(resolver, cacheUri) && !attemptDataSource(resolver, actualUri)) {
                setDataSource(uri.toString(), (Map) headers);
            }
        } else if (!attemptDataSource(resolver, uri)) {
            setDataSource(uri.toString(), (Map) headers);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean attemptDataSource(ContentResolver resolver, Uri uri) {
        Throwable th;
        Throwable th2 = null;
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = resolver.openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            setDataSource(assetFileDescriptor);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return METADATA_UPDATE_ONLY;
            }
            try {
                throw th2;
            } catch (Exception ex) {
                Log.w(TAG, "Couldn't open " + uri + ": " + ex);
                return METADATA_ALL;
            }
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, null, null);
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
            for (Entry<String, String> entry : headers.entrySet()) {
                keys[i] = (String) entry.getKey();
                values[i] = (String) entry.getValue();
                i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
            }
        }
        setDataSource(path, keys, values);
    }

    private void setDataSource(String path, String[] keys, String[] values) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (WifiManager.EXTRA_PASSPOINT_ICON_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (scheme != null) {
            nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, keys, values);
            return;
        }
        File file = new File(path);
        if (!file.exists() || path.toLowerCase(Locale.getDefault()).endsWith(".sdp")) {
            nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, keys, values);
        } else {
            FileInputStream is = new FileInputStream(file);
            try {
                setDataSource(is.getFD());
                is.close();
            } catch (SecurityException ex) {
                Log.d(TAG, "setDataSource failed:", ex);
                throw ex;
            } catch (IOException ex2) {
                Log.d(TAG, "setDataSource failed:", ex2);
                throw ex2;
            } catch (IllegalArgumentException ex3) {
                Log.d(TAG, "setDataSource failed:", ex3);
                throw ex3;
            } catch (IllegalStateException ex4) {
                Log.d(TAG, "setDataSource failed:", ex4);
                throw ex4;
            } catch (Throwable th) {
                is.close();
            }
        }
    }

    public void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
        } else {
            setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
        }
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        _setDataSource(fd, offset, length);
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        _setDataSource(dataSource);
    }

    public void prepare() throws IOException, IllegalStateException {
        _prepare();
        scanInternalSubtitleTracks();
    }

    public void start() throws IllegalStateException {
        baseStart();
        stayAwake(METADATA_UPDATE_ONLY);
        _start();
    }

    private int getAudioStreamType() {
        if (this.mStreamType == MEDIA_ERROR_SYSTEM) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() throws IllegalStateException {
        stayAwake(METADATA_ALL);
        _stop();
    }

    public void pause() throws IllegalStateException {
        stayAwake(METADATA_ALL);
        _pause();
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = METADATA_ALL;
        if (SystemProperties.getBoolean("audio.offload.ignore_setawake", METADATA_ALL)) {
            Log.w(TAG, "IGNORING setWakeMode " + mode);
            return;
        }
        if (this.mWakeLock != null) {
            if (this.mWakeLock.isHeld()) {
                washeld = METADATA_UPDATE_ONLY;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(KeymasterDefs.KM_ENUM_REP | mode, MediaPlayer.class.getName());
        this.mWakeLock.setReferenceCounted(METADATA_ALL);
        if (washeld) {
            this.mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (this.mScreenOnWhilePlaying != screenOn) {
            if (screenOn && this.mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        if (this.mWakeLock != null) {
            if (awake && !this.mWakeLock.isHeld()) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        if (this.mStayAwake) {
            Log.i(TAG, "[HSM] stayAwake true uid: " + Process.myUid() + ", pid: " + Process.myPid());
            MediaTransactWrapper.musicPlaying(Process.myUid(), Process.myPid());
        } else {
            Log.i(TAG, "[HSM] stayAwake false uid: " + Process.myUid() + ", pid: " + Process.myPid());
            MediaTransactWrapper.musicPausedOrStopped(Process.myUid(), Process.myPid());
        }
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (this.mSurfaceHolder != null) {
            this.mSurfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying ? this.mStayAwake : METADATA_ALL);
        }
    }

    public PlaybackParams easyPlaybackParams(float rate, int audioMode) {
        PlaybackParams params = new PlaybackParams();
        params.allowDefaults();
        switch (audioMode) {
            case PLAYBACK_RATE_AUDIO_MODE_DEFAULT /*0*/:
                params.setSpeed(rate).setPitch(Engine.DEFAULT_VOLUME);
                break;
            case VIDEO_SCALING_MODE_SCALE_TO_FIT /*1*/:
                params.setSpeed(rate).setPitch(Engine.DEFAULT_VOLUME).setAudioFallbackMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                break;
            case VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING /*2*/:
                params.setSpeed(rate).setPitch(rate);
                break;
            default:
                throw new IllegalArgumentException("Audio playback mode " + audioMode + " is not supported");
        }
        return params;
    }

    public MediaTimestamp getTimestamp() {
        try {
            return new MediaTimestamp(((long) getCurrentPosition()) * 1000, System.nanoTime(), isPlaying() ? getPlaybackParams().getSpeed() : 0.0f);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        Parcel reply = Parcel.obtain();
        Metadata data = new Metadata();
        if (!native_getMetadata(update_only, apply_filter, reply)) {
            reply.recycle();
            return null;
        } else if (data.parse(reply)) {
            return data;
        } else {
            reply.recycle();
            return null;
        }
    }

    public int setMetadataFilter(Set<Integer> allow, Set<Integer> block) {
        Parcel request = newRequest();
        int capacity = request.dataSize() + ((((allow.size() + VIDEO_SCALING_MODE_SCALE_TO_FIT) + VIDEO_SCALING_MODE_SCALE_TO_FIT) + block.size()) * MEDIA_SEEK_COMPLETE);
        if (request.dataCapacity() < capacity) {
            request.setDataCapacity(capacity);
        }
        request.writeInt(allow.size());
        for (Integer t : allow) {
            request.writeInt(t.intValue());
        }
        request.writeInt(block.size());
        for (Integer t2 : block) {
            request.writeInt(t2.intValue());
        }
        return native_setMetadataFilter(request);
    }

    public void release() {
        baseRelease();
        stayAwake(METADATA_ALL);
        updateSurfaceScreenOn();
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnTimedTextListener = null;
        if (this.mTimeProvider != null) {
            this.mTimeProvider.close();
            this.mTimeProvider = null;
        }
        this.mOnSubtitleDataListener = null;
        _release();
    }

    public void reset() {
        this.mSelectedSubtitleTrackIndex = -1;
        synchronized (this.mOpenSubtitleSources) {
            for (InputStream is : this.mOpenSubtitleSources) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            this.mOpenSubtitleSources.clear();
        }
        if (this.mSubtitleController != null) {
            this.mSubtitleController.reset();
        }
        if (this.mTimeProvider != null) {
            this.mTimeProvider.close();
            this.mTimeProvider = null;
        }
        stayAwake(METADATA_ALL);
        _reset();
        if (this.mEventHandler != null) {
            this.mEventHandler.removeCallbacksAndMessages(null);
        }
        synchronized (this.mIndexTrackPairs) {
            this.mIndexTrackPairs.clear();
            this.mInbandTrackIndices.clear();
        }
    }

    public void setAudioStreamType(int streamtype) {
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(streamtype), TAG);
        baseUpdateAudioAttributes(new Builder().setInternalLegacyStreamType(streamtype).build());
        _setAudioStreamType(streamtype);
        this.mStreamType = streamtype;
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        boolean z = METADATA_ALL;
        if (attributes == null) {
            String msg = "Cannot set AudioAttributes to null";
            throw new IllegalArgumentException("Cannot set AudioAttributes to null");
        }
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(AudioAttributes.toLegacyStreamType(attributes)), TAG);
        baseUpdateAudioAttributes(attributes);
        this.mUsage = attributes.getUsage();
        if ((attributes.getAllFlags() & 64) != 0) {
            z = METADATA_UPDATE_ONLY;
        }
        this.mBypassInterruptionPolicy = z;
        Parcel pattributes = Parcel.obtain();
        attributes.writeToParcel(pattributes, VIDEO_SCALING_MODE_SCALE_TO_FIT);
        setParameter(KEY_PARAMETER_AUDIO_ATTRIBUTES, pattributes);
        pattributes.recycle();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        baseSetVolume(leftVolume, rightVolume);
    }

    void playerSetVolume(float leftVolume, float rightVolume) {
        _setVolume(leftVolume, rightVolume);
    }

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    public void setAuxEffectSendLevel(float level) {
        baseSetAuxEffectSendLevel(level);
    }

    int playerSetAuxEffectSendLevel(float level) {
        _setAuxEffectSendLevel(level);
        return PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        TrackInfo[] allTrackInfo;
        TrackInfo[] trackInfo = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            allTrackInfo = new TrackInfo[this.mIndexTrackPairs.size()];
            for (int i = PLAYBACK_RATE_AUDIO_MODE_DEFAULT; i < allTrackInfo.length; i += VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(i);
                if (p.first != null) {
                    allTrackInfo[i] = trackInfo[((Integer) p.first).intValue()];
                } else {
                    SubtitleTrack track = p.second;
                    allTrackInfo[i] = new TrackInfo(track.getTrackType(), track.getFormat());
                }
            }
        }
        return allTrackInfo;
    }

    private TrackInfo[] getInbandTrackInfo() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(VIDEO_SCALING_MODE_SCALE_TO_FIT);
            invoke(request, reply);
            TrackInfo[] trackInfo = (TrackInfo[]) reply.createTypedArray(TrackInfo.CREATOR);
            return trackInfo;
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if (MEDIA_MIMETYPE_TEXT_SUBRIP.equals(mimeType)) {
            return METADATA_UPDATE_ONLY;
        }
        return METADATA_ALL;
    }

    public void setSubtitleAnchor(SubtitleController controller, Anchor anchor) {
        this.mSubtitleController = controller;
        this.mSubtitleController.setAnchor(anchor);
    }

    private synchronized void setSubtitleAnchor() {
        if (this.mSubtitleController == null) {
            HandlerThread thread = new HandlerThread("SetSubtitleAnchorThread");
            thread.start();
            new Handler(thread.getLooper()).post(new AnonymousClass2(thread));
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "failed to join SetSubtitleAnchorThread");
            }
        }
    }

    public void onSubtitleTrackSelected(SubtitleTrack track) {
        if (this.mSelectedSubtitleTrackIndex >= 0) {
            try {
                selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, METADATA_ALL);
            } catch (IllegalStateException e) {
            }
            this.mSelectedSubtitleTrackIndex = -1;
        }
        setOnSubtitleDataListener(null);
        if (track != null) {
            synchronized (this.mIndexTrackPairs) {
                for (Pair<Integer, SubtitleTrack> p : this.mIndexTrackPairs) {
                    if (p.first != null && p.second == track) {
                        this.mSelectedSubtitleTrackIndex = ((Integer) p.first).intValue();
                        break;
                    }
                }
            }
            if (this.mSelectedSubtitleTrackIndex >= 0) {
                try {
                    selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, METADATA_UPDATE_ONLY);
                } catch (IllegalStateException e2) {
                }
                setOnSubtitleDataListener(this.mSubtitleDataListener);
            }
        }
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) throws IllegalStateException {
        InputStream fIs = is;
        MediaFormat fFormat = format;
        if (is != null) {
            synchronized (this.mOpenSubtitleSources) {
                this.mOpenSubtitleSources.add(is);
            }
        } else {
            Log.w(TAG, "addSubtitleSource called with null InputStream");
        }
        getMediaTimeProvider();
        HandlerThread thread = new HandlerThread("SubtitleReadThread", MEDIA_SKIPPED);
        thread.start();
        new Handler(thread.getLooper()).post(new AnonymousClass3(is, format, thread));
    }

    private void scanInternalSubtitleTracks() {
        if (this.mSubtitleController == null) {
            Log.d(TAG, "setSubtitleAnchor in MediaPlayer");
            setSubtitleAnchor();
        }
        populateInbandTracks();
        if (this.mSubtitleController != null) {
            this.mSubtitleController.selectDefaultTrack();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void populateInbandTracks() {
        TrackInfo[] tracks = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            int i = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
            while (true) {
                if (i < tracks.length) {
                    if (!this.mInbandTrackIndices.get(i)) {
                        this.mInbandTrackIndices.set(i);
                        if (tracks[i].getTrackType() == MEDIA_SEEK_COMPLETE) {
                            this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), this.mSubtitleController.addTrack(tracks[i].getFormat())));
                        } else {
                            this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), null));
                        }
                    }
                    i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
                }
            }
        }
    }

    public void addTimedTextSource(String path, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (availableMimeTypeForExternalSource(mimeType)) {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                addTimedTextSource(is.getFD(), mimeType);
                is.close();
                return;
            }
            throw new IOException(path);
        }
        throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
    }

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(WifiManager.EXTRA_PASSPOINT_ICON_FILE)) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            addTimedTextSource(assetFileDescriptor.getFileDescriptor(), mimeType);
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException e) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IllegalStateException e3) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) throws IllegalArgumentException, IllegalStateException {
        addTimedTextSource(fd, 0, 576460752303423487L, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mime) throws IllegalArgumentException, IllegalStateException {
        if (availableMimeTypeForExternalSource(mime)) {
            try {
                FileDescriptor fd2 = Libcore.os.dup(fd);
                MediaFormat fFormat = new MediaFormat();
                fFormat.setString(MediaFormat.KEY_MIME, mime);
                fFormat.setInteger(MediaFormat.KEY_IS_TIMED_TEXT, VIDEO_SCALING_MODE_SCALE_TO_FIT);
                if (this.mSubtitleController == null) {
                    setSubtitleAnchor();
                }
                if (!this.mSubtitleController.hasRendererFor(fFormat)) {
                    this.mSubtitleController.registerRenderer(new SRTRenderer(ActivityThread.currentApplication(), this.mEventHandler));
                }
                SubtitleTrack track = this.mSubtitleController.addTrack(fFormat);
                synchronized (this.mIndexTrackPairs) {
                    this.mIndexTrackPairs.add(Pair.create(null, track));
                }
                getMediaTimeProvider();
                FileDescriptor fd3 = fd2;
                long offset2 = offset;
                long length2 = length;
                HandlerThread thread = new HandlerThread("TimedTextReadThread", MEDIA_SKIPPED);
                thread.start();
                new Handler(thread.getLooper()).post(new AnonymousClass4(fd2, offset, length, track, thread));
                return;
            } catch (ErrnoException ex) {
                Log.e(TAG, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
        throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mime);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getSelectedTrack(int trackType) throws IllegalStateException {
        int i;
        if (this.mSubtitleController != null && (trackType == MEDIA_SEEK_COMPLETE || trackType == MEDIA_INFO_VIDEO_RENDERING_START)) {
            SubtitleTrack subtitleTrack = this.mSubtitleController.getSelectedTrack();
            if (subtitleTrack != null) {
                synchronized (this.mIndexTrackPairs) {
                    i = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
                    while (true) {
                        if (i >= this.mIndexTrackPairs.size()) {
                            break;
                        } else if (((Pair) this.mIndexTrackPairs.get(i)).second == subtitleTrack && subtitleTrack.getTrackType() == trackType) {
                            return i;
                        } else {
                            i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
                        }
                    }
                }
            }
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(MEDIA_PAUSED);
            request.writeInt(trackType);
            invoke(request, reply);
            int inbandTrackIndex = reply.readInt();
            synchronized (this.mIndexTrackPairs) {
                i = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
                while (true) {
                    if (i < this.mIndexTrackPairs.size()) {
                        Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(i);
                        if (p.first != null && ((Integer) p.first).intValue() == inbandTrackIndex) {
                            break;
                        }
                        i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
                    } else {
                        request.recycle();
                        reply.recycle();
                        return -1;
                    }
                }
            }
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public void selectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, METADATA_UPDATE_ONLY);
    }

    public void deselectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, METADATA_ALL);
    }

    private void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
        populateInbandTracks();
        try {
            Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(index);
            SubtitleTrack track = p.second;
            if (track == null) {
                selectOrDeselectInbandTrack(((Integer) p.first).intValue(), select);
            } else if (this.mSubtitleController != null) {
                if (select) {
                    if (track.getTrackType() == MEDIA_INFO_VIDEO_RENDERING_START) {
                        int ttIndex = getSelectedTrack(MEDIA_INFO_VIDEO_RENDERING_START);
                        synchronized (this.mIndexTrackPairs) {
                            if (ttIndex >= 0) {
                                if (ttIndex < this.mIndexTrackPairs.size()) {
                                    Pair<Integer, SubtitleTrack> p2 = (Pair) this.mIndexTrackPairs.get(ttIndex);
                                    if (p2.first != null && p2.second == null) {
                                        selectOrDeselectInbandTrack(((Integer) p2.first).intValue(), METADATA_ALL);
                                    }
                                }
                            }
                        }
                    }
                    this.mSubtitleController.selectTrack(track);
                    return;
                }
                if (this.mSubtitleController.getSelectedTrack() == track) {
                    this.mSubtitleController.selectTrack(null);
                } else {
                    Log.w(TAG, "trying to deselect track that was not selected");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void selectOrDeselectInbandTrack(int index, boolean select) throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(select ? MEDIA_SEEK_COMPLETE : MEDIA_SET_VIDEO_SIZE);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public void setRetransmitEndpoint(InetSocketAddress endpoint) throws IllegalStateException, IllegalArgumentException {
        String addrString = null;
        int port = PLAYBACK_RATE_AUDIO_MODE_DEFAULT;
        if (endpoint != null) {
            addrString = endpoint.getAddress().getHostAddress();
            port = endpoint.getPort();
        }
        int ret = native_setRetransmitEndpoint(addrString, port);
        if (ret != 0) {
            throw new IllegalArgumentException("Illegal re-transmit endpoint; native ret " + ret);
        }
    }

    protected void finalize() {
        if (!this.isReleased) {
            baseRelease();
            native_finalize();
        }
    }

    public MediaTimeProvider getMediaTimeProvider() {
        if (this.mTimeProvider == null) {
            this.mTimeProvider = new TimeProvider(this);
        }
        return this.mTimeProvider;
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        MediaPlayer mp = (MediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp != null) {
            if (what == MEDIA_INFO && arg1 == VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING) {
                mp.start();
            }
            if (mp.mEventHandler != null) {
                mp.mEventHandler.sendMessage(mp.mEventHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener = listener;
    }

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener) {
        this.mOnSubtitleDataListener = listener;
    }

    public void setOnTimedMetaDataAvailableListener(OnTimedMetaDataAvailableListener listener) {
        this.mOnTimedMetaDataAvailableListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    private boolean isVideoScalingModeSupported(int mode) {
        if (mode == VIDEO_SCALING_MODE_SCALE_TO_FIT || mode == VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING) {
            return METADATA_UPDATE_ONLY;
        }
        return METADATA_ALL;
    }
}
