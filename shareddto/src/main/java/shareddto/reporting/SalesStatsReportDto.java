package shareddto.reporting;

import java.io.Serializable;
import java.util.Map;

public class SalesStatsReportDto implements Serializable {
    private String reportDate;
    private String aggregationType;
    private Map<String, Integer> salesData; 

    public SalesStatsReportDto(String reportDate, String aggregationType, Map<String, Integer> salesData) {
        this.reportDate = reportDate;
        this.aggregationType = aggregationType;
        this.salesData = salesData;
    }

    public String getReportDate() { return reportDate; }
    public String getAggregationType() { return aggregationType; }
    public Map<String, Integer> getSalesData() { return salesData; }
}