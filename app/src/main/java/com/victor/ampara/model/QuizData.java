package com.victor.ampara.model;

import java.util.Arrays;
import java.util.List;

/**
 * Quizzes educativos (entidades quiz e pergunta_quiz do diagrama de classes).
 * Cada pergunta tem 4 alternativas, o índice da correta (0 a 3) e uma explicação.
 */
public final class QuizData {

    private QuizData() {
    }

    public static class Pergunta {
        public final String enunciado;
        public final String[] alternativas;
        public final int correta;
        public final String explicacao;

        public Pergunta(String enunciado, String[] alternativas, int correta, String explicacao) {
            this.enunciado = enunciado;
            this.alternativas = alternativas;
            this.correta = correta;
            this.explicacao = explicacao;
        }
    }

    public static class Tema {
        public final String id;
        public final String titulo;
        public final String descricao;
        public final List<Pergunta> perguntas;

        public Tema(String id, String titulo, String descricao, List<Pergunta> perguntas) {
            this.id = id;
            this.titulo = titulo;
            this.descricao = descricao;
            this.perguntas = perguntas;
        }
    }

    public static Tema buscar(String id) {
        for (Tema t : temas()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    private static String[] alt(String a, String b, String c, String d) {
        return new String[]{a, b, c, d};
    }

    public static List<Tema> temas() {
        return Arrays.asList(

                new Tema("violencia_mulher", "Violência contra a Mulher",
                        "O que a Lei Maria da Penha garante.",
                        Arrays.asList(
                                new Pergunta("Quais formas de violência a Lei Maria da Penha reconhece?",
                                        alt("Apenas a física",
                                                "Física e sexual",
                                                "Física, psicológica, sexual, patrimonial e moral",
                                                "Somente as que deixam marcas"),
                                        2, "O art. 7º da Lei 11.340/2006 prevê cinco formas: física, psicológica, sexual, patrimonial e moral."),
                                new Pergunta("Quebrar o celular ou reter os documentos da vítima é que tipo de violência?",
                                        alt("Moral", "Patrimonial", "Não é violência", "Sexual"),
                                        1, "Reter, subtrair ou destruir objetos, documentos ou bens é violência patrimonial."),
                                new Pergunta("Qual é o número da Central de Atendimento à Mulher?",
                                        alt("180", "190", "192", "100"),
                                        0, "O Ligue 180 é gratuito, funciona 24 horas e também atende pelo WhatsApp."),
                                new Pergunta("Em quanto tempo o juiz deve decidir sobre um pedido de medida protetiva?",
                                        alt("Em até 30 dias", "Em até 7 dias", "Em até 48 horas", "Após o fim do processo"),
                                        2, "Pela Lei Maria da Penha (art. 18), o juiz tem até 48 horas para decidir sobre as medidas protetivas."),
                                new Pergunta("Em quais relações a Lei Maria da Penha se aplica?",
                                        alt("Somente no casamento",
                                                "Somente se o casal morar junto",
                                                "Relações domésticas, familiares ou de afeto, mesmo sem morar junto",
                                                "Somente quando há filhos")
                                        , 2, "A lei protege a mulher em relações domésticas, familiares ou íntimas de afeto, independentemente de coabitação (art. 5º).")
                        )),

                new Tema("diversidade", "Respeito e Diversidade",
                        "Direitos da população LGBTQIA+.",
                        Arrays.asList(
                                new Pergunta("Em 2019, o que o STF decidiu sobre a homofobia e a transfobia?",
                                        alt("Que não são crime",
                                                "Que devem ser punidas como crime de racismo",
                                                "Que são apenas infração administrativa",
                                                "Que só são crime se houver agressão física"),
                                        1, "No julgamento da ADO 26 e do MI 4733, o STF enquadrou a LGBTfobia na Lei do Racismo (Lei 7.716/1989)."),
                                new Pergunta("Orientação sexual se refere a:",
                                        alt("Por quem a pessoa sente atração afetiva ou sexual",
                                                "O gênero com o qual a pessoa se identifica",
                                                "O sexo atribuído no nascimento",
                                                "Uma escolha passageira"),
                                        0, "Orientação sexual é sobre atração. Identidade de gênero é sobre como a pessoa se reconhece."),
                                new Pergunta("Um estabelecimento recusa atender uma pessoa por ela ser trans. Isso é:",
                                        alt("Direito do dono do estabelecimento",
                                                "Apenas falta de educação",
                                                "Crime, enquadrado na Lei do Racismo",
                                                "Permitido se houver um aviso na porta"),
                                        2, "Desde a decisão do STF de 2019, discriminar por identidade de gênero ou orientação sexual é crime."),
                                new Pergunta("Uma pessoa trans pode alterar o nome e o gênero no registro civil?",
                                        alt("Só após cirurgia",
                                                "Só com laudo médico e ação judicial",
                                                "Sim, diretamente no cartório, sem cirurgia ou laudo",
                                                "Não é permitido"),
                                        2, "Em 2018, o STF (ADI 4275) garantiu a alteração diretamente em cartório, sem cirurgia, laudo ou processo judicial."),
                                new Pergunta("Qual atitude demonstra respeito à diversidade?",
                                        alt("Fazer piadas sobre a orientação de alguém",
                                                "Usar o nome e os pronomes com que a pessoa se identifica",
                                                "Perguntar detalhes íntimos sobre o corpo da pessoa",
                                                "Ignorar quando alguém é ofendido"),
                                        1, "Respeitar o nome social e os pronomes é uma forma simples e importante de acolher.")
                        )),

                new Tema("racismo", "Discriminação Racial",
                        "Igualdade racial e combate ao racismo.",
                        Arrays.asList(
                                new Pergunta("Qual lei institui o Estatuto da Igualdade Racial?",
                                        alt("Lei 11.340/2006", "Lei 12.288/2010", "Lei 12.737/2012", "Lei 13.104/2015"),
                                        1, "O Estatuto da Igualdade Racial é a Lei 12.288/2010."),
                                new Pergunta("O que mudou com a Lei 14.532/2023 em relação à injúria racial?",
                                        alt("Deixou de ser crime",
                                                "Passou a ser só contravenção",
                                                "Foi equiparada ao crime de racismo",
                                                "Passou a valer apenas com testemunhas"),
                                        2, "A injúria racial foi equiparada ao racismo e passou a ser inafiançável e imprescritível."),
                                new Pergunta("Onde está previsto que o racismo é crime inafiançável e imprescritível?",
                                        alt("Na Constituição Federal", "No Código Civil", "No Estatuto da Criança", "Na CLT"),
                                        0, "A Constituição Federal, no art. 5º, inciso XLII, prevê isso."),
                                new Pergunta("Xingar alguém pela cor da pele nas redes sociais:",
                                        alt("Não é crime, pois é na internet",
                                                "É crime e pode ser denunciado",
                                                "Só viola os termos de uso da rede",
                                                "Só é crime se acontecer várias vezes"),
                                        1, "Ofensas raciais na internet também são crime. Guarde prints e links e denuncie."),
                                new Pergunta("As ações afirmativas, como as cotas, são:",
                                        alt("Proibidas pela Constituição",
                                                "Formas de promover igualdade de oportunidades",
                                                "Um tipo de discriminação",
                                                "Permitidas só em empresas privadas"),
                                        1, "O Estatuto da Igualdade Racial prevê ações afirmativas para reduzir desigualdades históricas.")
                        )),

                new Tema("seguranca_digital", "Segurança Digital",
                        "Crimes cibernéticos e como se proteger.",
                        Arrays.asList(
                                new Pergunta("Invadir o celular de outra pessoa para obter dados é crime previsto em qual lei?",
                                        alt("Lei Maria da Penha", "Lei Carolina Dieckmann", "Estatuto da Igualdade Racial", "Lei do Feminicídio"),
                                        1, "A Lei 12.737/2012 (Lei Carolina Dieckmann) criou o crime de invasão de dispositivo (art. 154-A do Código Penal)."),
                                new Pergunta("Divulgar fotos íntimas de alguém sem consentimento:",
                                        alt("Não é crime se a pessoa enviou a foto",
                                                "É crime previsto no Código Penal",
                                                "Só é crime se houver cobrança de dinheiro",
                                                "É permitido em grupos privados"),
                                        1, "É crime pelo art. 218-C do Código Penal, mesmo que a vítima tenha enviado a imagem."),
                                new Pergunta("Perseguir alguém repetidamente pela internet, ameaçando ou invadindo sua privacidade, é:",
                                        alt("Liberdade de expressão", "Crime de perseguição (stalking)", "Apenas falta de educação", "Permitido entre ex-parceiros"),
                                        1, "A perseguição, inclusive virtual, é crime pelo art. 147-A do Código Penal."),
                                new Pergunta("Ao sofrer um crime virtual, o que você deve guardar como prova?",
                                        alt("Nada, é melhor apagar tudo",
                                                "Prints com data e hora, links e o perfil do autor",
                                                "Apenas a lembrança do que aconteceu",
                                                "Só a resposta que você enviou"),
                                        1, "Prints, URLs e dados do perfil ajudam na investigação. Não apague as conversas."),
                                new Pergunta("Qual destas práticas ajuda a evitar a invasão das suas contas?",
                                        alt("Usar a mesma senha em tudo",
                                                "Ativar a verificação em duas etapas",
                                                "Compartilhar a senha com amigos",
                                                "Clicar em links recebidos de desconhecidos"),
                                        1, "A verificação em duas etapas cria uma camada extra de proteção, mesmo se a senha vazar.")
                        )),

                new Tema("como_ajudar", "Como Ajudar e Apoiar",
                        "Saiba agir ao presenciar uma situação de violência.",
                        Arrays.asList(
                                new Pergunta("Uma amiga conta que sofre violência. Qual é a melhor atitude?",
                                        alt("Dizer que ela exagera",
                                                "Ouvir sem julgar e oferecer apoio e informação",
                                                "Contar para todos os conhecidos",
                                                "Confrontar o agressor sozinho"),
                                        1, "Acolher sem julgamento e apresentar os canais de ajuda fortalece a vítima."),
                                new Pergunta("Quem pode denunciar uma situação de violência doméstica?",
                                        alt("Somente a vítima",
                                                "Somente parentes",
                                                "Qualquer pessoa, inclusive de forma anônima",
                                                "Somente a polícia"),
                                        2, "Qualquer pessoa pode denunciar, por exemplo pelo 180 ou por este app, inclusive anonimamente."),
                                new Pergunta("Para que serve o Disque 100?",
                                        alt("Emergências médicas",
                                                "Denúncias de violações de direitos humanos",
                                                "Informações de trânsito",
                                                "Atendimento bancário"),
                                        1, "O Disque 100 recebe denúncias de violações contra crianças, idosos, pessoas LGBTQIA+, pessoas com deficiência e outros grupos."),
                                new Pergunta("Em uma situação de perigo imediato, qual número você deve ligar?",
                                        alt("190", "180", "100", "136"),
                                        0, "O 190 é o número da Polícia Militar para emergências."),
                                new Pergunta("Qual serviço público atende pessoas em situação de violação de direitos?",
                                        alt("CREAS", "Receita Federal", "Detran", "Cartório eleitoral"),
                                        0, "O CREAS (Centro de Referência Especializado de Assistência Social) atende pessoas que tiveram direitos violados.")
                        ))
        );
    }
}
