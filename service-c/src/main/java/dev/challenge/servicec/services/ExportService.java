package dev.challenge.servicec.services;

import java.io.IOException;
import java.io.Writer;

public interface ExportService {
    void writePedidosCsv(Writer out) throws IOException;
}
