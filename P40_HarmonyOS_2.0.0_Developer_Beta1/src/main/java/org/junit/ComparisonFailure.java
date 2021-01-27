package org.junit;

public class ComparisonFailure extends AssertionError {
    private static final int MAX_CONTEXT_LENGTH = 20;
    private static final long serialVersionUID = 1;
    private String fActual;
    private String fExpected;

    public ComparisonFailure(String message, String expected, String actual) {
        super(message);
        this.fExpected = expected;
        this.fActual = actual;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return new ComparisonCompactor(MAX_CONTEXT_LENGTH, this.fExpected, this.fActual).compact(super.getMessage());
    }

    public String getActual() {
        return this.fActual;
    }

    public String getExpected() {
        return this.fExpected;
    }

    /* access modifiers changed from: private */
    public static class ComparisonCompactor {
        private static final String DIFF_END = "]";
        private static final String DIFF_START = "[";
        private static final String ELLIPSIS = "...";
        private final String actual;
        private final int contextLength;
        private final String expected;

        public ComparisonCompactor(int contextLength2, String expected2, String actual2) {
            this.contextLength = contextLength2;
            this.expected = expected2;
            this.actual = actual2;
        }

        public String compact(String message) {
            String str;
            String str2 = this.expected;
            if (str2 == null || (str = this.actual) == null || str2.equals(str)) {
                return Assert.format(message, this.expected, this.actual);
            }
            DiffExtractor extractor = new DiffExtractor();
            String compactedPrefix = extractor.compactPrefix();
            String compactedSuffix = extractor.compactSuffix();
            return Assert.format(message, compactedPrefix + extractor.expectedDiff() + compactedSuffix, compactedPrefix + extractor.actualDiff() + compactedSuffix);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String sharedPrefix() {
            int end = Math.min(this.expected.length(), this.actual.length());
            for (int i = 0; i < end; i++) {
                if (this.expected.charAt(i) != this.actual.charAt(i)) {
                    return this.expected.substring(0, i);
                }
            }
            return this.expected.substring(0, end);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String sharedSuffix(String prefix) {
            int suffixLength = 0;
            int maxSuffixLength = Math.min(this.expected.length() - prefix.length(), this.actual.length() - prefix.length()) - 1;
            while (suffixLength <= maxSuffixLength) {
                String str = this.expected;
                char charAt = str.charAt((str.length() - 1) - suffixLength);
                String str2 = this.actual;
                if (charAt != str2.charAt((str2.length() - 1) - suffixLength)) {
                    break;
                }
                suffixLength++;
            }
            String str3 = this.expected;
            return str3.substring(str3.length() - suffixLength);
        }

        /* access modifiers changed from: private */
        public class DiffExtractor {
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
                StringBuilder sb = new StringBuilder();
                sb.append(ComparisonCompactor.ELLIPSIS);
                String str = this.sharedPrefix;
                sb.append(str.substring(str.length() - ComparisonCompactor.this.contextLength));
                return sb.toString();
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
    }
}
