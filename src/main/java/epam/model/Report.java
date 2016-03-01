package epam.model;


public class Report {
    public Double count= 0d;
    public String url;
    public Double pass=0d;
    public Double fails=0d;
    public Double min=0d;
    public Double av=0d;
    public Double max=0d;
    public Double sd=0d;
    public Double p90=0d;
    public Double tps=0d;

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
