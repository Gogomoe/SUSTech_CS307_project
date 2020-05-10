import com.google.gson.Gson;
import data.QueryOnlySimulation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Index {
    static List<QueryOnlySimulation> queries;
    public static void main(String[] args) throws Exception {
        queries = new ArrayList<>();
        var gson = new Gson();
        var sb = new StringBuilder();
        var query = QueryOnlySimulation.loadFromCsv("query_wuhan_guangzhou_30.csv");
        sb.append("wh_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_shenzhen_guangzhou_30.csv");
        sb.append("sz_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_fuzhou_guangzhou_30.csv");
        sb.append("fz_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_wuhan_guangzhou_50.csv");
        sb.append("wh_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_shenzhen_guangzhou_50.csv");
        sb.append("sz_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_fuzhou_guangzhou_50.csv");
        sb.append("fz_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_wuhan_guangzhou_100.csv");
        sb.append("wh_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_shenzhen_guangzhou_100.csv");
        sb.append("sz_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_fuzhou_guangzhou_100.csv");
        sb.append("fz_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_wuhan_guangzhou_150.csv");
        sb.append("wh_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_shenzhen_guangzhou_150.csv");
        sb.append("sz_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        query = QueryOnlySimulation.loadFromCsv("query_fuzhou_guangzhou_150.csv");
        sb.append("fz_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        Files.writeString(Path.of("queryOnly/querySimData.js"),sb.toString());
    }
}
