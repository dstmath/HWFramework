package ohos.global.icu.text;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ohos.global.icu.impl.Norm2AllModes;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.UCharacter;

public final class CanonicalIterator {
    private static boolean PROGRESS = false;
    private static final Set<String> SET_WITH_NULL_STRING = new HashSet();
    private static boolean SKIP_ZEROS = true;
    private transient StringBuilder buffer = new StringBuilder();
    private int[] current;
    private boolean done;
    private final Normalizer2Impl nfcImpl;
    private final Normalizer2 nfd;
    private String[][] pieces;
    private String source;

    public CanonicalIterator(String str) {
        Norm2AllModes nFCInstance = Norm2AllModes.getNFCInstance();
        this.nfd = nFCInstance.decomp;
        this.nfcImpl = nFCInstance.impl.ensureCanonIterData();
        setSource(str);
    }

    public String getSource() {
        return this.source;
    }

    public void reset() {
        this.done = false;
        int i = 0;
        while (true) {
            int[] iArr = this.current;
            if (i < iArr.length) {
                iArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    public String next() {
        if (this.done) {
            return null;
        }
        this.buffer.setLength(0);
        int i = 0;
        while (true) {
            String[][] strArr = this.pieces;
            if (i >= strArr.length) {
                break;
            }
            this.buffer.append(strArr[i][this.current[i]]);
            i++;
        }
        String sb = this.buffer.toString();
        int length = this.current.length - 1;
        while (true) {
            if (length < 0) {
                this.done = true;
                break;
            }
            int[] iArr = this.current;
            iArr[length] = iArr[length] + 1;
            if (iArr[length] < this.pieces[length].length) {
                break;
            }
            iArr[length] = 0;
            length--;
        }
        return sb;
    }

    public void setSource(String str) {
        this.source = this.nfd.normalize(str);
        this.done = false;
        if (str.length() == 0) {
            this.pieces = new String[1][];
            this.current = new int[1];
            this.pieces[0] = new String[]{""};
            return;
        }
        ArrayList arrayList = new ArrayList();
        int findOffsetFromCodePoint = UTF16.findOffsetFromCodePoint(this.source, 1);
        int i = 0;
        while (findOffsetFromCodePoint < this.source.length()) {
            int codePointAt = this.source.codePointAt(findOffsetFromCodePoint);
            if (this.nfcImpl.isCanonSegmentStarter(codePointAt)) {
                arrayList.add(this.source.substring(i, findOffsetFromCodePoint));
                i = findOffsetFromCodePoint;
            }
            findOffsetFromCodePoint += Character.charCount(codePointAt);
        }
        arrayList.add(this.source.substring(i, findOffsetFromCodePoint));
        this.pieces = new String[arrayList.size()][];
        this.current = new int[arrayList.size()];
        for (int i2 = 0; i2 < this.pieces.length; i2++) {
            if (PROGRESS) {
                System.out.println("SEGMENT");
            }
            this.pieces[i2] = getEquivalents((String) arrayList.get(i2));
        }
    }

    @Deprecated
    public static void permute(String str, boolean z, Set<String> set) {
        if (str.length() > 2 || UTF16.countCodePoint(str) > 1) {
            HashSet hashSet = new HashSet();
            int i = 0;
            while (i < str.length()) {
                int charAt = UTF16.charAt(str, i);
                if (!z || i == 0 || UCharacter.getCombiningClass(charAt) != 0) {
                    hashSet.clear();
                    permute(str.substring(0, i) + str.substring(UTF16.getCharCount(charAt) + i), z, hashSet);
                    String valueOf = UTF16.valueOf(str, i);
                    Iterator it = hashSet.iterator();
                    while (it.hasNext()) {
                        set.add(valueOf + ((String) it.next()));
                    }
                }
                i += UTF16.getCharCount(charAt);
            }
            return;
        }
        set.add(str);
    }

    private String[] getEquivalents(String str) {
        HashSet hashSet = new HashSet();
        Set<String> equivalents2 = getEquivalents2(str);
        HashSet<String> hashSet2 = new HashSet();
        for (String str2 : equivalents2) {
            hashSet2.clear();
            permute(str2, SKIP_ZEROS, hashSet2);
            for (String str3 : hashSet2) {
                if (Normalizer.compare(str3, str, 0) == 0) {
                    if (PROGRESS) {
                        PrintStream printStream = System.out;
                        printStream.println("Adding Permutation: " + Utility.hex(str3));
                    }
                    hashSet.add(str3);
                } else if (PROGRESS) {
                    PrintStream printStream2 = System.out;
                    printStream2.println("-Skipping Permutation: " + Utility.hex(str3));
                }
            }
        }
        String[] strArr = new String[hashSet.size()];
        hashSet.toArray(strArr);
        return strArr;
    }

    private Set<String> getEquivalents2(String str) {
        HashSet hashSet = new HashSet();
        if (PROGRESS) {
            System.out.println("Adding: " + Utility.hex(str));
        }
        hashSet.add(str);
        StringBuffer stringBuffer = new StringBuffer();
        UnicodeSet unicodeSet = new UnicodeSet();
        int i = 0;
        while (i < str.length()) {
            int codePointAt = str.codePointAt(i);
            if (this.nfcImpl.getCanonStartSet(codePointAt, unicodeSet)) {
                UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(unicodeSet);
                while (unicodeSetIterator.next()) {
                    int i2 = unicodeSetIterator.codepoint;
                    Set<String> extract = extract(i2, str, i, stringBuffer);
                    if (extract != null) {
                        String str2 = str.substring(0, i) + UTF16.valueOf(i2);
                        Iterator<String> it = extract.iterator();
                        while (it.hasNext()) {
                            hashSet.add(str2 + it.next());
                        }
                    }
                }
            }
            i += Character.charCount(codePointAt);
        }
        return hashSet;
    }

    private Set<String> extract(int i, String str, int i2, StringBuffer stringBuffer) {
        boolean z;
        if (PROGRESS) {
            System.out.println(" extract: " + Utility.hex(UTF16.valueOf(i)) + ", " + Utility.hex(str.substring(i2)));
        }
        String decomposition = this.nfcImpl.getDecomposition(i);
        if (decomposition == null) {
            decomposition = UTF16.valueOf(i);
        }
        int charAt = UTF16.charAt(decomposition, 0);
        stringBuffer.setLength(0);
        int charCount = UTF16.getCharCount(charAt) + 0;
        int i3 = charAt;
        int i4 = i2;
        while (true) {
            if (i4 >= str.length()) {
                z = false;
                break;
            }
            int charAt2 = UTF16.charAt(str, i4);
            if (charAt2 == i3) {
                if (PROGRESS) {
                    System.out.println("  matches: " + Utility.hex(UTF16.valueOf(charAt2)));
                }
                if (charCount == decomposition.length()) {
                    stringBuffer.append(str.substring(i4 + UTF16.getCharCount(charAt2)));
                    z = true;
                    break;
                }
                i3 = UTF16.charAt(decomposition, charCount);
                charCount += UTF16.getCharCount(i3);
            } else {
                if (PROGRESS) {
                    System.out.println("  buffer: " + Utility.hex(UTF16.valueOf(charAt2)));
                }
                UTF16.append(stringBuffer, charAt2);
            }
            i4 += UTF16.getCharCount(charAt2);
        }
        if (!z) {
            return null;
        }
        if (PROGRESS) {
            System.out.println("Matches");
        }
        if (stringBuffer.length() == 0) {
            return SET_WITH_NULL_STRING;
        }
        String stringBuffer2 = stringBuffer.toString();
        if (Normalizer.compare(UTF16.valueOf(i) + stringBuffer2, str.substring(i2), 0) != 0) {
            return null;
        }
        return getEquivalents2(stringBuffer2);
    }

    static {
        SET_WITH_NULL_STRING.add("");
    }
}
