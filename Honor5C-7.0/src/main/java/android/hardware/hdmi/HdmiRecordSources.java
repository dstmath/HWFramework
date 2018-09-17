package android.hardware.hdmi;

import android.graphics.Color;
import android.net.wifi.ScanResult.InformationElement;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

public final class HdmiRecordSources {
    public static final int ANALOGUE_BROADCAST_TYPE_CABLE = 0;
    public static final int ANALOGUE_BROADCAST_TYPE_SATELLITE = 1;
    public static final int ANALOGUE_BROADCAST_TYPE_TERRESTRIAL = 2;
    public static final int BROADCAST_SYSTEM_NTSC_M = 3;
    public static final int BROADCAST_SYSTEM_PAL_BG = 0;
    public static final int BROADCAST_SYSTEM_PAL_DK = 8;
    public static final int BROADCAST_SYSTEM_PAL_I = 4;
    public static final int BROADCAST_SYSTEM_PAL_M = 2;
    public static final int BROADCAST_SYSTEM_PAL_OTHER_SYSTEM = 31;
    public static final int BROADCAST_SYSTEM_SECAM_BG = 6;
    public static final int BROADCAST_SYSTEM_SECAM_DK = 5;
    public static final int BROADCAST_SYSTEM_SECAM_L = 7;
    public static final int BROADCAST_SYSTEM_SECAM_LP = 1;
    private static final int CHANNEL_NUMBER_FORMAT_1_PART = 1;
    private static final int CHANNEL_NUMBER_FORMAT_2_PART = 2;
    public static final int DIGITAL_BROADCAST_TYPE_ARIB = 0;
    public static final int DIGITAL_BROADCAST_TYPE_ARIB_BS = 8;
    public static final int DIGITAL_BROADCAST_TYPE_ARIB_CS = 9;
    public static final int DIGITAL_BROADCAST_TYPE_ARIB_T = 10;
    public static final int DIGITAL_BROADCAST_TYPE_ATSC = 1;
    public static final int DIGITAL_BROADCAST_TYPE_ATSC_CABLE = 16;
    public static final int DIGITAL_BROADCAST_TYPE_ATSC_SATELLITE = 17;
    public static final int DIGITAL_BROADCAST_TYPE_ATSC_TERRESTRIAL = 18;
    public static final int DIGITAL_BROADCAST_TYPE_DVB = 2;
    public static final int DIGITAL_BROADCAST_TYPE_DVB_C = 24;
    public static final int DIGITAL_BROADCAST_TYPE_DVB_S = 25;
    public static final int DIGITAL_BROADCAST_TYPE_DVB_S2 = 26;
    public static final int DIGITAL_BROADCAST_TYPE_DVB_T = 27;
    private static final int RECORD_SOURCE_TYPE_ANALOGUE_SERVICE = 3;
    private static final int RECORD_SOURCE_TYPE_DIGITAL_SERVICE = 2;
    private static final int RECORD_SOURCE_TYPE_EXTERNAL_PHYSICAL_ADDRESS = 5;
    private static final int RECORD_SOURCE_TYPE_EXTERNAL_PLUG = 4;
    private static final int RECORD_SOURCE_TYPE_OWN_SOURCE = 1;
    private static final String TAG = "HdmiRecordSources";

    public static abstract class RecordSource {
        final int mExtraDataSize;
        final int mSourceType;

        abstract int extraParamToByteArray(byte[] bArr, int i);

        RecordSource(int sourceType, int extraDataSize) {
            this.mSourceType = sourceType;
            this.mExtraDataSize = extraDataSize;
        }

        final int getDataSize(boolean includeType) {
            return includeType ? this.mExtraDataSize + HdmiRecordSources.RECORD_SOURCE_TYPE_OWN_SOURCE : this.mExtraDataSize;
        }

        final int toByteArray(boolean includeType, byte[] data, int index) {
            if (includeType) {
                int index2 = index + HdmiRecordSources.RECORD_SOURCE_TYPE_OWN_SOURCE;
                data[index] = (byte) this.mSourceType;
                index = index2;
            }
            extraParamToByteArray(data, index);
            return getDataSize(includeType);
        }
    }

    public static final class AnalogueServiceSource extends RecordSource {
        static final int EXTRA_DATA_SIZE = 4;
        private final int mBroadcastSystem;
        private final int mBroadcastType;
        private final int mFrequency;

        private AnalogueServiceSource(int broadcastType, int frequency, int broadcastSystem) {
            super(HdmiRecordSources.RECORD_SOURCE_TYPE_ANALOGUE_SERVICE, EXTRA_DATA_SIZE);
            this.mBroadcastType = broadcastType;
            this.mFrequency = frequency;
            this.mBroadcastSystem = broadcastSystem;
        }

        int extraParamToByteArray(byte[] data, int index) {
            data[index] = (byte) this.mBroadcastType;
            HdmiRecordSources.shortToByteArray((short) this.mFrequency, data, index + HdmiRecordSources.RECORD_SOURCE_TYPE_OWN_SOURCE);
            data[index + HdmiRecordSources.RECORD_SOURCE_TYPE_ANALOGUE_SERVICE] = (byte) this.mBroadcastSystem;
            return EXTRA_DATA_SIZE;
        }
    }

    private interface DigitalServiceIdentification {
        int toByteArray(byte[] bArr, int i);
    }

    public static final class AribData implements DigitalServiceIdentification {
        private final int mOriginalNetworkId;
        private final int mServiceId;
        private final int mTransportStreamId;

        public AribData(int transportStreamId, int serviceId, int originalNetworkId) {
            this.mTransportStreamId = transportStreamId;
            this.mServiceId = serviceId;
            this.mOriginalNetworkId = originalNetworkId;
        }

        public int toByteArray(byte[] data, int index) {
            return HdmiRecordSources.threeFieldsToSixBytes(this.mTransportStreamId, this.mServiceId, this.mOriginalNetworkId, data, index);
        }
    }

    public static final class AtscData implements DigitalServiceIdentification {
        private final int mProgramNumber;
        private final int mTransportStreamId;

        public AtscData(int transportStreamId, int programNumber) {
            this.mTransportStreamId = transportStreamId;
            this.mProgramNumber = programNumber;
        }

        public int toByteArray(byte[] data, int index) {
            return HdmiRecordSources.threeFieldsToSixBytes(this.mTransportStreamId, this.mProgramNumber, HdmiRecordSources.DIGITAL_BROADCAST_TYPE_ARIB, data, index);
        }
    }

    private static final class ChannelIdentifier {
        private final int mChannelNumberFormat;
        private final int mMajorChannelNumber;
        private final int mMinorChannelNumber;

        private ChannelIdentifier(int format, int majorNumber, int minorNumer) {
            this.mChannelNumberFormat = format;
            this.mMajorChannelNumber = majorNumber;
            this.mMinorChannelNumber = minorNumer;
        }

        private int toByteArray(byte[] data, int index) {
            data[index] = (byte) ((this.mChannelNumberFormat << HdmiRecordSources.RECORD_SOURCE_TYPE_DIGITAL_SERVICE) | ((this.mMajorChannelNumber >>> HdmiRecordSources.DIGITAL_BROADCAST_TYPE_ARIB_BS) & HdmiRecordSources.RECORD_SOURCE_TYPE_ANALOGUE_SERVICE));
            data[index + HdmiRecordSources.RECORD_SOURCE_TYPE_OWN_SOURCE] = (byte) (this.mMajorChannelNumber & Process.PROC_TERM_MASK);
            HdmiRecordSources.shortToByteArray((short) this.mMinorChannelNumber, data, index + HdmiRecordSources.RECORD_SOURCE_TYPE_DIGITAL_SERVICE);
            return HdmiRecordSources.RECORD_SOURCE_TYPE_EXTERNAL_PLUG;
        }
    }

    public static final class DigitalChannelData implements DigitalServiceIdentification {
        private final ChannelIdentifier mChannelIdentifier;

        public static DigitalChannelData ofTwoNumbers(int majorNumber, int minorNumber) {
            return new DigitalChannelData(new ChannelIdentifier(majorNumber, minorNumber, null));
        }

        public static DigitalChannelData ofOneNumber(int number) {
            return new DigitalChannelData(new ChannelIdentifier(HdmiRecordSources.DIGITAL_BROADCAST_TYPE_ARIB, number, null));
        }

        private DigitalChannelData(ChannelIdentifier id) {
            this.mChannelIdentifier = id;
        }

        public int toByteArray(byte[] data, int index) {
            this.mChannelIdentifier.toByteArray(data, index);
            data[index + HdmiRecordSources.RECORD_SOURCE_TYPE_EXTERNAL_PLUG] = (byte) 0;
            data[index + HdmiRecordSources.RECORD_SOURCE_TYPE_EXTERNAL_PHYSICAL_ADDRESS] = (byte) 0;
            return HdmiRecordSources.BROADCAST_SYSTEM_SECAM_BG;
        }
    }

    public static final class DigitalServiceSource extends RecordSource {
        private static final int DIGITAL_SERVICE_IDENTIFIED_BY_CHANNEL = 1;
        private static final int DIGITAL_SERVICE_IDENTIFIED_BY_DIGITAL_ID = 0;
        static final int EXTRA_DATA_SIZE = 7;
        private final int mBroadcastSystem;
        private final DigitalServiceIdentification mIdentification;
        private final int mIdentificationMethod;

        private DigitalServiceSource(int identificatinoMethod, int broadcastSystem, DigitalServiceIdentification identification) {
            super(HdmiRecordSources.RECORD_SOURCE_TYPE_DIGITAL_SERVICE, EXTRA_DATA_SIZE);
            this.mIdentificationMethod = identificatinoMethod;
            this.mBroadcastSystem = broadcastSystem;
            this.mIdentification = identification;
        }

        int extraParamToByteArray(byte[] data, int index) {
            data[index] = (byte) ((this.mIdentificationMethod << EXTRA_DATA_SIZE) | (this.mBroadcastSystem & InformationElement.EID_EXTENDED_CAPS));
            this.mIdentification.toByteArray(data, index + DIGITAL_SERVICE_IDENTIFIED_BY_CHANNEL);
            return EXTRA_DATA_SIZE;
        }
    }

    public static final class DvbData implements DigitalServiceIdentification {
        private final int mOriginalNetworkId;
        private final int mServiceId;
        private final int mTransportStreamId;

        public DvbData(int transportStreamId, int serviceId, int originalNetworkId) {
            this.mTransportStreamId = transportStreamId;
            this.mServiceId = serviceId;
            this.mOriginalNetworkId = originalNetworkId;
        }

        public int toByteArray(byte[] data, int index) {
            return HdmiRecordSources.threeFieldsToSixBytes(this.mTransportStreamId, this.mServiceId, this.mOriginalNetworkId, data, index);
        }
    }

    public static final class ExternalPhysicalAddress extends RecordSource {
        static final int EXTRA_DATA_SIZE = 2;
        private final int mPhysicalAddress;

        private ExternalPhysicalAddress(int physicalAddress) {
            super(HdmiRecordSources.RECORD_SOURCE_TYPE_EXTERNAL_PHYSICAL_ADDRESS, EXTRA_DATA_SIZE);
            this.mPhysicalAddress = physicalAddress;
        }

        int extraParamToByteArray(byte[] data, int index) {
            HdmiRecordSources.shortToByteArray((short) this.mPhysicalAddress, data, index);
            return EXTRA_DATA_SIZE;
        }
    }

    public static final class ExternalPlugData extends RecordSource {
        static final int EXTRA_DATA_SIZE = 1;
        private final int mPlugNumber;

        private ExternalPlugData(int plugNumber) {
            super(HdmiRecordSources.RECORD_SOURCE_TYPE_EXTERNAL_PLUG, EXTRA_DATA_SIZE);
            this.mPlugNumber = plugNumber;
        }

        int extraParamToByteArray(byte[] data, int index) {
            data[index] = (byte) this.mPlugNumber;
            return EXTRA_DATA_SIZE;
        }
    }

    public static final class OwnSource extends RecordSource {
        private static final int EXTRA_DATA_SIZE = 0;

        private OwnSource() {
            super(HdmiRecordSources.RECORD_SOURCE_TYPE_OWN_SOURCE, HdmiRecordSources.DIGITAL_BROADCAST_TYPE_ARIB);
        }

        int extraParamToByteArray(byte[] data, int index) {
            return HdmiRecordSources.DIGITAL_BROADCAST_TYPE_ARIB;
        }
    }

    private HdmiRecordSources() {
    }

    public static OwnSource ofOwnSource() {
        return new OwnSource();
    }

    public static DigitalServiceSource ofDigitalChannelId(int broadcastSystem, DigitalChannelData data) {
        if (data == null) {
            throw new IllegalArgumentException("data should not be null.");
        }
        switch (broadcastSystem) {
            case DIGITAL_BROADCAST_TYPE_ARIB /*0*/:
            case RECORD_SOURCE_TYPE_OWN_SOURCE /*1*/:
            case RECORD_SOURCE_TYPE_DIGITAL_SERVICE /*2*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_BS /*8*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_CS /*9*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_T /*10*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_CABLE /*16*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_SATELLITE /*17*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_TERRESTRIAL /*18*/:
            case DIGITAL_BROADCAST_TYPE_DVB_C /*24*/:
            case DIGITAL_BROADCAST_TYPE_DVB_S /*25*/:
            case DIGITAL_BROADCAST_TYPE_DVB_S2 /*26*/:
            case DIGITAL_BROADCAST_TYPE_DVB_T /*27*/:
                return new DigitalServiceSource(broadcastSystem, data, null);
            default:
                Log.w(TAG, "Invalid broadcast type:" + broadcastSystem);
                throw new IllegalArgumentException("Invalid broadcast system value:" + broadcastSystem);
        }
    }

    public static DigitalServiceSource ofArib(int aribType, AribData data) {
        if (data == null) {
            throw new IllegalArgumentException("data should not be null.");
        }
        switch (aribType) {
            case DIGITAL_BROADCAST_TYPE_ARIB /*0*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_BS /*8*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_CS /*9*/:
            case DIGITAL_BROADCAST_TYPE_ARIB_T /*10*/:
                return new DigitalServiceSource(aribType, data, null);
            default:
                Log.w(TAG, "Invalid ARIB type:" + aribType);
                throw new IllegalArgumentException("type should not be null.");
        }
    }

    public static DigitalServiceSource ofAtsc(int atscType, AtscData data) {
        if (data == null) {
            throw new IllegalArgumentException("data should not be null.");
        }
        switch (atscType) {
            case RECORD_SOURCE_TYPE_OWN_SOURCE /*1*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_CABLE /*16*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_SATELLITE /*17*/:
            case DIGITAL_BROADCAST_TYPE_ATSC_TERRESTRIAL /*18*/:
                return new DigitalServiceSource(atscType, data, null);
            default:
                Log.w(TAG, "Invalid ATSC type:" + atscType);
                throw new IllegalArgumentException("Invalid ATSC type:" + atscType);
        }
    }

    public static DigitalServiceSource ofDvb(int dvbType, DvbData data) {
        if (data == null) {
            throw new IllegalArgumentException("data should not be null.");
        }
        switch (dvbType) {
            case RECORD_SOURCE_TYPE_DIGITAL_SERVICE /*2*/:
            case DIGITAL_BROADCAST_TYPE_DVB_C /*24*/:
            case DIGITAL_BROADCAST_TYPE_DVB_S /*25*/:
            case DIGITAL_BROADCAST_TYPE_DVB_S2 /*26*/:
            case DIGITAL_BROADCAST_TYPE_DVB_T /*27*/:
                return new DigitalServiceSource(dvbType, data, null);
            default:
                Log.w(TAG, "Invalid DVB type:" + dvbType);
                throw new IllegalArgumentException("Invalid DVB type:" + dvbType);
        }
    }

    public static AnalogueServiceSource ofAnalogue(int broadcastType, int frequency, int broadcastSystem) {
        if (broadcastType < 0 || broadcastType > RECORD_SOURCE_TYPE_DIGITAL_SERVICE) {
            Log.w(TAG, "Invalid Broadcast type:" + broadcastType);
            throw new IllegalArgumentException("Invalid Broadcast type:" + broadcastType);
        } else if (frequency < 0 || frequency > PowerManager.WAKE_LOCK_LEVEL_MASK) {
            Log.w(TAG, "Invalid frequency value[0x0000-0xFFFF]:" + frequency);
            throw new IllegalArgumentException("Invalid frequency value[0x0000-0xFFFF]:" + frequency);
        } else if (broadcastSystem >= 0 && broadcastSystem <= BROADCAST_SYSTEM_PAL_OTHER_SYSTEM) {
            return new AnalogueServiceSource(frequency, broadcastSystem, null);
        } else {
            Log.w(TAG, "Invalid Broadcast system:" + broadcastSystem);
            throw new IllegalArgumentException("Invalid Broadcast system:" + broadcastSystem);
        }
    }

    public static ExternalPlugData ofExternalPlug(int plugNumber) {
        if (plugNumber >= RECORD_SOURCE_TYPE_OWN_SOURCE && plugNumber <= Process.PROC_TERM_MASK) {
            return new ExternalPlugData(null);
        }
        Log.w(TAG, "Invalid plug number[1-255]" + plugNumber);
        throw new IllegalArgumentException("Invalid plug number[1-255]" + plugNumber);
    }

    public static ExternalPhysicalAddress ofExternalPhysicalAddress(int physicalAddress) {
        if ((Color.RED & physicalAddress) == 0) {
            return new ExternalPhysicalAddress(null);
        }
        Log.w(TAG, "Invalid physical address:" + physicalAddress);
        throw new IllegalArgumentException("Invalid physical address:" + physicalAddress);
    }

    private static int threeFieldsToSixBytes(int first, int second, int third, byte[] data, int index) {
        shortToByteArray((short) first, data, index);
        shortToByteArray((short) second, data, index + RECORD_SOURCE_TYPE_DIGITAL_SERVICE);
        shortToByteArray((short) third, data, index + RECORD_SOURCE_TYPE_EXTERNAL_PLUG);
        return BROADCAST_SYSTEM_SECAM_BG;
    }

    private static int shortToByteArray(short value, byte[] byteArray, int index) {
        byteArray[index] = (byte) ((value >>> DIGITAL_BROADCAST_TYPE_ARIB_BS) & Process.PROC_TERM_MASK);
        byteArray[index + RECORD_SOURCE_TYPE_OWN_SOURCE] = (byte) (value & Process.PROC_TERM_MASK);
        return RECORD_SOURCE_TYPE_DIGITAL_SERVICE;
    }

    public static boolean checkRecordSource(byte[] recordSource) {
        boolean z = true;
        if (recordSource == null || recordSource.length == 0) {
            return false;
        }
        int extraDataSize = recordSource.length - 1;
        switch (recordSource[DIGITAL_BROADCAST_TYPE_ARIB]) {
            case RECORD_SOURCE_TYPE_OWN_SOURCE /*1*/:
                if (extraDataSize != 0) {
                    z = false;
                }
                return z;
            case RECORD_SOURCE_TYPE_DIGITAL_SERVICE /*2*/:
                if (extraDataSize != BROADCAST_SYSTEM_SECAM_L) {
                    z = false;
                }
                return z;
            case RECORD_SOURCE_TYPE_ANALOGUE_SERVICE /*3*/:
                if (extraDataSize != RECORD_SOURCE_TYPE_EXTERNAL_PLUG) {
                    z = false;
                }
                return z;
            case RECORD_SOURCE_TYPE_EXTERNAL_PLUG /*4*/:
                if (extraDataSize != RECORD_SOURCE_TYPE_OWN_SOURCE) {
                    z = false;
                }
                return z;
            case RECORD_SOURCE_TYPE_EXTERNAL_PHYSICAL_ADDRESS /*5*/:
                if (extraDataSize != RECORD_SOURCE_TYPE_DIGITAL_SERVICE) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }
}
