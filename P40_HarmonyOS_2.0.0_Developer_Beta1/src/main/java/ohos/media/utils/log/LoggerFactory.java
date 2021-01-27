package ohos.media.utils.log;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.system.Parameters;

public class LoggerFactory {
    private static final int AUDIO_DOMAIN_ID = 218114822;
    private static final int CAMERA_DOMAIN_ID = 218114818;
    private static final int DATA_TUNNEL_DOMAIN_ID = 218114820;
    private static final int IMAGE_DOMAIN_ID = 218114821;
    private static final int MEDIA_DOMAIN_ID = 218114817;
    private static final int PLUGIN_DOMAIN_ID = 218114819;
    private static final int RADIO_DOMAIN_ID = 218114823;

    public static Logger getAudioLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, AUDIO_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getMediaLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, MEDIA_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getCameraLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, CAMERA_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getPluginLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, PLUGIN_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getDataTunnelLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, DATA_TUNNEL_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getImageLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, IMAGE_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    public static Logger getRadioLogger(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return new Logger(new HiLogLabel(3, RADIO_DOMAIN_ID, simpleName), getLoggerLevel(simpleName));
    }

    private static int getLoggerLevel(String str) {
        if (!isLoggerEnable()) {
            return Integer.MAX_VALUE;
        }
        if (isLoggable(str, 3)) {
            return 3;
        }
        if (isLoggable(str, 4)) {
            return 4;
        }
        if (isLoggable(str, 5)) {
            return 5;
        }
        if (isLoggable(str, 6)) {
            return 6;
        }
        if (isLoggable(str, 7)) {
            return 7;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isLoggerEnable() {
        return Parameters.getBoolean("sys.multimedia.logger.on", true);
    }

    private static boolean isLoggable(String str, int i) {
        return HiLog.isLoggable(3, str, i);
    }
}
