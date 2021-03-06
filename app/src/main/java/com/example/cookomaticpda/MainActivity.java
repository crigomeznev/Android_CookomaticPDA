package com.example.cookomaticpda;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookomaticpda.adapters.InfoTaulaAdapter;
import com.example.cookomaticpda.adapters.TaulaAdapter;

import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.CreateComandaTuple;
import org.cookomatic.protocol.InfoTaula;
import org.cookomatic.protocol.LoginTuple;
import org.cookomatic.exception.CookomaticException;
import org.cookomatic.model.sala.Cambrer;
import org.cookomatic.model.sala.Comanda;
import org.cookomatic.model.sala.Taula;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

    // Elements UI
    private RecyclerView rcyTaules;
    private ProgressBar pgrLoading;

    private LoginTuple loginTuple;
    private Taula taulaSeleccionada;
    private List<InfoTaula> mInfoTaules;
    private InfoTaulaAdapter mAdapter;

    private Handler thRefrescaPantalla;

    private int millisegons = 3000;



    //--------------------------------------------------------------------------------------------------
    // M??TODES PRINCIPALS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Objectes UI
        rcyTaules = findViewById(R.id.rcyTaules);
        pgrLoading = findViewById(R.id.pgrLoading);

        //---------------------------------
        // recuperar el par??metre loginTuple:
        Intent i = getIntent();
        loginTuple = (LoginTuple) i.getSerializableExtra("loginTuple");
        Log.d("INTENT", "logintuple recuperat: " + loginTuple.getCambrer().getUser() + "/" + loginTuple.getCambrer().getPassword() + " SID:" + loginTuple.getSessionId());

        mInfoTaules = new ArrayList<>();

        // ini recycler view
        rcyTaules.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columnes

        // Refrescar la pantalla cada x segons
        thRefrescaPantalla = new Handler();
        iniciaRefrescaPantalla();
    }


    // Avisar al servidor que tanquem l'aplicaci??
    @Override
    protected void onDestroy() {
        Log.d("DESTROY", "Tancant aplicaci??");
        super.onDestroy();
        aturaRefrescaPantalla();

        // LOGOUT
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            try {
                userLogout();
            } catch (Exception ex) {
                Log.d("LOGOUT", ex.getMessage(), ex);
            }
            return true;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((loginTupleNull) -> {
                    //-------------  UI THREAD ---------------------------------------
                    Log.d("DESTROY", "Aplicaci?? tancada");
                    //-------------  END OF UI THREAD ---------------------------------------
                });


    }


    // Quan tornem de l'altra activity cap aquesta
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode){
            case Activity.RESULT_OK:
                Toast.makeText(getApplicationContext(), "Comanda creada amb ??xit", Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(getApplicationContext(), "No s'ha pogut crear la comanda", Toast.LENGTH_SHORT).show();
                break;
        }
        iniciaRefrescaPantalla();
    }



    //--------------------------------------------------------------------------------------------------
    // FIL QUE ACTUALITZA TAULES DE LA BD
    private void iniciaRefrescaPantalla() {
        refrescadorPantalla.run();
    }


    private void aturaRefrescaPantalla() {
        thRefrescaPantalla.removeCallbacks(refrescadorPantalla);
    }


    // Refrescar la pantalla cada x segons
    Runnable refrescadorPantalla = new Runnable() {
        @Override
        public void run() {
            try {
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
        // Crida ass??ncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Aix?? ??s el codi que s'executar?? en un fil
            List<InfoTaula> infoTaules = new ArrayList<>();
            try {
                infoTaules = getTaules(); // TODO: try-catch
            } catch (Exception e) {
                Log.d("INFOTAULES", "Error en recuperar info taules: " + e.getMessage(), e);
            }

            return infoTaules;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((infoTaules) -> {
                    //-------------  UI THREAD ---------------------------------------
                    mInfoTaules.clear();
                    mInfoTaules = infoTaules;

                    // no va
//                    mAdapter.notifyDataSetChanged(); // TODO: canviar per algo mes eficient
                    mAdapter = new InfoTaulaAdapter(this, mInfoTaules, loginTuple.getCambrer().getUser());
                    rcyTaules.setAdapter(mAdapter);
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }



    //--------------------------------------------------------------------------------------------------
    // UI
    private void setLoading(boolean isLoading) {
        pgrLoading.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onSelectedInfoTaula(InfoTaula seleccionada) {
        // Seleccionem registre de la taula seleccionada
        setLoading(true);
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Hem de recuperar: comanda + linies d'aquesta taula, si en t??
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
                    setLoading(false);

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
    public void confirmacioBuidarTaula(InfoTaula seleccionada) {
        if (seleccionada.getNomCambrer()==null){
            Toast.makeText(getApplicationContext(), "No pots buidar una taula buida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!seleccionada.isEsMeva()){
            Toast.makeText(getApplicationContext(), "No pots buidar una taula que no ??s teva", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Est??s segur que vols buidar aquesta taula?");
        builder1.setCancelable(true);

        builder1.setPositiveButton("S??", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                buidarTaula(seleccionada);
            }
        });
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public void buidarTaula(InfoTaula seleccionada) {
        // Seleccionem registre de la taula seleccionada
        setLoading(true);
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Aix?? ??s el codi que s'executar?? en un fil
            return finalitzarComandaDeTaula(seleccionada);
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resultat) -> {
                    //-------------  UI THREAD ---------------------------------------
                    setLoading(false);
                    // if resultat == -1: ERROR, 0 == tot b??
                    if (resultat == 0) {
                        Toast.makeText(getApplicationContext(), "Taula buidada amb ??xit", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error en buidar taula", Toast.LENGTH_LONG).show();
                    }
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }


    //--------------------------------------------------------------------------------------------------
    // CRIDES AL SERVIDOR

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


            // Enviem codi d'operaci??: GETTAULES 2
            oos.writeInt(CodiOperacio.GET_TAULES.getNumVal());
            oos.flush();

            // enviem sessionId
//            Log.d("SRV", "enviant logintuple = " + loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
//            Log.d("SRV", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operaci??
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // Llegim qt de taules que ens enviar?? el server
//            Log.d("SRV", "esperant resposta del server");
            qt = ois.readInt();
//            Log.d("SRV", "resposta del server REBUDA");

            for (int i = 0; i < qt; i++) {
                // llegim objecte
                InfoTaula it = (InfoTaula) ois.readObject();
//                Log.d("GETTAULA", "taula recuperada: " + it.getNumero() + " " + it.getNomCambrer());
                infoTaules.add(it);

                // enviem ok
                oos.write(new byte[1]);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SRV", e.getMessage());
        } finally {
            try {
                if (oos != null) oos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SRV", e.getMessage());
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

            // Enviem codi d'operaci??: GETTAULES 2
            oos.writeInt(CodiOperacio.GET_TAULA_SELECCIONADA.getNumVal());
            oos.flush();

            // enviem sessionId
//            Log.d("SRV", "enviant logintuple = " + loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
//            Log.d("SRV", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operaci??
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // enviem numero taula
            oos.writeInt(numeroTaula);
            oos.flush();

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operaci??
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


    private int finalitzarComandaDeTaula(InfoTaula infoTaula) {
        if (infoTaula == null) {
            Log.d("finComandaTaula", "infoTaula ??s null");
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

            // Enviem codi d'operaci??: GETTAULES 2
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
            // si resposta == KO, avortem operaci??
            if (res == CodiOperacio.KO.getNumVal()) {
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // Enviem infoTaula
            oos.writeObject(infoTaula);
            oos.flush();

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operaci??
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


    // Diem al server que ens esborri de la llista de session ids
    private void userLogout() {
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

            // Enviem codi d'operaci??: LOGOUT - 10
//            oos.writeInt(CodiOperacio.LOGOUT.getNumVal());
            oos.writeInt(CodiOperacio.LOGOUT.getNumVal());
            oos.flush();

            // Enviarem "tupla" LoginTuple
            oos.writeObject(loginTuple);
            oos.flush();

            // llegim ok
            ois.readInt();

        } catch (SocketTimeoutException e) {
            // Temps d'espera esgotat
            e.printStackTrace();
            Log.d("SRV", e.getMessage());
//            errNo = E_SRVCON;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SRV", e.getLocalizedMessage());
//            errNo = E_UNKNOWN;
        } finally {
            try {
                if (oos != null) oos.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SRV", e.getLocalizedMessage());
                throw new CookomaticException("Error en fer login", e);
            }
        }
    }








}