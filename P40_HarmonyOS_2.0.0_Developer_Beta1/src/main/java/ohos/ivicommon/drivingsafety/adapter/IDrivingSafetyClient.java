package ohos.ivicommon.drivingsafety.adapter;

import ohos.app.Context;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.ivicommon.drivingsafety.model.Position;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IDrivingSafetyClient extends IRemoteBroker {
    int getRestraint() throws RemoteException;

    Position getSecondaryScreenRange() throws RemoteException, IllegalArgumentException;

    boolean isDrivingMode() throws RemoteException;

    boolean isDrivingSafety(Context context, ControlItemEnum controlItemEnum, Position position) throws RemoteException;

    boolean isSecondaryScreenRange(Position position) throws RemoteException, IllegalArgumentException;
}
