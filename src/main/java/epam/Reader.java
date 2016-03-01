package epam;

import epam.model.Record;
import epam.report.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reader {

    private List<ReportContainer> reportContainerList;
    String regexForCSV = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    public Reader(List<ReportContainer> reportContainerList) {
        this.reportContainerList = reportContainerList;
    }

    public void read(String fn) throws IOException {
        System.out.println();
        BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
        String line = br.readLine();
        if (line != null) {
            String[] headers = line.split(regexForCSV);
            while ((line = br.readLine()) != null) {
                String[] lineSep = line.split(regexForCSV);
                Record rec = new Record(headers, lineSep);
                for (ReportContainer container : reportContainerList) {
                    container.add(rec);
                }
            }
        }
    }


    public static void main(String[] arg) throws IOException, InterruptedException {
        String rootDir = "D:\\LocalWorkspace\\jMeter\\Data\\26.02.2016\\";
        List<String> zipFileNameList = new ArrayList<>();
        for (String type : Arrays.asList("BASELINE", "LOAD", "STRESS")) {

            ReportContainer caseActionTransaction = new CaseActionTransaction(type);
            ReportContainer hitPerSecondContainer = new HitPerSecond(type);
            ReportContainer caseAction = new CaseAction(type);
            ReportContainer bytesPerSecondsAction = new BytesPerSecondsActions(type);
            ReportContainer userPerSecondVsErrorByAction = new UserPerSecondVsErrorByAction(type);

            List<String> fileNameList = new ArrayList<>();
            String localDir = rootDir + type;
            Reader reader = new Reader(Arrays.asList(caseActionTransaction, hitPerSecondContainer, caseAction, bytesPerSecondsAction, userPerSecondVsErrorByAction));
            reader.read(localDir + "\\summary.csv");
            String fn = localDir + "\\summary_BYTES_PER_SECONDS_ACTIONS_" + type + ".csv";
            bytesPerSecondsAction.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_CASE_ACTION_TRANSACTION_" + type + ".csv";
            caseActionTransaction.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_CASE_ACTION_" + type + ".csv";
            caseAction.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_HIT_PER_SECOND_" + type + ".csv";
            hitPerSecondContainer.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_USER_PER_SECOND_VS_ERROR_BY_ACTION_" + type + ".csv";
            userPerSecondVsErrorByAction.saveToFile(fn);
            fileNameList.add(fn);

            //ZIPPING
            String zipName = localDir + "\\summary_" + type + ".zip";
            new AppZip(fileNameList, zipName).zipIt();
            zipFileNameList.add(zipName);
        }
        String zipName = rootDir + "\\summaryParsedReport.zip";
        new AppZip(zipFileNameList, zipName).zipIt();
    }

}
