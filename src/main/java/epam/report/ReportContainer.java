package epam.report;

import epam.model.Record;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class ReportContainer {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public Double startTime;
    public Double endTime;
    public String fileName;
    public String type;
    public String name;

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

    public ReportContainer() {
        this.name = this.getClass().getName();
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

    public String correctLabel(String label) {
        Map<String, String> exceptReplaceMap = new HashMap<>();
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 1]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 1]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 2]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 2]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 3]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 3]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 4]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 4]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 5]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case  mobile 5]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request login [login] [Use Case mobile 6]",
                "POST v3/OAuth2/Authorize [login] [Use Case mobile 6]"
        );
        exceptReplaceMap.put("HTTP Request workouts [post workout] [Use Case mobile 3]",
                "POST v3/users/me/workouts [post workout] [Use Case mobile 3]"
        );
        exceptReplaceMap.put("HTTP Request lifetime-stats [statistics] [Use Case mobile 3]",
                "GET v3/Users/me/lifetime-stats [statistics] [Use Case mobile 3]"
        );
        exceptReplaceMap.put("HTTP Request lifetime-stats [statistics] [Use Case mobile 5]",
                "GET v3/Users/me/lifetime-stats [statistics] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request lifetime-stats [statistics] [Use Case  mobile 5]",
                "GET v3/Users/me/lifetime-stats [statistics] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request lifetime-stats [statistics] [Use Case mobile 6]",
                "GET v3/Users/me/lifetime-stats [statistics] [Use Case mobile 6]"
        );
        exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case mobile 3]",
                "GET v3/Users/me/workouts/history [statistics] [Use Case mobile 3]"
        );
        exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case mobile 5]",
                "GET v3/Users/me/workouts/history [statistics] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case  mobile 5]",
                "GET v3/Users/me/workouts/history [statistics] [Use Case mobile 5]"
        );
        exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case mobile 6]",
                "GET v3/Users/me/workouts/history [statistics] [Use Case mobile 6]"
        );
        for (Map.Entry<String, String> entry : exceptReplaceMap.entrySet()) {
            if (entry.getKey().equals(label)) {
                label = entry.getValue();
            }
        }
        return label;
    }

}
