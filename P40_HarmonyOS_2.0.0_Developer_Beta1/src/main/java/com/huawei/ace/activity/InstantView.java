package com.huawei.ace.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.ace.plugin.internal.PluginJNI;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceApplicationInfo;
import com.huawei.ace.runtime.AceContainer;
import com.huawei.ace.runtime.AceEnv;
import com.huawei.ace.runtime.AceEventCallback;
import com.huawei.ace.runtime.AcePage;
import com.huawei.ace.runtime.ActionEventCallback;
import com.huawei.ace.runtime.IAceView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public final class InstantView extends ViewGroup {
    private static final String LOG_TAG = "InstantView";
    private static final ViewGroup.LayoutParams MATCH_PARENT = new ViewGroup.LayoutParams(-1, -1);
    private static final int MESSAGE_EVENT = 101;
    private static final int ROUTER_EVENT = 100;
    private static int globalInstanceId = 1;
    private ActionEventCallback actionCallbackHandler = new ActionEventCallback() {
        /* class com.huawei.ace.activity.InstantView.AnonymousClass1 */

        @Override // com.huawei.ace.runtime.ActionEventCallback
        public void onRouterEvent(String str) {
            ALog.d(InstantView.LOG_TAG, "fire router event, params.");
            if (InstantView.this.eventHandler != null) {
                Message obtain = Message.obtain();
                obtain.what = 100;
                obtain.obj = str;
                InstantView.this.eventHandler.sendMessage(obtain);
                return;
            }
            ALog.e(InstantView.LOG_TAG, "fail to fire router event due to handler is null");
        }

        @Override // com.huawei.ace.runtime.ActionEventCallback
        public void onMessageEvent(String str) {
            ALog.d(InstantView.LOG_TAG, "fire message event, params.");
            if (InstantView.this.eventHandler != null) {
                Message obtain = Message.obtain();
                obtain.what = 101;
                obtain.obj = str;
                InstantView.this.eventHandler.sendMessage(obtain);
                return;
            }
            ALog.e(InstantView.LOG_TAG, "fail to fire message event due to handler is null");
        }
    };
    private final String cardHapPath;
    private AceContainer container;
    private final Context context;
    private float density = 1.0f;
    private Handler eventHandler;
    private final int instanceId;
    private final String jsModuleName;
    private View nativeView;

    static {
        ALog.setLogger(new Logger());
        AceEnv.setContainerType(2);
    }

    public InstantView(Context context2, String str, String str2) {
        super(context2);
        int i = globalInstanceId;
        globalInstanceId = i + 1;
        this.instanceId = i;
        this.context = context2;
        this.cardHapPath = str;
        this.jsModuleName = str2;
        Resources resources = getResources();
        if (resources != null) {
            this.density = resources.getDisplayMetrics().density;
        } else {
            ALog.e(LOG_TAG, "fail to get the density");
        }
        setBackgroundColor(0);
    }

    public void setEventHandler(Handler handler) {
        this.eventHandler = handler;
    }

    public void render(String str) {
        if (this.container != null) {
            ALog.w(LOG_TAG, "Already rendering");
            return;
        }
        ALog.i(LOG_TAG, "Render Js Card");
        boolean z = true;
        AceEnv.setViewCreator(new AceViewCreator(this.context, 1));
        AceEnv.getInstance().setupFirstFrameHandler(0);
        AceEnv.getInstance().setupNatives(0, 3);
        if (this.context.getApplicationContext().getApplicationInfo() == null || (this.context.getApplicationContext().getApplicationInfo().flags & 2) == 0) {
            z = false;
        }
        AceApplicationInfo.getInstance().setPackageInfo(this.context.getPackageName(), z, false);
        AceApplicationInfo.getInstance().setPid(Process.myPid());
        AceApplicationInfo.getInstance().setLocale();
        createContainer(this.cardHapPath, str);
    }

    public void refresh() {
        if (this.container == null) {
            ALog.e(LOG_TAG, "refresh: Not rendering");
        }
    }

    public void destroy() {
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.e(LOG_TAG, "destroy: Not rendering");
        } else {
            aceContainer.destroyContainer();
        }
    }

    private void createContainer(String str, String str2) {
        this.container = AceEnv.createContainer(new AceEventCallback() {
            /* class com.huawei.ace.activity.InstantView.AnonymousClass2 */

            @Override // com.huawei.ace.runtime.AceEventCallback
            public String onEvent(int i, String str, String str2) {
                return "";
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onFinish() {
            }

            @Override // com.huawei.ace.runtime.AceEventCallback
            public void onStatusBarBgColorChanged(int i) {
            }
        }, new PluginJNI(), this.instanceId);
        AceContainer aceContainer = this.container;
        if (aceContainer != null) {
            aceContainer.setActionEventCallback(this.actionCallbackHandler);
            this.container.setMultimodalObject(0);
            this.container.addCustomAssetPath(str, new String[]{"assets/js/" + this.jsModuleName + PsuedoNames.PSEUDONAME_ROOT, "assets/js/share/"});
            AceContainer aceContainer2 = this.container;
            AcePage createPage = aceContainer2.createPage();
            if (str2 == null) {
                str2 = "";
            }
            aceContainer2.setPageContent(createPage, "", str2);
            IAceView view = this.container.getView(this.density, getWidth(), getHeight());
            if (view instanceof View) {
                this.nativeView = (View) view;
                addView(this.nativeView, MATCH_PARENT);
            }
            view.viewCreated();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (z) {
            this.nativeView.layout(0, 0, i3 - i, i4 - i2);
        }
    }

    public static void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        AceEnv.dump(str, fileDescriptor, printWriter, strArr);
    }

    public void updateInstantData(String str) {
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.e(LOG_TAG, "container is null");
        } else {
            aceContainer.updateInstantData(str);
        }
    }
}
