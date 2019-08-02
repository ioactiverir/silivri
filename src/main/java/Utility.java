import java.util.Random;

public class Utiliti {
    private static int getUserCreditValue(String userId) throws NullPointerException {
        int value = Integer.parseInt(cache.userGifts.asMap().get(userId));
        logger.info("{} gift for user {} found.", value, userId);
        logger.info("add 1000 rial gift for user {}", userId);
        return value;
    }

    private static String[] generteQuezz(int quezzComplexity) {
        String result[] = new String[2];
        switch (quezzComplexity) {
            case 1:
                Random digit = new Random();
                Random r = new Random();

                int n1 = digit.nextInt(5);
                int n2 = digit.nextInt(5);
                int n3 = digit.nextInt(5);
                int nOption1 = digit.nextInt(20);
                int nOption2 = digit.nextInt(15);
                int realResp = n1 + n2 + n3;

                String subject = n1 + "+" + n2 + "+" + n3 + "=?";
                String responseOption1 = realResp + "," + nOption1 + "," + nOption2;
                String responseOption2 = nOption1 + "," + realResp + "," + "," + nOption2;
                String responseOption3 = nOption1 + "," + nOption2 + "," + realResp;
                String[] list={responseOption1,responseOption2,responseOption3};

                result[0] = subject;
                result[1] = list[r.nextInt(list.length)];
                break;
        }
        return result;

    }
}
