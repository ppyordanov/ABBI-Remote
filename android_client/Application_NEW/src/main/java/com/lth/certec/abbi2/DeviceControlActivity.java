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

package com.lth.certec.abbi2;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBIGattAttributes;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBISoundCtrl;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.ABBISoundMode;
import uk.ac.gla.peteryordanov.abbi_library.AudioContinuous;
import uk.ac.gla.peteryordanov.abbi_library.AudioIntermittent;
import uk.ac.gla.peteryordanov.abbi_library.BluetoothLeService;
import uk.ac.gla.peteryordanov.abbi_library.utility.Globals;
import uk.ac.gla.peteryordanov.abbi_library.utility.UUIDConstants;

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
    private LinearLayout mMuteLayout = null;
    private SeekBar mVolumeBar = null;
    private ToggleButton mSoundCtrl = null;
    private Switch mMuteSwitch = null;
    private ImageView mIconBattery = null;
    private ImageView mAnimBattery = null;
    private RadioGroup mRadioSound = null;
    private Button mButtonSoundProperties = null;

    private int _countClick = 0;
    private boolean experimenterMode = false;

    //------------------------------------------------------------------------

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ABBIGattAttributes.bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!ABBIGattAttributes.bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            ABBIGattAttributes.bluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ABBIGattAttributes.bluetoothLeService = null;
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
                displayGattServices(ABBIGattAttributes.bluetoothLeService.getSupportedGattServices());
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

        // Sets up UI references.
        mLayoutInfo = (LinearLayout) findViewById(R.id.linearLayoutInfo);
        mMuteLayout = (LinearLayout) findViewById(R.id.linearLayoutMute);
        mAddressField = (TextView) findViewById(R.id.device_address);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mIconBattery = (ImageView) findViewById(R.id.iconBattery);
        mAnimBattery = (ImageView) findViewById(R.id.animBattery);
        mMuteSwitch = (Switch) findViewById(R.id.switch1);
        mVolumeBar = (SeekBar) findViewById(R.id.seekBar1);
        mRadioSound = (RadioGroup) findViewById(R.id.radioGroup1);
        mButtonSoundProperties = (Button) findViewById(R.id.buttonSoundProperties);
        mSoundCtrl = (ToggleButton) findViewById(R.id.toggleButton1);

        // set text field data
        getActionBar().setTitle(mDeviceName);
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
        if (ABBIGattAttributes.bluetoothLeService != null) {
            final boolean result = ABBIGattAttributes.bluetoothLeService.connect(mDeviceAddress);
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
        ABBIGattAttributes.bluetoothLeService = null;
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
                ABBIGattAttributes.bluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                ABBIGattAttributes.bluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                //onBackPressed();
                _countClick++;
                if(_countClick>8)
                {
                    toggleExperimenterMode();
                    changeAppModel ();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == R.id.radioSoundContinuous) {
                writeContinuousStream (ABBIGattAttributes.audioContinuous);
            } else if (requestCode == R.id.radioSoundIntermmitent) {
                writeIntermittentStream(1, ABBIGattAttributes.audioStream1);
                writeIntermittentStream(2, ABBIGattAttributes.audioStream2);
                writeIntermittentBPM(ABBIGattAttributes.audioBPM);
            } else if (requestCode == R.id.radioSoundPlayback) {
                writeWavFileId(ABBIGattAttributes.audioPlayback);
            }

        }
    }*/

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
                Globals.soundCtrl = dataint;
                boolean soundOn  = Globals.soundCtrl == ABBISoundCtrl.SoundOn;
                boolean soundOff = Globals.soundCtrl == ABBISoundCtrl.SoundOff;
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
                //Todo: Do something with gyroscope data
                break;
            case UUIDConstants.Magnetometer_UUID:
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
            mMuteLayout.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.GONE);
            //this.Window.ClearFlags (WindowManagerFlags.KeepScreenOn);
        }
        else {
            mMuteLayout.setVisibility(View.GONE);
            mSoundCtrl.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.GONE); // Set to View.VISIBLE in order to see the device address, the status and the data (from displayData())
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
                ABBIGattAttributes.writeWavFileId(soundFile); // change the sound file
                ABBIGattAttributes.writeAudioMode(ABBISoundMode.Playback);
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
                    case ABBISoundMode.Continuous:
                        mRadioSound.check(R.id.radioSoundContinuous);
                        break;
                    case ABBISoundMode.Intermittent:
                        mRadioSound.check(R.id.radioSoundIntermmitent);
                        break;
                    case ABBISoundMode.Playback:
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
                ABBIGattAttributes.batteryService = gattService;
                ABBIGattAttributes.readBatteryLevel();
            }
            else if (uuid.equals(UUIDConstants.Custom_Service_UUID)){
                ABBIGattAttributes.customService = gattService;
                ABBIGattAttributes.readMainVolumeLevel();
                ABBIGattAttributes.readSoundCtrlMode();
                ABBIGattAttributes.readAudioMode();
                ABBIGattAttributes.readContinuousStream();
                ABBIGattAttributes.readIntermittentStream(1);
                ABBIGattAttributes.readIntermittentStream(2);
                ABBIGattAttributes.readIntermittentBPM();
                ABBIGattAttributes.readWavFileId();
            }
            else if (uuid.equals(UUIDConstants.Motion_Service_UUID)) {
                ABBIGattAttributes.motionService = gattService;
                ABBIGattAttributes.readAccelerometer();
                ABBIGattAttributes.readGyroscope();
                ABBIGattAttributes.readMagnetometer();
                ABBIGattAttributes.readIMU();
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

    private SeekBar.OnSeekBarChangeListener handleVolumeChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ABBIGattAttributes.writeMainVolumeLevel(seekBar.getProgress());
        }
    };

    private Switch.OnCheckedChangeListener handleSoundCtrlChanged = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattAttributes.writeSoundCtrlMode(ABBISoundCtrl.SoundOn);
            else
                ABBIGattAttributes.writeSoundCtrlMode(ABBISoundCtrl.SoundOff);

        }
    };

    private Switch.OnCheckedChangeListener handleMuteChanged = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattAttributes.writeSoundCtrlMode(ABBISoundCtrl.SoundOff);
            else
                ABBIGattAttributes.writeSoundCtrlMode(ABBISoundCtrl.SoundTrigger);
        }
    };

    private RadioGroup.OnCheckedChangeListener handleSoundModeChanged = new RadioGroup.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            ABBIGattAttributes.writeAudioMode(checkedId);
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
        View iconball1 = findViewById (R.id.view1);
        View iconball2 = findViewById (R.id.view2);
        View iconball3 = findViewById (R.id.view3);
        View iconball4 = findViewById (R.id.view4);
        // switch or compare
        if (n < 25 && n > 0)
        {
            // only red ball
            iconball1.setAlpha(1.0f);
            iconball2.setAlpha(0.0f);
            iconball3.setAlpha(0.0f);
            iconball4.setAlpha(0.0f);
        }
        else
        if (n >= 25 && n < 50)
        {
            //  red +yellow balls
            iconball1.setAlpha(1.0f);
            iconball2.setAlpha(1.0f);
            iconball3.setAlpha(0.0f);
            iconball4.setAlpha(0.0f);
        }
        else
        if (n >= 50 && n < 75)
        {
            //  red +yellow+green balls
            iconball1.setAlpha(1.0f);
            iconball2.setAlpha(1.0f);
            iconball3.setAlpha(1.0f);
            iconball4.setAlpha(0.0f);
        }
        else
        if (n >= 75 && n < 100)
        {
            //  red +yellow+greenx2 balls
            iconball1.setAlpha(1.0f);
            iconball2.setAlpha(1.0f);
            iconball3.setAlpha(1.0f);
            iconball4.setAlpha(1.0f);
        }
        else
        {
            // all dark
            iconball1.setAlpha(0.0f);
            iconball2.setAlpha(0.0f);
            iconball3.setAlpha(0.0f);
            iconball4.setAlpha(0.0f);
        }

        //if charging
        if (n == 100)
        {
            if (mIconBattery != null)
                mIconBattery.setVisibility(View.GONE);
            if (mAnimBattery != null)
                mAnimBattery.setVisibility(View.VISIBLE);
        }
        else
        {
            if (mIconBattery != null)
                mIconBattery.setVisibility(View.VISIBLE);
            if (mAnimBattery != null)
                mAnimBattery.setVisibility(View.GONE);
        }
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
