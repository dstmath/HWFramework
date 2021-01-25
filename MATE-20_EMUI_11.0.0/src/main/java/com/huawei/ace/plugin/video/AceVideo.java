package com.huawei.ace.plugin.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.view.Surface;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.IAceOnResourceEvent;
import java.io.IOException;
import java.util.Map;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public class AceVideo extends AceVideoBase implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private static final AudioAttributes ATTR_VIDEO = new AudioAttributes.Builder().setUsage(1).setContentType(3).build();
    private static final String LOG_TAG = "AceVideo";
    private final Context context;
    private String instanceName;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final Surface surface;

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void onActivityResume() {
    }

    public AceVideo(long j, String str, Surface surface2, Context context2, IAceOnResourceEvent iAceOnResourceEvent) {
        super(j, iAceOnResourceEvent);
        this.instanceName = str;
        this.surface = surface2;
        this.mediaPlayer.setSurface(surface2);
        this.context = context2;
        try {
            this.mediaPlayer.setAudioAttributes(ATTR_VIDEO);
        } catch (IllegalArgumentException unused) {
            ALog.w(LOG_TAG, "setAudioAttributes failed.");
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void release() {
        this.mediaPlayer.stop();
        this.mediaPlayer.release();
    }

    private String getUrl(String str) {
        return str.indexOf("file://") == 0 ? str.substring(7) : str;
    }

    private Boolean setDataSource(String str) {
        AssetFileDescriptor assetFileDescriptor;
        try {
            if (str.indexOf("file://") == 0) {
                this.mediaPlayer.setDataSource(str.substring(7));
            } else if (str.indexOf(PsuedoNames.PSEUDONAME_ROOT) == 0) {
                AssetManager assets = this.context.getAssets();
                if (assets == null) {
                    return false;
                }
                try {
                    assetFileDescriptor = assets.openFd("js/" + this.instanceName + '/' + str.substring(1));
                } catch (IOException unused) {
                    ALog.i(LOG_TAG, "not found asset in instance path, now begin to search asset in share path");
                    assetFileDescriptor = assets.openFd("js/share/" + str.substring(1));
                }
                this.mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            } else {
                this.mediaPlayer.setDataSource(str);
            }
            return true;
        } catch (IOException unused2) {
            ALog.e(LOG_TAG, "setDataSource failed, IOException");
            return false;
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String initMediaPlayer(Map<String, String> map) {
        super.lambda$new$0$AceVideoBase(map);
        try {
            if (map.containsKey("src")) {
                if (setDataSource(map.get("src")).booleanValue()) {
                    this.mediaPlayer.setOnPreparedListener(this);
                    this.mediaPlayer.setOnErrorListener(this);
                    this.mediaPlayer.setOnSeekCompleteListener(this);
                    this.mediaPlayer.setOnCompletionListener(this);
                    this.mediaPlayer.setOnBufferingUpdateListener(this);
                    this.mediaPlayer.prepare();
                    return "success";
                }
            }
            return "fail";
        } catch (IOException unused) {
            ALog.e(LOG_TAG, "initMediaPlayer failed, IOException");
            return "fail";
        } catch (IllegalStateException unused2) {
            ALog.e(LOG_TAG, "initMediaPlayer failed, IllegalStateException.");
            return "fail";
        }
    }

    @Override // android.media.MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mediaPlayer2) {
        if (isAutoPlay()) {
            mediaPlayer2.start();
        }
        if (isMute()) {
            mediaPlayer2.setVolume(0.0f, 0.0f);
        }
        firePrepared(mediaPlayer2.getVideoWidth(), mediaPlayer2.getVideoHeight(), mediaPlayer2.getDuration(), isAutoPlay());
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public boolean onError(MediaPlayer mediaPlayer2, int i, int i2) {
        fireError();
        return false;
    }

    @Override // android.media.MediaPlayer.OnCompletionListener
    public void onCompletion(MediaPlayer mediaPlayer2) {
        fireCompletion();
    }

    @Override // android.media.MediaPlayer.OnSeekCompleteListener
    public void onSeekComplete(MediaPlayer mediaPlayer2) {
        fireSeekComplete(this.mediaPlayer.getCurrentPosition() / 1000);
    }

    @Override // android.media.MediaPlayer.OnBufferingUpdateListener
    public void onBufferingUpdate(MediaPlayer mediaPlayer2, int i) {
        fireBufferingUpdate(i);
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String start(Map<String, String> map) {
        this.mediaPlayer.start();
        return "success";
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String pause(Map<String, String> map) {
        this.mediaPlayer.pause();
        return "success";
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String seekTo(Map<String, String> map) {
        if (!map.containsKey("value")) {
            return "fail";
        }
        try {
            this.mediaPlayer.seekTo(Integer.parseInt(map.get("value")) * 1000);
            return "success";
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException, seek failed. value = " + map.get("value"));
            return "fail";
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String setVolume(Map<String, String> map) {
        if (!map.containsKey("value")) {
            return "fail";
        }
        try {
            float parseFloat = Float.parseFloat(map.get("value"));
            this.mediaPlayer.setVolume(parseFloat, parseFloat);
            return "success";
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException, setVolume failed. value = " + map.get("value"));
            return "fail";
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String getPosition(Map<String, String> map) {
        int currentPosition = this.mediaPlayer.getCurrentPosition();
        return "currentpos=" + (currentPosition / 1000);
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void onActivityPause() {
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            firePlayStatusChange(false);
        }
    }
}
