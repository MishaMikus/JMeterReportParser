package epam.control;

import epam.model.Record;
import epam.report.ReportContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    private static final String regexForCSV = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    public static List<String> zipFileNameList = new ArrayList<>();

    public List<String> fileNameList = new ArrayList<>();
    private List<ReportContainer> reportContainerList;
    private String type;
    private String rootDir;

    public Reader(String rootDir, String type, List<ReportContainer> reportContainerList) {
        this.reportContainerList = reportContainerList;
        this.type = type;
        this.rootDir = rootDir;
    }

    public void read(String fn) throws IOException {
        initContainers();
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

    private void initContainers() {
        for (ReportContainer container : reportContainerList) {
            container.fileName = genFileName(container.name, type);
            container.type = type;
            fileNameList.add(container.fileName);
        }
    }

    private String genFileName(String name, String type) {
        return rootDir + type + "\\summary_" + name + "_" + type + ".csv";
    }

    public void saveAll() throws IOException, InterruptedException {
        for (ReportContainer container : reportContainerList) {
            container.saveToFile();
        }
    }

    public void zipAll() throws IOException {
        String zipName = rootDir + type + "\\summary_" + type + ".zip";
        new AppZip(fileNameList, zipName).zipIt();
        zipFileNameList.add(zipName);
    }
}
