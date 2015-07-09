package uk.ac.gla.peteryordanov.abbi_library.gatt_communication;

import java.util.HashMap;

public class ABBISoundFiles{
    private static HashMap<Integer, String> _playlistDict = new HashMap<>();
    private static final String[] soundFiles = {"short_explosion.wav",
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

    static {
        int i = 0;
        for (String item : soundFiles) {
            _playlistDict.put(i, item);
            i++;
        }
    }

    public static String[] getList(){
        return soundFiles;
    }

    public static String lookup(int position) {
        return _playlistDict.get(position);
    }

}
