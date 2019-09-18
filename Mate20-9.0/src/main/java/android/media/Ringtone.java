package android.media;

import android.app.DownloadManager;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.R;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Ringtone {
    private static final boolean LOGD = true;
    private static final String[] MEDIA_COLUMNS = {DownloadManager.COLUMN_ID, "_data", "title"};
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    private static final String TAG = "Ringtone";
    /* access modifiers changed from: private */
    public static final ArrayList<Ringtone> sActiveRingtones = new ArrayList<>();
    private final boolean mAllowRemote;
    private AudioAttributes mAudioAttributes = new AudioAttributes.Builder().setUsage(6).setContentType(4).build();
    private final AudioManager mAudioManager;
    private final MyOnCompletionListener mCompletionListener = new MyOnCompletionListener();
    private final Context mContext;
    private boolean mIsLooping = false;
    private MediaPlayer mLocalPlayer;
    private final Object mPlaybackSettingsLock = new Object();
    private final IRingtonePlayer mRemotePlayer;
    private final Binder mRemoteToken;
    private final Object mStopPlayLock = new Object();
    private String mTitle;
    private int mType = -1;
    private Uri mUri;
    private float mVolume = 1.0f;
    private boolean prepareStat;

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        MyOnCompletionListener() {
        }

        public void onCompletion(MediaPlayer mp) {
            synchronized (Ringtone.sActiveRingtones) {
                Ringtone.sActiveRingtones.remove(Ringtone.this);
            }
            mp.setOnCompletionListener(null);
        }
    }

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
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(streamType), TAG);
    }

    @Deprecated
    public int getStreamType() {
        return AudioAttributes.toLegacyStreamType(this.mAudioAttributes);
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (attributes != null) {
            this.mAudioAttributes = attributes;
            setUri(this.mUri);
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
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.setVolume(this.mVolume);
            this.mLocalPlayer.setLooping(this.mIsLooping);
        } else if (!this.mAllowRemote || this.mRemotePlayer == null) {
            Log.w(TAG, "Neither local nor remote player available when applying playback properties");
        } else {
            try {
                this.mRemotePlayer.setPlaybackProperties(this.mRemoteToken, this.mVolume, this.mIsLooping);
            } catch (RemoteException e) {
                Log.w(TAG, "Problem setting playback properties: ", e);
            }
        }
    }

    public String getTitle(Context context) {
        if (this.mTitle != null) {
            return this.mTitle;
        }
        String title = getTitle(context, this.mUri, true, this.mAllowRemote);
        this.mTitle = title;
        return title;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006b, code lost:
        if (r10 != null) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006d, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0095, code lost:
        if (r10 == null) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0098, code lost:
        if (r7 != null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009a, code lost:
        r7 = "";
     */
    public static String getTitle(Context context, Uri uri, boolean followSettingsUri, boolean allowRemote) {
        ContentResolver res = context.getContentResolver();
        String title = null;
        if (uri != null) {
            String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
            if (!"settings".equals(authority)) {
                String mediaSelection = null;
                Cursor cursor = null;
                try {
                    if ("media".equals(authority)) {
                        if (!allowRemote) {
                            mediaSelection = MEDIA_SELECTION;
                        }
                        cursor = res.query(uri, MEDIA_COLUMNS, mediaSelection, null, null);
                        if (cursor != null && cursor.getCount() == 1) {
                            cursor.moveToFirst();
                            String string = cursor.getString(2);
                            if (cursor != null) {
                                cursor.close();
                            }
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
                    if (cursor != null) {
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
        if (title == null) {
            title = context.getString(R.string.ringtone_unknown);
            if (title == null) {
                title = "";
            }
        }
        return title;
    }

    private Uri handleSettingsUri(Uri uri) {
        Uri fileUri = uri;
        Cursor mediaCursor = null;
        int ringtoneType = RingtoneManager.getDefaultType(uri);
        if (ringtoneType != -1) {
            try {
                Uri mediaUri = RingtoneManager.getActualDefaultRingtoneUri(this.mContext, ringtoneType);
                if (mediaUri != null) {
                    mediaCursor = this.mContext.getContentResolver().query(mediaUri, new String[]{"_data"}, null, null, null);
                    if (mediaCursor != null && mediaCursor.getCount() > 0) {
                        mediaCursor.moveToFirst();
                        if (new File(mediaCursor.getString(0)).exists()) {
                            fileUri = mediaUri;
                        }
                    }
                }
                if (mediaCursor != null) {
                    mediaCursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "ringtone uri convert failed " + e);
                if (mediaCursor != null) {
                    mediaCursor.close();
                }
                return uri;
            } catch (Throwable th) {
                if (mediaCursor != null) {
                    mediaCursor.close();
                }
                throw th;
            }
        }
        return fileUri;
    }

    public void setType(int type) {
        this.mType = type;
    }

    private AssetFileDescriptor getDefaultFd() {
        Uri uri = RingtoneManager.getDefaultUri(this.mType);
        if (uri == null) {
            uri = Settings.System.DEFAULT_RINGTONE_URI;
        }
        Log.i(TAG, "Using default uri " + uri);
        try {
            return this.mContext.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
        } catch (IOException | SecurityException e) {
            Log.w(TAG, "Open file failed, goto fallback, uri=" + uri);
            return null;
        }
    }

    public void setUri(Uri uri) {
        destroyLocalPlayer();
        this.mType = RingtoneManager.getDefaultType(uri);
        StringBuilder sb = new StringBuilder();
        sb.append("setUri uri=");
        sb.append(uri != null ? uri.toString().replaceAll("\t|\r|\n", "") : null);
        Log.i(TAG, sb.toString());
        this.mUri = uri;
        if (this.mUri != null) {
            Uri handleSettingsUri = handleSettingsUri(uri);
            this.mLocalPlayer = new MediaPlayer();
            this.prepareStat = false;
            try {
                if (this.mLocalPlayer != null) {
                    this.mLocalPlayer.setDataSource(this.mContext, uri);
                    this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                    synchronized (this.mPlaybackSettingsLock) {
                        applyPlaybackProperties_sync();
                    }
                    this.mLocalPlayer.prepare();
                }
            } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e) {
                Log.i(TAG, "Local player open file failed.");
                this.prepareStat = true;
                destroyLocalPlayer();
                if (!this.mAllowRemote) {
                    Log.w(TAG, "Remote playback not allowed: " + e);
                }
            }
            if (this.mLocalPlayer != null) {
                Log.d(TAG, "Successfully created local player");
            } else {
                Log.d(TAG, "Problem opening; delegating to remote player");
            }
        }
    }

    public boolean getPrepareStat() {
        return this.prepareStat;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void play() {
        boolean looping;
        float volume;
        if (this.mLocalPlayer != null) {
            if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
                Log.d(TAG, "Ringtone is playing = " + isPlaying());
                if (!isPlaying()) {
                    this.mLocalPlayer.seekTo(0);
                }
                startLocalPlayer();
            }
        } else if (this.mAllowRemote && this.mRemotePlayer != null && this.mUri != null) {
            Uri canonicalUri = this.mUri.getCanonicalUri();
            synchronized (this.mPlaybackSettingsLock) {
                looping = this.mIsLooping;
                volume = this.mVolume;
            }
            try {
                this.mRemotePlayer.play(this.mRemoteToken, canonicalUri, this.mAudioAttributes, volume, looping);
            } catch (RemoteException | IllegalStateException e) {
                if (!playFallbackRingtone()) {
                    Log.w(TAG, "Problem playing ringtone: " + e);
                }
            }
        } else if (!playFallbackRingtone()) {
            Log.w(TAG, "Neither local nor remote playback available");
        }
    }

    public void stop() {
        if (this.mLocalPlayer != null) {
            destroyLocalPlayer();
        } else if (this.mAllowRemote && this.mRemotePlayer != null) {
            try {
                this.mRemotePlayer.stop(this.mRemoteToken);
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
        }
    }

    public boolean isPlaying() {
        if (this.mLocalPlayer != null) {
            return this.mLocalPlayer.isPlaying();
        }
        if (!this.mAllowRemote || this.mRemotePlayer == null) {
            Log.w(TAG, "Neither local nor remote playback available");
            return false;
        }
        try {
            return this.mRemotePlayer.isPlaying(this.mRemoteToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Problem checking ringtone: " + e);
            return false;
        }
    }

    private boolean playFallbackRingtone() {
        String str;
        if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
            int ringtoneType = RingtoneManager.getDefaultType(this.mUri);
            if (ringtoneType == -1 || RingtoneManager.getActualDefaultRingtoneUri(this.mContext, ringtoneType) != null) {
                AssetFileDescriptor afd = null;
                try {
                    afd = getDefaultFd();
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
                        this.mLocalPlayer.prepare();
                        startLocalPlayer();
                        if (afd != null) {
                            try {
                                afd.close();
                            } catch (Exception e) {
                                Log.w(TAG, "close afd error.");
                            }
                        }
                        return true;
                    }
                    Log.e(TAG, "Could not load fallback ringtone");
                    if (afd != null) {
                        try {
                            afd.close();
                        } catch (Exception e2) {
                        }
                    }
                } catch (IOException e3) {
                    destroyLocalPlayer();
                    Log.e(TAG, "Failed to open fallback ringtone");
                    HwFrameworkFactory.getLogException().msg("app-ringtone", 65, "ringtone", "Failed to open fallback ringtone");
                    if (afd != null) {
                        afd.close();
                    }
                } catch (Resources.NotFoundException e4) {
                    Log.e(TAG, "Fallback ringtone does not exist ");
                    if (afd != null) {
                        afd.close();
                    }
                } catch (IllegalStateException e5) {
                    try {
                        Log.e(TAG, "Illegal ringtone Exception: ");
                    } finally {
                        if (afd != null) {
                            try {
                                afd.close();
                            } catch (Exception e6) {
                                str = "close afd error.";
                                Log.w(TAG, str);
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "not playing fallback for " + this.mUri);
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.release();
        }
    }
}
