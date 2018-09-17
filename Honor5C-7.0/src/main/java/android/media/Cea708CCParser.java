package android.media;

import android.graphics.Color;
import android.net.NetworkPolicyManager;
import android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer;
import android.net.wifi.ScanResult.InformationElement;
import android.os.Process;
import android.provider.Downloads.Impl;
import android.renderscript.ScriptIntrinsicBLAS;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/* compiled from: Cea708CaptionRenderer */
class Cea708CCParser {
    public static final int CAPTION_EMIT_TYPE_BUFFER = 1;
    public static final int CAPTION_EMIT_TYPE_COMMAND_CLW = 4;
    public static final int CAPTION_EMIT_TYPE_COMMAND_CWX = 3;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DFX = 16;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLC = 10;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLW = 8;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DLY = 9;
    public static final int CAPTION_EMIT_TYPE_COMMAND_DSW = 5;
    public static final int CAPTION_EMIT_TYPE_COMMAND_HDW = 6;
    public static final int CAPTION_EMIT_TYPE_COMMAND_RST = 11;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPA = 12;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPC = 13;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SPL = 14;
    public static final int CAPTION_EMIT_TYPE_COMMAND_SWA = 15;
    public static final int CAPTION_EMIT_TYPE_COMMAND_TGW = 7;
    public static final int CAPTION_EMIT_TYPE_CONTROL = 2;
    private static final boolean DEBUG = false;
    private static final String MUSIC_NOTE_CHAR = null;
    private static final String TAG = "Cea708CCParser";
    private final StringBuffer mBuffer;
    private int mCommand;
    private DisplayListener mListener;

    /* compiled from: Cea708CaptionRenderer */
    interface DisplayListener {
        void emitEvent(CaptionEvent captionEvent);
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionColor {
        private static final int[] COLOR_MAP = null;
        public static final int OPACITY_FLASH = 1;
        private static final int[] OPACITY_MAP = null;
        public static final int OPACITY_SOLID = 0;
        public static final int OPACITY_TRANSLUCENT = 2;
        public static final int OPACITY_TRANSPARENT = 3;
        public final int blue;
        public final int green;
        public final int opacity;
        public final int red;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea708CCParser.CaptionColor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea708CCParser.CaptionColor.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.media.Cea708CCParser.CaptionColor.<clinit>():void");
        }

        public CaptionColor(int opacity, int red, int green, int blue) {
            this.opacity = opacity;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getArgbValue() {
            return Color.argb(OPACITY_MAP[this.opacity], COLOR_MAP[this.red], COLOR_MAP[this.green], COLOR_MAP[this.blue]);
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionEvent {
        public final Object obj;
        public final int type;

        public CaptionEvent(int type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionPenAttr {
        public static final int OFFSET_NORMAL = 1;
        public static final int OFFSET_SUBSCRIPT = 0;
        public static final int OFFSET_SUPERSCRIPT = 2;
        public static final int PEN_SIZE_LARGE = 2;
        public static final int PEN_SIZE_SMALL = 0;
        public static final int PEN_SIZE_STANDARD = 1;
        public final int edgeType;
        public final int fontTag;
        public final boolean italic;
        public final int penOffset;
        public final int penSize;
        public final int textTag;
        public final boolean underline;

        public CaptionPenAttr(int penSize, int penOffset, int textTag, int fontTag, int edgeType, boolean underline, boolean italic) {
            this.penSize = penSize;
            this.penOffset = penOffset;
            this.textTag = textTag;
            this.fontTag = fontTag;
            this.edgeType = edgeType;
            this.underline = underline;
            this.italic = italic;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionPenColor {
        public final CaptionColor backgroundColor;
        public final CaptionColor edgeColor;
        public final CaptionColor foregroundColor;

        public CaptionPenColor(CaptionColor foregroundColor, CaptionColor backgroundColor, CaptionColor edgeColor) {
            this.foregroundColor = foregroundColor;
            this.backgroundColor = backgroundColor;
            this.edgeColor = edgeColor;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionPenLocation {
        public final int column;
        public final int row;

        public CaptionPenLocation(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionWindow {
        public final int anchorHorizontal;
        public final int anchorId;
        public final int anchorVertical;
        public final int columnCount;
        public final boolean columnLock;
        public final int id;
        public final int penStyle;
        public final int priority;
        public final boolean relativePositioning;
        public final int rowCount;
        public final boolean rowLock;
        public final boolean visible;
        public final int windowStyle;

        public CaptionWindow(int id, boolean visible, boolean rowLock, boolean columnLock, int priority, boolean relativePositioning, int anchorVertical, int anchorHorizontal, int anchorId, int rowCount, int columnCount, int penStyle, int windowStyle) {
            this.id = id;
            this.visible = visible;
            this.rowLock = rowLock;
            this.columnLock = columnLock;
            this.priority = priority;
            this.relativePositioning = relativePositioning;
            this.anchorVertical = anchorVertical;
            this.anchorHorizontal = anchorHorizontal;
            this.anchorId = anchorId;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.penStyle = penStyle;
            this.windowStyle = windowStyle;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    public static class CaptionWindowAttr {
        public final CaptionColor borderColor;
        public final int borderType;
        public final int displayEffect;
        public final int effectDirection;
        public final int effectSpeed;
        public final CaptionColor fillColor;
        public final int justify;
        public final int printDirection;
        public final int scrollDirection;
        public final boolean wordWrap;

        public CaptionWindowAttr(CaptionColor fillColor, CaptionColor borderColor, int borderType, boolean wordWrap, int printDirection, int scrollDirection, int justify, int effectDirection, int effectSpeed, int displayEffect) {
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.borderType = borderType;
            this.wordWrap = wordWrap;
            this.printDirection = printDirection;
            this.scrollDirection = scrollDirection;
            this.justify = justify;
            this.effectDirection = effectDirection;
            this.effectSpeed = effectSpeed;
            this.displayEffect = displayEffect;
        }
    }

    /* compiled from: Cea708CaptionRenderer */
    private static class Const {
        public static final int CODE_C0_BS = 8;
        public static final int CODE_C0_CR = 13;
        public static final int CODE_C0_ETX = 3;
        public static final int CODE_C0_EXT1 = 16;
        public static final int CODE_C0_FF = 12;
        public static final int CODE_C0_HCR = 14;
        public static final int CODE_C0_NUL = 0;
        public static final int CODE_C0_P16 = 24;
        public static final int CODE_C0_RANGE_END = 31;
        public static final int CODE_C0_RANGE_START = 0;
        public static final int CODE_C0_SKIP1_RANGE_END = 23;
        public static final int CODE_C0_SKIP1_RANGE_START = 16;
        public static final int CODE_C0_SKIP2_RANGE_END = 31;
        public static final int CODE_C0_SKIP2_RANGE_START = 24;
        public static final int CODE_C1_CLW = 136;
        public static final int CODE_C1_CW0 = 128;
        public static final int CODE_C1_CW1 = 129;
        public static final int CODE_C1_CW2 = 130;
        public static final int CODE_C1_CW3 = 131;
        public static final int CODE_C1_CW4 = 132;
        public static final int CODE_C1_CW5 = 133;
        public static final int CODE_C1_CW6 = 134;
        public static final int CODE_C1_CW7 = 135;
        public static final int CODE_C1_DF0 = 152;
        public static final int CODE_C1_DF1 = 153;
        public static final int CODE_C1_DF2 = 154;
        public static final int CODE_C1_DF3 = 155;
        public static final int CODE_C1_DF4 = 156;
        public static final int CODE_C1_DF5 = 157;
        public static final int CODE_C1_DF6 = 158;
        public static final int CODE_C1_DF7 = 159;
        public static final int CODE_C1_DLC = 142;
        public static final int CODE_C1_DLW = 140;
        public static final int CODE_C1_DLY = 141;
        public static final int CODE_C1_DSW = 137;
        public static final int CODE_C1_HDW = 138;
        public static final int CODE_C1_RANGE_END = 159;
        public static final int CODE_C1_RANGE_START = 128;
        public static final int CODE_C1_RST = 143;
        public static final int CODE_C1_SPA = 144;
        public static final int CODE_C1_SPC = 145;
        public static final int CODE_C1_SPL = 146;
        public static final int CODE_C1_SWA = 151;
        public static final int CODE_C1_TGW = 139;
        public static final int CODE_C2_RANGE_END = 31;
        public static final int CODE_C2_RANGE_START = 0;
        public static final int CODE_C2_SKIP0_RANGE_END = 7;
        public static final int CODE_C2_SKIP0_RANGE_START = 0;
        public static final int CODE_C2_SKIP1_RANGE_END = 15;
        public static final int CODE_C2_SKIP1_RANGE_START = 8;
        public static final int CODE_C2_SKIP2_RANGE_END = 23;
        public static final int CODE_C2_SKIP2_RANGE_START = 16;
        public static final int CODE_C2_SKIP3_RANGE_END = 31;
        public static final int CODE_C2_SKIP3_RANGE_START = 24;
        public static final int CODE_C3_RANGE_END = 159;
        public static final int CODE_C3_RANGE_START = 128;
        public static final int CODE_C3_SKIP4_RANGE_END = 135;
        public static final int CODE_C3_SKIP4_RANGE_START = 128;
        public static final int CODE_C3_SKIP5_RANGE_END = 143;
        public static final int CODE_C3_SKIP5_RANGE_START = 136;
        public static final int CODE_G0_MUSICNOTE = 127;
        public static final int CODE_G0_RANGE_END = 127;
        public static final int CODE_G0_RANGE_START = 32;
        public static final int CODE_G1_RANGE_END = 255;
        public static final int CODE_G1_RANGE_START = 160;
        public static final int CODE_G2_BLK = 48;
        public static final int CODE_G2_NBTSP = 33;
        public static final int CODE_G2_RANGE_END = 127;
        public static final int CODE_G2_RANGE_START = 32;
        public static final int CODE_G2_TSP = 32;
        public static final int CODE_G3_CC = 160;
        public static final int CODE_G3_RANGE_END = 255;
        public static final int CODE_G3_RANGE_START = 160;

        private Const() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea708CCParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea708CCParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.Cea708CCParser.<clinit>():void");
    }

    Cea708CCParser(DisplayListener listener) {
        this.mBuffer = new StringBuffer();
        this.mCommand = 0;
        this.mListener = new DisplayListener() {
            public void emitEvent(CaptionEvent event) {
            }
        };
        if (listener != null) {
            this.mListener = listener;
        }
    }

    private void emitCaptionEvent(CaptionEvent captionEvent) {
        emitCaptionBuffer();
        this.mListener.emitEvent(captionEvent);
    }

    private void emitCaptionBuffer() {
        if (this.mBuffer.length() > 0) {
            this.mListener.emitEvent(new CaptionEvent(CAPTION_EMIT_TYPE_BUFFER, this.mBuffer.toString()));
            this.mBuffer.setLength(0);
        }
    }

    public void parse(byte[] data) {
        int pos = 0;
        while (pos < data.length) {
            pos = parseServiceBlockData(data, pos);
        }
        emitCaptionBuffer();
    }

    private int parseServiceBlockData(byte[] data, int pos) {
        this.mCommand = data[pos] & Process.PROC_TERM_MASK;
        pos += CAPTION_EMIT_TYPE_BUFFER;
        if (this.mCommand == CAPTION_EMIT_TYPE_COMMAND_DFX) {
            return parseExt1(data, pos);
        }
        if (this.mCommand >= 0 && this.mCommand <= 31) {
            return parseC0(data, pos);
        }
        if (this.mCommand >= KeymasterDefs.KM_ALGORITHM_HMAC && this.mCommand <= Const.CODE_C3_RANGE_END) {
            return parseC1(data, pos);
        }
        if (this.mCommand >= 32 && this.mCommand <= InformationElement.EID_EXTENDED_CAPS) {
            return parseG0(data, pos);
        }
        if (this.mCommand < Const.CODE_G3_RANGE_START || this.mCommand > Process.PROC_TERM_MASK) {
            return pos;
        }
        return parseG1(data, pos);
    }

    private int parseC0(byte[] data, int pos) {
        if (this.mCommand >= 24 && this.mCommand <= 31) {
            if (this.mCommand == 24) {
                try {
                    if (data[pos] == null) {
                        this.mBuffer.append((char) data[pos + CAPTION_EMIT_TYPE_BUFFER]);
                    } else {
                        this.mBuffer.append(new String(Arrays.copyOfRange(data, pos, pos + CAPTION_EMIT_TYPE_CONTROL), "EUC-KR"));
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "P16 Code - Could not find supported encoding", e);
                }
            }
            return pos + CAPTION_EMIT_TYPE_CONTROL;
        } else if (this.mCommand >= CAPTION_EMIT_TYPE_COMMAND_DFX && this.mCommand <= 23) {
            return pos + CAPTION_EMIT_TYPE_BUFFER;
        } else {
            switch (this.mCommand) {
                case TextToSpeech.SUCCESS /*0*/:
                    return pos;
                case CAPTION_EMIT_TYPE_COMMAND_CWX /*3*/:
                    emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_CONTROL, Character.valueOf((char) this.mCommand)));
                    return pos;
                case CAPTION_EMIT_TYPE_COMMAND_DLW /*8*/:
                    emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_CONTROL, Character.valueOf((char) this.mCommand)));
                    return pos;
                case CAPTION_EMIT_TYPE_COMMAND_SPA /*12*/:
                    emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_CONTROL, Character.valueOf((char) this.mCommand)));
                    return pos;
                case CAPTION_EMIT_TYPE_COMMAND_SPC /*13*/:
                    this.mBuffer.append('\n');
                    return pos;
                case CAPTION_EMIT_TYPE_COMMAND_SPL /*14*/:
                    emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_CONTROL, Character.valueOf((char) this.mCommand)));
                    return pos;
                default:
                    return pos;
            }
        }
    }

    private int parseC1(byte[] data, int pos) {
        int windowBitmap;
        switch (this.mCommand) {
            case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
            case IllegalCharacterValueSanitizer.AMP_AND_SPACE_LEGAL /*129*/:
            case Const.CODE_C1_CW2 /*130*/:
            case ScriptIntrinsicBLAS.NON_UNIT /*131*/:
            case ScriptIntrinsicBLAS.UNIT /*132*/:
            case Const.CODE_C1_CW5 /*133*/:
            case Const.CODE_C1_CW6 /*134*/:
            case Const.CODE_C3_SKIP4_RANGE_END /*135*/:
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_CWX, Integer.valueOf(this.mCommand - 128)));
                return pos;
            case Const.CODE_C3_SKIP5_RANGE_START /*136*/:
                windowBitmap = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_CLW, Integer.valueOf(r0)));
                return pos;
            case Const.CODE_C1_DSW /*137*/:
                windowBitmap = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_DSW, Integer.valueOf(r0)));
                return pos;
            case Const.CODE_C1_HDW /*138*/:
                windowBitmap = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_HDW, Integer.valueOf(r0)));
                return pos;
            case Const.CODE_C1_TGW /*139*/:
                windowBitmap = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_TGW, Integer.valueOf(r0)));
                return pos;
            case Const.CODE_C1_DLW /*140*/:
                windowBitmap = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_DLW, Integer.valueOf(r0)));
                return pos;
            case ScriptIntrinsicBLAS.LEFT /*141*/:
                int tenthsOfSeconds = data[pos] & Process.PROC_TERM_MASK;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_DLY, Integer.valueOf(r0)));
                return pos;
            case ScriptIntrinsicBLAS.RIGHT /*142*/:
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_DLC, null));
                return pos;
            case Const.CODE_C3_SKIP5_RANGE_END /*143*/:
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_RST, null));
                return pos;
            case Const.CODE_C1_SPA /*144*/:
                int textTag = (data[pos] & NetworkPolicyManager.MASK_ALL_NETWORKS) >> CAPTION_EMIT_TYPE_COMMAND_CLW;
                int penSize = data[pos] & CAPTION_EMIT_TYPE_COMMAND_CWX;
                int penOffset = (data[pos] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL;
                boolean italic = (data[pos + CAPTION_EMIT_TYPE_BUFFER] & KeymasterDefs.KM_ALGORITHM_HMAC) != 0 ? true : DEBUG;
                boolean underline = (data[pos + CAPTION_EMIT_TYPE_BUFFER] & 64) != 0 ? true : DEBUG;
                int edgeType = (data[pos + CAPTION_EMIT_TYPE_BUFFER] & 56) >> CAPTION_EMIT_TYPE_COMMAND_CWX;
                pos += CAPTION_EMIT_TYPE_CONTROL;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_SPA, new CaptionPenAttr(penSize, penOffset, textTag, data[pos + CAPTION_EMIT_TYPE_BUFFER] & CAPTION_EMIT_TYPE_COMMAND_TGW, edgeType, underline, italic)));
                return pos;
            case Const.CODE_C1_SPC /*145*/:
                pos += CAPTION_EMIT_TYPE_BUFFER;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                pos += CAPTION_EMIT_TYPE_BUFFER;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_SPC, new CaptionPenColor(new CaptionColor((data[pos] & Impl.STATUS_RUNNING) >> CAPTION_EMIT_TYPE_COMMAND_HDW, (data[pos] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW, (data[pos] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, data[pos] & CAPTION_EMIT_TYPE_COMMAND_CWX), new CaptionColor((data[pos] & Impl.STATUS_RUNNING) >> CAPTION_EMIT_TYPE_COMMAND_HDW, (data[pos] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW, (data[pos] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, data[pos] & CAPTION_EMIT_TYPE_COMMAND_CWX), new CaptionColor(0, (data[pos] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW, (data[pos] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, data[pos] & CAPTION_EMIT_TYPE_COMMAND_CWX))));
                return pos;
            case Const.CODE_C1_SPL /*146*/:
                int row = data[pos] & CAPTION_EMIT_TYPE_COMMAND_SWA;
                pos += CAPTION_EMIT_TYPE_CONTROL;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_SPL, new CaptionPenLocation(row, data[pos + CAPTION_EMIT_TYPE_BUFFER] & 63)));
                return pos;
            case Const.CODE_C1_SWA /*151*/:
                CaptionColor fillColor = new CaptionColor((data[pos] & Impl.STATUS_RUNNING) >> CAPTION_EMIT_TYPE_COMMAND_HDW, (data[pos] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW, (data[pos] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, data[pos] & CAPTION_EMIT_TYPE_COMMAND_CWX);
                int borderType = ((data[pos + CAPTION_EMIT_TYPE_BUFFER] & Impl.STATUS_RUNNING) >> CAPTION_EMIT_TYPE_COMMAND_HDW) | ((data[pos + CAPTION_EMIT_TYPE_CONTROL] & KeymasterDefs.KM_ALGORITHM_HMAC) >> CAPTION_EMIT_TYPE_COMMAND_DSW);
                CaptionColor borderColor = new CaptionColor(0, (data[pos + CAPTION_EMIT_TYPE_BUFFER] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW, (data[pos + CAPTION_EMIT_TYPE_BUFFER] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, data[pos + CAPTION_EMIT_TYPE_BUFFER] & CAPTION_EMIT_TYPE_COMMAND_CWX);
                boolean wordWrap = (data[pos + CAPTION_EMIT_TYPE_CONTROL] & 64) != 0 ? true : DEBUG;
                int printDirection = (data[pos + CAPTION_EMIT_TYPE_CONTROL] & 48) >> CAPTION_EMIT_TYPE_COMMAND_CLW;
                int scrollDirection = (data[pos + CAPTION_EMIT_TYPE_CONTROL] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL;
                int justify = data[pos + CAPTION_EMIT_TYPE_CONTROL] & CAPTION_EMIT_TYPE_COMMAND_CWX;
                int effectSpeed = (data[pos + CAPTION_EMIT_TYPE_COMMAND_CWX] & NetworkPolicyManager.MASK_ALL_NETWORKS) >> CAPTION_EMIT_TYPE_COMMAND_CLW;
                pos += CAPTION_EMIT_TYPE_COMMAND_CLW;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_SWA, new CaptionWindowAttr(fillColor, borderColor, borderType, wordWrap, printDirection, scrollDirection, justify, (data[pos + CAPTION_EMIT_TYPE_COMMAND_CWX] & CAPTION_EMIT_TYPE_COMMAND_SPA) >> CAPTION_EMIT_TYPE_CONTROL, effectSpeed, data[pos + CAPTION_EMIT_TYPE_COMMAND_CWX] & CAPTION_EMIT_TYPE_COMMAND_CWX)));
                return pos;
            case Const.CODE_C1_DF0 /*152*/:
            case Const.CODE_C1_DF1 /*153*/:
            case Const.CODE_C1_DF2 /*154*/:
            case Const.CODE_C1_DF3 /*155*/:
            case Const.CODE_C1_DF4 /*156*/:
            case Const.CODE_C1_DF5 /*157*/:
            case Const.CODE_C1_DF6 /*158*/:
            case Const.CODE_C3_RANGE_END /*159*/:
                int windowId = this.mCommand - 152;
                boolean visible = (data[pos] & 32) != 0 ? true : DEBUG;
                boolean rowLock = (data[pos] & CAPTION_EMIT_TYPE_COMMAND_DFX) != 0 ? true : DEBUG;
                boolean columnLock = (data[pos] & CAPTION_EMIT_TYPE_COMMAND_DLW) != 0 ? true : DEBUG;
                int priority = data[pos] & CAPTION_EMIT_TYPE_COMMAND_TGW;
                boolean relativePositioning = (data[pos + CAPTION_EMIT_TYPE_BUFFER] & KeymasterDefs.KM_ALGORITHM_HMAC) != 0 ? true : DEBUG;
                int anchorVertical = data[pos + CAPTION_EMIT_TYPE_BUFFER] & InformationElement.EID_EXTENDED_CAPS;
                int anchorHorizontal = data[pos + CAPTION_EMIT_TYPE_CONTROL] & Process.PROC_TERM_MASK;
                int anchorId = (data[pos + CAPTION_EMIT_TYPE_COMMAND_CWX] & NetworkPolicyManager.MASK_ALL_NETWORKS) >> CAPTION_EMIT_TYPE_COMMAND_CLW;
                int rowCount = data[pos + CAPTION_EMIT_TYPE_COMMAND_CWX] & CAPTION_EMIT_TYPE_COMMAND_SWA;
                int columnCount = data[pos + CAPTION_EMIT_TYPE_COMMAND_CLW] & 63;
                int windowStyle = (data[pos + CAPTION_EMIT_TYPE_COMMAND_DSW] & 56) >> CAPTION_EMIT_TYPE_COMMAND_CWX;
                pos += CAPTION_EMIT_TYPE_COMMAND_HDW;
                emitCaptionEvent(new CaptionEvent(CAPTION_EMIT_TYPE_COMMAND_DFX, new CaptionWindow(windowId, visible, rowLock, columnLock, priority, relativePositioning, anchorVertical, anchorHorizontal, anchorId, rowCount, columnCount, data[pos + CAPTION_EMIT_TYPE_COMMAND_DSW] & CAPTION_EMIT_TYPE_COMMAND_TGW, windowStyle)));
                return pos;
            default:
                return pos;
        }
    }

    private int parseG0(byte[] data, int pos) {
        if (this.mCommand == InformationElement.EID_EXTENDED_CAPS) {
            this.mBuffer.append(MUSIC_NOTE_CHAR);
        } else {
            this.mBuffer.append((char) this.mCommand);
        }
        return pos;
    }

    private int parseG1(byte[] data, int pos) {
        this.mBuffer.append((char) this.mCommand);
        return pos;
    }

    private int parseExt1(byte[] data, int pos) {
        this.mCommand = data[pos] & Process.PROC_TERM_MASK;
        pos += CAPTION_EMIT_TYPE_BUFFER;
        if (this.mCommand >= 0 && this.mCommand <= 31) {
            return parseC2(data, pos);
        }
        if (this.mCommand >= KeymasterDefs.KM_ALGORITHM_HMAC && this.mCommand <= Const.CODE_C3_RANGE_END) {
            return parseC3(data, pos);
        }
        if (this.mCommand >= 32 && this.mCommand <= InformationElement.EID_EXTENDED_CAPS) {
            return parseG2(data, pos);
        }
        if (this.mCommand < Const.CODE_G3_RANGE_START || this.mCommand > Process.PROC_TERM_MASK) {
            return pos;
        }
        return parseG3(data, pos);
    }

    private int parseC2(byte[] data, int pos) {
        if (this.mCommand >= 0 && this.mCommand <= CAPTION_EMIT_TYPE_COMMAND_TGW) {
            return pos;
        }
        if (this.mCommand >= CAPTION_EMIT_TYPE_COMMAND_DLW && this.mCommand <= CAPTION_EMIT_TYPE_COMMAND_SWA) {
            return pos + CAPTION_EMIT_TYPE_BUFFER;
        }
        if (this.mCommand >= CAPTION_EMIT_TYPE_COMMAND_DFX && this.mCommand <= 23) {
            return pos + CAPTION_EMIT_TYPE_CONTROL;
        }
        if (this.mCommand < 24 || this.mCommand > 31) {
            return pos;
        }
        return pos + CAPTION_EMIT_TYPE_COMMAND_CWX;
    }

    private int parseC3(byte[] data, int pos) {
        if (this.mCommand >= KeymasterDefs.KM_ALGORITHM_HMAC && this.mCommand <= Const.CODE_C3_SKIP4_RANGE_END) {
            return pos + CAPTION_EMIT_TYPE_COMMAND_CLW;
        }
        if (this.mCommand < Const.CODE_C3_SKIP5_RANGE_START || this.mCommand > Const.CODE_C3_SKIP5_RANGE_END) {
            return pos;
        }
        return pos + CAPTION_EMIT_TYPE_COMMAND_DSW;
    }

    private int parseG2(byte[] data, int pos) {
        switch (this.mCommand) {
        }
        return pos;
    }

    private int parseG3(byte[] data, int pos) {
        return this.mCommand == Const.CODE_G3_RANGE_START ? pos : pos;
    }
}
