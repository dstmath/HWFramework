package android.media;

import android.app.ActivityThread;
import android.app.backup.FullBackup;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioRouting;
import android.media.AudioTrack;
import android.media.MediaDrm;
import android.media.MediaPlayer2;
import android.media.MediaPlayer2Impl;
import android.media.MediaPlayerBase;
import android.media.MediaTimeProvider;
import android.media.SubtitleController;
import android.media.SubtitleTrack;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoBridge;
import libcore.io.Streams;

public final class MediaPlayer2Impl extends MediaPlayer2 {
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
    private static final int MEDIA_DRM_INFO = 210;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;
    private static final int MEDIA_META_DATA = 202;
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_NOTIFY_TIME = 98;
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
    private static final int NEXT_SOURCE_STATE_ERROR = -1;
    private static final int NEXT_SOURCE_STATE_INIT = 0;
    private static final int NEXT_SOURCE_STATE_PREPARED = 2;
    private static final int NEXT_SOURCE_STATE_PREPARING = 1;
    private static final String TAG = "MediaPlayer2Impl";
    /* access modifiers changed from: private */
    public boolean mActiveDrmScheme;
    /* access modifiers changed from: private */
    public AtomicInteger mBufferedPercentageCurrent;
    /* access modifiers changed from: private */
    public AtomicInteger mBufferedPercentageNext;
    /* access modifiers changed from: private */
    public DataSourceDesc mCurrentDSD;
    /* access modifiers changed from: private */
    public long mCurrentSrcId;
    /* access modifiers changed from: private */
    @GuardedBy("mTaskLock")
    public Task mCurrentTask;
    private boolean mDrmConfigAllowed;
    /* access modifiers changed from: private */
    public ArrayList<Pair<Executor, MediaPlayer2.DrmEventCallback>> mDrmEventCallbackRecords;
    /* access modifiers changed from: private */
    public final Object mDrmEventCbLock;
    /* access modifiers changed from: private */
    public DrmInfoImpl mDrmInfoImpl;
    private boolean mDrmInfoResolved;
    /* access modifiers changed from: private */
    public final Object mDrmLock;
    /* access modifiers changed from: private */
    public MediaDrm mDrmObj;
    /* access modifiers changed from: private */
    public boolean mDrmProvisioningInProgress;
    private ProvisioningThread mDrmProvisioningThread;
    /* access modifiers changed from: private */
    public byte[] mDrmSessionId;
    private UUID mDrmUUID;
    /* access modifiers changed from: private */
    public ArrayList<Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback>> mEventCallbackRecords;
    /* access modifiers changed from: private */
    public final Object mEventCbLock;
    /* access modifiers changed from: private */
    public EventHandler mEventHandler;
    private final CloseGuard mGuard = CloseGuard.get();
    private HandlerThread mHandlerThread;
    private BitSet mInbandTrackIndices;
    /* access modifiers changed from: private */
    public Vector<Pair<Integer, SubtitleTrack>> mIndexTrackPairs;
    private int mListenerContext;
    /* access modifiers changed from: private */
    public long mNativeContext;
    private long mNativeSurfaceTexture;
    /* access modifiers changed from: private */
    public List<DataSourceDesc> mNextDSDs;
    /* access modifiers changed from: private */
    public boolean mNextSourcePlayPending;
    /* access modifiers changed from: private */
    public int mNextSourceState;
    /* access modifiers changed from: private */
    public long mNextSrcId;
    private MediaPlayer2.OnDrmConfigHelper mOnDrmConfigHelper;
    /* access modifiers changed from: private */
    public MediaPlayer2.OnSubtitleDataListener mOnSubtitleDataListener;
    /* access modifiers changed from: private */
    public Vector<InputStream> mOpenSubtitleSources;
    @GuardedBy("mTaskLock")
    private final List<Task> mPendingTasks;
    private AudioDeviceInfo mPreferredDevice;
    /* access modifiers changed from: private */
    public boolean mPrepareDrmInProgress;
    /* access modifiers changed from: private */
    @GuardedBy("mRoutingChangeListeners")
    public ArrayMap<AudioRouting.OnRoutingChangedListener, NativeRoutingEventHandlerDelegate> mRoutingChangeListeners;
    /* access modifiers changed from: private */
    public boolean mScreenOnWhilePlaying;
    private int mSelectedSubtitleTrackIndex;
    /* access modifiers changed from: private */
    public long mSrcIdGenerator = 0;
    /* access modifiers changed from: private */
    public final Object mSrcLock = new Object();
    private boolean mStayAwake;
    private int mStreamType = Integer.MIN_VALUE;
    /* access modifiers changed from: private */
    public SubtitleController mSubtitleController;
    private MediaPlayer2.OnSubtitleDataListener mSubtitleDataListener;
    /* access modifiers changed from: private */
    public SurfaceHolder mSurfaceHolder;
    private final Handler mTaskHandler;
    /* access modifiers changed from: private */
    public final Object mTaskLock;
    /* access modifiers changed from: private */
    public TimeProvider mTimeProvider;
    /* access modifiers changed from: private */
    public volatile float mVolume;
    private PowerManager.WakeLock mWakeLock = null;

    public static final class DrmInfoImpl extends MediaPlayer2.DrmInfo {
        private Map<UUID, byte[]> mapPssh;
        private UUID[] supportedSchemes;

        public Map<UUID, byte[]> getPssh() {
            return this.mapPssh;
        }

        public List<UUID> getSupportedSchemes() {
            return Arrays.asList(this.supportedSchemes);
        }

        private DrmInfoImpl(Map<UUID, byte[]> Pssh, UUID[] SupportedSchemes) {
            this.mapPssh = Pssh;
            this.supportedSchemes = SupportedSchemes;
        }

        private DrmInfoImpl(Parcel parcel) {
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl(" + parcel + ") size " + parcel.dataSize());
            int psshsize = parcel.readInt();
            byte[] pssh = new byte[psshsize];
            parcel.readByteArray(pssh);
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() PSSH: " + arrToHex(pssh));
            this.mapPssh = parsePSSH(pssh, psshsize);
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() PSSH: " + this.mapPssh);
            int supportedDRMsCount = parcel.readInt();
            this.supportedSchemes = new UUID[supportedDRMsCount];
            for (int i = 0; i < supportedDRMsCount; i++) {
                byte[] uuid = new byte[16];
                parcel.readByteArray(uuid);
                this.supportedSchemes[i] = bytesToUUID(uuid);
                Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() supportedScheme[" + i + "]: " + this.supportedSchemes[i]);
            }
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() Parcel psshsize: " + psshsize + " supportedDRMsCount: " + supportedDRMsCount);
        }

        /* access modifiers changed from: private */
        public DrmInfoImpl makeCopy() {
            return new DrmInfoImpl(this.mapPssh, this.supportedSchemes);
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
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse UUID: (%d < 16) pssh: %d", objArr));
                    return null;
                }
                UUID uuid = bytesToUUID(Arrays.copyOfRange(bArr, i, i + 16));
                int i2 = i + 16;
                int len2 = len - 16;
                if (len2 < 4) {
                    Object[] objArr2 = new Object[2];
                    objArr2[c] = Integer.valueOf(len2);
                    objArr2[1] = Integer.valueOf(psshsize);
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse datalen: (%d < 4) pssh: %d", objArr2));
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
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse data: (%d < %d) pssh: %d", new Object[]{Integer.valueOf(len3), Integer.valueOf(datalen), Integer.valueOf(psshsize)}));
                    return null;
                }
                byte[] data = Arrays.copyOfRange(bArr, i3, i3 + datalen);
                i = i3 + datalen;
                len = len3 - datalen;
                Log.v(MediaPlayer2Impl.TAG, String.format("parsePSSH[%d]: <%s, %s> pssh: %d", new Object[]{Integer.valueOf(numentries), uuid, arrToHex(data), Integer.valueOf(psshsize)}));
                numentries++;
                result.put(uuid, data);
                c = 0;
            }
            return result;
        }
    }

    private class EventHandler extends Handler {
        private MediaPlayer2Impl mMediaPlayer;

        public EventHandler(MediaPlayer2Impl mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        public void handleMessage(Message msg) {
            handleMessage(msg, 0);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v0, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: android.media.MediaPlayer2Impl$DrmInfoImpl} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v46, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v52, resolved type: android.media.TimedText} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v51, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v70, resolved type: android.media.TimedMetaData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v75, resolved type: android.media.TimedMetaData} */
        /* JADX WARNING: type inference failed for: r3v9, types: [android.media.MediaPlayer2Impl$DrmInfoImpl] */
        /* JADX WARNING: type inference failed for: r3v68, types: [android.media.TimedText] */
        /* JADX WARNING: Code restructure failed: missing block: B:134:0x02a4, code lost:
            r2 = android.media.MediaPlayer2Impl.access$3300(r11.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:135:0x02aa, code lost:
            if (r2 == null) goto L_0x02b1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x02ac, code lost:
            r2.onSeekComplete(r11.mMediaPlayer);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:137:0x02b1, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:245:0x04ea, code lost:
            return;
         */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg, long srcId) {
            DrmInfoImpl drmInfo;
            DataSourceDesc dsd;
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(MediaPlayer2Impl.TAG, "mediaplayer2 went away with unhandled events");
                return;
            }
            int what = msg.arg1;
            int extra = msg.arg2;
            int i = msg.what;
            TimedMetaData data = null;
            if (i == 210) {
                if (msg.obj == null) {
                    Log.w(MediaPlayer2Impl.TAG, "MEDIA_DRM_INFO msg.obj=NULL");
                } else if (msg.obj instanceof Parcel) {
                    synchronized (MediaPlayer2Impl.this.mDrmLock) {
                        if (MediaPlayer2Impl.this.mDrmInfoImpl != null) {
                            data = MediaPlayer2Impl.this.mDrmInfoImpl.makeCopy();
                        }
                        drmInfo = data;
                    }
                    if (drmInfo != 0) {
                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                            Iterator it = MediaPlayer2Impl.this.mDrmEventCallbackRecords.iterator();
                            while (it.hasNext()) {
                                Pair<Executor, MediaPlayer2.DrmEventCallback> cb = (Pair) it.next();
                                ((Executor) cb.first).execute(new Runnable(cb, drmInfo) {
                                    private final /* synthetic */ Pair f$1;
                                    private final /* synthetic */ MediaPlayer2Impl.DrmInfoImpl f$2;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                    }

                                    public final void run() {
                                        ((MediaPlayer2.DrmEventCallback) this.f$1.second).onDrmInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2);
                                    }
                                });
                            }
                        }
                    }
                } else {
                    Log.w(MediaPlayer2Impl.TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + msg.obj);
                }
            } else if (i != 10000) {
                boolean z = true;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        try {
                            MediaPlayer2Impl.this.scanInternalSubtitleTracks();
                        } catch (RuntimeException e) {
                            sendMessage(obtainMessage(100, 1, -1010, null));
                        }
                        synchronized (MediaPlayer2Impl.this.mSrcLock) {
                            Log.i(MediaPlayer2Impl.TAG, "MEDIA_PREPARED: srcId=" + srcId + ", currentSrcId=" + MediaPlayer2Impl.this.mCurrentSrcId + ", nextSrcId=" + MediaPlayer2Impl.this.mNextSrcId);
                            if (srcId == MediaPlayer2Impl.this.mCurrentSrcId) {
                                dsd = MediaPlayer2Impl.this.mCurrentDSD;
                                MediaPlayer2Impl.this.prepareNextDataSource_l();
                            } else if (MediaPlayer2Impl.this.mNextDSDs == null || MediaPlayer2Impl.this.mNextDSDs.isEmpty() || srcId != MediaPlayer2Impl.this.mNextSrcId) {
                                dsd = null;
                            } else {
                                dsd = (DataSourceDesc) MediaPlayer2Impl.this.mNextDSDs.get(0);
                                int unused = MediaPlayer2Impl.this.mNextSourceState = 2;
                                if (MediaPlayer2Impl.this.mNextSourcePlayPending) {
                                    MediaPlayer2Impl.this.playNextDataSource_l();
                                }
                            }
                        }
                        if (dsd != null) {
                            synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                                Iterator it2 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                while (it2.hasNext()) {
                                    Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb2 = (Pair) it2.next();
                                    ((Executor) cb2.first).execute(new Runnable(cb2, dsd) {
                                        private final /* synthetic */ Pair f$1;
                                        private final /* synthetic */ DataSourceDesc f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        public final void run() {
                                            ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, this.f$2, 100, 0);
                                        }
                                    });
                                }
                            }
                        }
                        synchronized (MediaPlayer2Impl.this.mTaskLock) {
                            if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 6 && MediaPlayer2Impl.this.mCurrentTask.mDSD == dsd && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                                MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                                Task unused2 = MediaPlayer2Impl.this.mCurrentTask = null;
                                MediaPlayer2Impl.this.processPendingTask_l();
                            }
                        }
                        return;
                    case 2:
                        DataSourceDesc dsd2 = MediaPlayer2Impl.this.mCurrentDSD;
                        synchronized (MediaPlayer2Impl.this.mSrcLock) {
                            if (srcId == MediaPlayer2Impl.this.mCurrentSrcId) {
                                Log.i(MediaPlayer2Impl.TAG, "MEDIA_PLAYBACK_COMPLETE: srcId=" + srcId + ", currentSrcId=" + MediaPlayer2Impl.this.mCurrentSrcId + ", nextSrcId=" + MediaPlayer2Impl.this.mNextSrcId);
                                MediaPlayer2Impl.this.playNextDataSource_l();
                            }
                        }
                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                            Iterator it3 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                            while (it3.hasNext()) {
                                Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb3 = (Pair) it3.next();
                                ((Executor) cb3.first).execute(new Runnable(cb3, dsd2) {
                                    private final /* synthetic */ Pair f$1;
                                    private final /* synthetic */ DataSourceDesc f$2;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                    }

                                    public final void run() {
                                        ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, this.f$2, 5, 0);
                                    }
                                });
                            }
                        }
                        MediaPlayer2Impl.this.stayAwake(false);
                        return;
                    case 3:
                        int percent = msg.arg1;
                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                            if (srcId == MediaPlayer2Impl.this.mCurrentSrcId) {
                                MediaPlayer2Impl.this.mBufferedPercentageCurrent.set(percent);
                                Iterator it4 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                while (it4.hasNext()) {
                                    Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb4 = (Pair) it4.next();
                                    ((Executor) cb4.first).execute(new Runnable(cb4, percent) {
                                        private final /* synthetic */ Pair f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        public final void run() {
                                            ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, MediaPlayer2.MEDIA_INFO_BUFFERING_UPDATE, this.f$2);
                                        }
                                    });
                                }
                            } else if (srcId == MediaPlayer2Impl.this.mNextSrcId && !MediaPlayer2Impl.this.mNextDSDs.isEmpty()) {
                                MediaPlayer2Impl.this.mBufferedPercentageNext.set(percent);
                                DataSourceDesc nextDSD = (DataSourceDesc) MediaPlayer2Impl.this.mNextDSDs.get(0);
                                Iterator it5 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                while (it5.hasNext()) {
                                    Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb5 = (Pair) it5.next();
                                    ((Executor) cb5.first).execute(new Runnable(cb5, nextDSD, percent) {
                                        private final /* synthetic */ Pair f$1;
                                        private final /* synthetic */ DataSourceDesc f$2;
                                        private final /* synthetic */ int f$3;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                            this.f$3 = r4;
                                        }

                                        public final void run() {
                                            ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, this.f$2, MediaPlayer2.MEDIA_INFO_BUFFERING_UPDATE, this.f$3);
                                        }
                                    });
                                }
                            }
                        }
                        return;
                    case 4:
                        synchronized (MediaPlayer2Impl.this.mTaskLock) {
                            if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 14 && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                                MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                                Task unused3 = MediaPlayer2Impl.this.mCurrentTask = null;
                                MediaPlayer2Impl.this.processPendingTask_l();
                            }
                            break;
                        }
                    case 5:
                        int width = msg.arg1;
                        int height = msg.arg2;
                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                            Iterator it6 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                            while (it6.hasNext()) {
                                Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb6 = (Pair) it6.next();
                                ((Executor) cb6.first).execute(new Runnable(cb6, width, height) {
                                    private final /* synthetic */ Pair f$1;
                                    private final /* synthetic */ int f$2;
                                    private final /* synthetic */ int f$3;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                    }

                                    public final void run() {
                                        ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onVideoSizeChanged(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2, this.f$3);
                                    }
                                });
                            }
                        }
                        return;
                    case 6:
                    case 7:
                        TimeProvider timeProvider = MediaPlayer2Impl.this.mTimeProvider;
                        if (timeProvider != null) {
                            if (msg.what != 7) {
                                z = false;
                            }
                            timeProvider.onPaused(z);
                            break;
                        }
                        break;
                    case 8:
                        TimeProvider timeProvider2 = MediaPlayer2Impl.this.mTimeProvider;
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
                                TimeProvider timeProvider3 = MediaPlayer2Impl.this.mTimeProvider;
                                if (timeProvider3 != null) {
                                    timeProvider3.onNotifyTime();
                                }
                                return;
                            case 99:
                                if (msg.obj instanceof Parcel) {
                                    Parcel parcel = (Parcel) msg.obj;
                                    data = new TimedText(parcel);
                                    parcel.recycle();
                                }
                                TimedText text = data;
                                synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                                    Iterator it7 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                    while (it7.hasNext()) {
                                        Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb7 = (Pair) it7.next();
                                        ((Executor) cb7.first).execute(new Runnable(cb7, text) {
                                            private final /* synthetic */ Pair f$1;
                                            private final /* synthetic */ TimedText f$2;

                                            {
                                                this.f$1 = r2;
                                                this.f$2 = r3;
                                            }

                                            public final void run() {
                                                ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onTimedText(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2);
                                            }
                                        });
                                    }
                                }
                                return;
                            case 100:
                                Log.e(MediaPlayer2Impl.TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                                synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                                    Iterator it8 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                    while (it8.hasNext()) {
                                        Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb8 = (Pair) it8.next();
                                        ((Executor) cb8.first).execute(new Runnable(cb8, what, extra) {
                                            private final /* synthetic */ Pair f$1;
                                            private final /* synthetic */ int f$2;
                                            private final /* synthetic */ int f$3;

                                            {
                                                this.f$1 = r2;
                                                this.f$2 = r3;
                                                this.f$3 = r4;
                                            }

                                            public final void run() {
                                                ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onError(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2, this.f$3);
                                            }
                                        });
                                        ((Executor) cb8.first).execute(new Runnable(cb8) {
                                            private final /* synthetic */ Pair f$1;

                                            {
                                                this.f$1 = r2;
                                            }

                                            public final void run() {
                                                ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, 5, 0);
                                            }
                                        });
                                    }
                                }
                                MediaPlayer2Impl.this.stayAwake(false);
                                return;
                            default:
                                switch (i) {
                                    case 200:
                                        int i2 = msg.arg1;
                                        if (i2 != 2) {
                                            switch (i2) {
                                                case 700:
                                                    Log.i(MediaPlayer2Impl.TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                                                    break;
                                                case 701:
                                                case 702:
                                                    TimeProvider timeProvider4 = MediaPlayer2Impl.this.mTimeProvider;
                                                    if (timeProvider4 != null) {
                                                        if (msg.arg1 != 701) {
                                                            z = false;
                                                        }
                                                        timeProvider4.onBuffering(z);
                                                        break;
                                                    }
                                                    break;
                                                default:
                                                    switch (i2) {
                                                        case 802:
                                                            try {
                                                                MediaPlayer2Impl.this.scanInternalSubtitleTracks();
                                                                break;
                                                            } catch (RuntimeException e2) {
                                                                sendMessage(obtainMessage(100, 1, -1010, null));
                                                                break;
                                                            }
                                                        case 803:
                                                            break;
                                                    }
                                                    msg.arg1 = 802;
                                                    if (MediaPlayer2Impl.this.mSubtitleController != null) {
                                                        MediaPlayer2Impl.this.mSubtitleController.selectDefaultTrack();
                                                        break;
                                                    }
                                                    break;
                                            }
                                        } else if (srcId == MediaPlayer2Impl.this.mCurrentSrcId) {
                                            MediaPlayer2Impl.this.prepareNextDataSource_l();
                                        }
                                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                                            Iterator it9 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                            while (it9.hasNext()) {
                                                Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb9 = (Pair) it9.next();
                                                ((Executor) cb9.first).execute(new Runnable(cb9, what, extra) {
                                                    private final /* synthetic */ Pair f$1;
                                                    private final /* synthetic */ int f$2;
                                                    private final /* synthetic */ int f$3;

                                                    {
                                                        this.f$1 = r2;
                                                        this.f$2 = r3;
                                                        this.f$3 = r4;
                                                    }

                                                    public final void run() {
                                                        ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onInfo(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2, this.f$3);
                                                    }
                                                });
                                            }
                                        }
                                        return;
                                    case 201:
                                        MediaPlayer2.OnSubtitleDataListener onSubtitleDataListener = MediaPlayer2Impl.this.mOnSubtitleDataListener;
                                        if (onSubtitleDataListener != null && (msg.obj instanceof Parcel)) {
                                            Parcel parcel2 = (Parcel) msg.obj;
                                            SubtitleData data2 = new SubtitleData(parcel2);
                                            parcel2.recycle();
                                            onSubtitleDataListener.onSubtitleData(this.mMediaPlayer, data2);
                                        }
                                        return;
                                    case 202:
                                        if (msg.obj instanceof Parcel) {
                                            Parcel parcel3 = (Parcel) msg.obj;
                                            data = TimedMetaData.createTimedMetaDataFromParcel(parcel3);
                                            parcel3.recycle();
                                        }
                                        TimedMetaData data3 = data;
                                        synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                                            Iterator it10 = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                                            while (it10.hasNext()) {
                                                Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb10 = (Pair) it10.next();
                                                ((Executor) cb10.first).execute(new Runnable(cb10, data3) {
                                                    private final /* synthetic */ Pair f$1;
                                                    private final /* synthetic */ TimedMetaData f$2;

                                                    {
                                                        this.f$1 = r2;
                                                        this.f$2 = r3;
                                                    }

                                                    public final void run() {
                                                        ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onTimedMetaDataAvailable(MediaPlayer2Impl.EventHandler.this.mMediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, this.f$2);
                                                    }
                                                });
                                            }
                                        }
                                        return;
                                    default:
                                        Log.e(MediaPlayer2Impl.TAG, "Unknown message type " + msg.what);
                                        return;
                                }
                        }
                }
            } else {
                AudioManager.resetAudioPortGeneration();
                synchronized (MediaPlayer2Impl.this.mRoutingChangeListeners) {
                    for (NativeRoutingEventHandlerDelegate delegate : MediaPlayer2Impl.this.mRoutingChangeListeners.values()) {
                        delegate.notifyClient();
                    }
                }
            }
        }
    }

    public static final class NoDrmSchemeExceptionImpl extends MediaPlayer2.NoDrmSchemeException {
        public NoDrmSchemeExceptionImpl(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ProvisioningNetworkErrorExceptionImpl extends MediaPlayer2.ProvisioningNetworkErrorException {
        public ProvisioningNetworkErrorExceptionImpl(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ProvisioningServerErrorExceptionImpl extends MediaPlayer2.ProvisioningServerErrorException {
        public ProvisioningServerErrorExceptionImpl(String detailMessage) {
            super(detailMessage);
        }
    }

    private class ProvisioningThread extends Thread {
        public static final int TIMEOUT_MS = 60000;
        private Object drmLock;
        private boolean finished;
        private MediaPlayer2Impl mediaPlayer;
        private int status;
        private String urlStr;
        private UUID uuid;

        private ProvisioningThread() {
        }

        public int status() {
            return this.status;
        }

        public ProvisioningThread initialize(MediaDrm.ProvisionRequest request, UUID uuid2, MediaPlayer2Impl mediaPlayer2) {
            this.drmLock = mediaPlayer2.mDrmLock;
            this.mediaPlayer = mediaPlayer2;
            this.urlStr = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
            this.uuid = uuid2;
            this.status = 3;
            Log.v(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread is initialised url: " + this.urlStr);
            return this;
        }

        public void run() {
            boolean hasCallback;
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
                    Log.v(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread run: response " + response.length + " " + response);
                    connection.disconnect();
                } catch (Exception e) {
                    this.status = 1;
                    Log.w(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread run: connect " + e + " url: " + url);
                    connection.disconnect();
                }
            } catch (Exception e2) {
                this.status = 1;
                Log.w(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread run: openConnection " + e2);
            } catch (Throwable th) {
                connection.disconnect();
                throw th;
            }
            if (response != null) {
                try {
                    MediaPlayer2Impl.this.mDrmObj.provideProvisionResponse(response);
                    Log.v(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse SUCCEEDED!");
                    provisioningSucceeded = true;
                } catch (Exception e3) {
                    this.status = 2;
                    Log.w(MediaPlayer2Impl.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse " + e3);
                }
            }
            boolean succeeded = false;
            synchronized (MediaPlayer2Impl.this.mDrmEventCbLock) {
                hasCallback = !MediaPlayer2Impl.this.mDrmEventCallbackRecords.isEmpty();
            }
            int i = 3;
            if (hasCallback) {
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
                        MediaPlayer2Impl.this.cleanDrmObj();
                    }
                }
                synchronized (MediaPlayer2Impl.this.mDrmEventCbLock) {
                    Iterator it = MediaPlayer2Impl.this.mDrmEventCallbackRecords.iterator();
                    while (it.hasNext()) {
                        Pair<Executor, MediaPlayer2.DrmEventCallback> cb = (Pair) it.next();
                        ((Executor) cb.first).execute(new Runnable(cb) {
                            private final /* synthetic */ Pair f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                ((MediaPlayer2.DrmEventCallback) this.f$1.second).onDrmPrepared(MediaPlayer2Impl.ProvisioningThread.this.mediaPlayer, MediaPlayer2Impl.this.mCurrentDSD, MediaPlayer2Impl.ProvisioningThread.this.status);
                            }
                        });
                    }
                }
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
                    MediaPlayer2Impl.this.cleanDrmObj();
                }
            }
            this.finished = true;
        }
    }

    private static class StreamEventCallback extends AudioTrack.StreamEventCallback {
        public long mJAudioTrackPtr;
        public long mNativeCallbackPtr;
        public long mUserDataPtr;

        public StreamEventCallback(long jAudioTrackPtr, long nativeCallbackPtr, long userDataPtr) {
            this.mJAudioTrackPtr = jAudioTrackPtr;
            this.mNativeCallbackPtr = nativeCallbackPtr;
            this.mUserDataPtr = userDataPtr;
        }

        public void onTearDown(AudioTrack track) {
            MediaPlayer2Impl.native_stream_event_onTearDown(this.mNativeCallbackPtr, this.mUserDataPtr);
        }

        public void onStreamPresentationEnd(AudioTrack track) {
            MediaPlayer2Impl.native_stream_event_onStreamPresentationEnd(this.mNativeCallbackPtr, this.mUserDataPtr);
        }

        public void onStreamDataRequest(AudioTrack track) {
            MediaPlayer2Impl.native_stream_event_onStreamDataRequest(this.mJAudioTrackPtr, this.mNativeCallbackPtr, this.mUserDataPtr);
        }
    }

    private abstract class Task implements Runnable {
        /* access modifiers changed from: private */
        public DataSourceDesc mDSD;
        /* access modifiers changed from: private */
        public final int mMediaCallType;
        /* access modifiers changed from: private */
        public final boolean mNeedToWaitForEventToComplete;

        /* access modifiers changed from: package-private */
        public abstract void process() throws IOException, MediaPlayer2.NoDrmSchemeException;

        public Task(int mediaCallType, boolean needToWaitForEventToComplete) {
            this.mMediaCallType = mediaCallType;
            this.mNeedToWaitForEventToComplete = needToWaitForEventToComplete;
        }

        public void run() {
            int status = 0;
            try {
                process();
            } catch (IllegalStateException e) {
                status = 1;
            } catch (IllegalArgumentException e2) {
                status = 2;
            } catch (SecurityException e3) {
                status = 3;
            } catch (IOException e4) {
                status = 4;
            } catch (MediaPlayer2.NoDrmSchemeException e5) {
                status = 5;
            } catch (Exception e6) {
                status = Integer.MIN_VALUE;
            }
            synchronized (MediaPlayer2Impl.this.mSrcLock) {
                this.mDSD = MediaPlayer2Impl.this.mCurrentDSD;
            }
            if (!this.mNeedToWaitForEventToComplete || status != 0) {
                sendCompleteNotification(status);
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    Task unused = MediaPlayer2Impl.this.mCurrentTask = null;
                    MediaPlayer2Impl.this.processPendingTask_l();
                }
            }
        }

        /* access modifiers changed from: private */
        public void sendCompleteNotification(int status) {
            if (this.mMediaCallType != 1003) {
                synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                    Iterator it = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                    while (it.hasNext()) {
                        Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb = (Pair) it.next();
                        ((Executor) cb.first).execute(new Runnable(cb, status) {
                            private final /* synthetic */ Pair f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onCallCompleted(MediaPlayer2Impl.this, MediaPlayer2Impl.Task.this.mDSD, MediaPlayer2Impl.Task.this.mMediaCallType, this.f$2);
                            }
                        });
                    }
                }
            }
        }
    }

    static class TimeProvider implements MediaTimeProvider {
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
        public EventHandler mEventHandler;
        private HandlerThread mHandlerThread;
        private long mLastReportedTime;
        private long mLastTimeUs = 0;
        private MediaTimeProvider.OnMediaTimeListener[] mListeners;
        private boolean mPaused = true;
        private boolean mPausing = false;
        private MediaPlayer2Impl mPlayer;
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

        public TimeProvider(MediaPlayer2Impl mp) {
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
                    this.mHandlerThread = new HandlerThread("MediaPlayer2MTPEventThread", -2);
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

        public void onSeekComplete(MediaPlayer2Impl mp) {
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

        /* JADX WARNING: Removed duplicated region for block: B:19:0x002e A[Catch:{ IllegalStateException -> 0x007e }] */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0057 A[SYNTHETIC, Splitter:B:25:0x0057] */
        public long getCurrentTimeUs(boolean refreshTime, boolean monotonic) throws IllegalStateException {
            boolean z;
            synchronized (this) {
                if (!this.mPaused || refreshTime) {
                    try {
                        this.mLastTimeUs = this.mPlayer.getCurrentPosition() * MAX_EARLY_CALLBACK_US;
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

    public static final class TrackInfoImpl extends MediaPlayer2.TrackInfo {
        static final Parcelable.Creator<TrackInfoImpl> CREATOR = new Parcelable.Creator<TrackInfoImpl>() {
            public TrackInfoImpl createFromParcel(Parcel in) {
                return new TrackInfoImpl(in);
            }

            public TrackInfoImpl[] newArray(int size) {
                return new TrackInfoImpl[size];
            }
        };
        final MediaFormat mFormat;
        final int mTrackType;

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

        TrackInfoImpl(Parcel in) {
            this.mTrackType = in.readInt();
            this.mFormat = MediaFormat.createSubtitleFormat(in.readString(), in.readString());
            if (this.mTrackType == 4) {
                this.mFormat.setInteger(MediaFormat.KEY_IS_AUTOSELECT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_DEFAULT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, in.readInt());
            }
        }

        TrackInfoImpl(int type, MediaFormat format) {
            this.mTrackType = type;
            this.mFormat = format;
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(getLanguage());
            if (this.mTrackType == 4) {
                dest.writeString(this.mFormat.getString(MediaFormat.KEY_MIME));
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

    /* access modifiers changed from: private */
    public native void _attachAuxEffect(int i);

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _notifyAt(long j);

    /* access modifiers changed from: private */
    public native void _pause() throws IllegalStateException;

    private native void _prepareDrm(byte[] bArr, byte[] bArr2);

    private native void _release();

    /* access modifiers changed from: private */
    public native void _releaseDrm();

    private native void _reset();

    /* access modifiers changed from: private */
    public final native void _seekTo(long j, int i);

    /* access modifiers changed from: private */
    public native void _setAudioSessionId(int i);

    /* access modifiers changed from: private */
    public native void _setAuxEffectSendLevel(float f);

    /* access modifiers changed from: private */
    public native void _setBufferingParams(BufferingParams bufferingParams);

    /* access modifiers changed from: private */
    public native void _setPlaybackParams(PlaybackParams playbackParams);

    /* access modifiers changed from: private */
    public native void _setSyncParams(SyncParams syncParams);

    /* access modifiers changed from: private */
    public native void _setVideoSurface(Surface surface);

    /* access modifiers changed from: private */
    public native void _setVolume(float f, float f2);

    /* access modifiers changed from: private */
    public native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native Parcel getParameter(int i);

    private native void nativeHandleDataSourceCallback(boolean z, long j, Media2DataSource media2DataSource);

    private native void nativeHandleDataSourceFD(boolean z, long j, FileDescriptor fileDescriptor, long j2, long j3) throws IOException;

    private native void nativeHandleDataSourceUrl(boolean z, long j, Media2HTTPService media2HTTPService, String str, String[] strArr, String[] strArr2) throws IOException;

    private native void nativePlayNextDataSource(long j);

    private final native void native_enableDeviceCallback(boolean z);

    private final native void native_finalize();

    private native int native_getMediaPlayer2State();

    private final native boolean native_getMetadata(boolean z, boolean z2, Parcel parcel);

    private native PersistableBundle native_getMetrics();

    private final native int native_getRoutedDeviceId();

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    private final native int native_setMetadataFilter(Parcel parcel);

    private final native boolean native_setOutputDevice(int i);

    private final native void native_setup(Object obj);

    /* access modifiers changed from: private */
    public static final native void native_stream_event_onStreamDataRequest(long j, long j2, long j3);

    /* access modifiers changed from: private */
    public static final native void native_stream_event_onStreamPresentationEnd(long j, long j2);

    /* access modifiers changed from: private */
    public static final native void native_stream_event_onTearDown(long j, long j2);

    /* access modifiers changed from: private */
    public native void setLooping(boolean z);

    /* access modifiers changed from: private */
    public native boolean setParameter(int i, Parcel parcel);

    public native void _prepare();

    public native int getAudioSessionId();

    public native BufferingParams getBufferingParams();

    public native long getCurrentPosition();

    public native long getDuration();

    public native PlaybackParams getPlaybackParams();

    public native SyncParams getSyncParams();

    public native int getVideoHeight();

    public native int getVideoWidth();

    public native boolean isLooping();

    public native boolean isPlaying();

    static {
        System.loadLibrary("media2_jni");
        native_init();
    }

    public MediaPlayer2Impl() {
        long j = this.mSrcIdGenerator;
        this.mSrcIdGenerator = j + 1;
        this.mCurrentSrcId = j;
        long j2 = this.mSrcIdGenerator;
        this.mSrcIdGenerator = 1 + j2;
        this.mNextSrcId = j2;
        this.mNextSourceState = 0;
        this.mNextSourcePlayPending = false;
        this.mBufferedPercentageCurrent = new AtomicInteger(0);
        this.mBufferedPercentageNext = new AtomicInteger(0);
        this.mVolume = 1.0f;
        this.mDrmLock = new Object();
        this.mTaskLock = new Object();
        this.mPendingTasks = new LinkedList();
        this.mPreferredDevice = null;
        this.mRoutingChangeListeners = new ArrayMap<>();
        this.mIndexTrackPairs = new Vector<>();
        this.mInbandTrackIndices = new BitSet();
        this.mSelectedSubtitleTrackIndex = -1;
        this.mSubtitleDataListener = new MediaPlayer2.OnSubtitleDataListener() {
            public void onSubtitleData(MediaPlayer2 mp, SubtitleData data) {
                int index = data.getTrackIndex();
                synchronized (MediaPlayer2Impl.this.mIndexTrackPairs) {
                    Iterator it = MediaPlayer2Impl.this.mIndexTrackPairs.iterator();
                    while (it.hasNext()) {
                        Pair<Integer, SubtitleTrack> p = (Pair) it.next();
                        if (!(p.first == null || ((Integer) p.first).intValue() != index || p.second == null)) {
                            ((SubtitleTrack) p.second).onData(data);
                        }
                    }
                }
            }
        };
        this.mEventCbLock = new Object();
        this.mEventCallbackRecords = new ArrayList<>();
        this.mDrmEventCbLock = new Object();
        this.mDrmEventCallbackRecords = new ArrayList<>();
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
        this.mHandlerThread = new HandlerThread("MediaPlayer2TaskThread");
        this.mHandlerThread.start();
        this.mTaskHandler = new Handler(this.mHandlerThread.getLooper());
        this.mTimeProvider = new TimeProvider(this);
        this.mOpenSubtitleSources = new Vector<>();
        this.mGuard.open("close");
        native_setup(new WeakReference(this));
    }

    public void close() {
        synchronized (this.mGuard) {
            release();
        }
    }

    public void play() {
        addTask(new Task(5, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.stayAwake(true);
                MediaPlayer2Impl.this._start();
            }
        });
    }

    public void prepare() {
        addTask(new Task(6, true) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this._prepare();
            }
        });
    }

    public void pause() {
        addTask(new Task(4, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.stayAwake(false);
                MediaPlayer2Impl.this._pause();
            }
        });
    }

    public void skipToNext() {
        addTask(new Task(29, false) {
            /* access modifiers changed from: package-private */
            public void process() {
            }
        });
    }

    public long getBufferedPosition() {
        return (getDuration() * ((long) this.mBufferedPercentageCurrent.get())) / 100;
    }

    public int getPlayerState() {
        switch (getMediaPlayer2State()) {
            case 1:
                return 0;
            case 2:
            case 3:
                return 1;
            case 4:
                return 2;
            default:
                return 3;
        }
    }

    public int getBufferingState() {
        return 0;
    }

    public void setAudioAttributes(final AudioAttributes attributes) {
        addTask(new Task(16, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                if (attributes != null) {
                    Parcel pattributes = Parcel.obtain();
                    attributes.writeToParcel(pattributes, 1);
                    boolean unused = MediaPlayer2Impl.this.setParameter(MediaPlayer2Impl.KEY_PARAMETER_AUDIO_ATTRIBUTES, pattributes);
                    pattributes.recycle();
                    return;
                }
                throw new IllegalArgumentException("Cannot set AudioAttributes to null");
            }
        });
    }

    public AudioAttributes getAudioAttributes() {
        Parcel pattributes = getParameter(KEY_PARAMETER_AUDIO_ATTRIBUTES);
        AudioAttributes attributes = AudioAttributes.CREATOR.createFromParcel(pattributes);
        pattributes.recycle();
        return attributes;
    }

    public void setDataSource(final DataSourceDesc dsd) {
        addTask(new Task(19, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
                synchronized (MediaPlayer2Impl.this.mSrcLock) {
                    DataSourceDesc unused = MediaPlayer2Impl.this.mCurrentDSD = dsd;
                    long unused2 = MediaPlayer2Impl.this.mCurrentSrcId = MediaPlayer2Impl.this.mSrcIdGenerator = 1 + MediaPlayer2Impl.this.mSrcIdGenerator;
                    try {
                        MediaPlayer2Impl.this.handleDataSource(true, dsd, MediaPlayer2Impl.this.mCurrentSrcId);
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public void setNextDataSource(final DataSourceDesc dsd) {
        addTask(new Task(22, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
                synchronized (MediaPlayer2Impl.this.mSrcLock) {
                    List unused = MediaPlayer2Impl.this.mNextDSDs = new ArrayList(1);
                    MediaPlayer2Impl.this.mNextDSDs.add(dsd);
                    long unused2 = MediaPlayer2Impl.this.mNextSrcId = MediaPlayer2Impl.this.mSrcIdGenerator = 1 + MediaPlayer2Impl.this.mSrcIdGenerator;
                    int unused3 = MediaPlayer2Impl.this.mNextSourceState = 0;
                    boolean unused4 = MediaPlayer2Impl.this.mNextSourcePlayPending = false;
                }
                if (MediaPlayer2Impl.this.getMediaPlayer2State() != 1) {
                    synchronized (MediaPlayer2Impl.this.mSrcLock) {
                        MediaPlayer2Impl.this.prepareNextDataSource_l();
                    }
                }
            }
        });
    }

    public void setNextDataSources(final List<DataSourceDesc> dsds) {
        addTask(new Task(23, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                if (dsds == null || dsds.size() == 0) {
                    throw new IllegalArgumentException("data source list cannot be null or empty.");
                }
                for (DataSourceDesc dsd : dsds) {
                    if (dsd == null) {
                        throw new IllegalArgumentException("DataSourceDesc in the source list cannot be null.");
                    }
                }
                synchronized (MediaPlayer2Impl.this.mSrcLock) {
                    List unused = MediaPlayer2Impl.this.mNextDSDs = new ArrayList(dsds);
                    long unused2 = MediaPlayer2Impl.this.mNextSrcId = MediaPlayer2Impl.this.mSrcIdGenerator = 1 + MediaPlayer2Impl.this.mSrcIdGenerator;
                    int unused3 = MediaPlayer2Impl.this.mNextSourceState = 0;
                    boolean unused4 = MediaPlayer2Impl.this.mNextSourcePlayPending = false;
                }
                if (MediaPlayer2Impl.this.getMediaPlayer2State() != 1) {
                    synchronized (MediaPlayer2Impl.this.mSrcLock) {
                        MediaPlayer2Impl.this.prepareNextDataSource_l();
                    }
                }
            }
        });
    }

    public DataSourceDesc getCurrentDataSource() {
        DataSourceDesc dataSourceDesc;
        synchronized (this.mSrcLock) {
            dataSourceDesc = this.mCurrentDSD;
        }
        return dataSourceDesc;
    }

    public void loopCurrent(final boolean loop) {
        addTask(new Task(3, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.setLooping(loop);
            }
        });
    }

    public void setPlaybackSpeed(final float speed) {
        addTask(new Task(25, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this._setPlaybackParams(MediaPlayer2Impl.this.getPlaybackParams().setSpeed(speed));
            }
        });
    }

    public float getPlaybackSpeed() {
        return getPlaybackParams().getSpeed();
    }

    public boolean isReversePlaybackSupported() {
        return false;
    }

    public void setPlayerVolume(final float volume) {
        addTask(new Task(26, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                float unused = MediaPlayer2Impl.this.mVolume = volume;
                MediaPlayer2Impl.this._setVolume(volume, volume);
            }
        });
    }

    public float getPlayerVolume() {
        return this.mVolume;
    }

    public float getMaxPlayerVolume() {
        return 1.0f;
    }

    public void registerPlayerEventCallback(Executor e, MediaPlayerBase.PlayerEventCallback cb) {
    }

    public void unregisterPlayerEventCallback(MediaPlayerBase.PlayerEventCallback cb) {
    }

    public Parcel newRequest() {
        return Parcel.obtain();
    }

    public void invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(0);
        if (retcode != 0) {
            throw new RuntimeException("failure code: " + retcode);
        }
    }

    public void notifyWhenCommandLabelReached(final Object label) {
        addTask(new Task(1003, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                synchronized (MediaPlayer2Impl.this.mEventCbLock) {
                    Iterator it = MediaPlayer2Impl.this.mEventCallbackRecords.iterator();
                    while (it.hasNext()) {
                        Pair<Executor, MediaPlayer2.MediaPlayer2EventCallback> cb = (Pair) it.next();
                        ((Executor) cb.first).execute(new Runnable(cb, label) {
                            private final /* synthetic */ Pair f$1;
                            private final /* synthetic */ Object f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                ((MediaPlayer2.MediaPlayer2EventCallback) this.f$1.second).onCommandLabelReached(MediaPlayer2Impl.this, this.f$2);
                            }
                        });
                    }
                }
            }
        });
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

    public void setSurface(final Surface surface) {
        addTask(new Task(27, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                if (MediaPlayer2Impl.this.mScreenOnWhilePlaying && surface != null) {
                    Log.w(MediaPlayer2Impl.TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
                }
                SurfaceHolder unused = MediaPlayer2Impl.this.mSurfaceHolder = null;
                MediaPlayer2Impl.this._setVideoSurface(surface);
                MediaPlayer2Impl.this.updateSurfaceScreenOn();
            }
        });
    }

    public void setVideoScalingMode(final int mode) {
        addTask(new Task(1002, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                if (MediaPlayer2Impl.this.isVideoScalingModeSupported(mode)) {
                    Parcel request = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        request.writeInt(6);
                        request.writeInt(mode);
                        MediaPlayer2Impl.this.invoke(request, reply);
                    } finally {
                        request.recycle();
                        reply.recycle();
                    }
                } else {
                    throw new IllegalArgumentException("Scaling mode " + mode + " is not supported");
                }
            }
        });
    }

    public void clearPendingCommands() {
    }

    private void addTask(Task task) {
        synchronized (this.mTaskLock) {
            this.mPendingTasks.add(task);
            processPendingTask_l();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mTaskLock")
    public void processPendingTask_l() {
        if (this.mCurrentTask == null && !this.mPendingTasks.isEmpty()) {
            Task task = this.mPendingTasks.remove(0);
            this.mCurrentTask = task;
            this.mTaskHandler.post(task);
        }
    }

    /* access modifiers changed from: private */
    public void handleDataSource(boolean isCurrent, DataSourceDesc dsd, long srcId) throws IOException {
        Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
        switch (dsd.getType()) {
            case 1:
                handleDataSource(isCurrent, srcId, dsd.getMedia2DataSource());
                return;
            case 2:
                handleDataSource(isCurrent, srcId, dsd.getFileDescriptor(), dsd.getFileDescriptorOffset(), dsd.getFileDescriptorLength());
                break;
            case 3:
                handleDataSource(isCurrent, srcId, dsd.getUriContext(), dsd.getUri(), dsd.getUriHeaders(), dsd.getUriCookies());
                return;
        }
        boolean z = isCurrent;
        long j = srcId;
    }

    private void handleDataSource(boolean isCurrent, long srcId, Context context, Uri uri, Map<String, String> headers, List<HttpCookie> cookies) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String scheme = uri.getScheme();
        String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            handleDataSource(isCurrent, srcId, uri.getPath(), (Map<String, String>) null, (List<HttpCookie>) null);
            return;
        }
        if (!"content".equals(scheme) || !"settings".equals(authority)) {
            Context context2 = context;
            if (!attemptDataSource(isCurrent, srcId, resolver, uri)) {
                handleDataSource(isCurrent, srcId, uri.toString(), headers, cookies);
            }
        } else {
            int type = RingtoneManager.getDefaultType(uri);
            Uri cacheUri = RingtoneManager.getCacheForType(type, context.getUserId());
            Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
            if (!attemptDataSource(isCurrent, srcId, resolver, cacheUri) && !attemptDataSource(isCurrent, srcId, resolver, actualUri)) {
                handleDataSource(isCurrent, srcId, uri.toString(), headers, cookies);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r5.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0068, code lost:
        android.util.Log.w(TAG, "Couldn't open " + r1 + ": " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0087, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0063 A[ExcHandler: IOException | NullPointerException | SecurityException (r0v1 'ex' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0007] */
    private boolean attemptDataSource(boolean isCurrent, long srcId, ContentResolver resolver, Uri uri) {
        AssetFileDescriptor afd;
        Throwable th;
        Uri uri2 = uri;
        try {
            afd = resolver.openAssetFileDescriptor(uri2, FullBackup.ROOT_TREE_TOKEN);
            if (afd.getDeclaredLength() < 0) {
                handleDataSource(isCurrent, srcId, afd.getFileDescriptor(), 0, (long) DataSourceDesc.LONG_MAX);
            } else {
                handleDataSource(isCurrent, srcId, afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            }
            if (afd != null) {
                afd.close();
            }
            return true;
        } catch (IOException | NullPointerException | SecurityException ex) {
        } catch (Throwable th2) {
            Throwable th3 = th;
            Throwable th4 = th2;
            if (afd != null) {
                if (th3 != null) {
                    afd.close();
                } else {
                    afd.close();
                }
            }
            throw th4;
        }
    }

    private void handleDataSource(boolean isCurrent, long srcId, String path, Map<String, String> headers, List<HttpCookie> cookies) throws IOException {
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
        handleDataSource(isCurrent, srcId, path, keys, values, cookies);
    }

    private void handleDataSource(boolean isCurrent, long srcId, String path, String[] keys, String[] values, List<HttpCookie> cookies) throws IOException {
        String path2;
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path2 = uri.getPath();
        } else if (scheme != null) {
            String str = path;
            nativeHandleDataSourceUrl(isCurrent, srcId, Media2HTTPService.createHTTPService(str, cookies), str, keys, values);
            return;
        } else {
            path2 = path;
        }
        List<HttpCookie> list = cookies;
        File file = new File(path2);
        if (file.exists()) {
            FileInputStream is = new FileInputStream(file);
            handleDataSource(isCurrent, srcId, is.getFD(), 0, (long) DataSourceDesc.LONG_MAX);
            is.close();
            return;
        }
        throw new IOException("handleDataSource failed.");
    }

    private void handleDataSource(boolean isCurrent, long srcId, FileDescriptor fd, long offset, long length) throws IOException {
        nativeHandleDataSourceFD(isCurrent, srcId, fd, offset, length);
    }

    private void handleDataSource(boolean isCurrent, long srcId, Media2DataSource dataSource) {
        nativeHandleDataSourceCallback(isCurrent, srcId, dataSource);
    }

    /* access modifiers changed from: private */
    public void prepareNextDataSource_l() {
        if (this.mNextDSDs != null && !this.mNextDSDs.isEmpty() && this.mNextSourceState == 0) {
            try {
                this.mNextSourceState = 1;
                handleDataSource(false, this.mNextDSDs.get(0), this.mNextSrcId);
            } catch (Exception e) {
                final Message msg2 = this.mEventHandler.obtainMessage(100, 1, -1010, null);
                final long nextSrcId = this.mNextSrcId;
                this.mEventHandler.post(new Runnable() {
                    public void run() {
                        MediaPlayer2Impl.this.mEventHandler.handleMessage(msg2, nextSrcId);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void playNextDataSource_l() {
        if (this.mNextDSDs != null && !this.mNextDSDs.isEmpty()) {
            if (this.mNextSourceState == 2) {
                this.mCurrentDSD = this.mNextDSDs.get(0);
                this.mCurrentSrcId = this.mNextSrcId;
                this.mBufferedPercentageCurrent.set(this.mBufferedPercentageNext.get());
                this.mNextDSDs.remove(0);
                long j = this.mSrcIdGenerator;
                this.mSrcIdGenerator = 1 + j;
                this.mNextSrcId = j;
                this.mBufferedPercentageNext.set(0);
                this.mNextSourceState = 0;
                this.mNextSourcePlayPending = false;
                final long srcId = this.mCurrentSrcId;
                try {
                    nativePlayNextDataSource(srcId);
                } catch (Exception e) {
                    final Message msg2 = this.mEventHandler.obtainMessage(100, 1, -1010, null);
                    this.mEventHandler.post(new Runnable() {
                        public void run() {
                            MediaPlayer2Impl.this.mEventHandler.handleMessage(msg2, srcId);
                        }
                    });
                }
            } else {
                if (this.mNextSourceState == 0) {
                    prepareNextDataSource_l();
                }
                this.mNextSourcePlayPending = true;
            }
        }
    }

    private int getAudioStreamType() {
        if (this.mStreamType == Integer.MIN_VALUE) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() {
        stayAwake(false);
        _stop();
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
        this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(536870912 | mode, MediaPlayer2Impl.class.getName());
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
        updateSurfaceScreenOn();
    }

    /* access modifiers changed from: private */
    public void updateSurfaceScreenOn() {
        if (this.mSurfaceHolder != null) {
            this.mSurfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying && this.mStayAwake);
        }
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    public int getMediaPlayer2State() {
        return native_getMediaPlayer2State();
    }

    public void setBufferingParams(final BufferingParams params) {
        addTask(new Task(1001, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(params, "the BufferingParams cannot be null");
                MediaPlayer2Impl.this._setBufferingParams(params);
            }
        });
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

    public void setPlaybackParams(final PlaybackParams params) {
        addTask(new Task(24, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(params, "the PlaybackParams cannot be null");
                MediaPlayer2Impl.this._setPlaybackParams(params);
            }
        });
    }

    public void setSyncParams(final SyncParams params) {
        addTask(new Task(28, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(params, "the SyncParams cannot be null");
                MediaPlayer2Impl.this._setSyncParams(params);
            }
        });
    }

    public void seekTo(long msec, int mode) {
        final int i = mode;
        final long j = msec;
        AnonymousClass20 r0 = new Task(14, true) {
            /* access modifiers changed from: package-private */
            public void process() {
                if (i < 0 || i > 3) {
                    throw new IllegalArgumentException("Illegal seek mode: " + i);
                }
                long posMs = j;
                if (posMs > 2147483647L) {
                    Log.w(MediaPlayer2Impl.TAG, "seekTo offset " + posMs + " is too large, cap to " + Integer.MAX_VALUE);
                    posMs = 2147483647L;
                } else if (posMs < -2147483648L) {
                    Log.w(MediaPlayer2Impl.TAG, "seekTo offset " + posMs + " is too small, cap to " + Integer.MIN_VALUE);
                    posMs = -2147483648L;
                }
                MediaPlayer2Impl.this._seekTo(posMs, i);
            }
        };
        addTask(r0);
    }

    public MediaTimestamp getTimestamp() {
        try {
            MediaTimestamp mediaTimestamp = new MediaTimestamp(getCurrentPosition() * 1000, System.nanoTime(), isPlaying() ? getPlaybackParams().getSpeed() : 0.0f);
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
        synchronized (this.mEventCbLock) {
            this.mEventCallbackRecords.clear();
        }
        synchronized (this.mDrmEventCbLock) {
            this.mDrmEventCallbackRecords.clear();
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

    public void setAudioSessionId(final int sessionId) {
        addTask(new Task(17, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this._setAudioSessionId(sessionId);
            }
        });
    }

    public void attachAuxEffect(final int effectId) {
        addTask(new Task(1, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this._attachAuxEffect(effectId);
            }
        });
    }

    public void setAuxEffectSendLevel(final float level) {
        addTask(new Task(18, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this._setAuxEffectSendLevel(level);
            }
        });
    }

    public List<MediaPlayer2.TrackInfo> getTrackInfo() {
        List<MediaPlayer2.TrackInfo> asList;
        TrackInfoImpl[] trackInfo = getInbandTrackInfoImpl();
        synchronized (this.mIndexTrackPairs) {
            TrackInfoImpl[] allTrackInfo = new TrackInfoImpl[this.mIndexTrackPairs.size()];
            for (int i = 0; i < allTrackInfo.length; i++) {
                Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(i);
                if (p.first != null) {
                    allTrackInfo[i] = trackInfo[((Integer) p.first).intValue()];
                } else {
                    SubtitleTrack track = (SubtitleTrack) p.second;
                    allTrackInfo[i] = new TrackInfoImpl(track.getTrackType(), track.getFormat());
                }
            }
            asList = Arrays.asList(allTrackInfo);
        }
        return asList;
    }

    private TrackInfoImpl[] getInbandTrackInfoImpl() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInt(1);
            invoke(request, reply);
            return (TrackInfoImpl[]) reply.createTypedArray(TrackInfoImpl.CREATOR);
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
                    SubtitleController unused = MediaPlayer2Impl.this.mSubtitleController = new SubtitleController(ActivityThread.currentApplication(), MediaPlayer2Impl.this.mTimeProvider, MediaPlayer2Impl.this);
                    MediaPlayer2Impl.this.mSubtitleController.setAnchor(new SubtitleController.Anchor() {
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
        setOnSubtitleDataListener(null);
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
                setOnSubtitleDataListener(this.mSubtitleDataListener);
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
                if (fIs == null || MediaPlayer2Impl.this.mSubtitleController == null) {
                    return 901;
                }
                SubtitleTrack track = MediaPlayer2Impl.this.mSubtitleController.addTrack(fFormat);
                if (track == null) {
                    return 901;
                }
                Scanner scanner = new Scanner(fIs, "UTF-8");
                String contents = scanner.useDelimiter("\\A").next();
                synchronized (MediaPlayer2Impl.this.mOpenSubtitleSources) {
                    MediaPlayer2Impl.this.mOpenSubtitleSources.remove(fIs);
                }
                scanner.close();
                synchronized (MediaPlayer2Impl.this.mIndexTrackPairs) {
                    MediaPlayer2Impl.this.mIndexTrackPairs.add(Pair.create(null, track));
                }
                Handler h = MediaPlayer2Impl.this.mTimeProvider.mEventHandler;
                h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(track, contents.getBytes())));
                return 803;
            }

            public void run() {
                int res = addTrack();
                if (MediaPlayer2Impl.this.mEventHandler != null) {
                    MediaPlayer2Impl.this.mEventHandler.sendMessage(MediaPlayer2Impl.this.mEventHandler.obtainMessage(200, res, 0, null));
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
        TrackInfoImpl[] tracks = getInbandTrackInfoImpl();
        synchronized (this.mIndexTrackPairs) {
            for (int i = 0; i < tracks.length; i++) {
                if (!this.mInbandTrackIndices.get(i)) {
                    this.mInbandTrackIndices.set(i);
                    if (tracks[i].getTrackType() == 4) {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), this.mSubtitleController.addTrack(tracks[i].getFormat())));
                    } else {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), null));
                    }
                }
            }
        }
    }

    public void addTimedTextSource(String path, String mimeType) throws IOException {
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
        if (r1 == null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003b, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0040, code lost:
        if (r1 == null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0043, code lost:
        return;
     */
    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException {
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
        } catch (Throwable th) {
            if (fd != null) {
                fd.close();
            }
            throw th;
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) {
        addTimedTextSource(fd, 0, DataSourceDesc.LONG_MAX, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mime) {
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
                AnonymousClass27 r1 = new Runnable() {
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
                            Handler h = MediaPlayer2Impl.this.mTimeProvider.mEventHandler;
                            h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(subtitleTrack, bos.toByteArray())));
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e) {
                                Log.e(MediaPlayer2Impl.TAG, e.getMessage(), e);
                            }
                            return 803;
                        } catch (Exception e2) {
                            Log.e(MediaPlayer2Impl.TAG, e2.getMessage(), e2);
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e3) {
                                Log.e(MediaPlayer2Impl.TAG, e3.getMessage(), e3);
                            }
                            return 900;
                        } catch (Throwable th) {
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e4) {
                                Log.e(MediaPlayer2Impl.TAG, e4.getMessage(), e4);
                            }
                            throw th;
                        }
                    }

                    public void run() {
                        int res = addTrack();
                        if (MediaPlayer2Impl.this.mEventHandler != null) {
                            MediaPlayer2Impl.this.mEventHandler.sendMessage(MediaPlayer2Impl.this.mEventHandler.obtainMessage(200, res, 0, null));
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

    public int getSelectedTrack(int trackType) {
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

    public void selectTrack(final int index) {
        addTask(new Task(15, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.selectOrDeselectTrack(index, true);
            }
        });
    }

    public void deselectTrack(final int index) {
        addTask(new Task(2, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.selectOrDeselectTrack(index, false);
            }
        });
    }

    /* access modifiers changed from: private */
    public void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
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
            request.writeInt(select ? 4 : 5);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mGuard != null) {
            this.mGuard.warnIfOpen();
        }
        close();
        native_finalize();
    }

    private void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        synchronized (this.mEventCbLock) {
            this.mEventCallbackRecords.clear();
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        if (this.mTimeProvider != null) {
            this.mTimeProvider.close();
            this.mTimeProvider = null;
        }
        this.mOnSubtitleDataListener = null;
        this.mOnDrmConfigHelper = null;
        synchronized (this.mDrmEventCbLock) {
            this.mDrmEventCallbackRecords.clear();
        }
        resetDrmState();
        _release();
    }

    public MediaTimeProvider getMediaTimeProvider() {
        if (this.mTimeProvider == null) {
            this.mTimeProvider = new TimeProvider(this);
        }
        return this.mTimeProvider;
    }

    private static void postEventFromNative(Object mediaplayer2_ref, final long srcId, int what, int arg1, int arg2, Object obj) {
        MediaPlayer2Impl mp = (MediaPlayer2Impl) ((WeakReference) mediaplayer2_ref).get();
        if (mp != null) {
            if (what == 1) {
                synchronized (mp.mDrmLock) {
                    mp.mDrmInfoResolved = true;
                }
            } else if (what != 200) {
                if (what == 210) {
                    Log.v(TAG, "postEventFromNative MEDIA_DRM_INFO");
                    if (obj instanceof Parcel) {
                        DrmInfoImpl drmInfo = new DrmInfoImpl((Parcel) obj);
                        synchronized (mp.mDrmLock) {
                            mp.mDrmInfoImpl = drmInfo;
                        }
                    } else {
                        Log.w(TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + obj);
                    }
                }
            } else if (arg1 == 2) {
                new Thread(new Runnable() {
                    public void run() {
                        MediaPlayer2Impl.this.play();
                    }
                }).start();
                Thread.yield();
            }
            if (mp.mEventHandler != null) {
                final Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
                mp.mEventHandler.post(new Runnable() {
                    public void run() {
                        MediaPlayer2Impl.this.mEventHandler.handleMessage(m, srcId);
                    }
                });
            }
        }
    }

    public void setMediaPlayer2EventCallback(Executor executor, MediaPlayer2.MediaPlayer2EventCallback eventCallback) {
        if (eventCallback == null) {
            throw new IllegalArgumentException("Illegal null MediaPlayer2EventCallback");
        } else if (executor != null) {
            synchronized (this.mEventCbLock) {
                this.mEventCallbackRecords.add(new Pair(executor, eventCallback));
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the MediaPlayer2EventCallback");
        }
    }

    public void clearMediaPlayer2EventCallback() {
        synchronized (this.mEventCbLock) {
            this.mEventCallbackRecords.clear();
        }
    }

    public void setOnSubtitleDataListener(MediaPlayer2.OnSubtitleDataListener listener) {
        this.mOnSubtitleDataListener = listener;
    }

    public void setOnDrmConfigHelper(MediaPlayer2.OnDrmConfigHelper listener) {
        synchronized (this.mDrmLock) {
            this.mOnDrmConfigHelper = listener;
        }
    }

    public void setDrmEventCallback(Executor executor, MediaPlayer2.DrmEventCallback eventCallback) {
        if (eventCallback == null) {
            throw new IllegalArgumentException("Illegal null MediaPlayer2EventCallback");
        } else if (executor != null) {
            synchronized (this.mDrmEventCbLock) {
                this.mDrmEventCallbackRecords.add(new Pair(executor, eventCallback));
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the MediaPlayer2EventCallback");
        }
    }

    public void clearDrmEventCallback() {
        synchronized (this.mDrmEventCbLock) {
            this.mDrmEventCallbackRecords.clear();
        }
    }

    public MediaPlayer2.DrmInfo getDrmInfo() {
        DrmInfoImpl drmInfo = null;
        synchronized (this.mDrmLock) {
            if (!this.mDrmInfoResolved) {
                if (this.mDrmInfoImpl == null) {
                    Log.v(TAG, "The Player has not been prepared yet");
                    throw new IllegalStateException("The Player has not been prepared yet");
                }
            }
            if (this.mDrmInfoImpl != null) {
                drmInfo = this.mDrmInfoImpl.makeCopy();
            }
        }
        return drmInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0062, code lost:
        if (0 != 0) goto L_0x0064;
     */
    /* JADX WARNING: Removed duplicated region for block: B:105:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00d0  */
    public void prepareDrm(UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, MediaPlayer2.ProvisioningNetworkErrorException, MediaPlayer2.ProvisioningServerErrorException {
        Log.v(TAG, "prepareDrm: uuid: " + uuid + " mOnDrmConfigHelper: " + this.mOnDrmConfigHelper);
        boolean allDoneWithoutProvisioning = false;
        synchronized (this.mDrmLock) {
            if (this.mDrmInfoImpl == null) {
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
            this.mOnDrmConfigHelper.onDrmConfig(this, this.mCurrentDSD);
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
                            throw new ProvisioningNetworkErrorExceptionImpl("prepareDrm: Provisioning was required but failed due to a network error.");
                        case 2:
                            Log.e(TAG, "prepareDrm: Provisioning was required but the request was denied by the server.");
                            throw new ProvisioningServerErrorExceptionImpl("prepareDrm: Provisioning was required but the request was denied by the server.");
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
                    if (!allDoneWithoutProvisioning) {
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
        if (!allDoneWithoutProvisioning) {
            synchronized (this.mDrmEventCbLock) {
                Iterator<Pair<Executor, MediaPlayer2.DrmEventCallback>> it = this.mDrmEventCallbackRecords.iterator();
                while (it.hasNext()) {
                    Pair<Executor, MediaPlayer2.DrmEventCallback> cb = it.next();
                    ((Executor) cb.first).execute(new Runnable(cb) {
                        private final /* synthetic */ Pair f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            ((MediaPlayer2.DrmEventCallback) this.f$1.second).onDrmPrepared(MediaPlayer2Impl.this, MediaPlayer2Impl.this.mCurrentDSD, 0);
                        }
                    });
                }
            }
        }
    }

    public void releaseDrm() throws MediaPlayer2.NoDrmSchemeException {
        addTask(new Task(12, false) {
            /* access modifiers changed from: package-private */
            public void process() throws MediaPlayer2.NoDrmSchemeException {
                synchronized (MediaPlayer2Impl.this.mDrmLock) {
                    Log.v(MediaPlayer2Impl.TAG, "releaseDrm:");
                    if (MediaPlayer2Impl.this.mActiveDrmScheme) {
                        try {
                            MediaPlayer2Impl.this._releaseDrm();
                            MediaPlayer2Impl.this.cleanDrmObj();
                            boolean unused = MediaPlayer2Impl.this.mActiveDrmScheme = false;
                        } catch (IllegalStateException e) {
                            Log.w(MediaPlayer2Impl.TAG, "releaseDrm: Exception ", e);
                            throw new IllegalStateException("releaseDrm: The player is not in a valid state.");
                        } catch (Exception e2) {
                            Log.e(MediaPlayer2Impl.TAG, "releaseDrm: Exception ", e2);
                        }
                    } else {
                        Log.e(MediaPlayer2Impl.TAG, "releaseDrm(): No active DRM scheme to release.");
                        throw new NoDrmSchemeExceptionImpl("releaseDrm: No active DRM scheme to release.");
                    }
                }
            }
        });
    }

    public MediaDrm.KeyRequest getDrmKeyRequest(byte[] keySetId, byte[] initData, String mimeType, int keyType, Map<String, String> optionalParameters) throws MediaPlayer2.NoDrmSchemeException {
        byte[] scope;
        HashMap<String, String> hmapOptionalParameters;
        MediaDrm.KeyRequest request;
        Log.v(TAG, "getDrmKeyRequest:  keySetId: " + keySetId + " initData:" + initData + " mimeType: " + mimeType + " keyType: " + keyType + " optionalParameters: " + optionalParameters);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keyType != 3) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "getDrmKeyRequest NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("getDrmKeyRequest: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "getDrmKeyRequest Exception " + e2);
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
                Log.v(TAG, "getDrmKeyRequest:   --> request: " + request);
            } else {
                Log.e(TAG, "getDrmKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeExceptionImpl("getDrmKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return request;
    }

    public byte[] provideDrmKeyResponse(byte[] keySetId, byte[] response) throws MediaPlayer2.NoDrmSchemeException, DeniedByServerException {
        byte[] scope;
        byte[] keySetResult;
        Log.v(TAG, "provideDrmKeyResponse: keySetId: " + keySetId + " response: " + response);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keySetId == null) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "provideDrmKeyResponse NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("provideDrmKeyResponse: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "provideDrmKeyResponse Exception " + e2);
                        throw e2;
                    }
                } else {
                    scope = keySetId;
                }
                keySetResult = this.mDrmObj.provideKeyResponse(scope, response);
                Log.v(TAG, "provideDrmKeyResponse: keySetId: " + keySetId + " response: " + response + " --> " + keySetResult);
            } else {
                Log.e(TAG, "getDrmKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeExceptionImpl("getDrmKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return keySetResult;
    }

    public void restoreDrmKeys(final byte[] keySetId) throws MediaPlayer2.NoDrmSchemeException {
        addTask(new Task(13, false) {
            /* access modifiers changed from: package-private */
            public void process() throws MediaPlayer2.NoDrmSchemeException {
                Log.v(MediaPlayer2Impl.TAG, "restoreDrmKeys: keySetId: " + keySetId);
                synchronized (MediaPlayer2Impl.this.mDrmLock) {
                    if (MediaPlayer2Impl.this.mActiveDrmScheme) {
                        try {
                            MediaPlayer2Impl.this.mDrmObj.restoreKeys(MediaPlayer2Impl.this.mDrmSessionId, keySetId);
                        } catch (Exception e) {
                            Log.w(MediaPlayer2Impl.TAG, "restoreKeys Exception " + e);
                            throw e;
                        }
                    } else {
                        Log.w(MediaPlayer2Impl.TAG, "restoreDrmKeys NoDrmSchemeException");
                        throw new NoDrmSchemeExceptionImpl("restoreDrmKeys: Has to set a DRM scheme first.");
                    }
                }
            }
        });
    }

    public String getDrmPropertyString(String propertyName) throws MediaPlayer2.NoDrmSchemeException {
        String value;
        Log.v(TAG, "getDrmPropertyString: propertyName: " + propertyName);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "getDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeExceptionImpl("getDrmPropertyString: Has to prepareDrm() first.");
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

    public void setDrmPropertyString(String propertyName, String value) throws MediaPlayer2.NoDrmSchemeException {
        Log.v(TAG, "setDrmPropertyString: propertyName: " + propertyName + " value: " + value);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "setDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeExceptionImpl("setDrmPropertyString: Has to prepareDrm() first.");
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

    private static boolean setAudioOutputDeviceById(AudioTrack track, int deviceId) {
        int i = 0;
        if (track == null) {
            return false;
        }
        if (deviceId == 0) {
            track.setPreferredDevice(null);
            return true;
        }
        AudioDeviceInfo[] outputDevices = AudioManager.getDevicesStatic(2);
        boolean success = false;
        int length = outputDevices.length;
        while (true) {
            if (i >= length) {
                break;
            }
            AudioDeviceInfo device = outputDevices[i];
            if (device.getId() == deviceId) {
                track.setPreferredDevice(device);
                success = true;
                break;
            }
            i++;
        }
        return success;
    }

    private int HandleProvisioninig(UUID uuid) {
        boolean hasCallback;
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
        synchronized (this.mDrmEventCbLock) {
            hasCallback = true ^ this.mDrmEventCallbackRecords.isEmpty();
        }
        if (hasCallback) {
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
            Log.v(TAG, "resetDrmState:  mDrmInfoImpl=" + this.mDrmInfoImpl + " mDrmProvisioningThread=" + this.mDrmProvisioningThread + " mPrepareDrmInProgress=" + this.mPrepareDrmInProgress + " mActiveDrmScheme=" + this.mActiveDrmScheme);
            this.mDrmInfoResolved = false;
            this.mDrmInfoImpl = null;
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

    /* access modifiers changed from: private */
    public boolean isVideoScalingModeSupported(int mode) {
        return mode == 1 || mode == 2;
    }
}
