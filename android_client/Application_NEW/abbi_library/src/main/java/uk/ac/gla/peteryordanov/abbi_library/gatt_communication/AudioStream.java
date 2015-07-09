package uk.ac.gla.peteryordanov.abbi_library.gatt_communication;

public abstract class AudioStream {
    static final int DAC_SAMPLING_RATE = 22050;

    protected int pitchToFrequency(int p) {
        return ((int) (1 / 4294967296.0 * ((float) (p)) * ((float) DAC_SAMPLING_RATE) + 0.5));
    }

    protected int frequencyToPitch(int f) {
        return ((int) (4294967296.0 * ((float) (f)) / ((float) DAC_SAMPLING_RATE) + 0.5));
    }

    protected abstract byte[] getByteArray();
}
