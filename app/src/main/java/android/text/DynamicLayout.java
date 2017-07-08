package android.text;

import android.graphics.Paint.FontMetricsInt;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.StaticLayout.Builder;
import android.text.TextUtils.TruncateAt;
import android.text.style.UpdateLayout;
import android.text.style.WrapTogetherSpan;
import android.view.inputmethod.EditorInfo;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.ref.WeakReference;

public class DynamicLayout extends Layout {
    private static final int BLOCK_MINIMUM_CHARACTER_LENGTH = 400;
    private static final int COLUMNS_ELLIPSIZE = 6;
    private static final int COLUMNS_NORMAL = 4;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 5;
    private static final int ELLIPSIS_START = 4;
    private static final int ELLIPSIS_UNDEFINED = Integer.MIN_VALUE;
    private static final int HYPHEN = 3;
    public static final int INVALID_BLOCK_INDEX = -1;
    private static final int PRIORITY = 128;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_MASK = 536870912;
    private static final int TOP = 1;
    private static Builder sBuilder;
    private static final Object[] sLock = null;
    private static StaticLayout sStaticLayout;
    private CharSequence mBase;
    private int[] mBlockEndLines;
    private int[] mBlockIndices;
    private int mBottomPadding;
    private int mBreakStrategy;
    private CharSequence mDisplay;
    private boolean mEllipsize;
    private TruncateAt mEllipsizeAt;
    private int mEllipsizedWidth;
    private int mHyphenationFrequency;
    private boolean mIncludePad;
    private int mIndexFirstChangedBlock;
    private PackedIntVector mInts;
    private int mNumberOfBlocks;
    private PackedObjectVector<Directions> mObjects;
    private int mTopPadding;
    private ChangeWatcher mWatcher;

    private static class ChangeWatcher implements TextWatcher, SpanWatcher {
        private WeakReference<DynamicLayout> mLayout;

        public ChangeWatcher(DynamicLayout layout) {
            this.mLayout = new WeakReference(layout);
        }

        private void reflow(CharSequence s, int where, int before, int after) {
            DynamicLayout ml = (DynamicLayout) this.mLayout.get();
            if (ml != null) {
                ml.reflow(s, where, before, after);
            } else if (s instanceof Spannable) {
                ((Spannable) s).removeSpan(this);
            }
        }

        public void beforeTextChanged(CharSequence s, int where, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int where, int before, int after) {
            reflow(s, where, before, after);
        }

        public void afterTextChanged(Editable s) {
        }

        public void onSpanAdded(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        public void onSpanRemoved(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        public void onSpanChanged(Spannable s, Object o, int start, int end, int nstart, int nend) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
                reflow(s, nstart, nend - nstart, nend - nstart);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.DynamicLayout.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.DynamicLayout.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.DynamicLayout.<clinit>():void");
    }

    public DynamicLayout(CharSequence base, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, base, paint, width, align, spacingmult, spacingadd, includepad);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, display, paint, width, align, spacingmult, spacingadd, includepad, null, TAB);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth) {
        this(base, display, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, TAB, TAB, ellipsize, ellipsizedWidth);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, int breakStrategy, int hyphenationFrequency, TruncateAt ellipsize, int ellipsizedWidth) {
        CharSequence charSequence;
        int[] start;
        if (ellipsize == null) {
            charSequence = display;
        } else if (display instanceof Spanned) {
            charSequence = new SpannedEllipsizer(display);
        } else {
            charSequence = new Ellipsizer(display);
        }
        super(charSequence, paint, width, align, textDir, spacingmult, spacingadd);
        this.mBase = base;
        this.mDisplay = display;
        if (ellipsize != null) {
            this.mInts = new PackedIntVector(COLUMNS_ELLIPSIZE);
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mEllipsizeAt = ellipsize;
        } else {
            this.mInts = new PackedIntVector(ELLIPSIS_START);
            this.mEllipsizedWidth = width;
            this.mEllipsizeAt = null;
        }
        this.mObjects = new PackedObjectVector(TOP);
        this.mIncludePad = includepad;
        this.mBreakStrategy = breakStrategy;
        this.mHyphenationFrequency = hyphenationFrequency;
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsize = true;
        }
        if (ellipsize != null) {
            start = new int[COLUMNS_ELLIPSIZE];
            start[ELLIPSIS_START] = ELLIPSIS_UNDEFINED;
        } else {
            start = new int[ELLIPSIS_START];
        }
        Directions[] dirs = new Directions[TOP];
        dirs[TAB] = DIRS_ALL_LEFT_TO_RIGHT;
        FontMetricsInt fm = paint.getFontMetricsInt();
        int asc = fm.ascent;
        int desc = fm.descent;
        start[TAB] = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        start[TOP] = TAB;
        start[DESCENT] = desc;
        this.mInts.insertAt(TAB, start);
        start[TOP] = desc - asc;
        this.mInts.insertAt(TOP, start);
        this.mObjects.insertAt(TAB, dirs);
        reflow(base, TAB, TAB, base.length());
        if (base instanceof Spannable) {
            if (this.mWatcher == null) {
                this.mWatcher = new ChangeWatcher(this);
            }
            Spannable sp = (Spannable) base;
            ChangeWatcher[] spans = (ChangeWatcher[]) sp.getSpans(TAB, sp.length(), ChangeWatcher.class);
            for (int i = TAB; i < spans.length; i += TOP) {
                sp.removeSpan(spans[i]);
            }
            sp.setSpan(this.mWatcher, TAB, base.length(), 8388626);
        }
    }

    private void reflow(CharSequence s, int where, int before, int after) {
        if (s == this.mBase) {
            int i;
            StaticLayout reflowed;
            Builder b;
            int[] ints;
            CharSequence text = this.mDisplay;
            int len = text.length();
            int find = TextUtils.lastIndexOf(text, '\n', where + INVALID_BLOCK_INDEX);
            if (find < 0) {
                find = TAB;
            } else {
                find += TOP;
            }
            int diff = where - find;
            before += diff;
            after += diff;
            where -= diff;
            int look = TextUtils.indexOf(text, '\n', where + after);
            if (look < 0) {
                look = len;
            } else {
                look += TOP;
            }
            int change = look - (where + after);
            before += change;
            after += change;
            if (text instanceof Spanned) {
                Spanned sp = (Spanned) text;
                boolean again;
                do {
                    again = false;
                    Object[] force = sp.getSpans(where, where + after, WrapTogetherSpan.class);
                    for (i = TAB; i < force.length; i += TOP) {
                        int st = sp.getSpanStart(force[i]);
                        int en = sp.getSpanEnd(force[i]);
                        if (st < where) {
                            again = true;
                            diff = where - st;
                            before += diff;
                            after += diff;
                            where -= diff;
                        }
                        if (en > where + after) {
                            again = true;
                            diff = en - (where + after);
                            before += diff;
                            after += diff;
                        }
                    }
                } while (again);
            }
            int startline = getLineForOffset(where);
            int startv = getLineTop(startline);
            int endline = getLineForOffset(where + before);
            if (where + after == len) {
                endline = getLineCount();
            }
            int endv = getLineTop(endline);
            boolean islast = endline == getLineCount();
            synchronized (sLock) {
                reflowed = sStaticLayout;
                b = sBuilder;
                sStaticLayout = null;
                sBuilder = null;
            }
            if (reflowed == null) {
                StaticLayout staticLayout = new StaticLayout(null);
                b = Builder.obtain(text, where, where + after, getPaint(), getWidth());
            }
            int i2 = where + after;
            int i3 = this.mEllipsizedWidth;
            b.setText(text, where, r32).setPaint(getPaint()).setWidth(getWidth()).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getSpacingAdd(), getSpacingMultiplier()).setEllipsizedWidth(r0).setEllipsize(this.mEllipsizeAt).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency);
            reflowed.generate(b, false, true);
            int n = reflowed.getLineCount();
            if (where + after != len) {
                if (reflowed.getLineStart(n + INVALID_BLOCK_INDEX) == where + after) {
                    n += INVALID_BLOCK_INDEX;
                }
            }
            this.mInts.deleteAt(startline, endline - startline);
            this.mObjects.deleteAt(startline, endline - startline);
            int ht = reflowed.getLineTop(n);
            int toppad = TAB;
            int botpad = TAB;
            if (this.mIncludePad && startline == 0) {
                toppad = reflowed.getTopPadding();
                this.mTopPadding = toppad;
                ht -= toppad;
            }
            if (this.mIncludePad && islast) {
                botpad = reflowed.getBottomPadding();
                this.mBottomPadding = botpad;
                ht += botpad;
            }
            this.mInts.adjustValuesBelow(startline, TAB, after - before);
            this.mInts.adjustValuesBelow(startline, TOP, (startv - endv) + ht);
            if (this.mEllipsize) {
                ints = new int[COLUMNS_ELLIPSIZE];
                ints[ELLIPSIS_START] = ELLIPSIS_UNDEFINED;
            } else {
                ints = new int[ELLIPSIS_START];
            }
            Directions[] objects = new Directions[TOP];
            for (i = TAB; i < n; i += TOP) {
                ints[TAB] = (reflowed.getLineContainsTab(i) ? TAB_MASK : TAB) | ((reflowed.getParagraphDirection(i) << DIR_SHIFT) | reflowed.getLineStart(i));
                int top = reflowed.getLineTop(i) + startv;
                if (i > 0) {
                    top -= toppad;
                }
                ints[TOP] = top;
                int desc = reflowed.getLineDescent(i);
                if (i == n + INVALID_BLOCK_INDEX) {
                    desc += botpad;
                }
                ints[DESCENT] = desc;
                objects[TAB] = reflowed.getLineDirections(i);
                ints[HYPHEN] = reflowed.getHyphen(i);
                if (this.mEllipsize) {
                    ints[ELLIPSIS_START] = reflowed.getEllipsisStart(i);
                    ints[ELLIPSIS_COUNT] = reflowed.getEllipsisCount(i);
                }
                this.mInts.insertAt(startline + i, ints);
                this.mObjects.insertAt(startline + i, objects);
            }
            updateBlocks(startline, endline + INVALID_BLOCK_INDEX, n);
            b.finish();
            synchronized (sLock) {
                sStaticLayout = reflowed;
                sBuilder = b;
            }
        }
    }

    private void createBlocks() {
        int offset = BLOCK_MINIMUM_CHARACTER_LENGTH;
        this.mNumberOfBlocks = TAB;
        CharSequence text = this.mDisplay;
        while (true) {
            offset = TextUtils.indexOf(text, '\n', offset);
            if (offset < 0) {
                break;
            }
            addBlockAtOffset(offset);
            offset += BLOCK_MINIMUM_CHARACTER_LENGTH;
        }
        addBlockAtOffset(text.length());
        this.mBlockIndices = new int[this.mBlockEndLines.length];
        for (int i = TAB; i < this.mBlockEndLines.length; i += TOP) {
            this.mBlockIndices[i] = INVALID_BLOCK_INDEX;
        }
    }

    private void addBlockAtOffset(int offset) {
        int line = getLineForOffset(offset);
        if (this.mBlockEndLines == null) {
            this.mBlockEndLines = ArrayUtils.newUnpaddedIntArray(TOP);
            this.mBlockEndLines[this.mNumberOfBlocks] = line;
            this.mNumberOfBlocks += TOP;
            return;
        }
        if (line > this.mBlockEndLines[this.mNumberOfBlocks + INVALID_BLOCK_INDEX]) {
            this.mBlockEndLines = GrowingArrayUtils.append(this.mBlockEndLines, this.mNumberOfBlocks, line);
            this.mNumberOfBlocks += TOP;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updateBlocks(int startLine, int endLine, int newLineCount) {
        if (this.mBlockEndLines == null) {
            createBlocks();
            return;
        }
        int i;
        int firstBlock = INVALID_BLOCK_INDEX;
        int lastBlock = INVALID_BLOCK_INDEX;
        int i2 = TAB;
        while (true) {
            i = this.mNumberOfBlocks;
            if (i2 >= r0) {
                break;
            }
            if (this.mBlockEndLines[i2] >= startLine) {
                break;
            }
            i2 += TOP;
        }
        i2 = firstBlock;
        while (true) {
            i = this.mNumberOfBlocks;
            if (i2 >= r0) {
                break;
            }
            if (this.mBlockEndLines[i2] >= endLine) {
                break;
            }
            i2 += TOP;
        }
        lastBlock = i2;
        int lastBlockEndLine = this.mBlockEndLines[lastBlock];
        if (firstBlock == 0) {
            i = TAB;
        } else {
            i = this.mBlockEndLines[firstBlock + INVALID_BLOCK_INDEX] + TOP;
        }
        boolean createBlockBefore = startLine > i;
        boolean createBlock = newLineCount > 0;
        boolean createBlockAfter = endLine < this.mBlockEndLines[lastBlock];
        int numAddedBlocks = TAB;
        if (createBlockBefore) {
            numAddedBlocks = TOP;
        }
        if (createBlock) {
            numAddedBlocks += TOP;
        }
        if (createBlockAfter) {
            numAddedBlocks += TOP;
        }
        int newNumberOfBlocks = (this.mNumberOfBlocks + numAddedBlocks) - ((lastBlock - firstBlock) + TOP);
        if (newNumberOfBlocks == 0) {
            this.mBlockEndLines[TAB] = TAB;
            this.mBlockIndices[TAB] = INVALID_BLOCK_INDEX;
            this.mNumberOfBlocks = TOP;
            return;
        }
        int newFirstChangedBlock;
        if (newNumberOfBlocks > this.mBlockEndLines.length) {
            int[] blockEndLines = ArrayUtils.newUnpaddedIntArray(Math.max(this.mBlockEndLines.length * DESCENT, newNumberOfBlocks));
            int[] blockIndices = new int[blockEndLines.length];
            System.arraycopy(this.mBlockEndLines, TAB, blockEndLines, TAB, firstBlock);
            System.arraycopy(this.mBlockIndices, TAB, blockIndices, TAB, firstBlock);
            System.arraycopy(this.mBlockEndLines, lastBlock + TOP, blockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) + INVALID_BLOCK_INDEX);
            System.arraycopy(this.mBlockIndices, lastBlock + TOP, blockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) + INVALID_BLOCK_INDEX);
            this.mBlockEndLines = blockEndLines;
            this.mBlockIndices = blockIndices;
        } else {
            System.arraycopy(this.mBlockEndLines, lastBlock + TOP, this.mBlockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) + INVALID_BLOCK_INDEX);
            System.arraycopy(this.mBlockIndices, lastBlock + TOP, this.mBlockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) + INVALID_BLOCK_INDEX);
        }
        this.mNumberOfBlocks = newNumberOfBlocks;
        int deltaLines = newLineCount - ((endLine - startLine) + TOP);
        if (deltaLines != 0) {
            newFirstChangedBlock = firstBlock + numAddedBlocks;
            i2 = newFirstChangedBlock;
            while (true) {
                i = this.mNumberOfBlocks;
                if (i2 >= r0) {
                    break;
                }
                int[] iArr = this.mBlockEndLines;
                iArr[i2] = iArr[i2] + deltaLines;
                i2 += TOP;
            }
        } else {
            newFirstChangedBlock = this.mNumberOfBlocks;
        }
        this.mIndexFirstChangedBlock = Math.min(this.mIndexFirstChangedBlock, newFirstChangedBlock);
        int blockIndex = firstBlock;
        if (createBlockBefore) {
            this.mBlockEndLines[blockIndex] = startLine + INVALID_BLOCK_INDEX;
            this.mBlockIndices[blockIndex] = INVALID_BLOCK_INDEX;
            blockIndex += TOP;
        }
        if (createBlock) {
            this.mBlockEndLines[blockIndex] = (startLine + newLineCount) + INVALID_BLOCK_INDEX;
            this.mBlockIndices[blockIndex] = INVALID_BLOCK_INDEX;
            blockIndex += TOP;
        }
        if (createBlockAfter) {
            this.mBlockEndLines[blockIndex] = lastBlockEndLine + deltaLines;
            this.mBlockIndices[blockIndex] = INVALID_BLOCK_INDEX;
        }
    }

    void setBlocksDataForTest(int[] blockEndLines, int[] blockIndices, int numberOfBlocks) {
        this.mBlockEndLines = new int[blockEndLines.length];
        this.mBlockIndices = new int[blockIndices.length];
        System.arraycopy(blockEndLines, TAB, this.mBlockEndLines, TAB, blockEndLines.length);
        System.arraycopy(blockIndices, TAB, this.mBlockIndices, TAB, blockIndices.length);
        this.mNumberOfBlocks = numberOfBlocks;
    }

    public int[] getBlockEndLines() {
        return this.mBlockEndLines;
    }

    public int[] getBlockIndices() {
        return this.mBlockIndices;
    }

    public int getNumberOfBlocks() {
        return this.mNumberOfBlocks;
    }

    public int getIndexFirstChangedBlock() {
        return this.mIndexFirstChangedBlock;
    }

    public void setIndexFirstChangedBlock(int i) {
        this.mIndexFirstChangedBlock = i;
    }

    public int getLineCount() {
        return this.mInts.size() + INVALID_BLOCK_INDEX;
    }

    public int getLineTop(int line) {
        return this.mInts.getValue(line, TOP);
    }

    public int getLineDescent(int line) {
        return this.mInts.getValue(line, DESCENT);
    }

    public int getLineStart(int line) {
        return this.mInts.getValue(line, TAB) & START_MASK;
    }

    public boolean getLineContainsTab(int line) {
        return (this.mInts.getValue(line, TAB) & TAB_MASK) != 0;
    }

    public int getParagraphDirection(int line) {
        return this.mInts.getValue(line, TAB) >> DIR_SHIFT;
    }

    public final Directions getLineDirections(int line) {
        return (Directions) this.mObjects.getValue(line, TAB);
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getHyphen(int line) {
        return this.mInts.getValue(line, HYPHEN);
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    public int getEllipsisStart(int line) {
        if (this.mEllipsizeAt == null) {
            return TAB;
        }
        return this.mInts.getValue(line, ELLIPSIS_START);
    }

    public int getEllipsisCount(int line) {
        if (this.mEllipsizeAt == null) {
            return TAB;
        }
        return this.mInts.getValue(line, ELLIPSIS_COUNT);
    }
}
