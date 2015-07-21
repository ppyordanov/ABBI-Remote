package proj.abbi.playback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
        bpmBar.setMax(Globals.NEW_BPM_RANGE_MAX);

        lockRatio =(CheckBox)findViewById(R.id.lockRatioCheckBox);

        frequencyBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        frequencyBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        volumeBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        bpmBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(currentBpm, Globals.BRACELET_BPM_RANGE_MIN, Globals.OLD_BPM_RANGE_MAX, Globals.NEW_BPM_RANGE_MIN, Globals.NEW_BPM_RANGE_MAX));
        saveButton.setVisibility(View.GONE);

        frequencyBar1.setOnSeekBarChangeListener(handleProgressChanged);
        volumeBar1.setOnSeekBarChangeListener(handleProgressChanged);
        frequencyBar2.setOnSeekBarChangeListener(handleProgressChanged);
        volumeBar2.setOnSeekBarChangeListener(handleProgressChanged);
        bpmBar.setOnSeekBarChangeListener(handleProgressChanged);
        saveButton.setOnClickListener(handleSaveClick);
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

    private Button.OnClickListener handleSaveClick = new Button.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            Intent pbActIntent = null;
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
        Intent pbActIntent = null;
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
                text.setText(Html.fromHtml("Stream 1<br><b>" + (progress * 100) / Globals.UI_FREQUENCY_RANGE_MAX + " %</b>"));
            }
            else if (seekBar.equals(frequencyBar2)){
                TextView text = (TextView)findViewById(R.id.textViewIndS2F);
                text.setText(Html.fromHtml("Stream 2<br><b>" + (progress * 100) / Globals.UI_FREQUENCY_RANGE_MAX + " %</b>"));

            }
            else if (seekBar.equals(volumeBar1)) {
                TextView text = (TextView)findViewById(R.id.textViewIndS1V);
                text.setText(Html.fromHtml("Stream 1<br><b>" + (progress*100)/Globals.UI_VOLUME_RANGE_MAX + " %</b>"));
            }
            else if (seekBar.equals(volumeBar2)){
                TextView text = (TextView)findViewById(R.id.textViewIndS2V);
                text.setText(Html.fromHtml("Stream 2<br><b>" + (progress*100)/Globals.UI_VOLUME_RANGE_MAX + " %</b>"));
            }
            else  {
                TextView text = (TextView)findViewById(R.id.textViewIndBPM);
                text.setText(Html.fromHtml("<b>" + (progress*100)/Globals.NEW_BPM_RANGE_MAX + " %</b>"));
            }
        }
        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            if (seekBar.equals(frequencyBar1)) {
                ai1.setFreq(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar1.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()){
                    ai2.setFreq(ai1.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    frequencyBar2.setProgress(frequencyBar1.getProgress());
                }
            }
            else if (seekBar.equals(frequencyBar2)){
                ai2.setFreq(UtilityFunctions.changeRangeMaintainingRatio(frequencyBar2.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()){
                    ai1.setFreq(ai2.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    frequencyBar1.setProgress(frequencyBar2.getProgress());
                }

            }
            else if (seekBar.equals(volumeBar1)) {
                ai1.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar1.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (lockRatio.isChecked()){
                    ai2.setVolume(ai1.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    volumeBar2.setProgress(volumeBar1.getProgress());
                }

            }
            else if (seekBar.equals(volumeBar2)){
                ai2.setVolume(UtilityFunctions.changeRangeMaintainingRatio(volumeBar2.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (lockRatio.isChecked()){
                    ai1.setVolume(ai2.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    volumeBar1.setProgress(volumeBar2.getProgress());
                }

            }
            else{
                currentBpm = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(),  Globals.NEW_BPM_RANGE_MIN, Globals.NEW_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.OLD_BPM_RANGE_MAX);
                ABBIGattReadWriteCharacteristics.writeIntermittentBPM(currentBpm);
            }
        }
    };




}
