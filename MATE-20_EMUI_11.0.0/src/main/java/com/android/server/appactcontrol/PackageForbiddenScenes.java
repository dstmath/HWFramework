package com.android.server.appactcontrol;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class PackageForbiddenScenes implements IAppActScenes {
    private static final String TAG = "PackageForbiddenScenes";
    private boolean isNormalModeNeedForbid = false;
    private boolean isSaftyModeNeedForbid = false;
    private HashSet<String> packageForbiddenSet = new HashSet<>();

    PackageForbiddenScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        String pkgNameHashCode = AppActUtils.getHashCodeForString(pkgName);
        if (AppActUtils.isSafeMode()) {
            if (this.isSaftyModeNeedForbid && this.packageForbiddenSet.contains(pkgNameHashCode)) {
                Log.i(TAG, "isNeedForbidAppAct SaftyMode true, pkgNameHashCode: " + pkgNameHashCode);
                return true;
            }
        } else if (this.isNormalModeNeedForbid && this.packageForbiddenSet.contains(pkgNameHashCode)) {
            Log.i(TAG, "isNeedForbidAppAct NormalMode true, pkgNameHashCode: " + pkgNameHashCode);
            return true;
        }
        return false;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        if (AppActConstant.PACKAGE_FORBIDDEN.equals(xmlParser.getName())) {
            this.isSaftyModeNeedForbid = AppActConstant.VALUE_TRUE.equals(xmlParser.getAttributeValue(null, AppActConstant.ATTR_SAFTY_MODE));
            this.isNormalModeNeedForbid = AppActConstant.VALUE_TRUE.equals(xmlParser.getAttributeValue(null, AppActConstant.ATTR_NORMAL_MODE));
        }
        this.packageForbiddenSet = AppActUtils.readPackageName(xmlEventType, xmlParser);
    }
}
