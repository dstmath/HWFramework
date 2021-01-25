package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import com.android.server.devicepolicy.PolicyStruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class XmlUtil {
    private static final String SUPPORT_MULTIPLE_USERS = "support_multiple_users";
    private static final String TAG = XmlUtil.class.getSimpleName();

    public static void writeOnePolicy(XmlSerializer out, PolicyStruct.PolicyItem item) throws IOException {
        if (PolicyStruct.PolicyItem.isValidItem(item) && item.hasAnyNonNullAttribute() && isEffectiveItem(item, false)) {
            String tag = item.getPolicyTag();
            String str = TAG;
            HwLog.d(str, "writeOnePolicy- Tag: " + tag);
            writeNoListStartOrEnd(out, item, tag, true);
            Bundle attrs = item.getAttributes();
            boolean supportMultipleUsers = item.isSuppportMultipleUsers();
            for (String attrName : attrs.keySet()) {
                int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[item.getItemType().ordinal()];
                if (i == 1) {
                    writeAttrValueToXml(null, attrName, String.valueOf(attrs.getBoolean(attrName)), out);
                    if (supportMultipleUsers) {
                        writeAttrValueToXml(null, SUPPORT_MULTIPLE_USERS, String.valueOf(true), out);
                    }
                } else if (i == 2) {
                    writeAttrValueToXml(null, attrName, attrs.getString(attrName), out);
                    if (supportMultipleUsers) {
                        writeAttrValueToXml(null, SUPPORT_MULTIPLE_USERS, String.valueOf(true), out);
                    }
                } else if (i == 3 || i == 4) {
                    writeListToXml(tag, attrName, attrs.getStringArrayList(attrName), out);
                }
            }
            writeNoListStartOrEnd(out, writeLeafPolicies(out, item, tag, attrs, supportMultipleUsers), tag, false);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.devicepolicy.XmlUtil$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType = new int[PolicyStruct.PolicyType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.STATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.CONFIGURATION.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.LIST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.CONFIGLIST.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static PolicyStruct.PolicyItem writeLeafPolicies(XmlSerializer out, PolicyStruct.PolicyItem item, String tag, Bundle attrs, boolean supportMultipleUsers) throws IOException {
        PolicyStruct.PolicyItem node = item;
        while (node.hasLeafItems()) {
            Iterator it = node.getChildItem().iterator();
            while (it.hasNext()) {
                node = (PolicyStruct.PolicyItem) it.next();
                writeListStartOrEnd(out, node, tag, true);
                if ((node.getItemType() == PolicyStruct.PolicyType.LIST || node.getItemType() == PolicyStruct.PolicyType.CONFIGLIST) && attrs.keySet().isEmpty() && supportMultipleUsers) {
                    out.attribute(null, SUPPORT_MULTIPLE_USERS, String.valueOf(true));
                }
                writeOnePolicy(out, node);
                writeListStartOrEnd(out, node, tag, false);
            }
        }
        return node;
    }

    private static void writeListStartOrEnd(XmlSerializer out, PolicyStruct.PolicyItem node, String tag, boolean isStart) throws IOException {
        if (node.getItemType() != PolicyStruct.PolicyType.LIST && node.getItemType() != PolicyStruct.PolicyType.CONFIGLIST) {
            return;
        }
        if (isStart) {
            out.startTag(null, tag);
        } else {
            out.endTag(null, tag);
        }
    }

    private static void writeNoListStartOrEnd(XmlSerializer out, PolicyStruct.PolicyItem node, String tag, boolean isStart) throws IOException {
        if (node.getItemType() != PolicyStruct.PolicyType.LIST && node.getItemType() != PolicyStruct.PolicyType.CONFIGLIST) {
            if (isStart) {
                out.startTag(null, tag);
            } else {
                out.endTag(null, tag);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0022  */
    private static boolean isEffectiveItem(PolicyStruct.PolicyItem item, boolean result) {
        if (!PolicyStruct.PolicyItem.isValidItem(item) || !item.hasAnyNonNullAttribute()) {
            return false;
        }
        Bundle attrs = item.getAttributes();
        for (String attrName : attrs.keySet()) {
            int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[item.getItemType().ordinal()];
            if (i == 1) {
                return attrs.getBoolean(attrName);
            }
            if (i != 2) {
                if (i == 3 || i == 4) {
                    List<String> attrLists = attrs.getStringArrayList(attrName);
                    if (attrLists == null || attrLists.size() <= 0) {
                        return false;
                    }
                    return true;
                }
                while (r2.hasNext()) {
                }
            } else if (attrs.getString(attrName) != null) {
                return true;
            } else {
                return false;
            }
        }
        Iterator<PolicyStruct.PolicyItem> it = item.getChildItem().iterator();
        while (it.hasNext()) {
            result = result || isEffectiveItem(it.next(), result);
        }
        return result;
    }

    private static void writeListToXml(String tag, String attrName, List<String> lists, XmlSerializer out) throws IOException {
        if (lists == null || lists.isEmpty()) {
            String str = TAG;
            HwLog.w(str, "writePolicy- attrName: [" + attrName + "] has no list attrValue");
            return;
        }
        for (String attrValue : lists) {
            writeAttrValueToXml(tag, attrName, attrValue, out);
        }
    }

    private static void writeAttrValueToXml(String tag, String attrName, String attrValue, XmlSerializer out) throws IOException {
        if (TextUtils.isEmpty(attrValue)) {
            String str = TAG;
            HwLog.w(str, "writePolicy- attrName: [" + attrName + "] has no string attrValue");
        } else if (!TextUtils.isEmpty(tag)) {
            out.startTag(null, tag);
            out.attribute(null, attrName, attrValue);
            out.endTag(null, tag);
        } else {
            out.attribute(null, attrName, attrValue);
        }
    }

    public static boolean readItems(XmlPullParser parser, PolicyStruct.PolicyItem item) throws IOException, XmlPullParserException {
        if (checkReadValid(parser, item)) {
            return false;
        }
        String tag = item.getPolicyTag();
        Bundle attrs = item.getAttributes();
        boolean result = true;
        boolean hasRead = false;
        for (String attrName : attrs.keySet()) {
            int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[item.getItemType().ordinal()];
            if (i == 1) {
                Object state = readStateFromXml(attrName, parser);
                if (state instanceof Boolean) {
                    hasRead = true;
                    attrs.putBoolean(attrName, ((Boolean) state).booleanValue());
                } else {
                    result = false;
                }
            } else if (i == 2) {
                String cfgResult = readCfgFromXml(attrName, parser);
                if (cfgResult != null) {
                    if (!item.hasLeafItems()) {
                        hasRead = true;
                    }
                    attrs.putString(attrName, cfgResult);
                } else {
                    result = false;
                }
            } else if (i == 3 || i == 4) {
                ArrayList<String> attrLists = readListFromXml(tag, attrName, parser);
                if (attrLists != null) {
                    hasRead = true;
                    attrs.putStringArrayList(attrName, attrLists);
                } else {
                    result = false;
                }
            }
        }
        checkReadEnd(parser, hasRead);
        if (readLeafItems(parser, item)) {
            return false;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:1:0x0002  */
    private static void checkReadEnd(XmlPullParser parser, boolean hasRead) throws IOException, XmlPullParserException {
        while (!hasRead) {
            int outerType = parser.next();
            if (outerType == 1 || outerType == 2) {
                return;
            }
            while (!hasRead) {
            }
        }
    }

    private static boolean checkReadValid(XmlPullParser parser, PolicyStruct.PolicyItem item) {
        if (parser == null || !PolicyStruct.PolicyItem.isValidItem(item)) {
            return true;
        }
        String tag = item.getPolicyTag();
        String str = TAG;
        HwLog.d(str, "[policyTag]: " + tag);
        if (tag == null || !tag.equals(parser.getName())) {
            return true;
        }
        return false;
    }

    private static boolean readLeafItems(XmlPullParser parser, PolicyStruct.PolicyItem item) throws IOException, XmlPullParserException {
        PolicyStruct.PolicyItem node = item;
        String str = TAG;
        HwLog.d(str, item.getPolicyName() + ", has children: " + item.hasLeafItems() + " ,number: " + item.getChildItem().size());
        while (node.hasLeafItems()) {
            Iterator<PolicyStruct.PolicyItem> it = node.getChildItem().iterator();
            while (true) {
                if (it.hasNext()) {
                    PolicyStruct.PolicyItem leaf = it.next();
                    node = leaf;
                    if (!readItems(parser, leaf)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Object readStateFromXml(String attrName, XmlPullParser parser) {
        if (TextUtils.isEmpty(attrName) || parser == null) {
            HwLog.w(TAG, "readStateFromXml - invalid input para");
            return -1;
        }
        String attrValue = parser.getAttributeValue(null, attrName);
        if (TextUtils.isEmpty(attrValue) || (!attrValue.equals("true") && !attrValue.equals("false"))) {
            return -1;
        }
        return Boolean.valueOf(Boolean.parseBoolean(attrValue));
    }

    private static ArrayList<String> readListFromXml(String tag, String attrName, XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<String> lists = new ArrayList<>();
        if (TextUtils.isEmpty(attrName) || parser == null) {
            HwLog.w(TAG, "readListFromXml - invalid input para");
            return null;
        }
        int outerDepth = parser.getDepth();
        int outerType = parser.getEventType();
        do {
            String outerTag = parser.getName();
            String str = TAG;
            HwLog.d(str, "readListFromXml tag:[" + outerTag + "]");
            if (tag.equals(outerTag)) {
                String value = parser.getAttributeValue(null, attrName);
                if (value != null) {
                    lists.add(value);
                } else {
                    String str2 = TAG;
                    HwLog.w(str2, "invalid list attrName: " + attrName);
                }
            }
            while (true) {
                int oldLineNumber = parser.getLineNumber();
                int newType = parser.next();
                if (parser.getDepth() != outerDepth && newType == 3) {
                    break;
                }
                int newLineNumber = parser.getLineNumber();
                if ((outerType == 1 || oldLineNumber != newLineNumber || newType == 2) && newType != 4) {
                    break;
                }
            }
            if (parser.getEventType() == 1) {
                break;
            }
        } while (parser.getDepth() == outerDepth);
        if (lists.isEmpty()) {
            return null;
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
