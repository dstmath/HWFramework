package android.view.textclassifier;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.EventLog;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLinks;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TextLinksParams {
    private static final Function<TextLinks.TextLink, TextLinks.TextLinkSpan> DEFAULT_SPAN_FACTORY = $$Lambda$TextLinksParams$km8pN8nazHT6NQiHykIrRALWbkE.INSTANCE;
    private final int mApplyStrategy;
    private final TextClassifier.EntityConfig mEntityConfig;
    private final Function<TextLinks.TextLink, TextLinks.TextLinkSpan> mSpanFactory;

    static /* synthetic */ TextLinks.TextLinkSpan lambda$static$0(TextLinks.TextLink textLink) {
        return new TextLinks.TextLinkSpan(textLink);
    }

    private TextLinksParams(int applyStrategy, Function<TextLinks.TextLink, TextLinks.TextLinkSpan> spanFactory) {
        this.mApplyStrategy = applyStrategy;
        this.mSpanFactory = spanFactory;
        this.mEntityConfig = TextClassifier.EntityConfig.createWithHints(null);
    }

    public static TextLinksParams fromLinkMask(int mask) {
        List<String> entitiesToFind = new ArrayList<>();
        if ((mask & 1) != 0) {
            entitiesToFind.add("url");
        }
        if ((mask & 2) != 0) {
            entitiesToFind.add("email");
        }
        if ((mask & 4) != 0) {
            entitiesToFind.add("phone");
        }
        if ((mask & 8) != 0) {
            entitiesToFind.add("address");
        }
        return new Builder().setEntityConfig(TextClassifier.EntityConfig.createWithExplicitEntityList(entitiesToFind)).build();
    }

    public TextClassifier.EntityConfig getEntityConfig() {
        return this.mEntityConfig;
    }

    public int apply(Spannable text, TextLinks textLinks) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(textLinks);
        String textString = text.toString();
        if (Linkify.containsUnsupportedCharacters(textString)) {
            EventLog.writeEvent(1397638484, "116321860", -1, "");
            return 4;
        } else if (!textString.startsWith(textLinks.getText())) {
            return 3;
        } else {
            if (textLinks.getLinks().isEmpty()) {
                return 1;
            }
            int applyCount = 0;
            for (TextLinks.TextLink link : textLinks.getLinks()) {
                TextLinks.TextLinkSpan span = this.mSpanFactory.apply(link);
                if (span != null) {
                    ClickableSpan[] existingSpans = (ClickableSpan[]) text.getSpans(link.getStart(), link.getEnd(), ClickableSpan.class);
                    if (existingSpans.length <= 0) {
                        text.setSpan(span, link.getStart(), link.getEnd(), 33);
                        applyCount++;
                    } else if (this.mApplyStrategy == 1) {
                        for (ClickableSpan existingSpan : existingSpans) {
                            text.removeSpan(existingSpan);
                        }
                        text.setSpan(span, link.getStart(), link.getEnd(), 33);
                        applyCount++;
                    }
                }
            }
            return applyCount == 0 ? 2 : 0;
        }
    }

    public static final class Builder {
        private int mApplyStrategy = 0;
        private Function<TextLinks.TextLink, TextLinks.TextLinkSpan> mSpanFactory = TextLinksParams.DEFAULT_SPAN_FACTORY;

        public Builder setApplyStrategy(int applyStrategy) {
            this.mApplyStrategy = TextLinksParams.checkApplyStrategy(applyStrategy);
            return this;
        }

        public Builder setSpanFactory(Function<TextLinks.TextLink, TextLinks.TextLinkSpan> spanFactory) {
            this.mSpanFactory = spanFactory == null ? TextLinksParams.DEFAULT_SPAN_FACTORY : spanFactory;
            return this;
        }

        public Builder setEntityConfig(TextClassifier.EntityConfig entityConfig) {
            return this;
        }

        public TextLinksParams build() {
            return new TextLinksParams(this.mApplyStrategy, this.mSpanFactory);
        }
    }

    /* access modifiers changed from: private */
    public static int checkApplyStrategy(int applyStrategy) {
        if (applyStrategy == 0 || applyStrategy == 1) {
            return applyStrategy;
        }
        throw new IllegalArgumentException("Invalid apply strategy. See TextLinksParams.ApplyStrategy for options.");
    }
}
