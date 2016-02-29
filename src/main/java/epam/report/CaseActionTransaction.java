package epam.report;

import epam.model.Record;
import epam.model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CaseActionTransaction implements ReportContainer {

    private Map<String, Map<String, Map<String, List<Record>>>> caseActionTransActionDraft = new HashMap<>();
    private Map<String, Map<String, Map<String, Report>>> caseActionTransAction = new HashMap<>();
    private Set<String> methodSet = new HashSet<>();

    @Override
    public void add(Record rec) {
        String label = rec.label;
        if ((!label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            String useCase = label.split("\\] \\[")[1].split("\\]")[0];
            String action = label.split(" \\[")[1].split("\\]")[0];
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
            exceptReplaceMap.put("HTTP Request lifetime-stats [statistics] [Use Case mobile 6]",
                    "GET v3/Users/me/lifetime-stats [statistics] [Use Case mobile 6]"
            );
            exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case mobile 3]",
                    "GET v3/Users/me/workouts/history [statistics] [Use Case mobile 3]"
            );
            exceptReplaceMap.put("HTTP Request workouts/history [statistics] [Use Case mobile 5]",
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
            String method = label.trim().split(" ")[0];

            methodSet.add(method);
            if (("HTTP".equals(method)) || ("".equals(method))) {
                System.err.println(label);
            }
            String transAction = method + " " + rec.url;
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
        report.count = list.size();
        int elapsedSum = 0;
        double elapsedSum2 = 0;
        report.min = list.get(0).elapsed;
        report.max = report.min;
        report.fails = 0;
        Long startTime = list.get(0).timeStamp;
        Long endTime = startTime;
        for (Record record : list) {
            elapsedSum += record.elapsed;
            report.fails += (("FALSE".equals(record.success)) ? 1 : 0);

            //ResponseTime
            if (record.elapsed >= report.max) {
                report.max = record.elapsed;
            }
            if ((record.elapsed <= report.min) && (record.elapsed > 0)) {
                report.min = record.elapsed;
            }

            //Times[start,stop]
            if (record.timeStamp >= endTime) {
                endTime = record.timeStamp;
            }
            if ((record.timeStamp <= startTime) && (record.timeStamp > 0)) {
                startTime = record.timeStamp;
            }
        }

        int n = list.size();
        report.av = elapsedSum / n;
        double dtSeconds = ((endTime - startTime) / 1000d);
        report.tps = n / dtSeconds;
        if (n >= 2) {
            for (Record record : list) {
                double d = record.elapsed - report.av;
                elapsedSum2 += (d * d);
                report.sd = Math.sqrt(elapsedSum2 / (n));
                if ("NaN".equals(report.sd + "")) {
                    System.err.println("elapsedSum2=" + elapsedSum2 + " n=" + n + " Math.sqrt(elapsedSum2 /(n-1))" + Math.sqrt(elapsedSum2 / (n - 1)));
                }
            }
            Collections.sort(list, (o1, o2) -> new Integer(o1.elapsed).compareTo(o2.elapsed));
            report.p90 = list.get((n * 90) / 100).elapsed;
        }

        report.pass = list.size() - report.fails;
        report.url = list.get(0).url;
        return report;
    }

    @Override
    public void saveToFile(String fn) throws IOException {
        calculate();
        // System.out.println(caseActionTransAction);
        // System.out.println("methodSet:" + methodSet);
        FileWriter writer = new FileWriter(fn);
        writer.append("UseCase,Action,URL,AV,Pass,Fails,Min,Max,SD,90%,TPS,count\n");
        for (Map.Entry<String, Map<String, Map<String, Report>>> caseEntry : caseActionTransAction.entrySet()) {
            for (Map.Entry<String, Map<String, Report>> actionEntry : caseEntry.getValue().entrySet()) {
                for (Map.Entry<String, Report> transActionEntry : actionEntry.getValue().entrySet()) {
                    writer.append(caseEntry.getKey()).append(",")
                          .append(actionEntry.getKey()).append(",")
                          .append(transActionEntry.getKey()).append(",");
                            Report report=transActionEntry.getValue();
                          writer.append(report.av+",")
                                .append(report.pass+",")
                                .append(report.fails+",")
                                .append(report.min+",")
                                .append(report.max+",")
                                .append(report.sd+",")
                                .append(report.p90+",")
                                .append(report.tps+",")
                                .append(report.count+"\n");
                }
            }
        }
        writer.flush();
        writer.close();
        System.out.println("SAVE to " + fn);
    }
}
