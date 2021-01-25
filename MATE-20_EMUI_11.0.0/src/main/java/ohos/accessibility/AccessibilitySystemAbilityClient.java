package ohos.accessibility;

import java.util.Collections;
import java.util.List;
import ohos.accessibility.adapter.AccessibilitySystemAbilityAdapter;
import ohos.accessibility.adapter.AdapterInterface;
import ohos.accessibility.utils.LogUtil;
import ohos.app.Context;

public class AccessibilitySystemAbilityClient {
    private static final Object LOCK_OBJECT = new Object();
    private static final String TAG = "AccessibilitySystemAbilityClient";
    private static AccessibilitySystemAbilityClient instance = new AccessibilitySystemAbilityClient();
    private AdapterInterface adapter;

    private AccessibilitySystemAbilityClient() {
    }

    public static AccessibilitySystemAbilityClient getInstance() {
        return instance;
    }

    public static AccessibilitySystemAbilityClient getInstance(Context context) {
        if (context == null) {
            return instance;
        }
        synchronized (LOCK_OBJECT) {
            if (instance.adapter == null) {
                instance.adapter = new AccessibilitySystemAbilityAdapter(context);
            }
        }
        return instance;
    }

    public boolean addContext(Context context) {
        if (context == null) {
            LogUtil.error(TAG, "context is null.");
            return false;
        }
        this.adapter = new AccessibilitySystemAbilityAdapter(context);
        return true;
    }

    public boolean sendEvent(int i, int i2) {
        LogUtil.debug(TAG, "begin sendEvent,time:" + System.nanoTime());
        AccessibilityEventInfo accessibilityEventInfo = new AccessibilityEventInfo(i);
        accessibilityEventInfo.setViewId(i2);
        boolean sendEvent = sendEvent(accessibilityEventInfo);
        LogUtil.debug(TAG, "end sendEvent,time:" + System.nanoTime());
        return sendEvent;
    }

    public boolean sendEvent(AccessibilityEventInfo accessibilityEventInfo) {
        if (accessibilityEventInfo != null) {
            return BarrierFreeInnerClient.sendBarrierFreeEvent(null, accessibilityEventInfo);
        }
        LogUtil.error(TAG, "sendEvent fail.");
        return false;
    }

    public boolean sendEvent(Context context, AccessibilityEventInfo accessibilityEventInfo) {
        if (accessibilityEventInfo != null) {
            return BarrierFreeInnerClient.sendBarrierFreeEvent(context, accessibilityEventInfo);
        }
        LogUtil.error(TAG, "sendEvent failed, event is null.");
        return false;
    }

    public List<AccessibilityAbilityInfo> getAbilityList(int i, int i2) {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface != null) {
            return adapterInterface.getAbilityList(i, i2);
        }
        LogUtil.error(TAG, "getAbilityList fail, adapter is null.");
        return Collections.emptyList();
    }

    public boolean isEnabled() {
        if (this.adapter == null) {
            LogUtil.error(TAG, "Get isEnabled fail, adapter is null.");
            return false;
        }
        LogUtil.debug(TAG, "begin isEnable,time:" + System.nanoTime());
        boolean isEnabled = this.adapter.isEnabled();
        LogUtil.debug(TAG, "end isEnable,time:" + System.nanoTime());
        return isEnabled;
    }

    public boolean isTouchExplorationEnabled() {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface != null) {
            return adapterInterface.isTouchExplorationEnabled();
        }
        LogUtil.error(TAG, "Get isTouchExplorationEnabled fail, adapter is null.");
        return false;
    }

    public boolean subscribeStateObserver(AccessibilityStateObserver accessibilityStateObserver, int i) {
        if (this.adapter == null) {
            LogUtil.error(TAG, "subscribeStateObserver fail, adapter is null.");
            return false;
        } else if (accessibilityStateObserver == null) {
            LogUtil.error(TAG, "subscribeStateObserver fail, observer is null.");
            return false;
        } else if (i == 1) {
            LogUtil.debug(TAG, "begin addAccessibilityStateChangeListener,time:" + System.nanoTime());
            boolean addAccessibilityStateChangeListener = this.adapter.addAccessibilityStateChangeListener(accessibilityStateObserver);
            LogUtil.debug(TAG, "end addAccessibilityStateChangeListener,time:" + System.nanoTime());
            return addAccessibilityStateChangeListener;
        } else if (i != 2) {
            return false;
        } else {
            LogUtil.debug(TAG, "begin addTouchExplorationStateChangeListener,time:" + System.nanoTime());
            boolean addTouchExplorationStateChangeListener = this.adapter.addTouchExplorationStateChangeListener(accessibilityStateObserver);
            LogUtil.debug(TAG, "end addTouchExplorationStateChangeListener,time:" + System.nanoTime());
            return addTouchExplorationStateChangeListener;
        }
    }

    public boolean unsubscribeStateObserver(AccessibilityStateObserver accessibilityStateObserver, int i) {
        if (this.adapter == null) {
            LogUtil.error(TAG, "unsubscribeStateObserver fail, adapter is null.");
            return false;
        } else if (accessibilityStateObserver == null) {
            LogUtil.error(TAG, "unsubscribeStateObserver fail, observer is null.");
            return false;
        } else if (i == 1) {
            LogUtil.debug(TAG, "begin removeAccessibilityStateChangeListener,time:" + System.nanoTime());
            boolean removeAccessibilityStateChangeListener = this.adapter.removeAccessibilityStateChangeListener(accessibilityStateObserver);
            LogUtil.debug(TAG, "end removeAccessibilityStateChangeListener,time:" + System.nanoTime());
            return removeAccessibilityStateChangeListener;
        } else if (i != 2) {
            return false;
        } else {
            LogUtil.debug(TAG, "begin removeTouchExplorationStateChangeListener,time:" + System.nanoTime());
            boolean removeTouchExplorationStateChangeListener = this.adapter.removeTouchExplorationStateChangeListener(accessibilityStateObserver);
            LogUtil.debug(TAG, "end removeTouchExplorationStateChangeListener,time:" + System.nanoTime());
            return removeTouchExplorationStateChangeListener;
        }
    }

    public boolean unsubscribeStateObserver(AccessibilityStateObserver accessibilityStateObserver) {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface == null) {
            LogUtil.error(TAG, "unsubscribeStateObserver fail, adapter is null.");
            return false;
        } else if (accessibilityStateObserver != null) {
            return adapterInterface.removeStateChangeListeners(accessibilityStateObserver);
        } else {
            LogUtil.error(TAG, "unsubscribeStateObserver fail, observer is null.");
            return false;
        }
    }

    public boolean isAccessibilityCaptionEnabled() {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface != null) {
            return adapterInterface.isAccessibilityCaptionEnabled();
        }
        LogUtil.error(TAG, "Get isAccessibilityCaptionEnabled fail, adapter is null.");
        return false;
    }

    public CaptionProperties getAccessibilityCaptionProperties() {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface != null) {
            return adapterInterface.getCaptionProperties();
        }
        LogUtil.error(TAG, "getAccessibilityCaptionProperties fail, adapter is null.");
        return null;
    }

    public boolean subscribeCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver) {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface == null) {
            LogUtil.error(TAG, "subscribeCaptionPropertiesObserver fail, adapter is null.");
            return false;
        } else if (captionPropertiesObserver != null) {
            return adapterInterface.addCaptionPropertiesObserver(captionPropertiesObserver);
        } else {
            LogUtil.error(TAG, "subscribeCaptionPropertiesObserver fail, observer is null.");
            return false;
        }
    }

    public boolean unSubscribeCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver) {
        AdapterInterface adapterInterface = this.adapter;
        if (adapterInterface == null) {
            LogUtil.error(TAG, "unSubscribeCaptionPropertiesObserver fail, adapter is null.");
            return false;
        } else if (captionPropertiesObserver != null) {
            return adapterInterface.removeCaptionPropertiesObserver(captionPropertiesObserver);
        } else {
            LogUtil.error(TAG, "unSubscribeCaptionPropertiesObserver fail, observer is null.");
            return false;
        }
    }
}
