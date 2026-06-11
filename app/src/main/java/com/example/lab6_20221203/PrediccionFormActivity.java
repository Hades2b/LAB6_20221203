package com.example.lab6_20221203;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab6_20221203.R;
import com.example.lab6_20221203.entity.Prediccion;
import com.example.lab6_20221203.repository.PrediccionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrediccionFormActivity extends AppCompatActivity {

    public static final String EXTRA_PREDICCION_ID = "prediccion_id";

    private AutoCompleteTextView actvTeamA, actvTeamB;
    private Spinner spinnerEstado;
    private TextInputEditText etFecha, etGolesA, etGolesB;
    private MaterialButton btnGuardar;
    private TextInputLayout layoutEstado;
    private TextView tvTitulo;
    private ImageButton btnBack;
    private Calendar selectedDateTime = Calendar.getInstance();
    private String prediccionId;
    private boolean isEditMode = false;
    private PrediccionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediccion_form);

        // Enlazar vistas
        btnBack = findViewById(R.id.btn_back);
        tvTitulo = findViewById(R.id.tv_titulo);
        actvTeamA = findViewById(R.id.autocomplete_a);
        actvTeamB = findViewById(R.id.autocomplete_b);
        etFecha = findViewById(R.id.et_fecha);
        etGolesA = findViewById(R.id.et_goles_a);
        etGolesB = findViewById(R.id.et_goles_b);
        spinnerEstado = findViewById(R.id.spinner_estado);
        layoutEstado = findViewById(R.id.layout_estado);
        btnGuardar = findViewById(R.id.btn_guardar);

        repository = new PrediccionRepository();

        // Configurar adaptadores para los equipos
        ArrayAdapter<CharSequence> teamAdapter = ArrayAdapter.createFromResource(this,
                R.array.teams, android.R.layout.simple_dropdown_item_1line);
        actvTeamA.setAdapter(teamAdapter);
        actvTeamB.setAdapter(teamAdapter);

        createNotificationChannel();

        // Configurar DatePicker
        etFecha.setOnClickListener(v -> showDateTimePicker());

        // Verificar si es modo edición
        prediccionId = getIntent().getStringExtra(EXTRA_PREDICCION_ID);
        if (prediccionId != null && !prediccionId.isEmpty()) {
            isEditMode = true;
            tvTitulo.setText("Editar pronóstico");
            layoutEstado.setVisibility(android.view.View.VISIBLE);
            loadPredictionData();
        } else {
            tvTitulo.setText("Nuevo pronóstico");
        }

        btnGuardar.setOnClickListener(v -> savePrediction());

        btnBack.setOnClickListener(v -> finish());
    }

    private void showDateTimePicker() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    showTimePicker();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    // Formato dd/MM/yyyy HH:mm
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    etFecha.setText(sdf.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // 24h
        ).show();
    }

    private void loadPredictionData() {
        Log.i("PrediccionFormActivity", "Cargando pronóstico con ID: " + prediccionId);
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("predicciones")
                .document(prediccionId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Prediccion p = doc.toObject(Prediccion.class);
                        if (p != null) {
                            Log.i("PrediccionFormActivity", "Cargando datos del pronóstico encontrado");
                            actvTeamA.setText(p.getSeleccionA());
                            actvTeamB.setText(p.getSeleccionB());
                            if (p.getFecha() != null) {
                                Log.i("PrediccionFormActivity", "Fecha: " + p.getFecha());
                                etFecha.setText(p.getFormattedDate());
                            }
                            etGolesA.setText(String.valueOf(p.getGolesA()));
                            etGolesB.setText(String.valueOf(p.getGolesB()));
                            if (isEditMode) {
                                spinnerEstado.setSelection(2);
                            }
                        }
                    }
                });
    }

    private void savePrediction() {
        String teamA = actvTeamA.getText().toString().trim();
        String teamB = actvTeamB.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();
        String golesAStr = etGolesA.getText().toString().trim();
        String golesBStr = etGolesB.getText().toString().trim();

        if (TextUtils.isEmpty(teamA) || TextUtils.isEmpty(teamB) ||
                TextUtils.isEmpty(fechaStr) || TextUtils.isEmpty(golesAStr) || TextUtils.isEmpty(golesBStr)) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (teamA.equals(teamB)) {
            Toast.makeText(this, "Las selecciones no pueden ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        int golesA = Integer.parseInt(golesAStr);
        int golesB = Integer.parseInt(golesBStr);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date date;
        try {
            date = sdf.parse(fechaStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        Timestamp timestamp = new Timestamp(date);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String estado = isEditMode ? spinnerEstado.getSelectedItem().toString() : "Pendiente";

        Prediccion prediccion = new Prediccion(userId, teamA, teamB, timestamp, golesA, golesB, estado);

        if (isEditMode) {
            repository.updatePrediction(prediccionId, prediccion)
                    .addOnSuccessListener(aVoid -> {
                        showNotification( "Pronóstico actualizado", "Los datos del pronóstico se modificaron exitosamente.");                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        } else {
            repository.addPrediction(prediccion)
                    .addOnSuccessListener(aVoid -> {
                        showNotification("Pronóstico registrado", "Se ha creado un nuevo pronóstico correctamente.");                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        }
    }

    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("pronosticos_channel",
                    "Pronosticos",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Canal para notificaciones con prioridad default");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            //
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                    ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(PrediccionFormActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "pronosticos_channel")
                .setSmallIcon(R.drawable.sports_soccer_24px) // Reemplaza con tu icono
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

}