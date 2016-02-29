package epam.report;

import epam.model.Record;

import java.io.IOException;
import java.text.SimpleDateFormat;

public interface ReportContainer {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    void add(Record rec);
    void saveToFile(String fn) throws IOException;
}
