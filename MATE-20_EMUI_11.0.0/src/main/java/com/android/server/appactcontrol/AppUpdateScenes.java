package com.android.server.appactcontrol;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class AppUpdateScenes implements IAppActScenes {
    private static final String TAG = "AppUpdateScenes";
    private HashSet<String> appUpdateForbiddenSet = new HashSet<>();

    AppUpdateScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.i(TAG, "isNeedForbidAppAct packageName is empty");
            return false;
        }
        String pkgNameHashCode = AppActUtils.getHashCodeForString(pkgName);
        if (!this.appUpdateForbiddenSet.contains(pkgNameHashCode)) {
            return false;
        }
        Log.i(TAG, "isNeedForbidAppAct true, pkgNameHashCode: " + pkgNameHashCode);
        return true;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        xmlParser.getName();
        this.appUpdateForbiddenSet = AppActUtils.readPackageName(xmlEventType, xmlParser);
    }
}
