package uk.ac.gla.abbi.abbi_library.utilities;

import android.media.AudioManager;
import android.media.SoundPool;

import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioContinuous;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioIntermittent;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.R;

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

    public static final int BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MAX = 90;
    public static final int BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MIN = 30;

    /*
    BPM range
     */
    public static final int BRACELET_BPM_RANGE_MIN = 10;
    public static final int BRACELET_BPM_RANGE_MAX = 240;
    public static final int UI_BPM_RANGE_MIN = 0;
    public static final int UI_BPM_RANGE_MAX = 23;


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

    //accessibility for circular seekbar
    public static int CURRENT_HAPTIC_BUTTONS_WIRING = -1;

    public static final int MAIN_VOLUME_ID = 1;

    public static final int CONTINUOUS_VOLUME_ID = 10;
    public static final int CONTINUOUS_FREQUENCY_ID = 11;

    public static final int INTERMITTENT_VOLUME_1_ID = 20;
    public static final int INTERMITTENT_VOLUME_2_ID = 21;
    public static final int INTERMITTENT_FREQUENCY_1_ID = 22;
    public static final int INTERMITTENT_FREQUENCY_2_ID = 23;
    public static final int INTERMITTENT_BEATS_PER_MINUTE_ID = 24;

    //accessibility for vibration feedback
    public static final int VOLUME_VIBRATION_MS = 100;
    public static final int FREQUENCY_VIBRATION_MS = 500;
    public static final int BPM_VIBRATION_MS = 1000;


    //sonification constants

    public static final int SOUND_STREAM1_LOOP = 0;
    public static final int SOUND_STREAM2_LOOP = 1;
    public static final float SOUND_PRIMARY_VOLUME = 1;
    public static final float SOUND_SECONDARY_VOLUME = (float) 0.5;


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
