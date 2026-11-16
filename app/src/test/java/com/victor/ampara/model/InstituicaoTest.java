package com.victor.ampara.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes da Instituição da Rede de Apoio. */
public class InstituicaoTest {

    @Test
    public void getCidadeUf_juntaCidadeEUf() {
        Instituicao i = new Instituicao();
        i.setCidade("Apucarana");
        i.setUf("PR");
        assertEquals("Apucarana - PR", i.getCidadeUf());
    }

    @Test
    public void getCidadeUf_semUfMostraSoACidade() {
        Instituicao i = new Instituicao();
        i.setCidade("Apucarana");
        i.setUf("");
        assertEquals("Apucarana", i.getCidadeUf());
    }

    @Test
    public void getCidadeUf_semCidadeRetornaTextoVazio() {
        Instituicao i = new Instituicao();
        i.setUf("PR");
        assertEquals("", i.getCidadeUf());
    }

    @Test
    public void novaInstituicao_comecaAtiva() {
        assertTrue(new Instituicao().isAtivo());
    }
}
