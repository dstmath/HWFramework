package tmsdk.common.module.aresengine;

public class ContactEntity extends TelephonyEntity {
    public static final int FLAG_SIMPLE_INFO = 1;
    public int contactId;
    public boolean enableForCalling;
    public boolean enableForSMS;
    public int entityId;
    public boolean isSimContact;

    public ContactEntity(ContactEntity contactEntity) {
        super(contactEntity);
        this.isSimContact = contactEntity.isSimContact;
        this.enableForCalling = contactEntity.enableForCalling;
        this.enableForSMS = contactEntity.enableForSMS;
        this.contactId = contactEntity.contactId;
        this.entityId = contactEntity.entityId;
    }
}
