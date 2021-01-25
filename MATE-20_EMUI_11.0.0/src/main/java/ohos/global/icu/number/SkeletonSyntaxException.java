package ohos.global.icu.number;

import ohos.global.icu.text.PluralRules;

public class SkeletonSyntaxException extends IllegalArgumentException {
    private static final long serialVersionUID = 7733971331648360554L;

    public SkeletonSyntaxException(String str, CharSequence charSequence) {
        super("Syntax error in skeleton string: " + str + PluralRules.KEYWORD_RULE_SEPARATOR + ((Object) charSequence));
    }

    public SkeletonSyntaxException(String str, CharSequence charSequence, Throwable th) {
        super("Syntax error in skeleton string: " + str + PluralRules.KEYWORD_RULE_SEPARATOR + ((Object) charSequence), th);
    }
}
