package org.apache.xml.serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.apache.xpath.compiler.OpCodes;

final class CharInfo {
    static final int ASCII_MAX = 128;
    public static final String HTML_ENTITIES_RESOURCE = null;
    private static final int LOW_ORDER_BITMASK = 31;
    private static final int SHIFT_PER_WORD = 5;
    static final char S_CARRIAGERETURN = '\r';
    static final char S_GT = '>';
    static final char S_HORIZONAL_TAB = '\t';
    static final char S_LINEFEED = '\n';
    static final char S_LINE_SEPARATOR = '\u2028';
    static final char S_LT = '<';
    static final char S_NEL = '\u0085';
    static final char S_QUOTE = '\"';
    static final char S_SPACE = ' ';
    public static final String XML_ENTITIES_RESOURCE = null;
    private static Hashtable m_getCharInfoCache;
    private final int[] array_of_bits;
    private int firstWordNotUsed;
    private final CharKey m_charKey;
    private HashMap m_charToString;
    boolean onlyQuotAmpLtGt;
    private final boolean[] shouldMapAttrChar_ASCII;
    private final boolean[] shouldMapTextChar_ASCII;

    /* renamed from: org.apache.xml.serializer.CharInfo.1 */
    static class AnonymousClass1 implements PrivilegedAction {
        final /* synthetic */ String val$entitiesFileName;
        final /* synthetic */ boolean val$internal;
        final /* synthetic */ String val$method;

        AnonymousClass1(String val$entitiesFileName, String val$method, boolean val$internal) {
            this.val$entitiesFileName = val$entitiesFileName;
            this.val$method = val$method;
            this.val$internal = val$internal;
        }

        public Object run() {
            return new CharInfo(this.val$method, this.val$internal, null);
        }
    }

    private static class CharKey {
        private char m_char;

        public CharKey(char key) {
            this.m_char = key;
        }

        public final void setChar(char c) {
            this.m_char = c;
        }

        public final int hashCode() {
            return this.m_char;
        }

        public final boolean equals(Object obj) {
            return ((CharKey) obj).m_char == this.m_char;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.CharInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.CharInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.CharInfo.<clinit>():void");
    }

    private CharInfo() {
        this.array_of_bits = createEmptySetOfIntegers(DTMManager.IDENT_NODE_DEFAULT);
        this.firstWordNotUsed = 0;
        this.shouldMapAttrChar_ASCII = new boolean[ASCII_MAX];
        this.shouldMapTextChar_ASCII = new boolean[ASCII_MAX];
        this.m_charKey = new CharKey();
        this.onlyQuotAmpLtGt = true;
    }

    private CharInfo(String entitiesResource, String method, boolean internal) {
        this();
        this.m_charToString = new HashMap();
        ResourceBundle entities = null;
        boolean noExtraEntities = true;
        if (internal) {
            try {
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {
            }
        }
        String name;
        if (entities != null) {
            Enumeration keys = entities.getKeys();
            while (keys.hasMoreElements()) {
                name = (String) keys.nextElement();
                if (defineEntity(name, (char) Integer.parseInt(entities.getString(name)))) {
                    noExtraEntities = false;
                }
            }
        } else {
            InputStream is = null;
            if (internal) {
                try {
                    is = CharInfo.class.getResourceAsStream(entitiesResource);
                } catch (Exception e2) {
                    throw new RuntimeException(Utils.messages.createMessage(MsgKey.ER_RESOURCE_COULD_NOT_LOAD, new Object[]{entitiesResource, e2.toString(), entitiesResource, e2.toString()}));
                } catch (Throwable th) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e3) {
                        }
                    }
                }
            } else {
                ClassLoader cl = ObjectFactory.findClassLoader();
                if (cl == null) {
                    is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                } else {
                    is = cl.getResourceAsStream(entitiesResource);
                }
                if (is == null) {
                    try {
                        is = new URL(entitiesResource).openStream();
                    } catch (Exception e4) {
                    }
                }
            }
            if (is == null) {
                throw new RuntimeException(Utils.messages.createMessage(MsgKey.ER_RESOURCE_COULD_NOT_FIND, new Object[]{entitiesResource, entitiesResource}));
            }
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException e5) {
                reader = new BufferedReader(new InputStreamReader(is));
            }
            String line = reader.readLine();
            while (line != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    line = reader.readLine();
                } else {
                    int index = line.indexOf(32);
                    if (index > 1) {
                        name = line.substring(0, index);
                        index++;
                        if (index < line.length()) {
                            String value = line.substring(index);
                            index = value.indexOf(32);
                            if (index > 0) {
                                value = value.substring(0, index);
                            }
                            if (defineEntity(name, (char) Integer.parseInt(value))) {
                                noExtraEntities = false;
                            }
                        }
                    }
                    line = reader.readLine();
                }
            }
            is.close();
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e6) {
                }
            }
        }
        this.onlyQuotAmpLtGt = noExtraEntities;
        if (SerializerConstants.XML_PREFIX.equals(method)) {
            this.shouldMapTextChar_ASCII[34] = false;
        }
        if (Method.HTML.equals(method)) {
            this.shouldMapAttrChar_ASCII[60] = false;
            this.shouldMapTextChar_ASCII[34] = false;
        }
    }

    private boolean defineEntity(String name, char value) {
        StringBuffer sb = new StringBuffer("&");
        sb.append(name);
        sb.append(';');
        return defineChar2StringMapping(sb.toString(), value);
    }

    String getOutputStringForChar(char value) {
        this.m_charKey.setChar(value);
        return (String) this.m_charToString.get(this.m_charKey);
    }

    final boolean shouldMapAttrChar(int value) {
        if (value < ASCII_MAX) {
            return this.shouldMapAttrChar_ASCII[value];
        }
        return get(value);
    }

    final boolean shouldMapTextChar(int value) {
        if (value < ASCII_MAX) {
            return this.shouldMapTextChar_ASCII[value];
        }
        return get(value);
    }

    private static CharInfo getCharInfoBasedOnPrivilege(String entitiesFileName, String method, boolean internal) {
        return (CharInfo) AccessController.doPrivileged(new AnonymousClass1(entitiesFileName, method, internal));
    }

    static CharInfo getCharInfo(String entitiesFileName, String method) {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
            return mutableCopyOf(charInfo);
        }
        try {
            charInfo = getCharInfoBasedOnPrivilege(entitiesFileName, method, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return mutableCopyOf(charInfo);
        } catch (Exception e) {
            try {
                return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            } catch (Exception e2) {
                String absoluteEntitiesFileName;
                if (entitiesFileName.indexOf(58) < 0) {
                    absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
                } else {
                    try {
                        absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
                    } catch (TransformerException te) {
                        throw new WrappedRuntimeException(te);
                    }
                }
                return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            }
        }
    }

    private static CharInfo mutableCopyOf(CharInfo charInfo) {
        CharInfo copy = new CharInfo();
        System.arraycopy(charInfo.array_of_bits, 0, copy.array_of_bits, 0, charInfo.array_of_bits.length);
        copy.firstWordNotUsed = charInfo.firstWordNotUsed;
        System.arraycopy(charInfo.shouldMapAttrChar_ASCII, 0, copy.shouldMapAttrChar_ASCII, 0, charInfo.shouldMapAttrChar_ASCII.length);
        System.arraycopy(charInfo.shouldMapTextChar_ASCII, 0, copy.shouldMapTextChar_ASCII, 0, charInfo.shouldMapTextChar_ASCII.length);
        copy.m_charToString = (HashMap) charInfo.m_charToString.clone();
        copy.onlyQuotAmpLtGt = charInfo.onlyQuotAmpLtGt;
        return copy;
    }

    private static int arrayIndex(int i) {
        return i >> SHIFT_PER_WORD;
    }

    private static int bit(int i) {
        return 1 << (i & LOW_ORDER_BITMASK);
    }

    private int[] createEmptySetOfIntegers(int max) {
        this.firstWordNotUsed = 0;
        return new int[(arrayIndex(max - 1) + 1)];
    }

    private final void set(int i) {
        setASCIItextDirty(i);
        setASCIIattrDirty(i);
        int j = i >> SHIFT_PER_WORD;
        int k = j + 1;
        if (this.firstWordNotUsed < k) {
            this.firstWordNotUsed = k;
        }
        int[] iArr = this.array_of_bits;
        iArr[j] = iArr[j] | (1 << (i & LOW_ORDER_BITMASK));
    }

    private final boolean get(int i) {
        int j = i >> SHIFT_PER_WORD;
        if (j < this.firstWordNotUsed) {
            return (this.array_of_bits[j] & (1 << (i & LOW_ORDER_BITMASK))) != 0;
        } else {
            return false;
        }
    }

    private boolean extraEntity(String outputString, int charToMap) {
        if (charToMap >= ASCII_MAX) {
            return false;
        }
        switch (charToMap) {
            case OpCodes.NODENAME /*34*/:
                if (outputString.equals(SerializerConstants.ENTITY_QUOT)) {
                    return false;
                }
                return true;
            case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                if (outputString.equals(SerializerConstants.ENTITY_AMP)) {
                    return false;
                }
                return true;
            case Constants.TATTRNAME_TEST /*60*/:
                if (outputString.equals(SerializerConstants.ENTITY_LT)) {
                    return false;
                }
                return true;
            case Constants.TATTRNAME_TYPE /*62*/:
                if (outputString.equals(SerializerConstants.ENTITY_GT)) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    private void setASCIItextDirty(int j) {
        if (j >= 0 && j < ASCII_MAX) {
            this.shouldMapTextChar_ASCII[j] = true;
        }
    }

    private void setASCIIattrDirty(int j) {
        if (j >= 0 && j < ASCII_MAX) {
            this.shouldMapAttrChar_ASCII[j] = true;
        }
    }

    boolean defineChar2StringMapping(String outputString, char inputChar) {
        this.m_charToString.put(new CharKey(inputChar), outputString);
        set(inputChar);
        return extraEntity(outputString, inputChar);
    }
}
