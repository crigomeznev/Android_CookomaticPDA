package com.example.cookomaticpda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.cookomaticpda.adapters.CategoriaAdapter;
import com.example.cookomaticpda.adapters.LiniaComandaAdapter;
import com.example.cookomaticpda.adapters.PlatAdapter;
import com.example.cookomaticpda.adapters.TaulaAdapter;
import com.example.cookomaticpda.model.cuina.Categoria;
import com.example.cookomaticpda.model.cuina.Plat;
import com.example.cookomaticpda.model.sala.Comanda;
import com.example.cookomaticpda.model.sala.EstatLinia;
import com.example.cookomaticpda.model.sala.LiniaComanda;
import com.example.cookomaticpda.model.sala.Taula;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

//    private PresaComandaActivity mActivity;
//    private LinearLayout llaContainerButtons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presa_comanda);

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

        rcyCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcyCategories.setAdapter(new CategoriaAdapter(this, mCategories));


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


    public void afegirPlat(Plat seleccionat) {
        Log.d("LINIA", "AFEGINT LINIA");
        mLinies.add(new LiniaComanda(mLinies.size() + 1, 1, EstatLinia.EN_PREPARACIO, seleccionat));

//        rcyLinies.setLayoutManager(new LinearLayoutManager(this));
        lcAdapter = new LiniaComandaAdapter(this, mLinies);
        rcyLinies.setAdapter(lcAdapter);
        Log.d("LINIA", "LINIES = " + mLinies);
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