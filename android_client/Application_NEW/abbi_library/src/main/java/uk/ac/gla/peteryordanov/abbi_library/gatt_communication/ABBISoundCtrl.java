package uk.ac.gla.peteryordanov.abbi_library.gatt_communication;

public class ABBISoundCtrl {
    public static final int SoundOff = 0;
    public static final int SoundTrigger = 1;
    public static final int SoundOn = 2;

    public static int getSoundOff() {
        return SoundOff;
    }

    public static int getSoundTrigger() {
        return SoundTrigger;
    }

    public static int getSoundOn() {
        return SoundOn;
    }
}
