package android.gesture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

class InstanceLearner extends Learner {
    private static final Comparator<Prediction> sComparator = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.gesture.InstanceLearner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.gesture.InstanceLearner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.gesture.InstanceLearner.<clinit>():void");
    }

    InstanceLearner() {
    }

    ArrayList<Prediction> classify(int sequenceType, int orientationType, float[] vector) {
        ArrayList<Prediction> predictions = new ArrayList();
        ArrayList<Instance> instances = getInstances();
        int count = instances.size();
        TreeMap<String, Double> label2score = new TreeMap();
        for (int i = 0; i < count; i++) {
            Instance sample = (Instance) instances.get(i);
            int length = sample.vector.length;
            int length2 = vector.length;
            if (length == r0) {
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
