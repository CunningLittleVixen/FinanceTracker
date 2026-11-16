package io.github.cunninglittlevixen.financetracker.infra;

import io.github.cunninglittlevixen.financetracker.core.service.PasswordHasher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Pbkdf2PasswordHasher implements PasswordHasher {

    public static final int DEFAULT_ITERATIONS = 600_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final int iterations;
    private final SecureRandom random = new SecureRandom();

    public Pbkdf2PasswordHasher(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("Число итераций должно быть положительным.");
        }
        this.iterations = iterations;
    }

    @Override
    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] derived = derive(password, salt, iterations, KEY_BITS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + iterations + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(derived);
    }

    @Override
    public boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4 || !parts[0].equals(PREFIX)) {
                return false;
            }
            int storedIterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, storedIterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Хеширование паролей недоступно.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
