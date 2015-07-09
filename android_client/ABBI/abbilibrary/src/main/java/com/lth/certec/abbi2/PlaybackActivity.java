package com.lth.certec.abbi2;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class PlaybackActivity extends ListActivity {


    private int _wavFileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, ABBISoundFiles.getList()));
        _wavFileId = ABBIGattAttributes.audioPlayback;
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setItemChecked(_wavFileId, true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_playback, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        ABBIGattAttributes.writeWavFileId(position);

        String t = ABBISoundFiles.lookup(position);
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
        //(ListView) findViewById (android.R.id.list).check (_wavFileId, true);

        /*Intent myIntent = new Intent (this, DeviceControlActivity.class);
        //myIntent.PutExtra ("SelectedWavFile", _wavFileId);
        setResult(RESULT_OK, myIntent);
        finish();*/
    }
}
