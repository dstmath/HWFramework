package com.android.commands.media;

import android.media.AudioSystem;
import android.media.IAudioService;
import android.os.ServiceManager;
import android.util.AndroidException;
import com.android.internal.os.BaseCommand;
import java.io.PrintStream;

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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x014f  */
    public static void run(BaseCommand cmd) throws Exception {
        int flag;
        char c;
        char c2;
        int stream = 3;
        int volIndex = 5;
        int mode = 0;
        int adjDir = VOLUME_CONTROL_MODE_SET;
        boolean showUi = false;
        boolean doGet = false;
        String adjustment = null;
        while (true) {
            String option = cmd.nextOption();
            if (option != null) {
                switch (option.hashCode()) {
                    case 42995463:
                        if (option.equals("--adj")) {
                            c2 = 4;
                            break;
                        }
                        c2 = 65535;
                        break;
                    case 43001270:
                        if (option.equals("--get")) {
                            c2 = VOLUME_CONTROL_MODE_SET;
                            break;
                        }
                        c2 = 65535;
                        break;
                    case 43012802:
                        if (option.equals("--set")) {
                            c2 = 3;
                            break;
                        }
                        c2 = 65535;
                        break;
                    case 1333399709:
                        if (option.equals("--show")) {
                            c2 = 0;
                            break;
                        }
                        c2 = 65535;
                        break;
                    case 1508023584:
                        if (option.equals("--stream")) {
                            c2 = VOLUME_CONTROL_MODE_ADJUST;
                            break;
                        }
                        c2 = 65535;
                        break;
                    default:
                        c2 = 65535;
                        break;
                }
                if (c2 == 0) {
                    showUi = true;
                } else if (c2 == VOLUME_CONTROL_MODE_SET) {
                    doGet = true;
                    log(LOG_V, "will get volume");
                } else if (c2 == VOLUME_CONTROL_MODE_ADJUST) {
                    stream = Integer.decode(cmd.nextArgRequired()).intValue();
                    log(LOG_V, "will control stream=" + stream + " (" + streamName(stream) + ")");
                } else if (c2 == 3) {
                    volIndex = Integer.decode(cmd.nextArgRequired()).intValue();
                    mode = VOLUME_CONTROL_MODE_SET;
                    log(LOG_V, "will set volume to index=" + volIndex);
                } else if (c2 == 4) {
                    mode = VOLUME_CONTROL_MODE_ADJUST;
                    adjustment = cmd.nextArgRequired();
                    log(LOG_V, "will adjust volume");
                } else {
                    throw new IllegalArgumentException("Unknown argument " + option);
                }
            } else {
                if (mode == VOLUME_CONTROL_MODE_ADJUST) {
                    if (adjustment == null) {
                        cmd.showError("Error: no valid volume adjustment (null)");
                        return;
                    }
                    int hashCode = adjustment.hashCode();
                    if (hashCode != 3522662) {
                        if (hashCode != 103164673) {
                            if (hashCode == 108275692 && adjustment.equals(ADJUST_RAISE)) {
                                c = 0;
                                if (c != 0) {
                                    adjDir = VOLUME_CONTROL_MODE_SET;
                                } else if (c == VOLUME_CONTROL_MODE_SET) {
                                    adjDir = 0;
                                } else if (c != VOLUME_CONTROL_MODE_ADJUST) {
                                    cmd.showError("Error: no valid volume adjustment, was " + adjustment + ", expected " + ADJUST_LOWER + "|" + ADJUST_SAME + "|" + ADJUST_RAISE);
                                    return;
                                } else {
                                    adjDir = -1;
                                }
                            }
                        } else if (adjustment.equals(ADJUST_LOWER)) {
                            c = 2;
                            if (c != 0) {
                            }
                        }
                    } else if (adjustment.equals(ADJUST_SAME)) {
                        c = VOLUME_CONTROL_MODE_SET;
                        if (c != 0) {
                        }
                    }
                    c = 65535;
                    if (c != 0) {
                    }
                }
                log(LOG_V, "Connecting to AudioService");
                IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
                if (audioService != null) {
                    if (mode != VOLUME_CONTROL_MODE_SET) {
                        flag = 0;
                    } else if (volIndex > audioService.getStreamMaxVolume(stream) || volIndex < audioService.getStreamMinVolume(stream)) {
                        cmd.showError(String.format("Error: invalid volume index %d for stream %d (should be in [%d..%d])", Integer.valueOf(volIndex), Integer.valueOf(stream), Integer.valueOf(audioService.getStreamMinVolume(stream)), Integer.valueOf(audioService.getStreamMaxVolume(stream))));
                        return;
                    } else {
                        flag = 0;
                    }
                    if (showUi) {
                        flag = VOLUME_CONTROL_MODE_SET;
                    }
                    String pack = cmd.getClass().getPackage().getName();
                    if (mode == VOLUME_CONTROL_MODE_SET) {
                        audioService.setStreamVolume(stream, volIndex, flag, pack);
                    } else if (mode == VOLUME_CONTROL_MODE_ADJUST) {
                        audioService.adjustStreamVolume(stream, adjDir, flag, pack);
                    }
                    if (doGet) {
                        log(LOG_V, "volume is " + audioService.getStreamVolume(stream) + " in range [" + audioService.getStreamMinVolume(stream) + ".." + audioService.getStreamMaxVolume(stream) + "]");
                        return;
                    }
                    return;
                }
                System.err.println("Error type 2");
                throw new AndroidException("Can't connect to audio service; is the system running?");
            }
        }
    }

    static void log(String code, String msg) {
        PrintStream printStream = System.out;
        printStream.println(code + " " + msg);
    }

    static String streamName(int stream) {
        try {
            return AudioSystem.STREAM_NAMES[stream];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "invalid stream";
        }
    }
}
