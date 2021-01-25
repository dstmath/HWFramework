package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public class OutZipEntity implements Sequenceable {
    private EidInfoExtendEntity deSkey;
    private EidInfoExtendEntity outImage;

    OutZipEntity(EidAssistant.SecImageZip secImageZip) {
        this.outImage = new EidInfoExtendEntity(secImageZip.getSecImage(), secImageZip.getSecImageLen());
        this.deSkey = new EidInfoExtendEntity(secImageZip.getDeSkey(), secImageZip.getDeSkeyLen());
    }

    public EidInfoExtendEntity getOutImage() {
        return this.outImage;
    }

    public EidInfoExtendEntity getDeSkey() {
        return this.deSkey;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        this.outImage.marshalling(parcel);
        this.deSkey.marshalling(parcel);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.outImage.unmarshalling(parcel);
        this.deSkey.unmarshalling(parcel);
        return true;
    }
}
