package ohos.account.app;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AppAccount implements Sequenceable {
    private String name;
    private String owner;

    public AppAccount() {
        this(null, null);
    }

    public AppAccount(String str, String str2) {
        this.name = str;
        this.owner = str2;
    }

    public String toString() {
        return "AppAccount {name=" + this.name + ", owner=" + this.owner + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof AppAccount)) {
            return false;
        }
        AppAccount appAccount = (AppAccount) obj;
        return Objects.equals(this.name, appAccount.name) && Objects.equals(this.owner, appAccount.owner);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.owner);
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel != null && parcel.writeString(this.name) && parcel.writeString(this.owner)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.name = parcel.readString();
        this.owner = parcel.readString();
        return true;
    }
}
