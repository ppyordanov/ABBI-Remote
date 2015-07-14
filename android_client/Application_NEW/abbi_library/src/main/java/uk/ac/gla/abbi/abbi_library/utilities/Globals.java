package uk.ac.gla.abbi.abbi_library.utilities;

import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioContinuous;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioIntermittent;

/**
 * Created by Peter Yordanov on 9.7.2015 ã..
 */
public class Globals {

    //constants

    /*
    Intermittent sound
     */
    public static final int ABBI_INTERMITTENT_STREAM1_ID = 1;
    public static final int ABBI_INTERMITTENT_STREAM2_ID = 2;

    /*
    Sound controls
     */
    public static final int SOUND_STATE_OFF_ID = 0;
    public static final int SOUND_STATE_TRIGGER_ID = 1;
    public static final int SOUND_STATE_ON_ID = 2;
    /*
    Sound Playback Mode
     */
    public static final int CONTINUOUS_SOUND_MODE_ID = 0;
    public static final int INTERMITTENT_SOUND_MODE_ID = 1;
    public static final int PLAYBACK_SOUND_MODE_ID = 2;

    //digital to analogue conversion sampling rate
    public static final int DAC_SAMPLING_RATE = 22050;

        /*
    Intermittent sound frequency ranges
     */

    //(500 - 4000 Hz) carrier wave
    public static final int BRACELET_HZ_FREQUENCY_RANGE_MIN = 500;
    public static final int BRACELET_HZ_FREQUENCY_RANGE_MAX = 4000;
    public static final int UI_FREQUENCY_RANGE_MIN = 0;
    public static final int UI_FREQUENCY_RANGE_MAX = 35;

    /*
    Sound volume ranges
     */
    public static final int BRACELET_VOLUME_RANGE_MIN = 0;
    public static final int BRACELET_VOLUME_RANGE_MAX = 65534;
    public static final int UI_VOLUME_RANGE_MIN = 0;
    public static final int UI_VOLUME_RANGE_MAX = 15;

    /*
    BPM range
     */
    public static final int BRACELET_BPM_RANGE_MIN = 10;
    public static final int OLD_BPM_RANGE_MAX = 240;
    public static final int NEW_BPM_RANGE_MIN = 0;
    public static final int NEW_BPM_RANGE_MAX = 23;


    // ABBI characteristic with default values
    public static int batteryLevel = 0;
    public static int volumeLevel = 20000;
    public static int soundControlState = SOUND_STATE_TRIGGER_ID;
    public static int audioMode = INTERMITTENT_SOUND_MODE_ID;
    public static AudioContinuous audioContinuous = new AudioContinuous(600, 50000);
    public static AudioIntermittent audioStream1 = new AudioIntermittent(500, 1000, 0, 50000, 50000);
    public static AudioIntermittent audioStream2 = new AudioIntermittent(800, 1000, 0, 50000, 50000);
    public static int audioBPM = 120;
    public static int audioPlayback = 1;

    //ABBI playback sound files list
    public static final String[] SOUND_FILES_PLAYLIST = {"short_explosion.wav",
            "elephant.wav",
            "whistling.wav",
            "piano_hi_repeat.wav",
            "drops_hi_short.wav",
            "smash-wood.wav",
            "06_snar_ping.wav",
            "drumos.wav",
            "monsoer-roar.wav",
            "stonos.wav",
            "dog.oav",
            "italoan_bank-phone.wav",
            "Moquoto-Buzzing.wav",
            "bubboing.wav",
            "horse_neigh.wav",
            "sheep-in-the-field.wav",
            "grandfather-clock.wav",
            "car-driving-away.wav",
            "footsteps.wav",
            "04_waves.wav",
            "wind.wav",
            "water_pouring.wav",
            "rocket.wav",
            "29_bird.wav"};


}
