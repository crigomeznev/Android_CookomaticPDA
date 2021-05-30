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
import org.cookomatic.protocol.CreateComandaTuple;
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

    private Handler thRefrescaPantalla;

    private int millisegons = 3000;

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
        Log.d("INTENT", "logintuple recuperat: " + loginTuple.getCambrer().getUser() + "/" + loginTuple.getCambrer().getPassword() + " SID:" + loginTuple.getSessionId());

        mInfoTaules = new ArrayList<>();
//        recuperarInfoTaules();

        // ini recycler view
        rcyTaules.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columnes


        // Refrescar la pantalla cada x segons
        thRefrescaPantalla = new Handler();
        iniciaRefrescaPantalla();
//        refrescarPantalla(500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        aturaRefrescaPantalla();
    }

    private void iniciaRefrescaPantalla() {
        refrescadorPantalla.run();
    }

    private void aturaRefrescaPantalla() {
        thRefrescaPantalla.removeCallbacks(refrescadorPantalla);
    }

//    // Refrescar la pantalla cada x segons
//    private void refrescarPantalla(int millisegons) {
//        final Handler handler = new Handler();
//        final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("REFRESH", "refrescant pantalla");
//                recuperarInfoTaules();
//                Log.d("REFRESH", "pantalla refrescada");
//                Toast.makeText(getApplicationContext(),"Pantalla actualitzada",Toast.LENGTH_SHORT).show();
//            }
//        };
//        handler.postDelayed(runnable, millisegons);
//    }

    // Refrescar la pantalla cada x segons
    Runnable refrescadorPantalla = new Runnable() {
        @Override
        public void run() {
            try {
                Toast.makeText(getApplicationContext(), "Actualitzant pantalla", Toast.LENGTH_SHORT).show();
                Log.d("REFRESH", "Actualitzant pantalla");
                recuperarInfoTaules();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                thRefrescaPantalla.postDelayed(refrescadorPantalla, millisegons);
            }
        }
    };


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
                    mInfoTaules.clear();
                    mInfoTaules = infoTaules;
                    Log.d("GETTAULES", "Taules rebudes: " + mInfoTaules);

                    // no va
//                    mAdapter.notifyDataSetChanged(); // TODO: canviar per algo mes eficient
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

            // Llegim qt de taules que ens enviarà el server
            Log.d("SRV", "esperant resposta del server");
            qt = ois.readInt();
            Log.d("SRV", "resposta del server REBUDA");

            for (int i = 0; i < qt; i++) {
                // llegim objecte
                InfoTaula it = (InfoTaula) ois.readObject();
                Log.d("GETTAULA", "taula recuperada: " + it.getNumero() + " " + it.getNomCambrer());
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


    // Quan tornem de l'altra activity cap aquesta
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("INTENTS", "Hem tornat a la MainActivity");

        if (resultCode == Activity.RESULT_OK) {
            // recarregar taules amb noves dades de les comandes
            Toast.makeText(getApplicationContext(), "INSERT de la comanda amb ÈXIT", Toast.LENGTH_SHORT).show();

//            recuperarInfoTaules();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "No s'ha pogut fer insert de la comanda", Toast.LENGTH_SHORT).show();
        }
        iniciaRefrescaPantalla();
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
                    Log.d("getTaulaSeleccionada", "Taula seleccionada: " + taulaSeleccionada);

                    if (this.taulaSeleccionada != null) {
                        // aturem thread "refrescador"
                        aturaRefrescaPantalla();

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

    @Override
    public void buidarTaula(InfoTaula seleccionada) {
        // Seleccionem registre de la taula seleccionada
        // Crida assíncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Això és el codi que s'executarà en un fil
            return finalitzarComandaDeTaula(seleccionada);
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resultat) -> {
                    //-------------  UI THREAD ---------------------------------------

                    // if resultat == -1: ERROR, 0 == tot bé
                    if (resultat == 0) {
                        Toast.makeText(getApplicationContext(), "Taula buidada amb èxit", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error en buidar taula", Toast.LENGTH_LONG).show();
                    }
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }


    // FUNCIONS D'INTERACCIÓ AMB EL SERVIDOR
    private int finalitzarComandaDeTaula(InfoTaula infoTaula) {
        if (infoTaula == null) {
            Log.d("finComandaTaula", "infoTaula és null");
            return -1;
        }

        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        int res = 0;
        Long codiNovaComanda = null;

        try {
            socket = new Socket(SRVIP, SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Enviem codi d'operació: GETTAULES 2
            oos.writeInt(CodiOperacio.BUIDAR_TAULA.getNumVal());
            oos.flush();

            // enviem sessionId
            Log.d("createComanda", "enviant logintuple = " + loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
            Log.d("createComanda", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // Enviem infoTaula
            oos.writeObject(infoTaula);
            oos.flush();

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("no s'ha pogut buidar taula, operacio avortada");
            }

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
        return res;
    }


}