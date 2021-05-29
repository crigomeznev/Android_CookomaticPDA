package com.example.cookomaticpda.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookomaticpda.R;
import com.example.cookomaticpda.model.sala.Taula;

import org.cookomatic.protocol.InfoTaula;

import java.util.List;

public class InfoTaulaAdapter extends RecyclerView.Adapter<InfoTaulaAdapter.ViewHolder> {

    private OnSelectedItemListener mListener;
    private List<InfoTaula> mInfoTaules;
    private String mUserLogin;

    // Diferents colors segons estat de la taula
    private static final int TIPUS_BUIDA = 0;
    private static final int TIPUS_PROPIA = 1;
    private static final int TIPUS_ALTRI = 2;


    public InfoTaulaAdapter(OnSelectedItemListener listener, List<InfoTaula> infoTaules, String userLogin) {
        mListener = listener;
        mInfoTaules = infoTaules;
        mUserLogin = userLogin;

        Log.d("ADAPTER","infoTaules = "+ mInfoTaules);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        switch (viewType) {
            case TIPUS_BUIDA:   layout = R.layout.taula_item_buida; break;
            case TIPUS_PROPIA:  layout = R.layout.taula_item_propia; break;
            case TIPUS_ALTRI:   layout = R.layout.taula_item_altri; break;
            default:throw new RuntimeException("tipus no suportat");
        }

        View taulaView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(taulaView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InfoTaula it = mInfoTaules.get(position);
        holder.txvNumTaula.setText(it.getNumero()+"");

        if (it.getCodiComanda() != null) {
            // TODO: mostrar el nom del cambrer, no el login
            holder.txvCambrer.setText(it.getNomCambrer());
        } else {
            holder.txvCambrer.setText("BUIDA");
        }
    }

    // Afegit per diferenciar entre taula amb comanda d'altri,
    // taula amb comanda pròpia i taula buida


    @Override
    public int getItemViewType(int position) {
        InfoTaula infoTaula = mInfoTaules.get(position);
        int tipus;

        Log.d("ADAPTER","current infotaula = "+infoTaula.getNumero()+"/"+infoTaula.getNomCambrer()+"--es meva="+infoTaula.isEsMeva()+"--comanda="+infoTaula.getCodiComanda());

        // codi comanda = 0 -> taula sense comanda actual
        if (infoTaula.getCodiComanda() == 0 || infoTaula.getNomCambrer()==null)
            tipus = TIPUS_BUIDA;
        else if (infoTaula.isEsMeva())
            tipus = TIPUS_PROPIA;
        else
            tipus = TIPUS_ALTRI;

        Log.d("ADAPTER","\ttipus = "+tipus);

        return tipus;
    }

    @Override
    public int getItemCount() {
        return mInfoTaules.size();
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
                        mListener.onSelectedInfoTaula(mInfoTaules.get(getAdapterPosition()));
                    }
                }
            });
            // TODO: set on long click listener
        }
    }



    // On selected listener
    public static interface OnSelectedItemListener {
        void onSelectedInfoTaula(InfoTaula seleccionada);
    }
}
