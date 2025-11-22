package com.tegra.spec.api;

import com.tegra.spec.model.Spec;
import com.tegra.spec.service.ExcelService;
import com.tegra.spec.service.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired ExcelService excelService;
    @Autowired PdfService pdfService;
    @Autowired ObjectMapper mapper;

    @PostMapping(value="/excel-to-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Spec> excelToJson(@RequestPart("file") MultipartFile file) {
        try {
            Spec spec = excelService.parseExcel(file.getInputStream());
            return ResponseEntity.ok(spec);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value="/generate-pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> generatePdf(@RequestBody Spec spec) {
        try {
            String templatePath = "/mnt/data/spec.pdf";
            ByteArrayOutputStream baos = pdfService.generatePdf(templatePath, spec);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("spec_filled.pdf").build());

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value="/generate-pdf-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generatePdfMultipart(
            @RequestPart("json") String json,
            @RequestPart(value="image", required=false) MultipartFile imageFile
    ) {
        try {
            Spec spec = mapper.readValue(json, Spec.class);

            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] b = imageFile.getBytes();
                String base64 = "data:" + imageFile.getContentType()
                        + ";base64," + java.util.Base64.getEncoder().encodeToString(b);
                spec.art_image = base64;
            }

            String templatePath = "/mnt/data/spec.pdf";
            ByteArrayOutputStream baos = pdfService.generatePdf(templatePath, spec);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("spec_filled.pdf").build());

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
