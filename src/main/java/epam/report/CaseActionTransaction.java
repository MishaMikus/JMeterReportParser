package epam.report;

import epam.model.Record;
import epam.model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CaseActionTransaction extends ReportContainer {

    private Map<String, Map<String, Map<String, List<Record>>>> caseActionTransActionDraft = new HashMap<>();
    private Map<String, Map<String, Map<String, Report>>> caseActionTransAction = new HashMap<>();

    public CaseActionTransaction(String type, String fn) {
        super(type, fn);
    }

    @Override
    public void add(Record rec) {
        String label = rec.label;
        if ((!label.startsWith("TC")) && (!("null".equals(rec.url)))) {
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

            String useCase = getUseCase(label);
            String action = getAction(label);
            String method = label.trim().split(" ")[0];

            if (("HTTP".equals(method)) || ("".equals(method))) {
                System.err.println(label);
            }
            String transAction = method + " " + rec.url.replaceAll("\"", "");
            calculateStartEndTime(rec);
            addTransAction(useCase, action, transAction, rec);
        }
    }

    private void addTransAction(String useCase, String action, String transAction, Record rec) {
        if (caseActionTransActionDraft.get(useCase) == null) {
            caseActionTransActionDraft.put(useCase, new HashMap<>());
        }
        if (caseActionTransActionDraft.get(useCase).get(action) == null) {
            caseActionTransActionDraft.get(useCase).put(action, new HashMap<>());
        }
        if (caseActionTransActionDraft.get(useCase).get(action).get(transAction) == null) {
            caseActionTransActionDraft.get(useCase).get(action).put(transAction, new ArrayList<>());
        }
        caseActionTransActionDraft.get(useCase).get(action).get(transAction).add(rec);
    }


    private void calculate() {
        correctStartEndTime();
        for (Map.Entry<String, Map<String, Map<String, List<Record>>>> caseEntry : caseActionTransActionDraft.entrySet()) {
            for (Map.Entry<String, Map<String, List<Record>>> actionEntry : caseEntry.getValue().entrySet()) {
                for (Map.Entry<String, List<Record>> transActionEntry : actionEntry.getValue().entrySet()) {
                    addReport(caseEntry.getKey(), actionEntry.getKey(), transActionEntry.getKey(), makeReport(transActionEntry.getValue()));
                }
            }
        }
    }


    private void addReport(String useCase, String action, String transAction, Report report) {
        if (caseActionTransAction.get(useCase) == null) {
            caseActionTransAction.put(useCase, new HashMap<>());
        }
        if (caseActionTransAction.get(useCase).get(action) == null) {
            caseActionTransAction.get(useCase).put(action, new HashMap<>());
        }
        caseActionTransAction.get(useCase).get(action).put(transAction, report);
    }

    private Report makeReport(List<Record> list) {
        Report report = new Report();
        int elapsedSum = 0;
        double elapsedSum2 = 0;
        report.count = 0d;
        report.fails = 0d;
        Double startTimeLocal = null;
        Double endTimeLocal = null;
        for (Record record : list) {
            if (passTimeLimit(record.timeStamp)) {
                if (0d < record.elapsed) {
                    elapsedSum += record.elapsed;
                    report.fails += (("FALSE".equals(record.success)) ? 1 : 0);
                    report.count++;

                    //ResponseTime
                    if ((report.min == null) || (report.min == 0)) {
                        report.min = record.elapsed;
                    }
                    if (report.max == null) {
                        report.max = record.elapsed;
                    }

                    if (record.elapsed >= report.max) {
                        report.max = record.elapsed;
                    }
                    if ((record.elapsed <= report.min)) {
                        report.min = record.elapsed;
                    }

                    //Times[start,stop]

                    if (startTimeLocal == null) startTimeLocal = record.timeStamp;
                    startTimeLocal = (record.timeStamp <= startTimeLocal) ? record.timeStamp : startTimeLocal;

                    if (endTimeLocal == null) endTimeLocal = record.timeStamp;
                    endTimeLocal = (record.timeStamp >= endTimeLocal) ? record.timeStamp : endTimeLocal;
                }
            }
        }

        if (report.count.intValue() > 0) {
            report.av = elapsedSum / report.count;
            double dtSeconds = ((endTimeLocal - startTimeLocal) / 1000d);
            report.tps = report.count / dtSeconds;
        }

        if (report.count >= 2) {
            for (Record record : list) {
                if (passTimeLimit(record.timeStamp)) {
                    double d = record.elapsed - report.av;
                    elapsedSum2 += (d * d);
                    report.sd = Math.sqrt(elapsedSum2 / (report.count));
                    if ("NaN".equals(report.sd + "")) {
                        System.err.println("elapsedSum2=" + elapsedSum2 + " n=" + report.count + " Math.sqrt(elapsedSum2 /(n-1))" + Math.sqrt(elapsedSum2 / (report.count - 1)));
                    }
                }
            }
            Collections.sort(list, (o1, o2) -> o1.elapsed.compareTo(o2.elapsed));
            report.p90 = list.get((int) ((report.count * 90) / 100)).elapsed;
        }

        report.pass = list.size() - report.fails;
        report.url = list.get(0).url;
        if (report.count == 0d) return null;
        return report;
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        calculate();
        FileWriter writer = new FileWriter(fileName);
        writer.append("UseCase,Action,URL,AV,Pass,Fails,Min,Max,SD,90%,TPS,count\n");
        for (Map.Entry<String, Map<String, Map<String, Report>>> caseEntry : caseActionTransAction.entrySet()) {
            for (Map.Entry<String, Map<String, Report>> actionEntry : caseEntry.getValue().entrySet()) {
                for (Map.Entry<String, Report> transActionEntry : actionEntry.getValue().entrySet()) {
                    Report report = transActionEntry.getValue();
                    if (report != null) {
                        writer.append(caseEntry.getKey()).append(",")
                                .append(actionEntry.getKey()).append(",")
                                .append(transActionEntry.getKey()).append(",");
                        writer.append(report.av.toString()).append(",")
                                .append(report.pass.toString()).append(",")
                                .append(report.fails.toString()).append(",")
                                .append(report.min.toString()).append(",")
                                .append(report.max.toString()).append(",")
                                .append(report.sd.toString()).append(",")
                                .append(report.p90.toString()).append(",")
                                .append(report.tps.toString()).append(",")
                                .append(report.count.toString()).append(",\n");
                    }
                }
            }
        }
        writer.flush();
        writer.close();
        logEnd();
    }
}
