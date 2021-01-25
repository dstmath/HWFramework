package ohos.ai.engine.bigreport;

import java.util.Optional;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class ReportCoreManager {
    private static final String TAG = ReportCoreManager.class.getSimpleName();
    private static volatile ReportCoreManager instance = null;
    private IRemoteObject coreService;

    private ReportCoreManager() {
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.coreService = iRemoteObject;
    }

    public static ReportCoreManager getInstance() {
        if (instance == null) {
            synchronized (ReportCoreManager.class) {
                if (instance == null) {
                    instance = new ReportCoreManager();
                }
            }
        }
        return instance;
    }

    public Optional<IReportCore> getReportCore() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.coreService).orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "iCoreService is null");
            return Optional.empty();
        }
        try {
            return ReportCoreSkeleton.asInterface(orElse.getReportCoreRemoteObject());
        } catch (RemoteException e) {
            String str = TAG;
            HiAILog.error(str, "getReportCore e" + e.getMessage());
            return Optional.empty();
        }
    }

    public void onInterfaceReport(String str, String str2, InterfaceInfo interfaceInfo) {
        HiAILog.info(TAG, "onInterfaceReport");
        if (interfaceInfo == null) {
            HiAILog.error(TAG, "interfaceInfo is null");
            return;
        }
        IReportCore orElse = getReportCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "reportCore is null");
            return;
        }
        try {
            orElse.onInterfaceReport(str, str2, interfaceInfo);
        } catch (RemoteException e) {
            String str3 = TAG;
            HiAILog.error(str3, "RemoteException e" + e.getMessage());
        }
    }

    public void onOperationReport(String str, String str2, OperationInfo operationInfo) {
        HiAILog.info(TAG, "onOperationReport");
        if (operationInfo == null) {
            HiAILog.error(TAG, "operationInfo is null");
            return;
        }
        IReportCore orElse = getReportCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "reportCore is null");
            return;
        }
        try {
            orElse.onOperationReport(str, str2, operationInfo);
        } catch (RemoteException e) {
            String str3 = TAG;
            HiAILog.error(str3, "RemoteException e" + e.getMessage());
        }
    }

    public void onScheduleReport(String str, String str2, ScheduleInfo scheduleInfo) {
        HiAILog.info(TAG, "onScheduleReport");
        if (scheduleInfo == null) {
            HiAILog.error(TAG, "scheduleInfo is null");
            return;
        }
        IReportCore orElse = getReportCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "reportCore is null");
            return;
        }
        try {
            orElse.onScheduleReport(str, str2, scheduleInfo);
        } catch (RemoteException e) {
            String str3 = TAG;
            HiAILog.error(str3, "RemoteException e" + e.getMessage());
        }
    }

    public void onOriginDataReport(String str, OriginInfo originInfo) {
        HiAILog.info(TAG, "onOriginDataReport");
        if (originInfo == null) {
            HiAILog.error(TAG, "originInfo is null");
            return;
        }
        IReportCore orElse = getReportCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "reportCore is null");
            return;
        }
        try {
            orElse.onOriginDataReport(str, originInfo);
        } catch (RemoteException e) {
            String str2 = TAG;
            HiAILog.error(str2, "RemoteException e" + e.getMessage());
        }
    }

    public void onMixedBuildInterfaceReport(MixedBuildInterfaceInfo mixedBuildInterfaceInfo) {
        HiAILog.info(TAG, "onMixedBuildInterfaceReport");
        if (mixedBuildInterfaceInfo == null) {
            HiAILog.error(TAG, "mixedBuildInterfaceInfo is null");
            return;
        }
        IReportCore orElse = getReportCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "reportCore is null");
            return;
        }
        try {
            orElse.onMixedBuildInterfaceReport(mixedBuildInterfaceInfo);
        } catch (RemoteException e) {
            String str = TAG;
            HiAILog.error(str, "RemoteException e" + e.getMessage());
        }
    }
}
