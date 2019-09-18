package android.text.style;

public interface TabStopSpan extends ParagraphStyle {

    public static class Standard implements TabStopSpan {
        private int mTabOffset;

        public Standard(int offset) {
            this.mTabOffset = offset;
        }

        public int getTabStop() {
            return this.mTabOffset;
        }
    }

    int getTabStop();
}
