package cn.chloeprime.kuromusic.platform;

public record MusicUrlTransformResult(
        String url,
        boolean useRealIpInHeader
) {
    static MusicUrlTransformResult of(MusicUrlContext context) {
        return new MusicUrlTransformResult(context.url, context.useRealIpInHeader);
    }
}
