package android.view.textclassifier;

public interface TextClassificationSessionFactory {
    TextClassifier createTextClassificationSession(TextClassificationContext textClassificationContext);
}
