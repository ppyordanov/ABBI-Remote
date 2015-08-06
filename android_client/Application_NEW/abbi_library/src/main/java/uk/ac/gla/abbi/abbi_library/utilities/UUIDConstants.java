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


package uk.ac.gla.abbi.abbi_library.utilities;

/**
 * This class stores the UUID constants
 * (denoting the protocols used by the Android application to connect to and communicate with the ABBI bracelet).
 * The variable names have been optimized to be as self-explanatory as possible.
 * <p/>
 * Created by Peter Yordanov on 9.7.2015 ã..
 */
public class UUIDConstants {

    /**
     * Service and characteristics UUIDs
     */
    public static final String BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BATTERY_LEVEL_UUID = "00002a19-0000-1000-8000-00805f9b34fb";

    public static final String CUSTOM_SERVICE_UUID = "624e957f-cb42-4cd6-bacc-84aeb898f69b";
    public static final String VOLUME_LEVEL_UUID = "e4c937b3-7f6d-41f9-b997-40c561f4453b";
    public static final String SOUND_CONTROL_UUID = "df342b03-53f9-43b4-acb6-62a63ca0615a";
    public static final String AUDIO_STREAM_1_UUID = "3709a55c-e4e2-43c1-9248-5d07da39630c";
    public static final String AUDIO_STREAM_2_UUID = "f63a51f9-ae25-480a-b7f1-ff88a8afb15a";
    public static final String AUDIO_MODE_UUID = "b3dc2fd0-5950-4e19-8623-a34d3d8330ee";
    public static final String AUDIO_BPM_UUID = "46906ff7-ca01-4353-87fb-510ed7a59d62";
    public static final String AUDIO_CONTINUOUS_UUID = "56025b6b-7642-42e9-9233-4e5bf2551ce2";
    public static final String AUDIO_PLAYBACK_UUID = "97d02b24-bbe4-44c3-b2ba-3049a654d9ed";

    /**
     * Statistics UUIDs
     */
    public static final String AWAKE_TIME_UUID = "0dc3add1-bfe2-42b5-9545-66fbbe49910f";
    public static final String RTC_UUID = "38302d33-6fb3-4c2e-8276-1e7ccece9d1c";
    public static final String TiRGGER_UUID = "98903107-c6f8-43e0-b6c2-7de391832a8a";
    public static final String SERIAL_NUMBER_UUID = "70b867f6-84ac-4e79-b24c-95e04192772e";
    public static final String DEVICE_ID_UUID = "fda36bfe-ef6f-4277-9d45-56613cd1ac4a";
    public static final String SYSTEM_ON_OFF_UUID = "0ab23985-8ebe-4fa4-9533-650a03dc747d";

    /**
     * Logging UUIDs
     */
    public static final String LOGGING_SERVICE_UUID = "51d4a419-282f-41ca-a6b2-87cb6efb4200";
    public static final String LOG_POINTER_UUID = "3539a442-fc2a-4082-91b9-45e8439641f7";
    public static final String ERASE_FLASH_UUID = "057b90e4-8f03-4f9b-a3bc-12c60a05b6ef";
    public static final String DATA_BLOB_UUID = "4cb2f3e6-f263-4105-94a4-19fa4fdce0cc";

    /**
     * Motion UUIDs
     */
    public static final String MOTION_SERVICE_UUID = "51d4a419-0004-0000-0000-87cb6efb4200";
    public static final String ACCELEROMETER_UUID = "51d4a419-0004-0001-0000-87cb6efb4200";
    public static final String GYROSCOPE_UUID = "51d4a419-0004-0002-0000-87cb6efb4200";
    public static final String MAGNETOMETER_UUID = "51d4a419-0004-0003-0000-87cb6efb4200";
    public static final String IMU_UUID = "51d4a419-0004-0004-0000-87cb6efb4200";
    public static final String IMU_CONFIG_UUID = "51d4a419-0004-0005-0000-87cb6efb4200";
    public static final String IMU_PERIOD_UUID = "51d4a419-0004-0006-0000-87cb6efb4200";


}
