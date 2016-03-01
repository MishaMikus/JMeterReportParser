package epam.report;

import epam.model.Record;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public abstract class ReportContainer {

    public Double startTime;
    public Double endTime;
    public String fileName;
    public String type;

    public final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public abstract void add(Record rec);

    public abstract void saveToFile() throws IOException, InterruptedException;

    public void logEnd() {
        System.out.println("SAVE " + new File(fileName).getName() + " DONE");
    }

    public void correctStartEndTime() {
        Double deltaTime = ("LOAD".equals(type)) ? (15d * 60d * 1000d) : (0d);
        startTime += deltaTime;
        endTime -= deltaTime;
    }

    public void calculateStartEndTime(Record rec) {
        if (startTime == null) {
            startTime = rec.timeStamp;
        }
        if (endTime == null) {
            endTime = rec.timeStamp;
        }

        startTime = startTime >= rec.timeStamp ? rec.timeStamp : startTime;
        endTime = endTime <= rec.timeStamp ? rec.timeStamp : endTime;
    }

    public ReportContainer(String type, String fileName) {
        this.type = type;
        this.fileName=fileName;
    }

    public boolean passTimeLimit(Double timeStamp) {
        return timeStamp >= startTime && timeStamp <= endTime;
    }

    public String getAction(String label) {
        String action = label.split(" \\[")[1].split("\\]")[0];
        if ("TC login".equals(action)) {
            action = "workout history WEB";
        }
        return action;
    }

    public String getUseCase(String label) {
        return label.split("\\] \\[")[1].split("\\]")[0];
    }
}
