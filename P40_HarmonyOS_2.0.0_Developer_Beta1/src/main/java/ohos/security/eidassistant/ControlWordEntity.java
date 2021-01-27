package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

class ControlWordEntity implements Sequenceable {
    private int encryptionMethod;
    private int transportCounter;

    ControlWordEntity(EidAssistant.ControlWord controlWord) {
        this.transportCounter = controlWord.getTransportCounter();
        this.encryptionMethod = controlWord.getEncryptionMethod();
    }

    public int getTransportCounter() {
        return this.transportCounter;
    }

    public void setTransportCounter(int i) {
        this.transportCounter = i;
    }

    public int getEncryptionMethod() {
        return this.encryptionMethod;
    }

    public void setEncryptionMethod(int i) {
        this.encryptionMethod = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.transportCounter);
        parcel.writeInt(this.encryptionMethod);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.transportCounter = parcel.readInt();
        this.encryptionMethod = parcel.readInt();
        return true;
    }
}
