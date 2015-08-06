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

package proj.abbi.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import proj.abbi.CircleSeekBarListener;
import proj.abbi.CircularSeekBar;
import proj.abbi.R;
import uk.ac.gla.abbi.abbi_library.AboutDialogue;
import uk.ac.gla.abbi.abbi_library.gatt_communication.ABBIGattReadWriteCharacteristics;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioContinuous;
import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

/**
 * This class represents the activity associated with the continuous sound emission. It uses layout circular seekbars
 * in order to allow the user to control the sound volume and pitch (frequency)
 */

public class ContinuousActivity extends Activity {

    private int currentFreq;
    private int currentVol;
    private CircularSeekBar frequencyBar;
    private CircularSeekBar volumeBar;
    private Button saveButton;
    private Vibrator vibrator;

    private SoundPool sp;
    private int soundIdVolume, soundIdFrequency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous);

        //retrieve the current frequency and volume settings
        currentFreq = AudioContinuous.getFrequency();
        currentVol = AudioContinuous.getVolume();

        //create references to the CircularSeekBar elements present in the layout and the invisible button used to save the settings
        frequencyBar = (CircularSeekBar) findViewById(R.id.seekBarContFreq);
        volumeBar = (CircularSeekBar) findViewById(R.id.seekBarContVol);
        saveButton = (Button) findViewById(R.id.buttonContSave);

        //set the maximum possible values for frequency (4000 Hz) and volume (35)
        frequencyBar.setMax(Globals.UI_FREQUENCY_RANGE_MAX);
        volumeBar.setMax(Globals.UI_VOLUME_RANGE_MAX);

        //update the frequency and volume indicators by changing the range (bracelet to UI), maintaining the ratio
        frequencyBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentFreq, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentVol, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        saveButton.setVisibility(View.GONE);

        frequencyBar.setOnSeekBarChangeListener(handleFreqChanged);
        volumeBar.setOnSeekBarChangeListener(handleVolChanged);
        saveButton.setOnClickListener(handleSaveClick);

        //accessibility-related features (sonification + vibratory haptic feedback)
        findViewById(R.id.frequencyLayout).setOnClickListener(handleFrequencyClicked);
        findViewById(R.id.volumeLayout).setOnClickListener(handleVolumeClicked);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //sonification
        sp = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        //volume control from the cellphone:
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //load the audio
        soundIdVolume = sp.load(this, R.raw.g_major, 1);
        soundIdFrequency = sp.load(this, R.raw.g_minor, 1);

    }

    /**
     * Create the ActionBar menu
     *
     * @param menu the menu instance
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_continuous, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Trigger a relevant action depending on which ActionBar menu item was interacted with.
     *
     * @param item the selected MenuItem is passed as an input parameter
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //include the 'About' dropdown menu link functionality
        if (id == R.id.menu_info) {
            AboutDialogue.show(ContinuousActivity.this, getString(R.string.about),
                    getString(R.string.close));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * EVENT LISTENERS: implement state changed listeners for the UI components
     */

    /**
     * Save the current volume and frequency settings
     */
    private Button.OnClickListener handleSaveClick = new Button.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            Intent myIntent = new Intent();
            AudioContinuous.setFrequency(currentFreq);
            AudioContinuous.setVolume(currentVol);
            setResult(RESULT_OK, myIntent);
            finish();
        }
    };

    /**
     * Wire the haptic volume buttons (up/down) to the UI seekbars, depending on the value of {@link Globals#CURRENT_HAPTIC_BUTTONS_WIRING}
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (Globals.CURRENT_HAPTIC_BUTTONS_WIRING) {
            case (Globals.CONTINUOUS_FREQUENCY_ID):
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    frequencyBar.setProgress(frequencyBar.getProgress() + 1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    frequencyBar.setProgress(frequencyBar.getProgress() - 1);
                    return true;
                }
            case (Globals.CONTINUOUS_VOLUME_ID):
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    volumeBar.setProgress(volumeBar.getProgress() + 1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    volumeBar.setProgress(volumeBar.getProgress() - 1);
                    return true;
                }

        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * When the volume SeekBar is clicked/tapped:
     * - wire the haptic buttons to the volume SeekBar
     * - play the earcon for the volume control
     * - perform vibratory haptic feedback
     */
    View.OnClickListener handleVolumeClicked = new View.OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.CONTINUOUS_VOLUME_ID;

            //volume
            sp.play(soundIdVolume, Globals.SOUND_SECONDARY_VOLUME, Globals.SOUND_PRIMARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);

            //vibration
            vibrator.vibrate(Globals.VOLUME_VIBRATION_MS);
        }
    };

    /**
     * When the volume SeekBar is clicked/tapped:
     * - wire the haptic buttons to the frequency SeekBar
     * - play the earcon for the pitch control
     * - perform vibratory haptic feedback
     */
    View.OnClickListener handleFrequencyClicked = new View.OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.CONTINUOUS_FREQUENCY_ID;

            //frequency
            sp.play(soundIdFrequency, Globals.SOUND_PRIMARY_VOLUME, Globals.SOUND_SECONDARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);

            vibrator.vibrate(Globals.FREQUENCY_VIBRATION_MS);
            //v.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
        }
    };

    /**
     * EVENT LISTENERS: implement state changed listeners for the UI components
     */

    /**
     * Handle frequency changed events
     */
    private CircularSeekBar.OnCircularSeekBarChangeListener handleFreqChanged = new CircleSeekBarListener() {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView) findViewById(R.id.textViewInd1);

            text.setText(Html.fromHtml("<b>Frequency<br>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX) + " Hz</b>"));

            currentFreq = (seekBar.getProgress() + 5) * 100;
            ABBIGattReadWriteCharacteristics.writeContinuousStream(currentFreq, currentVol);

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }
    };

    /**
     * Handle volume changed events
     */
    private CircularSeekBar.OnCircularSeekBarChangeListener handleVolChanged = new CircleSeekBarListener() {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView) findViewById(R.id.textViewInd2);

            text.setText(Html.fromHtml("<b>Volume<br>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MAX) + " dB</b>"));

            currentVol = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX);
            ABBIGattReadWriteCharacteristics.writeContinuousStream(currentFreq, currentVol);

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }
    };
}
