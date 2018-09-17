package android.media;

import android.app.DownloadManager;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.hardware.camera2.params.TonemapCurve;
import android.media.AudioAttributes.Builder;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Ringtone {
    private static final boolean LOGD = true;
    private static final String[] MEDIA_COLUMNS = new String[]{DownloadManager.COLUMN_ID, "_data", "title"};
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    private static final String TAG = "Ringtone";
    private static final ArrayList<Ringtone> sActiveRingtones = new ArrayList();
    private final boolean mAllowRemote;
    private AudioAttributes mAudioAttributes = new Builder().setUsage(6).setContentType(4).build();
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

    class MyOnCompletionListener implements OnCompletionListener {
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
        IRingtonePlayer ringtonePlayer;
        Binder binder = null;
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAllowRemote = allowRemote;
        if (allowRemote) {
            ringtonePlayer = this.mAudioManager.getRingtonePlayer();
        } else {
            ringtonePlayer = null;
        }
        this.mRemotePlayer = ringtonePlayer;
        if (allowRemote) {
            binder = new Binder();
        }
        this.mRemoteToken = binder;
    }

    @Deprecated
    public void setStreamType(int streamType) {
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "setStreamType()");
        setAudioAttributes(new Builder().setInternalLegacyStreamType(streamType).build());
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(streamType), TAG);
    }

    @Deprecated
    public int getStreamType() {
        return AudioAttributes.toLegacyStreamType(this.mAudioAttributes);
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (attributes == null) {
            throw new IllegalArgumentException("Invalid null AudioAttributes for Ringtone");
        }
        this.mAudioAttributes = attributes;
        setUri(this.mUri);
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

    public void setVolume(float volume) {
        synchronized (this.mPlaybackSettingsLock) {
            if (volume < TonemapCurve.LEVEL_BLACK) {
                volume = TonemapCurve.LEVEL_BLACK;
            }
            if (volume > 1.0f) {
                volume = 1.0f;
            }
            this.mVolume = volume;
            applyPlaybackProperties_sync();
        }
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

    public static String getTitle(Context context, Uri uri, boolean followSettingsUri, boolean allowRemote) {
        ContentResolver res = context.getContentResolver();
        String title = null;
        if (uri != null) {
            String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
            if (!"settings".equals(authority)) {
                Cursor cursor = null;
                try {
                    if ("media".equals(authority)) {
                        cursor = res.query(uri, MEDIA_COLUMNS, allowRemote ? null : MEDIA_SELECTION, null, null);
                        if (cursor != null && cursor.getCount() == 1) {
                            cursor.moveToFirst();
                            String string = cursor.getString(2);
                            if (cursor != null) {
                                cursor.close();
                            }
                            return string;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SecurityException e) {
                    IRingtonePlayer mRemotePlayer = null;
                    if (allowRemote) {
                        mRemotePlayer = ((AudioManager) context.getSystemService("audio")).getRingtonePlayer();
                    }
                    if (mRemotePlayer != null) {
                        try {
                            title = mRemotePlayer.getTitle(uri);
                        } catch (RemoteException e2) {
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                if (title == null) {
                    title = ProxyInfo.LOCAL_EXCL_LIST;
                }
            } else if (followSettingsUri) {
                Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri));
                title = context.getString(17040900, getTitle(context, actualUri, false, allowRemote));
            }
        } else {
            title = context.getString(17040904);
        }
        if (title == null) {
            title = context.getString(17040905);
            if (title == null) {
                title = ProxyInfo.LOCAL_EXCL_LIST;
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

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0033 A:{ExcHandler: java.lang.SecurityException (e java.lang.SecurityException), Splitter: B:4:0x0025} */
    /* JADX WARNING: Missing block: B:7:0x0034, code:
            android.util.Log.w(TAG, "Open file failed, goto fallback, uri=" + r2);
     */
    /* JADX WARNING: Missing block: B:8:?, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private AssetFileDescriptor getDefaultFd() {
        Uri uri = RingtoneManager.getDefaultUri(this.mType);
        if (uri == null) {
            uri = System.DEFAULT_RINGTONE_URI;
        }
        Log.i(TAG, "Using default uri " + uri);
        AssetFileDescriptor fd = null;
        try {
            return this.mContext.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
        } catch (SecurityException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0078 A:{ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception), Splitter: B:7:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0078 A:{ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception), Splitter: B:7:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0078 A:{ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception), Splitter: B:7:0x0049} */
    /* JADX WARNING: Missing block: B:25:0x0078, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x0079, code:
            android.util.Log.i(TAG, "Local player open file failed.");
            r7.prepareStat = true;
            destroyLocalPlayer();
     */
    /* JADX WARNING: Missing block: B:27:0x008a, code:
            if (r7.mAllowRemote == false) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:28:0x008c, code:
            android.util.Log.w(TAG, "Remote playback not allowed: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUri(Uri uri) {
        String str = null;
        destroyLocalPlayer();
        this.mType = RingtoneManager.getDefaultType(uri);
        String str2 = TAG;
        StringBuilder append = new StringBuilder().append("setUri uri=");
        if (uri != null) {
            str = uri.toString().replaceAll("\t|\r|\n", ProxyInfo.LOCAL_EXCL_LIST);
        }
        Log.i(str2, append.append(str).toString());
        this.mUri = uri;
        if (this.mUri != null) {
            Uri fileUri = handleSettingsUri(uri);
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
            } catch (Exception e) {
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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0064 A:{ExcHandler: android.os.RemoteException (r6_0 'e' java.lang.Exception), Splitter: B:19:0x005a} */
    /* JADX WARNING: Missing block: B:21:0x0064, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:23:0x0069, code:
            if (playFallbackRingtone() == false) goto L_0x006b;
     */
    /* JADX WARNING: Missing block: B:24:0x006b, code:
            android.util.Log.w(TAG, "Problem playing ringtone: " + r6);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void play() {
        if (this.mLocalPlayer != null) {
            if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
                Log.d(TAG, "Ringtone is playing = " + isPlaying());
                if (!isPlaying()) {
                    this.mLocalPlayer.seekTo(0);
                }
                startLocalPlayer();
            }
        } else if (this.mAllowRemote && this.mRemotePlayer != null && this.mUri != null) {
            boolean looping;
            float volume;
            Uri canonicalUri = this.mUri.getCanonicalUri();
            synchronized (this.mPlaybackSettingsLock) {
                looping = this.mIsLooping;
                volume = this.mVolume;
            }
            try {
                this.mRemotePlayer.play(this.mRemoteToken, canonicalUri, this.mAudioAttributes, volume, looping);
            } catch (Exception e) {
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
        if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
            int ringtoneType = RingtoneManager.getDefaultType(this.mUri);
            if (ringtoneType == -1 || RingtoneManager.getActualDefaultRingtoneUri(this.mContext, ringtoneType) != null) {
                AssetFileDescriptor afd = null;
                try {
                    afd = getDefaultFd();
                    if (afd == null) {
                        afd = this.mContext.getResources().openRawResourceFd(17825797);
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
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (IOException e3) {
                    destroyLocalPlayer();
                    Log.e(TAG, "Failed to open fallback ringtone");
                    HwFrameworkFactory.getLogException().msg("app-ringtone", 65, "ringtone", "Failed to open fallback ringtone");
                    if (afd != null) {
                        try {
                            afd.close();
                        } catch (Exception e4) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (NotFoundException e5) {
                    Log.e(TAG, "Fallback ringtone does not exist ");
                    if (afd != null) {
                        try {
                            afd.close();
                        } catch (Exception e6) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (IllegalStateException e7) {
                    Log.e(TAG, "Illegal ringtone Exception: ");
                    if (afd != null) {
                        try {
                            afd.close();
                        } catch (Exception e8) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (Throwable th) {
                    if (afd != null) {
                        try {
                            afd.close();
                        } catch (Exception e9) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                }
            } else {
                Log.w(TAG, "not playing fallback for " + this.mUri);
            }
        }
        return false;
    }

    void setTitle(String title) {
        this.mTitle = title;
    }

    protected void finalize() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.release();
        }
    }
}
