package formularios.ergasia.com.formulariosergasia;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaring main_activity.xml objects to play with them later
        TextView message = (TextView)findViewById(R.id.textView);
        final EditText input = (EditText)findViewById(R.id.editText);
        Button submit = (Button)findViewById(R.id.button);

        //Hide input EditText and sumbit Button (they may not be useful at all)
        input.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);

        //First of all, let's check that the app user is connected to Internet
        ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected())
        {
            //Let's instantiate an object from the class ErgasiaUser
            final ErgasiaUser user = new ErgasiaUser();
            user.assignPhoneNumber(null);
            String numtelf = user.returnPhoneNumber();

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
                        if (input.getText().toString() != null && TextUtils.isDigitsOnly(input.getText().toString()) == true)
                        {
                            user.assignPhoneNumber(input.getText().toString());
                        }
                    }
                });

            }
            //Wow!! We got the telephone number from the SIM card!
            else
            {
                message.setText(numtelf);
            }
        }
        else
        {
            message.setText("Para que la aplicación pueda operar correctamente, debe estar conectado a Internet (vía Wifi o red de datos). " +
                    "Por favor, conéctese a Internet y reinicie la aplicación.");
        }

    }

    public class ErgasiaUser {

        private String phonenum;
        private int status;
        private int form;

        public void assignPhoneNumber(String inputnum) {

            if (inputnum == null) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                phonenum = tm.getLine1Number();
            }
            else
            {
                phonenum = inputnum;
            }

        }

        public String returnPhoneNumber() {
            return phonenum;
        }

    }
}
