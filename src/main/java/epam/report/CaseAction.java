package epam.report;

import epam.model.Record;
import epam.model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseAction extends ReportContainer {

    private Map<String, Map<String, List<Record>>> caseActionTransActionDraft = new HashMap<>();
    private Map<String, Map<String, Report>> caseActionTransAction = new HashMap<>();

    public CaseAction() {
        super();
    }

    @Override
    public void add(Record rec) {
        String label = rec.label;
        if ((!label.startsWith("TC")) && (!("null".equals(rec.url)))) {
            label = correctLabel(label);
            addTransAction(getUseCase(label), getAction(label), rec);
            calculateStartEndTime(rec);
        }
    }

    private void addTransAction(String useCase, String action, Record rec) {
        if (caseActionTransActionDraft.get(useCase) == null) {
            caseActionTransActionDraft.put(useCase, new HashMap<>());
        }
        if (caseActionTransActionDraft.get(useCase).get(action) == null) {
            caseActionTransActionDraft.get(useCase).put(action, new ArrayList<>());
        }
        caseActionTransActionDraft.get(useCase).get(action).add(rec);
    }


    private void calculate() {
        for (Map.Entry<String, Map<String, List<Record>>> caseEntry : caseActionTransActionDraft.entrySet()) {
            for (Map.Entry<String, List<Record>> actionEntry : caseEntry.getValue().entrySet()) {
                if (caseActionTransAction.get(caseEntry.getKey()) == null) {
                    caseActionTransAction.put(caseEntry.getKey(), new HashMap<>());
                }
                caseActionTransAction.get(caseEntry.getKey()).put(actionEntry.getKey(), new Report(actionEntry.getValue(), startTime, endTime));
            }
        }
    }

    @Override
    public void saveToFile() throws IOException, InterruptedException {
        calculate();
        FileWriter writer = new FileWriter(fileName);
        writer.append("UseCase,Action,AV,Pass,Fails,Min,Max,SD,90%,TPS,count\n");
        for (Map.Entry<String, Map<String, Report>> caseEntry : caseActionTransAction.entrySet()) {
            for (Map.Entry<String, Report> actionEntry : caseEntry.getValue().entrySet()) {
                writer.append(caseEntry.getKey()).append(",")
                        .append(actionEntry.getKey()).append(",");
                Report report = actionEntry.getValue();
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
        writer.flush();
        writer.close();
        logEnd();
    }
}
