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
 * This class establishes an object structure to be used to encapsulate the continuous sound parameters.
 * AudioContinuous's constructors:
 * - transform a byte[] to pitch and frequency and store them
 * - or directly create an AudioContinuous object from two integers (corresponding to pitch and volume)
 */

public class AudioContinuous {

    static int frequency;   // Frequency of continuous sine wave (500-4000 Hz)
    static int volume;  // Local volume.amplitude of the sine wave ()

    /**
     * Constructor taking 2 integers as parameters.
     *
     * @param pitch  representing the sound's putch
     * @param volume representing the sound's volume
     */
    public AudioContinuous(int pitch, int volume) {
        frequency = pitch;
        AudioContinuous.volume = volume;
    }

    /**
     * Constructor taking a byte[] as an input parameter
     *
     * @param bytes an input array that is used to retrieve the frequency and volume values
     */
    public AudioContinuous(byte[] bytes) {
        frequency = UtilityFunctions.pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(), Globals.DAC_SAMPLING_RATE);
        volume = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray() {
        int pitch = UtilityFunctions.frequencyToPitch(frequency, Globals.DAC_SAMPLING_RATE);
        return new byte[]{
                (byte) (pitch & 0xff), (byte) ((pitch >> 8) & 0xff), (byte) ((pitch >> 16) & 0xff), (byte) ((pitch >> 24) & 0xff),
                (byte) (volume & 0xff), (byte) ((volume >> 8) & 0xff), 0, 0};
    }

    /**
     * Getters and setters have been implemented below.
     */
    public static int getFrequency() {
        return frequency;
    }

    public static int getVolume() {
        return volume;
    }

    public static void setFrequency(int frequency) {
        AudioContinuous.frequency = frequency;
    }

    public static void setVolume(int volume) {
        AudioContinuous.volume = volume;
    }
}
