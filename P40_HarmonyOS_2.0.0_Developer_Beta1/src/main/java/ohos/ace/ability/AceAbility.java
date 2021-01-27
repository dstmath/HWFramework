package ohos.ace.ability;

import com.huawei.ace.adapter.AceContextAdapter;
import com.huawei.ace.plugin.editing.TextInputPluginBase;
import com.huawei.ace.plugin.internal.PluginJNI;
import com.huawei.ace.runtime.AEventReport;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceApplicationInfo;
import com.huawei.ace.runtime.AceContainer;
import com.huawei.ace.runtime.AceEnv;
import com.huawei.ace.runtime.AceEventCallback;
import com.huawei.ace.runtime.AcePage;
import com.huawei.ace.runtime.AcePluginCallback;
import com.huawei.ace.runtime.HarmonySystemPluginLoader;
import com.huawei.ace.runtime.IAceLocaleFallback;
import com.huawei.ace.runtime.IAceView;
import com.huawei.ace.runtime.LibraryLoader;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.IAbilityContinuation;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.accessibility.BarrierFreeInnerClient;
import ohos.ace.featureabilityplugin.requestprocess.StartAbilityProcess;
import ohos.ace.plugin.clipboard.ClipboardPlugin;
import ohos.ace.plugin.editing.TextInputPlugin;
import ohos.ace.plugin.editing.TextViewListener;
import ohos.ace.plugin.vibrator.VibratorPlugin;
import ohos.ace.plugin.video.AceVideoPlugin;
import ohos.ace.runtime.AceBundleInfo;
import ohos.ace.runtime.DeviceInfoHelper;
import ohos.agp.colors.RgbColor;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.agp.window.wmc.DisplayManagerWrapper;
import ohos.agp.window.wmc.IAGPEngineAdapter;
import ohos.agp.window.wmc.TextViewListenerWrapper;
import ohos.app.Context;
import ohos.app.ProcessInfo;
import ohos.bundle.ModuleInfo;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.i18n.utils.LocalesFallback;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.resource.LocaleFallBackException;
import ohos.global.resource.ResourceManager;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.tools.Bytrace;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.serializer.SerializerFeature;

public class AceAbility extends Ability implements IAbilityContinuation {
    private static final String ASSET_PATH_SHARE = "share";
    private static final String CONTINUE_PARAMS_KEY = "__remoteData";
    private static final String CUSTOM_ASSET_PATH = "assets/js/";
    private static final int FLAG_NOT_TOUCH_MODAL = 32;
    private static final int FLAG_TRANSPARENT_NAVIGATION = 134217728;
    private static final int FLAG_TRANSPARENT_STATUS = 67108864;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static final String INSTANCE_DEFAULT_NAME = "default";
    private static final String PAGE_URI = "url";
    private static final String START_PARAMS_KEY = "__startParams";
    private static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 8192;
    private static final String TAG = "AceAbility";
    private static final int THEME_LIGHT = 0;
    private static final int THEME_TRANSPARENT = 1;
    private static final String WINDOW_HEIGHT = "window_height";
    private static final int WINDOW_MODAL_DIALOG = 3;
    private static final String WINDOW_MODAL_KEY = "window_modal";
    private static final int WINDOW_MODAL_NORMAL = 0;
    private static final int WINDOW_MODAL_SEMI = 1;
    private static final int WINDOW_MODAL_SEMI_FULL_SCREEN = 2;
    private static final String WINDOW_WIDTH = "window_width";
    private static final String WINDOW_X_POSITION = "window_x_position";
    private static final String WINDOW_Y_POSITION = "window_y_position";
    private int AGP_ENGINE_MODE_ACE = 3;
    private int abilityId = 0;
    private AceBundleInfo bundleInfo;
    private AceEventCallback callbackHandler = null;
    private ClipboardPlugin clipboardPlugin;
    private AceContainer container = null;
    private Context context = null;
    private AceContextAdapter contextAdapter;
    private float density = 1.0f;
    private float fontScale = 1.0f;
    private int heightPixels = 0;
    private String instanceName;
    private IntentParams intentParams = null;
    private AcePage mainPage = null;
    private IntentParams pageParams = null;
    private String pageUrl;
    private String parsedPageParams;
    private String parsedPageUrl;
    private AcePluginCallback pluginCallbackHandler = null;
    private PluginJNI pluginJni = null;
    private String remoteData = null;
    private String remotePageUrl;
    private TextInputPlugin textInputPlugin;
    private int themeType = 0;
    private VibratorPlugin vibratorPlugin;
    private int viewType = 1;
    private int widthPixels = 0;
    private int windowFlags = 0;
    private int windowHeight = 0;
    private int windowModal = 0;
    private int windowWidth = 0;
    private int xPosition = 0;
    private int yPosition = 0;

    /* access modifiers changed from: protected */
    public String onCallbackWithReturn(int i, String str, String str2) {
        return null;
    }

    /* access modifiers changed from: private */
    public static class AcePluginCallbackHandler implements AcePluginCallback {
        private AcePluginCallbackHandler() {
        }

        @Override // com.huawei.ace.runtime.AcePluginCallback
        public void onLoadPlugin(String str, int i) {
            HarmonySystemPluginLoader.checkAndLoadSystemPlugin(str, i);
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onStart(Intent intent) {
        Bytrace.startTrace(549755813888L, "Ability.onStart");
        super.onStart(intent);
        Bytrace.finishTrace(549755813888L, "Ability.onStart");
        ALog.setLogger(new Logger());
        this.abilityId = ID_GENERATOR.incrementAndGet();
        this.context = getContext();
        if (this.context == null) {
            ALog.e(TAG, "the context is null");
            return;
        }
        AEventReport.setEventReport(new EventReport());
        boolean z = this.context.getApplicationInfo().debug;
        boolean booleanParam = intent.getBooleanParam("debugApp", false);
        if (z && booleanParam) {
            LibraryLoader.setLoadDebugSo();
        }
        DeviceInfoHelper.init(this.context);
        if (DeviceInfoHelper.isWatchType()) {
            LibraryLoader.setUseWatchLib();
        } else if (!DeviceInfoHelper.isTvType()) {
            LibraryLoader.setUseV8Lib();
        }
        AceEnv.setViewCreator(new AceViewCreator(this, this.viewType));
        AceEnv.getInstance().setupFirstFrameHandler(1);
        AceEnv.getInstance().setupNatives(1, this.viewType);
        HarmonySystemPluginLoader.registerPlugin(this, this.abilityId);
        AceApplicationInfo instance = AceApplicationInfo.getInstance();
        this.bundleInfo = new AceBundleInfo(this);
        instance.setPackageInfo(getBundleName(), z, booleanParam);
        instance.setLocaleFallback(new IAceLocaleFallback() {
            /* class ohos.ace.ability.$$Lambda$AceAbility$WqlRcbbPjmioS3ID3HqZhDltvAs */

            @Override // com.huawei.ace.runtime.IAceLocaleFallback
            public final String onLocaleFallback(String str, String[] strArr) {
                return AceAbility.this.lambda$onStart$0$AceAbility(str, strArr);
            }
        });
        ProcessInfo processInfo = getProcessInfo();
        if (processInfo != null) {
            instance.setPid(processInfo.getPid());
        }
        this.pluginJni = new PluginJNI();
        this.intentParams = intent.getParams();
        instance.setLocale();
        setWindowInfo(intent);
        Bytrace.startTrace(549755813888L, "createContainer");
        createContainer();
        Bytrace.finishTrace(549755813888L, "createContainer");
    }

    private void setWindowInfo(Intent intent) {
        this.xPosition = intent.getIntParam(WINDOW_X_POSITION, 0);
        this.yPosition = intent.getIntParam(WINDOW_Y_POSITION, 0);
        this.windowWidth = intent.getIntParam(WINDOW_WIDTH, 0);
        this.windowHeight = intent.getIntParam(WINDOW_HEIGHT, 0);
        this.windowModal = intent.getIntParam(WINDOW_MODAL_KEY, 0);
    }

    private void createContainer() {
        int i;
        this.callbackHandler = new AceEventCallback() {
            /* class ohos.ace.ability.AceAbility.AnonymousClass1 */

            @Override // com.huawei.ace.runtime.AceEventCallback
            public String onEvent(int i, String str, String str2) {
                return AceAbility.this.onCallbackWithReturn(i, str, str2);
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onFinish() {
                AceAbility.this.terminateAbility();
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onStatusBarBgColorChanged(int i) {
                AceAbility.this.statusBarBgColorChanged(i);
            }
        };
        this.container = AceEnv.createContainer(this.callbackHandler, this.pluginJni, this.abilityId, getInstanceName());
        if (this.container == null) {
            ALog.e(TAG, "createContainer failed");
            return;
        }
        this.pluginCallbackHandler = new AcePluginCallbackHandler();
        this.pluginJni.setPluginLoadHandler(this.pluginCallbackHandler);
        initAssetPath(this.context);
        initDeviceInfo(this.context);
        if (DeviceInfoHelper.isPhoneType() && ((i = this.windowModal) == 1 || i == 2 || i == 3)) {
            this.themeType = 1;
            this.windowFlags |= 67108864;
            this.windowFlags |= 134217728;
            this.container.setWindowStyle(this.windowModal, this.themeType);
        }
        if (!AceEnv.isJSONContainerType()) {
            this.mainPage = this.container.createPage();
            parsePageParams();
            HarmonySystemPluginLoader.loadPluginJsCode(this.container);
            this.container.setPageContent(this.mainPage, this.parsedPageUrl, this.parsedPageParams);
            clearPageParams();
        }
        setAceView(this.container);
        if (!DeviceInfoHelper.isTvType()) {
            CompletableFuture.runAsync(new Runnable() {
                /* class ohos.ace.ability.$$Lambda$AceAbility$sUoUH867PEdKCTsEjU_XDzhPLDs */

                @Override // java.lang.Runnable
                public final void run() {
                    AceAbility.this.lambda$createContainer$1$AceAbility();
                }
            });
        }
        this.container.onRestoreData(this.remoteData);
    }

    public /* synthetic */ void lambda$createContainer$1$AceAbility() {
        BarrierFreeInnerClient.registerBarrierFreeAbility(this.context, 1);
    }

    private void parsePageParams() {
        String str = this.remotePageUrl;
        if (str == null || str.isEmpty()) {
            String str2 = this.pageUrl;
            if (str2 == null || str2.isEmpty()) {
                IntentParams intentParams2 = this.intentParams;
                if (intentParams2 == null || !intentParams2.hasParam(PAGE_URI)) {
                    this.parsedPageUrl = "";
                } else {
                    this.parsedPageUrl = this.intentParams.getParam(PAGE_URI).toString();
                }
            } else {
                this.parsedPageUrl = this.pageUrl;
            }
        } else {
            this.parsedPageUrl = this.remotePageUrl;
        }
        HashMap hashMap = new HashMap();
        loadIntentParams(this.intentParams, hashMap);
        loadPageParams(this.pageParams, hashMap);
        this.parsedPageParams = "";
        if (hashMap.size() != 0) {
            this.parsedPageParams = JSONObject.toJSONString(hashMap, new SerializerFeature[]{SerializerFeature.WriteMapNullValue});
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: onLocaleFallback */
    public String lambda$onStart$0$AceAbility(String str, String[] strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        for (String str2 : strArr) {
            arrayList.add(str2);
        }
        StringBuilder sb = new StringBuilder();
        try {
            ArrayList<String> findValidAndSort = LocalesFallback.getInstance().findValidAndSort(str, arrayList);
            if (findValidAndSort.size() > 0) {
                for (int i = 0; i < findValidAndSort.size(); i++) {
                    sb.append(findValidAndSort.get(i));
                    sb.append(",");
                }
            }
        } catch (LocaleFallBackException unused) {
            ALog.e(TAG, "findValidAndSort failed");
        }
        sb.append("en-US");
        return sb.toString();
    }

    private void clearPageParams() {
        this.intentParams = null;
        this.pageParams = null;
        this.pageUrl = "";
    }

    private void loadIntentParams(IntentParams intentParams2, Map<String, Object> map) {
        Object param;
        if (intentParams2 != null && intentParams2.hasParam(START_PARAMS_KEY) && (param = intentParams2.getParam(START_PARAMS_KEY)) != null && (param instanceof String)) {
            try {
                JSONObject parseObject = JSONObject.parseObject((String) param);
                if (parseObject != null) {
                    map.putAll(parseObject.getInnerMap());
                }
            } catch (JSONException unused) {
                ALog.e(TAG, "parse intent data json failed");
            }
        }
    }

    private void loadPageParams(IntentParams intentParams2, Map<String, Object> map) {
        Map<String, Object> params;
        if (intentParams2 != null && (params = intentParams2.getParams()) != null && params.size() != 0) {
            map.putAll(params);
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onConfigurationUpdated(Configuration configuration) {
        ALog.d(TAG, "AceAbility is onConfigurationUpdated");
        super.onConfigurationUpdated(configuration);
        Locale firstLocale = configuration.getFirstLocale();
        if (firstLocale != null) {
            StringJoiner stringJoiner = new StringJoiner(LanguageTag.SEP);
            stringJoiner.add(firstLocale.getLanguage());
            stringJoiner.add(firstLocale.getScript());
            stringJoiner.add(firstLocale.getCountry());
            Set<String> unicodeLocaleKeys = firstLocale.getUnicodeLocaleKeys();
            StringBuffer stringBuffer = new StringBuffer();
            if (!unicodeLocaleKeys.isEmpty()) {
                for (String str : unicodeLocaleKeys) {
                    stringBuffer.append(str);
                    stringBuffer.append("=");
                    stringBuffer.append(Locale.getDefault().getUnicodeLocaleType(str));
                    stringBuffer.append(DMSDPConfig.LIST_TO_STRING_SPLIT);
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            }
            stringJoiner.add(stringBuffer.toString());
            AceApplicationInfo instance = AceApplicationInfo.getInstance();
            if (!stringJoiner.toString().equals(instance.getLanguageTag())) {
                AceEnv.destroyContainer(this.container);
                instance.setLocale();
                createContainer();
            }
        }
        if (((double) Math.abs(configuration.fontRatio - this.fontScale)) <= 0.001d) {
            return;
        }
        if (this.container == null) {
            ALog.e(TAG, "the container is null");
            return;
        }
        this.fontScale = configuration.fontRatio;
        this.container.setFontScale(this.fontScale);
    }

    @Override // ohos.aafwk.ability.Ability
    public void onStop() {
        super.onStop();
        AceEnv.destroyContainer(this.container);
        HarmonySystemPluginLoader.deregisterPlugin(this.abilityId);
        BarrierFreeInnerClient.unRegisterBarrierFreeAbility(this.context);
    }

    @Override // ohos.aafwk.ability.Ability
    public void onNewIntent(Intent intent) {
        Object obj;
        ALog.d(TAG, "AceAbility is onNewIntent");
        super.onNewIntent(intent);
        if (this.container == null) {
            ALog.e(TAG, "the container is null");
            return;
        }
        IntentParams params = intent.getParams();
        String str = null;
        if (params == null) {
            obj = null;
        } else {
            obj = params.getParam(START_PARAMS_KEY);
        }
        if (obj != null) {
            if (obj instanceof String) {
                str = (String) obj;
            } else {
                str = JSON.toJSONString(obj);
            }
        }
        this.container.onNewRequest(str);
    }

    @Override // ohos.aafwk.ability.Ability
    public void onBackPressed() {
        ALog.d(TAG, "AceAbility is onBackPressed");
        AceContainer aceContainer = this.container;
        if (aceContainer == null || aceContainer.onBackPressed()) {
            ALog.d(TAG, "AceAbility's frontend process back press");
            return;
        }
        ALog.d(TAG, "Signal onBackPressed passed to Ability process");
        super.onBackPressed();
    }

    @Override // ohos.aafwk.ability.Ability
    public void onForeground(Intent intent) {
        ALog.d(TAG, "AceAbility is onForeground");
        super.onForeground(intent);
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.getView(this.density, this.widthPixels, this.heightPixels).onResume();
            this.container.onShow();
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onBackground() {
        ALog.d(TAG, "AceAbility is onBackground");
        super.onBackground();
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.getView(this.density, this.widthPixels, this.heightPixels).onPause();
            this.container.onHide();
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onActive() {
        ALog.d(TAG, "AceAbility is onActive");
        super.onActive();
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.onActive();
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onInactive() {
        super.onInactive();
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.onInactive();
        }
    }

    @Override // ohos.aafwk.ability.IAbilityContinuation
    public boolean onStartContinuation() {
        ALog.d(TAG, "ask AceAbility is can distribute");
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            return aceContainer.onStartContinuation();
        }
        ALog.e(TAG, "the container is null");
        return false;
    }

    @Override // ohos.aafwk.ability.IAbilityContinuation
    public void onCompleteContinuation(int i) {
        ALog.d(TAG, "AceAbility distribute result = " + i);
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.e(TAG, "the container is null");
        } else {
            aceContainer.onCompleteContinuation(i);
        }
    }

    @Override // ohos.aafwk.ability.IAbilityContinuation
    public boolean onSaveData(IntentParams intentParams2) {
        ALog.d(TAG, "start to save running data");
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.e(TAG, "the container is null");
            return false;
        }
        String onSaveData = aceContainer.onSaveData();
        if ("".equals(onSaveData)) {
            return true;
        }
        if (onSaveData != null && !"false".equals(onSaveData)) {
            try {
                parseSaveData(intentParams2, onSaveData);
                return true;
            } catch (JSONException unused) {
                ALog.e(TAG, "parse data json failed");
            }
        }
        return false;
    }

    @Override // ohos.aafwk.ability.Ability
    public boolean onTouchEvent(TouchEvent touchEvent) {
        if (this.viewType != 1) {
            return false;
        }
        IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
        if (view instanceof AceNativeView) {
            return ((AceNativeView) view).processTouchEvent(touchEvent);
        }
        return false;
    }

    @Override // ohos.aafwk.ability.Ability
    public boolean dispatchRotationEvent(RotationEvent rotationEvent) {
        if (this.viewType == 1) {
            IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
            if ((view instanceof AceNativeView) && ((AceNativeView) view).processRotationEventInner(rotationEvent)) {
                return true;
            }
        }
        return super.dispatchRotationEvent(rotationEvent);
    }

    @Override // ohos.aafwk.ability.Ability
    public boolean dispatchMouseEvent(MouseEvent mouseEvent) {
        if (this.viewType == 1) {
            IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
            if ((view instanceof AceNativeView) && ((AceNativeView) view).processMouseEventInner(mouseEvent)) {
                return true;
            }
        }
        return super.dispatchMouseEvent(mouseEvent);
    }

    @Override // ohos.aafwk.ability.Ability
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.viewType == 1) {
            IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
            if ((view instanceof AceNativeView) && ((AceNativeView) view).processKeyEvent(keyEvent)) {
                return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // ohos.aafwk.ability.Ability
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (this.viewType == 1) {
            IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
            if ((view instanceof AceNativeView) && ((AceNativeView) view).processKeyEvent(keyEvent)) {
                return true;
            }
        }
        return super.onKeyUp(i, keyEvent);
    }

    private void parseSaveData(IntentParams intentParams2, String str) throws JSONException {
        JSONObject parseObject = JSONObject.parseObject(str);
        if (parseObject != null) {
            if (parseObject.containsKey(PAGE_URI)) {
                Object obj = parseObject.get(PAGE_URI);
                if (obj instanceof String) {
                    intentParams2.setParam(PAGE_URI, (String) obj);
                }
            }
            if (parseObject.containsKey(CONTINUE_PARAMS_KEY)) {
                intentParams2.setParam(CONTINUE_PARAMS_KEY, JSON.toJSONString(parseObject.get(CONTINUE_PARAMS_KEY)));
            }
        }
    }

    @Override // ohos.aafwk.ability.IAbilityContinuation
    public boolean onRestoreData(IntentParams intentParams2) {
        ALog.d(TAG, "start to restore running data");
        if (intentParams2 == null) {
            return true;
        }
        if (intentParams2.hasParam(PAGE_URI)) {
            this.remotePageUrl = intentParams2.getParam(PAGE_URI).toString();
        }
        if (intentParams2.hasParam(CONTINUE_PARAMS_KEY)) {
            Object param = intentParams2.getParam(CONTINUE_PARAMS_KEY);
            if (param instanceof String) {
                this.remoteData = (String) param;
            } else {
                this.remoteData = JSON.toJSONString(param);
            }
        }
        return true;
    }

    private void setAceView(AceContainer aceContainer) {
        IAceView view = aceContainer.getView(this.density, this.widthPixels, this.heightPixels);
        Optional<AGPWindow> topWindow = AGPWindowManager.getInstance().getTopWindow();
        Boolean valueOf = Boolean.valueOf(DeviceInfoHelper.isWatchType());
        if (!(view instanceof IAGPEngineAdapter) || !topWindow.isPresent()) {
            if (view instanceof AceNativeView) {
                AceNativeView aceNativeView = (AceNativeView) view;
                aceNativeView.addToContent();
                if (!valueOf.booleanValue()) {
                    aceNativeView.initTextInputPlugins();
                }
            }
            ALog.e(TAG, "AGPWindow instance not exist!!");
        } else {
            AGPWindow aGPWindow = topWindow.get();
            aceContainer.setMultimodalObject(aGPWindow.setEngine(this.AGP_ENGINE_MODE_ACE, (IAGPEngineAdapter) view));
            if (!valueOf.booleanValue()) {
                this.clipboardPlugin = new ClipboardPlugin(this);
                TextViewListener textViewListener = new TextViewListener(new TextInputPluginBase.Delegate());
                this.textInputPlugin = new TextInputPlugin(textViewListener);
                aGPWindow.setTextViewListener(new TextViewListenerWrapper(textViewListener));
            }
            this.vibratorPlugin = new VibratorPlugin();
            if (this.themeType == 1) {
                aGPWindow.setTransparent(true);
            }
            AGPWindow.LayoutParams attributes = aGPWindow.getAttributes();
            if (attributes != null) {
                attributes.flags |= 32;
                attributes.flags |= this.windowFlags;
                if (!(this.xPosition == 0 && this.yPosition == 0 && this.windowWidth == 0 && this.windowHeight == 0)) {
                    int i = this.xPosition;
                    if (i == 0) {
                        i = attributes.x;
                    }
                    attributes.x = i;
                    int i2 = this.yPosition;
                    if (i2 == 0) {
                        i2 = attributes.y;
                    }
                    attributes.y = i2;
                    int i3 = this.windowWidth;
                    if (i3 == 0) {
                        i3 = attributes.width;
                    }
                    attributes.width = i3;
                    int i4 = this.windowHeight;
                    if (i4 == 0) {
                        i4 = attributes.height;
                    }
                    attributes.height = i4;
                }
                aGPWindow.setAttributes(attributes);
            }
        }
        view.setWindowModal(this.windowModal);
        view.viewCreated();
        if (!valueOf.booleanValue()) {
            view.addResourcePlugin(AceVideoPlugin.createRegister(this.context, getInstanceName()));
        }
        aceContainer.setFontScale(this.fontScale);
    }

    public String getInstanceName() {
        String str = this.instanceName;
        return str == null ? "default" : str;
    }

    @Override // ohos.aafwk.ability.Ability
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (strArr.length == 1) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            return;
        }
        String[] strArr2 = (String[]) Arrays.copyOfRange(strArr, 1, strArr.length);
        if (Arrays.asList("-element", "-render", "-focus", "-layer", "-memory", "-frontend", "-multimodal", "-accessibility", "-rotation", "-drawcmd").contains(strArr2[0])) {
            AceEnv.dump(str, fileDescriptor, printWriter, strArr2);
            return;
        }
        if ("-h".equals(strArr2[0])) {
            AceEnv.dump(str, fileDescriptor, printWriter, strArr2);
        }
        super.dump(str, fileDescriptor, printWriter, strArr);
    }

    private String getModuleHapPath(Context context2) {
        if (context2 == null) {
            return "";
        }
        String moduleName = context2.getAbilityInfo().getModuleName();
        for (ModuleInfo moduleInfo : context2.getApplicationInfo().getModuleInfos()) {
            if (moduleInfo.getModuleName().equals(moduleName)) {
                return moduleInfo.getModuleSourceDir();
            }
        }
        return "";
    }

    private void initAssetPath(Context context2) {
        String moduleHapPath = getModuleHapPath(context2);
        if (moduleHapPath == null || moduleHapPath.isEmpty()) {
            ALog.e(TAG, "the package path is null");
            return;
        }
        this.container.addCustomAssetPath(moduleHapPath, new String[]{CUSTOM_ASSET_PATH + getInstanceName() + '/', CUSTOM_ASSET_PATH + ASSET_PATH_SHARE + '/'});
    }

    private void initDeviceInfo(Context context2) {
        Optional<DisplayManagerWrapper.DisplayWrapper> defaultDisplay = DisplayManagerWrapper.getInstance().getDefaultDisplay(context2);
        if (defaultDisplay.isPresent()) {
            DisplayManagerWrapper.DisplayMetricsWrapper displayRealMetricsWrapper = defaultDisplay.get().getDisplayRealMetricsWrapper();
            this.density = displayRealMetricsWrapper.density;
            this.widthPixels = displayRealMetricsWrapper.widthPixels;
            this.heightPixels = displayRealMetricsWrapper.heightPixels;
        }
        ResourceManager resourceManager = context2.getResourceManager();
        if (resourceManager == null) {
            ALog.e(TAG, "initDeviceInfo failed, the resource is null");
            return;
        }
        Configuration configuration = resourceManager.getConfiguration();
        DeviceCapability deviceCapability = resourceManager.getDeviceCapability();
        if (configuration == null || deviceCapability == null) {
            ALog.e(TAG, "initDeviceInfo failed, the configuration or deviceCapability is null");
            return;
        }
        boolean z = deviceCapability.isRound;
        int i = configuration.direction;
        this.fontScale = configuration.fontRatio;
        this.container.initDeviceInfo(this.widthPixels, this.heightPixels, i, this.density, z);
    }

    public void setPageContent(String str) {
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.setPageContent(this.mainPage, str, "");
        }
    }

    public void setInstanceName(String str) {
        if (str.length() != 0) {
            if (str.charAt(str.length() - 1) == '/') {
                this.instanceName = str.substring(0, str.length() - 1);
            } else {
                this.instanceName = str;
            }
        }
    }

    public void setPageParams(String str, IntentParams intentParams2) {
        this.pageUrl = str;
        this.pageParams = intentParams2;
    }

    public void pushPage(String str, IntentParams intentParams2) {
        Map<String, Object> params;
        if (this.container == null) {
            ALog.e(TAG, "the container is null");
        } else {
            this.container.setPushPage(str, (intentParams2 == null || (params = intentParams2.getParams()) == null || params.size() == 0) ? "" : JSONObject.toJSONString(params, new SerializerFeature[]{SerializerFeature.WriteMapNullValue}));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void statusBarBgColorChanged(int i) {
        Optional<AGPWindow> topWindow = AGPWindowManager.getInstance().getTopWindow();
        if (topWindow.isPresent()) {
            AGPWindow aGPWindow = topWindow.get();
            int statusBarVisibility = aGPWindow.getStatusBarVisibility();
            RgbColor rgbColor = new RgbColor(i);
            int i2 = AceApplicationInfo.toGray(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue()) > AceApplicationInfo.getGrayThreshHold() ? statusBarVisibility | 8192 : statusBarVisibility & -8193;
            if (i2 != statusBarVisibility) {
                aGPWindow.setStatusBarVisibility(i2);
            }
        }
    }

    @Override // ohos.aafwk.ability.Ability
    public void onRequestPermissionsFromUserResult(int i, String[] strArr, int[] iArr) {
        HarmonySystemPluginLoader.transmitResultToPlugin(i, iArr);
    }

    public int getAbilityId() {
        return this.abilityId;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.aafwk.ability.Ability
    public void onAbilityResult(int i, int i2, Intent intent) {
        StartAbilityProcess.getInstance().onAbilityResultCallback(i, i2, intent);
    }
}
