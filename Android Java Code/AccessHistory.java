package androidapp.elec291group.smartsafe;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import UBC.G8.Message.HistoryResponse;
import UBC.G8.Message.Message;

public class AccessHistory extends AppCompatActivity {

    private Toolbar toolbar;
    private ArrayList<String> history = new ArrayList<String>();
    private ArrayAdapter<String> historyItems;
    private ListView historyList;
    private final String Item1 = "access_action";
    private final String Item2 = "access_time";
    private final String[] mapKey = new String[] {Item1, Item2};
    private final int[] layoutId = new int[] {android.R.id.text1, android.R.id.text2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_history);

        //create toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new checkHistory().execute();   //execute check history async task
    }

    class checkHistory extends AsyncTask<Void, Void, HistoryResponse>{
        @Override
        protected HistoryResponse doInBackground(Void[] idk){
            Message outGoing;
            HistoryResponse response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoing = MessageFactory.buildHistoryMessage(bridge.getCurrSafeID());  //construct outgoing message
            bridge.sendOutGoing(outGoing);  //send outgoing message
            response = (HistoryResponse) bridge.retrieveInComing(); //retrieve response message
            return response;
        }

        @Override
        protected void onPostExecute(HistoryResponse response){
            if(response.getLogs().isEmpty()){
                Toast.makeText(getApplicationContext(), "No Accesses", Toast.LENGTH_SHORT).show();
                return;
            }
            //execute if log has contents
            else{
                history = new ArrayList<String>(response.getLogs());
                historyItems = new ArrayAdapter<String>(AccessHistory.this, android.R.layout.simple_list_item_1, history);  //build adapter from arraylist containing access history
                //define list view and set adapter
                Collections.reverse(history);
                historyItems = new ArrayAdapter<String>(AccessHistory.this, android.R.layout.simple_list_item_1, history);
                historyList = (ListView) findViewById(R.id.historyList);
                historyList.setAdapter(historyItems);
                Toast.makeText(getApplicationContext(), "Accesses Updated", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    //create toolbar dropdown menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int clickId = item.getItemId();

        //start activity based on dropdown selection
        switch (clickId){
            case R.id.init_connection:
                startActivity(new Intent(AccessHistory.this, InitConnect.class));
                break;
            case R.id.add_safe:
                startActivity(new Intent(AccessHistory.this, AddSafe.class));
                break;
            case R.id.about:
                startActivity(new Intent(AccessHistory.this, About.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
