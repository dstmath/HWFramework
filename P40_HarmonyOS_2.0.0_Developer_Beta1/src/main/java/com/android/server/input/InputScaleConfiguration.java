package com.android.server.input;

public final class InputScaleConfiguration {
    public static final float DEFAULT_INPUT_SCALE = 1.0f;
    public static final int SIDE_COUNT = 4;
    public static final int SIDE_LEFT_BOTTOM = 2;
    public static final int SIDE_LEFT_TOP = 1;
    public static final int SIDE_NONE = 0;
    public static final int SIDE_RIGHT_BOTTOM = 3;
    public static final int SIDE_RIGHT_TOP = 4;
    public static final int TYPE_COUNT = 3;
    public static final int TYPE_GENERIC = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SINGLE_HAND = 2;
    public static final int TYPE_SUB_DISPLAY = 3;
    public int side;
    public int type;
    public float xScale;
    public float yScale;

    public InputScaleConfiguration() {
        reset();
    }

    public boolean isInputScaleEnabled() {
        return (this.type == 0 || this.side == 0) ? false : true;
    }

    public void set(InputScaleConfiguration config) {
        if (config != null) {
            this.side = config.side;
            this.type = config.type;
            this.xScale = config.xScale;
            this.yScale = config.yScale;
            return;
        }
        reset();
    }

    public void updateScaleConfig(float xScale2, float yScale2, int scaleSide, int scaleType) {
        boolean isValidType = true;
        boolean isValidSide = scaleSide >= 0 && scaleSide <= 4;
        if (scaleType < 0 || scaleType > 3) {
            isValidType = false;
        }
        if (isValidSide && isValidType) {
            this.side = scaleSide;
            this.type = scaleType;
            this.xScale = xScale2;
            this.yScale = yScale2;
            if (!isInputScaleEnabled()) {
                reset();
            }
        }
    }

    private void reset() {
        this.side = 0;
        this.type = 0;
        this.xScale = 1.0f;
        this.yScale = 1.0f;
    }
}
