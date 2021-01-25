package ohos.miscservices.inputmethod.adapter;

public class InputParam {
    private int imeOption;
    private int inputType;

    public InputParam(int i, int i2) {
        this.inputType = i;
        this.imeOption = i2;
    }

    public int getInputType() {
        return this.inputType;
    }

    public int getImeOption() {
        return this.imeOption;
    }

    public void setInputType(int i) {
        this.inputType = i;
    }

    public void setImeOption(int i) {
        this.imeOption = i;
    }

    public String toString() {
        return "InputParam{inputType=" + this.inputType + ", imeOption=" + this.imeOption + '}';
    }
}
