package androidapp.elec291group.smartsafe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Unlocked extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlocked);

        // ActionBar actionBar = getActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    //  protected boolean onOptionsItemsSelected(MenuItem item){
    //    Intent goBack = new Intent(getApplicationContext(), MainActivity.class);
    //  startActivityForResult(goBack, 0);
    // return true;
    //}
}
