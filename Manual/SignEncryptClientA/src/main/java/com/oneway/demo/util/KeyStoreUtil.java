package com.oneway.demo.util;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyStoreUtil {

    public static KeyPair getKeyPairFromKeyStore() throws Exception {
        String password = "keystore";

        InputStream ins = KeyStoreUtil.class.getResourceAsStream("/keystoreServer-keystore.jks");

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(ins, password.toCharArray());   //Keystore password
        KeyStore.PasswordProtection keyPassword =       //Key password
                new KeyStore.PasswordProtection(password.toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("server", keyPassword);

        java.security.cert.Certificate cert = keyStore.getCertificate("server");
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        return new KeyPair(publicKey, privateKey);
    }
}
