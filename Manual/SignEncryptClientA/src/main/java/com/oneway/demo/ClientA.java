package com.oneway.demo;

import com.oneway.demo.util.EncryptUtil;
import com.oneway.demo.util.KeyStoreUtil;
import com.oneway.demo.util.SignUtil;

import java.security.KeyPair;

public class ClientA {

    public static void main (String args[]) throws Exception {
        KeyPair pair = KeyStoreUtil.getKeyPairFromKeyStore();

        //Sign and Encrypt

        String signature = SignUtil.sign("foobar", pair.getPrivate());
        String encryptedText = EncryptUtil.encrypt("foobar", pair.getPublic());
        System.out.println(encryptedText);

        //Decrypt and verify signature
        String decryptedText = EncryptUtil.decrypt(encryptedText, pair.getPrivate());
        boolean isCorrect = SignUtil.verify(decryptedText, signature, pair.getPublic());
        System.out.println("Signature correct: " + isCorrect);
        System.out.println(decryptedText);
    }
}
