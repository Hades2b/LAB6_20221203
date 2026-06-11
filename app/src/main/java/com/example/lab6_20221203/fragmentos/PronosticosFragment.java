package com.example.lab6_20221203.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20221203.R;
import com.example.lab6_20221203.PrediccionFormActivity;
import com.example.lab6_20221203.adapters.PrediccionAdapter;
import com.example.lab6_20221203.entity.Prediccion;
import com.example.lab6_20221203.repository.PrediccionRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PronosticosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PrediccionAdapter adapter;
    private PrediccionRepository repository;
    private ListenerRegistration firestoreListener;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pronosticos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_pronosticos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PrediccionAdapter(
                this::onEditClick,
                this::onDeleteClick
        );
        recyclerView.setAdapter(adapter);

        repository = new PrediccionRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PrediccionFormActivity.class));
        });
        startFirestoreListener();
    }

    private void startFirestoreListener() {
        Query query = repository.getPredictionsQuery(userId);
        firestoreListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error al cargar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshots != null) {
                List<Prediccion> lista = new ArrayList<>();
                for (var doc : snapshots.getDocuments()) {
                    Prediccion p = doc.toObject(Prediccion.class);
                    p.setId(doc.getId());
                    lista.add(p);
                }
                adapter.updateList(lista);
            }
        });
    }

    private void onEditClick(Prediccion p) {
        Intent intent = new Intent(getActivity(), PrediccionFormActivity.class);
        intent.putExtra(PrediccionFormActivity.EXTRA_PREDICCION_ID, p.getId());
        startActivity(intent);
    }

    private void onDeleteClick(Prediccion prediccion) {
        Log.i("PronosticosFragment", "Eliminando pronostico con ID: " + prediccion.getId());
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar pronóstico")
                .setMessage("¿Estás seguro de eliminar este pronóstico?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repository.deletePrediction(prediccion.getId())
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}