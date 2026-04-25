package br.com.gestao.dto.dashboard;

import java.util.List;

public class DashboardDTO {

    public record Kpi(String label, String value, String meta, String icon, String accent) {}

    public record Alert(String icon, String title, String text, String pill) {}

    public record Shortcut(String icon, String title, String meta, String pill, String href) {}

    public record ChartPoint(String label, double value) {}

    public record Legend(String value, String label) {}

    public record RankedItem(String name, String meta, String value) {}

    public record Metric(String value, String label) {}

    public record StatusCell(String status, String tone) {}

    public record TableData(List<String> columns, List<List<Object>> rows) {}

    public record Activity(String icon, String title, String meta) {}

    public record AdminSection(
        String title,
        String subtitle,
        String cardClass,
        List<Metric> summary,
        TableData table
    ) {}

    public record DashboardPayload(
        List<Kpi> overview,
        List<Alert> alerts,
        List<Shortcut> shortcuts,
        List<ChartPoint> salesChart,
        List<Legend> salesLegend,
        List<RankedItem> topProducts,
        List<RankedItem> topCategories,
        List<Metric> financialKpis,
        TableData financialTable,
        List<Metric> stockSummary,
        TableData stockTable,
        List<Activity> activity,
        TableData salesTable,
        AdminSection admin
    ) {}
}
