package android.text;

import java.io.IOException;

public interface Editable extends CharSequence, GetChars, Spannable, Appendable {

    public static class Factory {
        private static Factory sInstance;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Editable.Factory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Editable.Factory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.Editable.Factory.<clinit>():void");
        }

        public static Factory getInstance() {
            return sInstance;
        }

        public Editable newEditable(CharSequence source) {
            return new SpannableStringBuilder(source);
        }
    }

    Editable append(char c);

    Editable append(CharSequence charSequence);

    Editable append(CharSequence charSequence, int i, int i2);

    void clear();

    void clearSpans();

    Editable delete(int i, int i2);

    InputFilter[] getFilters();

    Editable insert(int i, CharSequence charSequence);

    Editable insert(int i, CharSequence charSequence, int i2, int i3);

    Editable replace(int i, int i2, CharSequence charSequence);

    Editable replace(int i, int i2, CharSequence charSequence, int i3, int i4);

    void setFilters(InputFilter[] inputFilterArr);

    /* bridge */ /* synthetic */ Appendable m1append(CharSequence text) throws IOException {
        return append(text);
    }

    /* bridge */ /* synthetic */ Appendable m2append(CharSequence text, int start, int end) throws IOException {
        return append(text, start, end);
    }

    /* bridge */ /* synthetic */ Appendable m0append(char text) throws IOException {
        return append(text);
    }
}
