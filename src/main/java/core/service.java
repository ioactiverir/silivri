package core;

import IAM.sessionManager;
import com.google.gson.Gson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;


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
    //is a thread safe?
    private static String respJson; // based64 response in json


    public static void main(String[] args) {
        // make race
        Gson gson = new Gson();

        //fixme desing and implement race maker

        logger.info("starting core.service");
      /* Starting core.service
        Paths:
            /v1/sendCode    SMS code request (login/register)
            /v1/verify      get SMS code and verify phone, the forward to registering
            /v1/register    user registeration
            /v1/signIn      where users login
            /v1/singOut     user logOut
            /v1/credit      where users increment their credits
            /v1/fire        where users seelct the buttoms
            /v1/profile     where users get their profiles
            /v1/money       when user request his/her money
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
                        ).addExactPath("/v1/sendCode", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                //todo persist code in core.cache in order to lookup.
                                Random rndSmsCode = new Random();
                                int smsCode = rndSmsCode.nextInt(100000);
                                try { //try here
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    logger.info("set SMS {} for phone {}", smsCode, userPhone);
                                    cache.sendCode.put(userPhone, String.valueOf(smsCode));
                                    return responseType.SMS_MESSAGE_SEND_CODE + smsCode;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return "200";
                            }
                        }).addExactPath("/v1/verify", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                String verifyResult="";
                                try {
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    String verifyCode = exchange.getQueryParameters().get("smsCode").getFirst();
                                    if (!cache.sendCode.asMap().containsKey(userPhone)) {
                                        // sms for phone not existed.
                                        return responseType.SMS_MESSAGE_SEND_CODE_NOT_EXSITS;
                                    } else {

                                        // ok , then make session for phone and forward to registeration
                                        if (cache.sendCode.asMap().containsValue(verifyCode)) {
                                            cache.sendCode.invalidate(userPhone);
                                            /*
                                            if phone already registered give it sesssion only.
                                            if not give tmp session , forward register
                                             */

                                            Transaction transaction = null;
                                            try (Session session = sqlCommand.getSessionFactory().openSession()) {

                                                transaction = session.beginTransaction();
                                                String hql = "FROM userInfo  where phoneNumber= :phoneNumber";
                                                Query query = session.createQuery(hql);
                                                query.setParameter("phoneNumber", userPhone);

                                                List result = query.list();
                                                transaction.commit();
                                                if (!result.isEmpty()){
                                                    String sessionID = Utility.getRandomSessionID(25);
                                                    cache.sessions.put(userPhone, sessionID);
                                                    verifyResult=sessionID;
                                                } else {
                                                    String sessionID = Utility.getRandomSessionID(25);
                                                    cache.sessions.put(userPhone, sessionID);
                                                    verifyResult="Forward to register via session => "+sessionID;
                                                }
                                                // commit transaction
                                                session.close();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                if (transaction != null) {
                                                    transaction.rollback();
                                                }
                                                e.printStackTrace();
                                            }




                                        } else {
                                            return responseType.SMS_MESSAGE_SEND_CODE_INVALID;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return verifyResult;
                            }
                        })
                        .addExactPath("/v1/register", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                // get user info
                                String sessionID = "NULL";
                                userInfo newUser = new userInfo();
                                try { //try here
                                    String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    String userFirstName = exchange.getQueryParameters().get("userFirstName").getFirst();
                                    String userLastName = exchange.getQueryParameters().get("userLastName").getFirst();
                                    String userBankNo = exchange.getQueryParameters().get("userBankNo").getFirst();
                                    String userMail = exchange.getQueryParameters().get("userMail").getFirst();
                                    sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                    // check if usr already registered or not
                                    Transaction transaction = null;
                                    try (Session session = sqlCommand.getSessionFactory().openSession()) {

                                        transaction = session.beginTransaction();
                                        String hql = "FROM userInfo  where phoneNumber= :phoneNumber";
                                        Query query = session.createQuery(hql);
                                        query.setParameter("phoneNumber", userPhone);

                                        List result = query.list();
                                        if (!result.isEmpty()){
                                          return responseType.PHONE_ALREADY_REGISTERED;
                                        }

                                        // commit transaction
                                        transaction.commit();
                                        session.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (transaction != null) {
                                            transaction.rollback();
                                        }
                                        e.printStackTrace();
                                    }

                                    //


                                    try {
                                        if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                            logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                            return responseType.SESSION_IS_NOT_VALID;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                    newUser.setPhoneNumber(userPhone);
                                    newUser.setUserFirstName(userFirstName);
                                    newUser.setUserLastName(userLastName);
                                    newUser.setBankNo(userBankNo);
                                    newUser.setUserMail(userMail);
                                    newUser.setUserCreditValue(0);
                                    newUser.setUserGiftValue(0);

                                    transaction = null;
                                    try (Session session = sqlCommand.getSessionFactory().openSession()) {
                                        // start a transaction
                                        logger.info("starting transcation");
                                        transaction = session.beginTransaction();
                                        // save the student objects
                                        session.save(newUser);
                                        // commit transaction
                                        transaction.commit();
                                        // revoke sms code
                                        session.close();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (transaction != null) {
                                            transaction.rollback();
                                        }
                                        e.printStackTrace();
                                    }
                                    newUser.setUserGiftValue(0);
                                } catch (Exception e) { /// try here
                                    e.printStackTrace();
                                }

                                return gson.toJson(newUser);
                            }
                        })

                        .addExactPath("/v1/signin", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                return gson.toJson(respJson);
                            }
                        })
                        .addExactPath("/v1/signout", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                String sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                try {
                                    if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                        logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                        return responseType.SESSION_IS_NOT_VALID;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

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
                                String userId = "";
                                String userPhone = "";
                                int creditValue = 0;
                                String sessionID = "";

                                try {
                                    userId = exchange.getQueryParameters().get("userId").getFirst();
                                    userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    creditValue = Integer.parseInt(exchange.getQueryParameters().get("credit").getFirst());
                                    sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                    try {
                                        if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                            logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                            return responseType.SESSION_IS_NOT_VALID;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // persist usr credit in DB
                                    Transaction transaction = null;
                                    try (Session session = sqlCommand.getSessionFactory().openSession()) {

                                        transaction = session.beginTransaction();
                                        String hql = "UPDATE userInfo set userCreditValue = :userCreditValue where phoneNumber= :phoneNumber";
                                        Query query = session.createQuery(hql);
                                        query.setParameter("userCreditValue", creditValue);
                                        query.setParameter("phoneNumber", userPhone);

                                        int result = query.executeUpdate();
                                        logger.info("credit updating result {}", result);

                                        // commit transaction
                                        transaction.commit();
                                        session.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (transaction != null) {
                                            transaction.rollback();
                                        }
                                        e.printStackTrace();
                                    }
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
                                String userId = "";
                                String userPhone = "";
                                String sessionID = "";
                                userInfo employee = new userInfo();

                                try {
                                    userId = exchange.getQueryParameters().get("userId").getFirst();
                                    userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                    try {
                                        if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                            logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                            return responseType.SESSION_IS_NOT_VALID;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // todo transfer money for his/her banking

                                    Transaction transaction = null;
                                    try (Session session = sqlCommand.getSessionFactory().openSession()) {

                                        transaction = session.beginTransaction();
                                        String hql = "FROM userInfo where phoneNumber= :phoneNumber";
                                        Query query = session.createQuery(hql);
                                        query.setParameter("phoneNumber", userPhone);
                                        List qq = query.list();
                                        for (Iterator iterator1 = qq.iterator(); iterator1.hasNext(); ) {
                                            employee = (userInfo) iterator1.next();
                                            logger.info(" Lookup credit value {} for user {}", employee.getUserCreditValue(), userPhone);
                                        }

                                        // commit transaction
                                        transaction.commit();
                                        session.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (transaction != null) {
                                            transaction.rollback();
                                        }
                                        e.printStackTrace();
                                    }

                                    return gson.toJson(employee);
                                } catch (NullPointerException e) {
                                    return responseType.FATAL_INTERNAL_ERROR;
                                }

                            }
                        })

                        .addExactPath("/v1/fire", new ServiceHandler() {
                            @Override
                            public String serve(HttpServerExchange exchange) throws ExecutionException {
                                //fixme get all user info from json, then pars it.
                                String userPhone = "";
                                String userId = "";
                                String sessionID = "";
                                try {
                                    userId = exchange.getQueryParameters().get("userId").getFirst();
                                    userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                    sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                    if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                        logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                        return responseType.SESSION_IS_NOT_VALID;
                                    }
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


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                /* Select random gift, then send to the user.*/
                                Random rnd = new Random();
                                int selectResp = rnd.nextInt(5);
                                logger.info("selectResp value {}", selectResp);
                                Response response = new Response();
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

                                                resp = Utility.generteQuezz(1);
                                                Quezz simpleQuezz = new Quezz();
                                                simpleQuezz.setQuezzName("simple");
                                                simpleQuezz.setQuezzTime("5");
                                                simpleQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    simpleQuezz.setQuezzMessage("Charge Account and get more gifts!");
                                                }
                                                simpleQuezz.setQuezzSubject(resp[0]);
                                                simpleQuezz.setQuezzOptions(resp[1]);
                                                simpleQuezz.setQuezzCredit("1000");
                                                respJson = gson.toJson(simpleQuezz);
                                                break;
                                            case 2:
                                                resp = Utility.generteQuezz(2);
                                                Quezz mediumQuezz = new Quezz();
                                                mediumQuezz.setQuezzName("meduim");
                                                mediumQuezz.setQuezzTime("10");
                                                mediumQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    mediumQuezz.setQuezzMessage("Charge Account and get more gifts!");
                                                }

                                                mediumQuezz.setQuezzSubject(resp[0]);
                                                mediumQuezz.setQuezzOptions(resp[1]);
                                                mediumQuezz.setQuezzCredit("2000");
                                                respJson = gson.toJson(mediumQuezz);
                                                break;
                                            case 3:
                                                resp = Utility.generteQuezz(3);
                                                Quezz complexQuezz = new Quezz();
                                                complexQuezz.setQuezzName("complex");
                                                complexQuezz.setQuezzTime("15");
                                                complexQuezz.setQuezzType("math");
                                                if (!cache.userCredit.asMap().containsKey(userPhone)) {
                                                    logger.info("user {} playing free", userPhone);
                                                    complexQuezz.setQuezzMessage("Charge Account and get more gifts!");
                                                }
                                                complexQuezz.setQuezzSubject(resp[0]);
                                                complexQuezz.setQuezzOptions(resp[1]);
                                                complexQuezz.setQuezzCredit("3000");
                                                respJson = gson.toJson(complexQuezz);
                                                break;

                                        }
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

                        }).

                                addExactPath("/v1/profile", new ServiceHandler() {

                                    @Override
                                    public String serve(HttpServerExchange exchange) throws ExecutionException {
                                        String userPhone = exchange.getQueryParameters().get("userPhone").getFirst();
                                        String sessionID = exchange.getQueryParameters().get("sessionID").getFirst();
                                        try {
                                            if (!IAM.sessionManager.validateSession(userPhone, sessionID)) {
                                                logger.error("session validation error. {} -> {}", userPhone, sessionID);
                                                return responseType.SESSION_IS_NOT_VALID;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        userInfo employee = new userInfo();
                                        //todo call core.cache, then send query.

                                        Transaction transaction = null;
                                        try (Session session = sqlCommand.getSessionFactory().openSession()) {

                                            transaction = session.beginTransaction();

                                            String hql = "FROM userInfo E WHERE E.phoneNumber = :userPhone";
                                            Query query = session.createQuery(hql);
                                            query.setParameter("userPhone", userPhone);
                                            List qq = query.list();
                                            for (Iterator iterator1 = qq.iterator(); iterator1.hasNext(); ) {
                                                employee = (userInfo) iterator1.next();
                                                logger.info(" Lookup phone {} sucecess.", employee.getPhoneNumber());
                                            }
                                            // commit transaction
                                            transaction.commit();
                                            session.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            if (transaction != null) {
                                                transaction.rollback();
                                            }
                                            e.printStackTrace();
                                        }
                                        return new Gson().toJson(employee);

                                    }
                                }))
                .build();
        server.start();


    }


}

