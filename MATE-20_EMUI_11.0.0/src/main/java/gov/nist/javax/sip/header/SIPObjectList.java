package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import java.util.Iterator;

public class SIPObjectList extends GenericObjectList {
    private static final long serialVersionUID = -3015154738977508905L;

    public SIPObjectList(String lname) {
        super(lname);
    }

    public SIPObjectList() {
    }

    @Override // gov.nist.core.GenericObjectList
    public void mergeObjects(GenericObjectList mergeList) {
        Iterator<GenericObject> it1 = listIterator();
        Iterator<GenericObject> it2 = mergeList.listIterator();
        while (it1.hasNext()) {
            GenericObject outerObj = it1.next();
            while (it2.hasNext()) {
                outerObj.merge(it2.next());
            }
        }
    }

    public void concatenate(SIPObjectList otherList) {
        super.concatenate((GenericObjectList) otherList);
    }

    public void concatenate(SIPObjectList otherList, boolean topFlag) {
        super.concatenate((GenericObjectList) otherList, topFlag);
    }

    @Override // gov.nist.core.GenericObjectList
    public GenericObject first() {
        return (SIPObject) super.first();
    }

    @Override // gov.nist.core.GenericObjectList
    public GenericObject next() {
        return (SIPObject) super.next();
    }

    @Override // gov.nist.core.GenericObjectList
    public String debugDump(int indent) {
        return super.debugDump(indent);
    }
}
