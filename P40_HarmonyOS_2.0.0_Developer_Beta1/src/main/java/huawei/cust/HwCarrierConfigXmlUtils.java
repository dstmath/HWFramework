package huawei.cust;

import android.telephony.Rlog;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCarrierConfigXmlUtils {
    private static final String ARRAY_NAME = "array";
    private static final String BOOL_NAME = "bool";
    private static final String DICT_NAME = "dict";
    private static final String DOUBLE_NAME = "double";
    private static final HashMap EMPTY = new HashMap();
    private static final String HASH_VALUE = "hashvalue";
    private static final String INT_NAME = "int";
    private static final String KEY_NAME = "key";
    private static final String LOG_TAG = "HwCarrierConfigXmlUtils";
    private static final String START_TAG = "config";
    private static final String STRING_NAME = "string";

    public static Map read(XmlPullParser input) throws Exception {
        Map result = new HashMap();
        if (input == null) {
            return result;
        }
        while (true) {
            int e = input.next();
            if (e == 1) {
                return result;
            }
            if (e == 2) {
                if (!START_TAG.equalsIgnoreCase(input.getName())) {
                    log("begin parse name " + input.getName());
                    int outerDepth = input.getDepth();
                    String startTag = input.getName();
                    String[] tagName = new String[1];
                    while (true) {
                        int event = input.next();
                        if (event != 1 && (event != 3 || input.getDepth() < outerDepth)) {
                            if (event == 2) {
                                result.putAll(readXml(input, startTag, tagName));
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    String hashValue = input.getAttributeValue(null, HASH_VALUE);
                    if (!TextUtils.isEmpty(hashValue)) {
                        result.put(HASH_VALUE, hashValue);
                        log("parse hashvalue attribute = " + hashValue);
                    }
                }
            }
        }
    }

    private static Map readXml(XmlPullParser parser, String tagName, String[] outKey) throws Exception {
        HwCarrierConfigDictValue dictValue = new HwCarrierConfigDictValue();
        if (readComplexValueXml(parser, tagName, outKey, dictValue)) {
            return (Map) dictValue.getData();
        }
        loge("readXml tagName " + tagName + " is error");
        return EMPTY;
    }

    private static boolean readComplexValueXml(XmlPullParser parser, String tagName, String[] outKey, HWCarrierConfigComplexValue complexValue) {
        try {
            int eventType = parser.getEventType();
            do {
                if (eventType == 2) {
                    complexValue.addData(outKey[0], readValueXml(parser, outKey));
                } else if (eventType == 3) {
                    if (parser.getName().equals(tagName)) {
                        return true;
                    }
                    loge("Expected " + tagName + " end tag, but it is " + parser.getName());
                    return false;
                }
                eventType = parser.next();
            } while (eventType != 1);
        } catch (Exception e) {
            loge("readComplexValueXml catch Exception in  " + tagName);
        }
        loge("no found ended before " + tagName + " end tag");
        return false;
    }

    private static Object readValueXml(XmlPullParser parser, String[] outKey) throws XmlPullParserException {
        String key = parser.getAttributeValue(null, KEY_NAME);
        String typeName = parser.getName();
        char c = 65535;
        try {
            switch (typeName.hashCode()) {
                case -1325958191:
                    if (typeName.equals(DOUBLE_NAME)) {
                        c = 3;
                        break;
                    }
                    break;
                case -891985903:
                    if (typeName.equals(STRING_NAME)) {
                        c = 0;
                        break;
                    }
                    break;
                case 104431:
                    if (typeName.equals(INT_NAME)) {
                        c = 1;
                        break;
                    }
                    break;
                case 3029738:
                    if (typeName.equals(BOOL_NAME)) {
                        c = 2;
                        break;
                    }
                    break;
                case 3083190:
                    if (typeName.equals(DICT_NAME)) {
                        c = 4;
                        break;
                    }
                    break;
                case 93090393:
                    if (typeName.equals(ARRAY_NAME)) {
                        c = 5;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                outKey[0] = key;
                return parser.nextText();
            } else if (c == 1) {
                outKey[0] = key;
                return Integer.valueOf(Integer.parseInt(parser.nextText().trim()));
            } else if (c == 2) {
                outKey[0] = key;
                return Boolean.valueOf(parser.nextText().trim());
            } else if (c == 3) {
                outKey[0] = key;
                return Double.valueOf(parser.nextText().trim());
            } else if (c == 4) {
                parser.next();
                HwCarrierConfigDictValue dictValue = new HwCarrierConfigDictValue();
                if (readComplexValueXml(parser, DICT_NAME, outKey, dictValue)) {
                    Object result = dictValue.getData();
                    outKey[0] = key;
                    return result;
                }
                throw new XmlPullParserException("readComplexValueXml is Error for <" + typeName + ">");
            } else if (c == 5) {
                parser.next();
                HwCarrierConfigArrayValue arrayValue = new HwCarrierConfigArrayValue();
                if (readComplexValueXml(parser, ARRAY_NAME, outKey, arrayValue)) {
                    Object result2 = arrayValue.getData();
                    outKey[0] = key;
                    return result2;
                }
                throw new XmlPullParserException("readComplexValueXml is Error for <" + typeName + ">");
            } else {
                loge("No unknown type for parse " + typeName);
                throw new XmlPullParserException("No unknown type for parse <" + typeName + ">");
            }
        } catch (Exception e) {
            loge("It is Error for parse <" + typeName + ">");
            throw new XmlPullParserException("It is Error for parse <" + typeName + ">");
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
