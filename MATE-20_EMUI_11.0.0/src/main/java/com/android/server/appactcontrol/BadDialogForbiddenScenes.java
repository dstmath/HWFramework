package com.android.server.appactcontrol;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class BadDialogForbiddenScenes implements IAppActScenes {
    private static final String TAG = "BadDialogForbiddenScenes";
    private HashMap<String, HashSet<String>> dialogForbiddenClassNameMap = new HashMap<>();
    private HashSet<String> dialogForbiddenPkgSet = new HashSet<>();

    BadDialogForbiddenScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.i(TAG, "isNeedForbidAppAct pkgName is empty");
            return false;
        }
        String pkgNameHashCode = AppActUtils.getHashCodeForString(pkgName);
        if (this.dialogForbiddenPkgSet.contains(pkgNameHashCode)) {
            Log.i(TAG, "isNeedForbidAppAct true, pkgNameHashCode: " + pkgNameHashCode);
            return true;
        }
        HashSet<String> dialogForbiddenClassNameSet = this.dialogForbiddenClassNameMap.get(pkgNameHashCode);
        if (dialogForbiddenClassNameSet == null) {
            return false;
        }
        String classNameHashCode = AppActUtils.getHashCodeForString(className);
        if (!dialogForbiddenClassNameSet.contains(classNameHashCode)) {
            return false;
        }
        Log.i(TAG, "isNeedForbidAppAct true, pkgNameHashCode: " + pkgNameHashCode + ", classNameHashCode: " + classNameHashCode);
        return true;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        HashSet<String> tmpDialogForbiddenClassNameSet;
        String nodeName = xmlParser.getName();
        HashSet<String> tmpDialogForbiddenPkgSet = new HashSet<>();
        HashMap<String, HashSet<String>> tmpDialogForbiddenClassNameMap = new HashMap<>();
        while (true) {
            if (xmlEventType != 3 || "item".equals(nodeName)) {
                xmlEventType = xmlParser.next();
                nodeName = xmlParser.getName();
                if (xmlEventType == 2 && "item".equals(nodeName)) {
                    String packageName = xmlParser.getAttributeValue(null, AppActConstant.ATTR_PACKAGE_NAME);
                    String className = xmlParser.getAttributeValue(null, AppActConstant.ATTR_CLASS_NAME);
                    if (TextUtils.isEmpty(className)) {
                        tmpDialogForbiddenPkgSet.add(packageName);
                    } else {
                        if (tmpDialogForbiddenClassNameMap.containsKey(packageName)) {
                            tmpDialogForbiddenClassNameSet = tmpDialogForbiddenClassNameMap.get(packageName);
                        } else {
                            tmpDialogForbiddenClassNameSet = new HashSet<>();
                            tmpDialogForbiddenClassNameMap.put(packageName, tmpDialogForbiddenClassNameSet);
                        }
                        tmpDialogForbiddenClassNameSet.add(className);
                    }
                }
            } else {
                this.dialogForbiddenPkgSet = tmpDialogForbiddenPkgSet;
                this.dialogForbiddenClassNameMap = tmpDialogForbiddenClassNameMap;
                return;
            }
        }
    }
}
