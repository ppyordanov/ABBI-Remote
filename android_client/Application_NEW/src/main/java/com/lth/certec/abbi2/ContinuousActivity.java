package com.lth.certec.abbi2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import uk.ac.gla.peteryordanov.abbi_library.*;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBIGattAttributes;

public class ContinuousActivity extends Activity {

    private int _currentFreq;
    private int _currentVol;
    private SeekBar _freqBar;
    private SeekBar _volBar;
    private Button _saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous);

        _currentFreq = AudioContinuous.getFreq();
        _currentVol = AudioContinuous.getVolume();

        _freqBar = (SeekBar) findViewById(R.id.seekBarContFreq);
        _volBar = (SeekBar) findViewById(R.id.seekBarContVol);
        _saveButton = (Button) findViewById(R.id.buttonContSave);

        _freqBar.setProgress((_currentFreq / 100) - 5); //(500 - 4000) -> (0 - 35)
        _volBar.setProgress(_currentVol * 15 / 65534);  //(0 - 65534) -> (0 - 15)
        _saveButton.setVisibility(View.GONE);

        _freqBar.setOnSeekBarChangeListener(handleFreqChanged);
        _volBar.setOnSeekBarChangeListener(handleVolChanged);
        _saveButton.setOnClickListener(handleSaveClick);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_continuous, menu);
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
            AudioContinuous.setFreq(_currentFreq);
            AudioContinuous.setVolume(_currentVol);
            setResult(RESULT_OK, myIntent);
            finish();
        }
    };

    private SeekBar.OnSeekBarChangeListener handleFreqChanged = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            _currentFreq = (seekBar.getProgress() + 5) * 100;
            ABBIGattAttributes.writeContinuousStream(_currentFreq, _currentVol);
        }
    };

    private SeekBar.OnSeekBarChangeListener handleVolChanged = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            _currentVol = seekBar.getProgress() * 65534 / 15;
            ABBIGattAttributes.writeContinuousStream(_currentFreq, _currentVol);
        }
    };
}
