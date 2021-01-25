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
import ohos.hiviewdfx.HiLogLabel;

public class CommonDialogAttribute {
    private static final float BUTTON_WEIGHT = 1.0f;
    private static final float CONTENT_WEIGHT = 2.0f;
    private static final int DEF_BUTTON_LAYER_H = 105;
    private static final int DEF_MARGIN = 5;
    private static final int DEF_TEXT_VIEW_H = 100;
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
    public String contentImageUri;
    public String contentText;
    public Text contentTextComponent;
    public String[] imageButtonUri = new String[3];
    public Image[] imageButtonViews = new Image[3];
    private Context mContext;
    public DirectionalLayout titleCustomView;
    public String[] titleIconUri = new String[3];
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
            this.titleTextComponent.setHeight(100);
        }
        if (this.titleSubText != null && this.titleSubTextComponent == null) {
            this.titleSubTextComponent = new Text(this.mContext);
            this.titleSubTextComponent.setText(this.titleSubText);
            this.titleSubTextComponent.setTextSize(40);
            this.titleSubTextComponent.setHeight(100);
        }
        for (int i = 0; i <= 2; i++) {
            if (this.titleIconUri[i] != null) {
                this.titleIconView[i] = new Image(this.mContext);
                this.titleIconView[i].setImageURI(this.titleIconUri[i]);
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
        Component component = this.contentCustomView;
        if (component != null) {
            return Optional.of(component);
        }
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(1);
        directionalLayout.setAutoLayout(true);
        float f = 0.0f;
        if (this.contentImageUri != null && this.contentImage == null) {
            this.contentImage = new Image(this.mContext);
            this.contentImage.setImageURI(this.contentImageUri);
            directionalLayout.addComponent(this.contentImage);
            DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(this.contentImage.getWidth(), this.contentImage.getHeight());
            layoutConfig.weight = 2.0f;
            this.contentImage.setLayoutConfig(layoutConfig);
            f = 2.0f;
        }
        if (this.contentText != null && this.contentTextComponent == null) {
            this.contentTextComponent = new Text(this.mContext);
            this.contentTextComponent.setText(this.contentText);
            this.contentTextComponent.setTextSize(50);
            directionalLayout.addComponent(this.contentTextComponent);
            DirectionalLayout.LayoutConfig layoutConfig2 = new DirectionalLayout.LayoutConfig(-1, -2);
            layoutConfig2.weight = 2.0f;
            this.contentTextComponent.setLayoutConfig(layoutConfig2);
            f += 2.0f;
        }
        if (directionalLayout.getChildCount() == 0) {
            return Optional.empty();
        }
        directionalLayout.setWeightSum(f);
        directionalLayout.requestLayout();
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
            if (this.imageButtonUri[i] != null) {
                this.imageButtonViews[i] = new Image(this.mContext);
                this.imageButtonViews[i].setImageURI(this.imageButtonUri[i]);
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
            layoutConfig.leftMargin = 5;
            layoutConfig.gravity = 3;
        } else if (i == 2) {
            layoutConfig.rightMargin = 5;
            layoutConfig.gravity = 5;
        } else {
            layoutConfig.gravity = 17;
        }
    }

    private void setMainLyoutParam(DirectionalLayout directionalLayout, float f) {
        directionalLayout.setWeightSum(f);
        directionalLayout.requestLayout();
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
        directionalLayout.setWeightSum(2.0f);
        directionalLayout.requestLayout();
        return Optional.of(directionalLayout);
    }

    private DirectionalLayout titleDefaultLayout() {
        float f;
        DirectionalLayout directionalLayout = new DirectionalLayout(this.mContext);
        directionalLayout.setOrientation(0);
        directionalLayout.setAutoLayout(true);
        DirectionalLayout.LayoutConfig layoutConfig = new DirectionalLayout.LayoutConfig(-1, -2);
        if (this.titleIconUri[0] != null) {
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
        if (this.titleIconUri[1] != null) {
            directionalLayout.addComponent(this.titleIconView[1]);
            layoutConfig.weight = 1.0f;
            this.titleIconView[1].setLayoutConfig(layoutConfig);
            f += 1.0f;
        }
        if (this.titleIconUri[2] != null) {
            directionalLayout.addComponent(this.titleIconView[2]);
            layoutConfig.weight = 1.0f;
            this.titleIconView[2].setLayoutConfig(layoutConfig);
            f += 1.0f;
        }
        directionalLayout.setWeightSum(f);
        directionalLayout.requestLayout();
        return directionalLayout;
    }
}
