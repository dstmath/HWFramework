package javax.security.auth.callback;

import java.io.Serializable;

public class PasswordCallback implements Callback, Serializable {
    private static final long serialVersionUID = 2267422647454909926L;
    private boolean echoOn;
    private char[] inputPassword;
    private String prompt;

    public PasswordCallback(String prompt, boolean echoOn) {
        if (prompt == null || prompt.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.prompt = prompt;
        this.echoOn = echoOn;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public boolean isEchoOn() {
        return this.echoOn;
    }

    public void setPassword(char[] password) {
        char[] cArr = null;
        if (password != null) {
            cArr = (char[]) password.clone();
        }
        this.inputPassword = cArr;
    }

    public char[] getPassword() {
        return this.inputPassword == null ? null : (char[]) this.inputPassword.clone();
    }

    public void clearPassword() {
        if (this.inputPassword != null) {
            for (int i = 0; i < this.inputPassword.length; i++) {
                this.inputPassword[i] = ' ';
            }
        }
    }
}
