package com.example.cookomaticpda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.LoginTuple;
import org.milaifontanals.cookomatic.model.sala.Cambrer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    // moure d'aqui
//    private final String SRVIP = "192.168.1.108";
//    private final String SRVIP = "192.168.1.105";



    private Button btnStart;
    private EditText edtLogin, edtPassword;
    private TextView txvSessionId;

    // Login
    private LoginTuple loginTuple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnStart = findViewById(R.id.btnStart);
        edtLogin = findViewById(R.id.edtLogin);
        edtPassword = findViewById(R.id.edtPassword);
        txvSessionId = findViewById(R.id.txvSessionId);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = edtLogin.getText().toString();
                String password = edtPassword.getText().toString();

                String loginSP = getFromSharedPreferences("login");
                String passwordSP = getFromSharedPreferences("password");

                Log.d("LOGIN", "loginSP=" + loginSP + "\tpasswordSP=" + passwordSP);

                if (loginSP.isEmpty() || passwordSP.isEmpty()) {
                    // primer login, ens guardem aquestes credencials
                    saveLoginSharedPreferences(login, password);
                    Toast.makeText(getApplicationContext(), "Login i contrasenya registrats correctament", Toast.LENGTH_SHORT).show();

                } else {
                    // Crida assíncrona per enviar credencials al servidor
                    Observable.fromCallable(() -> {
                        //---------------- START OF THREAD ------------------------------------
                        // Això és el codi que s'executarà en un fil

                        // demanem al server un logintuple que conté session id
                        // si credencials correctes, ens el retorna
                        // altrament, obtenim null
                        return userLogin();
                        //--------------- END OF THREAD-------------------------------------
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((newLoginTuple) -> {
                                //-------------  UI THREAD ---------------------------------------
                                // El codi que tenim aquí s'executa només quan el fil
                                // ha acabat !! A més, aquest codi s'executa en el fil
                                // d'interfície gràfica.
//                                setLoading(false); // TODO: progressbar
                                loginTuple = newLoginTuple;
                                if (loginTuple == null) {
                                    // Login incorrecte (login o contrasenya incorrectes), no deixem entrar
                                    Toast.makeText(getApplicationContext(), "Login o contrasenya incorrectes, o cambrer no consta en la BD", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Login correcte o primer login, deixem entrar
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    // passem loginTuple a l'altra activity
                                    intent.putExtra("loginTuple", loginTuple);
                                    startActivity(intent);
                                }
                                //-------------  END OF UI THREAD ---------------------------------------
                            });
                }
            }
        });

        // VEGEU: https://programacionymas.com/series/app-android-sobre-registro-de-inventarios/login-usando-sharedpreferences
    }


    private void saveLoginSharedPreferences(String login, String password) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Guardar la preferencia
        editor.putString("login", login);
        editor.putString("password", password);
//        editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
        editor.apply();
    }

    private String getFromSharedPreferences(String key) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        String value = sharedPref.getString(key, "");
        // si no troba la propietat, retorna defValue (en aquest cas "")
//        int defaultValue = getResources().getInteger(R.integer.saved_high_score_default_key);
//        int highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
        return value;
    }

    // S'executa dins d'un THREAD
    private LoginTuple userLogin() {
        boolean credencialsCorrectes = false;

        LoginTuple loginTuple = null;

        String login = edtLogin.getText().toString();
        String password = edtPassword.getText().toString();

        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            socket = new Socket(ServerInfo.SRVIP, ServerInfo.SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Cambrer cambrer = new Cambrer(0,"a","a",null,login,password);
            loginTuple = new LoginTuple(cambrer, null);
            final Long sessionId;
            Long sessionIdAux = null;

//                    try {
            // enviem logintuple
            Log.d("SRV", "enviant missatge");

            // Enviem codi d'operació: LOGIN - 1
            oos.writeInt(CodiOperacio.LOGIN.getNumVal());
            oos.flush();

            // Enviarem "tupla" LoginTuple
            oos.writeObject(loginTuple);
            oos.flush();
            Log.d("SRV", "missatge enviat");
            // llegim ok
            ois.readInt();


            Log.d("SRV", "esperant resposta del server");
            int res = ois.readInt();
            Log.d("SRV", "resposta del server REBUDA");
//                        int res = (int)ois.readObject();

            // si la resposta == OK
            if (res == 1) {
                credencialsCorrectes = true;
                // llegim LoginTuple
                loginTuple = (LoginTuple) ois.readObject();
                // enviem ok
                oos.writeInt(1);
                oos.flush();
            } else {
                // Empleat no consta a la BD o login incorrecte
                credencialsCorrectes = false;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("SRV", e.getLocalizedMessage());
        } finally {
            try {
                oos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SRV", e.getLocalizedMessage());
            }
        }

        return loginTuple;
    }

//            public boolean getCredencialsCorrectes() {
//                return credencialsCorrectes;
//            }
//        });
//
//        thread.start();
//        try {
//            thread.join(); // TODO: afegir progressbar
//
//        } catch (InterruptedException e) {
//            Log.d("SRV", e.getLocalizedMessage());
//            e.printStackTrace();
//        }

}