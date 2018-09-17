package java.nio.charset;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.spi.CharsetProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import libcore.icu.NativeConverter;
import sun.misc.ASCIICaseInsensitiveComparator;
import sun.misc.VM;
import sun.nio.cs.ThreadLocalCoders;
import sun.security.action.GetPropertyAction;

public abstract class Charset implements Comparable<Charset> {
    private static volatile String bugLevel = null;
    private static volatile Entry<String, Charset> cache1 = null;
    private static final HashMap<String, Charset> cache2 = new HashMap();
    private static Charset defaultCharset;
    private static ThreadLocal<ThreadLocal<?>> gate = new ThreadLocal();
    private Set<String> aliasSet = null;
    private final String[] aliases;
    private final String name;

    public abstract boolean contains(Charset charset);

    public abstract CharsetDecoder newDecoder();

    public abstract CharsetEncoder newEncoder();

    static boolean atBugLevel(String bl) {
        String level = bugLevel;
        if (level == null) {
            if (!VM.isBooted()) {
                return false;
            }
            level = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.cs.bugLevel", ""));
            bugLevel = level;
        }
        return level.equals(bl);
    }

    private static void checkName(String s) {
        int n = s.length();
        if (atBugLevel("1.4") || n != 0) {
            int i = 0;
            while (i < n) {
                char c = s.charAt(i);
                if ((c < 'A' || c > 'Z') && ((c < 'a' || c > 'z') && ((c < '0' || c > '9') && ((c != '-' || i == 0) && ((c != '+' || i == 0) && ((c != ':' || i == 0) && ((c != '_' || i == 0) && (c != '.' || i == 0)))))))) {
                    throw new IllegalCharsetNameException(s);
                }
                i++;
            }
            return;
        }
        throw new IllegalCharsetNameException(s);
    }

    private static void cache(String charsetName, Charset cs) {
        synchronized (cache2) {
            String canonicalName = cs.name();
            Charset canonicalCharset = (Charset) cache2.get(canonicalName);
            if (canonicalCharset != null) {
                cs = canonicalCharset;
            } else {
                cache2.put(canonicalName, cs);
                for (String alias : cs.aliases()) {
                    cache2.put(alias, cs);
                }
            }
            cache2.put(charsetName, cs);
        }
        cache1 = new SimpleImmutableEntry(charsetName, cs);
    }

    private static Iterator<CharsetProvider> providers() {
        return new Iterator<CharsetProvider>() {
            Iterator<CharsetProvider> i = this.sl.iterator();
            CharsetProvider next = null;
            ServiceLoader<CharsetProvider> sl = ServiceLoader.load(CharsetProvider.class);

            private boolean getNext() {
                while (this.next == null) {
                    try {
                        if (!this.i.hasNext()) {
                            return false;
                        }
                        this.next = (CharsetProvider) this.i.next();
                    } catch (ServiceConfigurationError sce) {
                        if (!(sce.getCause() instanceof SecurityException)) {
                            throw sce;
                        }
                    }
                }
                return true;
            }

            public boolean hasNext() {
                return getNext();
            }

            public CharsetProvider next() {
                if (getNext()) {
                    CharsetProvider n = this.next;
                    this.next = null;
                    return n;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static Charset lookupViaProviders(final String charsetName) {
        if (!VM.isBooted() || gate.get() != null) {
            return null;
        }
        try {
            gate.set(gate);
            Charset charset = (Charset) AccessController.doPrivileged(new PrivilegedAction<Charset>() {
                public Charset run() {
                    Iterator<CharsetProvider> i = Charset.providers();
                    while (i.hasNext()) {
                        Charset cs = ((CharsetProvider) i.next()).charsetForName(charsetName);
                        if (cs != null) {
                            return cs;
                        }
                    }
                    return null;
                }
            });
            return charset;
        } finally {
            gate.set(null);
        }
    }

    private static Charset lookup(String charsetName) {
        if (charsetName == null) {
            throw new IllegalArgumentException("Null charset name");
        }
        Entry<String, Charset> cached = cache1;
        if (cached == null || !charsetName.equals(cached.getKey())) {
            return lookup2(charsetName);
        }
        return (Charset) cached.getValue();
    }

    /* JADX WARNING: Missing block: B:9:0x0018, code:
            r0 = libcore.icu.NativeConverter.charsetForName(r4);
     */
    /* JADX WARNING: Missing block: B:10:0x001c, code:
            if (r0 != null) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:11:0x001e, code:
            r0 = lookupViaProviders(r4);
     */
    /* JADX WARNING: Missing block: B:12:0x0022, code:
            if (r0 == null) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:13:0x0024, code:
            cache(r4, r0);
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:18:0x002b, code:
            checkName(r4);
     */
    /* JADX WARNING: Missing block: B:19:0x002e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Charset lookup2(String charsetName) {
        synchronized (cache2) {
            Charset cs = (Charset) cache2.get(charsetName);
            if (cs != null) {
                cache1 = new SimpleImmutableEntry(charsetName, cs);
                return cs;
            }
        }
    }

    public static boolean isSupported(String charsetName) {
        return lookup(charsetName) != null;
    }

    public static Charset forName(String charsetName) {
        Charset cs = lookup(charsetName);
        if (cs != null) {
            return cs;
        }
        throw new UnsupportedCharsetException(charsetName);
    }

    public static Charset forNameUEE(String charsetName) throws UnsupportedEncodingException {
        try {
            return forName(charsetName);
        } catch (Exception cause) {
            UnsupportedEncodingException ex = new UnsupportedEncodingException(charsetName);
            ex.initCause(cause);
            throw ex;
        }
    }

    private static void put(Iterator<Charset> i, Map<String, Charset> m) {
        while (i.hasNext()) {
            Charset cs = (Charset) i.next();
            if (!m.containsKey(cs.name())) {
                m.put(cs.name(), cs);
            }
        }
    }

    public static SortedMap<String, Charset> availableCharsets() {
        return (SortedMap) AccessController.doPrivileged(new PrivilegedAction<SortedMap<String, Charset>>() {
            public SortedMap<String, Charset> run() {
                TreeMap<String, Charset> m = new TreeMap(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
                for (String charsetName : NativeConverter.getAvailableCharsetNames()) {
                    Charset charset = NativeConverter.charsetForName(charsetName);
                    m.put(charset.name(), charset);
                }
                Iterator i = Charset.providers();
                while (i.hasNext()) {
                    Charset.put(((CharsetProvider) i.next()).charsets(), m);
                }
                return Collections.unmodifiableSortedMap(m);
            }
        });
    }

    public static Charset defaultCharset() {
        Charset charset;
        synchronized (Charset.class) {
            if (defaultCharset == null) {
                defaultCharset = StandardCharsets.UTF_8;
            }
            charset = defaultCharset;
        }
        return charset;
    }

    protected Charset(String canonicalName, String[] aliases) {
        checkName(canonicalName);
        String[] as = aliases == null ? new String[0] : aliases;
        for (String checkName : as) {
            checkName(checkName);
        }
        this.name = canonicalName;
        this.aliases = as;
    }

    public final String name() {
        return this.name;
    }

    public final Set<String> aliases() {
        if (this.aliasSet != null) {
            return this.aliasSet;
        }
        HashSet<String> hs = new HashSet(n);
        for (Object add : this.aliases) {
            hs.add(add);
        }
        this.aliasSet = Collections.unmodifiableSet(hs);
        return this.aliasSet;
    }

    public String displayName() {
        return this.name;
    }

    public final boolean isRegistered() {
        return !this.name.startsWith("X-") ? this.name.startsWith("x-") ^ 1 : false;
    }

    public String displayName(Locale locale) {
        return this.name;
    }

    public boolean canEncode() {
        return true;
    }

    public final CharBuffer decode(ByteBuffer bb) {
        try {
            return ThreadLocalCoders.decoderFor(this).onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).decode(bb);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    public final ByteBuffer encode(CharBuffer cb) {
        try {
            return ThreadLocalCoders.encoderFor(this).onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).encode(cb);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    public final ByteBuffer encode(String str) {
        return encode(CharBuffer.wrap((CharSequence) str));
    }

    public final int compareTo(Charset that) {
        return name().compareToIgnoreCase(that.name());
    }

    public final int hashCode() {
        return name().hashCode();
    }

    public final boolean equals(Object ob) {
        if (!(ob instanceof Charset)) {
            return false;
        }
        if (this == ob) {
            return true;
        }
        return this.name.equals(((Charset) ob).name());
    }

    public final String toString() {
        return name();
    }
}
