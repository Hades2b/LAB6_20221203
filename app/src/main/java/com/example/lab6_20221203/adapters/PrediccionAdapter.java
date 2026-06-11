package com.example.lab6_20221203.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20221203.R;
import com.example.lab6_20221203.entity.Prediccion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrediccionAdapter extends RecyclerView.Adapter<PrediccionAdapter.ViewHolder> {

    private List<Prediccion> predicciones = new ArrayList<>();
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnEditClickListener {
        void onEdit(Prediccion prediccion);
    }

    public interface OnDeleteClickListener {
        void onDelete(Prediccion prediccion);
    }

    public PrediccionAdapter(OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    public void updateList(List<Prediccion> newList) {
        this.predicciones = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prediccion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prediccion p = predicciones.get(position);
        holder.tvEquipos.setText(p.getSeleccionA() + " vs " + p.getSeleccionB());
        if (p.getFecha() != null) {
            holder.tvFecha.setText(p.getFormattedDate());
        }
        holder.tvResultado.setText(p.getGolesA() + " - " + p.getGolesB());
        holder.chipEstado.setText(p.getEstado());
        // Cambiar color del chip según estado
        int chipColor;
        switch (p.getEstado()) {
            case "Acertado":
                chipColor = R.color.chip_acertado;
                break;
            case "Fallado":
                chipColor = R.color.chip_fallado;
                break;
            default:
                chipColor = R.color.chip_pendiente;
        }
        holder.chipEstado.setChipBackgroundColorResource(chipColor);

        boolean isPending = "Pendiente".equals(p.getEstado());
        holder.btnEditar.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnEliminar.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.divider.setVisibility(isPending ? View.VISIBLE : View.GONE);

        holder.btnEditar.setOnClickListener(v -> editClickListener.onEdit(p));
        holder.btnEliminar.setOnClickListener(v -> deleteClickListener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return predicciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipos, tvFecha, tvResultado;
        Chip chipEstado;
        View divider;
        MaterialButton btnEditar, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipos = itemView.findViewById(R.id.tv_equipos);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvResultado = itemView.findViewById(R.id.tv_resultado);
            chipEstado = itemView.findViewById(R.id.chip_estado);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}