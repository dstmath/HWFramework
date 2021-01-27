package ohos.global.icu.text;

/* access modifiers changed from: package-private */
public interface RBNFPostProcessor {
    void init(RuleBasedNumberFormat ruleBasedNumberFormat, String str);

    void process(StringBuilder sb, NFRuleSet nFRuleSet);
}
