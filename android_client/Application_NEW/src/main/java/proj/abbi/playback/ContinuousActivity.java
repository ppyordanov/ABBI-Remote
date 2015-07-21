package proj.abbi.playback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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

    private CircularSeekBar.OnCircularSeekBarChangeListener handleFreqChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView)findViewById(R.id.textViewInd1);

            text.setText(Html.fromHtml("<b>Frequency<br>" + (progress * 100) / Globals.UI_FREQUENCY_RANGE_MAX + " %</b>"));
        }
        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            currentFreq = (seekBar.getProgress() + 5) * 100;
            ABBIGattReadWriteCharacteristics.writeContinuousStream(currentFreq, currentVol);
        }
    };

    private CircularSeekBar.OnCircularSeekBarChangeListener handleVolChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            TextView text = (TextView)findViewById(R.id.textViewInd2);

            text.setText(Html.fromHtml("<b>Volume<br>" + (progress*100)/Globals.UI_VOLUME_RANGE_MAX + " %</b>"));
        }
        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            currentVol = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX);
            ABBIGattReadWriteCharacteristics.writeContinuousStream(currentFreq, currentVol);
        }
    };
}
