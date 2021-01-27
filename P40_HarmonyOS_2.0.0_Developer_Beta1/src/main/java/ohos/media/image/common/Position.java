package ohos.media.image.common;

import java.util.Objects;

public class Position {
    public int posX;
    public int posY;

    public Position() {
    }

    public Position(int i, int i2) {
        this.posX = i;
        this.posY = i2;
    }

    public String toString() {
        return "Position{posX=" + this.posX + ", posY=" + this.posY + '}';
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Position)) {
            return false;
        }
        Position position = (Position) obj;
        return this.posX == position.posX && this.posY == position.posY;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.posX), Integer.valueOf(this.posY));
    }
}
