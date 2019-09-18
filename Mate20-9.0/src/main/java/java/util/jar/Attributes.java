package java.util.jar;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import sun.misc.ASCIICaseInsensitiveComparator;
import sun.util.logging.PlatformLogger;

public class Attributes implements Map<Object, Object>, Cloneable {
    protected Map<Object, Object> map;

    public static class Name {
        public static final Name CLASS_PATH = new Name("Class-Path");
        public static final Name CONTENT_TYPE = new Name("Content-Type");
        @Deprecated
        public static final Name EXTENSION_INSTALLATION = new Name("Extension-Installation");
        public static final Name EXTENSION_LIST = new Name("Extension-List");
        public static final Name EXTENSION_NAME = new Name("Extension-Name");
        public static final Name IMPLEMENTATION_TITLE = new Name("Implementation-Title");
        @Deprecated
        public static final Name IMPLEMENTATION_URL = new Name("Implementation-URL");
        public static final Name IMPLEMENTATION_VENDOR = new Name("Implementation-Vendor");
        @Deprecated
        public static final Name IMPLEMENTATION_VENDOR_ID = new Name("Implementation-Vendor-Id");
        public static final Name IMPLEMENTATION_VERSION = new Name("Implementation-Version");
        public static final Name MAIN_CLASS = new Name("Main-Class");
        public static final Name MANIFEST_VERSION = new Name("Manifest-Version");
        public static final Name NAME = new Name("Name");
        public static final Name SEALED = new Name("Sealed");
        public static final Name SIGNATURE_VERSION = new Name("Signature-Version");
        public static final Name SPECIFICATION_TITLE = new Name("Specification-Title");
        public static final Name SPECIFICATION_VENDOR = new Name("Specification-Vendor");
        public static final Name SPECIFICATION_VERSION = new Name("Specification-Version");
        private int hashCode = -1;
        private String name;

        public Name(String name2) {
            if (name2 == null) {
                throw new NullPointerException("name");
            } else if (isValid(name2)) {
                this.name = name2.intern();
            } else {
                throw new IllegalArgumentException(name2);
            }
        }

        private static boolean isValid(String name2) {
            int len = name2.length();
            if (len > 70 || len == 0) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (!isValid(name2.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isValid(char c) {
            return isAlpha(c) || isDigit(c) || c == '_' || c == '-';
        }

        private static boolean isAlpha(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Name)) {
                return false;
            }
            if (ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER.compare(this.name, ((Name) o).name) == 0) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            if (this.hashCode == -1) {
                this.hashCode = ASCIICaseInsensitiveComparator.lowerCaseHashCode(this.name);
            }
            return this.hashCode;
        }

        public String toString() {
            return this.name;
        }
    }

    public Attributes() {
        this(11);
    }

    public Attributes(int size) {
        this.map = new HashMap(size);
    }

    public Attributes(Attributes attr) {
        this.map = new HashMap(attr);
    }

    public Object get(Object name) {
        return this.map.get(name);
    }

    public String getValue(String name) {
        return (String) get(new Name(name));
    }

    public String getValue(Name name) {
        return (String) get(name);
    }

    public Object put(Object name, Object value) {
        return this.map.put((Name) name, (String) value);
    }

    public String putValue(String name, String value) {
        return (String) put(new Name(name), value);
    }

    public Object remove(Object name) {
        return this.map.remove(name);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public boolean containsKey(Object name) {
        return this.map.containsKey(name);
    }

    public void putAll(Map<?, ?> attr) {
        if (Attributes.class.isInstance(attr)) {
            for (Map.Entry<?, ?> me : attr.entrySet()) {
                put(me.getKey(), me.getValue());
            }
            return;
        }
        throw new ClassCastException();
    }

    public void clear() {
        this.map.clear();
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Set<Object> keySet() {
        return this.map.keySet();
    }

    public Collection<Object> values() {
        return this.map.values();
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.map.entrySet();
    }

    public boolean equals(Object o) {
        return this.map.equals(o);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public Object clone() {
        return new Attributes(this);
    }

    /* access modifiers changed from: package-private */
    public void write(DataOutputStream os) throws IOException {
        for (Map.Entry<Object, Object> e : entrySet()) {
            StringBuffer buffer = new StringBuffer(((Name) e.getKey()).toString());
            buffer.append(": ");
            String value = (String) e.getValue();
            if (value != null) {
                byte[] vb = value.getBytes("UTF8");
                value = new String(vb, 0, 0, vb.length);
            }
            buffer.append(value);
            buffer.append("\r\n");
            Manifest.make72Safe(buffer);
            os.writeBytes(buffer.toString());
        }
        os.writeBytes("\r\n");
    }

    /* access modifiers changed from: package-private */
    public void writeMain(DataOutputStream out) throws IOException {
        String vername = Name.MANIFEST_VERSION.toString();
        String version = getValue(vername);
        if (version == null) {
            vername = Name.SIGNATURE_VERSION.toString();
            version = getValue(vername);
        }
        if (version != null) {
            out.writeBytes(vername + ": " + version + "\r\n");
        }
        for (Map.Entry<Object, Object> e : entrySet()) {
            String name = ((Name) e.getKey()).toString();
            if (version != null && !name.equalsIgnoreCase(vername)) {
                StringBuffer buffer = new StringBuffer(name);
                buffer.append(": ");
                String value = (String) e.getValue();
                if (value != null) {
                    byte[] vb = value.getBytes("UTF8");
                    value = new String(vb, 0, 0, vb.length);
                }
                buffer.append(value);
                buffer.append("\r\n");
                Manifest.make72Safe(buffer);
                out.writeBytes(buffer.toString());
            }
        }
        out.writeBytes("\r\n");
    }

    /* access modifiers changed from: package-private */
    public void read(Manifest.FastInputStream is, byte[] lbuf) throws IOException {
        String value;
        String name = null;
        byte[] lastline = null;
        while (true) {
            int readLine = is.readLine(lbuf);
            int len = readLine;
            if (readLine != -1) {
                boolean lineContinued = false;
                int len2 = len - 1;
                if (lbuf[len2] == 10) {
                    if (len2 > 0 && lbuf[len2 - 1] == 13) {
                        len2--;
                    }
                    if (len2 != 0) {
                        int i = 0;
                        if (lbuf[0] != 32) {
                            while (true) {
                                int i2 = i + 1;
                                if (lbuf[i] == 58) {
                                    int i3 = i2 + 1;
                                    if (lbuf[i2] == 32) {
                                        name = new String(lbuf, 0, 0, i3 - 2);
                                        if (is.peek() == 32) {
                                            lastline = new byte[(len2 - i3)];
                                            System.arraycopy(lbuf, i3, lastline, 0, len2 - i3);
                                        } else {
                                            value = new String(lbuf, i3, len2 - i3, "UTF8");
                                        }
                                    } else {
                                        throw new IOException("invalid header field");
                                    }
                                } else if (i2 < len2) {
                                    i = i2;
                                } else {
                                    throw new IOException("invalid header field");
                                }
                            }
                        } else if (name != null) {
                            lineContinued = true;
                            byte[] buf = new byte[((lastline.length + len2) - 1)];
                            System.arraycopy(lastline, 0, buf, 0, lastline.length);
                            System.arraycopy(lbuf, 1, buf, lastline.length, len2 - 1);
                            if (is.peek() == 32) {
                                lastline = buf;
                            } else {
                                value = new String(buf, 0, buf.length, "UTF8");
                                lastline = null;
                            }
                        } else {
                            throw new IOException("misplaced continuation line");
                        }
                        try {
                            if (putValue(name, value) != null && !lineContinued) {
                                PlatformLogger.getLogger("java.util.jar").warning("Duplicate name in Manifest: " + name + ".\nEnsure that the manifest does not have duplicate entries, and\nthat blank lines separate individual sections in both your\nmanifest and in the META-INF/MANIFEST.MF entry in the jar file.");
                            }
                        } catch (IllegalArgumentException e) {
                            throw new IOException("invalid header field name: " + name);
                        }
                    } else {
                        return;
                    }
                } else {
                    throw new IOException("line too long");
                }
            } else {
                return;
            }
        }
    }
}
