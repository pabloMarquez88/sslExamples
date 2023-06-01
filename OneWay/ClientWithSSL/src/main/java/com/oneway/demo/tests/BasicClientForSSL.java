package com.oneway.demo.tests;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;

public class BasicClientForSSL {

    static File trustStoreFile = new File("src/main/resources/TrustStoreClient-truststore.jks");

    private static SSLSocketFactory createSslSocketFactory(KeyStore trustStore) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, null);
        return sslContext.getSocketFactory();
    }
    
    
    public static void main (String args[]) throws Exception {
        //Init truststore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(trustStoreFile);
        ks.load(fis, "truststore".toCharArray());
        ////
        SSLSocketFactory fac = createSslSocketFactory(ks);
        //the code
        URL url = new URL("https://localhost:8443/server-ssl/test");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(fac);
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println(content);
        in.close();



    }
}
