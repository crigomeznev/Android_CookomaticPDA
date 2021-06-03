package com.example.cookomaticpda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cookomaticpda.adapters.CategoriaAdapter;
import com.example.cookomaticpda.adapters.LiniaComandaAdapter;
import com.example.cookomaticpda.adapters.PlatAdapter;

import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.CreateComandaTuple;
import org.cookomatic.protocol.LoginTuple;
import org.cookomatic.model.cuina.Categoria;
import org.cookomatic.model.cuina.Plat;
import org.cookomatic.model.sala.Comanda;
import org.cookomatic.model.sala.EstatLinia;
import org.cookomatic.model.sala.LiniaComanda;
import org.cookomatic.model.sala.Taula;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.cookomaticpda.ServerInfo.SRVIP;
import static com.example.cookomaticpda.ServerInfo.SRVPORT;

public class PresaComandaActivity extends AppCompatActivity
        implements PlatAdapter.OnSelectedItemListener,
        CategoriaAdapter.OnSelectedItemListener {

    private List<Categoria> mCategories;
    private ArrayList<View> btnsCategories;

//    private List<Plat> mPlats = new ArrayList<>();
    private List<Plat> mPlatsFiltrats = new ArrayList<>();

    // TODO: DICCIONARIS DE PLATS, CATEGORIES
    private HashMap<Long, Categoria> hmCategories = new HashMap<>();
    private HashMap<Long, Plat> hmPlats = new HashMap<>();
    // suposem màxim 1 línia per plat
    private HashMap<Long, LiniaComanda> hmLinies = new HashMap<>();
    private List<LiniaComanda> mLinies = new ArrayList<>(hmLinies.values()); // per omplir el recyclerview



    private Button btnConfirmar;
    private Button btnTotesCategories;
    private RecyclerView rcyPlats;
    private RecyclerView rcyLinies;
    private RecyclerView rcyCategories;
    private ProgressBar pgrLoading;

    private LiniaComandaAdapter lcAdapter;

    private LoginTuple loginTuple;

    private Taula mTaula;
    private int numTaula;
    private Comanda mComanda;

    private boolean teComandaActiva;


    private int returnCode = RESULT_FIRST_USER;

//    private PresaComandaActivity mActivity;
//    private LinearLayout llaContainerButtons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presa_comanda);

        //---------------------------------
        // recuperar el paràmetre loginTuple:
        Intent i = getIntent();
        loginTuple = (LoginTuple) i.getSerializableExtra("loginTuple");
//        numTaula = i.getIntExtra("numTaula", 0);
        mTaula = (Taula)i.getSerializableExtra("taulaSeleccionada");
        mComanda = mTaula.getComandaActiva();

//        teComandaActiva = mTaula.getComandaActiva()!=null;
        Log.d("INTENT","logintuple recuperat: "+loginTuple.getCambrer().getUser()+"/"+loginTuple.getCambrer().getPassword()+" SID:"+loginTuple.getSessionId());
        Log.d("INTENT","comanda activa = "+ mComanda);
        //---------------------------------

        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setVisibility(mComanda!=null ? View.INVISIBLE : View.VISIBLE);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLinies.size()<1){
                    Toast.makeText(getApplicationContext(), "Una comanda ha de tenir com a mínim 1 línia", Toast.LENGTH_SHORT).show();
                    return;
                }
                inserirComanda();
            }
        });

        btnTotesCategories = findViewById(R.id.btnTotesCategories);
        btnTotesCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedCategoria(null); // passem categoria null i aixi omple amb tots plats de totes categories
            }
        });

        rcyPlats = findViewById(R.id.rcyPlats);
        rcyLinies = findViewById(R.id.rcyLinies);
        rcyCategories = findViewById(R.id.rcyCategories);
        pgrLoading = findViewById(R.id.pgrLoading);

        recuperarCarta();

        // si la taula té una comanda activa, en mostrarem les linies
        if (mComanda != null && mComanda.iteLinies()!=null){
            mLinies.clear();
            hmLinies.clear();

            Iterator<LiniaComanda> iteLinies = mComanda.iteLinies();

            while(iteLinies.hasNext())
            {
                LiniaComanda lc = iteLinies.next();
                mLinies.add(lc);
                hmLinies.put(lc.getItem().getCodi(), lc);
            }
        }

        rcyLinies.setLayoutManager(new LinearLayoutManager(this));
        lcAdapter = new LiniaComandaAdapter(this, mLinies);
        rcyLinies.setAdapter(lcAdapter);

        setResult(RESULT_FIRST_USER); // per defecte en tornar a l'activity anterior no es mostrarà cap missatge
    }


    @Override
    protected void onStop() {
        super.onStop();
        returnCode = RESULT_FIRST_USER;
        setResult(RESULT_FIRST_USER);
        Toast.makeText(getApplicationContext(), "PresaComanda destruida", Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        pgrLoading.setVisibility(isLoading? View.VISIBLE:View.INVISIBLE);
    }





    private void recuperarCarta() {
        // Crida assíncrona per enviar credencials al servidor
        setLoading(true);
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Això és el codi que s'executarà en un fil
            List<Categoria> categories = new ArrayList<>();
            List<Plat> plats = new ArrayList<>();

            getCarta(categories, plats);

            // Poblar hashmaps
            omplirDiccionaris(categories, plats);


            return true;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((infoTaules) -> {
                    //-------------  UI THREAD ---------------------------------------
                    mCategories = new ArrayList<>(hmCategories.values());

                    Log.d("GETCARTA","categories rebudes: "+mCategories);
                    Log.d("GETCARTA","plats rebudes: "+hmPlats.values());

                    rcyCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    rcyCategories.setAdapter(new CategoriaAdapter(this, mCategories));

                    rcyPlats.setLayoutManager(new GridLayoutManager(this, 2)); // 3 columnes
                    rcyPlats.setAdapter(new PlatAdapter(this, this, mPlatsFiltrats));

                    setLoading(false);
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }

    private void omplirDiccionaris(List<Categoria> categories, List<Plat> plats) {
        for(Categoria cat : categories){
            hmCategories.put(cat.getCodi(), cat);
        }
        for(Plat plat : plats){
            // assignem categoria a la que tenim ja carregada en memòria a partir del codi (diccionari)
            long codiCategoria = plat.getCategoria().getCodi();
            plat.setCategoria(hmCategories.get(codiCategoria));

            // ens guardem plat en memòria
            hmPlats.put(plat.getCodi(), plat);
        }
    }


    private void getCarta(List<Categoria> categories, List<Plat> plats) {
        if (categories == null || plats == null){
            Log.d("GETCARTA","llistes categories ó plats sense inicialitzar");
            return;
        }

        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        int qtCategories = 0, qtPlats = 0;
        int res;

        try {
            socket = new Socket(SRVIP, SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Enviem codi d'operació: GETTAULES 2
            oos.writeInt(CodiOperacio.GET_CARTA.getNumVal());
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

            // Llegim qt de categories que ens enviarà el server
            Log.d("SRV", "esperant resposta del server");
            qtCategories = ois.readInt();
            Log.d("SRV", "resposta del server REBUDA");

            for (int i = 0; i < qtCategories; i++) {
                // llegim objecte
                Categoria cat = (Categoria)ois.readObject();
                Log.d("GETCARTA","Categoria recuperada: "+cat.getNom());
                categories.add(cat);

                // enviem ok
                oos.write(new byte[1]);
                oos.flush();
            }


            // Llegim qt de plats que ens enviarà el server
            Log.d("SRV", "esperant resposta del server");
            qtPlats = ois.readInt();
            Log.d("SRV", "resposta del server REBUDA");

            for (int i = 0; i < qtPlats; i++) {
                // llegim objecte
                Plat plat = (Plat)ois.readObject();
                Log.d("GETCARTA","Plat recuperada: "+plat.getNom());
                plats.add(plat);

                // enviem ok
                oos.write(new byte[1]);
                oos.flush();
            }

        } catch (Exception  e) {
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


    private Long createComanda(CreateComandaTuple comandaTuple) {
        if (comandaTuple == null){
            Log.d("createComanda","comandatuple és null");
            return null;
        }

        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        int res;
        Long codiNovaComanda = null;

        try {
            socket = new Socket(SRVIP, SRVPORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Enviem codi d'operació: GETTAULES 2
            oos.writeInt(CodiOperacio.CREATE_COMANDA.getNumVal());
            oos.flush();

            // enviem sessionId
            Log.d("createComanda", "enviant logintuple = "+loginTuple.getSessionId());
//            oos.writeLong(loginTuple.getSessionId());
            oos.writeObject(loginTuple);
            oos.flush();
            Log.d("createComanda", "sessionId enviat");

            // llegim resposta del servidor
            res = ois.readInt();
            // si resposta == KO, avortem operació
            if (res == CodiOperacio.KO.getNumVal()){
                throw new RuntimeException("sessionId erroni, operacio avortada");
            }

            // Enviem comandaTuple
            oos.writeObject(comandaTuple);
            oos.flush();

            Log.d("createComanda", "ESPERANT NOU CODI COMANDA");
            // Recuperem id de nova comanda
            codiNovaComanda = ois.readLong();
            Log.d("createComanda", "CODI COMANDA REBUT = "+codiNovaComanda);
            // enviem ok
            oos.write(new byte[1]);
            oos.flush();
        } catch (Exception  e) {
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
        return codiNovaComanda;
    }


    private void inserirComanda() {
        Log.d("NOVACOMANDA", "inserirComanda");
        setLoading(true);
        // Crida assíncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------

            CreateComandaTuple cct = new CreateComandaTuple(loginTuple.getSessionId(), mTaula, mLinies.size(), mLinies);
            Log.d("NOVACOMANDA", "fil: creant nova comanda");
            Long codiNovaComanda = createComanda(cct);
            Log.d("NOVACOMANDA", "fil: he creat aquesta comanda = "+codiNovaComanda);
            return codiNovaComanda;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((codiNovaComanda) -> {
                    //-------------  UI THREAD ---------------------------------------
                    Log.d("NOVACOMANDA", "fil ha fet la seva feina");
                    Intent i = getIntent();

                    if (codiNovaComanda == -1)
                    {
                        setResult(Activity.RESULT_CANCELED, i);
                    } else {

                        // insert amb èxit
                        Log.d("NOVACOMANDA", "comanda inserida amb èxit. codi = " + codiNovaComanda);
//                        mComanda = new Comanda(codiNovaComanda, null, null, null, false);
                        setResult(Activity.RESULT_OK, i);
                    }
                    // Tornem a activity anterior
                    setLoading(false);
                    finish();
                    // TODO: tornar a l'altra activity
                    //-------------  END OF UI THREAD ---------------------------------------
                });
    }


    @Override
    public void onSelectedCategoria(Categoria seleccionada) {
        mPlatsFiltrats.clear();
        for (Plat plat : hmPlats.values()) {
            if (plat.getCategoria().equals(seleccionada) || seleccionada==null) {
                mPlatsFiltrats.add(plat);
            }
        }
        rcyPlats.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2)); // 3 columnes
        rcyPlats.setAdapter(new PlatAdapter(this, this, mPlatsFiltrats));
    }


    @Override
    public void addLiniaItem(Plat item) {
        if (mComanda!=null){
            Toast.makeText(getApplicationContext(), "No es pot modificar una comanda activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LINIA", "AFEGINT LINIA");
//        LiniaComanda lc = new LiniaComanda(mLinies.size() + 1, 1, EstatLinia.EN_PREPARACIO, item);

        LiniaComanda lc = hmLinies.get(item.getCodi());
        if (lc == null){
            lc = new LiniaComanda(mLinies.size() + 1, 1, EstatLinia.EN_PREPARACIO, item);
            // afegim a hashmap i llista
            hmLinies.put(lc.getItem().getCodi(), lc);
            mLinies.add(lc);
        } else {
            lc.setQuantitat(lc.getQuantitat()+1);
        }

        lcAdapter.notifyDataSetChanged(); // TODO: canviar per algo mes eficient
    }

    @Override
    public void deleteLiniaItem(Plat item) {
        if (mComanda!=null){
            Toast.makeText(getApplicationContext(), "No es pot modificar una comanda activa", Toast.LENGTH_SHORT).show();
            return;
        }

        LiniaComanda lc = hmLinies.get(item.getCodi());
        if (lc != null){
            if (lc.getQuantitat() > 1) {
                // actualitzem quantitat
                lc.setQuantitat(lc.getQuantitat()-1);

            } else {
                // esborrem línia
                hmLinies.remove(lc.getItem().getCodi());
                mLinies.remove(lc);
            }
            lcAdapter.notifyDataSetChanged(); // TODO: canviar per algo mes eficient
        }
    }
}