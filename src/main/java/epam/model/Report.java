package epam.model;


public class Report {
    public int count;
    public String url;
    public int pass;
    public int fails;
    public int min;
    public int av;
    public int max;
    public double sd;
    public int p90;
    public double tps;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Report{");
        sb.append("av=").append(av);
        sb.append(", pass=").append(pass);
        sb.append(", fails=").append(fails);
        sb.append(", min=").append(min);
        sb.append(", max=").append(max);
        sb.append(", sd='").append(sd).append('\'');
        sb.append(", p90='").append(p90).append('\'');
        sb.append(", tps='").append(tps).append('\'');
        sb.append(", count='").append(count).append('\'');
        sb.append("}\n");
        return sb.toString();
    }
}
