package ohos.media.image.common;

import java.util.Objects;

public class Size {
    public int height;
    public int width;

    public Size() {
    }

    public Size(int i, int i2) {
        this.width = i;
        this.height = i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Size)) {
            return false;
        }
        Size size = (Size) obj;
        return this.width == size.width && this.height == size.height;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.width), Integer.valueOf(this.height));
    }

    public String toString() {
        return "Size{width=" + this.width + ", height=" + this.height + '}';
    }
}
