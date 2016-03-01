package epam;

import epam.model.Record;
import epam.model.Report;
import epam.report.CaseAction;
import epam.report.CaseActionTransaction;
import epam.report.HitPerSecond;
import epam.report.ReportContainer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Reader {

    Map<String, List<Record>> urlMap = new HashMap<>();
    Map<String, List<Record>> tcMap = new HashMap<>();
    Map<String, Report> reportMap = new HashMap<>();

    Map<Double, UserErrorEntry> userPerSecond;
    Map<Double, Map<String, Double>> bytesPerSecondPerActions;
    HashSet<String> actionSet;

    private List<ReportContainer> reportContainerList;

    public Reader(List<ReportContainer> reportContainerList) {
        this.reportContainerList = reportContainerList;
    }

    public void read(String fn) throws IOException {
        System.out.println();
        String regexForCSV = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
        String line = br.readLine();
        if (line != null) {
            String[] headers = line.split(regexForCSV);//cur first line
            userPerSecond = new TreeMap<>();
            bytesPerSecondPerActions = new TreeMap<>();
            actionSet = new HashSet();
            while ((line = br.readLine()) != null) {
                String[] lineSep = line.split(regexForCSV);
                Record rec = new Record(headers, lineSep);

                //New Format
                for (ReportContainer container : reportContainerList) {
                    container.add(rec);
                }

                //URL
                if (!rec.label.startsWith("TC")) {

                    if (!("null".equals(rec.url))) {
                        //URL
                        addToMap(urlMap, rec);
                    }
                } else {
                    //BYTES
                    addToBytesPerSecondPerActionsMap(bytesPerSecondPerActions, rec);
                    //TC
                    Record recTC = new Record(headers, lineSep);
                    recTC.url = recTC.label;
                    addToMap(tcMap, recTC);
                }
                //USER
                addToUserMap(userPerSecond, rec);


            }
        }
    }

    private void addToBytesPerSecondPerActionsMap(Map<Double, Map<String, Double>> bytesPerSecondPerActions, Record rec) {
        Long deltaTime = 1000L * 60L;
        Double currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
        if (currentSecond > 0) {
            Map<String, Double> tmpMap = bytesPerSecondPerActions.get(currentSecond);
            String label = rec.label;
            if (tmpMap == null) {
                tmpMap = new HashMap<>();
            }
            bytesPerSecondPerActions.put(currentSecond, tmpMap);
            Double tmpBytes = tmpMap.get(label);
            if (tmpBytes == null) {
                tmpBytes = rec.bytes;
            } else {
                tmpBytes += rec.bytes;
            }
            tmpMap.put(label, tmpBytes);
            actionSet.add(label);
        }
    }

    private void addToUserMap(Map<Double, UserErrorEntry> userPerSecond, Record rec) {
        Long deltaTime = 1000L * 10L;
        Double currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
        if (currentSecond > 0) {
            UserErrorEntry tmpUserErrorEntry = userPerSecond.get(currentSecond);
            if (tmpUserErrorEntry == null) {
                tmpUserErrorEntry = new UserErrorEntry();
            }
            tmpUserErrorEntry.threadNameSet.add(rec.threadName);
            addErrorCountByTCMap(tmpUserErrorEntry, rec);
            userPerSecond.put(currentSecond, tmpUserErrorEntry);
        }
    }

    private void addErrorCountByTCMap(UserErrorEntry userErrorEntry, Record rec) {
        if (rec.label.startsWith("TC")) {
            Long errorCount = userErrorEntry.errorCountByTCMap.get(rec.label);
            if (errorCount == null) {
                errorCount = 0L;
            }
            if ("FALSE".equals(rec.success))
                errorCount++;
            userErrorEntry.errorCountByTCMap.put(rec.label, errorCount);
        }
    }

    private void addToHPSMap(Map<Double, Double> hitPerSecond, Record rec) {
        Long deltaTime = 1000L;
        Double currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
        if (currentSecond > 0) {
            if (hitPerSecond.get(currentSecond) != null) {
                hitPerSecond.put(currentSecond, hitPerSecond.get(currentSecond) + 1);
            } else {
                hitPerSecond.put(currentSecond, 1d);
            }
        }
    }

    private void addToMap(Map<String, List<Record>> map, Record rec) {
        List<Record> list = map.get(rec.url);
        if (list == null) {
            list = new ArrayList<>();
            map.put(rec.url, list);
        }
        list.add(rec);
    }

    private void calculate(Map<String, List<Record>> map) {
        for (List<Record> list : map.values()) {
            Report report = new Report();
            Double elapsedSum = 0d;
            Double elapsedSum2 = 0d;
            report.min = list.get(0).elapsed;
            report.max = report.min;
            report.fails = 0d;
            Double startTime = list.get(0).timeStamp;
            Double endTime = startTime;
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
                Collections.sort(list, (o1, o2) -> new Double(o1.elapsed).compareTo(o2.elapsed));
                report.p90 = list.get((n * 90) / 100).elapsed;
            }

            report.pass = list.size() - report.fails;
            report.url = list.get(0).url;
            reportMap.put(report.url, report);
        }

    }

    private void saveToFile(String fn) throws IOException {
        FileWriter writer = new FileWriter(fn);
        writer.append("URL,Pass,Fails,Error,Min,Av,Max,SD,90%,TPS\n");
        for (Report report : reportMap.values()) {
            writer.append("\"" + report.url + "\"").append(",");
            writer.append(report.pass + ",");
            writer.append(report.fails + ",");
            writer.append(",");
            writer.append(report.min + ",");
            writer.append(report.av + ",");
            writer.append(report.max + ",");
            writer.append(report.sd + ",");
            writer.append(report.p90 + ",");
            writer.append(report.tps + "\n");
        }
        writer.flush();
        writer.close();
        System.out.println("SAVE to " + fn);
    }

    private void saveUserPerSecToFile(String fn) throws IOException {
        FileWriter writer = new FileWriter(fn);
        String errorByTCHeader = "";
        Set<String> actionSetFilter = new HashSet<>();
        try {
            for (String action : actionSet) {
                Long sum = 0L;
                for (Map.Entry<Double, UserErrorEntry> entry : userPerSecond.entrySet()) {
                    Long value = entry.getValue().errorCountByTCMap.get(action);
                    sum += ((value == null) ? 0L : value);
                }
                if (sum > 0L) {
                    actionSetFilter.add(action);
                }
            }

            for (String action : actionSetFilter) {
                errorByTCHeader += ",ERR-" + action;
            }

            writer.append("Time,UserCount" + errorByTCHeader + "\n");
            for (Map.Entry<Double, UserErrorEntry> entry : userPerSecond.entrySet()) {
                writer.append(simpleDateFormat.format(new Date((long) (entry.getKey() - 60 * 60 * 1000)))).append(",");
                UserErrorEntry userErrorEntry = entry.getValue();

                String errors = "";
                for (String action : actionSetFilter) {
                    Long errorCount = userErrorEntry.errorCountByTCMap.get(action);
                    if (errorCount == null) errorCount = 0L;
                    errors += "," + errorCount;
                }
                writer.append(Integer.toString(userErrorEntry.threadNameSet.size())).append(errors).append("\n");
            }
        } catch (Exception ignored) {
        }
        writer.flush();
        writer.close();
        System.out.println("SAVE to " + fn);
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private void saveBytesPerSecToFile(String fn) throws IOException {
        FileWriter writer = new FileWriter(fn);
        List<String> headerList = null;
        try {
            for (Map.Entry<Double, Map<String, Double>> entry : bytesPerSecondPerActions.entrySet()) {
                if (headerList == null) {
                    headerList = new ArrayList<>(actionSet);
                    String headers = "";
                    for (String header : headerList) {
                        headers += "," + header;
                    }
                    writer.append("Time" + headers + ",Overall\n");
                }
                writer.append(simpleDateFormat.format(new Date((long) (entry.getKey() - 60 * 60 * 1000))));
                String line = "";
                Long overall = 0L;
                for (String header : headerList) {
                    Object value = entry.getValue().get(header);
                    if (value == null) {
                        value = "0";
                    }
                    line += "," + value;
                    overall += Long.parseLong(value + "");
                }

                writer.append(line).append(",").append(overall.toString()).append("\n");
            }
        } catch (Exception ignored) {
        }
        writer.flush();
        writer.close();
        System.out.println("SAVE to " + fn);
    }

    public static void main(String[] arg) throws IOException, InterruptedException {
        String rootDir = "D:\\LocalWorkspace\\jMeter\\Data\\26.02.2016\\";
        List<String> zipFileNameList = new ArrayList<>();
        for (String type : Arrays.asList("BASELINE", "LOAD", "STRESS")) {

            ReportContainer caseActionTransaction = new CaseActionTransaction(type);
            ReportContainer hitPerSecondContainer = new HitPerSecond(type);
            ReportContainer caseAction = new CaseAction(type);

            List<String> fileNameList = new ArrayList<>();
            String localDir = rootDir + type;
            Reader reader = new Reader(Arrays.asList(caseActionTransaction, hitPerSecondContainer,caseAction));
            reader.read(localDir + "\\summary.csv");
            String fn;
//            //URL
//            reader.calculate(reader.urlMap);
//             fn = localDir + "\\summary_URL_" + type + ".csv";
//            reader.saveToFile(fn);
//            fileNameList.add(fn);
//
//            //TC
//            reader.reportMap = new HashMap<>();
//            reader.calculate(reader.tcMap);
//            fn = localDir + "\\summary_TC_" + type + ".csv";
//            reader.saveToFile(fn);
//            fileNameList.add(fn);
//
//            //Users
//            fn = localDir + "\\summary_USER_" + type + ".csv";
//            reader.saveUserPerSecToFile(fn);
//            fileNameList.add(fn);
//
//            //BYTES
//            fn = localDir + "\\summary_BYTES_" + type + ".csv";
//            reader.saveBytesPerSecToFile(fn);
//            fileNameList.add(fn);

            fn = localDir + "\\summary_CASE_ACTION_TRANSACTION_" + type + ".csv";
            caseActionTransaction.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_CASE_ACTION_" + type + ".csv";
            caseAction.saveToFile(fn);
            fileNameList.add(fn);

            fn = localDir + "\\summary_HIT_PER_SECOND_" + type + ".csv";
            hitPerSecondContainer.saveToFile(fn);
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
