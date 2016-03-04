package epam.report;

import epam.model.Record;
import epam.model.UserErrorEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class UserPerSecondVsErrorByAction extends ReportContainer {

    HashSet<String> actionSet = new HashSet();
    Map<Long, UserErrorEntry> userPerSecond = new HashMap<>();

    public UserPerSecondVsErrorByAction() {
        super();
    }

    @Override
    public void add(Record rec) {
        if ((!rec.label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            String label = correctLabel(rec.label);
            String action = getAction(label);
            actionSet.add(action);
            Long deltaTime = 1000L * 10L;
            Long currentSecond = (rec.timeStamp.longValue() / deltaTime) * deltaTime;
            if (currentSecond > 0L) {
                UserErrorEntry tmpUserErrorEntry = userPerSecond.get(currentSecond);
                if (tmpUserErrorEntry == null) {
                    tmpUserErrorEntry = new UserErrorEntry();
                }
                tmpUserErrorEntry.threadNameSet.add(rec.threadName);
                Long errorCount = tmpUserErrorEntry.errorCountByActionMap.get(action);
                if (errorCount == null) {
                    errorCount = 0L;
                }
                if ("FALSE".equals(rec.success.toUpperCase()))
                    errorCount++;
                tmpUserErrorEntry.errorCountByActionMap.put(action, errorCount);


                userPerSecond.put(currentSecond, tmpUserErrorEntry);
            }
        }
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        FileWriter writer = new FileWriter(fileName);
        String errorByAction = "";
        Set<String> actionSetFilter = new HashSet<>();
        for (String action : actionSet) {
            Long sum = 0L;
            for (Map.Entry<Long, UserErrorEntry> entry : userPerSecond.entrySet()) {
                Long value = entry.getValue().errorCountByActionMap.get(action);
                sum += ((value == null) ? 0L : value);
            }
            if (sum > 0L) {
                actionSetFilter.add(action);
            }
        }

        for (String action : actionSetFilter) {
            errorByAction += ",ERR-" + action;
        }

        writer.append("Time,UserCount" + errorByAction + "\n");
        for (Map.Entry<Long, UserErrorEntry> entry : userPerSecond.entrySet()) {
            writer.append(simpleDateFormat.format(new Date(entry.getKey() - 60 * 60 * 1000))).append(",");
            UserErrorEntry userErrorEntry = entry.getValue();

            String errors = "";
            for (String action : actionSetFilter) {
                Long errorCount = userErrorEntry.errorCountByActionMap.get(action);
                if (errorCount == null) errorCount = 0L;
                errors += "," + errorCount;
            }
            writer.append(Integer.toString(userErrorEntry.threadNameSet.size())).append(errors).append("\n");
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
