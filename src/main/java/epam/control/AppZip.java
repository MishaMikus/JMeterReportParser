package epam.control;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppZip {

    private List<String> fileList;
    private String zipFileName;

    public AppZip(List<String> fileList, String zipFileName) {
        this.fileList = fileList;
        this.zipFileName = zipFileName;
    }


    public void zipIt() throws IOException {
        byte[] buffer = new byte[1024];
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
            for (String fileName : this.fileList) {
                File file=new File(fileName);
                zos.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream in = new FileInputStream(fileName);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
                file.delete();
            }
            zos.closeEntry();
            zos.close();
    }
}