package uk.ac.gla.peteryordanov.abbi_library.utility;

import uk.ac.gla.peteryordanov.abbi_library.AudioContinuous;
import uk.ac.gla.peteryordanov.abbi_library.AudioIntermittent;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBISoundCtrl;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBISoundMode;

/**
 * Created by Peter Yordanov on 9.7.2015 ã..
 */
public class Globals {

    // ABBI characteristic with default values
    public static int batteryLevel = 0;
    public static int volumeLevel = 20000;
    public static int soundCtrl = ABBISoundCtrl.SoundTrigger;
    public static int audioMode = ABBISoundMode.Intermittent;
    public static AudioContinuous audioContinuous = new AudioContinuous(600, 50000);
    public static AudioIntermittent audioStream1 = new AudioIntermittent(500, 1000, 0, 50000, 50000);
    public static AudioIntermittent audioStream2 = new AudioIntermittent(800, 1000, 0, 50000, 50000);
    public static int audioBPM = 120;
    public static int audioPlayback = 1;

}
