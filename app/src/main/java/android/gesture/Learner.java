package android.gesture;

import java.util.ArrayList;

abstract class Learner {
    private final ArrayList<Instance> mInstances;

    abstract ArrayList<Prediction> classify(int i, int i2, float[] fArr);

    Learner() {
        this.mInstances = new ArrayList();
    }

    void addInstance(Instance instance) {
        this.mInstances.add(instance);
    }

    ArrayList<Instance> getInstances() {
        return this.mInstances;
    }

    void removeInstance(long id) {
        ArrayList<Instance> instances = this.mInstances;
        int count = instances.size();
        for (int i = 0; i < count; i++) {
            Instance instance = (Instance) instances.get(i);
            if (id == instance.id) {
                instances.remove(instance);
                return;
            }
        }
    }

    void removeInstances(String name) {
        ArrayList<Instance> toDelete = new ArrayList();
        ArrayList<Instance> instances = this.mInstances;
        int count = instances.size();
        for (int i = 0; i < count; i++) {
            Instance instance = (Instance) instances.get(i);
            if (!(instance.label == null && name == null)) {
                if (instance.label != null && instance.label.equals(name)) {
                }
            }
            toDelete.add(instance);
        }
        instances.removeAll(toDelete);
    }
}
