package com.example.thechesslearninggame.modules;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StockfishManager {
    private static final String TAG = "StockfishManager";
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Process process;
    private BufferedReader inputReader;
    private BufferedWriter outputWriter;

    public interface InitCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface MoveCallback {
        void onMoveReceived(String move);
        void onError(String error);
    }

    public StockfishManager(Context context) {
        this.context = context;
    }

    public void initialize(int difficulty, InitCallback callback) {
        executor.execute(() -> {
            try {
                File engineFile = extractEngineBinary();
                startProcess(engineFile);
                initializeUCI(difficulty);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Engine init failed: " + e.getMessage());
            }
        });
    }

    private File extractEngineBinary() {
        String nativeLibDir = context.getApplicationInfo().nativeLibraryDir;
        File file = new File(nativeLibDir, "lib_stockfish.so");
        file.setExecutable(true);

        Log.d("STOCKFISH", "Binary exists: " + file.exists());
        Log.d("STOCKFISH", "Absolute path: " + file.getAbsolutePath());
        Log.d("STOCKFISH", "Can execute: " + file.canExecute());
        Log.d("STOCKFISH", "Permissions: " + getPermissions(file));
        return file;
    }

    private String getPermissions(File file) {
        try {
            Process process = Runtime.getRuntime().exec("ls -l " + file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return "Permission check failed";
        }
    }

    private void startProcess(File binary) throws IOException {
        process = new ProcessBuilder(binary.getAbsolutePath()).redirectErrorStream(true).start();
        inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    private void initializeUCI(int difficulty) throws IOException {
        sendCommand("uci");
        waitForResponse("uciok");
        sendCommand("setoption name Skill Level value " + difficulty);
        sendCommand("isready");
        waitForResponse("readyok");
    }

    public void getBestMove(String fen, int thinkTimeMs, MoveCallback callback) {
        executor.execute(() -> {
            try {
                sendCommand("position fen " + fen);
                sendCommand("go movetime " + thinkTimeMs);

                String line;
                while ((line = inputReader.readLine()) != null) {
                    if (line.startsWith("bestmove")) {
                        String move = line.split(" ")[1];
                        callback.onMoveReceived(move);
                        return;
                    }
                }
                callback.onError("No move received");
            } catch (IOException e) {
                callback.onError("Engine communication error: " + e.getMessage());
            }
        });
    }

    private void sendCommand(String command) throws IOException {
        outputWriter.write(command + "\n");
        outputWriter.flush();
    }

    private void waitForResponse(String expectedResponse) throws IOException {
        String line;
        while ((line = inputReader.readLine()) != null) {
            if (line.trim().equals(expectedResponse)) break;
        }
    }

    public void shutdown() {
        executor.execute(() -> {
            try {
                if (process != null) {
                    sendCommand("quit");
                    process.waitFor(500, TimeUnit.MILLISECONDS);
                    process.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "Shutdown error", e);
            } finally {
                executor.shutdown();
            }
        });
    }
}