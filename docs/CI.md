# Esteira de CI do AMPARA (GitHub Actions)

## Como rodar localmente

```bash
./gradlew testDebugUnitTest        # Linux/macOS
gradlew.bat testDebugUnitTest      # Windows
```

É o **mesmo comando** usado no job `testes-unitarios` da esteira.
Relatório HTML: `app/build/reports/tests/testDebugUnitTest/index.html`.

Semgrep local (opcional, precisa de Python ou Docker):

```bash
pip install semgrep
semgrep scan --config p/java --config p/owasp-top-ten --severity ERROR --error
```

## O que foi testado

| Funcionalidade / regra de negócio | Classe testada | Sucesso | Erro / borda |
|---|---|---|---|
| Senha forte no cadastro | `ValidacaoUtils.senhaForte` | senha completa; exatamente 8 caracteres | 7 caracteres, sem especial, sem maiúscula, nulo/vazio |
| Telefone do contato de confiança (denúncia anônima) | `ValidacaoUtils.telefoneValido` | celular (11) e fixo (10) | 9 dígitos, mais de 11, nulo, letras |
| Protocolo da denúncia | `ValidacaoUtils.gerarProtocolo` / `protocoloValido` | `AMP-2026-A1B2C` | id curto/nulo e ano inválido (exceção), formatos errados |
| Campos obrigatórios da denúncia | `ValidacaoUtils.camposObrigatoriosDenuncia` | denúncia completa | relato vazio ou só espaços, tipo nulo |
| Quiz educativo | `QuizData` | busca de tema existente | tema inexistente (nulo); toda pergunta com 4 alternativas e índice 0..3 |
| Rede de apoio | `Instituicao.getCidadeUf` | "Apucarana - PR" | sem UF, sem cidade |

Total: 26 casos de teste em 3 classes de teste.

As regras de validação estavam dentro das Activities (`RegisterActivity`, `DenunciaActivity`),
que dependem do Android e não rodam em teste unitário comum. Elas foram **extraídas para
`util/ValidacaoUtils.java`** (Java puro) e as Activities passaram a chamar essa classe.
Assim o comportamento do app é o mesmo e a regra fica testável.

## Perguntas prováveis na arguição

**O que é CI (Integração Contínua)?**
Prática de integrar o código no repositório com frequência e, a cada integração, rodar automaticamente
build, testes e análises. O objetivo é descobrir erros cedo, perto de quem os causou.

**Qual a diferença entre CI e CD?**
CI valida o código (build + testes + análise). CD (Entrega/Implantação Contínua) leva o que passou na CI
até um ambiente (homologação/produção ou loja de apps). Esta esteira faz só CI.

**Quando a esteira dispara? (`on:`)**
Em `push` na `main`, em `pull_request` para a `main` e manualmente (`workflow_dispatch`).

**O que é um job? E um step?**
Job é um conjunto de passos que roda numa máquina própria (runner). Steps são os passos em sequência
dentro do job. Jobs diferentes rodam **em paralelo** por padrão, e um não enxerga os arquivos do outro.

**Por que testes e Semgrep ficaram em jobs separados?**
Exigência da atividade e boa prática: rodam em paralelo (mais rápido), cada um tem seu status (fica claro
se falhou o teste ou a segurança) e um não depende do outro. Se quisesse ordem, usaria `needs:`.

**O que é `runs-on: ubuntu-latest`?**
O tipo de máquina virtual (runner) hospedada pelo GitHub onde o job roda. Ela já vem com o Android SDK.

**O que faz `actions/checkout`?**
Baixa o código do repositório para dentro do runner. Sem ele a máquina começa vazia.

**Por que `setup-java` com Java 21?**
O Gradle do projeto (9.4.1 / AGP 9.2.1) precisa de JDK 17+; o projeto declara Java 21 em
`gradle/gradle-daemon-jvm.properties`. `temurin` é a distribuição do OpenJDK.

**Para que serve `gradle/actions/setup-gradle`?**
Configura o Gradle e faz cache das dependências, então as próximas execuções ficam mais rápidas.

**Por que `chmod +x gradlew`?**
O `gradlew` estava salvo no Git sem permissão de execução; no Linux do runner ele não rodaria.

**Como a esteira fica vermelha quando um teste falha?**
Pelo **código de saída** (exit code). Se algum teste falha, o Gradle termina com código diferente de 0;
o step falha, o job falha e o workflow fica vermelho. Não precisa de nenhuma configuração extra.

**E o Semgrep, como faz a esteira falhar?**
Com `--error`: se houver qualquer achado, o Semgrep sai com código 1. O `--severity ERROR` filtra só
os achados de severidade máxima. O Semgrep usa os níveis INFO, WARNING e ERROR; o ERROR faz o papel de
"crítico" pedido na atividade. Achados WARNING/INFO aparecem no log mas não quebram a esteira.

**Por que `if: always()` no upload do relatório?**
Por padrão um step só roda se os anteriores passaram. Com `always()` o relatório é publicado mesmo
quando os testes falham, que é justamente quando ele é mais útil.

**O que é SAST? Qual a diferença para teste unitário?**
SAST (Static Application Security Testing) analisa o **código-fonte sem executá-lo**, procurando padrões
inseguros (SQL injection, criptografia fraca, segredos no código etc.). O teste unitário **executa** o
código e verifica se o comportamento está certo. Um verifica segurança/qualidade, o outro, a função.

**O que são `p/java` e `p/owasp-top-ten`?**
Conjuntos de regras prontos do registro do Semgrep: regras para Java e regras ligadas às 10 categorias
de risco mais comuns em aplicações segundo a OWASP.

**Por que rodar o Semgrep num `container`?**
A imagem oficial `semgrep/semgrep` já vem com a ferramenta instalada; não precisa instalar nada.

**Para que serve o `.semgrepignore`?**
Para não analisar o que não é código do projeto: pasta da IDE, builds, o APK de release, PDFs.

**O que é `permissions: contents: read`?**
Dá ao token do workflow só permissão de leitura (princípio do menor privilégio): a esteira não precisa
escrever no repositório.

**O que é teste unitário? Por que JUnit?**
Testa a menor unidade (um método/classe) de forma isolada, sem banco, rede ou tela. JUnit 4 já estava
nas dependências do projeto (`testImplementation(libs.junit)`).

**Por que não testaram as Activities ou o Firebase?**
Dependem do Android e de serviços externos; isso seria teste instrumentado ou de integração
(`androidTest`), que precisa de emulador. Por isso a regra de negócio foi separada em Java puro.

**O que é caminho feliz e caso de borda?**
Caminho feliz: entrada válida, resultado esperado (senha correta é aceita). Borda: valores no limite
ou inválidos (senha com 7 vs 8 caracteres, telefone com 9 dígitos, entrada nula, exceção).

**Como funciona o `@Test(expected = IllegalArgumentException.class)`?**
O teste só passa se o método **lançar** essa exceção; é como se testa um cenário de erro esperado.

**Onde vejo o resultado?**
Aba **Actions** do repositório → execução → cada job com seu log. Em pull requests, o status aparece
na própria PR. O relatório de testes fica em "Artifacts".
