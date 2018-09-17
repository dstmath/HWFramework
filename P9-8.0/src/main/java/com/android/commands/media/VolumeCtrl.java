package com.android.commands.media;

import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IAudioService.Stub;
import android.os.ServiceManager;
import android.util.AndroidException;
import com.android.internal.os.BaseCommand;

public class VolumeCtrl {
    private static final String ADJUST_LOWER = "lower";
    private static final String ADJUST_RAISE = "raise";
    private static final String ADJUST_SAME = "same";
    static final String LOG_OK = "[ok]";
    static final String LOG_V = "[v]";
    static final String LOG_W = "[w]";
    private static final String TAG = "VolumeCtrl";
    public static final String USAGE = new String("the options are as follows: \n\t\t--stream STREAM selects the stream to control, see AudioManager.STREAM_*\n\t\t                controls AudioManager.STREAM_MUSIC if no stream is specified\n\t\t--set INDEX     sets the volume index value\n\t\t--adj DIRECTION adjusts the volume, use raise|same|lower for the direction\n\t\t--get           outputs the current volume\n\t\t--show          shows the UI during the volume change\n\texamples:\n\t\tadb shell media volume --show --stream 3 --set 11\n\t\tadb shell media volume --stream 0 --adj lower\n\t\tadb shell media volume --stream 3 --get\n");
    private static final int VOLUME_CONTROL_MODE_ADJUST = 2;
    private static final int VOLUME_CONTROL_MODE_SET = 1;

    public static void run(BaseCommand cmd) throws Exception {
        int stream = 3;
        int volIndex = 5;
        int mode = 0;
        int adjDir = VOLUME_CONTROL_MODE_SET;
        boolean showUi = false;
        boolean doGet = false;
        String adjustment = null;
        while (true) {
            String option = cmd.nextOption();
            if (option == null) {
                if (mode == VOLUME_CONTROL_MODE_ADJUST) {
                    if (adjustment == null) {
                        cmd.showError("Error: no valid volume adjustment (null)");
                        return;
                    } else if (adjustment.equals(ADJUST_RAISE)) {
                        adjDir = VOLUME_CONTROL_MODE_SET;
                    } else if (adjustment.equals(ADJUST_SAME)) {
                        adjDir = 0;
                    } else if (adjustment.equals(ADJUST_LOWER)) {
                        adjDir = -1;
                    } else {
                        cmd.showError("Error: no valid volume adjustment, was " + adjustment + ", expected " + ADJUST_LOWER + "|" + ADJUST_SAME + "|" + ADJUST_RAISE);
                        return;
                    }
                }
                log(LOG_V, "Connecting to AudioService");
                IAudioService audioService = Stub.asInterface(ServiceManager.checkService("audio"));
                if (audioService == null) {
                    System.err.println("Error type 2");
                    throw new AndroidException("Can't connect to audio service; is the system running?");
                } else if (mode != VOLUME_CONTROL_MODE_SET || (volIndex <= audioService.getStreamMaxVolume(stream) && volIndex >= audioService.getStreamMinVolume(stream))) {
                    int flag = showUi ? VOLUME_CONTROL_MODE_SET : 0;
                    String pack = cmd.getClass().getPackage().getName();
                    if (mode == VOLUME_CONTROL_MODE_SET) {
                        audioService.setStreamVolume(stream, volIndex, flag, pack);
                    } else if (mode == VOLUME_CONTROL_MODE_ADJUST) {
                        audioService.adjustStreamVolume(stream, adjDir, flag, pack);
                    }
                    if (doGet) {
                        log(LOG_V, "volume is " + audioService.getStreamVolume(stream) + " in range [" + audioService.getStreamMinVolume(stream) + ".." + audioService.getStreamMaxVolume(stream) + "]");
                    }
                    return;
                } else {
                    cmd.showError(String.format("Error: invalid volume index %d for stream %d (should be in [%d..%d])", new Object[]{Integer.valueOf(volIndex), Integer.valueOf(stream), Integer.valueOf(audioService.getStreamMinVolume(stream)), Integer.valueOf(audioService.getStreamMaxVolume(stream))}));
                    return;
                }
            } else if (option.equals("--show")) {
                showUi = true;
            } else if (option.equals("--get")) {
                doGet = true;
                log(LOG_V, "will get volume");
            } else if (option.equals("--stream")) {
                stream = Integer.decode(cmd.nextArgRequired()).intValue();
                log(LOG_V, "will control stream=" + stream + " (" + streamName(stream) + ")");
            } else if (option.equals("--set")) {
                volIndex = Integer.decode(cmd.nextArgRequired()).intValue();
                mode = VOLUME_CONTROL_MODE_SET;
                log(LOG_V, "will set volume to index=" + volIndex);
            } else if (option.equals("--adj")) {
                mode = VOLUME_CONTROL_MODE_ADJUST;
                adjustment = cmd.nextArgRequired();
                log(LOG_V, "will adjust volume");
            } else {
                throw new IllegalArgumentException("Unknown argument " + option);
            }
        }
    }

    static void log(String code, String msg) {
        System.out.println(code + " " + msg);
    }

    static String streamName(int stream) {
        try {
            return AudioSystem.STREAM_NAMES[stream];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "invalid stream";
        }
    }
}
