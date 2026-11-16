package com.victor.ampara.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Regras de negócio de validação usadas no cadastro e no registro de denúncias.
 *
 * Classe em Java puro (sem dependências do Android ou do Firebase) para que as
 * regras possam ser testadas com testes unitários comuns (JUnit) na esteira de CI.
 */
public final class ValidacaoUtils {

    /** Mínimo de 8 caracteres, com maiúscula, minúscula, número e caractere especial. */
    private static final Pattern SENHA_FORTE =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    /** Protocolo de denúncia: AMP-ANO-XXXXX (5 caracteres hexadecimais maiúsculos). */
    private static final Pattern PROTOCOLO = Pattern.compile("^AMP-\\d{4}-[0-9A-F]{5}$");

    private ValidacaoUtils() {
    }

    /** Regra de senha do cadastro de usuário. */
    public static boolean senhaForte(String senha) {
        return senha != null && SENHA_FORTE.matcher(senha).matches();
    }

    /**
     * Telefone com DDD: depois de remover a máscara, precisa ter 10 (fixo)
     * ou 11 (celular) dígitos.
     */
    public static boolean telefoneValido(String telefone) {
        if (telefone == null) return false;
        int digitos = telefone.replaceAll("\\D", "").length();
        return digitos == 10 || digitos == 11;
    }

    /**
     * Gera o protocolo da denúncia a partir do ano e de um identificador aleatório
     * (ex.: UUID). Ex.: gerarProtocolo(2026, "a1b2c3d4-...") = "AMP-2026-A1B2C".
     */
    public static String gerarProtocolo(int ano, String idAleatorio) {
        if (idAleatorio == null || idAleatorio.length() < 5) {
            throw new IllegalArgumentException("Identificador aleatório precisa ter ao menos 5 caracteres");
        }
        if (ano < 2000 || ano > 9999) {
            throw new IllegalArgumentException("Ano inválido: " + ano);
        }
        return "AMP-" + ano + "-" + idAleatorio.substring(0, 5).toUpperCase(Locale.ROOT);
    }

    /** Confere se um texto tem o formato de protocolo usado pelo AMPARA. */
    public static boolean protocoloValido(String protocolo) {
        return protocolo != null && PROTOCOLO.matcher(protocolo).matches();
    }

    /** Campos obrigatórios da denúncia: tipo, data/hora e relato não podem ficar em branco. */
    public static boolean camposObrigatoriosDenuncia(String tipo, String dataHora, String relato) {
        return naoVazio(tipo) && naoVazio(dataHora) && naoVazio(relato);
    }

    private static boolean naoVazio(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
