package java.util.regex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import libcore.util.EmptyArray;
import libcore.util.NativeAllocationRegistry;

public final class Pattern implements Serializable {
    public static final int CANON_EQ = 128;
    public static final int CASE_INSENSITIVE = 2;
    public static final int COMMENTS = 4;
    public static final int DOTALL = 32;
    private static final String FASTSPLIT_METACHARACTERS = "\\?*+[](){}^$.|";
    public static final int LITERAL = 16;
    public static final int MULTILINE = 8;
    public static final int UNICODE_CASE = 64;
    public static final int UNICODE_CHARACTER_CLASS = 256;
    public static final int UNIX_LINES = 1;
    private static final NativeAllocationRegistry registry = null;
    private static final long serialVersionUID = 5073258162644648461L;
    transient long address;
    private final int flags;
    private final String pattern;

    final /* synthetic */ class -java_util_function_Predicate_asPredicate__LambdaImpl0 implements Predicate {
        private /* synthetic */ Pattern val$this;

        public /* synthetic */ -java_util_function_Predicate_asPredicate__LambdaImpl0(Pattern pattern) {
            this.val$this = pattern;
        }

        public boolean test(Object arg0) {
            return this.val$this.-java_util_regex_Pattern_lambda$1((String) arg0);
        }
    }

    /* renamed from: java.util.regex.Pattern.1MatcherIterator */
    class AnonymousClass1MatcherIterator implements Iterator<String> {
        private int current;
        private int emptyElementCount;
        private final Matcher matcher;
        private String nextElement;
        final /* synthetic */ CharSequence val$input;

        AnonymousClass1MatcherIterator(CharSequence val$input) {
            this.val$input = val$input;
            this.matcher = Pattern.this.matcher(val$input);
        }

        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else if (this.emptyElementCount == 0) {
                String n = this.nextElement;
                this.nextElement = null;
                return n;
            } else {
                this.emptyElementCount--;
                return "";
            }
        }

        public boolean hasNext() {
            if (this.nextElement != null || this.emptyElementCount > 0) {
                return true;
            }
            if (this.current == this.val$input.length()) {
                return false;
            }
            while (this.matcher.find()) {
                this.nextElement = this.val$input.subSequence(this.current, this.matcher.start()).toString();
                this.current = this.matcher.end();
                if (!this.nextElement.isEmpty()) {
                    return true;
                }
                if (this.current > 0) {
                    this.emptyElementCount += Pattern.UNIX_LINES;
                }
            }
            this.nextElement = this.val$input.subSequence(this.current, this.val$input.length()).toString();
            this.current = this.val$input.length();
            if (!this.nextElement.isEmpty()) {
                return true;
            }
            this.emptyElementCount = 0;
            this.nextElement = null;
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.regex.Pattern.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.regex.Pattern.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.regex.Pattern.<clinit>():void");
    }

    private Pattern(java.lang.String r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.regex.Pattern.<init>(java.lang.String, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.regex.Pattern.<init>(java.lang.String, int):void");
    }

    private static native long compileImpl(String str, int i);

    private static native long getNativeFinalizer();

    private static native int nativeSize();

    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    public static Pattern compile(String regex, int flags) throws PatternSyntaxException {
        return new Pattern(regex, flags);
    }

    public String pattern() {
        return this.pattern;
    }

    public String toString() {
        return this.pattern;
    }

    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }

    public int flags() {
        return this.flags;
    }

    public static boolean matches(String regex, CharSequence input) {
        return compile(regex).matcher(input).matches();
    }

    public String[] split(CharSequence input, int limit) {
        String[] fast = fastSplit(this.pattern, input.toString(), limit);
        if (fast != null) {
            return fast;
        }
        int index = 0;
        boolean matchLimited = limit > 0;
        ArrayList<String> matchList = new ArrayList();
        Matcher m = matcher(input);
        while (m.find()) {
            if (!matchLimited || matchList.size() < limit - 1) {
                matchList.add(input.subSequence(index, m.start()).toString());
                index = m.end();
            } else if (matchList.size() == limit - 1) {
                matchList.add(input.subSequence(index, input.length()).toString());
                index = m.end();
            }
        }
        if (index == 0) {
            String[] strArr = new String[UNIX_LINES];
            strArr[0] = input.toString();
            return strArr;
        }
        if (!matchLimited || matchList.size() < limit) {
            matchList.add(input.subSequence(index, input.length()).toString());
        }
        int resultSize = matchList.size();
        if (limit == 0) {
            while (resultSize > 0 && ((String) matchList.get(resultSize - 1)).equals("")) {
                resultSize--;
            }
        }
        return (String[]) matchList.subList(0, resultSize).toArray(new String[resultSize]);
    }

    public static String[] fastSplit(String re, String input, int limit) {
        int len = re.length();
        if (len == 0) {
            return null;
        }
        int ch;
        char ch2 = re.charAt(0);
        if (!(len == UNIX_LINES && FASTSPLIT_METACHARACTERS.indexOf((int) ch2) == -1)) {
            if (len != CASE_INSENSITIVE || ch2 != '\\') {
                return null;
            }
            ch = re.charAt(UNIX_LINES);
            if (FASTSPLIT_METACHARACTERS.indexOf(ch) == -1) {
                return null;
            }
        }
        if (input.isEmpty()) {
            String[] strArr = new String[UNIX_LINES];
            strArr[0] = "";
            return strArr;
        }
        int separatorCount = 0;
        int begin = 0;
        while (separatorCount + UNIX_LINES != limit) {
            int end = input.indexOf(ch, begin);
            if (end == -1) {
                break;
            }
            separatorCount += UNIX_LINES;
            begin = end + UNIX_LINES;
        }
        int lastPartEnd = input.length();
        if (limit == 0 && begin == lastPartEnd) {
            if (separatorCount == lastPartEnd) {
                return EmptyArray.STRING;
            }
            do {
                begin--;
            } while (input.charAt(begin - 1) == ch);
            separatorCount -= input.length() - begin;
            lastPartEnd = begin;
        }
        String[] result = new String[(separatorCount + UNIX_LINES)];
        begin = 0;
        for (int i = 0; i != separatorCount; i += UNIX_LINES) {
            end = input.indexOf(ch, begin);
            result[i] = input.substring(begin, end);
            begin = end + UNIX_LINES;
        }
        result[separatorCount] = input.substring(begin, lastPartEnd);
        return result;
    }

    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    public static String quote(String s) {
        if (s.indexOf("\\E") == -1) {
            return "\\Q" + s + "\\E";
        }
        StringBuilder sb = new StringBuilder(s.length() * CASE_INSENSITIVE);
        sb.append("\\Q");
        int current = 0;
        while (true) {
            int slashEIndex = s.indexOf("\\E", current);
            if (slashEIndex != -1) {
                sb.append(s.substring(current, slashEIndex));
                current = slashEIndex + CASE_INSENSITIVE;
                sb.append("\\E\\\\E\\Q");
            } else {
                sb.append(s.substring(current, s.length()));
                sb.append("\\E");
                return sb.toString();
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        compile();
    }

    private void compile() throws PatternSyntaxException {
        if (this.pattern == null) {
            throw new NullPointerException("pattern == null");
        }
        String icuPattern = this.pattern;
        if ((this.flags & LITERAL) != 0) {
            icuPattern = quote(this.pattern);
        }
        this.address = compileImpl(icuPattern, this.flags & 47);
        registry.registerNativeAllocation(this, this.address);
    }

    /* synthetic */ boolean -java_util_regex_Pattern_lambda$1(String s) {
        return matcher(s).find();
    }

    public Predicate<String> asPredicate() {
        return new -java_util_function_Predicate_asPredicate__LambdaImpl0();
    }

    public Stream<String> splitAsStream(CharSequence input) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new AnonymousClass1MatcherIterator(input), 272), false);
    }
}
