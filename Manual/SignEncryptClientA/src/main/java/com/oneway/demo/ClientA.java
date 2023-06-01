package com.oneway.demo;

import com.oneway.demo.util.EncryptUtil;
import com.oneway.demo.util.KeyStoreUtilClient;
import com.oneway.demo.util.KeyStoreUtilServer;
import com.oneway.demo.util.SignUtil;

import java.security.KeyPair;

public class ClientA {

    public static void main (String args[]) throws Exception {
        KeyPair pairServer = KeyStoreUtilServer.getKeyPairFromKeyStore();
        KeyPair pairClient = KeyStoreUtilClient.getKeyPairFromKeyStore();

        String messageToSend = "A PRIVATE MESSAGE TO BE SENT";
        System.out.println("ORIGINAL MESSAGE IS: " + messageToSend);
        //Sign and Encrypt

        String signature = SignUtil.sign(messageToSend, pairServer.getPrivate());
        String encryptedText = EncryptUtil.encrypt(messageToSend, pairClient.getPublic());
        System.out.println(encryptedText);

        //SENT USING ANY CHANNEL signature and encryptedText

        //Decrypt and verify signature
        String decryptedText = EncryptUtil.decrypt(encryptedText, pairClient.getPrivate());
        boolean isCorrect = SignUtil.verify(decryptedText, signature, pairServer.getPublic());
        System.out.println("Signature correct: " + isCorrect);
        System.out.println("DECODED MESSAGE IS: " + decryptedText);
    }
}
