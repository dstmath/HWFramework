package ohos.ace.featureabilityplugin.requestprocess;

import com.huawei.ace.plugin.Result;
import com.huawei.ace.runtime.ALog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.ace.featureabilityplugin.requestparse.ParsedJsRequest;
import ohos.ace.featureabilityplugin.requestparse.RequestParse;
import ohos.app.AbilityContext;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.IBundleManager;
import ohos.bundle.ProfileConstants;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.rpc.RemoteException;

public class QueryAbilityProcess {
    private static final QueryAbilityProcess INSTANCE = new QueryAbilityProcess();
    private static final String TAG = QueryAbilityProcess.class.getSimpleName();

    public static QueryAbilityProcess getInstance() {
        return INSTANCE;
    }

    public void queryAbility(AbilityContext abilityContext, List<Object> list, Result result) {
        if (result == null) {
            ALog.e(TAG, "query ability result handler is null!");
        } else if (abilityContext == null || list == null) {
            ALog.e(TAG, "query ability context or arguments is null");
            result.error(2001, "query ability context or arguments is null");
        } else {
            ParsedJsRequest parsedJsRequest = new ParsedJsRequest();
            if (!RequestParse.getInstance().checkAndParseRequest(list, parsedJsRequest, 4)) {
                result.error(2002, parsedJsRequest.getParseErrorMessage());
                return;
            }
            Intent intent = new Intent();
            setIntent(intent, parsedJsRequest);
            IBundleManager bundleManager = abilityContext.getBundleManager();
            if (bundleManager == null) {
                ALog.e(TAG, "query ability get bundleManager null");
                result.error(2001, "query ability get bundleManager null");
                return;
            }
            try {
                List<AbilityInfo> queryAbilityByIntent = bundleManager.queryAbilityByIntent(intent);
                ArrayList arrayList = new ArrayList();
                if (queryAbilityByIntent == null) {
                    ALog.i(TAG, "query ability ret list is empty");
                    result.success(arrayList);
                    return;
                }
                fillAbilityDeviceInfo(queryAbilityByIntent, arrayList);
                result.success(arrayList);
            } catch (RemoteException e) {
                String str = TAG;
                ALog.e(str, "query ability failed:" + e.getMessage());
                result.error(2011, "query ability failed:" + e.getMessage());
            }
        }
    }

    private void setIntent(Intent intent, ParsedJsRequest parsedJsRequest) {
        intent.setFlags(256);
        if (parsedJsRequest.getIntentType()) {
            intent.setElement(new ElementName("", parsedJsRequest.getBundleName(), parsedJsRequest.getAbilityName()));
            return;
        }
        intent.setAction(parsedJsRequest.getAction());
        List<String> entities = parsedJsRequest.getEntities();
        if (entities != null) {
            for (String str : entities) {
                intent.addEntity(str);
            }
        }
    }

    private void fillAbilityDeviceInfo(List<AbilityInfo> list, List<Map<String, String>> list2) {
        DeviceManager deviceManager = new DeviceManager();
        List<BasicInfo> nodesBasicInfo = deviceManager.getNodesBasicInfo();
        Optional localBasicInfo = deviceManager.getLocalBasicInfo();
        if (localBasicInfo.isPresent()) {
            combineAbilityAndDeviceInfo(list, (BasicInfo) localBasicInfo.get(), list2);
        }
        for (BasicInfo basicInfo : nodesBasicInfo) {
            combineAbilityAndDeviceInfo(list, basicInfo, list2);
        }
    }

    private void combineAbilityAndDeviceInfo(List<AbilityInfo> list, BasicInfo basicInfo, List<Map<String, String>> list2) {
        for (AbilityInfo abilityInfo : list) {
            if (abilityInfo.getDeviceId().equals(basicInfo.getNodeId())) {
                HashMap hashMap = new HashMap();
                hashMap.put("deviceId", abilityInfo.getDeviceId());
                hashMap.put("deviceName", basicInfo.getName());
                hashMap.put(ProfileConstants.BUNDLE_NAME, abilityInfo.getBundleName());
                hashMap.put("abilityName", abilityInfo.getClassName());
                list2.add(hashMap);
            }
        }
    }
}
