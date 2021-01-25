package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static Supplier<ComponentProvider> DEFAULT_REMOTE_VIEWS_BUILDER = $$Lambda$UxQPvovYsqBDdS9sOEPHp_Smk.INSTANCE;
    private static Function<String, ComponentProvider> DEFAULT_REMOTE_VIEWS_BUILDER_WITH_XML = $$Lambda$5NpKy26KzXhBPzdZ6hL5qNGpnu8.INSTANCE;
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
    private Context context;
    private Intent formIntent;
    private final Object lock = new Object();
    private Component.ClickedListener onClickListener = null;
    private ComponentProvider remoteView;
    private final Object tasklock = new Object();
    private TaskDispatcher uiTaskDispatcher;
    private final Set<Integer> viewIdsListen = new HashSet();
    private final Map<Integer, ViewListener> viewsListener = new HashMap();

    public interface OnAcquiredCallback {
        void onAcquired(AbilityForm abilityForm);

        void onDestroyed(AbilityForm abilityForm);
    }

    private AbilityForm() {
    }

    @Deprecated
    public AbilityForm(String str, Context context2) {
        if (str == null) {
            HiLog.error(LABEL, "xmlPath is null", new Object[0]);
            return;
        }
        asService();
        this.remoteView = DEFAULT_REMOTE_VIEWS_BUILDER_WITH_XML.apply(str);
        if (context2 == null || context2.getUITaskDispatcher() == null) {
            HiLog.error(LABEL, "context or uiTaskDispatcher is null", new Object[0]);
        } else {
            setUITaskDispatcher(context2.getUITaskDispatcher());
        }
    }

    public AbilityForm(int i, Context context2) {
        asService();
        if (context2 == null || context2.getUITaskDispatcher() == null) {
            HiLog.error(LABEL, "context or uiTaskDispatcher is null", new Object[0]);
            return;
        }
        this.remoteView = new ComponentProvider(i, context2);
        setUITaskDispatcher(context2.getUITaskDispatcher());
    }

    static AbilityForm createFromParcel(MessageParcel messageParcel) {
        AbilityForm abilityForm = new AbilityForm();
        if (messageParcel != null && messageParcel.readSequenceable(abilityForm) && abilityForm.abilityFormServiceProxy != null && abilityForm.remoteView != null) {
            return abilityForm;
        }
        HiLog.error(LABEL, "create from parcel failed. proxy: %{public}s, remote view: %{public}s", new Object[]{abilityForm.abilityFormServiceProxy, abilityForm.remoteView});
        return null;
    }

    static /* synthetic */ AbilityForm lambda$static$0(Parcel parcel) {
        AbilityForm abilityForm = new AbilityForm();
        abilityForm.unmarshalling(parcel);
        return abilityForm;
    }

    private boolean marshallingRemoteViewLocked(Parcel parcel, int i) {
        if (this.remoteView == null) {
            HiLog.error(LABEL, "remote views is null when marshalling.", new Object[0]);
            return false;
        }
        if (HiLog.isDebuggable()) {
            HiLog.debug(LABEL, "marshalling remote view in mode: %{public}d", new Object[]{Integer.valueOf(i)});
        }
        int applyType = this.remoteView.getApplyType();
        if (!this.remoteView.setApplyType(i)) {
            HiLog.error(LABEL, "set apply type error. before marshalling remote views.", new Object[0]);
            return false;
        }
        parcel.writeSequenceable(this.remoteView);
        if (this.remoteView.setApplyType(applyType)) {
            return true;
        }
        HiLog.error(LABEL, "set apply type error. after marshalling remote views.", new Object[0]);
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
                HiLog.error(LABEL, "write views listener error. view id: %{public}d", new Object[]{num});
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
            if (!marshallingRemoteViewLocked(parcel, 2)) {
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
            if (!marshallingRemoteViewLocked(parcel, 1)) {
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
            this.remoteView = DEFAULT_REMOTE_VIEWS_BUILDER.get();
            if (!parcel.readSequenceable(this.remoteView)) {
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
                    this.viewIdsListen.add(Integer.valueOf(parcel.readInt()));
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
        if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "set text only called in supplier side.", new Object[0]);
            return;
        }
        ComponentProvider componentProvider = DEFAULT_REMOTE_VIEWS_BUILDER.get();
        componentProvider.setText(i, str);
        sendActions(componentProvider);
    }

    public void setTextSize(int i, int i2) {
        if (i < 0) {
            HiLog.error(LABEL, "The viewId is illegal", new Object[0]);
            throw new IllegalArgumentException("The viewId is illegal");
        } else if (i2 < 0) {
            HiLog.error(LABEL, "The size is illegal", new Object[0]);
            throw new IllegalArgumentException("The size is illegal");
        } else if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "set text size only called in supplier side.", new Object[0]);
        } else {
            ComponentProvider componentProvider = DEFAULT_REMOTE_VIEWS_BUILDER.get();
            componentProvider.setTextSize(i, i2);
            sendActions(componentProvider);
        }
    }

    public void sendActions(ComponentProvider componentProvider) {
        if (componentProvider == null) {
            HiLog.error(LABEL, "null remote view actions for send actions.", new Object[0]);
            return;
        }
        Collection<ComponentProvider.Action> actions = componentProvider.getActions();
        if (actions == null || actions.isEmpty()) {
            HiLog.error(LABEL, "actions is null or actions is empty", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.remoteView.mergeActions(actions);
            if (this.abilityFormClientStub == null) {
                HiLog.error(LABEL, "abilityformclientstub is null", new Object[0]);
            }
            this.abilityFormServiceStub.addViewActionLocked(this.remoteView);
        }
    }

    public Component getView() {
        ComponentProvider componentProvider = this.remoteView;
        if (componentProvider != null) {
            return componentProvider.getAllComponents();
        }
        HiLog.error(LABEL, "remote views is null when get view.", new Object[0]);
        return null;
    }

    public boolean registerViewListener(int i, ViewListener viewListener) {
        if (this.abilityFormServiceStub == null) {
            HiLog.error(LABEL, "form service not init yet.", new Object[0]);
            return false;
        }
        synchronized (this.lock) {
            if (this.remoteView == null) {
                HiLog.error(LABEL, "remote views is null when register listener. view id: %{public}d", new Object[]{Integer.valueOf(i)});
                return false;
            }
            if (HiLog.isDebuggable()) {
                HiLog.debug(LABEL, "register view listener, view id: %{public}d", new Object[]{Integer.valueOf(i)});
            }
            this.abilityFormServiceStub.addViewListenerLocked(i);
            this.viewIdsListen.add(Integer.valueOf(i));
            viewListener.viewId = i;
            this.viewsListener.put(Integer.valueOf(i), viewListener);
            return true;
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
                } else if (requestLatestForm.remoteView == null) {
                    HiLog.error(LABEL, "enable update push failed. formLite's remote view is null.", new Object[0]);
                } else if (requestLatestForm.viewIdsListen == null) {
                    HiLog.error(LABEL, "enable update push failed. formLite's viewIds listen is null.", new Object[0]);
                } else {
                    requestLatestForm.remoteView.applyAction(this.remoteView.getAllComponents());
                    requestLatestForm.remoteView.resetActions();
                    this.viewIdsListen.addAll(requestLatestForm.viewIdsListen);
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
            if (this.remoteView == null) {
                HiLog.error(LABEL, "remote views is null when as client.", new Object[0]);
                return false;
            } else if (this.abilityFormServiceProxy == null) {
                HiLog.error(LABEL, "ability form service proxy is null when as client.", new Object[0]);
                return false;
            } else {
                this.context = context2;
                try {
                    this.remoteView.inflateLayout(context2);
                    ComponentContainer allComponents = this.remoteView.getAllComponents();
                    if (allComponents == null) {
                        HiLog.error(LABEL, "view group is null after inflate when as client.", new Object[0]);
                        return false;
                    }
                    if (HiLog.isDebuggable()) {
                        HiLog.debug(LABEL, "set default padding", new Object[0]);
                    }
                    allComponents.setPadding(10, 10, 10, 10);
                    this.remoteView.applyAction(allComponents);
                    allComponents.setClickedListener(new Component.ClickedListener() {
                        /* class ohos.aafwk.ability.$$Lambda$AbilityForm$BljCVXdE4oS6sG5YDgiAiD8x8 */

                        @Override // ohos.agp.components.Component.ClickedListener
                        public final void onClick(Component component) {
                            AbilityForm.this.lambda$asClient$1$AbilityForm(component);
                        }
                    });
                    initOnClickListenerLocked();
                    removeInvalidViewIdsListen();
                    setViewIdsListenerLocked(this.viewIdsListen);
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

    private void removeInvalidViewIdsListen() {
        Iterator<Integer> it = this.viewIdsListen.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            if (!this.remoteView.isValidComponentId(next.intValue())) {
                HiLog.info(LABEL, "invalid view id: %{public}d", new Object[]{next});
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setViewIdsListenerLocked(Set<Integer> set) {
        if (this.onClickListener == null) {
            HiLog.error(LABEL, "click listener is null", new Object[0]);
            return;
        }
        ComponentProvider componentProvider = this.remoteView;
        if (componentProvider == null) {
            HiLog.error(LABEL, "remote views is null when set listeners.", new Object[0]);
            return;
        }
        ComponentContainer allComponents = componentProvider.getAllComponents();
        if (allComponents == null) {
            HiLog.error(LABEL, "view group is null for remote views: %{public}s, when set listeners", new Object[]{this.remoteView});
            return;
        }
        for (Integer num : set) {
            int intValue = num.intValue();
            Component findComponentById = allComponents.findComponentById(intValue);
            if (findComponentById == null) {
                HiLog.error(LABEL, "unavailable view id: %{public}d.", new Object[]{Integer.valueOf(intValue)});
            } else {
                if (HiLog.isDebuggable()) {
                    HiLog.debug(LABEL, "set listener for view id: %{public}d, type: %{public}s", new Object[]{Integer.valueOf(intValue), findComponentById.getClass()});
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
                HiLog.debug(LABEL, "clicked. view id: %{public}d", new Object[]{Integer.valueOf(component.getId())});
            }
            synchronized (this.lock) {
                if (this.viewIdsListen.contains(Integer.valueOf(component.getId()))) {
                    ViewsStatus buildViewsStatus = ViewsStatus.buildViewsStatus(this.remoteView, this.viewIdsListen, this.formIntent);
                    if (buildViewsStatus == null) {
                        HiLog.info(LABEL, "no views status collected.", new Object[0]);
                        return;
                    }
                    try {
                        this.abilityFormServiceProxy.sendOnTouchEvent(component.getId(), buildViewsStatus);
                    } catch (RemoteException e) {
                        HiLog.error(LABEL, "remote error occurs when send event to provider on click callback. viewId: %{public}d, exception: %{public}s", new Object[]{Integer.valueOf(component.getId()), e});
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class AbilityFormLite implements Sequenceable {
        public static final Sequenceable.Producer<AbilityFormLite> PRODUCER = $$Lambda$AbilityForm$AbilityFormLite$HCkcyzb7J8GhFX2UHUNLHvRsLuw.INSTANCE;
        private AbilityForm form;
        private ComponentProvider remoteView;
        private Set<Integer> viewIdsListen;

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
            this.remoteView = (ComponentProvider) AbilityForm.DEFAULT_REMOTE_VIEWS_BUILDER.get();
            if (!parcel.readSequenceable(this.remoteView)) {
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
            this.viewIdsListen = new HashSet(readInt);
            for (int i = 0; i < readInt; i++) {
                this.viewIdsListen.add(Integer.valueOf(parcel.readInt()));
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
                HiLog.debug(AbilityForm.LABEL, "click event received. view id: %{public}d", new Object[]{Integer.valueOf(i)});
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
                HiLog.warn(AbilityForm.LABEL, "listener is null handle event from client. view id: %{public}d", new Object[]{Integer.valueOf(i)});
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
        public void addViewActionLocked(ComponentProvider componentProvider) {
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
                    HiLog.error(AbilityForm.LABEL, "send action failed. client: %{public}s, viewId: %{public}d.", new Object[]{abilityFormClient, Integer.valueOf(i)});
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
                    if (AbilityForm.this.remoteView == null) {
                        HiLog.error(AbilityForm.LABEL, "remote views is null when handle delta actions.", new Object[0]);
                        return;
                    }
                    ComponentContainer allComponents = AbilityForm.this.remoteView.getAllComponents();
                    if (allComponents == null) {
                        HiLog.error(AbilityForm.LABEL, "view group is null when handle delta actions.", new Object[0]);
                        return;
                    }
                    componentProvider.applyAction(allComponents);
                    componentProvider.resetActions();
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: handleDeltaViewIdsListen */
        public void lambda$sendListener$1$AbilityForm$AbilityFormClient(Set<Integer> set) {
            if (set != null) {
                synchronized (AbilityForm.this.lock) {
                    if (AbilityForm.this.remoteView == null) {
                        HiLog.error(AbilityForm.LABEL, "remote view is null when handle delta view id.", new Object[0]);
                        return;
                    }
                    for (Integer num : set) {
                        int intValue = num.intValue();
                        if (AbilityForm.this.viewIdsListen.contains(Integer.valueOf(intValue))) {
                            if (HiLog.isDebuggable()) {
                                HiLog.debug(AbilityForm.LABEL, "duplicate view id: %{public}d, handle delta view id.", new Object[]{Integer.valueOf(intValue)});
                            }
                        } else if (!AbilityForm.this.remoteView.isValidComponentId(intValue)) {
                            HiLog.error(AbilityForm.LABEL, "invalid view id: %{public}d, when handle delta view id", new Object[]{Integer.valueOf(intValue)});
                        } else {
                            AbilityForm.this.setViewIdsListenerLocked(set);
                            AbilityForm.this.viewIdsListen.add(Integer.valueOf(intValue));
                        }
                    }
                }
            }
        }
    }
}
