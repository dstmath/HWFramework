package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class Soundex implements StringEncoder {
    public static final Soundex US_ENGLISH = null;
    public static final char[] US_ENGLISH_MAPPING = null;
    public static final String US_ENGLISH_MAPPING_STRING = "01230120022455012623010202";
    private int maxLength;
    private char[] soundexMapping;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.commons.codec.language.Soundex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.commons.codec.language.Soundex.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.codec.language.Soundex.<clinit>():void");
    }

    public int difference(String s1, String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }

    public Soundex() {
        this(US_ENGLISH_MAPPING);
    }

    public Soundex(char[] mapping) {
        this.maxLength = 4;
        setSoundexMapping(mapping);
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject instanceof String) {
            return soundex((String) pObject);
        }
        throw new EncoderException("Parameter supplied to Soundex encode is not of type java.lang.String");
    }

    public String encode(String pString) {
        return soundex(pString);
    }

    private char getMappingCode(String str, int index) {
        char mappedChar = map(str.charAt(index));
        if (index > 1 && mappedChar != '0') {
            char hwChar = str.charAt(index - 1);
            if ('H' == hwChar || 'W' == hwChar) {
                char preHWChar = str.charAt(index - 2);
                if (map(preHWChar) == mappedChar || 'H' == preHWChar || 'W' == preHWChar) {
                    return '\u0000';
                }
            }
        }
        return mappedChar;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    private char[] getSoundexMapping() {
        return this.soundexMapping;
    }

    private char map(char ch) {
        int index = ch - 65;
        if (index >= 0 && index < getSoundexMapping().length) {
            return getSoundexMapping()[index];
        }
        throw new IllegalArgumentException("The character is not mapped: " + ch);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    private void setSoundexMapping(char[] soundexMapping) {
        this.soundexMapping = soundexMapping;
    }

    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.length() == 0) {
            return str;
        }
        char[] out = new char[]{'0', '0', '0', '0'};
        int incount = 1;
        int count = 1;
        out[0] = str.charAt(0);
        char last = getMappingCode(str, 0);
        while (incount < str.length() && count < out.length) {
            int incount2 = incount + 1;
            char mapped = getMappingCode(str, incount);
            if (mapped != '\u0000') {
                if (!(mapped == '0' || mapped == last)) {
                    int count2 = count + 1;
                    out[count] = mapped;
                    count = count2;
                }
                last = mapped;
            }
            incount = incount2;
        }
        return new String(out);
    }
}
