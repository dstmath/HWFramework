package org.junit.internal.runners.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMember;
import org.junit.runners.model.TestClass;

public class RuleMemberValidator {
    public static final RuleMemberValidator CLASS_RULE_METHOD_VALIDATOR = classRuleValidatorBuilder().forMethods().withValidator(new DeclaringClassMustBePublic()).withValidator(new MemberMustBeStatic()).withValidator(new MemberMustBePublic()).withValidator(new MethodMustBeATestRule()).build();
    public static final RuleMemberValidator CLASS_RULE_VALIDATOR = classRuleValidatorBuilder().withValidator(new DeclaringClassMustBePublic()).withValidator(new MemberMustBeStatic()).withValidator(new MemberMustBePublic()).withValidator(new FieldMustBeATestRule()).build();
    public static final RuleMemberValidator RULE_METHOD_VALIDATOR = testRuleValidatorBuilder().forMethods().withValidator(new MemberMustBeNonStaticOrAlsoClassRule()).withValidator(new MemberMustBePublic()).withValidator(new MethodMustBeARule()).build();
    public static final RuleMemberValidator RULE_VALIDATOR = testRuleValidatorBuilder().withValidator(new MemberMustBeNonStaticOrAlsoClassRule()).withValidator(new MemberMustBePublic()).withValidator(new FieldMustBeARule()).build();
    private final Class<? extends Annotation> annotation;
    private final boolean methods;
    private final List<RuleValidator> validatorStrategies;

    private static class Builder {
        /* access modifiers changed from: private */
        public final Class<? extends Annotation> annotation;
        /* access modifiers changed from: private */
        public boolean methods;
        /* access modifiers changed from: private */
        public final List<RuleValidator> validators;

        private Builder(Class<? extends Annotation> annotation2) {
            this.annotation = annotation2;
            this.methods = false;
            this.validators = new ArrayList();
        }

        /* access modifiers changed from: package-private */
        public Builder forMethods() {
            this.methods = true;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder withValidator(RuleValidator validator) {
            this.validators.add(validator);
            return this;
        }

        /* access modifiers changed from: package-private */
        public RuleMemberValidator build() {
            return new RuleMemberValidator(this);
        }
    }

    private static final class DeclaringClassMustBePublic implements RuleValidator {
        private DeclaringClassMustBePublic() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!isDeclaringClassPublic(member)) {
                errors.add(new ValidationError(member, annotation, "must be declared in a public class."));
            }
        }

        private boolean isDeclaringClassPublic(FrameworkMember<?> member) {
            return Modifier.isPublic(member.getDeclaringClass().getModifiers());
        }
    }

    private static final class FieldMustBeARule implements RuleValidator {
        private FieldMustBeARule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isRuleType(member)) {
                errors.add(new ValidationError(member, annotation, "must implement MethodRule or TestRule."));
            }
        }
    }

    private static final class FieldMustBeATestRule implements RuleValidator {
        private FieldMustBeATestRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isTestRule(member)) {
                errors.add(new ValidationError(member, annotation, "must implement TestRule."));
            }
        }
    }

    private static final class MemberMustBeNonStaticOrAlsoClassRule implements RuleValidator {
        private MemberMustBeNonStaticOrAlsoClassRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            String message;
            boolean isMethodRuleMember = RuleMemberValidator.isMethodRule(member);
            boolean isClassRuleAnnotated = member.getAnnotation(ClassRule.class) != null;
            if (!member.isStatic()) {
                return;
            }
            if (isMethodRuleMember || !isClassRuleAnnotated) {
                if (RuleMemberValidator.isMethodRule(member)) {
                    message = "must not be static.";
                } else {
                    message = "must not be static or it must be annotated with @ClassRule.";
                }
                errors.add(new ValidationError(member, annotation, message));
            }
        }
    }

    private static final class MemberMustBePublic implements RuleValidator {
        private MemberMustBePublic() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!member.isPublic()) {
                errors.add(new ValidationError(member, annotation, "must be public."));
            }
        }
    }

    private static final class MemberMustBeStatic implements RuleValidator {
        private MemberMustBeStatic() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!member.isStatic()) {
                errors.add(new ValidationError(member, annotation, "must be static."));
            }
        }
    }

    private static final class MethodMustBeARule implements RuleValidator {
        private MethodMustBeARule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isRuleType(member)) {
                errors.add(new ValidationError(member, annotation, "must return an implementation of MethodRule or TestRule."));
            }
        }
    }

    private static final class MethodMustBeATestRule implements RuleValidator {
        private MethodMustBeATestRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isTestRule(member)) {
                errors.add(new ValidationError(member, annotation, "must return an implementation of TestRule."));
            }
        }
    }

    interface RuleValidator {
        void validate(FrameworkMember<?> frameworkMember, Class<? extends Annotation> cls, List<Throwable> list);
    }

    RuleMemberValidator(Builder builder) {
        this.annotation = builder.annotation;
        this.methods = builder.methods;
        this.validatorStrategies = builder.validators;
    }

    public void validate(TestClass target, List<Throwable> errors) {
        List<FrameworkMember<?>> list;
        if (this.methods) {
            list = target.getAnnotatedMethods(this.annotation);
        } else {
            list = target.getAnnotatedFields(this.annotation);
        }
        for (FrameworkMember<?> each : list) {
            validateMember(each, errors);
        }
    }

    private void validateMember(FrameworkMember<?> member, List<Throwable> errors) {
        for (RuleValidator strategy : this.validatorStrategies) {
            strategy.validate(member, this.annotation, errors);
        }
    }

    private static Builder classRuleValidatorBuilder() {
        return new Builder(ClassRule.class);
    }

    private static Builder testRuleValidatorBuilder() {
        return new Builder(Rule.class);
    }

    /* access modifiers changed from: private */
    public static boolean isRuleType(FrameworkMember<?> member) {
        return isMethodRule(member) || isTestRule(member);
    }

    /* access modifiers changed from: private */
    public static boolean isTestRule(FrameworkMember<?> member) {
        return TestRule.class.isAssignableFrom(member.getType());
    }

    /* access modifiers changed from: private */
    public static boolean isMethodRule(FrameworkMember<?> member) {
        return MethodRule.class.isAssignableFrom(member.getType());
    }
}
