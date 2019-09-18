package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import android.util.ArrayMap;
import com.android.internal.annotations.VisibleForTesting;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AnswerToReset {
    private static final int B2_MASK = 2;
    private static final int B7_MASK = 64;
    public static final byte DIRECT_CONVENTION = 59;
    public static final byte EUICC_SUPPORTED = -126;
    private static final int EXTENDED_APDU_INDEX = 2;
    public static final int INTERFACE_BYTES_MASK = 240;
    public static final byte INVERSE_CONVENTION = 63;
    private static final String TAG = "AnswerToReset";
    private static final int TAG_CARD_CAPABILITIES = 7;
    public static final int TA_MASK = 16;
    public static final int TB_MASK = 32;
    public static final int TC_MASK = 64;
    public static final int TD_MASK = 128;
    public static final int T_MASK = 15;
    public static final int T_VALUE_FOR_GLOBAL_INTERFACE = 15;
    private static final boolean VDBG = false;
    private Byte mCheckByte;
    private byte mFormatByte;
    private HistoricalBytes mHistoricalBytes;
    private ArrayList<InterfaceByte> mInterfaceBytes = new ArrayList<>();
    private boolean mIsDirectConvention;
    private boolean mIsEuiccSupported;
    private boolean mOnlyTEqualsZero = true;

    public static class HistoricalBytes {
        private static final int LENGTH_MASK = 15;
        private static final int TAG_MASK = 240;
        private final byte mCategory;
        private final ArrayMap<Integer, byte[]> mNodes;
        private final byte[] mRawData;

        public byte getCategory() {
            return this.mCategory;
        }

        public byte[] getRawData() {
            return this.mRawData;
        }

        public byte[] getValue(int tag) {
            return this.mNodes.get(Integer.valueOf(tag));
        }

        /* access modifiers changed from: private */
        public static HistoricalBytes parseHistoricalBytes(byte[] originalData, int startIndex, int length) {
            if (length <= 0 || startIndex + length > originalData.length) {
                return null;
            }
            ArrayMap<Integer, byte[]> nodes = new ArrayMap<>();
            int index = startIndex + 1;
            while (index < startIndex + length && index > 0) {
                index = parseLtvNode(index, nodes, originalData, (startIndex + length) - 1);
            }
            if (index < 0) {
                return null;
            }
            byte[] rawData = new byte[length];
            System.arraycopy(originalData, startIndex, rawData, 0, length);
            return new HistoricalBytes(rawData, nodes, rawData[0]);
        }

        private HistoricalBytes(byte[] rawData, ArrayMap<Integer, byte[]> nodes, byte category) {
            this.mRawData = rawData;
            this.mNodes = nodes;
            this.mCategory = category;
        }

        private static int parseLtvNode(int index, ArrayMap<Integer, byte[]> nodes, byte[] data, int lastByteIndex) {
            if (index > lastByteIndex) {
                return -1;
            }
            int tag = (data[index] & 240) >> 4;
            int index2 = index + 1;
            int length = data[index] & 15;
            if (index2 + length > lastByteIndex + 1 || length == 0) {
                return -1;
            }
            byte[] value = new byte[length];
            System.arraycopy(data, index2, value, 0, length);
            nodes.put(Integer.valueOf(tag), value);
            return index2 + length;
        }
    }

    public static class InterfaceByte {
        private Byte mTA;
        private Byte mTB;
        private Byte mTC;
        private Byte mTD;

        public Byte getTA() {
            return this.mTA;
        }

        public Byte getTB() {
            return this.mTB;
        }

        public Byte getTC() {
            return this.mTC;
        }

        public Byte getTD() {
            return this.mTD;
        }

        public void setTA(Byte tA) {
            this.mTA = tA;
        }

        public void setTB(Byte tB) {
            this.mTB = tB;
        }

        public void setTC(Byte tC) {
            this.mTC = tC;
        }

        public void setTD(Byte tD) {
            this.mTD = tD;
        }

        private InterfaceByte() {
            this.mTA = null;
            this.mTB = null;
            this.mTC = null;
            this.mTD = null;
        }

        @VisibleForTesting
        public InterfaceByte(Byte tA, Byte tB, Byte tC, Byte tD) {
            this.mTA = tA;
            this.mTB = tB;
            this.mTC = tC;
            this.mTD = tD;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InterfaceByte ib = (InterfaceByte) o;
            if (!Objects.equals(this.mTA, ib.getTA()) || !Objects.equals(this.mTB, ib.getTB()) || !Objects.equals(this.mTC, ib.getTC()) || !Objects.equals(this.mTD, ib.getTD())) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.mTA, this.mTB, this.mTC, this.mTD});
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            sb.append("TA=");
            sb.append(AnswerToReset.byteToStringHex(this.mTA));
            sb.append(",");
            sb.append("TB=");
            sb.append(AnswerToReset.byteToStringHex(this.mTB));
            sb.append(",");
            sb.append("TC=");
            sb.append(AnswerToReset.byteToStringHex(this.mTC));
            sb.append(",");
            sb.append("TD=");
            sb.append(AnswerToReset.byteToStringHex(this.mTD));
            sb.append("}");
            return sb.toString();
        }
    }

    public static AnswerToReset parseAtr(String atr) {
        AnswerToReset answerToReset = new AnswerToReset();
        if (answerToReset.parseAtrString(atr)) {
            return answerToReset;
        }
        return null;
    }

    private AnswerToReset() {
    }

    /* access modifiers changed from: private */
    public static String byteToStringHex(Byte b) {
        if (b == null) {
            return null;
        }
        return IccUtils.byteToHex(b.byteValue());
    }

    private void checkIsEuiccSupported() {
        int i = 0;
        while (i < this.mInterfaceBytes.size() - 1) {
            if (this.mInterfaceBytes.get(i).getTD() == null || (this.mInterfaceBytes.get(i).getTD().byteValue() & 15) != 15 || this.mInterfaceBytes.get(i + 1).getTB() == null || this.mInterfaceBytes.get(i + 1).getTB().byteValue() != -126) {
                i++;
            } else {
                this.mIsEuiccSupported = true;
                return;
            }
        }
    }

    private int parseConventionByte(byte[] atrBytes, int index) {
        if (index >= atrBytes.length) {
            loge("Failed to read the convention byte.");
            return -1;
        }
        byte value = atrBytes[index];
        if (value == 59) {
            this.mIsDirectConvention = true;
        } else if (value == 63) {
            this.mIsDirectConvention = false;
        } else {
            loge("Unrecognized convention byte " + IccUtils.byteToHex(value));
            return -1;
        }
        return index + 1;
    }

    private int parseFormatByte(byte[] atrBytes, int index) {
        if (index >= atrBytes.length) {
            loge("Failed to read the format byte.");
            return -1;
        }
        this.mFormatByte = atrBytes[index];
        return index + 1;
    }

    private int parseInterfaceBytes(byte[] atrBytes, int index) {
        byte lastTD = this.mFormatByte;
        while ((lastTD & 240) != 0) {
            InterfaceByte interfaceByte = new InterfaceByte();
            if ((lastTD & 16) != 0) {
                if (index >= atrBytes.length) {
                    loge("Failed to read the byte for TA.");
                    return -1;
                }
                interfaceByte.setTA(Byte.valueOf(atrBytes[index]));
                index++;
            }
            if ((lastTD & 32) != 0) {
                if (index >= atrBytes.length) {
                    loge("Failed to read the byte for TB.");
                    return -1;
                }
                interfaceByte.setTB(Byte.valueOf(atrBytes[index]));
                index++;
            }
            if ((lastTD & 64) != 0) {
                if (index >= atrBytes.length) {
                    loge("Failed to read the byte for TC.");
                    return -1;
                }
                interfaceByte.setTC(Byte.valueOf(atrBytes[index]));
                index++;
            }
            if ((lastTD & 128) != 0) {
                if (index >= atrBytes.length) {
                    loge("Failed to read the byte for TD.");
                    return -1;
                }
                interfaceByte.setTD(Byte.valueOf(atrBytes[index]));
                index++;
            }
            this.mInterfaceBytes.add(interfaceByte);
            Byte newTD = interfaceByte.getTD();
            if (newTD == null) {
                break;
            }
            lastTD = newTD.byteValue();
            if ((lastTD & 15) != 0) {
                this.mOnlyTEqualsZero = false;
            }
        }
        return index;
    }

    private int parseHistoricalBytes(byte[] atrBytes, int index) {
        int length = this.mFormatByte & 15;
        if (length + index > atrBytes.length) {
            loge("Failed to read the historical bytes.");
            return -1;
        }
        if (length > 0) {
            this.mHistoricalBytes = HistoricalBytes.parseHistoricalBytes(atrBytes, index, length);
        }
        return index + length;
    }

    private int parseCheckBytes(byte[] atrBytes, int index) {
        if (index < atrBytes.length) {
            this.mCheckByte = Byte.valueOf(atrBytes[index]);
            index++;
        } else if (!this.mOnlyTEqualsZero) {
            loge("Check byte must be present because T equals to values other than 0.");
            return -1;
        } else {
            log("Check byte can be absent because T=0.");
        }
        return index;
    }

    private boolean parseAtrString(String atr) {
        if (atr == null) {
            loge("The input ATR string can not be null");
            return false;
        } else if (atr.length() % 2 != 0) {
            loge("The length of input ATR string " + atr.length() + " is not even.");
            return false;
        } else if (atr.length() < 4) {
            loge("Valid ATR string must at least contains TS and T0.");
            return false;
        } else {
            byte[] atrBytes = IccUtils.hexStringToBytes(atr);
            if (atrBytes == null) {
                return false;
            }
            int index = parseConventionByte(atrBytes, 0);
            if (index == -1) {
                return false;
            }
            int index2 = parseFormatByte(atrBytes, index);
            if (index2 == -1) {
                return false;
            }
            int index3 = parseInterfaceBytes(atrBytes, index2);
            if (index3 == -1) {
                return false;
            }
            int index4 = parseHistoricalBytes(atrBytes, index3);
            if (index4 == -1) {
                return false;
            }
            int index5 = parseCheckBytes(atrBytes, index4);
            if (index5 == -1) {
                return false;
            }
            if (index5 != atrBytes.length) {
                loge("Unexpected bytes after the check byte.");
                return false;
            }
            log("Successfully parsed the ATR string " + atr + " into " + toString());
            checkIsEuiccSupported();
            return true;
        }
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(TAG, msg);
    }

    public byte getConventionByte() {
        return this.mIsDirectConvention ? DIRECT_CONVENTION : INVERSE_CONVENTION;
    }

    public byte getFormatByte() {
        return this.mFormatByte;
    }

    public List<InterfaceByte> getInterfaceBytes() {
        return this.mInterfaceBytes;
    }

    public HistoricalBytes getHistoricalBytes() {
        return this.mHistoricalBytes;
    }

    public Byte getCheckByte() {
        return this.mCheckByte;
    }

    public boolean isEuiccSupported() {
        return this.mIsEuiccSupported;
    }

    public boolean isExtendedApduSupported() {
        boolean z = false;
        if (this.mHistoricalBytes == null) {
            return false;
        }
        byte[] cardCapabilities = this.mHistoricalBytes.getValue(7);
        if (cardCapabilities == null || cardCapabilities.length < 3) {
            return false;
        }
        if (this.mIsDirectConvention) {
            if ((cardCapabilities[2] & 64) > 0) {
                z = true;
            }
            return z;
        }
        if ((cardCapabilities[2] & 2) > 0) {
            z = true;
        }
        return z;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnswerToReset:{");
        sb.append("mConventionByte=");
        sb.append(IccUtils.byteToHex(getConventionByte()));
        sb.append(",");
        sb.append("mFormatByte=");
        sb.append(byteToStringHex(Byte.valueOf(this.mFormatByte)));
        sb.append(",");
        sb.append("mInterfaceBytes={");
        Iterator<InterfaceByte> it = this.mInterfaceBytes.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString());
        }
        sb.append("},");
        sb.append("mHistoricalBytes={");
        if (this.mHistoricalBytes != null) {
            for (byte b : this.mHistoricalBytes.getRawData()) {
                sb.append(IccUtils.byteToHex(b));
                sb.append(",");
            }
        }
        sb.append("},");
        sb.append("mCheckByte=");
        sb.append(byteToStringHex(this.mCheckByte));
        sb.append("}");
        return sb.toString();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("AnswerToReset:");
        pw.println(toString());
        pw.flush();
    }
}
