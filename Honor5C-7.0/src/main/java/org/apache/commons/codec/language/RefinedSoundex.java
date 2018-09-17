package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class RefinedSoundex implements StringEncoder {
    public static final RefinedSoundex US_ENGLISH = null;
    public static final char[] US_ENGLISH_MAPPING = null;
    private char[] soundexMapping;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.commons.codec.language.RefinedSoundex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.commons.codec.language.RefinedSoundex.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.codec.language.RefinedSoundex.<clinit>():void");
    }

    public RefinedSoundex() {
        this(US_ENGLISH_MAPPING);
    }

    public RefinedSoundex(char[] mapping) {
        this.soundexMapping = mapping;
    }

    public int difference(String s1, String s2) throws EncoderException {
        return SoundexUtils.difference(this, s1, s2);
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject instanceof String) {
            return soundex((String) pObject);
        }
        throw new EncoderException("Parameter supplied to RefinedSoundex encode is not of type java.lang.String");
    }

    public String encode(String pString) {
        return soundex(pString);
    }

    char getMappingCode(char c) {
        if (Character.isLetter(c)) {
            return this.soundexMapping[Character.toUpperCase(c) - 65];
        }
        return '\u0000';
    }

    public String soundex(String str) {
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.length() == 0) {
            return str;
        }
        StringBuffer sBuf = new StringBuffer();
        sBuf.append(str.charAt(0));
        char last = '*';
        for (int i = 0; i < str.length(); i++) {
            char current = getMappingCode(str.charAt(i));
            if (current != last) {
                if (current != '\u0000') {
                    sBuf.append(current);
                }
                last = current;
            }
        }
        return sBuf.toString();
    }
}
