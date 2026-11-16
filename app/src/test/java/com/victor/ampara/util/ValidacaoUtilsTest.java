package com.victor.ampara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes das regras de validação do cadastro e da denúncia. */
public class ValidacaoUtilsTest {

    // ------------------------------------------------ Regra 1: senha forte (cadastro)

    @Test
    public void senhaForte_aceitaSenhaQueCumpreTodasAsRegras() {
        assertTrue(ValidacaoUtils.senhaForte("Ampara@2026"));
    }

    @Test
    public void senhaForte_aceitaExatamenteOitoCaracteres_limiteInferior() {
        assertTrue(ValidacaoUtils.senhaForte("Abcde1@x"));
    }

    @Test
    public void senhaForte_rejeitaSeteCaracteres_abaixoDoLimite() {
        assertFalse(ValidacaoUtils.senhaForte("Abcd1@x"));
    }

    @Test
    public void senhaForte_rejeitaSenhaSemCaractereEspecial() {
        assertFalse(ValidacaoUtils.senhaForte("Ampara2026"));
    }

    @Test
    public void senhaForte_rejeitaSenhaSemMaiuscula() {
        assertFalse(ValidacaoUtils.senhaForte("ampara@2026"));
    }

    @Test
    public void senhaForte_rejeitaNuloEVazio() {
        assertFalse(ValidacaoUtils.senhaForte(null));
        assertFalse(ValidacaoUtils.senhaForte(""));
    }

    // ------------------------------------------------ Regra 2: telefone do contato de confiança

    @Test
    public void telefoneValido_aceitaCelularComMascara_11Digitos() {
        assertTrue(ValidacaoUtils.telefoneValido("(43) 99999-1234"));
    }

    @Test
    public void telefoneValido_aceitaFixo_10Digitos() {
        assertTrue(ValidacaoUtils.telefoneValido("4333221100"));
    }

    @Test
    public void telefoneValido_rejeitaSemDDD_9Digitos() {
        assertFalse(ValidacaoUtils.telefoneValido("99999-1234"));
    }

    @Test
    public void telefoneValido_rejeitaMaisDe11Digitos() {
        assertFalse(ValidacaoUtils.telefoneValido("+55 (43) 99999-1234"));
    }

    @Test
    public void telefoneValido_rejeitaNuloEApenasLetras() {
        assertFalse(ValidacaoUtils.telefoneValido(null));
        assertFalse(ValidacaoUtils.telefoneValido("telefone"));
    }

    // ------------------------------------------------ Regra 3: protocolo da denúncia

    @Test
    public void gerarProtocolo_montaNoFormatoAmpAnoCodigo() {
        String protocolo = ValidacaoUtils.gerarProtocolo(2026, "a1b2c3d4-0000-0000-0000-000000000000");
        assertEquals("AMP-2026-A1B2C", protocolo);
        assertTrue(ValidacaoUtils.protocoloValido(protocolo));
    }

    @Test(expected = IllegalArgumentException.class)
    public void gerarProtocolo_lancaExcecaoComIdentificadorCurto() {
        ValidacaoUtils.gerarProtocolo(2026, "abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void gerarProtocolo_lancaExcecaoComIdentificadorNulo() {
        ValidacaoUtils.gerarProtocolo(2026, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void gerarProtocolo_lancaExcecaoComAnoInvalido() {
        ValidacaoUtils.gerarProtocolo(99, "a1b2c3d4");
    }

    @Test
    public void protocoloValido_rejeitaFormatosErrados() {
        assertFalse(ValidacaoUtils.protocoloValido("AMP-2026-a1b2c")); // minúsculas
        assertFalse(ValidacaoUtils.protocoloValido("XYZ-2026-A1B2C")); // prefixo errado
        assertFalse(ValidacaoUtils.protocoloValido("AMP-26-A1B2C"));   // ano com 2 dígitos
        assertFalse(ValidacaoUtils.protocoloValido(null));
    }

    // ------------------------------------------------ Regra 4: campos obrigatórios da denúncia

    @Test
    public void camposObrigatorios_aceitaDenunciaCompleta() {
        assertTrue(ValidacaoUtils.camposObrigatoriosDenuncia(
                "Violência doméstica", "16/11/2026 14:30", "Relato do ocorrido"));
    }

    @Test
    public void camposObrigatorios_rejeitaRelatoVazioOuSoComEspacos() {
        assertFalse(ValidacaoUtils.camposObrigatoriosDenuncia("Racismo", "16/11/2026 14:30", ""));
        assertFalse(ValidacaoUtils.camposObrigatoriosDenuncia("Racismo", "16/11/2026 14:30", "   "));
    }

    @Test
    public void camposObrigatorios_rejeitaTipoNulo() {
        assertFalse(ValidacaoUtils.camposObrigatoriosDenuncia(null, "16/11/2026 14:30", "Relato"));
    }
}
