package androidapp.elec291group.smartsafe;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import UBC.G8.Message.*;


public class PasswordUnlock extends AppCompatActivity {

    private String password;
    private Button backButton;
    private Button passwordButton;
    private TextView passwordTxt;
    private EditText textPassword;
    private Toolbar toolbar;
    private boolean lockStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_unlock);

        backButton = (Button) findViewById(R.id.backButton);
        passwordButton = (Button) findViewById(R.id.passwordButton);
        passwordTxt = (TextView) findViewById(R.id.passwordTxt);
        textPassword = (EditText) findViewById(R.id.passwordField);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PasswordUnlock.this, MainActivity.class));
                finish();
            }
        });

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textPassword = (EditText) findViewById(R.id.passwordField);
                password = textPassword.getText().toString();
                passwordButton.setEnabled(false);

                new CheckPassword().execute(password);
            }
        });


    }

   class CheckPassword extends AsyncTask<String, Void, Message>{
        @Override
        protected Message doInBackground(String[] password){
            Message answer;
            Message response;
            MyDataBridges bridge = MyDataBridges.getInstance();
            answer = MessageFactory.buildUnlockMessage(password[0], bridge.getCurrSafeID());
            bridge.sendOutGoing(answer);
            response = bridge.retrieveInComing();
            return response;
        }

        @Override
        protected void onPostExecute(Message response){
            passwordButton.setEnabled(true);
            MainActivity.safeButtonState(false);
            String toastTxt = ResponseParse.ParseMessage((ResponseMessage)response);
            Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int clickId = item.getItemId();

        switch (clickId) {
            case R.id.init_connection:
                startActivity(new Intent(PasswordUnlock.this, InitConnect.class));
                break;
            case R.id.add_safe:
                startActivity(new Intent(PasswordUnlock.this, AddSafe.class));
                break;
            case R.id.about:
                startActivity(new Intent(PasswordUnlock.this, About.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}


