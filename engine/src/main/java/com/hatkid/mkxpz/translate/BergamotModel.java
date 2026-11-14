package com.hatkid.mkxpz.translate;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One model and a synchronous native service, owned by the game's translation worker. */
final class BergamotModel implements AutoCloseable {
    private static final String PACK = "neural/";
    private static final String VERSION = "mozilla-en-es-3.0";
    private long service;
    private long model;

    BergamotModel(Context context) throws Exception {
        if (Build.VERSION.SDK_INT < 28) throw new IOException("Gahvoria Neural AI requiere Android 9 o posterior");
        try {
            System.loadLibrary("gahvoria_bergamot");
        } catch (LinkageError failure) {
            throw new IOException("El motor Neural no está disponible en este dispositivo", failure);
        }

        File dir = new File(context.getFilesDir(), "neural/" + VERSION);
        if (!dir.mkdirs() && !dir.isDirectory()) throw new IOException("No pude preparar el espacio del modelo Neural");
        Map<String, String> paths = unpack(context.getAssets(), dir);
        try {
            service = BergamotNative.nativeNewService(0);
            if (service == 0) throw new IOException("No pude iniciar el motor Neural");
            model = BergamotNative.nativeLoadModel(paths.get("model"), paths.get("lex"),
                new String[]{paths.get("vocab")}, false);
            if (model == 0) throw new IOException("No pude cargar el modelo Neural");
        } catch (Exception | LinkageError failure) {
            close();
            throw failure;
        }
    }

    List<String> translate(List<String> texts) throws IOException {
        return translate(texts, false);
    }

    List<String> translateMarkup(List<String> texts) throws IOException {
        return translate(texts, true);
    }

    private List<String> translate(List<String> texts, boolean markup) throws IOException {
        byte[][] inputs = new byte[texts.size()][];
        for (int i = 0; i < texts.size(); i++) inputs[i] = texts.get(i).getBytes(StandardCharsets.UTF_8);
        byte[][] outputs = BergamotNative.nativeTranslate(service, model, inputs, markup);
        if (outputs == null || outputs.length != inputs.length) throw new IOException("Respuesta incompleta del motor Neural");
        List<String> result = new ArrayList<>(outputs.length);
        for (byte[] output : outputs) result.add(output == null ? null : new String(output, StandardCharsets.UTF_8));
        return result;
    }

    private static Map<String, String> unpack(AssetManager assets, File dir) throws Exception {
        JSONObject manifest;
        try (InputStream in = assets.open(PACK + "model-en-es.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            transfer(in, out);
            manifest = new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
        if (!VERSION.equals(manifest.getString("version"))) throw new IOException("Versión inesperada del modelo Neural");

        Map<String, String> paths = new HashMap<>();
        JSONArray files = manifest.getJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            JSONObject entry = files.getJSONObject(i);
            String name = entry.getString("name");
            if (!name.matches("[A-Za-z0-9._-]+") || ".".equals(name) || "..".equals(name)) {
                throw new IOException("Nombre de archivo Neural inválido");
            }
            File target = new File(dir, name);
            long size = entry.getLong("rawSize");
            String sha = entry.getString("rawSha256");
            if (!valid(target, size, sha)) {
                File compressed = new File(dir, name + ".zst.tmp");
                File temporary = new File(dir, name + ".tmp");
                try {
                    try (InputStream in = assets.open(PACK + name + ".zst");
                         FileOutputStream out = new FileOutputStream(compressed)) {
                        transfer(in, out);
                    }
                    String problem = BergamotNative.nativeUnzstd(compressed.getAbsolutePath(), temporary.getAbsolutePath());
                    if (problem != null || !valid(temporary, size, sha)) {
                        throw new IOException("No pude verificar el archivo Neural " + name);
                    }
                    if (!temporary.renameTo(target)) throw new IOException("No pude instalar el archivo Neural " + name);
                } finally {
                    compressed.delete();
                    temporary.delete();
                }
            }
            paths.put(entry.getString("role"), target.getAbsolutePath());
        }
        if (!paths.containsKey("model") || !paths.containsKey("vocab") || !paths.containsKey("lex")) {
            throw new IOException("El paquete Neural está incompleto");
        }
        return paths;
    }

    private static boolean valid(File file, long size, String sha) throws Exception {
        if (!file.isFile() || file.length() != size) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[32 * 1024];
            int count;
            while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        byte[] hash = digest.digest();
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            hex.append(Character.forDigit((value & 0xff) >>> 4, 16));
            hex.append(Character.forDigit(value & 15, 16));
        }
        return sha.contentEquals(hex);
    }

    private static void transfer(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
    }

    @Override public void close() {
        if (model != 0) {
            BergamotNative.nativeFreeModel(model);
            model = 0;
        }
        if (service != 0) {
            BergamotNative.nativeFreeService(service);
            service = 0;
        }
    }
}
