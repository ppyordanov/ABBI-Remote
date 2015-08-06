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
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import proj.abbi.CircleSeekBarListener;
import proj.abbi.CircularSeekBar;
import proj.abbi.R;
import uk.ac.gla.abbi.abbi_library.AboutDialogue;
import uk.ac.gla.abbi.abbi_library.gatt_communication.ABBIGattReadWriteCharacteristics;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioIntermittent;
import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

/**
 * This class represents the activity associated with the intermittent sound emission. It uses layout circular seekbars
 * in order to allow the user to control the sound volume and pitch (frequency) for each stream, as well as
 * the BPM (beats per minute) rate
 */

public class IntermittentActivity extends Activity {

    private CircularSeekBar frequencyBar1;
    private CircularSeekBar volumeBar1;
    private CircularSeekBar frequencyBar2;
    private CircularSeekBar volumeBar2;
    private CircularSeekBar bpmBar;


    private CheckBox lockRatio;

    private AudioIntermittent ai1;
    private AudioIntermittent ai2;
    private int currentBpm;

    private Button saveButton;
    private Vibrator vibrator;
    private SoundPool sp;
    private int soundIdVolume, soundIdFrequency, soundIdBpm;

    /**
     * When the activity is started:
     * - get current frequency, attack, delay, sustan, volume (AudioIntermittent object)
     * - retrieve current BPM
     * - create references for the volume, frequency and BPM seekbars for each of the 2 streams + stream equality checkbox
     * - set the maximum possible values
     * - wire the layout reference objects to the relevant handlers
     * - enable accessibility features:
     * - haptic feedback
     * - earcons sonification
     * - haptic volume buttons interaction
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermittent);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        ai1 = new AudioIntermittent(Globals.audioStream1.getByteArray());
        ai2 = new AudioIntermittent(Globals.audioStream2.getByteArray());
        currentBpm = Globals.audioBPM;

        frequencyBar1 = (CircularSeekBar) findViewById(R.id.seekBarStream1Freq);
        volumeBar1 = (CircularSeekBar) findViewById(R.id.seekBarStream1Vol);
        frequencyBar2 = (CircularSeekBar) findViewById(R.id.seekBarStream2Freq);
        volumeBar2 = (CircularSeekBar) findViewById(R.id.seekBarStream2Vol);
        bpmBar = (CircularSeekBar) findViewById(R.id.seekBarBpm);
        saveButton = (Button) findViewById(R.id.buttonIntermSave);

        frequencyBar1.setMax(Globals.UI_FREQUENCY_RANGE_MAX);
        volumeBar1.setMax(Globals.UI_VOLUME_RANGE_MAX);
        frequencyBar2.setMax(Globals.UI_FREQUENCY_RANGE_MAX);
        volumeBar2.setMax(Globals.UI_VOLUME_RANGE_MAX);
        bpmBar.setMax(Globals.UI_BPM_RANGE_MAX);

        lockRatio = (CheckBox) findViewById(R.id.lockRatioCheckBox);

        frequencyBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getFrequency(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        frequencyBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getFrequency(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        bpmBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentBpm, Globals.BRACELET_BPM_RANGE_MIN, Globals.BRACELET_BPM_RANGE_MAX, Globals.UI_BPM_RANGE_MIN, Globals.UI_BPM_RANGE_MAX));
        saveButton.setVisibility(View.GONE);

        frequencyBar1.setOnSeekBarChangeListener(handleProgressChanged);
        volumeBar1.setOnSeekBarChangeListener(handleProgressChanged);
        frequencyBar2.setOnSeekBarChangeListener(handleProgressChanged);
        volumeBar2.setOnSeekBarChangeListener(handleProgressChanged);
        bpmBar.setOnSeekBarChangeListener(handleProgressChanged);
        saveButton.setOnClickListener(handleSaveClick);


        //accessibility
        findViewById(R.id.seekBarStream1VolLayout).setOnClickListener(handleVolume1Clicked);
        findViewById(R.id.seekBarStream1FreqLayout).setOnClickListener(handleFrequency1Clicked);
        findViewById(R.id.seekBarStream2VolLayout).setOnClickListener(handleVolume2Clicked);
        findViewById(R.id.seekBarStream2FreqLayout).setOnClickListener(handleFrequency2Clicked);
        findViewById(R.id.seekBarBpmLayout).setOnClickListener(handleBpmClicked);


        //haptic feedback
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //sonification
        sp = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        //volume control from the cellphone:
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //load the audio
        soundIdVolume = sp.load(this, R.raw.g_major, 1);
        soundIdFrequency = sp.load(this, R.raw.g_minor, 1);
        soundIdBpm = sp.load(this, R.raw.c, 1);

    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu reference to the Menu object
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_intermittent, menu);
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
        if (id == R.id.menu_info) {
            AboutDialogue.show(IntermittentActivity.this, getString(R.string.about),
                    getString(R.string.close));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * EVENT LISTENERS: implement state changed listeners for the UI components
     */

    /**
     * configure the current {@link Globals#CURRENT_HAPTIC_BUTTONS_WIRING} for volume and frequency
     */
    OnClickListener handleVolume1Clicked = new OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_1_ID;

            //volume 1
            sp.play(soundIdVolume, Globals.SOUND_SECONDARY_VOLUME, Globals.SOUND_PRIMARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);

            vibrator.vibrate(Globals.VOLUME_VIBRATION_MS);
            //v.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
        }
    };
    OnClickListener handleVolume2Clicked = new OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_2_ID;

            //volume 2
            sp.play(soundIdVolume, Globals.SOUND_SECONDARY_VOLUME, Globals.SOUND_PRIMARY_VOLUME, 0, Globals.SOUND_STREAM2_LOOP, 1);

            vibrator.vibrate(Globals.VOLUME_VIBRATION_MS);
            v.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
        }
    };
    OnClickListener handleFrequency1Clicked = new OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_1_ID;

            //frequency 1
            sp.play(soundIdFrequency, Globals.SOUND_PRIMARY_VOLUME, Globals.SOUND_SECONDARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);

            vibrator.vibrate(Globals.FREQUENCY_VIBRATION_MS);
            //v.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
        }
    };
    OnClickListener handleFrequency2Clicked = new OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_2_ID;

            //frequency 2
            sp.play(soundIdFrequency, Globals.SOUND_PRIMARY_VOLUME, Globals.SOUND_SECONDARY_VOLUME, 0, Globals.SOUND_STREAM2_LOOP, 1);

            vibrator.vibrate(Globals.FREQUENCY_VIBRATION_MS);
            //v.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
        }
    };
    OnClickListener handleBpmClicked = new OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_BEATS_PER_MINUTE_ID;
            //v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            vibrator.vibrate(Globals.BPM_VIBRATION_MS);

            //bpm sound
            sp.play(soundIdBpm, Globals.SOUND_PRIMARY_VOLUME, Globals.SOUND_PRIMARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);
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

        if (!lockRatio.isChecked()) {

            switch (Globals.CURRENT_HAPTIC_BUTTONS_WIRING) {
                case (Globals.INTERMITTENT_FREQUENCY_1_ID):
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        frequencyBar1.setProgress(frequencyBar1.getProgress() + 1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        frequencyBar1.setProgress(frequencyBar1.getProgress() - 1);
                        return true;
                    }
                case (Globals.INTERMITTENT_FREQUENCY_2_ID):
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        frequencyBar2.setProgress(frequencyBar2.getProgress() + 1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        frequencyBar2.setProgress(frequencyBar2.getProgress() - 1);
                        return true;
                    }
                case (Globals.INTERMITTENT_VOLUME_1_ID):
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        volumeBar1.setProgress(volumeBar1.getProgress() + 1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        volumeBar1.setProgress(volumeBar1.getProgress() - 1);
                        return true;
                    }
                case (Globals.INTERMITTENT_VOLUME_2_ID):
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        volumeBar2.setProgress(volumeBar2.getProgress() + 1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        volumeBar2.setProgress(volumeBar2.getProgress() - 1);
                        return true;
                    }
                case (Globals.INTERMITTENT_BEATS_PER_MINUTE_ID):
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        bpmBar.setProgress(bpmBar.getProgress() + 1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        bpmBar.setProgress(bpmBar.getProgress() - 1);
                        return true;
                    }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Configure the invisible save button to save the new values for the settings
     */
    private OnClickListener handleSaveClick = new OnClickListener() {
        @Override
        public void onClick(View buttonView) {

            Intent myIntent = new Intent();
            Globals.audioStream1.setFrequency(ai1.getFrequency());
            Globals.audioStream1.setVolume(ai1.getVolume());
            Globals.audioStream2.setFrequency(ai2.getFrequency());
            Globals.audioStream2.setVolume(ai2.getVolume());
            Globals.audioBPM = currentBpm;
            setResult(RESULT_OK, myIntent);
            finish();
        }
    };

    /**
     * Save the current settings when the user tries to go back to the previous activity/view
     */
    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent();
        Globals.audioStream1.setFrequency(ai1.getFrequency());
        Globals.audioStream1.setVolume(ai1.getVolume());
        Globals.audioStream2.setFrequency(ai2.getFrequency());
        Globals.audioStream2.setVolume(ai2.getVolume());
        Globals.audioBPM = currentBpm;
        setResult(RESULT_OK, myIntent);
        finish();
    }

    /**
     * EVENT LISTENERS: implement state changed listeners for the UI components
     */

    /**
     * This is a general handler used to process the progress changes for stream  1 and 2:
     * - frequency
     * - volume
     * - bpm
     * - also process the stream equality checkbox events
     */
    private CircularSeekBar.OnCircularSeekBarChangeListener handleProgressChanged = new CircleSeekBarListener() {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {


            if (seekBar.equals(frequencyBar1)) {

                TextView text = (TextView) findViewById(R.id.textViewIndS1F);
                text.setText(Html.fromHtml("Stream 1<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX) + " Hz</b>"));

                ai1.setFrequency(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar1.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()) {
                    ai2.setFrequency(ai1.getFrequency());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    frequencyBar2.setProgress(frequencyBar1.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_1_ID;
                }

            } else if (seekBar.equals(frequencyBar2)) {

                TextView text = (TextView) findViewById(R.id.textViewIndS2F);
                text.setText(Html.fromHtml("Stream 2<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX) + " Hz</b>"));

                ai2.setFrequency(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar2.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()) {
                    ai1.setFrequency(ai2.getFrequency());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    frequencyBar1.setProgress(frequencyBar2.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_2_ID;
                }

            } else if (seekBar.equals(volumeBar1)) {

                TextView text = (TextView) findViewById(R.id.textViewIndS1V);
                text.setText(Html.fromHtml("Stream 1<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MAX) + " dB</b>"));

                ai1.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar1.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()) {
                    ai2.setVolume(ai1.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    volumeBar2.setProgress(volumeBar1.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_1_ID;
                }

            } else if (seekBar.equals(volumeBar2)) {

                TextView text = (TextView) findViewById(R.id.textViewIndS2V);
                text.setText(Html.fromHtml("Stream 2<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MAX) + " dB</b>"));

                ai2.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar2.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()) {
                    ai1.setVolume(ai2.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    volumeBar1.setProgress(volumeBar2.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_2_ID;
                }

            } else if (seekBar.equals(bpmBar)){

                TextView text = (TextView) findViewById(R.id.textViewIndBPM);
                text.setText(Html.fromHtml("<b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_BPM_RANGE_MIN, Globals.UI_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.BRACELET_BPM_RANGE_MAX) + "</b>"));

                currentBpm = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(), Globals.UI_BPM_RANGE_MIN, Globals.UI_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.BRACELET_BPM_RANGE_MAX);
                ABBIGattReadWriteCharacteristics.writeIntermittentBPM(currentBpm);

            }
        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }
    };


}
