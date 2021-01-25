package ohos.abilityshell.utils;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.AbilityShellData;
import ohos.abilityshell.DistributedImpl;
import ohos.abilityshell.IDistributedManager;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.HiTraceUtil;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.rpc.RemoteException;

public class SelectAbilityUtils {
    private static final IDistributedManager DISTRIBUTED_IMPL = new DistributedImpl();

    private SelectAbilityUtils() {
    }

    public static List<AbilityShellData> fetchAbilities(Context context, Intent intent) {
        List<AbilityShellData> list = null;
        if (intent == null) {
            return null;
        }
        HiTraceId hiTraceBegin = HiTraceUtil.hiTraceBegin("fetchAbilities");
        boolean z = intent.getElement() != null && (intent.getElement().getDeviceId() == null || intent.getElement().getDeviceId().isEmpty() || (intent.getFlags() & 256) == 0);
        if (!AbilityShellConverterUtils.isAndroidComponent(context, intent) || !z) {
            List<AbilityShellData> androidShellDatas = intent.getElement() == null ? AbilityShellConverterUtils.getAndroidShellDatas(context, intent) : null;
            try {
                list = DISTRIBUTED_IMPL.fetchAbilities(intent);
            } catch (RemoteException e) {
                AppLog.e("ContextDeal::fetchAbilities RemoteException: %{public}s", e.getMessage());
            }
            if (androidShellDatas == null || androidShellDatas.isEmpty()) {
                return list;
            }
            if (list == null || list.isEmpty()) {
                return androidShellDatas;
            }
            for (AbilityShellData abilityShellData : androidShellDatas) {
                if (!containsShellData(abilityShellData, list)) {
                    list.add(0, abilityShellData);
                }
            }
            HiTrace.end(hiTraceBegin);
            return list;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(AbilityShellConverterUtils.createAbilityShellData(intent, true));
        return arrayList;
    }

    private static boolean containsShellData(AbilityShellData abilityShellData, List<AbilityShellData> list) {
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        for (AbilityShellData abilityShellData2 : list) {
            if (abilityShellData.getLocal() == abilityShellData2.getLocal() && shellInfo.equals(abilityShellData2.getShellInfo())) {
                return true;
            }
        }
        return false;
    }
}
