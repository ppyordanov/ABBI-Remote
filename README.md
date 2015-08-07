# abbi
An Android application built atop a custom library module ( 'abbi_library' ) used to control the ABBI bracelet developed by IIT (Italian Institute of Technology). The project will follow the client-server software development paradigm. The server is going to be tied to a central data store and communicate with the Android app via a REST API.

The general structure of the Android ABBI library follows below:

- the package "gatt_communication" contains all of the Java classes used to interact with the bracelet, transmitting commands for continuous/ intermittent sound playback, etc, including the funcitons used to read and write GATT characteristics on the bracelet via a bluetooth connection (r/w battery, volume status and more)

- the "utilities" package contains the following utility classes:

 - Globals.java - containing constants and default values for frequency, volume level and other indicating variables
 - UtilityFunctions.java - some conversion functions (used for normalization purposes)
 - UUIDConstants.java - contains the UUID key constants for ABBI bluetooth communication

- BluetoothLeService.java is essentially Hector's class used to facilitate the process of reading and writing GATT characteristics to and from ABBI
- AboutDialogue.java is a helper class enabling developers to specify the aim of their application and include an About button in the ActionBar Android menu

Source: https://github.com/ppyordanov/ABBI-Remote
