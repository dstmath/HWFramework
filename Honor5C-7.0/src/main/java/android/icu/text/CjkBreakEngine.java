package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.CharacterIteration;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import libcore.icu.DateUtilsBridge;
import org.xmlpull.v1.XmlPullParser;

class CjkBreakEngine extends DictionaryBreakEngine {
    private static final UnicodeSet fHanWordSet = null;
    private static final UnicodeSet fHangulWordSet = null;
    private static final UnicodeSet fHiraganaWordSet = null;
    private static final UnicodeSet fKatakanaWordSet = null;
    private static final int kMaxKatakanaGroupLength = 20;
    private static final int kMaxKatakanaLength = 8;
    private static final int kint32max = Integer.MAX_VALUE;
    private static final int maxSnlp = 255;
    private DictionaryMatcher fDictionary;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CjkBreakEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CjkBreakEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CjkBreakEngine.<clinit>():void");
    }

    public CjkBreakEngine(boolean korean) throws IOException {
        super(Integer.valueOf(1));
        this.fDictionary = null;
        this.fDictionary = DictionaryData.loadDictionaryFor("Hira");
        if (korean) {
            setCharacters(fHangulWordSet);
            return;
        }
        UnicodeSet unicodeSet = new UnicodeSet();
        unicodeSet = new UnicodeSet();
        unicodeSet.addAll(fHanWordSet);
        unicodeSet.addAll(fKatakanaWordSet);
        unicodeSet.addAll(fHiraganaWordSet);
        unicodeSet.add(65392);
        unicodeSet.add(12540);
        setCharacters(unicodeSet);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CjkBreakEngine)) {
            return false;
        }
        return this.fSet.equals(((CjkBreakEngine) obj).fSet);
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    private static int getKatakanaCost(int wordlength) {
        return wordlength > kMaxKatakanaLength ? DateUtilsBridge.FORMAT_UTC : new int[]{DateUtilsBridge.FORMAT_UTC, 984, 408, Opcodes.OP_INVOKE_DIRECT_EMPTY, Opcodes.OP_SUB_DOUBLE_2ADDR, SCSU.ARMENIANINDEX, BreakIterator.WORD_LETTER_LIMIT, 372, 480}[wordlength];
    }

    private static boolean isKatakana(int value) {
        if (value >= 12449 && value <= 12542 && value != 12539) {
            return true;
        }
        if (value < 65382 || value > 65439) {
            return false;
        }
        return true;
    }

    public int divideUpDictionaryRange(CharacterIterator inText, int startPos, int endPos, DequeI foundBreaks) {
        if (startPos >= endPos) {
            return 0;
        }
        boolean isNormalized;
        CharacterIterator text;
        int i;
        inText.setIndex(startPos);
        int[] charPositions = new int[((endPos - startPos) + 1)];
        StringBuffer stringBuffer = new StringBuffer(XmlPullParser.NO_NAMESPACE);
        inText.setIndex(startPos);
        while (inText.getIndex() < endPos) {
            stringBuffer.append(inText.current());
            inText.next();
        }
        String prenormstr = stringBuffer.toString();
        if (Normalizer.quickCheck(prenormstr, Normalizer.NFKC) != Normalizer.YES) {
            isNormalized = Normalizer.isNormalized(prenormstr, Normalizer.NFKC, 0);
        } else {
            isNormalized = true;
        }
        int numChars = 0;
        int index;
        if (isNormalized) {
            text = new StringCharacterIterator(prenormstr);
            index = 0;
            charPositions[0] = 0;
            while (index < prenormstr.length()) {
                index += Character.charCount(prenormstr.codePointAt(index));
                numChars++;
                charPositions[numChars] = index;
            }
        } else {
            String normStr = Normalizer.normalize(prenormstr, Normalizer.NFKC);
            text = new StringCharacterIterator(normStr);
            charPositions = new int[(normStr.length() + 1)];
            Normalizer normalizer = new Normalizer(prenormstr, Normalizer.NFKC, 0);
            index = 0;
            charPositions[0] = 0;
            while (index < normalizer.endIndex()) {
                normalizer.next();
                numChars++;
                index = normalizer.getIndex();
                charPositions[numChars] = index;
            }
        }
        int[] bestSnlp = new int[(numChars + 1)];
        bestSnlp[0] = 0;
        for (i = 1; i <= numChars; i++) {
            bestSnlp[i] = kint32max;
        }
        int[] prev = new int[(numChars + 1)];
        for (i = 0; i <= numChars; i++) {
            prev[i] = -1;
        }
        int[] values = new int[numChars];
        int[] lengths = new int[numChars];
        boolean is_prev_katakana = false;
        i = 0;
        while (i < numChars) {
            text.setIndex(i);
            if (bestSnlp[i] != kint32max) {
                int j;
                int newSnlp;
                int maxSearchLength = i + kMaxKatakanaGroupLength < numChars ? kMaxKatakanaGroupLength : numChars - i;
                int[] count_ = new int[1];
                this.fDictionary.matches(text, maxSearchLength, lengths, count_, maxSearchLength, values);
                int count = count_[0];
                if (!((count != 0 && lengths[0] == 1) || CharacterIteration.current32(text) == kint32max || fHangulWordSet.contains(CharacterIteration.current32(text)))) {
                    values[count] = maxSnlp;
                    lengths[count] = 1;
                    count++;
                }
                for (j = 0; j < count; j++) {
                    newSnlp = bestSnlp[i] + values[j];
                    if (newSnlp < bestSnlp[lengths[j] + i]) {
                        bestSnlp[lengths[j] + i] = newSnlp;
                        prev[lengths[j] + i] = i;
                    }
                }
                text.setIndex(i);
                boolean is_katakana = isKatakana(CharacterIteration.current32(text));
                if (!is_prev_katakana && is_katakana) {
                    j = i + 1;
                    CharacterIteration.next32(text);
                    while (j < numChars && j - i < kMaxKatakanaGroupLength && isKatakana(CharacterIteration.current32(text))) {
                        CharacterIteration.next32(text);
                        j++;
                    }
                    if (j - i < kMaxKatakanaGroupLength) {
                        newSnlp = bestSnlp[i] + getKatakanaCost(j - i);
                        if (newSnlp < bestSnlp[j]) {
                            bestSnlp[j] = newSnlp;
                            prev[j] = i;
                        }
                    }
                }
                is_prev_katakana = is_katakana;
            }
            i++;
        }
        int[] t_boundary = new int[(numChars + 1)];
        int numBreaks = 0;
        if (bestSnlp[numChars] == kint32max) {
            t_boundary[0] = numChars;
            numBreaks = 1;
        } else {
            for (i = numChars; i > 0; i = prev[i]) {
                t_boundary[numBreaks] = i;
                numBreaks++;
            }
            Assert.assrt(prev[t_boundary[numBreaks + -1]] == 0);
        }
        if (foundBreaks.size() == 0 || foundBreaks.peek() < startPos) {
            int numBreaks2 = numBreaks + 1;
            t_boundary[numBreaks] = 0;
            numBreaks = numBreaks2;
        }
        int correctedNumBreaks = 0;
        for (i = numBreaks - 1; i >= 0; i--) {
            int pos = charPositions[t_boundary[i]] + startPos;
            Object obj = (foundBreaks.contains(pos) || pos == startPos) ? 1 : null;
            if (obj == null) {
                foundBreaks.push(charPositions[t_boundary[i]] + startPos);
                correctedNumBreaks++;
            }
        }
        if (!foundBreaks.isEmpty() && foundBreaks.peek() == endPos) {
            foundBreaks.pop();
            correctedNumBreaks--;
        }
        if (!foundBreaks.isEmpty()) {
            inText.setIndex(foundBreaks.peek());
        }
        return correctedNumBreaks;
    }
}
