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
import android.widget.ImageView;
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
public class AdvancedOptionsActivity extends Activity {
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
    private RadioGroup mRadioSound = null;
    private Button mButtonSoundProperties = null;

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
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(ABBIGattReadWriteCharacteristics.bluetoothLeService.getSupportedGattServices());
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
        setContentView(R.layout.activity_advanced_options);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mLayoutInfo = (LinearLayout) findViewById(R.id.linearLayoutInfo);
        mAddressField = (TextView) findViewById(R.id.device_address);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        mRadioSound = (RadioGroup) findViewById(R.id.radioGroup1);
        mButtonSoundProperties = (Button) findViewById(R.id.buttonSoundProperties);


        // set text field data
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //updateDeviceAddress(mDeviceAddress);

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





    private void displayAbbiUiValues(String charact, byte[] data) {
        if (charact == null)
            return;

        if (data == null)
            return;

        int dataint = byteArrayToInt(data);

        switch (charact) {
            case UUIDConstants.BatteryLevel_UUID:
                Globals.batteryLevel = dataint;
                //updateBatteryLevel(dataint);
                break;
            case UUIDConstants.VolumeLevel_UUID:
                Globals.volumeLevel = dataint;
                break;
            case UUIDConstants.SoundCtrl_UUID:
                Globals.soundControlState = dataint;
                boolean soundOn  = Globals.soundControlState == Globals.SOUND_STATE_ON_ID;
                boolean soundOff = Globals.soundControlState == Globals.SOUND_STATE_OFF_ID;
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
                //displayData(String.valueOf(dataint));
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

        mRadioSound.setOnCheckedChangeListener(handleSoundModeChanged);
        mButtonSoundProperties.setOnClickListener(handleSoundPropertiesClick);

    }



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

