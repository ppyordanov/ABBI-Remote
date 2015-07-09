package uk.ac.gla.peteryordanov.abbi_library.gatt_communication;

public class ABBISoundMode {
    public static final int Continuous = 0;
    public static final int Intermittent = 1;
    public static final int Playback = 2;

    public static int getContinuous() {
        return Continuous;
    }

    public static int getIntermittent() {
        return Intermittent;
    }

    public static int getPlayback() {
        return Playback;
    }
}
