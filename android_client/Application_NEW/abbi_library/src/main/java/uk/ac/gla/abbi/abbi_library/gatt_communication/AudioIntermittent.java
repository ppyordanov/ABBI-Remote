package uk.ac.gla.abbi.abbi_library.gatt_communication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

public class AudioIntermittent {
    int freq;   // Carrier frequency of the intermittent sound (500 - 4000 Hz)
    int attack;  // Rise time of the carrier wave (1000 - 999999 uS)
    int sustain; // Sustain period of the carrier wave (0 - 999999 uS)
    int decay;   // Fall time of the carrier wave (0 - 999999 uS)
    int volume;  // Local volume/amplitude of the carrier wave ()

    public AudioIntermittent(int freq, int attack, int sustain, int decay, int volume){
        this.freq = freq;
        this.attack = attack;
        this.sustain = sustain;
        this.decay = decay;
        this.volume = volume;
    }

    public AudioIntermittent(byte[] bytes){
        freq = UtilityFunctions.pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(), Globals.DAC_SAMPLING_RATE);
        attack = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        sustain = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        decay = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        volume = ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray(){
        int pitch = UtilityFunctions.frequencyToPitch(freq, Globals.DAC_SAMPLING_RATE);
        return new byte[] {
                (byte)(pitch & 0xff), (byte)((pitch >> 8) & 0xff), (byte)((pitch >> 16) & 0xff), (byte)((pitch >> 24) & 0xff),
                (byte)(attack & 0xff), (byte)((attack >> 8) & 0xff), (byte)((attack >> 16) & 0xff), (byte)((attack >> 24) & 0xff),
                (byte)(sustain & 0xff), (byte)((sustain >> 8) & 0xff), (byte)((sustain >> 16) & 0xff), (byte)((sustain >> 24) & 0xff),
                (byte)(decay & 0xff), (byte)((decay >> 8) & 0xff), (byte)((decay >> 16) & 0xff), (byte)((decay >> 24) & 0xff),
                (byte)(volume & 0xff), (byte)((volume >> 8) & 0xff), 0, 0};
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getFreq() {

        return freq;
    }

    public int getVolume() {
        return volume;
    }
}
