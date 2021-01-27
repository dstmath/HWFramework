package ohos.bundle;

import java.util.Objects;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ElementName implements Sequenceable {
    public static final Sequenceable.Producer<ElementName> PRODUCER = $$Lambda$ElementName$1PeoKhBxPan0G4peqK0CEMmsZds.INSTANCE;
    private String abilityName = "";
    private String bundleName = "";
    private String deviceId = "";

    static /* synthetic */ ElementName lambda$static$0(Parcel parcel) {
        ElementName elementName = new ElementName();
        elementName.unmarshalling(parcel);
        return elementName;
    }

    public ElementName() {
    }

    public ElementName(String str, String str2, String str3) {
        this.deviceId = str;
        this.bundleName = str2;
        this.abilityName = str3;
    }

    public ElementName(ElementName elementName) {
        if (elementName != null) {
            this.deviceId = elementName.deviceId;
            this.bundleName = elementName.bundleName;
            this.abilityName = elementName.abilityName;
        }
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
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

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getURI() {
        return this.deviceId + PsuedoNames.PSEUDONAME_ROOT + this.bundleName + PsuedoNames.PSEUDONAME_ROOT + this.abilityName;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.bundleName) && parcel.writeString(this.abilityName) && parcel.writeString(this.deviceId)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.abilityName = parcel.readString();
        this.deviceId = parcel.readString();
        return true;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ElementName)) {
            return false;
        }
        ElementName elementName = (ElementName) obj;
        return Objects.equals(this.deviceId, elementName.deviceId) && Objects.equals(this.bundleName, elementName.bundleName) && Objects.equals(this.abilityName, elementName.abilityName);
    }

    public int hashCode() {
        return Objects.hash(this.deviceId, this.bundleName, this.abilityName);
    }

    public static ElementName createRelative(String str, String str2, String str3) {
        if (str2 != null && !str2.isEmpty()) {
            return new ElementName(str3, str, getFullClassName(str, str2));
        }
        throw new IllegalArgumentException("the parameter abilityName cannot be empty");
    }

    public String getShortClassName() {
        int length;
        int length2;
        String str = this.abilityName;
        if (str == null || !str.startsWith(this.bundleName) || (length2 = this.abilityName.length()) <= (length = this.bundleName.length()) || this.abilityName.charAt(length) != '.') {
            return this.abilityName;
        }
        return this.abilityName.substring(length, length2);
    }

    public static ElementName unflattenFromString(String str) {
        if (str != null) {
            String[] split = str.split(PsuedoNames.PSEUDONAME_ROOT);
            if (split.length == 3) {
                String str2 = split[0];
                String str3 = split[1];
                return new ElementName(str2, str3, getFullClassName(str3, split[2]));
            }
            throw new IllegalArgumentException("the parameter elementName is illegal");
        }
        throw new IllegalArgumentException("the parameter elementName is null");
    }

    private static String getFullClassName(String str, String str2) {
        if (str2.isEmpty() || str2.charAt(0) != '.') {
            return str2;
        }
        return str + str2;
    }
}
