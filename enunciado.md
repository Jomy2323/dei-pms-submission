# PMS: Exercício Prático BL27/2025

O **DEI PhD Management System** (ou _PMS_) é uma plataforma interna do Departamento de Engenharia Informática (DEI) que permite a gestão centralizada do processo de acompanhamento de estudantes de doutoramento, facilitando a organização e o controlo dos diferentes fluxos (**workflows**) de trabalho relacionados com as fases da tese e da defesa.

Para melhorar a gestão do percurso dos doutorandos, é necessário o desenvolvimento de um subsistema simples para este efeito: o **PMS: PhD Management System** (o seu exercício prático).

Este subsistema deve permitir a gestão de:

- **Pessoas**, incluindo, para cada uma, pelo menos o nome, IST ID, email, e o seu tipo no sistema (staff, estudante, professor, coordenador, SC (Scientific Committee));
- **Workflows**, organizados em duas secções: workflow de tese e workflow de defesa, com etapas associadas a cada um.


## Workflows

### Workflow de Tese
- **Proposta de Júri Submetida** - Submetido pelo estudante
- **Aprovado pelo SC** - Aprovado pelo SC
- **Presidente do Júri Atribuído** - Atribuído pelo coordenador
- **Documento Assinado** - Assinado pelo coordenador
- **Submetido ao Fenix** - Submetido pelo staff

### Workflow de Defesa
- **Defesa Agendada** - Agendado pelo coordenador
- **Em Revisão** - Se a data atual for posterior à data de agendamento
- **Submetido ao Fenix** - Uma vez que a nota é atribuída pelo coordenador

#### Exemplo de workflows completos

Dá-se o exemplo de um workflow de tese completo:
- O _estudante_, na sua página, escolhe os professores (de um (1) professor a cinco (5) professores) que vão compor o júri da sua tese e submete a proposta, iniciando o workflow de tese com o estado **Proposta de Júri Submetida**.
- O _SC_ vê a proposta e aprova-a (ou não), mudando o estado para **Aprovado pelo SC**.
- O _coordenador_ vê a proposta aprovada e seleciona um dos professores para ser o presidente do júri, mudando o estado para **Presidente do Júri Atribuído**.
- O _coordenador_ faz download do documento (documento pdf que contém informação sobre a tese e o júri), assina-o e faz upload do documento assinado, mudando o estado para **Documento Assinado**.
- O _staff_ confirma ter submetido o documento assinado no Fenix (para efeitos de implementação, esta submissão é apenas um botão que muda o estado, não sendo necessário interação com o Fenix), mudando o estado para **Submetido ao Fenix**.

Uma vez que o workflow de tese está completo, o workflow de defesa pode começar:
- O _coordenador_ agenda a defesa, iniciando o workflow de defesa com o estado **Defesa Agendada**.
- Quando a data atual é posterior à data de agendamento, o estado muda para **Em Revisão**.
- O _coordenador_ atribui uma nota ao estudante, mudando o estado para **Submetido ao Fenix**.

## Implementação Obrigatória

O objetivo deste exercício é desenvolver o subsistema **PMS** usando [Spring Boot](https://spring.io/projects/spring-boot) (backend) e [Vue.js](https://vuejs.org/) (frontend).

O subsistema **PMS** deverá permitir, pelo menos:

- Visualizar todas as pessoas do DEI numa tabela de consulta rápida, contendo pelo menos nome, IST ID, email e tipo;
- Adicionar novas pessoas ao sistema;
- Atualizar e remover pessoas existentes;
- Atribuir papéis (roles) no frontend: staff, estudante, professor, coordenador, SC;
- Visualizar todos os alunos numa tabela de consulta rápida, contendo pelo menos nome, IST ID, email e estado do workflow de tese/defesa;
- Filtrar alunos por estado do workflow de tese/defesa ou outro campo relevante;
- Visualizar a página individual de cada aluno, incluindo:
  - Secção de informações pessoais;
  - Workflow de tese;
  - Workflow de defesa;
- Atualizar estados dos workflows conforme as etapas descritas;
- Reverter estados dos workflows conforme as etapas descritas;

Para simplificar a implementação, não é necessário implementar autenticação de utilizadores no backend. Apenas no frontend, deve ser possível (estando já implementado) selecionar o tipo de utilizador (staff, estudante, professor, coordenador, SC) e, com base nesse tipo, visualizar ou não certas funcionalidades.

A solução que cumpra os requisitos descritos acima será avaliada com a **cotação máxima de 14 valores**.


### Funcionalidades Adicionais para Notas Superiores

Para alcançar uma **nota até 17 valores**, deve implementar **uma** das seguintes funcionalidades adicionais:

  - Página de estatísticas dos dados do sistema, como por exemplo o número de alunos por estado do workflow de tese/defesa, notas médias, etc.;
  - Logs detalhados de ações feitas no sistema, como submissões, aprovações e atualizações nos workflows.

Para alcançar a **nota máxima de 20 valores**, deve implementar **ambas** as funcionalidades mencionadas acima.

---


O sistema será utilizado por membros do DEI e deve proporcionar uma interface intuitiva e funcional para a gestão do percurso académico dos estudantes de doutoramento.

É permitida e valorizada (mas **não obrigatória**) a implementação de funcionalidades adicionais às acima, especialmente se considerar que algum aspeto melhoraria significativamente a _User Experience (UX)_ durante a utilização do sistema (por exemplo, uma homepage, envio de [emails](https://github.com/tweedegolf/mailcrab), etc.).

É-lhe fornecido **código base** que deve analisar e utilizar como ponto de partida, estando algumas das funcionalidades acima já parcial ou integralmente implementadas. Será penalizada qualquer falta de validação de input do utilizador.

Alguns aspetos do código base poderão não estar de acordo com as melhores práticas de desenvolvimento, pelo que é sugerido que use o seu discernimento para melhorar o código existente, se assim o entender. Um exemplo de uma boa prática poderá ser criação de componentes reutilizáveis para código repetido.

Deve realizar o exercício de forma modular. Serão valorizadas qualidade e estética do código e da interface web apresentada.

Dúvidas sobre os requisitos do exercício devem ser esclarecidas por email.

_Note-se que a situação descrita neste enunciado foi simplificada e não retrata necessariamente a realidade. A sua solução será usada apenas para fins de avaliação._

## Submissão

É obrigatório e alvo de avaliação a utilização de um sistema de controlo de versões (especificamente, [git](https://git-scm.com/) durante o desenvolvimento do exercício. Deve submeter a sua solução no repositório privado que lhe foi atribuído para o efeito, na instância de [GitLab da RNL](https://gitlab.rnl.tecnico.ulisboa.pt/), e que inicialmente contém o código base, do qual deve partir. Não é necessária a utilização de nenhuma funcionalidade específica do GitLab (e.g., _Issues_ e _Merge Requests_), apenas da ferramenta `git` em si. Assim, ser-nos-á possível validar a sua capacidade de utilização desta ferramenta, podendo até ser-lhe benéfico durante a componente de entrevista.

Deve incluir, junto da sua solução, um ficheiro `README` que descreva o procedimento para iniciar um servidor local de testes. Pode incluir nesse ficheiro `README` qualquer informação que considere relevante para a avaliação do seu trabalho, tal como alguma eventual justificação de decisões tomadas. Não submeta artefactos irrelevantes (e.g., pasta `node_modules`).

Deve incluir também um ficheiro `populate.sql` com um _dump_ da base de dados que permita popular a base de dados com dados para testes e avaliação. Este ficheiro pode ser gerado usando [`pg_dump`](https://www.postgresql.org/docs/current/app-pgdump.html), por exemplo com o comando `pg_dump -U <USER> -p <PORT> > populate.sql`.

O exercício prático tem a **nota miníma de 10 valores**. Soluções com nota inferior a 10 valores serão reprovadas.

**Prazo máximo de entrega: domingo, dia 16 de Março de 2025, às 12h00 (hora de Lisboa).**

Não serão aceites quaisquer entregas após o prazo limite, por qualquer razão. Qualquer candidato que não entregue uma solução até ao prazo limite será automaticamente avaliado com a cotação de 0 (zero) valores na componente de seleção correspondente ao exercício prático.

**Boa sorte!**

## Recursos Potencialmente Úteis

- [https://vuetifyjs.com/en/](https://vuetifyjs.com/en/)
- [https://spring.io/guides](https://spring.io/guides)
- [https://www.javatpoint.com/spring-boot-architecture](https://www.javatpoint.com/spring-boot-architecture)

