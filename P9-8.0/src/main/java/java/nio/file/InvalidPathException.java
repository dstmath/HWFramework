package java.nio.file;

public class InvalidPathException extends IllegalArgumentException {
    static final long serialVersionUID = 4355821422286746137L;
    private int index;
    private String input;

    public InvalidPathException(String input, String reason, int index) {
        super(reason);
        if (input == null || reason == null) {
            throw new NullPointerException();
        } else if (index < -1) {
            throw new IllegalArgumentException();
        } else {
            this.input = input;
            this.index = index;
        }
    }

    public InvalidPathException(String input, String reason) {
        this(input, reason, -1);
    }

    public String getInput() {
        return this.input;
    }

    public String getReason() {
        return super.getMessage();
    }

    public int getIndex() {
        return this.index;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(getReason());
        if (this.index > -1) {
            sb.append(" at index ");
            sb.append(this.index);
        }
        sb.append(": ");
        sb.append(this.input);
        return sb.toString();
    }
}
