package org.bouncycastle.cert.dane.fetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.bouncycastle.cert.dane.DANEEntry;
import org.bouncycastle.cert.dane.DANEEntryFetcher;
import org.bouncycastle.cert.dane.DANEEntryFetcherFactory;
import org.bouncycastle.cert.dane.DANEException;

public class JndiDANEFetcherFactory implements DANEEntryFetcherFactory {
    private static final String DANE_TYPE = "53";
    private List dnsServerList = new ArrayList();
    private boolean isAuthoritative;

    /* access modifiers changed from: private */
    public void addEntries(List list, String str, Attribute attribute) throws NamingException, DANEException {
        for (int i = 0; i != attribute.size(); i++) {
            byte[] bArr = (byte[]) attribute.get(i);
            if (DANEEntry.isValidCertificate(bArr)) {
                try {
                    list.add(new DANEEntry(str, bArr));
                } catch (IOException e) {
                    throw new DANEException("Exception parsing entry: " + e.getMessage(), e);
                }
            }
        }
    }

    public DANEEntryFetcher build(final String str) {
        final Hashtable hashtable = new Hashtable();
        hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        hashtable.put("java.naming.authoritative", this.isAuthoritative ? "true" : "false");
        if (this.dnsServerList.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            Iterator it = this.dnsServerList.iterator();
            while (it.hasNext()) {
                if (stringBuffer.length() > 0) {
                    stringBuffer.append(" ");
                }
                stringBuffer.append("dns://" + it.next());
            }
            hashtable.put("java.naming.provider.url", stringBuffer.toString());
        }
        return new DANEEntryFetcher() {
            public List getEntries() throws DANEException {
                ArrayList arrayList = new ArrayList();
                try {
                    InitialDirContext initialDirContext = new InitialDirContext(hashtable);
                    if (str.indexOf("_smimecert.") > 0) {
                        Attribute attribute = initialDirContext.getAttributes(str, new String[]{JndiDANEFetcherFactory.DANE_TYPE}).get(JndiDANEFetcherFactory.DANE_TYPE);
                        if (attribute != null) {
                            JndiDANEFetcherFactory.this.addEntries(arrayList, str, attribute);
                            return arrayList;
                        }
                    } else {
                        NamingEnumeration listBindings = initialDirContext.listBindings("_smimecert." + str);
                        while (listBindings.hasMore()) {
                            DirContext dirContext = (DirContext) ((Binding) listBindings.next()).getObject();
                            Attribute attribute2 = initialDirContext.getAttributes(dirContext.getNameInNamespace().substring(1, dirContext.getNameInNamespace().length() - 1), new String[]{JndiDANEFetcherFactory.DANE_TYPE}).get(JndiDANEFetcherFactory.DANE_TYPE);
                            if (attribute2 != null) {
                                String nameInNamespace = dirContext.getNameInNamespace();
                                JndiDANEFetcherFactory.this.addEntries(arrayList, nameInNamespace.substring(1, nameInNamespace.length() - 1), attribute2);
                            }
                        }
                    }
                    return arrayList;
                } catch (NamingException e) {
                    throw new DANEException("Exception dealing with DNS: " + e.getMessage(), e);
                }
            }
        };
    }

    public JndiDANEFetcherFactory setAuthoritative(boolean z) {
        this.isAuthoritative = z;
        return this;
    }

    public JndiDANEFetcherFactory usingDNSServer(String str) {
        this.dnsServerList.add(str);
        return this;
    }
}
