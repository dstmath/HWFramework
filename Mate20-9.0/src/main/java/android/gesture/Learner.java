package android.gesture;

import java.util.ArrayList;

abstract class Learner {
    private final ArrayList<Instance> mInstances = new ArrayList<>();

    /* access modifiers changed from: package-private */
    public abstract ArrayList<Prediction> classify(int i, int i2, float[] fArr);

    Learner() {
    }

    /* access modifiers changed from: package-private */
    public void addInstance(Instance instance) {
        this.mInstances.add(instance);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Instance> getInstances() {
        return this.mInstances;
    }

    /* access modifiers changed from: package-private */
    public void removeInstance(long id) {
        ArrayList<Instance> instances = this.mInstances;
        int count = instances.size();
        for (int i = 0; i < count; i++) {
            Instance instance = instances.get(i);
            if (id == instance.id) {
                instances.remove(instance);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeInstances(String name) {
        ArrayList<Instance> toDelete = new ArrayList<>();
        ArrayList<Instance> instances = this.mInstances;
        int count = instances.size();
        for (int i = 0; i < count; i++) {
            Instance instance = instances.get(i);
            if ((instance.label == null && name == null) || (instance.label != null && instance.label.equals(name))) {
                toDelete.add(instance);
            }
        }
        instances.removeAll(toDelete);
    }
}
