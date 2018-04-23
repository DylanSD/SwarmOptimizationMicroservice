package com.dksd.optimization;

import com.dksd.optimization.api.EncryptDecrypt;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.*;

/**
 * Created by dylan on 3/3/18.
 */
public class EncryptDecryptUtilTest {
    @Test
    public void createFitnessFunction() throws Exception {
        EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
        String input = encryptDecrypt.encrypt("Fitness function goes here");
        System.out.println(input);
    }
}
