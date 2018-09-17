package android_maps_conflict_avoidance.com.google.common.io;

import java.io.IOException;
import java.io.OutputStream;

public class MarkedOutputStream extends OutputStream {
    private byte[] contents;
    private int[] markers;
    private int nextContent = 0;
    private int nextMarker = 0;

    public MarkedOutputStream() {
        clear();
    }

    public void reset() {
        this.nextContent = 0;
        this.nextMarker = 0;
    }

    public void clear() {
        reset();
        this.contents = new byte[16];
        this.markers = new int[1];
    }

    private static int calculateSize(int needed, int size, int current) {
        int remaining = size - current;
        while (remaining < needed) {
            remaining += size;
            size <<= 1;
        }
        return size;
    }

    private void expandMarkersIfNecessary(int needed) {
        int size = calculateSize(needed, this.markers.length, this.nextMarker);
        if (size > this.markers.length) {
            int[] newMarkers = new int[size];
            System.arraycopy(this.markers, 0, newMarkers, 0, this.nextMarker);
            this.markers = newMarkers;
        }
    }

    public int getMarker(int index) {
        return this.markers[index];
    }

    public int numMarkers() {
        return this.nextMarker;
    }

    public void addMarker(int marker) {
        expandMarkersIfNecessary(1);
        int[] iArr = this.markers;
        int i = this.nextMarker;
        this.nextMarker = i + 1;
        iArr[i] = marker;
    }

    public void setMarker(int position, int marker) {
        this.markers[position] = marker;
    }

    private void expandContentsIfNecessary(int needed) {
        int size = calculateSize(needed, this.contents.length, this.nextContent);
        if (size > this.contents.length) {
            byte[] newContents = new byte[size];
            System.arraycopy(this.contents, 0, newContents, 0, this.nextContent);
            this.contents = newContents;
        }
    }

    public int availableContent() {
        return this.nextContent;
    }

    public void writeContentsTo(OutputStream os, int off, int len) throws IOException {
        os.write(this.contents, off, len);
    }

    public void write(int value) throws IOException {
        expandContentsIfNecessary(1);
        byte[] bArr = this.contents;
        int i = this.nextContent;
        this.nextContent = i + 1;
        bArr[i] = (byte) ((byte) (value & 255));
    }

    public void write(byte[] value) throws IOException {
        expandContentsIfNecessary(value.length);
        System.arraycopy(value, 0, this.contents, this.nextContent, value.length);
        this.nextContent += value.length;
    }

    public void write(byte[] value, int off, int len) throws IOException {
        expandContentsIfNecessary(len);
        System.arraycopy(value, off, this.contents, this.nextContent, len);
        this.nextContent += len;
    }
}
