package epam.control;

import epam.model.Record;
import epam.report.ReportContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
}
