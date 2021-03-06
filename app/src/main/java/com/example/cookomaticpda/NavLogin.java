package com.example.cookomaticpda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.LoginTuple;
import org.cookomatic.exception.CookomaticException;
import org.cookomatic.model.sala.Cambrer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NavLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NavLogin extends Fragment {

    private Button btnStart;
    private EditText edtLogin, edtPassword;
    private TextView txvSessionId;

    // Login
    private LoginTuple mLoginTuple;
    private int errNo = 0;
    private static final int NOERROR = 0;
    private static final int E_SRVCON = 1; // Error en connectar-se al servidor
    private static final int E_CREDS = 2; // Error per credencials incorrectes
    private static final int E_UNKNOWN = 3; // Error per credencials incorrectes


    // Required empty public constructor
    public NavLogin() {
    }


    public static NavLogin newInstance(String param1, String param2) {
        NavLogin fragment = new NavLogin();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_nav_login, container, false);

        btnStart = root.findViewById(R.id.btnStart);
        edtLogin = root.findViewById(R.id.edtLogin);
        edtPassword = root.findViewById(R.id.edtPassword);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardar les noves shared preferences
                String login = edtLogin.getText().toString();
                String password = edtPassword.getText().toString();

                String loginSP = getFromSharedPreferences("login");
                String passwordSP = getFromSharedPreferences("password");

                // Noves preferences: les guardem
                if (!login.equals(loginSP) || !password.equals(passwordSP)) {
                    saveLoginSharedPreferences(login, password);
                    Toast.makeText(getContext(), "Login i contrasenya registrats correctament", Toast.LENGTH_SHORT).show();
                }

                Log.d("LOGIN", "loginSP=" + loginSP + "\tpasswordSP=" + passwordSP);

//                if (loginSP.isEmpty() || passwordSP.isEmpty()) {
//                    // primer login, ens guardem aquestes credencials
//
//                } else {
                    // Crida ass??ncrona per enviar credencials al servidor
                    Observable.fromCallable(() -> {
                        //---------------- START OF THREAD ------------------------------------
                        // Aix?? ??s el codi que s'executar?? en un fil

                        // demanem al server un logintuple que cont?? session id
                        // si credencials correctes, ens el retorna
                        // altrament, obtenim null
                        LoginTuple loginTuple = null;

                        try{
                            loginTuple = userLogin();
                        } catch (CookomaticException ex){
                            Toast.makeText(getContext(),"Error en fer login", Toast.LENGTH_SHORT);
                        }

                        mLoginTuple = loginTuple;

//                        return mLoginTuple==null; // TODO: NO POT RETORNAR NULL
                        return true; // TODO: NO POT RETORNAR NULL
                        //--------------- END OF THREAD-------------------------------------
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((loginTupleNull) -> {
                                //-------------  UI THREAD ---------------------------------------
                                // El codi que tenim aqu?? s'executa nom??s quan el fil
                                // ha acabat !! A m??s, aquest codi s'executa en el fil
                                // d'interf??cie gr??fica.
//                                setLoading(false); // TODO: progressbar
                                if (errNo == NOERROR) {
                                    // Login correcte o primer login, deixem entrar
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    // passem loginTuple a l'altra activity
                                    intent.putExtra("loginTuple", mLoginTuple);
                                    startActivity(intent);

                                }else{
//                                if (loginTupleNull) {
                                    // Hi ha hagut algun error, mostrem missatge personalitzat:
                                    String errMsg = "";
                                    switch (errNo){
                                        case E_SRVCON: errMsg = "Error: no es pot connectar al servidor"; break;
                                        case E_CREDS: errMsg = "Error: credencials incorrectes o usuari desconegut"; break;
                                        case E_UNKNOWN: errMsg = "Error: desconegut"; break;
                                    }
                                    // Login incorrecte (login o contrasenya incorrectes), no deixem entrar
                                    // Missatge diferent segons codi d'error (errNo)
                                    Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT).show();
                                }
                                //-------------  END OF UI THREAD ---------------------------------------
                            });
//                }
            }
        });

        // Carregar dades de sharedpreferences
        String loginSP = getFromSharedPreferences("login");
        String passwordSP = getFromSharedPreferences("password");

        edtLogin.setText(loginSP);
        edtPassword.setText(passwordSP);

        return root;
    }



    private void saveLoginSharedPreferences(String login, String password) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Guardar la preferencia
        editor.putString("login", login);
        editor.putString("password", password);
//        editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
        editor.apply();
    }

    private String getFromSharedPreferences(String key) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        String value = sharedPref.getString(key, "");
        // si no troba la propietat, retorna defValue (en aquest cas "")
        return value;
    }

    // S'executa dins d'un THREAD
    private LoginTuple userLogin() {
        LoginTuple loginTuple = null;

        String login = edtLogin.getText().toString();
        String password = edtPassword.getText().toString();

        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
//            socket = new Socket();
//            socket.setSoTimeout(1000);
//            socket.connect(new InetSocketAddress(ServerInfo.SRVIP, ServerInfo.SRVPORT), 1000);

            socket = new Socket(ServerInfo.SRVIP, ServerInfo.SRVPORT); // aix?? ??s bloquejant, millorar

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Cambrer cambrer = new Cambrer(0, "a", "a", null, login, password);
            loginTuple = new LoginTuple(cambrer, null);
//            final Long sessionId;

            // enviem logintuple
            Log.d("SRV", "enviant missatge");

            // Enviem codi d'operaci??: LOGIN - 1
            oos.writeInt(CodiOperacio.LOGIN.getNumVal());
            oos.flush();

            // Enviarem "tupla" LoginTuple
            oos.writeObject(loginTuple);
            oos.flush();
//            Log.d("SRV", "missatge enviat");
            // llegim ok
            ois.readInt();

//            Log.d("SRV", "esperant resposta del server");
            int res = ois.readInt();
//            Log.d("SRV", "resposta del server REBUDA = "+res);

            // si la resposta == OK
            if (res == CodiOperacio.OK.getNumVal()) {
                // llegim LoginTuple
                loginTuple = (LoginTuple) ois.readObject();
                // enviem ok
                oos.writeInt(1);
                oos.flush();
                errNo = NOERROR;
            } else {
                // Empleat no consta a la BD o login incorrecte
                Log.d("LOGIN","Empleat no consta a la BD o login incorrecte");
                errNo = E_CREDS;
            }
        } catch (SocketTimeoutException | ConnectException e) {
            // Temps d'espera esgotat
            e.printStackTrace();
            Log.d("SRV", e.getMessage());
            errNo = E_SRVCON;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SRV", e.getMessage());
            errNo = E_UNKNOWN;
        } finally {
            try {
                if (oos!=null) oos.close();
                if (socket!=null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SRV", e.getLocalizedMessage());
                throw new CookomaticException("Error en fer login", e);
            }
        }
        return loginTuple;
    }


}