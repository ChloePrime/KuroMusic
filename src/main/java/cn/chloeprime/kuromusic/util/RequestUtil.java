package cn.chloeprime.kuromusic.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

public class RequestUtil {
    public static URLConnection openConnection(URL url, boolean setXRealIp) throws IOException {
        var conn = url.openConnection();

        if (setXRealIp) {
            COMMON_REQUEST_HEADERS.forEach(conn::setRequestProperty);
            REAL_IP_HEADERS.forEach(conn::setRequestProperty);
        } else {
            COMMON_REQUEST_HEADERS.forEach(conn::setRequestProperty);
        }

        return conn;
    }

    private static final RandomGenerator RNG = new Random();
    private static String randomIp() {
        return "%s.%s.%s.%s".formatted(
                58, RNG.nextInt(14, 26), RNG.nextInt(256), RNG.nextInt(256)
        );
    }

    public static final Map<String, String> COMMON_REQUEST_HEADERS = Map.of("User-Agent", String.join(" ",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
            "AppleWebKit/537.36 (KHTML, like Gecko)",
            "Chrome/131.0.0.0",
            "Safari/537.36"));

    public static final Map<String, String> REAL_IP_HEADERS = Locale.getDefault() == Locale.SIMPLIFIED_CHINESE
            ? Map.of()
            : Map.of("X-Real-IP", randomIp());
}
