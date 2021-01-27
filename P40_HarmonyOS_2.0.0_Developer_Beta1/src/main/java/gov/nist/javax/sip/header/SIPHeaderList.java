package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.SIPHeader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.sip.header.Header;

public abstract class SIPHeaderList<HDR extends SIPHeader> extends SIPHeader implements List<HDR>, Header {
    private static boolean prettyEncode = false;
    protected List<HDR> hlist;
    private Class<HDR> myClass;

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: gov.nist.javax.sip.header.SIPHeaderList<HDR extends gov.nist.javax.sip.header.SIPHeader> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List
    public /* bridge */ /* synthetic */ void add(int i, Object obj) {
        add(i, (int) ((SIPHeader) obj));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: gov.nist.javax.sip.header.SIPHeaderList<HDR extends gov.nist.javax.sip.header.SIPHeader> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public /* bridge */ /* synthetic */ boolean add(Object obj) {
        return add((SIPHeaderList<HDR>) ((SIPHeader) obj));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: gov.nist.javax.sip.header.SIPHeaderList<HDR extends gov.nist.javax.sip.header.SIPHeader> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List
    public /* bridge */ /* synthetic */ Object set(int i, Object obj) {
        return set(i, (int) ((SIPHeader) obj));
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, javax.sip.header.Header
    public String getName() {
        return this.headerName;
    }

    private SIPHeaderList() {
        this.hlist = new LinkedList();
    }

    protected SIPHeaderList(Class<HDR> objclass, String hname) {
        this();
        this.headerName = hname;
        this.myClass = objclass;
    }

    public boolean add(HDR objectToAdd) {
        this.hlist.add(objectToAdd);
        return true;
    }

    public void addFirst(HDR obj) {
        this.hlist.add(0, obj);
    }

    public void add(HDR sipheader, boolean top) {
        if (top) {
            addFirst(sipheader);
        } else {
            add((SIPHeaderList<HDR>) sipheader);
        }
    }

    public void concatenate(SIPHeaderList<HDR> other, boolean topFlag) throws IllegalArgumentException {
        if (!topFlag) {
            addAll(other);
        } else {
            addAll(0, other);
        }
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        if (this.hlist.isEmpty()) {
            buffer.append(this.headerName);
            buffer.append(':');
            buffer.append(Separators.NEWLINE);
        } else if (this.headerName.equals("WWW-Authenticate") || this.headerName.equals("Proxy-Authenticate") || this.headerName.equals("Authorization") || this.headerName.equals("Proxy-Authorization") || ((prettyEncode && (this.headerName.equals("Via") || this.headerName.equals("Route") || this.headerName.equals("Record-Route"))) || getClass().equals(ExtensionHeaderList.class))) {
            ListIterator<HDR> li = this.hlist.listIterator();
            while (li.hasNext()) {
                li.next().encode(buffer);
            }
        } else {
            buffer.append(this.headerName);
            buffer.append(Separators.COLON);
            buffer.append(Separators.SP);
            encodeBody(buffer);
            buffer.append(Separators.NEWLINE);
        }
        return buffer;
    }

    public List<String> getHeadersAsEncodedStrings() {
        List<String> retval = new LinkedList<>();
        ListIterator<HDR> li = this.hlist.listIterator();
        while (li.hasNext()) {
            retval.add(li.next().toString());
        }
        return retval;
    }

    public Header getFirst() {
        List<HDR> list = this.hlist;
        if (list == null || list.isEmpty()) {
            return null;
        }
        return this.hlist.get(0);
    }

    public Header getLast() {
        List<HDR> list = this.hlist;
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<HDR> list2 = this.hlist;
        return list2.get(list2.size() - 1);
    }

    public Class<HDR> getMyClass() {
        return this.myClass;
    }

    @Override // java.util.List, java.util.Collection
    public boolean isEmpty() {
        return this.hlist.isEmpty();
    }

    @Override // java.util.List
    public ListIterator<HDR> listIterator() {
        return this.hlist.listIterator(0);
    }

    public List<HDR> getHeaderList() {
        return this.hlist;
    }

    @Override // java.util.List
    public ListIterator<HDR> listIterator(int position) {
        return this.hlist.listIterator(position);
    }

    public void removeFirst() {
        if (this.hlist.size() != 0) {
            this.hlist.remove(0);
        }
    }

    public void removeLast() {
        if (this.hlist.size() != 0) {
            List<HDR> list = this.hlist;
            list.remove(list.size() - 1);
        }
    }

    public boolean remove(HDR obj) {
        if (this.hlist.size() == 0) {
            return false;
        }
        return this.hlist.remove(obj);
    }

    /* access modifiers changed from: protected */
    public void setMyClass(Class<HDR> cl) {
        this.myClass = cl;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String debugDump(int indentation) {
        this.stringRepresentation = "";
        String indent = new Indentation(indentation).getIndentation();
        String className = getClass().getName();
        sprint(indent + className);
        sprint(indent + "{");
        Iterator<HDR> it = this.hlist.iterator();
        while (it.hasNext()) {
            sprint(indent + it.next().debugDump());
        }
        sprint(indent + "}");
        return this.stringRepresentation;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String debugDump() {
        return debugDump(0);
    }

    @Override // java.util.List, java.util.Collection
    public Object[] toArray() {
        return this.hlist.toArray();
    }

    public int indexOf(GenericObject gobj) {
        return this.hlist.indexOf(gobj);
    }

    public void add(int index, HDR sipHeader) throws IndexOutOfBoundsException {
        this.hlist.add(index, sipHeader);
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SIPHeaderList)) {
            return false;
        }
        List<HDR> list = this.hlist;
        List<HDR> list2 = ((SIPHeaderList) other).hlist;
        if (list == list2) {
            return true;
        }
        if (list != null) {
            return list.equals(list2);
        }
        if (list2 == null || list2.size() == 0) {
            return true;
        }
        return false;
    }

    public boolean match(SIPHeaderList<?> template) {
        if (template == null) {
            return true;
        }
        if (!getClass().equals(template.getClass())) {
            return false;
        }
        List<HDR> list = this.hlist;
        List<HDR> list2 = template.hlist;
        if (list == list2) {
            return true;
        }
        if (list == null) {
            return false;
        }
        for (HDR sipHeader : list2) {
            boolean found = false;
            Iterator<HDR> it1 = this.hlist.iterator();
            while (it1.hasNext() && !found) {
                found = it1.next().match(sipHeader);
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        try {
            SIPHeaderList<HDR> retval = (SIPHeaderList) getClass().getConstructor(null).newInstance(null);
            retval.headerName = this.headerName;
            retval.myClass = this.myClass;
            return retval.clonehlist(this.hlist);
        } catch (Exception ex) {
            throw new RuntimeException("Could not clone!", ex);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.util.List<HDR extends gov.nist.javax.sip.header.SIPHeader> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public final SIPHeaderList<HDR> clonehlist(List<HDR> hlistToClone) {
        if (hlistToClone != null) {
            for (HDR h : hlistToClone) {
                this.hlist.add((SIPHeader) h.clone());
            }
        }
        return this;
    }

    @Override // java.util.List, java.util.Collection
    public int size() {
        return this.hlist.size();
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public boolean isHeaderList() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        ListIterator<HDR> iterator = listIterator();
        while (true) {
            SIPHeader sipHeader = iterator.next();
            if (sipHeader != this) {
                sipHeader.encodeBody(buffer);
                if (!iterator.hasNext()) {
                    return buffer;
                }
                if (!this.headerName.equals("Privacy")) {
                    buffer.append(Separators.COMMA);
                } else {
                    buffer.append(Separators.SEMICOLON);
                }
            } else {
                throw new RuntimeException("Unexpected circularity in SipHeaderList");
            }
        }
    }

    @Override // java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends HDR> collection) {
        return this.hlist.addAll(collection);
    }

    @Override // java.util.List
    public boolean addAll(int index, Collection<? extends HDR> collection) {
        return this.hlist.addAll(index, collection);
    }

    @Override // java.util.List, java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        return this.hlist.containsAll(collection);
    }

    @Override // java.util.List, java.util.Collection
    public void clear() {
        this.hlist.clear();
    }

    @Override // java.util.List, java.util.Collection
    public boolean contains(Object header) {
        return this.hlist.contains(header);
    }

    @Override // java.util.List
    public HDR get(int index) {
        return this.hlist.get(index);
    }

    @Override // java.util.List
    public int indexOf(Object obj) {
        return this.hlist.indexOf(obj);
    }

    @Override // java.util.List, java.util.Collection, java.lang.Iterable
    public Iterator<HDR> iterator() {
        return this.hlist.listIterator();
    }

    @Override // java.util.List
    public int lastIndexOf(Object obj) {
        return this.hlist.lastIndexOf(obj);
    }

    @Override // java.util.List, java.util.Collection
    public boolean remove(Object obj) {
        return this.hlist.remove(obj);
    }

    @Override // java.util.List
    public HDR remove(int index) {
        return this.hlist.remove(index);
    }

    @Override // java.util.List, java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        return this.hlist.removeAll(collection);
    }

    @Override // java.util.List, java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        return this.hlist.retainAll(collection);
    }

    @Override // java.util.List
    public List<HDR> subList(int index1, int index2) {
        return this.hlist.subList(index1, index2);
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, java.lang.Object, javax.sip.header.Header
    public int hashCode() {
        return this.headerName.hashCode();
    }

    public HDR set(int position, HDR sipHeader) {
        return this.hlist.set(position, sipHeader);
    }

    public static void setPrettyEncode(boolean flag) {
        prettyEncode = flag;
    }

    @Override // java.util.List, java.util.Collection
    public <T> T[] toArray(T[] array) {
        return (T[]) this.hlist.toArray(array);
    }
}
