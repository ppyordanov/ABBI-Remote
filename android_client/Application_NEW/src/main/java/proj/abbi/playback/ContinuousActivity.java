package proj.abbi.playback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
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

public class ContinuousActivity extends Activity {

    private int currentFreq;
    private int currentVol;
    private CircularSeekBar frequencyBar;
    private CircularSeekBar volumeBar;
    private Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous);

        currentFreq = AudioContinuous.getFreq();
        currentVol = AudioContinuous.getVolume();

        frequencyBar = (CircularSeekBar) findViewById(R.id.seekBarContFreq);
        volumeBar = (CircularSeekBar) findViewById(R.id.seekBarContVol);

        frequencyBar.setMax(Globals.UI_FREQUENCY_RANGE_MAX);
        volumeBar.setMax(Globals.UI_VOLUME_RANGE_MAX);



        saveButton = (Button) findViewById(R.id.buttonContSave);

        frequencyBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentFreq, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentVol, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        saveButton.setVisibility(View.GONE);

        frequencyBar.setOnSeekBarChangeListener(handleFreqChanged);
        volumeBar.setOnSeekBarChangeListener(handleVolChanged);
        saveButton.setOnClickListener(handleSaveClick);

        //accessibility
        findViewById(R.id.frequencyLayout).setOnClickListener(handleFrequencyClicked);
        findViewById(R.id.volumeLayout).setOnClickListener(handleVolumeClicked);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_continuous, menu);
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
            AboutDialogue.show(ContinuousActivity.this, getString(R.string.about),
                    getString(R.string.close));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------

    private Button.OnClickListener handleSaveClick = new Button.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            Intent pbActIntent = null;
            Intent myIntent = new Intent();
            AudioContinuous.setFreq(currentFreq);
            AudioContinuous.setVolume(currentVol);
            setResult(RESULT_OK, myIntent);
            finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch(Globals.CURRENT_HAPTIC_BUTTONS_WIRING) {
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

    View.OnClickListener handleVolumeClicked = new View.OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.CONTINUOUS_VOLUME_ID;
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
        }
    };

    View.OnClickListener handleFrequencyClicked = new View.OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.CONTINUOUS_FREQUENCY_ID;
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
        }
    };

    private CircularSeekBar.OnCircularSeekBarChangeListener handleFreqChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView)findViewById(R.id.textViewInd1);

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

    private CircularSeekBar.OnCircularSeekBarChangeListener handleVolChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView)findViewById(R.id.textViewInd2);

            text.setText(Html.fromHtml("<b>Volume<br>" + UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_SOURCE_RANGE_MAX) + " dB</b>"));

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
