package com.timothee.reveilflash;

import android.app.Activity;
import android.app.KeyguardManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.*;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class FlashActivity extends Activity {
    private CameraManager cameraManager;
    private String cameraId;
    private boolean torchOn = false;
    private boolean running = true;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km != null) km.requestDismissKeyguard(this, null);

        setContentView(R.layout.activity_flash);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Boolean hasFlash = cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.LENS_FACING);
                if (hasFlash != null && hasFlash && facing != null
                        && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) { /* pas de flash dispo */ }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startFlashing();
        startVibrating();

        Button stop = findViewById(R.id.btn_stop);
        stop.setOnClickListener(v -> stopAll());
    }

    private void startFlashing() {
        FrameLayout bg = findViewById(R.id.flash_bg);
        Runnable r = new Runnable() {
            @Override public void run() {
                if (!running) return;
                torchOn = !torchOn;
                setTorch(torchOn);
                bg.setBackgroundColor(torchOn ? 0xFFFFFFFF : 0xFFE60033);
                handler.postDelayed(this, 300);
            }
        };
        handler.post(r);
    }

    private void setTorch(boolean on) {
        if (cameraId == null) return;
        try { cameraManager.setTorchMode(cameraId, on); } catch (Exception e) { /* ignore */ }
    }

    private void startVibrating() {
        long[] pattern = {0, 500, 200};
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void stopAll() {
        running = false;
        setTorch(false);
        vibrator.cancel();
        finish();
    }

    @Override
    protected void onDestroy() {
        stopAll();
        super.onDestroy();
    }
}
