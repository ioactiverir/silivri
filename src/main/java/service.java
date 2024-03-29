import com.google.gson.Gson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


abstract class ServiceHandler implements HttpHandler {

    abstract public String serve(HttpServerExchange exchange) throws ExecutionException;

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(serve(exchange));
    }
}

public class service {
    private static Logger logger = LogManager.getLogger(service.class);

    public static void main(String[] args) {
        // make race
        Gson gson = new Gson();
        List<raceObj> objList = new ArrayList<raceObj>();
        // fill race values
        for (int i = 0; i < 10; i++) {

            objList.add(new raceObj(i, "0"));
        }
        objList.add(new raceObj(6, "2000"));
        //fixme desing and implement race maker
        logger.info("starting service");
        String json = new Gson().toJson(objList);
        /* Starting service
        Paths:
            /v1/signUp      where users register in app
            /v1/signIn      where users login
            /v1/singOut     user logOut
            /v1/credit      where users increment their credits
            /v1/fire        where users seelct the buttoms
            /v1/profile     where users get their profiles
            /v1/version     print API version
        */
        Undertow server = Undertow.builder().addHttpListener(8080,
                "127.0.0.1")
                .setHandler(Handlers.path()
                        .addExactPath("/v1/version", new ServiceHandler() {
                                    @Override
                                    public String serve(HttpServerExchange exchange) throws ExecutionException {
                                        return responseType.VERSION;
                                    }
                                }
                        )
                        .addExactPath("/v1/signin", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                try {
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    /* if user is not registered then send sms
                                     * we assumed users alreay send and verified*/
                                    cache.signedUsers.put(userPhone, userPhone);
                                    logger.info("{} signed in successfully via {}.", userPhone, userPhone);
                                    logger.info("inside the userSigned cache {}", cache.signedUsers.asMap().values());
                                    return responseType.RESPONSE_SUCCESS_200;
                                } catch (NullPointerException e) {
                                    return responseType.FATAL_INTERNAL_ERROR;
                                }

                            }
                        })
                        .addExactPath("/v1/signout", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                String userPhone=exchange.getQueryParameters().get("userPhone").getFirst();
                                if (cache.signedUsers.asMap().containsValue(userPhone)) {
                                    logger.info("active session find for userPhone {}",userPhone);
                                    cache.signedUsers.invalidate(userPhone);
                                    return responseType.RESPONSE_SUCCESS_200;
                                } else {
                                    return responseType.ERROR_NO_SESSION;
                                }
                            }
                        })
                        .addExactPath("/v1/credit", new ServiceHandler() {
                            //fixme payment and banking API enhancment
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                try {
                                    String userId = exchange.getQueryParameters().get("userId").getFirst();
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    String creditValue = exchange.getQueryParameters().get("credit").getFirst();
                                    if (!cache.signedUsers.asMap().containsKey(userPhone)) {
                                        logger.error("{} authenticated error.", userPhone);
                                        return responseType.ERROR_USER_IS_NOT_REGISTERED;
                                    } else {
                                        logger.info("{} authenticated succesfully.", userPhone);
                                    }
                                    cache.userCredit.put(userPhone, creditValue);
                                    logger.info("{} append user credit {}.", userPhone, creditValue);
                                    logger.info("inside the userSigned cache {}", cache.userCredit.asMap().values());
                                    return responseType.RESPONSE_SUCCESS_200;
                                } catch (NullPointerException e) {
                                    return responseType.FATAL_INTERNAL_ERROR;
                                }

                            }
                        })


                        .addExactPath("/v1/fire", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                //fixme get all user info from json, then pars it.
                                try {
                                    String index = exchange.getQueryParameters().get("index").getFirst();
                                    String userId = exchange.getQueryParameters().get("userId").getFirst();
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();

                                    /*
                                        Mistake great than 3 and score is minimum
                                     */
                                    if (cache.userCredit.asMap().containsKey(userPhone)) {
                                        String score = String.valueOf(cache.userCredit.asMap().get(userPhone));
                                        int tmpScore = Integer.parseInt(score);
                                        if (tmpScore <= 100) {
                                            logger.warn("your socre {} is lower than basic plan.", tmpScore);
                                            return responseType.SCORE_MINIMUM_THAN_BASIC_PLAN;
                                        }
                                    }

                                    //check if user registerd or not
                                    if (!cache.signedUsers.asMap().containsKey(userPhone)) {
                                        logger.error("{} authenticated error.", userPhone);
                                        return responseType.ERROR_USER_IS_NOT_REGISTERED;
                                    } else {
                                        logger.info("{} authenticated succesfully.", userPhone);
                                    }

                                    if (userId.equals("0") || userId.isEmpty() || userPhone.isEmpty()) {
                                        // user is not registered yet, forward to regustering
                                        return responseType.ERROR_USER_IS_NOT_REGISTERED;
                                    }
                                    if (cache.mistakeCount.asMap().containsKey(Integer.valueOf(userId))) {
                                        if (cache.mistakeCount.get(Integer.valueOf(userId)) >= 3) {
                                            return responseType.WARNING_TRAIAL_LIMIT;
                                        }
                                    }
                                    if (index.equals(String.valueOf(responseType.MAGIC_INDEX_WIN))) {
                                        logger.info("user {} win the game.", userId);
                                        cache.userGifts.put(responseType.GIFT,userId);
                                        return "you win";
                                    } else {
                                        if (cache.mistakeCount.asMap().containsKey(Integer.valueOf(userId))) {
                                            int i = cache.mistakeCount.get(Integer.valueOf(userId));
                                            i++;
                                            cache.mistakeCount.put(Integer.valueOf(userId), i);
                                            logger.warn("{}  mistake for  {} ", userId, i);
                                            // reduce score
                                            if (cache.userCredit.asMap().containsKey(userPhone)) {
                                                String score = String.valueOf(cache.userCredit.asMap().get(userPhone));
                                                int tmpScore = Integer.parseInt(score);
                                                tmpScore = tmpScore - 100;
                                                logger.warn("now user score is  {}", tmpScore);
                                                cache.userCredit.put(userPhone, String.valueOf(tmpScore));
                                            } else {
                                                logger.warn("not found  userPhone {} credits", userPhone);
                                            }

                                        } else {
                                            logger.warn("{} first mistake via {} phone", userId, userPhone);
                                            cache.mistakeCount.put(Integer.valueOf(userId), 1);
                                            if (cache.userCredit.asMap().containsKey(userPhone)) {
                                                String score = String.valueOf(cache.userCredit.asMap().get(userPhone));
                                                int tmpScore = Integer.parseInt(score);
                                                tmpScore = tmpScore - 100;
                                                logger.warn("now user score is  {}", tmpScore);
                                                cache.userCredit.put(userPhone, String.valueOf(tmpScore));
                                            } else {
                                                logger.warn("not found  userPhone {} credits", userPhone);
                                            }
                                        }
                                        if (cache.userPlayResult.asMap().containsKey(Integer.valueOf(userId))) {
                                            String item;
                                            item = cache.userPlayResult.get(Integer.valueOf(userId));
                                            item = item + "," + index;
                                            cache.userPlayResult.put(Integer.valueOf(userId), item);
                                            logger.info("{} select {}", userId, item);
                                        } else { /* init */
                                            cache.userPlayResult.put(Integer.valueOf(userId), index);
                                            logger.info("{} select {}", userId, index);
                                        }
                                        String res = "user clicked -> " + cache.userPlayResult.get(Integer.valueOf(userId));
                                        return res;

                                    }
                                } catch (NullPointerException e) {
                                    logger.error(e.getMessage());
                                    return "Error. Bad Request.";
                                }

                            }

                        }).addExactPath("/v1/profile", new ServiceHandler() {

            @Override
            public String serve(HttpServerExchange exchange) throws ExecutionException {
                String userId = exchange.getQueryParameters().get("userId").getFirst();
                String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                //make user profile and return json
                // fill race values
                userInfo userInfo = new userInfo();

                if (cache.userCredit.asMap().containsKey(userPhone)){
                    userInfo.setUserCredit(cache.userCredit.get(userPhone));
                } else {
                    userInfo.setUserCredit("0");
                }

                if (cache.userPlayResult.asMap().containsKey(Integer.valueOf(userId))){
                    userInfo.setUserPalying(cache.userPlayResult.get(Integer.valueOf(userId)));
                } else {
                    userInfo.setUserPalying("0");
                }

                AtomicReference<String> giftList= new AtomicReference<>("");
                if (cache.userGifts.asMap().containsValue(userId)){
                    cache.userGifts.asMap().forEach((k,v)->{
                        if (v.equals(userId)) {
                            logger.info("find gif {} for {} ",k,v);
                            giftList.set(giftList + k);
                        }
                    });
                }
                userInfo.setUserGifts(giftList.toString());
                if (cache.mistakeCount.asMap().containsKey(Integer.valueOf(userId))){
                    userInfo.setUserMistakeCount(cache.mistakeCount.get(Integer.valueOf(userId)));
                } else {
                    userInfo.setUserMistakeCount(0);
                }


                userInfo.setPhoneNumber(userPhone);
                String res = new Gson().toJson(userInfo);
                return res;

            }
        }))
        .build();
        server.start();


    }
}

