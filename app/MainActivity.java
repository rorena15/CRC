package com.example.cannon_60d_remote_controll;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService executorService;
    private Vibrator vibrator;
    private EditText editTimer, editCount, editInterval;
    private TextView tvStatus;
    private CountDownTimer countDownTimer;
    private Switch switchMirrorUp;

    private Handler mirrorUpHandler = new Handler(Looper.getMainLooper());
    private Runnable mirrorUpRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Button btnShoot = findViewById(R.id.btnShoot);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnTimer = findViewById(R.id.btnTimer);
        Button btnTimelapse = findViewById(R.id.btnTimelapse);

        editTimer = findViewById(R.id.editTimer);
        editCount = findViewById(R.id.editCount);
        editInterval = findViewById(R.id.editInterval);
        tvStatus = findViewById(R.id.tvStatus);
        switchMirrorUp = findViewById(R.id.switchMirrorUp);

        btnShoot.setOnClickListener(v -> executeShoot());

        btnStop.setOnClickListener(v -> {
            vibrate();
            if (countDownTimer != null) countDownTimer.cancel();
            if (mirrorUpRunnable != null) mirrorUpHandler.removeCallbacks(mirrorUpRunnable);
            tvStatus.setText("READY");
            tvStatus.setTextColor(0xFF00FF00);
            sendCommand("/stop");
        });

        btnTimer.setOnClickListener(v -> {
            vibrate();
            String secStr = editTimer.getText().toString();
            int sec = secStr.isEmpty() ? 5 : Integer.parseInt(secStr);
            sendCommand("/timer?sec=" + sec);
            startUI_Timer(sec, "타이머 진행 중");
        });

        btnTimelapse.setOnClickListener(v -> {
            vibrate();
            String cntStr = editCount.getText().toString();
            String intStr = editInterval.getText().toString();
            int cnt = cntStr.isEmpty() ? 5 : Integer.parseInt(cntStr);
            int interval = intStr.isEmpty() ? 3 : Integer.parseInt(intStr);
            sendCommand("/burst?cnt=" + cnt + "&int=" + interval);
            startUI_Timer(cnt * interval, "타임랩스 촬영 중");
        });
    }

    private void executeShoot() {
        vibrate();
        if (switchMirrorUp.isChecked()) {
            tvStatus.setText("MIRROR UP...");
            tvStatus.setTextColor(0xFFFFA500);
            sendCommand("/shoot");

            mirrorUpRunnable = () -> {
                tvStatus.setText("SHOT!");
                sendCommand("/shoot");
                resetStatusDelay();
            };
            mirrorUpHandler.postDelayed(mirrorUpRunnable, 2000);
        } else {
            tvStatus.setText("SHOT!");
            sendCommand("/shoot");
            resetStatusDelay();
        }
    }

    private void startUI_Timer(int totalSeconds, String prefix) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(totalSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                tvStatus.setText(prefix + " (" + sec + "초 남음)");
                tvStatus.setTextColor(0xFFFFA500);
            }

            @Override
            public void onFinish() {
                tvStatus.setText("READY");
                tvStatus.setTextColor(0xFF00FF00);
                vibrate();
            }
        }.start();
    }

    private void resetStatusDelay() {
        if (countDownTimer != null) countDownTimer.cancel();
        tvStatus.postDelayed(() -> {
            tvStatus.setText("READY");
            tvStatus.setTextColor(0xFF00FF00);
        }, 1000);
    }

    private void vibrate() {
        if (vibrator != null) vibrator.vibrate(50);
    }

    private void sendCommand(String endpoint) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://192.168.4.1" + endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                // Ignore network errors in background
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            executeShoot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
