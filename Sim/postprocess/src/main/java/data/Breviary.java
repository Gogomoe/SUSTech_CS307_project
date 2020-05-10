package data;

public class Breviary {
    private int mid, quantile_90, max;
    private String average, tps;

    public void setMid(int mid) {
        this.mid = mid;
    }

    public void setQuantile_90(int quantile_90) {
        this.quantile_90 = quantile_90;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public int getMid() {
        return mid;
    }

    public int getQuantile_90() {
        return quantile_90;
    }

    public int getMax() {
        return max;
    }

    public String getAverage() {
        return average;
    }

    public String getTps() {
        return tps;
    }

    @Override
    public String toString() {
        return "Breviary{" +
                "mid=" + mid +
                ", quantile_90=" + quantile_90 +
                ", max=" + max +
                ", average='" + average + '\'' +
                ", tps='" + tps + '\'' +
                '}';
    }
}
