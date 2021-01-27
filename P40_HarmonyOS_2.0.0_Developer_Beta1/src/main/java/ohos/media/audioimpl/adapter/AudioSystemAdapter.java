package ohos.media.audioimpl.adapter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioDevicePort;
import android.media.AudioPort;
import android.media.AudioSystem;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import ohos.app.Context;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.net.Uri;

public class AudioSystemAdapter {
    private static final AudioStreamInfo.EncodingFormat[] ENCODING_MATCH_TABLE = {AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_DEFAULT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_8BIT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_FLOAT, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_MP3};
    private static final int ERROR_SESSION_ID = 0;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioSystemAdapter.class);
    private static final int NOW_TIME_STATUS = 0;

    public static AudioDeviceDescriptor[] getDevices(AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        int i;
        List<AudioDevicePort> audioDevicePorts = getAudioDevicePorts();
        if (audioDevicePorts.isEmpty()) {
            return new AudioDeviceDescriptor[0];
        }
        AudioDeviceDescriptor.DeviceRole deviceRole = AudioDeviceDescriptor.DeviceRole.INPUT_DEVICE;
        if ((deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.OUTPUT_DEVICES_FLAG.getValue()) != 0) {
            i = 2;
            deviceRole = AudioDeviceDescriptor.DeviceRole.OUTPUT_DEVICE;
        } else {
            i = 0;
        }
        if ((deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.INPUT_DEVICES_FLAG.getValue()) != 0) {
            deviceRole = AudioDeviceDescriptor.DeviceRole.INPUT_DEVICE;
            i = 1;
        }
        ArrayList arrayList = new ArrayList();
        int size = audioDevicePorts.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (audioDevicePorts.get(i2).role() == i) {
                arrayList.add(audioDevicePorts.get(i2));
            }
        }
        return convertToDescriptor(arrayList, deviceRole);
    }

    private static AudioDeviceDescriptor[] convertToDescriptor(List<AudioDevicePort> list, AudioDeviceDescriptor.DeviceRole deviceRole) {
        int size = list.size();
        AudioDeviceDescriptor[] audioDeviceDescriptorArr = new AudioDeviceDescriptor[size];
        for (int i = 0; i < size; i++) {
            AudioDevicePort audioDevicePort = list.get(i);
            audioDeviceDescriptorArr[i] = new AudioDeviceDescriptor(audioDevicePort.id(), audioDevicePort.name().length() != 0 ? audioDevicePort.name() : Build.MODEL, audioDevicePort.address(), AudioDeviceDescriptor.DeviceType.valueOf(AudioDeviceInfo.convertInternalDeviceToDeviceType(audioDevicePort.type())), deviceRole, audioDevicePort.samplingRates(), audioDevicePort.channelMasks(), audioDevicePort.channelIndexMasks(), filterEncodingFormats(audioDevicePort.formats()));
        }
        return audioDeviceDescriptorArr;
    }

    private static List<AudioStreamInfo.EncodingFormat> filterEncodingFormats(int[] iArr) {
        ArrayList arrayList = new ArrayList();
        if (iArr == null) {
            return arrayList;
        }
        for (int i : iArr) {
            if ((i > 1 && i <= 4) || i == 9) {
                arrayList.add(ENCODING_MATCH_TABLE[i]);
            }
        }
        return arrayList;
    }

    private static List<AudioDevicePort> getAudioDevicePorts() {
        List<AudioPort> audioPorts = getAudioPorts();
        ArrayList arrayList = new ArrayList();
        Iterator<AudioPort> it = audioPorts.iterator();
        while (it.hasNext()) {
            AudioDevicePort audioDevicePort = (AudioPort) it.next();
            if (audioDevicePort instanceof AudioDevicePort) {
                arrayList.add(audioDevicePort);
            }
        }
        return arrayList;
    }

    private static List<AudioPort> getAudioPorts() {
        ArrayList arrayList = new ArrayList();
        int listAudioPorts = AudioSystem.listAudioPorts(arrayList, new int[1]);
        if (listAudioPorts == 0) {
            return arrayList;
        }
        LOGGER.error("list audio ports failed, status=%{public}d", Integer.valueOf(listAudioPorts));
        return new ArrayList();
    }

    public static boolean isWiredHeadsetOn() {
        return getAudioPorts().stream().anyMatch($$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY.INSTANCE);
    }

    /* access modifiers changed from: private */
    public static boolean isWiredHeadsetPort(AudioPort audioPort) {
        if (audioPort instanceof AudioDevicePort) {
            AudioDevicePort audioDevicePort = (AudioDevicePort) audioPort;
            List asList = Arrays.asList(AudioDeviceDescriptor.DeviceType.WIRED_HEADPHONES, AudioDeviceDescriptor.DeviceType.WIRED_HEADPHONES, AudioDeviceDescriptor.DeviceType.USB_HEADSET);
            AudioDeviceDescriptor.DeviceType valueOf = AudioDeviceDescriptor.DeviceType.valueOf(AudioDeviceInfo.convertInternalDeviceToDeviceType(audioDevicePort.type()));
            if (audioDevicePort.role() == 2 && asList.contains(valueOf)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkDeviceType(AudioDevicePort audioDevicePort) {
        return AudioDeviceInfo.convertInternalDeviceToDeviceType(audioDevicePort.type()) != 0;
    }

    private static boolean checkDeviceFlag(AudioDevicePort audioDevicePort, AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        return isSinkPort(audioDevicePort, deviceFlag) || isSourcePort(audioDevicePort, deviceFlag);
    }

    private static boolean isSinkPort(AudioDevicePort audioDevicePort, AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        return audioDevicePort.role() == 2 && (deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.OUTPUT_DEVICES_FLAG.getValue()) != 0;
    }

    private static boolean isSourcePort(AudioDevicePort audioDevicePort, AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        if (audioDevicePort.role() != 1 || (deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.INPUT_DEVICES_FLAG.getValue()) == 0) {
            return false;
        }
        return true;
    }

    public static Uri getRingerUri(Context context, int i) {
        if (!(context.getHostContext() instanceof android.content.Context)) {
            LOGGER.error("[getRingerUri] context transform failed.", new Object[0]);
            return null;
        }
        android.net.Uri actualDefaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri((android.content.Context) context.getHostContext(), i);
        if (actualDefaultRingtoneUri != null) {
            return Uri.parse(actualDefaultRingtoneUri.toString());
        }
        LOGGER.error("[getRingerUri] uri is null.", new Object[0]);
        return null;
    }

    public static void setRingerUri(Context context, int i, Uri uri) {
        if (!(context.getHostContext() instanceof android.content.Context)) {
            LOGGER.error("[setRingerUri] context transform failed.", new Object[0]);
            return;
        }
        String encodedPath = uri.getEncodedPath();
        if (encodedPath != null) {
            try {
                if (encodedPath != new File(encodedPath).getCanonicalPath()) {
                    LOGGER.error("[setRingerUri] path is not equal to canonicalPath.", new Object[0]);
                    return;
                }
            } catch (IOException unused) {
                LOGGER.error("[setRingerUri] path is invalid.", new Object[0]);
                return;
            }
        }
        RingtoneManager.setActualDefaultRingtoneUri((android.content.Context) context.getHostContext(), i, new Uri.Builder().scheme(uri.getScheme()).authority(uri.getEncodedAuthority()).path(encodedPath).query(uri.getEncodedQuery()).fragment(uri.getEncodedFragment()).build());
    }

    public static int getMasterOutputSampleRate() {
        return AudioSystem.getPrimaryOutputSamplingRate();
    }

    public static int getMasterOutputFrameCount() {
        return AudioSystem.getPrimaryOutputFrameCount();
    }

    public static boolean isStreamActive(int i) {
        return AudioSystem.isStreamActive(i, 0);
    }

    public static int makeSessionId() {
        int newAudioSessionId = AudioSystem.newAudioSessionId();
        if (newAudioSessionId > 0) {
            return newAudioSessionId;
        }
        LOGGER.error("[makeSessionId] Failure to generate a new audio session ID.", new Object[0]);
        return 0;
    }

    public static void saveIntToSettings(Context context, String str, int i) {
        android.content.Context isValidContext = isValidContext(context);
        if (isValidContext == null) {
            LOGGER.error("saveIntToSettings error, context is invalid", new Object[0]);
        } else {
            Settings.Global.putInt(isValidContext.getContentResolver(), str, i);
        }
    }

    public static int getIntFromSettings(Context context, String str, int i) {
        android.content.Context isValidContext = isValidContext(context);
        if (isValidContext != null) {
            return Settings.Global.getInt(isValidContext.getContentResolver(), str, i);
        }
        LOGGER.error("getIntFromSettings error, context is invalid", new Object[0]);
        return i;
    }

    private static boolean hasSystemPermission(android.content.Context context) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            LOGGER.error("hasSystemPermission error, can not get package manager", new Object[0]);
            return false;
        }
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo == null || (applicationInfo.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            LOGGER.error("hasSystemPermission error, name not found", new Object[0]);
        }
    }

    private static android.content.Context isValidContext(Context context) {
        if (context == null) {
            LOGGER.error("isValidContext error, context is null.", new Object[0]);
            return null;
        } else if (!(context.getHostContext() instanceof android.content.Context)) {
            LOGGER.error("isValidContext context transform failed.", new Object[0]);
            return null;
        } else {
            android.content.Context context2 = (android.content.Context) context.getHostContext();
            if (hasSystemPermission(context2)) {
                return context2;
            }
            LOGGER.error("isValidContext error, application has not permission.", new Object[0]);
            return null;
        }
    }
}
