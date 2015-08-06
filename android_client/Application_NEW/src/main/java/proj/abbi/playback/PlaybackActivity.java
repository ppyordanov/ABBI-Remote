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

package proj.abbi.playback;


import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import proj.abbi.R;
import uk.ac.gla.abbi.abbi_library.AboutDialogue;
import uk.ac.gla.abbi.abbi_library.gatt_communication.ABBIGattReadWriteCharacteristics;
import uk.ac.gla.abbi.abbi_library.utilities.Globals;
import uk.ac.gla.abbi.abbi_library.utilities.UtilityFunctions;

/**
 * This class represents the activity associated with the playback sound emission.
 * It uses a ListView to display possible playback options.
 */

public class PlaybackActivity extends ListActivity {


    private int wavFileName;

    /**
     * Load the playback .wav file list and depending on the current ABBI bracelet's settings:
     * - check the filename on the ListView
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, Globals.SOUND_FILES_PLAYLIST));
        wavFileName = Globals.audioPlayback;
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setItemChecked(wavFileName, true);

    }


    /**
     * Create the ActionBar menu
     *
     * @param menu the menu instance
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Trigger a relevant action depending on which ActionBar menu item was interacted with.
     *
     * @param item the selected MenuItem is passed as an input parameter
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_info) {
            AboutDialogue.show(PlaybackActivity.this, getString(R.string.about),
                    getString(R.string.close));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When a list item from the playlist has been selected:
     * - trigger a call to {@link ABBIGattReadWriteCharacteristics#writeWavFileId(int)} in order to update the playback file id
     * - trigger a call to {@link UtilityFunctions#lookUpFileNameBasedOnIndex(int, String[])} to retrieve the file name
     * - generate a Toast instance, displaying the file name to the user
     *
     * @param l        the current ListView instance
     * @param v        the current View instance
     * @param position the position of the selected list item
     * @param id       the selected list item's id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ABBIGattReadWriteCharacteristics.writeWavFileId(position);

        String t = UtilityFunctions.lookUpFileNameBasedOnIndex(position, Globals.SOUND_FILES_PLAYLIST);
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();

    }
}
