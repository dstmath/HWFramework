package org.junit;

public class ComparisonFailure extends AssertionError {
    private static final int MAX_CONTEXT_LENGTH = 20;
    private static final long serialVersionUID = 1;
    private String fActual;
    private String fExpected;

    private static class ComparisonCompactor {
        private static final String DIFF_END = "]";
        private static final String DIFF_START = "[";
        private static final String ELLIPSIS = "...";
        /* access modifiers changed from: private */
        public final String actual;
        /* access modifiers changed from: private */
        public final int contextLength;
        /* access modifiers changed from: private */
        public final String expected;

        private class DiffExtractor {
            private final String sharedPrefix;
            private final String sharedSuffix;

            private DiffExtractor() {
                this.sharedPrefix = ComparisonCompactor.this.sharedPrefix();
                this.sharedSuffix = ComparisonCompactor.this.sharedSuffix(this.sharedPrefix);
            }

            public String expectedDiff() {
                return extractDiff(ComparisonCompactor.this.expected);
            }

            public String actualDiff() {
                return extractDiff(ComparisonCompactor.this.actual);
            }

            public String compactPrefix() {
                if (this.sharedPrefix.length() <= ComparisonCompactor.this.contextLength) {
                    return this.sharedPrefix;
                }
                return ComparisonCompactor.ELLIPSIS + this.sharedPrefix.substring(this.sharedPrefix.length() - ComparisonCompactor.this.contextLength);
            }

            public String compactSuffix() {
                if (this.sharedSuffix.length() <= ComparisonCompactor.this.contextLength) {
                    return this.sharedSuffix;
                }
                return this.sharedSuffix.substring(0, ComparisonCompactor.this.contextLength) + ComparisonCompactor.ELLIPSIS;
            }

            private String extractDiff(String source) {
                return ComparisonCompactor.DIFF_START + source.substring(this.sharedPrefix.length(), source.length() - this.sharedSuffix.length()) + ComparisonCompactor.DIFF_END;
            }
        }

        public ComparisonCompactor(int contextLength2, String expected2, String actual2) {
            this.contextLength = contextLength2;
            this.expected = expected2;
            this.actual = actual2;
        }

        public String compact(String message) {
            if (this.expected == null || this.actual == null || this.expected.equals(this.actual)) {
                return Assert.format(message, this.expected, this.actual);
            }
            DiffExtractor extractor = new DiffExtractor();
            String compactedPrefix = extractor.compactPrefix();
            String compactedSuffix = extractor.compactSuffix();
            return Assert.format(message, compactedPrefix + extractor.expectedDiff() + compactedSuffix, compactedPrefix + extractor.actualDiff() + compactedSuffix);
        }

        /* access modifiers changed from: private */
        public String sharedPrefix() {
            int end = Math.min(this.expected.length(), this.actual.length());
            for (int i = 0; i < end; i++) {
                if (this.expected.charAt(i) != this.actual.charAt(i)) {
                    return this.expected.substring(0, i);
                }
            }
            return this.expected.substring(0, end);
        }

        /* access modifiers changed from: private */
        public String sharedSuffix(String prefix) {
            int suffixLength = 0;
            int maxSuffixLength = Math.min(this.expected.length() - prefix.length(), this.actual.length() - prefix.length()) - 1;
            while (suffixLength <= maxSuffixLength && this.expected.charAt((this.expected.length() - 1) - suffixLength) == this.actual.charAt((this.actual.length() - 1) - suffixLength)) {
                suffixLength++;
            }
            return this.expected.substring(this.expected.length() - suffixLength);
        }
    }

    public ComparisonFailure(String message, String expected, String actual) {
        super(message);
        this.fExpected = expected;
        this.fActual = actual;
    }

    public String getMessage() {
        return new ComparisonCompactor(MAX_CONTEXT_LENGTH, this.fExpected, this.fActual).compact(super.getMessage());
    }

    public String getActual() {
        return this.fActual;
    }

    public String getExpected() {
        return this.fExpected;
    }
}
