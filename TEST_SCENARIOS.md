# Cenários de Teste (TDD)

Este arquivo descreve os cenários de teste do projeto e indica onde cada teste automatizado está localizado. Novos testes serão implementados com base nas descrições abaixo.

## Localização dos Testes
Todos os testes de componente (UI) estão em: `app/src/androidTest/java/com/fjdias/personalfinance/`
Todos os testes unitários estão em: `app/src/test/java/com/fjdias/personalfinance/`

---

## Componente: Transações

### Cenário 1: Estabilidade do Seletor de Data
**Contexto:** Ao adicionar ou editar uma transação.
**Ação:** Clicar no botão de seleção de data ("Data: ...").
**Resultado Esperado:** O calendário (DatePicker) deve abrir e permanecer visível até que o usuário clique em "OK" ou "Cancelar". Não deve fechar sozinho após a abertura.
**Arquivo de Teste:** `TransactionDateStabilityTest.kt`
**Status:** 🟢 Implementado

### Cenário 2: Edição Completa de Transação
**Contexto:** Na lista de transações.
**Ação:** Clicar em um item da lista, alterar o título para "Gasto Editado" e salvar.
**Resultado Esperado:** O diálogo deve fechar e a lista deve exibir o novo título "Gasto Editado".
**Arquivo de Teste:** `TransactionEditTest.kt`
**Status:** 🟢 Implementado

### Cenário 3: Inserção Manual de Gasto
**Contexto:** Na aba de Transações.
**Ação:** Clicar no botão "+", preencher Título, Valor e escolher uma Categoria (diferente de Renda).
**Resultado Esperado:** A transação deve aparecer na lista com o valor em Vermelho e ser contabilizada como "Saída" no Resumo.
**Arquivo de Teste:** `FinanceBusinessLogicTest.kt` (método `scenario3_testManualExpenseInsertion`)
**Status:** 🟢 Implementado

### Cenário 4: Exportação de Backup (CSV)
**Contexto:** Na barra superior (TopBar).
**Ação:** Clicar no ícone de Exportar, definir um nome e confirmar o salvamento.
**Resultado Esperado:** O sistema deve invocar o seletor de arquivos do Android e gerar um arquivo CSV compatível com a importação do app.
**Arquivo de Teste:** Testado manualmente (Integração com SAF). Lógica no `CsvReaderTest.kt`.
**Status:** 🟢 Implementado

## Componente: Categorias

### Cenário 5: Renomeação Global de Categoria
**Contexto:** Na tela de Gerenciar Categorias.
**Ação:** Editar o nome de uma categoria existente (ex: mudar "Alimentação" para "Restaurantes").
**Resultado Esperado:** Todas as transações que pertenciam à categoria antiga devem ser atualizadas automaticamente para a nova categoria em todo o app.
**Arquivo de Teste:** `FinanceBusinessLogicTest.kt` (método `scenario5_testGlobalCategoryRenaming`)
**Status:** 🟢 Implementado

### Cenário 9: Integridade da Categoria Renda
**Contexto:** Na tela de Gerenciar Categorias.
**Ação:** Tentar editar ou excluir a categoria "Renda".
**Resultado Esperado:** A categoria "Renda" deve exibir um ícone de cadeado e não permitir edição ou exclusão, sendo a única categoria fixa do sistema.
**Arquivo de Teste:** `RendaIntegrityTest.kt`
**Status:** 🟢 Implementado

## Componente: Resumo Financeiro

### Cenário 6: Lógica de Renda vs Gastos
**Contexto:** Na tela de Resumo.
**Ação:** Ter transações na categoria "Renda" e outras em categorias diversas.
**Resultado Esperado:** Apenas o que for "Renda" deve somar em "Entradas". Todo o restante deve somar em "Saídas", independentemente se o valor no CSV original era positivo ou negativo.
**Arquivo de Teste:** `FinanceBusinessLogicTest.kt` (método `scenario6_testIncomeVsExpenseSummaryLogic`)
**Status:** 🟢 Implementado

### Cenário 8: Filtro Temporal por Ano e Mês
**Contexto:** No topo das telas analíticas (Resumo/Gastos/Transações).
**Ação:** Selecionar um Ano específico e depois um Mês.
**Resultado Esperado:** Os dados devem ser filtrados hierarquicamente. Ao selecionar um ano, o resumo deve mostrar o balanço anual. Ao selecionar um mês, deve mostrar o balanço mensal.
**Arquivo de Teste:** `YearlySummaryTest.kt`
**Status:** 🟢 Implementado

## Componente: Importação/Exportação (CSV)

### Cenário 7: Importação com Coluna de Categoria
**Contexto:** Ao importar um arquivo CSV que contém 4 colunas (date, title, amount, category).
**Ação:** Processar o arquivo através do `CsvReader`.
**Resultado Esperado:** As transações devem ser criadas utilizando a categoria especificada na 4ª coluna, ignorando a categorização automática por título.
**Arquivo de Teste:** `CsvReaderTest.kt`
**Status:** 🟢 Implementado

---
*Nota: Adicione novos cenários acima seguindo o formato: Contexto, Ação, Resultado Esperado e Arquivo de Teste.*
