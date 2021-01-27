package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class ServiceRouteList extends SIPHeaderList<ServiceRoute> {
    private static final long serialVersionUID = -4264811439080938519L;

    public ServiceRouteList() {
        super(ServiceRoute.class, "Service-Route");
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        return new ServiceRouteList().clonehlist(this.hlist);
    }
}
