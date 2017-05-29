package ie.seankehoe.randombeer;

/**
 * Created by Sean Kehoe on 24/05/2017.
 */

public class BeerDB {


    private String name = "blank";
    private String description = "blank";
    private String labelUrl;


    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.description = desc;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }

    public String getLabelUrl(){return labelUrl;}
    public String getName() {return name;}
    public String getDesc() {return description;}

}
