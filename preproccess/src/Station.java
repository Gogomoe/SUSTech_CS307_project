public class Station {
    static int ID_increasing = 1;
    String name;
    String city;
    String code;
    int ID;

    public Station(String code, String name, String city) {
        this.name = name;
        this.city = city;
        this.code = code;
        ID = ID_increasing++;
    }
}
