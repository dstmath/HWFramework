package gov.nist.javax.sip.address;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import java.util.ListIterator;

public class NetObjectList extends GenericObjectList {
    private static final long serialVersionUID = -1551780600806959023L;

    public NetObjectList(String lname) {
        super(lname);
    }

    public NetObjectList(String lname, Class<?> cname) {
        super(lname, (Class) cname);
    }

    public void add(NetObject obj) {
        super.add(obj);
    }

    public void concatenate(NetObjectList net_obj_list) {
        super.concatenate(net_obj_list);
    }

    public GenericObject first() {
        return (NetObject) super.first();
    }

    public GenericObject next() {
        return (NetObject) super.next();
    }

    public GenericObject next(ListIterator li) {
        return (NetObject) super.next(li);
    }

    public void setMyClass(Class cl) {
        super.setMyClass(cl);
    }

    public String debugDump(int indent) {
        return super.debugDump(indent);
    }

    public String toString() {
        return encode();
    }
}
