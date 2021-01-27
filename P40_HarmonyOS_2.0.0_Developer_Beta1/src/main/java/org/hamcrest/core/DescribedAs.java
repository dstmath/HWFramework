package org.hamcrest.core;

import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class DescribedAs<T> extends BaseMatcher<T> {
    private static final Pattern ARG_PATTERN = Pattern.compile("%([0-9]+)");
    private final String descriptionTemplate;
    private final Matcher<T> matcher;
    private final Object[] values;

    public DescribedAs(String descriptionTemplate2, Matcher<T> matcher2, Object[] values2) {
        this.descriptionTemplate = descriptionTemplate2;
        this.matcher = matcher2;
        this.values = (Object[]) values2.clone();
    }

    @Override // org.hamcrest.Matcher
    public boolean matches(Object o) {
        return this.matcher.matches(o);
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        java.util.regex.Matcher arg = ARG_PATTERN.matcher(this.descriptionTemplate);
        int textStart = 0;
        while (arg.find()) {
            description.appendText(this.descriptionTemplate.substring(textStart, arg.start()));
            description.appendValue(this.values[Integer.parseInt(arg.group(1))]);
            textStart = arg.end();
        }
        if (textStart < this.descriptionTemplate.length()) {
            description.appendText(this.descriptionTemplate.substring(textStart));
        }
    }

    @Override // org.hamcrest.BaseMatcher, org.hamcrest.Matcher
    public void describeMismatch(Object item, Description description) {
        this.matcher.describeMismatch(item, description);
    }

    public static <T> Matcher<T> describedAs(String description, Matcher<T> matcher2, Object... values2) {
        return new DescribedAs(description, matcher2, values2);
    }
}
