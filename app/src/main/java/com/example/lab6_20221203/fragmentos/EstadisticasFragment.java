package com.example.lab6_20221203.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lab6_20221203.R;
import com.example.lab6_20221203.entity.Prediccion;
import com.example.lab6_20221203.repository.PrediccionRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

public class EstadisticasFragment extends Fragment {

    private TextView txtTotal, txtAcertados, txtFallados, txtPendientes;

    private PrediccionRepository repository;

    private ListenerRegistration predictionsListener;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        repository = new PrediccionRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtTotal = view.findViewById(R.id.txt_total);
        txtAcertados = view.findViewById(R.id.txt_acertados);
        txtFallados = view.findViewById(R.id.txt_fallados);
        txtPendientes = view.findViewById(R.id.txt_pendientes);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i("EstadisticasFragment", "userId: " + userId);
        startListening();
    }

    private void startListening() {
        Log.i("EstadisticasFragment", "Listening para: " + userId);
        Query query = repository.getPredictionsQuery(userId);
        predictionsListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error al cargar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshots != null) {
                List<Prediccion> predicciones = snapshots.toObjects(Prediccion.class);
                for (Prediccion p : predicciones) {
                    Log.i("EstadisticasFragment", "Prediccion: " + p.toString());}
                updateStatistics(predicciones);
            } else Log.i("EstadisticasFragment", "snapshots es null");
        });
    }

    private void updateStatistics(List<Prediccion> predicciones) {
        int total = predicciones.size();
        int acertados = 0, fallados = 0, pendientes = 0;
        for (Prediccion p : predicciones) {
            switch (p.getEstado()) {
                case "Acertado": acertados++; break;
                case "Fallado": fallados++; break;
                default: pendientes++;
            }
        }
        txtTotal.setText(String.valueOf(total));
        txtAcertados.setText(String.valueOf(acertados));
        txtFallados.setText(String.valueOf(fallados));
        txtPendientes.setText(String.valueOf(pendientes));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (predictionsListener != null) {
            predictionsListener.remove();
        }
    }
}