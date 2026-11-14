package com.victor.ampara.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de movimentações (auditoria) feitas pelo aplicativo.
 * Os documentos vão para a coleção "movimentacoes", que só pode receber novos
 * registros: ninguém altera nem apaga (regras do Firestore). Só o nível
 * administrador consulta, pelo painel (o moderador não tem acesso).
 *
 * Privacidade: o app NÃO registra login/logout nem configurações de PIN e
 * disfarce. O horário de login poderia ser cruzado com o horário de uma
 * denúncia anônima e revelar quem a fez; e registrar o disfarce entregaria
 * que a pessoa usa essa proteção.
 */
public final class MovimentacaoHelper {

    public static final String COLECAO = "movimentacoes";

    private MovimentacaoHelper() {
    }

    /** Movimentação de um usuário identificado. */
    public static Map<String, Object> doUsuario(String acao, String descricao, String entidade,
                                                String entidadeId, String referencia, String nome) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> m = base(acao, descricao, entidade, entidadeId, referencia);
        m.put("perfil", "usuario");
        m.put("autorUid", user != null ? user.getUid() : null);
        m.put("autorEmail", user != null ? user.getEmail() : null);
        m.put("autorNome", nome != null && !nome.isEmpty() ? nome : (user != null ? user.getEmail() : "Usuário"));
        return m;
    }

    /** Movimentação de uma denúncia anônima: sem nenhum dado de quem fez. */
    public static Map<String, Object> anonima(String entidadeId, String protocolo) {
        Map<String, Object> m = base("denuncia_anonima", "Registrou denúncia anônima",
                "denuncia", entidadeId, protocolo);
        m.put("perfil", "anonimo");
        m.put("autorUid", null);
        m.put("autorEmail", null);
        m.put("autorNome", "Anônimo");
        return m;
    }

    /** Grava sozinha (quando não faz parte de um lote). Falhas não atrapalham o app. */
    public static void registrar(Map<String, Object> movimentacao) {
        FirebaseFirestore.getInstance().collection(COLECAO).add(movimentacao);
    }

    private static Map<String, Object> base(String acao, String descricao, String entidade,
                                            String entidadeId, String referencia) {
        Map<String, Object> m = new HashMap<>();
        m.put("acao", acao);
        m.put("descricao", descricao);
        m.put("origem", "app");
        m.put("entidade", entidade);
        m.put("entidadeId", entidadeId);
        m.put("referencia", referencia);
        m.put("detalhes", null);
        m.put("dataHora", FieldValue.serverTimestamp()); // horário do servidor, não do celular
        return m;
    }
}
