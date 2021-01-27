package com.huawei.ace.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import com.huawei.ace.plugin.internal.PluginJNI;
import com.huawei.ace.plugin.video.AceVideoPlugin;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceApplicationInfo;
import com.huawei.ace.runtime.AceContainer;
import com.huawei.ace.runtime.AceEnv;
import com.huawei.ace.runtime.AceEventCallback;
import com.huawei.ace.runtime.AcePage;
import com.huawei.ace.runtime.DeviceInfoHelper;
import com.huawei.ace.runtime.IAceView;
import com.huawei.ace.runtime.LibraryLoader;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class AceActivity extends Activity {
    private static final String ASSET_PATH = "js/";
    private static final String ASSET_PATH_SHARE = "share";
    private static final int COMPONENT_ID = 1;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static final String INSTANCE_DEFAULT_NAME = "default";
    private static final String LOG_TAG = "AceActivity";
    private int activityId = ID_GENERATOR.getAndIncrement();
    private AceContainer container = null;
    private float density = 1.0f;
    private int heightPixels = 0;
    private String instanceName;
    private AcePage mainPage = null;
    private PluginJNI pluginJni = null;
    private AceViewCreator viewCreator = null;
    private int widthPixels = 0;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        ALog.d(LOG_TAG, "AceActivity::onCreate called");
        super.onCreate(bundle);
        loadLibrary();
        boolean z = true;
        requestWindowFeature(1);
        this.viewCreator = new AceViewCreator(this, 0);
        AceEnv.setViewCreator(this.viewCreator);
        AceEnv.getInstance().setupFirstFrameHandler(0);
        AceEnv.getInstance().setupNatives(0, 0);
        if (getApplicationContext().getApplicationInfo() == null || (getApplicationContext().getApplicationInfo().flags & 2) == 0) {
            z = false;
        }
        AceApplicationInfo.getInstance().setPackageInfo(getPackageName(), z, false);
        AceApplicationInfo.getInstance().setPid(Process.myPid());
        AceApplicationInfo.getInstance().setLocale();
        createContainer();
    }

    private void createContainer() {
        AnonymousClass1 r0 = new AceEventCallback() {
            /* class com.huawei.ace.activity.AceActivity.AnonymousClass1 */

            @Override // com.huawei.ace.runtime.AceEventCallback
            public String onEvent(int i, String str, String str2) {
                return AceActivity.this.onCallbackWithReturn(i, str, str2);
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onFinish() {
                ALog.d(AceActivity.LOG_TAG, "finish current activity");
                AceActivity.this.finish();
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onStatusBarBgColorChanged(int i) {
                AceActivity.this.statusBarBgColorChanged(i);
            }
        };
        this.pluginJni = new PluginJNI();
        this.container = AceEnv.createContainer(r0, this.pluginJni, this.activityId, getInstanceName());
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.setMultimodalObject(0);
            initDeviceInfo();
            initAsset();
            this.mainPage = this.container.createPage();
            if (!AceEnv.isJSONContainerType()) {
                this.container.setPageContent(this.mainPage, "", "");
            }
            Boolean valueOf = Boolean.valueOf(DeviceInfoHelper.isWatchType(getApplicationContext()));
            if (valueOf.booleanValue()) {
                this.viewCreator.setIsWearable();
            }
            IAceView view = this.container.getView(this.density, this.widthPixels, this.heightPixels);
            if (view instanceof View) {
                setContentView((View) view);
            }
            view.viewCreated();
            if (!valueOf.booleanValue()) {
                view.addResourcePlugin(AceVideoPlugin.createRegister(this, getInstanceName()));
                if (view instanceof AceNativeView) {
                    ((AceNativeView) view).initTextInputPlugins();
                }
            }
        }
    }

    private void loadLibrary() {
        Context applicationContext = getApplicationContext();
        if (DeviceInfoHelper.isWatchType(applicationContext)) {
            LibraryLoader.setUseWatchLib();
        } else if (DeviceInfoHelper.isTvType(applicationContext)) {
            ALog.d(LOG_TAG, "use default type");
        } else {
            LibraryLoader.setUseV8Lib();
        }
    }

    private void initAsset() {
        AceContainer aceContainer = this.container;
        AssetManager assets = getAssets();
        aceContainer.addAssetPath(assets, ASSET_PATH + getInstanceName());
        this.container.addAssetPath(getAssets(), "js/share");
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStart() {
        ALog.d(LOG_TAG, "AceActivity::onStart called");
        super.onStart();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        ALog.d(LOG_TAG, "AceActivity::onResume called");
        super.onResume();
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.getView(this.density, this.widthPixels, this.heightPixels).onResume();
            this.container.onShow();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onRestart() {
        ALog.d(LOG_TAG, "AceActivity::onRestart called");
        super.onRestart();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        ALog.d(LOG_TAG, "AceActivity::onPause called");
        super.onPause();
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.getView(this.density, this.widthPixels, this.heightPixels).onPause();
            this.container.onHide();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStop() {
        ALog.d(LOG_TAG, "AceActivity::onStop called");
        super.onStop();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onDestroy() {
        ALog.d(LOG_TAG, "AceActivity::onDestroy called");
        super.onDestroy();
        AceEnv.destroyContainer(this.container);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        ALog.d(LOG_TAG, "AceActivity::onBackPressed");
        AceContainer aceContainer = this.container;
        if (aceContainer != null && !aceContainer.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
        if (i == 1) {
            ALog.i(LOG_TAG, "AceActivity:onConfigurationChanged ORIENTATION_PORTRAIT");
        } else if (i != 2) {
            ALog.i(LOG_TAG, "AceActivity:onConfigurationChanged unknown");
        } else {
            ALog.i(LOG_TAG, "AceActivity:onConfigurationChanged ORIENTATION_LANDSCAPE");
        }
    }

    @Override // android.app.Activity
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        AceEnv.dump(str, fileDescriptor, printWriter, strArr);
    }

    public void setPageContent(String str) {
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.setPageContent(this.mainPage, str, "");
        }
    }

    public void updatePageContent(String str) {
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.updatePageContent(this.mainPage, str);
        }
    }

    public void setInstanceName(String str) {
        if (str.charAt(str.length() - 1) == '/') {
            this.instanceName = str.substring(0, str.length() - 1);
        } else {
            this.instanceName = str;
        }
    }

    /* access modifiers changed from: protected */
    public String onCallbackWithReturn(int i, String str, String str2) {
        if (str != null && !str.isEmpty()) {
            ALog.d(LOG_TAG, "AceActivity::onCallbackWithReturn");
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void statusBarBgColorChanged(int i) {
        ALog.d(LOG_TAG, "set status bar. light: " + i);
        if (getWindow() == null) {
            ALog.w(LOG_TAG, "statusBarBgColorChanged failed due to Window is null");
            return;
        }
        View decorView = getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        decorView.setSystemUiVisibility(AceApplicationInfo.toGray(Color.red(i), Color.green(i), Color.blue(i)) > AceApplicationInfo.getGrayThreshHold() ? systemUiVisibility | 8192 : systemUiVisibility & -8193);
    }

    private String getInstanceName() {
        String str = this.instanceName;
        return str == null ? "default" : str;
    }

    private void initDeviceInfo() {
        Resources resources = getResources();
        if (resources != null) {
            boolean isScreenRound = resources.getConfiguration().isScreenRound();
            int i = resources.getConfiguration().orientation;
            this.widthPixels = resources.getDisplayMetrics().widthPixels;
            this.heightPixels = resources.getDisplayMetrics().heightPixels;
            this.density = resources.getDisplayMetrics().density;
            this.container.initDeviceInfo(this.widthPixels, this.heightPixels, i, this.density, isScreenRound);
        }
    }

    public int getActivityId() {
        return this.activityId;
    }
}
