Aqui está a História de Usuário completa, estruturada para guiar o desenvolvimento da camada de segurança da sua API de Ingestão.

---

### **História de Usuário: Proteção da API de Ingestão com JWT e Cognito**

**Título:** Implementar Autenticação JWT via Amazon Cognito na API de Ingestão de Feedbacks

**Como um:** Administrador do Sistema / Desenvolvedor de Segurança
**Eu quero:** Restringir o acesso ao endpoint `POST /avaliacao` para que apenas usuários autenticados possam enviar feedbacks
**Para que:** Eu possa prevenir spam, garantir a integridade dos dados e identificar qual aluno enviou cada avaliação.

---

### **Critérios de Aceitação (AC)**

1.  **Infraestrutura de Identidade:**
    *   Um **User Pool** do Amazon Cognito deve ser criado para gerenciar os usuários.
    *   Um **App Client** deve ser configurado no Cognito para permitir fluxos de autenticação (sem *client secret* para facilitar integração futura com frontends web).
2.  **Proteção do Gateway:**
    *   O API Gateway (HTTP API) deve negar qualquer requisição que não possua um cabeçalho `Authorization`.
    *   O API Gateway deve validar a assinatura e expiração do Token JWT emitido pelo Cognito.
3.  **Identificação no Backend:**
    *   A função Lambda deve extrair o ID do usuário (campo `sub` do token) e salvá-lo junto com o feedback no DynamoDB.
4.  **CORS:**
    *   A API deve permitir o cabeçalho `Authorization` nas configurações de CORS.

---

### **Tarefas Técnicas (Passo a Passo de Implementação)**

Aqui está o guia detalhado do que precisa ser alterado no código e na infraestrutura.

#### **Tarefa 1: Atualizar o `template.yaml` (Infraestrutura)**

Adicione os recursos do Cognito e configure o Authorizer na API.

1.  **Adicione ao final de `Resources`:**
    ```yaml
    # 1. O Banco de Usuários
    FeedbackUserPool:
      Type: AWS::Cognito::UserPool
      Properties:
        UserPoolName: FeedbackUsers
        AutoVerifiedAttributes: [email]
        UsernameAttributes: [email]

    # 2. O Cliente da Aplicação (quem pede o token)
    FeedbackUserPoolClient:
      Type: AWS::Cognito::UserPoolClient
      Properties:
        ClientName: FeedbackAppClient
        UserPoolId: !Ref FeedbackUserPool
        GenerateSecret: false
        ExplicitAuthFlows:
          - ALLOW_USER_PASSWORD_AUTH
          - ALLOW_REFRESH_TOKEN_AUTH
          - ALLOW_ADMIN_USER_PASSWORD_AUTH # Vital para testes via CLI
    ```

2.  **Modifique o recurso `FeedbackApi` existente:**
    ```yaml
    FeedbackApi:
      Type: AWS::Serverless::HttpApi
      Properties:
        # Adicione 'Authorization' aos headers permitidos
        CorsConfiguration:
          AllowOrigins: ["*"]
          AllowMethods: [GET, POST, OPTIONS]
          AllowHeaders: ["Content-Type", "Authorization"] 
        
        # Configure o Authorizer
        Auth:
          DefaultAuthorizer: CognitoJwtAuthorizer
          Authorizers:
            CognitoJwtAuthorizer:
              IdentitySource: "$request.header.Authorization"
              JwtConfiguration:
                issuer: !Sub "https://cognito-idp.${AWS::Region}.amazonaws.com/${FeedbackUserPool}"
                audience:
                  - !Ref FeedbackUserPoolClient
    ```

3.  **Adicione aos `Outputs` (para facilitar o teste):**
    ```yaml
    CognitoUserPoolId:
      Value: !Ref FeedbackUserPool
    CognitoClientId:
      Value: !Ref FeedbackUserPoolClient
    ```

#### **Tarefa 2: Atualizar o Código Java (Quarkus)**

Modifique a aplicação `feedback-ingestion-api` para capturar e salvar quem é o usuário.

1.  **Atualizar o DTO/Modelo (`Feedback.java`):**
    Adicione um campo para armazenar o ID do usuário.
    ```java
    public class Feedback {
        // ... outros campos
        private String userId; // Novo campo
        // getters e setters
    }
    ```

2.  **Atualizar o Recurso (`FeedbackResource.java`):**
    Use o `SecurityContext` para pegar os dados do token validado pelo API Gateway.

    ```java
    import jakarta.ws.rs.core.Context;
    import jakarta.ws.rs.core.SecurityContext;
    import java.security.Principal;

    @POST
    @Path("/avaliacao")
    public Response criarAvaliacao(FeedbackRequest request, @Context SecurityContext sec) {
        // O API Gateway passa o 'sub' (ID do usuário) como o Principal
        Principal user = sec.getUserPrincipal();
        String userId = (user != null) ? user.getName() : "anonymous";

        // Passar o userId para o serviço/repositório
        Feedback feedback = service.salvar(request, userId);
        
        return Response.status(201).entity(feedback).build();
    }
    ```

3.  **Atualizar o Repositório (`FeedbackRepository.java`):**
    Garanta que o `userId` seja incluído no mapa de persistência do DynamoDB.
    ```java
    item.put("userId", AttributeValue.builder().s(feedback.getUserId()).build());
    ```

#### **Tarefa 3: Build e Deploy**

1.  Reconstrua a imagem Docker com as mudanças no Java:
    ```bash
    sam build
    ```
2.  Faça o deploy das mudanças de infraestrutura e código:
    ```bash
    sam deploy
    ```

---

### **Roteiro de Validação (Teste Manual)**

Após o deploy, siga estes passos para validar a história:

1.  **Teste Negativo:** Tente enviar um POST via Postman *sem* o header Authorization.
    *   **Resultado Esperado:** Erro `401 Unauthorized` (A Lambda nem é executada).
2.  **Criação de Usuário:** Crie um usuário no Cognito via AWS CLI ou Console.
3.  **Obtenção de Token:** Use o comando `aws cognito-idp initiate-auth` para fazer login e pegar o `IdToken`.
4.  **Teste Positivo:** Tente enviar um POST via Postman *com* o header `Authorization: Bearer <IdToken>`.
    *   **Resultado Esperado:** Status `201 Created`.
5.  **Validação de Dados:** Vá na tabela DynamoDB e verifique se o item criado possui a coluna `userId` preenchida com o ID do Cognito.