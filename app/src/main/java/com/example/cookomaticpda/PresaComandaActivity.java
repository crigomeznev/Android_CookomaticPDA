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
import android.widget.LinearLayout;

import com.example.cookomaticpda.adapters.CategoriaAdapter;
import com.example.cookomaticpda.adapters.InfoTaulaAdapter;
import com.example.cookomaticpda.adapters.LiniaComandaAdapter;
import com.example.cookomaticpda.adapters.PlatAdapter;
import com.example.cookomaticpda.adapters.TaulaAdapter;
import com.example.cookomaticpda.model.cuina.Categoria;
import com.example.cookomaticpda.model.cuina.Plat;
import com.example.cookomaticpda.model.sala.Comanda;
import com.example.cookomaticpda.model.sala.EstatLinia;
import com.example.cookomaticpda.model.sala.LiniaComanda;
import com.example.cookomaticpda.model.sala.Taula;

import org.cookomatic.protocol.CodiOperacio;
import org.cookomatic.protocol.InfoTaula;
import org.cookomatic.protocol.LoginTuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
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

    private List<Plat> mPlats;
    private List<Plat> mPlatsFiltrats;
    private List<LiniaComanda> mLinies;
    private Comanda mComanda;

    private RecyclerView rcyPlats;
    private RecyclerView rcyLinies;
    private RecyclerView rcyCategories;

    private LiniaComandaAdapter lcAdapter;

    private LoginTuple loginTuple;
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
        Log.d("INTENT","logintuple recuperat: "+loginTuple.getUser()+"/"+loginTuple.getPassword()+" SID:"+loginTuple.getSessionId());

        // TODO: Borrar
        iniCategories();
        iniPlats();
        iniLinies();

        rcyPlats = findViewById(R.id.rcyPlats);
        rcyLinies = findViewById(R.id.rcyLinies);
        rcyCategories = findViewById(R.id.rcyCategories);

        rcyPlats.setLayoutManager(new GridLayoutManager(this, 2)); // 3 columnes
        rcyPlats.setAdapter(new PlatAdapter(this, this, mPlatsFiltrats));

        rcyLinies.setLayoutManager(new LinearLayoutManager(this));
        lcAdapter = new LiniaComandaAdapter(this, mLinies);
        rcyLinies.setAdapter(lcAdapter);


        recuperarCarta();
//        rcyCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        rcyCategories.setAdapter(new CategoriaAdapter(this, mCategories));


    }


    // TODO: borrar
    private void iniCategories() {
        mCategories = new ArrayList<>();

        mCategories.add(new Categoria(1, "Entrants", 1));
        mCategories.add(new Categoria(2, "Primers", 1));
        mCategories.add(new Categoria(3, "Segons", 1));
        mCategories.add(new Categoria(4, "Postres", 1));

//        btnsCategories = new ArrayList<>();
//        for(Categoria c : mCategories){
//            Button b = new Button(getApplicationContext());
//            b.setText(c.getNom());
//            b.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mPlatsFiltrats.clear();
//                    for(Plat plat: mPlats){
//                        if (plat.getCategoria().equals(c)){
//                            mPlatsFiltrats.add(plat);
//                        }
//                    }
//                    rcyPlats.setLayoutManager(new GridLayoutManager(getApplicationContext(),2)); // 3 columnes
//                    rcyPlats.setAdapter(new PlatAdapter(null, mActivity, mPlatsFiltrats));
//                }
//            });
//            btnsCategories.add(b);
//        }
//
//        llaContainerButtons.addChildrenForAccessibility(btnsCategories);
    }

    // TODO: borrar
    private void iniPlats() {
        mPlats = new ArrayList<>();
        mPlatsFiltrats = new ArrayList<>();

        mPlats.add(new Plat(1, "Patates fregides", "patates fregides", new BigDecimal(2.00),
                null, true, mCategories.get(0), null));
        mPlats.add(new Plat(1, "Amanida", "Amanida", new BigDecimal(2.00),
                null, true, mCategories.get(0), null));
        mPlats.add(new Plat(1, "Gaspatxo", "patates fregides", new BigDecimal(2.00),
                null, true, mCategories.get(1), null));
        mPlats.add(new Plat(1, "Pollastre", "patates fregides", new BigDecimal(2.00),
                null, true, mCategories.get(2), null));
        mPlats.add(new Plat(1, "Coulant de xocolata", "patates fregides", new BigDecimal(2.00),
                null, true, mCategories.get(3), null));
    }

    // TODO: borrar
    private void iniLinies() {
        mLinies = new ArrayList<>();

//        mLinies.add(new LiniaComanda(1, 2, EstatLinia.EN_PREPARACIO, mPlats.get(0)));
//        mLinies.add(new LiniaComanda(2, 2, EstatLinia.EN_PREPARACIO, mPlats.get(2)));
//        mLinies.add(new LiniaComanda(3, 3, EstatLinia.EN_PREPARACIO, mPlats.get(4)));
//        mLinies.add(new LiniaComanda(4, 2, EstatLinia.EN_PREPARACIO, mPlats.get(3)));
    }



    private void recuperarCarta() {
        // Crida assíncrona per enviar credencials al servidor
        Observable.fromCallable(() -> {
            //---------------- START OF THREAD ------------------------------------
            // Això és el codi que s'executarà en un fil
            List<Categoria> categories = new ArrayList<>();
            List<Plat> plats = new ArrayList<>();

            getCarta(categories, plats);

            mCategories = categories;
            mPlats = plats;

            return true;
            //--------------- END OF THREAD-------------------------------------
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((infoTaules) -> {
                    //-------------  UI THREAD ---------------------------------------
                    Log.d("GETCARTA","categories rebudes: "+mCategories);
                    Log.d("GETCARTA","plats rebudes: "+mPlats);

                    rcyCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    rcyCategories.setAdapter(new CategoriaAdapter(this, mCategories));


                    // construir recycler taules
                    //-------------  END OF UI THREAD ---------------------------------------
                });
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

            // Llegim qt de taules que ens enviarà el server
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


            // TODO: recuperar plats

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




    @Override
    public void onSelectedCategoria(Categoria seleccionada) {
        mPlatsFiltrats.clear();
        for (Plat plat : mPlats) {
            if (plat.getCategoria().equals(seleccionada)) {
                mPlatsFiltrats.add(plat);
            }
        }
        rcyPlats.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2)); // 3 columnes
        rcyPlats.setAdapter(new PlatAdapter(this, this, mPlatsFiltrats));
    }

    @Override
    public void onSelectedPlat(Plat seleccionat) {
        Log.d("LINIA", "AFEGINT LINIA");
        mLinies.add(new LiniaComanda(mLinies.size() + 1, 1, EstatLinia.EN_PREPARACIO, seleccionat));

        lcAdapter.notifyDataSetChanged();

//        rcyLinies.setLayoutManager(new LinearLayoutManager(this));
//        lcAdapter = new LiniaComandaAdapter(this, mLinies);
//        rcyLinies.setAdapter(lcAdapter);
        Log.d("LINIA", "LINIES = " + mLinies);
    }
}