package ohos.ace.plugin.video;

import com.huawei.ace.plugin.video.AceVideoBase;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.IAceOnResourceEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import ohos.agp.graphics.Surface;
import ohos.app.Context;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.RawFileDescriptor;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.ResourceManager;
import ohos.media.common.Source;
import ohos.media.player.Player;

public class AceVideo extends AceVideoBase implements Player.IPlayerCallback {
    private static final String LOG_TAG = "AceVideo";
    private final Context context;
    private EventHandler handler;
    private String instanceName;
    private Object lock = new Object();
    private final Player mediaPlayer;
    private Surface surface;

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void onActivityResume() {
    }

    public void onBufferingChange(int i) {
    }

    public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) {
    }

    public void onMessage(int i, int i2) {
    }

    public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) {
    }

    public void onPrepared() {
    }

    public void onResolutionChanged(int i, int i2) {
    }

    public void onRewindToComplete() {
    }

    public AceVideo(long j, String str, Surface surface2, Context context2, IAceOnResourceEvent iAceOnResourceEvent) {
        super(j, iAceOnResourceEvent);
        this.surface = surface2;
        this.instanceName = str;
        this.context = context2;
        this.mediaPlayer = new Player(context2);
        this.handler = new EventHandler(EventRunner.current());
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void release() {
        CompletableFuture.runAsync(new Runnable() {
            /* class ohos.ace.plugin.video.$$Lambda$AceVideo$VRoeIjqeT5Naq9WfkmJVKDWM22k */

            @Override // java.lang.Runnable
            public final void run() {
                AceVideo.this.lambda$release$0$AceVideo();
            }
        });
    }

    public /* synthetic */ void lambda$release$0$AceVideo() {
        synchronized (this.lock) {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            if (this.handler != null) {
                this.handler.removeAllEvent();
                this.handler = null;
            }
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String initMediaPlayer(Map<String, String> map) {
        super.lambda$new$0$AceVideoBase(map);
        if (!map.containsKey("src") || !setSource(map.get("src"))) {
            ALog.e(LOG_TAG, "media setSource failed.");
            return "fail";
        } else if (!this.mediaPlayer.setVideoSurface(this.surface)) {
            ALog.e(LOG_TAG, "media setSurface failed.");
            return "fail";
        } else {
            this.mediaPlayer.setPlayerCallback(this);
            CompletableFuture.supplyAsync(new Supplier() {
                /* class ohos.ace.plugin.video.$$Lambda$AceVideo$E1GNT1kATVXW86Q1TaBIyvmVpGE */

                @Override // java.util.function.Supplier
                public final Object get() {
                    return AceVideo.this.lambda$initMediaPlayer$1$AceVideo();
                }
            }).thenAccept((Consumer) new Consumer() {
                /* class ohos.ace.plugin.video.$$Lambda$AceVideo$FUj5Ag_A7wPhLmvjKheDwbyzmc */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AceVideo.this.lambda$initMediaPlayer$2$AceVideo((Boolean) obj);
                }
            });
            return "success";
        }
    }

    public /* synthetic */ Boolean lambda$initMediaPlayer$1$AceVideo() {
        synchronized (this.lock) {
            if (this.mediaPlayer.prepare()) {
                return true;
            }
            ALog.e(LOG_TAG, "media prepare failed.");
            return false;
        }
    }

    public /* synthetic */ void lambda$initMediaPlayer$2$AceVideo(Boolean bool) {
        synchronized (this.lock) {
            if (this.handler != null) {
                this.handler.postTask(new PerparedTask(bool.booleanValue()));
            }
        }
    }

    /* access modifiers changed from: private */
    public class PerparedTask implements Runnable {
        private final boolean result;

        PerparedTask(boolean z) {
            this.result = z;
        }

        @Override // java.lang.Runnable
        public void run() {
            AceVideo.this.prepared(this.result);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        $closeResource(r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0095, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0096, code lost:
        if (r5 != null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0098, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x009b, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00d7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d8, code lost:
        if (r9 != null) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00da, code lost:
        $closeResource(r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00dd, code lost:
        throw r0;
     */
    private boolean setSource(String str) {
        if (str.indexOf("file://") == 0) {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(str.substring(7)));
                if (!this.mediaPlayer.setSource(new Source(fileInputStream.getFD()))) {
                    ALog.e(LOG_TAG, "media setSource failed.");
                    $closeResource(null, fileInputStream);
                    return false;
                }
                $closeResource(null, fileInputStream);
                return true;
            } catch (IOException unused) {
                ALog.e(LOG_TAG, "open file failed");
                return false;
            }
        } else if (str.indexOf(PsuedoNames.PSEUDONAME_ROOT) == 0) {
            ResourceManager resourceManager = this.context.getResourceManager();
            RawFileEntry rawFileEntry = resourceManager.getRawFileEntry("js/" + this.instanceName + '/' + str.substring(1));
            if (rawFileEntry != null) {
                try {
                    RawFileDescriptor openRawFileDescriptor = rawFileEntry.openRawFileDescriptor();
                    if (this.mediaPlayer.setSource(openRawFileDescriptor)) {
                        if (openRawFileDescriptor != null) {
                            $closeResource(null, openRawFileDescriptor);
                        }
                        return true;
                    } else if (openRawFileDescriptor != null) {
                        $closeResource(null, openRawFileDescriptor);
                    }
                } catch (IOException unused2) {
                    ALog.e(LOG_TAG, "open asset in instance path failed");
                }
            }
            RawFileEntry rawFileEntry2 = resourceManager.getRawFileEntry("js/share/" + str.substring(1));
            if (rawFileEntry2 != null) {
                try {
                    RawFileDescriptor openRawFileDescriptor2 = rawFileEntry2.openRawFileDescriptor();
                    if (this.mediaPlayer.setSource(openRawFileDescriptor2)) {
                        if (openRawFileDescriptor2 != null) {
                            $closeResource(null, openRawFileDescriptor2);
                        }
                        return true;
                    } else if (openRawFileDescriptor2 != null) {
                        $closeResource(null, openRawFileDescriptor2);
                    }
                } catch (IOException unused3) {
                    ALog.e(LOG_TAG, "open asset in share path failed");
                    return false;
                }
            }
            ALog.e(LOG_TAG, "[Asset]media setSource failed");
            return false;
        } else if (this.mediaPlayer.setSource(new Source(str))) {
            return true;
        } else {
            ALog.e(LOG_TAG, "[Other]media setSource failed");
            return false;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepared(boolean z) {
        if (!z) {
            firePrepared(0, 0, 0, false);
        }
        if (isAutoPlay()) {
            this.mediaPlayer.play();
        }
        if (isMute()) {
            this.mediaPlayer.setVolume(0.0f);
        }
        firePrepared(this.mediaPlayer.getVideoWidth(), this.mediaPlayer.getVideoHeight(), this.mediaPlayer.getDuration(), this.mediaPlayer.isNowPlaying());
    }

    public void onError(int i, int i2) {
        fireError();
    }

    public void onPlayBackComplete() {
        fireCompletion();
    }

    private void onSeekComplete() {
        fireSeekComplete(this.mediaPlayer.getCurrentTime() / 1000);
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String start(Map<String, String> map) {
        return (this.mediaPlayer.isNowPlaying() || this.mediaPlayer.play()) ? "success" : "fail";
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String pause(Map<String, String> map) {
        return (!this.mediaPlayer.isNowPlaying() || this.mediaPlayer.pause()) ? "success" : "fail";
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String seekTo(Map<String, String> map) {
        if (!map.containsKey("value")) {
            return "fail";
        }
        try {
            if (!this.mediaPlayer.rewindTo((long) (Integer.parseInt(map.get("value")) * 1000 * 1000))) {
                return "success";
            }
            onSeekComplete();
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
            this.mediaPlayer.setVolume(Float.parseFloat(map.get("value")));
            return "success";
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException, setVolume failed. value = " + map.get("value"));
            return "fail";
        }
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public String getPosition(Map<String, String> map) {
        int currentTime = this.mediaPlayer.getCurrentTime();
        return "currentpos=" + (currentTime / 1000);
    }

    @Override // com.huawei.ace.plugin.video.AceVideoBase
    public void onActivityPause() {
        if (this.mediaPlayer.isNowPlaying()) {
            this.mediaPlayer.pause();
            firePlayStatusChange(false);
        }
    }
}
