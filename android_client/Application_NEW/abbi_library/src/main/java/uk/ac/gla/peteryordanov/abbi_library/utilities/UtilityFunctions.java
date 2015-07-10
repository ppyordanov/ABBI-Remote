package uk.ac.gla.peteryordanov.abbi_library.utilities;

/**
 * Created by Peter Yordanov on 9.7.2015 ã..
 */
public class UtilityFunctions {

    //used to change the range of the frequency and volume when scaling the values during UI <-> bracelet communication
    public static int changeRangeMaintainingRatio(int currentValue, int oldMin, int oldMax, int newMin, int newMax){
        int newValue = -1;
        int oldRange = oldMax - oldMin;
        if (oldRange == 0)
            newValue = newMin;
        else
        {
            int newRange = (newMax - newMin);
            newValue = (((currentValue - oldMin) * newRange) / oldRange) + newMin;
        }
        return newValue;
    }

    //pitch to frequency/ frequency to pitch conversion
    public static int pitchToFrequency(int p, int samplingRate) {
        return ((int) (1 / 4294967296.0 * ((float) (p)) * ((float) samplingRate) + 0.5));
    }

    public static int frequencyToPitch(int f, int samplingRate) {
        return ((int) (4294967296.0 * ((float) (f)) / ((float) samplingRate) + 0.5));
    }

    //get a sound filename based on an input index
    public static String lookUpFileNameBasedOnIndex(int index, String[] array){
        return array[index-1];
    }
}
