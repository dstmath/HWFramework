package ohos.interwork.bundle;

import ohos.bundle.ElementName;
import ohos.interwork.utils.ParcelableEx;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ElementNameEx implements Sequenceable, ParcelableEx {
    private static final String COMPONETNANE = "android.content.ComponentName";
    public static final Sequenceable.Producer<ElementNameEx> PRODUCER = $$Lambda$ElementNameEx$uvnusUBDzs6n4FpHxHqnCVUasKo.INSTANCE;
    private String abilityName = "";
    private String bundleName = "";

    static /* synthetic */ ElementNameEx lambda$static$0(Parcel parcel) {
        ElementNameEx elementNameEx = new ElementNameEx();
        elementNameEx.unmarshalling(parcel);
        return elementNameEx;
    }

    public ElementNameEx() {
    }

    public ElementNameEx(String str, String str2) {
        this.bundleName = str;
        this.abilityName = str2;
    }

    public ElementNameEx(ElementName elementName) {
        if (elementName != null) {
            this.bundleName = elementName.getBundleName();
            this.abilityName = elementName.getAbilityName();
        }
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public void setAbilityName(String str) {
        this.abilityName = str;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public ElementName getElementName() {
        return new ElementName("", this.bundleName, this.abilityName);
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.bundleName) && parcel.writeString(this.abilityName)) {
            return true;
        }
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.abilityName = parcel.readString();
        return true;
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) {
        if (parcel.writeString(COMPONETNANE) && parcel.writeString(this.bundleName)) {
            parcel.writeString(this.abilityName);
        }
    }
}
