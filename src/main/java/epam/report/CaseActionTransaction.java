package epam.report;

import epam.model.Record;
import epam.model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseActionTransaction extends ReportContainer {

    private Map<String, Map<String, Map<String, List<Record>>>> caseActionTransActionDraft = new HashMap<>();
    private Map<String, Map<String, Map<String, Report>>> caseActionTransAction = new HashMap<>();


    public CaseActionTransaction() {
        super();

    }

    @Override
    public void add(Record rec) {
        String label = rec.label;
        if ((!label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            label = correctLabel(label);
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
                    addReport(caseEntry.getKey(), actionEntry.getKey(), transActionEntry.getKey(), new Report(transActionEntry.getValue(), startTime, endTime));
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
