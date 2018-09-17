package android.net.http;

public class LoggingEventHandler implements EventHandler {
    public void requestSent() {
        HttpLog.v("LoggingEventHandler:requestSent()");
    }

    public void status(int major_version, int minor_version, int code, String reason_phrase) {
    }

    public void headers(Headers headers) {
    }

    public void locationChanged(String newLocation, boolean permanent) {
    }

    public void data(byte[] data, int len) {
    }

    public void endData() {
    }

    public void certificate(SslCertificate certificate) {
    }

    public void error(int id, String description) {
    }

    public boolean handleSslErrorRequest(SslError error) {
        return false;
    }
}
