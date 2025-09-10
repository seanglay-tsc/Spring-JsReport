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

    @GetMapping("/test")
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
            item.put("no", toKhmerNumber(i + 1));   // start from 1
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

    private static final String[] KHMER_DIGITS = {"០", "១", "២", "៣", "៤", "៥", "៦", "៧", "៨", "៩"};

    private String toKhmerNumber(int number) {
        String numStr = String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (char c : numStr.toCharArray()) {
            sb.append(KHMER_DIGITS[c - '0']);
        }
        return sb.toString();
    }

}
