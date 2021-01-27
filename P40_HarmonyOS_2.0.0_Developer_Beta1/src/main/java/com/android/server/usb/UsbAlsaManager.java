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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;

public final class UsbAlsaManager {
    private static final String ACTION_TYPEC_UPDATE = "huawei.intent.action.TYPEC_UPDATE";
    private static final String ALSA_DIRECTORY = "/dev/snd/";
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int[] HUAWEI_TYPEC_PID = {1426, 14855, 257, 37390};
    private static final int[] HUAWEI_TYPEC_VID = {1426, 4817, 48727, 9770};
    private static final String TAG = UsbAlsaManager.class.getSimpleName();
    private static final int USB_BLACKLIST_INPUT = 2;
    private static final int USB_BLACKLIST_OUTPUT = 1;
    private static final int USB_PRODUCTID_PS4CONTROLLER_ZCT1 = 1476;
    private static final int USB_PRODUCTID_PS4CONTROLLER_ZCT2 = 2508;
    private static final int USB_VENDORID_SONY = 1356;
    private static final boolean mIsSingleMode = true;
    static final List<BlackListEntry> sDeviceBlacklist = Arrays.asList(new BlackListEntry(USB_VENDORID_SONY, USB_PRODUCTID_PS4CONTROLLER_ZCT1, 1), new BlackListEntry(USB_VENDORID_SONY, USB_PRODUCTID_PS4CONTROLLER_ZCT2, 1));
    private final ArrayList<UsbAlsaDevice> mAlsaDevices = new ArrayList<>();
    private IAudioService mAudioService;
    private final AlsaCardsParser mCardsParser = new AlsaCardsParser();
    private final Context mContext;
    private final boolean mHasMidiFeature;
    private final HashMap<String, UsbMidiDevice> mMidiDevices = new HashMap<>();
    private UsbMidiDevice mPeripheralMidiDevice = null;
    private UsbAlsaDevice mSelectedDevice;

    /* access modifiers changed from: private */
    public static class BlackListEntry {
        final int mFlags;
        final int mProductId;
        final int mVendorId;

        BlackListEntry(int vendorId, int productId, int flags) {
            this.mVendorId = vendorId;
            this.mProductId = productId;
            this.mFlags = flags;
        }
    }

    private static boolean isDeviceBlacklisted(int vendorId, int productId, int flags) {
        for (BlackListEntry entry : sDeviceBlacklist) {
            if (entry.mVendorId == vendorId && entry.mProductId == productId) {
                if ((entry.mFlags & flags) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

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
            Slog.d(str, "selectAlsaDevice() " + alsaDevice);
        }
        if (this.mSelectedDevice != null) {
            deselectAlsaDevice();
        }
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "usb_audio_automatic_routing_disabled", 0) == 0) {
            this.mSelectedDevice = alsaDevice;
            alsaDevice.start();
            if (DEBUG) {
                Slog.d(TAG, "selectAlsaDevice() - done.");
            }
        }
    }

    private synchronized void deselectAlsaDevice() {
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "deselectAlsaDevice() mSelectedDevice " + this.mSelectedDevice);
        }
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
            Slog.d(TAG, "selectDefaultDevice()");
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
        } else if (!isEnableTypecEarphoneUpgrade()) {
            Slog.i(TAG, "TypeC earphone uprade is disable, return");
        } else {
            for (int i = 0; i < HUAWEI_TYPEC_PID.length; i++) {
                if (usbDevice.getProductId() == HUAWEI_TYPEC_PID[i] && usbDevice.getVendorId() == HUAWEI_TYPEC_VID[i]) {
                    String str = TAG;
                    Slog.i(str, "huawei typec is connected,productId is " + HUAWEI_TYPEC_PID[i] + ",vendorId is " + HUAWEI_TYPEC_VID[i]);
                    Intent intent = new Intent(ACTION_TYPEC_UPDATE);
                    intent.putExtra("VendorId", usbDevice.getVendorId());
                    intent.putExtra("ProductId", usbDevice.getProductId());
                    Context context = this.mContext;
                    if (context != null) {
                        context.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.MANAGE_USB");
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
        boolean isOutputHeadset;
        boolean isInputHeadset;
        if (DEBUG) {
            Slog.d(TAG, "usbDeviceAdded(): " + usbDevice.getManufacturerName() + " nm:" + usbDevice.getProductName());
        }
        updateTypecHeadset(usbDevice);
        this.mCardsParser.scan();
        AlsaCardsParser.AlsaCardRecord cardRec = this.mCardsParser.findCardNumFor(deviceAddress);
        if (cardRec != null) {
            boolean hasOutput = true;
            boolean hasInput = parser.hasInput() && !isDeviceBlacklisted(usbDevice.getVendorId(), usbDevice.getProductId(), 2);
            if (!parser.hasOutput() || isDeviceBlacklisted(usbDevice.getVendorId(), usbDevice.getProductId(), 1)) {
                hasOutput = false;
            }
            if (DEBUG) {
                Slog.d(TAG, "hasInput: " + hasInput + " hasOutput:" + hasOutput);
            }
            if (hasInput || hasOutput) {
                if (parser.parseDevice(null)) {
                    boolean isInputHeadset2 = parser.isInputHeadset();
                    boolean isOutputHeadset2 = parser.isOutputHeadset();
                    Slog.i(TAG, "---- isHeadset[in: " + isInputHeadset2 + " , out: " + isOutputHeadset2 + "]");
                    isInputHeadset = isInputHeadset2;
                    isOutputHeadset = isOutputHeadset2;
                } else {
                    isInputHeadset = false;
                    isOutputHeadset = false;
                }
                IAudioService iAudioService = this.mAudioService;
                if (iAudioService == null) {
                    Slog.e(TAG, "no AudioService");
                    return;
                }
                UsbAlsaDevice alsaDevice = new UsbAlsaDevice(iAudioService, cardRec.getCardNum(), 0, deviceAddress, hasOutput, hasInput, isInputHeadset, isOutputHeadset);
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
                properties.putParcelable("usb_device", usbDevice);
                UsbMidiDevice usbMidiDevice = UsbMidiDevice.create(this.mContext, properties, cardRec.getCardNum(), 0);
                if (usbMidiDevice != null) {
                    this.mMidiDevices.put(deviceAddress, usbMidiDevice);
                }
            }
            logDevices("deviceAdded()");
            if (DEBUG) {
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
        logDevices("usbDeviceRemoved()");
    }

    /* access modifiers changed from: package-private */
    public void setPeripheralMidiState(boolean enabled, int card, int device) {
        UsbMidiDevice usbMidiDevice;
        if (this.mHasMidiFeature) {
            if (enabled && this.mPeripheralMidiDevice == null) {
                Bundle properties = new Bundle();
                Resources r = this.mContext.getResources();
                properties.putString(com.android.server.pm.Settings.ATTR_NAME, r.getString(17041414));
                properties.putString("manufacturer", r.getString(17041413));
                properties.putString("product", r.getString(17041415));
                properties.putInt("alsa_card", card);
                properties.putInt("alsa_device", device);
                this.mPeripheralMidiDevice = UsbMidiDevice.create(this.mContext, properties, card, device);
            } else if (!enabled && (usbMidiDevice = this.mPeripheralMidiDevice) != null) {
                IoUtils.closeQuietly(usbMidiDevice);
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

    public void logDevicesList(String title) {
        if (DEBUG) {
            String str = TAG;
            Slog.i(str, title + "----------------");
            Iterator<UsbAlsaDevice> it = this.mAlsaDevices.iterator();
            while (it.hasNext()) {
                Slog.i(TAG, "  -->");
                String str2 = TAG;
                Slog.i(str2, "" + it.next());
                Slog.i(TAG, "  <--");
            }
            Slog.i(TAG, "----------------");
        }
    }

    public void logDevices(String title) {
        if (DEBUG) {
            String str = TAG;
            Slog.i(str, title + "----------------");
            Iterator<UsbAlsaDevice> it = this.mAlsaDevices.iterator();
            while (it.hasNext()) {
                Slog.i(TAG, it.next().toShortString());
            }
            Slog.i(TAG, "----------------");
        }
    }
}
