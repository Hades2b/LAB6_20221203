package com.example.lab6_20221203.repository;

import com.example.lab6_20221203.entity.Prediccion;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PrediccionRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference predictionsRef;

    public PrediccionRepository() {
        predictionsRef = db.collection("predicciones");
    }

    public Query getPredictionsQuery(String userId) {
        return predictionsRef.whereEqualTo("userId", userId).orderBy("fecha", Query.Direction.ASCENDING);
    }

    public Task<Void> addPrediction(Prediccion prediccion) {
        return predictionsRef.add(prediccion).continueWith(task -> null);
    }

    public Task<Void> updatePrediction(String id, Prediccion prediccion) {
        return predictionsRef.document(id).set(prediccion);
    }

    public Task<Void> deletePrediction(String id) {
        return predictionsRef.document(id).delete();
    }
}