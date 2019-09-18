package java.nio.file;

public class InvalidPathException extends IllegalArgumentException {
    static final long serialVersionUID = 4355821422286746137L;
    private int index;
    private String input;

    public InvalidPathException(String input2, String reason, int index2) {
        super(reason);
        if (input2 == null || reason == null) {
            throw new NullPointerException();
        } else if (index2 >= -1) {
            this.input = input2;
            this.index = index2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public InvalidPathException(String input2, String reason) {
        this(input2, reason, -1);
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
