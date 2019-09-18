package com.android.internal.telephony;

import com.google.android.mms.ContentType;
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
    private static final HashMap<Integer, String> WELL_KNOWN_MIME_TYPES = new HashMap<>();
    private static final HashMap<Integer, String> WELL_KNOWN_PARAMETERS = new HashMap<>();
    HashMap<String, String> mContentParameters;
    int mDataLength;
    String mStringValue;
    long mUnsigned32bit;
    byte[] mWspData;

    static {
        WELL_KNOWN_MIME_TYPES.put(0, "*/*");
        WELL_KNOWN_MIME_TYPES.put(1, "text/*");
        WELL_KNOWN_MIME_TYPES.put(2, ContentType.TEXT_HTML);
        WELL_KNOWN_MIME_TYPES.put(3, ContentType.TEXT_PLAIN);
        WELL_KNOWN_MIME_TYPES.put(4, "text/x-hdml");
        WELL_KNOWN_MIME_TYPES.put(5, "text/x-ttml");
        WELL_KNOWN_MIME_TYPES.put(6, ContentType.TEXT_VCALENDAR);
        WELL_KNOWN_MIME_TYPES.put(7, ContentType.TEXT_VCARD);
        WELL_KNOWN_MIME_TYPES.put(8, "text/vnd.wap.wml");
        WELL_KNOWN_MIME_TYPES.put(9, "text/vnd.wap.wmlscript");
        WELL_KNOWN_MIME_TYPES.put(10, "text/vnd.wap.wta-event");
        WELL_KNOWN_MIME_TYPES.put(11, "multipart/*");
        WELL_KNOWN_MIME_TYPES.put(12, "multipart/mixed");
        WELL_KNOWN_MIME_TYPES.put(13, "multipart/form-data");
        WELL_KNOWN_MIME_TYPES.put(14, "multipart/byterantes");
        WELL_KNOWN_MIME_TYPES.put(15, "multipart/alternative");
        WELL_KNOWN_MIME_TYPES.put(16, "application/*");
        WELL_KNOWN_MIME_TYPES.put(17, "application/java-vm");
        WELL_KNOWN_MIME_TYPES.put(18, "application/x-www-form-urlencoded");
        WELL_KNOWN_MIME_TYPES.put(19, "application/x-hdmlc");
        WELL_KNOWN_MIME_TYPES.put(20, "application/vnd.wap.wmlc");
        WELL_KNOWN_MIME_TYPES.put(21, "application/vnd.wap.wmlscriptc");
        WELL_KNOWN_MIME_TYPES.put(22, "application/vnd.wap.wta-eventc");
        WELL_KNOWN_MIME_TYPES.put(23, "application/vnd.wap.uaprof");
        WELL_KNOWN_MIME_TYPES.put(24, "application/vnd.wap.wtls-ca-certificate");
        WELL_KNOWN_MIME_TYPES.put(25, "application/vnd.wap.wtls-user-certificate");
        WELL_KNOWN_MIME_TYPES.put(26, "application/x-x509-ca-cert");
        WELL_KNOWN_MIME_TYPES.put(27, "application/x-x509-user-cert");
        WELL_KNOWN_MIME_TYPES.put(28, ContentType.IMAGE_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(29, ContentType.IMAGE_GIF);
        WELL_KNOWN_MIME_TYPES.put(30, ContentType.IMAGE_JPEG);
        WELL_KNOWN_MIME_TYPES.put(31, "image/tiff");
        WELL_KNOWN_MIME_TYPES.put(32, ContentType.IMAGE_PNG);
        WELL_KNOWN_MIME_TYPES.put(33, ContentType.IMAGE_WBMP);
        WELL_KNOWN_MIME_TYPES.put(34, "application/vnd.wap.multipart.*");
        WELL_KNOWN_MIME_TYPES.put(35, ContentType.MULTIPART_MIXED);
        WELL_KNOWN_MIME_TYPES.put(36, "application/vnd.wap.multipart.form-data");
        WELL_KNOWN_MIME_TYPES.put(37, "application/vnd.wap.multipart.byteranges");
        WELL_KNOWN_MIME_TYPES.put(38, ContentType.MULTIPART_ALTERNATIVE);
        WELL_KNOWN_MIME_TYPES.put(39, "application/xml");
        WELL_KNOWN_MIME_TYPES.put(40, "text/xml");
        WELL_KNOWN_MIME_TYPES.put(41, "application/vnd.wap.wbxml");
        WELL_KNOWN_MIME_TYPES.put(42, "application/x-x968-cross-cert");
        WELL_KNOWN_MIME_TYPES.put(43, "application/x-x968-ca-cert");
        WELL_KNOWN_MIME_TYPES.put(44, "application/x-x968-user-cert");
        WELL_KNOWN_MIME_TYPES.put(45, "text/vnd.wap.si");
        WELL_KNOWN_MIME_TYPES.put(46, "application/vnd.wap.sic");
        WELL_KNOWN_MIME_TYPES.put(47, "text/vnd.wap.sl");
        WELL_KNOWN_MIME_TYPES.put(48, "application/vnd.wap.slc");
        WELL_KNOWN_MIME_TYPES.put(49, "text/vnd.wap.co");
        WELL_KNOWN_MIME_TYPES.put(50, CONTENT_TYPE_B_PUSH_CO);
        WELL_KNOWN_MIME_TYPES.put(51, ContentType.MULTIPART_RELATED);
        WELL_KNOWN_MIME_TYPES.put(52, "application/vnd.wap.sia");
        WELL_KNOWN_MIME_TYPES.put(53, "text/vnd.wap.connectivity-xml");
        WELL_KNOWN_MIME_TYPES.put(54, "application/vnd.wap.connectivity-wbxml");
        WELL_KNOWN_MIME_TYPES.put(55, "application/pkcs7-mime");
        WELL_KNOWN_MIME_TYPES.put(56, "application/vnd.wap.hashed-certificate");
        WELL_KNOWN_MIME_TYPES.put(57, "application/vnd.wap.signed-certificate");
        WELL_KNOWN_MIME_TYPES.put(58, "application/vnd.wap.cert-response");
        WELL_KNOWN_MIME_TYPES.put(59, ContentType.APP_XHTML);
        WELL_KNOWN_MIME_TYPES.put(60, "application/wml+xml");
        WELL_KNOWN_MIME_TYPES.put(61, "text/css");
        WELL_KNOWN_MIME_TYPES.put(62, "application/vnd.wap.mms-message");
        WELL_KNOWN_MIME_TYPES.put(63, "application/vnd.wap.rollover-certificate");
        WELL_KNOWN_MIME_TYPES.put(64, "application/vnd.wap.locc+wbxml");
        WELL_KNOWN_MIME_TYPES.put(65, "application/vnd.wap.loc+xml");
        WELL_KNOWN_MIME_TYPES.put(66, "application/vnd.syncml.dm+wbxml");
        WELL_KNOWN_MIME_TYPES.put(67, "application/vnd.syncml.dm+xml");
        WELL_KNOWN_MIME_TYPES.put(68, CONTENT_TYPE_B_PUSH_SYNCML_NOTI);
        WELL_KNOWN_MIME_TYPES.put(69, ContentType.APP_WAP_XHTML);
        WELL_KNOWN_MIME_TYPES.put(70, "application/vnd.wv.csp.cir");
        WELL_KNOWN_MIME_TYPES.put(71, "application/vnd.oma.dd+xml");
        WELL_KNOWN_MIME_TYPES.put(72, "application/vnd.oma.drm.message");
        WELL_KNOWN_MIME_TYPES.put(73, ContentType.APP_DRM_CONTENT);
        WELL_KNOWN_MIME_TYPES.put(74, "application/vnd.oma.drm.rights+xml");
        WELL_KNOWN_MIME_TYPES.put(75, "application/vnd.oma.drm.rights+wbxml");
        WELL_KNOWN_MIME_TYPES.put(76, "application/vnd.wv.csp+xml");
        WELL_KNOWN_MIME_TYPES.put(77, "application/vnd.wv.csp+wbxml");
        WELL_KNOWN_MIME_TYPES.put(78, "application/vnd.syncml.ds.notification");
        WELL_KNOWN_MIME_TYPES.put(79, ContentType.AUDIO_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(80, ContentType.VIDEO_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(81, "application/vnd.oma.dd2+xml");
        WELL_KNOWN_MIME_TYPES.put(82, "application/mikey");
        WELL_KNOWN_MIME_TYPES.put(83, "application/vnd.oma.dcd");
        WELL_KNOWN_MIME_TYPES.put(84, "application/vnd.oma.dcdc");
        WELL_KNOWN_MIME_TYPES.put(513, "application/vnd.uplanet.cacheop-wbxml");
        WELL_KNOWN_MIME_TYPES.put(514, "application/vnd.uplanet.signal");
        WELL_KNOWN_MIME_TYPES.put(515, "application/vnd.uplanet.alert-wbxml");
        WELL_KNOWN_MIME_TYPES.put(516, "application/vnd.uplanet.list-wbxml");
        WELL_KNOWN_MIME_TYPES.put(517, "application/vnd.uplanet.listcmd-wbxml");
        WELL_KNOWN_MIME_TYPES.put(518, "application/vnd.uplanet.channel-wbxml");
        WELL_KNOWN_MIME_TYPES.put(519, "application/vnd.uplanet.provisioning-status-uri");
        WELL_KNOWN_MIME_TYPES.put(520, "x-wap.multipart/vnd.uplanet.header-set");
        WELL_KNOWN_MIME_TYPES.put(521, "application/vnd.uplanet.bearer-choice-wbxml");
        WELL_KNOWN_MIME_TYPES.put(522, "application/vnd.phonecom.mmc-wbxml");
        WELL_KNOWN_MIME_TYPES.put(523, "application/vnd.nokia.syncset+wbxml");
        WELL_KNOWN_MIME_TYPES.put(524, "image/x-up-wpng");
        WELL_KNOWN_MIME_TYPES.put(768, "application/iota.mmc-wbxml");
        WELL_KNOWN_MIME_TYPES.put(769, "application/iota.mmc-xml");
        WELL_KNOWN_MIME_TYPES.put(770, "application/vnd.syncml+xml");
        WELL_KNOWN_MIME_TYPES.put(771, "application/vnd.syncml+wbxml");
        WELL_KNOWN_MIME_TYPES.put(772, "text/vnd.wap.emn+xml");
        WELL_KNOWN_MIME_TYPES.put(773, "text/calendar");
        WELL_KNOWN_MIME_TYPES.put(774, "application/vnd.omads-email+xml");
        WELL_KNOWN_MIME_TYPES.put(775, "application/vnd.omads-file+xml");
        WELL_KNOWN_MIME_TYPES.put(776, "application/vnd.omads-folder+xml");
        WELL_KNOWN_MIME_TYPES.put(777, "text/directory;profile=vCard");
        WELL_KNOWN_MIME_TYPES.put(778, "application/vnd.wap.emn+wbxml");
        WELL_KNOWN_MIME_TYPES.put(779, "application/vnd.nokia.ipdc-purchase-response");
        WELL_KNOWN_MIME_TYPES.put(780, "application/vnd.motorola.screen3+xml");
        WELL_KNOWN_MIME_TYPES.put(781, "application/vnd.motorola.screen3+gzip");
        WELL_KNOWN_MIME_TYPES.put(782, "application/vnd.cmcc.setting+wbxml");
        WELL_KNOWN_MIME_TYPES.put(783, "application/vnd.cmcc.bombing+wbxml");
        WELL_KNOWN_MIME_TYPES.put(784, "application/vnd.docomo.pf");
        WELL_KNOWN_MIME_TYPES.put(785, "application/vnd.docomo.ub");
        WELL_KNOWN_MIME_TYPES.put(786, "application/vnd.omaloc-supl-init");
        WELL_KNOWN_MIME_TYPES.put(787, "application/vnd.oma.group-usage-list+xml");
        WELL_KNOWN_MIME_TYPES.put(788, "application/oma-directory+xml");
        WELL_KNOWN_MIME_TYPES.put(789, "application/vnd.docomo.pf2");
        WELL_KNOWN_MIME_TYPES.put(790, "application/vnd.oma.drm.roap-trigger+wbxml");
        WELL_KNOWN_MIME_TYPES.put(791, "application/vnd.sbm.mid2");
        WELL_KNOWN_MIME_TYPES.put(792, "application/vnd.wmf.bootstrap");
        WELL_KNOWN_MIME_TYPES.put(793, "application/vnc.cmcc.dcd+xml");
        WELL_KNOWN_MIME_TYPES.put(794, "application/vnd.sbm.cid");
        WELL_KNOWN_MIME_TYPES.put(795, "application/vnd.oma.bcast.provisioningtrigger");
        WELL_KNOWN_PARAMETERS.put(0, "Q");
        WELL_KNOWN_PARAMETERS.put(1, "Charset");
        WELL_KNOWN_PARAMETERS.put(2, "Level");
        WELL_KNOWN_PARAMETERS.put(3, "Type");
        WELL_KNOWN_PARAMETERS.put(7, "Differences");
        WELL_KNOWN_PARAMETERS.put(8, "Padding");
        WELL_KNOWN_PARAMETERS.put(9, "Type");
        WELL_KNOWN_PARAMETERS.put(14, "Max-Age");
        WELL_KNOWN_PARAMETERS.put(16, "Secure");
        WELL_KNOWN_PARAMETERS.put(17, "SEC");
        WELL_KNOWN_PARAMETERS.put(18, "MAC");
        WELL_KNOWN_PARAMETERS.put(19, "Creation-date");
        WELL_KNOWN_PARAMETERS.put(20, "Modification-date");
        WELL_KNOWN_PARAMETERS.put(21, "Read-date");
        WELL_KNOWN_PARAMETERS.put(22, "Size");
        WELL_KNOWN_PARAMETERS.put(23, "Name");
        WELL_KNOWN_PARAMETERS.put(24, "Filename");
        WELL_KNOWN_PARAMETERS.put(25, "Start");
        WELL_KNOWN_PARAMETERS.put(26, "Start-info");
        WELL_KNOWN_PARAMETERS.put(27, "Comment");
        WELL_KNOWN_PARAMETERS.put(28, "Domain");
        WELL_KNOWN_PARAMETERS.put(29, "Path");
    }

    public WspTypeDecoder(byte[] pdu) {
        this.mWspData = pdu;
    }

    public boolean decodeTextString(int startIndex) {
        int index = startIndex;
        while (this.mWspData[index] != 0) {
            index++;
        }
        this.mDataLength = (index - startIndex) + 1;
        if (this.mWspData[startIndex] == Byte.MAX_VALUE) {
            this.mStringValue = new String(this.mWspData, startIndex + 1, this.mDataLength - 2);
        } else {
            this.mStringValue = new String(this.mWspData, startIndex, this.mDataLength - 1);
        }
        return true;
    }

    public boolean decodeTokenText(int startIndex) {
        int index = startIndex;
        while (this.mWspData[index] != 0) {
            index++;
        }
        this.mDataLength = (index - startIndex) + 1;
        this.mStringValue = new String(this.mWspData, startIndex, this.mDataLength - 1);
        return true;
    }

    public boolean decodeShortInteger(int startIndex) {
        if ((this.mWspData[startIndex] & 128) == 0) {
            return false;
        }
        this.mUnsigned32bit = (long) (this.mWspData[startIndex] & Byte.MAX_VALUE);
        this.mDataLength = 1;
        return true;
    }

    public boolean decodeLongInteger(int startIndex) {
        int lengthMultiOctet = this.mWspData[startIndex] & 255;
        if (lengthMultiOctet > 30) {
            return false;
        }
        this.mUnsigned32bit = 0;
        for (int i = 1; i <= lengthMultiOctet; i++) {
            this.mUnsigned32bit = (this.mUnsigned32bit << 8) | ((long) (this.mWspData[startIndex + i] & 255));
        }
        this.mDataLength = 1 + lengthMultiOctet;
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
        while ((this.mWspData[index] & 128) != 0) {
            if (index - startIndex >= 4) {
                return false;
            }
            this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & Byte.MAX_VALUE));
            index++;
        }
        this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & Byte.MAX_VALUE));
        this.mDataLength = (index - startIndex) + 1;
        return true;
    }

    public boolean decodeValueLength(int startIndex) {
        if ((this.mWspData[startIndex] & 255) > 31) {
            return false;
        }
        if (this.mWspData[startIndex] < 31) {
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
        boolean rtrn = false;
        this.mDataLength = 0;
        this.mStringValue = null;
        int length = this.mWspData.length;
        if (index < length) {
            rtrn = true;
        }
        while (index < length && this.mWspData[index] != 0) {
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
        this.mContentParameters = new HashMap<>();
        try {
            if (!decodeValueLength(startIndex)) {
                boolean found = decodeConstrainedEncoding(startIndex);
                if (found) {
                    expandWellKnownMimeType();
                }
                return found;
            }
            int headersLength = (int) this.mUnsigned32bit;
            int mediaPrefixLength = getDecodedDataLength();
            if (decodeForConnectwb(startIndex, mediaPrefixLength)) {
                return true;
            }
            if (decodeIntegerValue(startIndex + mediaPrefixLength)) {
                this.mDataLength += mediaPrefixLength;
                int readLength = this.mDataLength;
                this.mStringValue = null;
                expandWellKnownMimeType();
                long wellKnownValue = this.mUnsigned32bit;
                String mimeType = this.mStringValue;
                if (!readContentParameters(this.mDataLength + startIndex, headersLength - (this.mDataLength - mediaPrefixLength), 0)) {
                    return false;
                }
                this.mDataLength += readLength;
                this.mUnsigned32bit = wellKnownValue;
                this.mStringValue = mimeType;
                return true;
            }
            if (decodeExtensionMedia(startIndex + mediaPrefixLength)) {
                this.mDataLength += mediaPrefixLength;
                int readLength2 = this.mDataLength;
                expandWellKnownMimeType();
                long wellKnownValue2 = this.mUnsigned32bit;
                String mimeType2 = this.mStringValue;
                if (readContentParameters(this.mDataLength + startIndex, headersLength - (this.mDataLength - mediaPrefixLength), 0)) {
                    this.mDataLength += readLength2;
                    this.mUnsigned32bit = wellKnownValue2;
                    this.mStringValue = mimeType2;
                    return true;
                }
            }
            int i = mediaPrefixLength;
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean readContentParameters(int startIndex, int leftToRead, int accumulator) {
        String param;
        int totalRead;
        String value;
        int totalRead2;
        if (leftToRead > 0) {
            byte nextByte = this.mWspData[startIndex];
            if ((nextByte & 128) == 0 && nextByte > 31) {
                decodeTokenText(startIndex);
                param = this.mStringValue;
                totalRead = 0 + this.mDataLength;
            } else if (!decodeIntegerValue(startIndex)) {
                return false;
            } else {
                totalRead = 0 + this.mDataLength;
                int wellKnownParameterValue = (int) this.mUnsigned32bit;
                param = WELL_KNOWN_PARAMETERS.get(Integer.valueOf(wellKnownParameterValue));
                if (param == null) {
                    param = "unassigned/0x" + Long.toHexString((long) wellKnownParameterValue);
                }
                if (wellKnownParameterValue == 0) {
                    if (!decodeUintvarInteger(startIndex + totalRead)) {
                        return false;
                    }
                    int totalRead3 = totalRead + this.mDataLength;
                    this.mContentParameters.put(param, String.valueOf(this.mUnsigned32bit));
                    return readContentParameters(startIndex + totalRead3, leftToRead - totalRead3, accumulator + totalRead3);
                }
            }
            if (decodeNoValue(startIndex + totalRead)) {
                totalRead2 = totalRead + this.mDataLength;
                value = null;
            } else if (decodeIntegerValue(startIndex + totalRead)) {
                totalRead2 = totalRead + this.mDataLength;
                value = String.valueOf((int) this.mUnsigned32bit);
            } else {
                decodeTokenText(startIndex + totalRead);
                totalRead2 = totalRead + this.mDataLength;
                String value2 = this.mStringValue;
                if (value2.startsWith("\"")) {
                    value = value2.substring(1);
                } else {
                    value = value2;
                }
            }
            this.mContentParameters.put(param, value);
            return readContentParameters(startIndex + totalRead2, leftToRead - totalRead2, accumulator + totalRead2);
        }
        this.mDataLength = accumulator;
        return true;
    }

    private boolean decodeNoValue(int startIndex) {
        if (this.mWspData[startIndex] != 0) {
            return false;
        }
        this.mDataLength = 1;
        return true;
    }

    private void expandWellKnownMimeType() {
        if (this.mStringValue == null) {
            this.mStringValue = WELL_KNOWN_MIME_TYPES.get(Integer.valueOf((int) this.mUnsigned32bit));
        } else {
            this.mUnsigned32bit = -1;
        }
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
        int index = startIndex;
        while (index <= endIndex) {
            try {
                if (decodeIntegerValue(index)) {
                    if (((int) getValue32()) == 47) {
                        this.mUnsigned32bit = (long) (index + 1);
                        return true;
                    }
                } else if (!decodeTextString(index)) {
                    return false;
                }
                int index2 = index + getDecodedDataLength();
                if (index2 > endIndex) {
                    return false;
                }
                byte val = this.mWspData[index2];
                if (val >= 0 && val <= 30) {
                    index = index2 + this.mWspData[index2] + 1;
                } else if (val == 31) {
                    if (index2 + 1 >= endIndex) {
                        return false;
                    }
                    int index3 = index2 + 1;
                    if (!decodeUintvarInteger(index3)) {
                        return false;
                    }
                    index = index3 + getDecodedDataLength();
                } else if (31 >= val || val > Byte.MAX_VALUE) {
                    index = index2 + 1;
                } else if (!decodeTextString(index2)) {
                    return false;
                } else {
                    index = index2 + getDecodedDataLength();
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
