package ohos.ace.ability;

import com.huawei.ace.plugin.internal.PluginJNI;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceApplicationInfo;
import com.huawei.ace.runtime.AceContainer;
import com.huawei.ace.runtime.AceEnv;
import com.huawei.ace.runtime.AceEventCallback;
import com.huawei.ace.runtime.AcePage;
import com.huawei.ace.runtime.ActionEventCallback;
import com.huawei.ace.runtime.IAceLocaleFallback;
import com.huawei.ace.runtime.IAceView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.window.wmc.DisplayManagerWrapper;
import ohos.app.Context;
import ohos.app.ProcessInfo;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.InnerEvent;
import ohos.global.i18n.utils.LocalesFallback;
import ohos.global.resource.LocaleFallBackException;

public class InstantComponent extends ComponentContainer {
    private static final String LOG_TAG = "InstantComponent";
    private static final int MESSAGE_EVENT = 101;
    private static final int ROUTER_EVENT = 100;
    private static int globalInstanceId = 1;
    private ActionEventCallback actionCallbackHandler;
    private final String cardHapPath;
    private AceContainer container;
    private final Context context;
    private float density = 1.0f;
    private EventHandler eventHandler;
    private final int instanceId;
    private final String jsModuleName;

    static {
        ALog.setLogger(new Logger());
        AceEnv.setContainerType(2);
    }

    public InstantComponent(Context context2, String str, String str2) {
        super(context2);
        int i = globalInstanceId;
        globalInstanceId = i + 1;
        this.instanceId = i;
        this.actionCallbackHandler = new ActionEventCallback() {
            /* class ohos.ace.ability.InstantComponent.AnonymousClass1 */

            @Override // com.huawei.ace.runtime.ActionEventCallback
            public void onRouterEvent(String str) {
                ALog.d(InstantComponent.LOG_TAG, "fire router event, start ability.");
                if (InstantComponent.this.eventHandler != null) {
                    InstantComponent.this.eventHandler.sendEvent(InnerEvent.get(100, 0, str));
                }
            }

            @Override // com.huawei.ace.runtime.ActionEventCallback
            public void onMessageEvent(String str) {
                ALog.d(InstantComponent.LOG_TAG, "fire message event.");
                if (InstantComponent.this.eventHandler != null) {
                    InstantComponent.this.eventHandler.sendEvent(InnerEvent.get(101, 0, str));
                }
            }
        };
        this.context = context2;
        this.cardHapPath = str;
        this.jsModuleName = str2;
        Optional<DisplayManagerWrapper.DisplayWrapper> defaultDisplay = DisplayManagerWrapper.getInstance().getDefaultDisplay(context2);
        if (defaultDisplay.isPresent()) {
            this.density = defaultDisplay.get().getDisplayRealMetricsWrapper().density;
        } else {
            ALog.e(LOG_TAG, "fail to find density info");
        }
    }

    public void setEventHandler(EventHandler eventHandler2) {
        this.eventHandler = eventHandler2;
    }

    public void render(String str) {
        if (this.container != null) {
            ALog.e(LOG_TAG, "Already rendering");
            return;
        }
        AceEnv.setViewCreator(new AceViewCreator(this.context, 2));
        AceEnv.getInstance().setupFirstFrameHandler(1);
        AceEnv.getInstance().setupNatives(1, 2);
        AceApplicationInfo instance = AceApplicationInfo.getInstance();
        instance.setPackageInfo(this.context.getBundleName(), false, false);
        instance.setLocaleFallback(new IAceLocaleFallback() {
            /* class ohos.ace.ability.$$Lambda$InstantComponent$T5F_ogOP_UALiM0i18V7Bbc_Hg */

            @Override // com.huawei.ace.runtime.IAceLocaleFallback
            public final String onLocaleFallback(String str, String[] strArr) {
                return InstantComponent.m2lambda$T5F_ogOP_UALiM0i18V7Bbc_Hg(InstantComponent.this, str, strArr);
            }
        });
        ProcessInfo processInfo = this.context.getProcessInfo();
        instance.setLocale();
        if (processInfo != null) {
            instance.setPid(processInfo.getPid());
        }
        createContainer(str);
    }

    public void destroy() {
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.d(LOG_TAG, "Not rendering");
        } else {
            aceContainer.destroyContainer();
        }
    }

    private void createContainer(String str) {
        this.container = AceEnv.createContainer(new AceEventCallback() {
            /* class ohos.ace.ability.InstantComponent.AnonymousClass2 */

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
            this.container.addCustomAssetPath(this.cardHapPath, new String[]{"assets/js/" + this.jsModuleName + PsuedoNames.PSEUDONAME_ROOT, "assets/js/share/"});
            AceContainer aceContainer2 = this.container;
            AcePage createPage = aceContainer2.createPage();
            if (str == null) {
                str = "";
            }
            aceContainer2.setPageContent(createPage, "", str);
            IAceView view = this.container.getView(this.density, getWidth(), getHeight());
            if (view instanceof Component) {
                addComponent((Component) view);
            }
            view.viewCreated();
        }
    }

    /* access modifiers changed from: private */
    public String onLocaleFallback(String str, String[] strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        Collections.addAll(arrayList, strArr);
        StringBuilder sb = new StringBuilder();
        try {
            ArrayList<String> findValidAndSort = LocalesFallback.getInstance().findValidAndSort(str, arrayList);
            if (findValidAndSort.size() > 0) {
                for (String str2 : findValidAndSort) {
                    sb.append(str2);
                    sb.append(",");
                }
            }
        } catch (LocaleFallBackException unused) {
            ALog.e(LOG_TAG, "findValidAndSort failed");
        }
        sb.append("en-US");
        return sb.toString();
    }

    public void updateInstantData(String str) {
        AceContainer aceContainer = this.container;
        if (aceContainer == null) {
            ALog.w(LOG_TAG, "container is null");
        } else {
            aceContainer.updateInstantData(str);
        }
    }
}
