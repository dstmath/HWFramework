package android.icu.text;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.lang.UCharacterEnums.ECharacterDirection;
import android.icu.util.AnnualTimeZoneRule;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class UnicodeCompressor implements SCSU {
    private static boolean[] sSingleTagTable;
    private static boolean[] sUnicodeTagTable;
    private int fCurrentWindow;
    private int[] fIndexCount;
    private int fMode;
    private int[] fOffsets;
    private int fTimeStamp;
    private int[] fTimeStamps;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.UnicodeCompressor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.UnicodeCompressor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.UnicodeCompressor.<clinit>():void");
    }

    public UnicodeCompressor() {
        this.fCurrentWindow = 0;
        this.fOffsets = new int[8];
        this.fMode = 0;
        this.fIndexCount = new int[NodeFilter.SHOW_DOCUMENT];
        this.fTimeStamps = new int[8];
        this.fTimeStamp = 0;
        reset();
    }

    public static byte[] compress(String buffer) {
        return compress(buffer.toCharArray(), 0, buffer.length());
    }

    public static byte[] compress(char[] buffer, int start, int limit) {
        UnicodeCompressor comp = new UnicodeCompressor();
        int len = Math.max(4, ((limit - start) * 3) + 1);
        byte[] temp = new byte[len];
        int byteCount = comp.compress(buffer, start, limit, null, temp, 0, len);
        byte[] result = new byte[byteCount];
        System.arraycopy(temp, 0, result, 0, byteCount);
        return result;
    }

    public int compress(char[] charBuffer, int charBufferStart, int charBufferLimit, int[] charsRead, byte[] byteBuffer, int byteBufferStart, int byteBufferLimit) {
        int bytePos = byteBufferStart;
        int ucPos = charBufferStart;
        if (byteBuffer.length < 4 || byteBufferLimit - byteBufferStart < 4) {
            throw new IllegalArgumentException("byteBuffer.length < 4");
        }
        while (ucPos < charBufferLimit && bytePos < byteBufferLimit) {
            int ucPos2;
            int bytePos2;
            int curUC;
            int nextUC;
            int loByte;
            int whichWindow;
            int forwardUC;
            int[] iArr;
            int i;
            int curIndex;
            int hiByte;
            switch (this.fMode) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    ucPos2 = ucPos;
                    bytePos2 = bytePos;
                    while (ucPos2 < charBufferLimit && bytePos2 < byteBufferLimit) {
                        ucPos = ucPos2 + 1;
                        curUC = charBuffer[ucPos2];
                        if (ucPos < charBufferLimit) {
                            nextUC = charBuffer[ucPos];
                        } else {
                            nextUC = -1;
                        }
                        if (curUC < NodeFilter.SHOW_COMMENT) {
                            loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                            if (!sSingleTagTable[loByte]) {
                                bytePos = bytePos2;
                            } else if (bytePos2 + 1 >= byteBufferLimit) {
                                ucPos--;
                                bytePos = bytePos2;
                                break;
                            } else {
                                bytePos = bytePos2 + 1;
                                byteBuffer[bytePos2] = (byte) 1;
                            }
                            bytePos2 = bytePos + 1;
                            byteBuffer[bytePos] = (byte) loByte;
                            bytePos = bytePos2;
                        } else if (inDynamicWindow(curUC, this.fCurrentWindow)) {
                            bytePos = bytePos2 + 1;
                            byteBuffer[bytePos2] = (byte) ((curUC - this.fOffsets[this.fCurrentWindow]) + NodeFilter.SHOW_COMMENT);
                        } else if (isCompressible(curUC)) {
                            whichWindow = findDynamicWindow(curUC);
                            if (whichWindow != -1) {
                                if (ucPos + 1 < charBufferLimit) {
                                    forwardUC = charBuffer[ucPos + 1];
                                } else {
                                    forwardUC = -1;
                                }
                                if (inDynamicWindow(nextUC, whichWindow) && inDynamicWindow(forwardUC, whichWindow)) {
                                    if (bytePos2 + 1 >= byteBufferLimit) {
                                        ucPos--;
                                        bytePos = bytePos2;
                                        break;
                                    }
                                    bytePos = bytePos2 + 1;
                                    byteBuffer[bytePos2] = (byte) (whichWindow + 16);
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) ((curUC - this.fOffsets[whichWindow]) + NodeFilter.SHOW_COMMENT);
                                    iArr = this.fTimeStamps;
                                    i = this.fTimeStamp + 1;
                                    this.fTimeStamp = i;
                                    iArr[whichWindow] = i;
                                    this.fCurrentWindow = whichWindow;
                                    bytePos = bytePos2;
                                } else if (bytePos2 + 1 >= byteBufferLimit) {
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else {
                                    bytePos = bytePos2 + 1;
                                    byteBuffer[bytePos2] = (byte) (whichWindow + 1);
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) ((curUC - this.fOffsets[whichWindow]) + NodeFilter.SHOW_COMMENT);
                                    bytePos = bytePos2;
                                }
                            } else {
                                whichWindow = findStaticWindow(curUC);
                                if (whichWindow == -1 || inStaticWindow(nextUC, whichWindow)) {
                                    curIndex = makeIndex(curUC);
                                    iArr = this.fIndexCount;
                                    iArr[curIndex] = iArr[curIndex] + 1;
                                    if (ucPos + 1 < charBufferLimit) {
                                        forwardUC = charBuffer[ucPos + 1];
                                    } else {
                                        forwardUC = -1;
                                    }
                                    if (this.fIndexCount[curIndex] <= 1 && (curIndex != makeIndex(nextUC) || curIndex != makeIndex(forwardUC))) {
                                        if (bytePos2 + 3 < byteBufferLimit) {
                                            bytePos = bytePos2 + 1;
                                            byteBuffer[bytePos2] = ECharacterDirection.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;
                                            hiByte = curUC >>> 8;
                                            loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                                            if (sUnicodeTagTable[hiByte]) {
                                                bytePos2 = bytePos + 1;
                                                byteBuffer[bytePos] = (byte) -16;
                                                bytePos = bytePos2;
                                            }
                                            bytePos2 = bytePos + 1;
                                            byteBuffer[bytePos] = (byte) hiByte;
                                            bytePos = bytePos2 + 1;
                                            byteBuffer[bytePos2] = (byte) loByte;
                                            this.fMode = 1;
                                            break;
                                        }
                                        ucPos--;
                                        bytePos = bytePos2;
                                        break;
                                    } else if (bytePos2 + 2 >= byteBufferLimit) {
                                        ucPos--;
                                        bytePos = bytePos2;
                                        break;
                                    } else {
                                        whichWindow = getLRDefinedWindow();
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) (whichWindow + 24);
                                        bytePos2 = bytePos + 1;
                                        byteBuffer[bytePos] = (byte) curIndex;
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) ((curUC - sOffsetTable[curIndex]) + NodeFilter.SHOW_COMMENT);
                                        this.fOffsets[whichWindow] = sOffsetTable[curIndex];
                                        this.fCurrentWindow = whichWindow;
                                        iArr = this.fTimeStamps;
                                        i = this.fTimeStamp + 1;
                                        this.fTimeStamp = i;
                                        iArr[whichWindow] = i;
                                    }
                                } else if (bytePos2 + 1 >= byteBufferLimit) {
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else {
                                    bytePos = bytePos2 + 1;
                                    byteBuffer[bytePos2] = (byte) (whichWindow + 1);
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) (curUC - sOffsets[whichWindow]);
                                    bytePos = bytePos2;
                                }
                            }
                        } else if (nextUC == -1 || !isCompressible(nextUC)) {
                            if (bytePos2 + 3 < byteBufferLimit) {
                                bytePos = bytePos2 + 1;
                                byteBuffer[bytePos2] = ECharacterDirection.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;
                                hiByte = curUC >>> 8;
                                loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                                if (sUnicodeTagTable[hiByte]) {
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) -16;
                                    bytePos = bytePos2;
                                }
                                bytePos2 = bytePos + 1;
                                byteBuffer[bytePos] = (byte) hiByte;
                                bytePos = bytePos2 + 1;
                                byteBuffer[bytePos2] = (byte) loByte;
                                this.fMode = 1;
                                break;
                            }
                            ucPos--;
                            bytePos = bytePos2;
                            break;
                        } else if (bytePos2 + 2 >= byteBufferLimit) {
                            ucPos--;
                            bytePos = bytePos2;
                            break;
                        } else {
                            bytePos = bytePos2 + 1;
                            byteBuffer[bytePos2] = ECharacterDirection.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING;
                            bytePos2 = bytePos + 1;
                            byteBuffer[bytePos] = (byte) (curUC >>> 8);
                            bytePos = bytePos2 + 1;
                            byteBuffer[bytePos2] = (byte) (curUC & Opcodes.OP_CONST_CLASS_JUMBO);
                        }
                        ucPos2 = ucPos;
                        bytePos2 = bytePos;
                    }
                    ucPos = ucPos2;
                    bytePos = bytePos2;
                    continue;
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    ucPos2 = ucPos;
                    bytePos2 = bytePos;
                    while (ucPos2 < charBufferLimit && bytePos2 < byteBufferLimit) {
                        ucPos = ucPos2 + 1;
                        curUC = charBuffer[ucPos2];
                        if (ucPos < charBufferLimit) {
                            nextUC = charBuffer[ucPos];
                        } else {
                            nextUC = -1;
                        }
                        if (isCompressible(curUC) && (nextUC == -1 || isCompressible(nextUC))) {
                            if (curUC < NodeFilter.SHOW_COMMENT) {
                                loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                                if (nextUC != -1 && nextUC < NodeFilter.SHOW_COMMENT && !sSingleTagTable[loByte]) {
                                    if (bytePos2 + 1 < byteBufferLimit) {
                                        whichWindow = this.fCurrentWindow;
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) (whichWindow + Opcodes.OP_SHL_INT_LIT8);
                                        bytePos2 = bytePos + 1;
                                        byteBuffer[bytePos] = (byte) loByte;
                                        iArr = this.fTimeStamps;
                                        i = this.fTimeStamp + 1;
                                        this.fTimeStamp = i;
                                        iArr[whichWindow] = i;
                                        this.fMode = 0;
                                        bytePos = bytePos2;
                                        break;
                                    }
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else if (bytePos2 + 1 >= byteBufferLimit) {
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else {
                                    bytePos = bytePos2 + 1;
                                    byteBuffer[bytePos2] = (byte) 0;
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) loByte;
                                    bytePos = bytePos2;
                                }
                            } else {
                                whichWindow = findDynamicWindow(curUC);
                                if (whichWindow == -1) {
                                    curIndex = makeIndex(curUC);
                                    iArr = this.fIndexCount;
                                    iArr[curIndex] = iArr[curIndex] + 1;
                                    if (ucPos + 1 < charBufferLimit) {
                                        forwardUC = charBuffer[ucPos + 1];
                                    } else {
                                        forwardUC = -1;
                                    }
                                    if (this.fIndexCount[curIndex] > 1 || (curIndex == makeIndex(nextUC) && curIndex == makeIndex(forwardUC))) {
                                        if (bytePos2 + 2 < byteBufferLimit) {
                                            whichWindow = getLRDefinedWindow();
                                            bytePos = bytePos2 + 1;
                                            byteBuffer[bytePos2] = (byte) (whichWindow + Opcodes.OP_IGET_WIDE_VOLATILE);
                                            bytePos2 = bytePos + 1;
                                            byteBuffer[bytePos] = (byte) curIndex;
                                            bytePos = bytePos2 + 1;
                                            byteBuffer[bytePos2] = (byte) ((curUC - sOffsetTable[curIndex]) + NodeFilter.SHOW_COMMENT);
                                            this.fOffsets[whichWindow] = sOffsetTable[curIndex];
                                            this.fCurrentWindow = whichWindow;
                                            iArr = this.fTimeStamps;
                                            i = this.fTimeStamp + 1;
                                            this.fTimeStamp = i;
                                            iArr[whichWindow] = i;
                                            this.fMode = 0;
                                            break;
                                        }
                                        ucPos--;
                                        bytePos = bytePos2;
                                        break;
                                    } else if (bytePos2 + 2 >= byteBufferLimit) {
                                        ucPos--;
                                        bytePos = bytePos2;
                                        break;
                                    } else {
                                        hiByte = curUC >>> 8;
                                        loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                                        if (sUnicodeTagTable[hiByte]) {
                                            bytePos = bytePos2 + 1;
                                            byteBuffer[bytePos2] = (byte) -16;
                                        } else {
                                            bytePos = bytePos2;
                                        }
                                        bytePos2 = bytePos + 1;
                                        byteBuffer[bytePos] = (byte) hiByte;
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) loByte;
                                    }
                                } else if (inDynamicWindow(nextUC, whichWindow)) {
                                    if (bytePos2 + 1 < byteBufferLimit) {
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) (whichWindow + Opcodes.OP_SHL_INT_LIT8);
                                        bytePos2 = bytePos + 1;
                                        byteBuffer[bytePos] = (byte) ((curUC - this.fOffsets[whichWindow]) + NodeFilter.SHOW_COMMENT);
                                        iArr = this.fTimeStamps;
                                        i = this.fTimeStamp + 1;
                                        this.fTimeStamp = i;
                                        iArr[whichWindow] = i;
                                        this.fCurrentWindow = whichWindow;
                                        this.fMode = 0;
                                        bytePos = bytePos2;
                                        break;
                                    }
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else if (bytePos2 + 2 >= byteBufferLimit) {
                                    ucPos--;
                                    bytePos = bytePos2;
                                    break;
                                } else {
                                    hiByte = curUC >>> 8;
                                    loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                                    if (sUnicodeTagTable[hiByte]) {
                                        bytePos = bytePos2 + 1;
                                        byteBuffer[bytePos2] = (byte) -16;
                                    } else {
                                        bytePos = bytePos2;
                                    }
                                    bytePos2 = bytePos + 1;
                                    byteBuffer[bytePos] = (byte) hiByte;
                                    bytePos = bytePos2 + 1;
                                    byteBuffer[bytePos2] = (byte) loByte;
                                }
                            }
                        } else if (bytePos2 + 2 >= byteBufferLimit) {
                            ucPos--;
                            bytePos = bytePos2;
                            break;
                        } else {
                            hiByte = curUC >>> 8;
                            loByte = curUC & Opcodes.OP_CONST_CLASS_JUMBO;
                            if (sUnicodeTagTable[hiByte]) {
                                bytePos = bytePos2 + 1;
                                byteBuffer[bytePos2] = (byte) -16;
                            } else {
                                bytePos = bytePos2;
                            }
                            bytePos2 = bytePos + 1;
                            byteBuffer[bytePos] = (byte) hiByte;
                            bytePos = bytePos2 + 1;
                            byteBuffer[bytePos2] = (byte) loByte;
                        }
                        ucPos2 = ucPos;
                        bytePos2 = bytePos;
                    }
                    ucPos = ucPos2;
                    bytePos = bytePos2;
                    continue;
                default:
                    continue;
            }
        }
        if (charsRead != null) {
            charsRead[0] = ucPos - charBufferStart;
        }
        return bytePos - byteBufferStart;
    }

    public void reset() {
        int i;
        this.fOffsets[0] = NodeFilter.SHOW_COMMENT;
        this.fOffsets[1] = Opcodes.OP_AND_LONG_2ADDR;
        this.fOffsets[2] = NodeFilter.SHOW_DOCUMENT_FRAGMENT;
        this.fOffsets[3] = 1536;
        this.fOffsets[4] = 2304;
        this.fOffsets[5] = 12352;
        this.fOffsets[6] = 12448;
        this.fOffsets[7] = Normalizer2Impl.JAMO_VT;
        for (i = 0; i < 8; i++) {
            this.fTimeStamps[i] = 0;
        }
        for (i = 0; i <= Opcodes.OP_CONST_CLASS_JUMBO; i++) {
            this.fIndexCount[i] = 0;
        }
        this.fTimeStamp = 0;
        this.fCurrentWindow = 0;
        this.fMode = 0;
    }

    private static int makeIndex(int c) {
        if (c >= Opcodes.OP_AND_LONG_2ADDR && c < 320) {
            return Opcodes.OP_INVOKE_VIRTUAL_QUICK_RANGE;
        }
        if (c >= 592 && c < 720) {
            return Opcodes.OP_INVOKE_SUPER_QUICK;
        }
        if (c >= 880 && c < 1008) {
            return Opcodes.OP_INVOKE_SUPER_QUICK_RANGE;
        }
        if (c >= 1328 && c < 1424) {
            return SCSU.ARMENIANINDEX;
        }
        if (c >= 12352 && c < 12448) {
            return SCSU.HIRAGANAINDEX;
        }
        if (c >= 12448 && c < 12576) {
            return SCSU.KATAKANAINDEX;
        }
        if (c >= 65376 && c < 65439) {
            return Opcodes.OP_CONST_CLASS_JUMBO;
        }
        if (c >= NodeFilter.SHOW_COMMENT && c < Normalizer2Impl.COMP_1_TRAIL_LIMIT) {
            return (c / NodeFilter.SHOW_COMMENT) & Opcodes.OP_CONST_CLASS_JUMBO;
        }
        if (c < 57344 || c > DexFormat.MAX_TYPE_IDX) {
            return 0;
        }
        return ((c - Hangul.HANGUL_BASE) / NodeFilter.SHOW_COMMENT) & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    private boolean inDynamicWindow(int c, int whichWindow) {
        if (c < this.fOffsets[whichWindow] || c >= this.fOffsets[whichWindow] + NodeFilter.SHOW_COMMENT) {
            return false;
        }
        return true;
    }

    private static boolean inStaticWindow(int c, int whichWindow) {
        if (c < sOffsets[whichWindow] || c >= sOffsets[whichWindow] + NodeFilter.SHOW_COMMENT) {
            return false;
        }
        return true;
    }

    private static boolean isCompressible(int c) {
        return c < Normalizer2Impl.COMP_1_TRAIL_LIMIT || c >= 57344;
    }

    private int findDynamicWindow(int c) {
        for (int i = 7; i >= 0; i--) {
            if (inDynamicWindow(c, i)) {
                int[] iArr = this.fTimeStamps;
                iArr[i] = iArr[i] + 1;
                return i;
            }
        }
        return -1;
    }

    private static int findStaticWindow(int c) {
        for (int i = 7; i >= 0; i--) {
            if (inStaticWindow(c, i)) {
                return i;
            }
        }
        return -1;
    }

    private int getLRDefinedWindow() {
        int leastRU = AnnualTimeZoneRule.MAX_YEAR;
        int whichWindow = -1;
        for (int i = 7; i >= 0; i--) {
            if (this.fTimeStamps[i] < leastRU) {
                leastRU = this.fTimeStamps[i];
                whichWindow = i;
            }
        }
        return whichWindow;
    }
}
