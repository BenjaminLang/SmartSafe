package androidapp.elec291group.smartsafe;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import UBC.G8.Message.*;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    private static Button Password;
    private static Button History;
    private static Button lockButton;
    private Toolbar toolbar;
    private ArrayList<String> safeID = new ArrayList<String>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ArrayAdapter<String> listAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private boolean lockStatus;
    private static Client commsClient;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define buttons
        Password = (Button) findViewById(R.id.toPassword);
        History = (Button) findViewById(R.id.toHistory);
        lockButton = (Button) findViewById(R.id.toLock);

        //grey out buttons depending on whether or not connection has been established
        MyDataBridges bridge = MyDataBridges.getInstance();
        toggleButtons(bridge.getConnectionState());

        //initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize drawer with empty list and set an item click listener
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        listAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_items, safeID);
        drawerList.setAdapter(listAdapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        //define activities to perform on drawer open and drawer close
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //nothing happens
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //execute CheckSafeList async activity
                new CheckSafeList().execute();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);   //add drawer toggle to drawer listener

        //start PasswordUnlock when password button is pressed
        Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PasswordUnlock.class));
            }
        });

        //start AccessHistory when history button is pressed
        History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccessHistory.class));
            }
        });

        //execute sendLockingRequest async activity when lock button is pressed
        lockButton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                lockButton.setEnabled(false);
                new sendLockingRequest().execute();
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //async activity to check the list of safes
    class CheckSafeList extends AsyncTask<Void, Void, DrawerResponseMessage>{
        @Override
        protected DrawerResponseMessage doInBackground(Void[] nothing){
            Message outGoing;
            DrawerResponseMessage response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoing = MessageFactory.buildSafeMessageList();   //construct outgoing msg
            bridge.sendOutGoing(outGoing);  //send outgoing msg
            response = (DrawerResponseMessage) bridge.retrieveInComing();   //receive response msg
            return response;
        }

        @Override
        protected  void onPostExecute(DrawerResponseMessage answer){
            //if there are no safes return
            if(answer.getSafes().isEmpty()){
                return;
            }
            //if there are safes set list of safe IDs to the ones contained in safe list and populate the drawer with the safes
            else {
                safeID = new ArrayList<String>(answer.getSafes());
                listAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.drawer_list_items, safeID);
                drawerList.setAdapter(listAdapter);
                drawerList.setOnItemClickListener(new DrawerItemClickListener());
                return;
            }
        }
    }

    //async activity to send a locking request to a safe
    class sendLockingRequest extends AsyncTask<Void, Void, Message>{
        @Override
        protected Message doInBackground(Void[] idk){
            Message outGoingMsg;
            Message response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoingMsg = MessageFactory.buildLockMessage(bridge.getCurrSafeID());  //construct outgoing msg
            bridge.sendOutGoing(outGoingMsg);   //send outgoing msg
            response = bridge.retrieveInComing();   //receive incoming msg
            return response;
        }

        @Override
        protected void onPostExecute(Message response){
            //use a toast message to inform user whether or not the locking operation was successful
            String toastTxt = ResponseParse.ParseMessage((ResponseMessage) response);
            Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //async activity to check whether or not the safe is locked
    class lockStatusCheck extends AsyncTask<Void, Void, Message>{
        @Override
        protected Message doInBackground(Void[] idk){
            Message outGoingMsg;
            Message response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoingMsg = MessageFactory.buildStatusRequest(bridge.getCurrSafeID());    //construct outgoing msg
            bridge.sendOutGoing(outGoingMsg);   //send outgoing msg
            response = bridge.retrieveInComing();   //receive incoming msg
            return response;
        }
        @Override
        protected void onPostExecute(Message response){
            //set lock status to the one on the server and grey out either unlock or lock button based on the status
            lockStatus = ((StatusResponse)response).isLocked();
            safeButtonState(lockStatus);
        }
    }

    //populate dropdown menu on taskbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://androidapp.elec291group.smartsafe/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://androidapp.elec291group.smartsafe/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //start activity based on which option in the drop down menu or toolbar was chosen
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int clickId = item.getItemId();

        switch (clickId){
            //start InitConnect if Initialize Server Connection was chosen
            case R.id.init_connection:
                startActivity(new Intent(MainActivity.this, InitConnect.class));
                break;
            //start AddSafe if Add an Additional Safe waas chosen
            case R.id.add_safe:
                startActivity(new Intent(MainActivity.this, AddSafe.class));
                break;
            //start SafeSettings if Settings was chosen
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SafeSettings.class));
                break;
            //start about if About was chosen
            case R.id.about:
                startActivity(new Intent(MainActivity.this, About.class));
                break;
            //refresh the lock status if the refresh icon was chosen
            case R.id.refresh:
                new lockStatusCheck().execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //listenes to drawer item slections
    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id){
            drawerList.setItemChecked(position, true);  //highlight item that was clicked
            getSupportActionBar().setTitle(safeID.get(position));   //set the title of the toolbar to the selected safe id
            MyDataBridges bridge = MyDataBridges.getInstance();
            bridge.setCurrSafeID(safeID.get(position)); //set the current/target safe id to the one selected
            new lockStatusCheck().execute();    //check the lock status on the safe
            drawerLayout.closeDrawers();    //close the drawer after selection
        }
    }

    public static void toggleButtons(boolean connectionSet){
        //enable all buttons if connection was established
        if(connectionSet){
            lockButton.setEnabled(true);
            Password.setEnabled(true);
            History.setEnabled(true);
        }
        //grey out all buttons if connection wasn't established
        else{
            lockButton.setEnabled(false);
            Password.setEnabled(false);
            History.setEnabled(false);
        }
    }

    public static void safeButtonState(boolean lockState){
        //enable unlock and disable lock if the safe is locked
        if(lockState){
            lockButton.setEnabled(false);
            Password.setEnabled(true);
        }
        //enable lock and disable unlock if the safe is unlocked
        else{
            lockButton.setEnabled(true);
            Password.setEnabled(false);
        }
    }

    //sets up client connection
    public static void startConnection(){
        MyDataBridges bridge = MyDataBridges.getInstance();

        if(!bridge.getThreadRunning()){
            try{
                System.out.println("setting up new client thread");
                commsClient = new Client();
                bridge.setThreadRunning();

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
