package ohos.aafwk.ability;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.zson.ZSONObject;

public class FormBindingData implements Sequenceable {
    public static final Sequenceable.Producer<FormBindingData> PRODUCER = $$Lambda$FormBindingData$vDWuSXSXpk4IkXWojSyKdNteC3U.INSTANCE;
    private ZSONObject formBindingData = new ZSONObject();

    static /* synthetic */ FormBindingData lambda$static$0(Parcel parcel) {
        FormBindingData formBindingData2 = new FormBindingData();
        formBindingData2.unmarshalling(parcel);
        return formBindingData2;
    }

    public FormBindingData() {
    }

    public FormBindingData(ZSONObject zSONObject) {
        this.formBindingData = zSONObject;
    }

    public FormBindingData(String str) {
        this.formBindingData = ZSONObject.stringToZSON(str);
    }

    public void updateData(ZSONObject zSONObject) {
        this.formBindingData = zSONObject;
    }

    public String getDataString() {
        return ZSONObject.toZSONString(this.formBindingData);
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(ZSONObject.toZSONString(this.formBindingData));
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.formBindingData = ZSONObject.stringToZSON(parcel.readString());
        return true;
    }
}
