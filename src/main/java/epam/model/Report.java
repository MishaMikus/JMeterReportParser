package epam.model;

import java.util.Collections;
import java.util.List;

public class Report {

    private static final Double ELAPSED_LIMIT = 400d;

    public Double count = 0d;
    public String url;
    public Double pass = 0d;
    public Double fails = 0d;
    public Double min = 0d;
    public Double av = 0d;
    public Double max = 0d;
    public Double sd = 0d;
    public Double p90 = 0d;
    public Double tps = 0d;
    public Double outLimitCount = 0d;

    public Report(List<Record> list, Double startTime, Double endTime) {
        int elapsedSum = 0;
        double elapsedSum2 = 0;
        count = 0d;
        fails = 0d;
        Double startTimeLocal = null;
        Double endTimeLocal = null;
        for (Record record : list) {
            if (record.timeStamp >= startTime && record.timeStamp <= endTime) {
                elapsedSum += record.elapsed;
                fails += (("FALSE".equals(record.success.toUpperCase())) ? 1d : 0d);
                count++;
                if (record.elapsed > ELAPSED_LIMIT) outLimitCount++;

                //ResponseTime
                if ((min == null) || (min == 0)) {
                    min = record.elapsed;
                }
                if (max == null) {
                    max = record.elapsed;
                }

                if (record.elapsed >= max) {
                    max = record.elapsed;
                }
                if ((record.elapsed <= min)) {
                    min = record.elapsed;
                }

                //Times[start,stop]

                if (startTimeLocal == null) startTimeLocal = record.timeStamp;
                startTimeLocal = (record.timeStamp <= startTimeLocal) ? record.timeStamp : startTimeLocal;

                if (endTimeLocal == null) endTimeLocal = record.timeStamp;
                endTimeLocal = (record.timeStamp >= endTimeLocal) ? record.timeStamp : endTimeLocal;
            }
        }

        if (count.intValue() > 0) {
            av = elapsedSum / count;
            double dtSeconds = ((endTimeLocal - startTimeLocal) / 1000d);
            tps = count / dtSeconds;
        }

        if (count >= 2) {
            for (Record record : list) {
                if (record.timeStamp >= startTime && record.timeStamp <= endTime) {
                    double d = record.elapsed - av;
                    elapsedSum2 += (d * d);
                    sd = Math.sqrt(elapsedSum2 / (count));
                    if ("NaN".equals(sd + "")) {
                        System.err.println("elapsedSum2=" + elapsedSum2 + " n=" + count + " Math.sqrt(elapsedSum2 /(n-1))" + Math.sqrt(elapsedSum2 / (count - 1)));
                    }
                }
            }
            Collections.sort(list, (o1, o2) -> o1.elapsed.compareTo(o2.elapsed));
            p90 = list.get((int) ((count * 90) / 100)).elapsed;
        }

        pass = list.size() - fails;
        url = list.get(0).url;
    }

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
        sb.append(", outLimitCount='").append(outLimitCount).append('\'');
        sb.append("}\n");
        return sb.toString();
    }
}