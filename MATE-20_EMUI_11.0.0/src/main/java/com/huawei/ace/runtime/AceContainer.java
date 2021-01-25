package com.huawei.ace.runtime;

import java.util.concurrent.atomic.AtomicInteger;

public final class AceContainer {
    public static final int CONTAINER_TYPE_JS = 1;
    public static final int CONTAINER_TYPE_JSON = 0;
    public static final int CONTAINER_TYPE_JS_CARD = 2;
    public static final int DEVICE_TYPE_DEFAULT = 0;
    public static final int DEVICE_TYPE_WEARABLE = 1;
    private static final String PAGE_URI = "url";
    private static final String START_INTENT_DATA_KEY = "remoteData";
    private static final String TAG = "AceContainer";
    private AceEventCallback callback;
    private float fontScale = 1.0f;
    private final int instanceId;
    private AtomicInteger pageIdGenerator = new AtomicInteger(1);
    private int type = 0;
    private IAceView view;
    private final IAceViewCreator viewCreator;

    private native void nativeAddAssetPath(int i, Object obj, String str);

    private native void nativeAddCustomAssetPath(int i, String str, String[] strArr);

    private native void nativeCreateContainer(int i, int i2, AceEventCallback aceEventCallback, AcePluginMessage acePluginMessage);

    private native void nativeCreatePage(int i, int i2);

    private native void nativeDestroyContainer(int i);

    private native int nativeGetDeviceType();

    private native void nativeInitDeviceInfo(int i, int i2, int i3, float f, boolean z);

    private native void nativeLoadPluginJsCode(int i, String str);

    private native void nativeOnActive(int i);

    private native boolean nativeOnBackPressed(int i);

    private native void nativeOnCompleteContinuation(int i, int i2);

    private native void nativeOnHide(int i);

    private native void nativeOnInactive(int i);

    private native void nativeOnNewRequest(int i);

    private native boolean nativeOnRestoreData(int i, String str);

    private native String nativeOnSaveData(int i);

    private native void nativeOnShow(int i);

    private native boolean nativeOnStartContinuation(int i);

    private native boolean nativePush(int i, String str, String str2);

    private native boolean nativeRun(int i, int i2, String str, String str2);

    private native void nativeSetFontScale(int i, float f);

    private native void nativeSetMultimodal(int i, long j);

    private native void nativeSetView(int i, long j, float f, int i2, int i3);

    private native void nativeSetWindowStyle(int i, int i2, int i3);

    private native boolean nativeUpdate(int i, int i2, String str);

    public AceContainer(int i, int i2, IAceViewCreator iAceViewCreator, AceEventCallback aceEventCallback, AcePluginMessage acePluginMessage) {
        this.instanceId = i;
        this.type = i2;
        this.viewCreator = iAceViewCreator;
        this.view = null;
        this.callback = aceEventCallback;
        nativeCreateContainer(i, i2, aceEventCallback, acePluginMessage);
    }

    public void destroyContainer() {
        nativeDestroyContainer(this.instanceId);
        this.view.releaseNativeView();
    }

    public IAceView getView(float f, int i, int i2) {
        if (this.view == null) {
            this.view = this.viewCreator.createView(this.instanceId, f);
            this.view.initDeviceType();
            nativeSetView(this.instanceId, this.view.getNativePtr(), f, i, i2);
        }
        return this.view;
    }

    public AcePage createPage() {
        int generatePageId = generatePageId();
        AcePage acePage = new AcePage(generatePageId, this.callback);
        nativeCreatePage(this.instanceId, generatePageId);
        return acePage;
    }

    public void setPageContent(AcePage acePage, String str, String str2) {
        nativeRun(this.instanceId, acePage.getId(), str, str2);
    }

    public void setPushPage(String str, String str2) {
        nativePush(this.instanceId, str, str2);
    }

    public void updatePageContent(AcePage acePage, String str) {
        nativeUpdate(this.instanceId, acePage.getId(), str);
    }

    public boolean onBackPressed() {
        return nativeOnBackPressed(this.instanceId);
    }

    public void onShow() {
        nativeOnShow(this.instanceId);
    }

    public void onHide() {
        nativeOnHide(this.instanceId);
    }

    public void initDeviceInfo(int i, int i2, int i3, float f, boolean z) {
        nativeInitDeviceInfo(i, i2, i3, f, z);
    }

    public void onActive() {
        nativeOnActive(this.instanceId);
    }

    public void onInactive() {
        nativeOnInactive(this.instanceId);
    }

    public boolean onStartContinuation() {
        return nativeOnStartContinuation(this.instanceId);
    }

    public void onCompleteContinuation(int i) {
        nativeOnCompleteContinuation(this.instanceId, i);
    }

    public String onSaveData() {
        return nativeOnSaveData(this.instanceId);
    }

    public boolean onRestoreData(String str) {
        if (str == null || "".equals(str)) {
            return true;
        }
        return nativeOnRestoreData(this.instanceId, str);
    }

    public void onNewRequest() {
        nativeOnNewRequest(this.instanceId);
    }

    public void addAssetPath(Object obj, String str) {
        nativeAddAssetPath(this.instanceId, obj, str);
    }

    public void addCustomAssetPath(String str, String[] strArr) {
        nativeAddCustomAssetPath(this.instanceId, str, strArr);
    }

    public void setMultimodalObject(long j) {
        nativeSetMultimodal(this.instanceId, j);
    }

    public void setFontScale(float f) {
        this.fontScale = f;
        if (this.view != null) {
            nativeSetFontScale(this.instanceId, f);
        }
    }

    private int generatePageId() {
        return this.pageIdGenerator.getAndIncrement();
    }

    public void loadPluginJsCode(String str) {
        nativeLoadPluginJsCode(this.instanceId, str);
    }

    public int getInstanceId() {
        return this.instanceId;
    }

    public int getDeviceType() {
        return nativeGetDeviceType();
    }

    public void setWindowStyle(int i, int i2) {
        nativeSetWindowStyle(this.instanceId, i, i2);
    }
}
