Aqui está a História de Usuário detalhada para o desenvolvimento da API de Ingestão. Ela foi estruturada para ser entregue a um desenvolvedor, cobrindo todos os aspectos técnicos que discutimos e aplicando o aprendizado adquirido com a configuração do Docker/GraalVM.

---

### **História de Usuário: API Serverless de Ingestão de Feedbacks**

**Título:** Desenvolvimento da API de Ingestão de Feedbacks (Síncrona) com Quarkus e AWS Lambda via Container Image.

**Como um:** Desenvolvedor Backend / Arquiteto
**Eu quero:** Criar uma função Lambda exposta via API Gateway que receba os feedbacks dos alunos via HTTP POST
**Para que:** Os dados sejam validados, persistidos rapidamente no banco de dados e um evento seja disparado para processamento assíncrono, garantindo alta disponibilidade e baixa latência para o usuário.

---

### **Critérios de Aceitação (AC)**

#### **AC 1: Setup do Projeto Quarkus e Extensões**
1.  **Novo Módulo:** Deve ser criado um novo módulo Maven no monorepo chamado `feedback-ingestion-api`.
2.  **Extensões Obrigatórias (`pom.xml`):**
    *   `io.quarkus:quarkus-amazon-lambda-http`: **Crucial.** Diferente do processador, esta função deve usar a extensão HTTP para permitir o uso de JAX-RS (RESTEasy) sobre Lambda.
    *   `io.quarkus:quarkus-resteasy-reactive-jackson`: Para criação dos endpoints REST e serialização JSON.
    *   `io.quarkus:quarkus-amazon-dynamodb`: Para persistência.
    *   `io.quarkus:quarkus-amazon-sqs`: Para envio de mensagens.
    *   `io.quarkus:quarkus-hibernate-validator`: Para validação dos dados de entrada (ex: nota entre 0 e 10).
3.  **Configuração:** O `application.properties` deve ser configurado para não iniciar servidores HTTP locais quando rodar em modo Lambda, delegando isso para a extensão `amazon-lambda-http`.

#### **AC 2: Implementação da Lógica de Negócio (JAX-RS)**
1.  **Rota:** Implementar um recurso REST respondendo em `POST /avaliacao`.
2.  **Contrato de Entrada (JSON):**
    ```json
    { "descricao": "Texto do feedback", "nota": 8 }
    ```
3.  **Fluxo de Execução:**
    *   Receber e validar o objeto.
    *   Gerar um UUID para o `feedbackId`.
    *   Gerar um Timestamp ISO-8601 para `dataEnvio`.
    *   Definir status inicial como `PENDENTE`.
    *   **Passo A (Persistência):** Salvar o objeto completo na tabela DynamoDB `Feedbacks`.
    *   **Passo B (Evento):** Enviar uma mensagem para a fila SQS `FeedbackSubmittedQueue` contendo apenas o ID: `{"feedbackId": "uuid-gerado"}`.
4.  **Resposta:** Retornar status HTTP `201 Created` com o corpo contendo o feedback criado (incluindo o ID gerado).

#### **AC 3: Containerização (Docker Multi-stage)**
1.  Criar um `Dockerfile` na raiz do módulo `feedback-ingestion-api`.
2.  **Estágio de Build:** Usar a imagem `quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21`. Deve copiar o `pom.xml` e baixar dependências antes de copiar o código fonte (otimização de cache).
3.  **Compilação:** Executar `mvn package -Pnative` dentro do container.
4.  **Estágio Final:** Usar a imagem `public.ecr.aws/lambda/provided:al2023`. Copiar o executável gerado (`*-runner`) para `/var/runtime/bootstrap`.

#### **AC 4: Infraestrutura como Código (AWS SAM)**
1.  Adicionar o recurso `FeedbackIngestionFunction` ao arquivo `template.yaml`.
2.  **Configuração da Função:**
    *   `PackageType`: `Image`.
    *   `MemorySize`: Iniciar com 256MB (ou 512MB para otimizar CPU no cold start).
    *   `Metadata`: Apontar para o `Dockerfile` do novo módulo.
3.  **Gatilho (Event Source):**
    *   Tipo: `HttpApi` (API Gateway V2).
    *   Path: `/avaliacao`.
    *   Method: `POST`.
4.  **Permissões (Policies):**
    *   Permissão de **Escrita** (`dynamodb:PutItem`) na tabela `Feedbacks`.
    *   Permissão de **Envio** (`sqs:SendMessage`) na fila `FeedbackSubmittedQueue`.

#### **AC 5: Configuração do API Gateway**
1.  O recurso `AWS::Serverless::HttpApi` deve ser definido no template.
2.  Deve estar preparado para receber configurações de CORS (permitir requisições de qualquer origem `*` para desenvolvimento).
3.  *(Nota: A integração com Cognito será feita em uma tarefa subsequente, mas a estrutura da API deve ser criada agora).*

---

### **Definition of Done (DoD)**

*   [ ] O código compila localmente e os testes unitários passam.
*   [ ] O comando `sam build` executa com sucesso, gerando a imagem Docker com Java 21 Nativo.
*   [ ] O comando `sam deploy` atualiza a stack na AWS com sucesso, criando o endpoint do API Gateway e a nova função Lambda.
*   [ ] **Teste de Integração Manual:** Uma requisição `POST` enviada via Postman/Curl para a URL do API Gateway retorna `201 Created`.
*   [ ] **Verificação de Dados:**
    *   O item aparece corretamente na tabela DynamoDB (com todos os campos).
    *   Uma mensagem aparece na fila SQS.
    *   (Se o processador estiver ativo) O fluxo completo roda: Ingestão -> Banco -> SQS -> Processador -> Email.
*   [ ] O Cold Start observado no CloudWatch Logs é aceitável (< 800ms).

---

### **Dicas Técnicas para o Desenvolvedor**

*   **Injeção de Dependência:** Use `@Inject` para injetar o `DynamoDbClient` e o `SqsClient`. O Quarkus gerencia a criação desses clientes automaticamente.
*   **Variáveis de Ambiente:** Lembre-se de passar o nome da fila e da tabela via variáveis de ambiente no `template.yaml` e lê-las no código Java via `ConfigProperty`.
*   **Execução Local:** Use `sam local start-api` para subir um servidor local que simula o API Gateway e testa a Lambda via Docker na sua máquina antes do deploy.