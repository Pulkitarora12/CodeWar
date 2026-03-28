import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeTest {
    public static void main(String[] args) {
        LocalDateTime local = LocalDateTime.now();
        LocalDateTime utc = LocalDateTime.now(ZoneOffset.UTC);
        long localEpoch = local.toEpochSecond(ZoneOffset.UTC);
        long utcEpoch = utc.toEpochSecond(ZoneOffset.UTC);
        System.out.println("Local: " + local);
        System.out.println("UTC: " + utc);
        System.out.println("Local Epoch (if UTC): " + localEpoch);
        System.out.println("UTC Epoch (if UTC): " + utcEpoch);
        System.out.println("Real Epoch (System): " + (System.currentTimeMillis() / 1000));
    }
}
