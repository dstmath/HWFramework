package com.android.server.appactcontrol;

import android.util.Log;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

/* access modifiers changed from: package-private */
public class PreasAppUpdateScenes implements IAppActScenes {
    private static final String TAG = "PreasAppUpdateScenes";
    private boolean isPreasAppUpdate = false;

    PreasAppUpdateScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        if (!this.isPreasAppUpdate || !AppActUtils.isPreasApp(pkgName)) {
            return false;
        }
        Log.i(TAG, "isNeedForbidAppAct true");
        return true;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) {
        if (AppActConstant.PREAS_APP_UPDATE_FORBIDDEN.equals(xmlParser.getName())) {
            this.isPreasAppUpdate = AppActConstant.VALUE_TRUE.equals(xmlParser.getAttributeValue(null, "value"));
        }
    }
}
