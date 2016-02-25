package epam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Record implements Cloneable {
    public long timeStamp;
    public int elapsed;
    public String label;
    public String responseCode;
    public String responseMessage;
    public String threadName;
    public String dataType;
    public String success;
    public Long bytes;
    public String grpThread;
    public String allThread;
    public String url;
    public String latency;
    public String errorCount;
    public String idleTime;

    public Record(String[] headers, String[] lineSep) {
        List<String> headerList = new ArrayList<>(Arrays.asList(headers));
        try{
        this.timeStamp = Long.parseLong(lineSep[headerList.indexOf( "timeStamp")]);
        this.elapsed = Integer.parseInt(lineSep[headerList.indexOf("elapsed")]);
        this.bytes = Long.parseLong(lineSep[headerList.indexOf( "bytes")]);
        }catch (Exception ignored){
        }
        this.label = lineSep[headerList.indexOf( "label")];
        this.responseCode = lineSep[headerList.indexOf( "responseCode")];
        this.responseMessage = lineSep[headerList.indexOf( "responseMessage")];
        this.threadName = lineSep[headerList.indexOf( "threadName")];
        this.dataType = lineSep[headerList.indexOf( "dataType")];
        this.success = lineSep[headerList.indexOf( "success")];

        this.grpThread = lineSep[headerList.indexOf( "grpThread")];
        this.allThread = lineSep[headerList.indexOf( "allThread")];
        this.url = lineSep[headerList.indexOf("URL")].split("\\?")[0].replaceAll("/[\\d]+", "/SOME_ID").replaceAll("[lms][\\d]+[lms]up[\\d]+","SOME_USER_NAME");
        this.latency = lineSep[headerList.indexOf( "Latency")];
        this.errorCount = lineSep[headerList.indexOf( "ErrorCount")];
        this.idleTime = lineSep[headerList.indexOf( "idleTime")];


    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Record{");
        sb.append("allThreads='").append(allThread).append('\'');
        sb.append(", timeStamp='").append(timeStamp).append('\'');
        sb.append(", elapsed='").append(elapsed).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", responseCode='").append(responseCode).append('\'');
        sb.append(", responseMessage='").append(responseMessage).append('\'');
        sb.append(", threadName='").append(threadName).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", success='").append(success).append('\'');
        sb.append(", bytes='").append(bytes).append('\'');
        sb.append(", grpThreads='").append(grpThread).append('\'');
        sb.append(", URL='").append(url).append('\'');
        sb.append(", Latency='").append(latency).append('\'');
        sb.append(", ErrorCount='").append(errorCount).append('\'');
        sb.append(", IdleTime='").append(idleTime).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
