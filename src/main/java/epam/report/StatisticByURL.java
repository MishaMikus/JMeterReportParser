package epam.report;

import epam.model.Record;
import epam.model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticByURL extends ReportContainer {

    public Map<String, List<Record>> statisticByURLMap = new HashMap<>();
    public Map<String, Report> statisticByURLMapCalculated = new HashMap<>();

    public StatisticByURL() {
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
            List<Record> list = statisticByURLMap.get(transAction);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(rec);
            statisticByURLMap.put(transAction, list);
            calculateStartEndTime(rec);
        }
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        for (Map.Entry<String, List<Record>> entry : statisticByURLMap.entrySet()) {
            statisticByURLMapCalculated.put(entry.getKey(), new Report(entry.getValue(), startTime, endTime));
        }
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,AV,Pass,Fails,errRate,outLimit,Min,Max,SD,90%,TPS,count\n");
        for (Map.Entry<String, Report> urlEntry : statisticByURLMapCalculated.entrySet()) {
            writer.append(urlEntry.getKey()).append(",");
            Report report = urlEntry.getValue();
            writer.append(
                    report.av.toString()).append(",")
                    .append(report.pass.toString()).append(",")
                    .append(report.fails.toString()).append(",")
                    .append(new Double(report.fails / report.count * 100).intValue() + " %").append(",")
                    .append(new Double(report.outLimitCount / report.count * 100).intValue() + " %").append(",")
                    .append(report.min.toString()).append(",")
                    .append(report.max.toString()).append(",")
                    .append(report.sd.toString()).append(",")
                    .append(report.p90.toString()).append(",")
                    .append(report.tps.toString()).append(",")
                    .append(report.count.toString()).append(",\n");
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
