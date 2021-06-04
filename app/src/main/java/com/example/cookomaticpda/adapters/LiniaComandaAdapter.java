package com.example.cookomaticpda.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookomaticpda.R;

import org.cookomatic.model.cuina.Plat;
import org.cookomatic.model.sala.LiniaComanda;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LiniaComandaAdapter extends RecyclerView.Adapter<LiniaComandaAdapter.ViewHolder> {

    private Activity mActivity;
    private List<LiniaComanda> mLinies;
    private NumberFormat nf;

    // Diferents colors segons estat de la Plat
//    private static final int TIPUS_BUIDA = 0;
//    private static final int TIPUS_PROPIA = 1;
//    private static final int TIPUS_ALTRI = 2;


    public LiniaComandaAdapter(Activity activity, List<LiniaComanda> linies) {
        mActivity = activity;
        mLinies = linies;
        nf = NumberFormat.getCurrencyInstance();

        Log.d("ADAPTER","linies = "+ mLinies);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.linia_comanda_item;

        View PlatView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(PlatView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LiniaComanda lc = mLinies.get(position);

        holder.txvQuantitat.setText(lc.getQuantitat()+"");
        holder.txvNomPlat.setText(lc.getItem().getNom());
        holder.txvPreuPlat.setText(nf.format(lc.getItem().getPreu()));
        holder.txvSubtotal.setText(nf.format(lc.getImport())+"€");

    }



    @Override
    public int getItemCount() {
        return mLinies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txvQuantitat;
        public TextView txvNomPlat;
        public TextView txvPreuPlat;
        public TextView txvSubtotal;

        public ViewHolder(@NonNull View platView) {
            super(platView);
            txvQuantitat = platView.findViewById(R.id.txvQuantitat);
            txvNomPlat = platView.findViewById(R.id.txvNomPlat);
            txvPreuPlat = platView.findViewById(R.id.txvPreuPlat);
            txvSubtotal = platView.findViewById(R.id.txvSubtotal);
        }
    }





    // On selected listener
    public static interface OnSelectedItemListener {
        void onSelectedItem(Plat seleccionat);
    }
}
