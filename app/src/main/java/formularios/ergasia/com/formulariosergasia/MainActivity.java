package formularios.ergasia.com.formulariosergasia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaring main_activity.xml objects to play with them later
        final TextView message = (TextView) findViewById(R.id.textView);
        final EditText input = (EditText) findViewById(R.id.editText);
        final Button submit = (Button) findViewById(R.id.button);

        //Hide input EditText and sumbit Button (they may not be useful at all)
        input.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);

        //First of all, let's check that the app user is connected to Internet
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {

            //Let's instantiate an object from the class ErgasiaUser
            final SharedPreferences settings = getSharedPreferences("ErgasiaUserInfo", 0);
            final ErgasiaUser user = new ErgasiaUser();
            user.assignPhoneNumber(null, settings);
            String numtelf = user.returnPhoneNumber();

            if (numtelf == null) {

                message.setText("Por favor, conecte la tarjeta SIM al móvil o desactive el modo avión y reinicie la aplicación para continuar.");

            //How sad... the user's SIM card does not store its phone number... nor is it saved in the app's preferences
            } else if (numtelf.equals("")) {

                message.setText("Ha sido imposible obtener el número de teléfono de la SIM de su móvil. " +
                            "Por favor, introdúzcalo para continuar:");

                input.setVisibility(View.VISIBLE);
                submit.setVisibility(View.VISIBLE);

                //Setting a listener for the submit Button
                submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (input.getText().toString() != null && TextUtils.isDigitsOnly(input.getText().toString()) == true) {
                            user.assignPhoneNumber(input.getText().toString(), settings);
                            user.checkUserStatus(settings);

                            if (user.returnUserStatus() == 0) {

                                message.setText("Su número de teléfono no está registrado en la base de datos de Ergasia. " +
                                            "Por favor, contacte con Ergasia e inténtelo de nuevo más tarde.");

                            } else if (user.returnUserStatus() == 1) {

                                user.sendUserToForm(message, input, submit);

                            }
                        }
                    }
                });

            //Wow!! We either got the telephone number from the SIM card or from the preferences!
            } else {

                user.assignPhoneNumber(numtelf, settings);
                user.checkUserStatus(settings);

                if (user.returnUserStatus() == 0) {

                    message.setText("Su número de teléfono no está registrado en la base de datos de Ergasia. " +
                                "Por favor, contacte con Ergasia e inténtelo de nuevo más adelante.");

                } else if (user.returnUserStatus() == 1) {

                    user.sendUserToForm(message, input, submit);

                }
            }

        //Wait! Internet connectivity is needed to run this application.
        } else {

            message.setText("Para que la aplicación pueda operar correctamente, debe estar conectado a Internet (vía Wifi o red de datos). " +
                        "Por favor, conéctese a Internet y reinicie la aplicación.");
        }

    }

    //ERGASIA USER CLASS
    public class ErgasiaUser {

        private String phonenum;
        private int status;
        private int form;

        //Assigns a phone number to the Ergasia User
        public void assignPhoneNumber(String inputnum, SharedPreferences settings) {

            String settings_NumTelf = settings.getString("ErgasiaUserPhone", "").toString();

            if (settings_NumTelf == null || settings_NumTelf.equals("")) {

                if (inputnum == null) {

                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    phonenum = tm.getLine1Number();

                } else {

                    phonenum = inputnum;

                }

            } else {

                phonenum = settings_NumTelf;

            }

        }

        //Retrieves the current assigned user to the Ergasia User
        public String returnPhoneNumber() {
            return phonenum;
        }

        //Checks the status of an Ergasia User
        public void checkUserStatus(SharedPreferences settings) {

            String resource = "http://192.168.1.40/usr/check_user.php";
            String charset = "UTF-8";
            String param = phonenum;

            URL url;
            HttpURLConnection conn;
            InputStream is;

            try {

                String query = String.format("telf=%s", URLEncoder.encode(param, charset));

                try {

                    url = new URL(resource + "?" + query);

                    try {

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);

                        try {

                            conn.setRequestMethod("GET");
                            conn.setDoInput(true);

                            try {

                                conn.connect();
                                int response = conn.getResponseCode();
                                is = conn.getInputStream();

                                //Convert the InputStream into a string
                                Reader reader = new InputStreamReader(is, "UTF-8");
                                char[] buffer = new char[13];
                                reader.read(buffer);
                                String result = new String(buffer);


                                try {

                                    JSONObject parentObject = new JSONObject(result);
                                    status = Integer.parseInt(parentObject.getString("result"));

                                    if (status == 1) {
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("ErgasiaUserPhone", phonenum.toString());
                                        editor.commit();
                                    }

                                } catch (JSONException e){
                                    throw new RuntimeException(e);
                                }

                            }  catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        } catch (ProtocolException e) {
                            throw new RuntimeException(e);
                        }

                    } catch (IOException e){
                        throw new RuntimeException(e);
                    }

                } catch (MalformedURLException e){
                    throw new RuntimeException(e);
                }

            } catch (UnsupportedEncodingException e){
                throw new RuntimeException(e);
            }

        }

        public int returnUserStatus() {
            return status;
        }

        public void sendUserToForm(TextView message, EditText input, Button submit) {
            message.setText("Formulario cargado correctamente. Para volverlo a cargar, reinicie la aplicación.");
            input.setVisibility(View.INVISIBLE);
            submit.setVisibility(View.INVISIBLE);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.40/usr/get_form.php?telf="+phonenum));
            startActivity(browserIntent);
        }

    }
}