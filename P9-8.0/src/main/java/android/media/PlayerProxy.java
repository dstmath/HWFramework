package android.media;

import android.media.VolumeShaper.Configuration;
import android.media.VolumeShaper.Operation;

public class PlayerProxy {
    private static final boolean DEBUG = false;
    private static final String TAG = "PlayerProxy";
    private final AudioPlaybackConfiguration mConf;

    PlayerProxy(AudioPlaybackConfiguration apc) {
        if (apc == null) {
            throw new IllegalArgumentException("Illegal null AudioPlaybackConfiguration");
        }
        this.mConf = apc;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for start operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void start() {
        try {
            this.mConf.getIPlayer().start();
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for pause operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void pause() {
        try {
            this.mConf.getIPlayer().pause();
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for stop operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stop() {
        try {
            this.mConf.getIPlayer().stop();
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for setVolume operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setVolume(float vol) {
        try {
            this.mConf.getIPlayer().setVolume(vol);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for setPan operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPan(float pan) {
        try {
            this.mConf.getIPlayer().setPan(pan);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for setStartDelayMs operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setStartDelayMs(int delayMs) {
        try {
            this.mConf.getIPlayer().setStartDelayMs(delayMs);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0013, code:
            throw new java.lang.IllegalStateException("No player to proxy for applyVolumeShaper operation, player already released?", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void applyVolumeShaper(Configuration configuration, Operation operation) {
        try {
            this.mConf.getIPlayer().applyVolumeShaper(configuration, operation);
        } catch (Exception e) {
        }
    }
}
