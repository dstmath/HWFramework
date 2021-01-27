package org.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public interface TestRule {
    Statement apply(Statement statement, Description description);
}
