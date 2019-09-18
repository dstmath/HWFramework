package java.net;

public final class PasswordAuthentication {
    private char[] password;
    private String userName;

    public PasswordAuthentication(String userName2, char[] password2) {
        this.userName = userName2;
        this.password = (char[]) password2.clone();
    }

    public String getUserName() {
        return this.userName;
    }

    public char[] getPassword() {
        return this.password;
    }
}
