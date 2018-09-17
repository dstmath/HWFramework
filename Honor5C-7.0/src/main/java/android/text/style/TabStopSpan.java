package android.text.style;

public interface TabStopSpan extends ParagraphStyle {

    public static class Standard implements TabStopSpan {
        private int mTab;

        public Standard(int where) {
            this.mTab = where;
        }

        public int getTabStop() {
            return this.mTab;
        }
    }

    int getTabStop();
}
