package gov.nist.javax.sip.address;

import gov.nist.core.Debug;
import gov.nist.core.GenericObject;
import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParameterNames;
import java.text.ParseException;
import java.util.Iterator;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.header.HeaderFactory;
import org.ccil.cowan.tagsoup.XMLWriter;

public class SipUri extends GenericURI implements SipURI, SipURIExt {
    private static final long serialVersionUID = 7749781076218987044L;
    protected Authority authority;
    protected NameValueList qheaders = new NameValueList();
    protected TelephoneNumber telephoneSubscriber;
    protected NameValueList uriParms = new NameValueList();

    public SipUri() {
        this.scheme = "sip";
        this.qheaders.setSeparator(Separators.AND);
    }

    public void setScheme(String scheme) {
        if (scheme.compareToIgnoreCase("sip") == 0 || scheme.compareToIgnoreCase("sips") == 0) {
            this.scheme = scheme.toLowerCase();
            return;
        }
        throw new IllegalArgumentException("bad scheme " + scheme);
    }

    @Override // gov.nist.javax.sip.address.GenericURI, javax.sip.address.URI
    public String getScheme() {
        return this.scheme;
    }

    public void clearUriParms() {
        this.uriParms = new NameValueList();
    }

    public void clearPassword() {
        UserInfo userInfo;
        Authority authority2 = this.authority;
        if (authority2 != null && (userInfo = authority2.getUserInfo()) != null) {
            userInfo.clearPassword();
        }
    }

    public Authority getAuthority() {
        return this.authority;
    }

    public void clearQheaders() {
        this.qheaders = new NameValueList();
    }

    @Override // gov.nist.javax.sip.address.GenericURI, gov.nist.javax.sip.address.NetObject, gov.nist.core.GenericObject
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (!(that instanceof SipURI)) {
            return false;
        }
        SipURI b = (SipURI) that;
        if (isSecure() ^ b.isSecure()) {
            return false;
        }
        if ((getUser() == null) ^ (b.getUser() == null)) {
            return false;
        }
        if ((getUserPassword() == null) ^ (b.getUserPassword() == null)) {
            return false;
        }
        if (getUser() != null && !RFC2396UrlDecoder.decode(getUser()).equals(RFC2396UrlDecoder.decode(b.getUser()))) {
            return false;
        }
        if (getUserPassword() != null && !RFC2396UrlDecoder.decode(getUserPassword()).equals(RFC2396UrlDecoder.decode(b.getUserPassword()))) {
            return false;
        }
        if ((getHost() == null) ^ (b.getHost() == null)) {
            return false;
        }
        if ((getHost() != null && !getHost().equalsIgnoreCase(b.getHost())) || getPort() != b.getPort()) {
            return false;
        }
        Iterator i = getParameterNames();
        while (i.hasNext()) {
            String pname = (String) i.next();
            String p1 = getParameter(pname);
            String p2 = b.getParameter(pname);
            if (p1 != null && p2 != null && !RFC2396UrlDecoder.decode(p1).equalsIgnoreCase(RFC2396UrlDecoder.decode(p2))) {
                return false;
            }
        }
        if ((getTransportParam() == null) ^ (b.getTransportParam() == null)) {
            return false;
        }
        if ((getUserParam() == null) ^ (b.getUserParam() == null)) {
            return false;
        }
        if ((getTTLParam() == -1) ^ (b.getTTLParam() == -1)) {
            return false;
        }
        if ((getMethodParam() == null) ^ (b.getMethodParam() == null)) {
            return false;
        }
        if ((getMAddrParam() == null) ^ (b.getMAddrParam() == null)) {
            return false;
        }
        if (getHeaderNames().hasNext() && !b.getHeaderNames().hasNext()) {
            return false;
        }
        if (!getHeaderNames().hasNext() && b.getHeaderNames().hasNext()) {
            return false;
        }
        if (getHeaderNames().hasNext() && b.getHeaderNames().hasNext()) {
            try {
                HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
                Iterator i2 = getHeaderNames();
                while (i2.hasNext()) {
                    String hname = (String) i2.next();
                    String h1 = getHeader(hname);
                    String h2 = b.getHeader(hname);
                    if (h1 == null && h2 != null) {
                        return false;
                    }
                    if (h2 == null && h1 != null) {
                        return false;
                    }
                    if (h1 != null || h2 != null) {
                        try {
                            if (!headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h1)).equals(headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h2)))) {
                                return false;
                            }
                        } catch (ParseException e) {
                            Debug.logError("Cannot parse one of the header of the sip uris to compare " + this + Separators.SP + b, e);
                            return false;
                        }
                    }
                }
            } catch (PeerUnavailableException e2) {
                Debug.logError("Cannot get the header factory to parse the header of the sip uris to compare", e2);
                return false;
            }
        }
        return true;
    }

    @Override // gov.nist.javax.sip.address.GenericURI, gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.javax.sip.address.GenericURI, gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.scheme);
        buffer.append(Separators.COLON);
        Authority authority2 = this.authority;
        if (authority2 != null) {
            authority2.encode(buffer);
        }
        if (!this.uriParms.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.uriParms.encode(buffer);
        }
        if (!this.qheaders.isEmpty()) {
            buffer.append(Separators.QUESTION);
            this.qheaders.encode(buffer);
        }
        return buffer;
    }

    @Override // gov.nist.javax.sip.address.GenericURI, gov.nist.javax.sip.address.NetObject, javax.sip.address.URI
    public String toString() {
        return encode();
    }

    @Override // javax.sip.address.SipURI
    public String getUserAtHost() {
        StringBuffer s;
        String user = "";
        if (this.authority.getUserInfo() != null) {
            user = this.authority.getUserInfo().getUser();
        }
        String host = this.authority.getHost().encode();
        if (user.equals("")) {
            s = new StringBuffer();
        } else {
            s = new StringBuffer(user).append(Separators.AT);
        }
        s.append(host);
        return s.toString();
    }

    @Override // javax.sip.address.SipURI
    public String getUserAtHostPort() {
        StringBuffer s;
        String user = "";
        if (this.authority.getUserInfo() != null) {
            user = this.authority.getUserInfo().getUser();
        }
        String host = this.authority.getHost().encode();
        int port = this.authority.getPort();
        if (user.equals("")) {
            s = new StringBuffer();
        } else {
            s = new StringBuffer(user).append(Separators.AT);
        }
        if (port != -1) {
            s.append(host);
            s.append(Separators.COLON);
            s.append(port);
            return s.toString();
        }
        s.append(host);
        return s.toString();
    }

    public Object getParm(String parmname) {
        return this.uriParms.getValue(parmname);
    }

    public String getMethod() {
        return (String) getParm(XMLWriter.METHOD);
    }

    public NameValueList getParameters() {
        return this.uriParms;
    }

    public void removeParameters() {
        this.uriParms = new NameValueList();
    }

    public NameValueList getQheaders() {
        return this.qheaders;
    }

    @Override // javax.sip.address.SipURI
    public String getUserType() {
        return (String) this.uriParms.getValue("user");
    }

    @Override // javax.sip.address.SipURI
    public String getUserPassword() {
        Authority authority2 = this.authority;
        if (authority2 == null) {
            return null;
        }
        return authority2.getPassword();
    }

    @Override // javax.sip.address.SipURI
    public void setUserPassword(String password) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setPassword(password);
    }

    public TelephoneNumber getTelephoneSubscriber() {
        if (this.telephoneSubscriber == null) {
            this.telephoneSubscriber = new TelephoneNumber();
        }
        return this.telephoneSubscriber;
    }

    public HostPort getHostPort() {
        Authority authority2 = this.authority;
        if (authority2 == null || authority2.getHost() == null) {
            return null;
        }
        return this.authority.getHostPort();
    }

    @Override // javax.sip.address.SipURI
    public int getPort() {
        HostPort hp = getHostPort();
        if (hp == null) {
            return -1;
        }
        return hp.getPort();
    }

    @Override // javax.sip.address.SipURI
    public String getHost() {
        Authority authority2 = this.authority;
        if (authority2 == null || authority2.getHost() == null) {
            return null;
        }
        return this.authority.getHost().encode();
    }

    public boolean isUserTelephoneSubscriber() {
        String usrtype = (String) this.uriParms.getValue("user");
        if (usrtype == null) {
            return false;
        }
        return usrtype.equalsIgnoreCase("phone");
    }

    public void removeTTL() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            nameValueList.delete("ttl");
        }
    }

    public void removeMAddr() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            nameValueList.delete("maddr");
        }
    }

    public void removeTransport() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            nameValueList.delete(ParameterNames.TRANSPORT);
        }
    }

    @Override // gov.nist.javax.sip.address.SipURIExt
    public void removeHeader(String name) {
        NameValueList nameValueList = this.qheaders;
        if (nameValueList != null) {
            nameValueList.delete(name);
        }
    }

    @Override // gov.nist.javax.sip.address.SipURIExt
    public void removeHeaders() {
        this.qheaders = new NameValueList();
    }

    @Override // javax.sip.address.SipURI
    public void removeUserType() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            nameValueList.delete("user");
        }
    }

    public void removePort() {
        this.authority.removePort();
    }

    public void removeMethod() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            nameValueList.delete(XMLWriter.METHOD);
        }
    }

    @Override // javax.sip.address.SipURI
    public void setUser(String uname) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setUser(uname);
    }

    public void removeUser() {
        this.authority.removeUserInfo();
    }

    public void setDefaultParm(String name, Object value) {
        if (this.uriParms.getValue(name) == null) {
            this.uriParms.set(new NameValue(name, value));
        }
    }

    public void setAuthority(Authority authority2) {
        this.authority = authority2;
    }

    public void setHost(Host h) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setHost(h);
    }

    public void setUriParms(NameValueList parms) {
        this.uriParms = parms;
    }

    public void setUriParm(String name, Object value) {
        this.uriParms.set(new NameValue(name, value));
    }

    public void setQheaders(NameValueList parms) {
        this.qheaders = parms;
    }

    public void setMAddr(String mAddr) {
        NameValue nameValue = this.uriParms.getNameValue("maddr");
        Host host = new Host();
        host.setAddress(mAddr);
        if (nameValue != null) {
            nameValue.setValueAsObject(host);
            return;
        }
        this.uriParms.set(new NameValue("maddr", host));
    }

    @Override // javax.sip.address.SipURI
    public void setUserParam(String usertype) {
        this.uriParms.set("user", usertype);
    }

    public void setMethod(String method) {
        this.uriParms.set(XMLWriter.METHOD, method);
    }

    public void setIsdnSubAddress(String isdnSubAddress) {
        if (this.telephoneSubscriber == null) {
            this.telephoneSubscriber = new TelephoneNumber();
        }
        this.telephoneSubscriber.setIsdnSubaddress(isdnSubAddress);
    }

    public void setTelephoneSubscriber(TelephoneNumber tel) {
        this.telephoneSubscriber = tel;
    }

    @Override // javax.sip.address.SipURI
    public void setPort(int p) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setPort(p);
    }

    public boolean hasParameter(String name) {
        return this.uriParms.getValue(name) != null;
    }

    public void setQHeader(NameValue nameValue) {
        this.qheaders.set(nameValue);
    }

    public void setUriParameter(NameValue nameValue) {
        this.uriParms.set(nameValue);
    }

    @Override // javax.sip.address.SipURI
    public boolean hasTransport() {
        return hasParameter(ParameterNames.TRANSPORT);
    }

    @Override // javax.sip.header.Parameters
    public void removeParameter(String name) {
        this.uriParms.delete(name);
    }

    public void setHostPort(HostPort hostPort) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setHostPort(hostPort);
    }

    @Override // java.lang.Object, javax.sip.address.URI, gov.nist.core.GenericObject
    public Object clone() {
        SipUri retval = (SipUri) super.clone();
        Authority authority2 = this.authority;
        if (authority2 != null) {
            retval.authority = (Authority) authority2.clone();
        }
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            retval.uriParms = (NameValueList) nameValueList.clone();
        }
        NameValueList nameValueList2 = this.qheaders;
        if (nameValueList2 != null) {
            retval.qheaders = (NameValueList) nameValueList2.clone();
        }
        TelephoneNumber telephoneNumber = this.telephoneSubscriber;
        if (telephoneNumber != null) {
            retval.telephoneSubscriber = (TelephoneNumber) telephoneNumber.clone();
        }
        return retval;
    }

    @Override // javax.sip.address.SipURI
    public String getHeader(String name) {
        if (this.qheaders.getValue(name) != null) {
            return this.qheaders.getValue(name).toString();
        }
        return null;
    }

    @Override // javax.sip.address.SipURI
    public Iterator<String> getHeaderNames() {
        return this.qheaders.getNames();
    }

    @Override // javax.sip.address.SipURI
    public String getLrParam() {
        if (hasParameter("lr")) {
            return "true";
        }
        return null;
    }

    @Override // javax.sip.address.SipURI
    public String getMAddrParam() {
        NameValue maddr = this.uriParms.getNameValue("maddr");
        if (maddr == null) {
            return null;
        }
        return (String) maddr.getValueAsObject();
    }

    @Override // javax.sip.address.SipURI
    public String getMethodParam() {
        return getParameter(XMLWriter.METHOD);
    }

    @Override // javax.sip.header.Parameters
    public String getParameter(String name) {
        Object val = this.uriParms.getValue(name);
        if (val == null) {
            return null;
        }
        if (val instanceof GenericObject) {
            return ((GenericObject) val).encode();
        }
        return val.toString();
    }

    @Override // javax.sip.header.Parameters
    public Iterator<String> getParameterNames() {
        return this.uriParms.getNames();
    }

    @Override // javax.sip.address.SipURI
    public int getTTLParam() {
        Integer ttl = (Integer) this.uriParms.getValue("ttl");
        if (ttl != null) {
            return ttl.intValue();
        }
        return -1;
    }

    @Override // javax.sip.address.SipURI
    public String getTransportParam() {
        NameValueList nameValueList = this.uriParms;
        if (nameValueList != null) {
            return (String) nameValueList.getValue(ParameterNames.TRANSPORT);
        }
        return null;
    }

    @Override // javax.sip.address.SipURI
    public String getUser() {
        return this.authority.getUser();
    }

    @Override // javax.sip.address.SipURI
    public boolean isSecure() {
        return getScheme().equalsIgnoreCase("sips");
    }

    @Override // gov.nist.javax.sip.address.GenericURI, javax.sip.address.URI
    public boolean isSipURI() {
        return true;
    }

    @Override // javax.sip.address.SipURI
    public void setHeader(String name, String value) {
        this.qheaders.set(new NameValue(name, value));
    }

    @Override // javax.sip.address.SipURI
    public void setHost(String host) throws ParseException {
        setHost(new Host(host));
    }

    @Override // javax.sip.address.SipURI
    public void setLrParam() {
        this.uriParms.set("lr", null);
    }

    @Override // javax.sip.address.SipURI
    public void setMAddrParam(String maddr) throws ParseException {
        if (maddr != null) {
            setParameter("maddr", maddr);
            return;
        }
        throw new NullPointerException("bad maddr");
    }

    @Override // javax.sip.address.SipURI
    public void setMethodParam(String method) throws ParseException {
        setParameter(XMLWriter.METHOD, method);
    }

    @Override // javax.sip.header.Parameters
    public void setParameter(String name, String value) throws ParseException {
        if (name.equalsIgnoreCase("ttl")) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ParseException("bad parameter " + value, 0);
            }
        }
        this.uriParms.set(name, value);
    }

    @Override // javax.sip.address.SipURI
    public void setSecure(boolean secure) {
        if (secure) {
            this.scheme = "sips";
        } else {
            this.scheme = "sip";
        }
    }

    @Override // javax.sip.address.SipURI
    public void setTTLParam(int ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("Bad ttl value");
        } else if (this.uriParms != null) {
            this.uriParms.set(new NameValue("ttl", Integer.valueOf(ttl)));
        }
    }

    @Override // javax.sip.address.SipURI
    public void setTransportParam(String transport) throws ParseException {
        if (transport == null) {
            throw new NullPointerException("null arg");
        } else if (transport.compareToIgnoreCase(ListeningPoint.UDP) == 0 || transport.compareToIgnoreCase(ListeningPoint.TLS) == 0 || transport.compareToIgnoreCase(ListeningPoint.TCP) == 0 || transport.compareToIgnoreCase(ListeningPoint.SCTP) == 0) {
            this.uriParms.set(new NameValue(ParameterNames.TRANSPORT, transport.toLowerCase()));
        } else {
            throw new ParseException("bad transport " + transport, 0);
        }
    }

    @Override // javax.sip.address.SipURI
    public String getUserParam() {
        return getParameter("user");
    }

    @Override // javax.sip.address.SipURI
    public boolean hasLrParam() {
        return this.uriParms.getNameValue("lr") != null;
    }

    @Override // gov.nist.javax.sip.address.SipURIExt
    public boolean hasGrParam() {
        return this.uriParms.getNameValue("gr") != null;
    }

    @Override // gov.nist.javax.sip.address.SipURIExt
    public void setGrParam(String value) {
        this.uriParms.set("gr", value);
    }

    public String getGrParam() {
        return (String) this.uriParms.getValue("gr");
    }
}
