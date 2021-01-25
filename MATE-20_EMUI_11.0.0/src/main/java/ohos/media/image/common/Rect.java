package ohos.media.image.common;

import java.util.Objects;

public class Rect {
    public int height;
    public int minX;
    public int minY;
    public int width;

    public Rect() {
    }

    public Rect(int i, int i2, int i3, int i4) {
        this.minX = i;
        this.minY = i2;
        this.width = i3;
        this.height = i4;
    }

    public Rect(Rect rect) {
        if (rect == null) {
            this.minX = 0;
            this.minY = 0;
            this.width = 0;
            this.height = 0;
            return;
        }
        this.minX = rect.minX;
        this.minY = rect.minY;
        this.width = rect.width;
        this.height = rect.height;
    }

    public String toString() {
        return "Rect{minX=" + this.minX + ", minY=" + this.minY + ", width=" + this.width + ", height=" + this.height + '}';
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Rect)) {
            return false;
        }
        Rect rect = (Rect) obj;
        return this.minX == rect.minX && this.minY == rect.minY && this.width == rect.width && this.height == rect.height;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.minX), Integer.valueOf(this.minY), Integer.valueOf(this.width), Integer.valueOf(this.height));
    }

    public void setEmpty() {
        this.minX = 0;
        this.minY = 0;
        this.width = 0;
        this.height = 0;
    }

    public boolean cropRect(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = this.minX;
        int i8 = i + i3;
        if (i7 >= i8 || i >= this.width + i7 || (i5 = this.minY) >= (i6 = i2 + i4) || i2 >= i5 + this.height) {
            return false;
        }
        if (i7 < i8) {
            this.minX = i;
        }
        if (this.minY < i6) {
            this.minY = i2;
        }
        if (this.minX + this.width > i8) {
            this.width = i3;
        }
        if (this.minY + this.height <= i6) {
            return true;
        }
        this.height = i4;
        return true;
    }
}
