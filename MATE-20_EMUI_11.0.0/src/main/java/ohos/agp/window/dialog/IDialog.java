package ohos.agp.window.dialog;

public interface IDialog {
    public static final int BUTTON1 = 0;
    public static final int BUTTON2 = 1;
    public static final int BUTTON3 = 2;
    public static final int BUTTON_NUM = 3;
    public static final int DEF_RADIUS = 15;
    public static final int ICON1 = 0;
    public static final int ICON2 = 1;
    public static final int ICON3 = 2;
    public static final int ICON_NUM = 3;

    public interface CheckBoxClickedListener {
        void onClick(IDialog iDialog, int i, boolean z);
    }

    public interface ClickedListener {
        void onClick(IDialog iDialog, int i);
    }

    public interface ItemClickedListener {
        void onClick(IDialog iDialog, int i);
    }

    void destroy();

    void hide();

    void show();
}
