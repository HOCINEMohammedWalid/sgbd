package com.sgeb.sgbd.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {

    // Paramètres recommandés pour PBKDF2 (peut nécessiter un ajustement)
    private static final int ITERATIONS = 120000; // Nombre d'itérations : Plus c'est grand, plus c'est sécurisé/lent
    private static final int KEY_LENGTH = 256; // Longueur de la clé en bits
    private static final int SALT_LENGTH = 16; // Longueur du sel en bytes

    /**
     * Génère un sel aléatoire.
     */
    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash le mot de passe clair en utilisant PBKDF2 et un sel.
     * Le sel et le hachage sont encodés en Base64 et séparés par un ':' pour le
     * stockage.
     *
     * @param password Le mot de passe clair.
     * @return Le sel et le hachage combinés (Base64).
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = getSalt();
            byte[] hash = hash(password, salt);

            // Stocker le sel et le hachage ensemble, encodés en Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur de hachage de mot de passe", e);
        }
    }

    /**
     * Vérifie si le mot de passe clair correspond au hachage stocké.
     *
     * @param plainPassword  Le mot de passe clair soumis.
     * @param hashedPassword Le sel et le hachage combinés (stockés).
     * @return true si les mots de passe correspondent, false sinon.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                // Le format du hachage est invalide
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            byte[] newHash = hash(plainPassword, salt);

            // Comparaison sécurisée pour éviter les attaques par "timing"
            return java.security.MessageDigest.isEqual(storedHash, newHash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            // IllegalArgumentException si le décodage Base64 échoue
            return false;
        }
    }

    /**
     * Fonction interne de hachage PBKDF2
     */
    private static byte[] hash(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return skf.generateSecret(spec).getEncoded();
    }
}