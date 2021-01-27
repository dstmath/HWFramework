package com.android.server.appactcontrol;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class NotificationForbiddenScenes implements IAppActScenes {
    private static final String TAG = "NotificationForbiddenScenes";
    private HashSet<String> channelForbiddenSet = new HashSet<>();
    private HashMap<String, HashSet<String>> forbiddenMap = new HashMap<>();
    private HashSet<String> packageForbiddenSet = new HashSet<>();

    NotificationForbiddenScenes() {
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public boolean isNeedForbidAppAct(String pkgName, String className, HashMap<String, String> extra) {
        String pkgNameHashCode = AppActUtils.getHashCodeForString(pkgName);
        if (this.packageForbiddenSet.contains(pkgNameHashCode)) {
            Log.i(TAG, "isNeedForbidAppAct true, pkgNameHashCode: " + pkgNameHashCode);
            return true;
        } else if (extra == null) {
            return false;
        } else {
            String channelNameHashCode = AppActUtils.getHashCodeForString(extra.get(AppActConstant.ATTR_CHANNEL_NAME));
            if (this.channelForbiddenSet.contains(channelNameHashCode)) {
                Log.i(TAG, "isNeedForbidAppAct true, channelNameHashCode: " + channelNameHashCode);
                return true;
            }
            HashSet<String> channelNameHashCodeSet = this.forbiddenMap.get(pkgNameHashCode);
            if (channelNameHashCodeSet == null || !channelNameHashCodeSet.contains(channelNameHashCode)) {
                return false;
            }
            Log.i(TAG, "isNeedForbidAppAct true, pkgNameHashCode: " + pkgNameHashCode + ", channelNameHashCode: " + channelNameHashCode);
            return true;
        }
    }

    @Override // com.android.server.appactcontrol.IAppActScenes
    public void readXmlDataByScenes(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        HashSet<String> tmpForbiddenSet;
        String nodeName = xmlParser.getName();
        HashSet<String> tmpPackageForbiddenSet = new HashSet<>();
        HashSet<String> tmpChannelForbiddenSet = new HashSet<>();
        HashMap<String, HashSet<String>> tmpForbiddenMap = new HashMap<>();
        while (true) {
            if (xmlEventType != 3 || "item".equals(nodeName)) {
                xmlEventType = xmlParser.next();
                nodeName = xmlParser.getName();
                if (xmlEventType == 2 && "item".equals(nodeName)) {
                    String pkgName = xmlParser.getAttributeValue(null, AppActConstant.ATTR_PACKAGE_NAME);
                    String channelName = xmlParser.getAttributeValue(null, AppActConstant.ATTR_CHANNEL_NAME);
                    if (TextUtils.isEmpty(pkgName)) {
                        tmpChannelForbiddenSet.add(channelName);
                    } else if (TextUtils.isEmpty(channelName)) {
                        tmpPackageForbiddenSet.add(pkgName);
                    } else {
                        if (tmpForbiddenMap.containsKey(pkgName)) {
                            tmpForbiddenSet = tmpForbiddenMap.get(pkgName);
                        } else {
                            tmpForbiddenSet = new HashSet<>();
                            tmpForbiddenMap.put(pkgName, tmpForbiddenSet);
                        }
                        tmpForbiddenSet.add(channelName);
                    }
                }
            } else {
                this.packageForbiddenSet = tmpPackageForbiddenSet;
                this.channelForbiddenSet = tmpChannelForbiddenSet;
                this.forbiddenMap = tmpForbiddenMap;
                return;
            }
        }
    }
}
