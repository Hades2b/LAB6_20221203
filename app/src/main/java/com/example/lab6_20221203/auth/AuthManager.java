package com.example.lab6_20221203.auth;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private final FirebaseAuth auth;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface AuthListener {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("es");
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    // Correo y contrasena
    public void loginWithEmail(String email, String password, AuthListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess(auth.getCurrentUser());
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error de inicio de sesión";
                        Log.e(TAG, "Error en loginWithEmail: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    public void registerWithEmail(String email, String password, AuthListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess(auth.getCurrentUser());
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error de registro";
                        Log.e(TAG, "Error en registerWithEmail: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    // Google
    public void startGoogleSignIn(Activity activity, AuthListener listener) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getWebClientId(activity))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result, listener);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Error en startGoogleSignIn: " + e.getMessage(), e);
                        listener.onError("Error al iniciar sesión con Google: " + e.getMessage());
                    }
                });
    }

    private void handleGoogleSignInResult(GetCredentialResponse result, AuthListener listener) {
        Credential credential = result.getCredential();
        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
        String idToken = googleIdTokenCredential.getIdToken();
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess(auth.getCurrentUser());
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error al autenticar con Google";
                        Log.e(TAG, "Error en handleGoogleSignInResult: " + errorMsg, task.getException());
                        listener.onError(errorMsg);
                    }
                });
    }

    private String getWebClientId(Activity activity) {
        try {
            int id = activity.getResources().getIdentifier("default_web_client_id", "string", activity.getPackageName());
            if (id != 0) {
                return activity.getString(id);
            } else {
                Log.e(TAG, "No se encontró el recurso default_web_client_id");
                return "";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener el Web Client ID: " + e.getMessage(), e);
            return "";
        }
    }

    // GitHub
    public void startGitHubSignIn(Activity activity, AuthListener listener) {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

        Task<AuthResult> pendingResultTask = auth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(executor, authResult -> {
                        Log.d(TAG, "GitHub sign-in completed (pending).");
                        listener.onSuccess(authResult.getUser());
                    })
                    .addOnFailureListener(executor, e -> {
                        Log.e(TAG, "Error en GitHub sign-in (pending): " + e.getMessage(), e);
                        listener.onError(e.getMessage());
                    });
        } else {
            auth.startActivityForSignInWithProvider(activity, provider.build())
                    .addOnSuccessListener(executor, authResult -> {
                        Log.d(TAG, "GitHub sign-in completed successfully.");
                        listener.onSuccess(authResult.getUser());
                    })
                    .addOnFailureListener(executor, e -> {
                        Log.e(TAG, "Error en GitHub sign-in: " + e.getMessage(), e);
                        listener.onError(e.getMessage());
                    });
        }
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }
}