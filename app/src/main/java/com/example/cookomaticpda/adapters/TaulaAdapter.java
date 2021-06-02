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

import org.cookomatic.model.sala.Cambrer;
import org.cookomatic.model.sala.Taula;

import java.util.List;

public class TaulaAdapter extends RecyclerView.Adapter<TaulaAdapter.ViewHolder> {

    private OnSelectedItemListener mListener;
    private List<Taula> mTaules;

    // Diferents colors segons estat de la taula
    private static final int TIPUS_BUIDA = 0;
    private static final int TIPUS_PROPIA = 1;
    private static final int TIPUS_ALTRI = 2;


    public TaulaAdapter(OnSelectedItemListener listener, List<Taula> taules) {
        mListener = listener;
        mTaules = taules;

        Log.d("ADAPTER","taules = "+ mTaules);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        switch (viewType) {
            case TIPUS_BUIDA:   layout = R.layout.taula_item_buida; break;
            case TIPUS_PROPIA:  layout = R.layout.taula_item_propia; break;
            case TIPUS_ALTRI:   layout = R.layout.taula_item_buida; break;
            default:throw new RuntimeException("tipus no suportat");
        }

        View taulaView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(taulaView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Taula t = mTaules.get(position);
        holder.txvNumTaula.setText(t.getNumero()+"");

        if (t.getComandaActiva() != null) {
            Cambrer cambrer = t.getComandaActiva().getCambrer();
            String cambrerS = "BUIDA";
            if (cambrer != null)
                cambrerS = cambrer.getCognom1();
            holder.txvCambrer.setText(cambrerS);
        } else {
            holder.txvCambrer.setText("BUIDA");
        }
    }

    // Afegit per diferenciar entre taula amb comanda d'altri,
    // taula amb comanda pròpia i taula buida


    @Override
    public int getItemViewType(int position) {
        Taula taula = mTaules.get(position);

        if (taula.getComandaActiva() == null)
            return TIPUS_BUIDA;
        else
            return TIPUS_PROPIA;
        // TODO: tipus d'altri
    }

    @Override
    public int getItemCount() {
        return mTaules.size();
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

//                    int anticIdxSeleccionat = idxPersonatgeSeleccionat;
//                    idxPersonatgeSeleccionat = getAdapterPosition();
//                    notifyItemChanged(anticIdxSeleccionat);
//                    notifyItemChanged(idxPersonatgeSeleccionat);

                    if(mListener!=null) {
                        mListener.onSelectedItem(mTaules.get(getAdapterPosition()));
                    }
                }
            });
            // TODO: set on long click listener
        }
    }



    // On selected listener
    public static interface OnSelectedItemListener {
        void onSelectedItem(Taula seleccionada);
    }
}
