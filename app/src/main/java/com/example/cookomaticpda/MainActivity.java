package com.example.cookomaticpda;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookomaticpda.adapters.InfoTaulaAdapter;
import com.example.cookomaticpda.adapters.TaulaAdapter;

import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.InfoTaula;
import org.cookomatic.protocol.LoginTuple;
import org.milaifontanals.cookomatic.model.sala.Cambrer;
import org.milaifontanals.cookomatic.model.sala.Comanda;
import org.milaifontanals.cookomatic.model.sala.Taula;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.cookomaticpda.ServerInfo.SRVIP;
import static com.example.cookomaticpda.ServerInfo.SRVPORT;

public class MainActivity extends AppCompatActivity
    implements InfoTaulaAdapter.OnSelectedItemListener {

    private RecyclerView rcyTaules;
//    private ComandaAdapter mAdapter;
    private InfoTaulaAdapter mAdapter;
    private List<Comanda> mComandes;
    private List<Taula> mTaules;


    private List<InfoTaula> mInfoTaules;


    private LoginTuple loginTuple;

    private Taula taulaSeleccionada;

    // BORRAR
    private Button btnProva;
    private TextView txvServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Objectes UI
        rcyTaules = findViewById(R.id.rcyTaules);

        //---------------------------------
        // recuperar el paràmetre loginTuple:
        Intent i = getIntent();
        loginTuple = (LoginTuple) i.getSerializableExtra("loginTuple");
        Log.d("INTENT","logintuple recuperat: "+loginTuple.getCambrer().getUser()+"/"+loginTuple.getCambrer().getPassword()+" SID:"+loginTuple.getSessionId());

        mInfoTaules = new ArrayList<>();
        recuperarInfoTaules();



        // TODO: ini comandes de la DB
//        iniTaules();
//        iniComandes();





        //BORRAR
        txvServer = findViewById(R.id.txvServer);
        btnProva = findViewById(R.id.btnProva);
        btnProva.setOnClickListener(new View.OnClickListener() {
            // Prova de connexió amb el servidor
            @Override
            public void onClick(View v) {
//                sendMessage(txvServer.getText().toString());
//                sendMessage("HOLA MUNDO");

            }
        });
    }

    private void recuperarInfoTaules() {
        // Crida assíncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Això és el codi que s'executarà en un fil
            List<InfoTaula> infoTaules = new ArrayList<>();
            infoTaules = getTaules();
            return infoTaules;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((infoTaules) -> {
                    //-------------  UI THREAD ---------------------------------------
                    mInfoTaules = infoTaules;
                    Log.d("GETTAULES","Taules rebudes: "+mInfoTaules);

                    rcyTaules.setLayoutManager(new GridLayoutManager(this,3)); // 3 columnes
                    mAdapter = new InfoTaulaAdapter(this, mInfoTaules, loginTuple.getCambrer().getUser());
                    rcyTaules.setAdapter(mAdapter);


                    // construir recycler taules
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }


    private List<InfoTaula> getTaules() {
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        List<InfoTaula> infoTaules = new ArrayList<>();
        int qt = 0;
        int res;

        try {
            socket = new Socket(SRVIP, SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());


            // Enviem codi d'operació: GETTAULES 2
            oos.writeInt(CodiOperacio.GET_TAULES.getNumVal());
            oos.flush();

            // enviem sessionId
            Log.d("SRV", "enviant logintuple = "+loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
            Log.d("SRV", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()){
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // Llegim qt de taules que ens enviarà el server
            Log.d("SRV", "esperant resposta del server");
            qt = ois.readInt();
            Log.d("SRV", "resposta del server REBUDA");

            for (int i = 0; i < qt; i++) {
                // llegim objecte
                InfoTaula it = (InfoTaula)ois.readObject();
                Log.d("GETTAULA","taula recuperada: "+it.getNumero()+" "+it.getNomCambrer());
                infoTaules.add(it);

                // enviem ok
                oos.write(new byte[1]);
                oos.flush();
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

        return infoTaules;




    }

    private Taula getTaulaSeleccionada(int numeroTaula) {
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        int res;

        Taula taulaSeleccionada = null;

        try {
            socket = new Socket(SRVIP, SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());


            // Enviem codi d'operació: GETTAULES 2
            oos.writeInt(CodiOperacio.GET_TAULA_SELECCIONADA.getNumVal());
            oos.flush();

            // enviem sessionId
            Log.d("SRV", "enviant logintuple = " + loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
            Log.d("SRV", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // enviem numero taula
            oos.writeInt(numeroTaula);
            oos.flush();

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("taula no trobada, operacio avortada");
            }

            // Si OK, llegim taula
            taulaSeleccionada = (Taula) ois.readObject();
            // enviem ok
            oos.write(new byte[1]);
            oos.flush();
        } catch (Exception e) {
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
        return taulaSeleccionada;
    }

    private void iniTaules() {
        mTaules = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            mTaules.add(new Taula(i+1));
        }
    }

    private void sendMessage(final String msg) {

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
//                    Socket s = new Socket("xxx.xxx.xxx.xxx", 9002);
//                    Socket s = new Socket("10.132.0.115", 9876);
                    Socket socket = new Socket("192.168.1.108", 9876);
                    // aquí han d'anar ip i port del servidor (que sempre seran FIXES!)

                    // obtenim "la pipe" del socket per on ens comunicarem amb l'altre extrem
//                    OutputStream out = socket.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    oos.writeObject(msg);
                    oos.flush();
//                    PrintWriter output = new PrintWriter(out);
//
//                    output.println(msg);
//                    output.flush();
//                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    final String st = input.readLine();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String stAux ="";
                    try {
                        stAux = (String)ois.readObject();
                    } catch (ClassNotFoundException e) {
                        Log.d("SRV", "ERROR: "+e.getMessage());
                        e.printStackTrace();
                    }
                    final String st = stAux;

                    // enviem OK
//                    oos.write(new byte[1]);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // actualitzem la resposta del server per poder veure-la en la UI
                            String s = txvServer.getText().toString();
//                            String s = "Hola mundo";

                            Log.d("SRV", "srv response = "+st);

                            if (st.trim().length() != 0)
                                txvServer.setText(s + "\nFrom Server : " + st);
                        }
                    });

                    oos.close();
//                    output.close();
//                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    // Quan tornem de l'altra activity cap aquesta
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("INTENTS", "Hem tornat a la MainActivity");

        if (resultCode == Activity.RESULT_OK)
        {
            // recarregar taules amb noves dades de les comandes
            Toast.makeText(getApplicationContext(), "INSERT de la comanda amb ÈXIT", Toast.LENGTH_SHORT).show();
            recuperarInfoTaules();
        } else if (resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(getApplicationContext(),"No s'ha pogut fer insert de la comanda", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSelectedInfoTaula(InfoTaula seleccionada) {
        // Seleccionem registre de la taula seleccionada
        // Crida assíncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Això és el codi que s'executarà en un fil
            taulaSeleccionada = getTaulaSeleccionada(seleccionada.getNumero());
            return taulaSeleccionada;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((taulaSeleccionada) -> {
                    //-------------  UI THREAD ---------------------------------------
                    this.taulaSeleccionada = taulaSeleccionada;
                    Log.d("getTaulaSeleccionada","Taula seleccionada: "+taulaSeleccionada);

                    if (this.taulaSeleccionada != null) {
                        Intent intent = new Intent(getApplicationContext(), PresaComandaActivity.class);
                        intent.putExtra("loginTuple", loginTuple);
//                    intent.putExtra("numTaula", seleccionada.getNumero()); // nomes passem el numero de la taula seleccionada
                        intent.putExtra("taulaSeleccionada", taulaSeleccionada); // nomes passem el numero de la taula seleccionada
                        startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(getApplicationContext(), "ERROR: Taula no trobada en la BD", Toast.LENGTH_LONG).show();
                    }

                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }
}