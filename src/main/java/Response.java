public class Response {
    private String respType; // Video, Audio, Gift, etc.
    private String respMediaLink; // Audio, Video, etc.
    private String respText;   // message
    private String respCharacterName; // who want to show response?

    public String getRespType() {
        return respType;
    }

    public void setRespType(String respType) {
        this.respType = respType;
    }

    public String getRespMediaLink() {
        return respMediaLink;
    }

    public void setRespMediaLink(String respMediaLink) {
        this.respMediaLink = respMediaLink;
    }

    public String getRespText() {
        return respText;
    }

    public void setRespText(String respText) {
        this.respText = respText;
    }

    public String getRespCharacterName() {
        return respCharacterName;
    }

    public void setRespCharacterName(String respCharacterName) {
        this.respCharacterName = respCharacterName;
    }
}
