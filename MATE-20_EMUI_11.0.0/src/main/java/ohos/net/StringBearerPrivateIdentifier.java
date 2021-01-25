package ohos.net;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class StringBearerPrivateIdentifier extends BearerPrivateIdentifier implements Sequenceable {
    public String identifier;

    public StringBearerPrivateIdentifier() {
    }

    public StringBearerPrivateIdentifier(String str) {
        this.identifier = str;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StringBearerPrivateIdentifier)) {
            return false;
        }
        return Objects.equals(this.identifier, ((StringBearerPrivateIdentifier) obj).identifier);
    }

    public int hashCode() {
        return Objects.hashCode(this.identifier);
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.identifier);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.identifier = parcel.readString();
        return true;
    }
}
