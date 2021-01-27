package android.widget;

public interface HwWidgetColumn {
    public static final int DIALOG_TYPE = 2;
    public static final int MENU_TYPE = 0;
    public static final int TOAST_TYPE = 1;

    int getMaxColumnWidth(int i);

    int getMinColumnWidth(int i);
}
