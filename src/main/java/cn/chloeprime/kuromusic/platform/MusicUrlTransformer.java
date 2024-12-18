package cn.chloeprime.kuromusic.platform;

import java.util.Comparator;
import java.util.ServiceLoader;

/**
 * Transform share url to file url
 */
public interface MusicUrlTransformer {
    default int priority() {
        return 1000;
    }

    String transform(String url, MusicUrlContext context);

    /*public*/ static MusicUrlTransformResult doTransform(String url) {
        var context = new MusicUrlContext();
        context.url = url;

        ServiceLoader.load(MusicUrlTransformer.class, MusicUrlTransformerSupport.CL).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingLong(MusicUrlTransformer::priority).reversed())
                .forEach(transformer -> context.url = transformer.transform(context.url, context));
        return MusicUrlTransformResult.of(context);
    }
}
