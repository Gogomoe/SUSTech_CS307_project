import com.google.gson.Gson;
import data.Simulation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Index {
    static Gson gson;
    public static void main(String[] args) throws Exception {
        gson = new Gson();
        generateQueryPurchase();
        generateQueryOnlyData();
    }

    static void generateQueryPurchase() throws Exception{
        var sb = new StringBuilder();
        var purchaseBreviary = Simulation.loadFromCsv("queryPurchase/purchase_gzn_szb_30.csv").getBreviary();
        sb.append("gzn_szb_30_5=").append(gson.toJson(purchaseBreviary)).append("\n");

        purchaseBreviary = Simulation.loadFromCsv("queryPurchase/purchase_gzn_szb_50.csv").getBreviary();
        sb.append("gzn_szb_50_5=").append(gson.toJson(purchaseBreviary)).append("\n");

        purchaseBreviary = Simulation.loadFromCsv("queryPurchase/purchase_gzn_szb_100.csv").getBreviary();
        sb.append("gzn_szb_100_5=").append(gson.toJson(purchaseBreviary)).append("\n");

        purchaseBreviary = Simulation.loadFromCsv("queryPurchase/purchase_gzn_szb_150.csv").getBreviary();
        sb.append("gzn_szb_150_5=").append(gson.toJson(purchaseBreviary)).append("\n");


        Files.writeString(Path.of("queryPurchase/purchaseSimData.js"),sb.toString());
    }

    static void generateQueryOnlyData() throws Exception{
        var sb = new StringBuilder();
        var query = Simulation.loadFromCsv("queryOnly/query_wuhan_guangzhou_30.csv");
        sb.append("wh_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_shenzhen_guangzhou_30.csv");
        sb.append("sz_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_fuzhou_guangzhou_30.csv");
        sb.append("fz_gz_30_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_wuhan_guangzhou_50.csv");
        sb.append("wh_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_shenzhen_guangzhou_50.csv");
        sb.append("sz_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_fuzhou_guangzhou_50.csv");
        sb.append("fz_gz_50_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_wuhan_guangzhou_100.csv");
        sb.append("wh_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_shenzhen_guangzhou_100.csv");
        sb.append("sz_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_fuzhou_guangzhou_100.csv");
        sb.append("fz_gz_100_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_wuhan_guangzhou_150.csv");
        sb.append("wh_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_shenzhen_guangzhou_150.csv");
        sb.append("sz_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        query = Simulation.loadFromCsv("queryOnly/query_fuzhou_guangzhou_150.csv");
        sb.append("fz_gz_150_5=").append(gson.toJson(query)).append("\r\n");

        Files.writeString(Path.of("queryOnly/querySimData.js"),sb.toString());
    }


}
