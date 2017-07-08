package junit.runner;

import junit.framework.TestFailure;

public interface FailureDetailView {
    void clear();

    void showFailure(TestFailure testFailure);
}
