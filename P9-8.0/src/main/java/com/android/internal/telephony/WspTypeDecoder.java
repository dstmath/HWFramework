package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioError;
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
    private static final HashMap<Integer, String> WELL_KNOWN_MIME_TYPES = new HashMap();
    private static final HashMap<Integer, String> WELL_KNOWN_PARAMETERS = new HashMap();
    HashMap<String, String> mContentParameters;
    int mDataLength;
    String mStringValue;
    long mUnsigned32bit;
    byte[] mWspData;

    static {
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(0), "*/*");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(1), "text/*");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(2), ContentType.TEXT_HTML);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(3), ContentType.TEXT_PLAIN);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(4), "text/x-hdml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(5), "text/x-ttml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(6), ContentType.TEXT_VCALENDAR);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(7), ContentType.TEXT_VCARD);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(8), "text/vnd.wap.wml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(9), "text/vnd.wap.wmlscript");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(10), "text/vnd.wap.wta-event");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(11), "multipart/*");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(12), "multipart/mixed");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(13), "multipart/form-data");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(14), "multipart/byterantes");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(15), "multipart/alternative");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(16), "application/*");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(17), "application/java-vm");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(18), "application/x-www-form-urlencoded");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(19), "application/x-hdmlc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(20), "application/vnd.wap.wmlc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(21), "application/vnd.wap.wmlscriptc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(22), "application/vnd.wap.wta-eventc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(23), "application/vnd.wap.uaprof");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(24), "application/vnd.wap.wtls-ca-certificate");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(25), "application/vnd.wap.wtls-user-certificate");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(26), "application/x-x509-ca-cert");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(27), "application/x-x509-user-cert");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(28), ContentType.IMAGE_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(29), ContentType.IMAGE_GIF);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(30), ContentType.IMAGE_JPEG);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(31), "image/tiff");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(32), ContentType.IMAGE_PNG);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(33), ContentType.IMAGE_WBMP);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(34), "application/vnd.wap.multipart.*");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(35), ContentType.MULTIPART_MIXED);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(36), "application/vnd.wap.multipart.form-data");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(37), "application/vnd.wap.multipart.byteranges");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(38), ContentType.MULTIPART_ALTERNATIVE);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(39), "application/xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(40), "text/xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(41), "application/vnd.wap.wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(42), "application/x-x968-cross-cert");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(43), "application/x-x968-ca-cert");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(44), "application/x-x968-user-cert");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(45), "text/vnd.wap.si");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(46), "application/vnd.wap.sic");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(47), "text/vnd.wap.sl");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(48), "application/vnd.wap.slc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(49), "text/vnd.wap.co");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(50), CONTENT_TYPE_B_PUSH_CO);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(51), ContentType.MULTIPART_RELATED);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(52), "application/vnd.wap.sia");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(53), "text/vnd.wap.connectivity-xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(54), "application/vnd.wap.connectivity-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(55), "application/pkcs7-mime");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(56), "application/vnd.wap.hashed-certificate");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(57), "application/vnd.wap.signed-certificate");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(58), "application/vnd.wap.cert-response");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(59), ContentType.APP_XHTML);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(60), "application/wml+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(61), "text/css");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(62), "application/vnd.wap.mms-message");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(63), "application/vnd.wap.rollover-certificate");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(64), "application/vnd.wap.locc+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(65), "application/vnd.wap.loc+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(66), "application/vnd.syncml.dm+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(67), "application/vnd.syncml.dm+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(68), CONTENT_TYPE_B_PUSH_SYNCML_NOTI);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(69), ContentType.APP_WAP_XHTML);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(70), "application/vnd.wv.csp.cir");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(71), "application/vnd.oma.dd+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(72), "application/vnd.oma.drm.message");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(73), ContentType.APP_DRM_CONTENT);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(74), "application/vnd.oma.drm.rights+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(75), "application/vnd.oma.drm.rights+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(76), "application/vnd.wv.csp+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(77), "application/vnd.wv.csp+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(78), "application/vnd.syncml.ds.notification");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(79), ContentType.AUDIO_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(80), ContentType.VIDEO_UNSPECIFIED);
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(81), "application/vnd.oma.dd2+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(82), "application/mikey");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(83), "application/vnd.oma.dcd");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(84), "application/vnd.oma.dcdc");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_13), "application/vnd.uplanet.cacheop-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_14), "application/vnd.uplanet.signal");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_15), "application/vnd.uplanet.alert-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_16), "application/vnd.uplanet.list-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_17), "application/vnd.uplanet.listcmd-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_18), "application/vnd.uplanet.channel-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_19), "application/vnd.uplanet.provisioning-status-uri");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_20), "x-wap.multipart/vnd.uplanet.header-set");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_21), "application/vnd.uplanet.bearer-choice-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_22), "application/vnd.phonecom.mmc-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_23), "application/vnd.nokia.syncset+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(RadioError.OEM_ERROR_24), "image/x-up-wpng");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(768), "application/iota.mmc-wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(769), "application/iota.mmc-xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(770), "application/vnd.syncml+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(771), "application/vnd.syncml+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(772), "text/vnd.wap.emn+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(773), "text/calendar");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(774), "application/vnd.omads-email+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(775), "application/vnd.omads-file+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(776), "application/vnd.omads-folder+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(777), "text/directory;profile=vCard");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(778), "application/vnd.wap.emn+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(779), "application/vnd.nokia.ipdc-purchase-response");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(780), "application/vnd.motorola.screen3+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(781), "application/vnd.motorola.screen3+gzip");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(782), "application/vnd.cmcc.setting+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(783), "application/vnd.cmcc.bombing+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(784), "application/vnd.docomo.pf");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(785), "application/vnd.docomo.ub");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(786), "application/vnd.omaloc-supl-init");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(787), "application/vnd.oma.group-usage-list+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(788), "application/oma-directory+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(789), "application/vnd.docomo.pf2");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(790), "application/vnd.oma.drm.roap-trigger+wbxml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(791), "application/vnd.sbm.mid2");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(792), "application/vnd.wmf.bootstrap");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(793), "application/vnc.cmcc.dcd+xml");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(794), "application/vnd.sbm.cid");
        WELL_KNOWN_MIME_TYPES.put(Integer.valueOf(795), "application/vnd.oma.bcast.provisioningtrigger");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(0), "Q");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(1), "Charset");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(2), "Level");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(3), "Type");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(7), "Differences");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(8), "Padding");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(9), "Type");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(14), "Max-Age");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(16), "Secure");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(17), "SEC");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(18), "MAC");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(19), "Creation-date");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(20), "Modification-date");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(21), "Read-date");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(22), "Size");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(23), "Name");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(24), "Filename");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(25), "Start");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(26), "Start-info");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(27), "Comment");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(28), "Domain");
        WELL_KNOWN_PARAMETERS.put(Integer.valueOf(29), "Path");
    }

    public WspTypeDecoder(byte[] pdu) {
        this.mWspData = pdu;
    }

    public boolean decodeTextString(int startIndex) {
        int index = startIndex;
        while (this.mWspData[index] != (byte) 0) {
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
        while (this.mWspData[index] != (byte) 0) {
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
        this.mUnsigned32bit = (long) (this.mWspData[startIndex] & 127);
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
        while ((this.mWspData[index] & 128) != 0) {
            if (index - startIndex >= 4) {
                return false;
            }
            this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & 127));
            index++;
        }
        this.mUnsigned32bit = (this.mUnsigned32bit << 7) | ((long) (this.mWspData[index] & 127));
        this.mDataLength = (index - startIndex) + 1;
        return true;
    }

    public boolean decodeValueLength(int startIndex) {
        if ((this.mWspData[startIndex] & 255) > 31) {
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
        this.mDataLength = 0;
        this.mStringValue = null;
        int length = this.mWspData.length;
        boolean rtrn = startIndex < length;
        while (index < length && this.mWspData[index] != (byte) 0) {
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
                    readLength = this.mDataLength;
                    expandWellKnownMimeType();
                    wellKnownValue = this.mUnsigned32bit;
                    mimeType = this.mStringValue;
                    if (readContentParameters(this.mDataLength + startIndex, headersLength - (this.mDataLength - mediaPrefixLength), 0)) {
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
            if ((nextByte & 128) == 0 && nextByte > (byte) 31) {
                decodeTokenText(startIndex);
                param = this.mStringValue;
                totalRead = this.mDataLength + 0;
            } else if (!decodeIntegerValue(startIndex)) {
                return false;
            } else {
                totalRead = this.mDataLength + 0;
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
        if (this.mWspData[startIndex] != (byte) 0) {
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
                    if (((int) getValue32()) == 47) {
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
                if (val >= (byte) 0 && val <= (byte) 30) {
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
                } else if ((byte) 31 >= val || val > Byte.MAX_VALUE) {
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
