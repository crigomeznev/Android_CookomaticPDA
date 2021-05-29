package com.example.cookomaticpda.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookomaticpda.R;

import org.milaifontanals.cookomatic.model.sala.Comanda;

import java.util.List;

public class ComandaAdapter extends RecyclerView.Adapter<ComandaAdapter.ViewHolder> {

    private Activity mActivity;
    private List<Comanda> mComandes;

    public ComandaAdapter(Activity activity, List<Comanda> comandes) {
        mActivity = activity;
        mComandes = comandes;

        Log.d("ADAPTER","comandes = "+mComandes);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.taula_item_propia;

        View taulaView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(taulaView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comanda c = mComandes.get(position);
        holder.txvNumTaula.setText(c.getTaula()+"");
        holder.txvCambrer.setText(c.getCambrer().getCognom1());
    }

    @Override
    public int getItemCount() {
        return mComandes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txvNumTaula;
        public TextView txvCambrer;

        public ViewHolder(@NonNull View taulaView) {
            super(taulaView);
            txvNumTaula = taulaView.findViewById(R.id.txvNumTaula);
            txvCambrer = taulaView.findViewById(R.id.txvCambrer);

            taulaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TAULA","Taula: m'han clicat");
                }
            });
            // TODO: set on long click listener
        }
    }
}
