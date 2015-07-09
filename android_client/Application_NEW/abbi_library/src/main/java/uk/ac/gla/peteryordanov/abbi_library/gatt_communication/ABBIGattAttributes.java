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

package uk.ac.gla.peteryordanov.abbi_library.gatt_communication;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.HashMap;
import java.util.UUID;

import uk.ac.gla.peteryordanov.abbi_library.AudioContinuous;
import uk.ac.gla.peteryordanov.abbi_library.AudioIntermittent;
import uk.ac.gla.peteryordanov.abbi_library.BluetoothLeService;
import uk.ac.gla.peteryordanov.abbi_library.utility.UUIDConstants;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class ABBIGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    private static BluetoothGattCharacteristic notifyCharacteristic;
    public static BluetoothLeService bluetoothLeService;
    public static BluetoothGattService batteryService;
    public static BluetoothGattService customService;
    public static BluetoothGattService motionService;

    //------------------------------------------------------------------------
    // list of ABBI characteristic UUID with description
    static {
        // Sample Services.
        attributes.put(UUIDConstants.Battery_Service_UUID, "Battery Service");
        attributes.put(UUIDConstants.Custom_Service_UUID, "Custom Service");
        // Sample Characteristics.
        attributes.put(UUIDConstants.BatteryLevel_UUID, "Battery level");
        attributes.put(UUIDConstants.VolumeLevel_UUID, "Volume level");
        attributes.put(UUIDConstants.SoundCtrl_UUID, "Sound control");
        //attributes.put(MuteState_UUID, "system on/off");
        attributes.put(UUIDConstants.AudioMode_UUID, "Audio mode");
        attributes.put(UUIDConstants.AudioContinuous_UUID, "continuous audio structure");
        attributes.put(UUIDConstants.AudioStream1_UUID, "intermittent audio stream 1");
        attributes.put(UUIDConstants.AudioStream2_UUID, "intermittent audio stream 2");
        attributes.put(UUIDConstants.AudioBPM_UUID, "intermittent audio beats per minute");
        attributes.put(UUIDConstants.AudioPlayback_UUID, "audio playback file Id");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    //------------------------------------------------------------------------

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private static boolean GetCharacteristicData(BluetoothGattCharacteristic characteristic) {
        boolean returnval = false;
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (notifyCharacteristic != null) {
                bluetoothLeService.setCharacteristicNotification(
                        notifyCharacteristic, false);
                notifyCharacteristic = null;
            }
            bluetoothLeService.readCharacteristic(characteristic);
            returnval = true;
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            notifyCharacteristic = characteristic;
            bluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
            returnval = true;
        }
        return returnval;
    }

    //----------------------------------------------------------------------------

    private static boolean readAbbiBatteryCharacteristic(String characteristicUUID){
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = batteryService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }

    private static boolean readAbbiCustomCharacteristic(String characteristicUUID){
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = customService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }

    private static boolean readAbbiMotionCharacteristic(String characteristicUUID){
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = motionService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }

    // -----------------

    private static void writeAbbiCharacteristic(String uuid, int val, int format) {
        writeAbbiCharacteristic(uuid, val, format, 0, true);
    }

    private static void writeAbbiCharacteristic(String uuid, int val, int format, int offset, boolean bleWrite){
        if (customService != null) {
            BluetoothGattCharacteristic characteristic = customService.getCharacteristic(UUID.fromString(uuid));
            if (characteristic == null)
                return;
            characteristic.setValue(val, format, offset);
            if (bleWrite)
                bluetoothLeService.writeCharacteristic(characteristic);
        }
    }

    private static void writeAbbiCharacteristic(String uuid, byte[] val){
        if (customService != null) {
            BluetoothGattCharacteristic characteristic = customService.getCharacteristic(UUID.fromString(uuid));
            if (characteristic == null)
                return;
            characteristic.setValue(val);
            bluetoothLeService.writeCharacteristic(characteristic);
        }
    }

    //----------------------------------------------------------------------------

    public static void readBatteryLevel () {
        readAbbiBatteryCharacteristic(UUIDConstants.BatteryLevel_UUID);
    }

    public static void readMainVolumeLevel () {
        readAbbiCustomCharacteristic(UUIDConstants.VolumeLevel_UUID);
    }

    public static void readSoundCtrlMode () {
        readAbbiCustomCharacteristic(UUIDConstants.SoundCtrl_UUID);
    }

    public static void readAudioMode () {
        readAbbiCustomCharacteristic(UUIDConstants.AudioMode_UUID);
    }

    public static void readWavFileId () {
        readAbbiCustomCharacteristic(UUIDConstants.AudioPlayback_UUID);
    }

    public static void readContinuousStream () {
        readAbbiCustomCharacteristic(UUIDConstants.AudioContinuous_UUID);
    }

    public static void readIntermittentStream (int i) {
        String AudioStream_UUID;
        if (i == ABBIIntermittentStream.Stream1)
            AudioStream_UUID = UUIDConstants.AudioStream1_UUID;
        else if (i == ABBIIntermittentStream.Stream2)
            AudioStream_UUID = UUIDConstants.AudioStream2_UUID;
        else
            return;

        readAbbiCustomCharacteristic(AudioStream_UUID);
    }

    public static void readIntermittentBPM () {
        readAbbiCustomCharacteristic(UUIDConstants.AudioBPM_UUID);
    }

    public static void readAccelerometer () {
        readAbbiMotionCharacteristic(UUIDConstants.Accelerometer_UUID);
    }

    public static void readGyroscope () {
        readAbbiMotionCharacteristic(UUIDConstants.Gyroscope_UUID);
    }

    public static void readMagnetometer () {
        readAbbiMotionCharacteristic(UUIDConstants.Magnetometer_UUID);
    }

    public static void readIMU () {
        readAbbiMotionCharacteristic(UUIDConstants.IMU_UUID);
    }

    //----------------------------------------------------------------------------

    public static void writeMainVolumeLevel (int x)
    {
        writeAbbiCharacteristic(UUIDConstants.VolumeLevel_UUID,
                x * 32767 / 15,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

    public static void writeSoundCtrlMode (int n)
    {
        writeAbbiCharacteristic(UUIDConstants.SoundCtrl_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeAudioMode (int id)
    {
        int n = ABBISoundMode.Intermittent;
        if (id == R.id.radioSoundContinuous) {
            n = ABBISoundMode.Continuous;

        } else if (id == R.id.radioSoundIntermmitent) {
            n = ABBISoundMode.Intermittent;

        } else if (id == R.id.radioSoundPlayback) {
            n = ABBISoundMode.Playback;

        }
        writeAbbiCharacteristic(UUIDConstants.AudioMode_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeWavFileId (int x)
    {
        writeAbbiCharacteristic(UUIDConstants.AudioPlayback_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeContinuousStream (int f, int v)
    {
        writeContinuousStream(new AudioContinuous(f,v));
    }

    public static void writeContinuousStream (AudioContinuous stream){
        writeAbbiCharacteristic(UUIDConstants.AudioContinuous_UUID,
                stream.getByteArray());
    }

    public static void writeIntermittentStream(int index, int f, int a, int s, int d, int v)
    {
        writeIntermittentStream(index, new AudioIntermittent(f, a, s, d, v));
    }

    public static void writeIntermittentStream(int index, AudioIntermittent stream){
        String AudioStream_UUID;
        if (index == ABBIIntermittentStream.Stream1)
            AudioStream_UUID = UUIDConstants.AudioStream1_UUID;
        else if (index == ABBIIntermittentStream.Stream2)
            AudioStream_UUID = UUIDConstants.AudioStream2_UUID;
        else
            return;

        writeAbbiCharacteristic(AudioStream_UUID, stream.getByteArray());
    }

    public static void writeIntermittentBPM (int x)
    {
        writeAbbiCharacteristic(UUIDConstants.AudioBPM_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

}

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

class ABBIIntermittentStream {
    public static final int Stream1 = 1;
    public static final int Stream2 = 2;
}

/*class ABBIConstants{
    public static final int MAX_VOLUME = 32767 * 2;

}*/

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

