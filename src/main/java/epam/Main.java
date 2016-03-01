package epam;


import epam.control.AppZip;
import epam.control.Reader;
import epam.report.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static String rootDir = "D:\\LocalWorkspace\\jMeter\\Data\\26.02.2016\\";

    public static void main(String[] arg) throws IOException, InterruptedException {

        List<String> zipFileNameList = new ArrayList<>();
        for (String type : Arrays.asList("BASELINE", "LOAD", "STRESS")) {
            ReportContainer caseActionTransaction = new CaseActionTransaction(type,genFileName("CASE_ACTION_TRANSACTION", type));
            ReportContainer hitPerSecondContainer = new HitPerSecond(type,genFileName("HIT_PER_SECOND", type));
            ReportContainer caseAction = new CaseAction(type,genFileName("CASE_ACTION", type));
            ReportContainer bytesPerSecondsAction = new BytesPerSecondsActions(type,genFileName("BYTES_PER_SECONDS_ACTIONS", type));
            ReportContainer userPerSecondVsErrorByAction = new UserPerSecondVsErrorByAction(type,genFileName("USER_PER_SECOND_VS_ERROR_BY_ACTION", type));

            List<String> fileNameList = new ArrayList<>();
            Reader reader = new Reader(Arrays.asList(caseActionTransaction, hitPerSecondContainer, caseAction, bytesPerSecondsAction, userPerSecondVsErrorByAction));
            reader.read(rootDir + type + "\\summary.csv");

            bytesPerSecondsAction.saveToFile();
            fileNameList.add(bytesPerSecondsAction.fileName);

            caseActionTransaction.saveToFile();
            fileNameList.add(caseActionTransaction.fileName);

            caseAction.saveToFile();
            fileNameList.add(caseAction.fileName);

            hitPerSecondContainer.saveToFile();
            fileNameList.add(hitPerSecondContainer.fileName);

            userPerSecondVsErrorByAction.saveToFile();
            fileNameList.add(userPerSecondVsErrorByAction.fileName);

            //ZIPPING
            String zipName = rootDir + type + "\\summary_" + type + ".zip";
            new AppZip(fileNameList, zipName).zipIt();
            zipFileNameList.add(zipName);
        }
        new AppZip(zipFileNameList, rootDir + "\\summaryParsedReport.zip").zipIt();
    }

    private static String genFileName(String name, String type) {
        return rootDir + type + "\\summary_" + name + "_" + type + ".csv";
    }
}
