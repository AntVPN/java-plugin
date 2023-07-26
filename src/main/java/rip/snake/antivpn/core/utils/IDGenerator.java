package rip.snake.antivpn.core.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Unique ID Generator inspired on MongoDB's <a href="https://docs.mongodb.com/manual/reference/method/ObjectId/">ObjectId</a>
 * <p>
 * A 12-byte BSON type, constructed using:
 * - a 4-byte value representing the seconds since the Unix epoch,
 * - a 5-byte random value, and
 * - a 3-byte counter, starting with a random value.
 * <p>
 */
public class IDGenerator {

    private static final int TIMESTAMP_BYTES = 4;
    private static final int RANDOM_BYTES = 5;
    private static final int COUNTER_BYTES = 3;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();

    private static int counter = random.nextInt(1 << (COUNTER_BYTES * 8));

    public static String generateUniqueID() {
        byte[] objectId = new byte[TIMESTAMP_BYTES + RANDOM_BYTES + COUNTER_BYTES];

        // Add timestamp (current seconds since Unix epoch) to objectId
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        addIntToByteArray(timestamp, objectId, 0, TIMESTAMP_BYTES);

        // Add machine-specific random value to objectId
        byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, objectId, TIMESTAMP_BYTES, RANDOM_BYTES);

        // Add incrementing counter to objectId
        addIntToByteArray(counter++, objectId, TIMESTAMP_BYTES + RANDOM_BYTES, COUNTER_BYTES);

        // Convert bytes to hexadecimal representation
        StringBuilder sb = new StringBuilder();
        for (byte b : objectId) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static void addIntToByteArray(int value, byte[] array, int offset, int length) {
        for (int i = 0; i < length; i++) {
            array[offset + i] = (byte) (value >>> (8 * (length - i - 1)));
        }
    }

}
