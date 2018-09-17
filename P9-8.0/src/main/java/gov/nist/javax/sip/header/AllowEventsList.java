package gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class AllowEventsList extends SIPHeaderList<AllowEvents> {
    private static final long serialVersionUID = -684763195336212992L;

    public Object clone() {
        AllowEventsList retval = new AllowEventsList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AllowEventsList() {
        super(AllowEvents.class, "Allow-Events");
    }

    public ListIterator<String> getMethods() {
        ListIterator<AllowEvents> li = this.hlist.listIterator();
        LinkedList<String> ll = new LinkedList();
        while (li.hasNext()) {
            ll.add(((AllowEvents) li.next()).getEventType());
        }
        return ll.listIterator();
    }

    public void setMethods(List<String> methods) throws ParseException {
        ListIterator<String> it = methods.listIterator();
        while (it.hasNext()) {
            AllowEvents allowEvents = new AllowEvents();
            allowEvents.setEventType((String) it.next());
            add((SIPHeader) allowEvents);
        }
    }
}
