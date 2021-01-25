package android.media.midi;

public abstract class MidiSender {
    public abstract void onConnect(MidiReceiver midiReceiver);

    public abstract void onDisconnect(MidiReceiver midiReceiver);

    public void connect(MidiReceiver receiver) {
        if (receiver != null) {
            onConnect(receiver);
            return;
        }
        throw new NullPointerException("receiver null in MidiSender.connect");
    }

    public void disconnect(MidiReceiver receiver) {
        if (receiver != null) {
            onDisconnect(receiver);
            return;
        }
        throw new NullPointerException("receiver null in MidiSender.disconnect");
    }
}
