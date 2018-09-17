package com.android.internal.telephony;

import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.util.HashMap;

public class WspTypeDecoder extends AbstractWspTypeDecoder {
    public static final String CONTENT_TYPE_B_MMS = "application/vnd.wap.mms-message";
    public static final String CONTENT_TYPE_B_PUSH_CO = "application/vnd.wap.coc";
    public static final String CONTENT_TYPE_B_PUSH_SYNCML_NOTI = "application/vnd.syncml.notification";
    public static final int PARAMETER_ID_X_WAP_APPLICATION_ID = 47;
    public static final int PDU_TYPE_CONFIRMED_PUSH = 7;
    public static final int PDU_TYPE_PUSH = 6;
    private static final int Q_VALUE = 0;
    private static final int WAP_PDU_LENGTH_QUOTE = 31;
    private static final int WAP_PDU_SHORT_LENGTH_MAX = 30;
    private static final HashMap<Integer, String> WELL_KNOWN_MIME_TYPES = null;
    private static final HashMap<Integer, String> WELL_KNOWN_PARAMETERS = null;
    HashMap<String, String> mContentParameters;
    int mDataLength;
    String mStringValue;
    long mUnsigned32bit;
    byte[] mWspData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.WspTypeDecoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.WspTypeDecoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.WspTypeDecoder.<clinit>():void");
    }

    public WspTypeDecoder(byte[] pdu) {
        this.mWspData = pdu;
    }

    public boolean decodeTextString(int startIndex) {
        int index = startIndex;
        while (this.mWspData[index] != null) {
            index++;
        }
        this.mDataLength = (index - startIndex) + 1;
        if (this.mWspData[startIndex] == 127) {
            this.mStringValue = new String(this.mWspData, startIndex + 1, this.mDataLength - 2);
        } else {
            this.mStringValue = new String(this.mWspData, startIndex, this.mDataLength - 1);
        }
        return true;
    }

    public boolean decodeTokenText(int startIndex) {
        int index = startIndex;
        while (this.mWspData[index] != null) {
            index++;
        }
        this.mDataLength = (index - startIndex) + 1;
        this.mStringValue = new String(this.mWspData, startIndex, this.mDataLength - 1);
        return true;
    }

    public boolean decodeShortInteger(int startIndex) {
        if ((this.mWspData[startIndex] & PduPart.P_Q) == 0) {
            return false;
        }
        this.mUnsigned32bit = (long) (this.mWspData[startIndex] & CallFailCause.INTERWORKING_UNSPECIFIED);
        this.mDataLength = 1;
        return true;
    }

    public boolean decodeLongInteger(int startIndex) {
        int lengthMultiOctet = this.mWspData[startIndex] & PduHeaders.STORE_STATUS_ERROR_END;
        if (lengthMultiOctet > WAP_PDU_SHORT_LENGTH_MAX) {
            return false;
        }
        this.mUnsigned32bit = 0;
        for (int i = 1; i <= lengthMultiOctet; i++) {
            this.mUnsigned32bit = (this.mUnsigned32bit << 8) | ((long) (this.mWspData[startIndex + i] & PduHeaders.STORE_STATUS_ERROR_END));
        }
        this.mDataLength = lengthMultiOctet + 1;
        return true;
    }

    public boolean decodeIntegerValue(int startIndex) {
        if (decodeShortInteger(startIndex)) {
            return true;
        }
        return decodeLongInteger(startIndex);
    }

    public boolean decodeUintvarInteger(int startIndex) {
        int index = startIndex;
        this.mUnsigned32bit = 0;
        while ((this.mWspData[index] & PduPart.P_Q) != 0) {
            if (index - startIndex >= 4) {
                return false;
            }
            this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & CallFailCause.INTERWORKING_UNSPECIFIED));
            index++;
        }
        this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & CallFailCause.INTERWORKING_UNSPECIFIED));
        this.mDataLength = (index - startIndex) + 1;
        return true;
    }

    public boolean decodeValueLength(int startIndex) {
        if ((this.mWspData[startIndex] & PduHeaders.STORE_STATUS_ERROR_END) > WAP_PDU_LENGTH_QUOTE) {
            return false;
        }
        if (this.mWspData[startIndex] < (byte) 31) {
            this.mUnsigned32bit = (long) this.mWspData[startIndex];
            this.mDataLength = 1;
        } else {
            decodeUintvarInteger(startIndex + 1);
            this.mDataLength++;
        }
        return true;
    }

    public boolean decodeExtensionMedia(int startIndex) {
        int index = startIndex;
        this.mDataLength = Q_VALUE;
        this.mStringValue = null;
        int length = this.mWspData.length;
        boolean rtrn = startIndex < length;
        while (index < length && this.mWspData[index] != null) {
            index++;
        }
        this.mDataLength = (index - startIndex) + 1;
        this.mStringValue = new String(this.mWspData, startIndex, this.mDataLength - 1);
        return rtrn;
    }

    public boolean decodeConstrainedEncoding(int startIndex) {
        if (!decodeShortInteger(startIndex)) {
            return decodeExtensionMedia(startIndex);
        }
        this.mStringValue = null;
        return true;
    }

    public boolean decodeContentType(int startIndex) {
        this.mContentParameters = new HashMap();
        try {
            if (decodeValueLength(startIndex)) {
                int headersLength = (int) this.mUnsigned32bit;
                int mediaPrefixLength = getDecodedDataLength();
                if (decodeForConnectwb(startIndex, mediaPrefixLength)) {
                    return true;
                }
                int readLength;
                long wellKnownValue;
                String mimeType;
                if (decodeIntegerValue(startIndex + mediaPrefixLength)) {
                    this.mDataLength += mediaPrefixLength;
                    readLength = this.mDataLength;
                    this.mStringValue = null;
                    expandWellKnownMimeType();
                    wellKnownValue = this.mUnsigned32bit;
                    mimeType = this.mStringValue;
                    if (!readContentParameters(this.mDataLength + startIndex, headersLength - (this.mDataLength - mediaPrefixLength), Q_VALUE)) {
                        return false;
                    }
                    this.mDataLength += readLength;
                    this.mUnsigned32bit = wellKnownValue;
                    this.mStringValue = mimeType;
                    return true;
                }
                if (decodeExtensionMedia(startIndex + mediaPrefixLength)) {
                    this.mDataLength += mediaPrefixLength;
                    readLength = this.mDataLength;
                    expandWellKnownMimeType();
                    wellKnownValue = this.mUnsigned32bit;
                    mimeType = this.mStringValue;
                    if (readContentParameters(this.mDataLength + startIndex, headersLength - (this.mDataLength - mediaPrefixLength), Q_VALUE)) {
                        this.mDataLength += readLength;
                        this.mUnsigned32bit = wellKnownValue;
                        this.mStringValue = mimeType;
                        return true;
                    }
                }
                return false;
            }
            boolean found = decodeConstrainedEncoding(startIndex);
            if (found) {
                expandWellKnownMimeType();
            }
            return found;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean readContentParameters(int startIndex, int leftToRead, int accumulator) {
        if (leftToRead > 0) {
            String param;
            int totalRead;
            Object value;
            byte nextByte = this.mWspData[startIndex];
            if ((nextByte & PduPart.P_Q) == 0 && nextByte > WAP_PDU_LENGTH_QUOTE) {
                decodeTokenText(startIndex);
                param = this.mStringValue;
                totalRead = this.mDataLength + Q_VALUE;
            } else if (!decodeIntegerValue(startIndex)) {
                return false;
            } else {
                totalRead = this.mDataLength + Q_VALUE;
                int wellKnownParameterValue = (int) this.mUnsigned32bit;
                param = (String) WELL_KNOWN_PARAMETERS.get(Integer.valueOf(wellKnownParameterValue));
                if (param == null) {
                    param = "unassigned/0x" + Long.toHexString((long) wellKnownParameterValue);
                }
                if (wellKnownParameterValue == 0) {
                    if (!decodeUintvarInteger(startIndex + totalRead)) {
                        return false;
                    }
                    totalRead += this.mDataLength;
                    this.mContentParameters.put(param, String.valueOf(this.mUnsigned32bit));
                    return readContentParameters(startIndex + totalRead, leftToRead - totalRead, accumulator + totalRead);
                }
            }
            if (decodeNoValue(startIndex + totalRead)) {
                totalRead += this.mDataLength;
                value = null;
            } else if (decodeIntegerValue(startIndex + totalRead)) {
                totalRead += this.mDataLength;
                value = String.valueOf((int) this.mUnsigned32bit);
            } else {
                decodeTokenText(startIndex + totalRead);
                totalRead += this.mDataLength;
                value = this.mStringValue;
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                }
            }
            this.mContentParameters.put(param, value);
            return readContentParameters(startIndex + totalRead, leftToRead - totalRead, accumulator + totalRead);
        }
        this.mDataLength = accumulator;
        return true;
    }

    private boolean decodeNoValue(int startIndex) {
        if (this.mWspData[startIndex] != null) {
            return false;
        }
        this.mDataLength = 1;
        return true;
    }

    private void expandWellKnownMimeType() {
        if (this.mStringValue == null) {
            this.mStringValue = (String) WELL_KNOWN_MIME_TYPES.get(Integer.valueOf((int) this.mUnsigned32bit));
            return;
        }
        this.mUnsigned32bit = -1;
    }

    public boolean decodeContentLength(int startIndex) {
        return decodeIntegerValue(startIndex);
    }

    public boolean decodeContentLocation(int startIndex) {
        return decodeTextString(startIndex);
    }

    public boolean decodeXWapApplicationId(int startIndex) {
        if (!decodeIntegerValue(startIndex)) {
            return decodeTextString(startIndex);
        }
        this.mStringValue = null;
        return true;
    }

    public boolean seekXWapApplicationId(int startIndex, int endIndex) {
        int i = startIndex;
        i = startIndex;
        while (i <= endIndex) {
            try {
                if (decodeIntegerValue(i)) {
                    if (((int) getValue32()) == PARAMETER_ID_X_WAP_APPLICATION_ID) {
                        this.mUnsigned32bit = (long) (i + 1);
                        return true;
                    }
                } else if (!decodeTextString(i)) {
                    return false;
                }
                i += getDecodedDataLength();
                if (i > endIndex) {
                    return false;
                }
                byte val = this.mWspData[i];
                if (val >= null && val <= WAP_PDU_SHORT_LENGTH_MAX) {
                    i += this.mWspData[i] + 1;
                } else if (val == (byte) 31) {
                    if (i + 1 >= endIndex) {
                        return false;
                    }
                    i++;
                    if (!decodeUintvarInteger(i)) {
                        return false;
                    }
                    i += getDecodedDataLength();
                } else if ((byte) 31 >= val || val > 127) {
                    i++;
                } else if (!decodeTextString(i)) {
                    return false;
                } else {
                    i += getDecodedDataLength();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }
        return false;
    }

    public boolean decodeXWapContentURI(int startIndex) {
        return decodeTextString(startIndex);
    }

    public boolean decodeXWapInitiatorURI(int startIndex) {
        return decodeTextString(startIndex);
    }

    public int getDecodedDataLength() {
        return this.mDataLength;
    }

    public long getValue32() {
        return this.mUnsigned32bit;
    }

    public String getValueString() {
        return this.mStringValue;
    }

    public HashMap<String, String> getContentParameters() {
        return this.mContentParameters;
    }
}
