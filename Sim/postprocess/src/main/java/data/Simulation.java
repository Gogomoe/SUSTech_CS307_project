package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Simulation {
    private int[] elapsed;
    private int[] group;
    private long[] startTime;
    private Breviary breviary;

    public Simulation(int size) {
        this.elapsed = new int[size];
        this.group = new int[size];
        this.startTime = new long[size];
        this.breviary = new Breviary();
    }

    public int[] getElapsed() {
        return elapsed;
    }

    public int[] getGroup() {
        return group;
    }

    public long[] getStartTime() {
        return startTime;
    }

    public Breviary getBreviary() {
        return breviary;
    }

    public static Simulation loadFromCsv(String name) throws IOException {
        var lines = Files.readAllLines(Path.of(name))
                .stream().skip(1).map(l -> l.split(",")).collect(Collectors.toList());
        var query = new Simulation(lines.size());
        var start = Long.parseLong(lines.get(0)[0]);
        var sum = 0;
        for (int i = 0; i < lines.size(); i++) {
            query.startTime[i] = Long.parseLong(lines.get(i)[0]) - start;
            query.elapsed[i] = Integer.parseInt(lines.get(i)[1]);
            sum += query.elapsed[i];
            query.group[i] = Integer.parseInt(lines.get(i)[12]);
        }
        query.breviary.setAverage(String.format("%.2f", 1.0*sum / lines.size()));
        var elapse = Arrays.copyOf(query.elapsed, query.elapsed.length);
        Arrays.sort(elapse);
        query.breviary.setMid(elapse[elapse.length / 2]);
        query.breviary.setQuantile_90(elapse[9 * elapse.length / 10]);
        query.breviary.setMax(elapse[elapse.length - 1]);
        query.breviary.setTps(String.format("%.2f",1.0 * elapse.length/Arrays.stream(query.startTime).max().getAsLong()*1000));
        return query;
    }

}
