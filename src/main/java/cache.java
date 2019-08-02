import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class cache {
    public static final  LoadingCache<Integer, Integer> mistakeCount = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, Integer>() {
                @Override
                public Integer load(Integer integer) throws Exception {
                    return null;
                }
            });
    // Credit of users
    public static final  LoadingCache<String, String> userCredit = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {

                @Override
                public String load(String s) throws Exception {
                    return null;
                }
            });

    public static final  LoadingCache<String, String> signedUsers = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return null;
                }
            });

    public static final LoadingCache<Integer,String> userPlayResult = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1000, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, String>() {
                        @Override
                        public String load(Integer integer) throws Exception {
                            return null;
                        }
                    }

            );

    /* userId, credit*/
    public static final  LoadingCache<String, String> userGifts = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return null;
                }
            });
  
}
