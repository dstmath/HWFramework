package com.android.server.appactcontrol;

import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

class HarmfulPackageForbiddenScenes implements IAppActScenes {
    private boolean isHarmFulPkgForbidden = false;

    HarmfulPackageForbiddenScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        return this.isHarmFulPkgForbidden;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) {
        if (AppActConstant.HARMFUL_PACKAGE_FORBIDDEN.equals(xmlParser.getName())) {
            this.isHarmFulPkgForbidden = AppActConstant.VALUE_TRUE.equals(xmlParser.getAttributeValue(null, "value"));
        }
    }
}
