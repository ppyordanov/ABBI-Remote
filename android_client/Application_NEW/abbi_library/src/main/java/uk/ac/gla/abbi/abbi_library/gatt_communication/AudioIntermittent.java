/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.gla.abbi.abbi_library.gatt_communication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

/**
 * This class establishes an object structure to be used to encapsulate the intermittent sound parameters.
 * AudioIntermittent's constructors:
 * <p/>
 * - transform a byte[] to the relevant parameters and store them
 * - or directly create an AudioContinuous object from 5 integers
 * <p/>
 * The instance fields for this class that are used to construct an AudioIntermittent object are as follows:
 * <p/>
 * - frequency
 * - attack
 * - sustain
 * - decay
 * - volume
 */


public class AudioIntermittent {
    int frequency;   // Carrier frequency of the intermittent sound (500 - 4000 Hz)
    int attack;  // Rise time of the carrier wave (1000 - 999999 uS)
    int sustain; // Sustain period of the carrier wave (0 - 999999 uS)
    int decay;   // Fall time of the carrier wave (0 - 999999 uS)
    int volume;  // Local volume/amplitude of the carrier wave ()

    /**
     * This constructor takes 5 input parameters to create an AudioIntermittent object
     *
     * @param frequency the sound frequency
     * @param attack    represents the sound attack
     * @param sustain   sound sustain
     * @param decay     represents the decay time of the sound
     * @param volume    sound volume
     */
    public AudioIntermittent(int frequency, int attack, int sustain, int decay, int volume) {
        this.frequency = frequency;
        this.attack = attack;
        this.sustain = sustain;
        this.decay = decay;
        this.volume = volume;
    }

    /**
     * This constructor takes a byte[] array as an input parameter and extracts the
     * frequency, attack, sustain, decay, volume parameters from the input.
     *
     * @param bytes the input array used to create an AudioIntermittent object
     */
    public AudioIntermittent(byte[] bytes) {
        frequency = UtilityFunctions.pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(), Globals.DAC_SAMPLING_RATE);
        attack = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        sustain = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        decay = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        volume = ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * A conversion function used to retrieve an AudioIntermittent object in the form of a byte[] array
     *
     * @return a byte[] after performing the relevant variable conversions
     */
    public byte[] getByteArray() {
        int pitch = UtilityFunctions.frequencyToPitch(frequency, Globals.DAC_SAMPLING_RATE);
        return new byte[]{
                (byte) (pitch & 0xff), (byte) ((pitch >> 8) & 0xff), (byte) ((pitch >> 16) & 0xff), (byte) ((pitch >> 24) & 0xff),
                (byte) (attack & 0xff), (byte) ((attack >> 8) & 0xff), (byte) ((attack >> 16) & 0xff), (byte) ((attack >> 24) & 0xff),
                (byte) (sustain & 0xff), (byte) ((sustain >> 8) & 0xff), (byte) ((sustain >> 16) & 0xff), (byte) ((sustain >> 24) & 0xff),
                (byte) (decay & 0xff), (byte) ((decay >> 8) & 0xff), (byte) ((decay >> 16) & 0xff), (byte) ((decay >> 24) & 0xff),
                (byte) (volume & 0xff), (byte) ((volume >> 8) & 0xff), 0, 0};
    }

    /**
     * Getters and setters are implemented below.
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getVolume() {
        return volume;
    }
}
