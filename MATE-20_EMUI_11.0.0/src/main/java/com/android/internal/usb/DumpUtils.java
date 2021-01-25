package com.android.internal.usb;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.media.midi.MidiDeviceInfo;
import com.android.internal.util.dump.DualDumpOutputStream;

public class DumpUtils {
    public static void writeAccessory(DualDumpOutputStream dump, String idName, long id, UsbAccessory accessory) {
        long token = dump.start(idName, id);
        dump.write(MidiDeviceInfo.PROPERTY_MANUFACTURER, 1138166333441L, accessory.getManufacturer());
        dump.write("model", 1138166333442L, accessory.getModel());
        com.android.internal.util.dump.DumpUtils.writeStringIfNotNull(dump, "description", 1138166333443L, accessory.getManufacturer());
        dump.write("version", 1138166333444L, accessory.getVersion());
        com.android.internal.util.dump.DumpUtils.writeStringIfNotNull(dump, "uri", 1138166333445L, accessory.getUri());
        dump.write("serial", 1138166333446L, accessory.getSerial());
        dump.end(token);
    }

    public static void writeDevice(DualDumpOutputStream dump, String idName, long id, UsbDevice device) {
        long token = dump.start(idName, id);
        dump.write("name", 1138166333441L, device.getDeviceName());
        dump.write("vendor_id", 1120986464258L, device.getVendorId());
        dump.write("product_id", 1120986464259L, device.getProductId());
        dump.write("class", 1120986464260L, device.getDeviceClass());
        dump.write("subclass", 1120986464261L, device.getDeviceSubclass());
        dump.write("protocol", 1120986464262L, device.getDeviceProtocol());
        dump.write("manufacturer_name", 1138166333447L, device.getManufacturerName());
        dump.write("product_name", 1138166333448L, device.getProductName());
        dump.write("version", 1138166333449L, device.getVersion());
        dump.write("serial_number", 1138166333450L, device.getSerialNumber());
        int numConfigurations = device.getConfigurationCount();
        for (int i = 0; i < numConfigurations; i++) {
            writeConfiguration(dump, "configurations", 2246267895819L, device.getConfiguration(i));
        }
        dump.end(token);
    }

    private static void writeConfiguration(DualDumpOutputStream dump, String idName, long id, UsbConfiguration configuration) {
        long token = dump.start(idName, id);
        dump.write("id", 1120986464257L, configuration.getId());
        dump.write("name", 1138166333442L, configuration.getName());
        dump.write("attributes", 1155346202627L, configuration.getAttributes());
        dump.write("max_power", 1120986464260L, configuration.getMaxPower());
        int numInterfaces = configuration.getInterfaceCount();
        for (int i = 0; i < numInterfaces; i++) {
            writeInterface(dump, "interfaces", 2246267895813L, configuration.getInterface(i));
        }
        dump.end(token);
    }

    private static void writeInterface(DualDumpOutputStream dump, String idName, long id, UsbInterface iface) {
        long token = dump.start(idName, id);
        dump.write("id", 1120986464257L, iface.getId());
        dump.write("alternate_settings", 1120986464258L, iface.getAlternateSetting());
        dump.write("name", 1138166333443L, iface.getName());
        dump.write("class", 1120986464260L, iface.getInterfaceClass());
        dump.write("subclass", 1120986464261L, iface.getInterfaceSubclass());
        dump.write("protocol", 1120986464262L, iface.getInterfaceProtocol());
        int numEndpoints = iface.getEndpointCount();
        for (int i = 0; i < numEndpoints; i++) {
            writeEndpoint(dump, "endpoints", 2246267895815L, iface.getEndpoint(i));
        }
        dump.end(token);
    }

    private static void writeEndpoint(DualDumpOutputStream dump, String idName, long id, UsbEndpoint endpoint) {
        long token = dump.start(idName, id);
        dump.write("endpoint_number", 1120986464257L, endpoint.getEndpointNumber());
        dump.write("direction", 1159641169922L, endpoint.getDirection());
        dump.write("address", 1120986464259L, endpoint.getAddress());
        dump.write("type", 1159641169924L, endpoint.getType());
        dump.write("attributes", 1155346202629L, endpoint.getAttributes());
        dump.write("max_packet_size", 1120986464262L, endpoint.getMaxPacketSize());
        dump.write("interval", 1120986464263L, endpoint.getInterval());
        dump.end(token);
    }

    public static void writePort(DualDumpOutputStream dump, String idName, long id, UsbPort port) {
        long token = dump.start(idName, id);
        dump.write("id", 1138166333441L, port.getId());
        int mode = port.getSupportedModes();
        if (!dump.isProto()) {
            dump.write("supported_modes", 2259152797698L, UsbPort.modeToString(mode));
        } else if (mode == 0) {
            dump.write("supported_modes", 2259152797698L, 0);
        } else {
            if ((mode & 3) == 3) {
                dump.write("supported_modes", 2259152797698L, 3);
            } else if ((mode & 2) == 2) {
                dump.write("supported_modes", 2259152797698L, 2);
            } else if ((mode & 1) == 1) {
                dump.write("supported_modes", 2259152797698L, 1);
            }
            if ((mode & 4) == 4) {
                dump.write("supported_modes", 2259152797698L, 4);
            }
            if ((mode & 8) == 8) {
                dump.write("supported_modes", 2259152797698L, 8);
            }
        }
        dump.end(token);
    }

    private static void writePowerRole(DualDumpOutputStream dump, String idName, long id, int powerRole) {
        if (dump.isProto()) {
            dump.write(idName, id, powerRole);
        } else {
            dump.write(idName, id, UsbPort.powerRoleToString(powerRole));
        }
    }

    private static void writeDataRole(DualDumpOutputStream dump, String idName, long id, int dataRole) {
        if (dump.isProto()) {
            dump.write(idName, id, dataRole);
        } else {
            dump.write(idName, id, UsbPort.dataRoleToString(dataRole));
        }
    }

    private static void writeContaminantPresenceStatus(DualDumpOutputStream dump, String idName, long id, int contaminantPresenceStatus) {
        if (dump.isProto()) {
            dump.write(idName, id, contaminantPresenceStatus);
        } else {
            dump.write(idName, id, UsbPort.contaminantPresenceStatusToString(contaminantPresenceStatus));
        }
    }

    public static void writePortStatus(DualDumpOutputStream dump, String idName, long id, UsbPortStatus status) {
        long token = dump.start(idName, id);
        dump.write("connected", 1133871366145L, status.isConnected());
        if (dump.isProto()) {
            dump.write("current_mode", 1159641169922L, status.getCurrentMode());
        } else {
            dump.write("current_mode", 1159641169922L, UsbPort.modeToString(status.getCurrentMode()));
        }
        writePowerRole(dump, "power_role", 1159641169923L, status.getCurrentPowerRole());
        writeDataRole(dump, "data_role", 1159641169924L, status.getCurrentDataRole());
        int undumpedCombinations = status.getSupportedRoleCombinations();
        while (undumpedCombinations != 0) {
            int index = Integer.numberOfTrailingZeros(undumpedCombinations);
            undumpedCombinations &= ~(1 << index);
            long roleCombinationToken = dump.start("role_combinations", 2246267895813L);
            writePowerRole(dump, "power_role", 1159641169921L, (index / 3) + 0);
            writeDataRole(dump, "data_role", 1159641169922L, index % 3);
            dump.end(roleCombinationToken);
        }
        writeContaminantPresenceStatus(dump, "contaminant_presence_status", 1159641169926L, status.getContaminantDetectionStatus());
        dump.end(token);
    }
}
