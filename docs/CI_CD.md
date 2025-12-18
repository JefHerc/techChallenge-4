Aqui está uma proposta estruturada de **História de Usuário (User Story)** para a criação do seu pipeline de CI/CD, seguindo as boas práticas de metodologias ágeis.

Você pode adicionar isso ao quadro do seu projeto (Jira, Trello, GitHub Projects).

---

### Título: Automação de Build e Deploy Contínuo (CI/CD) para AWS

**ID:** US-01 (Exemplo)
**Prioridade:** Alta (Bloqueante para entregas futuras)
**Estimativa:** 3 a 5 Story Points

#### 📝 Declaração da História
> **Como** Desenvolvedor do time,
> **Eu quero** que o processo de compilação e implantação da aplicação Quarkus na AWS seja automatizado via GitHub Actions,
> **Para que** eu possa garantir que a versão mais recente do código esteja sempre rodando no ambiente de nuvem sem erros manuais, e para atender ao requisito de "Deploy Automatizado" do Tech Challenge.

---

#### ✅ Critérios de Aceite (Acceptance Criteria)
*Para que esta história seja considerada "Pronta" (Done), os seguintes itens devem ser verdadeiros:*

1.  **Gatilho Automático:** O pipeline deve ser iniciado automaticamente sempre que houver um `git push` na branch `main`.
2.  **Build com Sucesso:** O fluxo deve executar o Maven para compilar o projeto Quarkus e gerar o artefato (`uber-jar` ou zip nativo). Se houver erro de compilação, o pipeline deve falhar e notificar.
3.  **Segurança:** As credenciais da AWS (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`) não devem estar expostas no código, devendo ser consumidas via *GitHub Secrets*.
4.  **Infrastructure as Code (IaC):** O deploy deve utilizar o **AWS SAM** (ou Terraform) para atualizar ou criar os recursos (Lambda, API Gateway, DynamoDB) baseando-se no arquivo `template.yaml`.
5.  **Implantação Verificada:** Ao final da execução com sucesso (sinal verde no GitHub Actions), a API deve estar respondendo a requisições HTTP no ambiente AWS.

---

#### 🛠️ Tarefas Técnicas (Subtasks)
*Passo a passo sugerido para o desenvolvedor executar:*

*   [ ] Criar usuário IAM na AWS com permissões programáticas e gerar as chaves de acesso.
*   [ ] Configurar as chaves (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`) em **Settings > Secrets** do repositório GitHub.
*   [ ] Validar se o arquivo `template.yaml` (SAM) está na raiz do projeto e apontando corretamente para a pasta `target` do Quarkus.
*   [ ] Criar o arquivo de workflow em `.github/workflows/deploy.yml`.
*   [ ] Configurar steps do workflow: Setup Java 17, Maven Build, AWS Credentials, SAM Build e SAM Deploy.
*   [ ] Realizar um commit de teste para validar a execução completa do pipeline.

---

#### 💡 Valor de Negócio
Esta história é fundamental pois elimina a necessidade de "deploys manuais" da máquina do desenvolvedor (o famoso "na minha máquina funciona"), garantindo que o ambiente de avaliação dos professores seja idêntico ao código fonte entregue.

---

## 🔧 Implementação (exercida)
Implementei um workflow de GitHub Actions que automatiza o build e o deploy via **AWS SAM**.

- **Arquivo do workflow:** `.github/workflows/deploy.yml`
- **Gatilho:** `push` na branch `main` ✅
- **Principais steps:**
  - Setup Java 17
  - Build com Maven (`feedback-notification-processor/`)
  - Configurar credenciais AWS via *GitHub Secrets*
  - Login em ECR (quando necessário)
  - Instalar e executar `sam build --use-container` e `sam deploy` (uso `samconfig.toml` para parâmetros)
  - Verificação mínima: `aws lambda get-function --function-name NotificationProcessorFunction` e um `aws lambda invoke` de smoke test

### Segredos necessários (GitHub repository > Settings > Secrets)
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION` (ex.: `sa-east-1`)

> Observação: O `samconfig.toml` deste repositório já contém o `image_repositories` apontando para um repo ECR. Garanta que as credenciais configuradas tenham permissão de push para o ECR e de criação/atualização de stacks (CloudFormation, IAM, Lambda, SQS, DynamoDB, SES).

### Como testar localmente
1. Configure credenciais AWS na sua máquina (perfil com permissões apropriadas).
2. Na raiz do repo, rode:
   - `sam build --use-container`
   - `sam deploy --config-file samconfig.toml --config-env default`
3. Verifique a existência da função: `aws lambda get-function --function-name NotificationProcessorFunction`
4. Opcional: `aws lambda invoke` com um evento de teste para garantir que a função responda.

---
