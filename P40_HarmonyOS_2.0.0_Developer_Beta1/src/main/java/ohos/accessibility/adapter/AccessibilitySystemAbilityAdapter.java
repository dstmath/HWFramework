package ohos.accessibility.adapter;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.ArrayMap;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import ohos.accessibility.AccessibilityAbilityInfo;
import ohos.accessibility.AccessibilityStateEvent;
import ohos.accessibility.AccessibilityStateObserver;
import ohos.accessibility.CaptionProperties;
import ohos.accessibility.CaptionPropertiesObserver;
import ohos.accessibility.utils.LogUtil;
import ohos.bundle.AbilityInfo;

public class AccessibilitySystemAbilityAdapter implements AdapterInterface {
    private static final String TAG = "AccessibilitySystemAbilityAdapter";
    private static Map<CaptionPropertiesObserver, CaptioningManager.CaptioningChangeListener> sCaptionPropertiesListeners = new ArrayMap();
    private static Map<AccessibilityStateObserver, AccessibilityManager.AccessibilityStateChangeListener> sStateListeners = new ArrayMap();
    private static Map<AccessibilityStateObserver, AccessibilityManager.TouchExplorationStateChangeListener> sTouchStateListeners = new ArrayMap();
    private AccessibilityManager mAccessibilityManager = null;
    private Context mAndroidContext = null;

    public AccessibilitySystemAbilityAdapter() {
    }

    public AccessibilitySystemAbilityAdapter(ohos.app.Context context) {
        convertAbilityToContext(context);
        init();
    }

    private void init() {
        LogUtil.info(TAG, "init accessibility client");
        Context context = this.mAndroidContext;
        if (context != null) {
            this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        }
    }

    private void convertAbilityToContext(ohos.app.Context context) {
        if (context == null) {
            LogUtil.info(TAG, "abilityContext is null, just return.");
            return;
        }
        Object hostContext = context.getHostContext();
        if (hostContext != null && (hostContext instanceof Context)) {
            this.mAndroidContext = ((Context) hostContext).getApplicationContext();
        }
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean isEnabled() {
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager != null) {
            return accessibilityManager.isEnabled();
        }
        LogUtil.error(TAG, "AccessibilityManager is null");
        return false;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean isTouchExplorationEnabled() {
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager != null) {
            return accessibilityManager.isTouchExplorationEnabled();
        }
        LogUtil.error(TAG, "AccessibilityManager is null");
        return false;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean addAccessibilityStateChangeListener(AccessibilityStateObserver accessibilityStateObserver) {
        if (accessibilityStateObserver == null) {
            return false;
        }
        if (this.mAccessibilityManager == null) {
            LogUtil.error(TAG, "AccessibilityManager is null");
            return false;
        }
        $$Lambda$AccessibilitySystemAbilityAdapter$dwbrgJ7I4C2pB6cPoG2nOWAOY8 r0 = new AccessibilityManager.AccessibilityStateChangeListener(accessibilityStateObserver) {
            /* class ohos.accessibility.adapter.$$Lambda$AccessibilitySystemAbilityAdapter$dwbrgJ7I4C2pB6cPoG2nOWAOY8 */
            private final /* synthetic */ AccessibilityStateObserver f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener
            public final void onAccessibilityStateChanged(boolean z) {
                AccessibilitySystemAbilityAdapter.this.lambda$addAccessibilityStateChangeListener$0$AccessibilitySystemAbilityAdapter(this.f$1, z);
            }
        };
        sStateListeners.put(accessibilityStateObserver, r0);
        return this.mAccessibilityManager.addAccessibilityStateChangeListener(r0);
    }

    public /* synthetic */ void lambda$addAccessibilityStateChangeListener$0$AccessibilitySystemAbilityAdapter(AccessibilityStateObserver accessibilityStateObserver, boolean z) {
        AccessibilityStateEvent accessibilityStateEvent = new AccessibilityStateEvent();
        accessibilityStateEvent.setEventType(1);
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager == null || !accessibilityManager.isEnabled()) {
            accessibilityStateEvent.setEventResult(1);
        } else {
            accessibilityStateEvent.setEventResult(0);
        }
        if (accessibilityStateObserver != null) {
            accessibilityStateObserver.onStateChanged(accessibilityStateEvent);
        }
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean removeAccessibilityStateChangeListener(AccessibilityStateObserver accessibilityStateObserver) {
        if (accessibilityStateObserver == null) {
            return false;
        }
        if (this.mAccessibilityManager == null) {
            LogUtil.error(TAG, "AccessibilityManager is null.");
            return false;
        }
        AccessibilityManager.AccessibilityStateChangeListener accessibilityStateChangeListener = sStateListeners.get(accessibilityStateObserver);
        if (accessibilityStateChangeListener == null) {
            return false;
        }
        sStateListeners.remove(accessibilityStateObserver);
        return this.mAccessibilityManager.removeAccessibilityStateChangeListener(accessibilityStateChangeListener);
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean addTouchExplorationStateChangeListener(AccessibilityStateObserver accessibilityStateObserver) {
        if (accessibilityStateObserver == null) {
            return false;
        }
        if (this.mAccessibilityManager == null) {
            LogUtil.error(TAG, "AccessibilityManager is null.");
            return false;
        }
        $$Lambda$AccessibilitySystemAbilityAdapter$jyr02hiFGnoJ5ojyXU2gTvTHnk r0 = new AccessibilityManager.TouchExplorationStateChangeListener(accessibilityStateObserver) {
            /* class ohos.accessibility.adapter.$$Lambda$AccessibilitySystemAbilityAdapter$jyr02hiFGnoJ5ojyXU2gTvTHnk */
            private final /* synthetic */ AccessibilityStateObserver f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener
            public final void onTouchExplorationStateChanged(boolean z) {
                AccessibilitySystemAbilityAdapter.this.lambda$addTouchExplorationStateChangeListener$1$AccessibilitySystemAbilityAdapter(this.f$1, z);
            }
        };
        sTouchStateListeners.put(accessibilityStateObserver, r0);
        return this.mAccessibilityManager.addTouchExplorationStateChangeListener(r0);
    }

    public /* synthetic */ void lambda$addTouchExplorationStateChangeListener$1$AccessibilitySystemAbilityAdapter(AccessibilityStateObserver accessibilityStateObserver, boolean z) {
        AccessibilityStateEvent accessibilityStateEvent = new AccessibilityStateEvent();
        accessibilityStateEvent.setEventType(2);
        if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
            accessibilityStateEvent.setEventResult(0);
        } else {
            accessibilityStateEvent.setEventResult(1);
        }
        if (accessibilityStateObserver != null) {
            accessibilityStateObserver.onStateChanged(accessibilityStateEvent);
        }
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean removeTouchExplorationStateChangeListener(AccessibilityStateObserver accessibilityStateObserver) {
        AccessibilityManager.TouchExplorationStateChangeListener touchExplorationStateChangeListener;
        if (!(accessibilityStateObserver == null || (touchExplorationStateChangeListener = sTouchStateListeners.get(accessibilityStateObserver)) == null)) {
            sTouchStateListeners.remove(accessibilityStateObserver);
            AccessibilityManager accessibilityManager = this.mAccessibilityManager;
            if (accessibilityManager != null) {
                return accessibilityManager.removeTouchExplorationStateChangeListener(touchExplorationStateChangeListener);
            }
        }
        return false;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean removeStateChangeListeners(AccessibilityStateObserver accessibilityStateObserver) {
        if (accessibilityStateObserver == null) {
            return false;
        }
        if (sTouchStateListeners.get(accessibilityStateObserver) == null && sStateListeners.get(accessibilityStateObserver) == null) {
            return false;
        }
        boolean removeTouchExplorationStateChangeListener = sTouchStateListeners.get(accessibilityStateObserver) != null ? removeTouchExplorationStateChangeListener(accessibilityStateObserver) : true;
        boolean removeAccessibilityStateChangeListener = sStateListeners.get(accessibilityStateObserver) != null ? removeAccessibilityStateChangeListener(accessibilityStateObserver) : true;
        if (!removeTouchExplorationStateChangeListener || !removeAccessibilityStateChangeListener) {
            return false;
        }
        return true;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public List<AccessibilityAbilityInfo> getAbilityList(int i, int i2) {
        LogUtil.info(TAG, "getAbilityList start. ability type: " + i + " abilityFlags " + i2);
        ArrayList arrayList = new ArrayList(0);
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager == null) {
            LogUtil.error(TAG, "accessibilityManager is null");
            return arrayList;
        } else if (i2 == 1) {
            return convertServiceInfoToAbilityInfo(accessibilityManager.getEnabledAccessibilityServiceList(i));
        } else {
            if (i2 == 2) {
                List<AccessibilityServiceInfo> matchServiceInfoList = getMatchServiceInfoList(i, accessibilityManager.getInstalledAccessibilityServiceList());
                for (AccessibilityServiceInfo accessibilityServiceInfo : this.mAccessibilityManager.getEnabledAccessibilityServiceList(i)) {
                    String id = accessibilityServiceInfo.getId();
                    if (id != null) {
                        matchServiceInfoList.removeIf(new Predicate(id) {
                            /* class ohos.accessibility.adapter.$$Lambda$AccessibilitySystemAbilityAdapter$5uY6fLba1mZGdXXf9xWnOgNM50 */
                            private final /* synthetic */ String f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Predicate
                            public final boolean test(Object obj) {
                                return this.f$0.equals(((AccessibilityServiceInfo) obj).getId());
                            }
                        });
                    }
                }
                return convertServiceInfoToAbilityInfo(matchServiceInfoList);
            } else if (i2 == 3) {
                return convertServiceInfoToAbilityInfo(getMatchServiceInfoList(i, accessibilityManager.getInstalledAccessibilityServiceList()));
            } else {
                LogUtil.error(TAG, "abilityFlags is invalid");
                return arrayList;
            }
        }
    }

    private List<AccessibilityServiceInfo> getMatchServiceInfoList(int i, List<AccessibilityServiceInfo> list) {
        ArrayList arrayList = new ArrayList(0);
        if (list.isEmpty()) {
            LogUtil.info(TAG, "serviceInfoList is Empty");
            return arrayList;
        }
        for (AccessibilityServiceInfo accessibilityServiceInfo : list) {
            if (accessibilityServiceInfo.feedbackType == (accessibilityServiceInfo.feedbackType | i)) {
                arrayList.add(accessibilityServiceInfo);
            }
        }
        return arrayList;
    }

    private List<AccessibilityAbilityInfo> convertServiceInfoToAbilityInfo(List<AccessibilityServiceInfo> list) {
        ArrayList arrayList = new ArrayList(0);
        if (list.isEmpty()) {
            LogUtil.info(TAG, "serviceInfoList is Empty, convert fail");
            return arrayList;
        }
        for (AccessibilityServiceInfo accessibilityServiceInfo : list) {
            AccessibilityAbilityInfo accessibilityAbilityInfo = new AccessibilityAbilityInfo();
            accessibilityAbilityInfo.setAccessibilityAbilityType(accessibilityServiceInfo.feedbackType);
            accessibilityAbilityInfo.setEventTypes(accessibilityServiceInfo.eventTypes);
            accessibilityAbilityInfo.setId(accessibilityServiceInfo.getId());
            accessibilityAbilityInfo.setCapabilityValues(accessibilityServiceInfo.getCapabilities());
            accessibilityAbilityInfo.setName(accessibilityServiceInfo.getSettingsActivityName());
            Context context = this.mAndroidContext;
            if (!(context == null || context.getPackageManager() == null)) {
                accessibilityAbilityInfo.setDescription(accessibilityServiceInfo.loadDescription(this.mAndroidContext.getPackageManager()));
            }
            ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
            AbilityInfo abilityInfo = new AbilityInfo();
            if (!(resolveInfo == null || resolveInfo.serviceInfo == null)) {
                accessibilityAbilityInfo.setPackageName(resolveInfo.serviceInfo.packageName);
            }
            accessibilityAbilityInfo.setAbilityInfo(abilityInfo);
            arrayList.add(accessibilityAbilityInfo);
        }
        return arrayList;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean isAccessibilityCaptionEnabled() {
        Context context = this.mAndroidContext;
        if (context == null) {
            LogUtil.error(TAG, "get accessibility caption enabled failed, context is null.");
            return false;
        }
        CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(CaptioningManager.class);
        if (captioningManager != null) {
            return captioningManager.isEnabled();
        }
        LogUtil.error(TAG, "get accessibility caption enabled failed, can not find captioningManager.");
        return false;
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public CaptionProperties getCaptionProperties() {
        CaptionProperties captionProperties = new CaptionProperties();
        Context context = this.mAndroidContext;
        if (context == null) {
            LogUtil.error(TAG, "get caption properties failed, context is null.");
            return captionProperties;
        }
        CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(CaptioningManager.class);
        if (captioningManager == null) {
            LogUtil.error(TAG, "get caption properties failed, can not find captioningManager.");
            return captionProperties;
        }
        captionProperties.setIsEnabled(captioningManager.isEnabled());
        captionProperties.setLocale(captioningManager.getLocale());
        captionProperties.setFontSizeType(captioningManager.getFontScale());
        setCaptionStyleToProperties(captioningManager.getUserStyle(), captionProperties);
        return captionProperties;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCaptionStyleToProperties(CaptioningManager.CaptionStyle captionStyle, CaptionProperties captionProperties) {
        if (captionStyle == null) {
            LogUtil.error(TAG, "CaptionProperties captionStyle is null.");
            return;
        }
        if (captionStyle.hasEdgeType()) {
            LogUtil.info(TAG, "CaptionProperties hasEdgeTyp");
            captionProperties.setEdgeType(captionStyle.edgeType);
        }
        if (captionStyle.hasEdgeColor()) {
            LogUtil.info(TAG, "CaptionProperties hasEdgeColor");
            captionProperties.setEdgeColor(captionStyle.edgeColor);
        }
        if (captionStyle.hasBackgroundColor()) {
            LogUtil.info(TAG, "CaptionProperties hasBackgroundColor");
            captionProperties.setBackgroundColor(captionStyle.backgroundColor);
        }
        if (captionStyle.hasForegroundColor()) {
            LogUtil.info(TAG, "CaptionProperties hasForegroundColor");
            captionProperties.setForegroundColor(captionStyle.foregroundColor);
        }
        if (captionStyle.hasWindowColor()) {
            LogUtil.info(TAG, "CaptionProperties hasWindowColor");
            captionProperties.setWindowColor(captionStyle.windowColor);
        }
        captionProperties.setFontFamilyName(captionStyle.mRawTypeface);
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean addCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver) {
        if (captionPropertiesObserver == null) {
            LogUtil.error(TAG, "add caption properties observer failed, observer is null.");
            return false;
        } else if (this.mAndroidContext == null) {
            LogUtil.error(TAG, "add caption properties observer failed, context is null.");
            return false;
        } else if (sCaptionPropertiesListeners.containsKey(captionPropertiesObserver)) {
            LogUtil.info(TAG, "observer has already in the list.");
            return true;
        } else {
            CaptioningManager captioningManager = (CaptioningManager) this.mAndroidContext.getSystemService(CaptioningManager.class);
            if (captioningManager == null) {
                LogUtil.error(TAG, "add caption properties observer failed, can not find captioningManager.");
                return false;
            }
            CaptioningManager.CaptioningChangeListener createCaptioningListener = createCaptioningListener(captionPropertiesObserver);
            captioningManager.addCaptioningChangeListener(createCaptioningListener);
            sCaptionPropertiesListeners.put(captionPropertiesObserver, createCaptioningListener);
            return true;
        }
    }

    private CaptioningManager.CaptioningChangeListener createCaptioningListener(final CaptionPropertiesObserver captionPropertiesObserver) {
        return new CaptioningManager.CaptioningChangeListener() {
            /* class ohos.accessibility.adapter.AccessibilitySystemAbilityAdapter.AnonymousClass1 */

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onEnabledChanged(boolean z) {
                LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption enabled changed, isEnable:" + z);
                if (captionPropertiesObserver == null) {
                    LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption enabled changed end, observer is null.");
                    return;
                }
                CaptionProperties captionProperties = new CaptionProperties();
                captionProperties.setIsEnabled(z);
                captionPropertiesObserver.onStateChanged(1, captionProperties);
            }

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onUserStyleChanged(CaptioningManager.CaptionStyle captionStyle) {
                LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption style changed.");
                if (captionPropertiesObserver == null) {
                    LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption style changed end, observer is null.");
                    return;
                }
                CaptionProperties captionProperties = new CaptionProperties();
                AccessibilitySystemAbilityAdapter.this.setCaptionStyleToProperties(captionStyle, captionProperties);
                captionPropertiesObserver.onStateChanged(2, captionProperties);
            }

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onLocaleChanged(Locale locale) {
                LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption locale changed.");
                if (captionPropertiesObserver == null) {
                    LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption locale changed end, observer is null.");
                    return;
                }
                CaptionProperties captionProperties = new CaptionProperties();
                captionProperties.setLocale(locale);
                captionPropertiesObserver.onStateChanged(3, captionProperties);
            }

            @Override // android.view.accessibility.CaptioningManager.CaptioningChangeListener
            public void onFontScaleChanged(float f) {
                LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption font type size changed:" + f);
                if (captionPropertiesObserver == null) {
                    LogUtil.info(AccessibilitySystemAbilityAdapter.TAG, "caption font type changed end, observer is null.");
                    return;
                }
                CaptionProperties captionProperties = new CaptionProperties();
                captionProperties.setFontSizeType(f);
                captionPropertiesObserver.onStateChanged(4, captionProperties);
            }
        };
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public boolean removeCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver) {
        if (captionPropertiesObserver == null) {
            LogUtil.error(TAG, "observer is null.");
            return false;
        } else if (this.mAndroidContext == null) {
            LogUtil.error(TAG, "remove caption properties observer failed, context is null.");
            return false;
        } else {
            Map<CaptionPropertiesObserver, CaptioningManager.CaptioningChangeListener> map = sCaptionPropertiesListeners;
            if (map == null || !map.containsKey(captionPropertiesObserver)) {
                LogUtil.info(TAG, "observer is not exists in the map.");
                return true;
            }
            CaptioningManager captioningManager = (CaptioningManager) this.mAndroidContext.getSystemService(CaptioningManager.class);
            if (captioningManager == null) {
                LogUtil.error(TAG, "remove caption properties observer failed,, can not find captioningManager.");
                return false;
            }
            CaptioningManager.CaptioningChangeListener captioningChangeListener = sCaptionPropertiesListeners.get(captionPropertiesObserver);
            if (captioningChangeListener != null) {
                captioningManager.removeCaptioningChangeListener(captioningChangeListener);
            }
            sCaptionPropertiesListeners.remove(captionPropertiesObserver);
            return true;
        }
    }

    @Override // ohos.accessibility.adapter.AdapterInterface
    public int getSuggestedInterval(int i, int i2) {
        AccessibilityManager accessibilityManager = this.mAccessibilityManager;
        if (accessibilityManager != null) {
            return accessibilityManager.getRecommendedTimeoutMillis(i, i2);
        }
        LogUtil.error(TAG, "accessibilityManager is null");
        return 0;
    }
}
