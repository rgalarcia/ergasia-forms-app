package formularios.ergasia.com.formulariosergasia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Telephone telf = new Telephone();
        String numtelf = telf.number;

        TextView text = (TextView)findViewById(R.id.TextView);
        text.setText(numtelf);
    }

    class Telephone {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
    }
}
