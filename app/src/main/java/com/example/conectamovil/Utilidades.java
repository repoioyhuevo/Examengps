package com.example.conectamovil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utilidades {
    public static String obtenerDatosDesdeUrl(String strUrl) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        String data = "";

        try {
            URL url = new URL(strUrl);

            // Crea la conexión HTTP
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Lee la respuesta desde la conexión
            inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            data = stringBuilder.toString();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data;
    }
}
