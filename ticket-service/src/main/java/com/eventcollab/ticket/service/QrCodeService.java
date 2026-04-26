package com.eventcollab.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class QrCodeService {

    private static final int QR_SIZE = 300; // pixels

    // Genere un QR code PNG encode en base64
    // Le contenu encode : ticketId + eventId + userId (infos de check-in)
    public String generate(UUID ticketId, UUID eventId, UUID userId) {
        String content = String.format(
                "EVENTCOLLAB|TICKET:%s|EVENT:%s|USER:%s",
                ticketId, eventId, userId
        );

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            log.debug("QR code genere pour le ticket {}", ticketId);
            return "data:image/png;base64," + base64;

        } catch (WriterException | IOException e) {
            log.error("Erreur generation QR code : {}", e.getMessage());
            throw new RuntimeException("Impossible de generer le QR code", e);
        }
    }
}