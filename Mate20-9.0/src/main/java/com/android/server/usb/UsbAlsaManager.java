package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.media.IAudioService;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.alsa.AlsaCardsParser;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.usb.descriptors.UsbDescriptorParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import libcore.io.IoUtils;

public final class UsbAlsaManager {
    private static final String ACTION_TYPEC_UPDATE = "huawei.intent.action.TYPEC_UPDATE";
    private static final String ALSA_DIRECTORY = "/dev/snd/";
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int[] HUAWEI_TYPEC_PID = {1426, 14855, 257, 37390};
    private static final int[] HUAWEI_TYPEC_VID = {1426, 4817, 48727, 9770};
    private static final String TAG = UsbAlsaManager.class.getSimpleName();
    private final ArrayList<UsbAlsaDevice> mAlsaDevices = new ArrayList<>();
    private IAudioService mAudioService;
    private final AlsaCardsParser mCardsParser = new AlsaCardsParser();
    private final Context mContext;
    private final boolean mHasMidiFeature;
    private final HashMap<String, UsbMidiDevice> mMidiDevices = new HashMap<>();
    private UsbMidiDevice mPeripheralMidiDevice = null;
    private UsbAlsaDevice mSelectedDevice;

    UsbAlsaManager(Context context) {
        this.mContext = context;
        this.mHasMidiFeature = context.getPackageManager().hasSystemFeature("android.software.midi");
    }

    public void systemReady() {
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    private synchronized void selectAlsaDevice(UsbAlsaDevice alsaDevice) {
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "selectAlsaDevice " + alsaDevice);
        }
        if (this.mSelectedDevice != null) {
            deselectAlsaDevice();
        }
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "usb_audio_automatic_routing_disabled", 0) == 0) {
            this.mSelectedDevice = alsaDevice;
            alsaDevice.start();
        }
    }

    private synchronized void deselectAlsaDevice() {
        if (this.mSelectedDevice != null) {
            this.mSelectedDevice.stop();
            this.mSelectedDevice = null;
        }
    }

    private int getAlsaDeviceListIndexFor(String deviceAddress) {
        for (int index = 0; index < this.mAlsaDevices.size(); index++) {
            if (this.mAlsaDevices.get(index).getDeviceAddress().equals(deviceAddress)) {
                return index;
            }
        }
        return -1;
    }

    private UsbAlsaDevice removeAlsaDeviceFromList(String deviceAddress) {
        int index = getAlsaDeviceListIndexFor(deviceAddress);
        if (index > -1) {
            return this.mAlsaDevices.remove(index);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public UsbAlsaDevice selectDefaultDevice() {
        if (DEBUG) {
            Slog.d(TAG, "UsbAudioManager.selectDefaultDevice()");
        }
        if (this.mAlsaDevices.size() <= 0) {
            return null;
        }
        UsbAlsaDevice alsaDevice = this.mAlsaDevices.get(0);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "  alsaDevice:" + alsaDevice);
        }
        if (alsaDevice != null) {
            selectAlsaDevice(alsaDevice);
        }
        return alsaDevice;
    }

    private void updateTypecHeadset(UsbDevice usbDevice) {
        if (usbDevice == null) {
            Slog.e(TAG, "the usbDevice is null");
            return;
        }
        if (isEnableTypecEarphoneUpgrade()) {
            for (int i = 0; i < HUAWEI_TYPEC_PID.length; i++) {
                if (usbDevice.getProductId() == HUAWEI_TYPEC_PID[i] && usbDevice.getVendorId() == HUAWEI_TYPEC_VID[i]) {
                    String str = TAG;
                    Slog.i(str, "huawei typec is connected,productId is " + HUAWEI_TYPEC_PID[i] + ",vendorId is " + HUAWEI_TYPEC_VID[i]);
                    Intent intent = new Intent(ACTION_TYPEC_UPDATE);
                    intent.putExtra("VendorId", usbDevice.getVendorId());
                    intent.putExtra("ProductId", usbDevice.getProductId());
                    if (this.mContext != null) {
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.MANAGE_USB");
                    }
                }
            }
        }
    }

    private static boolean isEnableTypecEarphoneUpgrade() {
        return SystemProperties.getBoolean("ro.config.enable_typec_earphone", false);
    }

    /* access modifiers changed from: package-private */
    public void usbDeviceAdded(String deviceAddress, UsbDevice usbDevice, UsbDescriptorParser parser) {
        String name;
        String str = deviceAddress;
        UsbDevice usbDevice2 = usbDevice;
        if (DEBUG) {
            Slog.d(TAG, "usbDeviceAdded(): " + usbDevice.getManufacturerName() + " nm:" + usbDevice.getProductName());
        }
        updateTypecHeadset(usbDevice2);
        this.mCardsParser.scan();
        AlsaCardsParser.AlsaCardRecord cardRec = this.mCardsParser.findCardNumFor(str);
        if (cardRec != null) {
            boolean hasInput = parser.hasInput();
            boolean hasOutput = parser.hasOutput();
            if (DEBUG) {
                Slog.d(TAG, "hasInput: " + hasInput + " hasOutput:" + hasOutput);
            }
            if (hasInput || hasOutput) {
                boolean isInputHeadset = parser.isInputHeadset();
                boolean isOutputHeadset = parser.isOutputHeadset();
                if (this.mAudioService == null) {
                    Slog.e(TAG, "no AudioService");
                    return;
                }
                UsbAlsaDevice alsaDevice = new UsbAlsaDevice(this.mAudioService, cardRec.getCardNum(), 0, str, hasOutput, hasInput, isInputHeadset, isOutputHeadset);
                alsaDevice.setDeviceNameAndDescription(cardRec.getCardName(), cardRec.getCardDescription());
                this.mAlsaDevices.add(0, alsaDevice);
                selectAlsaDevice(alsaDevice);
            }
            boolean hasMidi = parser.hasMIDIInterface();
            if (DEBUG) {
                Slog.d(TAG, "hasMidi: " + hasMidi + " mHasMidiFeature:" + this.mHasMidiFeature);
            }
            if (hasMidi && this.mHasMidiFeature) {
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
                properties.putString(com.android.server.pm.Settings.ATTR_NAME, name);
                properties.putString("manufacturer", manufacturer);
                properties.putString("product", product);
                properties.putString("version", version);
                properties.putString("serial_number", usbDevice.getSerialNumber());
                properties.putInt("alsa_card", cardRec.getCardNum());
                properties.putInt("alsa_device", 0);
                properties.putParcelable("usb_device", usbDevice2);
                UsbMidiDevice usbMidiDevice = UsbMidiDevice.create(this.mContext, properties, cardRec.getCardNum(), 0);
                if (usbMidiDevice != null) {
                    this.mMidiDevices.put(str, usbMidiDevice);
                }
            }
            if (DEBUG != 0) {
                Slog.d(TAG, "deviceAdded() - done");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void usbDeviceRemoved(String deviceAddress) {
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "deviceRemoved(" + deviceAddress + ")");
        }
        UsbAlsaDevice alsaDevice = removeAlsaDeviceFromList(deviceAddress);
        String str2 = TAG;
        Slog.i(str2, "USB Audio Device Removed: " + alsaDevice);
        if (alsaDevice != null && alsaDevice == this.mSelectedDevice) {
            deselectAlsaDevice();
            selectDefaultDevice();
        }
        UsbMidiDevice usbMidiDevice = this.mMidiDevices.remove(deviceAddress);
        if (usbMidiDevice != null) {
            String str3 = TAG;
            Slog.i(str3, "USB MIDI Device Removed: " + usbMidiDevice);
            IoUtils.closeQuietly(usbMidiDevice);
        }
    }

    /* access modifiers changed from: package-private */
    public void setPeripheralMidiState(boolean enabled, int card, int device) {
        if (this.mHasMidiFeature) {
            if (enabled && this.mPeripheralMidiDevice == null) {
                Bundle properties = new Bundle();
                Resources r = this.mContext.getResources();
                properties.putString(com.android.server.pm.Settings.ATTR_NAME, r.getString(17041281));
                properties.putString("manufacturer", r.getString(17041280));
                properties.putString("product", r.getString(17041282));
                properties.putInt("alsa_card", card);
                properties.putInt("alsa_device", device);
                this.mPeripheralMidiDevice = UsbMidiDevice.create(this.mContext, properties, card, device);
            } else if (!enabled && this.mPeripheralMidiDevice != null) {
                IoUtils.closeQuietly(this.mPeripheralMidiDevice);
                this.mPeripheralMidiDevice = null;
            }
        }
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write("cards_parser", 1120986464257L, this.mCardsParser.getScanStatus());
        Iterator<UsbAlsaDevice> it = this.mAlsaDevices.iterator();
        while (it.hasNext()) {
            it.next().dump(dump, "alsa_devices", 2246267895810L);
        }
        for (String deviceAddr : this.mMidiDevices.keySet()) {
            this.mMidiDevices.get(deviceAddr).dump(deviceAddr, dump, "midi_devices", 2246267895811L);
        }
        dump.end(token);
    }
}
