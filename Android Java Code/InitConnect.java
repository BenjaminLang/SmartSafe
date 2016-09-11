package androidapp.elec291group.smartsafe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

public class InitConnect extends AppCompatActivity {
    private String userIP;
    private int userPort;
    private Button connectButton;
    private Button defaultButton;
    private EditText enteredIP;
    private EditText enteredPort;
    private Toolbar toolbar;
    private String defaultIP = "192.168.43.168";
    private int defaultPort = 42060;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_connect);

        connectButton = (Button) findViewById(R.id.connectButton);
        defaultButton = (Button) findViewById(R.id.defaultButton);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDataBridges bridge = MyDataBridges.getInstance();
                enteredIP = (EditText) findViewById(R.id.ipField);
                userIP = enteredIP.getText().toString();
                bridge.setIP(userIP);
                enteredPort = (EditText) findViewById(R.id.portField);
                userPort = Integer.parseInt(enteredPort.getText().toString());
                bridge.setPort(userPort);

                bridge.setConnectionState(true);
                MainActivity.toggleButtons(bridge.getConnectionState());
                MainActivity.startConnection();
                Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                MyDataBridges bridge = MyDataBridges.getInstance();
                bridge.setIP(defaultIP);
                bridge.setPort(defaultPort);

                bridge.setConnectionState(true);
                MainActivity.toggleButtons(bridge.getConnectionState());
                MainActivity.startConnection();
                Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
