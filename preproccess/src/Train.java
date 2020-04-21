import java.util.ArrayList;
import java.util.List;

public class Train{
    static int ID_increasing = 1;
    int ID;
    String mark;
    String type;
    int departStation, arriveStation;
    long departTime, arriveTime;
    List<Train_Station> stations;

    public Train(String mark, String type, int departStation, int arriveStation, long arriveTime) {
        this.mark = mark;
        this.type = type;
        this.departStation = departStation;
        this.arriveStation = arriveStation;
        this.arriveTime = arriveTime;
        this.ID = ID_increasing++;
        stations = new ArrayList<>();
    }

}