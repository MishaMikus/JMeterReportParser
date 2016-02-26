package epam;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Reader {

    Map<String, List<Record>> urlMap = new HashMap<>();
    Map<String, List<Record>> tcMap = new HashMap<>();
    Map<String, Report> reportMap = new HashMap<>();
    Map<Long, Long> hitPerSecond;
    Map<Long, UserErrorEntry> userPerSecond;
    Map<Long, Map<String, Long>> bytesPerSecondPerActions;
    Set<String> actionSet;

    public void read(String fn) throws IOException {
        System.out.println("START");
        String regexForCSV = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
        String line = br.readLine();
        if (line != null) {
            String[] headers = line.split(regexForCSV);//cur first line
            hitPerSecond = new TreeMap<>();
            userPerSecond = new TreeMap<>();
            bytesPerSecondPerActions = new TreeMap<>();
            actionSet = new HashSet();
            while ((line = br.readLine()) != null) {
                String[] lineSep = line.split(regexForCSV);
                Record rec = new Record(headers, lineSep);

                //URL
                if (!rec.label.startsWith("TC")) {

                    if (!("null".equals(rec.url))) {
                        //HPS
                        addToHPSMap(hitPerSecond, rec);
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

    private void addToBytesPerSecondPerActionsMap(Map<Long, Map<String, Long>> bytesPerSecondPerActions, Record rec) {
        Long deltaTime = 1000L * 60L;
        Long currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
        if (currentSecond > 0) {
            Map<String, Long> tmpMap = bytesPerSecondPerActions.get(currentSecond);
            String label = rec.label;
            if (tmpMap == null) {
                tmpMap = new HashMap<>();
            }
            bytesPerSecondPerActions.put(currentSecond, tmpMap);
            Long tmpBytes = tmpMap.get(label);
            if (tmpBytes == null) {
                tmpBytes = rec.bytes;
            } else {
                tmpBytes += rec.bytes;
            }
            tmpMap.put(label, tmpBytes);
            actionSet.add(label);
        }
    }

    private void addToUserMap(Map<Long, UserErrorEntry> userPerSecond, Record rec) {
        Long deltaTime = 1000L * 10L;
        Long currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
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

    private void addToHPSMap(Map<Long, Long> hitPerSecond, Record rec) {
        Long deltaTime = 1000L;
        Long currentSecond = (rec.timeStamp / deltaTime) * deltaTime;
        if (currentSecond > 0) {
            if (hitPerSecond.get(currentSecond) != null) {
                hitPerSecond.put(currentSecond, hitPerSecond.get(currentSecond) + 1);
            } else {
                hitPerSecond.put(currentSecond, 1L);
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

                Collections.sort(list, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return new Integer(o1.elapsed).compareTo(o2.elapsed);
                    }
                });

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

    private void saveHPSToFile(String fn) throws IOException {
        FileWriter writer = new FileWriter(fn);
        writer.append("Time,Count\n");
        try {
            for (Map.Entry<Long, Long> entry : hitPerSecond.entrySet()) {
                writer.append(simpleDateFormat.format(new Date(entry.getKey() - 60 * 60 * 1000))).append(",");
                writer.append(entry.getValue().toString()).append("\n");
            }
        } catch (Exception ignored) {
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
                for (Map.Entry<Long, UserErrorEntry> entry : userPerSecond.entrySet()) {
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
            for (Map.Entry<Long, UserErrorEntry> entry : userPerSecond.entrySet()) {
                writer.append(simpleDateFormat.format(new Date(entry.getKey() - 60 * 60 * 1000))).append(",");
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
        for (Map.Entry<Long, Map<String, Long>> entry : bytesPerSecondPerActions.entrySet()) {
            if (headerList == null) {
                headerList = new ArrayList<>(actionSet);
                String headers = "";
                for (String header : headerList) {
                    headers += "," + header;
                }
                writer.append("Time" + headers + ",Overall\n");
            }
            writer.append(simpleDateFormat.format(new Date(entry.getKey() - 60 * 60 * 1000)));
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
        writer.flush();
        writer.close();
        System.out.println("SAVE to " + fn);
    }

    public static void main(String[] arg) throws IOException {
        String rootDir = "D:\\LocalWorkspace\\jMeter\\Data\\26.02.2016\\";
        List<String> zipFileNameList = new ArrayList<>();
        for (String type : Arrays.asList("BASELINE", "LOAD", "STRESS")) {
            List<String> fileNameList = new ArrayList<>();
            String localDir = rootDir + type;
            Reader reader = new Reader();
            reader.read(localDir + "\\summary.csv");

            //URL
            reader.calculate(reader.urlMap);
            String fn = localDir + "\\summary_URL_" + type + ".csv";
            reader.saveToFile(fn);
            fileNameList.add(fn);

            //TC
            reader.reportMap = new HashMap<>();
            reader.calculate(reader.tcMap);
            fn = localDir + "\\summary_TC_" + type + ".csv";
            reader.saveToFile(fn);
            fileNameList.add(fn);

            //HPS
            fn = localDir + "\\summary_HPS_" + type + ".csv";
            reader.saveHPSToFile(fn);
            fileNameList.add(fn);

            //Users
            fn = localDir + "\\summary_USER_" + type + ".csv";
            reader.saveUserPerSecToFile(fn);
            fileNameList.add(fn);

            //BYTES
            fn = localDir + "\\summary_BYTES_" + type + ".csv";
            reader.saveBytesPerSecToFile(fn);
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
