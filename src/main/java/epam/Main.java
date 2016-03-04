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
            HitPerSecond hitPerSecond = new HitPerSecond();
            Reader reader = new Reader(rootDir, type, Arrays.asList(
                    new CaseActionTransaction(),
                    hitPerSecond,
                    new CaseAction(),
                    new BytesPerSecondsActions(),
                    new UserPerSecondVsErrorByAction(),
                    new TimeStampElapsedByURL(),
                    new StatisticByURL()));
            reader.read(rootDir + type + "\\summary.csv");
            reader.saveAll();
            reader.zipAll();

            //ChartMaker.make(hitPerSecond.hitPerSecond);
        }

        new AppZip(Reader.zipFileNameList, rootDir + "\\summaryParsedReport.zip").zipIt();


    }
}
