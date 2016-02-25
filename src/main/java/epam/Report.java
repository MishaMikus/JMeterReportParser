package epam;


public class Report {
    String url;
    int pass;
    int fails;
    int min;
    int av;
    int max;
    double sd;
    int p90;
    double tps;

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
        sb.append("}\n");
        return sb.toString();
    }
}
