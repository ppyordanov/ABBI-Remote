package proj.abbi.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.HapticFeedbackConstants;
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

        lockRatio =(CheckBox)findViewById(R.id.lockRatioCheckBox);

        frequencyBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        frequencyBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
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
        soundIdVolume = sp.load(this, R.raw.g_major,1);
        soundIdFrequency = sp.load(this,R.raw.g_minor,1);
        soundIdBpm = sp.load(this, R.raw.c,1);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intermittent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_info) {
            AboutDialogue.show(IntermittentActivity.this, getString(R.string.about),
                    getString(R.string.close));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(!lockRatio.isChecked()) {

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

    private OnClickListener handleSaveClick = new OnClickListener() {
        @Override
        public void onClick(View buttonView) {

            Intent myIntent = new Intent();
            Globals.audioStream1.setFreq(ai1.getFreq());
            Globals.audioStream1.setVolume(ai1.getVolume());
            Globals.audioStream2.setFreq(ai2.getFreq());
            Globals.audioStream2.setVolume(ai2.getVolume());
            Globals.audioBPM = currentBpm;
            setResult(RESULT_OK, myIntent);
            finish();
        }
    };

    //doesn't look like the settings are saved in the app (the save button is invisible)
    @Override
    public void onBackPressed(){
        Intent myIntent = new Intent();
        Globals.audioStream1.setFreq(ai1.getFreq());
        Globals.audioStream1.setVolume(ai1.getVolume());
        Globals.audioStream2.setFreq(ai2.getFreq());
        Globals.audioStream2.setVolume(ai2.getVolume());
        Globals.audioBPM = currentBpm;
        setResult(RESULT_OK, myIntent);
        finish();
    }

    private CircularSeekBar.OnCircularSeekBarChangeListener handleProgressChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {


            if (seekBar.equals(frequencyBar1)) {

                TextView text = (TextView)findViewById(R.id.textViewIndS1F);
                text.setText(Html.fromHtml("Stream 1<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX) + " Hz</b>"));

                ai1.setFreq(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar1.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()){
                    ai2.setFreq(ai1.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    frequencyBar2.setProgress(frequencyBar1.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_1_ID;
                }

            }
            else if (seekBar.equals(frequencyBar2)){

                TextView text = (TextView)findViewById(R.id.textViewIndS2F);
                text.setText(Html.fromHtml("Stream 2<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX) + " Hz</b>"));

                ai2.setFreq(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar2.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()){
                    ai1.setFreq(ai2.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    frequencyBar1.setProgress(frequencyBar2.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_FREQUENCY_2_ID;
                }

            }
            else if (seekBar.equals(volumeBar1)) {

                TextView text = (TextView)findViewById(R.id.textViewIndS1V);
                text.setText(Html.fromHtml("Stream 1<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MAX) + " dB</b>"));

                ai1.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar1.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()){
                    ai2.setVolume(ai1.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    volumeBar2.setProgress(volumeBar1.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_1_ID;
                }

            }
            else if (seekBar.equals(volumeBar2)){

                TextView text = (TextView)findViewById(R.id.textViewIndS2V);
                text.setText(Html.fromHtml("Stream 2<br><b>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MAX) + " dB</b>"));

                ai2.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar2.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()){
                    ai1.setVolume(ai2.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    volumeBar1.setProgress(volumeBar2.getProgress());
                    Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.INTERMITTENT_VOLUME_2_ID;
                }

            }
            else  {

                TextView text = (TextView)findViewById(R.id.textViewIndBPM);
                text.setText(Html.fromHtml("<b>" + UtilityFunctions.changeRangeMaintainingRatio(progress,  Globals.UI_BPM_RANGE_MIN, Globals.UI_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.BRACELET_BPM_RANGE_MAX)+ "</b>"));

                currentBpm = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(),  Globals.UI_BPM_RANGE_MIN, Globals.UI_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.BRACELET_BPM_RANGE_MAX);
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
