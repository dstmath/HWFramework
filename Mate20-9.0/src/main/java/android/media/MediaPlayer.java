package android.media;

import android.app.ActivityThread;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hsm.MediaTransactWrapper;
import android.media.AudioAttributes;
import android.media.AudioRouting;
import android.media.MediaDrm;
import android.media.MediaTimeProvider;
import android.media.SubtitleController;
import android.media.SubtitleTrack;
import android.media.VolumeShaper;
import android.net.Uri;
import android.net.booster.IHwCommBoosterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import libcore.io.IoBridge;
import libcore.io.Streams;

public class MediaPlayer extends PlayerBase implements SubtitleController.Listener, VolumeAutomation, AudioRouting {
    public static final boolean APPLY_METADATA_FILTER = true;
    private static final boolean BOOSTER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_booster", false);
    public static final boolean BYPASS_METADATA_FILTER = false;
    private static final boolean HISI_VIDEO_ACC_FUNC = SystemProperties.getBoolean("ro.config.hisi_video_acc", false);
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_SELECTED_TRACK = 7;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int KEY_PARAMETER_AUDIO_ATTRIBUTES = 1400;
    private static final int MEDIA_AUDIO_ROUTING_CHANGED = 10000;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_CALLBACK_DATATYPE = 1;
    private static final int MEDIA_DRM_INFO = 210;
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
    public static final int MEDIA_INFO_AUDIO_NOT_PLAYING = 804;
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
    public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = 805;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int MEDIA_META_DATA = 202;
    public static final String MEDIA_MIMETYPE_TEXT_CEA_608 = "text/cea-608";
    public static final String MEDIA_MIMETYPE_TEXT_CEA_708 = "text/cea-708";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_VTT = "text/vtt";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_NOTIFY_TIME = 98;
    private static final int MEDIA_PAUSED = 7;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_REPORT_DATATYPE_SLICEINFO = 202;
    private static final int MEDIA_REPORT_DATATYPE_STATUS = 201;
    private static final String MEDIA_REPORT_PKG_NAME = "android.media";
    /* access modifiers changed from: private */
    public static final boolean MEDIA_REPORT_PROP;
    private static final int MEDIA_REPORT_STATUS_END = 4;
    private static final int MEDIA_REPORT_STATUS_PAUSE = 2;
    private static final int MEDIA_REPORT_STATUS_PREPARE = 0;
    private static final int MEDIA_REPORT_STATUS_START = 1;
    private static final int MEDIA_REPORT_SWITCH_OFF = 0;
    private static final int MEDIA_REPORT_SWITCH_ON = 1;
    private static final int MEDIA_REPORT_VIDEO_PROTOCOL_HLS = 0;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_SKIPPED = 9;
    private static final int MEDIA_STARTED = 6;
    private static final int MEDIA_STOPPED = 8;
    private static final int MEDIA_SUBTITLE_DATA = 201;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_TIME_DISCONTINUITY = 211;
    private static final int MEDIA_UPDATE_METADATA = 250;
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int PLAYBACK_RATE_AUDIO_MODE_DEFAULT = 0;
    public static final int PLAYBACK_RATE_AUDIO_MODE_RESAMPLE = 2;
    public static final int PLAYBACK_RATE_AUDIO_MODE_STRETCH = 1;
    public static final int PREPARE_DRM_STATUS_PREPARATION_ERROR = 3;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_NETWORK_ERROR = 1;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_SERVER_ERROR = 2;
    public static final int PREPARE_DRM_STATUS_SUCCESS = 0;
    public static final int SEEK_CLOSEST = 3;
    public static final int SEEK_CLOSEST_SYNC = 2;
    public static final int SEEK_NEXT_SYNC = 1;
    public static final int SEEK_PREVIOUS_SYNC = 0;
    private static final String TAG = "MediaPlayer";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private boolean mActiveDrmScheme;
    private boolean mBypassInterruptionPolicy;
    private boolean mDrmConfigAllowed;
    /* access modifiers changed from: private */
    public DrmInfo mDrmInfo;
    private boolean mDrmInfoResolved;
    /* access modifiers changed from: private */
    public final Object mDrmLock = new Object();
    /* access modifiers changed from: private */
    public MediaDrm mDrmObj;
    /* access modifiers changed from: private */
    public boolean mDrmProvisioningInProgress;
    private ProvisioningThread mDrmProvisioningThread;
    private byte[] mDrmSessionId;
    private UUID mDrmUUID;
    /* access modifiers changed from: private */
    public EventHandler mEventHandler;
    /* access modifiers changed from: private */
    public Handler mExtSubtitleDataHandler;
    /* access modifiers changed from: private */
    public OnSubtitleDataListener mExtSubtitleDataListener;
    private IHwCommBoosterCallback mIHwCommBoosterCallBack = null;
    private BitSet mInbandTrackIndices = new BitSet();
    /* access modifiers changed from: private */
    public Vector<Pair<Integer, SubtitleTrack>> mIndexTrackPairs = new Vector<>();
    /* access modifiers changed from: private */
    public final OnSubtitleDataListener mIntSubtitleDataListener = new OnSubtitleDataListener() {
        public void onSubtitleData(MediaPlayer mp, SubtitleData data) {
            int index = data.getTrackIndex();
            synchronized (MediaPlayer.this.mIndexTrackPairs) {
                Iterator it = MediaPlayer.this.mIndexTrackPairs.iterator();
                while (it.hasNext()) {
                    Pair<Integer, SubtitleTrack> p = (Pair) it.next();
                    if (!(p.first == null || ((Integer) p.first).intValue() != index || p.second == null)) {
                        ((SubtitleTrack) p.second).onData(data);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsHLS = false;
    private int mListenerContext;
    private boolean mMediaReportRegister = false;
    /* access modifiers changed from: private */
    public boolean mMediaReportSwitch = false;
    /* access modifiers changed from: private */
    public long mNativeContext;
    private long mNativeSurfaceTexture;
    /* access modifiers changed from: private */
    public OnBufferingUpdateListener mOnBufferingUpdateListener;
    /* access modifiers changed from: private */
    public final OnCompletionListener mOnCompletionInternalListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            MediaPlayer.this.baseStop();
        }
    };
    /* access modifiers changed from: private */
    public OnCompletionListener mOnCompletionListener;
    private OnDrmConfigHelper mOnDrmConfigHelper;
    /* access modifiers changed from: private */
    public OnDrmInfoHandlerDelegate mOnDrmInfoHandlerDelegate;
    /* access modifiers changed from: private */
    public OnDrmPreparedHandlerDelegate mOnDrmPreparedHandlerDelegate;
    /* access modifiers changed from: private */
    public OnErrorListener mOnErrorListener;
    /* access modifiers changed from: private */
    public OnInfoListener mOnInfoListener;
    /* access modifiers changed from: private */
    public Handler mOnMediaTimeDiscontinuityHandler;
    /* access modifiers changed from: private */
    public OnMediaTimeDiscontinuityListener mOnMediaTimeDiscontinuityListener;
    /* access modifiers changed from: private */
    public OnPreparedListener mOnPreparedListener;
    /* access modifiers changed from: private */
    public OnSeekCompleteListener mOnSeekCompleteListener;
    /* access modifiers changed from: private */
    public OnTimedMetaDataAvailableListener mOnTimedMetaDataAvailableListener;
    /* access modifiers changed from: private */
    public OnTimedTextListener mOnTimedTextListener;
    /* access modifiers changed from: private */
    public OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    /* access modifiers changed from: private */
    public Vector<InputStream> mOpenSubtitleSources;
    private AudioDeviceInfo mPreferredDevice = null;
    /* access modifiers changed from: private */
    public boolean mPrepareDrmInProgress;
    /* access modifiers changed from: private */
    @GuardedBy("mRoutingChangeListeners")
    public ArrayMap<AudioRouting.OnRoutingChangedListener, NativeRoutingEventHandlerDelegate> mRoutingChangeListeners = new ArrayMap<>();
    private boolean mScreenOnWhilePlaying;
    private int mSelectedSubtitleTrackIndex = -1;
    private boolean mStayAwake;
    private int mStreamType = Integer.MIN_VALUE;
    /* access modifiers changed from: private */
    public SubtitleController mSubtitleController;
    /* access modifiers changed from: private */
    public boolean mSubtitleDataListenerDisabled;
    private SurfaceHolder mSurfaceHolder;
    /* access modifiers changed from: private */
    public TimeProvider mTimeProvider;
    private int mUsage = -1;
    private PowerManager.WakeLock mWakeLock = null;

    public static final class DrmInfo {
        private Map<UUID, byte[]> mapPssh;
        private UUID[] supportedSchemes;

        public Map<UUID, byte[]> getPssh() {
            return this.mapPssh;
        }

        public UUID[] getSupportedSchemes() {
            return this.supportedSchemes;
        }

        private DrmInfo(Map<UUID, byte[]> Pssh, UUID[] SupportedSchemes) {
            this.mapPssh = Pssh;
            this.supportedSchemes = SupportedSchemes;
        }

        private DrmInfo(Parcel parcel) {
            Log.v(MediaPlayer.TAG, "DrmInfo(" + parcel + ") size " + parcel.dataSize());
            int psshsize = parcel.readInt();
            byte[] pssh = new byte[psshsize];
            parcel.readByteArray(pssh);
            Log.v(MediaPlayer.TAG, "DrmInfo() PSSH: " + arrToHex(pssh));
            this.mapPssh = parsePSSH(pssh, psshsize);
            Log.v(MediaPlayer.TAG, "DrmInfo() PSSH: " + this.mapPssh);
            int supportedDRMsCount = parcel.readInt();
            this.supportedSchemes = new UUID[supportedDRMsCount];
            for (int i = 0; i < supportedDRMsCount; i++) {
                byte[] uuid = new byte[16];
                parcel.readByteArray(uuid);
                this.supportedSchemes[i] = bytesToUUID(uuid);
                Log.v(MediaPlayer.TAG, "DrmInfo() supportedScheme[" + i + "]: " + this.supportedSchemes[i]);
            }
            Log.v(MediaPlayer.TAG, "DrmInfo() Parcel psshsize: " + psshsize + " supportedDRMsCount: " + supportedDRMsCount);
        }

        /* access modifiers changed from: private */
        public DrmInfo makeCopy() {
            return new DrmInfo(this.mapPssh, this.supportedSchemes);
        }

        private String arrToHex(byte[] bytes) {
            String out = "0x";
            for (int i = 0; i < bytes.length; i++) {
                out = out + String.format("%02x", new Object[]{Byte.valueOf(bytes[i])});
            }
            return out;
        }

        private UUID bytesToUUID(byte[] uuid) {
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb |= (((long) uuid[i]) & 255) << ((7 - i) * 8);
                lsb |= (((long) uuid[i + 8]) & 255) << (8 * (7 - i));
            }
            return new UUID(msb, lsb);
        }

        private Map<UUID, byte[]> parsePSSH(byte[] pssh, int psshsize) {
            int datalen;
            byte[] bArr = pssh;
            Map<UUID, byte[]> result = new HashMap<>();
            char c = 0;
            int numentries = 0;
            int len = psshsize;
            int i = 0;
            while (len > 0) {
                if (len < 16) {
                    Object[] objArr = new Object[2];
                    objArr[c] = Integer.valueOf(len);
                    objArr[1] = Integer.valueOf(psshsize);
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse UUID: (%d < 16) pssh: %d", objArr));
                    return null;
                }
                UUID uuid = bytesToUUID(Arrays.copyOfRange(bArr, i, i + 16));
                int i2 = i + 16;
                int len2 = len - 16;
                if (len2 < 4) {
                    Object[] objArr2 = new Object[2];
                    objArr2[c] = Integer.valueOf(len2);
                    objArr2[1] = Integer.valueOf(psshsize);
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse datalen: (%d < 4) pssh: %d", objArr2));
                    return null;
                }
                byte[] subset = Arrays.copyOfRange(bArr, i2, i2 + 4);
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    datalen = ((subset[3] & 255) << 24) | ((subset[2] & 255) << 16) | ((subset[1] & 255) << 8) | (subset[0] & 255);
                } else {
                    datalen = ((subset[0] & 255) << 24) | ((subset[1] & 255) << 16) | ((subset[2] & 255) << 8) | (subset[3] & 255);
                }
                int i3 = i2 + 4;
                int len3 = len2 - 4;
                if (len3 < datalen) {
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse data: (%d < %d) pssh: %d", new Object[]{Integer.valueOf(len3), Integer.valueOf(datalen), Integer.valueOf(psshsize)}));
                    return null;
                }
                byte[] data = Arrays.copyOfRange(bArr, i3, i3 + datalen);
                i = i3 + datalen;
                len = len3 - datalen;
                Log.v(MediaPlayer.TAG, String.format("parsePSSH[%d]: <%s, %s> pssh: %d", new Object[]{Integer.valueOf(numentries), uuid, arrToHex(data), Integer.valueOf(psshsize)}));
                numentries++;
                result.put(uuid, data);
                c = 0;
            }
            return result;
        }
    }

    private class EventHandler extends Handler {
        /* access modifiers changed from: private */
        public MediaPlayer mMediaPlayer;

        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:147:0x02db, code lost:
            r0 = android.media.MediaPlayer.access$400(r1.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:148:0x02e1, code lost:
            if (r0 == null) goto L_0x02e8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:149:0x02e3, code lost:
            r0.onSeekComplete(r1.mMediaPlayer);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:150:0x02e8, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:168:0x0334, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x0162, code lost:
            if ((r2.obj instanceof android.os.Parcel) == false) goto L_0x018d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x0164, code lost:
            r4 = (android.os.Parcel) r2.obj;
            r5 = new android.media.SubtitleData(r4);
            r4.recycle();
            android.media.MediaPlayer.access$2700(r1.this$0).onSubtitleData(r1.mMediaPlayer, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x017b, code lost:
            if (r0 == null) goto L_0x018d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x017d, code lost:
            if (r3 != null) goto L_0x0185;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x017f, code lost:
            r0.onSubtitleData(r1.mMediaPlayer, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x0185, code lost:
            r3.post(new android.media.MediaPlayer.EventHandler.AnonymousClass1(r1));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x018d, code lost:
            return;
         */
        public void handleMessage(Message msg) {
            OnDrmInfoHandlerDelegate onDrmInfoHandlerDelegate;
            final OnMediaTimeDiscontinuityListener mediaTimeListener;
            Handler mediaTimeHandler;
            final MediaTimestamp timestamp;
            Message message = msg;
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(MediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            int i = message.what;
            boolean z = false;
            if (i == 250) {
                if (message.arg1 == 802 && MediaPlayer.MEDIA_REPORT_PROP && MediaPlayer.this.mMediaReportSwitch && MediaPlayer.this.mIsHLS) {
                    Parcel parcel = (Parcel) message.obj;
                    if (parcel != null) {
                        long videoRemainingPlayTime = parcel.readLong();
                        long segDuration = parcel.readLong();
                        int segIndex = parcel.readInt();
                        long aveCodeRate = parcel.readLong();
                        Bundle data = new Bundle();
                        data.putInt("videoProtocol", 0);
                        data.putLong("videoRemainingPlayTime", videoRemainingPlayTime);
                        data.putLong("segDuration", segDuration);
                        data.putInt("segIndex", segIndex);
                        data.putLong("aveCodeRate", aveCodeRate);
                        if (HwFrameworkFactory.getHwCommBoosterServiceManager().reportBoosterPara(MediaPlayer.MEDIA_REPORT_PKG_NAME, 202, data) != 0) {
                            Log.e(MediaPlayer.TAG, "report slice info return error = " + res);
                        }
                    }
                }
            } else if (i != 10000) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        try {
                            MediaPlayer.this.scanInternalSubtitleTracks();
                        } catch (RuntimeException e) {
                            sendMessage(obtainMessage(100, 1, -1010, null));
                        }
                        OnPreparedListener onPreparedListener = MediaPlayer.this.mOnPreparedListener;
                        if (onPreparedListener != null) {
                            onPreparedListener.onPrepared(this.mMediaPlayer);
                        }
                        return;
                    case 2:
                        MediaPlayer.this.mOnCompletionInternalListener.onCompletion(this.mMediaPlayer);
                        OnCompletionListener onCompletionListener = MediaPlayer.this.mOnCompletionListener;
                        if (onCompletionListener != null) {
                            onCompletionListener.onCompletion(this.mMediaPlayer);
                        }
                        MediaPlayer.this.stayAwake(false);
                        return;
                    case 3:
                        OnBufferingUpdateListener onBufferingUpdateListener = MediaPlayer.this.mOnBufferingUpdateListener;
                        if (onBufferingUpdateListener != null) {
                            onBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, message.arg1);
                        }
                        return;
                    case 4:
                        OnSeekCompleteListener onSeekCompleteListener = MediaPlayer.this.mOnSeekCompleteListener;
                        if (onSeekCompleteListener != null) {
                            onSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                            break;
                        }
                        break;
                    case 5:
                        OnVideoSizeChangedListener onVideoSizeChangedListener = MediaPlayer.this.mOnVideoSizeChangedListener;
                        if (onVideoSizeChangedListener != null) {
                            onVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, message.arg1, message.arg2);
                        }
                        return;
                    case 6:
                    case 7:
                        TimeProvider timeProvider = MediaPlayer.this.mTimeProvider;
                        if (timeProvider != null) {
                            if (message.what == 7) {
                                z = true;
                            }
                            timeProvider.onPaused(z);
                            break;
                        }
                        break;
                    case 8:
                        TimeProvider timeProvider2 = MediaPlayer.this.mTimeProvider;
                        if (timeProvider2 != null) {
                            timeProvider2.onStopped();
                            break;
                        }
                        break;
                    case 9:
                        break;
                    default:
                        switch (i) {
                            case 98:
                                TimeProvider timeProvider3 = MediaPlayer.this.mTimeProvider;
                                if (timeProvider3 != null) {
                                    timeProvider3.onNotifyTime();
                                }
                                return;
                            case 99:
                                OnTimedTextListener onTimedTextListener = MediaPlayer.this.mOnTimedTextListener;
                                if (onTimedTextListener != null) {
                                    if (message.obj == null) {
                                        onTimedTextListener.onTimedText(this.mMediaPlayer, null);
                                    } else if (message.obj instanceof Parcel) {
                                        Parcel parcel2 = (Parcel) message.obj;
                                        TimedText text = new TimedText(parcel2);
                                        parcel2.recycle();
                                        onTimedTextListener.onTimedText(this.mMediaPlayer, text);
                                    }
                                    return;
                                }
                                return;
                            case 100:
                                Log.e(MediaPlayer.TAG, "Error (" + message.arg1 + "," + message.arg2 + ")");
                                boolean error_was_handled = false;
                                OnErrorListener onErrorListener = MediaPlayer.this.mOnErrorListener;
                                if (onErrorListener != null) {
                                    error_was_handled = onErrorListener.onError(this.mMediaPlayer, message.arg1, message.arg2);
                                }
                                MediaPlayer.this.mOnCompletionInternalListener.onCompletion(this.mMediaPlayer);
                                OnCompletionListener onCompletionListener2 = MediaPlayer.this.mOnCompletionListener;
                                if (onCompletionListener2 != null && !error_was_handled) {
                                    onCompletionListener2.onCompletion(this.mMediaPlayer);
                                }
                                MediaPlayer.this.stayAwake(false);
                                return;
                            default:
                                switch (i) {
                                    case 200:
                                        int i2 = message.arg1;
                                        switch (i2) {
                                            case 700:
                                                Log.i(MediaPlayer.TAG, "Info (" + message.arg1 + "," + message.arg2 + ")");
                                                break;
                                            case 701:
                                            case 702:
                                                TimeProvider timeProvider4 = MediaPlayer.this.mTimeProvider;
                                                if (timeProvider4 != null) {
                                                    if (message.arg1 == 701) {
                                                        z = true;
                                                    }
                                                    timeProvider4.onBuffering(z);
                                                    break;
                                                }
                                                break;
                                            default:
                                                switch (i2) {
                                                    case 802:
                                                        try {
                                                            MediaPlayer.this.scanInternalSubtitleTracks();
                                                            break;
                                                        } catch (RuntimeException e2) {
                                                            sendMessage(obtainMessage(100, 1, -1010, null));
                                                            break;
                                                        }
                                                    case 803:
                                                        break;
                                                }
                                                message.arg1 = 802;
                                                if (MediaPlayer.this.mSubtitleController != null) {
                                                    MediaPlayer.this.mSubtitleController.selectDefaultTrack();
                                                    break;
                                                }
                                                break;
                                        }
                                        OnInfoListener onInfoListener = MediaPlayer.this.mOnInfoListener;
                                        if (onInfoListener != null) {
                                            onInfoListener.onInfo(this.mMediaPlayer, message.arg1, message.arg2);
                                        }
                                        return;
                                    case 201:
                                        synchronized (this) {
                                            if (!MediaPlayer.this.mSubtitleDataListenerDisabled) {
                                                final OnSubtitleDataListener extSubtitleListener = MediaPlayer.this.mExtSubtitleDataListener;
                                                Handler extSubtitleHandler = MediaPlayer.this.mExtSubtitleDataHandler;
                                                break;
                                            } else {
                                                return;
                                            }
                                        }
                                    case 202:
                                        OnTimedMetaDataAvailableListener onTimedMetaDataAvailableListener = MediaPlayer.this.mOnTimedMetaDataAvailableListener;
                                        if (onTimedMetaDataAvailableListener != null && (message.obj instanceof Parcel)) {
                                            Parcel parcel3 = (Parcel) message.obj;
                                            TimedMetaData data2 = TimedMetaData.createTimedMetaDataFromParcel(parcel3);
                                            parcel3.recycle();
                                            onTimedMetaDataAvailableListener.onTimedMetaDataAvailable(this.mMediaPlayer, data2);
                                        }
                                        return;
                                    default:
                                        switch (i) {
                                            case 210:
                                                Log.v(MediaPlayer.TAG, "MEDIA_DRM_INFO " + MediaPlayer.this.mOnDrmInfoHandlerDelegate);
                                                if (message.obj == null) {
                                                    Log.w(MediaPlayer.TAG, "MEDIA_DRM_INFO msg.obj=NULL");
                                                } else if (message.obj instanceof Parcel) {
                                                    DrmInfo drmInfo = null;
                                                    synchronized (MediaPlayer.this.mDrmLock) {
                                                        if (!(MediaPlayer.this.mOnDrmInfoHandlerDelegate == null || MediaPlayer.this.mDrmInfo == null)) {
                                                            drmInfo = MediaPlayer.this.mDrmInfo.makeCopy();
                                                        }
                                                        onDrmInfoHandlerDelegate = MediaPlayer.this.mOnDrmInfoHandlerDelegate;
                                                    }
                                                    if (onDrmInfoHandlerDelegate != null) {
                                                        onDrmInfoHandlerDelegate.notifyClient(drmInfo);
                                                    }
                                                } else {
                                                    Log.w(MediaPlayer.TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + message.obj);
                                                }
                                                return;
                                            case 211:
                                                synchronized (this) {
                                                    mediaTimeListener = MediaPlayer.this.mOnMediaTimeDiscontinuityListener;
                                                    mediaTimeHandler = MediaPlayer.this.mOnMediaTimeDiscontinuityHandler;
                                                }
                                                if (mediaTimeListener != null && (message.obj instanceof Parcel)) {
                                                    Parcel parcel4 = (Parcel) message.obj;
                                                    parcel4.setDataPosition(0);
                                                    long anchorMediaUs = parcel4.readLong();
                                                    long anchorRealUs = parcel4.readLong();
                                                    float playbackRate = parcel4.readFloat();
                                                    parcel4.recycle();
                                                    if (anchorMediaUs == -1 || anchorRealUs == -1) {
                                                        timestamp = MediaTimestamp.TIMESTAMP_UNKNOWN;
                                                    } else {
                                                        timestamp = new MediaTimestamp(anchorMediaUs, anchorRealUs * 1000, playbackRate);
                                                    }
                                                    if (mediaTimeHandler == null) {
                                                        mediaTimeListener.onMediaTimeDiscontinuity(this.mMediaPlayer, timestamp);
                                                    } else {
                                                        mediaTimeHandler.post(new Runnable() {
                                                            public void run() {
                                                                mediaTimeListener.onMediaTimeDiscontinuity(EventHandler.this.mMediaPlayer, timestamp);
                                                            }
                                                        });
                                                    }
                                                }
                                                return;
                                            default:
                                                Log.e(MediaPlayer.TAG, "Unknown message type " + message.what);
                                                return;
                                        }
                                }
                        }
                }
            } else {
                AudioManager.resetAudioPortGeneration();
                synchronized (MediaPlayer.this.mRoutingChangeListeners) {
                    for (NativeRoutingEventHandlerDelegate delegate : MediaPlayer.this.mRoutingChangeListeners.values()) {
                        delegate.notifyClient();
                    }
                }
            }
        }
    }

    public static final class MetricsConstants {
        public static final String CODEC_AUDIO = "android.media.mediaplayer.audio.codec";
        public static final String CODEC_VIDEO = "android.media.mediaplayer.video.codec";
        public static final String DURATION = "android.media.mediaplayer.durationMs";
        public static final String ERRORS = "android.media.mediaplayer.err";
        public static final String ERROR_CODE = "android.media.mediaplayer.errcode";
        public static final String FRAMES = "android.media.mediaplayer.frames";
        public static final String FRAMES_DROPPED = "android.media.mediaplayer.dropped";
        public static final String HEIGHT = "android.media.mediaplayer.height";
        public static final String MIME_TYPE_AUDIO = "android.media.mediaplayer.audio.mime";
        public static final String MIME_TYPE_VIDEO = "android.media.mediaplayer.video.mime";
        public static final String PLAYING = "android.media.mediaplayer.playingMs";
        public static final String WIDTH = "android.media.mediaplayer.width";

        private MetricsConstants() {
        }
    }

    public static final class NoDrmSchemeException extends MediaDrmException {
        public NoDrmSchemeException(String detailMessage) {
            super(detailMessage);
        }
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    public interface OnDrmConfigHelper {
        void onDrmConfig(MediaPlayer mediaPlayer);
    }

    private class OnDrmInfoHandlerDelegate {
        private Handler mHandler;
        /* access modifiers changed from: private */
        public MediaPlayer mMediaPlayer;
        /* access modifiers changed from: private */
        public OnDrmInfoListener mOnDrmInfoListener;

        OnDrmInfoHandlerDelegate(MediaPlayer mp, OnDrmInfoListener listener, Handler handler) {
            this.mMediaPlayer = mp;
            this.mOnDrmInfoListener = listener;
            if (handler != null) {
                this.mHandler = handler;
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyClient(final DrmInfo drmInfo) {
            if (this.mHandler != null) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        OnDrmInfoHandlerDelegate.this.mOnDrmInfoListener.onDrmInfo(OnDrmInfoHandlerDelegate.this.mMediaPlayer, drmInfo);
                    }
                });
            } else {
                this.mOnDrmInfoListener.onDrmInfo(this.mMediaPlayer, drmInfo);
            }
        }
    }

    public interface OnDrmInfoListener {
        void onDrmInfo(MediaPlayer mediaPlayer, DrmInfo drmInfo);
    }

    private class OnDrmPreparedHandlerDelegate {
        private Handler mHandler;
        /* access modifiers changed from: private */
        public MediaPlayer mMediaPlayer;
        /* access modifiers changed from: private */
        public OnDrmPreparedListener mOnDrmPreparedListener;

        OnDrmPreparedHandlerDelegate(MediaPlayer mp, OnDrmPreparedListener listener, Handler handler) {
            this.mMediaPlayer = mp;
            this.mOnDrmPreparedListener = listener;
            if (handler != null) {
                this.mHandler = handler;
            } else if (MediaPlayer.this.mEventHandler != null) {
                this.mHandler = MediaPlayer.this.mEventHandler;
            } else {
                Log.e(MediaPlayer.TAG, "OnDrmPreparedHandlerDelegate: Unexpected null mEventHandler");
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyClient(final int status) {
            if (this.mHandler != null) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        OnDrmPreparedHandlerDelegate.this.mOnDrmPreparedListener.onDrmPrepared(OnDrmPreparedHandlerDelegate.this.mMediaPlayer, status);
                    }
                });
            } else {
                Log.e(MediaPlayer.TAG, "OnDrmPreparedHandlerDelegate:notifyClient: Unexpected null mHandler");
            }
        }
    }

    public interface OnDrmPreparedListener {
        void onDrmPrepared(MediaPlayer mediaPlayer, int i);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnMediaTimeDiscontinuityListener {
        void onMediaTimeDiscontinuity(MediaPlayer mediaPlayer, MediaTimestamp mediaTimestamp);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnSubtitleDataListener {
        void onSubtitleData(MediaPlayer mediaPlayer, SubtitleData subtitleData);
    }

    public interface OnTimedMetaDataAvailableListener {
        void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData);
    }

    public interface OnTimedTextListener {
        void onTimedText(MediaPlayer mediaPlayer, TimedText timedText);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PlaybackRateAudioMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrepareDrmStatusCode {
    }

    public static final class ProvisioningNetworkErrorException extends MediaDrmException {
        public ProvisioningNetworkErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ProvisioningServerErrorException extends MediaDrmException {
        public ProvisioningServerErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    private class ProvisioningThread extends Thread {
        public static final int TIMEOUT_MS = 60000;
        private Object drmLock;
        private boolean finished;
        private MediaPlayer mediaPlayer;
        private OnDrmPreparedHandlerDelegate onDrmPreparedHandlerDelegate;
        private int status;
        private String urlStr;
        private UUID uuid;

        private ProvisioningThread() {
        }

        public int status() {
            return this.status;
        }

        public ProvisioningThread initialize(MediaDrm.ProvisionRequest request, UUID uuid2, MediaPlayer mediaPlayer2) {
            this.drmLock = mediaPlayer2.mDrmLock;
            this.onDrmPreparedHandlerDelegate = mediaPlayer2.mOnDrmPreparedHandlerDelegate;
            this.mediaPlayer = mediaPlayer2;
            this.urlStr = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
            this.uuid = uuid2;
            this.status = 3;
            Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread is initialised url: " + this.urlStr);
            return this;
        }

        public void run() {
            HttpURLConnection connection;
            byte[] response = null;
            boolean provisioningSucceeded = false;
            try {
                connection = (HttpURLConnection) new URL(this.urlStr).openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    connection.setConnectTimeout(60000);
                    connection.setReadTimeout(60000);
                    connection.connect();
                    response = Streams.readFully(connection.getInputStream());
                    Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread run: response " + response.length + " " + response);
                    connection.disconnect();
                } catch (Exception e) {
                    this.status = 1;
                    Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: connect " + e + " url: " + url);
                    connection.disconnect();
                }
            } catch (Exception e2) {
                this.status = 1;
                Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: openConnection " + e2);
            } catch (Throwable th) {
                connection.disconnect();
                throw th;
            }
            if (response != null) {
                try {
                    MediaPlayer.this.mDrmObj.provideProvisionResponse(response);
                    Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse SUCCEEDED!");
                    provisioningSucceeded = true;
                } catch (Exception e3) {
                    this.status = 2;
                    Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse " + e3);
                }
            }
            boolean succeeded = false;
            int i = 3;
            if (this.onDrmPreparedHandlerDelegate != null) {
                synchronized (this.drmLock) {
                    if (provisioningSucceeded) {
                        try {
                            succeeded = this.mediaPlayer.resumePrepareDrm(this.uuid);
                            if (succeeded) {
                                i = 0;
                            }
                            this.status = i;
                        } catch (Throwable th2) {
                            while (true) {
                                throw th2;
                            }
                        }
                    }
                    boolean unused = this.mediaPlayer.mDrmProvisioningInProgress = false;
                    boolean unused2 = this.mediaPlayer.mPrepareDrmInProgress = false;
                    if (!succeeded) {
                        MediaPlayer.this.cleanDrmObj();
                    }
                }
                this.onDrmPreparedHandlerDelegate.notifyClient(this.status);
            } else {
                if (provisioningSucceeded) {
                    succeeded = this.mediaPlayer.resumePrepareDrm(this.uuid);
                    if (succeeded) {
                        i = 0;
                    }
                    this.status = i;
                }
                boolean unused3 = this.mediaPlayer.mDrmProvisioningInProgress = false;
                boolean unused4 = this.mediaPlayer.mPrepareDrmInProgress = false;
                if (!succeeded) {
                    MediaPlayer.this.cleanDrmObj();
                }
            }
            this.finished = true;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekMode {
    }

    static class TimeProvider implements OnSeekCompleteListener, MediaTimeProvider {
        private static final long MAX_EARLY_CALLBACK_US = 1000;
        private static final long MAX_NS_WITHOUT_POSITION_CHECK = 5000000000L;
        private static final int NOTIFY = 1;
        private static final int NOTIFY_SEEK = 3;
        private static final int NOTIFY_STOP = 2;
        private static final int NOTIFY_TIME = 0;
        private static final int NOTIFY_TRACK_DATA = 4;
        private static final String TAG = "MTP";
        private static final long TIME_ADJUSTMENT_RATE = 2;
        public boolean DEBUG = false;
        private boolean mBuffering;
        /* access modifiers changed from: private */
        public Handler mEventHandler;
        private HandlerThread mHandlerThread;
        private long mLastReportedTime;
        private long mLastTimeUs = 0;
        private MediaTimeProvider.OnMediaTimeListener[] mListeners;
        private boolean mPaused = true;
        private boolean mPausing = false;
        private MediaPlayer mPlayer;
        private boolean mRefresh = false;
        private boolean mSeeking = false;
        private boolean mStopped = true;
        private long[] mTimes;

        private class EventHandler extends Handler {
            public EventHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    int i = msg.arg1;
                    if (i != 0) {
                        switch (i) {
                            case 2:
                                TimeProvider.this.notifyStop();
                                return;
                            case 3:
                                TimeProvider.this.notifySeek();
                                return;
                            case 4:
                                TimeProvider.this.notifyTrackData((Pair) msg.obj);
                                return;
                            default:
                                return;
                        }
                    } else {
                        TimeProvider.this.notifyTimedEvent(true);
                    }
                }
            }
        }

        public TimeProvider(MediaPlayer mp) {
            this.mPlayer = mp;
            try {
                getCurrentTimeUs(true, false);
            } catch (IllegalStateException e) {
                this.mRefresh = true;
            }
            Looper myLooper = Looper.myLooper();
            Looper looper = myLooper;
            if (myLooper == null) {
                Looper mainLooper = Looper.getMainLooper();
                looper = mainLooper;
                if (mainLooper == null) {
                    this.mHandlerThread = new HandlerThread("MediaPlayerMTPEventThread", -2);
                    this.mHandlerThread.start();
                    looper = this.mHandlerThread.getLooper();
                }
            }
            this.mEventHandler = new EventHandler(looper);
            this.mListeners = new MediaTimeProvider.OnMediaTimeListener[0];
            this.mTimes = new long[0];
            this.mLastTimeUs = 0;
        }

        private void scheduleNotification(int type, long delayUs) {
            if (!this.mSeeking || type != 0) {
                if (this.DEBUG) {
                    Log.v(TAG, "scheduleNotification " + type + " in " + delayUs);
                }
                this.mEventHandler.removeMessages(1);
                this.mEventHandler.sendMessageDelayed(this.mEventHandler.obtainMessage(1, type, 0), (long) ((int) (delayUs / MAX_EARLY_CALLBACK_US)));
            }
        }

        public void close() {
            this.mEventHandler.removeMessages(1);
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
        }

        public void onNotifyTime() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onNotifyTime: ");
                }
                scheduleNotification(0, 0);
            }
        }

        public void onPaused(boolean paused) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onPaused: " + paused);
                }
                if (this.mStopped) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    scheduleNotification(3, 0);
                } else {
                    this.mPausing = paused;
                    this.mSeeking = false;
                    scheduleNotification(0, 0);
                }
            }
        }

        public void onBuffering(boolean buffering) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onBuffering: " + buffering);
                }
                this.mBuffering = buffering;
                scheduleNotification(0, 0);
            }
        }

        public void onStopped() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onStopped");
                }
                this.mPaused = true;
                this.mStopped = true;
                this.mSeeking = false;
                this.mBuffering = false;
                scheduleNotification(2, 0);
            }
        }

        public void onSeekComplete(MediaPlayer mp) {
            synchronized (this) {
                this.mStopped = false;
                this.mSeeking = true;
                scheduleNotification(3, 0);
            }
        }

        public void onNewPlayer() {
            if (this.mRefresh) {
                synchronized (this) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    this.mBuffering = false;
                    scheduleNotification(3, 0);
                }
            }
        }

        /* access modifiers changed from: private */
        public synchronized void notifySeek() {
            this.mSeeking = false;
            try {
                long timeUs = getCurrentTimeUs(true, false);
                if (this.DEBUG) {
                    Log.d(TAG, "onSeekComplete at " + timeUs);
                }
                MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
                int length = onMediaTimeListenerArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    MediaTimeProvider.OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                    if (listener == null) {
                        break;
                    }
                    listener.onSeek(timeUs);
                    i++;
                }
            } catch (IllegalStateException e) {
                if (this.DEBUG) {
                    Log.d(TAG, "onSeekComplete but no player");
                }
                this.mPausing = true;
                notifyTimedEvent(false);
            }
        }

        /* access modifiers changed from: private */
        public synchronized void notifyTrackData(Pair<SubtitleTrack, byte[]> trackData) {
            ((SubtitleTrack) trackData.first).onData((byte[]) trackData.second, true, -1);
        }

        /* access modifiers changed from: private */
        public synchronized void notifyStop() {
            MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
            int length = onMediaTimeListenerArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                MediaTimeProvider.OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                if (listener == null) {
                    break;
                }
                listener.onStop();
                i++;
            }
        }

        private int registerListener(MediaTimeProvider.OnMediaTimeListener listener) {
            int i = 0;
            while (i < this.mListeners.length && this.mListeners[i] != listener && this.mListeners[i] != null) {
                i++;
            }
            if (i >= this.mListeners.length) {
                MediaTimeProvider.OnMediaTimeListener[] newListeners = new MediaTimeProvider.OnMediaTimeListener[(i + 1)];
                long[] newTimes = new long[(i + 1)];
                System.arraycopy(this.mListeners, 0, newListeners, 0, this.mListeners.length);
                System.arraycopy(this.mTimes, 0, newTimes, 0, this.mTimes.length);
                this.mListeners = newListeners;
                this.mTimes = newTimes;
            }
            if (this.mListeners[i] == null) {
                this.mListeners[i] = listener;
                this.mTimes[i] = -1;
            }
            return i;
        }

        public void notifyAt(long timeUs, MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "notifyAt " + timeUs);
                }
                this.mTimes[registerListener(listener)] = timeUs;
                scheduleNotification(0, 0);
            }
        }

        public void scheduleUpdate(MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "scheduleUpdate");
                }
                int i = registerListener(listener);
                if (!this.mStopped) {
                    this.mTimes[i] = 0;
                    scheduleNotification(0, 0);
                }
            }
        }

        public void cancelNotifications(MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                int i = 0;
                while (true) {
                    if (i >= this.mListeners.length) {
                        break;
                    } else if (this.mListeners[i] == listener) {
                        System.arraycopy(this.mListeners, i + 1, this.mListeners, i, (this.mListeners.length - i) - 1);
                        System.arraycopy(this.mTimes, i + 1, this.mTimes, i, (this.mTimes.length - i) - 1);
                        this.mListeners[this.mListeners.length - 1] = null;
                        this.mTimes[this.mTimes.length - 1] = -1;
                        break;
                    } else if (this.mListeners[i] == null) {
                        break;
                    } else {
                        i++;
                    }
                }
                scheduleNotification(0, 0);
            }
        }

        /* access modifiers changed from: private */
        public synchronized void notifyTimedEvent(boolean refreshTime) {
            long nowUs;
            long nowUs2;
            long nowUs3;
            boolean z = refreshTime;
            synchronized (this) {
                try {
                    nowUs = getCurrentTimeUs(z, true);
                } catch (IllegalStateException e) {
                    IllegalStateException illegalStateException = e;
                    this.mRefresh = true;
                    this.mPausing = true;
                    nowUs = getCurrentTimeUs(z, true);
                }
                long nextTimeUs = nowUs;
                if (!this.mSeeking) {
                    int ix = 0;
                    if (this.DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("notifyTimedEvent(");
                        sb.append(this.mLastTimeUs);
                        sb.append(" -> ");
                        sb.append(nowUs);
                        sb.append(") from {");
                        long[] jArr = this.mTimes;
                        int length = jArr.length;
                        boolean first = true;
                        int i = 0;
                        while (i < length) {
                            long nowUs4 = nowUs;
                            long time = jArr[i];
                            if (time != -1) {
                                if (!first) {
                                    sb.append(", ");
                                }
                                sb.append(time);
                                first = false;
                            }
                            i++;
                            nowUs = nowUs4;
                        }
                        nowUs2 = nowUs;
                        sb.append("}");
                        Log.d(TAG, sb.toString());
                    } else {
                        nowUs2 = nowUs;
                    }
                    Vector<MediaTimeProvider.OnMediaTimeListener> activatedListeners = new Vector<>();
                    while (true) {
                        int ix2 = ix;
                        if (ix2 >= this.mTimes.length) {
                            break;
                        } else if (this.mListeners[ix2] == null) {
                            break;
                        } else {
                            if (this.mTimes[ix2] > -1) {
                                if (this.mTimes[ix2] <= nowUs2 + MAX_EARLY_CALLBACK_US) {
                                    activatedListeners.add(this.mListeners[ix2]);
                                    if (this.DEBUG) {
                                        Log.d(TAG, "removed");
                                    }
                                    this.mTimes[ix2] = -1;
                                } else if (nextTimeUs == nowUs2 || this.mTimes[ix2] < nextTimeUs) {
                                    nextTimeUs = this.mTimes[ix2];
                                }
                            }
                            ix = ix2 + 1;
                        }
                    }
                    if (nextTimeUs <= nowUs2 || this.mPaused) {
                        nowUs3 = nowUs2;
                        this.mEventHandler.removeMessages(1);
                    } else {
                        if (this.DEBUG) {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("scheduling for ");
                            sb2.append(nextTimeUs);
                            sb2.append(" and ");
                            nowUs3 = nowUs2;
                            sb2.append(nowUs3);
                            Log.d(TAG, sb2.toString());
                        } else {
                            nowUs3 = nowUs2;
                        }
                        this.mPlayer.notifyAt(nextTimeUs);
                    }
                    Iterator<MediaTimeProvider.OnMediaTimeListener> it = activatedListeners.iterator();
                    while (it.hasNext()) {
                        it.next().onTimedEvent(nowUs3);
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x002f A[Catch:{ IllegalStateException -> 0x007f }] */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0058 A[SYNTHETIC, Splitter:B:25:0x0058] */
        public long getCurrentTimeUs(boolean refreshTime, boolean monotonic) throws IllegalStateException {
            boolean z;
            synchronized (this) {
                if (!this.mPaused || refreshTime) {
                    try {
                        this.mLastTimeUs = ((long) this.mPlayer.getCurrentPosition()) * MAX_EARLY_CALLBACK_US;
                        if (this.mPlayer.isPlaying()) {
                            if (!this.mBuffering) {
                                z = false;
                                this.mPaused = z;
                                if (this.DEBUG) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(this.mPaused ? "paused" : "playing");
                                    sb.append(" at ");
                                    sb.append(this.mLastTimeUs);
                                    Log.v(TAG, sb.toString());
                                }
                                if (monotonic) {
                                    if (this.mLastTimeUs < this.mLastReportedTime) {
                                        if (this.mLastReportedTime - this.mLastTimeUs > 1000000) {
                                            this.mStopped = false;
                                            this.mSeeking = true;
                                            scheduleNotification(3, 0);
                                        }
                                        long j = this.mLastReportedTime;
                                        return j;
                                    }
                                }
                                this.mLastReportedTime = this.mLastTimeUs;
                                long j2 = this.mLastReportedTime;
                                return j2;
                            }
                        }
                        z = true;
                        this.mPaused = z;
                        if (this.DEBUG) {
                        }
                        if (monotonic) {
                        }
                        this.mLastReportedTime = this.mLastTimeUs;
                        long j22 = this.mLastReportedTime;
                        return j22;
                    } catch (IllegalStateException e) {
                        if (this.mPausing) {
                            this.mPausing = false;
                            if (!monotonic || this.mLastReportedTime < this.mLastTimeUs) {
                                this.mLastReportedTime = this.mLastTimeUs;
                            }
                            this.mPaused = true;
                            if (this.DEBUG) {
                                Log.d(TAG, "illegal state, but pausing: estimating at " + this.mLastReportedTime);
                            }
                            return this.mLastReportedTime;
                        }
                        throw e;
                    }
                } else {
                    long j3 = this.mLastReportedTime;
                    return j3;
                }
            }
        }
    }

    public static class TrackInfo implements Parcelable {
        static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {
            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            public TrackInfo[] newArray(int size) {
                return new TrackInfo[size];
            }
        };
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final MediaFormat mFormat;
        final int mTrackType;

        @Retention(RetentionPolicy.SOURCE)
        public @interface TrackType {
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String language = this.mFormat.getString(MediaFormat.KEY_LANGUAGE);
            return language == null ? "und" : language;
        }

        public MediaFormat getFormat() {
            if (this.mTrackType == 3 || this.mTrackType == 4) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfo(Parcel in) {
            this.mTrackType = in.readInt();
            this.mFormat = MediaFormat.createSubtitleFormat(in.readString(), in.readString());
            if (this.mTrackType == 4) {
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
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(this.mFormat.getString(MediaFormat.KEY_MIME));
            dest.writeString(getLanguage());
            if (this.mTrackType == 4) {
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_AUTOSELECT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_DEFAULT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE));
            }
        }

        public String toString() {
            StringBuilder out = new StringBuilder(128);
            out.append(getClass().getName());
            out.append('{');
            switch (this.mTrackType) {
                case 1:
                    out.append("VIDEO");
                    break;
                case 2:
                    out.append("AUDIO");
                    break;
                case 3:
                    out.append("TIMEDTEXT");
                    break;
                case 4:
                    out.append("SUBTITLE");
                    break;
                default:
                    out.append("UNKNOWN");
                    break;
            }
            out.append(", " + this.mFormat.toString());
            out.append("}");
            return out.toString();
        }
    }

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _notifyAt(long j);

    private native void _pause() throws IllegalStateException;

    private native void _prepare() throws IOException, IllegalStateException;

    private native void _prepareDrm(byte[] bArr, byte[] bArr2);

    private native void _release();

    private native void _releaseDrm();

    private native void _reset();

    private final native void _seekTo(long j, int i);

    private native void _setAudioStreamType(int i);

    private native void _setAuxEffectSendLevel(float f);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException, IllegalStateException;

    private native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _setVolume(float f, float f2);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    private native int native_applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation);

    private final native void native_enableDeviceCallback(boolean z);

    private final native void native_finalize();

    private final native boolean native_getMetadata(boolean z, boolean z2, Parcel parcel);

    private native PersistableBundle native_getMetrics();

    private final native int native_getRoutedDeviceId();

    private native VolumeShaper.State native_getVolumeShaperState(int i);

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    public static native int native_pullBatteryData(Parcel parcel);

    private final native int native_setMetadataFilter(Parcel parcel);

    private final native boolean native_setOutputDevice(int i);

    private final native int native_setRetransmitEndpoint(String str, int i);

    private final native void native_setup(Object obj);

    private native boolean setParameter(int i, Parcel parcel);

    public native void attachAuxEffect(int i);

    public native int getAudioSessionId();

    public native BufferingParams getBufferingParams();

    public native int getCurrentPosition();

    public native int getDuration();

    public native PlaybackParams getPlaybackParams();

    public native SyncParams getSyncParams();

    public native int getVideoHeight();

    public native int getVideoWidth();

    public native boolean isLooping();

    public native boolean isPlaying();

    public native void prepareAsync() throws IllegalStateException;

    public native void setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    public native void setBufferingParams(BufferingParams bufferingParams);

    public native void setLooping(boolean z);

    public native void setNextMediaPlayer(MediaPlayer mediaPlayer);

    public native void setPlaybackParams(PlaybackParams playbackParams);

    public native void setSyncParams(SyncParams syncParams);

    static {
        System.loadLibrary("media_jni");
        native_init();
        boolean z = false;
        if (BOOSTER_SUPPORT && HISI_VIDEO_ACC_FUNC) {
            z = true;
        }
        MEDIA_REPORT_PROP = z;
    }

    public MediaPlayer() {
        super(new AudioAttributes.Builder().build(), 2);
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper mainLooper = Looper.getMainLooper();
            Looper looper2 = mainLooper;
            if (mainLooper != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mTimeProvider = new TimeProvider(this);
        this.mOpenSubtitleSources = new Vector<>();
        native_setup(new WeakReference(this));
        baseRegisterPlayer();
    }

    public Parcel newRequest() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(IMEDIA_PLAYER);
        return parcel;
    }

    public void invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(0);
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
                request.writeInt(6);
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
        return create(context, uri, holder, null, s > 0 ? s : 0);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(audioAttributes != null ? audioAttributes : new AudioAttributes.Builder().build());
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
        return create(context, resid, null, s > 0 ? s : 0);
    }

    public static MediaPlayer create(Context context, int resid, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) {
                return null;
            }
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(audioAttributes != null ? audioAttributes : new AudioAttributes.Builder().build());
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
        setDataSource(context, uri, (Map<String, String>) null, (List<HttpCookie>) null);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers, List<HttpCookie> cookies) throws IOException {
        if (context == null) {
            throw new NullPointerException("context param can not be null.");
        } else if (uri != null) {
            if (cookies != null) {
                CookieHandler cookieHandler = CookieHandler.getDefault();
                if (cookieHandler != null && !(cookieHandler instanceof CookieManager)) {
                    throw new IllegalArgumentException("The cookie handler has to be of CookieManager type when cookies are provided.");
                }
            }
            ContentResolver resolver = context.getContentResolver();
            String actualUrim = null;
            try {
                actualUrim = MediaStore.getPath(context, uri);
            } catch (SecurityException e) {
                Log.e(TAG, "MediaStore.getPath error ");
            }
            if (actualUrim == null || !actualUrim.endsWith(".isma")) {
                String scheme = uri.getScheme();
                String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
                if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                    setDataSource(uri.getPath());
                    return;
                }
                if ("content".equals(scheme) && "settings".equals(authority)) {
                    int type = RingtoneManager.getDefaultType(uri);
                    Uri cacheUri = RingtoneManager.getCacheForType(type, context.getUserId());
                    Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
                    if (!attemptDataSource(resolver, cacheUri) && !attemptDataSource(resolver, actualUri)) {
                        setDataSource(uri.toString(), headers, cookies);
                    } else {
                        return;
                    }
                } else if (!attemptDataSource(resolver, uri)) {
                    setDataSource(uri.toString(), headers, cookies);
                } else {
                    return;
                }
                return;
            }
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.fallbackring);
            if (afd != null) {
                if (afd.getDeclaredLength() < 0) {
                    setDataSource(afd.getFileDescriptor());
                } else {
                    setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                }
                afd.close();
            }
        } else {
            throw new NullPointerException("uri param can not be null.");
        }
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, headers, (List<HttpCookie>) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x001e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0027, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0028, code lost:
        android.util.Log.w(TAG, "Couldn't open " + r6 + ": " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0047, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0027 A[ExcHandler: IOException | NullPointerException | SecurityException (r0v1 'ex' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0003] */
    private boolean attemptDataSource(ContentResolver resolver, Uri uri) {
        AssetFileDescriptor afd;
        try {
            afd = resolver.openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            setDataSource(afd);
            if (afd != null) {
                afd.close();
            }
            return true;
        } catch (IOException | NullPointerException | SecurityException ex) {
        } catch (Throwable th) {
            if (afd != null) {
                if (r1 != null) {
                    afd.close();
                } else {
                    afd.close();
                }
            }
            throw th;
        }
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, (Map<String, String>) null, (List<HttpCookie>) null);
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, headers, (List<HttpCookie>) null);
    }

    private void setDataSource(String path, Map<String, String> headers, List<HttpCookie> cookies) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
        }
        setDataSource(path, keys, values, cookies);
    }

    private void setDataSource(String path, String[] keys, String[] values, List<HttpCookie> cookies) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (MEDIA_REPORT_PROP) {
            String tmpPath = path.toLowerCase(Locale.getDefault());
            if ((tmpPath.startsWith("http://") || tmpPath.startsWith("https://") || tmpPath.startsWith("file://")) && tmpPath.indexOf("m3u8") != -1) {
                this.mIsHLS = true;
            } else {
                this.mIsHLS = false;
            }
            if (!this.mMediaReportRegister && this.mIsHLS && this.mIHwCommBoosterCallBack == null) {
                this.mIHwCommBoosterCallBack = new IHwCommBoosterCallback.Stub() {
                    public void callBack(int callBackDataType, Bundle bundle) throws RemoteException {
                        if (callBackDataType == 1) {
                            int mediareportswitch = bundle.getInt("VideoInfoReportState");
                            if (mediareportswitch == 0) {
                                boolean unused = MediaPlayer.this.mMediaReportSwitch = false;
                            } else if (mediareportswitch == 1) {
                                boolean unused2 = MediaPlayer.this.mMediaReportSwitch = true;
                            } else {
                                Log.e(MediaPlayer.TAG, "callback VideoInfoReportState invalid");
                            }
                        }
                    }
                };
                int res = HwFrameworkFactory.getHwCommBoosterServiceManager().registerCallBack(MEDIA_REPORT_PKG_NAME, this.mIHwCommBoosterCallBack);
                if (res == 0) {
                    this.mMediaReportRegister = true;
                } else {
                    Log.e(TAG, "registerCallBack return other error = " + res);
                }
            }
        }
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (scheme != null) {
            nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path, cookies), path, keys, values);
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
                throw th;
            }
        }
    }

    public void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
            return;
        }
        setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, 0, (long) DataSourceDesc.LONG_MAX);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        _setDataSource(fd, offset, length);
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        _setDataSource(dataSource);
    }

    private void reportMediaStatus(int status) {
        if (MEDIA_REPORT_PROP && this.mMediaReportSwitch && this.mIsHLS) {
            Bundle data = new Bundle();
            data.putInt("videoStatus", status);
            int res = HwFrameworkFactory.getHwCommBoosterServiceManager().reportBoosterPara(MEDIA_REPORT_PKG_NAME, 201, data);
            if (res != 0) {
                Log.e(TAG, "media report return error = " + res);
            }
        }
    }

    public void prepare() throws IOException, IllegalStateException {
        _prepare();
        scanInternalSubtitleTracks();
        synchronized (this.mDrmLock) {
            this.mDrmInfoResolved = true;
        }
        reportMediaStatus(0);
    }

    public void start() throws IllegalStateException {
        final int delay = getStartDelayMs();
        if (delay == 0) {
            try {
                startImpl();
            } catch (IllegalStateException e) {
                stayAwake(false);
                Log.w(TAG, "Start Error, Maybe the MediaPlayer have been Changed ");
            }
        } else {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep((long) delay);
                    } catch (InterruptedException e) {
                        Log.w(MediaPlayer.TAG, "InterruptedException when delay in start run");
                    }
                    MediaPlayer.this.baseSetStartDelayMs(0);
                    try {
                        MediaPlayer.this.startImpl();
                    } catch (IllegalStateException e2) {
                        MediaPlayer.this.stayAwake(false);
                        Log.w(MediaPlayer.TAG, "Start Error, Maybe the MediaPlayer have been Changed delay" + delay);
                    }
                }
            }.start();
        }
        reportMediaStatus(1);
    }

    /* access modifiers changed from: private */
    public void startImpl() {
        baseStart();
        stayAwake(true);
        _start();
    }

    private int getAudioStreamType() {
        if (this.mStreamType == Integer.MIN_VALUE) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
        baseStop();
        reportMediaStatus(4);
    }

    public void pause() throws IllegalStateException {
        stayAwake(false);
        _pause();
        basePause();
        reportMediaStatus(2);
    }

    /* access modifiers changed from: package-private */
    public void playerStart() {
        start();
    }

    /* access modifiers changed from: package-private */
    public void playerPause() {
        pause();
    }

    /* access modifiers changed from: package-private */
    public void playerStop() {
        stop();
    }

    /* access modifiers changed from: package-private */
    public int playerApplyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) {
        return native_applyVolumeShaper(configuration, operation);
    }

    /* access modifiers changed from: package-private */
    public VolumeShaper.State playerGetVolumeShaperState(int id) {
        return native_getVolumeShaperState(id);
    }

    public VolumeShaper createVolumeShaper(VolumeShaper.Configuration configuration) {
        return new VolumeShaper(configuration, this);
    }

    public boolean setPreferredDevice(AudioDeviceInfo deviceInfo) {
        int preferredDeviceId = 0;
        if (deviceInfo != null && !deviceInfo.isSink()) {
            return false;
        }
        if (deviceInfo != null) {
            preferredDeviceId = deviceInfo.getId();
        }
        boolean status = native_setOutputDevice(preferredDeviceId);
        if (status) {
            synchronized (this) {
                this.mPreferredDevice = deviceInfo;
            }
        }
        return status;
    }

    public AudioDeviceInfo getPreferredDevice() {
        AudioDeviceInfo audioDeviceInfo;
        synchronized (this) {
            audioDeviceInfo = this.mPreferredDevice;
        }
        return audioDeviceInfo;
    }

    public AudioDeviceInfo getRoutedDevice() {
        int deviceId = native_getRoutedDeviceId();
        if (deviceId == 0) {
            return null;
        }
        AudioDeviceInfo[] devices = AudioManager.getDevicesStatic(2);
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getId() == deviceId) {
                return devices[i];
            }
        }
        return null;
    }

    @GuardedBy("mRoutingChangeListeners")
    private void enableNativeRoutingCallbacksLocked(boolean enabled) {
        if (this.mRoutingChangeListeners.size() == 0) {
            native_enableDeviceCallback(enabled);
        }
    }

    public void addOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        synchronized (this.mRoutingChangeListeners) {
            if (listener != null) {
                try {
                    if (!this.mRoutingChangeListeners.containsKey(listener)) {
                        enableNativeRoutingCallbacksLocked(true);
                        this.mRoutingChangeListeners.put(listener, new NativeRoutingEventHandlerDelegate(this, listener, handler != null ? handler : this.mEventHandler));
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public void removeOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener) {
        synchronized (this.mRoutingChangeListeners) {
            if (this.mRoutingChangeListeners.containsKey(listener)) {
                this.mRoutingChangeListeners.remove(listener);
                enableNativeRoutingCallbacksLocked(false);
            }
        }
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (SystemProperties.getBoolean("audio.offload.ignore_setawake", false)) {
            Log.w(TAG, "IGNORING setWakeMode " + mode);
            return;
        }
        if (this.mWakeLock != null) {
            if (this.mWakeLock.isHeld()) {
                washeld = true;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(536870912 | mode, MediaPlayer.class.getName());
        this.mWakeLock.setReferenceCounted(false);
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

    /* access modifiers changed from: private */
    public void stayAwake(boolean awake) {
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
            this.mSurfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying && this.mStayAwake);
        }
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    public PlaybackParams easyPlaybackParams(float rate, int audioMode) {
        PlaybackParams params = new PlaybackParams();
        params.allowDefaults();
        switch (audioMode) {
            case 0:
                params.setSpeed(rate).setPitch(1.0f);
                break;
            case 1:
                params.setSpeed(rate).setPitch(1.0f).setAudioFallbackMode(2);
                break;
            case 2:
                params.setSpeed(rate).setPitch(rate);
                break;
            default:
                throw new IllegalArgumentException("Audio playback mode " + audioMode + " is not supported");
        }
        return params;
    }

    public void seekTo(long msec, int mode) {
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Illegal seek mode: " + mode);
        }
        if (msec > 2147483647L) {
            Log.w(TAG, "seekTo offset " + msec + " is too large, cap to " + Integer.MAX_VALUE);
            msec = 2147483647L;
        } else if (msec < -2147483648L) {
            Log.w(TAG, "seekTo offset " + msec + " is too small, cap to " + Integer.MIN_VALUE);
            msec = -2147483648L;
        }
        _seekTo(msec, mode);
    }

    public void seekTo(int msec) throws IllegalStateException {
        seekTo((long) msec, 0);
    }

    public MediaTimestamp getTimestamp() {
        try {
            MediaTimestamp mediaTimestamp = new MediaTimestamp(((long) getCurrentPosition()) * 1000, System.nanoTime(), isPlaying() ? getPlaybackParams().getSpeed() : 0.0f);
            return mediaTimestamp;
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
        int capacity = request.dataSize() + (4 * (allow.size() + 1 + 1 + block.size()));
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
        stayAwake(false);
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
        synchronized (this) {
            this.mSubtitleDataListenerDisabled = false;
            this.mExtSubtitleDataListener = null;
            this.mExtSubtitleDataHandler = null;
            this.mOnMediaTimeDiscontinuityListener = null;
            this.mOnMediaTimeDiscontinuityHandler = null;
        }
        this.mOnDrmConfigHelper = null;
        this.mOnDrmInfoHandlerDelegate = null;
        this.mOnDrmPreparedHandlerDelegate = null;
        resetDrmState();
        _release();
        if (MEDIA_REPORT_PROP && this.mMediaReportRegister) {
            int res = HwFrameworkFactory.getHwCommBoosterServiceManager().unRegisterCallBack(MEDIA_REPORT_PKG_NAME, this.mIHwCommBoosterCallBack);
            if (res != 0) {
                Log.e(TAG, "unRegisterCallBack in release return error = " + res);
                return;
            }
            this.mMediaReportRegister = false;
        }
    }

    public void reset() {
        this.mSelectedSubtitleTrackIndex = -1;
        synchronized (this.mOpenSubtitleSources) {
            Iterator<InputStream> it = this.mOpenSubtitleSources.iterator();
            while (it.hasNext()) {
                try {
                    it.next().close();
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
        stayAwake(false);
        _reset();
        if (this.mEventHandler != null) {
            this.mEventHandler.removeCallbacksAndMessages(null);
        }
        synchronized (this.mIndexTrackPairs) {
            this.mIndexTrackPairs.clear();
            this.mInbandTrackIndices.clear();
        }
        resetDrmState();
    }

    public void notifyAt(long mediaTimeUs) {
        _notifyAt(mediaTimeUs);
    }

    public void setAudioStreamType(int streamtype) {
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(streamtype), TAG);
        deprecateStreamTypeForPlayback(streamtype, TAG, "setAudioStreamType()");
        baseUpdateAudioAttributes(new AudioAttributes.Builder().setInternalLegacyStreamType(streamtype).build());
        _setAudioStreamType(streamtype);
        this.mStreamType = streamtype;
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (attributes != null) {
            HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(AudioAttributes.toLegacyStreamType(attributes)), TAG);
            baseUpdateAudioAttributes(attributes);
            this.mUsage = attributes.getUsage();
            this.mBypassInterruptionPolicy = (attributes.getAllFlags() & 64) != 0;
            Parcel pattributes = Parcel.obtain();
            attributes.writeToParcel(pattributes, 1);
            setParameter(KEY_PARAMETER_AUDIO_ATTRIBUTES, pattributes);
            pattributes.recycle();
            return;
        }
        throw new IllegalArgumentException("Cannot set AudioAttributes to null");
    }

    public void setVolume(float leftVolume, float rightVolume) {
        baseSetVolume(leftVolume, rightVolume);
    }

    /* access modifiers changed from: package-private */
    public void playerSetVolume(boolean muting, float leftVolume, float rightVolume) {
        float f = 0.0f;
        float f2 = muting ? 0.0f : leftVolume;
        if (!muting) {
            f = rightVolume;
        }
        _setVolume(f2, f);
    }

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    public void setAuxEffectSendLevel(float level) {
        baseSetAuxEffectSendLevel(level);
    }

    /* access modifiers changed from: package-private */
    public int playerSetAuxEffectSendLevel(boolean muting, float level) {
        _setAuxEffectSendLevel(muting ? 0.0f : level);
        return 0;
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        TrackInfo[] allTrackInfo;
        TrackInfo[] trackInfo = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            allTrackInfo = new TrackInfo[this.mIndexTrackPairs.size()];
            for (int i = 0; i < allTrackInfo.length; i++) {
                Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(i);
                if (p.first != null) {
                    allTrackInfo[i] = trackInfo[((Integer) p.first).intValue()];
                } else {
                    SubtitleTrack track = (SubtitleTrack) p.second;
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
            request.writeInt(1);
            invoke(request, reply);
            return (TrackInfo[]) reply.createTypedArray(TrackInfo.CREATOR);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if ("application/x-subrip".equals(mimeType)) {
            return true;
        }
        return false;
    }

    public void setSubtitleAnchor(SubtitleController controller, SubtitleController.Anchor anchor) {
        this.mSubtitleController = controller;
        this.mSubtitleController.setAnchor(anchor);
    }

    private synchronized void setSubtitleAnchor() {
        if (this.mSubtitleController == null && ActivityThread.currentApplication() != null) {
            final HandlerThread thread = new HandlerThread("SetSubtitleAnchorThread");
            thread.start();
            new Handler(thread.getLooper()).post(new Runnable() {
                public void run() {
                    SubtitleController unused = MediaPlayer.this.mSubtitleController = new SubtitleController(ActivityThread.currentApplication(), MediaPlayer.this.mTimeProvider, MediaPlayer.this);
                    MediaPlayer.this.mSubtitleController.setAnchor(new SubtitleController.Anchor() {
                        public void setSubtitleWidget(SubtitleTrack.RenderingWidget subtitleWidget) {
                        }

                        public Looper getSubtitleLooper() {
                            return Looper.getMainLooper();
                        }
                    });
                    thread.getLooper().quitSafely();
                }
            });
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "failed to join SetSubtitleAnchorThread");
            }
        }
        return;
    }

    public void onSubtitleTrackSelected(SubtitleTrack track) {
        if (this.mSelectedSubtitleTrackIndex >= 0) {
            try {
                selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, false);
            } catch (IllegalStateException e) {
            }
            this.mSelectedSubtitleTrackIndex = -1;
        }
        synchronized (this) {
            this.mSubtitleDataListenerDisabled = true;
        }
        if (track != null) {
            synchronized (this.mIndexTrackPairs) {
                Iterator<Pair<Integer, SubtitleTrack>> it = this.mIndexTrackPairs.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Pair<Integer, SubtitleTrack> p = it.next();
                    if (p.first != null && p.second == track) {
                        this.mSelectedSubtitleTrackIndex = ((Integer) p.first).intValue();
                        break;
                    }
                }
            }
            if (this.mSelectedSubtitleTrackIndex >= 0) {
                try {
                    selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, true);
                } catch (IllegalStateException e2) {
                }
                synchronized (this) {
                    this.mSubtitleDataListenerDisabled = false;
                }
            }
        }
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) throws IllegalStateException {
        final InputStream fIs = is;
        final MediaFormat fFormat = format;
        if (is != null) {
            synchronized (this.mOpenSubtitleSources) {
                this.mOpenSubtitleSources.add(is);
            }
        } else {
            Log.w(TAG, "addSubtitleSource called with null InputStream");
        }
        getMediaTimeProvider();
        final HandlerThread thread = new HandlerThread("SubtitleReadThread", 9);
        thread.start();
        new Handler(thread.getLooper()).post(new Runnable() {
            private int addTrack() {
                if (fIs == null || MediaPlayer.this.mSubtitleController == null) {
                    return 901;
                }
                SubtitleTrack track = MediaPlayer.this.mSubtitleController.addTrack(fFormat);
                if (track == null) {
                    return 901;
                }
                Scanner scanner = new Scanner(fIs, "UTF-8");
                String contents = scanner.useDelimiter("\\A").next();
                synchronized (MediaPlayer.this.mOpenSubtitleSources) {
                    MediaPlayer.this.mOpenSubtitleSources.remove(fIs);
                }
                scanner.close();
                synchronized (MediaPlayer.this.mIndexTrackPairs) {
                    MediaPlayer.this.mIndexTrackPairs.add(Pair.create(null, track));
                }
                Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
                h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(track, contents.getBytes())));
                return 803;
            }

            public void run() {
                int res = addTrack();
                if (MediaPlayer.this.mEventHandler != null) {
                    MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(200, res, 0, null));
                }
                thread.getLooper().quitSafely();
            }
        });
    }

    /* access modifiers changed from: private */
    public void scanInternalSubtitleTracks() {
        setSubtitleAnchor();
        populateInbandTracks();
        if (this.mSubtitleController != null) {
            this.mSubtitleController.selectDefaultTrack();
        }
    }

    private void populateInbandTracks() {
        TrackInfo[] tracks = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            for (int i = 0; i < tracks.length; i++) {
                if (!this.mInbandTrackIndices.get(i)) {
                    this.mInbandTrackIndices.set(i);
                    if (tracks[i] == null) {
                        Log.w(TAG, "unexpected NULL track at index " + i);
                    }
                    if (tracks[i] == null || tracks[i].getTrackType() != 4) {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), null));
                    } else {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), this.mSubtitleController.addTrack(tracks[i].getFormat())));
                    }
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

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0039, code lost:
        if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003b, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0040, code lost:
        if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0044, code lost:
        if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0047, code lost:
        return;
     */
    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (fd == null) {
                if (fd != null) {
                    fd.close();
                }
                return;
            }
            addTimedTextSource(fd.getFileDescriptor(), mimeType);
            if (fd != null) {
                fd.close();
            }
        } catch (SecurityException e) {
        } catch (IOException e2) {
        } catch (IllegalStateException e3) {
        } catch (Throwable th) {
            if (fd != null) {
                fd.close();
            }
            throw th;
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) throws IllegalArgumentException, IllegalStateException {
        addTimedTextSource(fd, 0, DataSourceDesc.LONG_MAX, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mime) throws IllegalArgumentException, IllegalStateException {
        String str = mime;
        if (availableMimeTypeForExternalSource(mime)) {
            try {
                final FileDescriptor dupedFd = Os.dup(fd);
                MediaFormat fFormat = new MediaFormat();
                fFormat.setString(MediaFormat.KEY_MIME, str);
                fFormat.setInteger(MediaFormat.KEY_IS_TIMED_TEXT, 1);
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
                final long offset2 = offset;
                final long length2 = length;
                HandlerThread thread = new HandlerThread("TimedTextReadThread", 9);
                thread.start();
                Handler handler = new Handler(thread.getLooper());
                final SubtitleTrack subtitleTrack = track;
                final HandlerThread handlerThread = thread;
                AnonymousClass6 r1 = new Runnable() {
                    private int addTrack() {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            Os.lseek(dupedFd, offset2, OsConstants.SEEK_SET);
                            byte[] buffer = new byte[4096];
                            long total = 0;
                            while (true) {
                                if (total >= length2) {
                                    break;
                                }
                                int bytes = IoBridge.read(dupedFd, buffer, 0, (int) Math.min((long) buffer.length, length2 - total));
                                if (bytes < 0) {
                                    break;
                                }
                                bos.write(buffer, 0, bytes);
                                total += (long) bytes;
                            }
                            Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
                            h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(subtitleTrack, bos.toByteArray())));
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e) {
                                Log.e(MediaPlayer.TAG, e.getMessage(), e);
                            }
                            return 803;
                        } catch (Exception e2) {
                            Log.e(MediaPlayer.TAG, e2.getMessage(), e2);
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e3) {
                                Log.e(MediaPlayer.TAG, e3.getMessage(), e3);
                            }
                            return 900;
                        } catch (Throwable th) {
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e4) {
                                Log.e(MediaPlayer.TAG, e4.getMessage(), e4);
                            }
                            throw th;
                        }
                    }

                    public void run() {
                        int res = addTrack();
                        if (MediaPlayer.this.mEventHandler != null) {
                            MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(200, res, 0, null));
                        }
                        handlerThread.getLooper().quitSafely();
                    }
                };
                handler.post(r1);
            } catch (ErrnoException ex) {
                ErrnoException errnoException = ex;
                Log.e(TAG, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        } else {
            throw new IllegalArgumentException("Illegal mimeType for timed text source: " + str);
        }
    }

    public int getSelectedTrack(int trackType) throws IllegalStateException {
        int i = 0;
        if (this.mSubtitleController != null && (trackType == 4 || trackType == 3)) {
            SubtitleTrack subtitleTrack = this.mSubtitleController.getSelectedTrack();
            if (subtitleTrack != null) {
                synchronized (this.mIndexTrackPairs) {
                    for (int i2 = 0; i2 < this.mIndexTrackPairs.size(); i2++) {
                        if (this.mIndexTrackPairs.get(i2).second == subtitleTrack && subtitleTrack.getTrackType() == trackType) {
                            return i2;
                        }
                    }
                }
            }
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(7);
            request.writeInt(trackType);
            invoke(request, reply);
            int inbandTrackIndex = reply.readInt();
            synchronized (this.mIndexTrackPairs) {
                while (i < this.mIndexTrackPairs.size()) {
                    Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(i);
                    if (p.first == null || ((Integer) p.first).intValue() != inbandTrackIndex) {
                        i++;
                    } else {
                        request.recycle();
                        reply.recycle();
                        return i;
                    }
                }
                request.recycle();
                reply.recycle();
                return -1;
            }
        } catch (Throwable th) {
            request.recycle();
            reply.recycle();
            throw th;
        }
    }

    public void selectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, true);
    }

    public void deselectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, false);
    }

    private void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
        populateInbandTracks();
        try {
            Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(index);
            SubtitleTrack track = (SubtitleTrack) p.second;
            if (track == null) {
                selectOrDeselectInbandTrack(((Integer) p.first).intValue(), select);
            } else if (this.mSubtitleController != null) {
                if (!select) {
                    if (this.mSubtitleController.getSelectedTrack() == track) {
                        this.mSubtitleController.selectTrack(null);
                    } else {
                        Log.w(TAG, "trying to deselect track that was not selected");
                    }
                    return;
                }
                if (track.getTrackType() == 3) {
                    int ttIndex = getSelectedTrack(3);
                    synchronized (this.mIndexTrackPairs) {
                        if (ttIndex >= 0) {
                            try {
                                if (ttIndex < this.mIndexTrackPairs.size()) {
                                    Pair<Integer, SubtitleTrack> p2 = this.mIndexTrackPairs.get(ttIndex);
                                    if (p2.first != null && p2.second == null) {
                                        selectOrDeselectInbandTrack(((Integer) p2.first).intValue(), false);
                                    }
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    }
                }
                this.mSubtitleController.selectTrack(track);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void selectOrDeselectInbandTrack(int index, boolean select) throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(select ? 4 : 5);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public void setRetransmitEndpoint(InetSocketAddress endpoint) throws IllegalStateException, IllegalArgumentException {
        String addrString = null;
        int port = 0;
        if (endpoint != null) {
            addrString = endpoint.getAddress().getHostAddress();
            port = endpoint.getPort();
        }
        int ret = native_setRetransmitEndpoint(addrString, port);
        if (ret != 0) {
            throw new IllegalArgumentException("Illegal re-transmit endpoint; native ret " + ret);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (!this.isReleased) {
            baseRelease();
            native_finalize();
            if (MEDIA_REPORT_PROP && this.mMediaReportRegister) {
                int res = HwFrameworkFactory.getHwCommBoosterServiceManager().unRegisterCallBack(MEDIA_REPORT_PKG_NAME, this.mIHwCommBoosterCallBack);
                if (res != 0) {
                    Log.e(TAG, "unRegisterCallBack in finalize return error = " + res);
                }
            }
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
            if (what == 1) {
                synchronized (mp.mDrmLock) {
                    mp.mDrmInfoResolved = true;
                }
            } else if (what != 200) {
                if (what == 210) {
                    Log.v(TAG, "postEventFromNative MEDIA_DRM_INFO");
                    if (obj instanceof Parcel) {
                        DrmInfo drmInfo = new DrmInfo((Parcel) obj);
                        synchronized (mp.mDrmLock) {
                            mp.mDrmInfo = drmInfo;
                        }
                    } else {
                        Log.w(TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + obj);
                    }
                }
            } else if (arg1 == 2) {
                new Thread(new Runnable() {
                    public void run() {
                        MediaPlayer.this.start();
                    }
                }).start();
                Thread.yield();
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

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("Illegal null listener");
        } else if (handler != null) {
            setOnSubtitleDataListenerInt(listener, handler);
        } else {
            throw new IllegalArgumentException("Illegal null handler");
        }
    }

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener) {
        if (listener != null) {
            setOnSubtitleDataListenerInt(listener, null);
            return;
        }
        throw new IllegalArgumentException("Illegal null listener");
    }

    public void clearOnSubtitleDataListener() {
        setOnSubtitleDataListenerInt(null, null);
    }

    private void setOnSubtitleDataListenerInt(OnSubtitleDataListener listener, Handler handler) {
        synchronized (this) {
            this.mExtSubtitleDataListener = listener;
            this.mExtSubtitleDataHandler = handler;
        }
    }

    public void setOnMediaTimeDiscontinuityListener(OnMediaTimeDiscontinuityListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("Illegal null listener");
        } else if (handler != null) {
            setOnMediaTimeDiscontinuityListenerInt(listener, handler);
        } else {
            throw new IllegalArgumentException("Illegal null handler");
        }
    }

    public void setOnMediaTimeDiscontinuityListener(OnMediaTimeDiscontinuityListener listener) {
        if (listener != null) {
            setOnMediaTimeDiscontinuityListenerInt(listener, null);
            return;
        }
        throw new IllegalArgumentException("Illegal null listener");
    }

    public void clearOnMediaTimeDiscontinuityListener() {
        setOnMediaTimeDiscontinuityListenerInt(null, null);
    }

    private void setOnMediaTimeDiscontinuityListenerInt(OnMediaTimeDiscontinuityListener listener, Handler handler) {
        synchronized (this) {
            this.mOnMediaTimeDiscontinuityListener = listener;
            this.mOnMediaTimeDiscontinuityHandler = handler;
        }
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

    public void setOnDrmConfigHelper(OnDrmConfigHelper listener) {
        synchronized (this.mDrmLock) {
            this.mOnDrmConfigHelper = listener;
        }
    }

    public void setOnDrmInfoListener(OnDrmInfoListener listener) {
        setOnDrmInfoListener(listener, null);
    }

    public void setOnDrmInfoListener(OnDrmInfoListener listener, Handler handler) {
        synchronized (this.mDrmLock) {
            if (listener != null) {
                try {
                    this.mOnDrmInfoHandlerDelegate = new OnDrmInfoHandlerDelegate(this, listener, handler);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mOnDrmInfoHandlerDelegate = null;
            }
        }
    }

    public void setOnDrmPreparedListener(OnDrmPreparedListener listener) {
        setOnDrmPreparedListener(listener, null);
    }

    public void setOnDrmPreparedListener(OnDrmPreparedListener listener, Handler handler) {
        synchronized (this.mDrmLock) {
            if (listener != null) {
                try {
                    this.mOnDrmPreparedHandlerDelegate = new OnDrmPreparedHandlerDelegate(this, listener, handler);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mOnDrmPreparedHandlerDelegate = null;
            }
        }
    }

    public DrmInfo getDrmInfo() {
        DrmInfo drmInfo = null;
        synchronized (this.mDrmLock) {
            if (!this.mDrmInfoResolved) {
                if (this.mDrmInfo == null) {
                    Log.v(TAG, "The Player has not been prepared yet");
                    throw new IllegalStateException("The Player has not been prepared yet");
                }
            }
            if (this.mDrmInfo != null) {
                drmInfo = this.mDrmInfo.makeCopy();
            }
        }
        return drmInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0064, code lost:
        if (0 != 0) goto L_0x0066;
     */
    public void prepareDrm(UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, ProvisioningNetworkErrorException, ProvisioningServerErrorException {
        OnDrmPreparedHandlerDelegate onDrmPreparedHandlerDelegate;
        Log.v(TAG, "prepareDrm: uuid: " + uuid + " mOnDrmConfigHelper: " + this.mOnDrmConfigHelper);
        boolean allDoneWithoutProvisioning = false;
        synchronized (this.mDrmLock) {
            if (this.mDrmInfo == null) {
                Log.e(TAG, "prepareDrm(): Wrong usage: The player must be prepared and DRM info be retrieved before this call.");
                throw new IllegalStateException("prepareDrm(): Wrong usage: The player must be prepared and DRM info be retrieved before this call.");
            } else if (this.mActiveDrmScheme) {
                String msg = "prepareDrm(): Wrong usage: There is already an active DRM scheme with " + this.mDrmUUID;
                Log.e(TAG, msg);
                throw new IllegalStateException(msg);
            } else if (this.mPrepareDrmInProgress) {
                Log.e(TAG, "prepareDrm(): Wrong usage: There is already a pending prepareDrm call.");
                throw new IllegalStateException("prepareDrm(): Wrong usage: There is already a pending prepareDrm call.");
            } else if (!this.mDrmProvisioningInProgress) {
                cleanDrmObj();
                this.mPrepareDrmInProgress = true;
                onDrmPreparedHandlerDelegate = this.mOnDrmPreparedHandlerDelegate;
                try {
                    prepareDrm_createDrmStep(uuid);
                    this.mDrmConfigAllowed = true;
                } catch (Exception e) {
                    Log.w(TAG, "prepareDrm(): Exception ", e);
                    this.mPrepareDrmInProgress = false;
                    throw e;
                }
            } else {
                Log.e(TAG, "prepareDrm(): Unexpectd: Provisioning is already in progress.");
                throw new IllegalStateException("prepareDrm(): Unexpectd: Provisioning is already in progress.");
            }
        }
        if (this.mOnDrmConfigHelper != null) {
            this.mOnDrmConfigHelper.onDrmConfig(this);
        }
        synchronized (this.mDrmLock) {
            this.mDrmConfigAllowed = false;
            try {
                prepareDrm_openSessionStep(uuid);
                this.mDrmUUID = uuid;
                this.mActiveDrmScheme = true;
                allDoneWithoutProvisioning = true;
                if (!this.mDrmProvisioningInProgress) {
                    this.mPrepareDrmInProgress = false;
                }
            } catch (IllegalStateException e2) {
                Log.e(TAG, "prepareDrm(): Wrong usage: The player must be in the prepared state to call prepareDrm().");
                throw new IllegalStateException("prepareDrm(): Wrong usage: The player must be in the prepared state to call prepareDrm().");
            } catch (NotProvisionedException e3) {
                Log.w(TAG, "prepareDrm: NotProvisionedException");
                int result = HandleProvisioninig(uuid);
                if (result != 0) {
                    switch (result) {
                        case 1:
                            Log.e(TAG, "prepareDrm: Provisioning was required but failed due to a network error.");
                            throw new ProvisioningNetworkErrorException("prepareDrm: Provisioning was required but failed due to a network error.");
                        case 2:
                            Log.e(TAG, "prepareDrm: Provisioning was required but the request was denied by the server.");
                            throw new ProvisioningServerErrorException("prepareDrm: Provisioning was required but the request was denied by the server.");
                        default:
                            Log.e(TAG, "prepareDrm: Post-provisioning preparation failed.");
                            throw new IllegalStateException("prepareDrm: Post-provisioning preparation failed.");
                    }
                } else {
                    if (!this.mDrmProvisioningInProgress) {
                        this.mPrepareDrmInProgress = false;
                    }
                    if (0 != 0) {
                        cleanDrmObj();
                    }
                    if (allDoneWithoutProvisioning) {
                        return;
                    }
                }
            } catch (Exception e4) {
                Log.e(TAG, "prepareDrm: Exception " + e4);
                throw e4;
            } catch (Throwable th) {
                if (!this.mDrmProvisioningInProgress) {
                    this.mPrepareDrmInProgress = false;
                }
                if (0 != 0) {
                    cleanDrmObj();
                }
                throw th;
            }
        }
        if (allDoneWithoutProvisioning && onDrmPreparedHandlerDelegate != null) {
            onDrmPreparedHandlerDelegate.notifyClient(0);
        }
    }

    public void releaseDrm() throws NoDrmSchemeException {
        Log.v(TAG, "releaseDrm:");
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                try {
                    _releaseDrm();
                    cleanDrmObj();
                    this.mActiveDrmScheme = false;
                } catch (IllegalStateException e) {
                    Log.w(TAG, "releaseDrm: Exception ", e);
                    throw new IllegalStateException("releaseDrm: The player is not in a valid state.");
                } catch (Exception e2) {
                    Log.e(TAG, "releaseDrm: Exception ", e2);
                }
            } else {
                Log.e(TAG, "releaseDrm(): No active DRM scheme to release.");
                throw new NoDrmSchemeException("releaseDrm: No active DRM scheme to release.");
            }
        }
    }

    public MediaDrm.KeyRequest getKeyRequest(byte[] keySetId, byte[] initData, String mimeType, int keyType, Map<String, String> optionalParameters) throws NoDrmSchemeException {
        byte[] scope;
        HashMap<String, String> hmapOptionalParameters;
        MediaDrm.KeyRequest request;
        Log.v(TAG, "getKeyRequest:  keySetId: " + keySetId + " initData:" + initData + " mimeType: " + mimeType + " keyType: " + keyType + " optionalParameters: " + optionalParameters);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keyType != 3) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "getKeyRequest NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("getKeyRequest: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "getKeyRequest Exception " + e2);
                        throw e2;
                    }
                } else {
                    scope = keySetId;
                }
                if (optionalParameters != null) {
                    hmapOptionalParameters = new HashMap<>(optionalParameters);
                } else {
                    hmapOptionalParameters = null;
                }
                request = this.mDrmObj.getKeyRequest(scope, initData, mimeType, keyType, hmapOptionalParameters);
                Log.v(TAG, "getKeyRequest:   --> request: " + request);
            } else {
                Log.e(TAG, "getKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeException("getKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return request;
    }

    public byte[] provideKeyResponse(byte[] keySetId, byte[] response) throws NoDrmSchemeException, DeniedByServerException {
        byte[] scope;
        byte[] keySetResult;
        Log.v(TAG, "provideKeyResponse: keySetId: " + keySetId + " response: " + response);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keySetId == null) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "provideKeyResponse NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("provideKeyResponse: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "provideKeyResponse Exception " + e2);
                        throw e2;
                    }
                } else {
                    scope = keySetId;
                }
                keySetResult = this.mDrmObj.provideKeyResponse(scope, response);
                Log.v(TAG, "provideKeyResponse: keySetId: " + keySetId + " response: " + response + " --> " + keySetResult);
            } else {
                Log.e(TAG, "getKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeException("getKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return keySetResult;
    }

    public void restoreKeys(byte[] keySetId) throws NoDrmSchemeException {
        Log.v(TAG, "restoreKeys: keySetId: " + keySetId);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                try {
                    this.mDrmObj.restoreKeys(this.mDrmSessionId, keySetId);
                } catch (Exception e) {
                    Log.w(TAG, "restoreKeys Exception " + e);
                    throw e;
                }
            } else {
                Log.w(TAG, "restoreKeys NoDrmSchemeException");
                throw new NoDrmSchemeException("restoreKeys: Has to set a DRM scheme first.");
            }
        }
    }

    public String getDrmPropertyString(String propertyName) throws NoDrmSchemeException {
        String value;
        Log.v(TAG, "getDrmPropertyString: propertyName: " + propertyName);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "getDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeException("getDrmPropertyString: Has to prepareDrm() first.");
                }
            }
            try {
                value = this.mDrmObj.getPropertyString(propertyName);
            } catch (Exception e) {
                Log.w(TAG, "getDrmPropertyString Exception " + e);
                throw e;
            }
        }
        Log.v(TAG, "getDrmPropertyString: propertyName: " + propertyName + " --> value: " + value);
        return value;
    }

    public void setDrmPropertyString(String propertyName, String value) throws NoDrmSchemeException {
        Log.v(TAG, "setDrmPropertyString: propertyName: " + propertyName + " value: " + value);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "setDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeException("setDrmPropertyString: Has to prepareDrm() first.");
                }
            }
            try {
                this.mDrmObj.setPropertyString(propertyName, value);
            } catch (Exception e) {
                Log.w(TAG, "setDrmPropertyString Exception " + e);
                throw e;
            }
        }
    }

    private void prepareDrm_createDrmStep(UUID uuid) throws UnsupportedSchemeException {
        Log.v(TAG, "prepareDrm_createDrmStep: UUID: " + uuid);
        try {
            this.mDrmObj = new MediaDrm(uuid);
            Log.v(TAG, "prepareDrm_createDrmStep: Created mDrmObj=" + this.mDrmObj);
        } catch (Exception e) {
            Log.e(TAG, "prepareDrm_createDrmStep: MediaDrm failed with " + e);
            throw e;
        }
    }

    private void prepareDrm_openSessionStep(UUID uuid) throws NotProvisionedException, ResourceBusyException {
        Log.v(TAG, "prepareDrm_openSessionStep: uuid: " + uuid);
        try {
            this.mDrmSessionId = this.mDrmObj.openSession();
            Log.v(TAG, "prepareDrm_openSessionStep: mDrmSessionId=" + this.mDrmSessionId);
            _prepareDrm(getByteArrayFromUUID(uuid), this.mDrmSessionId);
            Log.v(TAG, "prepareDrm_openSessionStep: _prepareDrm/Crypto succeeded");
        } catch (Exception e) {
            Log.e(TAG, "prepareDrm_openSessionStep: open/crypto failed with " + e);
            throw e;
        }
    }

    private int HandleProvisioninig(UUID uuid) {
        int result;
        if (this.mDrmProvisioningInProgress) {
            Log.e(TAG, "HandleProvisioninig: Unexpected mDrmProvisioningInProgress");
            return 3;
        }
        MediaDrm.ProvisionRequest provReq = this.mDrmObj.getProvisionRequest();
        if (provReq == null) {
            Log.e(TAG, "HandleProvisioninig: getProvisionRequest returned null.");
            return 3;
        }
        Log.v(TAG, "HandleProvisioninig provReq  data: " + provReq.getData() + " url: " + provReq.getDefaultUrl());
        this.mDrmProvisioningInProgress = true;
        this.mDrmProvisioningThread = new ProvisioningThread().initialize(provReq, uuid, this);
        this.mDrmProvisioningThread.start();
        if (this.mOnDrmPreparedHandlerDelegate != null) {
            result = 0;
        } else {
            try {
                this.mDrmProvisioningThread.join();
            } catch (Exception e) {
                Log.w(TAG, "HandleProvisioninig: Thread.join Exception " + e);
            }
            result = this.mDrmProvisioningThread.status();
            this.mDrmProvisioningThread = null;
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean resumePrepareDrm(UUID uuid) {
        Log.v(TAG, "resumePrepareDrm: uuid: " + uuid);
        try {
            prepareDrm_openSessionStep(uuid);
            this.mDrmUUID = uuid;
            this.mActiveDrmScheme = true;
            return true;
        } catch (Exception e) {
            Log.w(TAG, "HandleProvisioninig: Thread run _prepareDrm resume failed with " + e);
            return false;
        }
    }

    private void resetDrmState() {
        synchronized (this.mDrmLock) {
            Log.v(TAG, "resetDrmState:  mDrmInfo=" + this.mDrmInfo + " mDrmProvisioningThread=" + this.mDrmProvisioningThread + " mPrepareDrmInProgress=" + this.mPrepareDrmInProgress + " mActiveDrmScheme=" + this.mActiveDrmScheme);
            this.mDrmInfoResolved = false;
            this.mDrmInfo = null;
            if (this.mDrmProvisioningThread != null) {
                try {
                    this.mDrmProvisioningThread.join();
                } catch (InterruptedException e) {
                    Log.w(TAG, "resetDrmState: ProvThread.join Exception " + e);
                }
                this.mDrmProvisioningThread = null;
            }
            this.mPrepareDrmInProgress = false;
            this.mActiveDrmScheme = false;
            cleanDrmObj();
        }
    }

    /* access modifiers changed from: private */
    public void cleanDrmObj() {
        Log.v(TAG, "cleanDrmObj: mDrmObj=" + this.mDrmObj + " mDrmSessionId=" + this.mDrmSessionId);
        if (this.mDrmSessionId != null) {
            this.mDrmObj.closeSession(this.mDrmSessionId);
            this.mDrmSessionId = null;
        }
        if (this.mDrmObj != null) {
            this.mDrmObj.release();
            this.mDrmObj = null;
        }
    }

    private static final byte[] getByteArrayFromUUID(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] uuidBytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            uuidBytes[i] = (byte) ((int) (msb >>> ((7 - i) * 8)));
            uuidBytes[8 + i] = (byte) ((int) (lsb >>> (8 * (7 - i))));
        }
        return uuidBytes;
    }

    private boolean isVideoScalingModeSupported(int mode) {
        return mode == 1 || mode == 2;
    }
}
