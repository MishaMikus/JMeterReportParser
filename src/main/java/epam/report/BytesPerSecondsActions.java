package epam.report;

import epam.model.Record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class BytesPerSecondsActions extends ReportContainer {

    Map<Long, Map<String, Double>> bytesPerSecondPerActions = new TreeMap<>();
    HashSet<String> actionSet = new HashSet();

    public BytesPerSecondsActions() {
        super();
    }

    @Override
    public void add(Record rec) {
        String label = rec.label;
        if ((!label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            label = correctLabel(label);
            Long deltaTime = 1000L * 60L;
            Long currentSecond = (rec.timeStamp.longValue() / deltaTime) * deltaTime;
            if (currentSecond > 0) {
                Map<String, Double> tmpMap = bytesPerSecondPerActions.get(currentSecond);
                if (tmpMap == null) {
                    tmpMap = new HashMap<>();
                }
                bytesPerSecondPerActions.put(currentSecond, tmpMap);
                String action = getAction(label);
                Double tmpBytes = tmpMap.get(action);
                if (tmpBytes == null) {
                    tmpBytes = rec.bytes;
                } else {
                    tmpBytes += rec.bytes;
                }
                tmpMap.put(action, tmpBytes);
                actionSet.add(action);
            }
        }
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        FileWriter writer = new FileWriter(fileName);
        List<String> headerList = null;
        for (Map.Entry<Long, Map<String, Double>> entry : bytesPerSecondPerActions.entrySet()) {

            if (headerList == null) {
                headerList = new ArrayList<>(actionSet);
                String headers = "";
                for (String header : headerList) {
                    headers += "," + header;
                }
                writer.append("Time").append(headers).append(",Overall\n");
            }
            writer.append(simpleDateFormat.format(new Date(entry.getKey() - 60L * 60L * 1000L)));
            String line = "";
            Double overall = 0d;
            for (String header : headerList) {
                Object value = entry.getValue().get(header);
                if (value == null) {
                    value = "0";
                }
                line += "," + value;
                overall += Double.parseDouble(value + "");
            }

            writer.append(line).append(",").append(overall.toString()).append("\n");
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
