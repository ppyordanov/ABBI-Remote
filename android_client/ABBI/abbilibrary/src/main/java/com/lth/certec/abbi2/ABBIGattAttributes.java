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

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.UUID;

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

    // Service and Characteristics UUIDs
    public static final String Battery_Service_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BatteryLevel_UUID    = "00002a19-0000-1000-8000-00805f9b34fb";

    public static final String Custom_Service_UUID  = "624e957f-cb42-4cd6-bacc-84aeb898f69b";
    public static final String VolumeLevel_UUID     = "e4c937b3-7f6d-41f9-b997-40c561f4453b";
    public static final String SoundCtrl_UUID       = "df342b03-53f9-43b4-acb6-62a63ca0615a";
    public static final String AudioStream1_UUID    = "3709a55c-e4e2-43c1-9248-5d07da39630c";
    public static final String AudioStream2_UUID 	= "f63a51f9-ae25-480a-b7f1-ff88a8afb15a";
    public static final String AudioMode_UUID       = "b3dc2fd0-5950-4e19-8623-a34d3d8330ee";
    public static final String AudioBPM_UUID 		= "46906ff7-ca01-4353-87fb-510ed7a59d62";
    public static final String AudioContinuous_UUID = "56025b6b-7642-42e9-9233-4e5bf2551ce2";
    public static final String AudioPlayback_UUID   = "97d02b24-bbe4-44c3-b2ba-3049a654d9ed";
    public static final String AwakeTime_UUID       = "0dc3add1-bfe2-42b5-9545-66fbbe49910f";
    public static final String RTC_UUID             = "38302d33-6fb3-4c2e-8276-1e7ccece9d1c";
    public static final String Trigger_UUID         = "98903107-c6f8-43e0-b6c2-7de391832a8a";
    public static final String SerialNumber_UUID    = "70b867f6-84ac-4e79-b24c-95e04192772e";
    public static final String DeviceId_UUID        = "fda36bfe-ef6f-4277-9d45-56613cd1ac4a";
    public static final String SystemOnOff_UUID     = "0ab23985-8ebe-4fa4-9533-650a03dc747d";

    public static final String Logging_Service_UUID = "51d4a419-282f-41ca-a6b2-87cb6efb4200";
    public static final String LogPointer_UUID      = "3539a442-fc2a-4082-91b9-45e8439641f7";
    public static final String ErraseFlash_UUID     = "057b90e4-8f03-4f9b-a3bc-12c60a05b6ef";
    public static final String DataBlob_UUID        = "4cb2f3e6-f263-4105-94a4-19fa4fdce0cc";

    public static final String Motion_Service_UUID  = "51d4a419-0004-0000-0000-87cb6efb4200";
    public static final String Accelerometer_UUID   = "51d4a419-0004-0001-0000-87cb6efb4200";
    public static final String Gyroscope_UUID       = "51d4a419-0004-0002-0000-87cb6efb4200";
    public static final String Magnetometer_UUID    = "51d4a419-0004-0003-0000-87cb6efb4200";
    public static final String IMU_UUID             = "51d4a419-0004-0004-0000-87cb6efb4200";
    public static final String IMU_Config_UUID      = "51d4a419-0004-0005-0000-87cb6efb4200";
    public static final String IMU_Period_UUID      = "51d4a419-0004-0006-0000-87cb6efb4200";

    // ABBI characteristic with default values
    public static int batteryLevel = 0;
    public static int volumeLevel = 20000;
    public static int soundCtrl = ABBISoundCtrl.SoundTrigger;
    public static int audioMode = ABBISoundMode.Intermittent;
    public static AudioContinuous audioContinuous = new AudioContinuous(600, 50000);
    public static AudioIntermittent audioStream1 = new AudioIntermittent(500, 1000, 0, 50000, 50000);
    public static AudioIntermittent audioStream2 = new AudioIntermittent(800, 1000, 0, 50000, 50000);
    public static int audioBPM = 120;
    public static int audioPlayback = 1;

    //------------------------------------------------------------------------
    // list of ABBI characteristic UUID with description
    static {
        // Sample Services.
        attributes.put(Battery_Service_UUID, "Battery Service");
        attributes.put(Custom_Service_UUID, "Custom Service");
        // Sample Characteristics.
        attributes.put(BatteryLevel_UUID, "Battery level");
        attributes.put(VolumeLevel_UUID, "Volume level");
        attributes.put(SoundCtrl_UUID, "Sound control");
        //attributes.put(MuteState_UUID, "system on/off");
        attributes.put(AudioMode_UUID, "Audio mode");
        attributes.put(AudioContinuous_UUID, "continuous audio structure");
        attributes.put(AudioStream1_UUID, "intermittent audio stream 1");
        attributes.put(AudioStream2_UUID, "intermittent audio stream 2");
        attributes.put(AudioBPM_UUID, "intermittent audio beats per minute");
        attributes.put(AudioPlayback_UUID, "audio playback file Id");
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
        readAbbiBatteryCharacteristic(BatteryLevel_UUID);
    }

    public static void readMainVolumeLevel () {
        readAbbiCustomCharacteristic(VolumeLevel_UUID);
    }

    public static void readSoundCtrlMode () {
        readAbbiCustomCharacteristic(SoundCtrl_UUID);
    }

    public static void readAudioMode () {
        readAbbiCustomCharacteristic(AudioMode_UUID);
    }

    public static void readWavFileId () {
        readAbbiCustomCharacteristic(AudioPlayback_UUID);
    }

    public static void readContinuousStream () {
        readAbbiCustomCharacteristic(AudioContinuous_UUID);
    }

    public static void readIntermittentStream (int i) {
        String AudioStream_UUID;
        if (i == ABBIIntermittentStream.Stream1)
            AudioStream_UUID = AudioStream1_UUID;
        else if (i == ABBIIntermittentStream.Stream2)
            AudioStream_UUID = AudioStream2_UUID;
        else
            return;

        readAbbiCustomCharacteristic(AudioStream_UUID);
    }

    public static void readIntermittentBPM () {
        readAbbiCustomCharacteristic(AudioBPM_UUID);
    }

    public static void readAccelerometer () {
        readAbbiMotionCharacteristic(Accelerometer_UUID);
    }

    public static void readGyroscope () {
        readAbbiMotionCharacteristic(Gyroscope_UUID);
    }

    public static void readMagnetometer () {
        readAbbiMotionCharacteristic(Magnetometer_UUID);
    }

    public static void readIMU () {
        readAbbiMotionCharacteristic(IMU_UUID);
    }

    //----------------------------------------------------------------------------

    public static void writeMainVolumeLevel (int x)
    {
        writeAbbiCharacteristic(VolumeLevel_UUID,
                x * 32767 / 15,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

    public static void writeSoundCtrlMode (int n)
    {
        writeAbbiCharacteristic(SoundCtrl_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeAudioMode (int id)
    {
        int n = ABBISoundMode.Intermittent;
        switch (id) {
            case R.id.radioSoundContinuous:
                n = ABBISoundMode.Continuous;
                break;
            case R.id.radioSoundIntermmitent:
                n = ABBISoundMode.Intermittent;
                break;
            case R.id.radioSoundPlayback:
                n = ABBISoundMode.Playback;
                break;
        }
        writeAbbiCharacteristic(AudioMode_UUID,
                n,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeWavFileId (int x)
    {
        writeAbbiCharacteristic(AudioPlayback_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public static void writeContinuousStream (int f, int v)
    {
        writeContinuousStream(new AudioContinuous(f,v));
    }

    public static void writeContinuousStream (AudioContinuous stream){
        writeAbbiCharacteristic(AudioContinuous_UUID,
                stream.getByteArray());
    }

    public static void writeIntermittentStream(int index, int f, int a, int s, int d, int v)
    {
        writeIntermittentStream(index, new AudioIntermittent(f, a, s, d, v));
    }

    public static void writeIntermittentStream(int index, AudioIntermittent stream){
        String AudioStream_UUID;
        if (index == ABBIIntermittentStream.Stream1)
            AudioStream_UUID = AudioStream1_UUID;
        else if (index == ABBIIntermittentStream.Stream2)
            AudioStream_UUID = AudioStream2_UUID;
        else
            return;

        writeAbbiCharacteristic(AudioStream_UUID, stream.getByteArray());
    }

    public static void writeIntermittentBPM (int x)
    {
        writeAbbiCharacteristic(AudioBPM_UUID,
                x,
                BluetoothGattCharacteristic.FORMAT_UINT16);
    }

}

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

class ABBISoundFiles{
    private static HashMap<Integer, String> _playlistDict = new HashMap<>();
    private static final String[] soundFiles = {"short_explosion.wav",
            "elephant.wav",
            "whistling.wav",
            "piano_hi_repeat.wav",
            "drops_hi_short.wav",
            "smash-wood.wav",
            "06_sonar_ping.wav",
            "drums.wav",
            "monster-roar.wav",
            "stones.wav",
            "dog.wav",
            "italian_bank-phone.wav",
            "Moquito-Buzzing.wav",
            "bubbling.wav",
            "horse_neigh.wav",
            "sheep-in-the-field.wav",
            "grandfather-clock.wav",
            "car-driving-away.wav",
            "footsteps.wav",
            "04_waves.wav",
            "wind.wav",
            "water_pouring.wav",
            "rocket.wav",
            "29_bird.wav"};

    static {
        int i = 0;
        for (String item : soundFiles) {
            _playlistDict.put(i, item);
            i++;
        }
    }

    public static String[] getList(){
        return soundFiles;
    }

    public static String lookup(int position) {
        return _playlistDict.get(position);
    }

}

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

class ABBISoundCtrl {
    public static final int SoundOff = 0;
    public static final int SoundTrigger = 1;
    public static final int SoundOn = 2;
}
class ABBISoundMode {
    public static final int Continuous = 0;
    public static final int Intermittent = 1;
    public static final int Playback = 2;
}

class ABBIIntermittentStream {
    public static final int Stream1 = 1;
    public static final int Stream2 = 2;
}

/*class ABBIConstants{
    public static final int MAX_VOLUME = 32767 * 2;

}*/

//----------------------------------------------------------------------------
//----------------------------------------------------------------------------

class AudioIntermittent extends AudioStream {
    int freq;   // Carrier frequency of the intermittent sound (500 - 4000 Hz)
    int attack;  // Rise time of the carrier wave (1000 - 999999 uS)
    int sustain; // Sustain period of the carrier wave (0 - 999999 uS)
    int decay;   // Fall time of the carrier wave (0 - 999999 uS)
    int volume;  // Local volume/amplitude of the carrier wave ()

    public AudioIntermittent(int freq, int attack, int sustain, int decay, int volume){
        this.freq = freq;
        this.attack = attack;
        this.sustain = sustain;
        this.decay = decay;
        this.volume = volume;
    }

    public AudioIntermittent(byte[] bytes){
        freq = pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        attack = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        sustain = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        decay = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        volume = ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray(){
        int pitch = frequencyToPitch(freq);
        return new byte[] {
                (byte)(pitch & 0xff), (byte)((pitch >> 8) & 0xff), (byte)((pitch >> 16) & 0xff), (byte)((pitch >> 24) & 0xff),
                (byte)(attack & 0xff), (byte)((attack >> 8) & 0xff), (byte)((attack >> 16) & 0xff), (byte)((attack >> 24) & 0xff),
                (byte)(sustain & 0xff), (byte)((sustain >> 8) & 0xff), (byte)((sustain >> 16) & 0xff), (byte)((sustain >> 24) & 0xff),
                (byte)(decay & 0xff), (byte)((decay >> 8) & 0xff), (byte)((decay >> 16) & 0xff), (byte)((decay >> 24) & 0xff),
                (byte)(volume & 0xff), (byte)((volume >> 8) & 0xff), 0, 0};
    }
}

class AudioContinuous extends AudioStream{
    int freq;   // Frequency of continuous sine wave (500-4000 Hz)
    int volume;  // Local volume.amplitude of the sine wave ()

    public AudioContinuous (int pitch, int volume){
        this.freq = pitch;
        this.volume = volume;
    }

    public AudioContinuous(byte[] bytes){
        freq = pitchToFrequency(ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        volume = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getByteArray(){
        int pitch = frequencyToPitch(freq);
        return new byte[] {
                (byte)(pitch & 0xff), (byte)((pitch >> 8) & 0xff), (byte)((pitch >> 16) & 0xff), (byte)((pitch >> 24) & 0xff),
                (byte)(volume & 0xff), (byte)((volume >> 8) & 0xff), 0, 0};
    }


}
abstract class AudioStream {
    static final int DAC_SAMPLING_RATE = 22050;

    int pitchToFrequency(int p) {
        return ((int) (1 / 4294967296.0 * ((float) (p)) * ((float) DAC_SAMPLING_RATE) + 0.5));
    }

    int frequencyToPitch(int f) {
        return ((int) (4294967296.0 * ((float) (f)) / ((float) DAC_SAMPLING_RATE) + 0.5));
    }

    abstract byte[] getByteArray();
}
