package ohos.miscservices.area;

import ohos.rpc.RemoteException;

interface IAreaSysAbility {
    void addAreaListener(IAreaListener iAreaListener) throws RemoteException;

    String getISOAlpha2Code() throws RemoteException;

    void removeAreaListener(IAreaListener iAreaListener) throws RemoteException;
}
