package com.example.lab6_20221203;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.lab6_20221203.auth.AuthManager;
import com.example.lab6_20221203.fragmentos.EstadisticasFragment;
import com.example.lab6_20221203.fragmentos.PronosticosFragment;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private Fragment misPronosticosFragment;
    private Fragment estadisticasFragment;
    private MaterialButton btnMisPronosticos;
    private MaterialButton btnEstadisticas;
    private MaterialButton btnCerrarSesion;

    private static final String KEY_SELECTED_OPTION = "selected_option";
    private static final int OPTION_MIS_PRONOSTICOS = 0;
    private static final int OPTION_ESTADISTICAS = 1;
    private int currentOption = OPTION_MIS_PRONOSTICOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        misPronosticosFragment = new PronosticosFragment();
        estadisticasFragment = new EstadisticasFragment();

        btnMisPronosticos = findViewById(R.id.btn_mis_pronosticos);
        btnEstadisticas = findViewById(R.id.btn_estadisticas);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);

        if (savedInstanceState != null) {
            currentOption = savedInstanceState.getInt(KEY_SELECTED_OPTION, OPTION_MIS_PRONOSTICOS);
        }

        loadFragment(currentOption);
        setActiveButton(currentOption);

        btnMisPronosticos.setOnClickListener(v -> {
            if (currentOption != OPTION_MIS_PRONOSTICOS) {
                currentOption = OPTION_MIS_PRONOSTICOS;
                loadFragment(OPTION_MIS_PRONOSTICOS);
                setActiveButton(OPTION_MIS_PRONOSTICOS);
            }
        });

        btnEstadisticas.setOnClickListener(v -> {
            if (currentOption != OPTION_ESTADISTICAS) {
                currentOption = OPTION_ESTADISTICAS;
                loadFragment(OPTION_ESTADISTICAS);
                setActiveButton(OPTION_ESTADISTICAS);
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            AuthManager.getInstance().logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
        }
    }


    private void loadFragment(int option) {
        Fragment fragment;
        if (option == OPTION_MIS_PRONOSTICOS) {
            fragment = misPronosticosFragment;
        } else {
            fragment = estadisticasFragment;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    private void setActiveButton(int option) {
        resetButtonStyle(btnMisPronosticos);
        resetButtonStyle(btnEstadisticas);

        MaterialButton activeButton = (option == OPTION_MIS_PRONOSTICOS) ? btnMisPronosticos : btnEstadisticas;

        activeButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.white_20)));
        activeButton.setTypeface(activeButton.getTypeface(), Typeface.BOLD);
    }

    private void resetButtonStyle(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        button.setTypeface(button.getTypeface(), Typeface.NORMAL);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_OPTION, currentOption);
    }
}