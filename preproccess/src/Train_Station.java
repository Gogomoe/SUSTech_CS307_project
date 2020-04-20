import java.util.Arrays;
import java.util.Objects;

public class Train_Station {

    int stationID;
    long arriveTime, leaveTime;
    long runTime;
    int[] prices;

    public Train_Station(int stationID, long runTime) {
        this.stationID = stationID;
        this.arriveTime = -1;
        this.leaveTime = -1;
        this.runTime = runTime;
        prices = new int[5];
        Arrays.fill(prices,-1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train_Station that = (Train_Station) o;
        return stationID == that.stationID &&
                arriveTime == that.arriveTime &&
                leaveTime == that.leaveTime;
//                runTime == that.runTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationID, arriveTime, leaveTime, runTime);
    }
}
