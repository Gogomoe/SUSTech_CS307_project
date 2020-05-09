import com.google.gson.Gson;
import data.QueryOnlySimulation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Index {
    static List<QueryOnlySimulation> queries;
    public static void main(String[] args) throws Exception {
        queries = new ArrayList<>();
        var gson = new Gson();
        var query = QueryOnlySimulation.loadFromCsv("query_wuhan_guangzhou.csv");
        Files.writeString(Path.of("queryOnly/wh_gz_100_5.js"),"wh_gz_100_5='"+gson.toJson(query)+"'");

        query = QueryOnlySimulation.loadFromCsv("query_shenzhen_guangzhou.csv");
        Files.writeString(Path.of("queryOnly/sz_gz_100_5.js"),"sz_gz_100_5='"+gson.toJson(query)+"'");

        query = QueryOnlySimulation.loadFromCsv("query_fuzhou_guangzhou.csv");
        Files.writeString(Path.of("queryOnly/fz_gz_100_5.js"),"fz_gz_100_5='"+gson.toJson(query)+"'");
    }
}
