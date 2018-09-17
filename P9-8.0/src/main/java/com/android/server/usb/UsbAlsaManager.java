package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.media.HwMediaMonitorManager;
import android.media.IAudioService;
import android.media.IAudioService.Stub;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.Slog;
import com.android.internal.alsa.AlsaCardsParser;
import com.android.internal.alsa.AlsaCardsParser.AlsaCardRecord;
import com.android.internal.alsa.AlsaDevicesParser;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.audio.AudioService;
import com.android.server.lights.LightsManager;
import java.io.File;
import java.util.HashMap;
import libcore.io.IoUtils;

public final class UsbAlsaManager {
    private static final String ACTION_TYPEC_UPDATE = "huawei.intent.action.TYPEC_UPDATE";
    private static final String ALSA_DIRECTORY = "/dev/snd/";
    private static final boolean DEBUG;
    private static final int[] HUAWEI_TYPEC_PID = new int[]{1426, 14855, LightsManager.LIGHT_ID_SMARTBACKLIGHT, 37390};
    private static final int[] HUAWEI_TYPEC_VID = new int[]{1426, 4817, 48727, 9770};
    private static final String TAG = UsbAlsaManager.class.getSimpleName();
    private static final String USB_PERSISTENT_CONFIG = "persist.sys.usb.capture";
    private UsbAudioDevice mAccessoryAudioDevice = null;
    private final HashMap<String, AlsaDevice> mAlsaDevices = new HashMap();
    private final FileObserver mAlsaObserver = new FileObserver(ALSA_DIRECTORY, 768) {
        public void onEvent(int event, String path) {
            switch (event) {
                case 256:
                    UsbAlsaManager.this.alsaFileAdded(path);
                    return;
                case 512:
                    UsbAlsaManager.this.alsaFileRemoved(path);
                    return;
                default:
                    return;
            }
        }
    };
    private final HashMap<UsbDevice, UsbAudioDevice> mAudioDevices = new HashMap();
    private IAudioService mAudioService;
    private final AlsaCardsParser mCardsParser = new AlsaCardsParser();
    private final Context mContext;
    private final AlsaDevicesParser mDevicesParser = new AlsaDevicesParser();
    private final boolean mHasMidiFeature;
    private final HashMap<UsbDevice, UsbMidiDevice> mMidiDevices = new HashMap();
    private UsbMidiDevice mPeripheralMidiDevice = null;

    private final class AlsaDevice {
        public static final int TYPE_CAPTURE = 2;
        public static final int TYPE_MIDI = 3;
        public static final int TYPE_PLAYBACK = 1;
        public static final int TYPE_UNKNOWN = 0;
        public int mCard;
        public int mDevice;
        public int mType;

        public AlsaDevice(int type, int card, int device) {
            this.mType = type;
            this.mCard = card;
            this.mDevice = device;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof AlsaDevice)) {
                return false;
            }
            AlsaDevice other = (AlsaDevice) obj;
            if (this.mType == other.mType && this.mCard == other.mCard && this.mDevice == other.mDevice) {
                z = true;
            }
            return z;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AlsaDevice: [card: ").append(this.mCard);
            sb.append(", device: ").append(this.mDevice);
            sb.append(", type: ").append(this.mType);
            sb.append("]");
            return sb.toString();
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    UsbAlsaManager(Context context) {
        this.mContext = context;
        this.mHasMidiFeature = context.getPackageManager().hasSystemFeature("android.software.midi");
        this.mCardsParser.scan();
    }

    public void systemReady() {
        this.mAudioService = Stub.asInterface(ServiceManager.getService("audio"));
        this.mAlsaObserver.startWatching();
        File[] files = new File(ALSA_DIRECTORY).listFiles();
        if (files != null) {
            for (File name : files) {
                alsaFileAdded(name.getName());
            }
        }
    }

    private void notifyDeviceState(UsbAudioDevice audioDevice, boolean enabled) {
        if (DEBUG) {
            Slog.d(TAG, "notifyDeviceState " + enabled + " " + audioDevice);
        }
        if (this.mAudioService == null) {
            Slog.e(TAG, "no AudioService");
        } else if (Secure.getInt(this.mContext.getContentResolver(), "usb_audio_automatic_routing_disabled", 0) == 0) {
            int state = enabled ? 1 : 0;
            int alsaCard = audioDevice.mCard;
            int alsaDevice = audioDevice.mDevice;
            if (alsaCard < 0 || alsaDevice < 0) {
                Slog.e(TAG, "Invalid alsa card or device alsaCard: " + alsaCard + " alsaDevice: " + alsaDevice);
                return;
            }
            if (enabled && audioDevice.mHasCapture) {
                SystemProperties.set(USB_PERSISTENT_CONFIG, "true");
            } else {
                SystemProperties.set(USB_PERSISTENT_CONFIG, "false");
            }
            String address = AudioService.makeAlsaAddressString(alsaCard, alsaDevice);
            try {
                int device;
                if (audioDevice.mHasPlayback) {
                    if (audioDevice == this.mAccessoryAudioDevice) {
                        device = 8192;
                    } else {
                        device = 16384;
                    }
                    if (DEBUG) {
                        Slog.i(TAG, "pre-call device:0x" + Integer.toHexString(device) + " addr:" + address + " name:" + audioDevice.mDeviceName);
                    }
                    this.mAudioService.setWiredDeviceConnectionState(device, state, address, audioDevice.mDeviceName, TAG);
                }
                if (audioDevice.mHasCapture) {
                    if (audioDevice == this.mAccessoryAudioDevice) {
                        device = -2147481600;
                    } else {
                        device = -2147479552;
                    }
                    this.mAudioService.setWiredDeviceConnectionState(device, state, address, audioDevice.mDeviceName, TAG);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in setWiredDeviceConnectionState");
            }
        }
    }

    private AlsaDevice waitForAlsaDevice(int card, int device, int type) {
        if (DEBUG) {
            Slog.e(TAG, "waitForAlsaDevice(c:" + card + " d:" + device + ")");
        }
        AlsaDevice testDevice = new AlsaDevice(type, card, device);
        synchronized (this.mAlsaDevices) {
            long timeoutMs = SystemClock.elapsedRealtime() + 2500;
            while (!this.mAlsaDevices.values().contains(testDevice)) {
                long waitTimeMs = timeoutMs - SystemClock.elapsedRealtime();
                if (waitTimeMs > 0) {
                    try {
                        this.mAlsaDevices.wait(waitTimeMs);
                    } catch (InterruptedException e) {
                        Slog.d(TAG, "usb: InterruptedException while waiting for ALSA file.");
                    }
                }
                if (timeoutMs <= SystemClock.elapsedRealtime()) {
                    Slog.e(TAG, "waitForAlsaDevice failed for " + testDevice);
                    return null;
                }
            }
            return testDevice;
        }
    }

    private void alsaFileAdded(String name) {
        int type = 0;
        if (name.startsWith("pcmC")) {
            if (name.endsWith("p")) {
                type = 1;
            } else if (name.endsWith("c")) {
                type = 2;
            }
        } else if (name.startsWith("midiC")) {
            type = 3;
        }
        if (type != 0) {
            try {
                int c_index = name.indexOf(67);
                int d_index = name.indexOf(68);
                int end = name.length();
                if (type == 1 || type == 2) {
                    end--;
                }
                int card = Integer.parseInt(name.substring(c_index + 1, d_index));
                int device = Integer.parseInt(name.substring(d_index + 1, end));
                synchronized (this.mAlsaDevices) {
                    if (this.mAlsaDevices.get(name) == null) {
                        AlsaDevice alsaDevice = new AlsaDevice(type, card, device);
                        Slog.d(TAG, "Adding ALSA device " + alsaDevice);
                        this.mAlsaDevices.put(name, alsaDevice);
                        this.mAlsaDevices.notifyAll();
                        if (alsaDevice.mType == 1) {
                            HwMediaMonitorManager.writeBigData(916019301, 1);
                        } else if (alsaDevice.mType == 3) {
                            HwMediaMonitorManager.writeBigData(916019301, 0);
                        }
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "Could not parse ALSA file name " + name, e);
            }
        }
    }

    private void alsaFileRemoved(String path) {
        synchronized (this.mAlsaDevices) {
            AlsaDevice device = (AlsaDevice) this.mAlsaDevices.remove(path);
            if (device != null) {
                Slog.d(TAG, "ALSA device removed: " + device);
            }
        }
    }

    UsbAudioDevice selectAudioCard(int card) {
        if (DEBUG) {
            Slog.d(TAG, "selectAudioCard() card:" + card + " isCardUsb(): " + this.mCardsParser.isCardUsb(card));
        }
        if (!this.mCardsParser.isCardUsb(card)) {
            return null;
        }
        int i;
        this.mDevicesParser.scan();
        int device = this.mDevicesParser.getDefaultDeviceNum(card);
        boolean hasPlayback = this.mDevicesParser.hasPlaybackDevices(card);
        boolean hasCapture = this.mDevicesParser.hasCaptureDevices(card);
        if (DEBUG) {
            Slog.d(TAG, "usb: hasPlayback:" + hasPlayback + " hasCapture:" + hasCapture);
        }
        if (this.mCardsParser.isCardUsb(card)) {
            i = 2;
        } else {
            i = 1;
        }
        int deviceClass = i | Integer.MIN_VALUE;
        if (hasPlayback && waitForAlsaDevice(card, device, 1) == null) {
            return null;
        }
        if (hasCapture && waitForAlsaDevice(card, device, 2) == null) {
            return null;
        }
        UsbAudioDevice audioDevice = new UsbAudioDevice(card, device, hasPlayback, hasCapture, deviceClass);
        AlsaCardRecord cardRecord = this.mCardsParser.getCardRecordFor(card);
        audioDevice.mDeviceName = cardRecord.mCardName;
        audioDevice.mDeviceDescription = cardRecord.mCardDescription;
        notifyDeviceState(audioDevice, true);
        return audioDevice;
    }

    UsbAudioDevice selectDefaultDevice() {
        if (DEBUG) {
            Slog.d(TAG, "UsbAudioManager.selectDefaultDevice()");
        }
        return selectAudioCard(this.mCardsParser.getDefaultCard());
    }

    private void updateTypecHeadset(UsbDevice usbDevice) {
        if (usbDevice == null) {
            Slog.e(TAG, "the usbDevice is null");
            return;
        }
        if (isEnableTypecEarphoneUpgrade()) {
            int i = 0;
            while (i < HUAWEI_TYPEC_PID.length) {
                if (usbDevice.getProductId() == HUAWEI_TYPEC_PID[i] && usbDevice.getVendorId() == HUAWEI_TYPEC_VID[i]) {
                    Slog.i(TAG, "huawei typec is connected,productId is " + HUAWEI_TYPEC_PID[i] + ",vendorId is " + HUAWEI_TYPEC_VID[i]);
                    Intent intent = new Intent(ACTION_TYPEC_UPDATE);
                    intent.putExtra("VendorId", usbDevice.getVendorId());
                    intent.putExtra("ProductId", usbDevice.getProductId());
                    if (this.mContext != null) {
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.MANAGE_USB");
                    }
                }
                i++;
            }
        }
    }

    private static boolean isEnableTypecEarphoneUpgrade() {
        return SystemProperties.getBoolean("ro.config.enable_typec_earphone", false);
    }

    void usbDeviceAdded(UsbDevice usbDevice) {
        if (DEBUG) {
            Slog.d(TAG, "deviceAdded(): " + usbDevice.getManufacturerName() + " nm:" + usbDevice.getProductName());
        }
        updateTypecHeadset(usbDevice);
        boolean isAudioDevice = false;
        int interfaceCount = usbDevice.getInterfaceCount();
        int ntrfaceIndex = 0;
        while (!isAudioDevice && ntrfaceIndex < interfaceCount) {
            if (usbDevice.getInterface(ntrfaceIndex).getInterfaceClass() == 1) {
                isAudioDevice = true;
            }
            ntrfaceIndex++;
        }
        if (DEBUG) {
            Slog.d(TAG, "  isAudioDevice: " + isAudioDevice);
        }
        if (isAudioDevice) {
            if (notifyUsbDeviceAdded(usbDevice)) {
                Slog.i(TAG, "deviceAdded() 1- success");
            } else {
                Slog.w(TAG, "deviceAdded() failed first time, try again after 500ms.");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (notifyUsbDeviceAdded(usbDevice)) {
                    Slog.i(TAG, "deviceAdded() 2- success");
                } else {
                    Slog.e(TAG, "deviceAdded() 2- failed");
                }
            }
            if (DEBUG) {
                Slog.d(TAG, "deviceAdded() - done");
            }
        }
    }

    boolean notifyUsbDeviceAdded(UsbDevice usbDevice) {
        int addedCard = this.mCardsParser.getDefaultUsbCard();
        if (DEBUG) {
            Slog.d(TAG, "  mCardsParser.isCardUsb(" + addedCard + ") = " + this.mCardsParser.isCardUsb(addedCard));
        }
        if (!this.mCardsParser.isCardUsb(addedCard)) {
            return false;
        }
        UsbAudioDevice audioDevice = selectAudioCard(addedCard);
        if (audioDevice != null) {
            this.mAudioDevices.put(usbDevice, audioDevice);
            Slog.i(TAG, "USB Audio Device Added: " + audioDevice);
        }
        if (this.mDevicesParser.hasMIDIDevices(addedCard) && this.mHasMidiFeature) {
            AlsaDevice alsaDevice = waitForAlsaDevice(addedCard, this.mDevicesParser.getDefaultDeviceNum(addedCard), 3);
            if (alsaDevice != null) {
                String name;
                Bundle properties = new Bundle();
                String manufacturer = usbDevice.getManufacturerName();
                String product = usbDevice.getProductName();
                String version = usbDevice.getVersion();
                if (manufacturer == null || manufacturer.isEmpty()) {
                    name = product;
                } else if (product == null || product.isEmpty()) {
                    name = manufacturer;
                } else {
                    name = manufacturer + " " + product;
                }
                properties.putString("name", name);
                properties.putString("manufacturer", manufacturer);
                properties.putString("product", product);
                properties.putString("version", version);
                properties.putString("serial_number", usbDevice.getSerialNumber());
                properties.putInt("alsa_card", alsaDevice.mCard);
                properties.putInt("alsa_device", alsaDevice.mDevice);
                properties.putParcelable("usb_device", usbDevice);
                UsbMidiDevice usbMidiDevice = UsbMidiDevice.create(this.mContext, properties, alsaDevice.mCard, alsaDevice.mDevice);
                if (usbMidiDevice != null) {
                    this.mMidiDevices.put(usbDevice, usbMidiDevice);
                }
            }
        }
        return true;
    }

    void usbDeviceRemoved(UsbDevice usbDevice) {
        if (DEBUG) {
            Slog.d(TAG, "deviceRemoved(): " + usbDevice.getManufacturerName() + " " + usbDevice.getProductName());
        }
        UsbAudioDevice audioDevice = (UsbAudioDevice) this.mAudioDevices.remove(usbDevice);
        Slog.i(TAG, "USB Audio Device Removed: " + audioDevice);
        if (audioDevice != null && (audioDevice.mHasPlayback || audioDevice.mHasCapture)) {
            notifyDeviceState(audioDevice, false);
            selectDefaultDevice();
        }
        UsbMidiDevice usbMidiDevice = (UsbMidiDevice) this.mMidiDevices.remove(usbDevice);
        if (usbMidiDevice != null) {
            IoUtils.closeQuietly(usbMidiDevice);
        }
    }

    void setAccessoryAudioState(boolean enabled, int card, int device) {
        if (DEBUG) {
            Slog.d(TAG, "setAccessoryAudioState " + enabled + " " + card + " " + device);
        }
        if (enabled) {
            this.mAccessoryAudioDevice = new UsbAudioDevice(card, device, true, false, 2);
            notifyDeviceState(this.mAccessoryAudioDevice, true);
        } else if (this.mAccessoryAudioDevice != null) {
            notifyDeviceState(this.mAccessoryAudioDevice, false);
            this.mAccessoryAudioDevice = null;
        }
    }

    void setPeripheralMidiState(boolean enabled, int card, int device) {
        if (this.mHasMidiFeature) {
            if (enabled && this.mPeripheralMidiDevice == null) {
                Bundle properties = new Bundle();
                Resources r = this.mContext.getResources();
                properties.putString("name", r.getString(17041146));
                properties.putString("manufacturer", r.getString(17041145));
                properties.putString("product", r.getString(17041147));
                properties.putInt("alsa_card", card);
                properties.putInt("alsa_device", device);
                this.mPeripheralMidiDevice = UsbMidiDevice.create(this.mContext, properties, card, device);
            } else if (!(enabled || this.mPeripheralMidiDevice == null)) {
                IoUtils.closeQuietly(this.mPeripheralMidiDevice);
                this.mPeripheralMidiDevice = null;
            }
        }
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("USB Audio Devices:");
        for (UsbDevice device : this.mAudioDevices.keySet()) {
            pw.println("  " + device.getDeviceName() + ": " + this.mAudioDevices.get(device));
        }
        pw.println("USB MIDI Devices:");
        for (UsbDevice device2 : this.mMidiDevices.keySet()) {
            pw.println("  " + device2.getDeviceName() + ": " + this.mMidiDevices.get(device2));
        }
    }
}
