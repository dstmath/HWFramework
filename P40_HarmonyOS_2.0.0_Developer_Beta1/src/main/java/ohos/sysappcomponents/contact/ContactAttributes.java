package ohos.sysappcomponents.contact;

import java.util.EnumSet;
import java.util.Set;

public class ContactAttributes {
    private final Set<Attribute> mAttributes = EnumSet.noneOf(Attribute.class);

    public enum Attribute {
        ATTR_EMAIL,
        ATTR_IM,
        ATTR_NICKNAME,
        ATTR_ORGANIZATION,
        ATTR_PHONE,
        ATTR_SIP_ADDRESS,
        ATTR_NAME,
        ATTR_POSTAL_ADDRESS,
        ATTR_IDENTITY,
        ATTR_PORTRAIT,
        ATTR_GROUP_MEMBERSHIP,
        ATTR_NOTE,
        ATTR_CONTACT_EVENT,
        ATTR_WEBSITE,
        ATTR_RELATION,
        ATTR_CONTACT_MISC,
        ATTR_CAMCARD_PHOTO,
        ATTR_HICALL_DEVICE
    }

    public Set<Attribute> getAttributes() {
        return this.mAttributes;
    }

    public void add(Attribute attribute) {
        if (attribute != null) {
            this.mAttributes.add(attribute);
        }
    }

    public boolean isValid(Attribute attribute) {
        if (attribute == null) {
            return false;
        }
        return this.mAttributes.contains(attribute);
    }
}
