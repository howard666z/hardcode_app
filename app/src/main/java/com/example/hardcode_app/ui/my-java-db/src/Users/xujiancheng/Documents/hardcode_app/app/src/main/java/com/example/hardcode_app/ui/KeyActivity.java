package com.example.hardcode_app.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hardcode_app.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KeyActivity extends AppCompatActivity {

    // ✅ 硬編碼的 token
    private static final String API_TOKEN = "17bd7fac77c9eb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.btnTest).setOnClickListener(v -> testIpInfo());
        findViewById(R.id.btnBenchmark).setOnClickListener(v -> benchmarkApiCall());

    }

    private void testIpInfo() {
        new Thread(() -> {
            try {
                String apiUrl = "https://ipinfo.io/json?token=" + API_TOKEN;
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                if (code == 200) {
                    handleSuccessfulResponse(conn);
                } else {
                    showToast("HTTP Error: " + code);
                }
            } catch (Exception e) {
                Log.e("IPinfo", "Error", e);
                showToast("Exception: " + e.getMessage());
            }
        }).start();
    }

    private void handleSuccessfulResponse(HttpURLConnection conn) throws Exception {
        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);

            String json = bos.toString("UTF-8");
            JSONObject obj = new JSONObject(json);
            String ip = obj.optString("ip", "N/A");

            runOnUiThread(() -> {
                Toast.makeText(this, "IP: " + ip, Toast.LENGTH_LONG).show();
                Log.i("IPinfo", json);
            });
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void benchmarkApiCall() {
        new Thread(() -> {
            final int rounds = 10;
            long[] latencies = new long[rounds];
            long[] memoryUsages = new long[rounds];

            for (int i = 0; i < rounds; i++) {
                try {
                    // 🔹 記錄開始時間
                    long startTime = System.currentTimeMillis();

                    // 🔹 呼叫 API
                    String apiUrl = "https://ipinfo.io/json?token=" + API_TOKEN;
                    HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                    conn.setRequestMethod("GET");

                    int code = conn.getResponseCode();
                    if (code == 200) {
                        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            byte[] buf = new byte[4096];
                            int n;
                            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
                            // 處理完成
                        }
                    }

                    // 🔹 記錄結束時間
                    long endTime = System.currentTimeMillis();
                    latencies[i] = endTime - startTime;

                    // 🔹 記錄記憶體使用量
                    Runtime runtime = Runtime.getRuntime();
                    runtime.gc();  // 建議強制 GC 減少雜訊
                    long usedMemKB = (runtime.totalMemory() - runtime.freeMemory()) / 1024L;
                    memoryUsages[i] = usedMemKB;

                    // 🔹 每次結果印出
                    Log.i("Benchmark", "第 " + (i + 1) + " 次：延遲 " + latencies[i] + " ms, 記憶體 " + memoryUsages[i] + " KB");

                    // 避免連續打太快造成阻擋，可加點 delay
                    Thread.sleep(500);

                } catch (Exception e) {
                    Log.e("Benchmark", "第 " + (i + 1) + " 次測試失敗", e);
                }
            }

            // 🔹 計算平均
            long totalLatency = 0;
            long totalMemory = 0;
            for (int i = 0; i < rounds; i++) {
                totalLatency += latencies[i];
                totalMemory += memoryUsages[i];
            }

            long avgLatency = totalLatency / rounds;
            long avgMemory = totalMemory / rounds;

            Log.i("Benchmark", "✅ 測試完成：平均延遲 " + avgLatency + " ms, 平均記憶體使用 " + avgMemory + " KB");

            runOnUiThread(() -> Toast.makeText(this, "測試完成！平均延遲: " + avgLatency + " ms", Toast.LENGTH_LONG).show());
        }).start();
    }

}
