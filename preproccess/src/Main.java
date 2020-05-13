import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Main {
    static PrintWriter pw;
    static HashMap<String, Station> stations;
    static HashMap<String, Train> trains;
    static DateTimeFormatter timeFormatter;
    public static void main(String[] args) throws Exception {
        pw = new PrintWriter("../database/data.sql");
        stations = new HashMap<>(3000);
        trains = new HashMap<>(10000);
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm", Locale.CHINA);
        var seats = """
                begin;
                insert into seat (seat_id, name) values (1, '二等座');
                insert into seat (seat_id, name) values (2, '一等座');
                insert into seat (seat_id, name) values (3, '硬卧');
                insert into seat (seat_id, name) values (4, '软卧');
                """;
        pw.println(seats);
        readStationCSV();
        writeStations();
        readEachStation();
        trains.values().forEach(Main::calculateTimes);
        writeTrains();
        pw.println("commit;");
        pw.close();
    }

    private static void calculateTimes(Train t) {
        //剩余运行时间降序，即按照车的运行顺序
        t.stations.sort(Comparator.comparingLong(i -> ((Train_Station) i).runTime).reversed());

        while (t.arriveTime - t.stations.get(0).runTime < 0) {
            t.arriveTime += 8640_0000;//一天的毫秒数
        }
        t.departTime = t.arriveTime - t.stations.get(0).runTime;
        t.arriveTime /= 1000;//转换为秒
        t.departTime /= 1000;
        t.stations.forEach(s -> {
            if (s.runTime == 0) {
                s.arriveTime = t.arriveTime;
                s.leaveTime = s.arriveTime+180;//停车3分钟
                return;
            }
            s.leaveTime = t.arriveTime - s.runTime / 1000;
            s.arriveTime = s.leaveTime - 180;//停车3分钟
        });
    }

    static void writeTrains() {
        var i_train_static = """
                insert into train_static (train_static_id, code, type, depart_station, arrive_station, depart_time, arrive_time) 
                values (A0, 'A1', 'A2', A3, A4, justify_interval(interval 'A5'), justify_interval(interval 'A6'));
                """;
        var i_train_station = """
                insert into train_station (train_static_id, station_id, arrive_time, depart_time) values 
                (A0, A1, justify_interval(interval 'A2'), justify_interval(interval 'A3'));
                """;
        var i_train_station_price = """
                insert into train_station_price (train_static_id, station_id, seat_id, remain_price) values 
                (A0, A1, A2, A3);
                """;
        var i_train_seat = """
                insert into train_seat (train_static_id, seat_id, count) values (A0, A1, A2);
                """;
        var commonSeatNums = new String[]{"","1656", "80", "264", "44"};
        // 无 K T Z S Y P
        var highSeatNums = new String[]{"","1140", "240", "648", "144"};
        // C D G
        trains.values().forEach(t -> {
            if(t.departStation==t.arriveStation)
                return;
            //抛弃环线

            var hasSeat = false;
            for (int i=1;i<=4;i++){
                if(t.stations.get(0).prices[i]==-1) continue;
                hasSeat = true;
                for(int j=1;j<t.stations.size();j++){
                    if(t.stations.get(j).prices[i]>t.stations.get(j-1).prices[i]){
                        return;//价格<0就不写入
                    }
                }
            }
            if(!hasSeat) return;//无座位车不写

            var state = i_train_static.replace("A0", "" + t.ID);
            state = state.replace("A1", t.mark);
            state = state.replace("A2", t.type);
            state = state.replace("A3", "" + t.departStation);
            state = state.replace("A4", "" + t.arriveStation);
            state = state.replace("A5", "" + t.departTime);
            state = state.replace("A6", "" + t.arriveTime);
            pw.println(state);

            //座位数
            for(int i=1;i<5;i++){
                if(t.stations.get(0).prices[i]==-1) continue;
                state = i_train_seat.replace("A0",""+t.ID);
                state = state.replace("A1",""+i);
                state = state.replace("A2",t.mark.matches("[DGC].+")?highSeatNums[i]:commonSeatNums[i]);
                pw.println(state);
            }

            t.stations.forEach(s -> {
                var $state = i_train_station.replace("A0", "" + t.ID);
                $state = $state.replace("A1", "" + s.stationID);
                $state = $state.replace("A2", "" + s.arriveTime);
                $state = $state.replace("A3", "" + s.leaveTime);
                pw.println($state);

                for (int i = 1; i < s.prices.length; i++) {
                    if (s.prices[i] == -1) continue;
                    $state = i_train_station_price.replace("A0", "" + t.ID);
                    $state = $state.replace("A1", "" + s.stationID);
                    $state = $state.replace("A2", "" + i);
                    $state = $state.replace("A3", "" + s.prices[i]);
                    pw.println($state);
                }
            });
        });
        pw.println();
    }

    static void writeStations() {
        var str = """
                insert into station (station_id, name, city, code) values (A0, 'A1', 'A2', 'A3');
                """;
        stations.values().forEach(s -> {
            var state = str.replace("A0", "" + s.ID);
            state = state.replace("A1", s.name);
            state = state.replace("A2", s.city);
            state = state.replace("A3", s.code);
            pw.println(state);
        });
        pw.println();
    }

    static void readStationCSV() throws Exception {
        Files.readAllLines(Paths.get("12307/station.csv")).stream()
                .map(l -> l.split(",")).map(l -> new Station(l[0], l[1], l[2]))
                .forEach(s -> stations.put(s.name, s));
    }

    static void readEachStation() throws Exception {
        var root = new File("12307");
        for (File csv : Objects.requireNonNull(root.listFiles(f -> f.getName().contains("csv") && !f.getName().contains("station")))) {
            Files.readAllLines(csv.toPath()).stream().skip(13).map(l -> l.split(","))
                    .forEach(l -> {
                        var mark = l[0].replaceAll(" .+", "");//干掉 *, -, +等东西
                        var train = trains.get(mark);
                        if (train == null) {
                            var type = l[2];
                            var d_a = l[1].split("-");
                            var departStation = stations.get(d_a[0]).ID;
                            var arriveStation = stations.get(d_a[1]).ID;
                            var arriveTime = getStaticTimeStamp(l[6]);
                            train = new Train(mark, type, departStation, arriveStation, arriveTime);
                            //特殊处置终到站
                            var arriveTrainStation = new Train_Station(arriveStation, 0);
                            for (int i = 1; i <= 4; i++) {
                                if (l[i + 8].contains("-"))
                                    continue;
                                arriveTrainStation.prices[i] = 0;//终到站，票价为0
                            }
                            train.stations.add(arriveTrainStation);

                            trains.put(mark, train);
                        }

                        var currStation = stations.get(l[3]).ID;
                        var runTime = getRunTime(l[8]);
                        var trainStation = new Train_Station(currStation, runTime);
                        //leave time will be set by calculating later.

                        for (int i = 1; i <= 2; i++) {
                            if (l[i + 8].contains("-"))
                                continue;
                            var price = (int) Double.parseDouble(l[i + 8]);
                            trainStation.prices[i] = price;
                        }

                        for (int i = 3; i <= 4; i++) {
                            if (l[i + 8].contains("/")) {
                                var prices = l[i + 8].split("/");
                                var price = 0.0;
                                for (String s : prices) price += Double.parseDouble(s);
                                price /= prices.length;
                                trainStation.prices[i] = (int) price;
                            } else {
                                try {
                                    var price = Double.parseDouble(l[i + 8]);
                                    trainStation.prices[i] = (int) price;
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        if (!train.stations.contains(trainStation)) {
                            train.stations.add(trainStation);
                        }
                    });
        }
    }

    static long getStaticTimeStamp(String t) {
        return LocalDateTime.parse("1970-1-1 " + t, timeFormatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    static long getRunTime(String t) {
        var h_m = t.split(":");
        return (Integer.parseInt(h_m[0]) * 60 + Integer.parseInt(h_m[1])) * 60 * 1000;
    }
}
