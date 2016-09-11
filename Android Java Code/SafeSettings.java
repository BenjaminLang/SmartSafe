package androidapp.elec291group.smartsafe;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Map;

import UBC.G8.Message.ChangeSettingsMessage;
import UBC.G8.Message.Message;
import UBC.G8.Message.ResponseCode;
import UBC.G8.Message.ResponseMessage;
import UBC.G8.Message.SettingType;
import UBC.G8.Message.SettingsResponse;
import UBC.G8.Message.StatusResponse;

/**
 * Created by Ben on 2016-04-03.
 */
public class SafeSettings extends AppCompatActivity {
    private ToggleButton autolockToggle;
    private ToggleButton motionToggle;
    private EditText newPwTxt;
    private Button setPw;
    private Spinner triesSpinner;
    private Button update;
    private final String[] triesSelection = new String[] {"1", "2", "3", "4", "5"};
    private boolean autoLockStat;
    private boolean motionStat;
    private int triesStat;

    private boolean autolock;
    private boolean motion;
    private String newPassword;
    private int numTries;

    private boolean autolockFlag =  false;
    private boolean motionFlag = false;
    private boolean newpasswordFlag = false;
    private boolean numTriesFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safe_settings);

        autolockToggle = (ToggleButton) findViewById(R.id.autolockToggle);
        motionToggle = (ToggleButton) findViewById(R.id.motionToggle);
        setPw = (Button) findViewById(R.id.setPwButton);
        update = (Button) findViewById(R.id.updateButton);
        triesSpinner = (Spinner) findViewById(R.id.spinner);
        newPwTxt = (EditText) findViewById(R.id.passwordText);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(SafeSettings.this, android.R.layout.simple_spinner_item, triesSelection);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triesSpinner.setAdapter(spinnerAdapter);

        new safeStatusCheck().execute();

        autolockToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autolock = isChecked;
                autolockFlag = true;
            }
        });

        motionToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                motion = isChecked;
                motionFlag = true;
            }
        });


        setPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword = newPwTxt.getText().toString();
                newpasswordFlag = true;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update.setEnabled(false);
                new changeSettings().execute();
                finish();
            }
        });


        triesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                numTries = Integer.parseInt(triesSelection[position]);
                numTriesFlag = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }
        });

    }

    class changeSettings extends AsyncTask<Void, Void, ResponseMessage>{
        @Override
        protected ResponseMessage doInBackground(Void[] idk){
            ChangeSettingsMessage outGoingMsg;
            ResponseMessage response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoingMsg = MessageFactory.buildSettingMessage(bridge.getCurrSafeID());
            if(autolockFlag) {
                outGoingMsg.setAutolock(autolock);
                autolockFlag = false;
            }
            if(motionFlag){
                outGoingMsg.setAlarmEnable(motion);
                motionFlag = false;
            }
            if(newpasswordFlag){
                outGoingMsg.setNewPassword(newPassword);
                newpasswordFlag = false;
            }
            if(numTriesFlag) {
                outGoingMsg.setNewAttempts(numTries);
                numTriesFlag = false;
            }
            bridge.sendOutGoing(outGoingMsg);
            response = (ResponseMessage)bridge.retrieveInComing();
            return response;
        }

        @Override
        protected void onPostExecute(ResponseMessage response){
            ResponseParse.parseSettingResponse(response, getApplicationContext());
            update.setEnabled(true);
        }

    }

    class safeStatusCheck extends AsyncTask<Void, Void, ResponseMessage>{
        @Override
        protected ResponseMessage doInBackground(Void[] idk){
            Message outGoingMsg;
            ResponseMessage response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            outGoingMsg = MessageFactory.buildStatusRequest(bridge.getCurrSafeID());
            bridge.sendOutGoing(outGoingMsg);
            response = (ResponseMessage)bridge.retrieveInComing();
            return response;
        }
        @Override
        protected void onPostExecute(ResponseMessage response){
            autoLockStat = ((StatusResponse)response).isAutolock();
            motionStat = ((StatusResponse)response).isMotion();
            triesStat = ((StatusResponse)response).getNumTries();
            autolockToggle.setChecked(autoLockStat);
            motionToggle.setChecked(motionStat);
            triesSpinner.setSelection(triesStat-1);
        }
    }

}
