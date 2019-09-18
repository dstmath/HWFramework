package android.icu.text;

interface RBNFPostProcessor {
    void init(RuleBasedNumberFormat ruleBasedNumberFormat, String str);

    void process(StringBuilder sb, NFRuleSet nFRuleSet);
}
