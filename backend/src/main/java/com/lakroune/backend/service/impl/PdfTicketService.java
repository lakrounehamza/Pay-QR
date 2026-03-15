package com.lakroune.backend.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lakroune.backend.dto.response.PaymentTicketDTO;
import com.lakroune.backend.enums.OperationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class PdfTicketService {

    public byte[] generatePaymentTicketPdf(PaymentTicketDTO ticket) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.setMargins(10, 10, 10, 10);

            Font fontTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9);
            Font fontSmall = new Font(Font.FontFamily.HELVETICA, 8);

            Paragraph title = new Paragraph("PAYMENT RECEIPT", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("PAY-QR", fontSmall);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            Paragraph txHeader = new Paragraph("Transaction Details", fontHeader);
            txHeader.setSpacingAfter(8);
            document.add(txHeader);

            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);

            addDetailRow(detailsTable, "Transaction ID:", ticket.operationId().toString(), fontHeader, fontNormal);
            addDetailRow(detailsTable, "Type:", ticket.type().toString(), fontHeader, fontNormal);
            addDetailRow(detailsTable, "Amount:", String.format("%.2f MAD", ticket.amount()), fontHeader, fontNormal);
            addDetailRow(detailsTable, "Status:", getStatusBadge(ticket.status()), fontHeader, fontNormal);
            addDetailRow(detailsTable, "Date:", formatDate(ticket.createdAt()), fontHeader, fontNormal);

            document.add(detailsTable);
            document.add(new Paragraph(" "));

            Paragraph senderRecipientHeader = new Paragraph("Transaction Parties", fontHeader);
            senderRecipientHeader.setSpacingAfter(8);
            document.add(senderRecipientHeader);

            PdfPTable partiesTable = new PdfPTable(2);
            partiesTable.setWidthPercentage(100);

            PdfPCell fromCell = new PdfPCell();
            fromCell.setBorderWidth(0.5f);
            fromCell.addElement(new Paragraph("From:", fontHeader));
            fromCell.addElement(new Paragraph(ticket.sourceUserName(), fontNormal));
            fromCell.addElement(new Paragraph("Acc: " + ticket.sourceAccountRef(), fontSmall));
            partiesTable.addCell(fromCell);

            PdfPCell toCell = new PdfPCell();
            toCell.setBorderWidth(0.5f);
            toCell.addElement(new Paragraph("To:", fontHeader));
            toCell.addElement(new Paragraph(ticket.destinationUserName(), fontNormal));
            toCell.addElement(new Paragraph("Acc: " + ticket.destinationAccountRef(), fontSmall));
            partiesTable.addCell(toCell);

            document.add(partiesTable);
            document.add(new Paragraph(" "));

            if (ticket.qrCodeId() != null) {
                Paragraph qrRef = new Paragraph("QR Code ID: " + ticket.qrCodeId(), fontSmall);
                qrRef.setAlignment(Element.ALIGN_CENTER);
                qrRef.setSpacingAfter(15);
                document.add(qrRef);
            }

            Paragraph footer = new Paragraph("Thank you for using PAY-QR", fontSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15);
            document.add(footer);

            Paragraph timestamp = new Paragraph("Generated: " + formatDateTime(new Date()), fontSmall);
            timestamp.setAlignment(Element.ALIGN_CENTER);
            document.add(timestamp);

            document.close();
            log.info("PDF ticket generated for operation: {}", ticket.operationId());
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("Failed to generate PDF ticket", e);
            throw new IOException("Unable to generate PDF ticket", e);
        }
    }

    private void addDetailRow(PdfPTable table, String label, String value, Font fontBold, Font fontNormal) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, fontBold));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value, fontNormal));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(valueCell);
    }

    private String getStatusBadge(OperationStatus status) {
        return status == OperationStatus.SUCCESS ? "✓ SUCCESS" : "✗ " + status.toString();
    }

    private String formatDate(Object date) {
        if (date == null) return "N/A";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(date);
    }
}
