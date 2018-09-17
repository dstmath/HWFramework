package android.gesture;

import java.util.ArrayList;
import java.util.Set;

public abstract class GestureLibrary {
    protected final GestureStore mStore;

    public abstract boolean load();

    public abstract boolean save();

    protected GestureLibrary() {
        this.mStore = new GestureStore();
    }

    public boolean isReadOnly() {
        return false;
    }

    public Learner getLearner() {
        return this.mStore.getLearner();
    }

    public void setOrientationStyle(int style) {
        this.mStore.setOrientationStyle(style);
    }

    public int getOrientationStyle() {
        return this.mStore.getOrientationStyle();
    }

    public void setSequenceType(int type) {
        this.mStore.setSequenceType(type);
    }

    public int getSequenceType() {
        return this.mStore.getSequenceType();
    }

    public Set<String> getGestureEntries() {
        return this.mStore.getGestureEntries();
    }

    public ArrayList<Prediction> recognize(Gesture gesture) {
        return this.mStore.recognize(gesture);
    }

    public void addGesture(String entryName, Gesture gesture) {
        this.mStore.addGesture(entryName, gesture);
    }

    public void removeGesture(String entryName, Gesture gesture) {
        this.mStore.removeGesture(entryName, gesture);
    }

    public void removeEntry(String entryName) {
        this.mStore.removeEntry(entryName);
    }

    public ArrayList<Gesture> getGestures(String entryName) {
        return this.mStore.getGestures(entryName);
    }
}
