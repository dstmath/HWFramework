package ohos.account.app;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AppAccount implements Sequenceable {
    private String name;
    private String type;

    public AppAccount() {
    }

    public AppAccount(String str, String str2) {
        this.type = str2;
        this.name = str;
    }

    public String toString() {
        return "AppAccount {name=" + this.name + ", type=" + this.type + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof AppAccount)) {
            return false;
        }
        AppAccount appAccount = (AppAccount) obj;
        return Objects.equals(this.name, appAccount.name) && Objects.equals(this.type, appAccount.type);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.type);
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel != null && parcel.writeString(this.name) && parcel.writeString(this.type)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.name = parcel.readString();
        this.type = parcel.readString();
        return true;
    }
}
