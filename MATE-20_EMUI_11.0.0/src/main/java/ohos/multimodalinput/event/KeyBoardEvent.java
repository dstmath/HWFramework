package ohos.multimodalinput.event;

public abstract class KeyBoardEvent extends KeyEvent {
    private boolean handledByIme;

    public abstract int getUnicode();

    public abstract boolean isNoncharacterKeyPressed(int i);

    public abstract boolean isNoncharacterKeyPressed(int i, int i2);

    public abstract boolean isNoncharacterKeyPressed(int i, int i2, int i3);

    public void enableIme() {
        this.handledByIme = true;
    }

    public void disableIme() {
        this.handledByIme = false;
    }

    public boolean isHandledByIme() {
        return this.handledByIme;
    }
}
