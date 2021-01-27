package ohos.agp.window.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.IDialog;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class CommonDialogAttribute {
    private static final float BUTTON_WEIGHT = 1.0f;
    private static final float CONTENT_WEIGHT = 2.0f;
    private static final int DEF_BUTTON_LAYER_H = 105;
    private static final int DEF_MARGIN = 5;
    private static final int DEF_TITLE_LAYER_H = 150;
    private static final float DEF_WEIGHT = 1.0f;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "CommonDialogAttribute");
    private static final int SUB_TITLE_SIZE = 40;
    private static final int TEXT_SIZE = 50;
    private static final float TEXT_WEIGHT = 2.0f;
    private static final int TITLE_SIZE = 60;
    private static final float TITLE_WEIGHT = 1.0f;
    public Map<Integer, IDialog.ClickedListener> buttonListener = new HashMap();
    public String[] buttonTexts = new String[3];
    public Button[] buttonViews = new Button[3];
    public Component contentCustomView;
    public Image contentImage;
    public int contentImageResid;
    public String contentText;
    public Text contentTextComponent;
    public int[] imageButtonResid = new int[3];
    public Image[] imageButtonViews = new Image[3];
    private Context mContext;
    public DirectionalLayout titleCustomView;
    public int[] titleIconResid = new int[3];
    public Image[] titleIconView = new Image[3];
    public String titleSubText;
    public Text titleSubTextComponent;
    public String titleText;
    public Text titleTextComponent;

    public CommonDialogAttribute(Context context) {
        this.mContext = context;
    }

    public Optional<DirectionalLayout> setupTitlePanel() {
        DirectionalLayout directionalLayout = this.titleCustomView;
        if (directionalLayout != null) {
            return Optional.of(directionalLayout);
        }
        if (this.titleText != null && this.titleTextComponent == null) {
            this.titleTextComponent = new Text(this.mContext);
            this.titleTextComponent.setText(this.titleText);
            this.titleTextComponent.setTextSize(60);
            this.titleTextComponent.setMultipleLine(false);
            this.titleTextComponent.setTruncationMode(Text.TruncationMode.ELLIPSIS_AT_END);
        }
        if (this.titleSubText != null && this.titleSubTextComponent == null) {
            this.titleSubTextComponent = new Text(this.mContext);
            this.titleSubTextComponent.setText(this.titleSubText);
            this.titleSubTextComponent.setTextSize(40);
        }
        for (int i = 0; i <= 2; i++) {
            if (this.titleIconResid[i] > 0) {
                this.titleIconView[i] = new Image(this.mContext);
                this.titleIconView[i].setPixelMap(this.titleIconResid[i]);
            }
        }
        DirectionalLayout titleDefaultLayout = titleDefaultLayout();
        if (titleDefaultLayout.getChildCount() == 0) {
            return Optional.empty();
        }
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
        titleDefaultLayout.setBackground(shapeElement);
        return Optional.of(titleDefaultLayout);
    }

    public Optional<Component> setupContentPanel() {
        if (this.contentCustomView != null) {
            HiLog.debug(LABEL, "setCustomComponent", new Object[0]);
            return Optional.of(this.contentCustomView);
        }
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(1);
        directionalLayout.setAutoLayout(true);
        DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-2, -2);
        layoutConfig.weight = 1.0f;
        directionalLayout.setLayoutConfig(layoutConfig);
        if (this.contentImageResid > 0 && this.contentImage == null) {
            this.contentImage = new Image(this.mContext);
            this.contentImage.setPixelMap(this.contentImageResid);
            directionalLayout.addComponent(this.contentImage);
            this.contentImage.setLayoutConfig(new DirectionalLayout.LayoutConfig(-2, -2));
        }
        if (this.contentText != null && this.contentTextComponent == null) {
            this.contentTextComponent = new Text(this.mContext);
            this.contentTextComponent.setText(this.contentText);
            this.contentTextComponent.setTextSize(50);
            directionalLayout.addComponent(this.contentTextComponent);
            this.contentTextComponent.setLayoutConfig(new DirectionalLayout.LayoutConfig(-2, -2));
        }
        if (directionalLayout.getChildCount() == 0) {
            return Optional.empty();
        }
        directionalLayout.postLayout();
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
        directionalLayout.setBackground(shapeElement);
        return Optional.of(directionalLayout);
    }

    public Optional<DirectionalLayout> setupButtonPanel() {
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(0);
        directionalLayout.setAutoLayout(true);
        directionalLayout.setWidth(-1);
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
        float f = 0.0f;
        for (int i = 0; i < 3; i++) {
            if (this.buttonTexts[i] != null) {
                this.buttonViews[i] = new Button(this.mContext);
                directionalLayout.addComponent(this.buttonViews[i]);
                this.buttonViews[i].setText(this.buttonTexts[i]);
                this.buttonViews[i].setTextSize(50);
                this.buttonViews[i].setTextColor(Color.RED);
                this.buttonViews[i].setBackground(shapeElement);
                this.buttonViews[i].setId(i);
                this.buttonViews[i].setMultipleLine(false);
                this.buttonViews[i].setTruncationMode(Text.TruncationMode.ELLIPSIS_AT_END);
                DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-1, -2);
                layoutConfig.weight = 2.0f;
                this.buttonViews[i].setLayoutConfig(layoutConfig);
                f += 2.0f;
            }
        }
        if (directionalLayout.getChildCount() == 0) {
            return Optional.empty();
        }
        setMainLyoutParam(directionalLayout, f);
        return Optional.of(directionalLayout);
    }

    public Optional<DirectionalLayout> setupImageButtonPanel() {
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(0);
        directionalLayout.setWidth(-1);
        float f = 0.0f;
        for (int i = 0; i < 3; i++) {
            if (this.imageButtonResid[i] > 0) {
                this.imageButtonViews[i] = new Image(this.mContext);
                this.imageButtonViews[i].setPixelMap(this.imageButtonResid[i]);
                this.imageButtonViews[i].setId(i);
                this.imageButtonViews[i].setHeight(105);
                this.imageButtonViews[i].setWidth(105);
                DirectionalLayout directionalLayout2 = new DirectionalLayout(this.mContext);
                directionalLayout2.setOrientation(1);
                DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-1, -2);
                layoutConfig.weight = 1.0f;
                directionalLayout2.setLayoutConfig(layoutConfig);
                DirectionalLayout.LayoutConfig layoutConfig2 = new DirectionalLayout.LayoutConfig(-2, -2);
                setImageButtonParam(layoutConfig2, i);
                this.imageButtonViews[i].setLayoutConfig(layoutConfig2);
                directionalLayout2.addComponent(this.imageButtonViews[i]);
                directionalLayout.addComponent(directionalLayout2);
                f += 1.0f;
            }
        }
        if (directionalLayout.getChildCount() == 0) {
            return Optional.empty();
        }
        setMainLyoutParam(directionalLayout, f);
        return Optional.of(directionalLayout);
    }

    private void setImageButtonParam(DirectionalLayout.LayoutConfig layoutConfig, int i) {
        if (i == 0) {
            layoutConfig.setMargins(5, layoutConfig.getMarginTop(), layoutConfig.getMarginRight(), layoutConfig.getMarginBottom());
            layoutConfig.alignment = 3;
        } else if (i == 2) {
            layoutConfig.setMargins(layoutConfig.getMarginLeft(), layoutConfig.getMarginTop(), 5, layoutConfig.getMarginBottom());
            layoutConfig.alignment = 5;
        } else {
            layoutConfig.alignment = 17;
        }
    }

    private void setMainLyoutParam(DirectionalLayout directionalLayout, float f) {
        directionalLayout.setTotalWeight(f);
        directionalLayout.postLayout();
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(RgbPalette.TRANSPARENT);
        directionalLayout.setBackground(shapeElement);
        directionalLayout.setHeight(105);
    }

    private Optional<Component> titleTextLayout() {
        Text text;
        Text text2;
        if (this.titleText != null && this.titleSubText == null && (text2 = this.titleTextComponent) != null) {
            return Optional.of(text2);
        }
        if (this.titleText == null && this.titleSubText != null && (text = this.titleSubTextComponent) != null) {
            return Optional.of(text);
        }
        if (this.titleText == null || this.titleSubText == null || this.titleTextComponent == null || this.titleSubTextComponent == null) {
            return Optional.empty();
        }
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(1);
        directionalLayout.setAutoLayout(true);
        directionalLayout.addComponent(this.titleTextComponent);
        directionalLayout.addComponent(this.titleSubTextComponent);
        DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-1, -2);
        layoutConfig.weight = 1.0f;
        this.titleTextComponent.setLayoutConfig(layoutConfig);
        this.titleSubTextComponent.setLayoutConfig(layoutConfig);
        directionalLayout.setTotalWeight(2.0f);
        directionalLayout.postLayout();
        return Optional.of(directionalLayout);
    }

    private DirectionalLayout titleDefaultLayout() {
        float f;
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(0);
        directionalLayout.setAutoLayout(true);
        DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-1, -1);
        if (this.titleIconResid[0] > 0) {
            directionalLayout.addComponent(this.titleIconView[0]);
            layoutConfig.weight = 1.0f;
            this.titleIconView[0].setLayoutConfig(layoutConfig);
            f = 1.0f;
        } else {
            f = 0.0f;
        }
        Optional<Component> titleTextLayout = titleTextLayout();
        if (titleTextLayout.isPresent()) {
            Component component = titleTextLayout.get();
            directionalLayout.addComponent(component);
            layoutConfig.weight = 2.0f;
            component.setLayoutConfig(layoutConfig);
            f += 2.0f;
        }
        if (this.titleIconResid[1] > 0) {
            directionalLayout.addComponent(this.titleIconView[1]);
            layoutConfig.weight = 1.0f;
            this.titleIconView[1].setLayoutConfig(layoutConfig);
            f += 1.0f;
        }
        if (this.titleIconResid[2] > 0) {
            directionalLayout.addComponent(this.titleIconView[2]);
            layoutConfig.weight = 1.0f;
            this.titleIconView[2].setLayoutConfig(layoutConfig);
            f += 1.0f;
        }
        directionalLayout.setTotalWeight(f);
        directionalLayout.setHeight(150);
        directionalLayout.postLayout();
        return directionalLayout;
    }
}
