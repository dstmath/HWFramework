package org.junit.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RuleChain implements TestRule {
    private static final RuleChain EMPTY_CHAIN = new RuleChain(Collections.emptyList());
    private List<TestRule> rulesStartingWithInnerMost;

    public static RuleChain emptyRuleChain() {
        return EMPTY_CHAIN;
    }

    public static RuleChain outerRule(TestRule outerRule) {
        return emptyRuleChain().around(outerRule);
    }

    private RuleChain(List<TestRule> rules) {
        this.rulesStartingWithInnerMost = rules;
    }

    public RuleChain around(TestRule enclosedRule) {
        List<TestRule> rulesOfNewChain = new ArrayList();
        rulesOfNewChain.add(enclosedRule);
        rulesOfNewChain.addAll(this.rulesStartingWithInnerMost);
        return new RuleChain(rulesOfNewChain);
    }

    public Statement apply(Statement base, Description description) {
        for (TestRule each : this.rulesStartingWithInnerMost) {
            base = each.apply(base, description);
        }
        return base;
    }
}
