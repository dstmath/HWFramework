package ohos.aafwk.ability;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;

public class InstantProvider implements Sequenceable {
    private static final String ABILITY_NAME_KEY = "abilityName";
    private static final String BUNDLE_NAME_KEY = "bundleName";
    private static final String INSTANT_COMPONENT_CLASS_NAME = "ohos.ace.ability.InstantComponent";
    private static final Object LOCK = new Object();
    private static final int LOG_DOMAIN = 218118416;
    private static final String LOG_FORMAT = "%{public}s: %{private}s";
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, "Ace");
    private static final int MESSAGE_EVENT = 101;
    private static final String PARAMS_KEY = "params";
    private static final String PARAM_MESSAGE_KEY = "ohos.extra.param.key.message";
    public static final Sequenceable.Producer<InstantProvider> PRODUCER = $$Lambda$InstantProvider$zrfQOJPISc9lIg7UTGe_hs3YyyU.INSTANCE;
    private static final int ROUTER_EVENT = 100;
    private static final String TAG = "InstantProvider";
    private static volatile Method destroy;
    private static volatile Class<?> instantComponentClass;
    private static volatile Constructor<?> instantComponentConstructor;
    private static volatile boolean isReflectionsInitialized = false;
    private static volatile Method render;
    private static volatile Method setEventHandler;
    private static volatile Method update;
    private EventHandler abilityHandler;
    private Component component;
    private FormBindingData formBindingData = new FormBindingData();
    private boolean isInitializedData = false;
    private String jsFormCodePath = "";
    private String jsFormModuleName = "";

    public boolean hasFileDescriptor() {
        return false;
    }

    static /* synthetic */ InstantProvider lambda$static$0(Parcel parcel) {
        InstantProvider instantProvider = new InstantProvider();
        instantProvider.unmarshalling(parcel);
        return instantProvider;
    }

    public InstantProvider() {
    }

    public InstantProvider(String str, String str2) {
        this.jsFormCodePath = str2;
        this.jsFormModuleName = str;
    }

    public FormBindingData getFormBindingData() {
        return this.formBindingData;
    }

    public void setFormBindingData(FormBindingData formBindingData2) {
        if (formBindingData2 != null) {
            this.formBindingData = formBindingData2;
            this.isInitializedData = true;
        }
    }

    public boolean isInitializedData() {
        return this.isInitializedData;
    }

    public void setComponent(Component component2) {
        this.component = component2;
    }

    public Component getInstantComponent(Context context) throws InstantProviderException {
        Component component2 = this.component;
        if (component2 != null) {
            return component2;
        }
        try {
            initializeReflections();
            Object newInstance = instantComponentConstructor.newInstance(context, this.jsFormCodePath, this.jsFormModuleName);
            if (newInstance instanceof Component) {
                render.invoke(newInstance, this.formBindingData.getDataString());
                this.component = (Component) newInstance;
                return this.component;
            }
            throw new InstantProviderException("the new instant is not component");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new InstantProviderException(e.getMessage());
        }
    }

    public void update() {
        if (this.component != null) {
            try {
                initializeReflections();
                update.invoke(this.component, this.formBindingData.getDataString());
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                HiLog.error(LOG_LABEL, LOG_FORMAT, new Object[]{TAG, e.getMessage()});
            }
        }
    }

    public void destroy() {
        if (this.component != null) {
            try {
                initializeReflections();
                destroy.invoke(this.component, new Object[0]);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                HiLog.error(LOG_LABEL, LOG_FORMAT, new Object[]{TAG, e.getMessage()});
            }
        }
    }

    public void setAbilityHandler(EventHandler eventHandler) {
        this.abilityHandler = eventHandler;
    }

    public void setEventHandler() {
        if (this.component == null) {
            HiLog.error(LOG_LABEL, LOG_FORMAT, new Object[]{TAG, "fail to setEventHandler due to view is null"});
            return;
        }
        try {
            AnonymousClass1 r0 = new EventHandler(EventRunner.current()) {
                /* class ohos.aafwk.ability.InstantProvider.AnonymousClass1 */

                @Override // ohos.eventhandler.EventHandler
                public void processEvent(InnerEvent innerEvent) {
                    super.processEvent(innerEvent);
                    if (innerEvent != null && (innerEvent.object instanceof String)) {
                        int i = innerEvent.eventId;
                        String str = (String) innerEvent.object;
                        if (i == 100) {
                            try {
                                HiLog.debug(InstantProvider.LOG_LABEL, InstantProvider.LOG_FORMAT, new Object[]{InstantProvider.TAG, "event type is router event"});
                                InnerEvent innerEvent2 = InnerEvent.get(100, 0, JSONObject.parseObject(str));
                                if (InstantProvider.this.abilityHandler != null) {
                                    InstantProvider.this.abilityHandler.sendEvent(innerEvent2);
                                }
                            } catch (JSONException e) {
                                HiLogLabel hiLogLabel = InstantProvider.LOG_LABEL;
                                HiLog.error(hiLogLabel, InstantProvider.LOG_FORMAT, new Object[]{InstantProvider.TAG, "parse param failed, err: " + e.getMessage()});
                            }
                        } else if (i == 101) {
                            try {
                                HiLog.debug(InstantProvider.LOG_LABEL, InstantProvider.LOG_FORMAT, new Object[]{InstantProvider.TAG, "event type is message event"});
                                String string = JSONObject.parseObject(str).getString(InstantProvider.PARAMS_KEY);
                                Intent intent = new Intent();
                                intent.setParam(InstantProvider.PARAM_MESSAGE_KEY, string);
                                InnerEvent innerEvent3 = InnerEvent.get(101, 0, intent);
                                if (InstantProvider.this.abilityHandler != null) {
                                    InstantProvider.this.abilityHandler.sendEvent(innerEvent3);
                                }
                            } catch (JSONException e2) {
                                HiLogLabel hiLogLabel2 = InstantProvider.LOG_LABEL;
                                HiLog.error(hiLogLabel2, InstantProvider.LOG_FORMAT, new Object[]{InstantProvider.TAG, "parse param failed, err: " + e2.getMessage()});
                            }
                        } else {
                            HiLog.error(InstantProvider.LOG_LABEL, InstantProvider.LOG_FORMAT, new Object[]{InstantProvider.TAG, "wrong param of form event"});
                        }
                    }
                }
            };
            initializeReflections();
            setEventHandler.invoke(this.component, r0);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.error(LOG_LABEL, LOG_FORMAT, new Object[]{TAG, "fail to invoke setEventHandler method"});
        }
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.jsFormModuleName);
        parcel.writeString(this.jsFormCodePath);
        parcel.writeBoolean(this.isInitializedData);
        parcel.writeString(this.formBindingData.getDataString());
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.jsFormModuleName = parcel.readString();
        this.jsFormCodePath = parcel.readString();
        this.isInitializedData = parcel.readBoolean();
        this.formBindingData = new FormBindingData(parcel.readString());
        return true;
    }

    private static void initializeReflections() throws ClassNotFoundException, NoSuchMethodException {
        if (!isReflectionsInitialized) {
            synchronized (LOCK) {
                if (instantComponentClass == null) {
                    instantComponentClass = Class.forName(INSTANT_COMPONENT_CLASS_NAME);
                }
                if (render == null) {
                    render = instantComponentClass.getDeclaredMethod("render", String.class);
                }
                if (update == null) {
                    update = instantComponentClass.getDeclaredMethod("updateInstantData", String.class);
                }
                if (destroy == null) {
                    destroy = instantComponentClass.getDeclaredMethod("destroy", new Class[0]);
                }
                if (setEventHandler == null) {
                    setEventHandler = instantComponentClass.getDeclaredMethod("setEventHandler", EventHandler.class);
                }
                if (instantComponentConstructor == null) {
                    instantComponentConstructor = instantComponentClass.getConstructor(Context.class, String.class, String.class);
                }
                isReflectionsInitialized = true;
            }
        }
    }
}
