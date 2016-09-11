package androidapp.elec291group.smartsafe;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import UBC.G8.Message.*;

public class AddSafe extends AppCompatActivity {

    private Button addButton;
    private String newSafeId;
    private Toolbar toolbar;
    private EditText enteredId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_safe);

        addButton = (Button) findViewById(R.id.addButton);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredId = (EditText) findViewById(R.id.idField);
                newSafeId = enteredId.getText().toString();
                addButton.setEnabled(false);
                new addSafeRequest().execute(newSafeId);
            }
        });
    }

    class addSafeRequest extends AsyncTask<String, Void, ResponseMessage>{
        @Override
        protected ResponseMessage doInBackground(String[] safeId){
            Message outGoing = MessageFactory.buildAddSafeMessage(safeId[0]);
            ResponseMessage response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            bridge.sendOutGoing(outGoing);
            response = (ResponseMessage)bridge.retrieveInComing();
            return response;
        }

        @Override
        protected void onPostExecute(ResponseMessage response){
            addButton.setEnabled(true);
            if(response.getResponse().equals(ResponseCode.SUCCESS) ){
                Toast.makeText(getApplicationContext(), "Safe Added", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                //String toastTxt = ResponseParse.parseResponse(response);
                //Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_SHORT).show();
            }
        }

    }
}
