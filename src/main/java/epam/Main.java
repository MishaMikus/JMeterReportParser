package epam;

import epam.control.AppZip;
import epam.control.Reader;
import epam.report.*;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] arg) throws IOException, InterruptedException {
        String rootDir = "D:\\LocalWorkspace\\jMeter\\Data\\26.02.2016\\";
        for (String type : Arrays.asList("BASELINE", "LOAD", "STRESS")) {
            HitPerSecond hitPerSecond = new HitPerSecond("HIT_PER_SECOND");
            Reader reader = new Reader(rootDir, type, Arrays.asList(
                    new CaseActionTransaction("CASE_ACTION_TRANSACTION"),
                    hitPerSecond,
                    new CaseAction("CASE_ACTION"),
                    new BytesPerSecondsActions("BYTES_PER_SECONDS_ACTIONS"),
                    new UserPerSecondVsErrorByAction("USER_PER_SECOND_VS_ERROR_BY_ACTION"),
                    new TimeStampElapsedByURL("TIME_STAMP_ELAPSED_BY_URL")));
            reader.read(rootDir + type + "\\summary.csv");
            reader.saveAll();
            reader.zipAll();
            ChartMaker.make(hitPerSecond.hitPerSecond);
        }

        new AppZip(Reader.zipFileNameList, rootDir + "\\summaryParsedReport.zip").zipIt();


    }
}
