package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class XmlUtil {
    private static final /* synthetic */ int[] -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues = null;
    private static final String TAG = XmlUtil.class.getSimpleName();

    private static /* synthetic */ int[] -getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues() {
        if (-com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues != null) {
            return -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues;
        }
        int[] iArr = new int[PolicyType.values().length];
        try {
            iArr[PolicyType.CONFIGURATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PolicyType.LIST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PolicyType.STATE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues = iArr;
        return iArr;
    }

    public static Bundle readStateFromXml(XmlPullParser parser, String targetTag, String attrName) throws IOException, XmlPullParserException {
        if (TextUtils.isEmpty(targetTag)) {
            Log.w(TAG, "warning_mdm: invalid policy tag:" + targetTag);
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                Log.w(TAG, "warning_mdm: can't find policy tag:" + targetTag);
            } else if (type != 3 && type != 4 && targetTag.equals(parser.getName())) {
                boolean result = Boolean.parseBoolean(parser.getAttributeValue(null, attrName));
                Bundle bundle = new Bundle();
                bundle.putBoolean(targetTag, result);
                return bundle;
            }
        }
        Log.w(TAG, "warning_mdm: can't find policy tag:" + targetTag);
        return null;
    }

    public static void writeListToXml(XmlSerializer out, String outerTag, String innerTag, String attrName, List<String> someList) throws IllegalArgumentException, IllegalStateException, IOException {
        if (someList != null && !someList.isEmpty()) {
            out.startTag(null, outerTag);
            for (String value : someList) {
                out.startTag(null, innerTag);
                out.attribute(null, attrName, value);
                out.endTag(null, innerTag);
            }
            out.endTag(null, outerTag);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void writeOnePolicy(XmlSerializer out, PolicyItem item) throws IOException {
        if (PolicyItem.isValidItem(item) && (item.hasAnyNonNullAttribute() ^ 1) == 0 && isEffectiveItem(item, false)) {
            PolicyItem node = item;
            String tag = item.getPolicyTag();
            HwLog.d(TAG, "writeOnePolicy- Tag: " + tag);
            if (item.getItemType() != PolicyType.LIST) {
                out.startTag(null, tag);
            }
            Bundle attrs = item.getAttributes();
            for (String attrName : attrs.keySet()) {
                switch (-getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues()[item.getItemType().ordinal()]) {
                    case 1:
                        writeAttrValueToXml(null, attrName, attrs.getString(attrName), out);
                        break;
                    case 2:
                        writeListToXml(tag, attrName, attrs.getStringArrayList(attrName), out);
                        break;
                    case 3:
                        writeAttrValueToXml(null, attrName, String.valueOf(attrs.getBoolean(attrName)), out);
                        break;
                    default:
                        break;
                }
            }
            while (node.hasLeafItems()) {
                for (PolicyItem leaf : node.getChildItem()) {
                    node = leaf;
                    if (leaf.getItemType() == PolicyType.LIST) {
                        out.startTag(null, tag);
                    }
                    writeOnePolicy(out, leaf);
                    if (leaf.getItemType() == PolicyType.LIST) {
                        out.endTag(null, tag);
                    }
                }
            }
            if (node.getItemType() != PolicyType.LIST) {
                out.endTag(null, tag);
            }
        }
    }

    private static boolean isEffectiveItem(PolicyItem item, boolean result) {
        boolean z = true;
        if (!PolicyItem.isValidItem(item) || (item.hasAnyNonNullAttribute() ^ 1) != 0) {
            return false;
        }
        Bundle attrs = item.getAttributes();
        for (String attrName : attrs.keySet()) {
            switch (-getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues()[item.getItemType().ordinal()]) {
                case 1:
                    if (attrs.getString(attrName) == null) {
                        z = false;
                    }
                    return z;
                case 2:
                    if (attrs.getStringArrayList(attrName).size() <= 0) {
                        z = false;
                    }
                    return z;
                case 3:
                    return attrs.getBoolean(attrName);
                default:
            }
        }
        for (PolicyItem child : item.getChildItem()) {
            result = !result ? isEffectiveItem(child, result) : true;
        }
        return result;
    }

    private static void writeListToXml(String tag, String attrName, List<String> lists, XmlSerializer out) throws IOException {
        if (lists == null || lists.isEmpty()) {
            HwLog.w(TAG, "writePolicy- attrName: [" + attrName + "] has no list attrValue");
            return;
        }
        for (String attrValue : lists) {
            writeAttrValueToXml(tag, attrName, attrValue, out);
        }
    }

    private static void writeAttrValueToXml(String tag, String attrName, String attrValue, XmlSerializer out) throws IOException {
        if (TextUtils.isEmpty(attrValue)) {
            HwLog.w(TAG, "writePolicy- attrName: [" + attrName + "] has no string attrValue");
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            out.attribute(null, attrName, attrValue);
        } else {
            out.startTag(null, tag);
            out.attribute(null, attrName, attrValue);
            out.endTag(null, tag);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x010d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean readItems(XmlPullParser parser, PolicyItem item) throws IOException, XmlPullParserException {
        String xmlTag = parser.getName();
        HwLog.d(TAG, "xml tag:" + xmlTag);
        boolean result = true;
        if (!PolicyItem.isValidItem(item)) {
            return false;
        }
        String tag = item.getPolicyTag();
        HwLog.d(TAG, "[policyTag]: " + tag);
        if (tag == null || !tag.equals(xmlTag)) {
            return false;
        }
        PolicyItem node;
        Bundle attrs = item.getAttributes();
        boolean hasRead = false;
        for (String attrName : attrs.keySet()) {
            switch (-getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues()[item.getItemType().ordinal()]) {
                case 1:
                    String cfgResult = readCfgFromXml(attrName, parser);
                    if (cfgResult == null) {
                        result = false;
                        break;
                    }
                    if (!item.hasLeafItems()) {
                        hasRead = true;
                    }
                    attrs.putString(attrName, cfgResult);
                    break;
                case 2:
                    ArrayList<String> attrLists = readListFromXml(tag, attrName, parser);
                    if (attrLists == null) {
                        result = false;
                        break;
                    }
                    hasRead = true;
                    attrs.putStringArrayList(attrName, attrLists);
                    break;
                case 3:
                    Object state = readStateFromXml(attrName, parser);
                    if (!(state instanceof Boolean)) {
                        result = false;
                        break;
                    }
                    hasRead = true;
                    attrs.putBoolean(attrName, ((Boolean) state).booleanValue());
                    break;
                default:
                    break;
            }
        }
        while (!hasRead) {
            int outerType = parser.next();
            if (outerType != 1) {
                if (outerType == 2) {
                }
            }
            node = item;
            HwLog.d(TAG, item.getPolicyName() + ", has children: " + item.hasLeafItems() + " ,number: " + item.getChildItem().size());
            while (node.hasLeafItems()) {
                for (PolicyItem leaf : node.getChildItem()) {
                    node = leaf;
                    if (!readItems(parser, leaf)) {
                        return false;
                    }
                }
            }
            return result;
        }
        node = item;
        HwLog.d(TAG, item.getPolicyName() + ", has children: " + item.hasLeafItems() + " ,number: " + item.getChildItem().size());
        while (node.hasLeafItems()) {
        }
        return result;
    }

    private static Object readStateFromXml(String attrName, XmlPullParser parser) {
        if (TextUtils.isEmpty(attrName) || parser == null) {
            HwLog.w(TAG, "readStateFromXml - invalid input para");
            return Integer.valueOf(-1);
        }
        String attrValue = parser.getAttributeValue(null, attrName);
        if (TextUtils.isEmpty(attrValue) || (!attrValue.equals("true") && !attrValue.equals("false"))) {
            return Integer.valueOf(-1);
        }
        return Boolean.valueOf(Boolean.parseBoolean(attrValue));
    }

    private static ArrayList<String> readListFromXml(String tag, String attrName, XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<String> lists = new ArrayList();
        if (TextUtils.isEmpty(attrName) || parser == null) {
            HwLog.w(TAG, "readListFromXml - invalid input para");
            return null;
        }
        int outerDepth = parser.getDepth();
        int outerType = parser.getEventType();
        do {
            String outerTag = parser.getName();
            HwLog.d(TAG, "readListFromXml tag:[" + outerTag + "]");
            if (tag.equals(outerTag)) {
                String value = parser.getAttributeValue(null, attrName);
                if (value != null) {
                    lists.add(value);
                } else {
                    HwLog.w(TAG, "invalid list attrName: " + attrName);
                }
            }
            while (true) {
                int oldLineNumber = parser.getLineNumber();
                int newType = parser.next();
                if (parser.getDepth() != outerDepth && newType == 3) {
                    break;
                }
                int newLineNumber = parser.getLineNumber();
                if (outerType == 1 || oldLineNumber != newLineNumber || newType == 2) {
                    if (newType != 4) {
                        break;
                    }
                }
            }
            if (parser.getEventType() == 1) {
                break;
            }
        } while (parser.getDepth() == outerDepth);
        if (lists.isEmpty()) {
            lists = null;
        }
        return lists;
    }

    private static String readCfgFromXml(String attrName, XmlPullParser parser) throws IOException, XmlPullParserException {
        if (!TextUtils.isEmpty(attrName) && parser != null) {
            return parser.getAttributeValue(null, attrName);
        }
        HwLog.w(TAG, "readCfgFromXml - invalid input para");
        return null;
    }
}
