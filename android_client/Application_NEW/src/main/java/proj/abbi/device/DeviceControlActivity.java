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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Random;

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
    private RelativeLayout mLogDataLayout = null;
    private CircularSeekBar mVolumeBar = null;
    private Switch mSoundCtrl = null;
    private Switch mMuteSwitch = null;
    private Switch mUserModeSwitch = null;
    private RadioGroup mRadioSound = null;
    private Button mButtonSoundProperties = null;
    private ProgressBar progressBar = null;
    private TextView PBtextView = null;
    private TextView textViewVol = null;

    private boolean experimenterMode = false;

    //accessibility-related fields
    private Vibrator vibrator;
    private SoundPool sp;
    private int soundIdVolume;

    private LineGraphSeries<DataPoint> accelerometerData;


    /**
     * Manage the service cycle when connecting to BLE devices.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * Specify behaviour on sucessful connection.
         * @param componentName
         * @param service
         */
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

        /**
         * Specify behaviour when the service is disconnected.
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ABBIGattReadWriteCharacteristics.bluetoothLeService = null;

        }
    };

    /**
     * Handles various events fired by the Service.
     * ACTION_GATT_CONNECTED: connected to a GATT server.
     * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     * ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
     * or notification operations.
     */
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

    /**
     * Reset the additional data field ("Sensor Data")
     */
    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        /**
         * Showcase device characteristics on the UI:
         *  - device name
         *  - device MAC address
         *  - sensor data, if available
         */
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //Initialize all of the layouts and components manipulated by this class

        mLayoutInfo = (LinearLayout) findViewById(R.id.linearLayoutInfo);
        mLogDataLayout = (RelativeLayout) findViewById(R.id.logDataLayout);
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
        mUserModeSwitch = (Switch) findViewById(R.id.modeSwitch);

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

        //accessibility-related features
        findViewById(R.id.linearLayoutVolume).setOnClickListener(handleVolumeClicked);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); // vibratory haptic feedback enabled by the vibrator service

        //sonification
        sp = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        //volume control from the cellphone:
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //load the audio
        soundIdVolume = sp.load(this, R.raw.g_major, 1);

        //visualize data
        Random random = new Random();
        int max = 100;

        //initialize the graph
        GraphView graph = (GraphView) findViewById(R.id.graph);

        /**
         * Sensor data plot - simulation using random values to generate a visualization:
         *  - accelerometer - green
         *  - gyroscope - red
         *  - magnetometer - blue
         */
        accelerometerData = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, random.nextInt((max) + 1)),
                new DataPoint(1, random.nextInt((max) + 1)),
                new DataPoint(2, random.nextInt((max) + 1)),
                new DataPoint(3, random.nextInt((max) + 1)),
                new DataPoint(4, random.nextInt((max) + 1)),

        });

        LineGraphSeries<DataPoint> gyroscopeData = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, random.nextInt((max) + 1)),
                new DataPoint(1, random.nextInt((max) + 1)),
                new DataPoint(2, random.nextInt((max) + 1)),
                new DataPoint(3, random.nextInt((max) + 1)),
                new DataPoint(4, random.nextInt((max) + 1)),

        });

        LineGraphSeries<DataPoint> magnetometerData = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, random.nextInt((max) + 1)),
                new DataPoint(2, random.nextInt((max) + 1)),
                new DataPoint(3, random.nextInt((max) + 1)),
                new DataPoint(4, random.nextInt((max) + 1)),

        });

        //set the labels for the LineGraphSeries
        accelerometerData.setTitle("Accelerometer");
        gyroscopeData.setTitle("Gyroscope");
        magnetometerData.setTitle("Magnetometer");

        //set the colors for the different series
        accelerometerData.setColor(Color.argb(255, 81, 218, 99));
        gyroscopeData.setColor(Color.argb(255, 254, 101, 53));
        magnetometerData.setColor(Color.argb(255, 62, 141, 218));

        //add all the three series to the main GraphView
        graph.addSeries(accelerometerData);
        graph.addSeries(gyroscopeData);
        graph.addSeries(magnetometerData);

        /**
         * - make the legend visible
         * - align to the top of the graph
         * - set the background color to light gray
         */
        graph.getLegendRenderer().setBackgroundColor(Color.argb(218, 218, 218, 218));
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

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
        // Restore user preferences
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

    /**
     * Create the ActionBar menu and set the visibility of the buttons depending on the current state.
     * - If the application is in idle state - the scan button is visible
     * - Otherwise, the stop button is available
     */
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

    /**
     * Trigger a relevant action depending on which ActionBar menu item was interacted with.
     *
     * @param item the selected MenuItem is passed as an input parameter
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Override the device haptic volume buttons behaviour in order to use them for SeekBar manipulation
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (Globals.CURRENT_HAPTIC_BUTTONS_WIRING == Globals.MAIN_VOLUME_ID) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                mVolumeBar.setProgress(mVolumeBar.getProgress() + 1);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                mVolumeBar.setProgress(mVolumeBar.getProgress() - 1);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * Activate/ deactivate experimenter/ parental mode.
     */

    private void toggleExperimenterMode() {

        experimenterMode = !experimenterMode;
        SharedPreferences settings = getSharedPreferences(CONFIG_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("experimenterMode", experimenterMode);
        editor.commit();
        String t = experimenterMode ? "You are now in Experimenter/ Parental mode" : "You are now in User mode";
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }


    /**
     * Process the received data {@paramref data} and make the appropriate function call, depending on the {@paramref charact} input parameter
     *
     * @param charact characteristic UUID code
     * @param data    a byte array of raw data received from the BLE device
     */
    private void displayAbbiUiValues(String charact, byte[] data) {
        if (charact == null)
            return;

        if (data == null)
            return;

        int dataint = byteArrayToInt(data);

        switch (charact) {
            case UUIDConstants.BATTERY_LEVEL_UUID:
                Globals.batteryLevel = dataint;
                updateBatteryLevel(dataint);
                break;
            case UUIDConstants.VOLUME_LEVEL_UUID:
                Globals.volumeLevel = dataint;
                updateMainVolumeLevel(dataint);
                break;
            case UUIDConstants.SOUND_CONTROL_UUID:
                Globals.soundControlState = dataint;
                boolean soundOn = Globals.soundControlState == Globals.SOUND_STATE_ON_ID;
                boolean soundOff = Globals.soundControlState == Globals.SOUND_STATE_OFF_ID;
                updateMuteSwitch(soundOff);
                updateSoundCtrlButton(soundOn);
                break;
            case UUIDConstants.AUDIO_MODE_UUID:
                Globals.audioMode = dataint;
                updateAudioMode(dataint);
                break;
            case UUIDConstants.AUDIO_CONTINUOUS_UUID:
                Globals.audioContinuous = new AudioContinuous(data);
                break;
            case UUIDConstants.AUDIO_STREAM_1_UUID:
                Globals.audioStream1 = new AudioIntermittent(data);
                break;
            case UUIDConstants.AUDIO_STREAM_2_UUID:
                Globals.audioStream2 = new AudioIntermittent(data);
                break;
            case UUIDConstants.AUDIO_BPM_UUID:
                Globals.audioBPM = dataint;
                break;
            case UUIDConstants.AUDIO_PLAYBACK_UUID:
                Globals.audioPlayback = dataint;
                break;
            case UUIDConstants.ACCELEROMETER_UUID:

                displayData(String.valueOf(dataint));
                //Todo: visualize accelerometer data
                break;
            case UUIDConstants.GYROSCOPE_UUID:
                displayData(String.valueOf(dataint));
                //Todo: visualize gyroscope data
                break;
            case UUIDConstants.MAGNETOMETER_UUID:
                displayData(String.valueOf(dataint));
                //Todo: visualize magnetometer data
                break;
            case UUIDConstants.IMU_UUID:
                // does not trigger with the regular ABBI devices; should work with the IMU device
                break;
        }
    }

    /**
     * Update the application mode - parental/experimenter or user mode, depending on the value of {@link DeviceControlActivity#experimenterMode}
     */
    private void changeAppModel() {

        //check current operational mode
        mUserModeSwitch.setChecked(experimenterMode);

        if (!experimenterMode) {
            mSoundCtrl.setVisibility(View.GONE);
            mMuteSwitch.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.GONE);
            mLogDataLayout.setVisibility(View.GONE);

        } else {
            mMuteSwitch.setVisibility(View.GONE);
            mSoundCtrl.setVisibility(View.VISIBLE);
            mLayoutInfo.setVisibility(View.VISIBLE);
            mLogDataLayout.setVisibility(View.VISIBLE);

        }
    }

    //Setter methods are implemented below and used to update UI values

    /**
     * @param deviceAddress this parameter is used to pass the device's MAC address
     */
    private void updateDeviceAddress(final String deviceAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddressField.setText(deviceAddress);
            }
        });
    }

    /**
     * @param resourceId current connection state: connected/ disconnected
     */
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    /**
     * @param data additional received data (sensor values)
     */
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

    /**
     * @param battLevel update the battery level using this integer from 0 to 100 %
     */
    private void updateBatteryLevel(final int battLevel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLevelIndicator(battLevel);
            }
        });
    }

    /**
     * @param volLevel current volume level on the BLE device
     */
    private void updateMainVolumeLevel(final int volLevel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVolumeBar.setProgress(volLevel);
            }
        });
    }

    /**
     * @param mute mute on/off
     */
    private void updateMuteSwitch(final boolean mute) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMuteSwitch.setChecked(mute);
            }
        });
    }

    /**
     * @param soundOn sound emission on/off
     */
    private void updateSoundCtrlButton(final boolean soundOn) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSoundCtrl.setChecked(soundOn);
            }
        });
    }

    /**
     * @param audioMode specify the audio mode code:
     *                  - continuous
     *                  - intermittent
     *                  - playback (choose a .wav file from the list if the files have been uploaded to ABBI)
     */
    private void updateAudioMode(final int audioMode) {
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


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            if (uuid.equals(UUIDConstants.BATTERY_SERVICE_UUID)) {
                ABBIGattReadWriteCharacteristics.batteryService = gattService;
                ABBIGattReadWriteCharacteristics.readBatteryLevel();
            } else if (uuid.equals(UUIDConstants.CUSTOM_SERVICE_UUID)) {
                ABBIGattReadWriteCharacteristics.customService = gattService;
                ABBIGattReadWriteCharacteristics.readMainVolumeLevel();
                ABBIGattReadWriteCharacteristics.readSoundCtrlMode();
                ABBIGattReadWriteCharacteristics.readAudioMode();
                ABBIGattReadWriteCharacteristics.readContinuousStream();
                ABBIGattReadWriteCharacteristics.readIntermittentStream(1);
                ABBIGattReadWriteCharacteristics.readIntermittentStream(2);
                ABBIGattReadWriteCharacteristics.readIntermittentBPM();
                ABBIGattReadWriteCharacteristics.readWavFileId();
            } else if (uuid.equals(UUIDConstants.MOTION_SERVICE_UUID)) {
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


    /**
     * Set up the handlers for seekbar, switches, standard buttons and radio buttons eventListeners
     */
    protected void wireUpLocalHandlers() {
        mVolumeBar.setOnSeekBarChangeListener(handleVolumeChanged);
        mSoundCtrl.setOnCheckedChangeListener(handleSoundCtrlChanged);
        mMuteSwitch.setOnCheckedChangeListener(handleMuteChanged);
        mUserModeSwitch.setOnCheckedChangeListener(handleUserModeChanged);
        mRadioSound.setOnCheckedChangeListener(handleSoundModeChanged);
        mButtonSoundProperties.setOnClickListener(handleSoundPropertiesClick);
    }

    /**
     * EVENT LISTENERS: implement state changed listeners for the UI components
     */

    /**
     * Sonification and vibration on volume seekbar tap/click
     */
    View.OnClickListener handleVolumeClicked = new View.OnClickListener() {
        public void onClick(View v) {
            Globals.CURRENT_HAPTIC_BUTTONS_WIRING = Globals.MAIN_VOLUME_ID;
            //v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            vibrator.vibrate(Globals.VOLUME_VIBRATION_MS);

            //volume main
            sp.play(soundIdVolume, Globals.SOUND_SECONDARY_VOLUME, Globals.SOUND_PRIMARY_VOLUME, 0, Globals.SOUND_STREAM1_LOOP, 1);
        }
    };

    /**
     * Volume seekbar changed state listeners
     */
    private CircularSeekBar.OnCircularSeekBarChangeListener handleVolumeChanged = new CircleSeekBarListener() {
        @Override
        public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getProgress() != 0) {
                textViewVol.setText(Html.fromHtml("<b>Volume<br>" + UtilityFunctions.changeRangeMaintainingRatio(seekBar.getProgress(), Globals.UI_VOLUME_RANGE_MIN, Globals.UI_VOLUME_RANGE_MAX, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MIN, Globals.BRACELET_SOURCE_DB_VOLUME_RANGE_MAX) + " dB</b>"));
            }

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            ABBIGattReadWriteCharacteristics.writeMainVolumeLevel(seekBar.getProgress());
        }
    };

    /**
     * Sound emission on/off switch changed state listener
     */
    private Switch.OnCheckedChangeListener handleSoundCtrlChanged = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_ON_ID);
            else if (mMuteSwitch.isChecked()) {
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_OFF_ID);
            } else {
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_TRIGGER_ID);
            }
        }
    };

    /**
     * User and parental/ experimenter mode switch event listener, calling {@link DeviceControlActivity#toggleExperimenterMode()} and {@link DeviceControlActivity#changeAppModel()}
     */
    private Switch.OnCheckedChangeListener handleUserModeChanged = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            toggleExperimenterMode();
            changeAppModel();
        }
    };

    /**
     * Handle the mute/unmute switch changes via the {@link ABBIGattReadWriteCharacteristics#writeSoundCtrlMode(int)} function
     */
    private Switch.OnCheckedChangeListener handleMuteChanged = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_OFF_ID);
            else
                ABBIGattReadWriteCharacteristics.writeSoundCtrlMode(Globals.SOUND_STATE_TRIGGER_ID);
        }
    };

    /**
     * Write sound mode changes to the ABBI bracelet whenever the radio group {@link DeviceControlActivity#mRadioSound} state changes
     */
    private RadioGroup.OnCheckedChangeListener handleSoundModeChanged = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            ABBIGattReadWriteCharacteristics.writeAudioMode(checkedId);

        }
    };

    /**
     * Handle audio playback mode changes when the {@link DeviceControlActivity#mButtonSoundProperties} is tapped:
     * - continuous
     * - intermittent sound emission
     * - file playback if .wav files have been uploaded on the bracelet
     */
    private Button.OnClickListener handleSoundPropertiesClick = new Button.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            Intent pbActIntent = null;
            int radioChecked = mRadioSound.getCheckedRadioButtonId();
            if (radioChecked == R.id.radioSoundContinuous) {
                pbActIntent = new Intent(buttonView.getContext(), ContinuousActivity.class);
            } else if (radioChecked == R.id.radioSoundIntermmitent) {
                pbActIntent = new Intent(buttonView.getContext(), IntermittentActivity.class);
            } else if (radioChecked == R.id.radioSoundPlayback) {
                pbActIntent = new Intent(buttonView.getContext(), PlaybackActivity.class);
            }
            startActivity(pbActIntent);
        }
    };

    /**
     * Update the battery level
     *
     * @param n the current battery level in percentages
     */
    protected void updateLevelIndicator(int n) {

        progressBar.setProgress(n);
        PBtextView.setText(Html.fromHtml("<b>Battery<br>" + n + " %</b>"));

    }

    /**
     * A function used to convert a byte[] to int
     *
     * @param data the input byte[]
     * @return an int representation of the array
     */
    public int byteArrayToInt(byte[] data) {
        int l = data.length;
        ByteBuffer bb = ByteBuffer.wrap(data);
        int res = 0;
        switch (l) {
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
