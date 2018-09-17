package android.gesture;

public class Prediction {
    public final String name;
    public double score;

    Prediction(String label, double predictionScore) {
        this.name = label;
        this.score = predictionScore;
    }

    public String toString() {
        return this.name;
    }
}
