package com.leaden2.app;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Transición para solicitudes de llamada desde segundo plano.
 * Enciende la pantalla, mantiene visible el bloqueo normal de Android y
 * espera a que el usuario desbloquee manualmente. Solo entonces abre WhatsApp.
 */
public class WakeAndLaunchActivity extends Activity {
    private static final String TAG = "ORVEXWakeLaunch";
    public static final String EXTRA_CONTACT_NAME = "orvex_contact_name";
    public static final String EXTRA_PHONE = "orvex_contact_phone";
    private boolean launchHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWakeScreenWithoutCoveringKeyguard();
        continueAfterUnlock();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        launchHandled = false;
        continueAfterUnlock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al desbloquear manualmente, Android devuelve la actividad al primer plano.
        continueAfterUnlock();
    }

    private void configureWakeScreenWithoutCoveringKeyguard() {
        Window window = getWindow();
        // Encender y mantener despierta la pantalla, pero NO ocultar la pantalla
        // de bloqueo. Así el usuario puede introducir su patrón/PIN normalmente.
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            // Intencionalmente NO llamar setShowWhenLocked(true).
        }
    }

    private void continueAfterUnlock() {
        if (launchHandled) return;

        KeyguardManager keyguard = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguard != null && keyguard.isKeyguardLocked()) {
            Log.i(TAG, "Teléfono bloqueado: se mantiene visible el bloqueo normal y se espera el desbloqueo manual.");
            return;
        }

        launchWhatsApp();
    }

    private void launchWhatsApp() {
        if (launchHandled) return;

        KeyguardManager keyguard = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguard != null && keyguard.isKeyguardLocked()) {
            Log.i(TAG, "El teléfono sigue bloqueado; WhatsApp no se abrirá todavía.");
            return;
        }

        String phone = getIntent().getStringExtra(EXTRA_PHONE);
        String contactName = getIntent().getStringExtra(EXTRA_CONTACT_NAME);
        if (phone == null || phone.trim().isEmpty()) {
            Log.e(TAG, "Falta el número validado; no se abrirá WhatsApp.");
            finish();
            return;
        }

        String digits = phone.replaceAll("\\D", "");
        Uri[] uris = new Uri[] {
                Uri.parse("whatsapp://send?phone=" + digits),
                Uri.parse("https://wa.me/" + digits)
        };
        String[] packages = {"com.whatsapp", "com.whatsapp.w4b"};

        for (String packageName : packages) {
            for (Uri uri : uris) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage(packageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    launchHandled = true;
                    Log.i(TAG, "Chat de " + (contactName == null ? "contacto" : contactName)
                            + " solicitado mediante " + packageName + ".");
                    finish();
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.i(TAG, "No se encontró manejador para " + packageName + " y URI " + uri);
                } catch (SecurityException e) {
                    Log.w(TAG, "Android bloqueó la apertura de WhatsApp para " + packageName, e);
                }
            }
        }

        Toast.makeText(this, "No pude abrir WhatsApp. Comprueba que esté instalado.", Toast.LENGTH_LONG).show();
        finish();
    }
}
