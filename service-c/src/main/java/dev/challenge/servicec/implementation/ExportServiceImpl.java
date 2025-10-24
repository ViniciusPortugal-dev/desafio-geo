package dev.challenge.servicec.implementation;

import com.opencsv.CSVWriter;
import dev.challenge.servicec.readmodel.PedidoB;
import dev.challenge.servicec.repo.PedidoBRepository;
import dev.challenge.servicec.services.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final String[] HEADER = {
            "id","descricao","valor","id_usuario","nome_entregador","telefone_entregador"
    };

    private final PedidoBRepository repo;

    @Override
    public void writePedidosCsv(Writer out) throws IOException {
        List<PedidoB> pedidos = repo.findAll();

        try (CSVWriter writer = new CSVWriter(out)) {
            writer.writeNext(HEADER, false);
            for (PedidoB p : pedidos) {
                writer.writeNext(new String[]{
                        s(p.getId()),
                        s(p.getDescricao()),
                        money(p.getValor()),
                        s(p.getIdUsuario()),
                        s(p.getNomeEntregador()),
                        s(p.getTelefoneEntregador())
                }, false);
            }
            writer.flush();
        }
    }

    // ---------- helpers ----------

    private static String s(Object o) {
        if (o == null) return "";
        if (o instanceof String str) return str;
        return String.valueOf(o);
    }

    private static String money(BigDecimal v) {
        if (v == null) return "";
        return v.stripTrailingZeros().toPlainString();
    }
}
