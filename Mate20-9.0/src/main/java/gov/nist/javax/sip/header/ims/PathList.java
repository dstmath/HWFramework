package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PathList extends SIPHeaderList<Path> {
    public PathList() {
        super(Path.class, "Path");
    }

    public Object clone() {
        return new PathList().clonehlist(this.hlist);
    }
}
