package cn.chloeprime.kuromusic.platform.spi;

import cn.chloeprime.kuromusic.platform.MusicUrlContext;
import cn.chloeprime.kuromusic.platform.MusicUrlTransformer;

import java.util.regex.Pattern;

public class NEMusicShareUrlTransformer implements MusicUrlTransformer {
    public static final int PLATFORM_ID = 163;
    public static final int PLATFORM_ID_ANOTHER = 126;
    private static final Pattern DOMAIN_PATTERN_1 = Pattern.compile("^https?://music\\.%s\\.com/.*$".formatted(PLATFORM_ID));
    private static final Pattern DOMAIN_PATTERN_2 = Pattern.compile("^https?://(?:.*\\.)?music\\.%s\\.net/.*$".formatted(PLATFORM_ID_ANOTHER));
    private static final Pattern[] SHARE_LINK_PATTERNS = {
            Pattern.compile("^https?://music\\.%s\\.com/song\\?id=(\\d+).*$".formatted(PLATFORM_ID)),
            Pattern.compile("^https?://music\\.%s\\.com/#/song\\?id=(\\d+).*$".formatted(PLATFORM_ID)),
    };
    public static final String FILE_URL_FORMAT = "https://music.%s.com/song/media/outer/url?id=%%s.mp3".formatted(PLATFORM_ID);

    @Override
    public String transform(String url, MusicUrlContext context) {
        if (DOMAIN_PATTERN_1.matcher(url).find() || DOMAIN_PATTERN_2.matcher(url).find()) {
            context.useRealIpHeader();
        }
        if (url.endsWith(".mp3")) {
            return url;
        }
        for (var urlPattern : SHARE_LINK_PATTERNS) {
            var matcher = urlPattern.matcher(url);
            if (matcher.find()) {
                return FILE_URL_FORMAT.formatted(matcher.group(1));
            }
        }
        return url;
    }
}
