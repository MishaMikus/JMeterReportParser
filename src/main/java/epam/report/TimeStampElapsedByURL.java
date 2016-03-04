package epam.report;

import epam.model.Record;
import epam.model.UrlEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TimeStampElapsedByURL extends ReportContainer {
    Map<Double, UrlEntry> timeStampElapsedByURLMap = new TreeMap<>();

    public TimeStampElapsedByURL() {
        super();
    }

    @Override
    public void add(Record rec) {
        if ((!rec.label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            String label = correctLabel(rec.label);
            String method = label.trim().split(" ")[0];

            if (("HTTP".equals(method)) || ("".equals(method))) {
                System.err.println(label);
            }
            String transAction = method + " " + rec.url.replaceAll("\"", "");
            timeStampElapsedByURLMap.put(rec.timeStamp, new UrlEntry(getAction(rec.label), rec.elapsed, transAction));
        }
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        FileWriter writer = new FileWriter(fileName);

        writer.append("Time,elapsed,Action,URL\n");
        for (Map.Entry<Double, UrlEntry> entry : timeStampElapsedByURLMap.entrySet()) {
            UrlEntry urlEntry = entry.getValue();
            writer.append(simpleDateFormat.format(new Date(entry.getKey().longValue() - 60 * 60 * 1000))).append(",")
                    .append(urlEntry.elapsed.longValue() + "").append(",")
                    .append(urlEntry.action).append(",")
                    .append(urlEntry.url).append("\n");
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
