package example.chatea.servicecamera;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class myVideoList extends ActionBarActivity {

    private ListView lv_myVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_video_list);
        lv_myVideoList = (ListView) findViewById(R.id.my_video_listview);
        ListAdapter adapter = new SimpleAdapter(
                    this, myVideoList,
                    R.layout.my_video_list_item, new String[] { PreferSet.TAG_CALLED_RECORD_NUMBER, PreferSet.TAG_CALLED_RECORDS_TIME}
                    , new int[] { R.id.my_videoview_item});
            lv_myVideoList.setAdapter(adapter);
    }
}
