package cn.chloeprime.kuromusic.platform;

/**
 * Transform share url to file url
 */
public class MusicUrlContext {
    MusicUrlContext() {
    }

    public void useRealIpHeader() {
        useRealIpInHeader = true;
    }

    String url;
    boolean useRealIpInHeader;
}
