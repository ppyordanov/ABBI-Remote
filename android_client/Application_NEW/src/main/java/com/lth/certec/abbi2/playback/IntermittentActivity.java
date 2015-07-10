package com.lth.certec.abbi2.playback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.CheckBox;

import com.lth.certec.abbi2.R;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBIGattReadWriteCharacteristics;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.AudioIntermittent;
import uk.ac.gla.peteryordanov.abbi_library.utilities.Globals;
import uk.ac.gla.peteryordanov.abbi_library.utilities.UtilityFunctions;


public class IntermittentActivity extends Activity {

    private SeekBar _freqBar1;
    private SeekBar _volBar1;
    private SeekBar _freqBar2;
    private SeekBar _volBar2;
    private SeekBar _bpmBar;


    private CheckBox _lockRatio;

    private AudioIntermittent ai1;
    private AudioIntermittent ai2;
    private int _currentBpm;

    private Button _saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermittent);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        ai1 = new AudioIntermittent(Globals.audioStream1.getByteArray());
        ai2 = new AudioIntermittent(Globals.audioStream2.getByteArray());
        _currentBpm = Globals.audioBPM;

        _freqBar1 = (SeekBar) findViewById(R.id.seekBarStream1Freq);
        _volBar1 = (SeekBar) findViewById(R.id.seekBarStream1Vol);
        _freqBar2 = (SeekBar) findViewById(R.id.seekBarStream2Freq);
        _volBar2 = (SeekBar) findViewById(R.id.seekBarStream2Vol);
        _bpmBar = (SeekBar) findViewById(R.id.seekBarBpm);
        _saveButton = (Button) findViewById(R.id.buttonIntermSave);

        _lockRatio =(CheckBox)findViewById(R.id.lockRatioCheckBox);

        _freqBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        _volBar1.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai1.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        _freqBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getFreq(), Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX, Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX));
        _volBar2.setProgress(UtilityFunctions.changeRangeMaintainingRatio(ai2.getVolume(), Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX));
        _bpmBar.setProgress(UtilityFunctions.changeRangeMaintainingRatio(_currentBpm, Globals.BRACELET_BPM_RANGE_MIN, Globals.OLD_BPM_RANGE_MAX, Globals.NEW_BPM_RANGE_MIN, Globals.NEW_BPM_RANGE_MAX));
        _saveButton.setVisibility(View.GONE);

        _freqBar1.setOnSeekBarChangeListener(handleProgressChanged);
        _volBar1.setOnSeekBarChangeListener(handleProgressChanged);
        _freqBar2.setOnSeekBarChangeListener(handleProgressChanged);
        _volBar2.setOnSeekBarChangeListener(handleProgressChanged);
        _bpmBar.setOnSeekBarChangeListener(handleProgressChanged);
        _saveButton.setOnClickListener(handleSaveClick);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_intermittent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            Globals.audioBPM = _currentBpm;
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
        Globals.audioBPM = _currentBpm;
        setResult(RESULT_OK, myIntent);
        finish();
    }

    private SeekBar.OnSeekBarChangeListener handleProgressChanged = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.equals(_freqBar1)) {
                ai1.setFreq(UtilityFunctions.changeRangeMaintainingRatio(_freqBar1.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (_lockRatio.isChecked()){
                    ai2.setFreq(ai1.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    _freqBar2.setProgress(_freqBar1.getProgress());
                }
            }
            else if (seekBar.equals(_freqBar2)){
                ai2.setFreq(UtilityFunctions.changeRangeMaintainingRatio(_freqBar2.getProgress(), Globals.UI_FREQUENCY_RANGE_MIN, Globals.UI_FREQUENCY_RANGE_MAX, Globals.BRACELET_HZ_FREQUENCY_RANGE_MIN, Globals.BRACELET_HZ_FREQUENCY_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (_lockRatio.isChecked()){
                    ai1.setFreq(ai2.getFreq());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    _freqBar1.setProgress(_freqBar2.getProgress());
                }

            }
            else if (seekBar.equals(_volBar1)) {
                ai1.setVolume(UtilityFunctions.changeRangeMaintainingRatio(_volBar1.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                if (_lockRatio.isChecked()){
                    ai2.setVolume(ai1.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                    _volBar2.setProgress(_volBar1.getProgress());
                }

            }
            else if (seekBar.equals(_volBar2)){
                ai2.setVolume(UtilityFunctions.changeRangeMaintainingRatio(_volBar2.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX));
                ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM2_ID, ai2);
                if (_lockRatio.isChecked()){
                    ai1.setVolume(ai2.getVolume());
                    ABBIGattReadWriteCharacteristics.writeIntermittentStream(Globals.ABBI_INTERMITTENT_STREAM1_ID, ai1);
                    _volBar1.setProgress(_volBar2.getProgress());
                }

            }
            else if (seekBar.equals(_bpmBar)) {
                _currentBpm = UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(),  Globals.NEW_BPM_RANGE_MIN, Globals.NEW_BPM_RANGE_MAX, Globals.BRACELET_BPM_RANGE_MIN, Globals.OLD_BPM_RANGE_MAX);
                ABBIGattReadWriteCharacteristics.writeIntermittentBPM(_currentBpm);
            }
        }
    };




}
