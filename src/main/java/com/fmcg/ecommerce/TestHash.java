import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.matches("Admin@123", "$2a$10$rBzOzAXq0P9V6qJKFGCT7.O6sNqmLmkx9ULJj7gVvMKVAh4fRCJE2"));
    }
}
