package ohos.interwork.ability;

import ohos.aafwk.ability.InstantProvider;
import ohos.interwork.utils.ParcelableEx;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;

public class InstantProviderEx implements ParcelableEx {
    private static final String CLASS_NAME = "com.huawei.ohos.localability.InstantProvider";
    private final InstantProvider provider;

    public InstantProviderEx(InstantProvider instantProvider) {
        this.provider = instantProvider;
    }

    @Override // ohos.interwork.utils.ParcelableEx
    public void marshallingEx(Parcel parcel) throws ParcelException {
        parcel.writeString(CLASS_NAME);
        this.provider.marshalling(parcel);
    }
}
