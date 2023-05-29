package com.oneway.demo.configuration;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.*;

@Configuration
public class ClientInit {

    private static final String ORGANIZATION = "VDI";

    private final String baseUrl = "https://localhost:8443";

    private final String endpoint = "/server-ssl/test";

    @Bean
    public WebClient configureWebclient(@Value("${server.ssl.trust-store}") String trustStorePath,
                                        @Value("${server.ssl.trust-store-password}") String trustStorePass,
                                        @Value("${server.ssl.key-store}") String keyStorePath,
                                        @Value("${server.ssl.key-store-password}") String keyStorePass,
                                        @Value("${server.ssl.key-alias}") String keyAlias) throws Exception {

        SslContext sslContext;
        final PrivateKey privateKey;
        final X509Certificate[] certificates;
        try {
            final KeyStore trustStore;
            final KeyStore keyStore;

            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePass.toCharArray());
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePass.toCharArray());
            List<Certificate> certificateList =
                    Collections.list(trustStore.aliases()).stream()
                            .filter(
                                    t -> {
                                        try {
                                            return trustStore.isCertificateEntry(t);
                                        } catch (KeyStoreException exception) {
                                            throw new RuntimeException("Error reading truststore", exception);
                                        }
                                    })
                            .map(
                                    t -> {
                                        try {
                                            return trustStore.getCertificate(t);
                                        } catch (KeyStoreException exception) {
                                            throw new RuntimeException("Error reading truststore", exception);
                                        }
                                    })
                            .collect(Collectors.toList());

            certificates = certificateList.toArray(new X509Certificate[certificateList.size()]);
            privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyStorePass.toCharArray());
            Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
            X509Certificate[] x509CertificateChain =
                    Arrays.stream(certChain)
                            .map(certificate -> (X509Certificate) certificate)
                            .collect(Collectors.toList())
                            .toArray(new X509Certificate[certChain.length]);

            X509Certificate certificate = x509CertificateChain[0];
            validateCertificate(certificate);
            sslContext =
                    SslContextBuilder.forClient()
                            .keyManager(privateKey, keyStorePass, x509CertificateChain)
                            .trustManager(certificates)
                            .build();

            HttpClient httpClient =
                    HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            /**
             *
             */
            // Load the Keystore
            //Load the Truststore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            // Add Keystore to KeyManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePass.toCharArray());

            // Create SSLContext with KeyManager and TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());
            SSLSocketFactory sslSocketFactory = context.getSocketFactory();

            URL url = new URL("https://localhost:8443/server-ssl/test");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setSSLSocketFactory(sslSocketFactory);
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

            /**
             *
             */
            return webClientConfiguration(httpClient);

        } catch (KeyStoreException
                 | CertificateException
                 | NoSuchAlgorithmException
                 | IOException
                 | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean validateCertificate(X509Certificate certificate) throws Exception {

        var certificateExpirationDate =
                certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        var certificateStartDate =
                certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (LocalDate.now().isAfter(certificateExpirationDate)) {
            throw new Exception("Service date expiration");
        }

        if (LocalDate.now().isBefore(certificateStartDate)) {
            throw new Exception(
                    "Service cannot be used until " + certificateStartDate);
        }

        var subject =
                Arrays.stream(certificate.getSubjectDN().getName().split(","))
                        .map(i -> i.split("="))
                        .collect(Collectors.toMap(element -> element[0].trim(), element -> element[1].trim()));

        if (!subject.get("O").equalsIgnoreCase(ORGANIZATION)) {
            throw new Exception("Organization is not correct");
        }

        return true;
    }

    private WebClient webClientConfiguration(HttpClient httpClient) {
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        var webClient =
                WebClient.builder()
                        .clientConnector(connector)
                        .baseUrl(baseUrl)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();

        var reponse =
                webClient.get().uri(endpoint).retrieve().bodyToMono(String.class).block();
        System.out.println(reponse);
        assert Objects.requireNonNull(reponse).equalsIgnoreCase("verified");
        return webClient;
    }
}