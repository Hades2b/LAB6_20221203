package com.example.lab6_20221203.entity;

import java.io.Serializable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;

public class Prediccion implements Serializable {

    @Exclude
    private String id;
    private String userId;
    private String seleccionA;
    private String seleccionB;
    private Timestamp fecha;
    private int golesA;
    private int golesB;
    private String estado="Pendiente"; // Acertado, Fallado


    public Prediccion() {}

    public Prediccion(String userId, String seleccionB, String seleccionA, Timestamp fecha, int golesA, int golesB, String estado) {
        this.userId = userId;
        this.seleccionB = seleccionB;
        this.seleccionA = seleccionA;
        this.fecha = fecha;
        this.golesA = golesA;
        this.golesB = golesB;
        this.estado = estado;
    }

    @Exclude
    public String getId() {
        return id;
    }
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSeleccionA() {
        return seleccionA;
    }
    public void setSeleccionA(String seleccionA) {
        this.seleccionA = seleccionA;
    }

    public String getSeleccionB() {
        return seleccionB;
    }
    public void setSeleccionB(String seleccionB) {
        this.seleccionB = seleccionB;
    }

    public Timestamp getFecha() {
        return fecha;
    }
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public int getGolesA() {
        return golesA;
    }
    public void setGolesA(int golesA) {
        this.golesA = golesA;
    }

    public int getGolesB() {
        return golesB;
    }
    public void setGolesB(int golesB) {
        this.golesB = golesB;
    }

    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }


    @Exclude
    public boolean isPending() {
        return "Pendiente".equals(estado);
    }

    @Exclude
    public String getFormattedDate() {
        if (fecha == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return dateFormat.format(fecha.toDate());
    }

}
