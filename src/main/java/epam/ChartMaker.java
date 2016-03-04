package epam;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ChartMaker {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void make(Map<Double, Double> map) throws IOException {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (Map.Entry<Double, Double> entry : map.entrySet()) {
            String time = simpleDateFormat.format(new Date(entry.getKey().longValue()));
            Long value = entry.getValue().longValue();
            dataSet.addValue(value, "SOME URL", time);
        }
        JFreeChart jFreeChart = ChartFactory.createBarChart("Subject Vs Marks", "Subject", "Marks", dataSet, PlotOrientation.VERTICAL, true, true, false);
        int width = 1024;
        int height = 768;
        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(chartOut, jFreeChart, width, height);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        int pictureId = workbook.addPicture(chartOut.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        chartOut.close();
        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = new HSSFClientAnchor();
        anchor.setCol1(1);
        anchor.setRow1(1);
        HSSFPicture my_picture = drawing.createPicture(anchor, pictureId);
        my_picture.resize();
        FileOutputStream out = new FileOutputStream(new File("barChart.xls"));
        workbook.write(out);
        out.close();
    }
}
