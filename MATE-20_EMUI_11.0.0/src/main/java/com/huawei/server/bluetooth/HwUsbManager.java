package com.huawei.server.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.HwLog;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwUsbManager {
    private static final String BT_CFG_VERSION = "version";
    private static final String BT_HEADER_CFG = "Header";
    private static final String CFG_COTA_FILE = "/data/cota/para/emcom/noncell/BluetoothCfg.json";
    private static final String CFG_FILE_NAME = "/BluetoothCfg.json";
    private static final String CFG_SYS_FILE = "/system/emui/base/emcom/noncell/BluetoothCfg.json";
    private static final String CFG_VER_DIR = "emcom/noncell";
    private static final int CONFIG_UNKOWN = -1;
    private static final String DEFAULT_ADDR = "00:00:00:00:00:00";
    private static final String DEFAULT_MODEL_ID = "000000";
    private static final String DEFAULT_NEARBY_VERSION = "01";
    private static final String DEFAULT_SUB_MODEL_ID = "00";
    private static final int ERROR = -1;
    private static final byte[] GET_PENCIL_INFO_COMMAND = {-88, 87};
    private static final String HW_BLE_PERIPHERAL_CFG_NAME = "HwBlePeripheralCfg";
    private static final int INVALID_INDEX = -1;
    private static final int MESSAGE_USB_STATE_CHANGED = 0;
    private static final int PENCIL_VENDOR_ID = 4817;
    private static final int READ_DATA_TIMEOUT = 4500;
    private static final String TAG = "BT-HwUsbManager";
    private static final int VALID_DATA_LENGTH = 15;
    private static final int WRITE_DATA_TIMEOUT = 3000;
    private String mAddr;
    private HwBluetoothPencilManager mBluetoothPencilManager;
    private Context mContext;
    private int mControlIndex;
    private UsbDeviceConnection mDeviceConnection;
    private UsbEndpoint mEndpointIn;
    private UsbEndpoint mEndpointOut;
    private Handler mHandler;
    private String mModelId;
    private String mNearbyVersion;
    private List<Integer> mPencilPid = new ArrayList(5);
    private String mSubModelId;
    private UsbInterface mUsbInterface;
    private UsbManager mUsbManager;

    HwUsbManager(Context context, HwBluetoothPencilManager bluetoothPencilManager) {
        HwLog.i(TAG, "construction");
        this.mAddr = DEFAULT_ADDR;
        this.mNearbyVersion = DEFAULT_NEARBY_VERSION;
        this.mModelId = DEFAULT_MODEL_ID;
        this.mSubModelId = DEFAULT_SUB_MODEL_ID;
        this.mContext = context;
        HandlerThread mUsbManagerThread = new HandlerThread(TAG);
        mUsbManagerThread.start();
        this.mHandler = new UsbHandler(mUsbManagerThread.getLooper());
        this.mBluetoothPencilManager = bluetoothPencilManager;
        this.mControlIndex = -1;
        setBluetoothCfgList();
    }

    /* access modifiers changed from: package-private */
    public void getUsbStateFromBroadcast(Intent intent) {
        Handler handler = this.mHandler;
        if (handler == null) {
            HwLog.e(TAG, "getUsbStateFromBroadcast, handler is null");
        } else {
            handler.obtainMessage(0, intent).sendToTarget();
        }
    }

    private class UsbHandler extends Handler {
        UsbHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwLog.i(HwUsbManager.TAG, "handleMessage: " + msg.what);
            if (msg.what != 0) {
                HwLog.e(HwUsbManager.TAG, "UsbHandler: unknow message:" + msg.what);
                return;
            }
            Intent intent = (Intent) msg.obj;
            Object usbDevice = intent.getParcelableExtra("device");
            if (usbDevice instanceof UsbDevice) {
                HwUsbManager.this.onUsbStateChanged((UsbDevice) usbDevice, intent.getAction());
            }
        }
    }

    private void getDeviceConnection(UsbDevice usbDevice) {
        if (this.mUsbManager == null) {
            HwLog.e(TAG, "getDeviceConnection: manager is null");
            return;
        }
        UsbDeviceConnection usbDeviceConnection = this.mDeviceConnection;
        if (usbDeviceConnection != null) {
            usbDeviceConnection.close();
        }
        this.mDeviceConnection = this.mUsbManager.openDevice(usbDevice);
    }

    private void getUsbInterface(UsbDevice usbDevice) {
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            HwLog.i(TAG, "InterfaceClass: " + usbDevice.getInterface(i).toString() + " " + usbDevice.getInterface(i).getEndpointCount());
            if (usbDevice.getInterface(i).getInterfaceClass() == 10) {
                this.mUsbInterface = usbDevice.getInterface(i);
            } else if (usbDevice.getInterface(i).getInterfaceClass() == 2) {
                this.mControlIndex = i;
            } else {
                HwLog.d(TAG, "ignore this interface");
            }
        }
    }

    private void getEndpoint() {
        int endpointCount = this.mUsbInterface.getEndpointCount();
        this.mEndpointOut = null;
        this.mEndpointIn = null;
        HwLog.i(TAG, "getEndpoint: endpointCount " + endpointCount);
        for (int i = 0; i < endpointCount; i++) {
            UsbEndpoint endpoint = this.mUsbInterface.getEndpoint(i);
            if (endpoint == null) {
                HwLog.w(TAG, "getEndpoint: endpoint is null i = " + i);
            } else {
                HwLog.i(TAG, "getEndpoint: endpoint: " + endpoint.toString() + ", type: " + endpoint.getType());
                if (endpoint.getType() == 2) {
                    if (endpoint.getDirection() == 0) {
                        this.mEndpointOut = endpoint;
                    } else if (endpoint.getDirection() == 128) {
                        this.mEndpointIn = endpoint;
                    } else {
                        HwLog.w(TAG, "getEndpoint: invalid type");
                    }
                }
            }
        }
    }

    private void releaseConnection() {
        UsbInterface usbInterface;
        UsbDeviceConnection usbDeviceConnection = this.mDeviceConnection;
        if (usbDeviceConnection != null && (usbInterface = this.mUsbInterface) != null) {
            usbDeviceConnection.releaseInterface(usbInterface);
            this.mDeviceConnection.close();
            this.mDeviceConnection = null;
        }
    }

    private boolean initEndpoint(UsbDevice usbDevice) {
        HwLog.i(TAG, "init Endpoint");
        getDeviceConnection(usbDevice);
        if (this.mDeviceConnection == null) {
            HwLog.e(TAG, "initEndpoint: mDeviceConnection is null");
            return false;
        }
        getUsbInterface(usbDevice);
        if (this.mUsbInterface == null) {
            HwLog.w(TAG, "initEndpoint: interface is null");
            this.mDeviceConnection.close();
            this.mDeviceConnection = null;
            return false;
        }
        setBaudRateAndDtrRts();
        if (!this.mDeviceConnection.claimInterface(this.mUsbInterface, true)) {
            HwLog.w(TAG, "initEndpoint: claimInterface failed");
            releaseConnection();
            return false;
        } else if (!this.mDeviceConnection.setInterface(this.mUsbInterface)) {
            HwLog.w(TAG, "initEndpoint: setInterface failed");
            releaseConnection();
            return false;
        } else {
            getEndpoint();
            if (this.mEndpointIn != null && this.mEndpointOut != null) {
                return true;
            }
            HwLog.i(TAG, "init failed");
            releaseConnection();
            return false;
        }
    }

    private void setBaudRateAndDtrRts() {
        int i = this.mControlIndex;
        if (i == -1) {
            HwLog.e(TAG, "setBaudRateAndDtrRts: control index is invalid");
            return;
        }
        byte[] msg = {0, -62, 1, 0, 0, 0, 8};
        int len = this.mDeviceConnection.controlTransfer(33, 32, 0, i, msg, msg.length, 3000);
        if (len < 0) {
            HwLog.e(TAG, "setBaudRateAndDtrRts set baudrate fail: " + len);
            return;
        }
        int len2 = this.mDeviceConnection.controlTransfer(33, 34, 3, this.mControlIndex, null, 0, 3000);
        if (len2 < 0) {
            HwLog.e(TAG, "setBaudRateAndDtrRts set DTSRTS fail: " + len2);
            return;
        }
        HwLog.i(TAG, "setBaudRateAndDtrRts set success");
    }

    private int sendCommand() {
        UsbDeviceConnection usbDeviceConnection = this.mDeviceConnection;
        if (usbDeviceConnection == null) {
            HwLog.e(TAG, "device connection is null");
            return -1;
        }
        UsbEndpoint usbEndpoint = this.mEndpointOut;
        if (usbEndpoint == null) {
            HwLog.e(TAG, "device endpointOut is null");
            return -1;
        }
        byte[] bArr = GET_PENCIL_INFO_COMMAND;
        int out = usbDeviceConnection.bulkTransfer(usbEndpoint, bArr, bArr.length, 3000);
        HwLog.i(TAG, "sendCommand: send data out " + out);
        return out;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006d, code lost:
        com.android.server.HwLog.w(com.huawei.server.bluetooth.HwUsbManager.TAG, "getInfoFromUsb: read error");
     */
    private void getInfoFromUsb() {
        HwLog.i(TAG, "getInfoFromUsb");
        ByteBuffer.allocate(this.mEndpointIn.getMaxPacketSize());
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(this.mDeviceConnection, this.mEndpointIn);
        try {
            byte[] tmpBuffer = new byte[this.mEndpointIn.getMaxPacketSize()];
            byte[] dstBuffer = new byte[100];
            int i = 0;
            while (true) {
                if (i >= 15) {
                    break;
                }
                int ret = read(this.mDeviceConnection, usbRequest, tmpBuffer, READ_DATA_TIMEOUT);
                HwLog.i(TAG, "read fragment: [" + ret + "] " + HwUtils.bytesToHexString(tmpBuffer));
                if (ret == -1) {
                    break;
                } else if (i + ret >= dstBuffer.length) {
                    break;
                } else {
                    System.arraycopy(tmpBuffer, 0, dstBuffer, i, ret);
                    i += ret;
                    if (i >= 15) {
                        HwLog.i(TAG, "getInfoFromUsb: get enough");
                        break;
                    }
                }
            }
            parseUsbData(dstBuffer);
        } catch (IOException | BufferOverflowException e) {
            HwLog.e(TAG, "getInfoFromUsb: exception occured");
        } catch (Throwable th) {
            releaseConnection();
            usbRequest.close();
            throw th;
        }
        releaseConnection();
        usbRequest.close();
    }

    private int read(UsbDeviceConnection connection, UsbRequest requestIn, byte[] dest, int timeout) throws IOException {
        UsbEndpoint endpoint = requestIn.getEndpoint();
        return connection.bulkTransfer(endpoint, dest, Math.min(dest.length, endpoint.getMaxPacketSize()), timeout);
    }

    private void sendPencilAttachEvent(String event) {
        HwLog.i(TAG, "sendPencilAttachEvent: " + event);
        HwBluetoothPencilManager hwBluetoothPencilManager = this.mBluetoothPencilManager;
        if (hwBluetoothPencilManager == null) {
            HwLog.e(TAG, "sendPencilAttachEvent: BluetoothPencilManager is null");
        } else {
            hwBluetoothPencilManager.sendHwPencilBroadcast(event, this.mAddr, this.mNearbyVersion, this.mModelId, this.mSubModelId, "1");
        }
    }

    private void parseUsbData(byte[] data) {
        if (data == null || data.length < 15) {
            HwLog.e(TAG, "parseUsbData: arg error");
            return;
        }
        this.mAddr = HwUtils.bytesToHexAddrString(Arrays.copyOfRange(data, 2, 8));
        this.mModelId = HwUtils.bytesToHexString(Arrays.copyOfRange(data, 8, 11));
        this.mSubModelId = HwUtils.bytesToHexString(Arrays.copyOfRange(data, 11, 12));
        this.mNearbyVersion = HwUtils.bytesToHexString(Arrays.copyOfRange(data, 13, 14));
        sendPencilAttachEvent("CONNECTED");
    }

    private boolean isHwPencil(UsbDevice usbDevice) {
        if (usbDevice.getVendorId() != PENCIL_VENDOR_ID) {
            return false;
        }
        for (int i = 0; i < this.mPencilPid.size(); i++) {
            if (usbDevice.getProductId() == this.mPencilPid.get(i).intValue()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUsbStateChanged(UsbDevice usbDevice, String action) {
        HwLog.i(TAG, "onUsbStateChanged");
        if (usbDevice == null) {
            HwLog.e(TAG, "onUsbStateChanged: device is null");
            return;
        }
        if (this.mUsbManager == null) {
            this.mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
        }
        if (!isHwPencil(usbDevice)) {
            HwLog.i(TAG, "onUsbStateChanged: not pencil");
        } else if (!this.mUsbManager.hasPermission(usbDevice)) {
            HwLog.e(TAG, "onUsbStateChanged: not has permission");
        } else if (Objects.equals(action, "android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            sendPencilAttachEvent("PING_SUCC");
            if (!initEndpoint(usbDevice)) {
                HwLog.e(TAG, "onUsbStateChanged: init endpoint failed");
            } else if (sendCommand() != -1) {
                getInfoFromUsb();
            }
        } else if (Objects.equals(action, "android.hardware.usb.action.USB_DEVICE_DETACHED")) {
            sendPencilAttachEvent("DISCONNECTED");
            this.mDeviceConnection = null;
            this.mControlIndex = -1;
        } else {
            HwLog.w(TAG, "onUsbStateChanged: unknown action " + action);
        }
    }

    private void closeFileStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                HwLog.e(TAG, "closeFileStream IOException");
            }
        }
    }

    private String getJsonContent(String cfgPath) {
        if (TextUtils.isEmpty(cfgPath)) {
            return "";
        }
        File targetFile = new File(cfgPath);
        if (!targetFile.isFile()) {
            HwLog.e(TAG, "getJsonContent targetFile error" + cfgPath);
            return "";
        }
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            streamReader = new InputStreamReader(new FileInputStream(targetFile));
            bufferedReader = new BufferedReader(streamReader);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    return buffer.toString();
                }
                buffer.append(line);
            }
        } catch (IOException e) {
            HwLog.e(TAG, "getJsonContent failed for IOException");
            return "";
        } finally {
            closeFileStream(bufferedReader);
            closeFileStream(streamReader);
        }
    }

    private int getConfigVersion(String content) {
        if (TextUtils.isEmpty(content)) {
            HwLog.e(TAG, "getConfigVersion error");
            return -1;
        }
        try {
            JSONObject headerCfg = new JSONObject(content).optJSONObject(BT_HEADER_CFG);
            if (headerCfg != null) {
                return headerCfg.optInt("version");
            }
            HwLog.e(TAG, "getConfigVersion headerCfg null");
            return -1;
        } catch (JSONException e) {
            HwLog.e(TAG, "getConfigVersion failed");
            return -1;
        }
    }

    private String getCfgContent() {
        int cotaVersion = -1;
        int sysVersion = -1;
        String cotaContent = getJsonContent(CFG_COTA_FILE);
        String sysContent = getJsonContent(CFG_SYS_FILE);
        if (!TextUtils.isEmpty(cotaContent)) {
            cotaVersion = getConfigVersion(cotaContent);
        }
        if (!TextUtils.isEmpty(sysContent)) {
            sysVersion = getConfigVersion(sysContent);
        }
        HwLog.d(TAG, "getCfgContent cotaVer:" + cotaVersion + " sysVer:" + sysVersion);
        if (cotaVersion < sysVersion) {
            return sysContent;
        }
        HwLog.i(TAG, "getCfgContent cotaVer:" + cotaVersion);
        return cotaContent;
    }

    private List<Map> getCfgList(JSONObject jsonObject, String key) {
        List<Map> cfgList = new ArrayList<>();
        try {
            JSONArray cfgArray = jsonObject.getJSONArray(key);
            if (cfgArray == null) {
                HwLog.e(TAG, "getCfgList cfgArray is null");
                return cfgList;
            }
            for (int i = 0; i < cfgArray.length(); i++) {
                JSONObject cfgObject = cfgArray.getJSONObject(i);
                Map map = new ArrayMap();
                Iterator iterator = cfgObject.keys();
                while (iterator.hasNext()) {
                    Object jsonObjectKey = iterator.next();
                    if (jsonObjectKey instanceof String) {
                        String jsonKey = (String) jsonObjectKey;
                        Object jsonValue = cfgObject.get(jsonKey);
                        String jsonKey2 = jsonKey.toLowerCase();
                        HwLog.d(TAG, "getCfgList: jsonKey-" + jsonKey2 + " jsonValue-" + jsonValue);
                        if (jsonValue == null) {
                            jsonValue = "";
                        }
                        map.put(jsonKey2, jsonValue);
                    }
                }
                cfgList.add(map);
            }
            return cfgList;
        } catch (JSONException e) {
            HwLog.e(TAG, "getCfgList failed");
        }
    }

    private void setBluetoothCfgList() {
        String content = getCfgContent();
        if (TextUtils.isEmpty(content)) {
            HwLog.e(TAG, "getBleCfglist is empty");
            return;
        }
        HwLog.i(TAG, "setBluetoothCfgList: begin");
        List<Map> btCfgList = new ArrayList<>();
        try {
            btCfgList.clear();
            JSONObject cfgContent = new JSONObject(content);
            Iterator<String> iterator = cfgContent.keys();
            while (iterator.hasNext()) {
                Object jsonObjectKey = iterator.next();
                if (jsonObjectKey instanceof String) {
                    String jsonKey = (String) jsonObjectKey;
                    if (!jsonKey.equals(BT_HEADER_CFG)) {
                        HwLog.d(TAG, "setBluetoothCfgList: jsonKey-" + jsonKey);
                        if (HW_BLE_PERIPHERAL_CFG_NAME.equals(jsonKey)) {
                            btCfgList = getCfgList(cfgContent, jsonKey);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            HwLog.e(TAG, "setBtCfgList for JSONException");
        }
        updateConfig(btCfgList);
    }

    private void updateConfig(List<Map> btCfgList) {
        HwLog.i(TAG, "updateConfig");
        for (Object mapObj : btCfgList) {
            if (!(mapObj instanceof Map)) {
                HwLog.e(TAG, "updateConfig: not map");
            } else {
                Object pencilObj = ((Map) mapObj).get("pencilpid");
                if (pencilObj instanceof String) {
                    this.mPencilPid.addAll(HwUtils.stringToIntegers((String) pencilObj));
                }
            }
        }
    }
}
