package android.text;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

public class Selection {
    public static final Object SELECTION_END = new END();
    public static final Object SELECTION_START = new START();

    private static final class END implements NoCopySpan {
        /* synthetic */ END(END -this0) {
            this();
        }

        private END() {
        }
    }

    public interface PositionIterator {
        public static final int DONE = -1;

        int following(int i);

        int preceding(int i);
    }

    private static final class START implements NoCopySpan {
        /* synthetic */ START(START -this0) {
            this();
        }

        private START() {
        }
    }

    private Selection() {
    }

    public static final int getSelectionStart(CharSequence text) {
        if (text instanceof Spanned) {
            return ((Spanned) text).getSpanStart(SELECTION_START);
        }
        return -1;
    }

    public static final int getSelectionEnd(CharSequence text) {
        if (text instanceof Spanned) {
            return ((Spanned) text).getSpanStart(SELECTION_END);
        }
        return -1;
    }

    public static void setSelection(Spannable text, int start, int stop) {
        int ostart = getSelectionStart(text);
        int oend = getSelectionEnd(text);
        if (ostart != start || oend != stop) {
            text.setSpan(SELECTION_START, start, start, MetricsEvent.DIALOG_VPN_APP_CONFIG);
            text.setSpan(SELECTION_END, stop, stop, 34);
        }
    }

    public static final void setSelection(Spannable text, int index) {
        setSelection(text, index, index);
    }

    public static final void selectAll(Spannable text) {
        setSelection(text, 0, text.length());
    }

    public static final void extendSelection(Spannable text, int index) {
        if (text.getSpanStart(SELECTION_END) != index) {
            text.setSpan(SELECTION_END, index, index, 34);
        }
    }

    public static final void removeSelection(Spannable text) {
        text.removeSpan(SELECTION_START);
        text.removeSpan(SELECTION_END);
    }

    public static boolean moveUp(Spannable text, Layout layout) {
        int start = getSelectionStart(text);
        int end = getSelectionEnd(text);
        if (start != end) {
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            setSelection(text, min);
            return (min == 0 && max == text.length()) ? false : true;
        } else {
            int line = layout.getLineForOffset(end);
            if (line > 0) {
                int move;
                if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line - 1)) {
                    move = layout.getOffsetForHorizontal(line - 1, layout.getPrimaryHorizontal(end));
                } else {
                    move = layout.getLineStart(line - 1);
                }
                setSelection(text, move);
                return true;
            } else if (end == 0) {
                return false;
            } else {
                setSelection(text, 0);
                return true;
            }
        }
    }

    public static boolean moveDown(Spannable text, Layout layout) {
        int start = getSelectionStart(text);
        int end = getSelectionEnd(text);
        if (start != end) {
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            setSelection(text, max);
            return (min == 0 && max == text.length()) ? false : true;
        } else {
            int line = layout.getLineForOffset(end);
            if (line < layout.getLineCount() - 1) {
                int move;
                if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line + 1)) {
                    move = layout.getOffsetForHorizontal(line + 1, layout.getPrimaryHorizontal(end));
                } else {
                    move = layout.getLineStart(line + 1);
                }
                setSelection(text, move);
                return true;
            } else if (end == text.length()) {
                return false;
            } else {
                setSelection(text, text.length());
                return true;
            }
        }
    }

    public static boolean moveLeft(Spannable text, Layout layout) {
        int start = getSelectionStart(text);
        int end = getSelectionEnd(text);
        if (start != end) {
            setSelection(text, chooseHorizontal(layout, -1, start, end));
            return true;
        }
        int to = layout.getOffsetToLeftOf(end);
        if (to == end) {
            return false;
        }
        setSelection(text, to);
        return true;
    }

    public static boolean moveRight(Spannable text, Layout layout) {
        int start = getSelectionStart(text);
        int end = getSelectionEnd(text);
        if (start != end) {
            setSelection(text, chooseHorizontal(layout, 1, start, end));
            return true;
        }
        int to = layout.getOffsetToRightOf(end);
        if (to == end) {
            return false;
        }
        setSelection(text, to);
        return true;
    }

    public static boolean extendUp(Spannable text, Layout layout) {
        int end = getSelectionEnd(text);
        int line = layout.getLineForOffset(end);
        if (line > 0) {
            int move;
            if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line - 1)) {
                move = layout.getOffsetForHorizontal(line - 1, layout.getPrimaryHorizontal(end));
            } else {
                move = layout.getLineStart(line - 1);
            }
            extendSelection(text, move);
            return true;
        } else if (end == 0) {
            return true;
        } else {
            extendSelection(text, 0);
            return true;
        }
    }

    public static boolean extendDown(Spannable text, Layout layout) {
        int end = getSelectionEnd(text);
        int line = layout.getLineForOffset(end);
        if (line < layout.getLineCount() - 1) {
            int move;
            if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line + 1)) {
                move = layout.getOffsetForHorizontal(line + 1, layout.getPrimaryHorizontal(end));
            } else {
                move = layout.getLineStart(line + 1);
            }
            extendSelection(text, move);
            return true;
        } else if (end == text.length()) {
            return true;
        } else {
            extendSelection(text, text.length());
            return true;
        }
    }

    public static boolean extendLeft(Spannable text, Layout layout) {
        int end = getSelectionEnd(text);
        int to = layout.getOffsetToLeftOf(end);
        if (to == end) {
            return true;
        }
        extendSelection(text, to);
        return true;
    }

    public static boolean extendRight(Spannable text, Layout layout) {
        int end = getSelectionEnd(text);
        int to = layout.getOffsetToRightOf(end);
        if (to == end) {
            return true;
        }
        extendSelection(text, to);
        return true;
    }

    public static boolean extendToLeftEdge(Spannable text, Layout layout) {
        extendSelection(text, findEdge(text, layout, -1));
        return true;
    }

    public static boolean extendToRightEdge(Spannable text, Layout layout) {
        extendSelection(text, findEdge(text, layout, 1));
        return true;
    }

    public static boolean moveToLeftEdge(Spannable text, Layout layout) {
        setSelection(text, findEdge(text, layout, -1));
        return true;
    }

    public static boolean moveToRightEdge(Spannable text, Layout layout) {
        setSelection(text, findEdge(text, layout, 1));
        return true;
    }

    public static boolean moveToPreceding(Spannable text, PositionIterator iter, boolean extendSelection) {
        int offset = iter.preceding(getSelectionEnd(text));
        if (offset != -1) {
            if (extendSelection) {
                extendSelection(text, offset);
            } else {
                setSelection(text, offset);
            }
        }
        return true;
    }

    public static boolean moveToFollowing(Spannable text, PositionIterator iter, boolean extendSelection) {
        int offset = iter.following(getSelectionEnd(text));
        if (offset != -1) {
            if (extendSelection) {
                extendSelection(text, offset);
            } else {
                setSelection(text, offset);
            }
        }
        return true;
    }

    private static int findEdge(Spannable text, Layout layout, int dir) {
        int line = layout.getLineForOffset(getSelectionEnd(text));
        if (dir * layout.getParagraphDirection(line) < 0) {
            return layout.getLineStart(line);
        }
        int end = layout.getLineEnd(line);
        if (line == layout.getLineCount() - 1) {
            return end;
        }
        return end - 1;
    }

    private static int chooseHorizontal(Layout layout, int direction, int off1, int off2) {
        if (layout.getLineForOffset(off1) == layout.getLineForOffset(off2)) {
            float h1 = layout.getPrimaryHorizontal(off1);
            float h2 = layout.getPrimaryHorizontal(off2);
            if (direction < 0) {
                if (h1 < h2) {
                    return off1;
                }
                return off2;
            } else if (h1 > h2) {
                return off1;
            } else {
                return off2;
            }
        } else if (layout.getParagraphDirection(layout.getLineForOffset(off1)) == direction) {
            return Math.max(off1, off2);
        } else {
            return Math.min(off1, off2);
        }
    }
}
