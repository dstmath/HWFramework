package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;

public abstract class SIPHeaderList<HDR extends SIPHeader> extends SIPHeader implements List<HDR>, Header {
    private static boolean prettyEncode;
    protected List<HDR> hlist;
    private Class<HDR> myClass;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.header.SIPHeaderList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.header.SIPHeaderList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.header.SIPHeaderList.<clinit>():void");
    }

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
            add((SIPHeader) sipheader);
        }
    }

    public void concatenate(SIPHeaderList<HDR> other, boolean topFlag) throws IllegalArgumentException {
        if (topFlag) {
            addAll(0, other);
        } else {
            addAll(other);
        }
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.hlist.isEmpty()) {
            buffer.append(this.headerName).append(':').append(Separators.NEWLINE);
        } else if (this.headerName.equals(WWWAuthenticateHeader.NAME) || this.headerName.equals(ProxyAuthenticateHeader.NAME) || this.headerName.equals(AuthorizationHeader.NAME) || this.headerName.equals(ProxyAuthorizationHeader.NAME) || ((prettyEncode && (this.headerName.equals(ViaHeader.NAME) || this.headerName.equals(RouteHeader.NAME) || this.headerName.equals(RecordRouteHeader.NAME))) || getClass().equals(ExtensionHeaderList.class))) {
            ListIterator<HDR> li = this.hlist.listIterator();
            while (li.hasNext()) {
                ((SIPHeader) li.next()).encode(buffer);
            }
        } else {
            buffer.append(this.headerName).append(Separators.COLON).append(Separators.SP);
            encodeBody(buffer);
            buffer.append(Separators.NEWLINE);
        }
        return buffer;
    }

    public List<String> getHeadersAsEncodedStrings() {
        List<String> retval = new LinkedList();
        ListIterator<HDR> li = this.hlist.listIterator();
        while (li.hasNext()) {
            retval.add(((Header) li.next()).toString());
        }
        return retval;
    }

    public Header getFirst() {
        if (this.hlist == null || this.hlist.isEmpty()) {
            return null;
        }
        return (Header) this.hlist.get(0);
    }

    public Header getLast() {
        if (this.hlist == null || this.hlist.isEmpty()) {
            return null;
        }
        return (Header) this.hlist.get(this.hlist.size() - 1);
    }

    public Class<HDR> getMyClass() {
        return this.myClass;
    }

    public boolean isEmpty() {
        return this.hlist.isEmpty();
    }

    public ListIterator<HDR> listIterator() {
        return this.hlist.listIterator(0);
    }

    public List<HDR> getHeaderList() {
        return this.hlist;
    }

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
            this.hlist.remove(this.hlist.size() - 1);
        }
    }

    public boolean remove(HDR obj) {
        if (this.hlist.size() == 0) {
            return false;
        }
        return this.hlist.remove(obj);
    }

    protected void setMyClass(Class<HDR> cl) {
        this.myClass = cl;
    }

    public String debugDump(int indentation) {
        this.stringRepresentation = "";
        String indent = new Indentation(indentation).getIndentation();
        sprint(indent + getClass().getName());
        sprint(indent + "{");
        for (SIPHeader sipHeader : this.hlist) {
            sprint(indent + sipHeader.debugDump());
        }
        sprint(indent + "}");
        return this.stringRepresentation;
    }

    public String debugDump() {
        return debugDump(0);
    }

    public Object[] toArray() {
        return this.hlist.toArray();
    }

    public int indexOf(GenericObject gobj) {
        return this.hlist.indexOf(gobj);
    }

    public void add(int index, HDR sipHeader) throws IndexOutOfBoundsException {
        this.hlist.add(index, sipHeader);
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof SIPHeaderList)) {
            return false;
        }
        SIPHeaderList<SIPHeader> that = (SIPHeaderList) other;
        if (this.hlist == that.hlist) {
            return true;
        }
        if (this.hlist != null) {
            return this.hlist.equals(that.hlist);
        }
        if (!(that.hlist == null || that.hlist.size() == 0)) {
            z = false;
        }
        return z;
    }

    public boolean match(SIPHeaderList<?> template) {
        if (template == null) {
            return true;
        }
        if (!getClass().equals(template.getClass())) {
            return false;
        }
        SIPHeaderList<SIPHeader> that = template;
        if (this.hlist == template.hlist) {
            return true;
        }
        if (this.hlist == null) {
            return false;
        }
        for (SIPHeader sipHeader : template.hlist) {
            boolean found = false;
            Iterator<HDR> it1 = this.hlist.iterator();
            while (it1.hasNext() && !found) {
                found = ((SIPHeader) it1.next()).match(sipHeader);
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public Object clone() {
        try {
            SIPHeaderList<HDR> retval = (SIPHeaderList) getClass().getConstructor((Class[]) null).newInstance((Object[]) null);
            retval.headerName = this.headerName;
            retval.myClass = this.myClass;
            return retval.clonehlist(this.hlist);
        } catch (Exception ex) {
            throw new RuntimeException("Could not clone!", ex);
        }
    }

    protected final SIPHeaderList<HDR> clonehlist(List<HDR> hlistToClone) {
        if (hlistToClone != null) {
            for (HDR h : hlistToClone) {
                this.hlist.add((SIPHeader) h.clone());
            }
        }
        return this;
    }

    public int size() {
        return this.hlist.size();
    }

    public boolean isHeaderList() {
        return true;
    }

    protected String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        ListIterator<HDR> iterator = listIterator();
        while (true) {
            SIPHeader sipHeader = (SIPHeader) iterator.next();
            if (sipHeader == this) {
                break;
            }
            sipHeader.encodeBody(buffer);
            if (!iterator.hasNext()) {
                return buffer;
            }
            if (this.headerName.equals(PrivacyHeader.NAME)) {
                buffer.append(Separators.SEMICOLON);
            } else {
                buffer.append(Separators.COMMA);
            }
        }
        throw new RuntimeException("Unexpected circularity in SipHeaderList");
    }

    public boolean addAll(Collection<? extends HDR> collection) {
        return this.hlist.addAll(collection);
    }

    public boolean addAll(int index, Collection<? extends HDR> collection) {
        return this.hlist.addAll(index, collection);
    }

    public boolean containsAll(Collection<?> collection) {
        return this.hlist.containsAll(collection);
    }

    public void clear() {
        this.hlist.clear();
    }

    public boolean contains(Object header) {
        return this.hlist.contains(header);
    }

    public HDR get(int index) {
        return (SIPHeader) this.hlist.get(index);
    }

    public int indexOf(Object obj) {
        return this.hlist.indexOf(obj);
    }

    public Iterator<HDR> iterator() {
        return this.hlist.listIterator();
    }

    public int lastIndexOf(Object obj) {
        return this.hlist.lastIndexOf(obj);
    }

    public boolean remove(Object obj) {
        return this.hlist.remove(obj);
    }

    public HDR remove(int index) {
        return (SIPHeader) this.hlist.remove(index);
    }

    public boolean removeAll(Collection<?> collection) {
        return this.hlist.removeAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return this.hlist.retainAll(collection);
    }

    public List<HDR> subList(int index1, int index2) {
        return this.hlist.subList(index1, index2);
    }

    public int hashCode() {
        return this.headerName.hashCode();
    }

    public HDR set(int position, HDR sipHeader) {
        return (SIPHeader) this.hlist.set(position, sipHeader);
    }

    public static void setPrettyEncode(boolean flag) {
        prettyEncode = flag;
    }

    public <T> T[] toArray(T[] array) {
        return this.hlist.toArray(array);
    }
}
