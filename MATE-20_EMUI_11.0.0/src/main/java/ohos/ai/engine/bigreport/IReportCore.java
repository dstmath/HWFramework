package ohos.ai.engine.bigreport;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IReportCore extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.bigreport.IReportCore";
    public static final int ON_INTERFACE_REPORT = 1;
    public static final int ON_MIXED_BUILD_INTERFACE_REPORT = 5;
    public static final int ON_OPERATION_REPORT = 2;
    public static final int ON_ORIGINDATE_REPORT = 4;
    public static final int ON_SCHEDULE_REPORT = 3;

    void onInterfaceReport(String str, String str2, InterfaceInfo interfaceInfo) throws RemoteException;

    void onMixedBuildInterfaceReport(MixedBuildInterfaceInfo mixedBuildInterfaceInfo) throws RemoteException;

    void onOperationReport(String str, String str2, OperationInfo operationInfo) throws RemoteException;

    void onOriginDataReport(String str, OriginInfo originInfo) throws RemoteException;

    void onScheduleReport(String str, String str2, ScheduleInfo scheduleInfo) throws RemoteException;
}
