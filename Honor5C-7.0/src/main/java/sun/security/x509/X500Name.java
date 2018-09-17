package sun.security.x509;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X500Name implements GeneralNameInterface, Principal {
    private static final int[] DNQUALIFIER_DATA = null;
    public static final ObjectIdentifier DNQUALIFIER_OID = null;
    private static final int[] DOMAIN_COMPONENT_DATA = null;
    public static final ObjectIdentifier DOMAIN_COMPONENT_OID = null;
    private static final int[] GENERATIONQUALIFIER_DATA = null;
    public static final ObjectIdentifier GENERATIONQUALIFIER_OID = null;
    private static final int[] GIVENNAME_DATA = null;
    public static final ObjectIdentifier GIVENNAME_OID = null;
    private static final int[] INITIALS_DATA = null;
    public static final ObjectIdentifier INITIALS_OID = null;
    private static final int[] SERIALNUMBER_DATA = null;
    public static final ObjectIdentifier SERIALNUMBER_OID = null;
    private static final int[] SURNAME_DATA = null;
    public static final ObjectIdentifier SURNAME_OID = null;
    private static final int[] commonName_data = null;
    public static final ObjectIdentifier commonName_oid = null;
    private static final int[] countryName_data = null;
    public static final ObjectIdentifier countryName_oid = null;
    private static final Map<ObjectIdentifier, ObjectIdentifier> internedOIDs = null;
    private static final int[] ipAddress_data = null;
    public static final ObjectIdentifier ipAddress_oid = null;
    private static final int[] localityName_data = null;
    public static final ObjectIdentifier localityName_oid = null;
    private static final int[] orgName_data = null;
    public static final ObjectIdentifier orgName_oid = null;
    private static final int[] orgUnitName_data = null;
    public static final ObjectIdentifier orgUnitName_oid = null;
    private static final Constructor principalConstructor = null;
    private static final Field principalField = null;
    private static final int[] stateName_data = null;
    public static final ObjectIdentifier stateName_oid = null;
    private static final int[] streetAddress_data = null;
    public static final ObjectIdentifier streetAddress_oid = null;
    private static final int[] title_data = null;
    public static final ObjectIdentifier title_oid = null;
    private static final int[] userid_data = null;
    public static final ObjectIdentifier userid_oid = null;
    private volatile List<AVA> allAvaList;
    private String canonicalDn;
    private String dn;
    private byte[] encoded;
    private RDN[] names;
    private volatile List<RDN> rdnList;
    private String rfc1779Dn;
    private String rfc2253Dn;
    private X500Principal x500Principal;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.X500Name.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.X500Name.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.X500Name.<clinit>():void");
    }

    public X500Name(String dname) throws IOException {
        this(dname, Collections.emptyMap());
    }

    public X500Name(String dname, Map<String, String> keywordMap) throws IOException {
        parseDN(dname, keywordMap);
    }

    public X500Name(String dname, String format) throws IOException {
        if (dname == null) {
            throw new NullPointerException("Name must not be null");
        } else if (format.equalsIgnoreCase(X500Principal.RFC2253)) {
            parseRFC2253DN(dname);
        } else if (format.equalsIgnoreCase("DEFAULT")) {
            parseDN(dname, Collections.emptyMap());
        } else {
            throw new IOException("Unsupported format " + format);
        }
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String country) throws IOException {
        this.names = new RDN[4];
        this.names[3] = new RDN(1);
        this.names[3].assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
        this.names[2] = new RDN(1);
        this.names[2].assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
        this.names[1] = new RDN(1);
        this.names[1].assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
        this.names[0] = new RDN(1);
        this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(country));
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String localityName, String stateName, String country) throws IOException {
        this.names = new RDN[6];
        this.names[5] = new RDN(1);
        this.names[5].assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
        this.names[4] = new RDN(1);
        this.names[4].assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
        this.names[3] = new RDN(1);
        this.names[3].assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
        this.names[2] = new RDN(1);
        this.names[2].assertion[0] = new AVA(localityName_oid, new DerValue(localityName));
        this.names[1] = new RDN(1);
        this.names[1].assertion[0] = new AVA(stateName_oid, new DerValue(stateName));
        this.names[0] = new RDN(1);
        this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(country));
    }

    public X500Name(RDN[] rdnArray) throws IOException {
        if (rdnArray == null) {
            this.names = new RDN[0];
            return;
        }
        this.names = (RDN[]) rdnArray.clone();
        for (RDN rdn : this.names) {
            if (rdn == null) {
                throw new IOException("Cannot create an X500Name");
            }
        }
    }

    public X500Name(DerValue value) throws IOException {
        this(value.toDerInputStream());
    }

    public X500Name(DerInputStream in) throws IOException {
        parseDER(in);
    }

    public X500Name(byte[] name) throws IOException {
        parseDER(new DerInputStream(name));
    }

    public List<RDN> rdns() {
        List<RDN> list = this.rdnList;
        if (list != null) {
            return list;
        }
        list = Collections.unmodifiableList(Arrays.asList(this.names));
        this.rdnList = list;
        return list;
    }

    public int size() {
        return this.names.length;
    }

    public List<AVA> allAvas() {
        List<AVA> list = this.allAvaList;
        if (list == null) {
            list = new ArrayList();
            for (RDN avas : this.names) {
                list.addAll(avas.avas());
            }
        }
        return list;
    }

    public int avaSize() {
        return allAvas().size();
    }

    public boolean isEmpty() {
        if (n == 0) {
            return true;
        }
        for (RDN rdn : this.names) {
            if (rdn.assertion.length != 0) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return getRFC2253CanonicalName().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof X500Name)) {
            return false;
        }
        X500Name other = (X500Name) obj;
        if (this.canonicalDn != null && other.canonicalDn != null) {
            return this.canonicalDn.equals(other.canonicalDn);
        }
        int n = this.names.length;
        if (n != other.names.length) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (this.names[i].assertion.length != other.names[i].assertion.length) {
                return false;
            }
        }
        return getRFC2253CanonicalName().equals(other.getRFC2253CanonicalName());
    }

    private String getString(DerValue attribute) throws IOException {
        if (attribute == null) {
            return null;
        }
        String value = attribute.getAsString();
        if (value != null) {
            return value;
        }
        throw new IOException("not a DER string encoding, " + attribute.tag);
    }

    public int getType() {
        return 4;
    }

    public String getCountry() throws IOException {
        return getString(findAttribute(countryName_oid));
    }

    public String getOrganization() throws IOException {
        return getString(findAttribute(orgName_oid));
    }

    public String getOrganizationalUnit() throws IOException {
        return getString(findAttribute(orgUnitName_oid));
    }

    public String getCommonName() throws IOException {
        return getString(findAttribute(commonName_oid));
    }

    public String getLocality() throws IOException {
        return getString(findAttribute(localityName_oid));
    }

    public String getState() throws IOException {
        return getString(findAttribute(stateName_oid));
    }

    public String getDomain() throws IOException {
        return getString(findAttribute(DOMAIN_COMPONENT_OID));
    }

    public String getDNQualifier() throws IOException {
        return getString(findAttribute(DNQUALIFIER_OID));
    }

    public String getSurname() throws IOException {
        return getString(findAttribute(SURNAME_OID));
    }

    public String getGivenName() throws IOException {
        return getString(findAttribute(GIVENNAME_OID));
    }

    public String getInitials() throws IOException {
        return getString(findAttribute(INITIALS_OID));
    }

    public String getGeneration() throws IOException {
        return getString(findAttribute(GENERATIONQUALIFIER_OID));
    }

    public String getIP() throws IOException {
        return getString(findAttribute(ipAddress_oid));
    }

    public String toString() {
        if (this.dn == null) {
            generateDN();
        }
        return this.dn;
    }

    public String getRFC1779Name() {
        return getRFC1779Name(Collections.emptyMap());
    }

    public String getRFC1779Name(Map<String, String> oidMap) throws IllegalArgumentException {
        if (!oidMap.isEmpty()) {
            return generateRFC1779DN(oidMap);
        }
        if (this.rfc1779Dn != null) {
            return this.rfc1779Dn;
        }
        this.rfc1779Dn = generateRFC1779DN(oidMap);
        return this.rfc1779Dn;
    }

    public String getRFC2253Name() {
        return getRFC2253Name(Collections.emptyMap());
    }

    public String getRFC2253Name(Map<String, String> oidMap) {
        if (!oidMap.isEmpty()) {
            return generateRFC2253DN(oidMap);
        }
        if (this.rfc2253Dn != null) {
            return this.rfc2253Dn;
        }
        this.rfc2253Dn = generateRFC2253DN(oidMap);
        return this.rfc2253Dn;
    }

    private String generateRFC2253DN(Map<String, String> oidMap) {
        if (this.names.length == 0) {
            return "";
        }
        StringBuilder fullname = new StringBuilder(48);
        for (int i = this.names.length - 1; i >= 0; i--) {
            if (i < this.names.length - 1) {
                fullname.append(',');
            }
            fullname.append(this.names[i].toRFC2253String((Map) oidMap));
        }
        return fullname.toString();
    }

    public String getRFC2253CanonicalName() {
        if (this.canonicalDn != null) {
            return this.canonicalDn;
        }
        if (this.names.length == 0) {
            this.canonicalDn = "";
            return this.canonicalDn;
        }
        StringBuilder fullname = new StringBuilder(48);
        for (int i = this.names.length - 1; i >= 0; i--) {
            if (i < this.names.length - 1) {
                fullname.append(',');
            }
            fullname.append(this.names[i].toRFC2253String(true));
        }
        this.canonicalDn = fullname.toString();
        return this.canonicalDn;
    }

    public String getName() {
        return toString();
    }

    private DerValue findAttribute(ObjectIdentifier attribute) {
        if (this.names != null) {
            for (RDN findAttribute : this.names) {
                DerValue value = findAttribute.findAttribute(attribute);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public DerValue findMostSpecificAttribute(ObjectIdentifier attribute) {
        if (this.names != null) {
            for (int i = this.names.length - 1; i >= 0; i--) {
                DerValue value = this.names[i].findAttribute(attribute);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private void parseDER(DerInputStream in) throws IOException {
        DerValue[] nameseq;
        byte[] derBytes = in.toByteArray();
        try {
            nameseq = in.getSequence(5);
        } catch (IOException e) {
            if (derBytes == null) {
                nameseq = null;
            } else {
                nameseq = new DerInputStream(new DerValue((byte) DerValue.tag_SequenceOf, derBytes).toByteArray()).getSequence(5);
            }
        }
        if (nameseq == null) {
            this.names = new RDN[0];
            return;
        }
        this.names = new RDN[nameseq.length];
        for (int i = 0; i < nameseq.length; i++) {
            this.names[i] = new RDN(nameseq[i]);
        }
    }

    @Deprecated
    public void emit(DerOutputStream out) throws IOException {
        encode(out);
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        for (RDN encode : this.names) {
            encode.encode(tmp);
        }
        out.write((byte) DerValue.tag_SequenceOf, tmp);
    }

    public byte[] getEncodedInternal() throws IOException {
        if (this.encoded == null) {
            DerOutputStream out = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            for (RDN encode : this.names) {
                encode.encode(tmp);
            }
            out.write((byte) DerValue.tag_SequenceOf, tmp);
            this.encoded = out.toByteArray();
        }
        return this.encoded;
    }

    public byte[] getEncoded() throws IOException {
        return (byte[]) getEncodedInternal().clone();
    }

    private void parseDN(String input, Map<String, String> keywordMap) throws IOException {
        if (input == null || input.length() == 0) {
            this.names = new RDN[0];
            return;
        }
        checkNoNewLinesNorTabsAtBeginningOfDN(input);
        List<RDN> dnVector = new ArrayList();
        int dnOffset = 0;
        int quoteCount = 0;
        String dnString = input;
        int searchOffset = 0;
        int nextComma = input.indexOf(44);
        int nextSemiColon = input.indexOf(59);
        while (true) {
            if (nextComma >= 0 || nextSemiColon >= 0) {
                int rdnEnd;
                if (nextSemiColon < 0) {
                    rdnEnd = nextComma;
                } else if (nextComma < 0) {
                    rdnEnd = nextSemiColon;
                } else {
                    rdnEnd = Math.min(nextComma, nextSemiColon);
                }
                quoteCount += countQuotes(input, searchOffset, rdnEnd);
                if (!(rdnEnd < 0 || quoteCount == 1 || escaped(rdnEnd, searchOffset, input))) {
                    dnVector.add(new RDN(input.substring(dnOffset, rdnEnd), (Map) keywordMap));
                    dnOffset = rdnEnd + 1;
                    quoteCount = 0;
                }
                searchOffset = rdnEnd + 1;
                nextComma = input.indexOf(44, searchOffset);
                nextSemiColon = input.indexOf(59, searchOffset);
            } else {
                dnVector.add(new RDN(input.substring(dnOffset), (Map) keywordMap));
                Collections.reverse(dnVector);
                this.names = (RDN[]) dnVector.toArray(new RDN[dnVector.size()]);
                return;
            }
        }
    }

    private void checkNoNewLinesNorTabsAtBeginningOfDN(String input) {
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == ' ') {
                i++;
            } else if (c == '\t' || c == '\n') {
                throw new IllegalArgumentException("DN cannot start with newline nor tab");
            } else {
                return;
            }
        }
    }

    private void parseRFC2253DN(String dnString) throws IOException {
        if (dnString.length() == 0) {
            this.names = new RDN[0];
            return;
        }
        List<RDN> dnVector = new ArrayList();
        int dnOffset = 0;
        int searchOffset = 0;
        int rdnEnd = dnString.indexOf(44);
        while (rdnEnd >= 0) {
            if (rdnEnd > 0 && !escaped(rdnEnd, searchOffset, dnString)) {
                dnVector.add(new RDN(dnString.substring(dnOffset, rdnEnd), X500Principal.RFC2253));
                dnOffset = rdnEnd + 1;
            }
            searchOffset = rdnEnd + 1;
            rdnEnd = dnString.indexOf(44, searchOffset);
        }
        dnVector.add(new RDN(dnString.substring(dnOffset), X500Principal.RFC2253));
        Collections.reverse(dnVector);
        this.names = (RDN[]) dnVector.toArray(new RDN[dnVector.size()]);
    }

    static int countQuotes(String string, int from, int to) {
        int count = 0;
        int escape = 0;
        for (int i = from; i < to; i++) {
            if (string.charAt(i) == '\"' && escape % 2 == 0) {
                count++;
            }
            escape = string.charAt(i) == '\\' ? escape + 1 : 0;
        }
        return count;
    }

    private static boolean escaped(int rdnEnd, int searchOffset, String dnString) {
        boolean z = true;
        if (rdnEnd == 1 && dnString.charAt(rdnEnd - 1) == '\\') {
            return true;
        }
        if (rdnEnd > 1 && dnString.charAt(rdnEnd - 1) == '\\' && dnString.charAt(rdnEnd - 2) != '\\') {
            return true;
        }
        if (rdnEnd <= 1 || dnString.charAt(rdnEnd - 1) != '\\' || dnString.charAt(rdnEnd - 2) != '\\') {
            return false;
        }
        int count = 0;
        for (rdnEnd--; rdnEnd >= searchOffset; rdnEnd--) {
            if (dnString.charAt(rdnEnd) == '\\') {
                count++;
            }
        }
        if (count % 2 == 0) {
            z = false;
        }
        return z;
    }

    private void generateDN() {
        if (this.names.length == 1) {
            this.dn = this.names[0].toString();
            return;
        }
        StringBuilder sb = new StringBuilder(48);
        if (this.names != null) {
            for (int i = this.names.length - 1; i >= 0; i--) {
                if (i != this.names.length - 1) {
                    sb.append(", ");
                }
                sb.append(this.names[i].toString());
            }
        }
        this.dn = sb.toString();
    }

    private String generateRFC1779DN(Map<String, String> oidMap) {
        if (this.names.length == 1) {
            return this.names[0].toRFC1779String(oidMap);
        }
        StringBuilder sb = new StringBuilder(48);
        if (this.names != null) {
            for (int i = this.names.length - 1; i >= 0; i--) {
                if (i != this.names.length - 1) {
                    sb.append(", ");
                }
                sb.append(this.names[i].toRFC1779String(oidMap));
            }
        }
        return sb.toString();
    }

    static ObjectIdentifier intern(ObjectIdentifier oid) {
        ObjectIdentifier interned = (ObjectIdentifier) internedOIDs.get(oid);
        if (interned != null) {
            return interned;
        }
        internedOIDs.put(oid, oid);
        return oid;
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 4) {
            return -1;
        }
        X500Name inputX500 = (X500Name) inputName;
        if (inputX500.equals(this)) {
            return 0;
        }
        if (inputX500.names.length == 0) {
            return 2;
        }
        if (this.names.length == 0) {
            return 1;
        }
        if (inputX500.isWithinSubtree(this)) {
            return 1;
        }
        if (isWithinSubtree(inputX500)) {
            return 2;
        }
        return 3;
    }

    private boolean isWithinSubtree(X500Name other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.names.length == 0) {
            return true;
        }
        if (this.names.length == 0 || this.names.length < other.names.length) {
            return false;
        }
        for (int i = 0; i < other.names.length; i++) {
            if (!this.names[i].equals(other.names[i])) {
                return false;
            }
        }
        return true;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        return this.names.length;
    }

    public X500Name commonAncestor(X500Name other) {
        if (other == null) {
            return null;
        }
        int otherLen = other.names.length;
        int thisLen = this.names.length;
        if (thisLen == 0 || otherLen == 0) {
            return null;
        }
        RDN[] ancestor;
        int minLen = thisLen < otherLen ? thisLen : otherLen;
        int i = 0;
        while (i < minLen) {
            int j;
            if (this.names[i].equals(other.names[i])) {
                i++;
            } else {
                if (i == 0) {
                    return null;
                }
                ancestor = new RDN[i];
                for (j = 0; j < i; j++) {
                    ancestor[j] = this.names[j];
                }
                return new X500Name(ancestor);
            }
        }
        ancestor = new RDN[i];
        for (j = 0; j < i; j++) {
            ancestor[j] = this.names[j];
        }
        try {
            return new X500Name(ancestor);
        } catch (IOException e) {
            return null;
        }
    }

    public X500Principal asX500Principal() {
        if (this.x500Principal == null) {
            try {
                this.x500Principal = (X500Principal) principalConstructor.newInstance(this);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }
        return this.x500Principal;
    }

    public static X500Name asX500Name(X500Principal p) {
        try {
            X500Name name = (X500Name) principalField.get(p);
            name.x500Principal = p;
            return name;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
