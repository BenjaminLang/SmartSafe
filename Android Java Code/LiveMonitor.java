package androidapp.elec291group.smartsafe;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class LiveMonitor extends AppCompatActivity {

    private Toolbar toolbar;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_monitor);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int clickId = item.getItemId();

        switch (clickId) {
            case R.id.init_connection:
                startActivity(new Intent(LiveMonitor.this, InitConnect.class));
                break;
            case R.id.add_safe:
                startActivity(new Intent(LiveMonitor.this, AddSafe.class));
                break;
            case R.id.settings:
                startActivity(new Intent(LiveMonitor.this, SafeSettings.class));
                break;
            case R.id.about:
                startActivity(new Intent(LiveMonitor.this, About.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
