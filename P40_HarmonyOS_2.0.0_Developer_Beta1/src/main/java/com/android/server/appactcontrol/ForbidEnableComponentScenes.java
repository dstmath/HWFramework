package com.android.server.appactcontrol;

import android.content.ComponentName;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class ForbidEnableComponentScenes implements IAppActScenes {
    private static final String TAG = "ForbidEnableComponentScenes";
    private HashSet<String> forbidEnableComponentSet = new HashSet<>();
    private boolean isForbidAll = false;

    ForbidEnableComponentScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> hashMap) {
        String callingPackage = ActivityManagerEx.getPackageNameForPid(Binder.getCallingPid());
        if (TextUtils.isEmpty(callingPackage) || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(className)) {
            Log.e(TAG, "isNeedForbidAppAct param empty return.");
            return false;
        }
        String callingPackageHashCode = AppActUtils.getHashCodeForString(callingPackage);
        if (!this.forbidEnableComponentSet.contains(callingPackageHashCode)) {
            return false;
        }
        if (this.isForbidAll) {
            Log.i(TAG, "isForbidAll forbid enable. callingPackageHashCode: " + callingPackageHashCode);
            return true;
        }
        HashMap<String, HashSet<ComponentName>> componentDisableMap = ComponentHiddenScenes.getInstance().getComponentDisableMap();
        if (!componentDisableMap.containsKey(pkgName) || !componentDisableMap.get(pkgName).contains(new ComponentName(pkgName, className))) {
            return false;
        }
        String pkgNameHashCode = AppActUtils.getHashCodeForString(pkgName);
        String classNameHashCode = AppActUtils.getHashCodeForString(className);
        Log.i(TAG, "forbid enable pkgNameHashCode:" + pkgNameHashCode + ", className " + classNameHashCode + ", callingPackageHashCode:" + callingPackageHashCode);
        return true;
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        if (AppActConstant.FORBID_ENABLE_COMPONENT.equals(xmlParser.getName())) {
            this.isForbidAll = AppActConstant.VALUE_TRUE.equals(xmlParser.getAttributeValue(null, AppActConstant.ATTR_FORBID_ALL));
        }
        this.forbidEnableComponentSet = AppActUtils.readPackageName(xmlEventType, xmlParser);
    }
}
