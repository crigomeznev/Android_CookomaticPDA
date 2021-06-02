package com.example.cookomaticpda.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookomaticpda.PresaComandaActivity;
import com.example.cookomaticpda.R;

import org.cookomatic.model.cuina.Plat;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class PlatAdapter extends RecyclerView.Adapter<PlatAdapter.ViewHolder> {

    private PlatAdapter.OnSelectedItemListener mListener;
    private PresaComandaActivity mActivity;
    private List<Plat> mPlats;

    // Diferents colors segons estat de la Plat
//    private static final int TIPUS_BUIDA = 0;
//    private static final int TIPUS_PROPIA = 1;
//    private static final int TIPUS_ALTRI = 2;


    public PlatAdapter(OnSelectedItemListener listener, PresaComandaActivity activity, List<Plat> plats) {
        mListener = listener;
        mActivity = activity;
        mPlats = plats;

        Log.d("ADAPTER", "plats = " + mPlats);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.plat_item;

        View PlatView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(PlatView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plat plat = mPlats.get(position);

        holder.txvNomPlat.setText(plat.getNom());
        holder.txvPreuPlat.setText(plat.getPreu() + "");

        // TODO: set imatge
        if (plat.getFotoBa() != null){
            Bitmap bmp = BitmapFactory.decodeByteArray(plat.getFotoBa(), 0, plat.getFotoBa().length);
            holder.imvImatgePlat.setImageBitmap(bmp);
            Log.d("PLATADAPTER","s'ha establert la imatge del plat");

        } else {
            Log.d("PLATADAPTER", "plat no té foto");
        }


//        if (plat.getFoto() != null) {
//            try {
//                byte[] image = plat.getFoto().getBytes(0, (int) plat.getFoto().length());
//            } catch (SQLException throwables) {
//                Log.d("PLATADAPTER", "error en carregar foto del plat: " + throwables.getErrorCode(), throwables);
//            }
//        } else {
//            Log.d("PLATADAPTER", "plat no té foto");
//        }
    }


    @Override
    public int getItemCount() {
        return mPlats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txvNomPlat;
        public TextView txvPreuPlat;
        public ImageView imvImatgePlat;

        public ImageButton btnAfegirPlat;
        public ImageButton btnTreurePlat;

        public ViewHolder(@NonNull View platView) {
            super(platView);
            txvNomPlat = platView.findViewById(R.id.txvNomPlat);
            txvPreuPlat = platView.findViewById(R.id.txvPreuPlat);
            imvImatgePlat = platView.findViewById(R.id.imvImatgePlat);
            btnAfegirPlat = platView.findViewById(R.id.btnAfegirPlat);
            btnTreurePlat = platView.findViewById(R.id.btnTreurePlat);


            btnAfegirPlat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Plat", "AFEGIR PLAT: " + mPlats.get(getAdapterPosition()));
//                    mListener.onSelectedItem(mPlats.get(getAdapterPosition()));
//                    mActivity.afegirPlat(mPlats.get(getAdapterPosition()));
                    mListener.addLiniaItem(mPlats.get(getAdapterPosition()));
                }
            });
            btnTreurePlat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Plat", "TREURE PLAT: " + this.toString());
                    mListener.deleteLiniaItem(mPlats.get(getAdapterPosition()));
                }
            });
            // TODO: set on long click listener
        }
    }


    // On selected listener
    public static interface OnSelectedItemListener {
        void addLiniaItem(Plat item);

        void deleteLiniaItem(Plat item);
    }
}
