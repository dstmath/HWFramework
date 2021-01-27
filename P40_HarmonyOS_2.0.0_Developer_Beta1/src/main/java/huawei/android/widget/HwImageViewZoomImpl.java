package huawei.android.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.HwImageViewZoom;
import android.widget.ImageView;

public class HwImageViewZoomImpl implements HwImageViewZoom {
    private static final int EDGE_BOTTOM = 4;
    private static final int EDGE_LEFT = 1;
    private static final int EDGE_NONE = 0;
    private static final int EDGE_RIGHT = 3;
    private static final int EDGE_TOP = 2;
    private static final float FLOAT_COMPARE_VALUE = 1.0E-6f;
    private static final int MATRIX_ITEM_NUM = 9;
    private static final float SCALE_MAX_VALUE = 2.6f;
    private static final float SCALE_MIN_VALUE = 1.0f;
    private static final float SCALE_STEP = 0.1f;
    private static final String TAG = "HwImageViewZoomImpl";
    private float mAnchorScaleFactor = 1.0f;
    private ImageView mImageView;
    private boolean mIsAnchorScale = false;
    private float mScaleAnchorX = 0.0f;
    private float mScaleAnchorY = 0.0f;
    private float mScaleFactor = 1.0f;

    public HwImageViewZoomImpl(Context context, ImageView imageView) {
        this.mImageView = imageView;
    }

    public void zoom(MotionEvent event, float value) {
        if (this.mImageView != null && event != null && !isFloatEqual(value, 0.0f)) {
            setImageMatrix(value > 0.0f, event);
        }
    }

    private void setImageMatrix(boolean isZoom, MotionEvent event) {
        if (isZoom) {
            float zoomFactor = this.mScaleFactor * 1.1f;
            if (zoomFactor <= SCALE_MAX_VALUE) {
                this.mScaleFactor = zoomFactor;
            } else {
                return;
            }
        } else {
            float f = this.mScaleFactor;
            this.mScaleFactor = f - (SCALE_STEP * f);
        }
        float[] matrixValues = new float[9];
        this.mImageView.getMatrix().getValues(matrixValues);
        this.mImageView.setPivotX(0.0f);
        this.mImageView.setPivotY(0.0f);
        float f2 = this.mScaleFactor;
        if (f2 < 1.0f || isFloatEqual(f2, 1.0f)) {
            this.mScaleFactor = 1.0f;
            changeScaleValue(this.mScaleFactor, 0.0f, 0.0f);
            this.mScaleAnchorX = 0.0f;
            this.mScaleAnchorY = 0.0f;
            return;
        }
        float transX = matrixValues[2] - (event.getX() * (this.mScaleFactor - matrixValues[0]));
        float transY = matrixValues[5] - (event.getY() * (this.mScaleFactor - matrixValues[4]));
        int width = this.mImageView.getWidth();
        int height = this.mImageView.getHeight();
        int edge = getAnchorEdge(transX, transY, matrixValues, event);
        if (edge == 1) {
            anchorZoomOutLeftTopProc(matrixValues, event, true);
        } else if (edge == 2) {
            anchorZoomOutLeftTopProc(matrixValues, event, false);
        } else if (edge == 3) {
            anchorZoomOutRightBottomProc(matrixValues, event, width, true);
        } else if (edge == 4) {
            anchorZoomOutRightBottomProc(matrixValues, event, height, false);
        } else {
            this.mAnchorScaleFactor = 1.0f;
            this.mIsAnchorScale = false;
            changeScaleValue(this.mScaleFactor, transX, transY);
        }
    }

    private int getAnchorEdge(float transX, float transY, float[] matrixValues, MotionEvent event) {
        int edge = 0;
        float maxScale = 1.0f;
        if ((transX > 0.0f || isFloatEqual(transX, 0.0f)) && event.getX() > 0.0f) {
            float scale = (matrixValues[2] / event.getX()) + matrixValues[0];
            if (scale > 1.0f) {
                maxScale = scale;
                edge = 1;
            }
        }
        if ((transY > 0.0f || isFloatEqual(transY, 0.0f)) && event.getY() > 0.0f) {
            float scale2 = (matrixValues[5] / event.getY()) + matrixValues[4];
            if (scale2 > maxScale) {
                maxScale = scale2;
                edge = 2;
            }
        }
        int width = this.mImageView.getWidth();
        float f = this.mScaleFactor;
        if ((((f - 1.0f) * ((float) width)) + transX < 0.0f || isFloatEqual(((f - 1.0f) * ((float) width)) + transX, 0.0f)) && event.getX() < ((float) width)) {
            float scale3 = ((matrixValues[2] - ((float) width)) + (event.getX() * matrixValues[0])) / (event.getX() - ((float) width));
            if (scale3 > maxScale) {
                maxScale = scale3;
                edge = 3;
            }
        }
        int height = this.mImageView.getHeight();
        float f2 = this.mScaleFactor;
        if ((((f2 - 1.0f) * ((float) height)) + transY < 0.0f || isFloatEqual(((f2 - 1.0f) * ((float) height)) + transY, 0.0f)) && event.getY() < ((float) height)) {
            float scale4 = ((matrixValues[5] - ((float) height)) + (event.getY() * matrixValues[4])) / (event.getY() - ((float) height));
            if (scale4 > maxScale) {
                maxScale = scale4;
                edge = 4;
            }
        }
        this.mAnchorScaleFactor = maxScale;
        return edge;
    }

    private void anchorZoomOutLeftTopProc(float[] matrixValues, MotionEvent event, boolean isLeft) {
        if (isLeft) {
            if (!this.mIsAnchorScale) {
                float f = matrixValues[5];
                float y = event.getY();
                float f2 = this.mAnchorScaleFactor;
                float transY = f - (y * (f2 - matrixValues[4]));
                this.mScaleAnchorX = 0.0f;
                if (f2 > 1.0f) {
                    this.mScaleAnchorY = transY / (1.0f - f2);
                } else if (matrixValues[4] > 1.0f) {
                    this.mScaleAnchorY = matrixValues[5] / (1.0f - matrixValues[4]);
                } else {
                    this.mScaleAnchorY = 0.0f;
                }
                changeScaleValue(this.mAnchorScaleFactor, 0.0f, transY);
                this.mIsAnchorScale = true;
            }
            float f3 = this.mScaleFactor;
            changeScaleValue(f3, 0.0f, this.mScaleAnchorY * (1.0f - f3));
            return;
        }
        if (!this.mIsAnchorScale) {
            float f4 = matrixValues[2];
            float x = event.getX();
            float f5 = this.mAnchorScaleFactor;
            float transX = f4 - (x * (f5 - matrixValues[0]));
            if (f5 > 1.0f) {
                this.mScaleAnchorX = transX / (1.0f - f5);
            } else if (matrixValues[0] > 1.0f) {
                this.mScaleAnchorX = matrixValues[2] / (1.0f - matrixValues[0]);
            } else {
                this.mScaleAnchorX = 0.0f;
            }
            this.mScaleAnchorY = 0.0f;
            changeScaleValue(this.mAnchorScaleFactor, transX, 0.0f);
            this.mIsAnchorScale = true;
        }
        float f6 = this.mScaleFactor;
        changeScaleValue(f6, this.mScaleAnchorX * (1.0f - f6), 0.0f);
    }

    private void anchorZoomOutRightBottomProc(float[] matrixValues, MotionEvent event, int size, boolean isRight) {
        if (isRight) {
            if (!this.mIsAnchorScale) {
                float f = matrixValues[5];
                float y = event.getY();
                float f2 = this.mAnchorScaleFactor;
                float transY = f - (y * (f2 - matrixValues[4]));
                this.mScaleAnchorX = (float) size;
                if (f2 > 1.0f) {
                    this.mScaleAnchorY = transY / (1.0f - f2);
                } else if (matrixValues[4] > 1.0f) {
                    this.mScaleAnchorY = matrixValues[5] / (1.0f - matrixValues[4]);
                } else {
                    this.mScaleAnchorY = (float) this.mImageView.getHeight();
                }
                changeScaleValue(this.mAnchorScaleFactor, matrixValues[2] - (event.getX() * (this.mAnchorScaleFactor - matrixValues[0])), transY);
                this.mIsAnchorScale = true;
            }
            float f3 = this.mScaleFactor;
            changeScaleValue(f3, this.mScaleAnchorX * (1.0f - f3), this.mScaleAnchorY * (1.0f - f3));
            return;
        }
        if (!this.mIsAnchorScale) {
            float f4 = matrixValues[2];
            float x = event.getX();
            float f5 = this.mAnchorScaleFactor;
            float transX = f4 - (x * (f5 - matrixValues[0]));
            if (f5 > 1.0f) {
                this.mScaleAnchorX = transX / (1.0f - f5);
            } else if (matrixValues[0] > 1.0f) {
                this.mScaleAnchorX = matrixValues[2] / (1.0f - matrixValues[0]);
            } else {
                this.mScaleAnchorX = (float) this.mImageView.getWidth();
            }
            this.mScaleAnchorY = (float) size;
            changeScaleValue(this.mAnchorScaleFactor, transX, matrixValues[5] - (event.getX() * (this.mAnchorScaleFactor - matrixValues[4])));
            this.mIsAnchorScale = true;
        }
        float f6 = this.mScaleFactor;
        changeScaleValue(f6, this.mScaleAnchorX * (1.0f - f6), this.mScaleAnchorY * (1.0f - f6));
    }

    private void changeScaleValue(float scaleFactor, float translationX, float translationY) {
        this.mImageView.setScaleX(scaleFactor);
        this.mImageView.setScaleY(scaleFactor);
        this.mImageView.setTranslationX(translationX);
        this.mImageView.setTranslationY(translationY);
    }

    private boolean isFloatEqual(float value, float targetValue) {
        return Math.abs(value - targetValue) < FLOAT_COMPARE_VALUE;
    }
}
