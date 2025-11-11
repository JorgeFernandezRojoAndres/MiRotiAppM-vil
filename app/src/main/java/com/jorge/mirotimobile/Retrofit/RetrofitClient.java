package com.jorge.mirotimobile.Retrofit;

import android.content.Context;

import com.jorge.mirotimobile.BuildConfig;
import com.jorge.mirotimobile.localdata.SessionManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 🔧 RetrofitClient — Configura Retrofit con:
 *  - Token JWT automático
 *  - HTTPS con certificado local (solo desarrollo)
 *  - Logging detallado de peticiones/respuestas
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    // 🌐 URL base fija (asegura que apunte a la API HTTPS con /api/)
    private static final String BASE_URL = "https://192.168.1.37:5001/api/";

    /**
     * Devuelve una instancia de Retrofit configurada.
     */
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // 🔍 Interceptor de logs: muestra request y response en Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 🔐 Interceptor: agrega token JWT automáticamente
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            SessionManager session = new SessionManager(context);
                            String token = session.getToken();

                            Request.Builder builder = original.newBuilder()
                                    .header("Accept", "application/json");

                            if (token != null && !token.isEmpty()) {
                                builder.header("Authorization", "Bearer " + token);
                            }

                            Request request = builder.build();
                            return chain.proceed(request);
                        }
                    });

            // ⚠️ Permitir certificados HTTPS autofirmados (solo en entorno local)
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);

                clientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true; // ✅ Acepta cualquier host (solo para desarrollo)
                    }
                });

            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            // 🧱 Crear cliente HTTP
            OkHttpClient client = clientBuilder.build();

            // 🚀 Construir instancia Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }

    /**
     * 🔁 Permite reiniciar la instancia (por ejemplo, al cerrar sesión)
     */
    public static void resetClient() {
        retrofit = null;
    }
}
