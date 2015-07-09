package uk.ac.gla.peteryordanov.abbi_library;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.AudioStream;


public class AudioContinuous extends AudioStream {

    static int freq;   // Frequency of continuous sine wave (500-4000 Hz)
    static int volume;  // Local volume.amplitude of the sine wave ()

    public static int getFreq() {
        return freq;
    }

    public static int getVolume() {
        return volume;
    }

    public static void setFreq(int freq) {
        AudioContinuous.freq = freq;
    }

    public static void setVolume(int volume) {
        AudioContinuous.volume = volume;
    }

    public AudioContinuous (int pitch, int volume){
        freq = pitch;
        AudioContinuous.volume = volume;
    }

    public AudioContinuous(byte[] bytes){
        freq = pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        volume = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray(){
        int pitch = frequencyToPitch(freq);
        return new byte[] {
                (byte)(pitch & 0xff), (byte)((pitch >> 8) & 0xff), (byte)((pitch >> 16) & 0xff), (byte)((pitch >> 24) & 0xff),
                (byte)(volume & 0xff), (byte)((volume >> 8) & 0xff), 0, 0};
    }


}
