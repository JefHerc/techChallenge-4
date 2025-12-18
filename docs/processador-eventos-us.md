### **História de Usuário: Notificação de Feedback Crítico**

**Título:** Notificar Administradores sobre Feedbacks Críticos

**Como um(a):** Administrador(a) do Sistema

**Eu quero:** Ser notificado(a) automaticamente por e-mail quando um estudante submete um feedback com avaliação crítica (nota muito baixa)

**Para que eu possa:** Agir rapidamente para resolver o problema, contatar o estudante se necessário e garantir a qualidade dos cursos.

---

### **Critérios de Aceitação (AC)**

#### **AC 1: Funcionalidade Principal do Handler**
1.  **Gatilho:** A função Lambda deve ser acionada exclusivamente por novas mensagens na fila SQS `FeedbackSubmittedQueue`.
2.  **Leitura da Mensagem:** A função deve ser capaz de processar um lote de mensagens (iniciando com `batchSize: 1` para simplificar), extraindo o corpo (body) de cada uma.
3.  **Parsing do ID:** O corpo da mensagem SQS será um JSON contendo o ID do feedback. Ex: `{"feedbackId": "abc-123-def-456"}`. A função deve extrair este valor.
4.  **Busca no Banco de Dados:** Utilizando o `feedbackId`, a função deve consultar a tabela `Feedbacks` no DynamoDB para obter o registro completo do feedback (principalmente `nota` e `descricao`).
5.  **Lógica de Criticidade:** A função deve avaliar se o feedback é crítico. A regra é: `feedback.nota <= 2`.
6.  **Ação Condicional:**
    *   **Se for crítico:** A função deve construir e enviar um e-mail via Amazon SES.
    *   **Se não for crítico:** A função deve apenas registrar um log informando que o feedback foi processado e não era crítico, e então encerrar sua execução para aquela mensagem com sucesso.
7.  **Conteúdo do E-mail:** O e-mail de notificação deve ter um formato claro:
    *   **Assunto:** `Alerta de Feedback Crítico Recebido!`
    *   **Corpo:**
        ```
        Olá, Administrador(a),

        Um novo feedback crítico foi recebido e precisa de sua atenção.

        - ID do Feedback: [feedbackId]
        - Nota: [nota]
        - Descrição: "[descricao]"
        - Data de Envio: [dataEnvio]

        Por favor, analise o mais rápido possível.

        Atenciosamente,
        Sistema de Feedback Automático
        ```

#### **AC 2: Setup do Projeto Quarkus e Dependências**
1.  **Criação do Projeto:** O projeto Quarkus deve ser inicializado (se ainda não existir) com o `artifactId` `feedback-notification-processor`.
2.  **Extensões Necessárias:** O arquivo `pom.xml` deve conter as seguintes extensões Quarkus:
    *   `quarkus-amazon-lambda`: Para a integração base com o AWS Lambda.
    *   `quarkus-amazon-sqs`: Para o cliente SQS.
    *   `quarkus-amazon-dynamodb`: Para o cliente DynamoDB.
    *   `quarkus-amazon-ses`: Para o cliente SES.
    *   `quarkus-jsonb`: Para a serialização/deserialização de objetos Java para JSON.
3.  **Configuração da Aplicação:** O arquivo `application.properties` deve ser configurado para permitir a injeção dos clientes da AWS e parametrizar a lógica.
    ```properties
    # Nome da tabela a ser lida
    feedback.dynamodb.table-name=Feedbacks

    # Configurações de e-mail
    email.admin.address=gustavo.jog+admin@hotmail.com
    email.source.address=gustavo.jog+noreply@hotmail.com
    ```

#### **AC 3: Configuração e Permissões na AWS (Infraestrutura como Código)**
1.  **Gatilho da Lambda:** A função Lambda deve ser configurada na AWS (via AWS SAM ou CDK) para ter um `EventSource` do tipo SQS, apontando para o ARN da fila `FeedbackSubmittedQueue`.
2.  **IAM Role:** A Role de execução da Lambda deve ter as seguintes permissões mínimas (Princípio do Mínimo Privilégio):
    *   **SQS:** `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:GetQueueAttributes` sobre o recurso `FeedbackSubmittedQueue`.
    *   **DynamoDB:** `dynamodb:GetItem` sobre o recurso da tabela `Feedbacks`.
    *   **SES:** `ses:SendEmail` sobre os ARNs das identidades de e-mail verificadas (remetente e, se em sandbox, destinatário).
    *   **CloudWatch Logs:** `logs:CreateLogGroup`, `logs:CreateLogStream`, `logs:PutLogEvents` para permitir a escrita de logs.
3.  **Dead-Letter Queue (DLQ):** A fila `FeedbackSubmittedQueue` deve ser configurada com uma DLQ. Se o processamento de uma mensagem falhar 3 vezes (`maxReceiveCount`), ela deve ser movida para a fila `FeedbackFailedQueue` para análise manual.

#### **AC 4: Tratamento de Erros e Resiliência**
1.  **Feedback Não Encontrado:** Se a função não encontrar um feedback no DynamoDB com o ID fornecido, ela deve registrar um erro (`ERROR`) no CloudWatch, mas **não** deve lançar uma exceção. A mensagem deve ser considerada processada (removida da fila) para não causar retentativas desnecessárias.
2.  **Falha na Comunicação com AWS:** Se ocorrer uma falha ao contatar o DynamoDB ou o SES (ex: erro de permissão, serviço indisponível), a função deve lançar uma `RuntimeException`. Isso sinaliza ao serviço Lambda que a execução falhou, fazendo com que a mensagem **não** seja removida da fila SQS e seja reenviada para uma nova tentativa após o *Visibility Timeout*.
3.  **Erro de Parsing:** Se o corpo da mensagem SQS for um JSON inválido, a função deve capturar a exceção, registrar um erro e finalizar sem lançar a exceção, para que a mensagem "envenenada" vá para a DLQ após as tentativas.

#### **AC 5: Observabilidade e Logging**
1.  **Logs Claros:** A função deve gerar logs estruturados para cada etapa importante:
    *   `INFO`: "Iniciando processamento de Lote com X mensagens."
    *   `INFO`: "Processando feedback ID: [feedbackId]."
    *   `INFO`: "Feedback [feedbackId] com nota [nota] não é crítico. Ignorando."
    *   `INFO`: "Feedback crítico detectado [feedbackId]. Enviando notificação para [email_admin]."
    *   `ERROR`: "Feedback com ID [feedbackId] não encontrado na tabela DynamoDB."
    *   `ERROR`: "Falha ao enviar e-mail para feedback [feedbackId]: [mensagem_de_erro]."

---

### **Definition of Done (DoD)**

*   O código para o handler de eventos foi implementado em Java com Quarkus.
*   A infraestrutura (Lambda, Role IAM, Gatilho SQS) está definida como código em um template `template.yaml` (AWS SAM).
*   A funcionalidade foi testada de ponta a ponta em um ambiente de desenvolvimento na AWS:
    1.  Um item com nota `1` é inserido na tabela.
    2.  Uma mensagem é enviada manualmente para a SQS com o ID do item.
    3.  O e-mail de notificação é recebido com sucesso na caixa de entrada do administrador.
    4.  Um teste com nota `5` é realizado e nenhum e-mail é recebido.
*   O código foi revisado por outro membro da equipe e mergeado na branch principal.