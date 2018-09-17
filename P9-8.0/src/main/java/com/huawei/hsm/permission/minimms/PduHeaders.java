package com.huawei.hsm.permission.minimms;

import java.util.ArrayList;
import java.util.HashMap;

class PduHeaders {
    public static final int ADAPTATION_ALLOWED = 188;
    public static final int ADDITIONAL_HEADERS = 176;
    public static final int APPLIC_ID = 183;
    public static final int ATTRIBUTES = 168;
    public static final int AUX_APPLIC_ID = 185;
    public static final int BCC = 129;
    public static final int CANCEL_ID = 190;
    public static final int CANCEL_STATUS = 191;
    public static final int CANCEL_STATUS_REQUEST_CORRUPTED = 129;
    public static final int CANCEL_STATUS_REQUEST_SUCCESSFULLY_RECEIVED = 128;
    public static final int CC = 130;
    public static final int CONTENT = 174;
    public static final int CONTENT_CLASS = 186;
    public static final int CONTENT_CLASS_CONTENT_RICH = 135;
    public static final int CONTENT_CLASS_TEXT = 128;
    public static final int CONTENT_LOCATION = 131;
    public static final int CONTENT_TYPE = 132;
    public static final int CURRENT_MMS_VERSION = 18;
    public static final int DATE = 133;
    public static final int DELIVERY_REPORT = 134;
    public static final int DELIVERY_TIME = 135;
    public static final int DISTRIBUTION_INDICATOR = 177;
    public static final int DRM_CONTENT = 187;
    public static final int ELEMENT_DESCRIPTOR = 178;
    public static final int EXPIRY = 136;
    public static final int FROM = 137;
    public static final int FROM_ADDRESS_PRESENT_TOKEN = 128;
    public static final String FROM_INSERT_ADDRESS_TOKEN_STR = "insert-address-token";
    public static final int LIMIT = 179;
    public static final int MBOX_QUOTAS = 172;
    public static final int MBOX_TOTALS = 170;
    public static final int MESSAGE_CLASS = 138;
    public static final int MESSAGE_CLASS_ADVERTISEMENT = 129;
    public static final String MESSAGE_CLASS_ADVERTISEMENT_STR = "advertisement";
    public static final int MESSAGE_CLASS_AUTO = 131;
    public static final String MESSAGE_CLASS_AUTO_STR = "auto";
    public static final int MESSAGE_CLASS_INFORMATIONAL = 130;
    public static final String MESSAGE_CLASS_INFORMATIONAL_STR = "informational";
    public static final int MESSAGE_CLASS_PERSONAL = 128;
    public static final String MESSAGE_CLASS_PERSONAL_STR = "personal";
    public static final int MESSAGE_COUNT = 173;
    public static final int MESSAGE_ID = 139;
    public static final int MESSAGE_SIZE = 142;
    public static final int MESSAGE_TYPE = 140;
    public static final int MESSAGE_TYPE_CANCEL_CONF = 151;
    public static final int MESSAGE_TYPE_CANCEL_REQ = 150;
    public static final int MESSAGE_TYPE_DELETE_CONF = 149;
    public static final int MESSAGE_TYPE_DELETE_REQ = 148;
    public static final int MESSAGE_TYPE_FORWARD_CONF = 138;
    public static final int MESSAGE_TYPE_FORWARD_REQ = 137;
    public static final int MESSAGE_TYPE_MBOX_DELETE_CONF = 146;
    public static final int MESSAGE_TYPE_MBOX_DELETE_REQ = 145;
    public static final int MESSAGE_TYPE_MBOX_DESCR = 147;
    public static final int MESSAGE_TYPE_MBOX_STORE_CONF = 140;
    public static final int MESSAGE_TYPE_MBOX_STORE_REQ = 139;
    public static final int MESSAGE_TYPE_MBOX_UPLOAD_CONF = 144;
    public static final int MESSAGE_TYPE_MBOX_UPLOAD_REQ = 143;
    public static final int MESSAGE_TYPE_MBOX_VIEW_CONF = 142;
    public static final int MESSAGE_TYPE_MBOX_VIEW_REQ = 141;
    public static final int MESSAGE_TYPE_SEND_REQ = 128;
    public static final int MMS_VERSION = 141;
    public static final int MMS_VERSION_1_0 = 16;
    public static final int MMS_VERSION_1_2 = 18;
    public static final int MMS_VERSION_1_3 = 19;
    public static final int MM_FLAGS = 164;
    public static final int MM_STATE = 163;
    public static final int MM_STATE_DRAFT = 128;
    public static final int MM_STATE_FORWARDED = 132;
    public static final int PREVIOUSLY_SENT_BY = 160;
    public static final int PREVIOUSLY_SENT_DATE = 161;
    public static final int PRIORITY = 143;
    public static final int PRIORITY_HIGH = 130;
    public static final int PRIORITY_LOW = 128;
    public static final int QUOTAS = 171;
    public static final int READ_REPORT = 144;
    public static final int READ_STATUS = 155;
    public static final int READ_STATUS_READ = 128;
    public static final int READ_STATUS__DELETED_WITHOUT_BEING_READ = 129;
    public static final int RECOMMENDED_RETRIEVAL_MODE = 180;
    public static final int RECOMMENDED_RETRIEVAL_MODE_MANUAL = 128;
    public static final int RECOMMENDED_RETRIEVAL_MODE_TEXT = 181;
    public static final int REPLACE_ID = 189;
    public static final int REPLY_APPLIC_ID = 184;
    public static final int REPLY_CHARGING = 156;
    public static final int REPLY_CHARGING_ACCEPTED_TEXT_ONLY = 131;
    public static final int REPLY_CHARGING_DEADLINE = 157;
    public static final int REPLY_CHARGING_ID = 158;
    public static final int REPLY_CHARGING_REQUESTED = 128;
    public static final int REPLY_CHARGING_SIZE = 159;
    public static final int REPORT_ALLOWED = 145;
    public static final int RESPONSE_STATUS = 146;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_END = 255;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_FAILURE = 224;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID = 235;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE = 192;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS = 196;
    public static final int RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE = 136;
    public static final int RESPONSE_STATUS_OK = 128;
    public static final int RESPONSE_TEXT = 147;
    public static final int RETRIEVE_STATUS = 153;
    public static final int RETRIEVE_STATUS_ERROR_END = 255;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED = 227;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE = 224;
    public static final int RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE = 192;
    public static final int RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 194;
    public static final int RETRIEVE_STATUS_OK = 128;
    public static final int RETRIEVE_TEXT = 154;
    public static final int SENDER_VISIBILITY = 148;
    public static final int START = 175;
    public static final int STATUS = 149;
    public static final int STATUS_EXPIRED = 128;
    public static final int STATUS_TEXT = 182;
    public static final int STATUS_UNREACHABLE = 135;
    public static final int STORE = 162;
    public static final int STORED = 167;
    public static final int STORE_STATUS = 165;
    public static final int STORE_STATUS_ERROR_END = 255;
    public static final int STORE_STATUS_ERROR_PERMANENT_FAILURE = 224;
    public static final int STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL = 228;
    public static final int STORE_STATUS_ERROR_TRANSIENT_FAILURE = 192;
    public static final int STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 193;
    public static final int STORE_STATUS_SUCCESS = 128;
    public static final int STORE_STATUS_TEXT = 166;
    public static final int SUBJECT = 150;
    public static final int TO = 151;
    public static final int TOTALS = 169;
    public static final int TRANSACTION_ID = 152;
    public static final int VALUE_NO = 129;
    public static final int VALUE_RELATIVE_TOKEN = 129;
    public static final int VALUE_YES = 128;
    private HashMap<Integer, Object> mHeaderMap;

    public PduHeaders() {
        this.mHeaderMap = null;
        this.mHeaderMap = new HashMap();
    }

    protected void setOctet(int value, int field) throws InvalidHeaderValueException {
        switch (field) {
            case DELIVERY_REPORT /*134*/:
            case 144:
            case 145:
            case 148:
            case STORE /*162*/:
            case STORED /*167*/:
            case TOTALS /*169*/:
            case QUOTAS /*171*/:
            case DISTRIBUTION_INDICATOR /*177*/:
            case DRM_CONTENT /*187*/:
            case ADAPTATION_ALLOWED /*188*/:
                if (!(128 == value || 129 == value)) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case 140:
                if (value < 128 || value > 151) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case 141:
                if (value < 16 || value > 19) {
                    value = 18;
                    break;
                }
            case 143:
                if (value < 128 || value > 130) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case 146:
                if (value <= RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS || value >= 224) {
                    if ((value > RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID && value <= 255) || value < 128 || ((value > 136 && value < 192) || value > 255)) {
                        value = 224;
                        break;
                    }
                }
                value = 192;
                break;
                break;
            case 149:
                if (value < 128 || value > 135) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case 153:
                if (value <= RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM || value >= 224) {
                    if (value <= RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED || value > 255) {
                        if (value < 128 || ((value > 128 && value < 192) || value > 255)) {
                            value = 224;
                            break;
                        }
                    }
                    value = 224;
                    break;
                }
                value = 192;
                break;
                break;
            case READ_STATUS /*155*/:
                if (!(128 == value || 129 == value)) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case REPLY_CHARGING /*156*/:
                if (value < 128 || value > 131) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case MM_STATE /*163*/:
                if (value < 128 || value > 132) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case STORE_STATUS /*165*/:
                if (value <= STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM || value >= 224) {
                    if (value <= STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL || value > 255) {
                        if (value < 128 || ((value > 128 && value < 192) || value > 255)) {
                            value = 224;
                            break;
                        }
                    }
                    value = 224;
                    break;
                }
                value = 192;
                break;
                break;
            case RECOMMENDED_RETRIEVAL_MODE /*180*/:
                if (128 != value) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case CONTENT_CLASS /*186*/:
                if (value < 128 || value > 135) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            case CANCEL_STATUS /*191*/:
                if (!(128 == value || 129 == value)) {
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
            default:
                throw new RuntimeException("Invalid header field!");
        }
        this.mHeaderMap.put(Integer.valueOf(field), Integer.valueOf(value));
    }

    protected void setTextString(byte[] value, int field) {
        if (value == null) {
            throw new NullPointerException();
        }
        switch (field) {
            case 131:
            case 132:
            case 138:
            case 139:
            case TRANSACTION_ID /*152*/:
            case REPLY_CHARGING_ID /*158*/:
            case APPLIC_ID /*183*/:
            case REPLY_APPLIC_ID /*184*/:
            case AUX_APPLIC_ID /*185*/:
            case REPLACE_ID /*189*/:
            case CANCEL_ID /*190*/:
                this.mHeaderMap.put(Integer.valueOf(field), value);
                return;
            default:
                throw new RuntimeException("Invalid header field!");
        }
    }

    protected EncodedStringValue[] getEncodedStringValues(int field) {
        ArrayList<EncodedStringValue> list = (ArrayList) this.mHeaderMap.get(Integer.valueOf(field));
        if (list == null) {
            return null;
        }
        return (EncodedStringValue[]) list.toArray(new EncodedStringValue[list.size()]);
    }

    protected void setEncodedStringValue(EncodedStringValue value, int field) {
        if (value == null) {
            throw new NullPointerException();
        }
        switch (field) {
            case 137:
            case 147:
            case 150:
            case RETRIEVE_TEXT /*154*/:
            case PREVIOUSLY_SENT_BY /*160*/:
            case MM_FLAGS /*164*/:
            case STORE_STATUS_TEXT /*166*/:
            case RECOMMENDED_RETRIEVAL_MODE_TEXT /*181*/:
            case STATUS_TEXT /*182*/:
                this.mHeaderMap.put(Integer.valueOf(field), value);
                return;
            default:
                throw new RuntimeException("Invalid header field!");
        }
    }

    protected void appendEncodedStringValue(EncodedStringValue value, int field) {
        if (value == null) {
            throw new NullPointerException();
        }
        switch (field) {
            case 129:
            case 130:
            case 151:
                ArrayList<EncodedStringValue> list = (ArrayList) this.mHeaderMap.get(Integer.valueOf(field));
                if (list == null) {
                    list = new ArrayList();
                }
                list.add(value);
                this.mHeaderMap.put(Integer.valueOf(field), list);
                return;
            default:
                throw new RuntimeException("Invalid header field!");
        }
    }

    protected void setLongInteger(long value, int field) {
        switch (field) {
            case 133:
            case 135:
            case 136:
            case 142:
            case REPLY_CHARGING_DEADLINE /*157*/:
            case REPLY_CHARGING_SIZE /*159*/:
            case PREVIOUSLY_SENT_DATE /*161*/:
            case MESSAGE_COUNT /*173*/:
            case START /*175*/:
            case LIMIT /*179*/:
                this.mHeaderMap.put(Integer.valueOf(field), Long.valueOf(value));
                return;
            default:
                throw new RuntimeException("Invalid header field!");
        }
    }
}
