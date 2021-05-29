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
//    private final String SRVIP = "192.168.1.108";
//    private final String SRVIP = "192.168.1.105";
    private final String SRVIP = "192.168.1.106";
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

                Log.d("LOGIN", "loginSP=" + loginSP + "\tpasswordSP=" + passwordSP);

                if (loginSP.isEmpty() || passwordSP.isEmpty()) {
                    // primer login, ens guardem aquestes credencials
                    saveLoginSharedPreferences(login, password);
                    Toast.makeText(getApplicationContext(), "Login i contrasenya registrats correctament", Toast.LENGTH_SHORT).show();

                } else {
                    userLogin();

                    if (!login.equals(loginSP) || !password.equals(passwordSP)) {
                        // Login incorrecte (login o contrasenya incorrectes), no deixem entrar
                        Toast.makeText(getApplicationContext(), "Login o contrasenya incorrectes", Toast.LENGTH_SHORT).show();

                    } else {
                        // Login correcte o primer login, deixem entrar
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
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


    private void userLogin_COPY() {
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
                    Long sessionIdAux = null;

                    try {
                        // enviem logintuple
                        Log.d("SRV", "enviant missatge");

                        // Enviem codi d'operació: LOGIN - 1
                        oos.writeInt(1);
                        oos.flush();
                        // llegim ok
                        ois.readInt();

                        //                        oos.writeObject(lt);
                        // Enviarem "tupla" camp per camp al server
                        oos.writeUTF(login);
                        oos.flush();
                        // llegim ok
                        ois.readInt();
                        oos.writeUTF(password);
                        oos.flush();
                        // llegim ok
                        ois.readInt();

                        oos.flush();
                        Log.d("SRV", "missatge enviat");


                        Log.d("SRV", "esperant resposta del server");
                        int res = ois.readInt();
//                        int res = (int)ois.readObject();

                        // si la resposta == OK
                        if (res == 1) {
                            // llegim login
                            String login = ois.readUTF();
                            // enviem ok
                            oos.writeInt(1);
                            oos.flush();

                            // llegim passwd
                            String password = ois.readUTF();
                            // enviem ok
                            oos.writeInt(1);
                            oos.flush();

                            // llegim session id
                            sessionIdAux = ois.readLong();
                            // enviem ok
                            oos.writeInt(1);
                            oos.flush();

//                            lt = (LoginTuple)ois.readObject();
                        }

                    } catch (IOException ex) {
                        System.out.println(ex);
                        System.out.println(ex.getMessage());
                        ex.printStackTrace();

                    } finally {
                        oos.close();
                        socket.close();
                    }

                    sessionId = sessionIdAux;
                    // actualitzar UI
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // actualitzem la resposta del server per poder veure-la en la UI
                            txvSessionId.setText(sessionId + "");
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private boolean userLogin() {
        final Handler handler = new Handler();
        boolean credencialsCorrectes = false;

        String login = edtLogin.getText().toString();
        String password = edtPassword.getText().toString();

        Thread thread = new Thread(new Runnable() {
            private boolean credencialsCorrectes = false;

            @Override
            public void run() {
                Socket socket = null;
                ObjectOutputStream oos = null;
                ObjectInputStream ois = null;

                try {
                    socket = new Socket(SRVIP, SRVPORT);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    LoginTuple lt = new LoginTuple(login, password, null); // client inicialment no coneix el seu sessionID
                    final Long sessionId;
                    Long sessionIdAux = null;

//                    try {
                    // enviem logintuple
                    Log.d("SRV", "enviant missatge");

                    // Enviem codi d'operació: LOGIN - 1
                    oos.writeInt(1);
                    oos.flush();
                    // llegim ok
//                        Log.d("SRV", "esperant ok");
//                        ois.readInt();

                    // Enviarem "tupla" LoginTuple
                    oos.writeObject(lt);
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
                        lt = (LoginTuple) ois.readObject();
                        // enviem ok
                        oos.writeInt(1);
                        oos.flush();
                    } else {
                        // Empleat no consta a la BD o login incorrecte
                        credencialsCorrectes = false;
                    }

//                    } catch (IOException | ClassNotFoundException ex) {
//                        System.out.println(ex);
//                        System.out.println(ex.getMessage());
//                        ex.printStackTrace();


//                    sessionId = sessionIdAux;
//                    // actualitzar UI
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            // actualitzem la resposta del server per poder veure-la en la UI
//                            txvSessionId.setText(sessionId+"");
//                        }
//                    });

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
            }

            public boolean getCredencialsCorrectes(){
                return credencialsCorrectes;
            }
        });

        thread.start();
        try {
            thread.join(); // TODO: afegir progressbar

        } catch (InterruptedException e) {
            Log.d("SRV", e.getLocalizedMessage());
            e.printStackTrace();
        }

    }


}