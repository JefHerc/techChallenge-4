# Tech Challenge 4 - Processador de Eventos e Notificação (Assíncrono)

Este repositório contém a implementação da parte 3 do sistema: um processador assíncrono que lê mensagens da SQS com o ID de um feedback, consulta os dados no DynamoDB e, caso a nota seja crítica (<= 2), envia um e-mail de alerta via Amazon SES.

## Módulo criado
- feedback-notification-processor (Quarkus + AWS Lambda)
  - Handler: `NotificationProcessorFunction` (SQS → DynamoDB → SES)
  - Regras de negócio: nota crítica quando `nota <= 2`
  - Tratamento de erros conforme AC dos docs

## Dependências principais
- quarkus-amazon-lambda
- quarkus-amazon-sqs
- quarkus-amazon-dynamodb
- quarkus-amazon-ses
- quarkus-jsonb

## Configuração
Arquivo `feedback-notification-processor/src/main/resources/application.properties`:
```
feedback.dynamodb.table-name=Feedbacks
email.admin.address=gustavo.jog+admin@hotmail.com
email.source.address=gustavo.jog+noreply@hotmail.com
quarkus.lambda.handler=notificationProcessor
```

## Infraestrutura como Código (AWS SAM)
Arquivo `template.yaml` inclui:
- Fila SQS `FeedbackSubmittedQueue` com DLQ `FeedbackFailedQueue` (maxReceiveCount=3)
- Função Lambda `NotificationProcessorFunction` com gatilho SQS (BatchSize=1)
- Permissões mínimas (SQS receive/delete/get attributes, DynamoDB GetItem, SES SendEmail, CloudWatch Logs)

## Build e Deploy (resumo)
1. Build do módulo Quarkus (gera artefato Zip compatível com Lambda):
   - `mvn -f feedback-notification-processor/pom.xml -DskipTests package`
   - O CodeUri do SAM aponta para `feedback-notification-processor/target/function.zip`
2. Deploy via SAM:
   - `sam build`
   - `sam deploy --guided`

## Teste manual ponta a ponta
1. Insira um item na tabela DynamoDB `Feedbacks` com os atributos principais (`FeedbackID`, `nota`, `descricao`, `dataEnvio`).
2. Envie uma mensagem na fila `FeedbackSubmittedQueue` com o corpo JSON: `{ "feedbackId": "<id do item>" }`.
3. Se `nota <= 2`, verifique o recebimento do e-mail no endereço do admin. Se `nota > 2`, nenhum e-mail deve ser enviado.

## Logs esperados (CloudWatch)
- INFO: "Iniciando processamento de Lote com X mensagens."
- INFO: "Processando feedback ID: [feedbackId]."
- INFO: "Feedback [feedbackId] com nota [nota] não é crítico. Ignorando."
- INFO: "Feedback crítico detectado [feedbackId]. Enviando notificação para [email_admin]."
- ERROR: "Feedback com ID [feedbackId] não encontrado na tabela DynamoDB."
- ERROR: "Falha ao enviar e-mail para feedback [feedbackId]: [mensagem_de_erro]."

Consulte os arquivos em `docs/` para a descrição completa da arquitetura e critérios de aceitação.
