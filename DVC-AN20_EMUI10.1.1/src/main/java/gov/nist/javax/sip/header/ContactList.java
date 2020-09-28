package gov.nist.javax.sip.header;

public class ContactList extends SIPHeaderList<Contact> {
    private static final long serialVersionUID = 1224806837758986814L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        ContactList retval = new ContactList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ContactList() {
        super(Contact.class, "Contact");
    }
}
