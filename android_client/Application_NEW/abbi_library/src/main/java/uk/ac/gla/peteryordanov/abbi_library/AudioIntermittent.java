package uk.ac.gla.peteryordanov.abbi_library;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.AudioStream;

public class AudioIntermittent extends AudioStream {
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
        freq = pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        attack = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        sustain = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        decay = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        volume = ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray(){
        int pitch = frequencyToPitch(freq);
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

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setSustain(int sustain) {
        this.sustain = sustain;
    }

    public void setDecay(int decay) {
        this.decay = decay;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getFreq() {

        return freq;
    }

    public int getAttack() {
        return attack;
    }

    public int getSustain() {
        return sustain;
    }

    public int getDecay() {
        return decay;
    }

    public int getVolume() {
        return volume;
    }
}
