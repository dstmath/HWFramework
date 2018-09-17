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
        private final Class<? extends Annotation> annotation;
        private boolean methods;
        private final List<RuleValidator> validators;

        /* synthetic */ Builder(Class annotation, Builder -this1) {
            this(annotation);
        }

        private Builder(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            this.methods = false;
            this.validators = new ArrayList();
        }

        Builder forMethods() {
            this.methods = true;
            return this;
        }

        Builder withValidator(RuleValidator validator) {
            this.validators.add(validator);
            return this;
        }

        RuleMemberValidator build() {
            return new RuleMemberValidator(this);
        }
    }

    interface RuleValidator {
        void validate(FrameworkMember<?> frameworkMember, Class<? extends Annotation> cls, List<Throwable> list);
    }

    private static final class DeclaringClassMustBePublic implements RuleValidator {
        /* synthetic */ DeclaringClassMustBePublic(DeclaringClassMustBePublic -this0) {
            this();
        }

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
        /* synthetic */ FieldMustBeARule(FieldMustBeARule -this0) {
            this();
        }

        private FieldMustBeARule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isRuleType(member)) {
                errors.add(new ValidationError(member, annotation, "must implement MethodRule or TestRule."));
            }
        }
    }

    private static final class FieldMustBeATestRule implements RuleValidator {
        /* synthetic */ FieldMustBeATestRule(FieldMustBeATestRule -this0) {
            this();
        }

        private FieldMustBeATestRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isTestRule(member)) {
                errors.add(new ValidationError(member, annotation, "must implement TestRule."));
            }
        }
    }

    private static final class MemberMustBeNonStaticOrAlsoClassRule implements RuleValidator {
        /* synthetic */ MemberMustBeNonStaticOrAlsoClassRule(MemberMustBeNonStaticOrAlsoClassRule -this0) {
            this();
        }

        private MemberMustBeNonStaticOrAlsoClassRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            boolean isMethodRuleMember = RuleMemberValidator.isMethodRule(member);
            boolean isClassRuleAnnotated = member.getAnnotation(ClassRule.class) != null;
            if (!member.isStatic()) {
                return;
            }
            if (isMethodRuleMember || (isClassRuleAnnotated ^ 1) != 0) {
                String message;
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
        /* synthetic */ MemberMustBePublic(MemberMustBePublic -this0) {
            this();
        }

        private MemberMustBePublic() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!member.isPublic()) {
                errors.add(new ValidationError(member, annotation, "must be public."));
            }
        }
    }

    private static final class MemberMustBeStatic implements RuleValidator {
        /* synthetic */ MemberMustBeStatic(MemberMustBeStatic -this0) {
            this();
        }

        private MemberMustBeStatic() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!member.isStatic()) {
                errors.add(new ValidationError(member, annotation, "must be static."));
            }
        }
    }

    private static final class MethodMustBeARule implements RuleValidator {
        /* synthetic */ MethodMustBeARule(MethodMustBeARule -this0) {
            this();
        }

        private MethodMustBeARule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isRuleType(member)) {
                errors.add(new ValidationError(member, annotation, "must return an implementation of MethodRule or TestRule."));
            }
        }
    }

    private static final class MethodMustBeATestRule implements RuleValidator {
        /* synthetic */ MethodMustBeATestRule(MethodMustBeATestRule -this0) {
            this();
        }

        private MethodMustBeATestRule() {
        }

        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!RuleMemberValidator.isTestRule(member)) {
                errors.add(new ValidationError(member, annotation, "must return an implementation of TestRule."));
            }
        }
    }

    RuleMemberValidator(Builder builder) {
        this.annotation = builder.annotation;
        this.methods = builder.methods;
        this.validatorStrategies = builder.validators;
    }

    public void validate(TestClass target, List<Throwable> errors) {
        List<? extends FrameworkMember<?>> members;
        if (this.methods) {
            members = target.getAnnotatedMethods(this.annotation);
        } else {
            members = target.getAnnotatedFields(this.annotation);
        }
        for (FrameworkMember<?> each : members) {
            validateMember(each, errors);
        }
    }

    private void validateMember(FrameworkMember<?> member, List<Throwable> errors) {
        for (RuleValidator strategy : this.validatorStrategies) {
            strategy.validate(member, this.annotation, errors);
        }
    }

    private static Builder classRuleValidatorBuilder() {
        return new Builder(ClassRule.class, null);
    }

    private static Builder testRuleValidatorBuilder() {
        return new Builder(Rule.class, null);
    }

    private static boolean isRuleType(FrameworkMember<?> member) {
        return !isMethodRule(member) ? isTestRule(member) : true;
    }

    private static boolean isTestRule(FrameworkMember<?> member) {
        return TestRule.class.isAssignableFrom(member.getType());
    }

    private static boolean isMethodRule(FrameworkMember<?> member) {
        return MethodRule.class.isAssignableFrom(member.getType());
    }
}
