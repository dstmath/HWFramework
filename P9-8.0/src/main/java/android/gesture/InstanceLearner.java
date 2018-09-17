package android.gesture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

class InstanceLearner extends Learner {
    private static final Comparator<Prediction> sComparator = new Comparator<Prediction>() {
        public int compare(Prediction object1, Prediction object2) {
            double score1 = object1.score;
            double score2 = object2.score;
            if (score1 > score2) {
                return -1;
            }
            if (score1 < score2) {
                return 1;
            }
            return 0;
        }
    };

    InstanceLearner() {
    }

    ArrayList<Prediction> classify(int sequenceType, int orientationType, float[] vector) {
        ArrayList<Prediction> predictions = new ArrayList();
        ArrayList<Instance> instances = getInstances();
        int count = instances.size();
        TreeMap<String, Double> label2score = new TreeMap();
        for (int i = 0; i < count; i++) {
            Instance sample = (Instance) instances.get(i);
            if (sample.vector.length == vector.length) {
                double distance;
                double weight;
                if (sequenceType == 2) {
                    distance = (double) GestureUtils.minimumCosineDistance(sample.vector, vector, orientationType);
                } else {
                    distance = (double) GestureUtils.squaredEuclideanDistance(sample.vector, vector);
                }
                if (distance == 0.0d) {
                    weight = Double.MAX_VALUE;
                } else {
                    weight = 1.0d / distance;
                }
                Double score = (Double) label2score.get(sample.label);
                if (score == null || weight > score.doubleValue()) {
                    label2score.put(sample.label, Double.valueOf(weight));
                }
            }
        }
        for (String name : label2score.keySet()) {
            predictions.add(new Prediction(name, ((Double) label2score.get(name)).doubleValue()));
        }
        Collections.sort(predictions, sComparator);
        return predictions;
    }
}
