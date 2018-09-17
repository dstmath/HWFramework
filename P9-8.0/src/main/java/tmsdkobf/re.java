package tmsdkobf;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Iterator;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.fg.module.cleanV2.AppGroupDesc;

public class re {
    public static HashMap<Integer, AppGroupDesc> kd() {
        HashMap<Integer, AppGroupDesc> hashMap = new HashMap();
        ea eaVar = (ea) mk.a(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEP_CLEAN_APPGROUP_DESC, UpdateConfig.intToString(40350), new ea(), "UTF-8");
        if (eaVar == null || eaVar.iC == null) {
            return null;
        }
        Iterator it = eaVar.iC.iterator();
        while (it.hasNext()) {
            dz dzVar = (dz) it.next();
            if (!(TextUtils.isEmpty(dzVar.iu) || TextUtils.isEmpty(dzVar.iv) || TextUtils.isEmpty(dzVar.iw) || TextUtils.isEmpty(dzVar.ix) || TextUtils.isEmpty(dzVar.iy))) {
                try {
                    AppGroupDesc appGroupDesc = new AppGroupDesc();
                    appGroupDesc.mType = Integer.valueOf(dzVar.iu).intValue();
                    if (appGroupDesc.mType == 1) {
                        appGroupDesc.mGroupId = Integer.valueOf(dzVar.iv).intValue();
                        appGroupDesc.mTitle = dzVar.iw;
                        appGroupDesc.mDesc = dzVar.ix;
                        appGroupDesc.mShowPhoto = dzVar.iy.equals("1");
                        if (dzVar.iy.equals("1") || dzVar.iy.equals("0")) {
                            hashMap.put(Integer.valueOf(appGroupDesc.mGroupId), appGroupDesc);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        AppGroupDesc appGroupDesc2 = new AppGroupDesc();
        appGroupDesc2.mGroupId = 1001;
        appGroupDesc2.mTitle = "微信缓存";
        appGroupDesc2.mDesc = "删除后可联网重新下载";
        appGroupDesc2.mShowPhoto = false;
        hashMap.put(Integer.valueOf(appGroupDesc2.mGroupId), appGroupDesc2);
        AppGroupDesc appGroupDesc3 = new AppGroupDesc();
        appGroupDesc3.mGroupId = 1002;
        appGroupDesc3.mTitle = "聊天产生的文件";
        appGroupDesc3.mDesc = "删除后无法恢复";
        appGroupDesc3.mShowPhoto = false;
        hashMap.put(Integer.valueOf(appGroupDesc3.mGroupId), appGroupDesc3);
        appGroupDesc2 = new AppGroupDesc();
        appGroupDesc2.mGroupId = 2001;
        appGroupDesc2.mTitle = "手Q产生的缓存";
        appGroupDesc2.mDesc = "删除后可联网重新下载";
        appGroupDesc2.mShowPhoto = false;
        hashMap.put(Integer.valueOf(appGroupDesc2.mGroupId), appGroupDesc2);
        appGroupDesc3 = new AppGroupDesc();
        appGroupDesc3.mGroupId = 2002;
        appGroupDesc3.mTitle = "聊天产生文件";
        appGroupDesc3.mDesc = "删除后无法恢复";
        appGroupDesc3.mShowPhoto = false;
        hashMap.put(Integer.valueOf(appGroupDesc3.mGroupId), appGroupDesc3);
        appGroupDesc3 = new AppGroupDesc();
        appGroupDesc3.mGroupId = 10;
        appGroupDesc3.mTitle = "下载的文件";
        appGroupDesc3.mDesc = "删除后文件将不可恢复，请再次确认是否需要删除。";
        appGroupDesc3.mShowPhoto = false;
        hashMap.put(Integer.valueOf(appGroupDesc3.mGroupId), appGroupDesc3);
        return hashMap;
    }
}
