package ohos.ai.engine.bigreport;

import java.util.LinkedHashMap;
import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class ReportCoreSkeleton extends RemoteObject implements IReportCore {
    public IRemoteObject asObject() {
        return this;
    }

    public ReportCoreSkeleton() {
        super(IReportCore.DESCRIPTOR);
    }

    public static Optional<IReportCore> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IReportCore queryLocalInterface = iRemoteObject.queryLocalInterface(IReportCore.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IReportCore)) {
            return Optional.ofNullable(new ReportCoreProxy(iRemoteObject));
        }
        return Optional.ofNullable(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i == 1) {
            String readString = messageParcel.readString();
            String readString2 = messageParcel.readString();
            InterfaceInfo interfaceInfo = new InterfaceInfo();
            messageParcel.readSequenceable(interfaceInfo);
            onInterfaceReport(readString, readString2, interfaceInfo);
            return true;
        } else if (i == 2) {
            String readString3 = messageParcel.readString();
            String readString4 = messageParcel.readString();
            OperationInfo operationInfo = new OperationInfo();
            messageParcel.readSequenceable(operationInfo);
            onOperationReport(readString3, readString4, operationInfo);
            return true;
        } else if (i == 3) {
            String readString5 = messageParcel.readString();
            String readString6 = messageParcel.readString();
            ScheduleInfo scheduleInfo = new ScheduleInfo();
            messageParcel.readSequenceable(scheduleInfo);
            onScheduleReport(readString5, readString6, scheduleInfo);
            return true;
        } else if (i == 4) {
            String readString7 = messageParcel.readString();
            OriginInfo originInfo = new OriginInfo(new LinkedHashMap());
            messageParcel.readSequenceable(originInfo);
            onOriginDataReport(readString7, originInfo);
            return true;
        } else if (i != 5) {
            return ReportCoreSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            MixedBuildInterfaceInfo mixedBuildInterfaceInfo = new MixedBuildInterfaceInfo();
            messageParcel.readSequenceable(mixedBuildInterfaceInfo);
            onMixedBuildInterfaceReport(mixedBuildInterfaceInfo);
            return true;
        }
    }
}
