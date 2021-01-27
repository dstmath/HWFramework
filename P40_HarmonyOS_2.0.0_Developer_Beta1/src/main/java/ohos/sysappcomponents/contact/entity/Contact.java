package ohos.sysappcomponents.contact.entity;

import java.util.ArrayList;
import java.util.List;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.LogUtil;

public class Contact {
    public static final long INVALID_CONTACT_ID = -1;
    private static final int PHONE_NUMBER_DEFAULT_SIZE = 20;
    private static final String TAG = Contact.class.getSimpleName();
    private ContactAttributes attributes;
    private Portrait camCard;
    private long contactId;
    private List<Email> emails;
    private List<Event> events;
    private List<Group> groups;
    private List<HiCallDevice> hiCallDevices;
    private Identity identity;
    private List<ImAddress> imAddresses;
    private boolean isFavorite;
    private String key;
    private Name name;
    private NickName nickName;
    private Note note;
    private Organization organization;
    private List<PhoneNumber> phoneNumbers;
    private Portrait portrait;
    private List<PostalAddress> postalAddresses;
    private List<Relation> relations;
    private List<SipAddress> sipAddresses;
    private List<Website> websites;

    public static boolean isMyCard(long j) {
        return j >= Attribute.Profile.MIN_ID;
    }

    public long getId() {
        return this.contactId;
    }

    public void setId(long j) {
        this.contactId = j;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public void addPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            LogUtil.error(TAG, "addPhoneNumber method failed, the parameter phoneNumber cannot be null");
            return;
        }
        if (this.phoneNumbers == null) {
            this.phoneNumbers = new ArrayList();
        }
        this.phoneNumbers.add(phoneNumber);
    }

    public Portrait getPortrait() {
        return this.portrait;
    }

    public void setPortrait(Portrait portrait2) {
        this.portrait = portrait2;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization2) {
        this.organization = organization2;
    }

    public Name getName() {
        return this.name;
    }

    public void setName(Name name2) {
        this.name = name2;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(boolean z) {
        this.isFavorite = z;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String str) {
        this.key = str;
    }

    public List<Email> getEmails() {
        return this.emails;
    }

    public void addEmail(Email email) {
        if (email == null) {
            LogUtil.error(TAG, "addEmail method failed, the parameter email cannot be null");
            return;
        }
        if (this.emails == null) {
            this.emails = new ArrayList();
        }
        this.emails.add(email);
    }

    public List<ImAddress> getImAddresses() {
        return this.imAddresses;
    }

    public void addImAddress(ImAddress imAddress) {
        if (imAddress == null) {
            LogUtil.error(TAG, "addImAddress method failed, the parameter imAddress cannot be null");
            return;
        }
        if (this.imAddresses == null) {
            this.imAddresses = new ArrayList();
        }
        this.imAddresses.add(imAddress);
    }

    public NickName getNickName() {
        return this.nickName;
    }

    public void setNickName(NickName nickName2) {
        this.nickName = nickName2;
    }

    public List<SipAddress> getSipAddresses() {
        return this.sipAddresses;
    }

    public void addSipAddress(SipAddress sipAddress) {
        if (sipAddress == null) {
            LogUtil.error(TAG, "addSipAddress method failed, the parameter sipAddress cannot be null");
            return;
        }
        if (this.sipAddresses == null) {
            this.sipAddresses = new ArrayList();
        }
        this.sipAddresses.add(sipAddress);
    }

    public List<PostalAddress> getPostalAddresses() {
        return this.postalAddresses;
    }

    public void addPostalAddress(PostalAddress postalAddress) {
        if (postalAddress == null) {
            LogUtil.error(TAG, "addPostalAddress method failed, the parameter postalAddress cannot be null");
            return;
        }
        if (this.postalAddresses == null) {
            this.postalAddresses = new ArrayList();
        }
        this.postalAddresses.add(postalAddress);
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setIdentity(Identity identity2) {
        this.identity = identity2;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    public void addGroup(Group group) {
        if (group == null) {
            LogUtil.error(TAG, "addGroup method failed, the parameter group cannot be null");
            return;
        }
        if (this.groups == null) {
            this.groups = new ArrayList();
        }
        this.groups.add(group);
    }

    public Note getNote() {
        return this.note;
    }

    public void setNote(Note note2) {
        this.note = note2;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    public void addEvent(Event event) {
        if (event == null) {
            LogUtil.error(TAG, "addEvent method failed, the parameter event cannot be null");
            return;
        }
        if (this.events == null) {
            this.events = new ArrayList();
        }
        this.events.add(event);
    }

    public List<Website> getWebsites() {
        return this.websites;
    }

    public void addWebsite(Website website) {
        if (website == null) {
            LogUtil.error(TAG, "addWebsite method failed, the parameter website cannot be null");
            return;
        }
        if (this.websites == null) {
            this.websites = new ArrayList();
        }
        this.websites.add(website);
    }

    public List<Relation> getRelations() {
        return this.relations;
    }

    public void addRelation(Relation relation) {
        if (relation == null) {
            LogUtil.error(TAG, "addRelation method failed, the parameter relation cannot be null");
            return;
        }
        if (this.relations == null) {
            this.relations = new ArrayList();
        }
        this.relations.add(relation);
    }

    public void setContactAttributes(ContactAttributes contactAttributes) {
        this.attributes = contactAttributes;
    }

    public List<HiCallDevice> getHiCallDevices() {
        return this.hiCallDevices;
    }

    public void addHiCallDevice(HiCallDevice hiCallDevice) {
        if (hiCallDevice == null) {
            LogUtil.error(TAG, "addHiCallDevice method failed, the parameter hiCallDevice cannot be null");
            return;
        }
        if (this.hiCallDevices == null) {
            this.hiCallDevices = new ArrayList();
        }
        this.hiCallDevices.add(hiCallDevice);
    }

    public Portrait getCamCard() {
        return this.camCard;
    }

    public void setCamCard(Portrait portrait2) {
        this.camCard = portrait2;
    }
}
