package android.bluetooth;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class BluetoothAdvFilterParamEx implements Parcelable {
    private static final byte ADVERTIS_DATA_IDX = 5;
    private static final byte ADVERTIS_PARAM_IDX = 4;
    private static final byte ADVERTIS_SETTING_IDX = 3;
    public static final Parcelable.Creator<BluetoothAdvFilterParamEx> CREATOR = new Parcelable.Creator<BluetoothAdvFilterParamEx>() {
        /* class android.bluetooth.BluetoothAdvFilterParamEx.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothAdvFilterParamEx[] newArray(int size) {
            return new BluetoothAdvFilterParamEx[size];
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothAdvFilterParamEx createFromParcel(Parcel in) {
            return new BluetoothAdvFilterParamEx(in);
        }
    };
    public static final byte DELIVERY_MODE_REPLY = -16;
    public static final byte DELIVERY_MODE_UPDATE_ACTIVE_LIST = -15;
    private static final byte DEVICE_INFO_IDX = 7;
    private static final byte RESPONSE_DATA_IDX = 6;
    private static final byte SCAN_FILTER_IDX = 2;
    private static final byte SCAN_SETTING_IDX = 1;
    private static final String TAG = "BluetoothAdvFilterParamEx";
    private AdvertiseHelper advHelper;
    private AdvertiseData mAdvData;
    private AdvertisingSetParameters mAdvParams;
    private AdvertiseSettings mAdvSettings;
    private byte mDeliveryMode;
    private List<BluetoothAdvDeviceInfo> mDevInfoList;
    private int mDuration;
    private int mFilterIdx;
    private AdvertiseData mRespData;
    private List<ScanFilter> mScanFilterList;
    private ScanSettings mScanSettings;
    private UUID mUuid;

    private String byteArrayToString(byte[] valueBuf) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < valueBuf.length; idx++) {
            if (idx != 0) {
                sb.append(" ");
            }
            sb.append(String.format(Locale.ENGLISH, "%02x", Byte.valueOf(valueBuf[idx])));
        }
        return sb.toString();
    }

    private static byte[] intToByteArray(int value) {
        ByteBuffer converter = ByteBuffer.allocate(4);
        converter.order(ByteOrder.nativeOrder());
        converter.putInt(value);
        return converter.array();
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUuid.getMostSignificantBits());
        dest.writeLong(this.mUuid.getLeastSignificantBits());
        dest.writeByte(this.mDeliveryMode);
        dest.writeInt(this.mDuration);
        dest.writeInt(genFieldBitMap());
        Log.e(TAG, "writeToParcel: uuid:" + this.mUuid);
        ScanSettings scanSettings = this.mScanSettings;
        if (scanSettings != null) {
            scanSettings.writeToParcel(dest, 0);
            Log.e(TAG, "writeToParcel: ScanSettings.RssiHigh:" + this.mScanSettings.getRssiHighValue());
        }
        List<ScanFilter> list = this.mScanFilterList;
        if (list != null) {
            dest.writeParcelableList(list, 0);
            Log.e(TAG, "writeToParcel: ScanFilterList.size:" + this.mScanFilterList.size());
        }
        AdvertiseSettings advertiseSettings = this.mAdvSettings;
        if (advertiseSettings != null) {
            advertiseSettings.writeToParcel(dest, 0);
            Log.e(TAG, "writeToParcel: AdvSettings.TimeoutMillis:" + this.mAdvSettings.getTimeout());
        }
        AdvertisingSetParameters advertisingSetParameters = this.mAdvParams;
        if (advertisingSetParameters != null) {
            advertisingSetParameters.writeToParcel(dest, 0);
            Log.e(TAG, "writeToParcel: AdvParams.Interval:" + this.mAdvParams.getInterval());
        }
        AdvertiseData advertiseData = this.mAdvData;
        if (advertiseData != null) {
            advertiseData.writeToParcel(dest, 0);
            Log.e(TAG, "writeToParcel: AdvData.TxPower:" + this.mAdvData.getIncludeTxPowerLevel());
        }
        AdvertiseData advertiseData2 = this.mRespData;
        if (advertiseData2 != null) {
            advertiseData2.writeToParcel(dest, 0);
            Log.e(TAG, "writeToParcel: RespData.TxPower:" + this.mRespData.getIncludeTxPowerLevel());
        }
        List<BluetoothAdvDeviceInfo> list2 = this.mDevInfoList;
        if (list2 != null) {
            dest.writeList(list2);
            Log.e(TAG, "writeToParcel: DevInfo num:" + this.mDevInfoList.size());
        }
    }

    private BluetoothAdvFilterParamEx(UUID id, byte deliveryMode) {
        this.mScanSettings = null;
        this.mScanFilterList = null;
        this.mAdvSettings = null;
        this.mAdvParams = null;
        this.mAdvData = null;
        this.mRespData = null;
        this.mDevInfoList = null;
        this.advHelper = new AdvertiseHelper();
        this.mUuid = id;
        this.mDeliveryMode = deliveryMode;
    }

    private BluetoothAdvFilterParamEx(Parcel src) {
        this.mScanSettings = null;
        this.mScanFilterList = null;
        this.mAdvSettings = null;
        this.mAdvParams = null;
        this.mAdvData = null;
        this.mRespData = null;
        this.mDevInfoList = null;
        this.advHelper = new AdvertiseHelper();
        this.mUuid = new UUID(src.readLong(), src.readLong());
        this.mDeliveryMode = src.readByte();
        this.mDuration = src.readInt();
        int fieldBitMap = src.readInt();
        Log.e(TAG, "BluetoothAdvFilterParamEx: Uuid:" + this.mUuid);
        if ((fieldBitMap & 2) != 0) {
            this.mScanSettings = (ScanSettings) ScanSettings.CREATOR.createFromParcel(src);
            Log.e(TAG, "BluetoothAdvFilterParamEx: ScanSettings.RssiHigh:" + this.mScanSettings.getRssiHighValue());
        }
        if ((fieldBitMap & 4) != 0) {
            this.mScanFilterList = new ArrayList();
            src.readParcelableList(this.mScanFilterList, ScanFilter.class.getClassLoader());
            Log.e(TAG, "BluetoothAdvFilterParamEx: ScanFilterList.size:" + this.mScanFilterList.size());
        }
        if ((fieldBitMap & 8) != 0) {
            this.mAdvSettings = (AdvertiseSettings) AdvertiseSettings.CREATOR.createFromParcel(src);
            Log.e(TAG, "BluetoothAdvFilterParamEx: AdvSettings.TimeoutMillis:" + this.mAdvSettings.getTimeout());
        }
        if ((fieldBitMap & 16) != 0) {
            this.mAdvParams = (AdvertisingSetParameters) AdvertisingSetParameters.CREATOR.createFromParcel(src);
            Log.e(TAG, "BluetoothAdvFilterParamEx: AdvParams.Interval:" + this.mAdvParams.getInterval());
        }
        if ((fieldBitMap & 32) != 0) {
            this.mAdvData = (AdvertiseData) AdvertiseData.CREATOR.createFromParcel(src);
            Log.e(TAG, "BluetoothAdvFilterParamEx: AdvData. ServiceUuids size:" + this.mAdvData.getServiceUuids().size());
        }
        if ((fieldBitMap & 64) != 0) {
            this.mRespData = (AdvertiseData) AdvertiseData.CREATOR.createFromParcel(src);
            Log.e(TAG, "BluetoothAdvFilterParamEx: RespData.TxPower:" + this.mRespData.getIncludeTxPowerLevel());
        }
        if ((fieldBitMap & 128) != 0) {
            this.mDevInfoList = new ArrayList();
            src.readList(this.mDevInfoList, BluetoothAdvDeviceInfo.class.getClassLoader());
            Log.e(TAG, "BluetoothAdvFilterParamEx: DevInfo num:" + this.mDevInfoList.size());
            for (BluetoothAdvDeviceInfo tmpDevInfo : this.mDevInfoList) {
                Log.e(TAG, byteArrayToString(tmpDevInfo.getDevId()));
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public byte[] genDeviceMsgData(int filterIdx) {
        if (this.mDevInfoList == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write(1);
        outStream.write(filterIdx);
        outStream.write(this.mDevInfoList.size());
        for (BluetoothAdvDeviceInfo devInfo : this.mDevInfoList) {
            outStream.write(devInfo.getDevId(), 0, 8);
            outStream.write(intToByteArray(devInfo.getStatus()), 0, 1);
            outStream.write(intToByteArray(devInfo.getTimeout()), 0, 2);
        }
        return outStream.toByteArray();
    }

    private byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ix++) {
            byteNum[ix] = (byte) ((int) ((num >> (64 - ((ix + 1) * 8))) & 255));
        }
        return byteNum;
    }

    private byte[] concateUuid(ParcelUuid filterUuid, ParcelUuid maskUuid) {
        if (filterUuid == null) {
            return new byte[0];
        }
        byte[] uuid = BluetoothUuid.uuidToBytes(filterUuid);
        byte[] concated = new byte[(uuid.length * 2)];
        System.arraycopy(uuid, 0, concated, 0, uuid.length);
        if (maskUuid == null) {
            byte[] tmpMask = new byte[uuid.length];
            Arrays.fill(tmpMask, (byte) 0);
            System.arraycopy(tmpMask, 0, concated, uuid.length, uuid.length);
        } else if (uuid.length == 2 || uuid.length == 4) {
            System.arraycopy(long2Bytes(maskUuid.getUuid().getMostSignificantBits()), 0, concated, uuid.length, uuid.length);
        } else if (uuid.length != 16) {
            return new byte[0];
        } else {
            System.arraycopy(long2Bytes(maskUuid.getUuid().getMostSignificantBits()), 0, concated, uuid.length, uuid.length);
            System.arraycopy(long2Bytes(maskUuid.getUuid().getLeastSignificantBits()), 0, concated, uuid.length, uuid.length);
        }
        return concated;
    }

    private byte[] concateService(ParcelUuid serviceDataUuid, byte[] serviceData, byte[] dataMask) {
        if (serviceDataUuid == null) {
            return new byte[0];
        }
        byte[] uuid = BluetoothUuid.uuidToBytes(serviceDataUuid);
        byte[] concated = new byte[((uuid.length + (serviceData == null ? 0 : serviceData.length)) * 2)];
        System.arraycopy(uuid, 0, concated, 0, uuid.length);
        int pos = 0 + uuid.length;
        if (serviceData != null) {
            System.arraycopy(serviceData, 0, concated, pos, serviceData.length);
            pos += serviceData.length;
        }
        System.arraycopy(uuid, 0, concated, pos, uuid.length);
        int pos2 = pos + uuid.length;
        if (serviceData != null) {
            if (dataMask != null) {
                System.arraycopy(dataMask, 0, concated, pos2, dataMask.length);
            } else {
                byte[] tmpMask = new byte[serviceData.length];
                Arrays.fill(tmpMask, (byte) -1);
                System.arraycopy(tmpMask, 0, concated, pos2, tmpMask.length);
            }
        }
        return concated;
    }

    private byte[] concateManufactory(int companyId, byte[] manuData, byte[] dataMask) {
        if (companyId <= 0) {
            return new byte[0];
        }
        byte[] concated = new byte[(((manuData == null ? 0 : manuData.length) + 2) * 2)];
        System.arraycopy(intToByteArray(companyId), 0, concated, 0, 2);
        int pos = 0 + 2;
        if (manuData != null) {
            System.arraycopy(manuData, 0, concated, pos, manuData.length);
            pos += manuData.length;
        }
        byte[] tmpMask = new byte[2];
        Arrays.fill(tmpMask, (byte) -1);
        System.arraycopy(tmpMask, 0, concated, pos, tmpMask.length);
        int pos2 = pos + tmpMask.length;
        if (manuData != null) {
            if (dataMask != null) {
                System.arraycopy(dataMask, 0, concated, pos2, dataMask.length);
            } else {
                byte[] tmpMask2 = new byte[manuData.length];
                Arrays.fill(tmpMask2, (byte) -1);
                System.arraycopy(tmpMask2, 0, concated, pos2, tmpMask2.length);
            }
        }
        return concated;
    }

    private void writeDeviceAddress(ByteArrayOutputStream outStream) {
        List<String> addrList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            String addr = filter.getDeviceAddress();
            if (addr != null) {
                addrList.add(addr);
            }
        }
        outStream.write(addrList.size());
        for (String tmpAddr : addrList) {
            outStream.write(2);
            try {
                outStream.write(tmpAddr.getBytes("UTF-8"), 0, 6);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Can't include name - encoding error!", e);
            }
        }
    }

    private void writeSrvUuidList(ByteArrayOutputStream outStream) {
        List<byte[]> srvUuidList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            byte[] srvUuid = concateUuid(filter.getServiceUuid(), filter.getServiceUuidMask());
            if (srvUuid.length != 0) {
                srvUuidList.add(srvUuid);
            }
        }
        outStream.write(srvUuidList.size());
        for (byte[] tmpUuid : srvUuidList) {
            outStream.write(tmpUuid.length);
            outStream.write(tmpUuid, 0, tmpUuid.length);
        }
    }

    private void writeSolicUuidList(ByteArrayOutputStream outStream) {
        List<byte[]> solicUuidList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            byte[] solicUuid = concateUuid(filter.getServiceSolicitationUuid(), filter.getServiceSolicitationUuidMask());
            if (solicUuid.length != 0) {
                solicUuidList.add(solicUuid);
            }
        }
        outStream.write(solicUuidList.size());
        for (byte[] tmpUuid : solicUuidList) {
            outStream.write(tmpUuid.length);
            outStream.write(tmpUuid, 0, tmpUuid.length);
        }
    }

    private void writeLocalNameList(ByteArrayOutputStream outStream) {
        List<String> localNameList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            String localName = filter.getDeviceName();
            if (localName != null) {
                localNameList.add(localName);
            }
        }
        outStream.write(localNameList.size());
        for (String tmpName : localNameList) {
            outStream.write(tmpName.length());
            try {
                outStream.write(tmpName.getBytes("UTF-8"), 0, tmpName.length());
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Can't include name - encoding error!", e);
            }
        }
    }

    private void writeManufDataList(ByteArrayOutputStream outStream) {
        List<byte[]> manufDataList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            byte[] manufData = concateManufactory(filter.getManufacturerId(), filter.getManufacturerData(), filter.getManufacturerDataMask());
            if (manufData.length != 0) {
                manufDataList.add(manufData);
            }
        }
        outStream.write(manufDataList.size());
        for (byte[] tmpData : manufDataList) {
            outStream.write(tmpData.length);
            outStream.write(tmpData, 0, tmpData.length);
        }
    }

    private void writeSrvDataList(ByteArrayOutputStream outStream) {
        List<byte[]> srvDataList = new ArrayList<>();
        for (ScanFilter filter : this.mScanFilterList) {
            byte[] srvData = concateService(filter.getServiceDataUuid(), filter.getServiceData(), filter.getServiceDataMask());
            if (srvData.length != 0) {
                srvDataList.add(srvData);
            }
        }
        outStream.write(srvDataList.size());
        for (byte[] tmpData : srvDataList) {
            outStream.write(tmpData.length);
            outStream.write(tmpData, 0, tmpData.length);
        }
    }

    public byte[] genSoftFilterMsgData(int filterIdx) {
        if (this.mScanSettings == null || this.mScanFilterList == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write(1);
        outStream.write(filterIdx);
        outStream.write(intToByteArray(4096), 0, 2);
        outStream.write(intToByteArray(this.mScanSettings.getListLogicType()), 0, 2);
        outStream.write(this.mScanSettings.getFilterLogicType());
        outStream.write(this.mScanSettings.getRssiHighValue());
        outStream.write(this.mDeliveryMode);
        outStream.write(intToByteArray(0), 0, 2);
        outStream.write(intToByteArray(0), 0, 1);
        outStream.write(this.mScanSettings.getRssiLowValue());
        outStream.write(intToByteArray(0), 0, 2);
        outStream.write(intToByteArray(0), 0, 2);
        writeDeviceAddress(outStream);
        writeSrvUuidList(outStream);
        writeSolicUuidList(outStream);
        writeLocalNameList(outStream);
        writeManufDataList(outStream);
        writeSrvDataList(outStream);
        return outStream.toByteArray();
    }

    public byte[] genAdvParamMsgData(int filterIdx, byte advHandle) {
        if (this.mAdvParams == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write(1);
        outStream.write(filterIdx);
        outStream.write(advHandle);
        short advEvtProp = 0;
        if (this.mAdvParams.isConnectable()) {
            advEvtProp = (short) (0 | 1);
        }
        if (this.mAdvParams.isScannable()) {
            advEvtProp = (short) (advEvtProp | 2);
        }
        if (this.mAdvParams.isLegacy()) {
            advEvtProp = (short) (advEvtProp | 16);
        }
        if (this.mAdvParams.isAnonymous()) {
            advEvtProp = (short) (advEvtProp | 32);
        }
        if (this.mAdvParams.includeTxPower()) {
            advEvtProp = (short) (advEvtProp | 64);
        }
        outStream.write(intToByteArray(advEvtProp), 0, 2);
        outStream.write(intToByteArray(this.mAdvParams.getInterval()), 0, 3);
        outStream.write(intToByteArray(this.mAdvParams.getInterval()), 0, 3);
        outStream.write(7);
        outStream.write(1);
        outStream.write(0);
        outStream.write(new byte[]{0, 0, 0, 0, 0, 0}, 0, 6);
        outStream.write(0);
        outStream.write(this.mAdvParams.getTxPowerLevel());
        outStream.write(this.mAdvParams.getPrimaryPhy());
        outStream.write(0);
        outStream.write(this.mAdvParams.getSecondaryPhy());
        outStream.write(1);
        outStream.write(0);
        outStream.write(intToByteArray(this.mDuration), 0, 2);
        return outStream.toByteArray();
    }

    public byte[] genAdvDataMsgData(int filterIdx, byte advHandle) {
        if (this.mAdvData == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write(1);
        outStream.write(filterIdx);
        outStream.write(advHandle);
        outStream.write(3);
        outStream.write(1);
        byte[] advDataBytes = this.advHelper.advertiseDataToBytes(this.mAdvData, BluetoothAdapter.getDefaultAdapter().getName());
        AdvertisingSetParameters advertisingSetParameters = this.mAdvParams;
        if (advertisingSetParameters == null || !advertisingSetParameters.isConnectable()) {
            outStream.write(advDataBytes.length);
            outStream.write(advDataBytes, 0, advDataBytes.length);
        } else {
            outStream.write(advDataBytes.length + 3);
            outStream.write(2);
            outStream.write(1);
            outStream.write(2);
            outStream.write(advDataBytes, 0, advDataBytes.length);
        }
        return outStream.toByteArray();
    }

    public byte[] genRespDataMsgData(int filterIdx, byte advHandle) {
        if (this.mRespData == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.write(1);
        outStream.write(filterIdx);
        outStream.write(advHandle);
        outStream.write(3);
        outStream.write(1);
        outStream.write(0);
        byte[] scanResponseBytes = this.advHelper.advertiseDataToBytes(this.mRespData, BluetoothAdapter.getDefaultAdapter().getName());
        outStream.write(scanResponseBytes.length);
        outStream.write(scanResponseBytes, 0, scanResponseBytes.length);
        return outStream.toByteArray();
    }

    private int genFieldBitMap() {
        int fieldBitMap = 0;
        if (this.mScanSettings != null) {
            fieldBitMap = 0 | 2;
        }
        if (this.mScanFilterList != null) {
            fieldBitMap |= 4;
        }
        if (this.mAdvSettings != null) {
            fieldBitMap |= 8;
        }
        if (this.mAdvParams != null) {
            fieldBitMap |= 16;
        }
        if (this.mAdvData != null) {
            fieldBitMap |= 32;
        }
        if (this.mRespData != null) {
            fieldBitMap |= 64;
        }
        if (this.mDevInfoList != null) {
            return fieldBitMap | 128;
        }
        return fieldBitMap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSoftFilters(ScanSettings settings, List<ScanFilter> filter) {
        this.mScanSettings = settings;
        this.mScanFilterList = filter;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAdvParams(AdvertiseSettings advSettings, AdvertisingSetParameters advParams, int duration) {
        this.mAdvParams = advParams;
        this.mDuration = duration;
        this.mAdvSettings = advSettings;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAdvDataAndRespData(AdvertiseData advData, AdvertiseData respData) {
        this.mAdvData = advData;
        this.mRespData = respData;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActiveDevInfo(List<BluetoothAdvDeviceInfo> devInfoList) {
        this.mDevInfoList = devInfoList;
    }

    public static final class Builder {
        private AdvertiseData mAdvData = null;
        private AdvertisingSetParameters mAdvParams = null;
        private AdvertiseSettings mAdvSettings = null;
        private byte mDeliveryMode = BluetoothAdvFilterParamEx.DELIVERY_MODE_REPLY;
        private List<BluetoothAdvDeviceInfo> mDevInfoList = null;
        private int mDuration = 0;
        private AdvertiseData mRespData = null;
        private List<ScanFilter> mScanFilterList = null;
        private ScanSettings mScanSettings = null;
        private UUID mUuid = null;

        public Builder setUuid(UUID id) {
            this.mUuid = id;
            return this;
        }

        public Builder setDeliveryMode(byte deliveryMode) {
            this.mDeliveryMode = deliveryMode;
            return this;
        }

        public Builder setScanSettings(ScanSettings settings) {
            this.mScanSettings = settings;
            return this;
        }

        public Builder setScanFilter(List<ScanFilter> filter) {
            this.mScanFilterList = filter;
            return this;
        }

        public Builder setAdvParams(AdvertisingSetParameters params) {
            this.mAdvParams = params;
            return this;
        }

        public Builder setAdvSetting(AdvertiseSettings settings) {
            this.mAdvSettings = settings;
            return this;
        }

        public Builder setAdvData(AdvertiseData advData) {
            this.mAdvData = advData;
            return this;
        }

        public Builder setRespData(AdvertiseData respData) {
            this.mRespData = respData;
            return this;
        }

        public Builder setActiveDevInfo(List<BluetoothAdvDeviceInfo> devInfo) {
            this.mDevInfoList = new ArrayList();
            this.mDevInfoList.addAll(devInfo);
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public BluetoothAdvFilterParamEx build() {
            UUID uuid = this.mUuid;
            if (uuid == null) {
                return null;
            }
            BluetoothAdvFilterParamEx filterExtParam = new BluetoothAdvFilterParamEx(uuid, this.mDeliveryMode);
            filterExtParam.setSoftFilters(this.mScanSettings, this.mScanFilterList);
            filterExtParam.setAdvParams(this.mAdvSettings, this.mAdvParams, this.mDuration);
            filterExtParam.setAdvDataAndRespData(this.mAdvData, this.mRespData);
            filterExtParam.setActiveDevInfo(this.mDevInfoList);
            return filterExtParam;
        }
    }

    class AdvertiseHelper {
        private static final int COMPLETE_LIST_128_BIT_SERVICE_UUIDS = 7;
        private static final int COMPLETE_LIST_16_BIT_SERVICE_UUIDS = 3;
        private static final int COMPLETE_LIST_32_BIT_SERVICE_UUIDS = 5;
        private static final int COMPLETE_LOCAL_NAME = 9;
        private static final int DEVICE_NAME_MAX = 26;
        private static final int MANUFACTURER_SPECIFIC_DATA = 255;
        private static final int SERVICE_DATA_128_BIT_UUID = 33;
        private static final int SERVICE_DATA_16_BIT_UUID = 22;
        private static final int SERVICE_DATA_32_BIT_UUID = 32;
        private static final int SHORTENED_LOCAL_NAME = 8;
        private static final String TAG = "AdvertiseHelper";
        private static final int TX_POWER_LEVEL = 10;

        AdvertiseHelper() {
        }

        private void writeDeviceName(AdvertiseData data, String name, ByteArrayOutputStream outStream) {
            byte type;
            if (data.getIncludeDeviceName()) {
                try {
                    byte[] nameBytes = name.getBytes("UTF-8");
                    int nameLength = nameBytes.length;
                    if (nameLength > 26) {
                        nameLength = 26;
                        type = 8;
                    } else {
                        type = 9;
                    }
                    outStream.write(nameLength + 1);
                    outStream.write(type);
                    outStream.write(nameBytes, 0, nameLength);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Can't include name - encoding error!", e);
                }
            }
        }

        private void writeManufacturerSpecificData(AdvertiseData data, ByteArrayOutputStream outStream) {
            for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
                int manufacturerId = data.getManufacturerSpecificData().keyAt(i);
                byte[] manufacturerData = data.getManufacturerSpecificData().get(manufacturerId);
                byte[] concated = new byte[((manufacturerData == null ? 0 : manufacturerData.length) + 2)];
                concated[0] = (byte) (manufacturerId & 255);
                concated[1] = (byte) ((manufacturerId >> 8) & 255);
                if (manufacturerData != null) {
                    System.arraycopy(manufacturerData, 0, concated, 2, manufacturerData.length);
                }
                outStream.write(concated.length + 1);
                outStream.write(255);
                outStream.write(concated, 0, concated.length);
            }
        }

        private void writeTxPowerLevel(AdvertiseData data, ByteArrayOutputStream outStream) {
            if (data.getIncludeTxPowerLevel()) {
                outStream.write(2);
                outStream.write(10);
                outStream.write(0);
            }
        }

        private void writeServiceUuids(AdvertiseData data, ByteArrayOutputStream outStream) {
            if (data.getServiceUuids() != null) {
                ByteArrayOutputStream serviceUuids16 = new ByteArrayOutputStream();
                ByteArrayOutputStream serviceUuids32 = new ByteArrayOutputStream();
                ByteArrayOutputStream serviceUuids128 = new ByteArrayOutputStream();
                for (ParcelUuid parcelUuid : data.getServiceUuids()) {
                    byte[] uuid = BluetoothUuid.uuidToBytes(parcelUuid);
                    if (uuid.length == 2) {
                        serviceUuids16.write(uuid, 0, uuid.length);
                    } else if (uuid.length == 4) {
                        serviceUuids32.write(uuid, 0, uuid.length);
                    } else {
                        serviceUuids128.write(uuid, 0, uuid.length);
                    }
                }
                if (serviceUuids16.size() != 0) {
                    outStream.write(serviceUuids16.size() + 1);
                    outStream.write(3);
                    outStream.write(serviceUuids16.toByteArray(), 0, serviceUuids16.size());
                }
                if (serviceUuids32.size() != 0) {
                    outStream.write(serviceUuids32.size() + 1);
                    outStream.write(5);
                    outStream.write(serviceUuids32.toByteArray(), 0, serviceUuids32.size());
                }
                if (serviceUuids128.size() != 0) {
                    outStream.write(serviceUuids128.size() + 1);
                    outStream.write(7);
                    outStream.write(serviceUuids128.toByteArray(), 0, serviceUuids128.size());
                }
            }
        }

        private void writeServiceData(AdvertiseData data, ByteArrayOutputStream outStream) {
            if (!data.getServiceData().isEmpty()) {
                for (ParcelUuid parcelUuid : data.getServiceData().keySet()) {
                    byte[] serviceData = data.getServiceData().get(parcelUuid);
                    byte[] uuid = BluetoothUuid.uuidToBytes(parcelUuid);
                    int uuidLen = uuid.length;
                    byte[] concated = new byte[((serviceData == null ? 0 : serviceData.length) + uuidLen)];
                    System.arraycopy(uuid, 0, concated, 0, uuidLen);
                    if (serviceData != null) {
                        System.arraycopy(serviceData, 0, concated, uuidLen, serviceData.length);
                    }
                    if (uuid.length == 2) {
                        outStream.write(concated.length + 1);
                        outStream.write(22);
                        outStream.write(concated, 0, concated.length);
                    } else if (uuid.length == 4) {
                        outStream.write(concated.length + 1);
                        outStream.write(32);
                        outStream.write(concated, 0, concated.length);
                    } else {
                        outStream.write(concated.length + 1);
                        outStream.write(33);
                        outStream.write(concated, 0, concated.length);
                    }
                }
            }
        }

        public byte[] advertiseDataToBytes(AdvertiseData data, String name) {
            if (data == null || name == null) {
                return new byte[0];
            }
            ByteArrayOutputStream ret = new ByteArrayOutputStream();
            writeDeviceName(data, name, ret);
            writeManufacturerSpecificData(data, ret);
            writeTxPowerLevel(data, ret);
            writeServiceUuids(data, ret);
            writeServiceData(data, ret);
            return ret.toByteArray();
        }
    }
}
