package android.widget;

import android.graphics.Rect;
import android.text.Layout;
import android.text.Spannable;
import android.view.AccessibilityIterators;

final class AccessibilityIterators {

    static class LineTextSegmentIterator extends AccessibilityIterators.AbstractTextSegmentIterator {
        protected static final int DIRECTION_END = 1;
        protected static final int DIRECTION_START = -1;
        private static LineTextSegmentIterator sLineInstance;
        protected Layout mLayout;

        LineTextSegmentIterator() {
        }

        public static LineTextSegmentIterator getInstance() {
            if (sLineInstance == null) {
                sLineInstance = new LineTextSegmentIterator();
            }
            return sLineInstance;
        }

        public void initialize(Spannable text, Layout layout) {
            this.mText = text.toString();
            this.mLayout = layout;
        }

        public int[] following(int offset) {
            int currentLine;
            if (this.mText.length() <= 0 || offset >= this.mText.length()) {
                return null;
            }
            if (offset < 0) {
                currentLine = this.mLayout.getLineForOffset(0);
            } else {
                currentLine = this.mLayout.getLineForOffset(offset);
                if (getLineEdgeIndex(currentLine, -1) == offset) {
                    int i = currentLine;
                } else {
                    currentLine++;
                }
            }
            if (currentLine >= this.mLayout.getLineCount()) {
                return null;
            }
            return getRange(getLineEdgeIndex(currentLine, -1), getLineEdgeIndex(currentLine, 1) + 1);
        }

        public int[] preceding(int offset) {
            int currentLine;
            if (this.mText.length() <= 0 || offset <= 0) {
                return null;
            }
            if (offset > this.mText.length()) {
                currentLine = this.mLayout.getLineForOffset(this.mText.length());
            } else {
                currentLine = this.mLayout.getLineForOffset(offset);
                if (getLineEdgeIndex(currentLine, 1) + 1 == offset) {
                    int i = currentLine;
                } else {
                    currentLine--;
                }
            }
            if (currentLine < 0) {
                return null;
            }
            return getRange(getLineEdgeIndex(currentLine, -1), getLineEdgeIndex(currentLine, 1) + 1);
        }

        /* access modifiers changed from: protected */
        public int getLineEdgeIndex(int lineNumber, int direction) {
            if (direction * this.mLayout.getParagraphDirection(lineNumber) < 0) {
                return this.mLayout.getLineStart(lineNumber);
            }
            return this.mLayout.getLineEnd(lineNumber) - 1;
        }
    }

    static class PageTextSegmentIterator extends LineTextSegmentIterator {
        private static PageTextSegmentIterator sPageInstance;
        private final Rect mTempRect = new Rect();
        private TextView mView;

        PageTextSegmentIterator() {
        }

        public static PageTextSegmentIterator getInstance() {
            if (sPageInstance == null) {
                sPageInstance = new PageTextSegmentIterator();
            }
            return sPageInstance;
        }

        public void initialize(TextView view) {
            super.initialize((Spannable) view.getIterableTextForAccessibility(), view.getLayout());
            this.mView = view;
        }

        public int[] following(int offset) {
            if (this.mText.length() <= 0 || offset >= this.mText.length() || !this.mView.getGlobalVisibleRect(this.mTempRect)) {
                return null;
            }
            int start = Math.max(0, offset);
            int nextPageStartY = this.mLayout.getLineTop(this.mLayout.getLineForOffset(start)) + ((this.mTempRect.height() - this.mView.getTotalPaddingTop()) - this.mView.getTotalPaddingBottom());
            return getRange(start, getLineEdgeIndex((nextPageStartY < this.mLayout.getLineTop(this.mLayout.getLineCount() - 1) ? this.mLayout.getLineForVertical(nextPageStartY) : this.mLayout.getLineCount()) - 1, 1) + 1);
        }

        public int[] preceding(int offset) {
            if (this.mText.length() <= 0 || offset <= 0 || !this.mView.getGlobalVisibleRect(this.mTempRect)) {
                return null;
            }
            int end = Math.min(this.mText.length(), offset);
            int currentLine = this.mLayout.getLineForOffset(end);
            int previousPageEndY = this.mLayout.getLineTop(currentLine) - ((this.mTempRect.height() - this.mView.getTotalPaddingTop()) - this.mView.getTotalPaddingBottom());
            int currentPageStartLine = previousPageEndY > 0 ? this.mLayout.getLineForVertical(previousPageEndY) : 0;
            if (end == this.mText.length() && currentPageStartLine < currentLine) {
                currentPageStartLine++;
            }
            return getRange(getLineEdgeIndex(currentPageStartLine, -1), end);
        }
    }

    AccessibilityIterators() {
    }
}
