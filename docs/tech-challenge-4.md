Desafio da disciplina
Boas-vindas ao Tech Challenge da fase 4! Este desafio é fundamental para consolidar os conhecimentos
obtidos ao longo da fase. Nosso desafio focará em Cloud Computing, Serverless e Deploy de Aplicações em
ambiente de nuvem. O projeto proposto envolve a criação de uma plataforma de feedback, onde os estudantes
podem avaliar as aulas e os administradores podem ter acesso a relatórios e análises desses feedbacks. Esta
atividade será desenvolvida em grupo, com prazo de entrega determinado e com impacto direto na nota final.
Problema
Para garantir a qualidade dos cursos on-line, é essencial que os estudantes possam fornecer feedbacks e que
os administradores possam acompanhar rapidamente a satisfação dos alunos. O sistema deverá ser capaz de
receber feedbacks, enviar notificações para itens críticos e gerar relatórios periódicos para auxiliar na análise
dos dados.
Objetivo
O objetivo é desenvolver uma aplicação hospedada em um ambiente de nuvem, com funções serverless para
automatizar o recebimento de feedbacks, o envio de notificações e a geração de relatórios. Como
estamos em um ambiente com créditos de cloud computing limitados, será solicitado que vocês gravem um
vídeo demonstrando o sistema em funcionamento.
Requisitos
1. Ambiente de nuvem configurado e funcionando, com configurações de segurança relacionados aos dados
   de clientes, e com governança de acesso
2. Configuração dos componentes de suporte (bancos de dados, etc.)
3. Deploy automatizado dos componentes atualizáveis (ex: funções)
4. Aplicação monitorada
5. Notificações automáticas aos administradores para problemas críticos
6. Relatório semanal dos feedbacks, com media de avaliações
   Artefatos de entrega
1. Repositório aberto com o código-fonte do projeto.
2. Vídeo de demonstração mostrando a aplicação em funcionamento, as funções serverless ativas, e as
   configurações do projeto.
   Avaliação
   A avaliação será baseada nos seguintes critérios:
   • Explicação do modelo de cloud escolhido e dos componentes envolvidos na solução
   • Funcionamento correto da aplicação
   • Qualidade do código, com documentação
   • Descrição do projeto com:
   ‣ Arquitetura da solução
   ‣ Instruções de deploy
   ‣ Configuração do monitoramento
   ‣ Documentação das funções criadas
   • Configuração do ambiente de nuvem e funções serverless, com explicações sobre:
   ‣ o modelo escolhido e,
   ‣ configurações de segurança
   Referências
   Endpoints de entrada
   POST /avaliacao
   {
   "descricao": string,
   "nota": int (0 a 10),
   }
   Dados para o e-mail de aviso de urgência
   • Descrição
   • Urgência
   • Data de envio
   Dados para o relatório semanal
   • Descrição
   • Urgência
   • Data de envio
   • Quantidade de avaliações por dia
   • Quantidade de avaliações por urgência