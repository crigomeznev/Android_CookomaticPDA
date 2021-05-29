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
import com.example.cookomaticpda.model.cuina.Plat;
import com.example.cookomaticpda.model.sala.LiniaComanda;

import java.util.List;

public class LiniaComandaAdapter extends RecyclerView.Adapter<LiniaComandaAdapter.ViewHolder> {

    private Activity mActivity;
    private List<LiniaComanda> mLinies;

    // Diferents colors segons estat de la Plat
//    private static final int TIPUS_BUIDA = 0;
//    private static final int TIPUS_PROPIA = 1;
//    private static final int TIPUS_ALTRI = 2;


    public LiniaComandaAdapter(Activity activity, List<LiniaComanda> linies) {
        mActivity = activity;
        mLinies = linies;

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

        holder.txvNum.setText(lc.getNum()+"");
        holder.txvNomPlat.setText(lc.getItem().getNom());
        holder.txvPreuPlat.setText(lc.getItem().getPreu()+"");
    }



    @Override
    public int getItemCount() {
        return mLinies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txvNum;
        public TextView txvNomPlat;
        public TextView txvPreuPlat;

        public ViewHolder(@NonNull View platView) {
            super(platView);
            txvNum = platView.findViewById(R.id.txvNum);
            txvNomPlat = platView.findViewById(R.id.txvNomPlat);
            txvPreuPlat = platView.findViewById(R.id.txvPreuPlat);
        }
    }





    // On selected listener
    public static interface OnSelectedItemListener {
        void onSelectedItem(Plat seleccionat);
    }
}
