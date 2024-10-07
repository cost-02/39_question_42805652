package com.example;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SecureAsyncHttpClient {

    public static void main(String[] args) {
        try {
            // Carica il certificato CA dal tuo file .crt
            InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Crea un KeyStore contenente il nostro CA affidabile
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Crea un TrustManager che si fida dei CAs nel nostro KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Crea un SSLContext che usa il nostro TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // Utilizza la SSLSocketFactory con AsyncHttpClient
            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext);
            AsyncHttpClient client = new AsyncHttpClient();
            client.setSSLSocketFactory(sslSocketFactory);

            // Fai una richiesta GET
            client.get("https://yourserver.com/path", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("Success! Response = " + new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    System.err.println("Failure! Reason = " + error.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
