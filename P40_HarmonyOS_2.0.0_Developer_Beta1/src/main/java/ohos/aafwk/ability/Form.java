package ohos.aafwk.ability;

import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentProvider;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class Form implements Sequenceable {
    private static final LogLabel LABEL = LogLabel.create();
    public static final Sequenceable.Producer<Form> PRODUCER = $$Lambda$Form$XIL773Mx63sXMHWyuKcErw65mA.INSTANCE;
    String abilityName;
    String bundleName;
    Component component;
    int formId;
    String formName;
    private InstantProvider instantProvider;
    private boolean isJsForm = false;
    int previewID = -1;
    ComponentProvider remoteComponent;

    static /* synthetic */ Form lambda$static$0(Parcel parcel) {
        Form form = new Form();
        form.unmarshalling(parcel);
        return form;
    }

    public static Form createFromParcel(Parcel parcel) {
        Form form = new Form();
        if (parcel.readSequenceable(form)) {
            return form;
        }
        Log.error(LABEL, "create from parcel failed.", new Object[0]);
        return null;
    }

    public void setRemoteComponent(ComponentProvider componentProvider) {
        this.remoteComponent = componentProvider;
    }

    public void setComponent(Component component2) {
        this.component = component2;
    }

    public Component getComponent() {
        return this.component;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public int getFormId() {
        return this.formId;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public String getFormName() {
        return this.formName;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeInt(this.formId) || !parcel.writeString(this.bundleName) || !parcel.writeString(this.abilityName) || !parcel.writeString(this.formName) || !parcel.writeInt(this.previewID) || !parcel.writeBoolean(this.isJsForm)) {
            return false;
        }
        if (this.isJsForm) {
            parcel.writeSequenceable(this.instantProvider);
            return true;
        }
        ComponentProvider componentProvider = this.remoteComponent;
        if (componentProvider == null) {
            return true;
        }
        parcel.writeSequenceable(componentProvider);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.formId = parcel.readInt();
        this.bundleName = parcel.readString();
        this.abilityName = parcel.readString();
        this.formName = parcel.readString();
        int readInt = parcel.readInt();
        if (readInt != -1) {
            this.previewID = readInt;
        }
        this.isJsForm = parcel.readBoolean();
        if (this.isJsForm) {
            InstantProvider instantProvider2 = new InstantProvider();
            if (!parcel.readSequenceable(instantProvider2)) {
                Log.error(LABEL, "addForm form InstantProvider readSequenceable failed----.", new Object[0]);
                return true;
            }
            this.instantProvider = instantProvider2;
            return true;
        }
        ComponentProvider componentProvider = new ComponentProvider();
        if (!parcel.readSequenceable(componentProvider)) {
            Log.info(LABEL, "addForm form ComponentProvider readSequenceable failed----.", new Object[0]);
            componentProvider = null;
        }
        this.remoteComponent = componentProvider;
        return true;
    }

    public boolean isJsForm() {
        return this.isJsForm;
    }

    public void setJsForm(boolean z) {
        this.isJsForm = z;
    }

    public InstantProvider getInstantProvider() {
        return this.instantProvider;
    }

    public void setInstantProvider(InstantProvider instantProvider2) {
        this.instantProvider = instantProvider2;
    }
}
