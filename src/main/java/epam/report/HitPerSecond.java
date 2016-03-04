package epam.report;

import epam.model.Record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class HitPerSecond extends ReportContainer {

    public Map<Double, Double> hitPerSecond = new TreeMap<>();

    public HitPerSecond() {
        super();
    }

    @Override
    public void add(Record rec) {
        if ((!rec.label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            calculateStartEndTime(rec);
            Long deltaTime = 1000L;
            Long currentSecond = (rec.timeStamp.longValue() / deltaTime) * deltaTime;
            if (currentSecond > 0) {
                if (hitPerSecond.get(Double.valueOf(currentSecond)) == null) {
                    hitPerSecond.put(Double.valueOf(currentSecond), 1d);
                }
                hitPerSecond.put(Double.valueOf(currentSecond), hitPerSecond.get(Double.valueOf(currentSecond)) + 1);
            }
        }
    }

    @Override
    public void saveToFile() throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.append("Time,Count\n");
        correctStartEndTime();
        try {
            for (Map.Entry<Double, Double> entry : hitPerSecond.entrySet()) {
                Double key = entry.getKey();
                if (key >= startTime && key <= endTime) {
                    writer.append(simpleDateFormat.format(new Date((long) (key - 60 * 60 * 1000)))).append(",");
                    writer.append(entry.getValue().longValue() + "").append("\n");
                }
            }
        } catch (Exception ignored) {
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
