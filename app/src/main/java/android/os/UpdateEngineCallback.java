package android.os;

public abstract class UpdateEngineCallback {
    public abstract void onPayloadApplicationComplete(int i);

    public abstract void onStatusUpdate(int i, float f);
}
