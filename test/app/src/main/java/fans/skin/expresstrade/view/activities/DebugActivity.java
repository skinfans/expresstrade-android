package fans.skin.expresstrade.view.activities;

import android.os.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;

public class DebugActivity extends AppActivity {

    public static String debugError = "-- debug --";
    public TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        setDisplayStatusbar(true);

        tx = findViewById(R.id.tx_debug);
        tx.setText(debugError);

        App.debugActivity = this;
    }
}
