package android.media.midi;

public abstract class MidiSender {
    public abstract void onConnect(MidiReceiver midiReceiver);

    public abstract void onDisconnect(MidiReceiver midiReceiver);

    public void connect(MidiReceiver receiver) {
        if (receiver == null) {
            throw new NullPointerException("receiver null in MidiSender.connect");
        }
        onConnect(receiver);
    }

    public void disconnect(MidiReceiver receiver) {
        if (receiver == null) {
            throw new NullPointerException("receiver null in MidiSender.disconnect");
        }
        onDisconnect(receiver);
    }
}
