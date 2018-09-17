package gov.nist.javax.sip.header;

import java.util.ListIterator;

public class RouteList extends SIPHeaderList<Route> {
    private static final long serialVersionUID = 3407603519354809748L;

    public RouteList() {
        super(Route.class, "Route");
    }

    public Object clone() {
        RouteList retval = new RouteList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public String encode() {
        if (this.hlist.isEmpty()) {
            return "";
        }
        return super.encode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof RouteList)) {
            return false;
        }
        RouteList that = (RouteList) other;
        if (size() != that.size()) {
            return false;
        }
        ListIterator<Route> it = listIterator();
        ListIterator<Route> it1 = that.listIterator();
        while (it.hasNext()) {
            if (!((Route) it.next()).equals((Route) it1.next())) {
                return false;
            }
        }
        return true;
    }
}
