package epam.model;

/**
 * Created by Mykhaylo_Mikus on 3/2/2016.
 */
public class UrlEntry {
    public String url;
    public String action;
    public Double elapsed;

    public UrlEntry(String action, Double elapsed, String url) {
        this.action = action;
        this.elapsed = elapsed;
        this.url = url;
    }
}
