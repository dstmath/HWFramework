package android.media;

import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.media.AudioAttributes.Builder;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings.System;
import android.provider.SettingsEx;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public class Ringtone {
    private static final boolean LOGD = true;
    private static final String[] MEDIA_COLUMNS = null;
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    private static final String TAG = "Ringtone";
    private static final ArrayList<Ringtone> sActiveRingtones = null;
    private final boolean mAllowRemote;
    private AudioAttributes mAudioAttributes;
    private final AudioManager mAudioManager;
    private final MyOnCompletionListener mCompletionListener;
    private final Context mContext;
    private boolean mIsLooping;
    private MediaPlayer mLocalPlayer;
    private final Object mPlaybackSettingsLock;
    private final IRingtonePlayer mRemotePlayer;
    private final Binder mRemoteToken;
    private final Object mStopPlayLock;
    private String mTitle;
    private int mType;
    private Uri mUri;
    private float mVolume;
    private boolean prepareStat;

    class MyOnCompletionListener implements OnCompletionListener {
        MyOnCompletionListener() {
        }

        public void onCompletion(MediaPlayer mp) {
            synchronized (Ringtone.sActiveRingtones) {
                Ringtone.sActiveRingtones.remove(Ringtone.this);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Ringtone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Ringtone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Ringtone.<clinit>():void");
    }

    private android.net.Uri handleSettingsUri(android.net.Uri r13) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x006a in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r12 = this;
        r8 = r13;
        r9 = 0;
        r1 = 0;
        r11 = android.media.RingtoneManager.getDefaultType(r13);
        r0 = -1;
        if (r11 == r0) goto L_0x0049;
    L_0x000a:
        r0 = r12.mContext;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r1 = android.media.RingtoneManager.getActualDefaultRingtoneUri(r0, r11);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        if (r1 == 0) goto L_0x0044;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
    L_0x0012:
        r0 = r12.mContext;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = 1;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r3 = "_data";	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r4 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r3 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r4 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r5 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r9 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        if (r9 == 0) goto L_0x0044;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
    L_0x002a:
        r0 = r9.getCount();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        if (r0 <= 0) goto L_0x0044;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
    L_0x0030:
        r9.moveToFirst();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r0 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r10 = r9.getString(r0);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r7 = new java.io.File;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r7.<init>(r10);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r0 = r7.exists();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        if (r0 == 0) goto L_0x0044;
    L_0x0043:
        r8 = r1;
    L_0x0044:
        if (r9 == 0) goto L_0x0049;
    L_0x0046:
        r9.close();
    L_0x0049:
        return r8;
    L_0x004a:
        r6 = move-exception;
        r0 = "Ringtone";	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2.<init>();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r3 = "ringtone uri convert failed ";	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = r2.append(r6);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        r2 = r2.toString();	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        android.util.Log.e(r0, r2);	 Catch:{ Exception -> 0x004a, all -> 0x006b }
        if (r9 == 0) goto L_0x006a;
    L_0x0067:
        r9.close();
    L_0x006a:
        return r13;
    L_0x006b:
        r0 = move-exception;
        if (r9 == 0) goto L_0x0071;
    L_0x006e:
        r9.close();
    L_0x0071:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Ringtone.handleSettingsUri(android.net.Uri):android.net.Uri");
    }

    public Ringtone(Context context, boolean allowRemote) {
        IRingtonePlayer ringtonePlayer;
        Binder binder = null;
        this.mCompletionListener = new MyOnCompletionListener();
        this.mType = -1;
        this.mAudioAttributes = new Builder().setUsage(6).setContentType(4).build();
        this.mIsLooping = false;
        this.mVolume = Engine.DEFAULT_VOLUME;
        this.mPlaybackSettingsLock = new Object();
        this.mStopPlayLock = new Object();
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
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
            if (volume < 0.0f) {
                volume = 0.0f;
            }
            if (volume > Engine.DEFAULT_VOLUME) {
                volume = Engine.DEFAULT_VOLUME;
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
        String title = getTitle(context, this.mUri, LOGD, this.mAllowRemote);
        this.mTitle = title;
        return title;
    }

    public static String getTitle(Context context, Uri uri, boolean followSettingsUri, boolean allowRemote) {
        ContentResolver res = context.getContentResolver();
        String str = null;
        if (uri != null) {
            String authority = uri.getAuthority();
            if (!SettingsEx.AUTHORITY.equals(authority)) {
                Cursor cursor = null;
                try {
                    if (MediaStore.AUTHORITY.equals(authority)) {
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
                        mRemotePlayer = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingtonePlayer();
                    }
                    if (mRemotePlayer != null) {
                        try {
                            str = mRemotePlayer.getTitle(uri);
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
                if (str == null) {
                    str = ProxyInfo.LOCAL_EXCL_LIST;
                }
            } else if (followSettingsUri) {
                Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri));
                str = context.getString(17040321, getTitle(context, actualUri, false, allowRemote));
            }
        }
        if (str == null) {
            str = context.getString(17040324);
            if (str == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            }
        }
        return str;
    }

    public void setType(int type) {
        this.mType = type;
    }

    private AssetFileDescriptor getDefaultFd() {
        Uri uri = RingtoneManager.getDefaultUri(this.mType);
        if (uri == null) {
            uri = System.DEFAULT_RINGTONE_URI;
        }
        Log.i(TAG, "Using default uri " + uri);
        AssetFileDescriptor fd = null;
        try {
            fd = this.mContext.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
        } catch (SecurityException e) {
            Log.w(TAG, "Open file failed, goto fallback, uri=" + uri);
        }
        return fd;
    }

    public void setUri(Uri uri) {
        destroyLocalPlayer();
        this.mType = RingtoneManager.getDefaultType(uri);
        Log.i(TAG, "setUri uri=" + uri);
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
                Log.i(TAG, "Local player open file failed.");
                this.prepareStat = LOGD;
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
                AssetFileDescriptor assetFileDescriptor = null;
                try {
                    assetFileDescriptor = getDefaultFd();
                    if (assetFileDescriptor == null) {
                        assetFileDescriptor = this.mContext.getResources().openRawResourceFd(17825797);
                    }
                    if (assetFileDescriptor != null) {
                        this.mLocalPlayer = new MediaPlayer();
                        if (assetFileDescriptor.getDeclaredLength() < 0) {
                            this.mLocalPlayer.setDataSource(assetFileDescriptor.getFileDescriptor());
                        } else {
                            this.mLocalPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
                        }
                        this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                        synchronized (this.mPlaybackSettingsLock) {
                            applyPlaybackProperties_sync();
                        }
                        this.mLocalPlayer.prepare();
                        startLocalPlayer();
                        if (assetFileDescriptor != null) {
                            try {
                                assetFileDescriptor.close();
                            } catch (Exception e) {
                                Log.w(TAG, "close afd error.");
                            }
                        }
                        return LOGD;
                    }
                    Log.e(TAG, "Could not load fallback ringtone");
                    if (assetFileDescriptor != null) {
                        try {
                            assetFileDescriptor.close();
                        } catch (Exception e2) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (IOException e3) {
                    destroyLocalPlayer();
                    Log.e(TAG, "Failed to open fallback ringtone");
                    HwFrameworkFactory.getLogException().msg("app-ringtone", 65, System.RINGTONE, "Failed to open fallback ringtone");
                    if (assetFileDescriptor != null) {
                        try {
                            assetFileDescriptor.close();
                        } catch (Exception e4) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (NotFoundException e5) {
                    Log.e(TAG, "Fallback ringtone does not exist ");
                    if (assetFileDescriptor != null) {
                        try {
                            assetFileDescriptor.close();
                        } catch (Exception e6) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (IllegalStateException e7) {
                    Log.e(TAG, "Illegal ringtone Exception: ");
                    if (assetFileDescriptor != null) {
                        try {
                            assetFileDescriptor.close();
                        } catch (Exception e8) {
                            Log.w(TAG, "close afd error.");
                        }
                    }
                } catch (Throwable th) {
                    if (assetFileDescriptor != null) {
                        try {
                            assetFileDescriptor.close();
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
