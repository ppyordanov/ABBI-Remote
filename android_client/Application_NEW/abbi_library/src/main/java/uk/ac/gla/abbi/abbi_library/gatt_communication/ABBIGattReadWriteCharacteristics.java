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

package uk.ac.gla.abbi.abbi_library.gatt_communication;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.HashMap;
import java.util.UUID;

import uk.ac.gla.abbi.abbi_library.BluetoothLeService;
import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UUIDConstants;
import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.R;

/**
 * This class includes the GATT attribute functions for reading and writing values from and to
 * an ABBI bracelet device.
 * <p/>
 * The folloring implementations are present:
 * <p/>
 * - a list of ABBI characteristic UUIDs with descriptions
 * - a lookup function for quick and easy UUID discovery
 * - read and write functions for ABBI bracelet's attributes
 */

public class ABBIGattReadWriteCharacteristics {
    private static HashMap<String, String> attributes = new HashMap();
    private static BluetoothGattCharacteristic notifyCharacteristic;
    public static BluetoothLeService bluetoothLeService;
    public static BluetoothGattService batteryService;
    public static BluetoothGattService customService;
    public static BluetoothGattService motionService;

    /**
     * This is a list of ABBI characteristic UUID with description to improve look-up.
     */
    static {
        // Sample Services.
        attributes.put(UUIDConstants.BATTERY_SERVICE_UUID, "Battery Service");
        attributes.put(UUIDConstants.CUSTOM_SERVICE_UUID, "Custom Service");
        // Sample Characteristics.
        attributes.put(UUIDConstants.BATTERY_LEVEL_UUID, "Battery level");
        attributes.put(UUIDConstants.VOLUME_LEVEL_UUID, "Volume level");
        attributes.put(UUIDConstants.SOUND_CONTROL_UUID, "Sound control");

        attributes.put(UUIDConstants.AUDIO_MODE_UUID, "Audio mode");
        attributes.put(UUIDConstants.AUDIO_CONTINUOUS_UUID, "continuous audio structure");
        attributes.put(UUIDConstants.AUDIO_STREAM_1_UUID, "intermittent audio stream 1");
        attributes.put(UUIDConstants.AUDIO_STREAM_2_UUID, "intermittent audio stream 2");
        attributes.put(UUIDConstants.AUDIO_BPM_UUID, "intermittent audio beats per minute");
        attributes.put(UUIDConstants.AUDIO_PLAYBACK_UUID, "audio playback file Id");
    }

    /**
     * A function to be used for UUID protocol name look-up
     *
     * @param uuid        the query UUID
     * @param defaultName the default UUID name
     * @return
     */
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }


    /**
     * If a given GATT characteristic is selected, check for supported features.  This sample
     * demonstrates 'Read' and 'Notify' features.  See
     * http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
     * list of supported characteristic features.
     */
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

    /**
     * Read the battery characteristics
     *
     * @param characteristicUUID UUID value
     * @return
     */
    private static boolean readAbbiBatteryCharacteristic(String characteristicUUID) {
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = batteryService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }

    /**
     * Read ABBI custom characteristics
     *
     * @param characteristicUUID UUID value
     * @return
     */
    private static boolean readAbbiCustomCharacteristic(String characteristicUUID) {
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = customService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }

    /**
     * Read ABBI motion characteristics
     *
     * @param characteristicUUID UUID value
     * @return
     */
    private static boolean readAbbiMotionCharacteristic(String characteristicUUID) {
        UUID char_uuid = UUID.fromString(characteristicUUID);
        BluetoothGattCharacteristic bleChar = motionService.getCharacteristic(char_uuid);

        return bleChar != null && GetCharacteristicData(bleChar);

    }


    /**
     * Write ABBI characteristic
     *
     * @param uuid   current UUID
     * @param val    characteristic value
     * @param format characteristic format
     */
    private static void writeAbbiCharacteristic(String uuid, int val, int format) {
        writeAbbiCharacteristic(uuid, val, format, 0, true);
    }

    /**
     * Write ABBI characteristic, including offset and specification whether to write the characteristic {@paramref bleWrite}
     *
     * @param uuid
     * @param val
     * @param format
     * @param offset
     * @param bleWrite
     */
    private static void writeAbbiCharacteristic(String uuid, int val, int format, int offset, boolean bleWrite) {
        if (customService != null) {
            BluetoothGattCharacteristic characteristic = customService.getCharacteristic(UUID.fromString(uuid));
            if (characteristic == null)
                return;
            characteristic.setValue(val, format, offset);
            if (bleWrite)
                bluetoothLeService.writeCharacteristic(characteristic);
        }
    }

    /**
     * Write ABBI characteristic
     *
     * @param uuid specific characteristic UUID
     * @param val  an input array to be used for writing
     */
    private static void writeAbbiCharacteristic(String uuid, byte[] val) {
        if (customService != null) {
            BluetoothGattCharacteristic characteristic = customService.getCharacteristic(UUID.fromString(uuid));
            if (characteristic == null)
                return;
            characteristic.setValue(val);
            bluetoothLeService.writeCharacteristic(characteristic);
        }
    }

    /**
     * Reading values for:
     * - battery level
     * - main volume level
     * - sound on/off/trigger mode
     * - audio mode (continuous, intermittent, playback)
     * - .wav file id
     * - continuous stream properties
     * - intermittent stream properties
     * - intermittent BPM
     * - accelerometer data
     * - gyroscope data
     * - magnetometer data
     * - IMU data
     */

    public static void readBatteryLevel() {
        readAbbiBatteryCharacteristic(UUIDConstants.BATTERY_LEVEL_UUID);
    }

    public static void readMainVolumeLevel() {
        readAbbiCustomCharacteristic(UUIDConstants.VOLUME_LEVEL_UUID);
    }

    public static void readSoundCtrlMode() {
        readAbbiCustomCharacteristic(UUIDConstants.SOUND_CONTROL_UUID);
    }

    public static void readAudioMode() {
        readAbbiCustomCharacteristic(UUIDConstants.AUDIO_MODE_UUID);
    }

    public static void readWavFileId() {
        readAbbiCustomCharacteristic(UUIDConstants.AUDIO_PLAYBACK_UUID);
    }

    public static void readContinuousStream() {
        readAbbiCustomCharacteristic(UUIDConstants.AUDIO_CONTINUOUS_UUID);
    }

    /**
     * Read the values for the intermittent stream
     *
     * @param i coresponds to the stream id (1 or 2, denoting stream 1 and stream 2 accordingly)
     */
    public static void readIntermittentStream(int i) {
        String AudioStream_UUID;
        if (i == Globals.ABBI_INTERMITTENT_STREAM1_ID)
            AudioStream_UUID = UUIDConstants.AUDIO_STREAM_1_UUID;
        else if (i == Globals.ABBI_INTERMITTENT_STREAM2_ID)
            AudioStream_UUID = UUIDConstants.AUDIO_STREAM_2_UUID;
        else
            return;

        readAbbiCustomCharacteristic(AudioStream_UUID);
    }

    public static void readIntermittentBPM() {
        readAbbiCustomCharacteristic(UUIDConstants.AUDIO_BPM_UUID);
    }

    public static void readAccelerometer() {
        readAbbiMotionCharacteristic(UUIDConstants.ACCELEROMETER_UUID);
    }

    public static void readGyroscope() {
        readAbbiMotionCharacteristic(UUIDConstants.GYROSCOPE_UUID);
    }

    public static void readMagnetometer() {
        readAbbiMotionCharacteristic(UUIDConstants.MAGNETOMETER_UUID);
    }

    public static void readIMU() {
        readAbbiMotionCharacteristic(UUIDConstants.IMU_UUID);
    }

    /**
     * Writing values to the BLE bracelet
     */

    /**
     * Write the main volume
     *
     * @param x denotes the volume value from the UI range
     */
    public static void writeMainVolumeLevel(int x) {
        writeAbbiCharacteristic(UUIDConstants.VOLUME_LEVEL_UUID,
                x * 32767 / 15,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

    /**
     * Write the sound on/off/trigger mode
     *
     * @param n sound control id
     */
    public static void writeSoundCtrlMode(int n) {
        writeAbbiCharacteristic(UUIDConstants.SOUND_CONTROL_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    /**
     * Write audio mode - continuous, intermittent, playback
     *
     * @param id denoting the mode
     */
    public static void writeAudioMode(int id) {
        int n = Globals.INTERMITTENT_SOUND_MODE_ID;
        if (id == R.id.radioSoundContinuous) {
            n = Globals.CONTINUOUS_SOUND_MODE_ID;

        } else if (id == R.id.radioSoundIntermmitent) {
            n = Globals.INTERMITTENT_SOUND_MODE_ID;

        } else if (id == R.id.radioSoundPlayback) {
            n = Globals.PLAYBACK_SOUND_MODE_ID;

        }
        writeAbbiCharacteristic(UUIDConstants.AUDIO_MODE_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    /**
     * Write .waf file id
     *
     * @param x file id
     */
    public static void writeWavFileId(int x) {
        writeAbbiCharacteristic(UUIDConstants.AUDIO_PLAYBACK_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    /**
     * Write continuous stream
     *
     * @param f denotes the frequency
     * @param v denotes the volume
     */
    public static void writeContinuousStream(int f, int v) {
        writeContinuousStream(new AudioContinuous(f, v));
    }

    /**
     * Write continuous stream
     *
     * @param stream input stream
     */
    public static void writeContinuousStream(AudioContinuous stream) {
        writeAbbiCharacteristic(UUIDConstants.AUDIO_CONTINUOUS_UUID,
                stream.getByteArray());
    }

    /**
     * Write intermittent stream
     *
     * @param index stream 1 or stream 2 id
     * @param f     stream frequency
     * @param a     stream attack
     * @param s     sustain
     * @param d     decay
     * @param v     volume
     */
    public static void writeIntermittentStream(int index, int f, int a, int s, int d, int v) {
        writeIntermittentStream(index, new AudioIntermittent(f, a, s, d, v));
    }

    /**
     * Write intermittent stream
     *
     * @param index  stream 1/2 id
     * @param stream input stream
     */
    public static void writeIntermittentStream(int index, AudioIntermittent stream) {
        String AudioStream_UUID;
        if (index == Globals.ABBI_INTERMITTENT_STREAM1_ID)
            AudioStream_UUID = UUIDConstants.AUDIO_STREAM_1_UUID;
        else if (index == Globals.ABBI_INTERMITTENT_STREAM2_ID)
            AudioStream_UUID = UUIDConstants.AUDIO_STREAM_2_UUID;
        else
            return;

        writeAbbiCharacteristic(AudioStream_UUID, stream.getByteArray());
    }

    /**
     * Write intermittent BPM
     *
     * @param x denotes the number of beats per minute
     */
    public static void writeIntermittentBPM(int x) {
        writeAbbiCharacteristic(UUIDConstants.AUDIO_BPM_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

}
