import java.util.Random;

public class Utility {

    public static String getRandomImage() {
        return "http://exmaple.com/IMG11111.jpg";
    }

    public static String getRandomVideo() {
        return "http://exmaple.com/vide00001.mp4";
    }

    public static String getRandomAudio() {
        return "http://exmaple.com/mp300001.mp3";
    }
    public static String getRandomText() {
        return "I am a random funny joke!";
    }
    public static int getUserCreditValue(String userId) throws NullPointerException {
        int value = Integer.parseInt(cache.userGifts.asMap().get(userId));
        return value;
    }

    public static String[] generteQuezz(int quezzComplexity) {
        String result[] = new String[2];
        Random digit = new Random();
        Random r = new Random();
        int n1 = 0;
        int n2 = 0;
        int n3 = 0;
        int nOption1 = 0;
        int nOption2 = 0;
        int realResp = 0;

        switch (quezzComplexity) {
            case 1:
                n1 = digit.nextInt(5);
                n2 = digit.nextInt(5);
                n3 = digit.nextInt(5);
                nOption1 = digit.nextInt(20);
                nOption2 = digit.nextInt(15);
                break;
            case 2:
                n1 = digit.nextInt(10);
                n2 = digit.nextInt(10);
                n3 = digit.nextInt(10);
                nOption1 = digit.nextInt(30);
                nOption2 = digit.nextInt(40);
                break;
            case 3:
                n1 = digit.nextInt(15);
                n2 = digit.nextInt(15);
                n3 = digit.nextInt(15);
                nOption1 = digit.nextInt(50);
                nOption2 = digit.nextInt(60);
                break;
        }
        realResp = n1 + n2 + n3;
        String subject = n1 + "+" + n2 + "+" + n3 + "=?";
        String responseOption1 = realResp + "," + nOption1 + "," + nOption2;
        String responseOption2 = nOption1 + "," + realResp + "," + nOption2;
        String responseOption3 = nOption2 + "," + nOption1 + "," + realResp;
        String[] list = {responseOption1, responseOption2, responseOption3};

        result[0] = subject;
        result[1] = list[r.nextInt(list.length)];
        return result;

    }
}
