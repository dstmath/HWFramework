package ohos.com.sun.xml.internal.stream;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.global.icu.impl.PatternTokenizer;

public class XMLEntityStorage {
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    private static char[] gAfterEscaping1 = new char[128];
    private static char[] gAfterEscaping2 = new char[128];
    private static String gEscapedUserDir;
    private static char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static boolean[] gNeedEscaping = new boolean[128];
    private static String gUserDir;
    protected Entity.ScannedEntity fCurrentEntity;
    protected Map<String, Entity> fEntities = new HashMap();
    private XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected boolean fInExternalSubset = false;
    protected PropertyManager fPropertyManager;
    protected boolean fWarnDuplicateEntityDef;

    public XMLEntityStorage(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
    }

    public XMLEntityStorage(XMLEntityManager xMLEntityManager) {
        this.fEntityManager = xMLEntityManager;
    }

    public void reset(PropertyManager propertyManager) {
        this.fErrorReporter = (XMLErrorReporter) propertyManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public void reset() {
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fWarnDuplicateEntityDef = xMLComponentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntities.clear();
        this.fCurrentEntity = null;
    }

    public Entity getEntity(String str) {
        return this.fEntities.get(str);
    }

    public boolean hasEntities() {
        return this.fEntities != null;
    }

    public int getEntitySize() {
        return this.fEntities.size();
    }

    public Enumeration getEntityKeys() {
        return Collections.enumeration(this.fEntities.keySet());
    }

    public void addInternalEntity(String str, String str2) {
        if (!this.fEntities.containsKey(str)) {
            this.fEntities.put(str, new Entity.InternalEntity(str, str2, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public void addExternalEntity(String str, String str2, String str3, String str4) {
        Entity.ScannedEntity scannedEntity;
        if (!this.fEntities.containsKey(str)) {
            if (!(str4 != null || (scannedEntity = this.fCurrentEntity) == null || scannedEntity.entityLocation == null)) {
                str4 = this.fCurrentEntity.entityLocation.getExpandedSystemId();
            }
            this.fCurrentEntity = this.fEntityManager.getCurrentEntity();
            this.fEntities.put(str, new Entity.ExternalEntity(str, new XMLResourceIdentifierImpl(str2, str3, str4, expandSystemId(str3, str4)), null, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public boolean isExternalEntity(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isExternal();
    }

    public boolean isEntityDeclInExternalSubset(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isEntityDeclInExternalSubset();
    }

    public void addUnparsedEntity(String str, String str2, String str3, String str4, String str5) {
        this.fCurrentEntity = this.fEntityManager.getCurrentEntity();
        if (!this.fEntities.containsKey(str)) {
            this.fEntities.put(str, new Entity.ExternalEntity(str, new XMLResourceIdentifierImpl(str2, str3, str4, null), str5, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public boolean isUnparsedEntity(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isUnparsed();
    }

    public boolean isDeclaredEntity(String str) {
        return this.fEntities.get(str) != null;
    }

    public static String expandSystemId(String str) {
        return expandSystemId(str, null);
    }

    static {
        for (int i = 0; i <= 31; i++) {
            gNeedEscaping[i] = true;
            char[] cArr = gAfterEscaping1;
            char[] cArr2 = gHexChs;
            cArr[i] = cArr2[i >> 4];
            gAfterEscaping2[i] = cArr2[i & 15];
        }
        gNeedEscaping[127] = true;
        gAfterEscaping1[127] = '7';
        gAfterEscaping2[127] = 'F';
        char[] cArr3 = {' ', '<', '>', '#', '%', '\"', '{', '}', '|', PatternTokenizer.BACK_SLASH, '^', '~', '[', ']', '`'};
        for (char c : cArr3) {
            gNeedEscaping[c] = true;
            char[] cArr4 = gAfterEscaping1;
            char[] cArr5 = gHexChs;
            cArr4[c] = cArr5[c >> 4];
            gAfterEscaping2[c] = cArr5[c & 15];
        }
    }

    private static synchronized String getUserDir() {
        char charAt;
        char upperCase;
        synchronized (XMLEntityStorage.class) {
            String str = "";
            try {
                str = SecuritySupport.getSystemProperty("user.dir");
            } catch (SecurityException unused) {
            }
            if (str.length() == 0) {
                return "";
            }
            if (str.equals(gUserDir)) {
                return gEscapedUserDir;
            }
            gUserDir = str;
            String replace = str.replace(File.separatorChar, '/');
            int length = replace.length();
            StringBuffer stringBuffer = new StringBuffer(length * 3);
            if (length >= 2 && replace.charAt(1) == ':' && (upperCase = Character.toUpperCase(replace.charAt(0))) >= 'A' && upperCase <= 'Z') {
                stringBuffer.append('/');
            }
            int i = 0;
            while (i < length && (charAt = replace.charAt(i)) < 128) {
                if (gNeedEscaping[charAt]) {
                    stringBuffer.append('%');
                    stringBuffer.append(gAfterEscaping1[charAt]);
                    stringBuffer.append(gAfterEscaping2[charAt]);
                } else {
                    stringBuffer.append((char) charAt);
                }
                i++;
            }
            if (i < length) {
                try {
                    byte[] bytes = replace.substring(i).getBytes("UTF-8");
                    for (byte b : bytes) {
                        if (b < 0) {
                            int i2 = b + 256;
                            stringBuffer.append('%');
                            stringBuffer.append(gHexChs[i2 >> 4]);
                            stringBuffer.append(gHexChs[i2 & 15]);
                        } else if (gNeedEscaping[b]) {
                            stringBuffer.append('%');
                            stringBuffer.append(gAfterEscaping1[b]);
                            stringBuffer.append(gAfterEscaping2[b]);
                        } else {
                            stringBuffer.append((char) b);
                        }
                    }
                } catch (UnsupportedEncodingException unused2) {
                    return replace;
                }
            }
            if (!replace.endsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                stringBuffer.append('/');
            }
            gEscapedUserDir = stringBuffer.toString();
            return gEscapedUserDir;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x008b A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008c  */
    public static String expandSystemId(String str, String str2) {
        URI uri;
        URI uri2;
        if (str == null || str.length() == 0) {
            return str;
        }
        try {
            new URI(str);
            return str;
        } catch (URI.MalformedURIException unused) {
            String fixURI = fixURI(str);
            URI uri3 = null;
            if (str2 != null) {
                try {
                    if (str2.length() != 0 && !str2.equals(str)) {
                        try {
                            uri2 = new URI(fixURI(str2));
                        } catch (URI.MalformedURIException unused2) {
                            if (str2.indexOf(58) != -1) {
                                uri2 = new URI(AsrConstants.ASR_SRC_FILE, "", fixURI(str2), null, null);
                            } else {
                                String userDir = getUserDir();
                                uri = new URI(AsrConstants.ASR_SRC_FILE, "", userDir + fixURI(str2), null, null);
                            }
                        }
                        uri = uri2;
                        uri3 = new URI(uri, fixURI);
                        if (uri3 == null) {
                            return str;
                        }
                        return uri3.toString();
                    }
                } catch (Exception unused3) {
                    if (uri3 == null) {
                    }
                }
            }
            uri = new URI(AsrConstants.ASR_SRC_FILE, "", getUserDir(), null, null);
            uri3 = new URI(uri, fixURI);
            if (uri3 == null) {
            }
        }
    }

    protected static String fixURI(String str) {
        String replace = str.replace(File.separatorChar, '/');
        if (replace.length() < 2) {
            return replace;
        }
        char charAt = replace.charAt(1);
        if (charAt == ':') {
            char upperCase = Character.toUpperCase(replace.charAt(0));
            if (upperCase < 'A' || upperCase > 'Z') {
                return replace;
            }
            return PsuedoNames.PSEUDONAME_ROOT + replace;
        } else if (charAt != '/' || replace.charAt(0) != '/') {
            return replace;
        } else {
            return "file:" + replace;
        }
    }

    public void startExternalSubset() {
        this.fInExternalSubset = true;
    }

    public void endExternalSubset() {
        this.fInExternalSubset = false;
    }
}
