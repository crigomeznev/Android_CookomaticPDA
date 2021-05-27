package com.example.cookomaticpda;

import androidx.appcompat.app.AppCompatActivity;

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

//import com.example.cookomaticpda.borrar.LoginTuple;


import org.cookomatic.protocol.LoginTuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    // moure d'aqui
    private final String SRVIP = "192.168.1.108";
    private final int SRVPORT = 9876;


    private Button btnStart;
    private EditText edtLogin, edtPassword;
    private TextView txvSessionId;

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

                Log.d("LOGIN", "loginSP="+loginSP+"\tpasswordSP="+passwordSP);

                if (loginSP.isEmpty() || passwordSP.isEmpty()){
                    // primer login, ens guardem aquestes credencials
                    saveLoginSharedPreferences(login, password);
                    Toast.makeText(getApplicationContext(), "Login i contrasenya registrats correctament", Toast.LENGTH_SHORT).show();


                    // PROVA
                    // Enviar resp al servidor




                } else {
                    userLogin();

//                    if (!login.equals(loginSP) || !password.equals(passwordSP)) {
//                        // Login incorrecte (login o contrasenya incorrectes), no deixem entrar
//                        Toast.makeText(getApplicationContext(), "Login o contrasenya incorrectes", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        // Login correcte o primer login, deixem entrar
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(intent);
//                    }
                }




            }
        });

        // VEGEU: https://programacionymas.com/series/app-android-sobre-registro-de-inventarios/login-usando-sharedpreferences
    }


    private void saveLoginSharedPreferences(String login, String password){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Guardar la preferencia
        editor.putString("login", login);
        editor.putString("password", password);
//        editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
        editor.apply();
    }

    private String getFromSharedPreferences(String key){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        String value = sharedPref.getString(key, "");
        // si no troba la propietat, retorna defValue (en aquest cas "")
//        int defaultValue = getResources().getInteger(R.integer.saved_high_score_default_key);
//        int highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), defaultValue);
        return value;
    }





    private void userLogin() {
        final Handler handler = new Handler();

        String login = edtLogin.getText().toString();
        String password = edtPassword.getText().toString();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SRVIP, SRVPORT);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    LoginTuple lt = new LoginTuple(login, password, null); // client inicialment no coneix el seu sessionID
                    final Long sessionId;

                    try {
                        // enviem logintuple
                        oos.writeObject(lt);

                        int res = ois.readInt();

                        // si la resposta == OK
                        if (res == 1) {
                            lt = (LoginTuple)ois.readObject();
                        }

                    } catch (IOException | ClassNotFoundException ex) {
                        System.out.println(ex);
                        System.out.println(ex.getMessage());
                        ex.printStackTrace();
                    }
                    sessionId = lt.getSessionId();


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // actualitzem la resposta del server per poder veure-la en la UI
                            txvSessionId.setText(sessionId+"");
                        }
                    });

                    oos.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }




}