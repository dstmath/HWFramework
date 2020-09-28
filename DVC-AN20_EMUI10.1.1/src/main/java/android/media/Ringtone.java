package android.media;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;

public class Ringtone {
    private static final boolean LOGD = true;
    private static final String[] MEDIA_COLUMNS = {"_id", "title"};
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    private static final String TAG = "Ringtone";
    private static final ArrayList<Ringtone> sActiveRingtones = new ArrayList<>();
    private final boolean mAllowRemote;
    private AudioAttributes mAudioAttributes = new AudioAttributes.Builder().setUsage(6).setContentType(4).build();
    private final AudioManager mAudioManager;
    private final MyOnCompletionListener mCompletionListener = new MyOnCompletionListener();
    private final Context mContext;
    private boolean mIsLooping = false;
    @UnsupportedAppUsage
    private MediaPlayer mLocalPlayer;
    private final Object mPlaybackSettingsLock = new Object();
    private boolean mPrepareStat = false;
    private final IRingtonePlayer mRemotePlayer;
    private final Binder mRemoteToken;
    private final Object mStopPlayLock = new Object();
    private String mTitle;
    private int mType = -1;
    @UnsupportedAppUsage
    private Uri mUri;
    private float mVolume = 1.0f;
    private VolumeShaper mVolumeShaper;
    private VolumeShaper.Configuration mVolumeShaperConfig;

    @UnsupportedAppUsage
    public Ringtone(Context context, boolean allowRemote) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAllowRemote = allowRemote;
        Binder binder = null;
        this.mRemotePlayer = allowRemote ? this.mAudioManager.getRingtonePlayer() : null;
        this.mRemoteToken = allowRemote ? new Binder() : binder;
    }

    @Deprecated
    public void setStreamType(int streamType) {
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "setStreamType()");
        setAudioAttributes(new AudioAttributes.Builder().setInternalLegacyStreamType(streamType).build());
    }

    @Deprecated
    public int getStreamType() {
        return AudioAttributes.toLegacyStreamType(this.mAudioAttributes);
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (attributes != null) {
            this.mAudioAttributes = attributes;
            setUri(this.mUri, this.mVolumeShaperConfig);
            return;
        }
        throw new IllegalArgumentException("Invalid null AudioAttributes for Ringtone");
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAudioAttributes;
    }

    public void setLooping(boolean looping) {
        synchronized (this.mPlaybackSettingsLock) {
            this.mIsLooping = looping;
            applyPlaybackProperties_sync();
        }
    }

    public boolean isLooping() {
        boolean z;
        synchronized (this.mPlaybackSettingsLock) {
            z = this.mIsLooping;
        }
        return z;
    }

    public void setVolume(float volume) {
        synchronized (this.mPlaybackSettingsLock) {
            if (volume < 0.0f) {
                volume = 0.0f;
            }
            if (volume > 1.0f) {
                volume = 1.0f;
            }
            this.mVolume = volume;
            applyPlaybackProperties_sync();
        }
    }

    public float getVolume() {
        float f;
        synchronized (this.mPlaybackSettingsLock) {
            f = this.mVolume;
        }
        return f;
    }

    private void applyPlaybackProperties_sync() {
        IRingtonePlayer iRingtonePlayer;
        MediaPlayer mediaPlayer = this.mLocalPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.mVolume);
            this.mLocalPlayer.setLooping(this.mIsLooping);
        } else if (!this.mAllowRemote || (iRingtonePlayer = this.mRemotePlayer) == null) {
            Log.w(TAG, "Neither local nor remote player available when applying playback properties");
        } else {
            try {
                iRingtonePlayer.setPlaybackProperties(this.mRemoteToken, this.mVolume, this.mIsLooping);
            } catch (RemoteException e) {
                Log.w(TAG, "Problem setting playback properties: ", e);
            }
        }
    }

    public String getTitle(Context context) {
        String str = this.mTitle;
        if (str != null) {
            return str;
        }
        String title = getTitle(context, this.mUri, true, this.mAllowRemote);
        this.mTitle = title;
        return title;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0068, code lost:
        if (r10 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006a, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0092, code lost:
        if (0 == 0) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0095, code lost:
        if (r7 != null) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0097, code lost:
        r7 = "";
     */
    public static String getTitle(Context context, Uri uri, boolean followSettingsUri, boolean allowRemote) {
        ContentResolver res = context.getContentResolver();
        String title = null;
        if (uri != null) {
            String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
            if (!"settings".equals(authority)) {
                Cursor cursor = null;
                try {
                    if (MediaStore.AUTHORITY.equals(authority)) {
                        cursor = res.query(uri, MEDIA_COLUMNS, allowRemote ? null : MEDIA_SELECTION, null, null);
                        if (cursor != null && cursor.getCount() == 1) {
                            cursor.moveToFirst();
                            String string = cursor.getString(1);
                            cursor.close();
                            return string;
                        }
                    }
                } catch (SecurityException e) {
                    IRingtonePlayer mRemotePlayer2 = null;
                    if (allowRemote) {
                        mRemotePlayer2 = ((AudioManager) context.getSystemService("audio")).getRingtonePlayer();
                    }
                    if (mRemotePlayer2 != null) {
                        try {
                            title = mRemotePlayer2.getTitle(uri);
                        } catch (RemoteException e2) {
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        cursor.close();
                    }
                    throw th;
                }
            } else if (followSettingsUri) {
                title = context.getString(R.string.ringtone_default_with_actual, getTitle(context, RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri)), false, allowRemote));
            }
        } else {
            title = context.getString(R.string.ringtone_silent);
        }
        if (title != null) {
            return title;
        }
        String title2 = context.getString(R.string.ringtone_unknown);
        if (title2 == null) {
            return "";
        }
        return title2;
    }

    @UnsupportedAppUsage
    public void setUri(Uri uri) {
        setUri(uri, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0076, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        android.util.Log.i(android.media.Ringtone.TAG, "Local player open file failed.");
        r4.mPrepareStat = true;
        destroyLocalPlayer();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0086, code lost:
        if (r4.mAllowRemote == false) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0088, code lost:
        android.util.Log.w(android.media.Ringtone.TAG, "Remote playback not allowed: " + r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0076 A[ExcHandler: IOException | IllegalArgumentException | IllegalStateException | SecurityException (r0v13 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:14:0x005e] */
    public void setUri(Uri uri, VolumeShaper.Configuration volumeShaperConfig) {
        this.mVolumeShaperConfig = volumeShaperConfig;
        destroyLocalPlayer();
        this.mType = RingtoneManager.getDefaultType(uri);
        StringBuilder sb = new StringBuilder();
        sb.append("setUri uri=");
        sb.append(uri != null ? uri.toString().replaceAll("\t|\r|\n", "") : null);
        Log.i(TAG, sb.toString());
        this.mUri = uri;
        if (this.mUri != null) {
            this.mLocalPlayer = new MediaPlayer();
            this.mPrepareStat = false;
            if (this.mLocalPlayer != null) {
                this.mLocalPlayer.setDataSource(this.mContext, this.mUri);
                this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                synchronized (this.mPlaybackSettingsLock) {
                    applyPlaybackProperties_sync();
                }
                try {
                    if (this.mVolumeShaperConfig != null) {
                        this.mVolumeShaper = this.mLocalPlayer.createVolumeShaper(this.mVolumeShaperConfig);
                    }
                    this.mLocalPlayer.prepare();
                } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e) {
                }
            }
            if (this.mLocalPlayer != null) {
                Log.d(TAG, "Successfully created local player");
            } else {
                Log.d(TAG, "Problem opening; delegating to remote player");
            }
        }
    }

    public void setType(int type) {
        this.mType = type;
    }

    private AssetFileDescriptor getDefaultFd() {
        Uri uri = RingtoneManager.getDefaultUri(this.mType);
        Log.i(TAG, "getDefaultFd mTypeValue : " + String.valueOf(this.mType));
        if (uri == null) {
            uri = Settings.System.DEFAULT_RINGTONE_URI;
        }
        Log.i(TAG, "Using default uri " + uri);
        try {
            return this.mContext.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (IOException | SecurityException e) {
            Log.w(TAG, "Open file failed, goto fallback, uri=" + uri);
            return null;
        }
    }

    public boolean getPrepareStat() {
        return this.mPrepareStat;
    }

    @UnsupportedAppUsage
    public Uri getUri() {
        return this.mUri;
    }

    public void play() {
        boolean looping;
        float volume;
        if (this.mLocalPlayer != null) {
            if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
                startLocalPlayer();
            }
        } else if (this.mAllowRemote && this.mRemotePlayer != null) {
            Uri canonicalUri = this.mUri.getCanonicalUri();
            synchronized (this.mPlaybackSettingsLock) {
                looping = this.mIsLooping;
                volume = this.mVolume;
            }
            try {
                this.mRemotePlayer.playWithVolumeShaping(this.mRemoteToken, canonicalUri, this.mAudioAttributes, volume, looping, this.mVolumeShaperConfig);
            } catch (RemoteException e) {
                if (!playFallbackRingtone()) {
                    Log.w(TAG, "Problem playing ringtone: " + e);
                }
            }
        } else if (!playFallbackRingtone()) {
            Log.w(TAG, "Neither local nor remote playback available");
        }
    }

    public void stop() {
        IRingtonePlayer iRingtonePlayer;
        if (this.mLocalPlayer != null) {
            destroyLocalPlayer();
        } else if (this.mAllowRemote && (iRingtonePlayer = this.mRemotePlayer) != null) {
            try {
                iRingtonePlayer.stop(this.mRemoteToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Problem stopping ringtone: " + e);
            }
        }
    }

    private void destroyLocalPlayer() {
        synchronized (this.mStopPlayLock) {
            if (this.mLocalPlayer != null) {
                this.mLocalPlayer.setOnCompletionListener(null);
                this.mLocalPlayer.reset();
                this.mLocalPlayer.release();
                this.mLocalPlayer = null;
                this.mVolumeShaper = null;
                synchronized (sActiveRingtones) {
                    sActiveRingtones.remove(this);
                }
            }
        }
    }

    private void startLocalPlayer() {
        if (this.mLocalPlayer != null) {
            synchronized (sActiveRingtones) {
                sActiveRingtones.add(this);
            }
            this.mLocalPlayer.setOnCompletionListener(this.mCompletionListener);
            this.mLocalPlayer.start();
            VolumeShaper volumeShaper = this.mVolumeShaper;
            if (volumeShaper != null) {
                volumeShaper.apply(VolumeShaper.Operation.PLAY);
            }
        }
    }

    public boolean isPlaying() {
        IRingtonePlayer iRingtonePlayer;
        MediaPlayer mediaPlayer = this.mLocalPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        if (!this.mAllowRemote || (iRingtonePlayer = this.mRemotePlayer) == null) {
            Log.w(TAG, "Neither local nor remote playback available");
            return false;
        }
        try {
            return iRingtonePlayer.isPlaying(this.mRemoteToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Problem checking ringtone: " + e);
            return false;
        }
    }

    private boolean playFallbackRingtone() {
        if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) == 0) {
            return false;
        }
        Log.i("playFallbackRingtone ringtoneUri : ", this.mUri.toString());
        int ringtoneType = RingtoneManager.getDefaultType(this.mUri);
        Log.i("playFallbackRingtone ringtoneType : ", String.valueOf(ringtoneType));
        if (ringtoneType == -1 || RingtoneManager.getActualDefaultRingtoneUri(this.mContext, ringtoneType) != null) {
            try {
                AssetFileDescriptor afd = getDefaultFd();
                if (afd == null) {
                    afd = this.mContext.getResources().openRawResourceFd(R.raw.fallbackring);
                }
                if (afd != null) {
                    this.mLocalPlayer = new MediaPlayer();
                    if (afd.getDeclaredLength() < 0) {
                        this.mLocalPlayer.setDataSource(afd.getFileDescriptor());
                    } else {
                        this.mLocalPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                    }
                    this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                    synchronized (this.mPlaybackSettingsLock) {
                        applyPlaybackProperties_sync();
                    }
                    if (this.mVolumeShaperConfig != null) {
                        this.mVolumeShaper = this.mLocalPlayer.createVolumeShaper(this.mVolumeShaperConfig);
                    }
                    this.mLocalPlayer.prepare();
                    startLocalPlayer();
                    afd.close();
                    return true;
                }
                Log.e(TAG, "Could not load fallback ringtone");
                return false;
            } catch (IOException e) {
                destroyLocalPlayer();
                Log.e(TAG, "Failed to open fallback ringtone");
                HwFrameworkFactory.getLogException().msg("app-ringtone", 65, Settings.System.RINGTONE, "Failed to open fallback ringtone");
                return false;
            } catch (Resources.NotFoundException e2) {
                Log.e(TAG, "Fallback ringtone does not exist");
                return false;
            }
        } else {
            Log.w(TAG, "not playing fallback");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        MediaPlayer mediaPlayer = this.mLocalPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    /* access modifiers changed from: package-private */
    public class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        MyOnCompletionListener() {
        }

        @Override // android.media.MediaPlayer.OnCompletionListener
        public void onCompletion(MediaPlayer mp) {
            synchronized (Ringtone.sActiveRingtones) {
                Ringtone.sActiveRingtones.remove(Ringtone.this);
            }
            mp.setOnCompletionListener(null);
        }
    }
}
