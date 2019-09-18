package android.support.v4.media;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.DeniedByServerException;
import android.media.MediaDataSource;
import android.media.MediaDrm;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.media.PlaybackParams;
import android.media.ResourceBusyException;
import android.media.SubtitleData;
import android.media.SyncParams;
import android.media.TimedMetaData;
import android.media.UnsupportedSchemeException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.media.BaseMediaPlayer;
import android.support.v4.media.MediaPlayer2;
import android.support.v4.media.PlaybackParams2;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Preconditions;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@TargetApi(28)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public final class MediaPlayer2Impl extends MediaPlayer2 {
    private static final int SOURCE_STATE_ERROR = -1;
    private static final int SOURCE_STATE_INIT = 0;
    private static final int SOURCE_STATE_PREPARED = 2;
    private static final int SOURCE_STATE_PREPARING = 1;
    private static final String TAG = "MediaPlayer2Impl";
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sErrorEventMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sInfoEventMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sPrepareDrmStatusMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static ArrayMap<Integer, Integer> sStateMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public BaseMediaPlayerImpl mBaseMediaPlayerImpl;
    /* access modifiers changed from: private */
    @GuardedBy("mTaskLock")
    public Task mCurrentTask;
    private Pair<Executor, MediaPlayer2.DrmEventCallback> mDrmEventCallbackRecord;
    private final Handler mEndPositionHandler;
    private HandlerThread mHandlerThread = new HandlerThread("MediaPlayer2TaskThread");
    private final Object mLock = new Object();
    private Pair<Executor, MediaPlayer2.EventCallback> mMp2EventCallbackRecord;
    @GuardedBy("mTaskLock")
    private final ArrayDeque<Task> mPendingTasks = new ArrayDeque<>();
    /* access modifiers changed from: private */
    public MediaPlayerSourceQueue mPlayer;
    private ArrayMap<BaseMediaPlayer.PlayerEventCallback, Executor> mPlayerEventCallbackMap = new ArrayMap<>();
    private final Handler mTaskHandler;
    /* access modifiers changed from: private */
    public final Object mTaskLock = new Object();

    private class BaseMediaPlayerImpl extends BaseMediaPlayer {
        private BaseMediaPlayerImpl() {
        }

        public void play() {
            MediaPlayer2Impl.this.play();
        }

        public void prepare() {
            MediaPlayer2Impl.this.prepare();
        }

        public void pause() {
            MediaPlayer2Impl.this.pause();
        }

        public void reset() {
            MediaPlayer2Impl.this.reset();
        }

        public void skipToNext() {
            MediaPlayer2Impl.this.skipToNext();
        }

        public void seekTo(long pos) {
            MediaPlayer2Impl.this.seekTo(pos);
        }

        public long getCurrentPosition() {
            return MediaPlayer2Impl.this.getCurrentPosition();
        }

        public long getDuration() {
            return MediaPlayer2Impl.this.getDuration();
        }

        public long getBufferedPosition() {
            return MediaPlayer2Impl.this.getBufferedPosition();
        }

        public int getPlayerState() {
            return MediaPlayer2Impl.this.getPlayerState();
        }

        public int getBufferingState() {
            return MediaPlayer2Impl.this.getBufferingState();
        }

        public void setAudioAttributes(AudioAttributesCompat attributes) {
            MediaPlayer2Impl.this.setAudioAttributes(attributes);
        }

        public AudioAttributesCompat getAudioAttributes() {
            return MediaPlayer2Impl.this.getAudioAttributes();
        }

        public void setDataSource(DataSourceDesc dsd) {
            MediaPlayer2Impl.this.setDataSource(dsd);
        }

        public void setNextDataSource(DataSourceDesc dsd) {
            MediaPlayer2Impl.this.setNextDataSource(dsd);
        }

        public void setNextDataSources(List<DataSourceDesc> dsds) {
            MediaPlayer2Impl.this.setNextDataSources(dsds);
        }

        public DataSourceDesc getCurrentDataSource() {
            return MediaPlayer2Impl.this.getCurrentDataSource();
        }

        public void loopCurrent(boolean loop) {
            MediaPlayer2Impl.this.loopCurrent(loop);
        }

        public void setPlaybackSpeed(float speed) {
            MediaPlayer2Impl.this.setPlaybackParams(new PlaybackParams2.Builder(MediaPlayer2Impl.this.getPlaybackParams().getPlaybackParams()).setSpeed(speed).build());
        }

        public float getPlaybackSpeed() {
            return MediaPlayer2Impl.this.getPlaybackParams().getSpeed().floatValue();
        }

        public void setPlayerVolume(float volume) {
            MediaPlayer2Impl.this.setPlayerVolume(volume);
        }

        public float getPlayerVolume() {
            return MediaPlayer2Impl.this.getPlayerVolume();
        }

        public void registerPlayerEventCallback(Executor e, BaseMediaPlayer.PlayerEventCallback cb) {
            MediaPlayer2Impl.this.registerPlayerEventCallback(e, cb);
        }

        public void unregisterPlayerEventCallback(BaseMediaPlayer.PlayerEventCallback cb) {
            MediaPlayer2Impl.this.unregisterPlayerEventCallback(cb);
        }

        public void close() throws Exception {
            MediaPlayer2Impl.this.close();
        }
    }

    private static class DataSourceError {
        final DataSourceDesc mDSD;
        final int mExtra;
        final int mWhat;

        DataSourceError(DataSourceDesc dsd, int what, int extra) {
            this.mDSD = dsd;
            this.mWhat = what;
            this.mExtra = extra;
        }
    }

    private interface DrmEventNotifier {
        void notify(MediaPlayer2.DrmEventCallback drmEventCallback);
    }

    public static final class DrmInfoImpl extends MediaPlayer2.DrmInfo {
        private Map<UUID, byte[]> mMapPssh;
        private UUID[] mSupportedSchemes;

        public Map<UUID, byte[]> getPssh() {
            return this.mMapPssh;
        }

        public List<UUID> getSupportedSchemes() {
            return Arrays.asList(this.mSupportedSchemes);
        }

        private DrmInfoImpl(Map<UUID, byte[]> pssh, UUID[] supportedSchemes) {
            this.mMapPssh = pssh;
            this.mSupportedSchemes = supportedSchemes;
        }

        private DrmInfoImpl(Parcel parcel) {
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl(" + parcel + ") size " + parcel.dataSize());
            int psshsize = parcel.readInt();
            byte[] pssh = new byte[psshsize];
            parcel.readByteArray(pssh);
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() PSSH: " + arrToHex(pssh));
            this.mMapPssh = parsePSSH(pssh, psshsize);
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() PSSH: " + this.mMapPssh);
            int supportedDRMsCount = parcel.readInt();
            this.mSupportedSchemes = new UUID[supportedDRMsCount];
            for (int i = 0; i < supportedDRMsCount; i++) {
                byte[] uuid = new byte[16];
                parcel.readByteArray(uuid);
                this.mSupportedSchemes[i] = bytesToUUID(uuid);
                Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() supportedScheme[" + i + "]: " + this.mSupportedSchemes[i]);
            }
            Log.v(MediaPlayer2Impl.TAG, "DrmInfoImpl() Parcel psshsize: " + psshsize + " supportedDRMsCount: " + supportedDRMsCount);
        }

        private DrmInfoImpl makeCopy() {
            return new DrmInfoImpl(this.mMapPssh, this.mSupportedSchemes);
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

        /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r10v17, types: [byte] */
        /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r10v7, types: [byte] */
        private Map<UUID, byte[]> parsePSSH(byte[] pssh, int psshsize) {
            int i;
            int i2;
            byte[] bArr = pssh;
            Map<UUID, byte[]> result = new HashMap<>();
            char c = 0;
            int numentries = 0;
            int len = psshsize;
            int i3 = 0;
            while (len > 0) {
                if (len < 16) {
                    Object[] objArr = new Object[2];
                    objArr[c] = Integer.valueOf(len);
                    objArr[1] = Integer.valueOf(psshsize);
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse UUID: (%d < 16) pssh: %d", objArr));
                    return null;
                }
                UUID uuid = bytesToUUID(Arrays.copyOfRange(bArr, i3, i3 + 16));
                int i4 = i3 + 16;
                int len2 = len - 16;
                if (len2 < 4) {
                    Object[] objArr2 = new Object[2];
                    objArr2[c] = Integer.valueOf(len2);
                    objArr2[1] = Integer.valueOf(psshsize);
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse datalen: (%d < 4) pssh: %d", objArr2));
                    return null;
                }
                byte[] subset = Arrays.copyOfRange(bArr, i4, i4 + 4);
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    i = ((subset[3] & 255) << 24) | ((subset[2] & 255) << 16) | ((subset[1] & 255) << 8);
                    i2 = subset[0];
                } else {
                    i = ((subset[0] & 255) << 24) | ((subset[1] & 255) << 16) | ((subset[2] & 255) << 8);
                    i2 = subset[3];
                }
                int datalen = i | (i2 & 255);
                int i5 = i4 + 4;
                int len3 = len2 - 4;
                if (len3 < datalen) {
                    Log.w(MediaPlayer2Impl.TAG, String.format("parsePSSH: len is too short to parse data: (%d < %d) pssh: %d", new Object[]{Integer.valueOf(len3), Integer.valueOf(datalen), Integer.valueOf(psshsize)}));
                    return null;
                }
                byte[] data = Arrays.copyOfRange(bArr, i5, i5 + datalen);
                i3 = i5 + datalen;
                len = len3 - datalen;
                Log.v(MediaPlayer2Impl.TAG, String.format("parsePSSH[%d]: <%s, %s> pssh: %d", new Object[]{Integer.valueOf(numentries), uuid, arrToHex(data), Integer.valueOf(psshsize)}));
                numentries++;
                result.put(uuid, data);
                c = 0;
            }
            return result;
        }
    }

    private class MediaPlayerSource {
        final AtomicInteger mBufferedPercentage = new AtomicInteger(0);
        int mBufferingState = 0;
        volatile DataSourceDesc mDSD;
        int mMp2State = 1001;
        boolean mPlayPending;
        final MediaPlayer mPlayer = new MediaPlayer();
        int mPlayerState = 0;
        int mSourceState = 0;

        MediaPlayerSource(DataSourceDesc dsd) {
            this.mDSD = dsd;
            MediaPlayer2Impl.this.setUpListeners(this);
        }

        /* access modifiers changed from: package-private */
        public DataSourceDesc getDSD() {
            return this.mDSD;
        }
    }

    private class MediaPlayerSourceQueue {
        AudioAttributesCompat mAudioAttributes;
        Integer mAudioSessionId;
        Integer mAuxEffect;
        Float mAuxEffectSendLevel;
        PlaybackParams mPlaybackParams;
        List<MediaPlayerSource> mQueue = new ArrayList();
        Surface mSurface;
        SyncParams mSyncParams;
        Float mVolume = Float.valueOf(1.0f);

        MediaPlayerSourceQueue() {
            this.mQueue.add(new MediaPlayerSource(null));
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaPlayer getCurrentPlayer() {
            return this.mQueue.get(0).mPlayer;
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaPlayerSource getFirst() {
            return this.mQueue.get(0);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setFirst(DataSourceDesc dsd) throws IOException {
            if (this.mQueue.isEmpty()) {
                this.mQueue.add(0, new MediaPlayerSource(dsd));
            } else {
                this.mQueue.get(0).mDSD = dsd;
                MediaPlayer2Impl.this.setUpListeners(this.mQueue.get(0));
            }
            MediaPlayer2Impl.handleDataSource(this.mQueue.get(0));
        }

        /* access modifiers changed from: package-private */
        public synchronized DataSourceError setNext(DataSourceDesc dsd) {
            MediaPlayerSource src = new MediaPlayerSource(dsd);
            if (this.mQueue.isEmpty()) {
                this.mQueue.add(src);
                return prepareAt(0);
            }
            this.mQueue.add(1, src);
            return prepareAt(1);
        }

        /* access modifiers changed from: package-private */
        public synchronized DataSourceError setNextMultiple(List<DataSourceDesc> descs) {
            List<MediaPlayerSource> sources = new ArrayList<>();
            for (DataSourceDesc dsd : descs) {
                sources.add(new MediaPlayerSource(dsd));
            }
            if (this.mQueue.isEmpty()) {
                this.mQueue.addAll(sources);
                return prepareAt(0);
            }
            this.mQueue.addAll(1, sources);
            return prepareAt(1);
        }

        /* access modifiers changed from: package-private */
        public synchronized void play() {
            MediaPlayerSource src = this.mQueue.get(0);
            if (src.mSourceState == 2) {
                src.mPlayer.start();
                setMp2State(src.mPlayer, 1004);
            } else {
                throw new IllegalStateException();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void prepare() {
            getCurrentPlayer().prepareAsync();
        }

        /* access modifiers changed from: package-private */
        public synchronized void release() {
            getCurrentPlayer().release();
        }

        /* access modifiers changed from: package-private */
        public synchronized void prepareAsync() {
            MediaPlayer mp = getCurrentPlayer();
            mp.prepareAsync();
            setBufferingState(mp, 2);
        }

        /* access modifiers changed from: package-private */
        public synchronized void pause() {
            MediaPlayer mp = getCurrentPlayer();
            mp.pause();
            setMp2State(mp, 1003);
        }

        /* access modifiers changed from: package-private */
        public synchronized long getCurrentPosition() {
            return (long) getCurrentPlayer().getCurrentPosition();
        }

        /* access modifiers changed from: package-private */
        public synchronized long getDuration() {
            return (long) getCurrentPlayer().getDuration();
        }

        /* access modifiers changed from: package-private */
        public synchronized long getBufferedPosition() {
            MediaPlayerSource src;
            src = this.mQueue.get(0);
            return (((long) src.mPlayer.getDuration()) * ((long) src.mBufferedPercentage.get())) / 100;
        }

        /* access modifiers changed from: package-private */
        public synchronized void setAudioAttributes(AudioAttributesCompat attributes) {
            AudioAttributes attr;
            this.mAudioAttributes = attributes;
            if (this.mAudioAttributes == null) {
                attr = null;
            } else {
                attr = (AudioAttributes) this.mAudioAttributes.unwrap();
            }
            getCurrentPlayer().setAudioAttributes(attr);
        }

        /* access modifiers changed from: package-private */
        public synchronized AudioAttributesCompat getAudioAttributes() {
            return this.mAudioAttributes;
        }

        /* access modifiers changed from: package-private */
        public synchronized DataSourceError onPrepared(MediaPlayer mp) {
            for (int i = 0; i < this.mQueue.size(); i++) {
                MediaPlayerSource src = this.mQueue.get(i);
                if (mp == src.mPlayer) {
                    if (i == 0) {
                        if (src.mPlayPending) {
                            src.mPlayPending = false;
                            src.mPlayer.start();
                            setMp2State(src.mPlayer, 1004);
                        } else {
                            setMp2State(src.mPlayer, 1002);
                        }
                    }
                    src.mSourceState = 2;
                    setBufferingState(src.mPlayer, 1);
                    return prepareAt(i + 1);
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public synchronized DataSourceError onCompletion(MediaPlayer mp) {
            if (!this.mQueue.isEmpty() && mp == getCurrentPlayer()) {
                if (this.mQueue.size() == 1) {
                    setMp2State(mp, 1003);
                    final DataSourceDesc dsd = this.mQueue.get(0).getDSD();
                    MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                        public void notify(MediaPlayer2.EventCallback callback) {
                            callback.onInfo(MediaPlayer2Impl.this, dsd, 6, 0);
                        }
                    });
                    return null;
                }
                moveToNext();
            }
            return playCurrent();
        }

        /* access modifiers changed from: package-private */
        public synchronized void moveToNext() {
            MediaPlayerSource src1 = this.mQueue.remove(0);
            src1.mPlayer.release();
            if (!this.mQueue.isEmpty()) {
                final MediaPlayerSource src2 = this.mQueue.get(0);
                if (src1.mPlayerState != src2.mPlayerState) {
                    MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                        public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                            cb.onPlayerStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, src2.mPlayerState);
                        }
                    });
                }
                MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                    public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                        cb.onCurrentDataSourceChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, src2.mDSD);
                    }
                });
            } else {
                throw new IllegalStateException("player/source queue emptied");
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized DataSourceError playCurrent() {
            DataSourceError err;
            err = null;
            final MediaPlayerSource src = this.mQueue.get(0);
            if (this.mSurface != null) {
                src.mPlayer.setSurface(this.mSurface);
            }
            if (this.mVolume != null) {
                src.mPlayer.setVolume(this.mVolume.floatValue(), this.mVolume.floatValue());
            }
            if (this.mAudioAttributes != null) {
                src.mPlayer.setAudioAttributes((AudioAttributes) this.mAudioAttributes.unwrap());
            }
            if (this.mAuxEffect != null) {
                src.mPlayer.attachAuxEffect(this.mAuxEffect.intValue());
            }
            if (this.mAuxEffectSendLevel != null) {
                src.mPlayer.setAuxEffectSendLevel(this.mAuxEffectSendLevel.floatValue());
            }
            if (this.mSyncParams != null) {
                src.mPlayer.setSyncParams(this.mSyncParams);
            }
            if (this.mPlaybackParams != null) {
                src.mPlayer.setPlaybackParams(this.mPlaybackParams);
            }
            if (src.mSourceState == 2) {
                src.mPlayer.start();
                setMp2State(src.mPlayer, 1004);
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback callback) {
                        callback.onInfo(MediaPlayer2Impl.this, src.getDSD(), 2, 0);
                    }
                });
            } else {
                if (src.mSourceState == 0) {
                    err = prepareAt(0);
                }
                src.mPlayPending = true;
            }
            return err;
        }

        /* access modifiers changed from: package-private */
        public synchronized void onError(MediaPlayer mp) {
            setMp2State(mp, 1005);
            setBufferingState(mp, 0);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0059, code lost:
            return null;
         */
        public synchronized DataSourceError prepareAt(int n) {
            if (n < this.mQueue.size() && this.mQueue.get(n).mSourceState == 0) {
                if (n == 0 || getPlayerState() != 0) {
                    MediaPlayerSource src = this.mQueue.get(n);
                    try {
                        if (this.mAudioSessionId != null) {
                            src.mPlayer.setAudioSessionId(this.mAudioSessionId.intValue());
                        }
                        src.mSourceState = 1;
                        MediaPlayer2Impl.handleDataSource(src);
                        src.mPlayer.prepareAsync();
                        return null;
                    } catch (Exception e) {
                        DataSourceDesc dsd = src.getDSD();
                        setMp2State(src.mPlayer, 1005);
                        return new DataSourceError(dsd, 1, MediaPlayer2.MEDIA_ERROR_UNSUPPORTED);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void skipToNext() {
            if (this.mQueue.size() > 1) {
                MediaPlayerSource src = this.mQueue.get(0);
                moveToNext();
                if (src.mPlayerState == 2 || src.mPlayPending) {
                    playCurrent();
                }
            } else {
                throw new IllegalStateException("No next source available");
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void setLooping(boolean loop) {
            getCurrentPlayer().setLooping(loop);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setPlaybackParams(PlaybackParams playbackParams) {
            getCurrentPlayer().setPlaybackParams(playbackParams);
            this.mPlaybackParams = playbackParams;
        }

        /* access modifiers changed from: package-private */
        public synchronized float getVolume() {
            return this.mVolume.floatValue();
        }

        /* access modifiers changed from: package-private */
        public synchronized void setVolume(float volume) {
            this.mVolume = Float.valueOf(volume);
            getCurrentPlayer().setVolume(volume, volume);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setSurface(Surface surface) {
            this.mSurface = surface;
            getCurrentPlayer().setSurface(surface);
        }

        /* access modifiers changed from: package-private */
        public synchronized int getVideoWidth() {
            return getCurrentPlayer().getVideoWidth();
        }

        /* access modifiers changed from: package-private */
        public synchronized int getVideoHeight() {
            return getCurrentPlayer().getVideoHeight();
        }

        /* access modifiers changed from: package-private */
        public synchronized PersistableBundle getMetrics() {
            return getCurrentPlayer().getMetrics();
        }

        /* access modifiers changed from: package-private */
        public synchronized PlaybackParams getPlaybackParams() {
            return getCurrentPlayer().getPlaybackParams();
        }

        /* access modifiers changed from: package-private */
        public synchronized void setSyncParams(SyncParams params) {
            getCurrentPlayer().setSyncParams(params);
            this.mSyncParams = params;
        }

        /* access modifiers changed from: package-private */
        public synchronized SyncParams getSyncParams() {
            return getCurrentPlayer().getSyncParams();
        }

        /* access modifiers changed from: package-private */
        public synchronized void seekTo(long msec, int mode) {
            getCurrentPlayer().seekTo(msec, mode);
        }

        /* access modifiers changed from: package-private */
        public synchronized void reset() {
            MediaPlayerSource src = this.mQueue.get(0);
            src.mPlayer.reset();
            src.mBufferedPercentage.set(0);
            this.mVolume = Float.valueOf(1.0f);
            this.mSurface = null;
            this.mAuxEffect = null;
            this.mAuxEffectSendLevel = null;
            this.mAudioAttributes = null;
            this.mAudioSessionId = null;
            this.mSyncParams = null;
            this.mPlaybackParams = null;
            setMp2State(src.mPlayer, 1001);
            setBufferingState(src.mPlayer, 0);
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaTimestamp2 getTimestamp() {
            MediaTimestamp t;
            t = getCurrentPlayer().getTimestamp();
            return t == null ? null : new MediaTimestamp2(t);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setAudioSessionId(int sessionId) {
            getCurrentPlayer().setAudioSessionId(sessionId);
        }

        /* access modifiers changed from: package-private */
        public synchronized int getAudioSessionId() {
            return getCurrentPlayer().getAudioSessionId();
        }

        /* access modifiers changed from: package-private */
        public synchronized void attachAuxEffect(int effectId) {
            getCurrentPlayer().attachAuxEffect(effectId);
            this.mAuxEffect = Integer.valueOf(effectId);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setAuxEffectSendLevel(float level) {
            getCurrentPlayer().setAuxEffectSendLevel(level);
            this.mAuxEffectSendLevel = Float.valueOf(level);
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaPlayer.TrackInfo[] getTrackInfo() {
            return getCurrentPlayer().getTrackInfo();
        }

        /* access modifiers changed from: package-private */
        public synchronized int getSelectedTrack(int trackType) {
            return getCurrentPlayer().getSelectedTrack(trackType);
        }

        /* access modifiers changed from: package-private */
        public synchronized void selectTrack(int index) {
            getCurrentPlayer().selectTrack(index);
        }

        /* access modifiers changed from: package-private */
        public synchronized void deselectTrack(int index) {
            getCurrentPlayer().deselectTrack(index);
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaPlayer.DrmInfo getDrmInfo() {
            return getCurrentPlayer().getDrmInfo();
        }

        /* access modifiers changed from: package-private */
        public synchronized void prepareDrm(UUID uuid) throws ResourceBusyException, MediaPlayer.ProvisioningServerErrorException, MediaPlayer.ProvisioningNetworkErrorException, UnsupportedSchemeException {
            getCurrentPlayer().prepareDrm(uuid);
        }

        /* access modifiers changed from: package-private */
        public synchronized void releaseDrm() throws MediaPlayer.NoDrmSchemeException {
            getCurrentPlayer().stop();
            getCurrentPlayer().releaseDrm();
        }

        /* access modifiers changed from: package-private */
        public synchronized byte[] provideKeyResponse(byte[] keySetId, byte[] response) throws DeniedByServerException, MediaPlayer.NoDrmSchemeException {
            return getCurrentPlayer().provideKeyResponse(keySetId, response);
        }

        /* access modifiers changed from: package-private */
        public synchronized void restoreKeys(byte[] keySetId) throws MediaPlayer.NoDrmSchemeException {
            getCurrentPlayer().restoreKeys(keySetId);
        }

        /* access modifiers changed from: package-private */
        public synchronized String getDrmPropertyString(String propertyName) throws MediaPlayer.NoDrmSchemeException {
            return getCurrentPlayer().getDrmPropertyString(propertyName);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setDrmPropertyString(String propertyName, String value) throws MediaPlayer.NoDrmSchemeException {
            getCurrentPlayer().setDrmPropertyString(propertyName, value);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setOnDrmConfigHelper(MediaPlayer.OnDrmConfigHelper onDrmConfigHelper) {
            getCurrentPlayer().setOnDrmConfigHelper(onDrmConfigHelper);
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaDrm.KeyRequest getKeyRequest(byte[] keySetId, byte[] initData, String mimeType, int keyType, Map<String, String> optionalParameters) throws MediaPlayer.NoDrmSchemeException {
            return getCurrentPlayer().getKeyRequest(keySetId, initData, mimeType, keyType, optionalParameters);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setMp2State(MediaPlayer mp, int mp2State) {
            for (MediaPlayerSource src : this.mQueue) {
                if (src.mPlayer == mp) {
                    if (src.mMp2State != mp2State) {
                        src.mMp2State = mp2State;
                        final int playerState = ((Integer) MediaPlayer2Impl.sStateMap.get(Integer.valueOf(mp2State))).intValue();
                        if (src.mPlayerState != playerState) {
                            src.mPlayerState = playerState;
                            MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                                public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                                    cb.onPlayerStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, playerState);
                                }
                            });
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void setBufferingState(MediaPlayer mp, final int state) {
            for (final MediaPlayerSource src : this.mQueue) {
                if (src.mPlayer == mp) {
                    if (src.mBufferingState != state) {
                        src.mBufferingState = state;
                        MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                            public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                                cb.onBufferingStateChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, src.getDSD(), state);
                            }
                        });
                        return;
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized int getMediaPlayer2State() {
            return this.mQueue.get(0).mMp2State;
        }

        /* access modifiers changed from: package-private */
        public synchronized int getBufferingState() {
            return this.mQueue.get(0).mBufferingState;
        }

        /* access modifiers changed from: package-private */
        public synchronized int getPlayerState() {
            return this.mQueue.get(0).mPlayerState;
        }

        /* access modifiers changed from: package-private */
        public synchronized MediaPlayerSource getSourceForPlayer(MediaPlayer mp) {
            for (MediaPlayerSource src : this.mQueue) {
                if (src.mPlayer == mp) {
                    return src;
                }
            }
            return null;
        }
    }

    private interface Mp2EventNotifier {
        void notify(MediaPlayer2.EventCallback eventCallback);
    }

    public static final class NoDrmSchemeExceptionImpl extends MediaPlayer2.NoDrmSchemeException {
        public NoDrmSchemeExceptionImpl(String detailMessage) {
            super(detailMessage);
        }
    }

    private interface PlayerEventNotifier {
        void notify(BaseMediaPlayer.PlayerEventCallback playerEventCallback);
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

    private abstract class Task implements Runnable {
        /* access modifiers changed from: private */
        public DataSourceDesc mDSD;
        /* access modifiers changed from: private */
        public final int mMediaCallType;
        /* access modifiers changed from: private */
        public final boolean mNeedToWaitForEventToComplete;

        /* access modifiers changed from: package-private */
        public abstract void process() throws IOException, MediaPlayer2.NoDrmSchemeException;

        Task(int mediaCallType, boolean needToWaitForEventToComplete) {
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
            } catch (Exception e5) {
                status = Integer.MIN_VALUE;
            }
            this.mDSD = MediaPlayer2Impl.this.getCurrentDataSource();
            if (!this.mNeedToWaitForEventToComplete || status != 0) {
                sendCompleteNotification(status);
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    Task unused = MediaPlayer2Impl.this.mCurrentTask = null;
                    MediaPlayer2Impl.this.processPendingTask_l();
                }
            }
        }

        /* access modifiers changed from: private */
        public void sendCompleteNotification(final int status) {
            if (this.mMediaCallType != 1003) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onCallCompleted(MediaPlayer2Impl.this, Task.this.mDSD, Task.this.mMediaCallType, status);
                    }
                });
            }
        }
    }

    public static final class TrackInfoImpl extends MediaPlayer2.TrackInfo {
        final MediaFormat mFormat;
        final int mTrackType;

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String language = this.mFormat.getString("language");
            return language == null ? "und" : language;
        }

        public MediaFormat getFormat() {
            if (this.mTrackType == 3 || this.mTrackType == 4) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfoImpl(int type, MediaFormat format) {
            this.mTrackType = type;
            this.mFormat = format;
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

    static {
        sInfoEventMap.put(1, 1);
        sInfoEventMap.put(2, 2);
        sInfoEventMap.put(3, 3);
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_TRACK_LAGGING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_TRACK_LAGGING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_START), Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_START));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_END), Integer.valueOf(MediaPlayer2.MEDIA_INFO_BUFFERING_END));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_NOT_SEEKABLE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_NOT_SEEKABLE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_METADATA_UPDATE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_METADATA_UPDATE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_AUDIO_NOT_PLAYING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_AUDIO_NOT_PLAYING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_NOT_PLAYING), Integer.valueOf(MediaPlayer2.MEDIA_INFO_VIDEO_NOT_PLAYING));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_UNSUPPORTED_SUBTITLE), Integer.valueOf(MediaPlayer2.MEDIA_INFO_UNSUPPORTED_SUBTITLE));
        sInfoEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_INFO_SUBTITLE_TIMED_OUT), Integer.valueOf(MediaPlayer2.MEDIA_INFO_SUBTITLE_TIMED_OUT));
        sErrorEventMap.put(1, 1);
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_IO), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_IO));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_MALFORMED), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_MALFORMED));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_UNSUPPORTED), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_UNSUPPORTED));
        sErrorEventMap.put(Integer.valueOf(MediaPlayer2.MEDIA_ERROR_TIMED_OUT), Integer.valueOf(MediaPlayer2.MEDIA_ERROR_TIMED_OUT));
        sPrepareDrmStatusMap.put(0, 0);
        sPrepareDrmStatusMap.put(1, 1);
        sPrepareDrmStatusMap.put(2, 2);
        sPrepareDrmStatusMap.put(2, 2);
        sStateMap.put(1001, 0);
        sStateMap.put(1002, 1);
        sStateMap.put(1003, 1);
        sStateMap.put(1004, 2);
        sStateMap.put(1005, 3);
    }

    /* access modifiers changed from: private */
    public void handleDataSourceError(final DataSourceError err) {
        if (err != null) {
            notifyMediaPlayer2Event(new Mp2EventNotifier() {
                public void notify(MediaPlayer2.EventCallback callback) {
                    callback.onError(MediaPlayer2Impl.this, err.mDSD, err.mWhat, err.mExtra);
                }
            });
        }
    }

    public MediaPlayer2Impl() {
        this.mHandlerThread.start();
        Looper looper = this.mHandlerThread.getLooper();
        this.mEndPositionHandler = new Handler(looper);
        this.mTaskHandler = new Handler(looper);
        this.mPlayer = new MediaPlayerSourceQueue();
    }

    public BaseMediaPlayer getBaseMediaPlayer() {
        BaseMediaPlayerImpl baseMediaPlayerImpl;
        synchronized (this.mLock) {
            if (this.mBaseMediaPlayerImpl == null) {
                this.mBaseMediaPlayerImpl = new BaseMediaPlayerImpl();
            }
            baseMediaPlayerImpl = this.mBaseMediaPlayerImpl;
        }
        return baseMediaPlayerImpl;
    }

    public void close() {
        this.mPlayer.release();
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
    }

    public void play() {
        addTask(new Task(5, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.play();
            }
        });
    }

    public void prepare() {
        addTask(new Task(6, true) {
            /* access modifiers changed from: package-private */
            public void process() throws IOException {
                MediaPlayer2Impl.this.mPlayer.prepareAsync();
            }
        });
    }

    public void pause() {
        addTask(new Task(4, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.pause();
            }
        });
    }

    public void skipToNext() {
        addTask(new Task(29, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.skipToNext();
            }
        });
    }

    public long getCurrentPosition() {
        return this.mPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return this.mPlayer.getDuration();
    }

    public long getBufferedPosition() {
        return this.mPlayer.getBufferedPosition();
    }

    public int getState() {
        return this.mPlayer.getMediaPlayer2State();
    }

    /* access modifiers changed from: private */
    public int getPlayerState() {
        return this.mPlayer.getPlayerState();
    }

    /* access modifiers changed from: private */
    public int getBufferingState() {
        return this.mPlayer.getBufferingState();
    }

    public void setAudioAttributes(@NonNull final AudioAttributesCompat attributes) {
        addTask(new Task(16, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAudioAttributes(attributes);
            }
        });
    }

    @NonNull
    public AudioAttributesCompat getAudioAttributes() {
        return this.mPlayer.getAudioAttributes();
    }

    public void setDataSource(@NonNull final DataSourceDesc dsd) {
        addTask(new Task(19, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
                try {
                    MediaPlayer2Impl.this.mPlayer.setFirst(dsd);
                } catch (IOException e) {
                    Log.e(MediaPlayer2Impl.TAG, "process: setDataSource", e);
                }
            }
        });
    }

    public void setNextDataSource(@NonNull final DataSourceDesc dsd) {
        addTask(new Task(22, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
                MediaPlayer2Impl.this.handleDataSourceError(MediaPlayer2Impl.this.mPlayer.setNext(dsd));
            }
        });
    }

    public void setNextDataSources(@NonNull final List<DataSourceDesc> dsds) {
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
                MediaPlayer2Impl.this.handleDataSourceError(MediaPlayer2Impl.this.mPlayer.setNextMultiple(dsds));
            }
        });
    }

    @NonNull
    public DataSourceDesc getCurrentDataSource() {
        return this.mPlayer.getFirst().getDSD();
    }

    public void loopCurrent(final boolean loop) {
        addTask(new Task(3, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setLooping(loop);
            }
        });
    }

    public void setPlayerVolume(final float volume) {
        addTask(new Task(26, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setVolume(volume);
            }
        });
    }

    public float getPlayerVolume() {
        return this.mPlayer.getVolume();
    }

    public float getMaxPlayerVolume() {
        return 1.0f;
    }

    /* access modifiers changed from: private */
    public void registerPlayerEventCallback(@NonNull Executor e, @NonNull BaseMediaPlayer.PlayerEventCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null PlayerEventCallback");
        } else if (e != null) {
            synchronized (this.mLock) {
                this.mPlayerEventCallbackMap.put(cb, e);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the PlayerEventCallback");
        }
    }

    /* access modifiers changed from: private */
    public void unregisterPlayerEventCallback(@NonNull BaseMediaPlayer.PlayerEventCallback cb) {
        if (cb != null) {
            synchronized (this.mLock) {
                this.mPlayerEventCallbackMap.remove(cb);
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null PlayerEventCallback");
    }

    public void notifyWhenCommandLabelReached(final Object label) {
        addTask(new Task(1003, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onCommandLabelReached(MediaPlayer2Impl.this, label);
                    }
                });
            }
        });
    }

    public void setSurface(final Surface surface) {
        addTask(new Task(27, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setSurface(surface);
            }
        });
    }

    public void clearPendingCommands() {
        synchronized (this.mTaskLock) {
            this.mPendingTasks.clear();
        }
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
            Task task = this.mPendingTasks.removeFirst();
            this.mCurrentTask = task;
            this.mTaskHandler.post(task);
        }
    }

    /* access modifiers changed from: private */
    public static void handleDataSource(MediaPlayerSource src) throws IOException {
        final DataSourceDesc dsd = src.getDSD();
        Preconditions.checkNotNull(dsd, "the DataSourceDesc cannot be null");
        MediaPlayer player = src.mPlayer;
        switch (dsd.getType()) {
            case 1:
                player.setDataSource(new MediaDataSource() {
                    Media2DataSource mDataSource = dsd.getMedia2DataSource();

                    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                        return this.mDataSource.readAt(position, buffer, offset, size);
                    }

                    public long getSize() throws IOException {
                        return this.mDataSource.getSize();
                    }

                    public void close() throws IOException {
                        this.mDataSource.close();
                    }
                });
                return;
            case 2:
                player.setDataSource(dsd.getFileDescriptor(), dsd.getFileDescriptorOffset(), dsd.getFileDescriptorLength());
                return;
            case 3:
                player.setDataSource(dsd.getUriContext(), dsd.getUri(), dsd.getUriHeaders(), dsd.getUriCookies());
                return;
            default:
                return;
        }
    }

    public int getVideoWidth() {
        return this.mPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return this.mPlayer.getVideoHeight();
    }

    public PersistableBundle getMetrics() {
        return this.mPlayer.getMetrics();
    }

    public void setPlaybackParams(@NonNull final PlaybackParams2 params) {
        addTask(new Task(24, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.setPlaybackParamsInternal(params.getPlaybackParams());
            }
        });
    }

    @NonNull
    public PlaybackParams2 getPlaybackParams() {
        return new PlaybackParams2.Builder(this.mPlayer.getPlaybackParams()).build();
    }

    public void seekTo(long msec, int mode) {
        final long j = msec;
        final int i = mode;
        AnonymousClass16 r0 = new Task(14, true) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.seekTo(j, i);
            }
        };
        addTask(r0);
    }

    @Nullable
    public MediaTimestamp2 getTimestamp() {
        return this.mPlayer.getTimestamp();
    }

    public void reset() {
        this.mPlayer.reset();
        synchronized (this.mLock) {
            this.mMp2EventCallbackRecord = null;
            this.mPlayerEventCallbackMap.clear();
            this.mDrmEventCallbackRecord = null;
        }
    }

    public void setAudioSessionId(final int sessionId) {
        addTask(new Task(17, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAudioSessionId(sessionId);
            }
        });
    }

    public int getAudioSessionId() {
        return this.mPlayer.getAudioSessionId();
    }

    public void attachAuxEffect(final int effectId) {
        addTask(new Task(1, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.attachAuxEffect(effectId);
            }
        });
    }

    public void setAuxEffectSendLevel(final float level) {
        addTask(new Task(18, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.setAuxEffectSendLevel(level);
            }
        });
    }

    public List<MediaPlayer2.TrackInfo> getTrackInfo() {
        MediaPlayer.TrackInfo[] list = this.mPlayer.getTrackInfo();
        List<MediaPlayer2.TrackInfo> trackList = new ArrayList<>();
        for (MediaPlayer.TrackInfo info : list) {
            trackList.add(new TrackInfoImpl(info.getTrackType(), info.getFormat()));
        }
        return trackList;
    }

    public int getSelectedTrack(int trackType) {
        return this.mPlayer.getSelectedTrack(trackType);
    }

    public void selectTrack(final int index) {
        addTask(new Task(15, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.selectTrack(index);
            }
        });
    }

    public void deselectTrack(final int index) {
        addTask(new Task(2, false) {
            /* access modifiers changed from: package-private */
            public void process() {
                MediaPlayer2Impl.this.mPlayer.deselectTrack(index);
            }
        });
    }

    public void setEventCallback(@NonNull Executor executor, @NonNull MediaPlayer2.EventCallback eventCallback) {
        if (eventCallback == null) {
            throw new IllegalArgumentException("Illegal null EventCallback");
        } else if (executor != null) {
            synchronized (this.mLock) {
                this.mMp2EventCallbackRecord = new Pair<>(executor, eventCallback);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the EventCallback");
        }
    }

    public void clearEventCallback() {
        synchronized (this.mLock) {
            this.mMp2EventCallbackRecord = null;
        }
    }

    public void setOnDrmConfigHelper(final MediaPlayer2.OnDrmConfigHelper listener) {
        this.mPlayer.setOnDrmConfigHelper(new MediaPlayer.OnDrmConfigHelper() {
            public void onDrmConfig(MediaPlayer mp) {
                MediaPlayerSource src = MediaPlayer2Impl.this.mPlayer.getSourceForPlayer(mp);
                listener.onDrmConfig(MediaPlayer2Impl.this, src == null ? null : src.getDSD());
            }
        });
    }

    public void setDrmEventCallback(@NonNull Executor executor, @NonNull MediaPlayer2.DrmEventCallback eventCallback) {
        if (eventCallback == null) {
            throw new IllegalArgumentException("Illegal null EventCallback");
        } else if (executor != null) {
            synchronized (this.mLock) {
                this.mDrmEventCallbackRecord = new Pair<>(executor, eventCallback);
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the EventCallback");
        }
    }

    public void clearDrmEventCallback() {
        synchronized (this.mLock) {
            this.mDrmEventCallbackRecord = null;
        }
    }

    public MediaPlayer2.DrmInfo getDrmInfo() {
        MediaPlayer.DrmInfo info = this.mPlayer.getDrmInfo();
        if (info == null) {
            return null;
        }
        return new DrmInfoImpl(info.getPssh(), info.getSupportedSchemes());
    }

    public void prepareDrm(@NonNull UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, MediaPlayer2.ProvisioningNetworkErrorException, MediaPlayer2.ProvisioningServerErrorException {
        try {
            this.mPlayer.prepareDrm(uuid);
        } catch (MediaPlayer.ProvisioningNetworkErrorException e) {
            throw new MediaPlayer2.ProvisioningNetworkErrorException(e.getMessage());
        } catch (MediaPlayer.ProvisioningServerErrorException e2) {
            throw new MediaPlayer2.ProvisioningServerErrorException(e2.getMessage());
        }
    }

    public void releaseDrm() throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.releaseDrm();
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    @NonNull
    public MediaDrm.KeyRequest getDrmKeyRequest(@Nullable byte[] keySetId, @Nullable byte[] initData, @Nullable String mimeType, int keyType, @Nullable Map<String, String> optionalParameters) throws MediaPlayer2.NoDrmSchemeException {
        try {
            return this.mPlayer.getKeyRequest(keySetId, initData, mimeType, keyType, optionalParameters);
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public byte[] provideDrmKeyResponse(@Nullable byte[] keySetId, @NonNull byte[] response) throws MediaPlayer2.NoDrmSchemeException, DeniedByServerException {
        try {
            return this.mPlayer.provideKeyResponse(keySetId, response);
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public void restoreDrmKeys(@NonNull byte[] keySetId) throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.restoreKeys(keySetId);
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    @NonNull
    public String getDrmPropertyString(@NonNull String propertyName) throws MediaPlayer2.NoDrmSchemeException {
        try {
            return this.mPlayer.getDrmPropertyString(propertyName);
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    public void setDrmPropertyString(@NonNull String propertyName, @NonNull String value) throws MediaPlayer2.NoDrmSchemeException {
        try {
            this.mPlayer.setDrmPropertyString(propertyName, value);
        } catch (MediaPlayer.NoDrmSchemeException e) {
            throw new MediaPlayer2.NoDrmSchemeException(e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void setPlaybackParamsInternal(final PlaybackParams params) {
        PlaybackParams current = this.mPlayer.getPlaybackParams();
        this.mPlayer.setPlaybackParams(params);
        if (current.getSpeed() != params.getSpeed()) {
            notifyPlayerEvent(new PlayerEventNotifier() {
                public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                    cb.onPlaybackSpeedChanged(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, params.getSpeed());
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0006, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0008, code lost:
        ((java.util.concurrent.Executor) r1.first).execute(new android.support.v4.media.MediaPlayer2Impl.AnonymousClass24(r3));
     */
    public void notifyMediaPlayer2Event(final Mp2EventNotifier notifier) {
        synchronized (this.mLock) {
            try {
                final Pair<Executor, MediaPlayer2.EventCallback> record = this.mMp2EventCallbackRecord;
                try {
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyPlayerEvent(final PlayerEventNotifier notifier) {
        ArrayMap arrayMap;
        synchronized (this.mLock) {
            arrayMap = new ArrayMap((SimpleArrayMap) this.mPlayerEventCallbackMap);
        }
        int callbackCount = arrayMap.size();
        for (int i = 0; i < callbackCount; i++) {
            final BaseMediaPlayer.PlayerEventCallback cb = (BaseMediaPlayer.PlayerEventCallback) arrayMap.keyAt(i);
            ((Executor) arrayMap.valueAt(i)).execute(new Runnable() {
                public void run() {
                    notifier.notify(cb);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0006, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0008, code lost:
        ((java.util.concurrent.Executor) r1.first).execute(new android.support.v4.media.MediaPlayer2Impl.AnonymousClass26(r3));
     */
    public void notifyDrmEvent(final DrmEventNotifier notifier) {
        synchronized (this.mLock) {
            try {
                final Pair<Executor, MediaPlayer2.DrmEventCallback> record = this.mDrmEventCallbackRecord;
                try {
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setEndPositionTimerIfNeeded(MediaPlayer.OnCompletionListener completionListener, MediaPlayerSource src, MediaTimestamp timedsd) {
        final MediaPlayerSource mediaPlayerSource = src;
        if (mediaPlayerSource == this.mPlayer.getFirst()) {
            this.mEndPositionHandler.removeCallbacksAndMessages(null);
            DataSourceDesc dsd = src.getDSD();
            if (dsd.getEndPosition() != 576460752303423487L && timedsd.getMediaClockRate() > 0.0f) {
                long anchorMediaTimeUs = timedsd.getAnchorMediaTimeUs();
                long timeLeftMs = (long) (((float) (dsd.getEndPosition() - ((anchorMediaTimeUs + ((System.nanoTime() - timedsd.getAnchorSytemNanoTime()) / 1000)) / 1000))) / timedsd.getMediaClockRate());
                Handler handler = this.mEndPositionHandler;
                final MediaPlayer.OnCompletionListener onCompletionListener = completionListener;
                AnonymousClass27 r12 = new Runnable() {
                    public void run() {
                        if (MediaPlayer2Impl.this.mPlayer.getFirst() == mediaPlayerSource) {
                            MediaPlayer2Impl.this.mPlayer.pause();
                            onCompletionListener.onCompletion(mediaPlayerSource.mPlayer);
                        }
                    }
                };
                long j = 0;
                if (timeLeftMs >= 0) {
                    j = timeLeftMs;
                }
                handler.postDelayed(r12, j);
                return;
            }
        }
        MediaPlayer.OnCompletionListener onCompletionListener2 = completionListener;
    }

    /* access modifiers changed from: private */
    public void setUpListeners(final MediaPlayerSource src) {
        MediaPlayer p = src.mPlayer;
        final MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                MediaPlayer2Impl.this.handleDataSourceError(MediaPlayer2Impl.this.mPlayer.onPrepared(mp));
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback callback) {
                        callback.onInfo(MediaPlayer2Impl.this, src.getDSD(), 100, 0);
                    }
                });
                MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                    public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                        cb.onMediaPrepared(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, src.getDSD());
                    }
                });
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 6 && MediaPlayer2Impl.this.mCurrentTask.mDSD == src.getDSD() && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                        MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                        Task unused = MediaPlayer2Impl.this.mCurrentTask = null;
                        MediaPlayer2Impl.this.processPendingTask_l();
                    }
                }
            }
        };
        p.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (src.getDSD().getStartPosition() != 0) {
                    src.mPlayer.seekTo((long) ((int) src.getDSD().getStartPosition()), 3);
                } else {
                    preparedListener.onPrepared(mp);
                }
            }
        });
        p.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, final int width, final int height) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onVideoSizeChanged(MediaPlayer2Impl.this, src.getDSD(), width, height);
                    }
                });
            }
        });
        p.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what != 3) {
                    switch (what) {
                        case MediaPlayer2.MEDIA_INFO_BUFFERING_START:
                            MediaPlayer2Impl.this.mPlayer.setBufferingState(mp, 2);
                            break;
                        case MediaPlayer2.MEDIA_INFO_BUFFERING_END:
                            MediaPlayer2Impl.this.mPlayer.setBufferingState(mp, 1);
                            break;
                    }
                } else {
                    MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                        public void notify(MediaPlayer2.EventCallback cb) {
                            cb.onInfo(MediaPlayer2Impl.this, src.getDSD(), 3, 0);
                        }
                    });
                }
                return false;
            }
        });
        final MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                MediaPlayer2Impl.this.handleDataSourceError(MediaPlayer2Impl.this.mPlayer.onCompletion(mp));
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onInfo(MediaPlayer2Impl.this, src.getDSD(), 5, 0);
                    }
                });
            }
        };
        p.setOnCompletionListener(completionListener);
        p.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, final int what, final int extra) {
                MediaPlayer2Impl.this.mPlayer.onError(mp);
                synchronized (MediaPlayer2Impl.this.mTaskLock) {
                    if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                        MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(Integer.MIN_VALUE);
                        Task unused = MediaPlayer2Impl.this.mCurrentTask = null;
                        MediaPlayer2Impl.this.processPendingTask_l();
                    }
                }
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onError(MediaPlayer2Impl.this, src.getDSD(), ((Integer) MediaPlayer2Impl.sErrorEventMap.getOrDefault(Integer.valueOf(what), 1)).intValue(), extra);
                    }
                });
                return true;
            }
        });
        p.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            public void onSeekComplete(MediaPlayer mp) {
                if (src.mMp2State != 1001 || src.getDSD().getStartPosition() == 0) {
                    synchronized (MediaPlayer2Impl.this.mTaskLock) {
                        if (MediaPlayer2Impl.this.mCurrentTask != null && MediaPlayer2Impl.this.mCurrentTask.mMediaCallType == 14 && MediaPlayer2Impl.this.mCurrentTask.mNeedToWaitForEventToComplete) {
                            MediaPlayer2Impl.this.mCurrentTask.sendCompleteNotification(0);
                            Task unused = MediaPlayer2Impl.this.mCurrentTask = null;
                            MediaPlayer2Impl.this.processPendingTask_l();
                        }
                    }
                    final long seekPos = MediaPlayer2Impl.this.getCurrentPosition();
                    MediaPlayer2Impl.this.notifyPlayerEvent(new PlayerEventNotifier() {
                        public void notify(BaseMediaPlayer.PlayerEventCallback cb) {
                            cb.onSeekCompleted(MediaPlayer2Impl.this.mBaseMediaPlayerImpl, seekPos);
                        }
                    });
                    return;
                }
                preparedListener.onPrepared(mp);
            }
        });
        p.setOnTimedMetaDataAvailableListener(new MediaPlayer.OnTimedMetaDataAvailableListener() {
            public void onTimedMetaDataAvailable(MediaPlayer mp, final TimedMetaData data) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onTimedMetaDataAvailable(MediaPlayer2Impl.this, src.getDSD(), data);
                    }
                });
            }
        });
        p.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, final int what, final int extra) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onInfo(MediaPlayer2Impl.this, src.getDSD(), ((Integer) MediaPlayer2Impl.sInfoEventMap.getOrDefault(Integer.valueOf(what), 1)).intValue(), extra);
                    }
                });
                return true;
            }
        });
        p.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, final int percent) {
                if (percent >= 100) {
                    MediaPlayer2Impl.this.mPlayer.setBufferingState(mp, 3);
                }
                src.mBufferedPercentage.set(percent);
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onInfo(MediaPlayer2Impl.this, src.getDSD(), MediaPlayer2.MEDIA_INFO_BUFFERING_UPDATE, percent);
                    }
                });
            }
        });
        p.setOnMediaTimeDiscontinuityListener(new MediaPlayer.OnMediaTimeDiscontinuityListener() {
            public void onMediaTimeDiscontinuity(MediaPlayer mp, final MediaTimestamp timestamp) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onMediaTimeDiscontinuity(MediaPlayer2Impl.this, src.getDSD(), new MediaTimestamp2(timestamp));
                    }
                });
                MediaPlayer2Impl.this.setEndPositionTimerIfNeeded(completionListener, src, timestamp);
            }
        });
        p.setOnSubtitleDataListener(new MediaPlayer.OnSubtitleDataListener() {
            public void onSubtitleData(MediaPlayer mp, final SubtitleData data) {
                MediaPlayer2Impl.this.notifyMediaPlayer2Event(new Mp2EventNotifier() {
                    public void notify(MediaPlayer2.EventCallback cb) {
                        cb.onSubtitleData(MediaPlayer2Impl.this, src.getDSD(), new SubtitleData2(data));
                    }
                });
            }
        });
        p.setOnDrmInfoListener(new MediaPlayer.OnDrmInfoListener() {
            public void onDrmInfo(MediaPlayer mp, final MediaPlayer.DrmInfo drmInfo) {
                MediaPlayer2Impl.this.notifyDrmEvent(new DrmEventNotifier() {
                    public void notify(MediaPlayer2.DrmEventCallback cb) {
                        cb.onDrmInfo(MediaPlayer2Impl.this, src.getDSD(), new DrmInfoImpl(drmInfo.getPssh(), drmInfo.getSupportedSchemes()));
                    }
                });
            }
        });
        p.setOnDrmPreparedListener(new MediaPlayer.OnDrmPreparedListener() {
            public void onDrmPrepared(MediaPlayer mp, final int status) {
                MediaPlayer2Impl.this.notifyDrmEvent(new DrmEventNotifier() {
                    public void notify(MediaPlayer2.DrmEventCallback cb) {
                        cb.onDrmPrepared(MediaPlayer2Impl.this, src.getDSD(), ((Integer) MediaPlayer2Impl.sPrepareDrmStatusMap.getOrDefault(Integer.valueOf(status), 3)).intValue());
                    }
                });
            }
        });
    }
}
