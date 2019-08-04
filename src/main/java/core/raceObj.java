package core;

public class raceObj {
   private  String  value;
   private int id;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public raceObj(int id, String value) {
        this.id = id;
        this.value = value;
    }
}
