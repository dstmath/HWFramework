package android.widget;

public class HwWidgetColumnDummy implements HwWidgetColumn {
    @Override // android.widget.HwWidgetColumn
    public int getMaxColumnWidth(int columnType) {
        return Integer.MAX_VALUE;
    }

    @Override // android.widget.HwWidgetColumn
    public int getMinColumnWidth(int columnType) {
        return 0;
    }
}
