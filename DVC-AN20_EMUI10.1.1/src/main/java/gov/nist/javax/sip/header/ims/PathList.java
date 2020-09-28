package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PathList extends SIPHeaderList<Path> {
    public PathList() {
        super(Path.class, "Path");
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        return new PathList().clonehlist(this.hlist);
    }
}
