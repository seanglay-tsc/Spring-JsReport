package com.seanglay.js_report;

import net.jsreport.java.JsReportException;
import net.jsreport.java.dto.*;
import net.jsreport.java.service.JsReportService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    private final JsReportService jsReportService;

    public TestController(JsReportService jsReportService) {
        this.jsReportService = jsReportService;
    }

    @GetMapping("/receipt")
    public ResponseEntity<byte[]> hello() throws IOException, JsReportException {
        ClassPathResource tpl = new ClassPathResource("templates/receipt.html");
        String html = new String(tpl.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Template template = new Template();
        template.setContent(html);
        template.setEngine(Engine.HANDLEBARS);
        template.setRecipe(Recipe.CHROME_PDF);

        Map<String, Object> data = new HashMap<>();
        data.put("requestCode", "000002");
        data.put("txnId", "TXN250305024625498");
        data.put("txnDate", "05-03-2025 09:46");
        data.put("payerName", "សេងលី សេងលីអូរ៉ា");
        data.put("payerPhone", "+85581612629");

        List<Map<String, Object>> items = new ArrayList<>();

        String[] descriptions = {"លិខិតអនុញ្ញាតបង្កើតកសិដ្ឋានចិញ្ចឹមសត្វ និង/ឬ បង្កាត់ពូជសត្វ (ខ្នាតមធ្យម និង ខ្នាតធំ)", "សេវាកម្មពាក់ព័ន្ធផ្សេងៗ"};
        int[] quantities = {1, 2};
        String[] fees = {"800,000", "500,000"};

        for (int i = 0; i < descriptions.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("no", toKhmerNumber(i + 1));
            item.put("description", descriptions[i]);
            item.put("quantity", quantities[i]);
            item.put("fee", fees[i]);
            items.add(item);
        }

        data.put("items", items);
        data.put("total", "1,300,000");

        RenderRequest renderRequest = new RenderRequest();
        renderRequest.setTemplate(template);
        renderRequest.setData(data);

        Report report = jsReportService.render(renderRequest);
        byte[] pdf = report.getContent().readAllBytes();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receipt.pdf").body(pdf);
    }

    @GetMapping("/invoice")
    public ResponseEntity<byte[]> invoice() throws IOException, JsReportException {
        ClassPathResource tpl = new ClassPathResource("templates/invoice.html");
        String html = new String(tpl.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Template template = new Template();
        template.setContent(html);
        template.setEngine(Engine.HANDLEBARS);
        template.setRecipe(Recipe.CHROME_PDF);

        template.setHelpers(
                "function totalAmount(q, p) {" +
                        "  function n(x){ if(typeof x==='number') return x; return parseFloat(String(x).replace(/[,\\s]/g,''))||0; }" +
                        "  const val = n(q) * n(p);" +
                        "  return val.toLocaleString('en-US');" +
                        "}"
        );

        Map<String, Object> data = new HashMap<>();
        data.put("billNumber", "OBR00118318");
        data.put("issuedDateHtml", "10-09-2025");
        data.put("payerName",  "ឈឹម សុគន្ធ");
        data.put("payerPhone", "+855 92 923 833");

        data.put("payerTypeHtml",
                "<tr>" +
                        "  <td class='info-label'>តួនាទីអ្នកបំពេញ/Relevant to the applicant’s position:</td>" +
                        "  <td colspan='3'><div class='underline-dashed'>តំណាងរោងចក្រ (Representative)</div></td>" +
                        "</tr>"
        );
        data.put("representativeCompanyHtml",
                "<tr>" +
                        "  <td class='info-label'>ស្នើសុំតំណាងឲ្យ/Request as a representative for:</td>" +
                        "  <td colspan='3'><div class='underline-dashed'>TEST CO., LTD</div></td>" +
                        "</tr>"
        );

        Map<String, List<Map<String, Object>>> groups = new java.util.LinkedHashMap<>();

        groups.put("ក្រសួងការងារ និងវិជ្ជាជីវៈ / MINISTRY OF LABOR AND VOCATIONAL TRAINING",
                List.of(
                        item("សេចក្ដីជូនដំណឹងនៃសហគ្រាស / Notification of Enterprise", 1, 120_000)
                )
        );

        groups.put("អគ្គនាយកដ្ឋានពន្ធដារ / GENERAL DEPARTMENT OF TAXATION",
                List.of(
                        item("ពន្ធសញ្ញាប័ត្រសម្រាប់អាជីវកម្មទាំងអស់ / Patent Tax of All Business Activities", 1, 200_000),
                        item("ចុះបញ្ជីពន្ធ / Tax Registration", 1, 20_000)
                )
        );

        groups.put("ក្រសួងពាណិជ្ជកម្ម / MINISTRY OF COMMERCE",
                List.of(
                        item("ចុះបញ្ជីម្ចាស់ពាណិជ្ជកម្មតែម្នាក់ / Sole Proprietorship Registration", 1, 180_000),
                        item("រក្សាទុកឈ្មោះ / Name Reservation", 1, 25_000)
                )
        );

        data.put("payment_amounts", groups);
        data.put("subTotalAmount", money(
                120_000L + 200_000L + 20_000L + 180_000L + 25_000L
        )); // -> "545,000"

        // Render
        RenderRequest renderRequest = new RenderRequest();
        renderRequest.setTemplate(template);
        renderRequest.setData(data);

        Report report = jsReportService.render(renderRequest);
        byte[] pdf = report.getContent().readAllBytes();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice.pdf")
                .body(pdf);
    }

    private static final String[] KHMER_DIGITS = {"០", "១", "២", "៣", "៤", "៥", "៦", "៧", "៨", "៩"};

    private String toKhmerNumber(int number) {
        String numStr = String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (char c : numStr.toCharArray()) {
            sb.append(KHMER_DIGITS[c - '0']);
        }
        return sb.toString();
    }

    private static Map<String, Object> item(String name, int quantity, long priceRiels) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("quantity", quantity);
        m.put("price", money(priceRiels));
        return m;
    }

    private static String money(long riels) {
        return java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(riels);
    }

}
