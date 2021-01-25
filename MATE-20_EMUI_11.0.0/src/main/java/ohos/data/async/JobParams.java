package ohos.data.async;

public class JobParams {
    private Object cookie;
    private Integer token;

    public int getToken() {
        return this.token.intValue();
    }

    public Object getCookie() {
        return this.cookie;
    }

    private JobParams(Builder builder) {
        this.token = builder.token;
        this.cookie = builder.cookie;
    }

    public static final class Builder {
        private Object cookie;
        private Integer token;

        public Builder setToken(Integer num) {
            this.token = num;
            return this;
        }

        public Builder setCookie(Object obj) {
            this.cookie = obj;
            return this;
        }

        public JobParams build() {
            if (this.token != null) {
                return new JobParams(this);
            }
            throw new IllegalArgumentException("Value of token should not be null, must be a effective value.");
        }
    }
}
