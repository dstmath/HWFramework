package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

class CoordinateEntity implements Sequenceable {
    private int down;
    private int left;
    private int right;
    private int up;

    CoordinateEntity(EidAssistant.CutCoordinate cutCoordinate) {
        this.up = cutCoordinate.getUp();
        this.down = cutCoordinate.getDown();
        this.left = cutCoordinate.getLeft();
        this.right = cutCoordinate.getRight();
    }

    public int getUp() {
        return this.up;
    }

    public void setUp(int i) {
        this.up = i;
    }

    public int getDown() {
        return this.down;
    }

    public void setDown(int i) {
        this.down = i;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int i) {
        this.left = i;
    }

    public int getRight() {
        return this.right;
    }

    public void setRight(int i) {
        this.right = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.up);
        parcel.writeInt(this.down);
        parcel.writeInt(this.left);
        parcel.writeInt(this.right);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.up = parcel.readInt();
        this.down = parcel.readInt();
        this.left = parcel.readInt();
        this.right = parcel.readInt();
        return true;
    }
}
