package ohos.location.callback;

import ohos.annotation.SystemApi;
import ohos.location.Location;
import ohos.location.RequestParam;
import ohos.rpc.IRemoteObject;

@SystemApi
public interface ILocatorAdapter {
    void enableAbility(boolean z);

    Location getCachedLocation() throws SecurityException;

    boolean isLocationSwitchEnbale();

    boolean registerSwitchCallback(IRemoteObject iRemoteObject);

    void requestEnable() throws IllegalArgumentException;

    void startLocating(RequestParam requestParam, IRemoteObject iRemoteObject, int i);

    void stopLocating(IRemoteObject iRemoteObject);

    boolean unregisterSwitchCallback(IRemoteObject iRemoteObject);
}
