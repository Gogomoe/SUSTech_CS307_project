package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class QueryOnlySimulation {
    private int[] elapsed;
    private int[] group;
    private long[] startTime;
    private int mid, quantile_90, max;
    private String average, tps;

    public QueryOnlySimulation(int size) {
        this.elapsed = new int[size];
        this.group = new int[size];
        this.startTime = new long[size];
    }

    public int[] getElapsed() {
        return elapsed;
    }

    public int[] getGroup() {
        return group;
    }

    public static QueryOnlySimulation loadFromCsv(String name) throws IOException {
        var lines = Files.readAllLines(Path.of("queryOnly/" + name))
                .stream().skip(1).map(l -> l.split(",")).collect(Collectors.toList());
        var query = new QueryOnlySimulation(lines.size());
        var start = Long.parseLong(lines.get(0)[0]);
        var sum = 0;
        for (int i = 0; i < lines.size(); i++) {
            query.startTime[i] = Long.parseLong(lines.get(i)[0]) - start;
            query.elapsed[i] = Integer.parseInt(lines.get(i)[1]);
            sum += query.elapsed[i];
            query.group[i] = Integer.parseInt(lines.get(i)[12]);
        }
        query.average = String.format("%.2f", 1.0*sum / lines.size());
        var elapse = Arrays.copyOf(query.elapsed, query.elapsed.length);
        Arrays.sort(elapse);
        query.mid = elapse[elapse.length / 2];
        query.quantile_90 = elapse[9 * elapse.length / 10];
        query.max = elapse[elapse.length - 1];
        query.tps = String.format("%.2f",1.0 * Arrays.stream(query.startTime).max().getAsLong() / elapse.length);
        return query;
    }

}
