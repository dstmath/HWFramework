package ohos.agp.window.dialog;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbColor;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PopupDialog extends BaseDialog {
    private static final int CONIC_WEIGHT = 20;
    private static final int FONT_SIZE = 50;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "PopupDialog");
    private Color mArrowColor;
    private int mArrowHeight;
    private DirectionalLayout mArrowLayout;
    private int mArrowOffset;
    private int mArrowWidth;
    private RgbColor mBackColor;
    private int mComponentHeight;
    private int mComponentWidth;
    private int mComponentX;
    private int mComponentY;
    private int mGravity;
    private boolean mHasArrow;
    private Component mMainComponent;
    private DirectionalLayout mMainLayout;
    private int mMode;
    private String mText;
    private int mX;
    private int mY;

    public PopupDialog(Context context, Component component) {
        this(context, component, 0, 0);
    }

    public PopupDialog(Context context, Component component, int i, int i2) {
        super(context);
        this.mArrowHeight = 30;
        this.mArrowWidth = 50;
        this.mArrowOffset = 10;
        this.mHasArrow = false;
        this.mArrowColor = Color.WHITE;
        this.mBackColor = RgbPalette.WHITE;
        this.mFlag = 4;
        if (component != null) {
            int[] locationOnScreen = component.getLocationOnScreen();
            this.mComponentX = locationOnScreen[0];
            this.mComponentY = locationOnScreen[1];
            this.mComponentWidth = component.getWidth();
            this.mComponentHeight = component.getHeight();
        }
        if (i == 0) {
            this.mWidth = -2;
        } else {
            this.mWidth = i;
        }
        if (i2 == 0) {
            this.mHeight = -2;
        } else {
            this.mHeight = i2;
        }
        countDefaultMode();
    }

    private void countDefaultMode() {
        HiLog.debug(LABEL, "PopupDialog countDefaultMode", new Object[0]);
        int i = this.mComponentX;
        int i2 = (i + i) + this.mComponentWidth > this.mDeviceWidth ? 5 : 3;
        int i3 = this.mComponentY;
        this.mMode = i2 | ((i3 + i3) + this.mComponentHeight < this.mDeviceHeight ? 80 : 48);
    }

    private void countDefaultPostion() {
        HiLog.debug(LABEL, "PopupDialog countDefaultPostion", new Object[0]);
        if (this.mGravity != 0) {
            HiLog.debug(LABEL, "This is show at location mode.", new Object[0]);
            return;
        }
        int i = this.mMode;
        if ((i & 3) == 3) {
            this.mX = this.mComponentX;
        } else if ((i & 5) == 5) {
            this.mX = (this.mComponentX + this.mComponentWidth) - this.mWidth;
        } else {
            this.mX = (this.mComponentX + (this.mComponentWidth >> 1)) - (this.mWidth >> 1);
        }
        if ((this.mMode & 48) == 48) {
            this.mY = this.mComponentY - this.mHeight;
        } else {
            this.mY = this.mComponentY + this.mComponentHeight;
        }
    }

    public PopupDialog setHasArrow(boolean z) {
        if (this.mWidth > 0 && this.mHeight > 0) {
            this.mHasArrow = z;
            setTransparent(this.mHasArrow);
        }
        return this;
    }

    public void setArrowSize(int i, int i2) {
        if (i >= this.mWidth || i <= 0) {
            HiLog.error(LABEL, "arrow width should less than dialog width and more than zero.", new Object[0]);
        } else {
            this.mArrowWidth = i;
        }
        if (i2 >= this.mHeight || i2 <= 0) {
            HiLog.error(LABEL, "arrow height should less than dialog height and more than zero.", new Object[0]);
        } else {
            this.mArrowHeight = i2;
        }
    }

    public void setBackColor(Color color) {
        this.mArrowColor = color;
        this.mBackColor = RgbColor.fromArgbInt(this.mArrowColor.getValue());
    }

    public void setArrowOffset(int i) {
        if (i >= this.mWidth || i <= 0) {
            HiLog.error(LABEL, "arrow offset should less than dialog width and more than zero.", new Object[0]);
        } else {
            this.mArrowOffset = i;
        }
    }

    private Path getArrowDirectionByMode(int i) {
        Path path = new Path();
        if (i == 48) {
            path.moveTo((float) ((this.mWidth - this.mArrowWidth) >> 1), 0.0f);
            path.conicTo(new Point((float) (this.mWidth >> 1), (float) this.mArrowHeight), new Point((float) ((this.mWidth + this.mArrowWidth) >> 1), 0.0f), 20.0f);
        } else if (i == 51) {
            path.moveTo((float) this.mArrowOffset, 0.0f);
            path.conicTo(new Point((float) (this.mArrowOffset + (this.mArrowWidth >> 1)), (float) this.mArrowHeight), new Point((float) (this.mArrowOffset + this.mArrowWidth), 0.0f), 20.0f);
        } else if (i == 53) {
            path.moveTo((float) ((this.mWidth - this.mArrowWidth) - this.mArrowOffset), 0.0f);
            path.conicTo(new Point((float) ((this.mWidth - this.mArrowOffset) - (this.mArrowWidth >> 1)), (float) this.mArrowHeight), new Point((float) (this.mWidth - this.mArrowOffset), 0.0f), 20.0f);
        } else if (i == 83) {
            path.moveTo((float) this.mArrowOffset, (float) this.mArrowHeight);
            path.conicTo(new Point((float) (this.mArrowOffset + (this.mArrowWidth >> 1)), 0.0f), new Point((float) (this.mArrowOffset + this.mArrowWidth), (float) this.mArrowHeight), 20.0f);
        } else if (i != 85) {
            path.moveTo((float) ((this.mWidth - this.mArrowWidth) >> 1), (float) this.mArrowHeight);
            path.conicTo(new Point((float) (this.mWidth >> 1), 0.0f), new Point((float) ((this.mWidth + this.mArrowWidth) >> 1), (float) this.mArrowHeight), 20.0f);
        } else {
            path.moveTo((float) ((this.mWidth - this.mArrowWidth) - this.mArrowOffset), (float) this.mArrowHeight);
            path.conicTo(new Point((float) ((this.mWidth - this.mArrowOffset) - (this.mArrowWidth >> 1)), 0.0f), new Point((float) (this.mWidth - this.mArrowOffset), (float) this.mArrowHeight), 20.0f);
        }
        return path;
    }

    private Paint getArrowLayoutPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILLANDSTROKE_STYLE);
        paint.setColor(this.mArrowColor);
        return paint;
    }

    private void initArrowLayout() {
        this.mArrowLayout = new DirectionalLayout(this.mContext);
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
        this.mArrowLayout.setBackground(shapeElement);
        this.mArrowLayout.setHeight(this.mArrowHeight);
        this.mArrowLayout.setWidth(this.mWidth);
        this.mArrowLayout.addDrawTask(new DrawTaskImpl(getArrowDirectionByMode(this.mMode), getArrowLayoutPaint()));
    }

    /* access modifiers changed from: private */
    public static class DrawTaskImpl implements Component.DrawTask {
        private Paint drawPaint;
        private Path drawPath;

        public DrawTaskImpl(Path path, Paint paint) {
            this.drawPath = path;
            this.drawPaint = paint;
        }

        @Override // ohos.agp.components.Component.DrawTask
        public void onDraw(Component component, Canvas canvas) {
            canvas.drawPath(this.drawPath, this.drawPaint);
        }
    }

    private void initMainLayout() {
        this.mMainLayout = new DirectionalLayout(this.mContext);
        DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(this.mWidth, -1);
        layoutConfig.weight = 1.0f;
        this.mMainLayout.setLayoutConfig(layoutConfig);
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(this.mBackColor);
        shapeElement.setCornerRadius(this.mCornerRadius);
        this.mMainLayout.setBackground(shapeElement);
        if (this.mMainComponent == null) {
            if (this.mText != null) {
                this.mMainComponent = creatDefaultComponent();
            } else {
                if (this.mDeviceHeight > 0) {
                    this.mHeight = this.mDeviceHeight >> 1;
                }
                if (this.mDeviceWidth > 0) {
                    this.mWidth = this.mDeviceWidth >> 1;
                }
                HiLog.error(LABEL, "PopupDialog no component", new Object[0]);
                return;
            }
        }
        this.mMainLayout.addComponent(this.mMainComponent);
    }

    private void setLayout() {
        if (this.mHasArrow) {
            if ((this.mMode & 48) == 48) {
                this.mLayout.addComponent(this.mMainLayout);
                this.mLayout.addComponent(this.mArrowLayout);
            } else {
                this.mLayout.addComponent(this.mArrowLayout);
                this.mLayout.addComponent(this.mMainLayout);
            }
            ShapeElement shapeElement = new ShapeElement();
            shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
            this.mLayout.setBackground(shapeElement);
            this.mLayout.setHeight(this.mHeight);
            return;
        }
        this.mMainLayout.setHeight(this.mHeight);
        this.mLayout.addComponent(this.mMainLayout);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onShow() {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        if (this.mLayout == null || this.mParam == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShow failed for mLayout or mParam or mWindow is null", new Object[0]);
            return;
        }
        if (this.mHasArrow) {
            initArrowLayout();
        }
        initMainLayout();
        setLayout();
        if (this.mGravity == 0) {
            this.mParam.gravity = 51;
        } else {
            this.mParam.gravity = this.mGravity;
        }
        if (this.mWidth > 0 && this.mHeight > 0) {
            countDefaultPostion();
            this.mParam.width = this.mWidth;
            this.mParam.height = this.mHeight;
            this.mParam.x = this.mX;
            this.mParam.y = this.mY;
        }
        this.mWindow.updateAttributes(this.mParam);
        super.onShow();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.BaseDialog
    public void onShowing() {
        if (this.mLayout == null || this.mParam == null || this.mWindow == null) {
            HiLog.error(LABEL, "onShowing failed for mLayout or mParam or mWindow is null", new Object[0]);
            return;
        }
        int height = this.mLayout.getHeight();
        int width = this.mLayout.getWidth();
        if (this.mWidth <= 0 || this.mHeight <= 0) {
            if (this.mHeight <= 0) {
                this.mHeight = height;
            }
            if (this.mWidth <= 0) {
                this.mWidth = width;
            }
            countDefaultPostion();
            this.mParam.width = this.mWidth;
            this.mParam.height = height;
            this.mParam.x = this.mX;
            this.mParam.y = this.mY;
            this.mWindow.updateAttributes(this.mParam);
        }
    }

    public PopupDialog setMode(int i) {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        this.mMode = i;
        return this;
    }

    public PopupDialog setText(String str) {
        HiLog.debug(LABEL, "PopupDialog setText", new Object[0]);
        this.mText = str;
        return this;
    }

    private Component creatDefaultComponent() {
        Text text = new Text(this.mContext);
        text.setLeft(0);
        text.setTop(0);
        text.setWidth(this.mWidth);
        text.setTextSize(50);
        text.setText(this.mText);
        text.setTextAlignment(72);
        setCornerRadius(15.0f);
        return text;
    }

    public PopupDialog setCustomComponent(Component component) {
        HiLog.debug(LABEL, "PopupDialog setCustomComponent", new Object[0]);
        this.mMainComponent = component;
        return this;
    }

    public void showOnCertainPosition(int i, int i2, int i3) {
        HiLog.debug(LABEL, "PopupDialog onShow", new Object[0]);
        this.mGravity = i;
        this.mX = i2;
        this.mY = i3;
        show();
    }
}
