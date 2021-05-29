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

import org.milaifontanals.cookomatic.model.cuina.Categoria;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private Activity mActivity;
    private OnSelectedItemListener mListener;
    private List<Categoria> mCategories;

    // Diferents colors segons estat de la Plat
//    private static final int TIPUS_BUIDA = 0;
//    private static final int TIPUS_PROPIA = 1;
//    private static final int TIPUS_ALTRI = 2;


//    public CategoriaAdapter(Activity activity, List<Categoria> categories) {
//        mActivity = activity;
//        mCategories = categories;
//
//        Log.d("ADAPTER","categories = "+ mCategories);
//    }

    public CategoriaAdapter(OnSelectedItemListener listener, List<Categoria> categories) {
        mListener = listener;
        mCategories = categories;

        Log.d("ADAPTER","categories = "+ mCategories);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.categoria_item;

        View PlatView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(PlatView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categoria categoria = mCategories.get(position);

        holder.txvNomCategoria.setText(categoria.getNom());
    }



    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txvNomCategoria;

        public ViewHolder(@NonNull View platView) {
            super(platView);
            txvNomCategoria = platView.findViewById(R.id.txvNomCategoria);

            platView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSelectedCategoria(mCategories.get(getAdapterPosition()));
                }
            });
        }
    }





    // On selected listener
    public static interface OnSelectedItemListener {
        void onSelectedCategoria(Categoria seleccionada);
    }
}
