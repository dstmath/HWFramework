package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;

final class BoundingBox {
    private ResultPoint bottomLeft;
    private ResultPoint bottomRight;
    private BitMatrix image;
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;
    private ResultPoint topLeft;
    private ResultPoint topRight;

    BoundingBox(BitMatrix image, ResultPoint topLeft, ResultPoint bottomLeft, ResultPoint topRight, ResultPoint bottomRight) throws NotFoundException {
        if (!(topLeft == null && topRight == null) && (!(bottomLeft == null && bottomRight == null) && ((topLeft == null || bottomLeft != null) && (topRight == null || bottomRight != null)))) {
            init(image, topLeft, bottomLeft, topRight, bottomRight);
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    BoundingBox(BoundingBox boundingBox) {
        init(boundingBox.image, boundingBox.topLeft, boundingBox.bottomLeft, boundingBox.topRight, boundingBox.bottomRight);
    }

    private void init(BitMatrix image, ResultPoint topLeft, ResultPoint bottomLeft, ResultPoint topRight, ResultPoint bottomRight) {
        this.image = image;
        this.topLeft = topLeft;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        calculateMinMaxValues();
    }

    static BoundingBox merge(BoundingBox leftBox, BoundingBox rightBox) throws NotFoundException {
        if (leftBox == null) {
            return rightBox;
        }
        if (rightBox == null) {
            return leftBox;
        }
        return new BoundingBox(leftBox.image, leftBox.topLeft, leftBox.bottomLeft, rightBox.topRight, rightBox.bottomRight);
    }

    BoundingBox addMissingRows(int missingStartRows, int missingEndRows, boolean isLeft) throws NotFoundException {
        ResultPoint newTopLeft = this.topLeft;
        ResultPoint newBottomLeft = this.bottomLeft;
        ResultPoint newTopRight = this.topRight;
        ResultPoint newBottomRight = this.bottomRight;
        if (missingStartRows > 0) {
            ResultPoint top = isLeft ? this.topLeft : this.topRight;
            int newMinY = ((int) top.getY()) - missingStartRows;
            if (newMinY < 0) {
                newMinY = 0;
            }
            ResultPoint newTop = new ResultPoint(top.getX(), (float) newMinY);
            if (isLeft) {
                newTopLeft = newTop;
            } else {
                newTopRight = newTop;
            }
        }
        if (missingEndRows > 0) {
            ResultPoint bottom = isLeft ? this.bottomLeft : this.bottomRight;
            int newMaxY = ((int) bottom.getY()) + missingEndRows;
            if (newMaxY >= this.image.getHeight()) {
                newMaxY = this.image.getHeight() - 1;
            }
            ResultPoint newBottom = new ResultPoint(bottom.getX(), (float) newMaxY);
            if (isLeft) {
                newBottomLeft = newBottom;
            } else {
                newBottomRight = newBottom;
            }
        }
        calculateMinMaxValues();
        return new BoundingBox(this.image, newTopLeft, newBottomLeft, newTopRight, newBottomRight);
    }

    private void calculateMinMaxValues() {
        if (this.topLeft == null) {
            this.topLeft = new ResultPoint(0.0f, this.topRight.getY());
            this.bottomLeft = new ResultPoint(0.0f, this.bottomRight.getY());
        } else if (this.topRight == null) {
            this.topRight = new ResultPoint((float) (this.image.getWidth() - 1), this.topLeft.getY());
            this.bottomRight = new ResultPoint((float) (this.image.getWidth() - 1), this.bottomLeft.getY());
        }
        this.minX = (int) Math.min(this.topLeft.getX(), this.bottomLeft.getX());
        this.maxX = (int) Math.max(this.topRight.getX(), this.bottomRight.getX());
        this.minY = (int) Math.min(this.topLeft.getY(), this.topRight.getY());
        this.maxY = (int) Math.max(this.bottomLeft.getY(), this.bottomRight.getY());
    }

    int getMinX() {
        return this.minX;
    }

    int getMaxX() {
        return this.maxX;
    }

    int getMinY() {
        return this.minY;
    }

    int getMaxY() {
        return this.maxY;
    }

    ResultPoint getTopLeft() {
        return this.topLeft;
    }

    ResultPoint getTopRight() {
        return this.topRight;
    }

    ResultPoint getBottomLeft() {
        return this.bottomLeft;
    }

    ResultPoint getBottomRight() {
        return this.bottomRight;
    }
}
