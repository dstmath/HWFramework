package android.icu.text;

import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.traversal.NodeFilter;

public class CharsetDetector {
    private static final List<CSRecognizerInfo> ALL_CS_RECOGNIZERS = null;
    private static final int kBufSize = 8000;
    short[] fByteStats;
    boolean fC1Bytes;
    String fDeclaredEncoding;
    private boolean[] fEnabledRecognizers;
    byte[] fInputBytes;
    int fInputLen;
    InputStream fInputStream;
    byte[] fRawInput;
    int fRawLength;
    private boolean fStripTags;

    private static class CSRecognizerInfo {
        boolean isDefaultEnabled;
        CharsetRecognizer recognizer;

        CSRecognizerInfo(CharsetRecognizer recognizer, boolean isDefaultEnabled) {
            this.recognizer = recognizer;
            this.isDefaultEnabled = isDefaultEnabled;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetDetector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetDetector.<clinit>():void");
    }

    public CharsetDetector() {
        this.fInputBytes = new byte[kBufSize];
        this.fByteStats = new short[NodeFilter.SHOW_DOCUMENT];
        this.fC1Bytes = false;
        this.fStripTags = false;
    }

    public CharsetDetector setDeclaredEncoding(String encoding) {
        this.fDeclaredEncoding = encoding;
        return this;
    }

    public CharsetDetector setText(byte[] in) {
        this.fRawInput = in;
        this.fRawLength = in.length;
        return this;
    }

    public CharsetDetector setText(InputStream in) throws IOException {
        this.fInputStream = in;
        this.fInputStream.mark(kBufSize);
        this.fRawInput = new byte[kBufSize];
        this.fRawLength = 0;
        int remainingLength = kBufSize;
        while (remainingLength > 0) {
            int bytesRead = this.fInputStream.read(this.fRawInput, this.fRawLength, remainingLength);
            if (bytesRead <= 0) {
                break;
            }
            this.fRawLength += bytesRead;
            remainingLength -= bytesRead;
        }
        this.fInputStream.reset();
        return this;
    }

    public CharsetMatch detect() {
        CharsetMatch[] matches = detectAll();
        if (matches == null || matches.length == 0) {
            return null;
        }
        return matches[0];
    }

    public CharsetMatch[] detectAll() {
        ArrayList<CharsetMatch> matches = new ArrayList();
        MungeInput();
        int i = 0;
        while (i < ALL_CS_RECOGNIZERS.size()) {
            CSRecognizerInfo rcinfo = (CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i);
            if (this.fEnabledRecognizers != null ? this.fEnabledRecognizers[i] : rcinfo.isDefaultEnabled) {
                CharsetMatch m = rcinfo.recognizer.match(this);
                if (m != null) {
                    matches.add(m);
                }
            }
            i++;
        }
        Collections.sort(matches);
        Collections.reverse(matches);
        return (CharsetMatch[]) matches.toArray(new CharsetMatch[matches.size()]);
    }

    public Reader getReader(InputStream in, String declaredEncoding) {
        this.fDeclaredEncoding = declaredEncoding;
        try {
            setText(in);
            CharsetMatch match = detect();
            if (match == null) {
                return null;
            }
            return match.getReader();
        } catch (IOException e) {
            return null;
        }
    }

    public String getString(byte[] in, String declaredEncoding) {
        this.fDeclaredEncoding = declaredEncoding;
        try {
            setText(in);
            CharsetMatch match = detect();
            if (match == null) {
                return null;
            }
            return match.getString(-1);
        } catch (IOException e) {
            return null;
        }
    }

    public static String[] getAllDetectableCharsets() {
        String[] allCharsetNames = new String[ALL_CS_RECOGNIZERS.size()];
        for (int i = 0; i < allCharsetNames.length; i++) {
            allCharsetNames[i] = ((CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i)).recognizer.getName();
        }
        return allCharsetNames;
    }

    public boolean inputFilterEnabled() {
        return this.fStripTags;
    }

    public boolean enableInputFilter(boolean filter) {
        boolean previous = this.fStripTags;
        this.fStripTags = filter;
        return previous;
    }

    private void MungeInput() {
        int srci;
        int dsti = 0;
        boolean inMarkup = false;
        int openTags = 0;
        int badTags = 0;
        if (this.fStripTags) {
            for (srci = 0; srci < this.fRawLength && dsti < this.fInputBytes.length; srci++) {
                byte b = this.fRawInput[srci];
                if (b == 60) {
                    if (inMarkup) {
                        badTags++;
                    }
                    inMarkup = true;
                    openTags++;
                }
                if (!inMarkup) {
                    int dsti2 = dsti + 1;
                    this.fInputBytes[dsti] = b;
                    dsti = dsti2;
                }
                if (b == 62) {
                    inMarkup = false;
                }
            }
            this.fInputLen = dsti;
        }
        if (openTags < 5 || openTags / 5 < badTags || (this.fInputLen < 100 && this.fRawLength > 600)) {
            int limit = this.fRawLength;
            if (limit > kBufSize) {
                limit = kBufSize;
            }
            srci = 0;
            while (srci < limit) {
                this.fInputBytes[srci] = this.fRawInput[srci];
                srci++;
            }
            this.fInputLen = srci;
        }
        Arrays.fill(this.fByteStats, (short) 0);
        for (srci = 0; srci < this.fInputLen; srci++) {
            int val = this.fInputBytes[srci] & Opcodes.OP_CONST_CLASS_JUMBO;
            short[] sArr = this.fByteStats;
            sArr[val] = (short) (sArr[val] + 1);
        }
        this.fC1Bytes = false;
        for (int i = NodeFilter.SHOW_COMMENT; i <= Opcodes.OP_REM_LONG; i++) {
            if (this.fByteStats[i] != (short) 0) {
                this.fC1Bytes = true;
                return;
            }
        }
    }

    @Deprecated
    public String[] getDetectableCharsets() {
        List<String> csnames = new ArrayList(ALL_CS_RECOGNIZERS.size());
        int i = 0;
        while (i < ALL_CS_RECOGNIZERS.size()) {
            CSRecognizerInfo rcinfo = (CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i);
            if (this.fEnabledRecognizers == null ? rcinfo.isDefaultEnabled : this.fEnabledRecognizers[i]) {
                csnames.add(rcinfo.recognizer.getName());
            }
            i++;
        }
        return (String[]) csnames.toArray(new String[csnames.size()]);
    }

    @Deprecated
    public CharsetDetector setDetectableCharset(String encoding, boolean enabled) {
        int i;
        int modIdx = -1;
        boolean isDefaultVal = false;
        for (i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
            CSRecognizerInfo csrinfo = (CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i);
            if (csrinfo.recognizer.getName().equals(encoding)) {
                modIdx = i;
                isDefaultVal = csrinfo.isDefaultEnabled == enabled;
                if (modIdx >= 0) {
                    throw new IllegalArgumentException("Invalid encoding: \"" + encoding + "\"");
                }
                if (this.fEnabledRecognizers == null && !isDefaultVal) {
                    this.fEnabledRecognizers = new boolean[ALL_CS_RECOGNIZERS.size()];
                    for (i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
                        this.fEnabledRecognizers[i] = ((CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i)).isDefaultEnabled;
                    }
                }
                if (this.fEnabledRecognizers != null) {
                    this.fEnabledRecognizers[modIdx] = enabled;
                }
                return this;
            }
        }
        if (modIdx >= 0) {
            this.fEnabledRecognizers = new boolean[ALL_CS_RECOGNIZERS.size()];
            for (i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
                this.fEnabledRecognizers[i] = ((CSRecognizerInfo) ALL_CS_RECOGNIZERS.get(i)).isDefaultEnabled;
            }
            if (this.fEnabledRecognizers != null) {
                this.fEnabledRecognizers[modIdx] = enabled;
            }
            return this;
        }
        throw new IllegalArgumentException("Invalid encoding: \"" + encoding + "\"");
    }
}
