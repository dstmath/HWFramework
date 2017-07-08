package com.android.server.usb;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.media.HwMediaMonitorManager;
import android.media.IAudioService;
import android.media.IAudioService.Stub;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.internal.alsa.AlsaCardsParser;
import com.android.internal.alsa.AlsaCardsParser.AlsaCardRecord;
import com.android.internal.alsa.AlsaDevicesParser;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.audio.AudioService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import libcore.io.IoUtils;

public final class UsbAlsaManager {
    private static final String ALSA_DIRECTORY = "/dev/snd/";
    private static final boolean DEBUG = false;
    private static final String TAG = null;
    private UsbAudioDevice mAccessoryAudioDevice;
    private final HashMap<String, AlsaDevice> mAlsaDevices;
    private final FileObserver mAlsaObserver;
    private final HashMap<UsbDevice, UsbAudioDevice> mAudioDevices;
    private IAudioService mAudioService;
    private final AlsaCardsParser mCardsParser;
    private final Context mContext;
    private final AlsaDevicesParser mDevicesParser;
    private final boolean mHasMidiFeature;
    private final HashMap<UsbDevice, UsbMidiDevice> mMidiDevices;
    private UsbMidiDevice mPeripheralMidiDevice;

    /* renamed from: com.android.server.usb.UsbAlsaManager.1 */
    class AnonymousClass1 extends FileObserver {
        AnonymousClass1(String $anonymous0, int $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case DumpState.DUMP_SHARED_USERS /*256*/:
                    UsbAlsaManager.this.alsaFileAdded(path);
                case DumpState.DUMP_MESSAGES /*512*/:
                    UsbAlsaManager.this.alsaFileRemoved(path);
                default:
            }
        }
    }

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.usb.UsbAlsaManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.usb.UsbAlsaManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbAlsaManager.<clinit>():void");
    }

    UsbAlsaManager(Context context) {
        this.mCardsParser = new AlsaCardsParser();
        this.mDevicesParser = new AlsaDevicesParser();
        this.mAudioDevices = new HashMap();
        this.mMidiDevices = new HashMap();
        this.mAlsaDevices = new HashMap();
        this.mAccessoryAudioDevice = null;
        this.mPeripheralMidiDevice = null;
        this.mAlsaObserver = new AnonymousClass1(ALSA_DIRECTORY, 768);
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
            String address = AudioService.makeAlsaAddressString(alsaCard, alsaDevice);
            try {
                int device;
                if (audioDevice.mHasPlayback) {
                    if (audioDevice == this.mAccessoryAudioDevice) {
                        device = DumpState.DUMP_PREFERRED_XML;
                    } else {
                        device = DumpState.DUMP_KEYSETS;
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
        AlsaDevice testDevice = new AlsaDevice(type, card, device);
        synchronized (this.mAlsaDevices) {
            long timeout = SystemClock.elapsedRealtime() + 2500;
            do {
                if (this.mAlsaDevices.values().contains(testDevice)) {
                    return testDevice;
                }
                long waitTime = timeout - SystemClock.elapsedRealtime();
                if (waitTime > 0) {
                    try {
                        this.mAlsaDevices.wait(waitTime);
                    } catch (InterruptedException e) {
                        Slog.d(TAG, "usb: InterruptedException while waiting for ALSA file.");
                    }
                }
            } while (timeout > SystemClock.elapsedRealtime());
            Slog.e(TAG, "waitForAlsaDevice failed for " + testDevice);
            return null;
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
                            HwMediaMonitorManager.writeMediaBigData(Process.myPid(), 20003, "UsbAlsaManager");
                        } else if (alsaDevice.mType == 3) {
                            HwMediaMonitorManager.writeMediaBigData(Process.myPid(), 20004, "UsbAlsaManager");
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
        if (!this.mCardsParser.isCardUsb(card)) {
            return null;
        }
        int i;
        this.mDevicesParser.scan();
        int device = this.mDevicesParser.getDefaultDeviceNum(card);
        boolean hasPlayback = this.mDevicesParser.hasPlaybackDevices(card);
        boolean hasCapture = this.mDevicesParser.hasCaptureDevices(card);
        if (this.mCardsParser.isCardUsb(card)) {
            i = 2;
        } else {
            i = 1;
        }
        int deviceClass = i | UsbAudioDevice.kAudioDeviceMeta_Alsa;
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
        return selectAudioCard(this.mCardsParser.getDefaultCard());
    }

    void usbDeviceAdded(UsbDevice usbDevice) {
        boolean isAudioDevice = false;
        int interfaceCount = usbDevice.getInterfaceCount();
        int ntrfaceIndex = 0;
        while (!isAudioDevice && ntrfaceIndex < interfaceCount) {
            if (usbDevice.getInterface(ntrfaceIndex).getInterfaceClass() == 1) {
                isAudioDevice = true;
            }
            ntrfaceIndex++;
        }
        if (isAudioDevice) {
            int addedCard = this.mCardsParser.getDefaultUsbCard();
            if (this.mCardsParser.isCardUsb(addedCard)) {
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
                        String str = "alsa_card";
                        properties.putInt(r18, alsaDevice.mCard);
                        str = "alsa_device";
                        properties.putInt(r18, alsaDevice.mDevice);
                        properties.putParcelable("usb_device", usbDevice);
                        UsbMidiDevice usbMidiDevice = UsbMidiDevice.create(this.mContext, properties, alsaDevice.mCard, alsaDevice.mDevice);
                        if (usbMidiDevice != null) {
                            this.mMidiDevices.put(usbDevice, usbMidiDevice);
                        }
                    }
                }
            }
        }
    }

    void usbDeviceRemoved(UsbDevice usbDevice) {
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
                properties.putString("name", r.getString(17040825));
                properties.putString("manufacturer", r.getString(17040826));
                properties.putString("product", r.getString(17040827));
                properties.putInt("alsa_card", card);
                properties.putInt("alsa_device", device);
                this.mPeripheralMidiDevice = UsbMidiDevice.create(this.mContext, properties, card, device);
            } else if (!(enabled || this.mPeripheralMidiDevice == null)) {
                IoUtils.closeQuietly(this.mPeripheralMidiDevice);
                this.mPeripheralMidiDevice = null;
            }
        }
    }

    public ArrayList<UsbAudioDevice> getConnectedDevices() {
        ArrayList<UsbAudioDevice> devices = new ArrayList(this.mAudioDevices.size());
        for (Entry<UsbDevice, UsbAudioDevice> entry : this.mAudioDevices.entrySet()) {
            devices.add((UsbAudioDevice) entry.getValue());
        }
        return devices;
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

    public void logDevicesList(String title) {
    }

    public void logDevices(String title) {
    }
}
