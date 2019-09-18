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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002d, code lost:
        if (r9.equals("--show") != false) goto L_0x004f;
     */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0154  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0158  */
    public static void run(BaseCommand cmd) throws Exception {
        char c;
        BaseCommand baseCommand = cmd;
        int stream = 3;
        int volIndex = 5;
        int mode = 0;
        int adjDir = VOLUME_CONTROL_MODE_SET;
        boolean showUi = false;
        boolean doGet = false;
        String adjustment = null;
        while (true) {
            String nextOption = cmd.nextOption();
            String option = nextOption;
            int i = 0;
            if (nextOption != null) {
                switch (option.hashCode()) {
                    case 42995463:
                        if (option.equals("--adj")) {
                            i = 4;
                            break;
                        }
                    case 43001270:
                        if (option.equals("--get")) {
                            i = VOLUME_CONTROL_MODE_SET;
                            break;
                        }
                    case 43012802:
                        if (option.equals("--set")) {
                            i = 3;
                            break;
                        }
                    case 1333399709:
                        break;
                    case 1508023584:
                        if (option.equals("--stream")) {
                            i = VOLUME_CONTROL_MODE_ADJUST;
                            break;
                        }
                    default:
                        i = -1;
                        break;
                }
                switch (i) {
                    case 0:
                        showUi = true;
                        break;
                    case VOLUME_CONTROL_MODE_SET /*1*/:
                        doGet = true;
                        log(LOG_V, "will get volume");
                        break;
                    case VOLUME_CONTROL_MODE_ADJUST /*2*/:
                        stream = Integer.decode(cmd.nextArgRequired()).intValue();
                        log(LOG_V, "will control stream=" + stream + " (" + streamName(stream) + ")");
                        break;
                    case 3:
                        volIndex = Integer.decode(cmd.nextArgRequired()).intValue();
                        mode = VOLUME_CONTROL_MODE_SET;
                        log(LOG_V, "will set volume to index=" + volIndex);
                        break;
                    case 4:
                        mode = VOLUME_CONTROL_MODE_ADJUST;
                        adjustment = cmd.nextArgRequired();
                        log(LOG_V, "will adjust volume");
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown argument " + option);
                }
            } else {
                if (mode == VOLUME_CONTROL_MODE_ADJUST) {
                    if (adjustment != null) {
                        int hashCode = adjustment.hashCode();
                        if (hashCode == 3522662) {
                            if (adjustment.equals(ADJUST_SAME)) {
                                c = VOLUME_CONTROL_MODE_SET;
                                switch (c) {
                                    case 0:
                                        break;
                                    case VOLUME_CONTROL_MODE_SET /*1*/:
                                        break;
                                    case VOLUME_CONTROL_MODE_ADJUST /*2*/:
                                        break;
                                }
                            }
                        } else if (hashCode == 103164673) {
                            if (adjustment.equals(ADJUST_LOWER)) {
                                c = VOLUME_CONTROL_MODE_ADJUST;
                                switch (c) {
                                    case 0:
                                        break;
                                    case VOLUME_CONTROL_MODE_SET /*1*/:
                                        break;
                                    case VOLUME_CONTROL_MODE_ADJUST /*2*/:
                                        break;
                                }
                            }
                        } else if (hashCode == 108275692 && adjustment.equals(ADJUST_RAISE)) {
                            c = 0;
                            switch (c) {
                                case 0:
                                    adjDir = VOLUME_CONTROL_MODE_SET;
                                    break;
                                case VOLUME_CONTROL_MODE_SET /*1*/:
                                    adjDir = 0;
                                    break;
                                case VOLUME_CONTROL_MODE_ADJUST /*2*/:
                                    adjDir = -1;
                                    break;
                                default:
                                    baseCommand.showError("Error: no valid volume adjustment, was " + adjustment + ", expected " + ADJUST_LOWER + "|" + ADJUST_SAME + "|" + ADJUST_RAISE);
                                    return;
                            }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                                break;
                            case VOLUME_CONTROL_MODE_SET /*1*/:
                                break;
                            case VOLUME_CONTROL_MODE_ADJUST /*2*/:
                                break;
                        }
                    } else {
                        baseCommand.showError("Error: no valid volume adjustment (null)");
                        return;
                    }
                }
                log(LOG_V, "Connecting to AudioService");
                IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
                if (audioService == null) {
                    System.err.println("Error type 2");
                    throw new AndroidException("Can't connect to audio service; is the system running?");
                } else if (mode != VOLUME_CONTROL_MODE_SET || (volIndex <= audioService.getStreamMaxVolume(stream) && volIndex >= audioService.getStreamMinVolume(stream))) {
                    if (showUi) {
                        i = VOLUME_CONTROL_MODE_SET;
                    }
                    int flag = i;
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
                    Object[] objArr = new Object[4];
                    objArr[0] = Integer.valueOf(volIndex);
                    objArr[VOLUME_CONTROL_MODE_SET] = Integer.valueOf(stream);
                    objArr[VOLUME_CONTROL_MODE_ADJUST] = Integer.valueOf(audioService.getStreamMinVolume(stream));
                    objArr[3] = Integer.valueOf(audioService.getStreamMaxVolume(stream));
                    baseCommand.showError(String.format("Error: invalid volume index %d for stream %d (should be in [%d..%d])", objArr));
                    return;
                }
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
