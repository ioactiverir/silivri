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
import java.util.Random;
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
    static String respJson; // based64 response in json
    static String userId;
    static String userPhone;


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
            /v1/money     when user request his/her money
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
                                String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                if (cache.signedUsers.asMap().containsValue(userPhone)) {
                                    logger.info("active session find for userPhone {}", userPhone);
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
                        .addExactPath("/v1/money", new ServiceHandler() {
                            //fixme payment and banking API enhancment
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                try {
                                    userId = exchange.getQueryParameters().get("userId").getFirst();
                                    userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    if (!cache.signedUsers.asMap().containsKey(userPhone)) {
                                        logger.error("{} authenticated error.", userPhone);
                                        return responseType.ERROR_USER_IS_NOT_REGISTERED;
                                    } else {
                                        logger.info("{} authenticated succesfully.", userPhone);
                                    }
                                    // todo transfer money for his/her banking
                                    logger.info("found {} credit balance for user {}", Utility.getUserCreditValue(userId), userId);
                                    return String.valueOf(Utility.getUserCreditValue(userId) + " Rial transferred to your account.");
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
                                    userId = exchange.getQueryParameters().get("userId").getFirst();
                                    userPhone = exchange.getQueryParameters().get("userPhone").getFirst();

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

                                } catch (NullPointerException e) {
                                    logger.error(e.getMessage());
                                    return "Error. Bad Request.";
                                }

                                /* Select random gift, then send to the user.*/
                                Random rnd = new Random();
                                int selectResp = rnd.nextInt(5);
                                logger.info("selectResp value {}", selectResp);
                                Response response=new Response();
                                switch (selectResp) {
                                    case 1:

                                        response.setRespType(responseType.RESPONSE_TEXT);
                                        response.setRespText(Utility.getRandomText());
                                        response.setRespCharacterName(responseType.RESPONSE_CHARACTER_FERI);
                                        response.setRespMediaLink("NULL");
                                        respJson = gson.toJson(response);
                                        break;
                                    case 2:
                                        /*
                                        if user have not credit balance or credit is lower than
                                         minimum value, then do action:
                                          1- no balance: only gift per 100 try
                                          2- no balance: if let us to show Ads then gift per 25 try
                                          2- if balance is upper than 1/3 of standrd then gift per 10 try
                                           and so on....
                                           */


                                        /*
                                        1- Simple puzzle , example: Nazarsanji, game, mathematics, etc.
                                         */
                                        Random rndQuezz = new Random();
                                        int selectQuezz = rndQuezz.nextInt(3);
                                        /*
                                        1= simple  1000 Rial, 5 second
                                        2= medium  2000 Rial, 10 second
                                        3= complex 3000 Rial, 15 second
                                         */
                                        String resp[] = new String[2];
                                        switch (selectQuezz) {
                                            case 1:

                                                resp= Utility.generteQuezz(1);
                                                Quezz simpleQuezz = new Quezz();
                                                simpleQuezz.setQuezzName("simple");
                                                simpleQuezz.setQuezzTime("5");
                                                simpleQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    simpleQuezz.setQuezzMessage(responseType.QUEEZ_NO_CREDIT_MESSAGE);
                                                }
                                                    simpleQuezz.setQuezzSubject(resp[0]);
                                                simpleQuezz.setQuezzOptions(resp[1]);
                                                simpleQuezz.setQuezzCredit("1000");
                                                respJson = gson.toJson(simpleQuezz);
                                                break;
                                            case 2:
                                                resp= Utility.generteQuezz(2);
                                                Quezz mediumQuezz = new Quezz();
                                                mediumQuezz.setQuezzName("meduim");
                                                mediumQuezz.setQuezzTime("10");
                                                mediumQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    mediumQuezz.setQuezzMessage(responseType.QUEEZ_NO_CREDIT_MESSAGE);
                                                }

                                                mediumQuezz.setQuezzSubject(resp[0]);
                                                mediumQuezz.setQuezzOptions(resp[1]);
                                                mediumQuezz.setQuezzCredit("2000");
                                                respJson = gson.toJson(mediumQuezz);
                                                break;
                                            case 3:
                                                resp= Utility.generteQuezz(3);
                                                Quezz complexQuezz = new Quezz();
                                                complexQuezz.setQuezzName("complex");
                                                complexQuezz.setQuezzTime("15");
                                                complexQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    complexQuezz.setQuezzMessage(responseType.QUEEZ_NO_CREDIT_MESSAGE);
                                                }
                                                complexQuezz.setQuezzSubject(resp[0]);
                                                complexQuezz.setQuezzOptions(resp[1]);
                                                complexQuezz.setQuezzCredit("3000");
                                                respJson = gson.toJson(complexQuezz);
                                                break;

                                        }
//
//                                        // there is not gift yet!
//                                        if (!cache.userGifts.asMap().containsKey(userId)) {
//                                            cache.userGifts.put(userId, String.valueOf("1000"));
//                                            logger.info("add 1000 rial gift for user {}",userId);
//                                        }
//                                        try {
//                                            int value = Integer.parseInt(cache.userGifts.asMap().get(userId));
//                                            value = value + 1000;
//                                            logger.info("{} gift for user {} found.",value, userId);
//
//                                            cache.userGifts.put(userId, String.valueOf(value));
//                                            logger.info("add 1000 rial gift for user {}",userId);
//
//                                        } catch (NullPointerException e) {
//                                            return responseType.ERROR_USER_IS_NOT_REGISTERED;
//                                        }
                                        break;
                                    case 3:
                                        response.setRespType(responseType.RESPONSE_VIDEO);
                                        response.setRespText("NULL");
                                        response.setRespCharacterName(responseType.RESPONSE_CHARACTER_FERI);
                                        response.setRespMediaLink(Utility.getRandomVideo());
                                        respJson = gson.toJson(response);
                                        break;
                                    case 4:
                                        response.setRespType(responseType.RESPONSE_AUDIO);
                                        response.setRespText("NULL");
                                        response.setRespCharacterName(responseType.RESPONSE_CHARACTER_FERI);
                                        response.setRespMediaLink(Utility.getRandomAudio());
                                        respJson = gson.toJson(response);
                                        break;
                                    case 5:
                                        response.setRespType(responseType.RESPONSE_IMAGE);
                                        response.setRespText("NULL");
                                        response.setRespCharacterName(responseType.RESPONSE_CHARACTER_FERI);
                                        response.setRespMediaLink(Utility.getRandomImage());
                                        respJson = gson.toJson(response);
                                        break;
                                }

                                return respJson;
                            }

                        }).addExactPath("/v1/profile", new ServiceHandler() {

                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                String userId = exchange.getQueryParameters().get("userId").getFirst();
                                String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                //make user profile and return json
                                // fill race values
                                userInfo userInfo = new userInfo();

                                if (cache.userCredit.asMap().containsKey(userPhone)) {
                                    userInfo.setUserCredit(cache.userCredit.get(userPhone));
                                } else {
                                    userInfo.setUserCredit("0");
                                }

                                if (cache.userPlayResult.asMap().containsKey(Integer.valueOf(userId))) {
                                    userInfo.setUserPalying(cache.userPlayResult.get(Integer.valueOf(userId)));
                                } else {
                                    userInfo.setUserPalying("0");
                                }

                                AtomicReference<String> giftList = new AtomicReference<>("");
                                if (cache.userGifts.asMap().containsValue(userId)) {
                                    cache.userGifts.asMap().forEach((k, v) -> {
                                        if (v.equals(userId)) {
                                            logger.info("find gif {} for {} ", k, v);
                                            giftList.set(giftList + k);
                                        }
                                    });
                                }
                                userInfo.setUserGifts(giftList.toString());
                                if (cache.mistakeCount.asMap().containsKey(Integer.valueOf(userId))) {
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

