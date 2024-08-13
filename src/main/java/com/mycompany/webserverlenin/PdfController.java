package com.mycompany.webserverlenin;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
//import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.Canvas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class PdfController {

    private final ViewingService viewingService;

    public PdfController(ViewingService viewingService) {
        this.viewingService = viewingService;
    }

    @GetMapping("/download-pdf/{jobCode}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String jobCode) {
        try {
            // Fetch project details
            List<Document> projectDetails = viewingService.getProjectDetail(jobCode);
            if (projectDetails == null || projectDetails.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Create PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            // Add header and footer
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterHandler());

            // Add title
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Paragraph title = new Paragraph("Lenin Project Details")
                .setFont(font)
                .setFontSize(20)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Add project details table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
            table.setWidth(UnitValue.createPercentValue(100));

            Document project = projectDetails.get(0);
            addCellToTable(table, "Job Type:", project.getString("job_type"));
            addCellToTable(table, "Job Code:", project.getString("job_code"));
            addCellToTable(table, "Client Name:", project.getString("client_name"));
            addCellToTable(table, "Address:", project.getString("client_address"));
            addCellToTable(table, "Contact:", project.getString("client_contact"));
            addCellToTable(table, "Request:", project.getString("client_request"));
            addCellToTable(table, "Team Leader:", project.getString("team_leader"));
            addCellToTable(table, "Solution Manpower:", String.valueOf(project.getInteger("solution_manpower")));
            addCellToTable(table, "Solution Instructions:", project.getString("solution_instructions"));
            addCellToTable(table, "Partial Deployment:", project.getString("partial_deployed"));
            addCellToTable(table, "Date Due:", project.getString("date_due"));
            addCellToTable(table, "Date Issued:", project.getString("date_issued"));
            addCellToTable(table, "Date Confirmed:", project.getString("date_confirmed"));
            addCellToTable(table, "Date Release:", project.getString("date_released"));
            addCellToTable(table, "Running Days:", project.getString("running_days"));
            addCellToTable(table, "Warranty:", project.getString("warranty"));
            addCellToTable(table, "Status:", project.getString("status"));
            addCellToTable(table, "Confirmed by:", project.getString("user_confirmed"));

            document.add(table);
            document.close();

            // Return PDF as response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "job_details_" + jobCode + ".pdf");
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void addCellToTable(Table table, String header, String content) {
        table.addCell(new Paragraph(header).setBold());
        table.addCell(new Paragraph(content != null ? content : ""));
    }

    private class HeaderFooterHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);

            // Add header
            PdfFont font;
            try {
                font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            Rectangle pageSize = pdfDoc.getDefaultPageSize();
            Canvas layoutCanvas = new Canvas(canvas, pageSize);
            layoutCanvas.setFontSize(12).setFontColor(ColorConstants.BLACK);
            layoutCanvas.showTextAligned("Project Details - Confidential", 36, pageSize.getTop() - 20, TextAlignment.LEFT);
            layoutCanvas.showTextAligned("Page " + pageNumber, pageSize.getWidth() - 36, pageSize.getTop() - 20, TextAlignment.RIGHT);
            layoutCanvas.close();
        }
    }
}

//package com.mycompany.webserverlenin;
//
//import com.itextpdf.io.font.constants.StandardFonts;
//import com.itextpdf.io.image.ImageData;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.events.Event;
//import com.itextpdf.kernel.events.IEventHandler;
//import com.itextpdf.kernel.events.PdfDocumentEvent;
//import com.itextpdf.kernel.font.PdfFont;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.Rectangle;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfPage;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
//import com.itextpdf.layout.Canvas;
//import com.itextpdf.layout.element.Image;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.properties.TextAlignment;
//import com.itextpdf.layout.properties.UnitValue;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//import org.bson.Document;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/")
//public class PdfController {
//
//    private final ViewingService viewingService;
//
//    public PdfController(ViewingService viewingService) {
//        this.viewingService = viewingService;
//    }
//
//    @GetMapping("/download-pdf/{jobCode}")
//    public ResponseEntity<byte[]> downloadPdf(@PathVariable String jobCode) {
//        try {
//            // Fetch project details
//            List<Document> projectDetails = viewingService.getProjectDetail(jobCode);
//            if (projectDetails == null || projectDetails.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//
//            // Create PDF
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            PdfWriter writer = new PdfWriter(baos);
//            PdfDocument pdf = new PdfDocument(writer);
//            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);
//
//            // Add header and footer
//            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterHandler("/images/logo.png"));
//
//            // Add title
//            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            Paragraph title = new Paragraph("Lenin Job Order Details")
//                .setFont(font)
//                .setFontSize(20)
//                .setFontColor(ColorConstants.BLUE)
//                .setTextAlignment(TextAlignment.CENTER);
//            document.add(title);
//
//            // Add project details table
//            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
//            table.setWidth(UnitValue.createPercentValue(100));
//
//            Document project = projectDetails.get(0);
//            addCellToTable(table, "Job Type:", project.getString("job_type"));
//            addCellToTable(table, "Job Code:", project.getString("job_code"));
//            addCellToTable(table, "Client Name:", project.getString("client_name"));
//            addCellToTable(table, "Address:", project.getString("client_address"));
//            addCellToTable(table, "Contact:", project.getString("client_contact"));
//            addCellToTable(table, "Request:", project.getString("client_request"));
//            addCellToTable(table, "Team Leader:", project.getString("team_leader"));
//            addCellToTable(table, "Solution Manpower:", String.valueOf(project.getInteger("solution_manpower")));
//            addCellToTable(table, "Solution Instructions:", project.getString("solution_instructions"));
//            addCellToTable(table, "Partial Deployment:", project.getString("partial_deployed"));
//            addCellToTable(table, "Date Due:", project.getString("date_due"));
//            addCellToTable(table, "Date Issued:", project.getString("date_issued"));
//            addCellToTable(table, "Date Confirmed:", project.getString("date_confirmed"));
//            addCellToTable(table, "Date Release:", project.getString("date_released"));
//            addCellToTable(table, "Running Days:", project.getString("running_days"));
//            addCellToTable(table, "Warranty:", project.getString("warranty"));
//            addCellToTable(table, "Status:", project.getString("status"));
//            addCellToTable(table, "Confirmed by:", project.getString("user_confirmed"));
//
//            document.add(table);
//            document.close();
//
//            // Return PDF as response
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "job_details_" + jobCode + ".pdf");
//            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    private void addCellToTable(Table table, String header, String content) {
//        table.addCell(new Paragraph(header).setBold());
//        table.addCell(new Paragraph(content != null ? content : ""));
//    }
//
//    private class HeaderFooterHandler implements IEventHandler {
//        private String logoPath;
//
//        public HeaderFooterHandler(String logoPath) {
//            this.logoPath = logoPath;
//        }
//
//        @Override
//        public void handleEvent(Event event) {
//            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
//            PdfDocument pdfDoc = docEvent.getDocument();
//            PdfPage page = docEvent.getPage();
//            int pageNumber = pdfDoc.getPageNumber(page);
//
//            // Add header
//            PdfFont font;
//            try {
//                font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
//            Rectangle pageSize = pdfDoc.getDefaultPageSize();
//            Canvas layoutCanvas = new Canvas(canvas, pageSize);
//
//            try {
//                InputStream is = new ClassPathResource(logoPath).getInputStream();
//                ImageData imageData = ImageDataFactory.create(is.readAllBytes());
//                Image logo = new Image(imageData).scaleAbsolute(50, 50).setFixedPosition(pageSize.getLeft() + 36, pageSize.getTop() - 60);
//                layoutCanvas.add(logo);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            Paragraph title = new Paragraph("Lenin Job Order Details")
//                .setFont(font)
//                .setFontSize(16)
//                .setFontColor(ColorConstants.BLACK)
//                .setTextAlignment(TextAlignment.RIGHT)
//                .setFixedPosition(pageSize.getWidth() - 200, pageSize.getTop() - 40, 150);
//            layoutCanvas.add(title);
//
//            Paragraph pageNumberParagraph = new Paragraph("Page " + pageNumber)
//                .setFont(font)
//                .setFontSize(12)
//                .setFontColor(ColorConstants.BLACK)
//                .setTextAlignment(TextAlignment.RIGHT)
//                .setFixedPosition(pageSize.getWidth() - 60, pageSize.getTop() - 20, 50);
//            layoutCanvas.add(pageNumberParagraph);
//
//            layoutCanvas.close();
//        }
//    }
//}

