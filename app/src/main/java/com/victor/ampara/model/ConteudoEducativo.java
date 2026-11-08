package com.victor.ampara.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Módulo educativo (modulo_educativo no diagrama de classes).
 * Conteúdos informativos e legislações exibidos no app.
 * Para incluir um novo tópico, basta adicioná-lo em uma das listas abaixo.
 */
public final class ConteudoEducativo {

    private ConteudoEducativo() {
    }

    public static class Topico implements Serializable {
        public final String titulo;
        public final String resumo;
        public final String texto;

        public Topico(String titulo, String resumo, String texto) {
            this.titulo = titulo;
            this.resumo = resumo;
            this.texto = texto;
        }
    }

    // ------------------------------------------------------------------
    // CONTEÚDOS EDUCATIVOS (3.1.9)
    // ------------------------------------------------------------------
    public static List<Topico> conteudos() {
        return Arrays.asList(
                new Topico(
                        "Tipos de violência contra a mulher",
                        "As 5 formas previstas na Lei Maria da Penha.",
                        "A violência contra a mulher não é apenas física. A Lei Maria da Penha (art. 7º) reconhece cinco formas:\n\n"
                                + "• Física: qualquer conduta que ofenda a integridade ou a saúde do corpo.\n\n"
                                + "• Psicológica: causar dano emocional, diminuir a autoestima, humilhar, ameaçar, isolar ou controlar ações e decisões.\n\n"
                                + "• Sexual: forçar a presenciar, manter ou participar de relação sexual não desejada, ou impedir o uso de métodos contraceptivos.\n\n"
                                + "• Patrimonial: reter, subtrair ou destruir objetos, documentos, instrumentos de trabalho, bens ou dinheiro.\n\n"
                                + "• Moral: calúnia, difamação ou injúria."),
                new Topico(
                        "Ciclo da violência",
                        "Entenda as fases que costumam se repetir.",
                        "A violência em relacionamentos costuma seguir um ciclo que se repete:\n\n"
                                + "1. Aumento da tensão: irritação, insultos, ameaças. A vítima sente que precisa \"pisar em ovos\".\n\n"
                                + "2. Ato de violência: a tensão explode em agressão verbal, física, psicológica, moral ou patrimonial.\n\n"
                                + "3. Arrependimento (\"lua de mel\"): o agressor pede desculpas, faz promessas e demonstra carinho, até que a tensão volta.\n\n"
                                + "Com o tempo, os intervalos ficam menores e as agressões mais graves. Reconhecer o ciclo é o primeiro passo para quebrá-lo."),
                new Topico(
                        "Racismo e injúria racial",
                        "O que são e por que ambos são crime.",
                        "Racismo é a discriminação ou o preconceito por raça, cor, etnia, religião ou procedência nacional, como impedir alguém de entrar em um local, de ser contratado ou atendido.\n\n"
                                + "Injúria racial é ofender a dignidade de uma pessoa usando elementos de raça, cor ou etnia, como xingamentos e apelidos.\n\n"
                                + "Desde a Lei nº 14.532/2023, a injúria racial foi equiparada ao crime de racismo (Lei nº 7.716/1989). Os dois são inafiançáveis e imprescritíveis, como prevê a Constituição Federal (art. 5º, XLII).\n\n"
                                + "Ofensas feitas pela internet também são crime e podem ser denunciadas."),
                new Topico(
                        "LGBTfobia",
                        "Discriminação contra pessoas LGBTQIA+ é crime.",
                        "LGBTfobia é o preconceito, a discriminação ou a violência contra pessoas lésbicas, gays, bissexuais, travestis, transexuais, queer, intersexo e outras identidades.\n\n"
                                + "Em 2019, o Supremo Tribunal Federal (ADO 26 e MI 4733) decidiu que atos de homofobia e transfobia devem ser enquadrados na Lei do Racismo (Lei nº 7.716/1989), até que exista uma lei específica.\n\n"
                                + "Exemplos: recusar atendimento, ofender, ameaçar ou agredir alguém por sua orientação sexual ou identidade de gênero."),
                new Topico(
                        "Crimes cibernéticos",
                        "Ameaças virtuais, invasão de conta e exposição.",
                        "A internet não é terra sem lei. Alguns crimes comuns:\n\n"
                                + "• Invasão de dispositivo (celular, computador, contas): art. 154-A do Código Penal, incluído pela Lei Carolina Dieckmann (Lei nº 12.737/2012).\n\n"
                                + "• Divulgação de fotos ou vídeos íntimos sem consentimento: art. 218-C do Código Penal.\n\n"
                                + "• Perseguição (stalking), inclusive pela internet: art. 147-A do Código Penal.\n\n"
                                + "• Ameaças e ofensas por mensagens ou redes sociais.\n\n"
                                + "O que fazer: guarde prints com data e hora, copie os links (URLs) e o perfil do autor, não apague as conversas e denuncie. Ative a verificação em duas etapas nas suas contas."),
                new Topico(
                        "Como fazer uma denúncia",
                        "Canais disponíveis e o que informar.",
                        "Você pode denunciar de várias formas:\n\n"
                                + "• Pelo AMPARA: em \"Registrar denúncia\", de forma identificada (para acompanhar o andamento) ou anônima.\n\n"
                                + "• 190 (Polícia Militar): em caso de perigo imediato.\n\n"
                                + "• 180 (Central de Atendimento à Mulher): gratuito, 24 horas, também por WhatsApp.\n\n"
                                + "• 100 (Disque Direitos Humanos): violações contra crianças, idosos, pessoas LGBTQIA+, pessoas com deficiência e outros grupos.\n\n"
                                + "• Delegacia, especialmente a Delegacia da Mulher, para registrar boletim de ocorrência e pedir medidas protetivas.\n\n"
                                + "Informe o que aconteceu, quando, onde e quem estava envolvido. Fotos, prints e mensagens ajudam como evidência."),
                new Topico(
                        "Como buscar ajuda",
                        "Onde encontrar acolhimento e orientação.",
                        "Você não está sozinha(o). Além da denúncia, existem serviços de acolhimento:\n\n"
                                + "• CRAS e CREAS: assistência social. O CREAS atende pessoas em situação de violação de direitos.\n\n"
                                + "• Defensoria Pública: orientação jurídica gratuita, inclusive para medidas protetivas.\n\n"
                                + "• Unidades de saúde (SUS): atendimento médico e psicológico.\n\n"
                                + "• ONGs e casas de acolhimento: veja as instituições da sua cidade em \"Rede de Apoio\".\n\n"
                                + "Dica: combine com uma pessoa de confiança um sinal ou palavra para pedir ajuda, e deixe documentos e números importantes em local de fácil acesso.")
        );
    }

    // ------------------------------------------------------------------
    // LEIS E DIREITOS (3.1.10)
    // ------------------------------------------------------------------
    public static List<Topico> leis() {
        return Arrays.asList(
                new Topico(
                        "Lei Maria da Penha",
                        "Lei nº 11.340/2006",
                        "Principal instrumento de combate à violência doméstica e familiar contra a mulher no Brasil.\n\n"
                                + "Ela se aplica a relações domésticas, familiares ou íntimas de afeto, mesmo que o agressor e a vítima não morem juntos.\n\n"
                                + "Medidas protetivas de urgência:\n"
                                + "• Afastamento do agressor do lar.\n"
                                + "• Proibição de aproximação e de contato com a vítima.\n"
                                + "• Suspensão da posse ou restrição do porte de armas.\n"
                                + "• Proteção dos filhos e pagamento de alimentos.\n\n"
                                + "O juiz tem até 48 horas para decidir sobre o pedido de medida protetiva."),
                new Topico(
                        "Lei do Feminicídio",
                        "Leis nº 13.104/2015 e nº 14.994/2024",
                        "Feminicídio é o assassinato de uma mulher por razões da condição do sexo feminino, como em casos de violência doméstica ou de menosprezo e discriminação à mulher.\n\n"
                                + "A Lei nº 13.104/2015 incluiu o feminicídio no Código Penal e no rol dos crimes hediondos.\n\n"
                                + "Com a Lei nº 14.994/2024, o feminicídio passou a ser um crime autônomo (art. 121-A do Código Penal), com pena de 20 a 40 anos de reclusão."),
                new Topico(
                        "Estatuto da Igualdade Racial",
                        "Lei nº 12.288/2010",
                        "Garante à população negra a igualdade de oportunidades, a defesa dos direitos étnicos individuais, coletivos e difusos e o combate à discriminação e às demais formas de intolerância étnica.\n\n"
                                + "Ele trata de temas como saúde, educação, cultura, liberdade de crença, acesso à terra, moradia, trabalho e meios de comunicação, e prevê políticas de ações afirmativas para reduzir desigualdades."),
                new Topico(
                        "Lei do Racismo",
                        "Lei nº 7.716/1989 e Lei nº 14.532/2023",
                        "Define os crimes resultantes de preconceito de raça, cor, etnia, religião ou procedência nacional, como recusar atendimento, impedir o acesso a locais ou negar emprego.\n\n"
                                + "A Lei nº 14.532/2023 equiparou a injúria racial ao crime de racismo. Assim, ofender alguém por sua raça, cor ou etnia também é inafiançável e imprescritível (Constituição Federal, art. 5º, XLII)."),
                new Topico(
                        "Criminalização da LGBTfobia",
                        "STF: ADO 26 e MI 4733 (2019)",
                        "Em junho de 2019, o Supremo Tribunal Federal reconheceu a omissão do Congresso em criar uma lei específica e decidiu que a homofobia e a transfobia devem ser punidas com base na Lei do Racismo (Lei nº 7.716/1989).\n\n"
                                + "Com isso, discriminar, ofender ou impedir o acesso de alguém a serviços por sua orientação sexual ou identidade de gênero é crime.\n\n"
                                + "Em 2018, o STF também garantiu às pessoas trans o direito de alterar nome e gênero no registro civil diretamente em cartório, sem necessidade de cirurgia ou laudo."),
                new Topico(
                        "Lei Carolina Dieckmann",
                        "Lei nº 12.737/2012",
                        "Tipificou os crimes informáticos no Brasil, como a invasão de dispositivo informático (celular, computador, contas) para obter, alterar ou destruir dados sem autorização (art. 154-A do Código Penal).\n\n"
                                + "As penas foram aumentadas pela Lei nº 14.155/2021.\n\n"
                                + "Outros crimes digitais relacionados: divulgação de cena íntima sem consentimento (art. 218-C do CP) e perseguição, inclusive virtual (art. 147-A do CP).")
        );
    }
}
