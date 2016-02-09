package formularios.ergasia.com.formulariosergasia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaring main_activity.xml objects to play with them later
        TextView message = (TextView)findViewById(R.id.textView);
        EditText input = (EditText)findViewById(R.id.editText);
        Button submit = (Button)findViewById(R.id.button);

        //Hide input EditText and sumbit Button (they may not be useful at all)
        input.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);

        Telephone telf = new Telephone();
        String numtelf = telf.number;

        if (numtelf == null)
        {
            message.setText("Por favor, conecte la tarjeta SIM al móvil o desactive el modo avión y reinicie la aplicación para continuar.");
        }
        //How sad... the user's SIM card does not store its phone number
        else if (numtelf.equals(""))
        {
            message.setText("Ha sido imposible obtener el número de teléfono de la SIM de su móvil. " +
                    "Por favor, introdúzcalo para continuar:");

            input.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);

            //Setting a listener for the submit Button
            submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                }
            });

        }
        //Woww!! We got the telephone number from the SIM card!
        else
        {
            message.setText(numtelf);
        }
    }

    class Telephone {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
    }
}
