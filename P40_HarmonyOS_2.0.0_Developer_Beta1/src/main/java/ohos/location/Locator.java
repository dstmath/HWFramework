package ohos.location;

import java.util.HashMap;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.callback.LocatorCallbackHost;
import ohos.location.callback.SwitchCallbackHost;
import ohos.location.common.LBSLog;

public class Locator {
    public static final int ERROR_PERMISSION_NOT_GRANTED = 256;
    public static final int ERROR_SWITCH_UNOPEN = 257;
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "Locator");
    public static final int LOCATION_SERVICE_SWITCH_OFF = 0;
    public static final int LOCATION_SERVICE_SWITCH_ON = 1;
    private static final int REQUEST_CONTINUOUS_FIX = 0;
    private static final int REQUEST_ONE_FIX = 1;
    public static final int SESSION_START = 2;
    public static final int SESSION_STOP = 3;
    private LocatorAdapter mLocatorAdapter = LocatorAdapter.getInstance();
    private final HashMap<LocatorCallback, LocatorCallbackHost> mLocatorCallbacks = new HashMap<>();
    private final HashMap<SwitchCallback, SwitchCallbackHost> mSwitchCallbacks = new HashMap<>();

    @Deprecated
    public Locator(AbilityInfo abilityInfo) {
        this.mLocatorAdapter.setAbilityInfo(abilityInfo);
    }

    public Locator(Context context) {
        this.mLocatorAdapter.setContext(context);
    }

    public boolean isLocationSwitchOn() {
        return this.mLocatorAdapter.isLocationSwitchEnbale();
    }

    public void requestEnableLocation() throws IllegalArgumentException {
        this.mLocatorAdapter.requestEnable();
    }

    @SystemApi
    public void enableLocation(boolean z) {
        this.mLocatorAdapter.enableAbility(z);
    }

    public void startLocating(RequestParam requestParam, LocatorCallback locatorCallback) throws IllegalArgumentException {
        startLocating(requestParam, locatorCallback, 0);
    }

    public void stopLocating(LocatorCallback locatorCallback) throws IllegalArgumentException {
        LocatorCallbackHost remove;
        if (locatorCallback != null) {
            HiLog.info(LABEL, "stopLocating callback: %{public}s", Integer.toHexString(System.identityHashCode(locatorCallback)));
            synchronized (this.mLocatorCallbacks) {
                remove = this.mLocatorCallbacks.remove(locatorCallback);
            }
            if (remove != null) {
                this.mLocatorAdapter.stopLocating(remove);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("callback cannot be null");
    }

    public void requestOnce(RequestParam requestParam, LocatorCallback locatorCallback) throws IllegalArgumentException {
        startLocating(requestParam, locatorCallback, 1);
    }

    private void startLocating(RequestParam requestParam, LocatorCallback locatorCallback, int i) throws IllegalArgumentException {
        if (requestParam == null || locatorCallback == null) {
            throw new IllegalArgumentException("param cannot be null");
        }
        HiLog.info(LABEL, "startLocating callback: %{public}s", Integer.toHexString(System.identityHashCode(locatorCallback)));
        this.mLocatorAdapter.startLocating(requestParam, wrapLocatorCallback(locatorCallback), i);
    }

    public Location getCachedLocation() throws SecurityException {
        return this.mLocatorAdapter.getCachedLocation();
    }

    public boolean registerSwitchCallback(SwitchCallback switchCallback) throws IllegalArgumentException {
        if (switchCallback != null) {
            return this.mLocatorAdapter.registerSwitchCallback(wrapSwitchCallback(switchCallback, true));
        }
        throw new IllegalArgumentException("callback cannot be null");
    }

    public boolean unregisterSwitchCallback(SwitchCallback switchCallback) throws IllegalArgumentException {
        if (switchCallback != null) {
            return this.mLocatorAdapter.unregisterSwitchCallback(wrapSwitchCallback(switchCallback, false));
        }
        throw new IllegalArgumentException("callback cannot be null");
    }

    private LocatorCallbackHost wrapLocatorCallback(LocatorCallback locatorCallback) {
        LocatorCallbackHost locatorCallbackHost;
        synchronized (this.mLocatorCallbacks) {
            locatorCallbackHost = this.mLocatorCallbacks.get(locatorCallback);
            if (locatorCallbackHost == null) {
                locatorCallbackHost = new LocatorCallbackHost(locatorCallback);
            }
            this.mLocatorCallbacks.put(locatorCallback, locatorCallbackHost);
        }
        return locatorCallbackHost;
    }

    private SwitchCallbackHost wrapSwitchCallback(SwitchCallback switchCallback, boolean z) {
        SwitchCallbackHost switchCallbackHost;
        synchronized (this.mSwitchCallbacks) {
            switchCallbackHost = this.mSwitchCallbacks.get(switchCallback);
            if (switchCallbackHost == null) {
                switchCallbackHost = new SwitchCallbackHost(switchCallback);
            }
            this.mSwitchCallbacks.put(switchCallback, switchCallbackHost);
            if (z) {
                this.mSwitchCallbacks.put(switchCallback, switchCallbackHost);
            } else {
                this.mSwitchCallbacks.remove(switchCallback);
            }
        }
        return switchCallbackHost;
    }
}
