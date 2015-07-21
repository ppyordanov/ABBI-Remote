/*
 * Copyright (C) 2013 The Android Open Source Project
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

package proj.abbi.device;


import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import proj.abbi.CircleSeekBarListener;
import proj.abbi.CircularSeekBar;
import proj.abbi.R;
import proj.abbi.playback.ContinuousActivity;
import proj.abbi.playback.IntermittentActivity;
import proj.abbi.playback.PlaybackActivity;
import uk.ac.gla.abbi.abbi_library.AboutDialogue;
import uk.ac.gla.abbi.abbi_library.BluetoothLeService;
import uk.ac.gla.abbi.abbi_library.gatt_communication.ABBIGattReadWriteCharacteristics;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioContinuous;
import uk.ac.gla.abbi.abbi_library.gatt_communication.AudioIntermittent;
import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UUIDConstants;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String CONFIG_FILE = "ABBI2CfgFile";

    private TextView mAddressField;
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;

    private LinearLayout mLayoutInfo = null;
    private CircularSeekBar mVolumeBar = null;
    private Switch mSoundCtrl = null;
    private Switch mMuteSwitch = null;
    private RadioGroup mRadioSound = null;
    private Button mButtonSoundProperties = null;
    private ProgressBar progressBar = null;
    private TextView PBtextView = null;
    private TextView textViewVol = null;

    private int _countClick = 0;
    private boolean experimenterMode = false;

    //------------------------------------------------------------------------

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ABBIGattReadWriteCharacteristics.bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!ABBIGattReadWriteCharacteristics.bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            ABBIGattReadWriteCharacteristics.bluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ABBIGattReadWriteCharacteristics.bluetoothLeService = null;
            //finish();
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(ABBIGattReadWriteCharacteristics.bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                displayAbbiUiValues(intent.getStringExtra(BluetoothLeService.EXTRA_CHARACT),
                        intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    //------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mLayoutInfo = (LinearLayout) findViewById(R.id.linearLayoutInfo);
        // Sets up UI references.

        mAddressField = (TextView) findViewById(R.id.device_address);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mVolumeBar = (CircularSeekBar) findViewById(R.id.seekBarVol);
        mVolumeBar.setMax(Globals.UI_VOLUME_RANGE_MAX);
        textViewVol = (TextView) findViewById(R.id.textViewVol);
        mRadioSound = (RadioGroup) findViewById(R.id.radioGroup1);
        mButtonSoundProperties = (Button) findViewById(R.id.buttonSoundProperties);
        mSoundCtrl = (Switch) findViewById(R.id.soundOnOffSwitch);
        mMuteSwitch = (Switch) findViewById(R.id.muteSwitch);


        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_bar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(25);   // Main Progress
        progressBar.setSecondaryProgress(50); // Secondary Progress
        progressBar.setMax(100); // Maximum Progress
        progressBar.setProgressDrawable(drawable);
        PBtextView = (TextView) findViewById(R.id.PBtextView);

        // set text field data
        getActionBar().setTitle("ABBI Remote: " + mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        updateDeviceAddress(mDeviceAddress);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        wireUpLocalHandlers();
        if (ABBIGattReadWriteCharacteristics.bluetoothLeService != null) {
            final boolean result = ABBIGattReadWriteCharacteristics.bluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(CONFIG_FILE, 0);
        experimenterMode = settings.getBoolean("experimenterMode", false);
        changeAppModel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        ABBIGattReadWriteCharacteristics.bluetoothLeService = null;
    }

    @Override
    public void onBackPressed()
    {
        if (experimenterMode || !mConnected) {
            super.onBackPressed ();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                ABBIGattReadWriteCharacteristics.bluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                ABBIGattReadWriteCharacteristics.bluetoothLeService.disconnect();
                return true;
            case R.id.menu_info:
                AboutDialogue.show(DeviceControlActivity.this, getString(R.string.about),
                        getString(R.string.close));
                return true;
            case android.R.id.home:
                //onBackPressed();
                _countClick++;
                if(_countClick>3)
                {
                    toggleExperimenterMode();
                    changeAppModel ();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //------------------------------------------------------------------------

    private void toggleExperimenterMode(){
        _countClick=0;
        experimenterMode = !experimenterMode;
        SharedPreferences settings = getSharedPreferences(CONFIG_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("experimenterMode", experimenterMode);
        editor.commit();
        String t = experimenterMode ? "You are now in Experimenter mode":"You are now in User mode";
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }

    private void displayAbbiUiValues(String charact, byte[] data) {
        if (charact == null)
            return;

        if (data == null)
            return;

        int dataint = byteArrayToInt(data);

        switch (charact) {
            case UUIDConstants.BatteryLevel_UUID:
                Globals.batteryLevel = dataint;
                updateBatteryLevel(dataint);
                break;
            case UUIDConstants.VolumeLevel_UUID:
                Globals.volumeLevel = dataint;
                updateMainVolumeLevel(dataint);
                break;
            case UUIDConstants.SoundCtrl_UUID:
                Globals.soundControlState = dataint;
                boolean soundOn  = Globals.soundControlState == Globals.SOUND_STATE_ON_ID;
                boolean soundOff = Globals.soundControlState == Globals.SOUND_STATE_OFF_ID;
                updateMuteSwitch(soundOff);
                updateSoundCtrlButton(soundOn);
                break;
            case UUIDConstants.AudioMode_UUID:
                Globals.audioMode = dataint;
                updateAudioMode(dataint);
                break;
            case UUIDConstants.AudioContinuous_UUID:
                Globals.audioContinuous = new AudioContinuous(data);
                break;
            case UUIDConstants.AudioStream1_UUID:
                Globals.audioStream1 = new AudioIntermittent(data);
                break;
            case UUIDConstants.AudioStream2_UUID:
                Globals.audioStream2 = new AudioIntermittent(data);
                break;
            case UUIDConstants.AudioBPM_UUID:
                Globals.audioBPM = dataint;
                break;
            case UUIDConstants.AudioPlayback_UUID:
                Globals.audioPlayback = dataint;
                break;
            case UUIDConstants.Accelerometer_UUID:

                displayData(String.valueOf(dataint));
                //Todo: Do something with accelerometer data
                // most probably the thing to do is something like:
                //recognizeMovement(dataint);
                break;
            case UUIDConstants.Gyroscope_UUID:
                displayData(String.valueOf(dataint));
                //Todo: Do something with gyroscope data
                break;
            case UUIDConstants.Magnetometer_UUID:
                displayData(String.valueOf(dataint));
                //Todo: do something with magnetometer data
                break;
            case UUIDConstants.IMU_UUID:
                // for some reason this does not trigger: it seems there is no IMU data notification
                break;
        }
    }

    private void changeAppModel ()
    {
        if (!experimenterMode) {
            mSoundCtrl.setVisibility(View.GONE);
            mMuteSwitch.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.GONE);
            //this.Window.ClearFlags (WindowManagerFlags.KeepScreenOn);
        }
        else {
            mMuteSwitch.setVisibility(View.GONE);
            mSoundCtrl.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.VISIBLE); // Set to View.VISIBLE in order to see the device address, the status and the data (from displayData())
            //this.Window.AddFlags (WindowManagerFlags.KeepScreenOn);
        }
    }

    /* Todo: develop a movement recognizer class (based on the android code from Charlotte)
       Todo: develop the recognizeMovementMethod (below) that calls that class and selects sound to play
     */
    /*private void recognizeMovement() {
        int[] mybuffer; // declared somewhere above
        boolean recognized; // declared somewhere above
        int soundFile;
        mybuffer.add(dataint);
        if (mybuffer.length > 5) {
            int gesture = MyGestureRecognitionClass.recognize(mybuffer);
            if (gesture != 0) {
                recognized = true;
                switch (gesture)
                case 1:
                    soundFile = blabla;
                    break;
                case 2:
                    soundFile = blabla2;
                    break;
                etc...

            }
            if (recognized) {
                mybuffer = new int[]{}; // clear the buffer
                ABBIGattReadWriteCharacteristics.writeWavFileId(soundFile); // change the sound file
                ABBIGattReadWriteCharacteristics.writeAudioMode(ABBISoundMode.Playback);
            }
        }
    }*/

    //------------------------------------------------------------------------

    private void updateDeviceAddress(final String deviceAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddressField.setText(deviceAddress);
            }
        });
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(final String data) {
        if (data != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDataField.setText(data);
                }
            });
        }
    }

    private void updateBatteryLevel(final int battLevel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLevelIndicator(battLevel);
            }
        });
    }

    private void updateMainVolumeLevel(final int volLevel){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVolumeBar.setProgress(volLevel);
            }
        });
    }

    private void updateMuteSwitch(final boolean mute){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMuteSwitch.setChecked(mute);
            }
        });
    }

    private void updateSoundCtrlButton(final boolean soundOn){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSoundCtrl.setChecked(soundOn);
            }
        });
    }

    private void updateAudioMode(final int audioMode){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (audioMode) {
                    case Globals.CONTINUOUS_SOUND_MODE_ID:
                        mRadioSound.check(R.id.radioSoundContinuous);
                        break;
                    case Globals.INTERMITTENT_SOUND_MODE_ID:
                        mRadioSound.check(R.id.radioSoundIntermmitent);
                        break;
                    case Globals.PLAYBACK_SOUND_MODE_ID:
                        mRadioSound.check(R.id.radioSoundPlayback);
                        break;
                }
            }
        });
    }

    //------------------------------------------------------------------------

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            if (uuid.equals(UUIDConstants.Battery_Service_UUID)){
                ABBIGattReadWriteCharacteristics.batteryService = gattService;
                ABBIGattReadWriteCharacteristics.readBatteryLevel();
            }
            else if (uuid.equals(UUIDConstants.Custom_Service_UUID)){
                ABBIGattReadWriteCharacteristics.customService = gattService;
                ABBIGattReadWriteCharacteristics.readMainVolumeLevel();
                ABBIGattReadWriteCharacteristics.readSoundCtrlMode();
                ABBIGattReadWriteCharacteristics.readAudioMode();
                ABBIGattReadWriteCharacteristics.readContinuousStream();
                ABBIGattReadWriteCharacteristics.readIntermittentStream(1);
                ABBIGattReadWriteCharacteristics.readIntermittentStream(2);
                ABBIGattReadWriteCharacteristics.readIntermittentBPM();
                ABBIGattReadWriteCharacteristics.readWavFileId();
            }
            else if (uuid.equals(UUIDConstants.Motion_Service_UUID)) {
                ABBIGattReadWriteCharacteristics.motionService = gattService;
                ABBIGattReadWriteCharacteristics.readAccelerometer();
                ABBIGattReadWriteCharacteristics.readGyroscope();
                ABBIGattReadWriteCharacteristics.readMagnetometer();
                ABBIGattReadWriteCharacteristics.readIMU();
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //------------------------------------------------------------------------

    /// <summary>
    /// Wires up local handlers delayed until situation is stable.
    /// </summary>
    protected void wireUpLocalHandlers ()
    {
        mVolumeBar.setOnSeekBarChangeListener(handleVolumeChanged);
        mSoundCtrl.setOnCheckedChangeListener(handleSoundCtrlChanged);
        mMuteSwitch.setOnCheckedChangeListener(handleMuteChanged);
        mRadioSound.setOnCheckedChangeListener(handleSoundModeChanged);
        mButtonSoundProperties.setOnClickListener(handleSoundPropertiesClick);
    }

    private CircularSeekBar.OnCircularSeekBarChangeListener handleVolumeChanged = new CircleSeekBarListener()
    {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            if(progress>15){
                progress = UtilityFunctions.changeRangeMaintainingRatio(progress, Globals.BRACELET_VOLUME_RANGE_MIN, Globals.BRACELET_VOLUME_RANGE_MAX, Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX);
            }
            textViewVol.setText(Html.fromHtml("<b>Volume<br>" + (progress*100)/Globals.UI_VOLUME_RANGE_MAX + " %</b>"));
        }
        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            ABBIGattReadWriteCharacteristics.writeMainVolumeLevel(seekBar.getProgress());
        }
    };

    private Switch.OnCheckedChangeListener handleSoundCtrlChanged = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_ON_ID);
            else
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_OFF_ID);

        }
    };

    private Switch.OnCheckedChangeListener handleMuteChanged = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_OFF_ID);
            else
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_TRIGGER_ID);
        }
    };

    private RadioGroup.OnCheckedChangeListener handleSoundModeChanged = new RadioGroup.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            ABBIGattReadWriteCharacteristics.writeAudioMode(checkedId);
        }
    };

    private Button.OnClickListener handleSoundPropertiesClick = new Button.OnClickListener(){
        @Override
        public void onClick(View buttonView){
            Intent pbActIntent = null;
            int radioChecked = mRadioSound.getCheckedRadioButtonId();
            if (radioChecked == R.id.radioSoundContinuous) {
                pbActIntent = new Intent(buttonView.getContext(), ContinuousActivity.class);
            }
            else if (radioChecked == R.id.radioSoundIntermmitent) {
                pbActIntent = new Intent (buttonView.getContext(), IntermittentActivity.class);
            }
            else if (radioChecked == R.id.radioSoundPlayback) {
                pbActIntent = new Intent (buttonView.getContext(), PlaybackActivity.class);
            }
            startActivity(pbActIntent);
        }
    };

    //------------------------------------------------------------------------

    protected void updateLevelIndicator(int n)
    {

        progressBar.setProgress(n);
        PBtextView.setText(Html.fromHtml("<b>Battery<br>" + n + " %</b>"));

    }


    public int byteArrayToInt(byte[] data){
        int l = data.length;
        ByteBuffer bb = ByteBuffer.wrap(data);
        int res = 0;
        switch (l){
            case 1:
                res = bb.get() & 0xff;
                break;
            case 2:
                res = bb.order(ByteOrder.LITTLE_ENDIAN).getShort();
                break;
            case 4:
                res = bb.order(ByteOrder.LITTLE_ENDIAN).getInt();
                break;
        }
        return res;
    }



}
