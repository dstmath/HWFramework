package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import ohos.aafwk.ability.AbilityForm;
import ohos.aafwk.ability.IAbilityFormClient;
import ohos.aafwk.ability.IAbilityFormService;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.LayoutScatterException;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class AbilityForm implements Sequenceable {
    private static Supplier<ComponentProvider> DEFAULT_REMOTE_COMPONENTS_BUILDER = $$Lambda$UxQPvovYsqBDdS9sOEPHp_Smk.INSTANCE;
    private static BiFunction<Integer, Context, ComponentProvider> DEFAULT_REMOTE_COMPONENTS_BUILDER_WITH_LAYOUTID = $$Lambda$w41QkgBP4fV7giPP62RbMZpBqI.INSTANCE;
    private static Function<String, ComponentProvider> DEFAULT_REMOTE_COMPONENT_BUILDER_WITH_XML = $$Lambda$5NpKy26KzXhBPzdZ6hL5qNGpnu8.INSTANCE;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108672, "AbilityForm");
    private static final int MAX_UNMARSHALLING_SIZE = 3145728;
    private static final int PADDING_BOTTOM = 10;
    private static final int PADDING_LEFT = 10;
    private static final int PADDING_RIGHT = 10;
    private static final int PADDING_TOP = 10;
    public static final String PERMISSION_REQUIRE_FORM = "ohos.permission.REQUIRE_FORM";
    public static final Sequenceable.Producer<AbilityForm> PRODUCER = $$Lambda$AbilityForm$ePp9TyvYAshtyWWLnuQl0ZHMHP0.INSTANCE;
    private AbilityFormClient abilityFormClientStub;
    private IAbilityFormService abilityFormServiceProxy;
    private AbilityFormService abilityFormServiceStub;
    private final Set<Integer> componentIdsListen = new HashSet();
    private Context context;
    private Intent formIntent;
    private final Object lock = new Object();
    private Component.ClickedListener onClickListener = null;
    private ComponentProvider remoteComponent;
    private final Object tasklock = new Object();
    private TaskDispatcher uiTaskDispatcher;
    private final Map<Integer, ViewListener> viewsListener = new HashMap();

    public interface OnAcquiredCallback {
        void onAcquired(AbilityForm abilityForm);

        void onDestroyed(AbilityForm abilityForm);
    }

    private AbilityForm() {
    }

    public AbilityForm(int i, Context context2) {
        asService();
        if (context2 == null || context2.getUITaskDispatcher() == null) {
            HiLog.error(LABEL, "context or uiTaskDispatcher is null", new Object[0]);
            throw new IllegalStateException("context or uiTaskDispatcher is null");
        }
        this.remoteComponent = DEFAULT_REMOTE_COMPONENTS_BUILDER_WITH_LAYOUTID.apply(Integer.valueOf(i), context2);
        setUITaskDispatcher(context2.getUITaskDispatcher());
    }

    static AbilityForm createFromParcel(MessageParcel messageParcel) {
        AbilityForm abilityForm = new AbilityForm();
        if (messageParcel != null && messageParcel.readSequenceable(abilityForm) && abilityForm.abilityFormServiceProxy != null && abilityForm.remoteComponent != null) {
            return abilityForm;
        }
        HiLog.error(LABEL, "create from parcel failed. proxy: %{public}s, remote component: %{public}s", new Object[]{abilityForm.abilityFormServiceProxy, abilityForm.remoteComponent});
        return null;
    }

    static /* synthetic */ AbilityForm lambda$static$0(Parcel parcel) {
        AbilityForm abilityForm = new AbilityForm();
        abilityForm.unmarshalling(parcel);
        return abilityForm;
    }

    private boolean marshallingRemoteComponentLocked(Parcel parcel, int i) {
        if (this.remoteComponent == null) {
            HiLog.error(LABEL, "remote components is null when marshalling.", new Object[0]);
            return false;
        }
        if (HiLog.isDebuggable()) {
            HiLog.debug(LABEL, "marshalling remote component in mode: %{public}d", new Object[]{Integer.valueOf(i)});
        }
        int applyType = this.remoteComponent.getApplyType();
        if (!this.remoteComponent.setApplyType(i)) {
            HiLog.error(LABEL, "set apply type error. before marshalling remote components.", new Object[0]);
            return false;
        }
        parcel.writeSequenceable(this.remoteComponent);
        if (this.remoteComponent.setApplyType(applyType)) {
            return true;
        }
        HiLog.error(LABEL, "set apply type error. after marshalling remote components.", new Object[0]);
        return false;
    }

    private boolean marshallingViewsListenerLocked(Parcel parcel) {
        int size = this.viewsListener.size();
        if (!parcel.writeInt(size)) {
            HiLog.error(LABEL, "write views listener size error. size: %{public}d", new Object[]{Integer.valueOf(size)});
            return false;
        }
        for (Integer num : this.viewsListener.keySet()) {
            if (!parcel.writeInt(num.intValue())) {
                HiLog.error(LABEL, "write views listener error. component id: %{public}d", new Object[]{num});
                return false;
            }
        }
        if (HiLog.isDebuggable()) {
            HiLog.debug(LABEL, "marshalling views listener, size: %{public}d", new Object[]{Integer.valueOf(size)});
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean marshallingAbilityFormLite(Parcel parcel) {
        synchronized (this.lock) {
            if (!marshallingRemoteComponentLocked(parcel, 2)) {
                return false;
            }
            if (!marshallingViewsListenerLocked(parcel)) {
                return false;
            }
            return true;
        }
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel instanceof MessageParcel)) {
            return false;
        }
        MessageParcel messageParcel = (MessageParcel) parcel;
        AbilityFormService abilityFormService = this.abilityFormServiceStub;
        if (!messageParcel.writeRemoteObject(abilityFormService == null ? null : abilityFormService.asObject())) {
            return false;
        }
        if (HiLog.isDebuggable()) {
            HiLog.debug(LABEL, "start marshalling ability form.", new Object[0]);
        }
        synchronized (this.lock) {
            if (!marshallingRemoteComponentLocked(parcel, 1)) {
                return false;
            }
            if (!marshallingViewsListenerLocked(parcel)) {
                return false;
            }
            return true;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel instanceof MessageParcel) {
            IRemoteObject readRemoteObject = ((MessageParcel) parcel).readRemoteObject();
            if (readRemoteObject == null) {
                HiLog.error(LABEL, "unmarshalling error. Remote is null", new Object[0]);
                return false;
            }
            this.abilityFormServiceProxy = AbilityFormService.asProxy(readRemoteObject);
            if (HiLog.isDebuggable()) {
                HiLog.debug(LABEL, "start unmarshalling ability form.", new Object[0]);
            }
            this.remoteComponent = DEFAULT_REMOTE_COMPONENTS_BUILDER.get();
            if (!parcel.readSequenceable(this.remoteComponent)) {
                return false;
            }
            int readInt = parcel.readInt();
            if (readInt > 3145728 || readInt < 0) {
                HiLog.error(LABEL, "unmarshalling error. wrong length", new Object[0]);
                return false;
            }
            if (HiLog.isDebuggable()) {
                HiLog.debug(LABEL, "unmarshalling views listener, size: %{public}d", new Object[]{Integer.valueOf(readInt)});
            }
            synchronized (this.lock) {
                for (int i = 0; i < readInt; i++) {
                    this.componentIdsListen.add(Integer.valueOf(parcel.readInt()));
                }
            }
            return true;
        }
        HiLog.error(LABEL, "unmarshalling error. Parcel not in right type", new Object[0]);
        return false;
    }

    public Intent getFullPageIntent() {
        Intent intent;
        synchronized (this.lock) {
            intent = new Intent(this.formIntent);
        }
        return intent;
    }

    public void setText(int i, String str) {
        if (i < 0) {
            HiLog.error(LABEL, "The componentId is illegal", new Object[0]);
            throw new IllegalArgumentException("The componentId is illegal");
        } else if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "set text only called in supplier side.", new Object[0]);
        } else {
            ComponentProvider componentProvider = DEFAULT_REMOTE_COMPONENTS_BUILDER.get();
            componentProvider.setText(i, str);
            sendActions(componentProvider);
        }
    }

    public void setTextSize(int i, int i2) {
        if (i < 0) {
            HiLog.error(LABEL, "The componentId is illegal", new Object[0]);
            throw new IllegalArgumentException("The componentId is illegal");
        } else if (i2 < 0) {
            HiLog.error(LABEL, "The size is illegal", new Object[0]);
            throw new IllegalArgumentException("The size is illegal");
        } else if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "set text size only called in supplier side.", new Object[0]);
        } else {
            ComponentProvider componentProvider = DEFAULT_REMOTE_COMPONENTS_BUILDER.get();
            componentProvider.setTextSize(i, i2);
            sendActions(componentProvider);
        }
    }

    public void sendActions(ComponentProvider componentProvider) {
        if (componentProvider == null) {
            HiLog.error(LABEL, "null remote component actions for send actions.", new Object[0]);
            return;
        }
        Collection<ComponentProvider.Action> actions = componentProvider.getActions();
        if (actions == null || actions.isEmpty()) {
            HiLog.error(LABEL, "actions is null or actions is empty", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.remoteComponent.mergeActions(actions);
            if (this.abilityFormClientStub == null) {
                HiLog.error(LABEL, "abilityformclientstub is null", new Object[0]);
            }
            this.abilityFormServiceStub.addComponentActionLocked(this.remoteComponent);
        }
    }

    public Component getComponent() {
        ComponentProvider componentProvider = this.remoteComponent;
        if (componentProvider != null) {
            return componentProvider.getAllComponents();
        }
        HiLog.error(LABEL, "remote components is null when get component.", new Object[0]);
        return null;
    }

    public boolean registerViewListener(int i, ViewListener viewListener) {
        if (i < 0) {
            HiLog.error(LABEL, "The componentId is illegal", new Object[0]);
            throw new IllegalArgumentException("The componentId is illegal");
        } else if (viewListener == null) {
            HiLog.error(LABEL, "The listener is null, registerViewListener failed", new Object[0]);
            throw new IllegalArgumentException("The listener is null, registerViewListener failed");
        } else if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "form service not init yet.", new Object[0]);
            return false;
        } else {
            synchronized (this.lock) {
                if (this.remoteComponent == null) {
                    HiLog.error(LABEL, "remote components is null when register listener. component id: %{public}d", new Object[]{Integer.valueOf(i)});
                    return false;
                }
                if (HiLog.isDebuggable()) {
                    HiLog.debug(LABEL, "register view listener, component id: %{public}d", new Object[]{Integer.valueOf(i)});
                }
                this.abilityFormServiceStub.addViewListenerLocked(i);
                this.componentIdsListen.add(Integer.valueOf(i));
                viewListener.componentId = i;
                this.viewsListener.put(Integer.valueOf(i), viewListener);
                return true;
            }
        }
    }

    public void startFullPage() {
        synchronized (this.lock) {
            if (this.formIntent == null) {
                HiLog.error(LABEL, "formIntent is null. start full page can not be called at supplier side", new Object[0]);
            } else if (this.context == null) {
                HiLog.error(LABEL, "context is null. start full page can not be called at supplier side", new Object[0]);
            } else {
                this.formIntent.setFlags(32);
                this.context.startAbility(this.formIntent, -1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disableUpdatePush() {
        AbilityFormClient abilityFormClient = this.abilityFormClientStub;
        if (abilityFormClient == null) {
            HiLog.error(LABEL, "disable update only allow to be called in client side.", new Object[0]);
            return;
        }
        IAbilityFormService iAbilityFormService = this.abilityFormServiceProxy;
        if (iAbilityFormService == null) {
            HiLog.error(LABEL, "disable form client not init yet.", new Object[0]);
            return;
        }
        try {
            iAbilityFormService.disableUpdatePush(abilityFormClient.asObject());
        } catch (RemoteException e) {
            HiLog.error(LABEL, "disable update push failed. exception: %{public}s", new Object[]{e});
        }
    }

    /* access modifiers changed from: package-private */
    public void enableUpdatePush() {
        AbilityFormClient abilityFormClient = this.abilityFormClientStub;
        if (abilityFormClient == null) {
            HiLog.error(LABEL, "enable update only allow to be called in client side.", new Object[0]);
            return;
        }
        IAbilityFormService iAbilityFormService = this.abilityFormServiceProxy;
        if (iAbilityFormService == null) {
            HiLog.error(LABEL, "enable form client not init yet.", new Object[0]);
            return;
        }
        try {
            AbilityFormLite requestLatestForm = iAbilityFormService.requestLatestForm(abilityFormClient.asObject());
            synchronized (this.lock) {
                if (requestLatestForm == null) {
                    HiLog.error(LABEL, "enable update push failed. formLite is null.", new Object[0]);
                } else if (requestLatestForm.remoteComponent == null) {
                    HiLog.error(LABEL, "enable update push failed. formLite's remote component is null.", new Object[0]);
                } else if (requestLatestForm.componentIdsListen == null) {
                    HiLog.error(LABEL, "enable update push failed. formLite's componentIds listen is null.", new Object[0]);
                } else {
                    requestLatestForm.remoteComponent.applyAction(this.remoteComponent.getAllComponents());
                    requestLatestForm.remoteComponent.resetActions();
                    this.componentIdsListen.addAll(requestLatestForm.componentIdsListen);
                    this.abilityFormServiceProxy.enableUpdatePush(this.abilityFormClientStub.asObject());
                }
            }
        } catch (RemoteException e) {
            HiLog.error(LABEL, "enable update push failed. exception: %{public}s", new Object[]{e});
        }
    }

    /* access modifiers changed from: package-private */
    public void release() {
        AbilityFormService abilityFormService = this.abilityFormServiceStub;
        if (abilityFormService == null) {
            HiLog.warn(LABEL, "form service is already null when release.", new Object[0]);
            return;
        }
        abilityFormService.release();
        this.abilityFormServiceStub = null;
    }

    /* access modifiers changed from: package-private */
    public void setUITaskDispatcher(TaskDispatcher taskDispatcher) {
        if (taskDispatcher != null) {
            synchronized (this.tasklock) {
                this.uiTaskDispatcher = taskDispatcher;
            }
            return;
        }
        HiLog.error(LABEL, "uiTaskDispatcher is null", new Object[0]);
        throw new IllegalArgumentException("uiTaskDispatcher is null");
    }

    /* access modifiers changed from: package-private */
    public void setFullPageIntentElement(ElementName elementName) {
        if (elementName == null) {
            HiLog.error(LABEL, "intent element is null. when set intent element for form", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            if (this.formIntent == null) {
                if (HiLog.isDebuggable()) {
                    HiLog.debug(LABEL, "form intent is null, when set intent for form. refresh it with new one", new Object[0]);
                }
                this.formIntent = new Intent();
            }
            this.formIntent.setElement(elementName);
        }
    }

    public void setFullPageIntent(Intent intent) {
        if (intent == null) {
            HiLog.error(LABEL, "intent is null when set intent for form.", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.formIntent = intent;
        }
    }

    private void asService() {
        this.abilityFormServiceStub = new AbilityFormService();
    }

    /* access modifiers changed from: package-private */
    public boolean asClient(Context context2) {
        if (context2 == null) {
            HiLog.error(LABEL, "context is null, as client failed", new Object[0]);
            return false;
        }
        synchronized (this.lock) {
            if (this.remoteComponent == null) {
                HiLog.error(LABEL, "remote components is null when as client.", new Object[0]);
                return false;
            } else if (this.abilityFormServiceProxy == null) {
                HiLog.error(LABEL, "ability form service proxy is null when as client.", new Object[0]);
                return false;
            } else {
                this.context = context2;
                try {
                    this.remoteComponent.inflateLayout(context2);
                    ComponentContainer allComponents = this.remoteComponent.getAllComponents();
                    if (allComponents == null) {
                        HiLog.error(LABEL, "component container is null after inflate when as client.", new Object[0]);
                        return false;
                    }
                    if (HiLog.isDebuggable()) {
                        HiLog.debug(LABEL, "set default padding", new Object[0]);
                    }
                    allComponents.setPadding(10, 10, 10, 10);
                    this.remoteComponent.applyAction(allComponents);
                    allComponents.setClickedListener(new Component.ClickedListener() {
                        /* class ohos.aafwk.ability.$$Lambda$AbilityForm$BljCVXdE4oS6sG5YDgiAiD8x8 */

                        @Override // ohos.agp.components.Component.ClickedListener
                        public final void onClick(Component component) {
                            AbilityForm.this.lambda$asClient$1$AbilityForm(component);
                        }
                    });
                    initOnClickListenerLocked();
                    removeInvalidComponentIdsListen();
                    setComponentIdsListenerLocked(this.componentIdsListen);
                    this.abilityFormClientStub = new AbilityFormClient();
                    try {
                        this.abilityFormServiceProxy.registerClient(this.abilityFormClientStub.asObject());
                        return true;
                    } catch (RemoteException unused) {
                        HiLog.error(LABEL, "register client failed", new Object[0]);
                        return false;
                    }
                } catch (LayoutScatterException unused2) {
                    HiLog.error(LABEL, "remote layout inflate failed, its invalid!", new Object[0]);
                    return false;
                }
            }
        }
    }

    public /* synthetic */ void lambda$asClient$1$AbilityForm(Component component) {
        startFullPage();
    }

    /* access modifiers changed from: package-private */
    public ComponentProvider getRemoteComponent() {
        return this.remoteComponent;
    }

    private void removeInvalidComponentIdsListen() {
        Iterator<Integer> it = this.componentIdsListen.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            if (!this.remoteComponent.isValidComponentId(next.intValue())) {
                HiLog.info(LABEL, "invalid component id: %{public}d", new Object[]{next});
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setComponentIdsListenerLocked(Set<Integer> set) {
        if (this.onClickListener == null) {
            HiLog.error(LABEL, "click listener is null", new Object[0]);
            return;
        }
        ComponentProvider componentProvider = this.remoteComponent;
        if (componentProvider == null) {
            HiLog.error(LABEL, "remote components is null when set listeners.", new Object[0]);
            return;
        }
        ComponentContainer allComponents = componentProvider.getAllComponents();
        if (allComponents == null) {
            HiLog.error(LABEL, "component container is null for remote components: %{public}s, when set listeners", new Object[]{this.remoteComponent});
            return;
        }
        for (Integer num : set) {
            int intValue = num.intValue();
            Component findComponentById = allComponents.findComponentById(intValue);
            if (findComponentById == null) {
                HiLog.error(LABEL, "unavailable component id: %{public}d.", new Object[]{Integer.valueOf(intValue)});
            } else {
                if (HiLog.isDebuggable()) {
                    HiLog.debug(LABEL, "set listener for component id: %{public}d, type: %{public}s", new Object[]{Integer.valueOf(intValue), findComponentById.getClass()});
                }
                findComponentById.setClickedListener(this.onClickListener);
            }
        }
    }

    private void initOnClickListenerLocked() {
        this.onClickListener = new Component.ClickedListener() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityForm$uGqolV2Jq_jXE68VPqzQCSVm0N4 */

            @Override // ohos.agp.components.Component.ClickedListener
            public final void onClick(Component component) {
                AbilityForm.this.lambda$initOnClickListenerLocked$2$AbilityForm(component);
            }
        };
    }

    public /* synthetic */ void lambda$initOnClickListenerLocked$2$AbilityForm(Component component) {
        if (component != null) {
            if (HiLog.isDebuggable()) {
                HiLog.debug(LABEL, "clicked. component id: %{public}d", new Object[]{Integer.valueOf(component.getId())});
            }
            synchronized (this.lock) {
                if (this.componentIdsListen.contains(Integer.valueOf(component.getId()))) {
                    ViewsStatus buildViewsStatus = ViewsStatus.buildViewsStatus(this.remoteComponent, this.componentIdsListen, this.formIntent);
                    if (buildViewsStatus == null) {
                        HiLog.info(LABEL, "no components status collected.", new Object[0]);
                        return;
                    }
                    try {
                        this.abilityFormServiceProxy.sendOnTouchEvent(component.getId(), buildViewsStatus);
                    } catch (RemoteException e) {
                        HiLog.error(LABEL, "remote error occurs when send event to provider on click callback. componentId: %{public}d, exception: %{public}s", new Object[]{Integer.valueOf(component.getId()), e});
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class AbilityFormLite implements Sequenceable {
        public static final Sequenceable.Producer<AbilityFormLite> PRODUCER = $$Lambda$AbilityForm$AbilityFormLite$HCkcyzb7J8GhFX2UHUNLHvRsLuw.INSTANCE;
        private Set<Integer> componentIdsListen;
        private AbilityForm form;
        private ComponentProvider remoteComponent;

        AbilityFormLite(AbilityForm abilityForm) {
            this.form = abilityForm;
        }

        AbilityFormLite() {
        }

        static /* synthetic */ AbilityFormLite lambda$static$0(Parcel parcel) {
            AbilityFormLite abilityFormLite = new AbilityFormLite();
            abilityFormLite.unmarshalling(parcel);
            return abilityFormLite;
        }

        public boolean marshalling(Parcel parcel) {
            if (this.form == null) {
                HiLog.error(AbilityForm.LABEL, "main form is null while marshaling lite.", new Object[0]);
                return false;
            }
            if (HiLog.isDebuggable()) {
                HiLog.debug(AbilityForm.LABEL, "start marshalling ability form lite.", new Object[0]);
            }
            return this.form.marshallingAbilityFormLite(parcel);
        }

        public boolean unmarshalling(Parcel parcel) {
            if (HiLog.isDebuggable()) {
                HiLog.debug(AbilityForm.LABEL, "start unmarshalling ability form lite.", new Object[0]);
            }
            this.remoteComponent = (ComponentProvider) AbilityForm.DEFAULT_REMOTE_COMPONENTS_BUILDER.get();
            if (!parcel.readSequenceable(this.remoteComponent)) {
                return false;
            }
            int readInt = parcel.readInt();
            if (readInt > 3145728 || readInt < 0) {
                HiLog.error(AbilityForm.LABEL, "unmarshalling error. wrong length", new Object[0]);
                return false;
            }
            if (HiLog.isDebuggable()) {
                HiLog.debug(AbilityForm.LABEL, "unmarshalling views listener for lite, size: %{public}d", new Object[]{Integer.valueOf(readInt)});
            }
            this.componentIdsListen = new HashSet(readInt);
            for (int i = 0; i < readInt; i++) {
                this.componentIdsListen.add(Integer.valueOf(parcel.readInt()));
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class AbilityFormService extends IAbilityFormService.FormServiceStub {
        private final List<FormClientRecord> formClients;

        private AbilityFormService() {
            this.formClients = new ArrayList();
        }

        /* access modifiers changed from: private */
        public class FormClientRecord {
            private IAbilityFormClient client;
            private IRemoteObject.DeathRecipient deathRecipient;
            private boolean isUpdateEnabled = true;

            FormClientRecord(IAbilityFormClient iAbilityFormClient) {
                this.client = iAbilityFormClient;
            }

            /* access modifiers changed from: package-private */
            public IAbilityFormClient getAbilityFormClient() {
                return this.client;
            }

            /* access modifiers changed from: package-private */
            public void disableUpdatePush() {
                this.isUpdateEnabled = false;
            }

            /* access modifiers changed from: package-private */
            public void enableUpdatePush() {
                this.isUpdateEnabled = true;
            }

            /* access modifiers changed from: package-private */
            public void setDeathRecipient(IRemoteObject.DeathRecipient deathRecipient2) {
                this.deathRecipient = deathRecipient2;
            }

            /* access modifiers changed from: package-private */
            public IRemoteObject.DeathRecipient getDeathRecipient() {
                return this.deathRecipient;
            }
        }

        private boolean isAlreadyRegisteredLocked(IRemoteObject iRemoteObject) {
            for (FormClientRecord formClientRecord : this.formClients) {
                IAbilityFormClient abilityFormClient = formClientRecord.getAbilityFormClient();
                if (abilityFormClient != null && abilityFormClient.asObject() == iRemoteObject) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private FormClientRecord getClientByTokenLocked(IRemoteObject iRemoteObject) {
            for (FormClientRecord formClientRecord : this.formClients) {
                IAbilityFormClient abilityFormClient = formClientRecord.getAbilityFormClient();
                if (abilityFormClient != null && abilityFormClient.asObject() == iRemoteObject) {
                    return formClientRecord;
                }
            }
            return null;
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void registerClient(final IRemoteObject iRemoteObject) throws RemoteException {
            synchronized (AbilityForm.this.lock) {
                if (!isAlreadyRegisteredLocked(iRemoteObject)) {
                    IAbilityFormClient asProxy = AbilityFormClient.asProxy(iRemoteObject);
                    if (asProxy != null) {
                        IRemoteObject.DeathRecipient r3 = new IRemoteObject.DeathRecipient() {
                            /* class ohos.aafwk.ability.AbilityForm.AbilityFormService.AnonymousClass1 */

                            public void onRemoteDied() {
                                synchronized (AbilityForm.this.lock) {
                                    iRemoteObject.removeDeathRecipient(this, 0);
                                    AbilityFormService.this.formClients.remove(AbilityFormService.this.getClientByTokenLocked(iRemoteObject));
                                }
                            }
                        };
                        iRemoteObject.addDeathRecipient(r3, 0);
                        FormClientRecord formClientRecord = new FormClientRecord(asProxy);
                        formClientRecord.setDeathRecipient(r3);
                        this.formClients.add(formClientRecord);
                    }
                } else {
                    HiLog.error(AbilityForm.LABEL, "client is already registered", new Object[0]);
                    throw new RemoteException();
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void sendOnTouchEvent(int i, ViewsStatus viewsStatus) {
            if (HiLog.isDebuggable()) {
                HiLog.debug(AbilityForm.LABEL, "click event received. component id: %{public}d", new Object[]{Integer.valueOf(i)});
            }
            synchronized (AbilityForm.this.tasklock) {
                if (AbilityForm.this.uiTaskDispatcher == null) {
                    HiLog.error(AbilityForm.LABEL, "task dispatcher is null handle event from client.", new Object[0]);
                } else {
                    AbilityForm.this.uiTaskDispatcher.asyncDispatch(new Runnable(i, viewsStatus) {
                        /* class ohos.aafwk.ability.$$Lambda$AbilityForm$AbilityFormService$FYbIo6kODB4IIcCdzHBs2942_Jc */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ ViewsStatus f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AbilityForm.AbilityFormService.this.lambda$sendOnTouchEvent$0$AbilityForm$AbilityFormService(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$sendOnTouchEvent$0$AbilityForm$AbilityFormService(int i, ViewsStatus viewsStatus) {
            ViewListener viewListener;
            synchronized (AbilityForm.this.lock) {
                viewListener = (ViewListener) AbilityForm.this.viewsListener.get(Integer.valueOf(i));
            }
            if (viewListener == null) {
                HiLog.warn(AbilityForm.LABEL, "listener is null handle event from client. component id: %{public}d", new Object[]{Integer.valueOf(i)});
            } else {
                viewListener.onTouchEvent(AbilityForm.this, viewsStatus);
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void unregisterClient(IRemoteObject iRemoteObject) throws RemoteException {
            synchronized (AbilityForm.this.lock) {
                if (isAlreadyRegisteredLocked(iRemoteObject)) {
                    FormClientRecord clientByTokenLocked = getClientByTokenLocked(iRemoteObject);
                    if (clientByTokenLocked != null) {
                        iRemoteObject.removeDeathRecipient(clientByTokenLocked.getDeathRecipient(), 0);
                        this.formClients.remove(clientByTokenLocked);
                    }
                } else {
                    throw new RemoteException();
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void disableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException {
            synchronized (AbilityForm.this.lock) {
                FormClientRecord clientByTokenLocked = getClientByTokenLocked(iRemoteObject);
                if (clientByTokenLocked != null) {
                    clientByTokenLocked.disableUpdatePush();
                } else {
                    HiLog.warn(AbilityForm.LABEL, "client has not been registered yet. Unable to disable update client: %{public}s", new Object[]{iRemoteObject});
                    throw new RemoteException();
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void enableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException {
            synchronized (AbilityForm.this.lock) {
                FormClientRecord clientByTokenLocked = getClientByTokenLocked(iRemoteObject);
                if (clientByTokenLocked != null) {
                    clientByTokenLocked.enableUpdatePush();
                } else {
                    HiLog.warn(AbilityForm.LABEL, "client has not been registered yet. Unable to enable update client: %{public}s", new Object[]{iRemoteObject});
                    throw new RemoteException();
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public AbilityFormLite requestLatestForm(IRemoteObject iRemoteObject) {
            return new AbilityFormLite(AbilityForm.this);
        }

        /* access modifiers changed from: package-private */
        public void addComponentActionLocked(ComponentProvider componentProvider) {
            if (componentProvider != null) {
                if (HiLog.isDebuggable()) {
                    HiLog.debug(AbilityForm.LABEL, "begin to send action to all clients. size: %{public}d.", new Object[]{Integer.valueOf(this.formClients.size())});
                }
                for (FormClientRecord formClientRecord : this.formClients) {
                    if (formClientRecord.isUpdateEnabled) {
                        IAbilityFormClient abilityFormClient = formClientRecord.getAbilityFormClient();
                        try {
                            abilityFormClient.sendAction(componentProvider);
                        } catch (RemoteException unused) {
                            HiLog.error(AbilityForm.LABEL, "send action failed to client: %{public}s.", new Object[]{abilityFormClient});
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void addViewListenerLocked(int i) {
            if (HiLog.isDebuggable()) {
                HiLog.debug(AbilityForm.LABEL, "begin to send listener to all clients. size: %{public}d.", new Object[]{Integer.valueOf(this.formClients.size())});
            }
            for (FormClientRecord formClientRecord : this.formClients) {
                IAbilityFormClient abilityFormClient = formClientRecord.getAbilityFormClient();
                try {
                    abilityFormClient.sendListener(i);
                } catch (RemoteException unused) {
                    HiLog.error(AbilityForm.LABEL, "send action failed. client: %{public}s, componentId: %{public}d.", new Object[]{abilityFormClient, Integer.valueOf(i)});
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormService
        public void release() {
            synchronized (AbilityForm.this.lock) {
                HiLog.info(AbilityForm.LABEL, "supplier begins to release all clients. size: %{public}d.", new Object[]{Integer.valueOf(this.formClients.size())});
                this.formClients.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    public class AbilityFormClient extends IAbilityFormClient.FormClientStub {
        private AbilityFormClient() {
        }

        @Override // ohos.aafwk.ability.IAbilityFormClient
        public void sendAction(ComponentProvider componentProvider) {
            synchronized (AbilityForm.this.tasklock) {
                if (AbilityForm.this.uiTaskDispatcher == null) {
                    HiLog.error(AbilityForm.LABEL, "task dispatcher is null when handle action from supplier.", new Object[0]);
                } else {
                    AbilityForm.this.uiTaskDispatcher.asyncDispatch(new Runnable(componentProvider) {
                        /* class ohos.aafwk.ability.$$Lambda$AbilityForm$AbilityFormClient$fOAnN0SbLhNeVU18heqSwdS4Os */
                        private final /* synthetic */ ComponentProvider f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AbilityForm.AbilityFormClient.this.lambda$sendAction$0$AbilityForm$AbilityFormClient(this.f$1);
                        }
                    });
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityFormClient
        public void sendListener(int i) throws RemoteException {
            HashSet hashSet = new HashSet();
            hashSet.add(Integer.valueOf(i));
            synchronized (AbilityForm.this.tasklock) {
                if (AbilityForm.this.uiTaskDispatcher != null) {
                    AbilityForm.this.uiTaskDispatcher.asyncDispatch(new Runnable(hashSet) {
                        /* class ohos.aafwk.ability.$$Lambda$AbilityForm$AbilityFormClient$ujETHkx6JJUrCutLXt1bXu8WVM */
                        private final /* synthetic */ Set f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AbilityForm.AbilityFormClient.this.lambda$sendListener$1$AbilityForm$AbilityFormClient(this.f$1);
                        }
                    });
                } else {
                    HiLog.error(AbilityForm.LABEL, "task dispatcher is null when handle add listener from supplier.", new Object[0]);
                    throw new RemoteException();
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: handleDeltaActions */
        public void lambda$sendAction$0$AbilityForm$AbilityFormClient(ComponentProvider componentProvider) {
            if (componentProvider != null) {
                synchronized (AbilityForm.this.lock) {
                    if (AbilityForm.this.remoteComponent == null) {
                        HiLog.error(AbilityForm.LABEL, "remote components is null when handle delta actions.", new Object[0]);
                        return;
                    }
                    ComponentContainer allComponents = AbilityForm.this.remoteComponent.getAllComponents();
                    if (allComponents == null) {
                        HiLog.error(AbilityForm.LABEL, "component container is null when handle delta actions.", new Object[0]);
                        return;
                    }
                    componentProvider.applyAction(allComponents);
                    componentProvider.resetActions();
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: handleDeltaComponentIdsListen */
        public void lambda$sendListener$1$AbilityForm$AbilityFormClient(Set<Integer> set) {
            if (set != null) {
                synchronized (AbilityForm.this.lock) {
                    if (AbilityForm.this.remoteComponent == null) {
                        HiLog.error(AbilityForm.LABEL, "remote component is null when handle delta component id.", new Object[0]);
                        return;
                    }
                    for (Integer num : set) {
                        int intValue = num.intValue();
                        if (AbilityForm.this.componentIdsListen.contains(Integer.valueOf(intValue))) {
                            if (HiLog.isDebuggable()) {
                                HiLog.debug(AbilityForm.LABEL, "duplicate component id: %{public}d, handle delta component id.", new Object[]{Integer.valueOf(intValue)});
                            }
                        } else if (!AbilityForm.this.remoteComponent.isValidComponentId(intValue)) {
                            HiLog.error(AbilityForm.LABEL, "invalid component id: %{public}d, when handle delta component id", new Object[]{Integer.valueOf(intValue)});
                        } else {
                            AbilityForm.this.setComponentIdsListenerLocked(set);
                            AbilityForm.this.componentIdsListen.add(Integer.valueOf(intValue));
                        }
                    }
                }
            }
        }
    }
}
