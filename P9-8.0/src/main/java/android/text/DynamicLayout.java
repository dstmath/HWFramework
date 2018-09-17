package android.text;

import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.StaticLayout.Builder;
import android.text.TextUtils.TruncateAt;
import android.text.style.ReplacementSpan;
import android.text.style.UpdateLayout;
import android.text.style.WrapTogetherSpan;
import android.util.ArraySet;
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
    private static final int HYPHEN_MASK = 255;
    public static final int INVALID_BLOCK_INDEX = -1;
    private static final int MAY_PROTRUDE_FROM_TOP_OR_BOTTOM = 3;
    private static final int MAY_PROTRUDE_FROM_TOP_OR_BOTTOM_MASK = 256;
    private static final int PRIORITY = 128;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_MASK = 536870912;
    private static final int TOP = 1;
    private static Builder sBuilder = null;
    private static final Object[] sLock = new Object[0];
    private static StaticLayout sStaticLayout = null;
    private CharSequence mBase;
    private int[] mBlockEndLines;
    private int[] mBlockIndices;
    private ArraySet<Integer> mBlocksAlwaysNeedToBeRedrawn;
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
    private int mJustificationMode;
    private int mNumberOfBlocks;
    private PackedObjectVector<Directions> mObjects;
    private Rect mTempRect;
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

    public DynamicLayout(CharSequence base, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, base, paint, width, align, spacingmult, spacingadd, includepad);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, display, paint, width, align, spacingmult, spacingadd, includepad, null, 0);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth) {
        this(base, display, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, 0, 0, 0, ellipsize, ellipsizedWidth);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, int breakStrategy, int hyphenationFrequency, int justificationMode, TruncateAt ellipsize, int ellipsizedWidth) {
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
        this.mTempRect = new Rect();
        this.mBase = base;
        this.mDisplay = display;
        if (ellipsize != null) {
            this.mInts = new PackedIntVector(6);
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mEllipsizeAt = ellipsize;
        } else {
            this.mInts = new PackedIntVector(4);
            this.mEllipsizedWidth = width;
            this.mEllipsizeAt = null;
        }
        this.mObjects = new PackedObjectVector(1);
        this.mIncludePad = includepad;
        this.mBreakStrategy = breakStrategy;
        this.mJustificationMode = justificationMode;
        this.mHyphenationFrequency = hyphenationFrequency;
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsize = true;
        }
        if (ellipsize != null) {
            start = new int[6];
            start[4] = Integer.MIN_VALUE;
        } else {
            start = new int[4];
        }
        Directions[] dirs = new Directions[]{DIRS_ALL_LEFT_TO_RIGHT};
        FontMetricsInt fm = paint.getFontMetricsInt();
        int asc = fm.ascent;
        int desc = fm.descent;
        start[0] = 1073741824;
        start[1] = 0;
        start[2] = desc;
        this.mInts.insertAt(0, start);
        start[1] = desc - asc;
        this.mInts.insertAt(1, start);
        this.mObjects.insertAt(0, dirs);
        reflow(base, 0, 0, base.length());
        if (base instanceof Spannable) {
            if (this.mWatcher == null) {
                this.mWatcher = new ChangeWatcher(this);
            }
            Spannable sp = (Spannable) base;
            ChangeWatcher[] spans = (ChangeWatcher[]) sp.getSpans(0, sp.length(), ChangeWatcher.class);
            for (Object removeSpan : spans) {
                sp.removeSpan(removeSpan);
            }
            sp.setSpan(this.mWatcher, 0, base.length(), 8388626);
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
            int find = TextUtils.lastIndexOf(text, 10, where - 1);
            if (find < 0) {
                find = 0;
            } else {
                find++;
            }
            int diff = where - find;
            before += diff;
            after += diff;
            where -= diff;
            int look = TextUtils.indexOf(text, 10, where + after);
            if (look < 0) {
                look = len;
            } else {
                look++;
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
                    for (i = 0; i < force.length; i++) {
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
            b.setText(text, where, where + after).setPaint(getPaint()).setWidth(getWidth()).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getSpacingAdd(), getSpacingMultiplier()).setEllipsizedWidth(this.mEllipsizedWidth).setEllipsize(this.mEllipsizeAt).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode);
            reflowed.generate(b, false, true);
            int n = reflowed.getLineCount();
            if (where + after != len && reflowed.getLineStart(n - 1) == where + after) {
                n--;
            }
            this.mInts.deleteAt(startline, endline - startline);
            this.mObjects.deleteAt(startline, endline - startline);
            int ht = reflowed.getLineTop(n);
            int toppad = 0;
            int botpad = 0;
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
            this.mInts.adjustValuesBelow(startline, 0, after - before);
            this.mInts.adjustValuesBelow(startline, 1, (startv - endv) + ht);
            if (this.mEllipsize) {
                ints = new int[6];
                ints[4] = Integer.MIN_VALUE;
            } else {
                ints = new int[4];
            }
            Directions[] objects = new Directions[1];
            i = 0;
            while (i < n) {
                int start = reflowed.getLineStart(i);
                ints[0] = start;
                ints[0] = ints[0] | (reflowed.getParagraphDirection(i) << 30);
                ints[0] = (reflowed.getLineContainsTab(i) ? 536870912 : 0) | ints[0];
                int top = reflowed.getLineTop(i) + startv;
                if (i > 0) {
                    top -= toppad;
                }
                ints[1] = top;
                int desc = reflowed.getLineDescent(i);
                if (i == n - 1) {
                    desc += botpad;
                }
                ints[2] = desc;
                objects[0] = reflowed.getLineDirections(i);
                int end = i == n + -1 ? where + after : reflowed.getLineStart(i + 1);
                ints[3] = reflowed.getHyphen(i) & 255;
                ints[3] = (contentMayProtrudeFromLineTopOrBottom(text, start, end) ? 256 : 0) | ints[3];
                if (this.mEllipsize) {
                    ints[4] = reflowed.getEllipsisStart(i);
                    ints[5] = reflowed.getEllipsisCount(i);
                }
                this.mInts.insertAt(startline + i, ints);
                this.mObjects.insertAt(startline + i, objects);
                i++;
            }
            updateBlocks(startline, endline - 1, n);
            b.finish();
            synchronized (sLock) {
                sStaticLayout = reflowed;
                sBuilder = b;
            }
        }
    }

    private boolean contentMayProtrudeFromLineTopOrBottom(CharSequence text, int start, int end) {
        if ((text instanceof Spanned) && ((ReplacementSpan[]) ((Spanned) text).getSpans(start, end, ReplacementSpan.class)).length > 0) {
            return true;
        }
        Paint paint = getPaint();
        paint.getTextBounds(text, start, end, this.mTempRect);
        FontMetricsInt fm = paint.getFontMetricsInt();
        boolean z = this.mTempRect.top < fm.top || this.mTempRect.bottom > fm.bottom;
        return z;
    }

    private void createBlocks() {
        int offset = 400;
        this.mNumberOfBlocks = 0;
        CharSequence text = this.mDisplay;
        while (true) {
            offset = TextUtils.indexOf(text, 10, offset);
            if (offset < 0) {
                break;
            }
            addBlockAtOffset(offset);
            offset += 400;
        }
        addBlockAtOffset(text.length());
        this.mBlockIndices = new int[this.mBlockEndLines.length];
        for (int i = 0; i < this.mBlockEndLines.length; i++) {
            this.mBlockIndices[i] = -1;
        }
    }

    public ArraySet<Integer> getBlocksAlwaysNeedToBeRedrawn() {
        return this.mBlocksAlwaysNeedToBeRedrawn;
    }

    private void updateAlwaysNeedsToBeRedrawn(int blockIndex) {
        int startLine = blockIndex == 0 ? 0 : this.mBlockEndLines[blockIndex - 1] + 1;
        int endLine = this.mBlockEndLines[blockIndex];
        for (int i = startLine; i <= endLine; i++) {
            if (getContentMayProtrudeFromTopOrBottom(i)) {
                if (this.mBlocksAlwaysNeedToBeRedrawn == null) {
                    this.mBlocksAlwaysNeedToBeRedrawn = new ArraySet();
                }
                this.mBlocksAlwaysNeedToBeRedrawn.add(Integer.valueOf(blockIndex));
                return;
            }
        }
        if (this.mBlocksAlwaysNeedToBeRedrawn != null) {
            this.mBlocksAlwaysNeedToBeRedrawn.remove(Integer.valueOf(blockIndex));
        }
    }

    private void addBlockAtOffset(int offset) {
        int line = getLineForOffset(offset);
        if (this.mBlockEndLines == null) {
            this.mBlockEndLines = ArrayUtils.newUnpaddedIntArray(1);
            this.mBlockEndLines[this.mNumberOfBlocks] = line;
            updateAlwaysNeedsToBeRedrawn(this.mNumberOfBlocks);
            this.mNumberOfBlocks++;
            return;
        }
        if (line > this.mBlockEndLines[this.mNumberOfBlocks - 1]) {
            this.mBlockEndLines = GrowingArrayUtils.append(this.mBlockEndLines, this.mNumberOfBlocks, line);
            updateAlwaysNeedsToBeRedrawn(this.mNumberOfBlocks);
            this.mNumberOfBlocks++;
        }
    }

    public void updateBlocks(int startLine, int endLine, int newLineCount) {
        if (this.mBlockEndLines == null) {
            createBlocks();
            return;
        }
        int i;
        int i2;
        int firstBlock = -1;
        int lastBlock = -1;
        for (i = 0; i < this.mNumberOfBlocks; i++) {
            if (this.mBlockEndLines[i] >= startLine) {
                firstBlock = i;
                break;
            }
        }
        for (i = firstBlock; i < this.mNumberOfBlocks; i++) {
            if (this.mBlockEndLines[i] >= endLine) {
                lastBlock = i;
                break;
            }
        }
        int lastBlockEndLine = this.mBlockEndLines[lastBlock];
        if (firstBlock == 0) {
            i2 = 0;
        } else {
            i2 = this.mBlockEndLines[firstBlock - 1] + 1;
        }
        boolean createBlockBefore = startLine > i2;
        boolean createBlock = newLineCount > 0;
        boolean createBlockAfter = endLine < this.mBlockEndLines[lastBlock];
        int numAddedBlocks = 0;
        if (createBlockBefore) {
            numAddedBlocks = 1;
        }
        if (createBlock) {
            numAddedBlocks++;
        }
        if (createBlockAfter) {
            numAddedBlocks++;
        }
        int numRemovedBlocks = (lastBlock - firstBlock) + 1;
        int newNumberOfBlocks = (this.mNumberOfBlocks + numAddedBlocks) - numRemovedBlocks;
        if (newNumberOfBlocks == 0) {
            this.mBlockEndLines[0] = 0;
            this.mBlockIndices[0] = -1;
            this.mNumberOfBlocks = 1;
            return;
        }
        int newFirstChangedBlock;
        if (newNumberOfBlocks > this.mBlockEndLines.length) {
            int[] blockEndLines = ArrayUtils.newUnpaddedIntArray(Math.max(this.mBlockEndLines.length * 2, newNumberOfBlocks));
            int[] blockIndices = new int[blockEndLines.length];
            System.arraycopy(this.mBlockEndLines, 0, blockEndLines, 0, firstBlock);
            System.arraycopy(this.mBlockIndices, 0, blockIndices, 0, firstBlock);
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, blockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, blockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            this.mBlockEndLines = blockEndLines;
            this.mBlockIndices = blockIndices;
        } else if (numAddedBlocks + numRemovedBlocks != 0) {
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, this.mBlockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, this.mBlockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
        }
        if (!(numAddedBlocks + numRemovedBlocks == 0 || this.mBlocksAlwaysNeedToBeRedrawn == null)) {
            ArraySet<Integer> set = new ArraySet();
            for (i = 0; i < this.mBlocksAlwaysNeedToBeRedrawn.size(); i++) {
                Integer block = (Integer) this.mBlocksAlwaysNeedToBeRedrawn.valueAt(i);
                if (block.intValue() > firstBlock) {
                    block = Integer.valueOf(block.intValue() + (numAddedBlocks - numRemovedBlocks));
                }
                set.add(block);
            }
            this.mBlocksAlwaysNeedToBeRedrawn = set;
        }
        this.mNumberOfBlocks = newNumberOfBlocks;
        int deltaLines = newLineCount - ((endLine - startLine) + 1);
        if (deltaLines != 0) {
            newFirstChangedBlock = firstBlock + numAddedBlocks;
            for (i = newFirstChangedBlock; i < this.mNumberOfBlocks; i++) {
                int[] iArr = this.mBlockEndLines;
                iArr[i] = iArr[i] + deltaLines;
            }
        } else {
            newFirstChangedBlock = this.mNumberOfBlocks;
        }
        this.mIndexFirstChangedBlock = Math.min(this.mIndexFirstChangedBlock, newFirstChangedBlock);
        int blockIndex = firstBlock;
        if (createBlockBefore) {
            this.mBlockEndLines[blockIndex] = startLine - 1;
            updateAlwaysNeedsToBeRedrawn(blockIndex);
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlock) {
            this.mBlockEndLines[blockIndex] = (startLine + newLineCount) - 1;
            updateAlwaysNeedsToBeRedrawn(blockIndex);
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlockAfter) {
            this.mBlockEndLines[blockIndex] = lastBlockEndLine + deltaLines;
            updateAlwaysNeedsToBeRedrawn(blockIndex);
            this.mBlockIndices[blockIndex] = -1;
        }
    }

    public void setBlocksDataForTest(int[] blockEndLines, int[] blockIndices, int numberOfBlocks) {
        this.mBlockEndLines = new int[blockEndLines.length];
        this.mBlockIndices = new int[blockIndices.length];
        System.arraycopy(blockEndLines, 0, this.mBlockEndLines, 0, blockEndLines.length);
        System.arraycopy(blockIndices, 0, this.mBlockIndices, 0, blockIndices.length);
        this.mNumberOfBlocks = numberOfBlocks;
    }

    public int[] getBlockEndLines() {
        return this.mBlockEndLines;
    }

    public int[] getBlockIndices() {
        return this.mBlockIndices;
    }

    public int getBlockIndex(int index) {
        return this.mBlockIndices[index];
    }

    public void setBlockIndex(int index, int blockIndex) {
        this.mBlockIndices[index] = blockIndex;
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
        return this.mInts.size() - 1;
    }

    public int getLineTop(int line) {
        return this.mInts.getValue(line, 1);
    }

    public int getLineDescent(int line) {
        return this.mInts.getValue(line, 2);
    }

    public int getLineStart(int line) {
        return this.mInts.getValue(line, 0) & START_MASK;
    }

    public boolean getLineContainsTab(int line) {
        return (this.mInts.getValue(line, 0) & 536870912) != 0;
    }

    public int getParagraphDirection(int line) {
        return this.mInts.getValue(line, 0) >> 30;
    }

    public final Directions getLineDirections(int line) {
        return (Directions) this.mObjects.getValue(line, 0);
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getHyphen(int line) {
        return this.mInts.getValue(line, 3) & 255;
    }

    private boolean getContentMayProtrudeFromTopOrBottom(int line) {
        return (this.mInts.getValue(line, 3) & 256) != 0;
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    public int getEllipsisStart(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 4);
    }

    public int getEllipsisCount(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 5);
    }
}
