package java.awt.font;

import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

public final class TextAttribute extends Attribute {
    public static final TextAttribute BACKGROUND = null;
    public static final TextAttribute BIDI_EMBEDDING = null;
    public static final TextAttribute CHAR_REPLACEMENT = null;
    public static final TextAttribute FAMILY = null;
    public static final TextAttribute FONT = null;
    public static final TextAttribute FOREGROUND = null;
    public static final TextAttribute INPUT_METHOD_HIGHLIGHT = null;
    public static final TextAttribute INPUT_METHOD_UNDERLINE = null;
    public static final TextAttribute JUSTIFICATION = null;
    public static final Float JUSTIFICATION_FULL = null;
    public static final Float JUSTIFICATION_NONE = null;
    public static final TextAttribute KERNING = null;
    public static final Integer KERNING_ON = null;
    public static final TextAttribute LIGATURES = null;
    public static final Integer LIGATURES_ON = null;
    public static final TextAttribute NUMERIC_SHAPING = null;
    public static final TextAttribute POSTURE = null;
    public static final Float POSTURE_OBLIQUE = null;
    public static final Float POSTURE_REGULAR = null;
    public static final TextAttribute RUN_DIRECTION = null;
    public static final Boolean RUN_DIRECTION_LTR = null;
    public static final Boolean RUN_DIRECTION_RTL = null;
    public static final TextAttribute SIZE = null;
    public static final TextAttribute STRIKETHROUGH = null;
    public static final Boolean STRIKETHROUGH_ON = null;
    public static final TextAttribute SUPERSCRIPT = null;
    public static final Integer SUPERSCRIPT_SUB = null;
    public static final Integer SUPERSCRIPT_SUPER = null;
    public static final TextAttribute SWAP_COLORS = null;
    public static final Boolean SWAP_COLORS_ON = null;
    public static final TextAttribute TRACKING = null;
    public static final Float TRACKING_LOOSE = null;
    public static final Float TRACKING_TIGHT = null;
    public static final TextAttribute TRANSFORM = null;
    public static final TextAttribute UNDERLINE = null;
    public static final Integer UNDERLINE_LOW_DASHED = null;
    public static final Integer UNDERLINE_LOW_DOTTED = null;
    public static final Integer UNDERLINE_LOW_GRAY = null;
    public static final Integer UNDERLINE_LOW_ONE_PIXEL = null;
    public static final Integer UNDERLINE_LOW_TWO_PIXEL = null;
    public static final Integer UNDERLINE_ON = null;
    public static final TextAttribute WEIGHT = null;
    public static final Float WEIGHT_BOLD = null;
    public static final Float WEIGHT_DEMIBOLD = null;
    public static final Float WEIGHT_DEMILIGHT = null;
    public static final Float WEIGHT_EXTRABOLD = null;
    public static final Float WEIGHT_EXTRA_LIGHT = null;
    public static final Float WEIGHT_HEAVY = null;
    public static final Float WEIGHT_LIGHT = null;
    public static final Float WEIGHT_MEDIUM = null;
    public static final Float WEIGHT_REGULAR = null;
    public static final Float WEIGHT_SEMIBOLD = null;
    public static final Float WEIGHT_ULTRABOLD = null;
    public static final TextAttribute WIDTH = null;
    public static final Float WIDTH_CONDENSED = null;
    public static final Float WIDTH_EXTENDED = null;
    public static final Float WIDTH_REGULAR = null;
    public static final Float WIDTH_SEMI_CONDENSED = null;
    public static final Float WIDTH_SEMI_EXTENDED = null;
    private static final Map instanceMap = null;
    static final long serialVersionUID = 7744112784117861702L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.awt.font.TextAttribute.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.awt.font.TextAttribute.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.TextAttribute.<clinit>():void");
    }

    protected TextAttribute(String name) {
        super(name);
        if (getClass() == TextAttribute.class) {
            instanceMap.put(name, this);
        }
    }

    protected Object readResolve() throws InvalidObjectException {
        if (getClass() != TextAttribute.class) {
            throw new InvalidObjectException("subclass didn't correctly implement readResolve");
        }
        TextAttribute instance = (TextAttribute) instanceMap.get(getName());
        if (instance != null) {
            return instance;
        }
        throw new InvalidObjectException("unknown attribute name");
    }
}
