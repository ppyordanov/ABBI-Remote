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

package uk.ac.gla.abbi.abbi_library.utilities;

/**
 * This class contains utility functions that are used to implement the demo Android application ABBI Remote.
 * These are scaling/ normalization functions and conversion functions.
 * <p/>
 * Created by Peter Yordanov on 9.7.2015 ã..
 */
public class UtilityFunctions {

    /**
     * This function is used to change the range of the frequency and volume when scaling the values during UI <-> bracelet communication
     * <p/>
     * - ( 0-65534 /bracelet/ <-> 0-15 /UI/ ) for sound volume
     * - ( 500-4000 Hz /bracelet/ <-> 0-35 /UI/ ) for sound frequency/ pitch
     * - ( 10-240 /bracelet/ <-> 0-23 /UI/ ) for beats per minute (bpm)
     *
     * @param currentValue this parameter represents the current value that needs to be scaled
     * @param oldMin       represents the minimum possible value of the current parameter range
     * @param oldMax       represents the maximum possible value of the current parameter range
     * @param newMin       represents the minimum possible value of the new range for the variable
     * @param newMax       represents the maximum possible value of the new range for the variable
     * @return the new, scaled value
     */
    public static int changeRangeMaintainingRatio(int currentValue, int oldMin, int oldMax, int newMin, int newMax) {
        int newValue;
        int oldRange = oldMax - oldMin;
        if (oldRange == 0)
            newValue = newMin;
        else {
            int newRange = (newMax - newMin);
            newValue = (((currentValue - oldMin) * newRange) / oldRange) + newMin;
        }
        return newValue;
    }

    /**
     * Pitch to frequency conversion function
     *
     * @param p            this is the current pitch
     * @param samplingRate current sampling rate
     * @return the equivalent frequency
     */
    public static int pitchToFrequency(int p, int samplingRate) {
        return ((int) (1 / 4294967296.0 * ((float) (p)) * ((float) samplingRate) + 0.5));
    }

    /**
     * Frequency to pitch conversion function
     *
     * @param f            the current frequency
     * @param samplingRate the current sampling rate
     * @return the equivalent pitch
     */
    public static int frequencyToPitch(int f, int samplingRate) {
        return ((int) (4294967296.0 * ((float) (f)) / ((float) samplingRate) + 0.5));
    }

    /**
     * A very simple function to retrieve a sound filename based on an input index
     *
     * @param index input index parameter, a query id
     * @param array a String[] array to be used to return an element
     * @return
     */
    public static String lookUpFileNameBasedOnIndex(int index, String[] array) {
        return array[index - 1];
    }


}
