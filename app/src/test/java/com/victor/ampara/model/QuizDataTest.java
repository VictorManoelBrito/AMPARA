package com.victor.ampara.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes do quiz educativo (temas e perguntas). */
public class QuizDataTest {

    @Test
    public void buscar_retornaTemaExistente() {
        QuizData.Tema tema = QuizData.buscar("racismo");
        assertNotNull(tema);
        assertEquals("Discriminação Racial", tema.titulo);
    }

    @Test
    public void buscar_retornaNuloParaTemaInexistente() {
        assertNull(QuizData.buscar("tema_que_nao_existe"));
    }

    @Test
    public void todaPergunta_temQuatroAlternativasERespostaCorretaValida() {
        for (QuizData.Tema tema : QuizData.temas()) {
            assertFalse("Tema sem perguntas: " + tema.id, tema.perguntas.isEmpty());
            for (QuizData.Pergunta p : tema.perguntas) {
                assertEquals("Pergunta sem 4 alternativas: " + p.enunciado, 4, p.alternativas.length);
                assertTrue("Índice da correta fora de 0..3: " + p.enunciado,
                        p.correta >= 0 && p.correta <= 3);
            }
        }
    }
}
