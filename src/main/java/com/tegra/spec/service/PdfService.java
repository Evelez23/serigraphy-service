package com.tegra.spec.service;

import com.tegra.spec.model.Spec;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.graphics.image.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.List;

@Service
public class PdfService {

    public ByteArrayOutputStream generatePdf(String templatePath, Spec spec) throws Exception {

        PDDocument doc = PDDocument.load(new File(templatePath));
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

        if (form == null)
            throw new IllegalStateException("La plantilla PDF no tiene formulario (AcroForm).");

        set(form, "customer", spec.customer);
        set(form, "style", spec.style);
        set(form, "colorway_number", spec.colorway_number);
        set(form, "colorway_name", spec.colorway_name);
        set(form, "season", spec.season);
        set(form, "pattern", spec.pattern);
        set(form, "po", spec.po);
        set(form, "sample_type", spec.sample_type);
        set(form, "date", spec.date);
        set(form, "requested_by", spec.requested_by);

        set(form, "ink_type", spec.ink_type);
        set(form, "temp", spec.temp);
        set(form, "time", spec.time);

        set(form, "placement", spec.placement);
        set(form, "dimensions", spec.dimensions);

        set(form, "instructions", spec.instructions);

        set(form, "artist", spec.artist);
        set(form, "technician", spec.technician);

        List<String> inks = spec.ink_sequence;
        if (inks != null) {
            for (int i = 0; i < inks.size(); i++) {
                set(form, "ink_" + (i + 1), inks.get(i));
            }
        }

        form.flatten();

        if (spec.art_image != null && !spec.art_image.isBlank()) {
            PDPage page = doc.getPage(0);
            PDPageContentStream cs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

            PDImageXObject img = convertImage(doc, spec.art_image);

            float x = 60;
            float y = 300;
            float w = 360;
            float h = 360;

            cs.drawImage(img, x, y, w, h);
            cs.close();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return baos;
    }

    private void set(PDAcroForm form, String name, String value) throws IOException {
        PDField f = form.getField(name);
        if (f != null) f.setValue(value == null ? "" : value);
    }

    private PDImageXObject convertImage(PDDocument doc, String base64) throws IOException {
        String clean = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64;
        byte[] bytes = Base64.getDecoder().decode(clean);
        BufferedImage bim = ImageIO.read(new ByteArrayInputStream(bytes));
        return LosslessFactory.createFromImage(doc, bim);
    }
}
